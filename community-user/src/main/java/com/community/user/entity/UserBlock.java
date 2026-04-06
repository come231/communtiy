package com.community.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_block")
public class UserBlock {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long blockId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
