package com.community.community.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_topic")
public class UserTopic {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long topicId;
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
