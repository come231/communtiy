package com.community.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("rate_limit_record")
public class RateLimitRecord {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String ip;
    private String endpoint;
    private Integer count;
    private LocalDateTime windowStart;
}
