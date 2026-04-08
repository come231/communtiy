---
name: admin-login-cors-fix
overview: 修复管理员登录 CORS 问题 - SecurityConfig 未集成 CorsConfig 导致 OPTIONS 预检失败
todos:
  - id: fix-corsconfig
    content: 修复 CorsConfig.java，使用 addAllowedOriginPattern 替代 setAllowedOrigins
    status: pending
  - id: fix-securityconfig
    content: 修复 SecurityConfig.java，添加 .cors() 配置启用跨域
    status: pending
    dependencies:
      - fix-corsconfig
  - id: restart-admin-service
    content: 重启 community-admin 服务使配置生效
    status: pending
    dependencies:
      - fix-securityconfig
  - id: test-admin-login
    content: 测试管理员登录功能是否正常
    status: pending
    dependencies:
      - restart-admin-service
  - id: verify-admin-features
    content: 验证管理员后台各功能页面正常运行
    status: pending
    dependencies:
      - test-admin-login
---

## 问题描述

管理员登录时出现 CORS 跨域错误：`Access to XMLHttpRequest at 'http://localhost:8099/login' from origin 'http://localhost:3000' has been blocked by CORS policy: No 'Access-Control-Allow-Origin' header is present`

## 根本原因

1. 前端请求 `/api/admin/login` 通过代理转发到网关 8095，再路由到后端 8099
2. 8099 服务的 `CorsConfig.java` 定义了 CorsFilter Bean，但 `SecurityConfig.java` 未启用
3. OPTIONS 预检请求被 Spring Security 拦截，未返回 CORS 响应头
4. `CorsConfig` 使用 `setAllowedOrigins()` 与 `allowCredentials(true)` 不兼容

## 修复方案

### 方案：修复 SecurityConfig 和 CorsConfig

1. **CorsConfig.java**：使用 `addAllowedOriginPatterns("*")` 替代 `setAllowedOrigins()`
2. **SecurityConfig.java**：添加 `.cors()` 配置启用跨域支持

## 功能验证范围

- 管理员登录功能（/admin/login）
- 用户管理（列表、封禁/解封）
- 帖子管理（列表、审核、删除）
- 举报管理
- 敏感词管理
- 系统配置
- 数据仪表盘

## 技术栈

- Spring Cloud Gateway (8095) - 网关路由
- Spring Security (8099) - 管理员服务认证
- Vue3 + Element Plus (3000) - 前端
- JWT 双令牌认证机制

## 修复方案

### 问题定位

请求流程：`localhost:3000/api/admin/login` → 代理到 `8095` → 路由到 `8099/admin/login`
OPTIONS 预检请求未通过 CORS 验证

### 修改点

1. **CorsConfig.java**：

- `setAllowedOrigins()` 与 `allowCredentials(true)` 不兼容
- 改为 `addAllowedOriginPatterns("*")` 或 `addAllowedOriginPattern("*")`

2. **SecurityConfig.java**：

- 在 `.csrf(AbstractHttpConfigurer::disable)` 后添加 `.cors(Customizer.withDefaults())`
- 或添加 `.addFilterBefore(corsFilter(), UsernamePasswordAuthenticationFilter.class)`

### 依赖注入

SecurityConfig 需要注入 CorsFilter Bean