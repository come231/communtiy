package com.community.community.controller;

import com.community.common.annotation.OperationLog;
import com.community.common.exception.BusinessException;
import com.community.common.result.PageResult;
import com.community.common.result.Result;
import com.community.common.util.ContextHolder;
import com.community.community.dto.PostDTO;
import com.community.community.dto.PostVO;
import com.community.community.entity.PostDraft;
import com.community.community.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
@Tag(name = "帖子管理", description = "帖子发布、编辑、删除、列表查询等接口")
public class PostController {

    private final PostService postService;

    @GetMapping("/list")
    @Operation(summary = "帖子列表", description = "分页获取帖子列表，支持关键词、板块、话题筛选和排序")
    public Result<PageResult<PostVO>> listPosts(
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "板块ID") @RequestParam(required = false) Long sectionId,
            @Parameter(description = "话题ID") @RequestParam(required = false) Long topicId,
            @Parameter(description = "排序方式") @RequestParam(defaultValue = "latest") String sort,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(postService.listPosts(keyword, sectionId, topicId, sort, pageNum, pageSize));
    }

    @GetMapping("/hot")
    @Operation(summary = "热门帖子", description = "获取热门帖子列表")
    public Result<PageResult<PostVO>> hotPosts(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(postService.listHotPosts(pageNum, pageSize));
    }

    @GetMapping("/{postId}")
    @Operation(summary = "帖子详情", description = "根据ID获取帖子详细信息")
    public Result<PostVO> getPostDetail(@PathVariable Long postId) {
        return Result.success(postService.getPostDetail(postId));
    }

    @PostMapping
    @OperationLog(module = "帖子", operation = "发布帖子")
    @Operation(summary = "发布帖子", description = "创建新的帖子")
    public Result<Long> createPost(@Valid @RequestBody PostDTO dto) {
        return Result.success(postService.createPost(ContextHolder.getUserId(), dto));
    }

    @PutMapping("/{postId}")
    @OperationLog(module = "帖子", operation = "编辑帖子")
    @Operation(summary = "编辑帖子", description = "修改已有帖子内容")
    public Result<Void> updatePost(@PathVariable Long postId, @Valid @RequestBody PostDTO dto) {
        postService.updatePost(ContextHolder.getUserId(), postId, dto);
        return Result.success();
    }

    @DeleteMapping("/{postId}")
    @OperationLog(module = "帖子", operation = "删除帖子")
    @Operation(summary = "删除帖子", description = "删除自己的帖子")
    public Result<Void> deletePost(@PathVariable Long postId) {
        postService.deletePost(ContextHolder.getUserId(), postId);
        return Result.success();
    }

    @PutMapping("/{postId}/top")
    @Operation(summary = "置顶帖子", description = "将帖子置顶/取消置顶")
    public Result<Void> toggleTop(@PathVariable Long postId) {
        postService.toggleTop(postId);
        return Result.success();
    }

    @PutMapping("/{postId}/essence")
    @Operation(summary = "精华帖子", description = "将帖子设为精华/取消精华")
    public Result<Void> toggleEssence(@PathVariable Long postId) {
        postService.toggleEssence(postId);
        return Result.success();
    }

    @PostMapping("/drafts/save")
    @Operation(summary = "保存草稿", description = "保存帖子草稿")
    public Result<Long> saveDraft(@Valid @RequestBody PostDTO dto) {
        return Result.success(postService.saveDraft(ContextHolder.getUserId(), dto));
    }

    @GetMapping("/drafts")
    @Operation(summary = "草稿列表", description = "获取当前用户的帖子草稿列表")
    public Result<PageResult<PostDraft>> listDrafts(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(postService.listDrafts(ContextHolder.getUserId(), pageNum, pageSize));
    }

    @DeleteMapping("/drafts/{draftId}")
    @Operation(summary = "删除草稿", description = "删除指定的帖子草稿")
    public Result<Void> deleteDraft(@PathVariable Long draftId) {
        postService.deleteDraft(ContextHolder.getUserId(), draftId);
        return Result.success();
    }
}
