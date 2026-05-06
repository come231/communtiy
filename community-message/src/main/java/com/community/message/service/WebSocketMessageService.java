package com.community.message.service;

import com.community.message.websocket.UserSessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * WebSocket消息推送服务
 * 用于向指定用户或广播发送消息
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketMessageService {

    private final SimpMessagingTemplate messagingTemplate;
    private final UserSessionManager userSessionManager;

    /**
     * 发送消息给指定用户（单端）
     * 使用/user/{userId}前缀，Spring会自动路由到用户的私有队列
     */
    public void sendToUser(Long userId, String destination, Object payload) {
        if (userId == null) {
            log.warn("发送消息失败: userId为空");
            return;
        }

        String userDestination = "/user/" + userId + destination;
        try {
            messagingTemplate.convertAndSend(userDestination, payload);
            log.debug("消息已发送给用户: userId={}, destination={}", userId, destination);
        } catch (Exception e) {
            log.error("发送消息给用户失败: userId={}, destination={}", userId, destination, e);
        }
    }

    /**
     * 广播消息给所有在线用户
     */
    public void broadcast(String destination, Object payload) {
        try {
            messagingTemplate.convertAndSend(destination, payload);
            log.debug("广播消息已发送: destination={}", destination);
        } catch (Exception e) {
            log.error("广播消息失败: destination={}", destination, e);
        }
    }

    /**
     * 发送消息给指定用户（多端同时推送）
     * 如果用户有多个设备登录，会同时推送到所有设备
     */
    public void sendToUserAllSessions(Long userId, String destination, Object payload) {
        if (!userSessionManager.isUserOnline(userId)) {
            log.debug("用户不在线，消息未发送: userId={}", userId);
            return;
        }

        sendToUser(userId, destination, payload);
    }

    /**
     * 发送系统通知给指定用户
     */
    public void sendSystemNotice(Long userId, Object notice) {
        sendToUser(userId, "/queue/notices", notice);
    }

    /**
     * 广播系统通知给所有用户
     */
    public void broadcastSystemNotice(Object notice) {
        broadcast("/topic/notices", notice);
    }

    /**
     * 发送未读消息数量更新
     */
    public void sendUnreadCountUpdate(Long userId, Long unreadCount) {
        UnreadCountUpdate update = new UnreadCountUpdate();
        update.setUnreadCount(unreadCount);
        sendToUser(userId, "/queue/unread", update);
    }

    /**
     * 未读数更新DTO
     */
    public static class UnreadCountUpdate {
        private Long unreadCount;
        private Long timestamp = System.currentTimeMillis();

        public Long getUnreadCount() {
            return unreadCount;
        }

        public void setUnreadCount(Long unreadCount) {
            this.unreadCount = unreadCount;
        }

        public Long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(Long timestamp) {
            this.timestamp = timestamp;
        }
    }
}
