package com.community.ai.controller;

import com.community.ai.service.AiReviewFeedbackService;
import com.community.ai.service.SemanticRiskDetector;
import com.community.ai.vector.VectorStore;
import com.community.common.result.Result;
import com.community.common.util.ContextHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * AI审核内部接口（供管理端人工审核后反馈）
 * 路径：/ai/internal/**
 */
@Slf4j
@RestController
@RequestMapping("/ai/internal")
@RequiredArgsConstructor
@Tag(name = "AI审核内部接口", description = "人工审核反馈、样本管理等")
public class AiReviewInternalController {

    private final AiReviewFeedbackService feedbackService;
    private final SemanticRiskDetector semanticRiskDetector;

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
            @RequestParam(required = false) String remark    // 备注
    ) {
        // 从 ContextHolder 获取当前用户ID（通过 JwtInterceptor 注入）
        Long reviewerId = ContextHolder.getUserId();
        log.info("人工审核反馈: checkId={}, manualStatus={}, reviewerId={}", checkId, manualStatus, reviewerId);
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

    @PostMapping("/vector-search-debug")
    @Operation(summary = "向量命中调试", description = "输入文本返回相似样本及分数")
    public Result<java.util.List<VectorHitVO>> vectorSearchDebug(
            @RequestParam String content,
            @RequestParam(defaultValue = "3") Integer topK,
            @RequestParam(required = false) Double minScore) {
        java.util.List<VectorStore.ScoredVector> hits = semanticRiskDetector.debugSearch(content, topK, minScore);
        java.util.List<VectorHitVO> data = hits.stream()
                .map(h -> new VectorHitVO(h.id(), h.label(), h.score(), h.content()))
                .toList();
        return Result.success(data);
    }

    @GetMapping("/vector-samples")
    @Operation(summary = "向量样本列表", description = "查看向量库最近样本")
    public Result<java.util.List<VectorSampleVO>> vectorSamples(
            @RequestParam(defaultValue = "20") Integer limit) {
        java.util.List<VectorStore.VectorDocument> docs = semanticRiskDetector.listSamples(limit);
        java.util.List<VectorSampleVO> data = docs.stream()
                .map(d -> new VectorSampleVO(d.id(), d.label(), d.content(), d.vector() != null ? d.vector().length : 0))
                .toList();
        return Result.success(data);
    }

    public record VectorStatsVO(int totalSize, String description) {}
    public record VectorHitVO(String id, String label, double score, String content) {}
    public record VectorSampleVO(String id, String label, String content, int dimensions) {}
}
