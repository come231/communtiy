package com.community.community.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Feign 全局配置
 * 服务间调用配置
 */
@Slf4j
@Configuration
public class FeignConfig {

    /**
     * Feign 请求拦截器
     * 为服务间调用添加必要的标识
     */
    @Bean
    public RequestInterceptor requestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                // 标记为内部服务调用
                template.header("X-Internal-Call", "true");
                log.debug("Feign 服务间调用: {}", template.url());
            }
        };
    }
}
