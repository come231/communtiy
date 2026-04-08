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
          <input v-model="keyword" type="text" placeholder="搜索标题/作者..." class="px-4 py-2 bg-gray-50 border border-gray-200 rounded-lg text-sm outline-none focus:border-primary w-64" @keyup.enter="handleSearch" />
          <select v-model="statusFilter" class="px-3 py-2 bg-gray-50 border border-gray-200 rounded-lg text-sm outline-none">
            <option :value="undefined">全部状态</option>
            <option :value="0">待审核</option>
            <option :value="1">已通过</option>
            <option :value="2">已拒绝</option>
          </select>
          <button @click="handleSearch" class="px-4 py-2 bg-primary text-white text-sm rounded-lg hover:bg-blue-600 transition-colors">搜索</button>
          <button @click="handleReset" class="px-4 py-2 border border-gray-200 text-gray-600 text-sm rounded-lg hover:bg-gray-50">重置</button>
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
                  <p class="text-xs text-gray-400 mt-1 line-clamp-1">{{ post.summary || post.content?.substring(0, 50) }}</p>
                </div>
              </td>
              <td class="px-5 py-4">
                <div class="flex items-center gap-2">
                  <img :src="post.authorAvatar || defaultAvatar" class="w-8 h-8 rounded-full object-cover bg-gray-100" alt="" />
                  <span class="text-gray-600 text-xs">{{ post.authorName||post.nickname || '未知' }}</span>
                </div>
              </td>
              <td class="px-5 py-4">
                <span class="text-xs bg-gray-100 text-gray-600 px-2 py-1 rounded">{{ post.sectionName || post.section || '-' }}</span>
              </td>
              <td class="px-5 py-4">
                <div class="flex gap-3 text-xs text-gray-500">
                  <span>{{ post.viewCount || post.views || 0 }} 浏览</span>
                  <span>{{ post.likeCount || post.likes || 0 }} 赞</span>
                  <span>{{ post.commentCount || post.comments || 0 }} 评论</span>
                </div>
              </td>
              <td class="px-5 py-4">
                <span :class="['text-xs px-2.5 py-1 rounded-full font-medium', getStatusStyle(post.status)]">{{ getStatusLabel(post.status) }}</span>
              </td>
              <td class="px-5 py-4">
                <div class="flex items-center gap-2">
                  <button class="text-primary hover:text-blue-700 text-xs font-medium" @click="viewPost(post)">查看</button>
                  <button v-if="post.status === 0" @click="handleApprove(post)" class="text-green-500 hover:text-green-700 text-xs font-medium">通过</button>
                  <button v-if="post.status === 0" @click="handleReject(post)" class="text-orange-500 hover:text-orange-700 text-xs font-medium">拒绝</button>
                  <button @click="handleDelete(post)" class="text-red-500 hover:text-red-700 text-xs font-medium">删除</button>
                </div>
              </td>
            </tr>
            <tr v-if="posts.length === 0">
              <td colspan="6" class="px-5 py-8 text-center text-gray-400">暂无数据</td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- 分页 -->
      <div class="flex items-center justify-between px-5 py-4 border-t border-gray-100">
        <span class="text-sm text-gray-500">共 {{ total }} 条</span>
        <div class="flex items-center gap-2">
          <button @click="handlePageChange(currentPage - 1)" :disabled="currentPage <= 1" class="px-3 py-1.5 text-sm border border-gray-200 rounded-lg disabled:opacity-40 hover:bg-gray-50">上一页</button>
          <span class="text-sm text-gray-600">{{ currentPage }} / {{ totalPages || 1 }}</span>
          <button @click="handlePageChange(currentPage + 1)" :disabled="currentPage >= totalPages" class="px-3 py-1.5 text-sm border border-gray-200 rounded-lg disabled:opacity-40 hover:bg-gray-50">下一页</button>
        </div>
      </div>
    </div>

    <!-- 帖子详情弹窗 -->
    <el-dialog v-model="detailVisible" title="帖子详情" width="800px">
      <div v-if="currentPost" class="space-y-4">
        <h3 class="text-lg font-medium">{{ currentPost.title }}</h3>
        <div class="flex items-center gap-4 text-sm text-gray-500">
          <span>作者: {{ currentPost.authorName || currentPost.nickname || '未知' }}</span>
          <span>分类: {{ currentPost.sectionName || currentPost.section || '-' }}</span>
          <span>时间: {{ formatTime(currentPost.createTime) }}</span>
        </div>
        <div class="bg-gray-50 rounded-lg p-4 text-sm whitespace-pre-wrap">{{ currentPost.content }}</div>
        <div class="flex gap-4 text-sm text-gray-500">
          <span>{{ currentPost.viewCount || 0 }} 浏览</span>
          <span>{{ currentPost.likeCount || 0 }} 赞</span>
          <span>{{ currentPost.commentCount || 0 }} 评论</span>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi } from '@/api'

const defaultAvatar = 'https://placehold.co/32x32/409EFF/fff?text=U'

interface Post {
  id: number
  title: string
  content?: string
  summary?: string
  authorId?: number
  authorName?: string
  authorAvatar?: string
  nickname?: string
  section?: string
  sectionName?: string
  viewCount?: number
  likeCount?: number
  commentCount?: number
  views?: number
  likes?: number
  comments?: number
  status: number
  createTime?: string
}

const tabs = [{ key: 'posts', label: '帖子管理' }]
const activeTab = ref('posts')
const keyword = ref('')
const statusFilter = ref<number | undefined>(undefined)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const totalPages = ref(1)
const posts = ref<Post[]>([])
const detailVisible = ref(false)
const currentPost = ref<Post | null>(null)

async function loadPosts() {
  try {
    const res: any = await adminApi.posts({
      keyword: keyword.value || undefined,
      status: statusFilter.value,
      page: currentPage.value,
      size: pageSize.value
    })
    
    if (res?.records) {
      posts.value = res.records
      total.value = res.total || 0
      totalPages.value = Math.ceil((res.total || 0) / pageSize.value)
    } else if (Array.isArray(res)) {
      posts.value = res
      total.value = res.length
      totalPages.value = 1
    }
  } catch (e: any) {
    ElMessage.error('加载帖子列表失败')
    console.error(e)
  }
}

function handleSearch() {
  currentPage.value = 1
  loadPosts()
}

function handleReset() {
  keyword.value = ''
  statusFilter.value = undefined
  currentPage.value = 1
  loadPosts()
}

function handlePageChange(page: number) {
  currentPage.value = page
  loadPosts()
}

function getStatusStyle(status: number): string {
  const s: Record<number, string> = {
    0: 'bg-yellow-50 text-yellow-600',
    1: 'bg-green-50 text-green-600',
    2: 'bg-red-50 text-red-600'
  }
  return s[status] || 'bg-gray-100'
}

function getStatusLabel(status: number): string {
  const l: Record<number, string> = {
    0: '待审核',
    1: '已通过',
    2: '已拒绝'
  }
  return l[status] || '未知'
}

function formatTime(dateStr?: string): string {
  if (!dateStr) return '-'
  const d = new Date(dateStr)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

function viewPost(post: Post) {
  currentPost.value = post
  detailVisible.value = true
}

async function handleApprove(post: Post) {
  try {
    await ElMessageBox.confirm(`确定要通过帖子 "${post.title}" 吗？`, '确认通过', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    await adminApi.reviewPost(post.id, 1, '审核通过')
    ElMessage.success('审核通过')
    loadPosts()
  } catch (e: any) {
    if (e !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

async function handleReject(post: Post) {
  try {
    const { value: reason } = await ElMessageBox.prompt('请输入拒绝原因', '审核拒绝', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPlaceholder: '请输入拒绝原因'
    })
    await adminApi.reviewPost(post.id, 2, reason)
    ElMessage.success('已拒绝')
    loadPosts()
  } catch (e: any) {
    if (e !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

async function handleDelete(post: Post) {
  try {
    await ElMessageBox.confirm(`确定要删除帖子 "${post.title}" 吗？此操作不可恢复！`, '确认删除', {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await adminApi.deletePost(post.id)
    ElMessage.success('删除成功')
    loadPosts()
  } catch (e: any) {
    if (e !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  loadPosts()
})
</script>
