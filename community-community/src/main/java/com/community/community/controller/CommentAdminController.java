package com.community.community.controller;

import com.community.common.result.PageResult;
import com.community.common.result.Result;
import com.community.community.entity.SensitiveFilterRecord;
import com.community.community.service.SensitiveFilterRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/comments/admin")
@RequiredArgsConstructor
@Tag(name = "评论管理(管理员)", description = "评论管理的管理员接口")
public class CommentAdminController {

    private final SensitiveFilterRecordService sensitiveFilterRecordService;

    @GetMapping("/sensitive-records")
    @Operation(summary = "分页查询敏感词过滤记录")
    public Result<PageResult<SensitiveFilterRecord>> listSensitiveFilterRecords(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long userId) {
        return Result.success(sensitiveFilterRecordService.pageRecords(pageNum, pageSize, keyword, userId));
    }

    @GetMapping("/sensitive-stats")
    @Operation(summary = "敏感词过滤统计")
    public Result<Map<String, Object>> sensitiveFilterStats() {
        return Result.success(sensitiveFilterRecordService.getStats());
    }
}
