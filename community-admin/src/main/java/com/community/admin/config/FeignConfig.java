package com.community.admin.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Feign 全局配置
 * 自动传递 Token 到下游服务
 */
@Slf4j
@Configuration
public class FeignConfig {

    /**
     * Feign 请求拦截器
     * 从当前请求的 Header 中提取 Token，传递给下游服务
     */
    @Bean
    public RequestInterceptor requestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                log.debug("Feign RequestInterceptor 执行: uri={}", template.url());
                
                // 获取当前请求的 ServletRequestAttributes
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attributes != null) {
                    HttpServletRequest request = attributes.getRequest();
                    String requestUri = request.getRequestURI();
                    log.debug("原始请求路径: {}", requestUri);

                    // 优先获取 Authorization Header（管理员使用 adminToken 作为 Bearer token）
                    String authorization = request.getHeader("Authorization");
                    if (authorization != null && !authorization.isEmpty()) {
                        template.header("Authorization", authorization);
                        log.info("Feign 传递 Authorization: {} -> {}", requestUri, authorization.substring(0, Math.min(20, authorization.length())) + "...");
                    } else {
                        log.warn("Feign 未获取到 Authorization Header: {}", requestUri);
                    }

                    // 传递 X-User-Id（如果存在）
                    String userId = request.getHeader("X-User-Id");
                    if (userId != null && !userId.isEmpty()) {
                        template.header("X-User-Id", userId);
                        log.debug("Feign 传递 X-User-Id: {}", userId);
                    }

                    // 传递 X-Admin-Id
                    String adminId = request.getHeader("X-Admin-Id");
                    if (adminId != null && !adminId.isEmpty()) {
                        template.header("X-Admin-Id", adminId);
                        log.debug("Feign 传递 X-Admin-Id: {}", adminId);
                    }
                } else {
                    log.error("RequestContextHolder 为空，无法获取原始请求信息！");
                }
            }
        };
    }
}
