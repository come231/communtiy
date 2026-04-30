package com.community.ai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("post_review_rule")
public class PostReviewRule {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String ruleName;

    private String ruleText;

    private Integer sortOrder;

    /** 1启用 0禁用 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
