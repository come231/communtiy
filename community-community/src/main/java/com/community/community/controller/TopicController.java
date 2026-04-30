package com.community.community.controller;

import com.community.common.annotation.OperationLog;
import com.community.common.result.PageResult;
import com.community.common.result.Result;
import com.community.common.util.ContextHolder;
import com.community.community.entity.Topic;
import com.community.community.service.TopicService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/topics")
@RequiredArgsConstructor
@Tag(name = "话题管理", description = "话题接口")
public class TopicController {

    private final TopicService topicService;

    @GetMapping
    @Operation(summary = "话题列表", description = "获取所有话题列表")
    public Result<List<Topic>> listTopics() {
        return Result.success(topicService.listAllTopics());
    }

    @GetMapping("/hot")
    @Operation(summary = "热门话题", description = "获取热门话题列表")
    public Result<List<Topic>> listHotTopics() {
        return Result.success(topicService.listHotTopics());
    }

    @GetMapping("/keyword-search")
    @Operation(summary = "搜索话题", description = "按名称或简介关键词分页搜索话题")
    public Result<PageResult<Topic>> searchTopics(
            @Parameter(description = "关键词") @RequestParam String keyword,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(topicService.searchTopics(keyword, pageNum, pageSize));
    }

    @GetMapping("/{topicId}")
    @Operation(summary = "话题详情", description = "获取指定话题的详细信息")
    public Result<Topic> getTopicDetail(@PathVariable Long topicId) {
        return Result.success(topicService.getTopicDetail(topicId));
    }

    @PostMapping("/{topicId}/follow")
    @OperationLog(module = "话题", operation = "关注话题")
    @Operation(summary = "关注话题", description = "关注指定话题")
    public Result<Void> followTopic(@PathVariable Long topicId) {
        topicService.followTopic(ContextHolder.getUserId(), topicId);
        return Result.success();
    }

    @DeleteMapping("/{topicId}/follow")
    @OperationLog(module = "话题", operation = "取消关注话题")
    @Operation(summary = "取消关注话题", description = "取消关注指定话题")
    public Result<Void> unfollowTopic(@PathVariable Long topicId) {
        topicService.unfollowTopic(ContextHolder.getUserId(), topicId);
        return Result.success();
    }

    @GetMapping("/{topicId}/isFollowed")
    @Operation(summary = "检查关注状态", description = "检查当前用户是否关注了指定话题")
    public Result<Boolean> isFollowingTopic(@PathVariable Long topicId) {
        return Result.success(topicService.isFollowingTopic(ContextHolder.getUserId(), topicId));
    }
}
