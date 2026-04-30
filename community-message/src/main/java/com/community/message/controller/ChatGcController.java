package com.community.message.controller;

import com.community.common.result.Result;
import com.community.message.mapper.ChatMsgAttachmentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 私聊/会话附件在 DB 中显式 file_id 引用，供全站 file GC
 */
@RestController
@RequestMapping("/chat/internal/gc")
@RequiredArgsConstructor
public class ChatGcController {

    private final ChatMsgAttachmentMapper chatMsgAttachmentMapper;

    @GetMapping("/attachment-file-ids")
    public Result<List<Long>> listAttachmentFileIds() {
        try {
            return Result.success(chatMsgAttachmentMapper.listAllFileIds());
        } catch (Exception e) {
            return Result.success(List.of());
        }
    }
}
