package com.community.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community.admin.entity.SensitiveWord;
import com.community.admin.mapper.security.SensitiveWordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SensitiveWordService {

    private final SensitiveWordMapper sensitiveWordMapper;

    @Transactional
    public SensitiveWord addSensitiveWord(String word, Integer level) {
        if (!StringUtils.hasText(word)) {
            throw new RuntimeException("敏感词不能为空");
        }

        // 检查是否已存在
        Long count = sensitiveWordMapper.selectCount(
                new LambdaQueryWrapper<SensitiveWord>()
                        .eq(SensitiveWord::getWord, word.trim()));
        if (count > 0) {
            throw new RuntimeException("敏感词已存在");
        }

        SensitiveWord sensitiveWord = new SensitiveWord();
        sensitiveWord.setWord(word.trim());
        sensitiveWord.setLevel(level != null ? level : 1);
        sensitiveWord.setStatus(1);
        sensitiveWord.setCreateTime(LocalDateTime.now());
        sensitiveWord.setUpdateTime(LocalDateTime.now());

        sensitiveWordMapper.insert(sensitiveWord);
        return sensitiveWord;
    }

    @Transactional
    public boolean updateSensitiveWord(Long id, String word, Integer level) {
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
        sensitiveWord.setUpdateTime(LocalDateTime.now());

        return sensitiveWordMapper.updateById(sensitiveWord) > 0;
    }

    @Transactional
    public boolean deleteSensitiveWord(Long id) {
        return sensitiveWordMapper.deleteById(id) > 0;
    }

    public IPage<SensitiveWord> getSensitiveWords(int page, int size, String keyword) {
        Page<SensitiveWord> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<SensitiveWord> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            wrapper.like(SensitiveWord::getWord, keyword);
        }

        wrapper.orderByAsc(SensitiveWord::getLevel)
               .orderByDesc(SensitiveWord::getCreateTime);

        return sensitiveWordMapper.selectPage(pageObj, wrapper);
    }

    /**
     * 启用的敏感词全量（供社区等服务构建内存词表）
     */
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
            // 检查是否已存在
            Long count = sensitiveWordMapper.selectCount(
                    new LambdaQueryWrapper<SensitiveWord>()
                            .eq(SensitiveWord::getWord, word));
            if (count > 0) continue;

            SensitiveWord sensitiveWord = new SensitiveWord();
            sensitiveWord.setWord(word);
            sensitiveWord.setLevel(1);
            sensitiveWord.setStatus(1);
            sensitiveWord.setCreateTime(LocalDateTime.now());
            sensitiveWord.setUpdateTime(LocalDateTime.now());

            try {
                sensitiveWordMapper.insert(sensitiveWord);
                successCount++;
            } catch (Exception e) {
                // 跳过重复或错误
            }
        }
        return successCount;
    }
}
