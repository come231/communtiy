package com.community.ai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ai_summary")
public class AiSummary {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long targetId;

    /** 目标类型: 1帖子 2文章 */
    private Integer targetType;

    private String originalContent;

    private String summaryText;

    /** 关键词JSON */
    private String keywords;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
