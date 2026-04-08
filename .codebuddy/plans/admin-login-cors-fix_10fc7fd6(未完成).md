---
name: admin-login-cors-fix
overview: 修复管理员登录CORS问题，确保请求正确通过网关路由，并实现管理员后台各项功能
todos:
  - id: fix-cors-config
    content: 修复community-admin CorsConfig.java，使用addAllowedOriginPatterns替代setAllowedOrigins
    status: pending
  - id: fix-security-cors
    content: 修复SecurityConfig.java，添加.cors()配置启用跨域支持
    status: pending
    dependencies:
      - fix-cors-config
  - id: verify-gateway-config
    content: 确认community-gateway application.yml路由配置正确
    status: pending
  - id: verify-proxy-config
    content: 确认vite.config.ts代理配置正确，请求走/api路径
    status: pending
  - id: test-admin-login
    content: 测试管理员登录功能
    status: pending
    dependencies:
      - fix-cors-config
      - fix-security-cors
      - verify-gateway-config
      - verify-proxy-config
  - id: verify-admin-features
    content: 验证管理员后台各功能页面正常
    status: pending
    dependencies:
      - test-admin-login
---

## 问题描述

管理员登录时出现CORS跨域错误：`Access to XMLHttpRequest at 'http://localhost:8099/login' (redirected from 'http://localhost:3000/api/admin/login') from origin 'http://localhost:3000' has been blocked by CORS policy: Response to preflight request doesn't pass access control check: No 'Access-Control-Allow-Origin' header is present`

## 根本原因

1. 请求绕过了网关（8095）直接访问了后端服务（8099）
2. 管理员服务CorsConfig使用`setAllowedOrigins()`与`allowCredentials(true)`不兼容（这是Spring Security的已知限制）
3. SecurityConfig未配置CORS处理器

## 修复范围

1. 修复管理员服务CORS配置
2. 修复SecurityConfig添加CORS支持
3. 确认网关和前端代理配置正确
4. 完善管理员后台各项功能（用户管理、帖子管理、举报管理、敏感词管理、系统配置、数据仪表盘）

## 技术架构

### 问题分析

请求流程：前端(3000) → 网关(8095) → 后端(8099)
错误显示请求直接访问了8099，跳过了网关

### 修复方案

1. **community-admin CorsConfig.java**: 使用`addAllowedOriginPatterns("*")`替代`setAllowedOrigins()`，解决与credentials不兼容问题
2. **community-admin SecurityConfig.java**: 添加`.cors(Customizer.withDefaults())`启用CORS
3. **确认配置正确性**: 网关8095和前端代理配置正确

### 关键配置

- 网关8095：CORS已配置`allowed-origin-patterns: "http://localhost:*"`
- 前端代理：/api → http://localhost:8095，rewrite去掉/api
- 后端8099：需要配置允许跨域