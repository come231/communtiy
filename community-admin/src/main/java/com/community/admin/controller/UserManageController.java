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
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword) {
        return Result.success(userManageService.getUserList(page, size, keyword));
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
}
