package com.community.admin.feign;

import com.community.common.result.PageResult;
import com.community.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "community-community", contextId = "sensitiveFilterFeignClient")
public interface SensitiveFilterFeignClient {

    @GetMapping("/comments/admin/sensitive-records")
    Result<PageResult<Map<String, Object>>> listRecords(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long userId
    );

    @GetMapping("/comments/admin/sensitive-stats")
    Result<Map<String, Object>> getStats();
}
