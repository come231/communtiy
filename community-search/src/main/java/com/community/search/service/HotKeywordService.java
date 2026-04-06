package com.community.search.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.community.common.constant.RedisKeyConstant;
import com.community.search.entity.SearchHotKeyword;
import com.community.search.mapper.SearchHotKeywordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HotKeywordService {

    private final SearchHotKeywordMapper hotKeywordMapper;
    private final org.springframework.data.redis.core.StringRedisTemplate redisTemplate;

    /**
     * 搜索次数+1或新建记录
     */
    @Transactional
    public void incrementCount(String keyword) {
        LambdaQueryWrapper<SearchHotKeyword> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SearchHotKeyword::getKeyword, keyword);
        SearchHotKeyword existing = hotKeywordMapper.selectOne(wrapper);

        LocalDateTime now = LocalDateTime.now();
        if (existing != null) {
            // 更新现有记录
            LambdaUpdateWrapper<SearchHotKeyword> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(SearchHotKeyword::getId, existing.getId())
                    .set(SearchHotKeyword::getSearchCount, existing.getSearchCount() + 1)
                    .set(SearchHotKeyword::getUpdateTime, now);
            hotKeywordMapper.update(null, updateWrapper);

            // 更新Redis缓存
            redisTemplate.opsForZSet().incrementScore(
                    RedisKeyConstant.SEARCH_HOT_KEYWORDS, keyword, 1);
        } else {
            // 新建记录
            SearchHotKeyword hotKeyword = new SearchHotKeyword();
            hotKeyword.setKeyword(keyword);
            hotKeyword.setSearchCount(1);
            hotKeyword.setRank(1);
            hotKeyword.setUpdateTime(now);
            hotKeywordMapper.insert(hotKeyword);

            // 添加到Redis缓存
            redisTemplate.opsForZSet().add(RedisKeyConstant.SEARCH_HOT_KEYWORDS, keyword, 1);
        }
        log.debug("热搜关键词计数更新: keyword={}", keyword);
    }

    /**
     * 按searchCount降序获取热搜TOP N
     */
    public List<SearchHotKeyword> getTopKeywords(int limit) {
        LambdaQueryWrapper<SearchHotKeyword> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(SearchHotKeyword::getSearchCount);
        wrapper.last("LIMIT " + limit);

        List<SearchHotKeyword> list = hotKeywordMapper.selectList(wrapper);

        // 设置排名
        for (int i = 0; i < list.size(); i++) {
            list.get(i).setRank(i + 1);
        }

        // 同步刷新Redis缓存
        refreshRedisCache(list);

        return list;
    }

    /**
     * 刷新Redis热搜缓存
     */
    private void refreshRedisCache(List<SearchHotKeyword> keywords) {
        String cacheKey = RedisKeyConstant.SEARCH_HOT_KEYWORDS;
        // 先删除旧的缓存数据
        redisTemplate.delete(cacheKey);

        // 重新写入
        keywords.forEach(kw ->
                redisTemplate.opsForZSet().add(cacheKey, kw.getKeyword(), kw.getSearchCount()));
    }
}
