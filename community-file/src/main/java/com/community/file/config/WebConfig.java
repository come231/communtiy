package com.community.file.config;

import com.community.common.constant.CommonConstant;
import com.community.common.util.ContextHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new UserIdInterceptor()).addPathPatterns("/**");
    }

    private static class UserIdInterceptor implements HandlerInterceptor {
        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
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
}
