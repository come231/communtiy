package com.community.ai.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * AI调用工具类 - 使用原生HttpClient调用OpenAI API
 * 避免依赖不稳定的Spring AI版本
 */
@Component
@Slf4j
public class AiCallHelper {

    @Value("${spring.ai.openai.api-key:df28fd2c06ca400f8f2af93901dc16cb.lDfaiHsjXkIKCVaY}")
    private String apiKey;

    @Value("${spring.ai.openai.base-url:https://open.bigmodel.cn/api/paas/v4}")
    private String baseUrl;

    @Value("${spring.ai.openai.chat.options.model:glm-4-flash}")
    private String model;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();

    private boolean aiEnabled = false;

    /**
     * 发送消息到AI
     */
    public String call(String prompt) {
        return callWithSystem(null, prompt);
    }

    /**
     * 发送带系统提示的消息到AI
     */
    public String callWithSystem(String systemPrompt, String userMsg) {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new RuntimeException("AI功能未启用：未配置 API Key，请在 application.yml 中配置 spring.ai.openai.api-key");
        }

        try {
            // 构建请求体
            StringBuilder requestBody = new StringBuilder();
            requestBody.append("{\"model\":\"").append(model).append("\",\"messages\":[");
            
            if (systemPrompt != null && !systemPrompt.isEmpty()) {
                requestBody.append("{\"role\":\"system\",\"content\":\"").append(escapeJson(systemPrompt)).append("\"},");
            }
            requestBody.append("{\"role\":\"user\",\"content\":\"").append(escapeJson(userMsg)).append("\"}");
            requestBody.append("],\"temperature\":0.7}");
            
            // 发送请求
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/chat/completions"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                    .timeout(Duration.ofSeconds(120))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                String body = response.body();
                // 简单解析 JSON 响应
                int contentIndex = body.indexOf("\"content\":\"");
                if (contentIndex > 0) {
                    int start = contentIndex + 11;
                    int end = body.indexOf("\"", start);
                    return unescapeJson(body.substring(start, end));
                }
                return body;
            } else {
                log.error("AI API调用失败: {} - {}", response.statusCode(), response.body());
                throw new RuntimeException("AI API调用失败: " + response.statusCode());
            }
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException("AI调用异常: " + e.getMessage(), e);
        }
    }

    public boolean isEnabled() {
        return apiKey != null && !apiKey.isEmpty();
    }

    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }

    private String unescapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\\"", "\"")
                   .replace("\\n", "\n")
                   .replace("\\r", "\r")
                   .replace("\\t", "\t")
                   .replace("\\\\", "\\");
    }
}
