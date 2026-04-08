package com.community.admin.feign;

import com.community.common.result.PageResult;
import com.community.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * AI服务Feign客户端
 */
@FeignClient(name = "community-ai")
public interface AiFeignClient {

    @GetMapping("/ai/chat/sessions")
    Result<PageResult<Map<String, Object>>> getChatSessions(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size);

    @GetMapping("/ai/chat/history")
    Result<PageResult<Map<String, Object>>> getChatHistory(
            @RequestParam Long sessionId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int size);

    @GetMapping("/ai/admin/config")
    Result<Map<String, Object>> getAiConfig();

    @PutMapping("/ai/admin/config")
    Result<Void> updateAiConfig(@RequestBody Map<String, String> config);
}
