<template>
  <div class="min-h-screen bg-[#F5F7FA] pt-20 pb-8">
    <div class="max-w-4xl mx-auto px-4">
      <!-- 搜索框 -->
      <div class="relative mb-6">
        <Search class="absolute left-5 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400 pointer-events-none" />
        <input ref="inputRef" v-model="keyword" @keyup.enter="doSearch" type="search"
          placeholder="搜索帖子、话题、用户..." autofocus
          class="w-full pl-14 pr-32 py-4 bg-white rounded-2xl shadow-lg focus:ring-2 focus:ring-blue-400 focus:shadow-xl transition-all outline-none text-base" />
        <button @click="doSearch"
          :disabled="!keyword.trim()"
          class="absolute right-3 top-1/2 -translate-y-1/2 px-6 py-2 bg-gradient-to-r from-blue-500 to-blue-600 text-white rounded-xl hover:from-blue-600 hover:to-blue-700 transition-all cursor-pointer text-sm font-medium disabled:opacity-50">
          搜索
        </button>
      </div>

      <!-- Tab切换 -->
      <div class="flex gap-1 mb-6 bg-white p-1.5 rounded-xl w-fit shadow-sm">
        <button v-for="tab in tabs" :key="tab.key" @click="activeTab = tab.key"
          :class="['px-5 py-2 rounded-lg text-sm font-medium transition-all duration-200 cursor-pointer',
            activeTab === tab.key ? 'bg-gradient-to-r from-blue-500 to-blue-600 text-white shadow-md' : 'text-gray-600 hover:bg-gray-50']">
          {{ tab.label }}
        </button>
      </div>

      <!-- 搜索历史 -->
      <div v-if="!searched && history.length > 0" class="bg-white rounded-xl p-6 shadow-sm mb-6">
        <div class="flex items-center justify-between mb-4">
          <h3 class="text-sm font-semibold text-gray-700 flex items-center gap-2">
            <Clock class="w-4 h-4" /> 搜索历史
          </h3>
          <button @click="clearHistory"
            class="text-xs text-gray-400 hover:text-red-500 transition-colors cursor-pointer">清空</button>
        </div>
        <div class="flex flex-wrap gap-2">
          <span v-for="(item, i) in history" :key="i" @click="quickSearch(item)"
            class="px-3 py-1.5 bg-gray-100 hover:bg-blue-50 hover:text-blue-600 rounded-full text-sm text-gray-600 cursor-pointer transition-colors">
            {{ item }}
          </span>
        </div>
      </div>

      <!-- 热门搜索 -->
      <div v-if="!searched" class="bg-white rounded-xl p-6 shadow-sm mb-6">
        <h3 class="text-sm font-semibold text-gray-700 flex items-center gap-2 mb-4">
          <Flame class="w-4 h-4 text-orange-500" /> 热门搜索
        </h3>
        <div class="space-y-3">
          <div v-for="(kw, idx) in hotKeywords" :key="idx" @click="quickSearch(kw)"
            class="flex items-center gap-3 py-2 px-3 -mx-3 rounded-lg hover:bg-gray-50 cursor-pointer group transition-colors">
            <span class="w-5 text-center text-sm font-bold"
              :class="idx < 3 ? 'text-red-500' : 'text-gray-400'">{{ idx + 1 }}</span>
            <span class="flex-1 text-gray-700 group-hover:text-blue-600 transition-colors">{{ kw }}</span>
            <TrendingUp v-if="idx < 3" class="w-4 h-4 text-red-400" />
          </div>
        </div>
      </div>

      <!-- 搜索结果 -->
      <div v-if="searched && activeTab === 'post'">
        <!-- 帖子结果 -->
        <div v-if="posts.length > 0" class="space-y-4">
          <p class="text-sm text-gray-500 mb-3">找到 {{ postTotal }} 个相关帖子</p>
          <div v-for="post in posts" :key="post.id" @click="$router.push(`/post/${post.id}`)"
            class="group bg-white rounded-xl p-5 shadow-sm hover:shadow-md transition-all cursor-pointer">
            <h3 class="font-semibold text-gray-800 group-hover:text-blue-600 transition-colors mb-2 line-clamp-1">
              {{ post.title }}
            </h3>
            <p class="text-sm text-gray-500 line-clamp-2 mb-3">{{ post.summary || post.content.substring(0, 120) }}</p>
            <div class="flex items-center gap-4 text-xs text-gray-400">
              <span>{{ post.authorName }}</span>
              <span>{{ formatTime(post.createTime) }}</span>
              <span>♥ {{ post.likeCount }} · 💬 {{ post.commentCount }}</span>
            </div>
          </div>
        </div>
        <div v-else-if="!loading" class="text-center py-16 bg-white rounded-xl">
          <SearchX class="w-14 h-14 text-gray-300 mx-auto mb-3" />
          <p class="text-gray-400">没有找到相关帖子，换个关键词试试吧</p>
        </div>
      </div>

      <!-- 用户搜索结果 -->
      <div v-if="searched && activeTab === 'user'">
        <div v-if="users.length > 0" class="space-y-3">
          <p class="text-sm text-gray-500 mb-3">找到 {{ userTotal }} 位相关用户</p>
          <div v-for="user in users" :key="user.id" @click="$router.push(`/profile/${user.id}`)"
            class="group flex items-center gap-4 bg-white rounded-xl p-4 shadow-sm hover:shadow-md transition-all cursor-pointer">
            <img :src="user.avatar || defaultAvatar" class="w-12 h-12 rounded-full object-cover ring-2 ring-gray-100" />
            <div class="flex-1 min-w-0">
              <h4 class="font-medium text-gray-800 truncate group-hover:text-blue-600 transition-colors">{{ user.nickname || user.username }}</h4>
              <p class="text-xs text-gray-400 truncate">{{ user.bio || '这个人很懒，什么都没写~' }}</p>
            </div>
            <div class="flex items-center gap-4 text-xs text-gray-400 mr-2">
              <span>关注 {{ user.followingCount || 0 }}</span>
              <span>粉丝 {{ user.followersCount || 0 }}</span>
            </div>
          </div>
        </div>
        <div v-else-if="!loading" class="text-center py-16 bg-white rounded-xl">
          <Users class="w-14 h-14 text-gray-300 mx-auto mb-3" />
          <p class="text-gray-400">未找到相关用户</p>
        </div>
      </div>

      <!-- 加载中 -->
      <div v-if="loading" class="space-y-4">
        <div v-for="i in 5" :key="i" class="bg-white rounded-xl p-5 animate-pulse">
          <div class="h-5 bg-gray-200 rounded w-3/4 mb-3"></div>
          <div class="h-4 bg-gray-100 rounded w-full mb-2"></div>
          <div class="h-4 bg-gray-100 rounded w-1/2"></div>
        </div>
      </div>

      <!-- 分页 -->
      <div v-if="totalPages > 1" class="flex justify-center mt-8 gap-2">
        <button @click="page--" :disabled="page <= 1"
          class="px-4 py-2 bg-white rounded-lg border border-gray-200 text-gray-600 hover:border-blue-400 hover:text-blue-600 transition-colors disabled:opacity-40 cursor-pointer text-sm">上一页</button>
        <span class="px-4 py-2 text-sm text-gray-500 self-center">{{ page }} / {{ totalPages }}</span>
        <button @click="page++" :disabled="page >= totalPages"
          class="px-4 py-2 bg-white rounded-lg border border-gray-200 text-gray-600 hover:border-blue-400 hover:text-blue-600 transition-colors disabled:opacity-40 cursor-pointer text-sm">下一页</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { Search, Clock, Flame, TrendingUp, SearchX, Users } from 'lucide-vue-next'
import { postApi, userApi } from '../../../api'

const keyword = ref('')
const activeTab = ref('post')
const searched = ref(false)
const loading = ref(false)
const page = ref(1)
const posts = ref<any[]>([])
const users = ref<any[]>([])
const postTotal = ref(0)
const userTotal = ref(0)
const defaultAvatar = 'https://placehold.co/80x80/E8EAF6/9FA8DA?text=U'

const tabs = [
  { key: 'post', label: '帖子' },
  { key: 'user', label: '用户' },
]

const history = computed(() => {
  const raw = localStorage.getItem('search_history')
  return raw ? JSON.parse(raw) : []
})

const hotKeywords = ['Spring Boot微服务', 'Vue3最佳实践', 'Redis缓存策略', 'Docker容器化部署', 'Elasticsearch全文搜索', 'RabbitMQ消息队列', 'Nacos配置中心']

const totalPages = computed(() => Math.ceil((activeTab.value === 'post' ? postTotal.value : userTotal.value) / 10))

function doSearch() {
  if (!keyword.value.trim()) return
  searched.value = true
  page.value = 1
  saveHistory()
  fetchResults()
}

function quickSearch(kw: string) {
  keyword.value = kw
  doSearch()
}

async function fetchResults() {
  loading.value = true
  try {
    if (activeTab.value === 'post') {
      const res: any = await postApi.list({ keyword: keyword.value.trim(), pageNum: page.value, pageSize: 10 })
      posts.value = res?.records || []
      postTotal.value = res?.total || 0
    } else {
      const res: any = await userApi.search(keyword.value.trim(), page.value, 10)
      users.value = res?.records || []
      userTotal.value = res?.total || 0
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

function saveHistory() {
  if (!keyword.value.trim()) return
  const list = history.value.filter((h: string) => h !== keyword.value.trim())
  list.unshift(keyword.value.trim())
  localStorage.setItem('search_history', JSON.stringify(list.slice(0, 20)))
}

function clearHistory() {
  localStorage.removeItem('search_history')
}

function formatTime(time: string): string {
  return time ? new Date(time).toLocaleDateString('zh-CN') : ''
}
</script>
