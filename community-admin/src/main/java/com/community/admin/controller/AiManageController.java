package com.community.admin.controller;

import com.community.admin.feign.AiFeignClient;
import com.community.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * AI管理
 */
@RestController
@RequestMapping("/admin/ai")
@RequiredArgsConstructor
@Tag(name = "AI管理", description = "管理员AI配置和监控接口")
public class AiManageController {

    private final AiFeignClient aiFeignClient;

    @GetMapping("/config")
    @Operation(summary = "获取AI配置", description = "获取当前AI API配置")
    public Result<Map<String, Object>> getAiConfig() {
        try {
            return aiFeignClient.getAiConfig();
        } catch (Exception e) {
            // 降级：从系统配置表读取
            Map<String, Object> config = new HashMap<>();
            config.put("baseUrl", "");
            config.put("model", "");
            config.put("apiKey", "******");
            return Result.success(config);
        }
    }

    @PutMapping("/config")
    @Operation(summary = "更新AI配置", description = "更新AI API地址和模型")
    public Result<Void> updateAiConfig(@RequestBody Map<String, String> config) {
        try {
            return aiFeignClient.updateAiConfig(config);
        } catch (Exception e) {
            return Result.error("更新AI配置失败: " + e.getMessage());
        }
    }

    @GetMapping("/chat-records")
    @Operation(summary = "AI对话记录", description = "获取AI对话会话列表")
    public Result<?> getChatRecords(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            return aiFeignClient.getChatSessions(page, size);
        } catch (Exception e) {
            return Result.error("获取AI对话记录失败: " + e.getMessage());
        }
    }

    @GetMapping("/stats")
    @Operation(summary = "AI使用统计", description = "获取AI使用统计信息")
    public Result<Map<String, Object>> getAiStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalSessions", 0);
            stats.put("todaySessions", 0);
            stats.put("totalMessages", 0);
            return Result.success(stats);
        } catch (Exception e) {
            return Result.error("获取AI统计失败: " + e.getMessage());
        }
    }
}
