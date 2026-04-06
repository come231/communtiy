package com.community.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.community.common.constant.CommonConstant;
import com.community.common.constant.MQConstant;
import com.community.common.exception.BusinessException;
import com.community.common.result.ResultCode;
import com.community.user.entity.User;
import com.community.user.entity.UserFollow;
import com.community.user.mapper.UserFollowMapper;
import com.community.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FollowService {

    private final UserFollowMapper userFollowMapper;
    private final UserMapper userMapper;
    private final RabbitTemplate rabbitTemplate;

    @Transactional
    public void follow(Long userId, Long followId) {
        if (userId.equals(followId)) {
            throw new BusinessException("不能关注自己");
        }
        User target = userMapper.selectById(followId);
        if (target == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        UserFollow existing = userFollowMapper.selectOne(
                new LambdaQueryWrapper<UserFollow>()
                        .eq(UserFollow::getUserId, userId)
                        .eq(UserFollow::getFollowId, followId));

        if (existing != null && existing.getStatus() == CommonConstant.FOLLOW_STATUS_FOLLOW) {
            throw new BusinessException(ResultCode.ALREADY_FOLLOWED);
        }

        if (existing != null) {
            existing.setStatus(CommonConstant.FOLLOW_STATUS_FOLLOW);
            existing.setCreateTime(LocalDateTime.now());
            userFollowMapper.updateById(existing);
        } else {
            UserFollow follow = new UserFollow();
            follow.setUserId(userId);
            follow.setFollowId(followId);
            follow.setStatus(CommonConstant.FOLLOW_STATUS_FOLLOW);
            userFollowMapper.insert(follow);
        }

        // 发送关注通知MQ
        Map<String, Object> msg = new HashMap<>();
        msg.put("userId", userId);
        msg.put("targetUserId", followId);
        msg.put("type", CommonConstant.NOTICE_TYPE_FOLLOW);
        msg.put("createTime", LocalDateTime.now());
        rabbitTemplate.convertAndSend(MQConstant.COMMUNITY_EXCHANGE, MQConstant.FOLLOW_ROUTING_KEY, msg);

        log.info("用户{}关注了用户{}", userId, followId);
    }

    @Transactional
    public void unfollow(Long userId, Long followId) {
        UserFollow existing = userFollowMapper.selectOne(
                new LambdaQueryWrapper<UserFollow>()
                        .eq(UserFollow::getUserId, userId)
                        .eq(UserFollow::getFollowId, followId));
        if (existing == null || existing.getStatus() == CommonConstant.FOLLOW_STATUS_UNFOLLOW) {
            throw new BusinessException("未关注该用户");
        }
        existing.setStatus(CommonConstant.FOLLOW_STATUS_UNFOLLOW);
        userFollowMapper.updateById(existing);
        log.info("用户{}取消关注用户{}", userId, followId);
    }

    public List<Long> getFollowingList(Long userId) {
        return userFollowMapper.selectList(
                new LambdaQueryWrapper<UserFollow>()
                        .eq(UserFollow::getUserId, userId)
                        .eq(UserFollow::getStatus, CommonConstant.FOLLOW_STATUS_FOLLOW)
                        .orderByDesc(UserFollow::getCreateTime))
                .stream().map(UserFollow::getFollowId).collect(Collectors.toList());
    }

    public List<Long> getFollowersList(Long userId) {
        return userFollowMapper.selectList(
                new LambdaQueryWrapper<UserFollow>()
                        .eq(UserFollow::getFollowId, userId)
                        .eq(UserFollow::getStatus, CommonConstant.FOLLOW_STATUS_FOLLOW)
                        .orderByDesc(UserFollow::getCreateTime))
                .stream().map(UserFollow::getUserId).collect(Collectors.toList());
    }

    public boolean isFollowing(Long userId, Long targetId) {
        return userFollowMapper.selectCount(
                new LambdaQueryWrapper<UserFollow>()
                        .eq(UserFollow::getUserId, userId)
                        .eq(UserFollow::getFollowId, targetId)
                        .eq(UserFollow::getStatus, CommonConstant.FOLLOW_STATUS_FOLLOW)) > 0;
    }
}
