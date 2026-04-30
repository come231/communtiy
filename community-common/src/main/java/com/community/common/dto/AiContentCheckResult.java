package com.community.common.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * AI/内容审核服务返回的简要结果（与 community-ai 中 AiCheck 的 JSON 字段一致，供 Feign 反序列化）
 */
@Data
public class AiContentCheckResult implements Serializable {
    private Long id;
    private Integer checkResult;
    /** AI 输出的综合违规程度 0～100，可为空（兼容旧响应） */
    private Double violationPercent;
    private String sensitiveWords;
    private String aiSuggestion;
}
