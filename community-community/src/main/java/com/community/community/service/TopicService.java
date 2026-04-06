package com.community.community.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.community.common.exception.BusinessException;
import com.community.common.result.ResultCode;
import com.community.common.util.ContextHolder;
import com.community.community.entity.Topic;
import com.community.community.entity.UserTopic;
import com.community.community.mapper.TopicMapper;
import com.community.community.mapper.UserTopicMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class TopicService {

    private final TopicMapper topicMapper;
    private final UserTopicMapper userTopicMapper;
    private final RabbitTemplate rabbitTemplate;

    public List<Topic> listAllTopics() {
        return topicMapper.selectList(
                new LambdaQueryWrapper<Topic>()
                        .eq(Topic::getStatus, 1).and(
                                w -> w.eq(Topic::getIsHot, 1).or()
                                        .eq(Topic::getIsRecommend, 1))
                        .orderByDesc(Topic::getIsHot)
                        .orderByAsc(Topic::getSort));
    }

    public List<Topic> listHotTopics() {
        return topicMapper.selectList(
                new LambdaQueryWrapper<Topic>()
                        .eq(Topic::getStatus, 1)
                        .eq(Topic::getIsHot, 1)
                        .orderByDesc(Topic::getPostCount));
    }

    public Topic getTopicDetail(Long topicId) {
        Topic topic = topicMapper.selectById(topicId);
        if (topic == null) throw new BusinessException(ResultCode.TOPIC_NOT_FOUND);
        return topic;
    }

    @Transactional
    public void followTopic(Long userId, Long topicId) {
        Topic topic = topicMapper.selectById(topicId);
        if (topic == null) throw new BusinessException(ResultCode.TOPIC_NOT_FOUND);

        UserTopic existing = userTopicMapper.selectOne(
                new LambdaQueryWrapper<UserTopic>()
                        .eq(UserTopic::getUserId, userId)
                        .eq(UserTopic::getTopicId, topicId));

        if (existing != null) {
            existing.setStatus(1);
            userTopicMapper.updateById(existing);
        } else {
            UserTopic ut = new UserTopic();
            ut.setUserId(userId);
            ut.setTopicId(topicId);
            ut.setStatus(1);
            userTopicMapper.insert(ut);
        }

        // 更新关注数
        topic.setFollowCount(topic.getFollowCount() + 1);
        topicMapper.updateById(topic);
    }

    @Transactional
    public void unfollowTopic(Long userId, Long topicId) {
        UserTopic existing = userTopicMapper.selectOne(
                new LambdaQueryWrapper<UserTopic>()
                        .eq(UserTopic::getUserId, userId)
                        .eq(UserTopic::getTopicId, topicId));
        if (existing != null) {
            existing.setStatus(0);
            userTopicMapper.updateById(existing);
            Topic topic = topicMapper.selectById(topicId);
            if (topic != null && topic.getFollowCount() > 0) {
                topic.setFollowCount(Math.max(0, topic.getFollowCount() - 1));
                topicMapper.updateById(topic);
            }
        }
    }

    public boolean isFollowingTopic(Long userId, Long topicId) {
        return userTopicMapper.selectCount(
                new LambdaQueryWrapper<UserTopic>()
                        .eq(UserTopic::getUserId, userId)
                        .eq(UserTopic::getTopicId, topicId)
                        .eq(UserTopic::getStatus, 1)) > 0;
    }
}
