package com.community.ai.service;

import com.community.ai.entity.AiCheck;
import com.community.ai.mapper.AiCheckMapper;
import com.community.ai.util.AiCallHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiReviewService {

    private final AiCallHelper aiCallHelper;
    private final AiCheckMapper aiCheckMapper;

    private static final List<String> SENSITIVE_WORDS = List.of(
            "敏感词1", "敏感词2", "违规内容", "违法"
    );

    public AiCheck checkContent(Long userId, String content, Integer type) {
        AiCheck check = new AiCheck();
        check.setUserId(userId);
        check.setContentType(type);
        check.setOriginalContent(content);
        check.setCreateTime(LocalDateTime.now());

        List<String> foundWords = new ArrayList<>();
        for (String word : SENSITIVE_WORDS) {
            if (content.contains(word)) {
                foundWords.add(word);
            }
        }

        if (!foundWords.isEmpty()) {
            check.setCheckResult(2);
            check.setSensitiveWords(String.join(",", foundWords));
            check.setAiSuggestion("内容包含敏感词汇: " + String.join(",", foundWords));
            aiCheckMapper.insert(check);
            return check;
        }

        try {
            String prompt = String.format(
                    "请判断以下内容是否包含违规、不良或敏感信息。回复格式：{\"result\": 0/1/2, \"suggestion\": \"建议\"}。\n" +
                    "其中0表示正常，1表示可能敏感，2表示违规。\n" +
                    "待审核内容：%s",
                    content
            );
            String aiResponse = aiCallHelper.call(prompt);

            if (aiResponse.contains("\"result\": 2") || aiResponse.contains("违规")) {
                check.setCheckResult(2);
                check.setAiSuggestion("AI判定为违规内容");
            } else if (aiResponse.contains("\"result\": 1") || aiResponse.contains("敏感")) {
                check.setCheckResult(1);
                check.setAiSuggestion("AI判定为可能敏感");
            } else {
                check.setCheckResult(0);
                check.setAiSuggestion("内容正常");
            }
        } catch (Exception e) {
            log.error("AI审核失败, 使用默认结果, error={}", e.getMessage(), e);
            check.setCheckResult(0);
            check.setAiSuggestion("AI审核异常，默认通过");
        }

        aiCheckMapper.insert(check);
        return check;
    }

    public List<AiCheck> batchCheck(List<String> contents) {
        List<AiCheck> results = new ArrayList<>();
        for (String content : contents) {
            results.add(checkContent(null, content, 1));
        }
        return results;
    }
}
