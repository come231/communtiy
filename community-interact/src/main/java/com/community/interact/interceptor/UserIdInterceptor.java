package com.community.interact.interceptor;

import com.community.common.constant.CommonConstant;
import com.community.common.util.ContextHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class UserIdInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String userId = request.getHeader(CommonConstant.HEADER_USER_ID);
        String username = request.getHeader(CommonConstant.HEADER_USERNAME);
        if (StringUtils.hasText(userId)) ContextHolder.setUserId(Long.valueOf(userId));
        if (StringUtils.hasText(username)) ContextHolder.setUsername(username);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        ContextHolder.clear();
    }
}
