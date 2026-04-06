# TechCommunity 微信小程序

## 项目简介

TechCommunity 微信小程序客户端，基于原生微信小程序框架开发，提供社区交流平台的移动端体验。

## 功能特性

### 用户端功能
- **首页浏览**：推荐/热门/最新帖子列表，支持下拉刷新和触底加载
- **帖子详情**：查看帖子内容、评论、点赞、收藏、分享
- **发布帖子**：支持标题、内容、图片上传、话题标签、匿名发布
- **消息通知**：通知列表、私信会话、系统通知
- **个人中心**：用户信息、关注/粉丝、签到、收藏、设置
- **搜索功能**：帖子/用户/话题搜索，热门搜索和历史记录
- **AI助手**：智能对话、AI写作、文章润色、内容总结、违规检测

## 技术栈

- **框架**：原生微信小程序框架
- **开发语言**：TypeScript
- **样式**：WXSS (微信样式表)
- **网络请求**：wx.request 封装
- **状态管理**：小程序 App 实例 globalData

## 项目结构

```
miniprogram/
├── api/                 # API 接口封装
│   └── index.ts
├── assets/              # 静态资源
│   ├── icons/           # 图标
│   └── tabbar/          # TabBar 图标
├── components/          # 公共组件
├── pages/               # 页面
│   ├── index/           # 首页
│   ├── post/            # 帖子详情
│   ├── publish/         # 发布帖子
│   ├── profile/         # 个人中心
│   ├── message/         # 消息通知
│   ├── search/          # 搜索页
│   ├── ai/              # AI助手
│   ├── login/           # 登录页
│   └── user/            # 用户主页
├── styles/              # 全局样式
│   └── common.wxss
├── utils/               # 工具函数
│   ├── request.ts       # 请求封装
│   └── util.ts          # 通用工具
├── app.js               # 应用入口
├── app.json             # 应用配置
└── app.wxss             # 全局样式
```

## 快速开始

### 1. 安装依赖

本项目为原生小程序，无需安装 npm 依赖。

### 2. 配置服务器域名

在微信公众平台 -> 开发管理 -> 开发设置 中配置以下服务器域名：

- request 合法域名：`http://localhost:8095`（开发环境）
- uploadFile 合法域名：`http://localhost:8095`（开发环境）

### 3. 启动开发工具

1. 下载并安装 [微信开发者工具](https://developers.weixin.qq.com/miniprogram/dev/devtools/download.html)
2. 打开微信开发者工具
3. 导入本项目（选择 `community-wx` 目录）
4. 选择"导入"即可

### 4. 后端服务

确保后端服务已启动：
- 网关服务（端口：8095）
- 用户服务（端口：8096）
- 社区服务（端口：8091）
- 消息服务（端口：8098）
- AI服务（端口：8092）
- 搜索服务（端口：8094）

## API 接口

所有接口通过网关统一路由，前端请求示例：

```typescript
// GET 请求
wx.request({
  url: 'http://localhost:8095/user/login',
  method: 'GET',
  data: { username: 'user001', password: '123456' }
})

// POST 请求
wx.request({
  url: 'http://localhost:8095/community/posts',
  method: 'POST',
  data: { title: '标题', content: '内容' },
  header: {
    'Authorization': 'Bearer ' + token,
    'Content-Type': 'application/json'
  }
})
```

## 页面路由

| 页面 | 路径 | 说明 |
|------|------|------|
| 首页 | /pages/index/index | 帖子列表 |
| 帖子详情 | /pages/post/post | 查看帖子 |
| 发布帖子 | /pages/publish/publish | 发布内容 |
| 个人中心 | /pages/profile/profile | 用户信息 |
| 消息 | /pages/message/message | 通知私信 |
| 搜索 | /pages/search/search | 搜索功能 |
| AI助手 | /pages/ai/ai | 智能对话 |
| 登录 | /pages/login/login | 用户登录 |
| 用户主页 | /pages/user/user | 查看用户 |

## 测试账号

- 用户名：user001
- 密码：123456

## 开发规范

### 文件命名
- 页面文件：使用 kebab-case（如 `user-profile`）
- 组件文件：使用 PascalCase（如 `UserCard`）
- 工具文件：使用 camelCase（如 `request.ts`）

### 代码规范
- 使用 TypeScript 进行类型检查
- 合理使用 `any` 类型，避免类型丢失
- 异步操作使用 `async/await` 语法
- 统一使用 ES6+ 语法

### 样式规范
- 使用 rpx 单位实现响应式布局
- 颜色值统一使用十六进制格式
- 避免使用内联样式，优先使用 WXSS 类名

## 注意事项

1. **域名配置**：生产环境需配置 HTTPS 域名
2. **用户权限**：部分功能需要登录后才能使用
3. **网络请求**：建议添加错误处理和重试机制
4. **性能优化**：长列表建议使用虚拟列表优化

## 更新日志

### v1.0.0 (2026-04-04)
- 初始版本发布
- 实现首页、帖子详情、发布功能
- 实现消息通知、个人中心
- 实现搜索、AI助手功能

## 许可证

MIT License
