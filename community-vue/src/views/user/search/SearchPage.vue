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
          <button @click="clearAllHistory"
            class="text-xs text-gray-400 hover:text-red-500 transition-colors cursor-pointer">清空</button>
        </div>
        <div class="flex flex-wrap gap-2">
          <div v-for="(item, i) in history" :key="i"
            class="group px-3 py-1.5 bg-gray-100 hover:bg-blue-50 rounded-full text-sm text-gray-600 cursor-pointer transition-colors flex items-center gap-1">
            <span @click="quickSearch(item)" class="hover:text-blue-600">{{ item }}</span>
            <button @click="removeHistory(i)"
              class="opacity-0 group-hover:opacity-100 text-gray-400 hover:text-red-500 transition-all text-xs px-1">×</button>
          </div>
        </div>
      </div>

      <!-- 热门搜索 -->
      <div v-if="!searched" class="bg-white rounded-xl p-6 shadow-sm mb-6">
        <h3 class="text-sm font-semibold text-gray-700 flex items-center gap-2 mb-4">
          <Flame class="w-4 h-4 text-orange-500" /> 热门搜索
          <button @click="refreshHotKeywords"
            class="ml-auto text-xs text-gray-400 hover:text-blue-600 transition-colors cursor-pointer flex items-center gap-1">
            <RefreshCw class="w-3 h-3" /> 刷新
          </button>
        </h3>
        <div class="space-y-3">
          <div v-for="(kw, idx) in hotKeywords" :key="idx" @click="quickSearch(kw)"
            class="flex items-center gap-3 py-2 px-3 -mx-3 rounded-lg hover:bg-gray-50 cursor-pointer group transition-colors">
            <span class="w-5 text-center text-sm font-bold"
              :class="idx < 3 ? 'text-red-500' : 'text-gray-400'">{{ idx + 1 }}</span>
            <span class="flex-1 text-gray-700 group-hover:text-blue-600 transition-colors">{{ kw.keyword || kw }}</span>
            <span v-if="kw.count" class="text-xs text-gray-400">{{ kw.count }}次</span>
            <TrendingUp v-if="idx < 3" class="w-4 h-4 text-red-400" />
          </div>
        </div>
      </div>

      <!-- Tab相关内容展示 -->
      <template v-if="!searched">
        <!-- 帖子Tab下显示热门帖子 -->
        <div v-if="activeTab === 'post' && hotPosts.length > 0" class="bg-white rounded-xl p-6 shadow-sm mb-6">
          <h3 class="text-sm font-semibold text-gray-700 flex items-center gap-2 mb-4">
            <TrendingUp class="w-4 h-4 text-orange-500" /> 热门帖子
          </h3>
          <div class="space-y-3">
          <div v-for="(post, idx) in hotPosts" :key="idx" @click="goToPost(post)"
            class="flex items-center gap-3 py-2 px-3 -mx-3 rounded-lg hover:bg-gray-50 cursor-pointer group transition-colors">
              <span class="w-5 text-center text-sm font-bold"
                :class="idx < 3 ? 'text-red-500' : 'text-gray-400'">{{ idx + 1 }}</span>
              <div class="flex-1 min-w-0">
                <h4 class="font-medium text-gray-800 truncate group-hover:text-blue-600 transition-colors"
                  v-html="highlightText(post.title)"></h4>
                <p class="text-xs text-gray-400 mt-1">♥ {{ post.likeCount || 0 }} · 👁 {{ post.viewCount || 0 }}</p>
              </div>
            </div>
          </div>
        </div>

        <!-- 话题Tab下显示热门话题 -->
        <div v-if="activeTab === 'topic' && hotTopics.length > 0" class="bg-white rounded-xl p-6 shadow-sm mb-6">
          <h3 class="text-sm font-semibold text-gray-700 flex items-center gap-2 mb-4">
            <Hash class="w-4 h-4 text-orange-500" /> 热门话题
          </h3>
          <div class="flex flex-wrap gap-2">
            <button v-for="topic in hotTopics" :key="topic.topicId || topic.id" @click="searchTopic(topic.name)"
              class="px-4 py-2 bg-blue-50 text-blue-600 rounded-full text-sm hover:bg-blue-100 transition-colors cursor-pointer">
              #{{ topic.name }}
              <span class="text-xs text-blue-400 ml-1">({{ topic.postCount || 0 }})</span>
            </button>
          </div>
        </div>

        <!-- 用户Tab下显示热门用户 -->
        <div v-if="activeTab === 'user' && hotUsers.length > 0" class="bg-white rounded-xl p-6 shadow-sm mb-6">
          <h3 class="text-sm font-semibold text-gray-700 flex items-center gap-2 mb-4">
            <Users class="w-4 h-4 text-orange-500" /> 热门用户
          </h3>
          <div class="space-y-3">
            <div v-for="(user, idx) in hotUsers" :key="idx" @click="goToUserProfile(user)"
              class="flex items-center gap-3 py-2 px-3 -mx-3 rounded-lg hover:bg-gray-50 cursor-pointer group transition-colors">
              <img :src="user.avatar || defaultAvatar" class="w-8 h-8 rounded-full object-cover" />
              <div class="flex-1 min-w-0">
                <h4 class="font-medium text-gray-800 truncate group-hover:text-blue-600 transition-colors">
                  {{ user.nickname || user.username }}
                </h4>
                <p class="text-xs text-gray-400">
                  粉丝 {{ user.followersCount || 0 }} · 帖子 {{ user.postCount || user.postsCount || 0 }}
                </p>
              </div>
            </div>
          </div>
        </div>
      </template>

      <!-- 搜索结果 -->
      <div v-if="searched && activeTab === 'post'">
        <!-- 帖子结果 -->
        <div v-if="posts.length > 0" class="space-y-4">
          <p class="text-sm text-gray-500 mb-3">找到 {{ postTotal }} 个相关帖子</p>
          <div v-for="post in posts" :key="post.postId || post.id" @click="goToPost(post)"
            class="group bg-white rounded-xl p-5 shadow-sm hover:shadow-md transition-all cursor-pointer">
            <h3 class="font-semibold text-gray-800 group-hover:text-blue-600 transition-colors mb-2 line-clamp-1"
              v-html="highlightText(post.title)">
            </h3>
            <p class="text-sm text-gray-500 line-clamp-2 mb-3" v-html="highlightText(post.summary || (post.content ? post.content.substring(0, 120) : ''))"></p>
            <div class="flex items-center gap-4 text-xs text-gray-400">
              <span>{{ post.authorName || post.username }}</span>
              <span>{{ formatTime(post.createTime) }}</span>
              <span>♥ {{ post.likeCount || 0 }} · 💬 {{ post.commentCount || 0 }}</span>
            </div>
          </div>
        </div>
        <div v-else-if="!loading" class="text-center py-16 bg-white rounded-xl">
          <SearchX class="w-14 h-14 text-gray-300 mx-auto mb-3" />
          <p class="text-gray-400">没有找到相关帖子，换个关键词试试吧</p>
        </div>
      </div>

      <!-- 话题搜索结果 -->
      <div v-if="searched && activeTab === 'topic'">
        <div v-if="topics.length > 0" class="space-y-3">
          <p class="text-sm text-gray-500 mb-3">找到 {{ topicTotal }} 个相关话题</p>
          <div v-for="topic in topics" :key="topic.topicId || topic.id" @click="searchTopic(topic.name)"
            class="group bg-white rounded-xl p-4 shadow-sm hover:shadow-md transition-all cursor-pointer">
            <div class="flex items-center gap-3">
              <Hash class="w-5 h-5 text-blue-500" />
              <div class="flex-1">
                <h4 class="font-medium text-gray-800 group-hover:text-blue-600 transition-colors"
                  v-html="highlightText(topic.name)"></h4>
                <p class="text-sm text-gray-500 mt-1" v-html="highlightText(topic.description || '')"></p>
                <p class="text-xs text-gray-400 mt-2">{{ topic.postCount || 0 }} 篇帖子</p>
              </div>
              <button v-if="topic.isHot" class="px-2 py-1 bg-red-100 text-red-600 rounded-full text-xs font-medium">热门</button>
            </div>
          </div>
        </div>
        <div v-else-if="!loading" class="text-center py-16 bg-white rounded-xl">
          <Hash class="w-14 h-14 text-gray-300 mx-auto mb-3" />
          <p class="text-gray-400">未找到相关话题</p>
        </div>
      </div>

      <!-- 用户搜索结果 -->
      <div v-if="searched && activeTab === 'user'">
        <div v-if="users.length > 0" class="space-y-3">
          <p class="text-sm text-gray-500 mb-3">找到 {{ userTotal }} 位相关用户</p>
          <div v-for="user in users" :key="user.userId || user.id" @click="goToUserProfile(user)"
            class="group flex items-center gap-4 bg-white rounded-xl p-4 shadow-sm hover:shadow-md transition-all cursor-pointer">
            <img :src="user.avatar || defaultAvatar" class="w-12 h-12 rounded-full object-cover ring-2 ring-gray-100" />
            <div class="flex-1 min-w-0">
              <h4 class="font-medium text-gray-800 truncate group-hover:text-blue-600 transition-colors"
                v-html="highlightText(user.nickname || user.username)"></h4>
              <p class="text-xs text-gray-400 truncate" v-html="highlightText(user.bio || '这个人很懒，什么都没写~')"></p>
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
import { ref, computed, onMounted, watch } from 'vue'
import { Search, Clock, Flame, TrendingUp, SearchX, Users, RefreshCw, Hash, X } from 'lucide-vue-next'
import { postApi, userApi, searchApi, topicApi } from '../../../api'
import type { Post, UserInfo, Topic } from '../../../types'
import { useRouter } from 'vue-router'

const router = useRouter()
const keyword = ref('')
const activeTab = ref('post')
const searched = ref(false)
const loading = ref(false)
const page = ref(1)
const posts = ref<Post[]>([])
const users = ref<UserInfo[]>([])
const topics = ref<Topic[]>([])
const postTotal = ref(0)
const userTotal = ref(0)
const topicTotal = ref(0)
const hotKeywords = ref<any[]>([])
const hotPosts = ref<Post[]>([])
const hotTopics = ref<Topic[]>([])
const hotUsers = ref<UserInfo[]>([])
const defaultAvatar = 'https://placehold.co/80x80/E8EAF6/9FA8DA?text=U'

const tabs = [
  { key: 'post', label: '帖子' },
  { key: 'topic', label: '话题' },
  { key: 'user', label: '用户' },
]

// 使用ref替代computed，解决localStorage不触发响应式更新的问题
const history = ref<string[]>([])

function loadHistory() {
  try {
    const raw = localStorage.getItem('search_history')
    history.value = raw ? JSON.parse(raw) : []
  } catch {
    history.value = []
  }
}
loadHistory()

const totalPages = computed(() => {
  const total = activeTab.value === 'post' ? postTotal.value :
                activeTab.value === 'user' ? userTotal.value : topicTotal.value
  return Math.ceil(total / 10)
})

// 高亮显示匹配文本
function highlightText(text: string): string {
  if (!text || !keyword.value.trim()) return text || ''
  const escaped = keyword.value.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
  const regex = new RegExp(`(${escaped})`, 'gi')
  return text.replace(regex, '<mark class="bg-yellow-200 text-gray-800 rounded px-1">$1</mark>')
}

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

function searchTopic(topicName: string) {
  keyword.value = `#${topicName}`
  activeTab.value = 'post'
  doSearch()
}

async function fetchResults() {
  loading.value = true
  try {
    if (activeTab.value === 'post') {
      const res: any = await postApi.list({ keyword: keyword.value.trim(), pageNum: page.value, pageSize: 10 })
      console.log('帖子搜索结果:', res?.records)
      // 兼容后端返回的postId和id两种字段名
      posts.value = (res?.records || []).map((post: any) => ({
        ...post,
        id: Number(post.postId || post.id) || 0
      })).filter((post: any) => post.id > 0)
      postTotal.value = res?.total || 0
    } else if (activeTab.value === 'user') {
      const res: any = await userApi.search(keyword.value.trim(), page.value, 10)
      console.log('用户搜索结果:', res?.records)
      // 兼容后端返回的userId和id两种字段名
      users.value = (res?.records || []).map((user: any) => ({
        ...user,
        id: Number(user.userId || user.id) || 0
      })).filter((user: any) => user.id > 0)
      userTotal.value = res?.total || 0
    } else if (activeTab.value === 'topic') {
      const res: any = await topicApi.list()
      const allTopics = res || []
      const searchKey = keyword.value.trim().toLowerCase()
      topics.value = allTopics.filter((t: any) =>
        (t.name || '').toLowerCase().includes(searchKey) ||
        ((t.description || '').toLowerCase().includes(searchKey))
      )
      topicTotal.value = topics.value.length
    }
  } catch (e) {
    console.error('搜索失败:', e)
  } finally {
    loading.value = false
  }
}

// 保存搜索历史
function saveHistory() {
  if (!keyword.value.trim()) return
  const list = history.value.filter((h: string) => h !== keyword.value.trim())
  list.unshift(keyword.value.trim())
  history.value = list.slice(0, 20)
  localStorage.setItem('search_history', JSON.stringify(history.value))
}

// 删除单个历史记录
function removeHistory(index: number) {
  history.value.splice(index, 1)
  localStorage.setItem('search_history', JSON.stringify(history.value))
}

// 清空所有历史记录
function clearAllHistory() {
  history.value = []
  localStorage.removeItem('search_history')
}

// 获取热门搜索
async function fetchHotKeywords() {
  try {
    const res: any = await searchApi.hot()
    hotKeywords.value = res || []
  } catch (e) {
    hotKeywords.value = ['Spring Boot微服务', 'Vue3最佳实践', 'Redis缓存策略', 'Docker容器化部署', 'Elasticsearch全文搜索']
  }
}

// 刷新热门搜索
async function refreshHotKeywords() {
  await fetchHotKeywords()
}

// 获取热门帖子
async function fetchHotPosts() {
  try {
    const res: any = await postApi.hot(1, 5)
    hotPosts.value = (res?.records || []).map((post: any) => ({
      ...post,
      id: Number(post.postId || post.id) || 0
    })).filter((post: any) => post.id > 0).slice(0, 5)
  } catch (e) {
    hotPosts.value = []
  }
}

// 获取热门话题
async function fetchHotTopics() {
  try {
    const res: any = await topicApi.hot()
    hotTopics.value = res || []
  } catch (e) {
    hotTopics.value = []
  }
}

// 获取热门用户
async function fetchHotUsers() {
  try {
    const res: any = await userApi.search('', 1, 5)
    console.log('热门用户:', res?.records)
    hotUsers.value = (res?.records || []).map((user: any) => ({
      ...user,
      id: Number(user.userId || user.id) || 0
    })).filter((user: any) => user.id > 0)
  } catch (e) {
    hotUsers.value = []
  }
}

// 跳转到帖子详情 - 兼容postId和id字段
function goToPost(post: any) {
  const postId = Number(post?.postId || post?.id) || 0
  console.log('跳转帖子, postId:', postId)
  if (!postId || postId <= 0) {
    console.error('无效的帖子ID:', post)
    return
  }
  router.push(`/post/${postId}`)
}

// 跳转到用户主页 - 兼容userId和id字段
function goToUserProfile(user: any) {
  const userId = Number(user?.userId || user?.id) || 0
  console.log('跳转用户, userId:', userId)
  if (!userId || userId <= 0) {
    console.error('无效的用户ID:', user)
    return
  }
  router.push(`/user/${userId}`)
}

function formatTime(time: string): string {
  return time ? new Date(time).toLocaleDateString('zh-CN') : ''
}

// 监听activeTab变化，加载对应的热门内容
watch(activeTab, (newTab) => {
  if (!searched.value) {
    if (newTab === 'post') {
      fetchHotPosts()
    } else if (newTab === 'topic') {
      fetchHotTopics()
    } else if (newTab === 'user') {
      fetchHotUsers()
    }
  }
})

// 监听页码变化
watch(page, () => {
  if (searched.value) {
    fetchResults()
  }
})

onMounted(() => {
  fetchHotKeywords()
  fetchHotPosts()
  fetchHotTopics()
  fetchHotUsers()
})
</script>

<style scoped>
::deep(mark) {
  padding: 0 2px;
  border-radius: 2px;
}

.line-clamp-1 {
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
