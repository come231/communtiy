package com.community.ai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ai_check")
public class AiCheck {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    /** 内容类型: 1帖子 2评论 3私信 */
    private Integer contentType;

    private Long contentId;

    private String originalContent;

    /** 审核结果: 0正常 1敏感 2违规 */
    private Integer checkResult;

    /** AI 综合违规程度 0～100，与 check_result 分档一致时由模型输出 */
    private Double violationPercent;

    private String sensitiveWords;

    /** AI建议 */
    private String aiSuggestion;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    // ========== 人机回环数据飞轮字段 ==========

    /**
     * 人工复核标记：0 未复核 1 确认AI判定正确 2 人工纠正为违规 3 人工纠正为正常
     * 用于数据飞轮：标记为2的记录会自动进入向量样本库
     */
    private Integer manualReviewStatus;

    /**
     * 人工复核标签：如"严重违规-危化DIY"、"误判-正常讨论"等
     * 用于样本分类和后续分析
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
     * 避免重复计算embedding
     */
    @TableField("in_vector_store")
    private Integer inVectorStore;
}
