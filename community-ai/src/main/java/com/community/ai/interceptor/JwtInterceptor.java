package com.community.ai.interceptor;

import com.community.common.constant.CommonConstant;
import com.community.common.constant.RedisKeyConstant;
import com.community.common.util.ContextHolder;
import com.community.common.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();
        
        // 优先从 Header 获取（Gateway 传递）
        String userId = request.getHeader(CommonConstant.HEADER_USER_ID);
        String username = request.getHeader(CommonConstant.HEADER_USERNAME);
        
        log.debug("JwtInterceptor: path={}, X-User-Id={}", path, userId);

        // 如果 Header 中没有用户信息，尝试从 Authorization Token 解析
        // 这支持 Feign 调用和管理员后台直接访问
        if (!StringUtils.hasText(userId)) {
            String authorization = request.getHeader("Authorization");
            log.debug("JwtInterceptor: X-User-Id 为空，尝试解析 Authorization: {}", 
                    authorization != null ? authorization.substring(0, Math.min(20, authorization.length())) + "..." : "null");
            
            if (StringUtils.hasText(authorization) && authorization.startsWith("Bearer ")) {
                String token = authorization.substring(7);
                try {
                    if (jwtUtil.validateToken(token)) {
                        Long tokenUserId = jwtUtil.getUserId(token);
                        String tokenUsername = jwtUtil.getUsername(token);
                        if (tokenUserId != null) {
                            userId = String.valueOf(tokenUserId);
                            username = tokenUsername;
                            log.info("从Token解析用户成功: userId={}, username={}, path={}", userId, username, path);
                        }
                    } else {
                        log.warn("Token 验证失败，可能已过期: {}", token.substring(0, Math.min(20, token.length())) + "...");
                    }
                } catch (Exception e) {
                    log.error("Token解析异常: {}", e.getMessage());
                }
            } else {
                log.warn("Authorization Header 格式不正确或为空");
            }
        }

        if (StringUtils.hasText(userId)) {
            ContextHolder.setUserId(Long.valueOf(userId));
            log.debug("设置用户上下文: userId={}", userId);
        } else {
            log.warn("无法获取用户ID，路径: {}", path);
        }
        
        if (StringUtils.hasText(username)) {
            ContextHolder.setUsername(username);
        }
        
        // 对于 /ai/admin/** 路径，必须有用户ID才能访问
        if (path.startsWith("/ai/admin/") && !StringUtils.hasText(userId)) {
            log.error("访问 {} 需要登录，但未获取到用户ID", path);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"code\":401,\"message\":\"登录已过期，请重新登录\"}");
            return false;
        }
        
        // /ai/internal/** 是内部接口，允许匿名访问（由Gateway统一控制权限）
        if (path.startsWith("/ai/internal/")) {
            log.debug("访问内部接口，放行: {}", path);
            return true;
        }
        
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        ContextHolder.clear();
    }
}
