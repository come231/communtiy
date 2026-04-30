package com.community.common.feign;

import com.community.common.dto.AiContentCheckResult;
import com.community.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "community-ai", contextId = "aiReview")
public interface AiReviewFeignClient {

    @PostMapping("/ai/review/check")
    Result<AiContentCheckResult> checkContent(@RequestBody Map<String, Object> body);

    /**
     * 将审核记录与帖子 ID 关联（发帖成功后调用，便于后台追踪）
     */
    @PutMapping("/ai/internal/check/{checkId}/post/{postId}")
    Result<Void> bindCheckToPost(@PathVariable("checkId") Long checkId, @PathVariable("postId") Long postId);
}
