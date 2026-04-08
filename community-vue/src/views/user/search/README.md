# 搜索功能完善说明

## 新增功能

### 1. 搜索高亮显示
- 搜索结果中的匹配关键词会被高亮显示（黄色背景）
- 支持帖子标题、内容摘要、用户昵称和个人简介的高亮
- 使用 `<mark>` 标签实现，样式可自定义

### 2. 删除单个搜索历史
- 鼠标悬停在历史记录上时显示删除按钮（×）
- 点击删除按钮可移除单个历史记录
- 操作流畅，用户体验友好

### 3. 清空所有历史记录
- 搜索历史标题右侧提供"清空"按钮
- 一键清除所有搜索历史
- 确认操作简单直接

### 4. 热门搜索实时更新
- 热门搜索标题右侧提供"刷新"按钮
- 点击可实时获取最新的热门搜索关键词
- 显示搜索次数（如果API返回）

### 5. Tab相关内容展示
根据当前选中的Tab，在未搜索状态下展示对应的热门内容：

#### 帖子Tab
- 显示热门帖子TOP5
- 包含帖子标题、点赞数、浏览数
- 点击可直接跳转到帖子详情

#### 话题Tab
- 显示热门话题（最多8个）
- 显示话题名称和帖子数量
- 点击话题可直接搜索相关帖子

#### 用户Tab
- 显示热门用户（简化版）
- 显示用户头像、昵称、粉丝数和帖子数
- 点击可跳转到用户主页

### 6. 搜索结果优化
- 帖子搜索结果：显示标题（高亮）、内容摘要（高亮）、作者、时间和互动数据
- 话题搜索结果：显示话题名称（高亮）、描述（高亮）和帖子数量
- 用户搜索结果：显示头像、昵称（高亮）、简介（高亮）和关注数据

### 7. 点击跳转功能
- **帖子**：点击搜索结果中的帖子卡片 → 跳转到帖子详情页 `/post/:id`
- **用户**：点击搜索结果中的用户卡片 → 跳转到用户主页 `/user/:id`（或 `/profile/:id`）
- **话题**：点击热门话题 → 自动搜索该话题相关帖子

## 技术实现

### 高亮显示
```typescript
function highlightText(text: string): string {
  if (!keyword.value.trim()) return text
  const regex = new RegExp(`(${keyword.value.replace(/[.*+?^${}()|[\\]\\]/g, '\\$&')})`, 'gi')
  return text.replace(regex, '<mark class="bg-yellow-200 text-gray-800 rounded px-1">$1</mark>')
}
```

### 历史记录管理
```typescript
// 删除单个历史记录
function removeHistory(index: number) {
  const list = [...history.value]
  list.splice(index, 1)
  localStorage.setItem('search_history', JSON.stringify(list))
}

// 清空所有历史记录
function clearAllHistory() {
  localStorage.removeItem('search_history')
}
```

### 热门内容加载
```typescript
// 监听Tab切换，加载对应的热门内容
watch(activeTab, (newTab) => {
  if (!searched.value) {
    if (newTab === 'post') fetchHotPosts()
    else if (newTab === 'topic') fetchHotTopics()
    else if (newTab === 'user') fetchHotUsers()
  }
})
```

## API接口

### 已有接口
- `searchApi.hot()` - 获取热门搜索关键词
- `postApi.hot()` - 获取热门帖子
- `topicApi.hot()` - 获取热门话题
- `postApi.list()` - 搜索帖子
- `userApi.search()` - 搜索用户

### 建议新增接口（后端）
- `GET /search/topics` - 话题搜索接口
- `GET /user/hot` - 获取热门用户接口

## 使用说明

### 搜索操作
1. 在搜索框输入关键词
2. 按Enter键或点击"搜索"按钮
3. 系统自动保存搜索历史（最多20条）
4. 搜索结果中高亮显示匹配内容

### 历史记录管理
1. **快速搜索**：点击历史记录直接搜索
2. **删除单条**：悬停后点击×按钮
3. **清空全部**：点击"清空"按钮

### 热门内容浏览
1. **刷新热门搜索**：点击热门搜索标题旁的刷新图标
2. **浏览热门帖子**：切换到帖子Tab，查看下方热门帖子
3. **浏览热门话题**：切换到话题Tab，查看下方热门话题
4. **浏览热门用户**：切换到用户Tab，查看下方热门用户

### 跳转操作
1. **帖子**：点击帖子卡片任意位置
2. **用户**：点击用户卡片任意位置
3. **话题**：点击热门话题按钮

## 样式说明

### 高亮样式
```css
mark {
  background-color: #fef08a;  /* 黄色背景 */
  color: #1f2937;             /* 深灰色文字 */
  padding: 0 2px;
  border-radius: 2px;
}
```

### 交互效果
- 悬停效果：卡片阴影加深、文字变蓝
- 按钮效果：透明度变化、颜色过渡
- 删除按钮：默认隐藏，悬停时显示

## 性能优化

1. **防抖处理**：输入关键词时未做防抖，后续可优化
2. **缓存机制**：热门内容可添加缓存，减少重复请求
3. **分页加载**：搜索结果支持分页，避免一次性加载过多数据
4. **懒加载**：图片使用懒加载，提升页面加载速度

## 兼容性

- 支持现代浏览器（Chrome、Firefox、Safari、Edge）
- 移动端适配良好
- 响应式设计，自适应不同屏幕尺寸
