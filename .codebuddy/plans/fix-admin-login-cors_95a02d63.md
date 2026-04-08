---
name: fix-admin-login-cors
overview: 修复管理员登录CORS问题：SecurityConfig未集成CORS导致预检请求被Spring Security拦截，以及一系列管理员后台功能的完善。
todos:
  - id: fix-cors-config
    content: 修复 CorsConfig.java，将 setAllowedOrigins 改为 setAllowedOriginPatterns
    status: completed
  - id: fix-security-config
    content: 修复 SecurityConfig.java，添加 .cors(Customizer.withDefaults()) 启用 CORS
    status: completed
    dependencies:
      - fix-cors-config
  - id: connect-user-manage
    content: UserManage.vue 接入 adminApi 真实数据，实现搜索/封禁/解封功能
    status: completed
    dependencies:
      - fix-security-config
  - id: connect-post-manage
    content: PostManage.vue 接入 adminApi 真实数据，实现审核/删除功能
    status: completed
    dependencies:
      - fix-security-config
  - id: connect-report-manage
    content: ReportManage.vue 接入 adminApi 真实数据，实现举报处理功能
    status: completed
    dependencies:
      - fix-security-config
  - id: connect-sensitive-config
    content: SensitiveWordManage.vue 和 SystemConfig.vue 接入真实 API
    status: completed
    dependencies:
      - fix-security-config
---

## 产品概述

修复社区管理后台管理员登录时的 CORS 跨域问题，使管理员可以正常登录并使用后台各项管理功能。

## 核心问题

CORS 预检请求(OPTIONS)在 admin 服务(8099)处被 Spring Security 拦截。虽然 `CorsConfig` 注册了 `CorsFilter` Bean，但 `SecurityConfig` 未通过 `.cors()` 启用 CORS 支持，导致 Spring Security 过滤链优先拦截 OPTIONS 请求。

## 核心功能

- 修复 CORS 配置：在 SecurityConfig 中启用 CORS，确保预检请求通过
- 修复 CorsConfig：`setAllowedOrigins()` 与 `setAllowCredentials(true)` 不兼容时使用 `setAllowedOriginPatterns()`
- 将管理后台前端页面的硬编码数据替换为真实 API 调用（用户管理、帖子管理、举报管理、敏感词管理、系统配置）

## 技术栈

- 后端：Spring Boot + Spring Security + Spring Cloud Gateway
- 前端：Vue 3 + TypeScript + Tailwind CSS + Element Plus
- 数据库：MySQL + Redis

## 实现方案

### 根因分析

请求链路：前端(3000) `/api/admin/login` → vite proxy(去/api) → 网关(8095)/admin/login → admin服务(8099)/admin/login

CORS 问题出在 admin 服务(8099)的 `SecurityConfig` 没有启用 CORS 支持：

1. Spring Security 过滤链在 `CorsFilter` 之前执行，拦截了 OPTIONS 预检请求
2. `CorsConfig` 使用 `setAllowedOrigins()` 配合 `setAllowCredentials(true)`，在 Spring 高版本中可能不兼容

### 修复步骤

**第一步：修复 CorsConfig.java**

- `setAllowedOrigins()` 改为 `setAllowedOriginPatterns()`，解决与 `allowCredentials(true)` 的兼容性问题
- 暴露必要的响应头（Authorization 等）

**第二步：修复 SecurityConfig.java**

- 在 `filterChain` 中添加 `.cors(Customizer.withDefaults())`，让 Spring Security 使用已配置的 `CorsConfiguration`
- 这样 Spring Security 过滤链会正确处理 OPTIONS 预检请求

**第三步：将前端管理页面接入真实 API**
目前 UserManage.vue、PostManage.vue、ReportManage.vue、SensitiveWordManage.vue、SystemConfig.vue 全部使用硬编码数据，需要接入 adminApi。后端 Controller 和 Service 层已就绪。

### 目录结构

```
community-admin/src/main/java/com/community/admin/config/
├── CorsConfig.java        # [MODIFY] 修复 CORS 配置
├── SecurityConfig.java    # [MODIFY] 启用 CORS 支持
└── MvcConfig.java         # 不变

community-vue/src/views/admin/
├── dashboard/DashboardPage.vue       # 已接入API，验证即可
├── user/UserManage.vue               # [MODIFY] 接入真实API
├── content/PostManage.vue            # [MODIFY] 接入真实API
├── report/ReportManage.vue           # [MODIFY] 接入真实API
├── sensitive/SensitiveWordManage.vue # [MODIFY] 接入真实API
└── config/SystemConfig.vue           # [MODIFY] 接入真实API
```

### 实现注意事项

- 修复时保持网关 CORS 配置不变，网关侧配置已正确
- 前端代理配置不变，vite.config.ts 已正确代理到 8095
- request.ts 拦截器不变，已正确区分 admin/user 请求
- 后端 Controller/Service 层已完整实现，无需修改
- 注意 CorsFilter 优先级：确保在 Security 过滤链之前执行

## Agent Extensions

- **SubAgent: code-explorer**
- Purpose: 探索 admin 服务中各 Service 层的实现，确认 API 返回数据结构与前端类型定义匹配
- Expected outcome: 确认后端 API 返回格式，确保前端接入时数据字段正确对应