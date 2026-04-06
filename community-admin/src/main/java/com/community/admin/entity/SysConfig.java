package com.community.admin.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_config")
public class SysConfig {

    @TableId
    private String id;
    private String configValue;
    private String configName;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
