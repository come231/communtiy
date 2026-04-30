package com.community.community.mq;

import com.community.common.constant.MQConstant;
import com.community.common.feign.UserFeignClient;
import com.community.community.entity.Post;
import com.community.community.mapper.PostMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class InteractEventConsumer {

    private final PostMapper postMapper;
    private final ObjectMapper objectMapper;
    private final RabbitTemplate rabbitTemplate;
    private final UserFeignClient userFeignClient;

    /**
     * 监听点赞事件，更新帖子/评论的点赞计数，并发送通知
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = MQConstant.LIKE_QUEUE, durable = "true"),
            exchange = @Exchange(name = MQConstant.COMMUNITY_EXCHANGE, type = ExchangeTypes.TOPIC),
            key = MQConstant.LIKE_ROUTING_KEY
    ))
    // 👇 只改这里！！！
    public void handleLikeEvent(HashMap<String, Object> message) {
        try {
            // 直接从 Map 取值，不用 JSON 解析了！
            Long userId = (Long) message.get("userId");
            Long targetId = (Long) message.get("targetId");
            Integer targetType = (Integer) message.getOrDefault("targetType", 1);
            String action = (String) message.get("action");

            if (targetId == null) {
                log.warn("点赞事件缺少targetId: {}", message);
                return;
            }

            if (targetType == com.community.common.constant.CommonConstant.LIKE_TYPE_POST) {
                Post post = postMapper.selectById(targetId);
                if (post != null) {
                    int delta = "like".equals(action) ? 1 : -1;
                    post.setLikeCount(Math.max(0, post.getLikeCount() + delta));
                    postMapper.updateById(post);
                    log.info("更新帖子点赞数: postId={}, delta={}", targetId, delta);

                    // 更新作者获赞总数
                    try {
                        int authorDelta = "like".equals(action) ? 1 : -1;
                        userFeignClient.updateLikeCount(post.getUserId(), authorDelta);
                    } catch (Exception ex) {
                        log.warn("更新用户获赞数失败: userId={}", post.getUserId(), ex);
                    }

                    // 发送点赞通知
                    if ("like".equals(action) && userId != null && !userId.equals(post.getUserId())) {
                        Map<String, Object> noticeMsg = new HashMap<>();
                        noticeMsg.put("event", "like");
                        noticeMsg.put("targetUserId", post.getUserId());
                        noticeMsg.put("operatorId", userId);
                        noticeMsg.put("relatedId", targetId);
                        noticeMsg.put("relatedType", 1);
                        rabbitTemplate.convertAndSend(MQConstant.COMMUNITY_EXCHANGE, MQConstant.NOTICE_ROUTING_KEY, noticeMsg);
                    }
                }
            }
        } catch (Exception e) {
            log.error("处理点赞事件失败: {}", message, e);
        }
    }

    /**
     * 监听收藏事件，更新帖子的收藏计数，并发送通知
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = MQConstant.COLLECT_QUEUE, durable = "true"),
            exchange = @Exchange(name = MQConstant.COMMUNITY_EXCHANGE, type = ExchangeTypes.TOPIC),
            key = MQConstant.COLLECT_ROUTING_KEY
    ))
    // 👇 只改这里！！！
    public void handleCollectEvent(HashMap<String, Object> message) {
        try {
            // 直接从 Map 取值
            Long userId = (Long) message.get("userId");
            Long postId = (Long) message.get("postId");
            String action = (String) message.get("action");

            if (postId == null) {
                log.warn("收藏事件缺少postId: {}", message);
                return;
            }

            Post post = postMapper.selectById(postId);
            if (post != null) {
                int delta = "collect".equals(action) ? 1 : -1;
                post.setCollectCount(Math.max(0, post.getCollectCount() + delta));
                postMapper.updateById(post);
                log.info("更新帖子收藏数: postId={}, delta={}", postId, delta);

                // 发送收藏通知
                if ("collect".equals(action) && userId != null && !userId.equals(post.getUserId())) {
                    Map<String, Object> noticeMsg = new HashMap<>();
                    noticeMsg.put("event", "collect");
                    noticeMsg.put("targetUserId", post.getUserId());
                    noticeMsg.put("operatorId", userId);
                    noticeMsg.put("relatedId", postId);
                    noticeMsg.put("relatedType", 1);
                    rabbitTemplate.convertAndSend(MQConstant.COMMUNITY_EXCHANGE, MQConstant.NOTICE_ROUTING_KEY, noticeMsg);
                }
            }
        } catch (Exception e) {
            log.error("处理收藏事件失败: {}", message, e);
        }
    }
}