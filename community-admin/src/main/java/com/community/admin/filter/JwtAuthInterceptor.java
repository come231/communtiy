package com.community.admin.filter;

import cn.hutool.core.text.AntPathMatcher;
import com.community.admin.service.AdminAuthService;
import com.community.admin.util.AdminContextHolder;
import com.community.common.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import java.io.IOException;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final AdminAuthService adminAuthService;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    // 从 yml 读取自动刷新阈值（你要的 <5分钟）
    @Value("${jwt.auto-refresh-threshold:300000}")
    private long autoRefreshThreshold;

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) throws Exception {

        // 1. 放行 OPTIONS 预检请求（解决跨域）
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String path = request.getRequestURI();
        log.debug("Admin服务请求路径: {}", path);

        // 3. 获取 Token
        String bearerToken = request.getHeader("Authorization");
        if (!StringUtils.hasText(bearerToken) || !bearerToken.startsWith("Bearer ")) {
            setUnauthorized(response, "未登录，请先登录");
            return false;
        }

        String token = bearerToken.substring(7);

        try {
            // 4. 黑名单校验
            if (Boolean.TRUE.equals(adminAuthService.isBlacklisted(token))) {
                setUnauthorized(response, "令牌已失效");
                return false;
            }

            // 5. 过期校验
            if (jwtUtil.isTokenExpired(token)) {
                setUnauthorized(response, "令牌已过期");
                return false;
            }

            // 6. 必须是 accessToken
            if (!jwtUtil.isAccessToken(token)) {
                setUnauthorized(response, "无效的令牌类型");
                return false;
            }
// ==========================================
            // 🔥 自动刷新逻辑（你要的核心功能）
            // ==========================================
            long remain = jwtUtil.getTokenRemainingTime(token);
            if (remain <= autoRefreshThreshold) {
                log.info("token 即将过期，自动刷新：剩余 {}ms", remain);

                // 从当前 token 获取 adminId
                Long adminId = jwtUtil.getUserId(token);

                // 调用你原有的 Service 刷新
                Map<String, Object> newToken = adminAuthService.refreshTokenByAdminId(adminId);

                // 把新 token 放进响应头
                response.setHeader("new-refresh-token", (String) newToken.get("refreshToken"));
                response.setHeader("new-access-token", "Bearer " + newToken.get("accessToken"));

                log.info("自动刷新 token 成功，adminId: {}", adminId);
            }
            // 7. 解析用户信息
            Long adminId = jwtUtil.getUserId(token);
            String username = jwtUtil.getUsername(token);

            // 8. 存入上下文
            AdminContextHolder.setAdminId(adminId);
            AdminContextHolder.setUsername(username);
            System.out.println("访问令牌为："+token);
            return true;

        } catch (Exception e) {
            log.error("JWT 认证失败: {}", e.getMessage());
            AdminContextHolder.clear();
            setUnauthorized(response, "认证失败");
            return false;
        }
    }

    // 统一返回 401 JSON
    private void setUnauthorized(HttpServletResponse response, String msg) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(401);
        response.getWriter().write("{\"code\":401,\"message\":\"" + msg + "\"}");
    }


    @Override
    public void afterCompletion(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            Exception ex
    ) {
        // 清理上下文
        AdminContextHolder.clear();
    }
}