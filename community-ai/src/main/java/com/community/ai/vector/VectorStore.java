package com.community.ai.vector;

import java.util.List;

/**
 * 向量存储抽象接口
 * 用于存储违规内容样本的embedding，支持相似度搜索
 * 当前为简化实现，可扩展为PGVector、Milvus、Pinecone等专业向量数据库
 */
public interface VectorStore {

    /**
     * 添加或更新向量
     * @param id 样本ID（通常对应ai_check.id）
     * @param content 原始文本（用于调试）
     * @param vector embedding向量
     * @param label 分类标签，如"严重违规-危化DIY"
     */
    void upsert(String id, String content, double[] vector, String label);

    /**
     * 批量添加向量
     */
    void upsertBatch(List<VectorDocument> documents);

    /**
     * 相似度搜索
     * @param queryVector 查询向量
     * @param topK 返回最相似的K个
     * @param minScore 最低相似度阈值（0-1，cosine similarity）
     * @return 相似文档列表，按相似度降序
     */
    List<ScoredVector> search(double[] queryVector, int topK, double minScore);

    /**
     * 按标签删除
     */
    void deleteByLabel(String label);

    /**
     * 获取存储的文档数量
     */
    int size();

    /**
     * 清空存储
     */
    void clear();

    /**
     * 调试：列出部分样本（实现可按时间倒序）
     */
    default List<VectorDocument> list(int limit) {
        return List.of();
    }

    /**
     * 向量文档
     */
    record VectorDocument(String id, String content, double[] vector, String label) {}

    /**
     * 带相似度分数的向量结果
     */
    record ScoredVector(String id, String content, String label, double score) {}
}
