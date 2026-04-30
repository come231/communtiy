package com.community.common.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 帖子 AI 审核规则（与 community_ai.post_review_rule 对应，供管理端 Feign 序列化）
 */
@Data
public class PostReviewRuleDTO implements Serializable {
    private Long id;
    private String ruleName;
    private String ruleText;
    private Integer sortOrder;
    /** 1启用 0禁用 */
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
