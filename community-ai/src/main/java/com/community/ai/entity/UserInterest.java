package com.community.ai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_interest")
public class UserInterest {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    /** 兴趣标签 */
    private String tag;

    /** 权重 */
    private Double weight;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
