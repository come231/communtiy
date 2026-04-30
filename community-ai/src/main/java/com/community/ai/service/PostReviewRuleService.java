package com.community.ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.community.ai.entity.PostReviewRule;
import com.community.ai.mapper.PostReviewRuleMapper;
import com.community.common.dto.PostReviewRuleDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostReviewRuleService {

    private final PostReviewRuleMapper postReviewRuleMapper;

    /**
     * 构建拼入 AI 提示词的规则正文（仅 status=1 且 rule_text 非空，按 sort_order、id 排序）。
     * 若库中无任何可用启用规则，则使用 {@link DefaultPostReviewRulesFallback}，避免模型仅看到「公序良俗」一句而随意放行。
     */
    public String buildRulesTextForPrompt() {
        List<PostReviewRule> list = postReviewRuleMapper.selectList(
                new LambdaQueryWrapper<PostReviewRule>()
                        .eq(PostReviewRule::getStatus, 1)
                        .orderByAsc(PostReviewRule::getSortOrder)
                        .orderByAsc(PostReviewRule::getId));
        StringBuilder sb = new StringBuilder();
        int i = 1;
        int used = 0;
        for (PostReviewRule r : list) {
            if (!StringUtils.hasText(r.getRuleText())) {
                continue;
            }
            used++;
            if (StringUtils.hasText(r.getRuleName())) {
                sb.append(i++).append(". 【").append(r.getRuleName().trim()).append("】")
                        .append(r.getRuleText().trim()).append("\n");
            } else {
                sb.append(i++).append(". ").append(r.getRuleText().trim()).append("\n");
            }
        }
        if (used > 0) {
            log.info("AI 审核提示词：已从 post_review_rule 加载 {} 条启用规则", used);
            return sb.toString().trim();
        }
        log.warn("AI 审核提示词：post_review_rule 无启用规则，已回退为内置默认规则集（与 database/post_review_rule.sql 模板一致）");
        return DefaultPostReviewRulesFallback.PROMPT_BLOCK;
    }

    public List<PostReviewRuleDTO> listAll() {
        return postReviewRuleMapper.selectList(
                        new LambdaQueryWrapper<PostReviewRule>()
                                .orderByAsc(PostReviewRule::getSortOrder)
                                .orderByAsc(PostReviewRule::getId))
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public PostReviewRuleDTO create(PostReviewRuleDTO dto) {
        PostReviewRule e = new PostReviewRule();
        e.setRuleName(dto.getRuleName());
        e.setRuleText(dto.getRuleText() != null ? dto.getRuleText() : "");
        e.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        e.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        postReviewRuleMapper.insert(e);
        return toDto(postReviewRuleMapper.selectById(e.getId()));
    }

    public PostReviewRuleDTO update(Long id, PostReviewRuleDTO dto) {
        PostReviewRule e = postReviewRuleMapper.selectById(id);
        if (e == null) {
            return null;
        }
        if (dto.getRuleName() != null) {
            e.setRuleName(dto.getRuleName());
        }
        if (dto.getRuleText() != null) {
            e.setRuleText(dto.getRuleText());
        }
        if (dto.getSortOrder() != null) {
            e.setSortOrder(dto.getSortOrder());
        }
        if (dto.getStatus() != null) {
            e.setStatus(dto.getStatus());
        }
        postReviewRuleMapper.updateById(e);
        return toDto(postReviewRuleMapper.selectById(id));
    }

    public boolean delete(Long id) {
        return postReviewRuleMapper.deleteById(id) > 0;
    }

    private PostReviewRuleDTO toDto(PostReviewRule e) {
        if (e == null) {
            return null;
        }
        PostReviewRuleDTO d = new PostReviewRuleDTO();
        BeanUtils.copyProperties(e, d);
        return d;
    }
}
