package com.community.interact.controller;

import com.community.common.annotation.OperationLog;
import com.community.common.result.PageResult;
import com.community.common.result.Result;
import com.community.common.util.ContextHolder;
import com.community.interact.entity.Collect;
import com.community.interact.entity.CollectFolder;
import com.community.interact.service.CollectFolderService;
import com.community.interact.service.CollectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/collects")
@RequiredArgsConstructor
@Tag(name = "收藏管理", description = "帖子收藏接口")
public class CollectController {

    private final CollectService collectService;
    private final CollectFolderService collectFolderService;

    @GetMapping("/check/{postId}")
    @Operation(summary = "检查收藏状态", description = "检查当前用户是否收藏了指定帖子")
    public Result<Boolean> isCollected(@PathVariable Long postId) {
        return Result.success(collectService.isCollected(ContextHolder.getUserId(), postId));
    }

    @PostMapping
    @Operation(summary = "收藏帖子", description = "将帖子收藏到指定文件夹")
    public Result<Void> collectPost(@RequestBody CollectRequest request) {
        collectService.collect(ContextHolder.getUserId(), request.getPostId(), request.getFolderId());
        return Result.success();
    }

    @lombok.Data
    public static class CollectRequest {
        private Long postId;
        private Long folderId;
    }

    @DeleteMapping("/{postId}")
    @Operation(summary = "取消收藏", description = "取消对帖子的收藏")
    public Result<Void> uncollectPost(@PathVariable Long postId) {
        collectService.uncollect(ContextHolder.getUserId(), postId);
        return Result.success();
    }

    @GetMapping
    @Operation(summary = "收藏列表", description = "获取当前用户的收藏列表")
    public Result<PageResult<Collect>> listMyCollects(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int pageSize,
            @Parameter(description = "文件夹ID") @RequestParam(required = false) Long folderId) {
        return Result.success(collectService.listCollects(
                ContextHolder.getUserId(), folderId, pageNum, pageSize));
    }
}
