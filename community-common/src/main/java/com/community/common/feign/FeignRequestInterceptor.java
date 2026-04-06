package com.community.common.feign;

import com.community.common.constant.CommonConstant;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

public class FeignRequestInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String userId = request.getHeader(CommonConstant.HEADER_USER_ID);
            String username = request.getHeader(CommonConstant.HEADER_USERNAME);
            String token = request.getHeader(CommonConstant.HEADER_TOKEN);
            if (userId != null) {
                template.header(CommonConstant.HEADER_USER_ID, userId);
            }
            if (username != null) {
                template.header(CommonConstant.HEADER_USERNAME, username);
            }
            if (token != null) {
                template.header(CommonConstant.HEADER_TOKEN, token);
            }
        }
    }
}
