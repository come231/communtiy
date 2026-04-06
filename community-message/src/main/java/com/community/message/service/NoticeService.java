package com.community.message.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.common.constant.RedisKeyConstant;
import com.community.common.result.PageResult;
import com.community.common.util.ContextHolder;
import com.community.message.entity.Notice;
import com.community.message.mapper.NoticeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeMapper noticeMapper;
    private final StringRedisTemplate redisTemplate;

    /**
     * 分页查询当前用户通知
     */
    public PageResult<Notice> getNotices(Integer type, int pageNum, int pageSize) {
        Long userId = ContextHolder.getUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }
        Page<Notice> pageParam = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Notice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notice::getUserId, userId);
        if (type != null) {
            wrapper.eq(Notice::getType, type);
        }
        wrapper.orderByDesc(Notice::getCreateTime);
        Page<Notice> result = noticeMapper.selectPage(pageParam, wrapper);
        return new PageResult<>(result.getRecords(), result.getTotal(), result.getSize(), result.getCurrent());
    }

    /**
     * 删除通知
     */
    public void deleteNotice(Long noticeId) {
        Long userId = ContextHolder.getUserId();
        Notice notice = noticeMapper.selectById(noticeId);
        if (notice != null && notice.getUserId().equals(userId)) {
            if (!notice.getIsRead()) {
                decrementUnreadCount(userId);
            }
            noticeMapper.deleteById(noticeId);
        }
    }

    /**
     * 标记单条通知已读
     */
    public void markAsRead(Long noticeId) {
        if (noticeId == null) {
            throw new IllegalArgumentException("通知ID不能为空");
        }
        Long userId = ContextHolder.getUserId();
        Notice notice = noticeMapper.selectById(noticeId);
        if (notice == null) {
            throw new RuntimeException("通知不存在");
        }
        if (!notice.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此通知");
        }
        Notice update = new Notice();
        update.setId(noticeId);
        update.setIsRead(true);
        noticeMapper.updateById(update);
        // 更新Redis未读计数
        decrementUnreadCount(userId);
    }

    /**
     * 全部标记已读
     */
    public void markAllRead() {
        Long userId = ContextHolder.getUserId();
        Notice update = new Notice();
        update.setIsRead(true);
        LambdaQueryWrapper<Notice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notice::getUserId, userId).eq(Notice::getIsRead, false);
        noticeMapper.update(update, wrapper);
        // 清除Redis缓存
        String key = RedisKeyConstant.USER_UNREAD_NOTICE + userId;
        redisTemplate.delete(key);
    }

    /**
     * 获取未读通知数(Redis缓存优化)
     */
    public long getUnreadCount() {
        Long userId = ContextHolder.getUserId();
        String key = RedisKeyConstant.USER_UNREAD_NOTICE + userId;
        String cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            return Long.parseLong(cached);
        }
        LambdaQueryWrapper<Notice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notice::getUserId, userId).eq(Notice::getIsRead, false);
        Long count = noticeMapper.selectCount(wrapper);
        // 缓存5分钟
        redisTemplate.opsForValue().set(key, String.valueOf(count), 5, TimeUnit.MINUTES);
        return count;
    }

    // =====================================================================
    // 🔥 🔥 🔥 这是【完全匹配你数据表】的最终版创建方法
    // =====================================================================
    public void createNotice(Long userId, Integer type, String content, Long fromUid, Long targetId) {
        if (userId == null || content == null) {
            log.warn("创建通知参数不完整: userId={}, content={}", userId, content);
            return;
        }
        Notice notice = new Notice();
        notice.setUserId(userId);
        notice.setType(type);
        notice.setContent(content);
        notice.setFromUid(fromUid == null ? 0L : fromUid);
        notice.setTargetId(targetId == null ? 0L : targetId);
        notice.setIsRead(false);
        // ✅ 完全匹配你的表：user_id, type, content, from_uid, target_id
        noticeMapper.insert(notice);

        incrementUnreadCount(userId);
        log.info("创建通知成功: userId={}, type={}, content={}", userId, type, content);
    }

    private void incrementUnreadCount(Long userId) {
        String key = RedisKeyConstant.USER_UNREAD_NOTICE + userId;
        redisTemplate.opsForValue().increment(key);
    }

    private void decrementUnreadCount(Long userId) {
        String key = RedisKeyConstant.USER_UNREAD_NOTICE + userId;
        Long count = redisTemplate.opsForValue().decrement(key);
        if (count != null && count <= 0) {
            redisTemplate.delete(key);
        }
    }
}