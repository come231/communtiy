package com.community.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.community.admin.service.PostManageService;
import com.community.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin/posts")
@Tag(name = "帖子管理", description = "管理员帖子管理接口")
public class PostManageController {

    private final PostManageService postManageService;

    public PostManageController(PostManageService postManageService) {
        this.postManageService = postManageService;
    }

    @GetMapping
    @Operation(summary = "帖子列表", description = "分页获取帖子列表")
    public Result<IPage<Map<String, Object>>> getPostList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status) {
        return Result.success(postManageService.getPostList(page, size, keyword, status));
    }

    @GetMapping("/{id}")
    @Operation(summary = "帖子详情", description = "获取指定帖子的详细信息")
    public Result<Map<String, Object>> getPostDetail(@PathVariable Long id) {
        return Result.success(postManageService.getPostDetail(id));
    }

    @PutMapping("/{id}/review")
    @Operation(summary = "审核帖子", description = "审核指定帖子")
    public Result<Void> reviewPost(
            @PathVariable Long id,
            @Parameter(description = "审核状态") @RequestParam Integer status,
            @Parameter(description = "审核原因") @RequestParam(required = false) String reason) {
        postManageService.reviewPost(id, status, reason);
        return Result.success();
    }

    @PutMapping("/{id}/top")
    @Operation(summary = "设置置顶", description = "设置/取消帖子置顶")
    public Result<Void> setTop(@PathVariable Long id, @RequestParam boolean isTop) {
        postManageService.setTop(id, isTop);
        return Result.success();
    }

    @PutMapping("/{id}/essence")
    @Operation(summary = "设置精华", description = "设置/取消帖子精华")
    public Result<Void> setEssence(@PathVariable Long id, @RequestParam boolean isEssence) {
        postManageService.setEssence(id, isEssence);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除帖子", description = "删除指定帖子")
    public Result<Void> deletePost(@PathVariable Long id) {
        postManageService.deletePost(id);
        return Result.success();
    }
}
