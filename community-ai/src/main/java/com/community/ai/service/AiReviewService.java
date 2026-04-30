package com.community.ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.ai.entity.AiCheck;
import com.community.ai.mapper.AiCheckMapper;
import com.community.ai.util.AiCallHelper;
import com.community.common.dto.AiCheckRecordVO;
import com.community.common.result.PageResult;
import com.community.common.util.BuiltinContentRiskScanner;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiReviewService {

    /**
     * 审核专用 system 角色：与用户消息中的《平台审核规则》绑定，强制逐条适用（规则来自 DB 启用项或内置默认，见 {@link PostReviewRuleService#buildRulesTextForPrompt()}）。
     */
    private static final String SYSTEM_CONTENT_REVIEW = """
            你是平台内容安全审核模型。用户消息中给出的《平台审核规则》是本次判定的唯一标准清单，你必须逐条对照，不可仅凭「语气中性」「无脏字」放行。
            若待审核文本违反清单中任一条（含宣扬、教唆、步骤化描述违规意图），violationPercent 必须大于 0，且 suggestion 须用中文指出对应规则的主题或序号（如「暴力与危险」「第2条」）。
            禁止在明显触犯规则时输出 violationPercent=0，或在 suggestion 中使用「内容正常」「无违规」「未违规」等放行表述。
            输出仅限一个 JSON 对象，格式：{"violationPercent":数字,"suggestion":"简短说明"}，不要其它文字。""";

    /** 大于 0 且小于等于该值 → 人工审核；大于该值 → 拦截 */
    private static final double MANUAL_REVIEW_MAX_PERCENT = 30.0;

    private static final Pattern VIOLATION_PERCENT_PATTERN =
            Pattern.compile("\"violationPercent\"\\s*:\\s*([0-9]+(?:\\.[0-9]+)?)", Pattern.CASE_INSENSITIVE);

    private final AiCallHelper aiCallHelper;
    private final AiCheckMapper aiCheckMapper;
    private final PostReviewRuleService postReviewRuleService;
    private final SemanticRiskDetector semanticRiskDetector; // 三层语义检测
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 内容审核主入口（三层防护）
     * 第1层：内置规则（性能最好）
     * 第2层：向量语义相似度（捕获换说法的违规）
     * 第3层：AI大模型（兜底）
     */
    public AiCheck checkContent(Long userId, String content, Integer type) {
        AiCheck check = new AiCheck();
        check.setUserId(userId);
        check.setContentType(type);
        check.setOriginalContent(content);
        check.setCreateTime(LocalDateTime.now());

        // ===== 三层检测（内置规则 + 向量语义）=====
        SemanticRiskDetector.DetectionResult precheck = semanticRiskDetector.detect(content);

        // 记录检测日志
        log.debug("内容审核三层检测日志: {}", String.join("; ", precheck.detectionLog()));

        // 设置命中规则
        if (!precheck.matchedRules().isEmpty()) {
            check.setSensitiveWords(String.join("；", precheck.matchedRules()));
        }

        // 前置层已确定结果（拦截或转人工）
        if (precheck.checkResult() == 2) {
            // 严重违规直接拦截
            check.setCheckResult(2);
            check.setViolationPercent(precheck.violationPercent());
            check.setAiSuggestion(precheck.suggestion());
            aiCheckMapper.insert(check);
            log.info("三层检测拦截: userId={}, reason={}", userId, precheck.suggestion());
            return check;
        } else if (precheck.checkResult() == 1) {
            // 可疑转人工，但仍给AI层一个机会（可能AI认为更严重）
            // 继续执行，但带着前置结果
        }

        // AI未启用时的处理
        if (!aiCallHelper.isEnabled()) {
            int cr = precheck.checkResult(); // 0或1
            check.setCheckResult(cr);
            if (cr == 0) {
                check.setAiSuggestion("未配置 AI，三层检测无风险命中；仍受敏感词与内置规则约束");
            } else {
                check.setViolationPercent(precheck.violationPercent());
                check.setAiSuggestion(precheck.suggestion() != null ? precheck.suggestion() : "未配置 AI，已根据前置检测转人工审核");
            }
            aiCheckMapper.insert(check);
            return check;
        }

        // ===== 第3层：AI大模型终审 =====
        try {
            String rulesBlock = postReviewRuleService.buildRulesTextForPrompt();
            String userPrompt = buildReviewUserPrompt(content, rulesBlock);
            String aiResponse = aiCallHelper.callForContentReview(SYSTEM_CONTENT_REVIEW, userPrompt);
            Parsed parsed = parseAiJson(aiResponse);

            // 取三层结果的最大值（最严格）
            int effective = Math.max(parsed.result, precheck.checkResult());
            Double vp = parsed.violationPercent;

            // 前置层已标记为可疑，AI给0分也不放行
            if (precheck.checkResult() == 1 && effective == 0) {
                effective = 1; // 强制转人工
                vp = Math.max(vp != null ? vp : 0, 15.0); // 至少15%
            }

            if (effective > parsed.result) {
                if (vp == null) {
                    vp = effective >= 2 ? 80.0 : 22.0;
                } else {
                    vp = Math.max(vp, effective >= 2 ? 80.0 : 22.0);
                }
            }

            check.setViolationPercent(vp);
            check.setCheckResult(effective);

            String sug = parsed.suggestion != null ? parsed.suggestion : labelFor(parsed.result, parsed.violationPercent);
            if (effective > parsed.result) {
                sug = (sug != null ? sug + "；" : "") + "已根据前置风险检测提高处置等级";
            }
            check.setAiSuggestion(sug != null ? sug : labelFor(effective, vp));

        } catch (Exception e) {
            log.error("AI审核失败, 转人工审核, error={}", e.getMessage(), e);
            // AI异常时，前置层结果兜底
            int cr = Math.max(1, precheck.checkResult());
            check.setCheckResult(cr);
            check.setViolationPercent(cr >= 2 ? 85.0 : 30.0);
            check.setAiSuggestion("AI 调用失败或超时，已转人工审核（勿默认放行）");
        }

        aiCheckMapper.insert(check);
        return check;
    }

    /**
     * 发帖成功后写入 contentId，供管理端「AI 审核记录」与帖子关联
     */
    public void bindCheckToPost(Long checkId, Long postId) {
        if (checkId == null || postId == null) {
            return;
        }
        AiCheck row = aiCheckMapper.selectById(checkId);
        if (row != null) {
            row.setContentId(postId);
            aiCheckMapper.updateById(row);
        }
    }

    public PageResult<AiCheckRecordVO> pageCheckRecords(
            int pageNum, int pageSize, Long userId, Long postId, Integer checkResult) {
        Page<AiCheck> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<AiCheck> w = new LambdaQueryWrapper<>();
        if (userId != null) {
            w.eq(AiCheck::getUserId, userId);
        }
        if (postId != null) {
            w.eq(AiCheck::getContentId, postId);
        }
        if (checkResult != null) {
            w.eq(AiCheck::getCheckResult, checkResult);
        }
        w.orderByDesc(AiCheck::getCreateTime);
        Page<AiCheck> result = aiCheckMapper.selectPage(page, w);
        java.util.List<AiCheckRecordVO> list = result.getRecords().stream()
                .map(this::toCheckRecordVo)
                .toList();
        return new PageResult<>(list, result.getTotal(), result.getSize(), result.getCurrent());
    }

    private AiCheckRecordVO toCheckRecordVo(AiCheck e) {
        AiCheckRecordVO vo = new AiCheckRecordVO();
        BeanUtils.copyProperties(e, vo);
        return vo;
    }

    /**
     * 用户消息：载入规则全文 + 输出格式 + 待审文本（规则由 {@link PostReviewRuleService#buildRulesTextForPrompt()} 保证非空）。
     */
    private String buildReviewUserPrompt(String content, String rulesBlock) {
        String rulesSection = (rulesBlock != null && !rulesBlock.isBlank())
                ? rulesBlock
                : DefaultPostReviewRulesFallback.PROMPT_BLOCK;
        String safe = content == null ? "" : content;
        return """
                以下为本次生效的《平台审核规则》（来自数据库启用配置；若无启用项则为平台内置默认，具有同等效力）。你必须逐条适用：

                %s

                ---
                输出要求（必须严格遵守）：
                1. 仅输出一个 JSON 对象，不要任何其它文字。
                2. 格式：{"violationPercent":数字,"suggestion":"简短说明"}
                3. violationPercent 为 0～100（可含小数）：与上述规则完全无关且无风险时为 0；触犯任一条规则则必须大于 0。大于 0 且小于等于 30 → 转人工审核；大于 30 → 拦截发帖。
                4. suggestion：若违规须写明触犯的规则主题或序号（与上文【】内类别或序号对应）；若确实未触犯任何一条，可简要写「未触犯上述规则列表中的条款」。
                5. 硬性底线（与上表同时适用）：教唆在校园/职场等场景通过排挤、抱团施压、肢体威慑、欺负弱小、强硬压制他人以获取话语权等内容，violationPercent 至少为 20，且不得使用「内容正常」等放行表述；露骨教唆或步骤化欺凌的，至少为 45。
                6. 教唆利用常见材料「简易制作」危险物品/装置、讲解危险材料「组合使用」、私自改造危险器具、违规操作高风险物品、或宣称不顾安全隐患即可「做出/制成危险装置」的，视为严重触犯《暴力与危险》，violationPercent 至少为 50。

                待审核文本：
                %s
                """.formatted(rulesSection, safe);
    }

    private Parsed parseAiJson(String raw) {
        if (raw == null || raw.isBlank()) {
            return new Parsed(1, "AI 无输出，已转人工", null);
        }
        Parsed p = new Parsed(0, null, null);
        try {
            int start = raw.indexOf('{');
            int end = raw.lastIndexOf('}');
            if (start >= 0 && end > start) {
                JsonNode n = objectMapper.readTree(raw.substring(start, end + 1));
                if (n.has("violationPercent") && !n.get("violationPercent").isNull()) {
                    double vp = clampPercent(n.get("violationPercent").asDouble());
                    p.violationPercent = vp;
                    p.result = mapPercentToResult(vp);
                    if (n.hasNonNull("suggestion")) {
                        p.suggestion = n.get("suggestion").asText();
                    }
                    return p;
                }
                if (n.hasNonNull("result")) {
                    p.result = Math.max(0, Math.min(2, n.get("result").asInt()));
                    if (n.hasNonNull("suggestion")) {
                        p.suggestion = n.get("suggestion").asText();
                    }
                    return p;
                }
            }
        } catch (Exception e) {
            log.debug("未能按 JSON 解析 AI 输出，回退启发式: {}", e.getMessage());
        }
        Matcher m = VIOLATION_PERCENT_PATTERN.matcher(raw);
        if (m.find()) {
            try {
                double vp = clampPercent(Double.parseDouble(m.group(1)));
                p.violationPercent = vp;
                p.result = mapPercentToResult(vp);
                return p;
            } catch (NumberFormatException ignored) {
                // fall through
            }
        }
        String t = raw.toLowerCase();
        if (t.contains("\"result\":2") || t.contains("\"result\": 2") || raw.contains("违规")) {
            p.result = 2;
        } else if (t.contains("\"result\":1") || t.contains("\"result\": 1") || raw.contains("敏感")) {
            p.result = 1;
        } else if (raw.length() > 30) {
            p.result = 1;
            p.suggestion = "AI 输出无法解析为有效 JSON，已转人工";
        }
        return p;
    }

    private static double clampPercent(double vp) {
        if (vp < 0) {
            return 0;
        }
        if (vp > 100) {
            return 100;
        }
        return vp;
    }

    private static int mapPercentToResult(double violationPercent) {
        if (violationPercent <= 0) {
            return 0;
        }
        if (violationPercent <= MANUAL_REVIEW_MAX_PERCENT) {
            return 1;
        }
        return 2;
    }

    private String labelFor(int r, Double vp) {
        if (vp != null) {
            return switch (r) {
                case 2 -> "帖子违规（综合违规程度约 %.1f%%）".formatted(vp);
                case 1 -> "帖子敏感，转人工审核（综合违规程度约 %.1f%%）".formatted(vp);
                default -> "内容正常";
            };
        }
        return switch (r) {
            case 2 -> "帖子违规";
            case 1 -> "帖子敏感，转人工审核";
            default -> "内容正常";
        };
    }

    public List<AiCheck> batchCheck(List<String> contents) {
        List<AiCheck> results = new ArrayList<>();
        for (String content : contents) {
            results.add(checkContent(null, content, 1));
        }
        return results;
    }

    private static final class Parsed {
        int result;
        String suggestion;
        Double violationPercent;

        Parsed(int result, String suggestion, Double violationPercent) {
            this.result = result;
            this.suggestion = suggestion;
            this.violationPercent = violationPercent;
        }
    }
}
