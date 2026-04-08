package com.community.admin.feign;

import com.community.common.result.PageResult;
import com.community.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 社区服务Feign客户端 - 帖子管理
 */
@FeignClient(name = "community-community")
public interface PostFeignClient {

    /**
     * 获取帖子列表
     */
    @GetMapping("/posts/list")
    Result<PageResult<Map<String, Object>>> listPosts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long sectionId,
            @RequestParam(required = false) Long topicId,
            @RequestParam(defaultValue = "latest") String sort,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize);

    /**
     * 获取帖子详情
     */
    @GetMapping("/posts/{postId}")
    Result<Map<String, Object>> getPostDetail(@PathVariable Long postId);

    /**
     * 审核帖子（管理员权限）
     */
    @PutMapping("/posts/admin/{postId}/review")
    Result<Void> reviewPost(
            @PathVariable Long postId,
            @RequestParam Integer status,
            @RequestParam(required = false) String reason);

    /**
     * 设置置顶（管理员权限）
     */
    @PutMapping("/posts/admin/{postId}/top")
    Result<Void> setTop(@PathVariable Long postId, @RequestParam boolean isTop);

    /**
     * 设置精华（管理员权限）
     */
    @PutMapping("/posts/admin/{postId}/essence")
    Result<Void> setEssence(@PathVariable Long postId, @RequestParam boolean isEssence);

    /**
     * 删除帖子（管理员权限）
     */
    @DeleteMapping("/posts/admin/{postId}")
    Result<Void> deletePost(@PathVariable Long postId);

    /**
     * 获取帖子统计信息（管理员接口）
     */
    @GetMapping("/posts/admin/stats")
    Result<Map<String, Object>> getPostStats();

    /**
     * 获取帖子发布趋势
     */
    @GetMapping("/posts/admin/post-trend")
    Result<java.util.List<Map<String, Object>>> getPostTrend(@RequestParam(defaultValue = "7") int days);
}