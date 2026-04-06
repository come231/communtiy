package com.community.ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.community.ai.entity.UserInterest;
import com.community.ai.mapper.UserInterestMapper;
import com.community.ai.util.AiCallHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiRecommendService {

    private final AiCallHelper aiCallHelper;
    private final UserInterestMapper userInterestMapper;
    private final StringRedisTemplate redisTemplate;

    private static final String INTEREST_KEY = "ai:user:interest:";

    public String getRecommendPosts(Long userId) {
        try {
            LambdaQueryWrapper<UserInterest> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(UserInterest::getUserId, userId)
                   .orderByDesc(UserInterest::getWeight)
                   .last("LIMIT 10");
            List<UserInterest> interests = userInterestMapper.selectList(wrapper);

            if (interests.isEmpty()) {
                return "暂无推荐，请先设置您的兴趣偏好";
            }

            String tags = interests.stream()
                    .map(UserInterest::getTag)
                    .collect(Collectors.joining(","));

            String prompt = String.format(
                    "用户的兴趣标签为: %s。请为该用户推荐5个相关的社区话题或内容方向，每个附带简短理由。" +
                    "以JSON数组格式返回。",
                    tags
            );
            return aiCallHelper.call(prompt);
        } catch (Exception e) {
            log.error("获取推荐帖子失败, userId={}, error={}", userId, e.getMessage(), e);
            throw new RuntimeException("推荐服务暂时不可用，请稍后重试");
        }
    }

    public void updateUserInterests(Long userId, List<String> tags) {
        LambdaQueryWrapper<UserInterest> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserInterest::getUserId, userId);
        userInterestMapper.delete(wrapper);

        for (String tag : tags) {
            UserInterest interest = new UserInterest();
            interest.setUserId(userId);
            interest.setTag(tag);
            interest.setWeight(1.0);
            interest.setUpdateTime(LocalDateTime.now());
            userInterestMapper.insert(interest);
        }

        String key = INTEREST_KEY + userId;
        redisTemplate.opsForValue().set(key, String.join(",", tags));
    }

    public String analyzeUserPreferences(Long userId) {
        try {
            LambdaQueryWrapper<UserInterest> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(UserInterest::getUserId, userId)
                   .orderByDesc(UserInterest::getWeight);
            List<UserInterest> interests = userInterestMapper.selectList(wrapper);

            if (interests.isEmpty()) {
                return "暂无足够数据进行分析，请多浏览和使用社区功能";
            }

            String profile = interests.stream()
                    .map(i -> i.getTag() + "(权重:" + i.getWeight() + ")")
                    .collect(Collectors.joining(", "));

            String prompt = String.format(
                    "根据以下用户兴趣数据分析用户偏好并给出建议：\n%s\n" +
                    "请输出：1. 用户画像描述 2. 推荐的内容方向 3. 可能感兴趣的话题",
                    profile
            );
            return aiCallHelper.call(prompt);
        } catch (Exception e) {
            log.error("分析用户偏好失败, userId={}, error={}", userId, e.getMessage(), e);
            throw new RuntimeException("偏好分析失败，请稍后重试");
        }
    }
}
