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
        String userId = request.getHeader(CommonConstant.HEADER_USER_ID);
        String username = request.getHeader(CommonConstant.HEADER_USERNAME);

        if (StringUtils.hasText(userId)) {
            ContextHolder.setUserId(Long.valueOf(userId));
        }
        if (StringUtils.hasText(username)) {
            ContextHolder.setUsername(username);
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        ContextHolder.clear();
    }
}
