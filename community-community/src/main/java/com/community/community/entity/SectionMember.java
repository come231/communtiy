package com.community.community.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("section_member")
public class SectionMember {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("section_id")
    private Long sectionId;
    
    @TableField("user_id")
    private Long userId;
    
    private Integer role; // 0-成员，1-管理员，2-版主
    
    @TableField("join_time")
    private LocalDateTime joinTime;
    
    private Integer status; // 1-正常，0-禁用
}