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
     * 搜索用户
     */
    @GetMapping("/user/search")
    Result<Map<String, Object>> searchUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize);
    
    /**
     * 获取用户统计信息
     */
    @GetMapping("/user/admin/stats")
    Result<Map<String, Object>> getUserStats();

    /**
     * 管理员更新用户状态（禁用/启用）
     */
    @PutMapping("/user/admin/{userId}/status")
    Result<Void> updateUserStatus(@PathVariable Long userId, @RequestParam Integer status);
}