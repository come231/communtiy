package com.community.ai.controller;

import com.community.ai.config.AiConfig;
import com.community.ai.entity.AiChat;
import com.community.ai.entity.AiTopic;
import com.community.ai.service.AiChatService;
import com.community.ai.service.AiReviewService;
import com.community.ai.service.AiTopicService;
import com.community.ai.service.AiWriteService;
import com.community.ai.service.PostReviewRuleService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.common.dto.AiCheckRecordVO;
import com.community.common.dto.PostReviewRuleDTO;
import com.community.common.result.PageResult;
import com.community.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ai/admin")
@RequiredArgsConstructor
@Tag(name = "AI管理", description = "AI配置和话题管理接口")
public class AiAdminController {

    private final AiTopicService aiTopicService;
    private final AiConfig aiConfig;
    private final PostReviewRuleService postReviewRuleService;
    private final AiReviewService aiReviewService;
    private final AiWriteService aiWriteService;
    private final AiChatService aiChatService;

    @GetMapping("/check-records")
    @Operation(summary = "AI审核记录", description = "分页查询 AI 内容审核流水，可按用户、帖子、结果筛选")
    public Result<PageResult<AiCheckRecordVO>> listCheckRecords(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long postId,
            @RequestParam(required = false) Integer checkResult) {
        return Result.success(aiReviewService.pageCheckRecords(page, size, userId, postId, checkResult));
    }

    @GetMapping("/topics")
    @Operation(summary = "话题列表", description = "获取AI推荐话题列表")
    public Result<List<AiTopic>> getTopics() {
        List<AiTopic> topics = aiTopicService.getHotTopics();
        return Result.success(topics);
    }

    @PostMapping("/generate-topics")
    @Operation(summary = "生成话题", description = "AI生成推荐话题")
    public Result<List<String>> generateTopics(@RequestBody(required = false) Map<String, String> params) {
        String keywords = params != null ? params.get("keywords") : null;
        List<String> topics = aiTopicService.generateTopics(keywords);
        return Result.success(topics);
    }

    @PostMapping("/topic")
    @Operation(summary = "创建话题", description = "手动创建AI话题")
    public Result<AiTopic> createTopic(@RequestBody Map<String, String> params) {
        String name = params.get("name");
        String description = params.get("description");
        AiTopic topic = aiTopicService.createTopic(name, description);
        return Result.success(topic);
    }

    @GetMapping("/config")
    @Operation(summary = "获取AI配置", description = "获取当前AI API配置")
    public Result<Map<String, Object>> getAiConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("baseUrl", aiConfig.getBaseUrl());
        config.put("model", aiConfig.getModel());
        config.put("apiKey", aiConfig.getApiKey() != null && !aiConfig.getApiKey().isEmpty() ? "******" : "");
        config.put("temperature", aiConfig.getTemperature());
        return Result.success(config);
    }

    @GetMapping("/post-review-rules")
    @Operation(summary = "帖子审核规则列表", description = "获取帖子 AI 审核规则配置（含禁用项）")
    public Result<List<PostReviewRuleDTO>> listPostReviewRules() {
        return Result.success(postReviewRuleService.listAll());
    }

    @PostMapping("/post-review-rules")
    @Operation(summary = "新增帖子审核规则", description = "新增一条规则，启用后进入 AI 提示词")
    public Result<PostReviewRuleDTO> createPostReviewRule(@RequestBody PostReviewRuleDTO dto) {
        return Result.success(postReviewRuleService.create(dto));
    }

    @PutMapping("/post-review-rules/{id}")
    @Operation(summary = "更新帖子审核规则", description = "更新指定规则")
    public Result<PostReviewRuleDTO> updatePostReviewRule(@PathVariable Long id, @RequestBody PostReviewRuleDTO dto) {
        PostReviewRuleDTO updated = postReviewRuleService.update(id, dto);
        if (updated == null) {
            return Result.error("规则不存在");
        }
        return Result.success(updated);
    }

    @DeleteMapping("/post-review-rules/{id}")
    @Operation(summary = "删除帖子审核规则", description = "删除指定规则")
    public Result<Void> deletePostReviewRule(@PathVariable Long id) {
        if (!postReviewRuleService.delete(id)) {
            return Result.error("删除失败或规则不存在");
        }
        return Result.success();
    }

    @PutMapping("/config")
    @Operation(summary = "更新AI配置", description = "更新AI API地址和模型")
    public Result<Void> updateAiConfig(@RequestBody Map<String, String> config) {
        if (config.containsKey("baseUrl")) {
            aiConfig.setBaseUrl(config.get("baseUrl"));
        }
        if (config.containsKey("model")) {
            aiConfig.setModel(config.get("model"));
        }
        if (config.containsKey("apiKey")) {
            aiConfig.setApiKey(config.get("apiKey"));
        }
        if (config.containsKey("temperature")) {
            try {
                aiConfig.setTemperature(Double.parseDouble(config.get("temperature")));
            } catch (NumberFormatException e) {
                aiConfig.setTemperature(0.7);
            }
        }
        return Result.success();
    }

    @GetMapping("/feedback-stats")
    @Operation(summary = "AI反馈统计", description = "获取满意/不满意反馈统计与原因分布")
    public Result<Map<String, Object>> feedbackStats() {
        return Result.success(aiWriteService.feedbackStats());
    }

    // ==================== 管理员查看用户AI会话记录 ====================

    @GetMapping("/chat/sessions")
    @Operation(summary = "【管理端】获取AI会话列表", description = "获取所有用户的AI会话列表，支持按用户ID筛选")
    public Result<?> getAllChatSessions(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long userId) {
        try {
            if (userId != null) {
                // 获取指定用户的会话
                return Result.success(aiChatService.getSessionList(userId));
            } else {
                // 获取所有会话（分页）
                return Result.success(aiChatService.getAllSessions(page, size));
            }
        } catch (Exception e) {
            return Result.error("获取会话列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/chat/history")
    @Operation(summary = "【管理端】获取聊天记录", description = "获取指定会话的详细聊天记录")
    public Result<Page<AiChat>> getChatHistory(
            @RequestParam String sessionId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int size) {
        try {
            return Result.success(aiChatService.getChatHistory(sessionId, page, size));
        } catch (Exception e) {
            return Result.error("获取聊天记录失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/chat/session/{sessionId}")
    @Operation(summary = "【管理端】删除AI会话", description = "删除指定的AI会话及其所有聊天记录")
    public Result<Void> deleteChatSession(@PathVariable String sessionId) {
        try {
            boolean success = aiChatService.clearSession(sessionId);
            if (success) {
                return Result.success();
            } else {
                return Result.error("删除会话失败");
            }
        } catch (Exception e) {
            return Result.error("删除会话失败: " + e.getMessage());
        }
    }

    @GetMapping("/chat/stats")
    @Operation(summary = "【管理端】AI聊天统计", description = "获取AI聊天使用统计信息")
    public Result<Map<String, Object>> getChatStats() {
        try {
            return Result.success(aiChatService.getChatStats());
        } catch (Exception e) {
            return Result.error("获取统计信息失败: " + e.getMessage());
        }
    }
}
