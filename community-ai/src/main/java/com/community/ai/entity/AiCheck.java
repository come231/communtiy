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

    private String sensitiveWords;

    /** AI建议 */
    private String aiSuggestion;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
