package com.community.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community.admin.entity.SysConfig;
import com.community.admin.mapper.SysConfigMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SysConfigService extends ServiceImpl<SysConfigMapper, SysConfig> {

    private final SysConfigMapper sysConfigMapper;

    public List<SysConfig> getConfigList() {
        return sysConfigMapper.selectList(new LambdaQueryWrapper<SysConfig>()
                .orderByAsc(SysConfig::getId));
    }

    @Transactional
    public boolean updateConfig(String id, String configValue) {
        SysConfig sysConfig = sysConfigMapper.selectById(id);
        if (sysConfig == null) {
            throw new RuntimeException("配置项不存在");
        }

        sysConfig.setConfigValue(configValue);
        sysConfig.setUpdateTime(LocalDateTime.now());

        return sysConfigMapper.updateById(sysConfig) > 0;
    }

    public SysConfig getConfigByKey(String key) {
        if (!StringUtils.hasText(key)) {
            return null;
        }
        return sysConfigMapper.selectById(key);
    }
}
