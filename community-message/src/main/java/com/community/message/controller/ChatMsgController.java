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
            @Parameter(description = "对方用户ID") @RequestParam(required = false) Long otherUserId,
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
    @Operation(summary = "发送私信", description = "向指定用户发送私信；可传 sharePostId 分享帖子（对方可点击跳转）")
    public Result<Void> sendChat(@RequestBody SendChatRequest request) {
        chatMsgService.sendChat(request.getToUserId(), request.getContent(), request.getSharePostId(), request.getSharePostTitle());
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
        /** 普通文本；与 sharePostId 二选一或同时有（分享时可为空） */
        private String content;
        /** 若填写则发送帖子分享卡片 */
        private Long sharePostId;
        private String sharePostTitle;
    }

    @GetMapping("/admin/list")
    @Operation(summary = "【管理员】查询所有聊天记录", description = "管理员查看全站聊天记录，支持分页")
    public Result<PageResult<ChatMsg>> getAdminChatList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") int size) {
        return Result.success(chatMsgService.getAdminChatList(page, size));
    }

    @GetMapping("/admin/user")
    @Operation(summary = "【管理员】根据用户ID查询所有聊天记录")
    public Result<PageResult<ChatMsg>> getAdminChatListByUserId(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.success(chatMsgService.getAdminChatListByUserId(userId, page, size));
    }
    @DeleteMapping("/admin/delete/{id}")
    @Operation(summary = "【管理员】删除单条消息", description = "管理员删除任意聊天消息")
    public Result<Void> adminDeleteMessage(@PathVariable Long id) {
        chatMsgService.deleteMessage(id);
        return Result.success();
    }
}
