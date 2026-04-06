package com.community.community.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("post_topic")
public class PostTopic {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long postId;
    private Long topicId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
