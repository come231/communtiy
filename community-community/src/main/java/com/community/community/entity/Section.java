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
    /**
     * 1=仅版主与已加入成员可发帖；0=开放。
     * 未执行 database/section_join_and_member_only.sql 时须保持 exist=false，否则会报 Unknown column。
     * 迁移完成后可去掉 {@code exist = false}，以便持久化该列。
     */
    @TableField(value = "member_only_post", exist = false)
    private Integer memberOnlyPost;
    private Long moderatorId;

    /**
     * 板块分类ID，关联section_category表
     */
    private Long categoryId;

    /**
     * 板块分类名称（自定义分类时使用）
     */
    private String categoryName;
}
