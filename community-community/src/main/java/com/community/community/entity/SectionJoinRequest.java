package com.community.community.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("section_join_request")
public class SectionJoinRequest {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("section_id")
    private Long sectionId;

    @TableField("user_id")
    private Long userId;

    /** 0待审 1通过 2拒绝 */
    private Integer status;

    @TableField("create_time")
    private LocalDateTime createTime;
}
