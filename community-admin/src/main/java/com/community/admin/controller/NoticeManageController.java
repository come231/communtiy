package com.community.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.community.admin.service.NoticeManageService;
import com.community.admin.vo.ChatMsgVO;
import com.community.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/notices")
@RequiredArgsConstructor
@Tag(name = "通知管理", description = "管理员通知管理")
public class NoticeManageController {

    private final NoticeManageService noticeManageService;

    @GetMapping
    @Operation(summary = "通知列表")
    public Result<IPage<ChatMsgVO>> getNotices(
            @RequestParam(required = false) Integer type,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.success(noticeManageService.getNoticeList(type, page, size));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除通知")
    public Result<Void> deleteNotice(@PathVariable Long id) {
        noticeManageService.deleteNotice(id);
        return Result.success();
    }
}