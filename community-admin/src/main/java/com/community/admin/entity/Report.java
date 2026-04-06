package com.community.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("report")
public class Report {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long reporterId;
    private Long targetId;
    private Integer targetType;
    private String reason;
    private Integer status;
    private Long handlerId;
    private String handleResult;
    private LocalDateTime createTime;
    private LocalDateTime handleTime;
}
