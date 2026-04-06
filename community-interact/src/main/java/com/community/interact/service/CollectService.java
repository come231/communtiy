package com.community.interact.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.common.annotation.OperationLog;
import com.community.common.constant.CommonConstant;
import com.community.common.constant.MQConstant;
import com.community.common.constant.RedisKeyConstant;
import com.community.common.exception.BusinessException;
import com.community.common.result.PageResult;
import com.community.common.result.ResultCode;
import com.community.common.util.ContextHolder;
import com.community.common.util.RedisUtil;
import com.community.interact.entity.Collect;
import com.community.interact.entity.CollectFolder;
import com.community.interact.mapper.CollectFolderMapper;
import com.community.interact.mapper.CollectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class CollectService {

    private final CollectMapper collectMapper;
    private final CollectFolderMapper collectFolderMapper;
    private final RedisUtil redisUtil;
    private final RedissonClient redissonClient;
    private final RabbitTemplate rabbitTemplate;

    public boolean isCollected(Long userId, Long postId) {
        return collectMapper.selectCount(
                new LambdaQueryWrapper<Collect>()
                        .eq(Collect::getUserId, userId)
                        .eq(Collect::getPostId, postId)) > 0;
    }

    @OperationLog(module = "互动", operation = "收藏帖子")
    @SuppressWarnings("unchecked")
    public void collect(Long userId, Long postId, Long folderId) {
        String lockKey = "lock:collect:" + userId + ":" + postId;
        RLock lock = redissonClient.getLock(lockKey);
        try {
            if (!lock.tryLock(3, 10, TimeUnit.SECONDS)) {
                throw new BusinessException("操作过于频繁");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException("操作被中断");
        }
        try {
            long count = collectMapper.selectCount(
                    new LambdaQueryWrapper<Collect>()
                            .eq(Collect::getUserId, userId)
                            .eq(Collect::getPostId, postId));
            if (count > 0) throw new BusinessException(ResultCode.ALREADY_COLLECTED);

            Collect collect = new Collect();
            collect.setUserId(userId);
            collect.setPostId(postId);
            collect.setFolderId(folderId);
            collectMapper.insert(collect);

            redisUtil.increment(RedisKeyConstant.POST_COLLECT_COUNT + postId);

            Map<String, Object> msg = new HashMap<>();
            msg.put("userId", userId);
            msg.put("postId", postId);
            msg.put("action", "collect");
            msg.put("createTime", LocalDateTime.now());
            rabbitTemplate.convertAndSend(MQConstant.COMMUNITY_EXCHANGE, MQConstant.COLLECT_ROUTING_KEY, msg);

            log.info("用户{}收藏了帖子{}", userId, postId);
        } finally { lock.unlock(); }
    }

    @OperationLog(module = "互动", operation = "取消收藏")
    @SuppressWarnings("unchecked")
    public void uncollect(Long userId, Long postId) {
        collectMapper.delete(new LambdaQueryWrapper<Collect>()
                .eq(Collect::getUserId, userId).eq(Collect::getPostId, postId));
        redisUtil.decrement(RedisKeyConstant.POST_COLLECT_COUNT + postId);

        Map<String, Object> msg = new HashMap<>();
        msg.put("userId", userId); msg.put("postId", postId);
        msg.put("action", "uncollect"); msg.put("createTime", LocalDateTime.now());
        rabbitTemplate.convertAndSend(MQConstant.COMMUNITY_EXCHANGE, MQConstant.COLLECT_ROUTING_KEY, msg);
    }

    public PageResult<Collect> listCollects(Long userId, Long folderId,
                                            int pageNum, int pageSize) {
        Page<Collect> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Collect> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Collect::getUserId, userId);
        if (folderId != null) wrapper.eq(Collect::getFolderId, folderId);
        wrapper.orderByDesc(Collect::getCreateTime);
        Page<Collect> result = collectMapper.selectPage(page, wrapper);
        return new PageResult<>(result.getRecords(), result.getTotal(), result.getSize(), result.getCurrent());
    }
}
