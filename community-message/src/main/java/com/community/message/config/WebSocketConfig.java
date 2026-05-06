package com.community.message.config;

import com.community.message.websocket.AuthChannelInterceptor;
import com.community.message.websocket.WebSocketHandshakeInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket + STOMP 配置类
 * 配置消息代理和STOMP端点
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final AuthChannelInterceptor authChannelInterceptor;
    private final WebSocketHandshakeInterceptor handshakeInterceptor;

    /**
     * 配置消息代理
     * 前缀为 /topic 的消息会被代理到消息代理（广播）
     * 前缀为 /queue 的消息会被代理到消息代理（点对点）
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 启用简单的内存消息代理，前缀为 /topic（广播）和 /user（点对点）
        config.enableSimpleBroker("/topic", "/user");
        // 设置应用前缀，客户端发送消息的前缀
        config.setApplicationDestinationPrefixes("/app");
        // 设置用户目的地前缀（用于点对点消息）
        config.setUserDestinationPrefix("/user");
    }

    /**
     * 注册STOMP端点
     * 客户端通过该端点连接WebSocket
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 注册端点 /ws/chat，启用SockJS回退选项
        // SockJS会处理 /ws/chat/info 等内部端点
        registry.addEndpoint("/ws/chat")
                .setAllowedOriginPatterns("*")
                .withSockJS();

        // 同时注册一个不带SockJS的端点（用于原生WebSocket客户端）
        registry.addEndpoint("/ws/chat")
                .setAllowedOriginPatterns("*");
    }

    /**
     * 配置客户端入站通道拦截器
     * 用于用户认证和会话管理
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(authChannelInterceptor);
    }
}
