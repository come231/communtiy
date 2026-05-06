package com.community.ai.service;

import com.community.ai.vector.EmbeddingClient;
import com.community.ai.vector.VectorStore;
import com.community.common.util.BuiltinContentRiskScanner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 语义风险检测器（三层防护）
 * 第1层：内置规则（性能最好，捕获已知模式）
 * 第2层：向量语义相似度（捕获「换说法但意思一样」）
 * 第3层：AI大模型（兜底，处理复杂推理）
 *
 * 人机回环：人工审核确认的违规样本自动入库，系统越用越准
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SemanticRiskDetector {

    private final EmbeddingClient embeddingClient;
    private final VectorStore vectorStore;
    private final AiCheckLoader aiCheckLoader; // 用于加载历史违规样本

    @Value("${content.audit.vector.enabled:true}")
    private boolean vectorEnabled;

    @Value("${content.audit.vector.threshold:0.82}")
    private double vectorThreshold;

    @Value("${content.audit.vector.min-score-for-review:0.88}")
    private double reviewThreshold;

    @Value("${content.audit.vector.top-k:3}")
    private int vectorTopK;

    @Value("${content.audit.vector.min-score-for-block:0.90}")
    private double blockThreshold; // 极高相似度直接拦截

    /**
     * 启动时加载历史严重违规样本到向量库
     */
    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        if (!vectorEnabled) {
            log.info("语义向量检测已禁用");
            return;
        }

        try {
            log.info("正在加载历史严重违规样本到向量库...");
            List<Sample> samples = aiCheckLoader.loadSevereCases(5000);

            int loaded = 0;
            for (Sample s : samples) {
                if (s.content() == null || s.content().length() < 10) continue;

                double[] vec = embeddingClient.embed(s.content());
                vectorStore.upsert(
                        s.id(),
                        truncate(s.content(), 200), // 存储前200字用于调试
                        vec,
                        s.label()
                );
                loaded++;
            }

            log.info("向量库初始化完成：加载 {} 条历史样本，当前总量 {}", loaded, vectorStore.size());
        } catch (Exception e) {
            log.error("向量库初始化失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 三层检测
     * @return 检测结果，包含最终判定和每层检测详情
     */
    public DetectionResult detect(String content) {
        List<String> detectionLog = new ArrayList<>();

        // ===== 第1层：内置规则 =====
        BuiltinContentRiskScanner.ScanOutcome builtin = BuiltinContentRiskScanner.scan(content);
        if (builtin.minCheckResult >= 2) {
            detectionLog.add("内置规则命中（严重）：" + builtin.matchedRules);
            return new DetectionResult(2, 85.0, "内置规则命中严重违规模式", builtin.matchedRules, detectionLog);
        } else if (builtin.minCheckResult == 1) {
            detectionLog.add("内置规则命中（可疑）：" + builtin.matchedRules);
            // 继续走向量层，可能升级为拦截
        }

        // ===== 第2层：向量语义相似度 =====
        if (vectorEnabled && vectorStore.size() > 0) {
            try {
                double[] queryVec = embeddingClient.embed(content);
                List<VectorStore.ScoredVector> hits = vectorStore.search(queryVec, vectorTopK, vectorThreshold);

                if (!hits.isEmpty()) {
                    VectorStore.ScoredVector topHit = hits.get(0);
                    detectionLog.add(String.format(
                            "向量命中历史样本: score=%.3f, label=%s, id=%s",
                            topHit.score(), topHit.label(), topHit.id()
                    ));

                    // 极高相似度直接拦截
                    if (topHit.score() >= blockThreshold) {
                        return new DetectionResult(
                                2,
                                Math.min(95.0, 70 + topHit.score() * 25),
                                "与历史严重违规样本高度相似（" + topHit.label() + "）",
                                List.of("向量语义命中:" + topHit.label()),
                                detectionLog
                        );
                    }

                    // 中等相似度至少转人工
                    if (builtin.minCheckResult == 0 && topHit.score() >= reviewThreshold) {
                        return new DetectionResult(
                                1,
                                Math.min(45.0, 25 + topHit.score() * 20),
                                "与历史违规样本语义相似（" + topHit.label() + "），建议复核",
                                List.of("向量语义疑似:" + topHit.label()),
                                detectionLog
                        );
                    }
                    if (builtin.minCheckResult == 0) {
                        detectionLog.add(String.format(
                                "向量命中但未达提级阈值: score=%.3f < reviewThreshold=%.3f, 保持不提级",
                                topHit.score(), reviewThreshold
                        ));
                    }
                }
            } catch (Exception e) {
                log.error("向量检测异常: {}", e.getMessage());
                detectionLog.add("向量检测异常: " + e.getMessage());
            }
        }

        // 内置规则已标记为可疑，向量未推翻，保持可疑
        if (builtin.minCheckResult == 1) {
            return new DetectionResult(
                    1,
                    25.0,
                    "内置规则命中可疑模式",
                    builtin.matchedRules,
                    detectionLog
            );
        }

        // 无明显风险，放行给AI层（最终决策）
        detectionLog.add("内置+向量层未命中，交由AI层终审");
        return new DetectionResult(
                0, // 待AI层决定
                null,
                null,
                List.of(),
                detectionLog
        );
    }

    /**
     * 人工审核反馈：确认违规样本自动入库
     */
    public void feedback(String aiCheckId, String content, boolean isViolation, String label) {
        if (!vectorEnabled || content == null || content.length() < 10) {
            return;
        }

        try {
            // 只有确认违规且标签不为空才入库
            if (isViolation && label != null && !label.isEmpty()) {
                double[] vec = embeddingClient.embed(content);
                vectorStore.upsert(aiCheckId, truncate(content, 200), vec, label);
                log.info("样本入库: id={}, label={}, 向量库总量={}", aiCheckId, label, vectorStore.size());
            }
        } catch (Exception e) {
            log.error("样本入库失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 调试：输入文本，返回向量相似命中
     */
    public List<VectorStore.ScoredVector> debugSearch(String content, int topK, Double minScore) {
        if (!vectorEnabled || content == null || content.isBlank()) {
            return Collections.emptyList();
        }
        double[] queryVec = embeddingClient.embed(content);
        int k = topK > 0 ? topK : vectorTopK;
        double threshold = minScore != null ? minScore : vectorThreshold;
        return vectorStore.search(queryVec, k, threshold);
    }

    /**
     * 调试：查看向量库部分样本
     */
    public List<VectorStore.VectorDocument> listSamples(int limit) {
        if (!vectorEnabled) {
            return Collections.emptyList();
        }
        return vectorStore.list(limit);
    }

    private String truncate(String s, int maxLen) {
        if (s == null || s.length() <= maxLen) return s;
        return s.substring(0, maxLen) + "...";
    }

    // ===== 记录定义 =====

    public record DetectionResult(
            int checkResult,        // 0=待AI决定 1=转人工 2=拦截
            Double violationPercent, // 违规度（可为null，让AI决定）
            String suggestion,      // 说明
            List<String> matchedRules,
            List<String> detectionLog
    ) {}

    public record Sample(String id, String content, String label) {}

    /**
     * 加载历史样本的接口
     */
    public interface AiCheckLoader {
        List<Sample> loadSevereCases(int limit);
    }
}
