package com.community.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.admin.entity.SensitiveWord;
import com.community.admin.feign.SensitiveFilterFeignClient;
import com.community.admin.mapper.security.SensitiveWordMapper;
import com.community.admin.vo.SensitiveFilterRecordVO;
import com.community.admin.vo.SensitiveWordStatsVO;
import com.community.admin.vo.SensitiveWordVO;
import com.community.common.result.PageResult;
import com.community.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class SensitiveWordService {

    private final SensitiveWordMapper sensitiveWordMapper;
    private final SensitiveFilterFeignClient sensitiveFilterFeignClient;

    @Transactional
    public SensitiveWord addSensitiveWord(String word, Integer level, Integer type, String replacement) {
        if (!StringUtils.hasText(word)) {
            throw new RuntimeException("敏感词不能为空");
        }

        Long count = sensitiveWordMapper.selectCount(
                new LambdaQueryWrapper<SensitiveWord>()
                        .eq(SensitiveWord::getWord, word.trim()));
        if (count > 0) {
            throw new RuntimeException("敏感词已存在");
        }

        SensitiveWord sensitiveWord = new SensitiveWord();
        sensitiveWord.setWord(word.trim());
        sensitiveWord.setType(type != null ? type : 0);
        sensitiveWord.setLevel(level != null ? level : 1);
        sensitiveWord.setReplaceWord(StringUtils.hasText(replacement) ? replacement.trim() : "***");
        sensitiveWord.setStatus(1);
        sensitiveWord.setCreateTime(LocalDateTime.now());
        sensitiveWord.setUpdateTime(LocalDateTime.now());

        sensitiveWordMapper.insert(sensitiveWord);
        return sensitiveWord;
    }

    @Transactional
    public boolean updateSensitiveWord(Long id, String word, Integer level, Integer type, String replacement, Integer status) {
        SensitiveWord sensitiveWord = sensitiveWordMapper.selectById(id);
        if (sensitiveWord == null) {
            throw new RuntimeException("敏感词不存在");
        }

        if (StringUtils.hasText(word)) {
            sensitiveWord.setWord(word.trim());
        }
        if (level != null) {
            sensitiveWord.setLevel(level);
        }
        if (type != null) {
            sensitiveWord.setType(type);
        }
        if (StringUtils.hasText(replacement)) {
            sensitiveWord.setReplaceWord(replacement.trim());
        }
        if (status != null) {
            sensitiveWord.setStatus(status);
        }
        sensitiveWord.setUpdateTime(LocalDateTime.now());
        return sensitiveWordMapper.updateById(sensitiveWord) > 0;
    }

    @Transactional
    public boolean deleteSensitiveWord(Long id) {
        return sensitiveWordMapper.deleteById(id) > 0;
    }

    public IPage<SensitiveWordVO> getSensitiveWords(int page, int size, String keyword, Integer level, Integer type) {
        Page<SensitiveWord> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<SensitiveWord> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(SensitiveWord::getWord, keyword);
        }
        if (level != null) {
            wrapper.eq(SensitiveWord::getLevel, level);
        }
        if (type != null) {
            wrapper.eq(SensitiveWord::getType, type);
        }
        wrapper.orderByAsc(SensitiveWord::getType)
                .orderByAsc(SensitiveWord::getLevel)
                .orderByDesc(SensitiveWord::getCreateTime);

        Page<SensitiveWord> entityPage = sensitiveWordMapper.selectPage(pageObj, wrapper);
        Map<String, Object> statsMap = safeGetStats();
        Map<String, Long> totalHitMap = toLongMap(statsMap.get("wordTotalHits"));
        Map<String, Long> todayHitMap = toLongMap(statsMap.get("wordTodayHits"));

        Page<SensitiveWordVO> voPage = new Page<>(page, size);
        voPage.setTotal(entityPage.getTotal());
        voPage.setCurrent(entityPage.getCurrent());
        voPage.setSize(entityPage.getSize());
        voPage.setRecords(entityPage.getRecords().stream().map(word -> {
            SensitiveWordVO vo = new SensitiveWordVO();
            vo.setId(word.getId());
            vo.setWord(word.getWord());
            vo.setLevel(word.getLevel());
            vo.setType(word.getType());
            vo.setCategory(typeToCategory(word.getType()));
            vo.setReplacement(word.getReplaceWord());
            vo.setStatus(word.getStatus());
            vo.setEnabled(word.getStatus() == null || word.getStatus() == 1);
            vo.setHitCount(totalHitMap.getOrDefault(word.getWord(), 0L));
            vo.setTodayHits(todayHitMap.getOrDefault(word.getWord(), 0L));
            vo.setCreateTime(word.getCreateTime());
            vo.setUpdateTime(word.getUpdateTime());
            return vo;
        }).collect(Collectors.toList()));
        return voPage;
    }

    public List<String> listAllActiveWordStrings() {
        List<SensitiveWord> list = sensitiveWordMapper.selectList(
                new LambdaQueryWrapper<SensitiveWord>()
                        .eq(SensitiveWord::getStatus, 1)
                        .select(SensitiveWord::getWord));
        return list.stream()
                .map(SensitiveWord::getWord)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(StringUtils::hasText)
                .distinct()
                .collect(Collectors.toList());
    }

    @Transactional
    public int importSensitiveWords(List<String> words) {
        if (words == null || words.isEmpty()) {
            return 0;
        }
        int successCount = 0;
        for (String word : words) {
            if (!StringUtils.hasText(word)) continue;
            word = word.trim();
            Long count = sensitiveWordMapper.selectCount(
                    new LambdaQueryWrapper<SensitiveWord>().eq(SensitiveWord::getWord, word));
            if (count > 0) continue;

            SensitiveWord sensitiveWord = new SensitiveWord();
            sensitiveWord.setWord(word);
            sensitiveWord.setType(0);
            sensitiveWord.setLevel(1);
            sensitiveWord.setReplaceWord("***");
            sensitiveWord.setStatus(1);
            sensitiveWord.setCreateTime(LocalDateTime.now());
            sensitiveWord.setUpdateTime(LocalDateTime.now());
            try {
                sensitiveWordMapper.insert(sensitiveWord);
                successCount++;
            } catch (Exception ignored) {
            }
        }
        return successCount;
    }

    public PageResult<SensitiveFilterRecordVO> getFilterRecords(int pageNum, int pageSize, String keyword, Long userId) {
        Result<PageResult<Map<String, Object>>> result = sensitiveFilterFeignClient.listRecords(pageNum, pageSize, keyword, userId);
        if (result == null || !result.isSuccess() || result.getData() == null) {
            return new PageResult<>(List.of(), 0, pageSize, pageNum);
        }
        PageResult<Map<String, Object>> source = result.getData();
        List<SensitiveFilterRecordVO> records = source.getRecords().stream().map(this::toFilterRecordVO).collect(Collectors.toList());
        return new PageResult<>(records, source.getTotal(), source.getSize(), source.getCurrent());
    }

    public SensitiveWordStatsVO getStats() {
        SensitiveWordStatsVO statsVO = new SensitiveWordStatsVO();
        statsVO.setTotalWords(sensitiveWordMapper.selectCount(null));
        Map<String, Object> remoteStats = safeGetStats();
        statsVO.setTotalHits(asLong(remoteStats.get("totalHits")));
        statsVO.setTodayHits(asLong(remoteStats.get("todayHits")));
        LocalDateTime start = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        statsVO.setTodayNew(sensitiveWordMapper.selectCount(
                new LambdaQueryWrapper<SensitiveWord>().ge(SensitiveWord::getCreateTime, start)
        ));
        return statsVO;
    }

    private SensitiveFilterRecordVO toFilterRecordVO(Map<String, Object> source) {
        SensitiveFilterRecordVO vo = new SensitiveFilterRecordVO();
        vo.setId(asLongObj(source.get("id")));
        vo.setCommentId(asLongObj(source.get("commentId")));
        vo.setUserId(asLongObj(source.get("userId")));
        vo.setUsername(String.valueOf(source.getOrDefault("username", "-")));
        vo.setOriginalContent(String.valueOf(source.getOrDefault("originalContent", "")));
        vo.setFilteredContent(String.valueOf(source.getOrDefault("filteredContent", "")));
        vo.setHitWords(parseWords(String.valueOf(source.getOrDefault("hitWords", ""))));
        Object createTime = source.get("createTime");
        if (createTime != null) {
            try {
                vo.setCreateTime(LocalDateTime.parse(String.valueOf(createTime).replace(" ", "T")));
            } catch (Exception ignored) {
            }
        }
        return vo;
    }

    private Map<String, Object> safeGetStats() {
        try {
            Result<Map<String, Object>> result = sensitiveFilterFeignClient.getStats();
            if (result != null && result.isSuccess() && result.getData() != null) {
                return result.getData();
            }
        } catch (Exception ignored) {
        }
        return Map.of();
    }

    private String typeToCategory(Integer type) {
        if (type == null) return "其他";
        return switch (type) {
            case 1 -> "广告营销";
            case 2 -> "政治敏感";
            case 3 -> "暴力恐吓";
            case 4 -> "色情低俗";
            default -> "其他";
        };
    }

    private Map<String, Long> toLongMap(Object source) {
        if (!(source instanceof Map<?, ?> raw)) {
            return Map.of();
        }
        return raw.entrySet().stream().filter(e -> e.getKey() != null)
                .collect(Collectors.toMap(
                        e -> String.valueOf(e.getKey()),
                        e -> asLong(e.getValue())
                ));
    }

    private List<String> parseWords(String source) {
        if (!StringUtils.hasText(source)) {
            return List.of();
        }
        return Stream.of(source.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .distinct()
                .collect(Collectors.toList());
    }

    private long asLong(Object value) {
        if (value instanceof Number num) return num.longValue();
        if (value != null) {
            try {
                return Long.parseLong(String.valueOf(value));
            } catch (NumberFormatException ignored) {
            }
        }
        return 0L;
    }

    private Long asLongObj(Object value) {
        if (value == null) return null;
        if (value instanceof Number num) return num.longValue();
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
