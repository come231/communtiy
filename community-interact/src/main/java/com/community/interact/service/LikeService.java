package com.community.interact.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.common.annotation.OperationLog;
import com.community.common.constant.CommonConstant;
import com.community.common.dto.UserLikedPostItemDTO;
import com.community.common.result.PageResult;
import com.community.common.constant.MQConstant;
import com.community.common.constant.RedisKeyConstant;
import com.community.common.exception.BusinessException;
import com.community.common.result.ResultCode;
import com.community.common.util.ContextHolder;
import com.community.common.util.RedisUtil;
import com.community.interact.entity.LikeRecord;
import com.community.interact.mapper.LikeRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeRecordMapper likeRecordMapper;
    private final RedisUtil redisUtil;
    private final RedissonClient redissonClient;
    private final RabbitTemplate rabbitTemplate;

    public boolean isLiked(Long userId, Integer type, Long targetId) {
        return likeRecordMapper.selectCount(
                new LambdaQueryWrapper<LikeRecord>()
                        .eq(LikeRecord::getUserId, userId)
                        .eq(LikeRecord::getType, type)
                        .eq(LikeRecord::getTargetId, targetId)) > 0;
    }

    /**
     * 分页查询用户点赞的帖子记录（type=帖子），供用户服务等通过 Feign 调用
     */
    public PageResult<UserLikedPostItemDTO> pageUserPostLikes(Long userId, int pageNum, int pageSize) {
        Page<LikeRecord> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<LikeRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LikeRecord::getUserId, userId)
                .eq(LikeRecord::getType, CommonConstant.LIKE_TYPE_POST)
                .orderByDesc(LikeRecord::getCreateTime);
        Page<LikeRecord> result = likeRecordMapper.selectPage(page, wrapper);
        List<UserLikedPostItemDTO> list = result.getRecords().stream()
                .map(r -> {
                    UserLikedPostItemDTO dto = new UserLikedPostItemDTO();
                    dto.setPostId(r.getTargetId());
                    dto.setLikeTime(r.getCreateTime());
                    return dto;
                })
                .collect(Collectors.toList());
        return new PageResult<>(list, result.getTotal(), result.getSize(), result.getCurrent());
    }

    @OperationLog(module = "互动", operation = "点赞")
    @SuppressWarnings("unchecked")
    public void like(Long userId, Integer type, Long targetId) {
        String lockKey = "lock:like:" + type + ":" + userId + ":" + targetId;
        RLock lock = redissonClient.getLock(lockKey);
        try {
            if (!lock.tryLock(3, 10, TimeUnit.SECONDS)) {
                throw new BusinessException("操作过于频繁，请稍后再试");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException("操作被中断");
        }
        try {
            // 幂等检查
            long count = likeRecordMapper.selectCount(
                    new LambdaQueryWrapper<LikeRecord>()
                            .eq(LikeRecord::getUserId, userId)
                            .eq(LikeRecord::getType, type)
                            .eq(LikeRecord::getTargetId, targetId));
            if (count > 0) throw new BusinessException(ResultCode.ALREADY_LIKED);

            // 写入数据库
            LikeRecord record = new LikeRecord();
            record.setUserId(userId);
            record.setType(type);
            record.setTargetId(targetId);
            likeRecordMapper.insert(record);

            // 更新Redis计数
            String key = (type == CommonConstant.LIKE_TYPE_POST) ?
                    RedisKeyConstant.POST_LIKE_COUNT + targetId :
                    RedisKeyConstant.POST_COMMENT_COUNT + ":like:" + targetId;
            redisUtil.increment(key);

            // 发送MQ通知（community服务消费后更新计数并发送通知）
            Map<String, Object> msg = new HashMap<>();
            msg.put("userId", userId);
            msg.put("targetType", type);
            msg.put("targetId", targetId);
            msg.put("action", "like");
            msg.put("createTime", LocalDateTime.now());
            rabbitTemplate.convertAndSend(MQConstant.COMMUNITY_EXCHANGE, MQConstant.LIKE_ROUTING_KEY, msg);

            log.info("用户{}点赞: type={}, id={}", userId, type, targetId);
        } finally {
            lock.unlock();
        }
    }

    @OperationLog(module = "互动", operation = "取消点赞")
    @SuppressWarnings("unchecked")
    public void unlike(Long userId, Integer type, Long targetId) {
        String lockKey = "lock:unlike:" + type + ":" + userId + ":" + targetId;
        RLock lock = redissonClient.getLock(lockKey);
        try {
            if (!lock.tryLock(3, 10, TimeUnit.SECONDS)) return;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }
        try {

            likeRecordMapper.delete(new LambdaQueryWrapper<LikeRecord>()
                    .eq(LikeRecord::getUserId, userId)
                    .eq(LikeRecord::getType, type)
                    .eq(LikeRecord::getTargetId, targetId));

            String key = (type == CommonConstant.LIKE_TYPE_POST) ?
                    RedisKeyConstant.POST_LIKE_COUNT + targetId :
                    RedisKeyConstant.POST_COMMENT_COUNT + ":like:" + targetId;
            redisUtil.decrement(key);

            Map<String, Object> msg = new HashMap<>();
            msg.put("userId", userId);
            msg.put("targetType", type);
            msg.put("targetId", targetId);
            msg.put("action", "unlike");
            rabbitTemplate.convertAndSend(MQConstant.COMMUNITY_EXCHANGE, MQConstant.LIKE_ROUTING_KEY, msg);

            log.info("用户{}取消点赞: type={}, id={}", userId, type, targetId);
        } finally {
            lock.unlock();
        }
    }
}
