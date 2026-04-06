package com.community.ai.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.ai.dto.ChatRequest;
import com.community.ai.dto.ChatResponse;
import com.community.ai.entity.AiChat;
import com.community.ai.service.AiChatService;
import com.community.common.util.ContextHolder;
import com.community.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ai/chat")
@RequiredArgsConstructor
@Tag(name = "AI聊天", description = "AI对话服务接口")
public class AiChatController {

    private final AiChatService aiChatService;

    @PostMapping
    @Operation(summary = "发送消息", description = "发送消息给AI并获取回复")
    public Result<ChatResponse> chat(@RequestBody ChatRequest request) {
        Long userId = ContextHolder.getUserId();
        ChatResponse response = aiChatService.chat(userId, request);
        return Result.success(response);
    }

    @GetMapping("/history")
    @Operation(summary = "获取聊天历史", description = "获取指定会话的聊天记录")
    public Result<Page<AiChat>> getHistory(
            @Parameter(description = "会话ID") @RequestParam String sessionId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") Integer size) {
        return Result.success(aiChatService.getChatHistory(sessionId, page, size));
    }

    @GetMapping("/sessions")
    @Operation(summary = "会话列表", description = "获取当前用户的所有会话列表")
    public Result<List<Map<String, Object>>> getSessionList() {
        Long userId = ContextHolder.getUserId();
        return Result.success(aiChatService.getSessionList(userId));
    }

    @DeleteMapping("/session/{sessionId}")
    @Operation(summary = "清空会话", description = "清空指定会话的所有聊天记录")
    public Result<Boolean> clearSession(@PathVariable String sessionId) {
        return Result.success(aiChatService.clearSession(sessionId));
    }

    @PostMapping("/new-session")
    @Operation(summary = "创建会话", description = "创建新的AI聊天会话")
    public Result<String> newSession() {
        return Result.success(aiChatService.newSession());
    }
}
