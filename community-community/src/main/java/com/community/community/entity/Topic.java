package com.community.community.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("topic")
public class Topic {
    @TableId(value = "topic_id", type = IdType.AUTO)
    private Long topicId;
    private String name;
    private String cover;
    private String description;
    private Integer postCount;
    private Integer followCount;
    private Integer isHot;
    private Integer isRecommend;
    private Integer sort;
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
