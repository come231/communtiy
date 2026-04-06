# 社区交流平台

基于 Spring Cloud Alibaba + Vue 3 + 微信小程序 + Spring AI 的微服务社区平台

## 技术架构

### 后端技术栈
- **微服务框架**: Spring Cloud Alibaba 2021.0.5.0
- **服务注册与配置中心**: Nacos
- **API网关**: Spring Cloud Gateway
- **服务调用**: OpenFeign
- **持久层**: MyBatis-Plus
- **数据库**: MySQL 8.0
- **缓存**: Redis
- **消息队列**: RabbitMQ
- **搜索引擎**: Elasticsearch 7.17
- **AI框架**: Spring AI 1.0.0 (OpenAI)
- **分布式锁**: Redisson

### 前端技术栈
- **Web前端**: Vue 3 + Vite + Element Plus
- **状态管理**: Pinia
- **路由**: Vue Router 4
- **HTTP客户端**: Axios
- **小程序**: 原生微信小程序框架

## 项目结构

```
community/
├── community-gateway/          # 网关服务 (端口: 8095)
├── community-user/             # 用户服务 (端口: 8096)
├── community-community/        # 社区服务 (帖子、评论等) (端口: 8091)
├── community-ai/               # AI智能服务 (端口: 8092)
├── community-search/           # 搜索服务 (端口: 8094)
├── community-interact/         # 互动服务 (点赞、收藏等) (端口: 8093)
├── community-common/           # 公共模块
├── community-file/             # 文件服务 (端口: 8097)
├── community-message/          # 消息服务 (端口: 8098)
├── community-admin/            # 管理后台服务 (端口: 8099)
├── community-vue/              # Vue前端项目 (端口: 3000)
├── community-wx/               # 微信小程序
├── database/                   # 数据库SQL文件
└── pom.xml                     # 父项目POM
```

## 环境准备

### 必需服务
1. **MySQL 8.0+**: 数据库服务
2. **Redis**: 缓存服务
3. **RabbitMQ**: 消息队列
4. **Elasticsearch 7.17**: 搜索引擎
5. **Nacos**: 服务注册与配置中心

### 数据库配置
执行 `database/` 目录下的所有SQL文件创建数据库：
- community_user.sql: 用户数据库
- community_content.sql: 内容数据库（帖子、评论等）
- community_ai.sql: AI相关数据
- community_interact.sql: 互动数据
- community_search.sql: 搜索相关数据
- community_message.sql: 消息数据
- community_file.sql: 文件数据
- community_security.sql: 安全相关数据
- commubity_admin管理员相关信息

所有数据库默认用户名密码：root/root

### AI配置
在 `community-ai/src/main/resources/application.yml` 中配置OpenAI API:
```yaml
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY:sk-your-api-key}
      base-url: ${OPENAI_BASE_URL:https://api.openai.com}
```

## 快速开始

### 后端服务启动

1. **启动Nacos**
```bash
# 下载并启动Nacos
# http://localhost:8848/nacos
# 默认用户名密码: nacos/nacos
```

2. **编译项目**
```bash
cd community_study
mvn clean install
```

3. **启动微服务**
按以下顺序启动各个微服务：

```bash
# 启动网关服务
cd community-gateway
mvn spring-boot:run

# 启动用户服务
cd community-user
mvn spring-boot:run

# 启动社区服务
cd community-community
mvn spring-boot:run

# 启动AI服务
cd community-ai
mvn spring-boot:run

# 启动搜索服务 (可选)
cd community-search
mvn spring-boot:run

# 启动互动服务
cd community-interact
mvn spring-boot:run
```

### 前端启动

#### Vue前端
```bash
cd community-vue
npm install
npm run dev
```
访问: http://localhost:3000

#### 微信小程序
使用微信开发者工具打开 `community-wx` 目录

## 功能特性

### 用户系统
- JWT双令牌认证 (AccessToken + RefreshToken)
- 用户注册、登录、退出
- 个人信息管理
- 关注/粉丝系统

### 帖子系统
- 发布、编辑、删除帖子
- 富文本编辑器支持Markdown
- 图片上传
- 帖子分类和标签
- AI自动生成摘要

### 评论系统
- 多级评论回复
- 评论点赞
- 评论举报

### 互动系统
- 帖子点赞、收藏
- 关注用户
- 关注话题

### AI智能助手
- AI对话聊天 (基于Spring AI)
- AI写作助手
- 文章润色
- 内容总结
- 违规内容检测

### 搜索功能
- Elasticsearch全文搜索
- 帖子、用户、话题搜索
- 搜索历史记录
- 热门搜索推荐

### 消息通知
- 系统通知
- 评论回复通知
- 点赞收藏通知
- 关注通知

## 核心特性

### 1. Spring Cloud微服务架构
- 基于Nacos实现服务注册与发现
- 通过OpenFeign完成服务间远程调用
- Spring Cloud Gateway统一路由和认证

### 2. JWT双令牌认证体系
- AccessToken (1小时) + RefreshToken (7天)
- 网关层JWT过滤器 + 业务层拦截器双重认证
- Redis Token黑名单实现主动失效

### 3. Redis分布式锁
- Redisson分布式锁防止重复点赞
- Redis缓存实时计数
- RabbitMQ异步落库

### 4. Elasticsearch全文搜索
- IK分词器支持
- 多字段加权检索
- 搜索高亮显示

### 5. Spring AI集成
- OpenAI GPT-3.5-turbo模型
- 多会话管理
- Redis缓存对话历史
- AI写作、润色、总结、检测

### 6. 异步消息队列
- RabbitMQ解耦异步任务
- AI任务异步处理
- 内容审核异步处理
- 搜索热词同步

## API文档

启动服务后访问Swagger文档：
- 网关服务: http://localhost:8095/swagger-ui.html
- 用户服务: http://localhost:8096/swagger-ui.html
- 社区服务: http://localhost:8091/swagger-ui.html
- AI服务: http://localhost:8092/swagger-ui.html

## 测试账号

管理员账号：
- 用户名: admin
- 密码: admin123

普通用户：
- 用户名: user001
- 密码: 123456

## 项目截图

### Vue前端首页
![Vue首页](docs/images/vue-home.png)

### AI智能助手
![AI助手](docs/images/ai-assistant.png)

### 帖子详情页
![帖子详情](docs/images/post-detail.png)

### 微信小程序
![小程序首页](docs/images/wx-home.png)

## 开发规范

### 代码规范
- 遵循阿里巴巴Java开发规范
- 使用Lombok简化实体类
- 统一使用Result作为返回结果

### Git提交规范
- feat: 新功能
- fix: 修复bug
- docs: 文档更新
- style: 代码格式调整
- refactor: 重构
- test: 测试相关
- chore: 构建或辅助工具

## 部署说明

### Docker部署（推荐）

1. **构建镜像**
```bash
# 构建所有服务镜像
mvn clean package -DskipTests
docker-compose build
```

2. **启动服务**
```bash
docker-compose up -d
```

3. **查看日志**
```bash
docker-compose logs -f
```

### 生产环境配置

1. 修改各服务的 `application.yml` 中的数据库、Redis等配置
2. 配置Nacos生产环境
3. 配置Elasticsearch生产环境
4. 配置RabbitMQ生产环境
5. 使用Nginx反向代理前端静态资源

## 监控与运维

### 健康检查
各服务提供 `/actuator/health` 端点用于健康检查

### 日志管理
- 使用SLF4J + Logback
- 日志级别：INFO、DEBUG、ERROR
- 日志路径：logs/

## 常见问题

### 1. Nacos连接失败
确保Nacos服务已启动，并检查bootstrap.yml中的配置

### 2. Redis连接失败
检查Redis服务是否启动，以及application.yml中的配置

### 3. AI服务调用失败
检查OpenAI API Key是否配置正确

### 4. 前端跨域问题
检查网关服务的CORS配置

## 贡献指南

欢迎提交Issue和Pull Request！

## 许可证

MIT License

## 联系方式

- 项目地址: https://github.com/yourusername/community-platform
- 问题反馈: https://github.com/yourusername/community-platform/issues
- 邮箱: contact@community.com

---

## 更新日志

### v1.0.0 (2026-04-01)
- 初始版本发布
- 实现用户、帖子、评论核心功能
- 集成Spring AI智能助手
- 支持Vue 3前端和微信小程序
- 实现Elasticsearch全文搜索
- 集成RabbitMQ异步处理

Swagger访问地址： 各服务启动后访问：

用户服务: http://localhost:8096/swagger-ui.html
社区服务: http://localhost:8091/swagger-ui.html
互动服务: http://localhost:8093/swagger-ui.html
AI服务: http://localhost:8092/swagger-ui.html
消息服务: http://localhost:8094/swagger-ui.html
文件服务: http://localhost:8095/swagger-ui.html
搜索服务: http://localhost:8097/swagger-ui.html
管理后台: http://localhost:8099/swagger-ui.html