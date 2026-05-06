package com.community.message.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * WebSocket用户会话管理器
 * 管理用户与WebSocket Session的映射关系
 */
@Slf4j
@Component
public class UserSessionManager {

    /**
     * userId -> Set<sessionId>
     * 一个用户可能有多个连接（多端登录）
     */
    private final Map<Long, Set<String>> userSessions = new ConcurrentHashMap<>();

    /**
     * sessionId -> userId
     */
    private final Map<String, Long> sessionUserMap = new ConcurrentHashMap<>();

    /**
     * 用户上线
     */
    public void userConnected(Long userId, String sessionId) {
        if (userId == null || sessionId == null) {
            return;
        }

        userSessions.computeIfAbsent(userId, k -> new CopyOnWriteArraySet<>()).add(sessionId);
        sessionUserMap.put(sessionId, userId);
        log.info("用户上线: userId={}, sessionId={}, 当前连接数={}",
                userId, sessionId, getUserSessionCount(userId));
    }

    /**
     * 用户下线
     */
    public void userDisconnected(String sessionId) {
        Long userId = sessionUserMap.remove(sessionId);
        if (userId != null) {
            Set<String> sessions = userSessions.get(userId);
            if (sessions != null) {
                sessions.remove(sessionId);
                if (sessions.isEmpty()) {
                    userSessions.remove(userId);
                }
            }
            log.info("用户下线: userId={}, sessionId={}, 剩余连接数={}",
                    userId, sessionId, getUserSessionCount(userId));
        }
    }

    /**
     * 判断用户是否在线
     */
    public boolean isUserOnline(Long userId) {
        Set<String> sessions = userSessions.get(userId);
        return sessions != null && !sessions.isEmpty();
    }

    /**
     * 获取用户所有Session ID
     */
    public Set<String> getUserSessions(Long userId) {
        return userSessions.getOrDefault(userId, Set.of());
    }

    /**
     * 获取用户连接数
     */
    public int getUserSessionCount(Long userId) {
        Set<String> sessions = userSessions.get(userId);
        return sessions != null ? sessions.size() : 0;
    }

    /**
     * 获取在线用户数量
     */
    public int getOnlineUserCount() {
        return userSessions.size();
    }

    /**
     * 获取总连接数
     */
    public int getTotalConnectionCount() {
        return sessionUserMap.size();
    }
}
