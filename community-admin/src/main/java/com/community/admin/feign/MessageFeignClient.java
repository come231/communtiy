package com.community.admin.feign;

import com.community.admin.vo.ChatMsgVO;
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

    // 管理员 → 根据用户ID查询消息
    @GetMapping("/chat/admin/user")
    Result<PageResult<ChatMsgVO>> getAdminChatListByUserId(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size);


    @GetMapping("/chat/admin/list")
    Result<PageResult<ChatMsgVO>> getAdminChatList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size);

    @DeleteMapping("/chat/admin/delete/{id}")
    Result<Void> adminDeleteMessage(@PathVariable Long id);

    // ==================== 【管理员 - 通知管理】====================
    @GetMapping("/notice/admin/list")
    Result<PageResult<ChatMsgVO>> getAdminNotices(
            @RequestParam(required = false) Integer type,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size);

    @DeleteMapping("/notice/admin/delete/{id}")
    Result<Void> adminDeleteNotice(@PathVariable Long id);


}
