package com.community.ai.vector;

/**
 * Embedding客户端接口
 * 将文本转换为向量表示
 */
public interface EmbeddingClient {

    /**
     * 将单条文本转换为embedding向量
     * @param text 输入文本
     * @return 向量数组（通常是384、768、1536维等）
     */
    double[] embed(String text);

    /**
     * 批量embedding（效率更高）
     */
    double[][] embedBatch(String[] texts);

    /**
     * 获取向量维度
     */
    int getDimensions();
}
