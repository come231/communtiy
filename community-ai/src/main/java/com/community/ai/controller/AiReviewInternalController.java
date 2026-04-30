package com.community.ai.controller;

import com.community.ai.service.AiReviewFeedbackService;
import com.community.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * AI审核内部接口（供管理端人工审核后反馈）
 * 路径：/ai/internal/**
 */
@RestController
@RequestMapping("/ai/internal")
@RequiredArgsConstructor
@Tag(name = "AI审核内部接口", description = "人工审核反馈、样本管理等")
public class AiReviewInternalController {

    private final AiReviewFeedbackService feedbackService;

    /**
     * 人工审核反馈
     * 管理端确认或纠正AI判定后调用，用于数据飞轮
     */
    @PostMapping("/feedback/{checkId}")
    @Operation(summary = "人工审核反馈", description = "确认或纠正AI判定，样本自动进入向量库")
    public Result<Void> feedback(
            @PathVariable Long checkId,
            @RequestParam Integer manualStatus,  // 1=确认AI正确 2=纠正为违规 3=纠正为正常
            @RequestParam(required = false) String label,     // 标签，如"严重违规-危化DIY"
            @RequestParam(required = false) String remark,    // 备注
            @RequestAttribute("adminId") Long reviewerId      // 从网关或拦截器注入
    ) {
        feedbackService.handleFeedback(checkId, manualStatus, label, remark, reviewerId);
        return Result.success();
    }

    /**
     * 批量标记为已加入向量库（内部使用）
     */
    @PostMapping("/mark-in-vector/{checkId}")
    @Operation(summary = "标记已入库", description = "标记该记录已加入向量样本库")
    public Result<Void> markInVectorStore(@PathVariable Long checkId) {
        feedbackService.markInVectorStore(checkId);
        return Result.success();
    }

    /**
     * 获取向量库统计
     */
    @GetMapping("/vector-stats")
    @Operation(summary = "向量库统计", description = "获取语义样本库数量")
    public Result<VectorStatsVO> getVectorStats() {
        return Result.success(new VectorStatsVO(
                feedbackService.getVectorStoreSize(),
                "样本自动从人工审核确认的严重违规记录生成"
        ));
    }

    public record VectorStatsVO(int totalSize, String description) {}
}
