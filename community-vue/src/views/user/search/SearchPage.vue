<template>
  <UserPageShell title="搜索" icon="fa fa-search" subtitle="帖子、话题与用户" max-width-class="max-w-[1200px]">
    <div class="max-w-[800px] mx-auto w-full">
      <div class="mb-8 mt-1">
        <div class="relative w-full">
          <i class="fa fa-search absolute left-5 top-1/2 -translate-y-1/2 text-[20px] text-[#6b7280]"></i>
          <input
            ref="inputRef"
            v-model="keyword"
            @keyup.enter="doSearch"
            type="search"
            placeholder="搜索帖子、话题、用户..."
            autofocus
            class="w-full py-[18px] pr-[100px] pl-[60px] rounded-[20px] border-none outline-none text-base shadow-[0_10px_30px_-5px_rgba(99,102,241,0.15)] transition-all duration-300 focus:shadow-[0_20px_40px_-5px_rgba(99,102,241,0.25)] focus:-translate-y-0.5"
          />
          <button
            @click="doSearch"
            :disabled="!keyword.trim()"
            class="absolute right-[15px] top-1/2 -translate-y-1/2 px-5 py-2.5 bg-gradient-to-r from-[#6366f1] to-[#a855f7] text-white rounded-[12px] font-semibold cursor-pointer transition-all duration-300 hover:scale-105 disabled:opacity-50"
          >
            搜索
          </button>
        </div>
      </div>
    </div>

    <div v-if="!searched" class="max-w-[1100px] mx-auto w-full pb-12">
      <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
        <!-- 搜索历史 -->
        <div v-if="history.length > 0" class="bg-white rounded-[18px] p-5 shadow-[0_10px_30px_-5px_rgba(99,102,241,0.15)]">
          <div class="flex items-center justify-between mb-4">
            <h3 class="text-base font-semibold text-[#1f2937] flex items-center gap-2">
              <i class="fa fa-clock-o text-[#6366f1]"></i> 搜索历史
            </h3>
            <button
              @click="clearAllHistory"
              class="text-xs text-[#6b7280] hover:text-red-500 transition-colors cursor-pointer"
            >
              清空
            </button>
          </div>
          <div class="flex flex-wrap gap-2">
            <div
              v-for="(item, i) in history"
              :key="i"
              class="group px-3 py-1.5 bg-[#f0f4ff] hover:bg-[#6366f1]/10 rounded-full text-sm text-[#6b7280] cursor-pointer transition-colors flex items-center gap-1"
            >
              <span @click="quickSearch(item)" class="hover:text-[#6366f1]">{{ item }}</span>
              <button
                @click="removeHistory(i)"
                class="opacity-0 group-hover:opacity-100 text-[#6b7280] hover:text-red-500 transition-all text-xs px-1"
              >
                ×
              </button>
            </div>
          </div>
        </div>

        <!-- 热门搜索 -->
        <div class="bg-white rounded-[18px] p-5 shadow-[0_10px_30px_-5px_rgba(99,102,241,0.15)]">
          <div class="flex items-center justify-between mb-4">
            <h3 class="text-base font-semibold text-[#1f2937] flex items-center gap-2">
              <i class="fa fa-fire text-[#ec4899]"></i> 热门搜索
            </h3>
            <button
              @click="refreshHotKeywords"
              class="text-xs text-[#6b7280] hover:text-[#6366f1] transition-colors cursor-pointer flex items-center gap-1"
            >
              <i class="fa fa-refresh"></i> 刷新
            </button>
          </div>
          <div class="space-y-2">
            <div
              v-for="(kw, idx) in hotKeywords"
              :key="idx"
              @click="quickSearch(typeof kw === 'string' ? kw : kw.keyword)"
              class="flex items-center gap-3 py-2 px-3 -mx-3 rounded-lg hover:bg-[#f9fafb] cursor-pointer group transition-colors"
            >
              <span
                class="w-5 text-center text-sm font-bold"
                :class="idx < 3 ? 'text-[#ec4899]' : 'text-[#6b7280]'"
              >
                {{ idx + 1 }}
              </span>
              <span class="flex-1 text-[#1f2937] group-hover:text-[#6366f1] transition-colors">
                {{ typeof kw === 'string' ? kw : kw.keyword }}
              </span>
              <span v-if="typeof kw !== 'string' && kw.count" class="text-xs text-[#9ca3af]">
                {{ kw.count }}次
              </span>
              <i v-if="idx < 3" class="fa fa-arrow-up text-[#ec4899]"></i>
            </div>
          </div>
        </div>
      </div>

      <!-- Tab相关内容展示 -->
      <div class="mt-6">
        <!-- 帖子Tab下显示热门帖子 -->
        <div v-if="activeTab === 'post' && hotPosts.length > 0" class="bg-white rounded-[18px] p-5 shadow-[0_10px_30px_-5px_rgba(99,102,241,0.15)]">
          <h3 class="text-base font-semibold text-[#1f2937] flex items-center gap-2 mb-4">
            <i class="fa fa-fire text-orange-500"></i> 热门帖子
          </h3>
          <div class="space-y-3">
            <div
              v-for="(post, idx) in hotPosts"
              :key="idx"
              @click="goToPost(post)"
              class="flex items-center gap-3 py-2 px-3 -mx-3 rounded-lg hover:bg-[#f9fafb] cursor-pointer group transition-colors"
            >
              <span
                class="w-5 text-center text-sm font-bold"
                :class="idx < 3 ? 'text-[#ec4899]' : 'text-[#6b7280]'"
              >
                {{ idx + 1 }}
              </span>
              <div class="flex-1 min-w-0">
                <h4 class="font-medium text-[#1f2937] truncate group-hover:text-[#6366f1] transition-colors">
                  {{ post.title }}
                </h4>
                <p class="text-xs text-[#9ca3af] mt-1">
                  ♥ {{ post.likeCount || 0 }} · 👁 {{ post.viewCount || 0 }}
                </p>
              </div>
            </div>
          </div>
        </div>

        <!-- 话题Tab下显示热门话题 -->
        <div v-if="activeTab === 'topic' && hotTopics.length > 0" class="bg-white rounded-[18px] p-5 shadow-[0_10px_30px_-5px_rgba(99,102,241,0.15)]">
          <h3 class="text-base font-semibold text-[#1f2937] flex items-center gap-2 mb-4">
            <i class="fa fa-hashtag text-[#a855f7]"></i> 热门话题
          </h3>
          <div class="flex flex-wrap gap-2">
            <button
              v-for="topic in hotTopics"
              :key="topic.id"
              @click="searchTopic(topic.name)"
              class="px-4 py-2 bg-[#f0f4ff] text-[#6366f1] rounded-full text-sm hover:bg-[#6366f1]/10 transition-colors cursor-pointer"
            >
              #{{ topic.name }}
              <span class="text-xs text-[#818cf8] ml-1">({{ topic.postCount || 0 }})</span>
            </button>
          </div>
        </div>

        <!-- 用户Tab下显示热门用户 -->
        <div v-if="activeTab === 'user' && hotUsers.length > 0" class="bg-white rounded-[18px] p-5 shadow-[0_10px_30px_-5px_rgba(99,102,241,0.15)]">
          <h3 class="text-base font-semibold text-[#1f2937] flex items-center gap-2 mb-4">
            <i class="fa fa-users text-[#6366f1]"></i> 热门用户
          </h3>
          <div class="space-y-3">
            <div
              v-for="(user, idx) in hotUsers"
              :key="idx"
              @click="goToUserProfile(user)"
              class="flex items-center gap-3 py-2 px-3 -mx-3 rounded-lg hover:bg-[#f9fafb] cursor-pointer group transition-colors"
            >
              <img
                :src="resolveFileUrl(user.avatar) || defaultAvatar"
                referrerpolicy="no-referrer"
                class="w-10 h-10 rounded-full object-cover border-2 border-[#818cf8]"
              />
              <div class="flex-1 min-w-0">
                <h4 class="font-medium text-[#1f2937] truncate group-hover:text-[#6366f1] transition-colors">
                  {{ user.nickname || user.username }}
                </h4>
                <p class="text-xs text-[#9ca3af]">
                  粉丝 {{ user.followersCount || 0 }} · 帖子 {{ user.postsCount || 0 }}
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-if="searched" class="max-w-[1100px] mx-auto w-full pb-12">
      <!-- 搜索结果标题 -->
      <h2 class="text-[24px] font-bold text-[#1f2937] text-center mb-5">
        搜索结果：{{ keyword }}
      </h2>

      <!-- Tab切换 -->
      <div class="flex justify-center gap-4 mb-9 flex-wrap">
        <button
          v-for="tab in tabs"
          :key="tab.key"
          @click="activeTab = tab.key"
          :class="[
            'px-6 py-3 rounded-[15px] text-sm font-semibold transition-all duration-300 cursor-pointer',
            activeTab === tab.key
              ? 'bg-gradient-to-r from-[#6366f1] to-[#a855f7] text-white'
              : 'bg-white text-[#6b7280] hover:bg-[#f0f4ff] hover:text-[#6366f1] hover:-translate-y-0.5'
          ]"
          class="shadow-[0_4px_10px_rgba(0,0,0,0.05)]"
        >
          {{ tab.label }}
        </button>
      </div>

      <!-- 帖子搜索结果 -->
      <div v-if="searched && activeTab === 'post'" class="animate-[fadeIn_0.5s_ease]">
        <div v-if="posts.length > 0" class="space-y-4">
          <p class="text-sm text-[#6b7280] mb-3">找到 {{ postTotal }} 个相关帖子</p>
          <div
            v-for="post in posts"
            :key="post.id"
            @click="goToPost(post)"
            class="bg-white rounded-[18px] p-5 shadow-[0_10px_30px_-5px_rgba(99,102,241,0.15)] cursor-pointer transition-all duration-300 hover:-translate-y-1 hover:shadow-[0_20px_40px_-5px_rgba(99,102,241,0.25)]"
          >
            <h3
              class="text-lg font-semibold text-[#1f2937] mb-2 line-clamp-1"
              v-html="formatSearchHtml(post.title)"
            ></h3>
            <p
              class="text-sm text-[#6b7280] line-clamp-2 mb-3"
              v-html="formatSearchHtml(post.summary || (post.content ? stripHtml(post.content).substring(0, 160) : ''))"
            ></p>
            <div class="flex items-center gap-4 text-xs text-[#9ca3af]">
              <span>{{ post.authorName }}</span>
              <span>{{ formatTime(post.createTime) }}</span>
              <span>♥ {{ post.likeCount || 0 }} · 💬 {{ post.commentCount || 0 }}</span>
            </div>
          </div>
        </div>
        <div v-else-if="!loading" class="text-center py-16">
          <i class="fa fa-search text-[50px] text-[#d1d5db] mb-4"></i>
          <p class="text-[#6b7280]">没有找到相关帖子，换个关键词试试吧</p>
        </div>
      </div>

      <!-- 话题搜索结果 -->
      <div v-if="searched && activeTab === 'topic'" class="animate-[fadeIn_0.5s_ease]">
        <div v-if="topics.length > 0" class="space-y-4">
          <p class="text-sm text-[#6b7280] mb-3">找到 {{ topicTotal }} 个相关话题</p>
          <div
            v-for="topic in topics"
            :key="topic.id"
            @click="searchTopic(topic.name)"
            class="bg-white rounded-[18px] p-5 shadow-[0_10px_30px_-5px_rgba(99,102,241,0.15)] flex items-center gap-4 cursor-pointer transition-all duration-300 hover:translate-x-1"
          >
            <div class="w-[50px] h-[50px] rounded-[15px] bg-gradient-to-br from-[#818cf8] to-[#a855f7] flex items-center justify-center text-white text-xl">
              <i class="fa fa-hashtag"></i>
            </div>
            <div class="flex-1">
              <h4
                class="font-semibold text-base text-[#1f2937]"
                v-html="highlightText(topic.name)"
              ></h4>
              <p
                class="text-sm text-[#6b7280] mt-1"
                v-html="highlightText(topic.description || '')"
              ></p>
              <p class="text-xs text-[#9ca3af] mt-1">{{ topic.postCount || 0 }} 篇帖子</p>
            </div>
            <span
              v-if="topic.isHot"
              class="px-2 py-1 bg-red-100 text-red-500 rounded-full text-xs font-medium"
            >
              热门
            </span>
          </div>
        </div>
        <div v-else-if="!loading" class="text-center py-16">
          <i class="fa fa-hashtag text-[50px] text-[#d1d5db] mb-4"></i>
          <p class="text-[#6b7280]">未找到相关话题</p>
        </div>
      </div>

      <!-- 用户搜索结果 -->
      <div v-if="searched && activeTab === 'user'" class="animate-[fadeIn_0.5s_ease]">
        <div v-if="users.length > 0" class="space-y-4">
          <p class="text-sm text-[#6b7280] mb-3">找到 {{ userTotal }} 位相关用户</p>
          <div
            v-for="user in users"
            :key="user.id"
            @click="goToUserProfile(user)"
            class="bg-white rounded-[18px] p-5 shadow-[0_10px_30px_-5px_rgba(99,102,241,0.15)] flex items-center gap-4 cursor-pointer transition-all duration-300 hover:translate-x-1"
          >
            <img
              :src="resolveFileUrl(user.avatar) || defaultAvatar"
              referrerpolicy="no-referrer"
              class="w-[60px] h-[60px] rounded-full object-cover border-[3px] border-[#818cf8]"
            />
            <div class="flex-1 min-w-0">
              <h4
                class="font-bold text-base text-[#1f2937] truncate"
                v-html="highlightText(user.nickname || user.username)"
              ></h4>
              <p
                class="text-sm text-[#6b7280] truncate"
                v-html="highlightText(user.bio || '这个人很懒，什么都没写~')"
              ></p>
            </div>
            <div class="flex items-center gap-4 text-xs text-[#9ca3af] mr-2">
              <span>关注 {{ user.followingCount || 0 }}</span>
              <span>粉丝 {{ user.followersCount || 0 }}</span>
            </div>
            <button class="px-4 py-2 rounded-[12px] bg-gradient-to-r from-[#6366f1] to-[#a855f7] text-white text-sm font-semibold hover:scale-105 transition-transform">
              关注
            </button>
          </div>
        </div>
        <div v-else-if="!loading" class="text-center py-16">
          <i class="fa fa-users text-[50px] text-[#d1d5db] mb-4"></i>
          <p class="text-[#6b7280]">未找到相关用户</p>
        </div>
      </div>

      <!-- 加载中 -->
      <div v-if="loading" class="space-y-4">
        <div v-for="i in 5" :key="i" class="bg-white rounded-[18px] p-5 animate-pulse">
          <div class="h-5 bg-[#e5e7eb] rounded w-3/4 mb-3"></div>
          <div class="h-4 bg-[#f3f4f6] rounded w-full mb-2"></div>
          <div class="h-4 bg-[#f3f4f6] rounded w-1/2"></div>
        </div>
      </div>

      <!-- 分页 -->
      <div v-if="totalPages > 1" class="flex justify-center mt-8 gap-2">
        <button
          @click="page--"
          :disabled="page <= 1"
          class="px-4 py-2 bg-white border border-[#e5e7eb] text-[#6b7280] rounded-[10px] hover:border-[#6366f1] hover:text-[#6366f1] transition-colors disabled:opacity-40 cursor-pointer text-sm"
        >
          上一页
        </button>
        <span class="px-4 py-2 text-sm text-[#6b7280] self-center">{{ page }} / {{ totalPages }}</span>
        <button
          @click="page++"
          :disabled="page >= totalPages"
          class="px-4 py-2 bg-white border border-[#e5e7eb] text-[#6b7280] rounded-[10px] hover:border-[#6366f1] hover:text-[#6366f1] transition-colors disabled:opacity-40 cursor-pointer text-sm"
        >
          下一页
        </button>
      </div>
    </div>
  </UserPageShell>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { postApi, userApi, searchApi, topicApi } from '../../../api'
import type { Post, UserInfo, Topic } from '../../../types'
import { useRouter } from 'vue-router'
import { resolveFileUrl } from '../../../utils/resolveFileUrl'
import UserPageShell from '../../../components/user/UserPageShell.vue'

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

// 高亮显示匹配文本（本地过滤场景）
function highlightText(text: string): string {
  if (!text || !keyword.value.trim()) return text || ''
  const escaped = keyword.value.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
  const regex = new RegExp(`(${escaped})`, 'gi')
  return text.replace(regex, '<mark class="bg-yellow-200 text-[#1f2937] rounded px-1">$1</mark>')
}

/** 去掉 HTML 标签（用于摘要纯文本） */
function stripHtml(s: string): string {
  return s.replace(/<[^>]+>/g, '')
}

/**
 * ES 返回的标题/摘要可能已含 <em> 高亮；避免与本地 <mark> 重复包裹
 */
function formatSearchHtml(raw: string): string {
  if (!raw) return ''
  const k = keyword.value.trim()
  if (k && /<em[\s>]/i.test(raw)) {
    return raw.replace(/<em>/gi, '<mark class="bg-amber-200 text-[#1f2937] rounded px-0.5">').replace(/<\/em>/gi, '</mark>')
  }
  return highlightText(stripHtml(raw))
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
      const res: any = await searchApi.posts(keyword.value.trim(), page.value, 10)
      posts.value = (res?.records || []).map((post: any) => ({
        ...post,
        id: Number(post.postId || post.id) || 0,
        commentCount: post.commentCount ?? 0,
        createTime: post.createTime ?? '',
      })).filter((post: any) => post.id > 0)
      postTotal.value = Number(res?.total) || 0
    } else if (activeTab.value === 'user') {
      const res: any = await userApi.search(keyword.value.trim(), page.value, 10)
      users.value = (res?.records || []).map((user: any) => ({
        ...user,
        id: Number(user.userId || user.id) || 0
      })).filter((user: any) => user.id > 0)
      userTotal.value = Number(res?.total) || 0
    } else if (activeTab.value === 'topic') {
      const res: any = await topicApi.search(keyword.value.trim(), page.value, 10)
      topics.value = (res?.records || []).map((t: any) => ({
        ...t,
        id: Number(t.topicId ?? t.id) || 0,
        isHot: t.isHot === 1,
      })).filter((t: any) => t.id > 0)
      topicTotal.value = Number(res?.total) || 0
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
  if (!postId || postId <= 0) return
  router.push(`/post/${postId}`)
}

// 跳转到用户主页 - 兼容userId和id字段
function goToUserProfile(user: any) {
  const userId = Number(user?.userId || user?.id) || 0
  if (!userId || userId <= 0) return
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

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
