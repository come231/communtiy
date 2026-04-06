package com.community.ai.controller;

import com.community.common.util.ContextHolder;
import com.community.common.result.Result;
import com.community.ai.service.AiRecommendService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ai/recommend")
@RequiredArgsConstructor
@Tag(name = "AI推荐", description = "AI个性化推荐接口")
public class AiRecommendController {

    private final AiRecommendService aiRecommendService;

    @PostMapping("/posts")
    @Operation(summary = "获取推荐帖子", description = "根据用户兴趣获取AI推荐帖子")
    public Result<String> getRecommendPosts() {
        Long userId = ContextHolder.getUserId();
        String result = aiRecommendService.getRecommendPosts(userId);
        return Result.success(result);
    }

    @PutMapping("/interests")
    @Operation(summary = "更新兴趣标签", description = "更新用户的兴趣标签")
    public Result<Void> updateInterests(@RequestBody Map<String, Object> params) {
        Long userId = ContextHolder.getUserId();
        @SuppressWarnings("unchecked")
        List<String> tags = (List<String>) params.get("tags");
        aiRecommendService.updateUserInterests(userId, tags);
        return Result.success();
    }

    @GetMapping("/preferences")
    @Operation(summary = "分析用户偏好", description = "AI分析用户内容偏好")
    public Result<String> analyzePreferences() {
        Long userId = ContextHolder.getUserId();
        String result = aiRecommendService.analyzeUserPreferences(userId);
        return Result.success(result);
    }
}
