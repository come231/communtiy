package com.community.search.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostSyncConsumer {

    private final com.community.search.service.SearchService searchService;
    private final ObjectMapper objectMapper;

    /**
     * 监听帖子同步队列，处理帖子新增/更新/删除事件
     */
    @RabbitListener(queues = "community.search.sync.queue")
    public void handlePostSync(Map<String, Object> message) {
        try {
            log.info("收到帖子同步消息: {}", message);

            String action = (String) message.get("action");

            if ("DELETE".equalsIgnoreCase(action)) {
                // 删除帖子
                Number postIdNum = (Number) message.get("id");
                if (postIdNum != null) {
                    searchService.deletePostFromEs(postIdNum.longValue());
                }
            } else {
                // 新增或更新：同步到ES
                // 转换为ES文档格式
                Map<String, Object> esDoc = convertToEsDocument(message);
                searchService.syncPostToEs(esDoc);
            }
        } catch (Exception e) {
            log.error("处理帖子同步消息失败", e);
        }
    }

    private Map<String, Object> convertToEsDocument(Map<String, Object> source) {
        Map<String, Object> doc = new HashMap<>();
        doc.put("id", source.get("id"));
        doc.put("title", source.get("title"));
        doc.put("content", source.get("content"));
        doc.put("summary", source.get("summary"));
        doc.put("authorId", source.get("authorId"));
        doc.put("authorName", source.get("authorName"));
        doc.put("sectionId", source.get("sectionId"));
        doc.put("sectionName", source.get("sectionName"));
        doc.put("status", source.get("status") != null ? Integer.valueOf(source.get("status").toString()) : 0);
        doc.put("createTime", source.get("createTime"));
        doc.put("viewCount", source.get("viewCount") != null ? Integer.valueOf(source.get("viewCount").toString()) : 0);
        doc.put("likeCount", source.get("likeCount") != null ? Integer.valueOf(source.get("likeCount").toString()) : 0);
        return doc;
    }
}
