package com.community.admin.controller;

import com.community.admin.dto.AdminLoginDTO;
import com.community.admin.util.AdminContextHolder;
import com.community.admin.service.AdminAuthService;
import com.community.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin")
@Tag(name = "管理员认证", description = "管理员登录、退出、信息接口")
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    public AdminAuthController(AdminAuthService adminAuthService) {
        this.adminAuthService = adminAuthService;
    }

    @PostMapping("/login")
    @Operation(summary = "管理员登录", description = "管理员用户名密码登录，返回accessToken和refreshToken")
    public Result<Map<String, Object>> login(@RequestBody AdminLoginDTO loginDTO) {
        Map<String, Object> data = adminAuthService.login(loginDTO);
        return Result.success(data);
    }

    @PostMapping("/refresh")
    @Operation(summary = "刷新Token", description = "使用refreshToken获取新的accessToken")
    public Result<Map<String, Object>> refresh(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        Map<String, Object> data = adminAuthService.refreshToken(refreshToken);
        return Result.success(data);
    }

    @PostMapping("/logout")
    @Operation(summary = "管理员退出", description = "退出登录，token加入黑名单")
    public Result<Void> logout(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        String accessToken = null;
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            accessToken = bearerToken.substring(7);
        }

        Long adminId = AdminContextHolder.getAdminId();
        adminAuthService.logout(accessToken, adminId);

        AdminContextHolder.clear();
        return Result.success();
    }

    @GetMapping("/info")
    @Operation(summary = "获取管理员信息", description = "获取当前登录管理员的信息")
    public Result<Map<String, Object>> getAdminInfo() {
        Long adminId = AdminContextHolder.getAdminId();
        Map<String, Object> info = adminAuthService.getAdminInfo(adminId);
        return Result.success(info);
    }
}