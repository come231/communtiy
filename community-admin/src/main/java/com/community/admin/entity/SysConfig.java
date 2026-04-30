package com.community.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 与 database/community_security.sql 中 sys_config 对齐（主数据源 community_security）。
 */
@Data
@TableName("sys_config")
public class SysConfig {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("config_key")
    private String configKey;

    @TableField("config_value")
    private String configValue;

    @TableField("config_type")
    private String configType;

    @TableField("description")
    private String description;

    @TableField("group_key")
    private String groupKey;

    /** 库中无此列时由服务层填充，供前端下拉等控件 */
    @TableField(value = "options", exist = false)
    private String options;

    @TableField("status")
    private Integer status;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    /**
     * 非表字段：与前端展示字段 configName 对齐，由 {@link com.community.admin.service.SysConfigService} 从 description 填充。
     */
    @TableField(exist = false)
    private String configName;
}
