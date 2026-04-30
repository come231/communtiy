package com.community.common.feign;

import com.community.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户服务Feign客户端
 */
@FeignClient(name = "community-user", contextId = "userClient")
public interface UserFeignClient {

    /**
     * 获取用户信息
     */
    @GetMapping("/user/info")
    Result<Map<String, Object>> getUserInfo(@RequestParam Long userId);

    /**
     * 更新用户获赞数（内部调用）
     */
    @PostMapping("/user/internal/like-count")
    Result<Void> updateLikeCount(@RequestParam Long userId, @RequestParam Integer delta);
}
