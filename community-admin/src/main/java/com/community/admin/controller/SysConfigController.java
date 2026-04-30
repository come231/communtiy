package com.community.admin.controller;

import com.community.admin.entity.SysConfig;
import com.community.admin.service.SysConfigService;
import com.community.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/configs")
@Tag(name = "系统配置", description = "系统配置管理接口")
public class SysConfigController {

    private final SysConfigService sysConfigService;

    public SysConfigController(SysConfigService sysConfigService) {
        this.sysConfigService = sysConfigService;
    }

    @GetMapping
    @Operation(summary = "配置列表", description = "获取所有系统配置")
    public Result<List<SysConfig>> getConfigList(
            @Parameter(description = "分组Key") @RequestParam(required = false) String groupKey) {
        return Result.success(sysConfigService.getConfigListByGroup(groupKey));
    }

    @PutMapping("/{id:.+}")
    @Operation(summary = "更新配置", description = "更新指定配置项（community_security.sys_config 主键 id）")
    public Result<Void> updateConfig(@PathVariable String id,
                                     @RequestBody Map<String, Object> params) {
        String configValue = (String) params.get("configValue");
        sysConfigService.updateConfig(id, configValue);
        return Result.success();
    }

    @GetMapping("/get/{key}")
    @Operation(summary = "获取配置", description = "根据Key获取配置项")
    public Result<SysConfig> getConfigByKey(@PathVariable String key) {
        return Result.success(sysConfigService.getConfigByKey(key));
    }

    @PostMapping
    @Operation(summary = "创建配置", description = "创建新的配置项")
    public Result<SysConfig> createConfig(@RequestBody SysConfig config) {
        return Result.success(sysConfigService.createConfig(config));
    }

    @DeleteMapping("/{id:.+}")
    @Operation(summary = "删除配置", description = "删除指定配置项")
    public Result<Void> deleteConfig(@PathVariable String id) {
        sysConfigService.deleteConfig(id);
        return Result.success();
    }
}
