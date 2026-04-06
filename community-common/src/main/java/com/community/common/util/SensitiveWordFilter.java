package com.community.common.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class SensitiveWordFilter {

    private final Set<String> sensitiveWords = ConcurrentHashMap.newKeySet();

    public void reloadWords(Set<String> words) {
        sensitiveWords.clear();
        if (words != null) {
            sensitiveWords.addAll(words);
        }
        log.info("敏感词库重载完成，共{}个词", sensitiveWords.size());
    }

    public Set<String> getSensitiveWords() {
        return Collections.unmodifiableSet(sensitiveWords);
    }

    public boolean containsSensitiveWord(String text) {
        if (StrUtil.isBlank(text)) return false;
        for (String word : sensitiveWords) {
            if (text.contains(word)) {
                return true;
            }
        }
        return false;
    }

    public String filter(String text, String replaceChar) {
        if (StrUtil.isBlank(text)) return text;
        String result = text;
        for (String word : sensitiveWords) {
            result = result.replace(word, replaceChar != null ? replaceChar : "***");
        }
        return result;
    }

    public String filter(String text) {
        return filter(text, "***");
    }

    public List<String> findSensitiveWords(String text) {
        List<String> found = new ArrayList<>();
        if (StrUtil.isBlank(text)) return found;
        for (String word : sensitiveWords) {
            if (text.contains(word)) {
                found.add(word);
            }
        }
        return found;
    }
}
