package com.community.ai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ai_topic")
public class AiTopic {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String topicName;

    private String description;

    /** 帖子数量 */
    private Integer postCount;

    /** 状态: 1正常 0禁用 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
