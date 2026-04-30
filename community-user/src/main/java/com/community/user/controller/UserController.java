package com.community.user.controller;

import com.community.common.annotation.OperationLog;
import com.community.common.exception.BusinessException;
import com.community.common.result.PageResult;
import com.community.common.result.Result;
import com.community.common.util.ContextHolder;
import com.community.user.dto.*;
import com.community.user.entity.User;
import com.community.user.service.CheckinService;
import com.community.user.service.FollowService;
import com.community.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import com.community.user.service.UserTagService;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "用户登录、注册、个人信息管理接口")
public class UserController {

    private final UserService userService;
    private final FollowService followService;
    private final CheckinService checkinService;
    private final UserTagService userTagService;

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户名密码登录，返回Token和用户信息")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        return Result.success(userService.login(dto));
    }

    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "注册新用户")
    public Result<Void> register(@Valid @RequestBody RegisterDTO dto) {
        userService.register(dto);
        return Result.success();
    }

    @PostMapping("/refresh")
    @Operation(summary = "刷新Token", description = "使用刷新令牌获取新的访问令牌")
    public Result<LoginVO> refresh(@RequestBody Map<String, String> body) {
        return Result.success(userService.refreshToken(body.get("refreshToken")));
    }

    @PostMapping("/logout")
    @Operation(summary = "用户退出", description = "退出登录，清除Token")
    public Result<Void> logout(@RequestHeader(value = "Authorization", required = false) String token) {
        Long userId = ContextHolder.getUserId();
        userService.logout(userId, token);
        return Result.success();
    }

    @GetMapping("/infoById")
    @Operation(summary = "获取用户信息", description = "根据用户ID获取用户详细信息")
    public Result<Map<String,Object>> getUserInfoById(@Parameter(description = "用户ID") @RequestParam Long userId) {
        return Result.success(userService.getUserInfoById(userId));
    }

    @GetMapping("/info")
    @Operation(summary = "获取用户信息", description = "根据用户ID获取用户详细信息")
    public Result<UserVO> getUserInfo(@RequestParam Long userId) {
        Long currentUserId = ContextHolder.getUserId();
        return Result.success(userService.getUserInfo(currentUserId, userId));
    }

    @GetMapping("/my")
    @Operation(summary = "获取当前用户信息", description = "获取登录用户自己的信息")
    public Result<UserVO> getMyInfo() {
        return Result.success(userService.getUserInfo(ContextHolder.getUserId()));
    }

    @PutMapping("/update")
    @OperationLog(module = "用户", operation = "修改个人信息")
    @Operation(summary = "更新用户信息", description = "修改当前用户的个人资料")
    public Result<UserVO> updateUserInfo(@RequestBody UserUpdateDTO dto) {
        return Result.success(userService.updateUserInfo(ContextHolder.getUserId(), dto));
    }

    @GetMapping("/search")
    @Operation(summary = "搜索用户", description = "根据关键词搜索用户列表")
    public Result<PageResult<UserVO>> searchUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(userService.listUsers(keyword, pageNum, pageSize));
    }

    @PostMapping("/follow/{userId}")
    @OperationLog(module = "用户", operation = "关注用户")
    @Operation(summary = "关注用户", description = "关注指定用户")
    public Result<Void> follow(@PathVariable Long userId) {
        followService.follow(ContextHolder.getUserId(), userId);
        return Result.success();
    }

    @DeleteMapping("/follow/{userId}")
    @OperationLog(module = "用户", operation = "取消关注")
    @Operation(summary = "取消关注", description = "取消关注指定用户")
    public Result<Void> unfollow(@PathVariable Long userId) {
        followService.unfollow(ContextHolder.getUserId(), userId);
        return Result.success();
    }

    @GetMapping("/following/{userId}")
    @Operation(summary = "获取关注列表", description = "获取指定用户的关注列表")
    public Result<List<FollowUserVO>> getFollowingList(@PathVariable Long userId) {
        return Result.success(followService.getFollowingList(userId, ContextHolder.getUserId()));
    }

    @GetMapping("/followers/{userId}")
    @Operation(summary = "获取粉丝列表", description = "获取指定用户的粉丝列表")
    public Result<List<FollowUserVO>> getFollowersList(@PathVariable Long userId) {
        return Result.success(followService.getFollowersList(userId, ContextHolder.getUserId()));
    }

    @GetMapping("/isFollowing/{userId}")
    @Operation(summary = "检查关注状态", description = "检查当前用户是否关注了指定用户")
    public Result<Boolean> isFollowing(@PathVariable Long userId) {
        return Result.success(followService.isFollowing(ContextHolder.getUserId(), userId));
    }

    @PostMapping("/checkin")
    @OperationLog(module = "用户", operation = "每日签到")
    @Operation(summary = "每日签到", description = "用户每日签到获取积分")
    public Result<Map<String, Object>> checkin() {
        return Result.success(checkinService.checkin(ContextHolder.getUserId()));
    }

    @GetMapping("/checkin/status")
    @Operation(summary = "签到状态", description = "获取用户今日签到状态")
    public Result<Map<String, Object>> checkinStatus() {
        return Result.success(checkinService.getCheckinStatus(ContextHolder.getUserId()));
    }

    // ==================== 个人标签 ====================

    @GetMapping("/tags")
    @Operation(summary = "获取用户标签", description = "获取当前用户的个人标签列表")
    public Result<List<String>> getUserTags() {
        return Result.success(userTagService.getUserTags(ContextHolder.getUserId()));
    }

    @PutMapping("/tags")
    @OperationLog(module = "用户", operation = "更新个人标签")
    @Operation(summary = "更新用户标签", description = "更新当前用户的个人标签列表")
    public Result<Void> updateUserTags(@RequestBody List<String> tags) {
        userTagService.updateUserTags(ContextHolder.getUserId(), tags);
        return Result.success();
    }

    // ==================== 隐私设置 ====================

    @GetMapping("/privacy")
    @Operation(summary = "获取隐私设置", description = "获取当前用户的隐私设置")
    public Result<Map<String, Object>> getPrivacySettings() {
        return Result.success(userService.getPrivacySettings(ContextHolder.getUserId()));
    }

    @PutMapping("/privacy")
    @OperationLog(module = "用户", operation = "修改隐私设置")
    @Operation(summary = "更新隐私设置", description = "更新喜欢列表和收藏列表的可见性")
    public Result<Void> updatePrivacySettings(@RequestBody Map<String, Integer> settings) {
        userService.updatePrivacySettings(ContextHolder.getUserId(), settings);
        return Result.success();
    }

    // ==================== 喜欢列表 ====================

    @GetMapping("/likes")
    @Operation(summary = "我的喜欢列表", description = "获取当前用户点赞的帖子列表")
    public Result<PageResult<Map<String, Object>>> getMyLikedPosts(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(userService.getUserLikedPosts(ContextHolder.getUserId(), ContextHolder.getUserId(), pageNum, pageSize));
    }

    @GetMapping("/{userId}/likes")
    @Operation(summary = "用户喜欢列表", description = "获取指定用户的点赞帖子列表（受隐私设置限制）")
    public Result<PageResult<Map<String, Object>>> getUserLikedPosts(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(userService.getUserLikedPosts(ContextHolder.getUserId(), userId, pageNum, pageSize));
    }

    // ==================== 内部接口（服务间调用） ====================

    @PostMapping("/internal/like-count")
    @Operation(summary = "内部：更新获赞数", description = "服务间调用，更新用户获赞总数")
    public Result<Void> updateLikeCountInternal(@RequestParam Long userId, @RequestParam Integer delta) {
        userService.updateLikeCount(userId, delta);
        return Result.success();
    }
}
