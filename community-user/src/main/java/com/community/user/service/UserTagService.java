package com.community.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.community.user.entity.UserTag;
import com.community.user.mapper.UserTagMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserTagService {

    private final UserTagMapper userTagMapper;

    public List<String> getUserTags(Long userId) {
        if (userId == null) return Collections.emptyList();
        return userTagMapper.selectTagsByUserId(userId);
    }

    @Transactional
    public void updateUserTags(Long userId, List<String> tags) {
        if (userId == null || tags == null) return;

        // 去重并过滤空标签
        List<String> cleanTags = tags.stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .limit(10) // 最多10个标签
                .collect(Collectors.toList());

        // 删除旧标签
        userTagMapper.delete(new LambdaQueryWrapper<UserTag>()
                .eq(UserTag::getUserId, userId));

        // 插入新标签
        for (String tagName : cleanTags) {
            UserTag tag = new UserTag();
            tag.setUserId(userId);
            tag.setTagName(tagName.length() > 20 ? tagName.substring(0, 20) : tagName);
            userTagMapper.insert(tag);
        }
    }

    @Transactional
    public void addUserTag(Long userId, String tagName) {
        if (userId == null || tagName == null || tagName.trim().isEmpty()) return;

        String cleanTag = tagName.trim();
        if (cleanTag.length() > 20) {
            cleanTag = cleanTag.substring(0, 20);
        }

        // 检查是否已存在
        Long count = userTagMapper.selectCount(new LambdaQueryWrapper<UserTag>()
                .eq(UserTag::getUserId, userId)
                .eq(UserTag::getTagName, cleanTag));
        if (count > 0) return;

        UserTag tag = new UserTag();
        tag.setUserId(userId);
        tag.setTagName(cleanTag);
        userTagMapper.insert(tag);
    }

    @Transactional
    public void removeUserTag(Long userId, String tagName) {
        if (userId == null || tagName == null) return;
        userTagMapper.delete(new LambdaQueryWrapper<UserTag>()
                .eq(UserTag::getUserId, userId)
                .eq(UserTag::getTagName, tagName.trim()));
    }

    /**
     * 从逗号分隔的字符串解析标签列表
     */
    public List<String> parseTags(String bioTags) {
        if (bioTags == null || bioTags.isEmpty()) return Collections.emptyList();
        return Arrays.stream(bioTags.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .collect(Collectors.toList());
    }
}
