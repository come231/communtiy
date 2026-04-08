# 管理员服务多数据源配置说明

## 配置结构

管理员服务连接两个数据库：
1. **community_security** (安全数据库) - 主数据源
2. **community_admin** (管理员数据库) - 第二数据源

其他数据库操作通过Feign调用对应服务。

## 数据源配置

### 1. 数据库连接配置 (application.yml)
```yaml
# 主数据源 - 安全库
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/community_security
    username: root
    password: root

  # 管理员数据源 - 管理员库
  admin:
    datasource:
      url: jdbc:mysql://localhost:3306/community_admin
      username: root
      password: root
```

### 2. 数据源配置类 (DataSourceConfig.java)
- **securityDataSource**: 主数据源，连接community_security数据库
- **adminDataSource**: 第二数据源，连接community_admin数据库
- 每个数据源都有对应的JdbcTemplate

### 3. MyBatis配置类 (MyBatisConfig.java)
- **securitySqlSessionFactory**: 使用security数据源
- **adminSqlSessionFactory**: 使用admin数据源

### 4. Mapper扫描配置
- **SecurityMapperConfig**: 扫描`com.community.admin.mapper.security`包，使用securitySqlSessionFactory
- **AdminMapperConfig**: 扫描`com.community.admin.mapper.admin`包，使用adminSqlSessionFactory

## Mapper分类

### Security数据库Mapper (com.community.admin.mapper.security)
- IpBlacklistMapper - IP黑名单
- OperationLogMapper - 操作日志
- SensitiveWordMapper - 敏感词
- RateLimitRecordMapper - 限流记录
- TextReviewMapper - 文本审核记录

### Admin数据库Mapper (com.community.admin.mapper.admin)
- SysAdminMapper - 管理员表
- SysAdminLogMapper - 管理员操作日志
- SysConfigMapper - 系统配置

## 使用示例

### 在Service中使用Mapper

```java
@Service
public class AdminAuthService {
    // 使用admin数据源的Mapper
    private final SysAdminMapper sysAdminMapper;
    
    public AdminAuthService(SysAdminMapper sysAdminMapper) {
        this.sysAdminMapper = sysAdminMapper;
    }
    
    public void someMethod() {
        // 自动使用admin数据源
        SysAdmin admin = sysAdminMapper.selectById(1L);
    }
}
```

```java
@Service
public class SomeSecurityService {
    // 使用security数据源的Mapper
    private final SensitiveWordMapper sensitiveWordMapper;
    
    public SomeSecurityService(SensitiveWordMapper sensitiveWordMapper) {
        this.sensitiveWordMapper = sensitiveWordMapper;
    }
    
    public void someMethod() {
        // 自动使用security数据源
        List<SensitiveWord> words = sensitiveWordMapper.selectList(null);
    }
}
```

## Feign客户端调用其他服务

对于其他数据库的操作，使用Feign客户端调用对应服务：

```java
@Service
@RequiredArgsConstructor
public class DashboardService {
    private final UserFeignClient userFeignClient;      // 操作用户数据
    private final PostFeignClient postFeignClient;      // 操作帖子数据
    private final InteractFeignClient interactFeignClient; // 操作互动数据
    
    public void someMethod() {
        // 通过Feign调用其他服务
        Long userCount = userFeignClient.getTotalUserCount();
        Long postCount = postFeignClient.getTotalPostCount();
    }
}
```

## 配置验证

启动服务后，可以通过以下方式验证配置是否正确：

1. 管理员登录（使用admin数据源）
   - 访问：POST /admin/auth/login
   - 验证：sys_admin表数据是否正确读取

2. 敏感词查询（使用security数据源）
   - 验证：sensitive_word表数据是否正确读取

3. 用户统计（使用Feign）
   - 访问：GET /admin/dashboard/user-stats
   - 验证：通过Feign从user服务获取数据

## 常见问题

### 问题：表不存在错误
**错误信息**：`Table 'community_security.sys_admin' doesn't exist`

**原因**：Mapper被错误地连接到了security数据源，但表在admin数据库中

**解决方法**：
1. 确保Mapper接口在正确的包中（admin或security）
2. 确保对应的MapperConfig扫描正确的包
3. 检查DataSourceConfig中数据源配置是否正确

### 问题：Feign调用失败
**原因**：目标服务未启动或未注册到Nacos

**解决方法**：
1. 确保目标服务已启动
2. 检查Nacos注册中心
3. 检查FeignClient的serviceId是否正确

## 数据库表映射

### community_security数据库
- ip_blacklist - IP黑名单表
- operation_log - 操作日志表
- rate_limit_record - 限流记录表
- report - 举报表
- sensitive_word - 敏感词表
- sys_config - 系统配置表（注意：这个可能需要移到admin库）
- text_review - 文本审核记录表

### community_admin数据库
- sys_admin - 管理员表
- sys_admin_log - 管理员操作日志表

### 其他数据库（通过Feign访问）
- community_user - 用户数据
- community_community - 帖子、评论数据
- community_interact - 点赞、关注数据
- community_message - 消息数据
- community_file - 文件数据
- community_search - 搜索数据
- community_ai - AI相关数据
