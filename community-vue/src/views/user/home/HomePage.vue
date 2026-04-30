<template>
  <div class="relative overflow-hidden bg-gradient-to-br from-[#f0f4ff] via-[#f8faff] to-[#eef2ff] text-[#1f2937]">
    <section class="relative min-h-[78vh] flex items-center justify-center px-4 pb-16 pt-10">
      <div
        class="pointer-events-none absolute -top-24 left-1/2 h-[28rem] w-[28rem] -translate-x-1/2 rounded-full bg-indigo-400/20 blur-[100px]"
        aria-hidden="true"
      />
      <div
        class="pointer-events-none absolute bottom-0 right-0 h-64 w-64 rounded-full bg-fuchsia-400/15 blur-[80px]"
        aria-hidden="true"
      />
      <div class="container relative z-[1] mx-auto text-center">
        <div class="mx-auto max-w-4xl animate-fade-in-up">
          <p class="mb-4 inline-flex items-center gap-2 rounded-full border border-[#e5e7eb] bg-white/80 px-4 py-1.5 text-xs font-medium tracking-wide text-[#6b7280] shadow-sm">
            <span class="h-1.5 w-1.5 rounded-full bg-emerald-500 shadow-[0_0_8px_rgba(16,185,129,0.6)]" /> 社区在线 · 欢迎回来
          </p>
          <h1 class="mb-6 text-[clamp(2.25rem,7vw,4.25rem)] font-bold leading-[1.1] tracking-tight text-[#1f2937]">
            连接<span class="bg-gradient-to-r from-[#6366f1] to-[#a855f7] bg-clip-text text-transparent">未来</span>
            <br />
            遇见有趣的<span class="bg-gradient-to-r from-[#6366f1] to-[#ec4899] bg-clip-text text-transparent">灵魂</span>
          </h1>

          <p class="mx-auto mb-10 max-w-2xl text-base leading-relaxed text-[#6b7280] md:text-lg">
            星云社区 · 用科技连接热爱。发帖、逛板块、搜话题，把灵感留在每一次交流里。
          </p>

          <div class="mb-14 flex flex-col justify-center gap-3 sm:flex-row sm:gap-4">
            <button
              type="button"
              class="btn-gradient inline-flex items-center justify-center gap-2 px-8 py-3.5 text-base"
              @click="$router.push('/post/create')"
            >
              <i class="fa fa-paper-plane" />立即发布
            </button>
            <button
              type="button"
              class="inline-flex items-center justify-center gap-2 rounded-full px-8 py-3.5 text-base border-2 border-[#e5e7eb] bg-white text-[#6366f1] font-semibold shadow-sm transition-all hover:border-[#c7d2fe] hover:bg-[#f8faff]"
              @click="$router.push('/section')"
            >
              <i class="fa fa-compass" />探索板块
            </button>
          </div>

          <div id="3d-element" class="relative mx-auto h-48 max-w-2xl md:h-64" />
        </div>
      </div>
    </section>

    <section class="border-t border-[#e5e7eb]/80 bg-white/60 py-20 px-4 backdrop-blur-sm">
      <div class="container mx-auto">
        <div class="mb-16 text-center">
          <h2 class="mb-3 text-3xl font-bold tracking-tight text-[#1f2937] md:text-4xl">
            核心<span class="bg-gradient-to-r from-[#6366f1] to-[#a855f7] bg-clip-text text-transparent">功能</span>
          </h2>
          <p class="mx-auto max-w-2xl text-[#6b7280]">发帖互动、话题发现、个人主页，一站式社区体验。</p>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
          <div class="user-surface-card p-8 card-hover">
            <div class="mb-6 flex h-14 w-14 items-center justify-center rounded-2xl bg-[#eef2ff] ring-1 ring-[#c7d2fe]">
              <i class="fa fa-comments text-2xl text-[#6366f1]"></i>
            </div>
            <h3 class="text-2xl font-bold mb-3 text-[#1f2937]">即时交流</h3>
            <p class="text-[#6b7280]">极速响应的聊天系统，支持文字、图片、表情，让交流从未如此顺畅</p>
          </div>

          <div class="user-surface-card p-8 card-hover">
            <div class="mb-6 flex h-14 w-14 items-center justify-center rounded-2xl bg-[#fae8ff] ring-1 ring-[#e9d5ff]">
              <i class="fa fa-lightbulb-o text-2xl text-[#a855f7]"></i>
            </div>
            <h3 class="text-2xl font-bold mb-3 text-[#1f2937]">话题探索</h3>
            <p class="text-[#6b7280]">海量热门话题，精准推荐，找到与你志同道合的伙伴，畅聊所想</p>
          </div>

          <div class="user-surface-card p-8 card-hover">
            <div class="w-16 h-16 rounded-full bg-rose-50 flex items-center justify-center mb-6 ring-1 ring-rose-100">
              <i class="fa fa-user-circle-o text-2xl text-[#ec4899]"></i>
            </div>
            <h3 class="text-2xl font-bold mb-3 text-[#1f2937]">个人空间</h3>
            <p class="text-[#6b7280]">专属个性化主页，记录你的每一份精彩，展示独一无二的你</p>
          </div>
        </div>
      </div>
    </section>

    <section class="py-20 px-4">
      <div class="container mx-auto">
        <div class="flex flex-col md:flex-row justify-between items-center mb-12">
          <h2 class="text-4xl font-bold mb-4 md:mb-0 text-[#1f2937]">热门<span class="bg-gradient-to-r from-[#6366f1] to-[#a855f7] bg-clip-text text-transparent">话题</span></h2>
          <button type="button" class="px-6 py-2 rounded-full border border-[#e5e7eb] bg-white text-[#6366f1] font-medium shadow-sm hover:bg-[#f8faff] transition-all" @click="$router.push('/topic')">
            查看全部 <i class="fa fa-angle-right ml-1"></i>
          </button>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          <div v-for="topic in hotTopics" :key="topic.id" class="user-surface-card overflow-hidden card-hover cursor-pointer" @click="filterByTopic(topic)">
            <img
              :src="`https://picsum.photos/seed/topic${topic.id}/600/300`"
              alt=""
              class="h-48 w-full object-cover"
            />
            <div class="p-6">
              <div class="flex justify-between items-center mb-3">
                <span class="px-3 py-1 rounded-full bg-[#eef2ff] text-[#6366f1] text-sm font-medium">{{ topic.name }}</span>
                <span class="text-[#9ca3af] text-sm"><i class="fa fa-eye mr-1"></i>{{ topic.postCount }}</span>
              </div>
              <h3 class="text-xl font-bold mb-2 text-[#1f2937]">#{{ topic.name }}</h3>
              <p class="text-[#6b7280] text-sm mb-4">参与讨论，分享你的观点和想法</p>
              <span class="text-[#6366f1] font-medium">参与讨论 <i class="fa fa-arrow-right ml-1"></i></span>
            </div>
          </div>

          <div v-if="hotTopics.length === 0" class="col-span-1 md:col-span-2 lg:col-span-3 user-surface-card p-12 text-center">
            <i class="fa fa-fire text-4xl text-[#c7d2fe] mb-4"></i>
            <p class="text-[#6b7280]">暂无热门话题，快来创建第一个话题吧~</p>
          </div>
        </div>
      </div>
    </section>

    <section class="py-20 px-4 bg-white/50">
      <div class="container mx-auto">
        <div class="text-center mb-16">
          <h2 class="text-4xl font-bold mb-4 text-[#1f2937]">社区<span class="bg-gradient-to-r from-[#6366f1] to-[#a855f7] bg-clip-text text-transparent">达人</span></h2>
          <p class="text-[#6b7280] max-w-2xl mx-auto">来自各行各业的优秀创作者，分享优质内容，传递思想力量</p>
        </div>

        <div class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-6">
          <div v-for="user in activeUsers" :key="user.id" class="text-center card-hover cursor-pointer user-surface-card py-5 px-3" @click="$router.push(`/user/${user.id}`)">
            <div class="mx-auto mb-4 h-20 w-20 rounded-full bg-gradient-to-br from-indigo-500 to-violet-600 p-0.5">
              <img :src="resolveFileUrl(user.avatar) || 'https://placehold.co/200x200/e0e7ff/6366f1?text=U'" referrerpolicy="no-referrer" class="h-full w-full rounded-full border-2 border-white object-cover" alt="" />
            </div>
            <h3 class="font-bold text-lg text-[#1f2937]">{{ user.nickname || user.username }}</h3>
            <p class="text-[#9ca3af] text-sm">{{ user.postsCount }} 帖子</p>
          </div>

          <div v-if="activeUsers.length === 0" class="col-span-1 md:col-span-2 lg:col-span-5 user-surface-card p-12 text-center">
            <i class="fa fa-users text-4xl text-[#c7d2fe] mb-4"></i>
            <p class="text-[#6b7280]">暂无活跃用户，快来成为第一个社区达人吧~</p>
          </div>
        </div>
      </div>
    </section>

    <section v-if="userStore.isLoggedIn" class="border-t border-[#e5e7eb] py-16 px-4 bg-[#fafbff]">
      <div class="container mx-auto">
        <div class="flex flex-col md:flex-row justify-between items-start md:items-center gap-4 mb-8">
          <div>
            <h2 class="text-2xl md:text-3xl font-bold text-[#1f2937] tracking-tight">为你<span class="bg-gradient-to-r from-[#6366f1] to-[#a855f7] bg-clip-text text-transparent">推荐</span></h2>
            <p class="text-sm text-[#6b7280] mt-1">结合你近期发帖所在板块与互动偏好生成</p>
          </div>
        </div>
        <div v-loading="recLoading" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          <div
            v-for="(post, ridx) in recommendedPosts"
            :key="postRowId(post) || `rec-${ridx}`"
            class="user-surface-card p-5 card-hover cursor-pointer"
            @click="$router.push(`/post/${postRowId(post)}`)"
          >
            <div class="flex items-center gap-2 mb-2">
              <span class="text-[10px] uppercase tracking-wider text-[#6366f1] px-2 py-0.5 rounded bg-[#eef2ff]">推荐</span>
              <span class="text-xs text-[#9ca3af] truncate">{{ post.sectionName || '板块' }}</span>
            </div>
            <h3 class="text-base font-semibold text-[#1f2937] line-clamp-2 leading-snug">{{ post.title }}</h3>
            <p class="text-xs text-[#9ca3af] mt-3 flex items-center gap-2">
              <span>{{ post.authorName }}</span>
              <span>·</span>
              <span>{{ formatTime(post.createTime) }}</span>
            </p>
          </div>
        </div>
        <div v-if="!recLoading && recommendedPosts.length === 0" class="text-center text-[#9ca3af] text-sm py-8">暂无推荐，多发帖、多互动后再来看看</div>
      </div>
    </section>

    <section class="border-t border-[#e5e7eb] bg-white/70 py-20 px-4">
      <div class="container mx-auto">
        <div class="flex flex-col md:flex-row justify-between items-center mb-12">
          <h2 class="text-4xl font-bold mb-4 md:mb-0 text-[#1f2937]">最新<span class="bg-gradient-to-r from-[#6366f1] to-[#a855f7] bg-clip-text text-transparent">帖子</span></h2>
          <button type="button" class="px-6 py-2 rounded-full border border-[#e5e7eb] bg-white text-[#6366f1] font-medium shadow-sm hover:bg-[#f8faff] transition-all" @click="$router.push('/section')">
            查看全部 <i class="fa fa-angle-right ml-1"></i>
          </button>
        </div>

        <div v-loading="loading" class="space-y-6">
          <div v-for="(post, pidx) in posts" :key="postRowId(post) || `lp-${pidx}`" class="user-surface-card overflow-hidden card-hover cursor-pointer" @click="$router.push(`/post/${postRowId(post)}`)">
            <div class="flex flex-col md:flex-row gap-0">
              <div v-if="parsePostImageList(post.images).length" class="w-full md:w-2/5 flex flex-wrap gap-2 p-4 md:p-5 md:pr-0">
                <img
                  v-for="(u, i) in parsePostImageList(post.images).slice(0, 4)"
                  :key="i"
                  :src="u"
                  alt=""
                  class="w-[calc(50%-4px)] h-28 object-cover rounded-lg border border-[#e5e7eb]"
                />
              </div>
              <div class="p-6 flex-1 min-w-0">
                <div class="flex items-center gap-2 mb-2">
                  <span v-if="post.isTop" class="px-1.5 py-0.5 text-xs bg-rose-100 text-rose-700 rounded font-medium">置顶</span>
                  <span v-if="post.isEssence" class="px-1.5 py-0.5 text-xs bg-amber-100 text-amber-800 rounded font-medium">精华</span>
                  <span class="px-1.5 py-0.5 text-xs bg-[#eef2ff] text-[#6366f1] rounded">{{ post.sectionName || '默认' }}</span>
                </div>
                <h3 class="text-xl font-bold mb-3 text-[#1f2937] hover:text-[#6366f1] transition-colors">{{ post.title }}</h3>
                <p v-if="post.summary" class="text-[#6b7280] line-clamp-2 mb-4">{{ post.summary }}</p>
                <div class="flex items-center justify-between text-sm text-[#9ca3af]">
                  <div class="flex items-center gap-3">
                    <img :src="resolveFileUrl(post.authorAvatar) || 'https://placehold.co/40x40/e0e7ff/6366f1?text=U'" referrerpolicy="no-referrer" class="h-6 w-6 rounded-full object-cover ring-1 ring-[#e5e7eb]" />
                    <span class="text-[#4b5563]">{{ post.authorName }}</span>
                    <span>·</span>
                    <span>{{ formatTime(post.createTime) }}</span>
                  </div>
                  <div class="flex items-center gap-4">
                    <span class="flex items-center gap-1"><i class="fa fa-eye mr-1"></i>{{ post.viewCount }}</span>
                    <span class="flex items-center gap-1"><i class="fa fa-heart mr-1"></i>{{ post.likeCount }}</span>
                    <span class="flex items-center gap-1"><i class="fa fa-comment mr-1"></i>{{ post.commentCount }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div v-if="!loading && posts.length === 0" class="text-center py-16 text-[#9ca3af] user-surface-card">
            <i class="fa fa-file-text-o text-4xl mb-4"></i>
            <p>暂无帖子，快来发布第一条吧~</p>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { postApi, topicApi, userApi } from '../../../api'
import type { Post, Topic, UserInfo } from '../../../types'
import { parsePostImageList } from '../../../utils/renderMarkdown'
import { postRowId } from '../../../utils/postRowId'
import { useUserStore } from '../../../store/user'
import { resolveFileUrl } from '../../../utils/resolveFileUrl'

const userStore = useUserStore()
const loading = ref(false)
const recLoading = ref(false)
const recommendedPosts = ref<any[]>([])
const posts = ref<Post[]>([])
const hotTopics = ref<Topic[]>([])
const activeUsers = ref<UserInfo[]>([])

onMounted(async () => {
  await Promise.all([fetchPosts(), fetchHotTopics(), fetchActiveUsers(), fetchRecommended()])
  init3DElement()
})

async function fetchRecommended() {
  if (!userStore.isLoggedIn) return
  recLoading.value = true
  try {
    const res: any = await postApi.recommended(1, 6)
    recommendedPosts.value = (res.records || []).map((p: any) => ({
      ...p,
      id: p.postId,
    }))
  } catch {
    recommendedPosts.value = []
  } finally {
    recLoading.value = false
  }
}

async function fetchPosts() {
  loading.value = true
  try {
    const res: any = await postApi.list({
      sort: 'latest',
      pageNum: 1,
      pageSize: 5
    })
    posts.value = (res.records || []).map((p: any) => ({
      ...p,
      id: p.postId,
      authorId: p.authorId || p.userId,
    }))
  } catch (error) {
    console.error('获取帖子列表失败:', error)
    posts.value = []
  } finally {
    loading.value = false
  }
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
      id: u.userId || u.id,
      postsCount: u.postsCount || 0,
    }))
  } catch {
    activeUsers.value = []
  }
}

function filterByTopic(topic: Topic) {
  // 这里可以实现话题筛选逻辑
  console.log('筛选话题:', topic.name)
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

function init3DElement() {
  // 3D交互元素简化版
  const container = document.getElementById('3d-element')
  if (!container) return

  const canvas = document.createElement('canvas')
  const ctx = canvas.getContext('2d')
  container.appendChild(canvas)

  canvas.width = container.offsetWidth
  canvas.height = container.offsetHeight

  type Ball = { x: number; y: number; radius: number; dx: number; dy: number; color: string }
  const balls: Ball[] = []
  for (let i = 0; i < 30; i++) {
    balls.push({
      x: Math.random() * canvas.width,
      y: Math.random() * canvas.height,
      radius: Math.random() * 20 + 10,
      dx: Math.random() * 2 - 1,
      dy: Math.random() * 2 - 1,
      color: `hsl(${Math.random() * 60 + 250}, 70%, 60%)`
    })
  }

  function animate() {
    requestAnimationFrame(animate)
    if (!ctx) return
    ctx.clearRect(0, 0, canvas.width, canvas.height)

    balls.forEach(ball => {
      ctx.beginPath()
      ctx.arc(ball.x, ball.y, ball.radius, 0, Math.PI * 2)
      ctx.fillStyle = ball.color
      ctx.fill()
      ctx.closePath()

      ball.x += ball.dx
      ball.y += ball.dy

      if (ball.x + ball.radius > canvas.width || ball.x - ball.radius < 0) {
        ball.dx = -ball.dx
      }
      if (ball.y + ball.radius > canvas.height || ball.y - ball.radius < 0) {
        ball.dy = -ball.dy
      }
    })
  }
  animate()

  // 鼠标跟随3D效果
  window.addEventListener('mousemove', (e) => {
    const x = e.clientX / window.innerWidth - 0.5
    const y = e.clientY / window.innerHeight - 0.5
    container.style.transform = `perspective(1000px) rotateY(${x * 20}deg) rotateX(${-y * 20}deg)`
  })
}
</script>

<style scoped>
/* 自定义样式 */
.container {
  max-width: 1280px;
  margin: 0 auto;
}

.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
