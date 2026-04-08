package com.community.user.controller;

import com.community.common.result.Result;
import com.community.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
    @Operation(summary = "更新用户状态", description = "管理员禁用或启用用户")
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
}