package com.community.interact.feign;

import com.community.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(name = "community-community")
public interface CommunityPostFeignClient {

    @GetMapping("/posts/{postId}")
    Result<Map<String, Object>> getPostDetail(@PathVariable("postId") Long postId);
}
