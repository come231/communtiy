package com.community.message.feign;

import com.community.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * 用户服务Feign客户端
 */
@FeignClient(name = "community-user")
public interface UserFeignClient {

    /**
     * 获取用户信息
     */
    @GetMapping("/user/infoById")
    Result<Map<String, Object>> getUserInfo(@RequestParam Long userId);

}