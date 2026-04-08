package com.community.ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.ai.dto.ChatRequest;
import com.community.ai.dto.ChatResponse;
import com.community.ai.entity.AiChat;
import com.community.ai.mapper.AiChatMapper;
import com.community.ai.util.AiCallHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiChatService {

    private final AiCallHelper aiCallHelper;
    private final AiChatMapper aiChatMapper;
    @Value("${spring.ai.openai.chat.options.model:glm-4-flash}")
    private String model;
    private static final int MAX_HISTORY_SIZE = 20;

    public ChatResponse chat(Long userId, ChatRequest request) {
        String sessionId = request.getSessionId();
        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = newSession();
        }

        AiChat userMsg = new AiChat();
        userMsg.setUserId(userId);
        userMsg.setSessionId(sessionId);
        userMsg.setRole("user");
        userMsg.setContent(request.getContent());
        userMsg.setCreateTime(LocalDateTime.now());
        aiChatMapper.insert(userMsg);

        try {
            StringBuilder contextBuilder = new StringBuilder();
            List<AiChat> history = getRecentHistory(sessionId);
            for (AiChat chat : history) {
                contextBuilder.append(chat.getRole()).append(": ").append(chat.getContent()).append("\n");
            }

            String fullPrompt = contextBuilder.toString() + "\n[用户]" + request.getContent();
            String reply = aiCallHelper.callWithSystem("你是一个智能社区助手，请用友好、专业的态度回答用户的问题。", fullPrompt);

            int tokens = reply.length() / 2;

            AiChat assistantMsg = new AiChat();
            assistantMsg.setUserId(userId);
            assistantMsg.setSessionId(sessionId);
            assistantMsg.setRole("assistant");
            assistantMsg.setContent(reply);
            assistantMsg.setTokens(tokens);
            assistantMsg.setModel(model);
            assistantMsg.setCreateTime(LocalDateTime.now());
            aiChatMapper.insert(assistantMsg);

            return new ChatResponse(reply, sessionId, tokens);
        } catch (Exception e) {
            log.error("AI聊天失败, sessionId={}, error={}", sessionId, e.getMessage(), e);
            throw new RuntimeException("AI调用失败，请稍后重试: " + e.getMessage());
        }
    }

    public Page<AiChat> getChatHistory(String sessionId, Integer page, Integer size) {
        LambdaQueryWrapper<AiChat> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiChat::getSessionId, sessionId)
               .orderByAsc(AiChat::getCreateTime);
        return aiChatMapper.selectPage(new Page<>(page, size), wrapper);
    }

    public List<Map<String, Object>> getSessionList(Long userId) {
        return aiChatMapper.selectMaps(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<AiChat>()
                        .select("session_id", "MAX(create_time) as latest_time")
                        .eq("user_id", userId)
                        .groupBy("session_id")
                        .orderByDesc("latest_time")
        );
    }

    public boolean clearSession(String sessionId) {
        LambdaQueryWrapper<AiChat> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiChat::getSessionId, sessionId);
        return aiChatMapper.delete(wrapper) >= 0;
    }

    public String newSession() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private List<AiChat> getRecentHistory(String sessionId) {
        LambdaQueryWrapper<AiChat> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiChat::getSessionId, sessionId)
               .orderByDesc(AiChat::getCreateTime)
               .last("LIMIT " + MAX_HISTORY_SIZE);
        return aiChatMapper.selectList(wrapper);
    }
}
