package com.community.admin.feign;

import com.community.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

/**
 * 互动服务Feign客户端
 */
@FeignClient(name = "community-interact")
public interface InteractFeignClient {

    @GetMapping("/interact/stats")
    Result<Map<String, Object>> getInteractStats();
}
