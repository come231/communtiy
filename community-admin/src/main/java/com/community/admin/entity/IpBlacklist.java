package com.community.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ip_blacklist")
public class IpBlacklist {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String ip;
    private String reason;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime expireTime;
}
