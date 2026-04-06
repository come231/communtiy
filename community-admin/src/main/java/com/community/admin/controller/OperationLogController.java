package com.community.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.community.admin.entity.OperationLog;
import com.community.admin.service.OperationLogService;
import com.community.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/admin/logs")
@Tag(name = "操作日志", description = "系统操作日志接口")
public class OperationLogController {

    private final OperationLogService operationLogService;

    public OperationLogController(OperationLogService operationLogService) {
        this.operationLogService = operationLogService;
    }

    @GetMapping
    @Operation(summary = "日志列表", description = "分页获取操作日志")
    public Result<IPage<OperationLog>> getLogs(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "模块名称") @RequestParam(required = false) String module,
            @Parameter(description = "开始时间") @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        return Result.success(operationLogService.getLogs(page, size, module, startTime, endTime));
    }

    @GetMapping("/modules")
    @Operation(summary = "模块列表", description = "获取所有操作模块")
    public Result<List<String>> getModuleList() {
        return Result.success(operationLogService.getModuleList());
    }
}
