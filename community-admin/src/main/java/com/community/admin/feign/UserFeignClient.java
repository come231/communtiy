package com.community.admin.feign;

import com.community.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户服务Feign客户端
 */
@FeignClient(name = "community-user")
public interface UserFeignClient {

    /**
     * 获取用户信息
     */
    @GetMapping("/user/info")
    Result<Map<String, Object>> getUserInfo(@RequestParam Long userId);
    
    /**
     * 获取用户统计信息
     */
    @GetMapping("/user/admin/stats")
    Result<Map<String, Object>> getUserStats();

    /**
     * 搜索用户（支持状态和角色筛选）- 管理员专用
     */
    @GetMapping("/user/admin/search")
    Result<Map<String, Object>> searchUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer role);

    /**
     * 搜索用户（普通接口，无权限控制）
     */
    @GetMapping("/user/search")
    Result<Map<String, Object>> searchUsersSimple(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize);

    /**
     * 管理员更新用户状态（禁用/启用）
     */
    @PutMapping("/user/admin/{userId}/status")
    Result<Void> updateUserStatus(@PathVariable Long userId, @RequestParam Integer status);

    /**
     * 管理员更新用户角色
     */
    @PutMapping("/user/admin/{userId}/role")
    Result<Void> updateUserRole(@PathVariable Long userId, @RequestParam Integer role);

    /**
     * 禁言用户
     */
    @PostMapping("/user/admin/{userId}/mute")
    Result<Void> muteUser(
            @PathVariable Long userId,
            @RequestParam Integer minutes,
            @RequestParam(required = false) String reason);

    /**
     * 解除禁言
     */
    @PostMapping("/user/admin/{userId}/unmute")
    Result<Void> unmuteUser(@PathVariable Long userId);

    /**
     * 批量更新用户信息
     */
    @PutMapping("/user/admin/{userId}")
    Result<Void> updateUser(@PathVariable Long userId, @RequestBody Map<String, Object> userInfo);
}