package com.community.community.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.common.result.PageResult;
import com.community.community.entity.SensitiveFilterRecord;
import com.community.community.mapper.SensitiveFilterRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SensitiveFilterRecordService {

    private final SensitiveFilterRecordMapper sensitiveFilterRecordMapper;

    public void saveCommentRecord(Long commentId, Long userId, String username, String originalContent, String filteredContent, List<String> hits) {
        SensitiveFilterRecord record = new SensitiveFilterRecord();
        record.setCommentId(commentId);
        record.setUserId(userId);
        record.setUsername(StringUtils.hasText(username) ? username : "用户" + userId);
        record.setOriginalContent(originalContent);
        record.setFilteredContent(filteredContent);
        record.setHitWords(String.join(",", normalizeHits(hits)));
        record.setCreateTime(LocalDateTime.now());
        sensitiveFilterRecordMapper.insert(record);
    }

    public PageResult<SensitiveFilterRecord> pageRecords(int pageNum, int pageSize, String keyword, Long userId) {
        Page<SensitiveFilterRecord> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SensitiveFilterRecord> wrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            wrapper.eq(SensitiveFilterRecord::getUserId, userId);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(SensitiveFilterRecord::getOriginalContent, keyword)
                    .or().like(SensitiveFilterRecord::getFilteredContent, keyword)
                    .or().like(SensitiveFilterRecord::getHitWords, keyword)
                    .or().like(SensitiveFilterRecord::getUsername, keyword));
        }
        wrapper.orderByDesc(SensitiveFilterRecord::getCreateTime);
        Page<SensitiveFilterRecord> result = sensitiveFilterRecordMapper.selectPage(page, wrapper);
        return new PageResult<>(result.getRecords(), result.getTotal(), result.getSize(), result.getCurrent());
    }

    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        List<SensitiveFilterRecord> all = sensitiveFilterRecordMapper.selectList(
                new LambdaQueryWrapper<SensitiveFilterRecord>()
                        .select(SensitiveFilterRecord::getHitWords, SensitiveFilterRecord::getCreateTime));

        Map<String, Long> totalWordHits = new HashMap<>();
        Map<String, Long> todayWordHits = new HashMap<>();
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);

        long totalHits = 0;
        long todayHits = 0;
        for (SensitiveFilterRecord record : all) {
            List<String> words = parseHitWords(record.getHitWords());
            totalHits += words.size();
            for (String w : words) {
                totalWordHits.put(w, totalWordHits.getOrDefault(w, 0L) + 1L);
            }
            if (record.getCreateTime() != null && !record.getCreateTime().isBefore(todayStart)) {
                todayHits += words.size();
                for (String w : words) {
                    todayWordHits.put(w, todayWordHits.getOrDefault(w, 0L) + 1L);
                }
            }
        }

        stats.put("totalHits", totalHits);
        stats.put("todayHits", todayHits);
        stats.put("wordTotalHits", totalWordHits);
        stats.put("wordTodayHits", todayWordHits);
        return stats;
    }

    private List<String> parseHitWords(String hitWords) {
        if (!StringUtils.hasText(hitWords)) {
            return Collections.emptyList();
        }
        String[] arr = hitWords.split(",");
        List<String> words = new ArrayList<>(arr.length);
        for (String item : arr) {
            if (StringUtils.hasText(item)) {
                words.add(item.trim());
            }
        }
        return words;
    }

    private List<String> normalizeHits(List<String> hits) {
        if (hits == null || hits.isEmpty()) {
            return Collections.emptyList();
        }
        LinkedHashSet<String> set = new LinkedHashSet<>();
        for (String hit : hits) {
            if (StringUtils.hasText(hit)) {
                set.add(hit.trim());
            }
        }
        return new ArrayList<>(set);
    }
}
