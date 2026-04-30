package com.community.community.config;

import com.community.common.feign.SensitiveWordLexiconClient;
import com.community.common.result.Result;
import com.community.common.util.SensitiveWordFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 从管理端同步敏感词到本机内存；后台增删改后最多一个周期内生效
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SensitiveWordLexiconRefresher {

    private final SensitiveWordLexiconClient lexiconClient;
    private final SensitiveWordFilter sensitiveWordFilter;

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        refresh();
    }

    @Scheduled(fixedDelayString = "${community.sensitive-word.refresh-ms:120000}")
    public void scheduled() {
        refresh();
    }

    public void refresh() {
        try {
            Result<List<String>> r = lexiconClient.getActiveWords();
            if (r != null && r.isSuccess() && r.getData() != null) {
                Set<String> set = new HashSet<>(r.getData());
                sensitiveWordFilter.reloadWords(set);
            } else {
                log.warn("拉取敏感词失败: {}", r != null ? r.getMessage() : "null");
            }
        } catch (Exception e) {
            log.warn("同步敏感词异常(保留旧词表): {}", e.getMessage());
        }
    }
}
