package com.community.community.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("section_category")
public class SectionCategory {
    @TableId(value = "category_id", type = IdType.AUTO)
    private Long categoryId;

    private String name;

    private Integer sort;

    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
