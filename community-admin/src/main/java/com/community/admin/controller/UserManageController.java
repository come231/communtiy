package com.community.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.community.admin.service.UserManageService;
import com.community.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin/users")
@Tag(name = "用户管理", description = "管理员用户管理接口")
public class UserManageController {

    private final UserManageService userManageService;

    public UserManageController(UserManageService userManageService) {
        this.userManageService = userManageService;
    }

    @GetMapping
    @Operation(summary = "用户列表", description = "分页获取用户列表")
    public Result<IPage<Map<String, Object>>> getUserList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "用户状态筛选：0-禁用 1-正常") @RequestParam(required = false) Integer status,
            @Parameter(description = "角色筛选：0-普通用户 1-管理员") @RequestParam(required = false) Integer role) {
        return Result.success(userManageService.getUserList(page, size, keyword, status, role));
    }

    @GetMapping("/{id}")
    @Operation(summary = "用户详情", description = "获取指定用户的详细信息")
    public Result<Map<String, Object>> getUserDetail(@PathVariable Long id) {
        return Result.success(userManageService.getUserDetail(id));
    }

    @PostMapping("/{id}/ban")
    @Operation(summary = "封禁用户", description = "封禁指定用户")
    public Result<Void> banUser(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, Object> params) {
        String reason = params != null ? (String) params.get("reason") : "违规操作";
        Integer days = params != null && params.get("days") != null ?
                ((Number) params.get("days")).intValue() : 30;
        userManageService.banUser(id, reason, days);
        return Result.success();
    }

    @PostMapping("/{id}/unban")
    @Operation(summary = "解封用户", description = "解除对用户的封禁")
    public Result<Void> unbanUser(@PathVariable Long id) {
        userManageService.unbanUser(id);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "修改用户状态", description = "修改用户账号状态：0-禁用 1-正常")
    public Result<Void> updateUserStatus(
            @PathVariable Long id,
            @Parameter(description = "状态：0-禁用 1-正常", required = true)
            @RequestParam Integer status) {
        userManageService.updateUserStatus(id, status);
        return Result.success();
    }

    @PutMapping("/{id}/role")
    @Operation(summary = "修改用户角色", description = "修改用户角色：0-普通用户 1-管理员")
    public Result<Void> updateUserRole(
            @PathVariable Long id,
            @Parameter(description = "角色：0-普通用户 1-管理员", required = true)
            @RequestParam Integer role) {
        userManageService.updateUserRole(id, role);
        return Result.success();
    }

    @PostMapping("/{id}/mute")
    @Operation(summary = "禁言用户", description = "禁止用户发言一段时间")
    public Result<Void> muteUser(
            @PathVariable Long id,
            @Parameter(description = "禁言时长（分钟）", required = true)
            @RequestParam Integer minutes,
            @Parameter(description = "禁言原因")
            @RequestParam(required = false) String reason) {
        userManageService.muteUser(id, minutes, reason);
        return Result.success();
    }

    @PostMapping("/{id}/unmute")
    @Operation(summary = "解除禁言", description = "提前解除用户禁言")
    public Result<Void> unmuteUser(@PathVariable Long id) {
        userManageService.unmuteUser(id);
        return Result.success();
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新用户信息", description = "批量更新用户信息（状态、角色、经验值、积分等）")
    public Result<Void> updateUser(
            @PathVariable Long id,
            @RequestBody Map<String, Object> userInfo) {
        userManageService.updateUser(id, userInfo);
        return Result.success();
    }
}
