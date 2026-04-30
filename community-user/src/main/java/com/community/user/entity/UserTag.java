package com.community.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user_tag")
public class UserTag {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String tagName;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
