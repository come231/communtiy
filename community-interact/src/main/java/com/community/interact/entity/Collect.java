package com.community.interact.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("collect")
public class Collect {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long postId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    private Long folderId;
    @TableField(value = "is_hidden", exist = false)
    private Integer isHidden;
}
