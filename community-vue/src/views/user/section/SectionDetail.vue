<template>
  <div class="min-h-screen bg-[#F5F7FA] pt-20 pb-8">
    <div class="max-w-6xl mx-auto px-4">
      <!-- 板块头部信息 -->
      <div v-if="!loading && section" class="bg-white rounded-xl shadow-sm border border-gray-100 p-6 mb-6">
        <div class="flex items-start gap-5">
          <div class="w-16 h-16 rounded-2xl bg-gradient-to-br flex items-center justify-center text-2xl shadow-inner text-white"
            :style="{ background: getGradient(section.sectionId || section.id) }">
            {{ section.icon || (section.name || '').charAt(0) }}
          </div>
          <div class="flex-1">
            <h1 class="text-xl font-bold text-gray-800 mb-1">{{ section.name }}</h1>
            <p class="text-sm text-gray-500 mb-3">{{ section.intro || '暂无描述' }}</p>
            <div class="flex items-center gap-4 text-xs text-gray-400">
              <span class="flex items-center gap-1">
                <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 20H5a2 2 0 01-2-2V6a2 2 0 012-2h10a2 2 0 012 2v1m2 13a2 2 0 01-2-2V7m2 13a2 2 0 002-2V9a2 2 0 00-2-2h-2m-4-3H9M7 16h6M7 8h6v4H7V8z"/></svg>
                {{ section.postCount || 0 }} 帖子
              </span>
              <span v-if="moderator" class="flex items-center gap-1">
                <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"/></svg>
                版主: {{ moderator.nickname || moderator.username }}
              </span>
            </div>
          </div>
          <button @click="showCreateDialog = true" class="px-4 py-2 bg-primary text-white text-sm rounded-lg hover:bg-blue-600 transition-colors flex-shrink-0">
            创建板块
          </button>
        </div>
      </div>

      <!-- 帖子列表 -->
      <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-5">
        <div class="flex items-center justify-between mb-4">
          <h2 class="font-semibold text-gray-800">板块帖子</h2>
          <span class="text-sm text-gray-400">共 {{ total }} 条</span>
        </div>
        <div v-loading="postsLoading" class="space-y-3">
          <div v-for="post in posts" :key="post.id"
            @click="$router.push(`/post/${post.id}`)"
            class="flex items-center gap-4 p-3 rounded-lg hover:bg-gray-50 cursor-pointer transition-colors group">
            <div class="flex-1 min-w-0">
              <h3 class="text-sm font-medium text-gray-800 group-hover:text-primary transition-colors truncate">{{ post.title }}</h3>
              <div class="flex items-center gap-3 text-xs text-gray-400 mt-1">
                <span>{{ post.authorName || '匿名' }}</span>
                <span>{{ formatTime(post.createTime) }}</span>
              </div>
            </div>
            <div class="flex items-center gap-4 text-xs text-gray-400 flex-shrink-0">
              <span class="flex items-center gap-1">
                <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"/></svg>
                {{ post.viewCount || 0 }}
              </span>
              <span class="flex items-center gap-1">
                <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z"/></svg>
                {{ post.likeCount || 0 }}
              </span>
              <span class="flex items-center gap-1">
                <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"/></svg>
                {{ post.commentCount || 0 }}
              </span>
            </div>
          </div>
          <div v-if="!postsLoading && posts.length === 0" class="text-center py-12 text-gray-400">
            <p class="text-sm">该板块暂无帖子</p>
          </div>
        </div>
        <!-- 分页 -->
        <div v-if="totalPages > 1" class="flex items-center justify-center gap-2 mt-6 pt-4 border-t border-gray-100">
          <button @click="currentPage > 1 && (currentPage--, loadPosts())" :disabled="currentPage <= 1" class="px-3 py-1.5 text-sm border border-gray-200 rounded-lg disabled:opacity-40 hover:bg-gray-50">上一页</button>
          <span class="text-sm text-gray-600">{{ currentPage }} / {{ totalPages }}</span>
          <button @click="currentPage < totalPages && (currentPage++, loadPosts())" :disabled="currentPage >= totalPages" class="px-3 py-1.5 text-sm border border-gray-200 rounded-lg disabled:opacity-40 hover:bg-gray-50">下一页</button>
        </div>
      </div>

      <!-- 创建板块对话框 -->
      <div v-if="showCreateDialog" class="fixed inset-0 bg-black/40 z-50 flex items-center justify-center" @click.self="showCreateDialog = false">
        <div class="bg-white rounded-2xl shadow-xl w-full max-w-md p-6">
          <h3 class="text-lg font-semibold text-gray-800 mb-4">创建新板块</h3>
          <div class="space-y-4">
            <div>
              <label class="block text-sm text-gray-600 mb-1">板块名称</label>
              <input v-model="newSection.name" type="text" placeholder="输入板块名称" class="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm outline-none focus:border-primary" />
            </div>
            <div>
              <label class="block text-sm text-gray-600 mb-1">板块描述</label>
              <textarea v-model="newSection.description" placeholder="输入板块描述" rows="3" class="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm outline-none focus:border-primary resize-none"></textarea>
            </div>
          </div>
          <div class="flex justify-end gap-3 mt-6">
            <button @click="showCreateDialog = false" class="px-4 py-2 text-sm text-gray-600 border border-gray-200 rounded-lg hover:bg-gray-50">取消</button>
            <button @click="createSection" :disabled="creating" class="px-4 py-2 bg-primary text-white text-sm rounded-lg hover:bg-blue-600 disabled:opacity-50">创建</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { sectionApi, postApi, userApi } from '../../../api'
import { ElMessage } from 'element-plus'

const route = useRoute()
const loading = ref(true)
const postsLoading = ref(false)
const section = ref<any>(null)
const moderator = ref<any>(null)
const posts = ref<any[]>([])
const total = ref(0)
const totalPages = ref(1)
const currentPage = ref(1)

const showCreateDialog = ref(false)
const creating = ref(false)
const newSection = ref({ name: '', description: '' })

const gradients = [
  'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
  'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
  'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
  'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
  'linear-gradient(135deg, #fa709a 0%, #fee140 100%)',
]

function getGradient(id: number): string {
  return gradients[(id || 0) % gradients.length]
}

async function loadSection() {
  const id = Number(route.params.id)
  try {
    const res: any = await sectionApi.detail(id)
    section.value = res
    if (res.moderatorId) {
      try {
        moderator.value = await userApi.info(res.moderatorId)
      } catch { /* ignore */ }
    }
  } catch (e) {
    ElMessage.error('板块不存在')
  } finally {
    loading.value = false
  }
}

async function loadPosts() {
  postsLoading.value = true
  const id = Number(route.params.id)
  try {
    const res: any = await postApi.list({ sectionId: id, pageNum: currentPage.value, pageSize: 10 })
    const list = res.records || res || []
    posts.value = list.map((p: any) => ({ ...p, id: p.postId }))
    total.value = res.total || list.length
    totalPages.value = Math.ceil(total.value / 10)
  } catch (e) {
    console.error(e)
    posts.value = []
  } finally {
    postsLoading.value = false
  }
}

async function createSection() {
  if (!newSection.value.name.trim()) {
    ElMessage.warning('请输入板块名称')
    return
  }
  creating.value = true
  try {
    await sectionApi.create({ name: newSection.value.name, intro: newSection.value.description })
    ElMessage.success('板块创建成功')
    showCreateDialog.value = false
    newSection.value = { name: '', description: '' }
  } catch (e: any) {
    ElMessage.error('创建失败')
  } finally {
    creating.value = false
  }
}

function formatTime(time: string): string {
  if (!time) return '-'
  const now = new Date().getTime()
  const t = new Date(time).getTime()
  const diff = now - t
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  if (diff < 604800000) return Math.floor(diff / 86400000) + '天前'
  return time.split(' ')[0]
}

onMounted(() => {
  loadSection()
  loadPosts()
})
</script>
