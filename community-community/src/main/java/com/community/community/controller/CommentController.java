package com.community.community.controller;

import com.community.common.annotation.OperationLog;
import com.community.common.result.PageResult;
import com.community.common.result.Result;
import com.community.common.util.ContextHolder;
import com.community.community.dto.CommentDTO;
import com.community.community.dto.CommentVO;
import com.community.community.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
@Tag(name = "评论管理", description = "帖子评论接口")
public class CommentController {

    private final CommentService commentService;

    @GetMapping
    @Operation(summary = "评论列表", description = "获取指定帖子的评论列表")
    public Result<PageResult<CommentVO>> listComments(
            @Parameter(description = "帖子ID") @RequestParam Long postId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(commentService.listComments(postId, pageNum, pageSize));
    }

    @PostMapping
    @OperationLog(module = "评论", operation = "发表评论")
    @Operation(summary = "发表评论", description = "在指定帖子下发表评论")
    public Result<Long> createComment(@Valid @RequestBody CommentDTO dto) {
        return Result.success(commentService.createComment(ContextHolder.getUserId(), dto));
    }

    @DeleteMapping("/{commentId}")
    @OperationLog(module = "评论", operation = "删除评论")
    @Operation(summary = "删除评论", description = "删除自己的评论")
    public Result<Void> deleteComment(@PathVariable Long commentId) {
        commentService.deleteComment(ContextHolder.getUserId(), commentId);
        return Result.success();
    }
}
