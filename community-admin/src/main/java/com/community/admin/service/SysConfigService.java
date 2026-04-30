package com.community.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.community.admin.entity.SysConfig;
import com.community.admin.mapper.security.SysConfigMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class SysConfigService {

    private final SysConfigMapper sysConfigMapper;

    public SysConfigService(SysConfigMapper sysConfigMapper) {
        this.sysConfigMapper = sysConfigMapper;
    }

    public List<SysConfig> getConfigList() {
        try {
            List<SysConfig> list = sysConfigMapper.selectList(new LambdaQueryWrapper<SysConfig>()
                    .orderByAsc(SysConfig::getId));
            list.forEach(this::enrichDisplayFields);
            return list;
        } catch (Exception e) {
            log.error("加载系统配置失败", e);
            return Collections.emptyList();
        }
    }

    public List<SysConfig> getConfigListByGroup(String groupKey) {
        if (!StringUtils.hasText(groupKey)) {
            return getConfigList();
        }
        List<SysConfig> list = getConfigList();
        return list.stream()
                .filter(c -> groupKey.equals(c.getGroupKey()))
                .toList();
    }

    /**
     * 当库中 group_key / config_type 为空时，按 config_key 推断；并补全前端 configName、options。
     */
    private void enrichDisplayFields(SysConfig c) {
        if (!StringUtils.hasText(c.getDescription()) && StringUtils.hasText(c.getConfigName())) {
            c.setDescription(c.getConfigName());
        }
        String key = c.getConfigKey() != null ? c.getConfigKey() : "";
        String val = c.getConfigValue();
        if (!StringUtils.hasText(c.getConfigType())) {
            if (val != null && ("true".equalsIgnoreCase(val.trim()) || "false".equalsIgnoreCase(val.trim()))) {
                c.setConfigType("switch");
            } else if (val != null && val.trim().matches("^-?\\d+(\\.\\d+)?$")) {
                c.setConfigType("number");
            } else {
                c.setConfigType("input");
            }
        }
        if (!StringUtils.hasText(c.getGroupKey())) {
            if (key.startsWith("site.") || key.startsWith("site_") || "ai_rate_limit".equals(key)) {
                c.setGroupKey("basic");
            } else if (key.startsWith("post.") || key.startsWith("comment.") || key.startsWith("post_") || key.startsWith("comment_")) {
                c.setGroupKey("content");
            } else if (key.contains("audit") || key.contains("register") || key.startsWith("allow_")) {
                c.setGroupKey("security");
            } else if (key.startsWith("ai_")) {
                c.setGroupKey("ai");
            } else if (key.startsWith("theme_") || key.startsWith("page_") || key.startsWith("hot_topic")) {
                c.setGroupKey("ui");
            } else if (key.contains("upload") || key.startsWith("image_")) {
                c.setGroupKey("upload");
            } else if (key.startsWith("admin_") || key.contains("log_level") || key.startsWith("refresh_admin")) {
                c.setGroupKey("system");
            } else if (key.startsWith("sensitive.") || key.contains("sensitive")) {
                c.setGroupKey("security");
            } else {
                c.setGroupKey("basic");
            }
        }
        if (c.getOptions() == null) {
            c.setOptions("");
        }
        c.setConfigName(StringUtils.hasText(c.getDescription()) ? c.getDescription() : key);
    }

    @Transactional
    public boolean updateConfig(String idOrKey, String configValue) {
        try {
            long id = Long.parseLong(idOrKey);
            SysConfig sysConfig = sysConfigMapper.selectById(id);
            if (sysConfig == null) {
                throw new RuntimeException("配置项不存在");
            }
            sysConfig.setConfigValue(configValue);
            sysConfig.setUpdateTime(LocalDateTime.now());
            return sysConfigMapper.updateById(sysConfig) > 0;
        } catch (NumberFormatException e) {
            throw new RuntimeException("配置项不存在或非法 ID");
        }
    }

    public SysConfig getConfigByKey(String key) {
        if (!StringUtils.hasText(key)) {
            return null;
        }
        SysConfig one = sysConfigMapper.selectOne(new LambdaQueryWrapper<SysConfig>()
                .eq(SysConfig::getConfigKey, key));
        if (one != null) {
            enrichDisplayFields(one);
        }
        return one;
    }

    @Transactional
    public SysConfig createConfig(SysConfig config) {
        if (!StringUtils.hasText(config.getDescription()) && StringUtils.hasText(config.getConfigName())) {
            config.setDescription(config.getConfigName());
        }
        config.setCreateTime(LocalDateTime.now());
        config.setUpdateTime(LocalDateTime.now());
        if (config.getStatus() == null) {
            config.setStatus(1);
        }
        if (!StringUtils.hasText(config.getConfigType())) {
            config.setConfigType("string");
        }
        if (config.getGroupKey() == null) {
            config.setGroupKey("");
        }
        enrichDisplayFields(config);
        sysConfigMapper.insert(config);
        return config;
    }

    @Transactional
    public boolean deleteConfig(String idOrKey) {
        try {
            long id = Long.parseLong(idOrKey);
            return sysConfigMapper.deleteById(id) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
