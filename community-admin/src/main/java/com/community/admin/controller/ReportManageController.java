package com.community.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.community.admin.dto.HandleReportDTO;
import com.community.admin.entity.Report;
import com.community.admin.service.ReportService;
import com.community.admin.util.AdminContextHolder;
import com.community.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin/reports")
@Tag(name = "举报管理", description = "内容举报管理接口")
public class ReportManageController {

    private final ReportService reportService;

    public ReportManageController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping
    @Operation(summary = "举报列表", description = "分页获取举报列表")
    public Result<IPage<Report>> getReportList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "处理状态") @RequestParam(required = false) Integer status) {
        return Result.success(reportService.getReportList(page, size, status));
    }

    @PutMapping("/{id}/handle")
    @Operation(summary = "处理举报", description = "处理指定举报")
    public Result<Void> handleReport(@PathVariable Long id,
                                     @RequestBody HandleReportDTO dto) {
        dto.setReportId(id);
        Long handlerId = AdminContextHolder.getAdminId();
        reportService.handleReport(dto, handlerId);
        return Result.success();
    }

    @GetMapping("/count")
    @Operation(summary = "举报统计", description = "获取各状态举报数量统计")
    public Result<Map<Integer, Long>> getReportCount() {
        return Result.success(reportService.getReportCount());
    }
}
