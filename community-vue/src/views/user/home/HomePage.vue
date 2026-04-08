<template>
  <div class="max-w-7xl mx-auto px-4 py-6">
    <div class="flex gap-6">
      <!-- 主内容区 - 帖子列表 -->
      <div class="flex-1 min-w-0">
        <!-- Tab 切换 -->
        <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-1 mb-4 inline-flex">
          <button
            v-for="tab in tabs" :key="tab.key"
            @click="activeTab = tab.key"
            :class="[
              'px-5 py-2 rounded-lg text-sm font-medium transition-all',
              activeTab === tab.key ? 'bg-primary text-white shadow-md shadow-primary/25' : 'text-gray-600 hover:text-primary hover:bg-blue-50'
            ]"
          >{{ tab.label }}</button>
        </div>

        <!-- 帖子列表 -->
        <div v-loading="loading" class="space-y-4">
          <div v-for="post in posts" :key="post.id"
            @click="$router.push(`/post/${post.id}`)"
            class="bg-white rounded-xl shadow-sm border border-gray-100 p-5 hover:shadow-md hover:border-primary/20 transition-all cursor-pointer group">
            <div class="flex gap-4">
              <!-- 帖子封面（如果有）-->
              <img v-if="post.coverImage" :src="post.coverImage" class="w-36 h-24 object-cover rounded-lg flex-shrink-0" />
              <div class="flex-1 min-w-0">
                <!-- 标签行 -->
                <div class="flex items-center gap-2 mb-2">
                  <span v-if="post.isTop" class="px-1.5 py-0.5 text-xs bg-red-50 text-red-500 rounded font-medium">置顶</span>
                  <span v-if="post.isEssence" class="px-1.5 py-0.5 text-xs bg-orange-50 text-orange-500 rounded font-medium">精华</span>
                  <span class="px-1.5 py-0.5 text-xs bg-blue-50 text-primary rounded">{{ post.sectionName || '默认' }}</span>
                </div>
                <!-- 标题 -->
                <h3 class="text-base font-semibold text-gray-800 group-hover:text-primary transition-colors line-clamp-2 mb-2">{{ post.title }}</h3>
                <!-- 摘要 -->
                <p v-if="post.summary" class="text-sm text-gray-500 line-clamp-2 mb-3">{{ post.summary }}</p>
                <!-- 底部信息栏 -->
                <div class="flex items-center justify-between text-xs text-gray-400">
                  <div class="flex items-center gap-3">
                    <img :src="post.authorAvatar || defaultAvatar" class="w-5 h-5 rounded-full" />
                    <span>{{ post.authorName }}</span>
                    <span>·</span>
                    <span>{{ formatTime(post.createTime) }}</span>
                  </div>
                  <div class="flex items-center gap-4">
                    <span class="flex items-center gap-1"><svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"/></svg>{{ post.viewCount }}</span>
                    <span class="flex items-center gap-1"><svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z"/></svg>{{ post.likeCount }}</span>
                    <span class="flex items-center gap-1"><svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"/></svg>{{ post.commentCount }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 空状态 -->
          <div v-if="!loading && posts.length === 0" class="text-center py-16 text-gray-400">
            <svg class="w-16 h-16 mx-auto mb-4 text-gray-300" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M19 20H5a2 2 0 01-2-2V6a2 2 0 012-2h10a2 2 0 012 2v1m2 13a2 2 0 01-2-2V7m2 13a2 2 0 002-2V9a2 2 0 00-2-2h-2m-4-3H9M7 16h6M7 8h6v4H7V8z"/></svg>
            <p class="text-sm">暂无帖子，快来发布第一条吧~</p>
          </div>
        </div>
      </div>

      <!-- 右侧边栏 -->
      <aside class="w-80 hidden lg:block flex-shrink-0 space-y-4">
        <!-- 热门话题 -->
        <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-5">
          <h3 class="text-sm font-bold text-gray-800 mb-4 flex items-center gap-2">
            <svg class="w-4 h-4 text-orange-500" fill="currentColor" viewBox="0 0 24 24"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/></svg>
            热门话题
          </h3>
          <div class="space-y-3">
            <div v-for="(topic, i) in hotTopics" :key="topic.id"
            @click="filterByTopic(topic)"
            :class="['flex items-center gap-3 cursor-pointer group', selectedTopic?.id === topic.id ? 'bg-blue-50 rounded-lg p-1.5 -mx-1.5' : '']">
              <span :class="['w-5 h-5 rounded text-xs font-bold flex items-center justify-center flex-shrink-0', i < 3 ? 'bg-red-50 text-red-500' : 'bg-gray-100 text-gray-400']">{{ i + 1 }}</span>
              <span class="text-sm text-gray-700 group-hover:text-primary transition-colors truncate">#{{ topic.name }}</span>
              <span class="text-xs text-gray-400 ml-auto">{{ topic.postCount }}帖</span>
            </div>
          </div>
        </div>

        <!-- 推荐用户 -->
        <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-5">
          <h3 class="text-sm font-bold text-gray-800 mb-4 flex items-center gap-2">
            <svg class="w-4 h-4 text-primary" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"/></svg>
            活跃用户
          </h3>
          <div class="space-y-3">
            <div v-for="user in activeUsers" :key="user.id" class="flex items-center gap-3">
              <img :src="user.avatar || defaultAvatar" class="w-9 h-9 rounded-full" />
              <div class="flex-1 min-w-0">
                <p class="text-sm font-medium text-gray-800 truncate">{{ user.nickname || user.username }}</p>
                <p class="text-xs text-gray-400">{{ user.postsCount }} 帖子</p>
              </div>
              <button class="px-3 py-1 text-xs border border-primary text-primary rounded-full hover:bg-primary hover:text-white transition-colors">关注</button>
            </div>
          </div>
        </div>
      </aside>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { postApi, topicApi, userApi } from '../../../api'
import type { Post, Topic, UserInfo } from '../../../types'

const defaultAvatar = 'https://placehold.co/40x40/e0e7ff/409EFF?text=U'
const selectedTopic = ref<Topic | null>(null)

const tabs = [
  { key: 'recommend', label: '推荐' },
  { key: 'latest', label: '最新' },
  { key: 'hot', label: '热榜' },
]

const activeTab = ref('recommend')
const loading = ref(false)
const posts = ref<Post[]>([])
const hotTopics = ref<Topic[]>([])
const activeUsers = ref<UserInfo[]>([])

watch(activeTab, () => fetchPosts())

onMounted(async () => {
  await Promise.all([fetchPosts(), fetchHotTopics(), fetchActiveUsers()])
})

async function fetchPosts() {
  loading.value = true
  try {
    let res: any
    if (activeTab.value === 'hot') {
      res = await postApi.hot(1, 10)
    } else {
      res = await postApi.list({
        sort: activeTab.value === 'recommend' ? 'latest' : activeTab.value,
        pageNum: 1, pageSize: 10,
        topicId: selectedTopic.value?.id || undefined,
      })
    }
    posts.value = (res.records || []).map((p: any) => ({
      ...p,
      id: p.postId,
      coverImage: p.images || '',
      authorId: p.authorId || p.userId,
    }))
  } catch (error) {
    console.error('获取帖子列表失败:', error)
    posts.value = []
  } finally {
    loading.value = false
  }
}

function filterByTopic(topic: Topic) {
  if (selectedTopic.value?.id === topic.id) {
    selectedTopic.value = null
  } else {
    selectedTopic.value = topic
  }
  fetchPosts()
}

async function fetchHotTopics() {
  try {
    const res: any = await topicApi.hot()
    hotTopics.value = res || []
  } catch {
    hotTopics.value = []
  }
}

async function fetchActiveUsers() {
  try {
    const res: any = await userApi.search('', 1, 5)
    activeUsers.value = (res?.records || []).map((u: any) => ({
      ...u,
      postsCount: u.postsCount || 0,
    }))
  } catch {
    activeUsers.value = []
  }
}

function formatTime(time: string): string {
  const now = new Date().getTime()
  const t = new Date(time).getTime()
  const diff = now - t
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  if (diff < 604800000) return Math.floor(diff / 86400000) + '天前'
  return time.split(' ')[0]
}
</script>
