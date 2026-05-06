package com.community.user.controller;

import com.community.common.result.Result;
import com.community.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户管理管理员接口
 */
@RestController
@RequestMapping("/user/admin")
@RequiredArgsConstructor
@Tag(name = "用户管理(管理员)", description = "管理员用户统计接口")
public class UserAdminController {

    private final UserService userService;

    /**
     * 获取用户统计信息
     */
    @GetMapping("/stats")
    @Operation(summary = "用户统计", description = "获取用户统计信息")
    public Result<Map<String, Object>> getUserStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalUserCount", userService.count());
            stats.put("activeUserCount", userService.countTodayActive());
            stats.put("newUserCount", userService.countTodayNew());
            return Result.success(stats);
        } catch (Exception e) {
            return Result.error("获取用户统计信息失败");
        }
    }

    /**
     * 管理员禁用/启用用户
     */
    @PutMapping("/{userId}/status")
    @Operation(summary = "更新用户状态", description = "管理员禁用或启用用户: 0-禁用 1-正常")
    public Result<Void> updateUserStatus(
            @PathVariable Long userId,
            @RequestParam Integer status) {
        try {
            userService.updateStatus(userId, status);
            return Result.success();
        } catch (Exception e) {
            return Result.error("更新用户状态失败: " + e.getMessage());
        }
    }

    /**
     * 管理员更新用户角色
     */
    @PutMapping("/{userId}/role")
    @Operation(summary = "更新用户角色", description = "管理员更新用户角色: 0-普通用户 1-管理员")
    public Result<Void> updateUserRole(
            @PathVariable Long userId,
            @RequestParam Integer role) {
        try {
            userService.updateRole(userId, role);
            return Result.success();
        } catch (Exception e) {
            return Result.error("更新用户角色失败: " + e.getMessage());
        }
    }

    /**
     * 管理员禁言用户
     */
    @PostMapping("/{userId}/mute")
    @Operation(summary = "禁言用户", description = "禁止用户发言一段时间")
    public Result<Void> muteUser(
            @PathVariable Long userId,
            @RequestParam Integer minutes,
            @RequestParam(required = false) String reason) {
        try {
            userService.muteUser(userId, minutes, reason);
            return Result.success();
        } catch (Exception e) {
            return Result.error("禁言用户失败: " + e.getMessage());
        }
    }

    /**
     * 管理员解除禁言
     */
    @PostMapping("/{userId}/unmute")
    @Operation(summary = "解除禁言", description = "提前解除用户禁言")
    public Result<Void> unmuteUser(@PathVariable Long userId) {
        try {
            userService.unmuteUser(userId);
            return Result.success();
        } catch (Exception e) {
            return Result.error("解除禁言失败: " + e.getMessage());
        }
    }

    /**
     * 管理员批量更新用户信息
     */
    @PutMapping("/{userId}")
    @Operation(summary = "更新用户信息", description = "管理员批量更新用户信息（状态、角色、经验值、积分等）")
    public Result<Void> updateUser(
            @PathVariable Long userId,
            @RequestBody Map<String, Object> userInfo) {
        try {
            userService.adminUpdateUser(userId, userInfo);
            return Result.success();
        } catch (Exception e) {
            return Result.error("更新用户信息失败: " + e.getMessage());
        }
    }
}