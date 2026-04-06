<template>
  <div class="space-y-6">
    <!-- Tab切换 -->
    <div class="bg-white rounded-xl border border-gray-100 shadow-sm">
      <div class="border-b border-gray-100">
        <div class="flex">
          <button
            v-for="tab in tabs" :key="tab.key"
            :class="['px-6 py-3.5 text-sm font-medium border-b-2 transition-colors', activeTab === tab.key ? 'text-primary border-primary' : 'text-gray-500 border-transparent hover:text-gray-700']"
            @click="activeTab = tab.key"
          >{{ tab.label }}</button>
        </div>
      </div>

      <!-- 搜索栏 -->
      <div class="p-5 border-b border-gray-100">
        <div class="flex flex-wrap items-center gap-3">
          <input v-model="keyword" type="text" placeholder="搜索标题/作者..." class="px-4 py-2 bg-gray-50 border border-gray-200 rounded-lg text-sm outline-none focus:border-primary w-64" />
          <select v-model="statusFilter" class="px-3 py-2 bg-gray-50 border border-gray-200 rounded-lg text-sm outline-none">
            <option value="">全部状态</option>
            <option value="published">已发布</option>
            <option value="pending">审核中</option>
            <option value="rejected">已拒绝</option>
            <option value="deleted">已删除</option>
          </select>
          <button class="px-4 py-2 bg-primary text-white text-sm rounded-lg hover:bg-blue-600 transition-colors">搜索</button>
          <button class="px-4 py-2 border border-gray-200 text-gray-600 text-sm rounded-lg hover:bg-gray-50">重置</button>
        </div>
      </div>

      <!-- 列表 -->
      <div class="overflow-x-auto">
        <table class="w-full text-sm">
          <thead class="bg-gray-50 border-b text-left text-xs text-gray-500 uppercase tracking-wider">
            <tr><th class="px-5 py-3.5">帖子信息</th><th class="px-5 py-3.5">作者</th><th class="px-5 py-3.5">分类</th><th class="px-5 py-3.5">互动数据</th><th class="px-5 py-3.5">状态</th><th class="px-5 py-3.5">操作</th></tr>
          </thead>
          <tbody class="divide-y divide-gray-50">
            <tr v-for="post in posts" :key="post.id" class="hover:bg-gray-50/50 transition-colors">
              <td class="px-5 py-4">
                <div class="max-w-[280px]">
                  <p class="font-medium text-gray-800 line-clamp-1">{{ post.title }}</p>
                  <p class="text-xs text-gray-400 mt-1 line-clamp-1">{{ post.summary }}</p>
                </div>
              </td>
              <td class="px-5 py-4">
                <div class="flex items-center gap-2">
                  <img :src="post.authorAvatar" class="w-8 h-8 rounded-full object-cover" alt="" />
                  <span class="text-gray-600 text-xs">{{ post.authorName }}</span>
                </div>
              </td>
              <td class="px-5 py-4">
                <span class="text-xs bg-gray-100 text-gray-600 px-2 py-1 rounded">{{ post.section }}</span>
              </td>
              <td class="px-5 py-4">
                <div class="flex gap-3 text-xs text-gray-500">
                  <span>{{ post.views }} 浏览</span>
                  <span>{{ post.likes }} 赞</span>
                  <span>{{ post.comments }} 评论</span>
                </div>
              </td>
              <td class="px-5 py-4">
                <span :class="['text-xs px-2.5 py-1 rounded-full font-medium', getStatusStyle(post.status)]">{{ getStatusLabel(post.status) }}</span>
              </td>
              <td class="px-5 py-4">
                <div class="flex items-center gap-2">
                  <button class="text-primary hover:text-blue-700 text-xs font-medium" @click="viewPost(post)">查看</button>
                  <button v-if="post.status === 'pending'" class="text-green-500 hover:text-green-700 text-xs font-medium">通过</button>
                  <button v-if="post.status === 'pending'" class="text-orange-500 hover:text-orange-700 text-xs font-medium">拒绝</button>
                  <button class="text-red-500 hover:text-red-700 text-xs font-medium">删除</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- 分页 -->
      <div class="flex items-center justify-between px-5 py-4 border-t border-gray-100">
        <span class="text-sm text-gray-500">共 {{ totalPosts }} 条</span>
        <div class="flex items-center gap-2">
          <button :disabled="page <= 1" class="px-3 py-1.5 text-sm border border-gray-200 rounded-lg disabled:opacity-40">上一页</button>
          <span class="text-sm text-gray-600">{{ page }} / {{ totalPages }}</span>
          <button :disabled="page >= totalPages" class="px-3 py-1.5 text-sm border border-gray-200 rounded-lg disabled:opacity-40">下一页</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const tabs = [{ key: 'posts', label: '帖子管理' }, { key: 'comments', label: '评论管理' }]
const activeTab = ref('posts')
const keyword = ref('')
const statusFilter = ref('')
const page = ref(1)
const totalPosts = ref(256)
const totalPages = ref(26)

const posts = [
  { id: 1, title: 'Spring Boot 3 新特性完全指南：从迁移到最佳实践', summary: '详细讲解 Spring Boot 3 的核心新特性，包括虚拟线程、GraalVM支持...', authorName: '张三', authorAvatar: 'https://placehold.co/32x32/409EFF/fff?text=Z', section: '后端开发', views: 12560, likes: 856, comments: 123, status: 'published' },
  { id: 2, title: 'Vue 3.5 组合式API深度解析与实战技巧', summary: '全面介绍 Vue 3.5 组合式API的使用方法和设计模式...', authorName: '李四', authorAvatar: 'https://placehold.co/32x32/67C23A/fff?text=L', section: '前端开发', views: 8930, likes: 624, comments: 89, status: 'pending' },
  { id: 3, title: '微服务架构设计模式：从单体到分布式演进', summary: '分享微服务架构的核心设计模式和实际落地经验...', authorName: '王五', authorAvatar: 'https://placehold.co/32x32/E6A23C/fff?text=W', section: '架构设计', views: 6780, likes: 445, comments: 67, status: 'published' },
  { id: 4, title: 'Docker容器化部署完整教程（含K8s）', summary: '从零开始学习 Docker 和 Kubernetes 的生产环境部署...', authorName: '赵六', authorAvatar: 'https://placehold.co/32x32/F56C6C/fff?text=Z', section: 'DevOps', views: 15420, likes: 1023, comments: 156, status: 'rejected' },
  { id: 5, title: 'TypeScript 高级类型编程实战手册', summary: '深入理解 TypeScript 类型系统，掌握高级类型编程...', authorName: '孙七', authorAvatar: 'https://placehold.co/32x32/9B59B6/fff?text=S', section: '前端开发', views: 9870, likes: 721, comments: 94, status: 'published' },
]

function getStatusStyle(status: string): string {
  const s: Record<string, string> = { published: 'bg-green-50 text-green-600', pending: 'bg-yellow-50 text-yellow-600', rejected: 'bg-red-50 text-red-600', deleted: 'bg-gray-100 text-gray-500' }
  return s[status] || 'bg-gray-100'
}
function getStatusLabel(status: string): string {
  const l: Record<string, string> = { published: '已发布', pending: '审核中', rejected: '已拒绝', deleted: '已删除' }
  return l[status] || '未知'
}
function viewPost(post: any) {}
</script>
