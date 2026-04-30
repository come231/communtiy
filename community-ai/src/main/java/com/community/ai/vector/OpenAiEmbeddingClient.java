package com.community.ai.vector;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * OpenAI Embedding API客户端
 * 使用text-embedding-3-small模型（性价比高，1536维）
 * 国内用户可替换为阿里、百度等embedding服务
 */
@Slf4j
@Component
public class OpenAiEmbeddingClient implements EmbeddingClient {

    @Value("${spring.ai.openai.api-key:}")
    private String apiKey;

    @Value("${spring.ai.openai.base-url:https://api.openai.com}")
    private String baseUrl;

    @Value("${spring.ai.embedding.model:text-embedding-3-small}")
    private String model;

    @Value("${spring.ai.embedding.enabled:false}")
    private boolean enabled;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public double[] embed(String text) {
        if (!enabled || apiKey.isEmpty()) {
            // 降级：返回基于哈希的伪向量（仅用于测试，生产应报错或换本地模型）
            log.warn("Embedding服务未启用，返回降级向量");
            return fallbackVector(text);
        }

        try {
            String requestBody = objectMapper.writeValueAsString(new EmbeddingRequest(model, text));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl.endsWith("/") ? baseUrl + "v1/embeddings" : baseUrl + "/v1/embeddings"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                    .timeout(Duration.ofSeconds(60))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("Embedding API失败: {} - {}", response.statusCode(), response.body());
                return fallbackVector(text);
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode embedding = root.path("data").get(0).path("embedding");

            double[] vector = new double[embedding.size()];
            for (int i = 0; i < embedding.size(); i++) {
                vector[i] = embedding.get(i).asDouble();
            }
            return vector;

        } catch (Exception e) {
            log.error("Embedding计算异常: {}", e.getMessage(), e);
            return fallbackVector(text);
        }
    }

    @Override
    public double[][] embedBatch(String[] texts) {
        double[][] result = new double[texts.length][];
        for (int i = 0; i < texts.length; i++) {
            result[i] = embed(texts[i]);
        }
        return result;
    }

    @Override
    public int getDimensions() {
        return switch (model) {
            case "text-embedding-3-small" -> 1536;
            case "text-embedding-3-large" -> 3072;
            case "text-embedding-ada-002" -> 1536;
            default -> 1536;
        };
    }

    /**
     * 降级方案：基于文本哈希生成伪向量
     * 仅用于测试，无实际语义相似度能力
     */
    private double[] fallbackVector(String text) {
        int dim = 384; // 降维以减少内存占用
        double[] vec = new double[dim];
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);

        for (int i = 0; i < bytes.length; i++) {
            vec[i % dim] += bytes[i] / 255.0;
        }

        // 归一化
        double norm = 0;
        for (double v : vec) norm += v * v;
        norm = Math.sqrt(norm);
        if (norm > 0) {
            for (int i = 0; i < vec.length; i++) vec[i] /= norm;
        }
        return vec;
    }

    private record EmbeddingRequest(String model, String input) {}
}
