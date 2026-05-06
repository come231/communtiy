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
import java.util.HashMap;
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
        List<Map<String, Object>> sessions = aiChatMapper.selectMaps(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<AiChat>()
                        .select("session_id", "MAX(create_time) as latest_time")
                        .eq("user_id", userId)
                        .groupBy("session_id")
                        .orderByDesc("latest_time")
        );

        for (Map<String, Object> session : sessions) {
            String sessionId = (String) session.get("session_id");
            LambdaQueryWrapper<AiChat> titleWrapper = new LambdaQueryWrapper<>();
            titleWrapper.eq(AiChat::getSessionId, sessionId)
                       .eq(AiChat::getRole, "user")
                       .orderByAsc(AiChat::getCreateTime)
                       .last("LIMIT 1");
            AiChat firstUserMsg = aiChatMapper.selectOne(titleWrapper);
            if (firstUserMsg != null) {
                String title = firstUserMsg.getContent();
                session.put("title", title.length() > 30 ? title.substring(0, 30) + "..." : title);
            } else {
                session.put("title", "新对话");
            }
        }
        return sessions;
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

    /**
     * 【管理员】获取所有会话列表（分页）
     */
    public Page<Map<String, Object>> getAllSessions(Integer page, Integer size) {
        // 使用自定义SQL查询所有会话，包含用户信息
        List<Map<String, Object>> sessions = aiChatMapper.selectAllSessionsWithUser((page - 1) * size, size);

        // 获取总会话数
        Long total = aiChatMapper.selectDistinctSessionCount();

        Page<Map<String, Object>> pageResult = new Page<>(page, size);
        pageResult.setRecords(sessions);
        pageResult.setTotal(total);

        return pageResult;
    }

    /**
     * 【管理员】获取AI聊天统计
     */
    public Map<String, Object> getChatStats() {
        Map<String, Object> stats = new HashMap<>();

        // 总会话数
        Long totalSessions = aiChatMapper.selectDistinctSessionCount();
        stats.put("totalSessions", totalSessions);

        // 总消息数
        Long totalMessages = aiChatMapper.selectCount(null);
        stats.put("totalMessages", totalMessages);

        // 今日消息数
        LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LambdaQueryWrapper<AiChat> todayWrapper = new LambdaQueryWrapper<>();
        todayWrapper.ge(AiChat::getCreateTime, today);
        Long todayMessages = aiChatMapper.selectCount(todayWrapper);
        stats.put("todayMessages", todayMessages);

        // 今日会话数
        Long todaySessions = aiChatMapper.selectTodaySessionCount();
        stats.put("todaySessions", todaySessions);

        // 活跃用户（近7天有聊天记录的用户）
        Long activeUsers = aiChatMapper.selectActiveUserCount(7);
        stats.put("activeUsers", activeUsers);

        return stats;
    }
}
