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
            "/user/info", "/user/my", "/user/update","/user/infoById",
            "/user/following/**", "/user/followers/**", "/user/isFollowing/**",
            "/user/checkin", "/user/checkin/status",
            "/user/admin/**",
            "/posts/admin/**",
            "/admin/**","/dashboard/**",
            "/search/hot-keywords",
            "/file/upload", "/file/download/**",
            "/sections", "/sections/**", "/topics/hot", "/topics", "/topics/**",
            "/posts/list", "/posts/hot", "/posts/detail/**",
            "/comments",
            "/hot-keyword/**",
            // AI endpoints - 管理员需要访问的AI接口
            "/ai/write/**", "/ai/review/**", "/ai/admin/**", "/ai/internal/**",
            // Notice endpoints
            "/notice/list", "/notice/unread-count",
            // Chat endpoints
            "/chat/users", "/chat/list", "/chat/unread-count",
            // WebSocket endpoints - 必须放行以支持WebSocket握手
            "/ws/chat/**",
            // Interact stats
            "/interact/stats",
            // Admin internal endpoints (服务间调用敏感词同步等)
            "/admin/internal/**"
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

        // 强制放行 OPTIONS 预检请求（解决跨域核心）
        if (exchange.getRequest().getMethod() == org.springframework.http.HttpMethod.OPTIONS) {
            return chain.filter(exchange);
        }

        // 所有路径都尝试提取用户信息并传递到下游（try enhance模式）
        // 有token则解析并传递X-User-Id/X-Username头，无token则不处理
        ServerWebExchange enhancedExchange = tryEnhanceWithUser(exchange);
        if (enhancedExchange == null) {
            // 解析token时发生异常（token无效/过期），仅对需要认证的路径返回401
            if (isAuthRequiredPath(path)) {
                return unauthorizedResponse(exchange, "认证失败，请重新登录");
            }
            // 其他路径（包括白名单路径），token无效时直接放行（不带用户信息）
            return chain.filter(exchange);
        }

        // 放行路径（不需要认证），但已经携带了用户信息（如果有token的话）
        if (isIgnorePath(path)) {
            return chain.filter(enhancedExchange);
        }

        // 需要认证的路径
        if (isAuthRequiredPath(path)) {
            String token = extractToken(exchange.getRequest());
            if (!StringUtils.hasText(token)) {
                return unauthorizedResponse(exchange, "未登录，请先登录");
            }
            // tryEnhanceWithUser已成功解析token（enhancedExchange不为null）
            return chain.filter(enhancedExchange);
        }

        // 其他路径：有token则验证并传递，无token则返回401
        String token = extractToken(exchange.getRequest());
        if (!StringUtils.hasText(token)) {
            return unauthorizedResponse(exchange, "未登录，请先登录");
        }
        // tryEnhanceWithUser已成功（enhancedExchange不为null）
        return chain.filter(enhancedExchange);
    }

    /**
     * 尝试从请求中解析JWT token并增强请求头（X-User-Id, X-Username）。
     * 有有效token时返回增强后的exchange；无token时返回原始exchange；token无效时返回null。
     */
    private ServerWebExchange tryEnhanceWithUser(ServerWebExchange exchange) {
        String token = extractToken(exchange.getRequest());
        if (!StringUtils.hasText(token)) {
            // 无token，不增强，返回原始exchange
            return exchange;
        }

        try {
            SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
            Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();

            String blacklistKey = "user:blacklist:" + claims.getSubject();
            if (Boolean.TRUE.equals(redisTemplate.hasKey(blacklistKey))) {
                // token在黑名单中
                return null;
            }

            String userId = String.valueOf(claims.get("userId"));
            String username = (String) claims.get("username");

            ServerHttpRequest request = exchange.getRequest().mutate()
                    .header("X-User-Id", userId)
                    .header("X-Username", username != null ? username : "")
                    .build();

            return exchange.mutate().request(request).build();
        } catch (Exception e) {
            log.warn("JWT解析失败: {}", e.getMessage());
            return null;
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
