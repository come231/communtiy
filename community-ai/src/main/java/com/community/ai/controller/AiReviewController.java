package com.community.ai.controller;

import com.community.ai.entity.AiCheck;
import com.community.ai.service.AiReviewService;
import com.community.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ai/review")
@RequiredArgsConstructor
@Tag(name = "AI内容审核", description = "AI内容审核接口")
public class AiReviewController {

    private final AiReviewService aiReviewService;

    @PostMapping("/check")
    @Operation(summary = "内容审核", description = "AI审核单条内容")
    public Result<AiCheck> checkContent(@RequestBody Map<String, Object> params) {
        Long userId = params.containsKey("userId") ? Long.valueOf(params.get("userId").toString()) : null;
        String content = (String) params.get("content");
        Integer type = params.containsKey("type") ? Integer.valueOf(params.get("type").toString()) : 1;
        AiCheck result = aiReviewService.checkContent(userId, content, type);
        return Result.success(result);
    }

    @PostMapping("/batch-check")
    @Operation(summary = "批量审核", description = "AI批量审核内容")
    public Result<List<AiCheck>> batchCheck(@RequestBody Map<String, Object> params) {
        @SuppressWarnings("unchecked")
        List<String> contents = (List<String>) params.get("contents");
        List<AiCheck> results = aiReviewService.batchCheck(contents);
        return Result.success(results);
    }
}
