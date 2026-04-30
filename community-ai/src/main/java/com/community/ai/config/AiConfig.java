package com.community.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "spring.ai.openai")
public class AiConfig {

    private String apiKey;
    private String baseUrl;
    private ChatOptions chat = new ChatOptions();

    @Data
    public static class ChatOptions {
        private String model;
        private Double temperature = 0.7;
    }

    public String getModel() {
        return chat != null ? chat.getModel() : null;
    }

    public void setModel(String model) {
        if (chat == null) {
            chat = new ChatOptions();
        }
        chat.setModel(model);
    }

    public Double getTemperature() {
        return chat != null ? chat.getTemperature() : 0.7;
    }

    public void setTemperature(Double temperature) {
        if (chat == null) {
            chat = new ChatOptions();
        }
        chat.setTemperature(temperature);
    }
}