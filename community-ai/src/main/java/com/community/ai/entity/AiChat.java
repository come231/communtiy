package com.community.ai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ai_chat")
public class AiChat {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String sessionId;

    /** 角色: user/assistant */
    private String role;

    private String content;

    /** AI模型名 */
    private String model;

    /** token数 */
    private Integer tokens;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
