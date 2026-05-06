package com.community.ai.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.community.ai.dto.WriteRequest;
import com.community.ai.entity.AiFeedback;
import com.community.ai.mapper.AiFeedbackMapper;
import com.community.ai.util.AiCallHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiWriteService {

    private final AiCallHelper aiCallHelper;
    private final AiFeedbackMapper aiFeedbackMapper;

    public String writeArticle(String title, String requirements) {
        try {
            String prompt = String.format(
                    "请根据以下要求写一篇完整的文章：\n标题：%s\n要求：%s\n" +
                    "请输出结构完整、内容丰富的文章。",
                    title, requirements != null ? requirements : ""
            );
            return aiCallHelper.call(prompt);
        } catch (Exception e) {
            log.error("AI写作失败, error={}", e.getMessage(), e);
            throw new RuntimeException("AI写作失败，请稍后重试: " + e.getMessage());
        }
    }

    public String polishContent(String content) {
        try {
            String prompt = String.format(
                    "请对以下内容进行润色和优化，使其更加通顺、专业、易读：\n%s",
                    content
            );
            return aiCallHelper.call(prompt);
        } catch (Exception e) {
            log.error("AI润色失败, error={}", e.getMessage(), e);
            throw new RuntimeException("AI润色失败，请稍后重试: " + e.getMessage());
        }
    }

    public String translateContent(String content, String targetLang) {
        try {
            String prompt = String.format(
                    "请将以下内容翻译成%s，保持原文的语气和风格：\n%s",
                    targetLang != null ? targetLang : "英文", content
            );
            return aiCallHelper.call(prompt);
        } catch (Exception e) {
            log.error("AI翻译失败, error={}", e.getMessage(), e);
            throw new RuntimeException("AI翻译失败，请稍后重试: " + e.getMessage());
        }
    }

    public String summarizeContent(String content) {
        try {
            String prompt = String.format(
                    "请对以下内容进行总结，提取关键信息：\n%s",
                    content
            );
            return aiCallHelper.call(prompt);
        } catch (Exception e) {
            log.error("AI总结失败, error={}", e.getMessage(), e);
            throw new RuntimeException("AI总结失败，请稍后重试: " + e.getMessage());
        }
    }

    public String generateCommunityPost(WriteRequest request) {
        try {
            String prompt = String.format(
                    "作为星云社区专属AI助手，请生成一篇符合【%s】板块调性的帖子。核心主题：【%s】。\n" +
                            "要求：\n" +
                            "1. 语言亲切自然、简洁明了，贴合邻里交流氛围；\n" +
                            "2. 内容合规，不使用低俗用语，不泄露隐私，不夸大宣传；\n" +
                            "3. 包含场景关键信息（交易类含物品/成色/价格，活动类含时间/地点，求助类含需求）；\n" +
                            "4. 字数控制在【%s】；\n" +
                            "5. 风格偏向【%s】；\n" +
                            "请直接输出可发布正文，不要解释。",
                    safe(request.getBoardName(), "生活分享"),
                    safe(request.getKeyword(), "社区日常"),
                    safe(request.getLengthRange(), "100-200字"),
                    safe(request.getStyle(), "亲切")
            );
            return aiCallHelper.call(prompt);
        } catch (Exception e) {
            log.error("社区帖子生成失败, error={}", e.getMessage(), e);
            throw new RuntimeException("AI生成帖子失败，请稍后重试: " + e.getMessage());
        }
    }

    public String completeCommunityPost(WriteRequest request) {
        try {
            String prompt = String.format(
                    "你是星云社区输入补全助手。根据用户当前输入前缀补全5-20字。\n" +
                            "板块：%s\n" +
                            "用户已输入：%s\n" +
                            "要求：语义连贯、贴合社区语气、优先补全时间地点需求等关键信息，不添加无关内容。\n" +
                            "仅输出补全文本，不要加引号或解释。",
                    safe(request.getBoardName(), "社区交流"),
                    safe(request.getPrefix(), "")
            );
            return aiCallHelper.call(prompt);
        } catch (Exception e) {
            log.error("社区帖子补全失败, error={}", e.getMessage(), e);
            throw new RuntimeException("AI补全失败，请稍后重试: " + e.getMessage());
        }
    }

    public String continueCommunityPost(WriteRequest request) {
        try {
            String prompt = String.format(
                    "基于以下已写内容，为星云社区【%s】板块续写50-100字，要求主题一致、语气连贯、可直接衔接。\n" +
                            "已写内容：\n%s\n" +
                            "续写要求：风格【%s】，如缺少关键要素（时间/地点/价格/需求）请合理补充。\n" +
                            "仅输出续写段落。",
                    safe(request.getBoardName(), "生活分享"),
                    safe(request.getContent(), ""),
                    safe(request.getStyle(), "亲切")
            );
            return aiCallHelper.call(prompt);
        } catch (Exception e) {
            log.error("社区帖子续写失败, error={}", e.getMessage(), e);
            throw new RuntimeException("AI续写失败，请稍后重试: " + e.getMessage());
        }
    }

    public String polishCommunityPost(WriteRequest request) {
        try {
            String prompt = String.format(
                    "请对以下星云社区帖子进行润色优化。\n" +
                            "板块：%s\n" +
                            "优化方向：%s\n" +
                            "风格：%s\n" +
                            "段落：%s\n" +
                            "内容：\n%s\n" +
                            "要求：纠正病句错别字、优化流畅度与可读性、保持原意、确保合规，输出润色后正文。",
                    safe(request.getBoardName(), "社区交流"),
                    safe(request.getPolishMode(), "一键润色"),
                    safe(request.getStyle(), "亲切"),
                    request.getParagraphNo() == null ? "全部" : String.valueOf(request.getParagraphNo()),
                    safe(request.getContent(), "")
            );
            return aiCallHelper.call(prompt);
        } catch (Exception e) {
            log.error("社区帖子润色失败, error={}", e.getMessage(), e);
            throw new RuntimeException("AI润色失败，请稍后重试: " + e.getMessage());
        }
    }

    public String summarizePostForReading(WriteRequest request) {
        try {
            String prompt = String.format(
                    "作为星云社区AI助手，请对帖子生成30-50字摘要。\n" +
                            "标题：%s\n" +
                            "内容：\n%s\n" +
                            "要求：提炼主题和关键诉求，交易类需含物品与价格，活动类需含时间地点，语气中立简洁。\n" +
                            "仅输出摘要。",
                    safe(request.getPostTitle(), "无标题"),
                    safe(request.getContent(), "")
            );
            return aiCallHelper.call(prompt);
        } catch (Exception e) {
            log.error("帖子摘要生成失败, error={}", e.getMessage(), e);
            throw new RuntimeException("AI摘要失败，请稍后重试: " + e.getMessage());
        }
    }

    public String extractPostKeyPoints(WriteRequest request) {
        try {
            String prompt = String.format(
                    "请解读以下星云社区帖子重点，按1-3条列出关键点。\n标题：%s\n内容：\n%s\n" +
                            "要求：突出关键信息，不复述冗余，不加无关建议。",
                    safe(request.getPostTitle(), "无标题"),
                    safe(request.getContent(), "")
            );
            return aiCallHelper.call(prompt);
        } catch (Exception e) {
            log.error("帖子重点解读失败, error={}", e.getMessage(), e);
            throw new RuntimeException("AI解读失败，请稍后重试: " + e.getMessage());
        }
    }

    public String suggestPostReply(WriteRequest request) {
        try {
            String prompt = String.format(
                    "请为以下星云社区帖子生成3条简短回复建议，每条20-40字，语气友善、可直接发送。\n标题：%s\n内容：\n%s\n" +
                            "要求：贴合帖子语境（求助/交易/活动/分享），避免模板化和无关内容。",
                    safe(request.getPostTitle(), "无标题"),
                    safe(request.getContent(), "")
            );
            return aiCallHelper.call(prompt);
        } catch (Exception e) {
            log.error("帖子回复建议生成失败, error={}", e.getMessage(), e);
            throw new RuntimeException("AI回复建议失败，请稍后重试: " + e.getMessage());
        }
    }

    public void submitFeedback(WriteRequest request) {
        String type = safe(request.getFeedbackType(), "unknown");
        String scene = safe(request.getScene(), "unknown");
        String op = safe(request.getOperation(), "unknown");
        String reason = safe(request.getFeedbackReason(), "");
        AiFeedback feedback = new AiFeedback();
        feedback.setFeedbackType(type);
        feedback.setFeedbackReason(reason);
        feedback.setScene(scene);
        feedback.setOperation(op);
        feedback.setUserId(0L);
        feedback.setCreateTime(LocalDateTime.now());
        aiFeedbackMapper.insert(feedback);
        log.info("ai feedback received, type={}, scene={}, operation={}, reason={}", type, scene, op, reason);
    }

    public Map<String, Object> feedbackStats() {
        Map<String, Object> stats = new HashMap<>();
        long total = aiFeedbackMapper.selectCount(new QueryWrapper<>());
        long likes = aiFeedbackMapper.selectCount(new QueryWrapper<AiFeedback>().eq("feedback_type", "like"));
        long dislikes = aiFeedbackMapper.selectCount(new QueryWrapper<AiFeedback>().eq("feedback_type", "dislike"));
        long today = aiFeedbackMapper.selectCount(new QueryWrapper<AiFeedback>().ge("create_time", LocalDate.now().atStartOfDay()));
        double likeRate = total == 0 ? 0D : (likes * 100.0D / total);
        stats.put("totalFeedback", total);
        stats.put("likeCount", likes);
        stats.put("dislikeCount", dislikes);
        stats.put("todayFeedback", today);
        stats.put("likeRate", Math.round(likeRate * 100.0D) / 100.0D);

        List<Map<String, Object>> sceneRows = aiFeedbackMapper.selectMaps(
                new QueryWrapper<AiFeedback>()
                        .select("scene, count(*) as cnt")
                        .groupBy("scene")
                        .orderByDesc("cnt")
                        .last("limit 8")
        );
        List<Map<String, Object>> reasonRows = aiFeedbackMapper.selectMaps(
                new QueryWrapper<AiFeedback>()
                        .select("feedback_reason as reason, count(*) as cnt")
                        .eq("feedback_type", "dislike")
                        .isNotNull("feedback_reason")
                        .ne("feedback_reason", "")
                        .groupBy("feedback_reason")
                        .orderByDesc("cnt")
                        .last("limit 8")
        );
        stats.put("sceneTop", sceneRows);
        stats.put("dislikeReasonTop", reasonRows);
        return stats;
    }

    private String safe(String v, String dft) {
        return (v == null || v.trim().isEmpty()) ? dft : v.trim();
    }
}
