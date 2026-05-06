package com.community.ai.vector;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 内存向量存储实现（简化版）
 * 适用于样本量<1万的场景，无需外部依赖
 * 生产环境可替换为PGVector/Milvus实现
 */
@Slf4j
@Component
@ConditionalOnProperty(
        prefix = "content.audit.vector",
        name = "store-type",
        havingValue = "inmemory",
        matchIfMissing = true
)
public class InMemoryVectorStore implements VectorStore {

    private final Map<String, VectorDocument> storage = new ConcurrentHashMap<>();

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        log.info("InMemoryVectorStore 初始化完成，当前容量: {}", storage.size());
    }

    @Override
    public void upsert(String id, String content, double[] vector, String label) {
        storage.put(id, new VectorDocument(id, content, vector, label));
        log.debug("向量入库: id={}, label={}, 当前总量={}", id, label, storage.size());
    }

    @Override
    public void upsertBatch(List<VectorDocument> documents) {
        for (VectorDocument doc : documents) {
            storage.put(doc.id(), doc);
        }
        log.info("批量向量入库: {} 条，当前总量: {}", documents.size(), storage.size());
    }

    @Override
    public List<ScoredVector> search(double[] queryVector, int topK, double minScore) {
        if (storage.isEmpty() || queryVector == null) {
            return Collections.emptyList();
        }

        return storage.values().stream()
                .map(doc -> {
                    double score = cosineSimilarity(queryVector, doc.vector());
                    return new ScoredVector(doc.id(), doc.content(), doc.label(), score);
                })
                .filter(sv -> sv.score() >= minScore)
                .sorted(Comparator.comparingDouble(ScoredVector::score).reversed())
                .limit(topK)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteByLabel(String label) {
        List<String> toDelete = storage.values().stream()
                .filter(doc -> label.equals(doc.label()))
                .map(VectorDocument::id)
                .toList();
        toDelete.forEach(storage::remove);
        log.info("按标签删除向量: label={}, 删除{}条", label, toDelete.size());
    }

    @Override
    public int size() {
        return storage.size();
    }

    @Override
    public void clear() {
        storage.clear();
        log.info("向量库已清空");
    }

    @Override
    public List<VectorDocument> list(int limit) {
        int n = limit > 0 ? limit : 20;
        return storage.values().stream().limit(n).collect(Collectors.toList());
    }

    /**
     * 余弦相似度计算
     * @return 相似度分数，范围[-1, 1]，通常归一化到[0, 1]
     */
    private double cosineSimilarity(double[] v1, double[] v2) {
        if (v1 == null || v2 == null || v1.length != v2.length) {
            return -1.0;
        }

        double dot = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (int i = 0; i < v1.length; i++) {
            dot += v1[i] * v2[i];
            norm1 += v1[i] * v1[i];
            norm2 += v2[i] * v2[i];
        }

        if (norm1 == 0.0 || norm2 == 0.0) {
            return 0.0;
        }

        // 归一化到[0, 1]
        double cosine = dot / (Math.sqrt(norm1) * Math.sqrt(norm2));
        return (cosine + 1.0) / 2.0;
    }
}
