package com.community.community.controller;

import com.community.common.result.PageResult;
import com.community.common.result.Result;
import com.community.community.dto.PostVO;
import com.community.community.entity.Comment;
import com.community.community.entity.Post;
import com.community.community.mapper.CommentMapper;
import com.community.community.mapper.PostMapper;
import com.community.community.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

/**
 * 帖子管理管理员接口
 */
@RestController
@RequestMapping("/posts/admin")
@RequiredArgsConstructor
@Tag(name = "帖子管理(管理员)", description = "管理员帖子管理接口")
public class PostAdminController {

    private final PostService postService;
    private final PostMapper postMapper;
    private final CommentMapper commentMapper;

    @GetMapping("/feed")
    @Operation(summary = "管理端帖子列表", description = "按状态、关键词筛选（服务间/管理端）")
    public Result<PageResult<PostVO>> listForAdmin(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(postService.listPostsForAdmin(keyword, status, pageNum, pageSize));
    }

    @GetMapping("/post/{postId}")
    @Operation(summary = "管理端帖子详情", description = "可见待审/驳回等非公开帖")
    public Result<PostVO> getForAdmin(@PathVariable Long postId) {
        return Result.success(postService.getPostDetailForAdmin(postId));
    }

    /**
     * 审核帖子
     */
    @PutMapping("/{postId}/review")
    @Operation(summary = "审核帖子", description = "管理员审核帖子")
    public Result<Void> reviewPost(
            @PathVariable Long postId,
            @Parameter(description = "审核状态") @RequestParam Integer status,
            @Parameter(description = "审核原因") @RequestParam(required = false) String reason) {
        try {
            postService.reviewPost(postId, status, reason);
            return Result.success();
        } catch (Exception e) {
            return Result.error("审核帖子失败: " + e.getMessage());
        }
    }

    /**
     * 设置置顶
     */
    @PutMapping("/{postId}/top")
    @Operation(summary = "设置置顶", description = "管理员设置帖子置顶")
    public Result<Void> setTop(@PathVariable Long postId, @RequestParam boolean isTop) {
        try {
            postService.setTop(postId, isTop);
            return Result.success();
        } catch (Exception e) {
            return Result.error("设置置顶失败: " + e.getMessage());
        }
    }

    /**
     * 设置精华
     */
    @PutMapping("/{postId}/essence")
    @Operation(summary = "设置精华", description = "管理员设置帖子精华")
    public Result<Void> setEssence(@PathVariable Long postId, @RequestParam boolean isEssence) {
        try {
            postService.setEssence(postId, isEssence);
            return Result.success();
        } catch (Exception e) {
            return Result.error("设置精华失败: " + e.getMessage());
        }
    }

    /**
     * 删除帖子（管理员权限）
     */
    @DeleteMapping("/{postId}")
    @Operation(summary = "删除帖子", description = "管理员删除帖子")
    public Result<Void> deletePost(@PathVariable Long postId) {
        try {
            postService.deletePost(postId);
            return Result.success();
        } catch (Exception e) {
            return Result.error("删除帖子失败: " + e.getMessage());
        }
    }

    /**
     * 获取帖子统计信息
     */
    @GetMapping("/stats")
    @Operation(summary = "帖子统计", description = "获取帖子统计信息")
    public Result<Map<String, Object>> getPostStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalPostCount", postService.count());
            stats.put("totalCommentCount", commentMapper.selectCount(null));
            LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
            Long todayCount = postMapper.selectCount(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Post>()
                            .ge(Post::getCreateTime, todayStart));
            stats.put("todayPostCount", todayCount);
            return Result.success(stats);
        } catch (Exception e) {
            return Result.error("获取帖子统计信息失败");
        }
    }

    /**
     * 获取帖子发布趋势（最近N天）
     */
    @GetMapping("/post-trend")
    @Operation(summary = "帖子趋势", description = "获取帖子发布趋势数据")
    public Result<List<Map<String, Object>>> getPostTrend(
            @Parameter(description = "天数") @RequestParam(defaultValue = "7") int days) {
        try {
            List<Map<String, Object>> result = new ArrayList<>();
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(days - 1);

            for (int i = 0; i < days; i++) {
                LocalDate date = startDate.plusDays(i);
                LocalDateTime dayStart = LocalDateTime.of(date, LocalTime.MIN);
                LocalDateTime dayEnd = LocalDateTime.of(date, LocalTime.MAX);
                Long count = postMapper.selectCount(
                        new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Post>()
                                .ge(Post::getCreateTime, dayStart)
                                .le(Post::getCreateTime, dayEnd));
                Map<String, Object> dayData = new HashMap<>();
                dayData.put("date", date.toString());
                dayData.put("count", count);
                result.add(dayData);
            }
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("获取帖子趋势失败");
        }
    }
}