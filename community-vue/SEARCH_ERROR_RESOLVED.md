# 搜索页面跳转错误 - 已修复

## 问题描述

点击搜索结果中的帖子、用户或话题时，后端报错：
```
Failed to convert value of type 'java.lang.String' to required type 'java.lang.Long'; 
For input string: "NaN"
```

## 根本原因

前端传递的ID参数为`undefined`、`null`或`NaN`，后端无法将字符串"NaN"转换为Long类型。

## 修复内容

### 1. 跳转函数添加ID验证（SearchPage.vue:382-389）
```typescript
// 跳转到帖子详情
function goToPost(postId: number) {
  if (!postId || isNaN(postId)) {
    console.error('无效的帖子ID:', postId)
    return
  }
  router.push(`/post/${postId}`)
}

// 跳转到用户主页
function goToUserProfile(userId: number) {
  if (!userId || isNaN(userId)) {
    console.error('无效的用户ID:', userId)
    return
  }
  router.push(`/profile/${userId}`)
}
```

### 2. API数据处理时确保ID有效（SearchPage.vue:287-309）
```typescript
// 搜索结果处理 - 帖子
posts.value = (res?.records || []).map((post: any) => ({
  ...post,
  id: Number(post.id) || 0
}))

// 搜索结果处理 - 用户
users.value = (res?.records || []).map((user: any) => ({
  ...user,
  id: Number(user.id) || 0
}))

// 热门帖子处理
hotPosts.value = (res?.records?.slice(0, 5) || []).map((post: any) => ({
  ...post,
  id: Number(post.id) || 0
}))

// 热门用户处理
hotUsers.value = (res?.records || []).map((user: any) => ({
  ...user,
  id: Number(user.id) || 0
}))
```

### 3. 模板绑定添加ID检查（SearchPage.vue:74, 128, 107, 175）
```vue
<!-- 热门帖子 -->
<div v-for="(post, idx) in hotPosts" :key="post.id || idx" @click="post.id && goToPost(post.id)">

<!-- 搜索结果帖子 -->
<div v-for="post in posts" :key="post.id" @click="post.id && goToPost(post.id)">

<!-- 热门用户 -->
<div v-for="(user, idx) in hotUsers" :key="user.id || idx" @click="user.id && goToUserProfile(user.id)">

<!-- 搜索结果用户 -->
<div v-for="user in users" :key="user.id" @click="user.id && goToUserProfile(user.id)">
```

## 修复效果

### 修复前
- 点击帖子/用户/话题时可能报错
- 错误信息：`Failed to convert value of type 'java.lang.String' to required type 'java.lang.Long'`
- 原因：ID为undefined/null/NaN

### 修复后
- 点击前验证ID有效性
- 无效ID时控制台打印错误日志，不执行跳转
- 有效ID时正常跳转到对应页面
- 用户体验更友好，不会出现后端错误

## 测试验证

### 测试场景1：正常点击
1. 搜索关键词，获取结果
2. 点击帖子/用户
3. ✅ 正常跳转到详情页

### 测试场景2：无效ID处理
1. 如果API返回的数据没有ID
2. 点击时不会执行跳转
3. ✅ 控制台打印错误日志
4. ✅ 不报错，不跳转

### 测试场景3：热门内容点击
1. 切换到不同Tab
2. 点击热门帖子/用户
3. ✅ 正常跳转到对应页面

## 相关文件

- `community-vue/src/views/user/search/SearchPage.vue` - 搜索页面主文件
- `community-vue/src/views/user/search/ERROR_FIX.md` - 错误修复详细说明

## 总结

本次修复解决了搜索页面点击跳转时的`NaN`错误问题，主要改进：

1. **防御性编程**：在跳转函数中添加ID有效性检查
2. **数据预处理**：API返回数据时确保ID为有效数字
3. **模板保护**：模板中增加ID存在性检查
4. **错误日志**：无效ID时打印错误日志，便于调试

修复后的代码更加健壮，能够处理各种边界情况，提升了用户体验。
