package com.community.message.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.common.constant.RedisKeyConstant;
import com.community.common.result.PageResult;
import com.community.common.util.ContextHolder;
import com.community.message.entity.ChatMsg;
import com.community.message.feign.UserFeignClient;
import com.community.message.mapper.ChatMsgMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMsgService {

    private final ChatMsgMapper chatMsgMapper;
    private final StringRedisTemplate redisTemplate;
    private final UserFeignClient userFeignClient;
    /**
     * 获取与某用户的聊天记录(分页)
     */
    public PageResult<ChatMsg> getChatList(Long otherUserId, int page, int size) {
        Long currentUserId = ContextHolder.getUserId();
        if (otherUserId == null) {
            throw new IllegalArgumentException("对方用户ID不能为空");
        }
        markChatRead(otherUserId);
        Page<ChatMsg> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<ChatMsg> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.eq(ChatMsg::getFromUserId, currentUserId).eq(ChatMsg::getToUserId, otherUserId))
               .or(o -> o.eq(ChatMsg::getFromUserId, otherUserId).eq(ChatMsg::getToUserId, currentUserId))
               .orderByAsc(ChatMsg::getCreateTime);
        Page<ChatMsg> result = chatMsgMapper.selectPage(pageParam, wrapper);
        return new PageResult<>(result.getRecords(), result.getTotal(), result.getSize(), result.getCurrent());
    }

    /**
     * 获取最近联系人列表(LAST_MSG+UNREAD_COUNT)
     */
    public List<Map<String, Object>> getChatUserList() {
        Long currentUserId = ContextHolder.getUserId();
        // 查询所有与当前用户相关的消息，按用户分组，取最新一条
        // 先查最近联系人的ID列表
        List<Long> contactUserIds = chatMsgMapper.selectList(
                new LambdaQueryWrapper<ChatMsg>()
                        .and(w -> w.eq(ChatMsg::getFromUserId, currentUserId)
                                .or(o -> o.eq(ChatMsg::getToUserId, currentUserId)))
                        .select(ChatMsg::getFromUserId, ChatMsg::getToUserId)
        ).stream().map(msg -> {
                    if (msg.getFromUserId().equals(currentUserId)) return msg.getToUserId();
                    return msg.getFromUserId();
                }).distinct()
                .collect(Collectors.toList());

        return contactUserIds.stream().map(otherId -> {
                    // 获取最新消息
                    LambdaQueryWrapper<ChatMsg> lastMsgWrapper = new LambdaQueryWrapper<>();
                    lastMsgWrapper.and(w -> w.eq(ChatMsg::getFromUserId, currentUserId).eq(ChatMsg::getToUserId, otherId))
                            .or(o -> o.eq(ChatMsg::getFromUserId, otherId).eq(ChatMsg::getToUserId, currentUserId))
                            .orderByDesc(ChatMsg::getCreateTime)
                            .last("LIMIT 1");
                    ChatMsg lastMsg = chatMsgMapper.selectOne(lastMsgWrapper);

                    // 获取该用户的未读数
                    String unreadKey = RedisKeyConstant.USER_UNREAD_CHAT + currentUserId + ":" + otherId;
                    String unreadStr = redisTemplate.opsForValue().get(unreadKey);
                    long unreadCount = unreadStr != null ? Long.parseLong(unreadStr) : 0;

                    Map<String, Object> map = new HashMap<>();
                    map.put("otherUserId", otherId);
                    map.put("lastMessage", lastMsg != null ? lastMsg.getContent() : "");
                    map.put("lastTime", lastMsg != null ? lastMsg.getCreateTime() : null);
                    map.put("unreadCount", unreadCount);
                    map.put("nickName",userFeignClient.getUserInfo(otherId).getData().get("nickname"));
                    map.put("avatar",userFeignClient.getUserInfo(otherId).getData().get("avatar"));
                    return map;
                }).collect(Collectors.toList());
    }

    /**
     * 发送私信
     */
    public void sendChat(Long toUserId, String content) {
        Long fromUserId = ContextHolder.getUserId();
        if (toUserId == null) {
            throw new IllegalArgumentException("接收者ID不能为空");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("消息内容不能为空");
        }
        ChatMsg chatMsg = new ChatMsg();
        chatMsg.setFromUserId(fromUserId);
        chatMsg.setToUserId(toUserId);
        chatMsg.setContent(content.trim());
        chatMsgMapper.insert(chatMsg);
        // 增加接收者的未读计数
        String key = RedisKeyConstant.USER_UNREAD_CHAT + toUserId + ":" + fromUserId;
        redisTemplate.opsForValue().increment(key);
        log.info("发送私信成功: from={} to={}", fromUserId, toUserId);
    }

    /**
     * 标记某用户消息已读
     */
    public void markChatRead(Long fromUserId) {
        Long currentUserId = ContextHolder.getUserId();
        if (fromUserId == null) {
            throw new IllegalArgumentException("发送者ID不能为空");
        }
        ChatMsg update = new ChatMsg();
        update.setIsRead(true);
        LambdaQueryWrapper<ChatMsg> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMsg::getFromUserId, fromUserId)
               .eq(ChatMsg::getToUserId, currentUserId)
               .eq(ChatMsg::getIsRead, false);
        chatMsgMapper.update(update, wrapper);
        // 清除Redis未读缓存
        String key = RedisKeyConstant.USER_UNREAD_CHAT + currentUserId + ":" + fromUserId;
        redisTemplate.delete(key);
    }

    /**
     * 获取未读私信总数
     */
    public long getUnreadChatCount() {
        Long currentUserId = ContextHolder.getUserId();
        long totalCount = 0;
        // 遍历所有未读key
        String pattern = RedisKeyConstant.USER_UNREAD_CHAT + currentUserId + ":*";
        var keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            for (Object keyObj : keys) {
                String key = keyObj.toString();
                String val = redisTemplate.opsForValue().get(key);
                if (val != null) {
                    totalCount += Long.parseLong(val);
                }
            }
        }
        return totalCount;
    }
}
