package com.community.admin.controller;

import com.community.admin.feign.MessageFeignClient;
import com.community.common.result.PageResult;
import com.community.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 消息管理
 */
@RestController
@RequestMapping("/admin/messages")
@RequiredArgsConstructor
@Tag(name = "消息管理", description = "管理员消息管理接口")
public class MessageManageController {

    private final MessageFeignClient messageFeignClient;

    @GetMapping
    @Operation(summary = "消息列表", description = "获取所有用户私信列表")
    public Result<PageResult<Map<String, Object>>> getMessages(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            return messageFeignClient.getChatList(null, page, size);
        } catch (Exception e) {
            return Result.error("获取消息列表失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除消息", description = "删除指定私信")
    public Result<Void> deleteMessage(@PathVariable Long id) {
        try {
            return messageFeignClient.deleteMessage(id);
        } catch (Exception e) {
            return Result.error("删除消息失败: " + e.getMessage());
        }
    }
}
