package com.community.common.feign;

import com.community.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * 从管理端安全库拉取启用的敏感词，供各业务服务内存词表同步
 */
@FeignClient(name = "community-admin", contextId = "sensitiveWordLexicon")
public interface SensitiveWordLexiconClient {

    @GetMapping("/admin/internal/sensitive-words/lexicon")
    Result<List<String>> getActiveWords();
}
