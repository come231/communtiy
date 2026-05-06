package com.community.message.websocket;

import com.community.common.util.ContextHolder;
import com.community.message.entity.ChatMsg;
import com.community.message.service.ChatMsgService;
import com.community.message.service.WebSocketMessageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.security.Principal;

/**
 * WebSocket聊天消息控制器
 * 处理STOMP协议消息
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ChatMsgService chatMsgService;
    private final WebSocketMessageService webSocketMessageService;
    private final ObjectMapper objectMapper;
    private final UserSessionManager userSessionManager;

    /**
     * 发送私信消息
     * 客户端发送消息到 /app/chat/send
     * 服务器将消息转发给接收者 /user/{toUserId}/queue/chat
     */
    @MessageMapping("/chat/send")
    public void sendChatMessage(@Payload ChatMessageRequest request, StompHeaderAccessor headerAccessor) {
        Long fromUserId = getUserIdFromSession(headerAccessor);
        if (fromUserId == null) {
            log.error("发送消息失败: 未获取到用户ID");
            return;
        }

        try {
            // 保存消息到数据库
            chatMsgService.sendChat(request.getToUserId(), request.getContent(),
                    request.getSharePostId(), request.getSharePostTitle());

            // 构建消息体
            ChatMessageResponse response = new ChatMessageResponse();
            response.setFromUserId(fromUserId);
            response.setToUserId(request.getToUserId());
            response.setContent(request.getContent());
            response.setSharePostId(request.getSharePostId());
            response.setSharePostTitle(request.getSharePostTitle());
            response.setTimestamp(System.currentTimeMillis());

            // 实时推送给接收者
            webSocketMessageService.sendToUser(request.getToUserId(), "/queue/chat", response);

            // 确认消息发送给发送者
            webSocketMessageService.sendToUser(fromUserId, "/queue/chat/ack", response);

            log.info("WebSocket消息已转发: from={} to={}", fromUserId, request.getToUserId());
        } catch (Exception e) {
            log.error("发送WebSocket消息失败", e);
        }
    }

    /**
     * 标记消息已读
     * 客户端发送消息到 /app/chat/read
     */
    @MessageMapping("/chat/read")
    public void markChatRead(@Payload ReadRequest request, StompHeaderAccessor headerAccessor) {
        Long currentUserId = getUserIdFromSession(headerAccessor);
        if (currentUserId == null) {
            return;
        }

        try {
            chatMsgService.markChatRead(request.getFromUserId());

            // 通知发送者消息已被阅读
            ReadReceipt receipt = new ReadReceipt();
            receipt.setReaderId(currentUserId);
            receipt.setTimestamp(System.currentTimeMillis());
            webSocketMessageService.sendToUser(request.getFromUserId(), "/queue/chat/read", receipt);

            log.info("消息已读标记: reader={} from={}", currentUserId, request.getFromUserId());
        } catch (Exception e) {
            log.error("标记消息已读失败", e);
        }
    }

    /**
     * 获取联系人列表（通过WebSocket）
     * 客户端发送消息到 /app/chat/users
     * 服务器返回给 /user/{userId}/queue/chat/users
     */
    @MessageMapping("/chat/users")
    @SendToUser("/queue/chat/users")
    public Object getChatUserList(StompHeaderAccessor headerAccessor) {
        Long currentUserId = getUserIdFromSession(headerAccessor);
        if (currentUserId == null) {
            return null;
        }

        // 临时设置上下文（WebSocket线程中没有HTTP上下文）
        ContextHolder.setUserId(currentUserId);
        try {
            return chatMsgService.getChatUserList();
        } finally {
            ContextHolder.clear();
        }
    }

    /**
     * 心跳检测
     * 客户端定期发送消息到 /app/chat/ping
     */
    @MessageMapping("/chat/ping")
    @SendToUser("/queue/chat/pong")
    public String ping(@Payload String message) {
        return "pong:" + System.currentTimeMillis();
    }

    /**
     * 从WebSocket Session中获取用户ID
     */
    private Long getUserIdFromSession(StompHeaderAccessor headerAccessor) {
        Principal principal = headerAccessor.getUser();
        if (principal != null) {
            try {
                return Long.valueOf(principal.getName());
            } catch (NumberFormatException e) {
                log.error("解析用户ID失败: {}", principal.getName());
            }
        }
        // 尝试从Session属性获取
        Object userId = headerAccessor.getSessionAttributes().get("userId");
        if (userId instanceof Long) {
            return (Long) userId;
        }
        return null;
    }

    // ============== DTO类 ==============

    @Data
    public static class ChatMessageRequest {
        private Long toUserId;
        private String content;
        private Long sharePostId;
        private String sharePostTitle;
    }

    @Data
    public static class ChatMessageResponse {
        private Long fromUserId;
        private Long toUserId;
        private String content;
        private Long sharePostId;
        private String sharePostTitle;
        private Long timestamp;
    }

    @Data
    public static class ReadRequest {
        private Long fromUserId;
    }

    @Data
    public static class ReadReceipt {
        private Long readerId;
        private Long timestamp;
    }
}
