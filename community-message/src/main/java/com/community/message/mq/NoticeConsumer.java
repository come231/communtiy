package com.community.message.mq;

import com.community.common.constant.MQConstant;
import com.community.message.service.NoticeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class NoticeConsumer {

    private final NoticeService noticeService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = MQConstant.NOTICE_QUEUE, durable = "true"),
            exchange = @Exchange(name = MQConstant.COMMUNITY_EXCHANGE, type = ExchangeTypes.TOPIC),
            key = MQConstant.NOTICE_ROUTING_KEY
    ))
    public void handleNotice(Map<String, Object> message) {
        try {
            String event = (String) message.get("event");
            Long targetUserId = (Long) message.get("targetUserId");
            Long operatorId = (Long) message.get("operatorId");
            Long relatedId = (Long) message.get("relatedId");
            Integer relatedType = (Integer) message.get("relatedType");
            String content = (String) message.get("content");

            if (targetUserId == null) {
                log.warn("收到无效的通知消息，缺少targetUserId: {}", message);
                return;
            }

            switch (event) {
                case "like" -> {
                    String noticeContent = "有人点赞了你的" + getRelatedTypeName(relatedType);
                    // 完全匹配你的表字段：user_id, type, content, from_uid, target_id
                    noticeService.createNotice(targetUserId, 1, noticeContent, operatorId, relatedId);
                }
                case "comment" -> {
                    String noticeContent = content != null ? content : "有人评论了你的内容";
                    noticeService.createNotice(targetUserId, 2, noticeContent, operatorId, relatedId);
                }
                case "follow" -> {
                    noticeService.createNotice(targetUserId, 3, "有人关注了你", operatorId, relatedId);
                }
                case "collect" -> {
                    String noticeContent = "有人收藏了你的" + getRelatedTypeName(relatedType);
                    noticeService.createNotice(targetUserId, 4, noticeContent, operatorId, relatedId);
                }
                default -> log.info("未知的通知事件类型: {}", event);
            }
            log.info("处理通知消息成功: event={}, targetUserId={}", event, targetUserId);
        } catch (Exception e) {
            log.error("处理通知消息失败: {}, error: {}", message, e.getMessage(), e);
        }
    }

    private String getRelatedTypeName(Integer type) {
        if (type == null) return "内容";
        return switch (type) {
            case 1 -> "帖子";
            case 2 -> "评论";
            default -> "内容";
        };
    }
}