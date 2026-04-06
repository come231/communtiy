package com.community.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_admin_log")
public class SysAdminLog {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long adminId;
    private String adminName;
    private String module;
    private String operation;
    private String method;
    private String url;
    private String ip;
    private String params;
    private Long time;
    private Integer result;
    private LocalDateTime createTime;
}
