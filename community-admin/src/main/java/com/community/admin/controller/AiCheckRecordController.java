package com.community.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.community.admin.service.AiCheckRecordAdminService;
import com.community.common.dto.AiCheckRecordVO;
import com.community.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/ai-check-records")
@RequiredArgsConstructor
@Tag(name = "AI审核记录", description = "内容管理-追踪 AI 自动审核流水")
public class AiCheckRecordController {

    private final AiCheckRecordAdminService aiCheckRecordAdminService;

    @GetMapping
    @Operation(summary = "分页查询 AI 审核记录")
    public Result<IPage<AiCheckRecordVO>> list(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long postId,
            @RequestParam(required = false) Integer checkResult) {
        return Result.success(aiCheckRecordAdminService.list(page, size, userId, postId, checkResult));
    }
}
