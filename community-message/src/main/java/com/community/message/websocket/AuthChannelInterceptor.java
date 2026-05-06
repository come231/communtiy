package com.community.message.websocket;

import com.community.common.util.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

/**
 * WebSocket通道拦截器
 * 处理STOMP连接的认证和会话管理
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthChannelInterceptor implements ChannelInterceptor {

    private final UserSessionManager userSessionManager;
    private final JwtUtil jwtUtil;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) {
            log.debug("preSend: accessor is null");
            return message;
        }

        log.debug("preSend: command={}, sessionId={}", accessor.getCommand(), accessor.getSessionId());

        // 处理CONNECT命令 - 用户连接时进行认证
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            // 从URL参数或Header中获取Token
            String token = extractToken(accessor);
            log.debug("CONNECT received, token present: {}", token != null);

            Long userId = null;
            String username = "anonymous";

            if (token != null && jwtUtil.validateToken(token)) {
                Claims claims = jwtUtil.parseToken(token);
                userId = claims.get("userId", Long.class);
                username = claims.getSubject();
                log.debug("Token parsed: userId={}, username={}", userId, username);
            } else {
                // Token无效时，尝试从Session Attributes获取（由HandshakeInterceptor设置）
                Object sessionToken = accessor.getSessionAttributes().get("token");
                if (sessionToken instanceof String && jwtUtil.validateToken((String) sessionToken)) {
                    Claims claims = jwtUtil.parseToken((String) sessionToken);
                    userId = claims.get("userId", Long.class);
                    username = claims.getSubject();
                }
            }

            if (userId == null) {
                // 临时允许匿名连接（仅用于测试）
                log.warn("WebSocket连接: Token无效，允许匿名连接（测试模式）");
                userId = 1L; // 临时设置默认用户ID
                username = "test_user";
            }

            // 设置用户身份
            accessor.setUser(new WebSocketUserPrincipal(userId, username));
            accessor.getSessionAttributes().put("userId", userId);
            accessor.getSessionAttributes().put("username", username);

            log.info("WebSocket用户连接成功: userId={}, username={}", userId, username);
        }

        // 处理SUBSCRIBE命令
        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            Principal principal = accessor.getUser();
            if (principal == null) {
                log.warn("订阅失败: 未认证的用户");
                return null;
            }
        }

        return message;
    }

    @Override
    public void postSend(Message<?> message, MessageChannel channel, boolean sent) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) {
            return;
        }

        // 处理连接成功后的会话注册
        if (StompCommand.CONNECTED.equals(accessor.getCommand())) {
            Long userId = (Long) accessor.getSessionAttributes().get("userId");
            String sessionId = accessor.getSessionId();
            if (userId != null && sessionId != null) {
                userSessionManager.userConnected(userId, sessionId);
            }
        }

        // 处理断开连接
        if (StompCommand.DISCONNECT.equals(accessor.getCommand())) {
            String sessionId = accessor.getSessionId();
            if (sessionId != null) {
                userSessionManager.userDisconnected(sessionId);
            }
        }
    }

    /**
     * 从STOMP Header中提取Token
     * 支持两种方式：
     * 1. 原生WebSocket URL参数: ?token=xxx
     * 2. STOMP Header: Authorization: Bearer xxx
     */
    private String extractToken(StompHeaderAccessor accessor) {
        // 先从Header获取
        String authHeader = accessor.getFirstNativeHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        // 从Header中获取token参数
        String token = accessor.getFirstNativeHeader("token");
        if (token != null) {
            return token;
        }

        // 从Session Attributes获取（在URL参数中传递的token）
        Object tokenAttr = accessor.getSessionAttributes().get("token");
        if (tokenAttr instanceof String) {
            return (String) tokenAttr;
        }

        return null;
    }

    /**
     * WebSocket用户身份实现
     */
    public static class WebSocketUserPrincipal implements Principal {
        private final Long userId;
        private final String username;

        public WebSocketUserPrincipal(Long userId, String username) {
            this.userId = userId;
            this.username = username;
        }

        @Override
        public String getName() {
            return String.valueOf(userId);
        }

        public Long getUserId() {
            return userId;
        }

        public String getUsername() {
            return username;
        }
    }
}
