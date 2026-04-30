package com.community.common.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AI 内容审核记录（管理端列表，与 ai_check 表对应）
 */
@Data
public class AiCheckRecordVO implements Serializable {

    private Long id;
    private Long userId;
    /** 1帖子 2评论 3私信 */
    private Integer contentType;
    /** 关联帖子/内容 ID，发帖绑定后才有 */
    private Long contentId;
    private String originalContent;
    /**
     * 0 正常 1 敏感/转人工 2 违规拦截
     */
    private Integer checkResult;
    private Double violationPercent;
    private String sensitiveWords;
    private String aiSuggestion;
    private LocalDateTime createTime;

    // ========== 人机回环数据飞轮字段 ==========

    /**
     * 人工复核标记：0 未复核 1 确认AI判定正确 2 人工纠正为违规 3 人工纠正为正常
     */
    private Integer manualReviewStatus;

    /**
     * 人工复核标签：如"严重违规-危化DIY"、"误判-正常讨论"等
     */
    private String manualLabel;

    /**
     * 人工复核备注
     */
    private String manualRemark;

    /**
     * 复核管理员ID
     */
    private Long reviewerId;

    /**
     * 复核时间
     */
    private LocalDateTime reviewTime;

    /**
     * 是否已加入向量样本库：0 未加入 1 已加入
     */
    private Integer inVectorStore;
}
