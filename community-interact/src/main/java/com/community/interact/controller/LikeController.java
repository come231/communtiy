package com.community.interact.controller;

import com.community.common.annotation.OperationLog;
import com.community.common.result.Result;
import com.community.common.util.ContextHolder;
import com.community.interact.service.LikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/likes")
@RequiredArgsConstructor
@Tag(name = "点赞管理", description = "帖子、评论点赞接口")
public class LikeController {

    private final LikeService likeService;

    @GetMapping("/check")
    @Operation(summary = "检查点赞状态", description = "检查当前用户是否点赞了指定内容")
    public Result<Boolean> isLiked(
            @Parameter(description = "类型：1帖子 2评论") @RequestParam Integer type,
            @Parameter(description = "目标ID") @RequestParam Long targetId) {
        return Result.success(likeService.isLiked(ContextHolder.getUserId(), type, targetId));
    }

    @PostMapping("/{type}/{targetId}")
    @Operation(summary = "点赞", description = "对指定内容进行点赞")
    public Result<Void> like(
            @Parameter(description = "类型") @PathVariable Integer type,
            @Parameter(description = "目标ID") @PathVariable Long targetId) {
        likeService.like(ContextHolder.getUserId(), type, targetId);
        return Result.success();
    }

    @DeleteMapping("/{type}/{targetId}")
    @Operation(summary = "取消点赞", description = "取消对指定内容的点赞")
    public Result<Void> unlike(
            @Parameter(description = "类型") @PathVariable Integer type,
            @Parameter(description = "目标ID") @PathVariable Long targetId) {
        likeService.unlike(ContextHolder.getUserId(), type, targetId);
        return Result.success();
    }
}
