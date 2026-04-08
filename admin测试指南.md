# Admin后台接口测试指南

## 测试前准备

1. **启动基础服务**：
   - Nacos (8848)
   - MySQL (3306)
   - Redis (6379)
   - RabbitMQ (5672)
   - Elasticsearch (9200)

2. **创建数据库**：
   执行 `database/community_admin.sql` 和 `database/community_security.sql`

3. **编译并启动Admin服务**：
   ```bash
   cd c:\Users\lenovo\Desktop\community
   java -jar community-admin/target/community-admin-1.0-SNAPSHOT.jar
   ```

## 接口测试（使用curl）

### 1. 管理员登录
```bash
curl -X POST http://localhost:8099/admin/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}'
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

### 2. 获取仪表盘统计数据
```bash
curl -X GET http://localhost:8099/dashboard/stats \
  -H "Authorization: Bearer <your-access-token>"
```

### 3. 获取用户列表
```bash
curl -X GET "http://localhost:8099/admin/users?page=1&size=10" \
  -H "Authorization: Bearer <your-access-token>"
```

### 4. 获取帖子列表
```bash
curl -X GET "http://localhost:8099/admin/posts?page=1&size=10&keyword=测试" \
  -H "Authorization: Bearer <your-access-token>"
```

### 5. 获取敏感词列表
```bash
curl -X GET "http://localhost:8099/admin/sensitive-words?page=1&size=10" \
  -H "Authorization: Bearer <your-access-token>"
```

### 6. 批量导入敏感词
```bash
curl -X POST http://localhost:8099/admin/sensitive-words/import \
  -H "Authorization: Bearer <your-access-token>" \
  -H "Content-Type: application/json" \
  -d '{"words":["测试词1","测试词2","测试词3"]}'
```

### 7. 获取系统配置
```bash
curl -X GET http://localhost:8099/admin/configs \
  -H "Authorization: Bearer <your-access-token>"
```

### 8. 获取操作日志
```bash
curl -X GET "http://localhost:8099/admin/logs?page=1&size=10" \
  -H "Authorization: Bearer <your-access-token>"
```

### 9. 获取举报列表
```bash
curl -X GET "http://localhost:8099/admin/reports-manage?page=1&size=10" \
  -H "Authorization: Bearer <your-access-token>"
```

## 完整功能验证清单

### ✅ 认证模块
- [ ] 登录接口返回正确Token
- [ ] Token可以访问受保护接口
- [ ] Token过期后刷新接口可用
- [ ] 退出登录后Token失效

### ✅ 仪表盘模块
- [ ] 统计数据接口正常返回
- [ ] 用户趋势数据格式正确
- [ ] 帖子趋势数据格式正确
- [ ] 最新举报列表正常

### ✅ 用户管理模块
- [ ] 用户列表分页正常
- [ ] 用户详情信息完整
- [ ] 封禁用户功能正常
- [ ] 解封用户功能正常

### ✅ 帖子管理模块
- [ ] 帖子列表分页正常
- [ ] 帖子详情包含举报次数
- [ ] 审核帖子功能正常
- [ ] 置顶/取消置顶功能正常
- [ ] 精华/取消精华功能正常
- [ ] 删除帖子功能正常

### ✅ 举报管理模块
- [ ] 举报列表分页正常
- [ ] 处理举报功能正常
- [ ] 举报统计数量正确

### ✅ 敏感词管理模块
- [ ] 敏感词列表分页正常
- [ ] 添加敏感词功能正常
- [ ] 更新敏感词功能正常
- [ ] 删除敏感词功能正常
- [ ] 批量导入功能正常

### ✅ 系统配置模块
- [ ] 配置列表正常返回
- [ ] 更新配置功能正常
- [ ] 根据Key获取配置正常

### ✅ 操作日志模块
- [ ] 日志列表分页正常
- [ ] 按模块筛选功能正常
- [ ] 按时间范围筛选功能正常
- [ ] 模块列表正常返回

## 常见问题

### 1. Token无效或过期
**解决**：重新调用登录接口获取新Token

### 2. 数据库连接失败
**解决**：检查MySQL是否启动，数据库是否创建

### 3. 接口返回401
**解决**：确认请求头包含 `Authorization: Bearer <token>`

### 4. 接口返回500
**解决**：查看服务日志，检查SQL语句是否正确

## 测试账号

**管理员账号**：
- 用户名：admin
- 密码：123456
- 昵称：超级管理员

**版主账号**：
- 用户名：moderator
- 密码：123456
- 昵称：版主

## 日志查看

Admin服务日志路径：
```
logs/admin.log
```

查看实时日志：
```bash
tail -f logs/admin.log
```

## Swagger文档

启动服务后访问：
```
http://localhost:8099/swagger-ui.html
```

## 总结

Admin后台管理模块共有 **31个接口**，覆盖完整后台管理功能：

- **认证模块**：4个接口
- **仪表盘**：4个接口
- **用户管理**：4个接口
- **帖子管理**：6个接口
- **举报管理**：3个接口
- **敏感词管理**：5个接口
- **系统配置**：3个接口
- **操作日志**：2个接口

**所有接口已实现完整功能，可直接使用。**
