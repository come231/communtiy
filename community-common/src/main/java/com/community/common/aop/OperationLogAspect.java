package com.community.common.aop;

import com.community.common.constant.CommonConstant;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Pointcut("@annotation(com.community.common.annotation.OperationLog)")
    public void logPointcut() {}

    @Around("logPointcut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        long startTime = System.currentTimeMillis();
        MethodSignature signature = (MethodSignature) point.getSignature();
        com.community.common.annotation.OperationLog annotation =
                signature.getMethod().getAnnotation(com.community.common.annotation.OperationLog.class);

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes != null ? attributes.getRequest() : null;

        Map<String, Object> logData = new HashMap<>();
        if (request != null) {
            logData.put("userId", request.getHeader(CommonConstant.HEADER_USER_ID));
            logData.put("ip", request.getRemoteAddr());
            logData.put("method", request.getMethod());
            logData.put("path", request.getRequestURI());
            logData.put("userAgent", request.getHeader("User-Agent"));
        }
        logData.put("module", annotation.module());
        logData.put("operation", annotation.operation());

        try {
            Object result = point.proceed();
            logData.put("status", 1);
            log.info("操作日志: module={}, operation={}, cost={}ms",
                    annotation.module(), annotation.operation(),
                    System.currentTimeMillis() - startTime);
            return result;
        } catch (Throwable e) {
            logData.put("status", 0);
            logData.put("error", e.getMessage());
            throw e;
        } finally {
            logData.put("costTime", System.currentTimeMillis() - startTime);
            try {
                rabbitTemplate.convertAndSend("community.exchange", "community.operation.log", objectMapper.writeValueAsString(logData));
            } catch (Exception e) {
                log.error("发送操作日志失败", e);
            }
        }
    }
}
