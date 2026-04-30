package com.community.ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.community.ai.entity.AiTopic;
import com.community.ai.mapper.AiTopicMapper;
import com.community.ai.util.AiCallHelper;
import com.community.common.exception.BusinessException;
import com.community.common.result.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiTopicService {

    private final AiCallHelper aiCallHelper;
    private final AiTopicMapper aiTopicMapper;

    public List<String> generateTopics(String keywords) {
        try {
            String prompt = String.format(
                    "请根据关键词\"%s\"生成8个热门的社区话题建议，要求话题新颖、有讨论价值。" +
                    "直接输出话题名称列表，每行一个。",
                    keywords != null ? keywords : "社区热门"
            );
            String response = aiCallHelper.call(prompt);

            return List.of(response.split("\n")).stream()
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .limit(8)
                    .toList();
        } catch (Exception e) {
            log.error("AI生成话题失败, error={}", e.getMessage(), e);
            throw new RuntimeException("话题生成失败，请稍后重试");
        }
    }

    public List<AiTopic> getHotTopics() {
        LambdaQueryWrapper<AiTopic> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiTopic::getStatus, 1)
               .orderByDesc(AiTopic::getPostCount)
               .last("LIMIT 20");
        return aiTopicMapper.selectList(wrapper);
    }

    public AiTopic getTopicById(Long id) {
        AiTopic topic = aiTopicMapper.selectById(id);
        if (topic == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        return topic;
    }

    public AiTopic createTopic(String name, String description) {
        AiTopic topic = new AiTopic();
        topic.setTopicName(name);
        topic.setDescription(description);
        topic.setPostCount(0);
        topic.setStatus(1);
        topic.setCreateTime(LocalDateTime.now());
        aiTopicMapper.insert(topic);
        return topic;
    }

    public void updateTopic(Long id, String name, String description) {
        AiTopic topic = aiTopicMapper.selectById(id);
        if (topic == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        topic.setTopicName(name);
        topic.setDescription(description);
        aiTopicMapper.updateById(topic);
    }

    public void deleteTopic(Long id) {
        AiTopic topic = aiTopicMapper.selectById(id);
        if (topic == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        aiTopicMapper.deleteById(id);
    }

    public void updatePostCount(Long topicId, int delta) {
        AiTopic topic = aiTopicMapper.selectById(topicId);
        if (topic != null) {
            topic.setPostCount(topic.getPostCount() + delta);
            aiTopicMapper.updateById(topic);
        }
    }
}
