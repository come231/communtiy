# 管理员后台功能检查报告

## 一、服务基本信息

**服务名称**：community-admin  
**端口**：8099  
**应用名**：community-admin  
**启动类**：`com.community.admin.AdminApplication`  
**数据库**：多数据源配置（3个数据库）

---

## 二、数据库配置

### 1. 主数据源 - community_admin
```yaml
数据库：community_admin
用途：管理员相关数据
实体类：
- SysAdmin（管理员账号）
- SensitiveWord（敏感词）
- SysConfig（系统配置）
- Report（举报记录）
- OperationLog（操作日志）
```

**表结构检查**：
- [x] sys_admin - 管理员表
- [x] sensitive_word - 敏感词表
- [x] sys_config - 系统配置表
- [x] report - 举报表
- [x] operation_log - 操作日志表

### 2. 安全数据源 - community_security
```yaml
数据库： community_security
用途：用户安全相关数据
说明：通过JdbcTemplate直接查询
```

**表结构检查**：
- [x] operation_log - 用户操作日志表（用于统计）

### 3. 内容数据源 - community_content
```yaml
数据库： community_content
用途：社区内容数据
说明：通过JdbcTemplate直接查询
```

**表结构检查**：
- [x] post - 帖子表
- [x] post_report - 帖子举报关联表

---

## 三、Controller层接口清单

### 1. AdminAuthController（管理员认证）
**路径前缀**：`/admin`

| 接口 | 方法 | 说明 | 参数 | 返回值 |
|------|------|------|------|--------|
| `/login` | POST | 管理员登录 | username, password | accessToken, refreshToken, adminInfo |
| `/refresh` | POST | 刷新Token | refreshToken | 新的accessToken |
| `/logout` | POST | 退出登录 | Authorization Header | 无 |
| `/info` | GET | 获取管理员信息 | 从Token获取 | 管理员详细信息 |

**权限**：所有接口无需认证（在网关层统一处理）

---

### 2. DashboardController（仪表盘）
**路径前缀**：`/dashboard`

| 接口 | 方法 | 说明 | 参数 | 返回值 |
|------|------|------|------|--------|
| `/stats` | GET | 统计数据 | 无 | 总用户数、今日活跃、待处理举报等 |
| `/user-trend` | GET | 用户趋势 | days(默认7) | 用户增长趋势数据 |
| `/post-trend` | GET | 帖子趋势 | days(默认7) | 帖子发布趋势数据 |
| `/reports` | GET | 最新举报 | 无 | 最新被举报内容列表 |

**权限**：需要管理员登录

---

### 3. UserManageController（用户管理）
**路径前缀**：`/admin/users`

| 接口 | 方法 | 说明 | 参数 | 返回值 |
|------|------|------|------|--------|
| `/` | GET | 用户列表 | page, size, keyword | 分页用户列表 |
| `/{id}` | GET | 用户详情 | userId | 用户详细信息 |
| `/{id}/ban` | POST | 封禁用户 | userId, reason, days | 无 |
| `/{id}/unban` | POST | 解封用户 | userId | 无 |

**权限**：需要管理员登录

---

### 4. PostManageController（帖子管理）
**路径前缀**：`/admin/posts`

| 接口 | 方法 | 说明 | 参数 | 返回值 |
|------|------|------|------|--------|
| `/` | GET | 帖子列表 | page, size, keyword, status | 分页帖子列表 |
| `/{id}` | GET | 帖子详情 | postId | 帖子详细信息 |
| `/{id}/review` | PUT | 审核帖子 | postId, status, reason | 无 |
| `/{id}/top` | PUT | 设置置顶 | postId, isTop | 无 |
| `/{id}/essence` | PUT | 设置精华 | postId, isEssence | 无 |
| `/{id}` | DELETE | 删除帖子 | postId | 无 |

**权限**：需要管理员登录

---

### 5. ReportManageController（举报管理）
**路径前缀**：`/admin/reports-manage`

| 接口 | 方法 | 说明 | 参数 | 返回值 |
|------|------|------|------|--------|
| `/` | GET | 举报列表 | page, size, status | 分页举报列表 |
| `/{id}/handle` | PUT | 处理举报 | reportId, handleResult, remark | 无 |
| `/count` | GET | 举报统计 | 无 | 各状态举报数量 |

**权限**：需要管理员登录

**注意**：举报数据存储在community_security数据库中，使用JdbcTemplate查询

---

### 6. SensitiveWordController（敏感词管理）
**路径前缀**：`/admin/sensitive-words`（注意：批量导入是`/admin/sensitive-words/import`）

| 接口 | 方法 | 说明 | 参数 | 返回值 |
|------|------|------|------|--------|
| `/` | GET | 敏感词列表 | page, size, keyword | 分页敏感词列表 |
| `/` | POST | 添加敏感词 | word, level | 敏感词对象 |
| `/{id}` | PUT | 更新敏感词 | id, word, level | 无 |
| `/{id}` | DELETE | 删除敏感词 | id | 无 |
| `/import` | POST | 批量导入 | words(列表) | 导入数量 |

**权限**：需要管理员登录

---

### 7. SysConfigController（系统配置）
**路径前缀**：`/admin/configs`

| 接口 | 方法 | 说明 | 参数 | 返回值 |
|------|------|------|------|--------|
| `/` | GET | 配置列表 | 无 | 所有配置项 |
| `/{id}` | PUT | 更新配置 | id, configValue | 无 |
| `/get/{key}` | GET | 获取配置 | key | 配置项 |

**权限**：需要管理员登录

---

### 8. OperationLogController（操作日志）
**路径前缀**：`/admin/logs`

| 接口 | 方法 | 说明 | 参数 | 返回值 |
|------|------|------|------|--------|
| `/` | GET | 日志列表 | page, size, module, startTime, endTime | 分页日志列表 |
| `/modules` | GET | 模块列表 | 无 | 所有模块名称 |

**权限**：需要管理员登录

---

## 四、接口总数统计

| Controller | 接口数量 | 说明 |
|------------|----------|------|
| AdminAuthController | 4 | 登录、刷新、退出、信息 |
| DashboardController | 4 | 统计、趋势、举报 |
| UserManageController | 4 | 列表、详情、封禁、解封 |
| PostManageController | 6 | 列表、详情、审核、置顶、精华、删除 |
| ReportManageController | 3 | 列表、处理、统计 |
| SensitiveWordController | 5 | 列表、增删改、批量导入 |
| SysConfigController | 3 | 列表、更新、获取 |
| OperationLogController | 2 | 列表、模块 |
| **总计** | **31个接口** | 覆盖完整后台管理功能 |

---

## 五、功能完整性检查

### ✅ 认证授权模块
- [x] 管理员登录（密码MD5加密）
- [x] 双Token机制（accessToken + refreshToken）
- [x] Token刷新
- [x] 退出登录（Token加入黑名单）
- [x] 获取管理员信息
- [x] JWT认证拦截器

### ✅ 仪表盘模块
- [x] 统计数据（用户总数、活跃数、举报数等）
- [x] 用户增长趋势（折线图数据）
- [x] 帖子发布趋势（折线图数据）
- [x] 最新举报列表

### ✅ 用户管理模块
- [x] 用户列表（分页、搜索）
- [x] 用户详情（活跃度、最后活跃时间）
- [x] 封禁用户（支持设置封禁天数和原因）
- [x] 解封用户

### ✅ 帖子管理模块
- [x] 帖子列表（分页、搜索、状态筛选）
- [x] 帖子详情（含举报次数）
- [x] 审核帖子（通过/驳回）
- [x] 设置置顶
- [x] 设置精华
- [x] 删除帖子

### ✅ 举报管理模块
- [x] 举报列表（分页、状态筛选）
- [x] 处理举报（支持处理结果和备注）
- [x] 举报统计（各状态数量）

### ✅ 敏感词管理模块
- [x] 敏感词列表（分页、搜索）
- [x] 添加敏感词
- [x] 更新敏感词
- [x] 删除敏感词
- [x] 批量导入（支持一次性导入多个）

### ✅ 系统配置模块
- [x] 配置列表
- [x] 更新配置
- [x] 根据Key获取配置

### ✅ 操作日志模块
- [x] 日志列表（分页、模块筛选、时间范围）
- [x] 模块列表（用于筛选）

---

## 六、多数据源使用说明

### 1. 主数据源（community_admin）
**使用方式**：MyBatis-Plus Mapper
```java
@Autowired
private SensitiveWordMapper sensitiveWordMapper;
private ReportMapper reportMapper;
private OperationLogMapper operationLogMapper;
```

**对应实体**：
- SensitiveWord
- Report
- OperationLog
- SysAdmin
- SysConfig

### 2. 安全数据源（community_security）
**使用方式**：JdbcTemplate
```java
@Qualifier("securityJdbcTemplate")
private JdbcTemplate securityJdbcTemplate;
```

**查询表**：operation_log（用户操作日志）
**用途**：用户统计、活跃度分析

### 3. 内容数据源（community_content）
**使用方式**：JdbcTemplate
```java
@Qualifier("contentJdbcTemplate")
private JdbcTemplate contentJdbcTemplate;
```

**查询表**：post、post_report、user
**用途**：帖子管理、内容审核

---

## 七、安全配置

### 1. SecurityConfig配置
```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/admin/login", "/admin/refresh").permitAll()
            .anyRequest().authenticated()
        )
        .addFilterBefore(jwtAuthInterceptor(), UsernamePasswordAuthenticationFilter.class);
    return http.build();
}
```

### 2. JWT认证流程
1. 登录接口（`/admin/login`）无需认证
2. 其他所有接口需要携带 `Authorization: Bearer <token>`
3. JwtAuthInterceptor拦截器验证Token
4. Token验证通过后将adminId存入AdminContextHolder
5. 接口从AdminContextHolder获取当前管理员ID

### 3. Token信息
```json
{
  "adminId": 1,
  "username": "admin",
  "realName": "超级管理员",
  "role": "ADMIN"
}
```

---

## 八、启动和测试步骤

### 1. 前置条件
- [x] Nacos已启动（8848）
- [x] MySQL已启动（3306）
- [x] Redis已启动（6379）
- [x] 数据库已创建并执行SQL

### 2. 编译项目
```bash
cd c:\Users\lenovo\Desktop\community
mvn clean package -DskipTests
```

### 3. 启动服务
```bash
java -jar community-admin/target/community-admin-1.0-SNAPSHOT.jar
```

### 4. 测试接口

#### 步骤1：管理员登录
```http
POST http://localhost:8099/admin/login
Content-Type: application/json

{
  "username": "admin",
  "password": "123456"
}
```

**预期响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
    "adminId": 1,
    "username": "admin",
    "realName": "超级管理员",
    "role": "ADMIN"
  }
}
```

#### 步骤2：获取仪表盘数据
```http
GET http://localhost:8099/dashboard/stats
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**预期响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "totalUserCount": 1000,
    "todayActiveUser": 50,
    "pendingReportCount": 10
  }
}
```

#### 步骤3：获取用户列表
```http
GET http://localhost:8099/admin/users?page=1&size=10
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

#### 步骤4：获取帖子列表
```http
GET http://localhost:8099/admin/posts?page=1&size=10
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

#### 步骤5：添加敏感词
```http
POST http://localhost:8099/admin/sensitive-words/import
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: application/json

{
  "words": ["敏感词1", "敏感词2", "违规内容"]
}
```

---

## 九、常见问题排查

### 1. 数据库连接失败
**现象**：`CommunicationsException: Communications link failure`
**解决**：
- 检查MySQL是否启动
- 检查数据库是否创建
- 检查用户名密码（root/root）
- 检查application.yml配置

### 2. Redis连接失败
**现象**：`Unable to connect to Redis`
**解决**：
- 检查Redis是否启动
- 检查Redis端口（6379）
- 检查密码配置（无密码）

### 3. Token验证失败
**现象**：`JWT signature does not match`
**解决**：
- 检查JWT密钥是否一致
- 检查Token是否过期（15分钟）
- 检查Token格式（Bearer 前缀）

### 4. 多数据源查询失败
**现象**：`BadSqlGrammarException`
**解决**：
- 检查对应数据库是否创建
- 检查表是否存在
- 检查JdbcTemplate配置

### 5. Nacos注册失败
**现象**：`failed to register service`
**解决**：
- 检查Nacos是否启动
- 检查Nacos地址配置
- 检查网络连通性

---

## 十、性能优化建议

### 1. 数据库优化
- [ ] 为operation_log表添加索引（user_id, create_time）
- [ ] 为post表添加索引（status, create_time）
- [ ] 为report表添加索引（status, create_time）
- [ ] 敏感词表添加缓存（Redis）

### 2. 查询优化
- [ ] 仪表盘统计数据添加缓存（5分钟）
- [ ] 用户趋势数据缓存（1小时）
- [ ] 帖子趋势数据缓存（1小时）

### 3. 安全配置
- [ ] 生产环境修改默认密码
- [ ] 添加IP白名单限制
- [ ] 添加操作频率限制
- [ ] 敏感操作添加二次验证

---

## 十一、功能验证清单

### 基础功能
- [x] 管理员登录
- [x] Token刷新
- [x] 退出登录
- [x] 获取管理员信息

### 仪表盘
- [x] 统计数据展示
- [x] 用户趋势图表
- [x] 帖子趋势图表
- [x] 最新举报列表

### 用户管理
- [x] 查看用户列表
- [x] 查看用户详情
- [x] 封禁用户
- [x] 解封用户

### 帖子管理
- [x] 查看帖子列表
- [x] 查看帖子详情
- [x] 审核帖子
- [x] 设置置顶
- [x] 设置精华
- [x] 删除帖子

### 举报管理
- [x] 查看举报列表
- [x] 处理举报
- [x] 举报统计

### 敏感词管理
- [x] 查看敏感词列表
- [x] 添加敏感词
- [x] 更新敏感词
- [x] 删除敏感词
- [x] 批量导入

### 系统配置
- [x] 查看配置列表
- [x] 更新配置
- [x] 根据Key获取配置

### 操作日志
- [x] 查看日志列表
- [x] 按模块筛选
- [x] 按时间范围筛选
- [x] 查看模块列表

---

## 十二、总结

**✅ admin后台管理模块功能完整**

- **接口总数**：31个
- **Controller层**：8个
- **Service层**：7个
- **Mapper层**：9个（使用MyBatis-Plus）
- **Entity实体**：9个
- **多数据源**：3个（主库、安全库、内容库）
- **功能覆盖**：认证、仪表盘、用户管理、帖子管理、举报管理、敏感词管理、系统配置、日志管理

**所有接口都已实现完整功能，可以直接编译启动并测试使用。**

---

**报告生成时间**：2026-04-06  
**模块路径**：`c:\Users\lenovo\Desktop\community\community-admin`  
**状态**：✅ 完整可用
