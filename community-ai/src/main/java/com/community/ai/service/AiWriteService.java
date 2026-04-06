package com.community.ai.service;

import com.community.ai.dto.WriteRequest;
import com.community.ai.util.AiCallHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiWriteService {

    private final AiCallHelper aiCallHelper;

    public String writeArticle(String title, String requirements) {
        try {
            String prompt = String.format(
                    "请根据以下要求写一篇完整的文章：\n标题：%s\n要求：%s\n" +
                    "请输出结构完整、内容丰富的文章。",
                    title, requirements != null ? requirements : ""
            );
            return aiCallHelper.call(prompt);
        } catch (Exception e) {
            log.error("AI写作失败, error={}", e.getMessage(), e);
            throw new RuntimeException("AI写作失败，请稍后重试: " + e.getMessage());
        }
    }

    public String polishContent(String content) {
        try {
            String prompt = String.format(
                    "请对以下内容进行润色和优化，使其更加通顺、专业、易读：\n%s",
                    content
            );
            return aiCallHelper.call(prompt);
        } catch (Exception e) {
            log.error("AI润色失败, error={}", e.getMessage(), e);
            throw new RuntimeException("AI润色失败，请稍后重试: " + e.getMessage());
        }
    }

    public String translateContent(String content, String targetLang) {
        try {
            String prompt = String.format(
                    "请将以下内容翻译成%s，保持原文的语气和风格：\n%s",
                    targetLang != null ? targetLang : "英文", content
            );
            return aiCallHelper.call(prompt);
        } catch (Exception e) {
            log.error("AI翻译失败, error={}", e.getMessage(), e);
            throw new RuntimeException("AI翻译失败，请稍后重试: " + e.getMessage());
        }
    }

    public String summarizeContent(String content) {
        try {
            String prompt = String.format(
                    "请对以下内容进行总结，提取关键信息：\n%s",
                    content
            );
            return aiCallHelper.call(prompt);
        } catch (Exception e) {
            log.error("AI总结失败, error={}", e.getMessage(), e);
            throw new RuntimeException("AI总结失败，请稍后重试: " + e.getMessage());
        }
    }
}
