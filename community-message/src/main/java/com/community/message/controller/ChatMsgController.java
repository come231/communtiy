package com.community.message.controller;

import com.community.common.result.PageResult;
import com.community.common.result.Result;
import com.community.message.entity.ChatMsg;
import com.community.message.service.ChatMsgService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
@Tag(name = "私信聊天", description = "用户私信聊天接口")
public class ChatMsgController {

    private final ChatMsgService chatMsgService;

    @GetMapping("/list")
    @Operation(summary = "聊天记录", description = "获取与指定用户的聊天记录")
    public Result<PageResult<ChatMsg>> getChatList(
            @Parameter(description = "对方用户ID") @RequestParam Long otherUserId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size) {
        return Result.success(chatMsgService.getChatList(otherUserId, page, size));
    }

    @GetMapping("/users")
    @Operation(summary = "联系人列表", description = "获取最近联系的用户列表")
    public Result<List<Map<String, Object>>> getChatUserList() {
        return Result.success(chatMsgService.getChatUserList());
    }

    @PostMapping("/send")
    @Operation(summary = "发送私信", description = "向指定用户发送私信")
    public Result<Void> sendChat(@RequestBody SendChatRequest request) {
        chatMsgService.sendChat(request.getToUserId(), request.getContent());
        return Result.success();
    }

    @PutMapping("/read")
    @Operation(summary = "标记已读", description = "标记来自指定用户的消息为已读")
    public Result<Void> markChatRead(@Parameter(description = "发送者用户ID") @RequestParam Long fromUserId) {
        chatMsgService.markChatRead(fromUserId);
        return Result.success();
    }

    @GetMapping("/unread-count")
    @Operation(summary = "未读数量", description = "获取未读私信总数")
    public Result<Long> getUnreadChatCount() {
        return Result.success(chatMsgService.getUnreadChatCount());
    }

    @lombok.Data
    public static class SendChatRequest {
        private Long toUserId;
        private String content;
    }
}
