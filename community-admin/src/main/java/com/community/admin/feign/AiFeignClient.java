package com.community.admin.feign;

import com.community.common.dto.AiCheckRecordVO;
import com.community.common.dto.PostReviewRuleDTO;
import com.community.common.result.PageResult;
import com.community.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * AI服务Feign客户端
 */
@FeignClient(name = "community-ai")
public interface AiFeignClient {

    @GetMapping("/ai/chat/sessions")
    Result<?> getChatSessions(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size);

    @GetMapping("/ai/chat/history")
    Result<?> getChatHistory(
            @RequestParam Long sessionId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int size);

    @GetMapping("/ai/admin/check-records")
    Result<PageResult<AiCheckRecordVO>> listCheckRecords(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long postId,
            @RequestParam(required = false) Integer checkResult);

    @GetMapping("/ai/admin/config")
    Result<Map<String, Object>> getAiConfig();

    @PutMapping("/ai/admin/config")
    Result<Void> updateAiConfig(@RequestBody Map<String, String> config);

    @GetMapping("/ai/admin/post-review-rules")
    Result<List<PostReviewRuleDTO>> listPostReviewRules();

    @PostMapping("/ai/admin/post-review-rules")
    Result<PostReviewRuleDTO> createPostReviewRule(@RequestBody PostReviewRuleDTO dto);

    @PutMapping("/ai/admin/post-review-rules/{id}")
    Result<PostReviewRuleDTO> updatePostReviewRule(@PathVariable("id") Long id, @RequestBody PostReviewRuleDTO dto);

    @DeleteMapping("/ai/admin/post-review-rules/{id}")
    Result<Void> deletePostReviewRule(@PathVariable("id") Long id);
}
