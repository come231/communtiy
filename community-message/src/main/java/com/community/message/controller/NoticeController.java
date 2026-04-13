package com.community.message.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.community.common.result.PageResult;
import com.community.common.result.Result;
import com.community.message.entity.Notice;
import com.community.message.mapper.NoticeMapper;
import com.community.message.service.NoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notice")
@RequiredArgsConstructor
@Tag(name = "通知管理", description = "用户通知接口")
public class NoticeController {

    private final NoticeService noticeService;
    private final NoticeMapper noticeMapper;

    @GetMapping("/list")
    @Operation(summary = "通知列表", description = "分页获取当前用户的通知列表")
    public Result<PageResult<Notice>> getNotices(
            @Parameter(description = "通知类型") @RequestParam(required = false) Integer type,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(noticeService.getNotices(type, pageNum, pageSize));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除通知", description = "删除指定通知")
    public Result<Void> deleteNotice(@PathVariable Long id) {
        noticeService.deleteNotice(id);
        return Result.success();
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "标记已读", description = "将单条通知标记为已读")
    public Result<Void> markAsRead(@PathVariable Long id) {
        noticeService.markAsRead(id);
        return Result.success();
    }

    @PutMapping("/read-all")
    @Operation(summary = "全部已读", description = "将所有通知标记为已读")
    public Result<Void> markAllRead() {
        noticeService.markAllRead();
        return Result.success();
    }
    // ==================== 【管理员接口】====================
    @GetMapping("/admin/list")
    @Operation(summary = "【管理员】查询所有通知")
    public Result<PageResult<Notice>> getAdminNotices(
            @RequestParam(required = false) Integer type,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        LambdaQueryWrapper<Notice> wrapper = new LambdaQueryWrapper<>();
        if (type != null) {
            wrapper.eq(Notice::getType, type);
        }
        wrapper.orderByDesc(Notice::getCreateTime);
        Page<Notice> pageParam = new Page<>(page, size);
        Page<Notice> result = noticeMapper.selectPage(pageParam, wrapper);
        return Result.success(new PageResult<>(result.getRecords(), result.getTotal(), size, page));
    }

    @DeleteMapping("/admin/delete/{id}")
    @Operation(summary = "【管理员】删除通知")
    public Result<Void> adminDeleteNotice(@PathVariable Long id) {
        noticeMapper.deleteById(id);
        return Result.success();
    }
// ==================== 【管理员接口结束】====================

    @GetMapping("/unread-count")
    @Operation(summary = "未读数量", description = "获取未读通知数量")
    public Result<Long> getUnreadCount() {
        return Result.success(noticeService.getUnreadCount());
    }
}
