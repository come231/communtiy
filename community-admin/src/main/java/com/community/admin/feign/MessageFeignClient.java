package com.community.admin.feign;

import com.community.common.result.PageResult;
import com.community.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 消息服务Feign客户端
 */
@FeignClient(name = "community-message")
public interface MessageFeignClient {

    @GetMapping("/chat/list")
    Result<PageResult<Map<String, Object>>> getChatList(
            @RequestParam(required = false) Long otherUserId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size);

    @GetMapping("/chat/users")
    Result<java.util.List<Map<String, Object>>> getChatUserList();

    @DeleteMapping("/chat/delete/{id}")
    Result<Void> deleteMessage(@PathVariable Long id);
}
