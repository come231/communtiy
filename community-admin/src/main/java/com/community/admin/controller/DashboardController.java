package com.community.admin.controller;

import com.community.admin.service.DashboardService;
import com.community.admin.vo.DashboardVO;
import com.community.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/dashboard")
@Tag(name = "仪表盘", description = "管理后台仪表盘数据接口")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/stats")
    @Operation(summary = "统计数据", description = "获取仪表盘统计数据")
    public Result<DashboardVO> getDashboardStats() {
        return Result.success(dashboardService.getDashboardStats());
    }

    @GetMapping("/user-trend")
    @Operation(summary = "用户趋势", description = "获取用户增长趋势数据")
    public Result<List<Map<String, Object>>> getUserTrend(
            @Parameter(description = "天数") @RequestParam(defaultValue = "7") int days) {
        return Result.success(dashboardService.getUserTrend(days));
    }

    @GetMapping("/post-trend")
    @Operation(summary = "帖子趋势", description = "获取帖子发布趋势数据")
    public Result<List<Map<String, Object>>> getPostTrend(
            @Parameter(description = "天数") @RequestParam(defaultValue = "7") int days) {
        return Result.success(dashboardService.getPostTrend(days));
    }

    @GetMapping("/reports")
    @Operation(summary = "最新举报", description = "获取最新被举报的内容列表")
    public Result<List<Map<String, Object>>> getLatestReports() {
        return Result.success(dashboardService.getLatestReports());
    }
}
