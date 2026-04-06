package com.community.community.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("section")
public class Section {
    @TableId(value = "section_id", type = IdType.AUTO)
    private Long sectionId;
    private String name;
    private String cover;
    private String intro;
    private Integer sort;
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    private Long parentId;
    private Integer postCount;
    private Integer postPerm;
    private Long moderatorId;
}
