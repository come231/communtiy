package com.community.gateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {

    @Value("${jwt.secret}")
    private String secret;

    private static final Set<String> IGNORE_PATHS = Set.of(
            "/user/login", "/user/register", "/user/refresh", "/user/search",
            "/user/info", "/user/my", "/user/update",
            "/user/following/**", "/user/followers/**", "/user/isFollowing/**",
            "/user/checkin", "/user/checkin/status",
            "/admin/**",
            "/search/hot-keywords",
            "/file/upload", "/file/download/**",
            "/sections", "/topics/hot",
            "/posts/list", "/posts/hot", "/posts/detail/**",
            "/comments",
            "/hot-keyword/**",
            // AI chat endpoints
            "/ai/chat", "/ai/chat/sessions", "/ai/chat/session/**", "/ai/chat/history", "/ai/chat/new-session",
            "/ai/write/**", "/ai/review/**",
            // Notice endpoints
            "/notice/list", "/notice/unread-count",
            // Chat endpoints
            "/chat/users", "/chat/list", "/chat/unread-count"
    );

    private static final Set<String> AUTH_REQUIRED_PATHS = Set.of(
            "/likes/**", "/collects/**", "/folders/**"
    );

    private static final Set<String> IGNORE_PREFIXES = Set.of(
            "/posts/", "/comments/", "/topics/", "/sections/",
            "/search/", "/hot-keyword/"
    );

    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public JwtAuthFilter(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        log.info("Gateway request: {}", path);
        log.info("网关收到请求：method={},path={}",
        exchange.getRequest().getMethod(),
        exchange.getRequest().getPath().value());
// ==============================================
        // 🔴 强制放行 OPTIONS 预检请求（解决跨域核心）
        // ==============================================
        if (exchange.getRequest().getMethod() == org.springframework.http.HttpMethod.OPTIONS) {
            return chain.filter(exchange);
        }
        // 放行路径（不需要认证）
        if (isIgnorePath(path)) {
            return chain.filter(exchange);
        }

        // 需要认证的路径
        if (isAuthRequiredPath(path)) {
            String token = extractToken(exchange.getRequest());
            if (!StringUtils.hasText(token)) {
                return unauthorizedResponse(exchange, "未登录，请先登录");
            }

            try {
                SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
                Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();

                String blacklistKey = "user:blacklist:" + claims.getSubject();
                if (Boolean.TRUE.equals(redisTemplate.hasKey(blacklistKey))) {
                    return unauthorizedResponse(exchange, "令牌已失效，请重新登录");
                }

                String userId = String.valueOf(claims.get("userId"));
                String username = (String) claims.get("username");

                ServerHttpRequest request = exchange.getRequest().mutate()
                        .header("X-User-Id", userId)
                        .header("X-Username", username != null ? username : "")
                        .build();

                return chain.filter(exchange.mutate().request(request).build());

            } catch (Exception e) {
                log.warn("JWT验证失败: {}", e.getMessage());
                return unauthorizedResponse(exchange, "认证失败，请重新登录");
            }
        }

        // 其他路径也需要token验证
        String token = extractToken(exchange.getRequest());
        if (!StringUtils.hasText(token)) {
            return unauthorizedResponse(exchange, "未登录，请先登录");
        }

        try {
            SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
            Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();

            String blacklistKey = "user:blacklist:" + claims.getSubject();
            if (Boolean.TRUE.equals(redisTemplate.hasKey(blacklistKey))) {
                return unauthorizedResponse(exchange, "令牌已失效，请重新登录");
            }

            String userId = String.valueOf(claims.get("userId"));
            String username = (String) claims.get("username");

            ServerHttpRequest request = exchange.getRequest().mutate()
                    .header("X-User-Id", userId)
                    .header("X-Username", username != null ? username : "")
                    .build();

            return chain.filter(exchange.mutate().request(request).build());

        } catch (Exception e) {
            log.warn("JWT验证失败: {}", e.getMessage());
            return unauthorizedResponse(exchange, "认证失败，请重新登录");
        }
    }

    private boolean isAuthRequiredPath(String path) {
        for (String authPath : AUTH_REQUIRED_PATHS) {
            if (pathMatcher.match(authPath, path)) return true;
        }
        return false;
    }

    private boolean isIgnorePath(String path) {
        for (String ignorePath : IGNORE_PATHS) {
            if (pathMatcher.match(ignorePath, path)) return true;
        }
        for (String prefix : IGNORE_PREFIXES) {
            if (path.startsWith(prefix)) return true;
        }
        return false;
    }

    private String extractToken(ServerHttpRequest request) {
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return authHeader;
    }

    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 401);
        result.put("message", message);
        result.put("data", null);
        try {
            DataBuffer buffer = response.bufferFactory()
                    .wrap(objectMapper.writeValueAsBytes(result));
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            return response.setComplete();
        }
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
