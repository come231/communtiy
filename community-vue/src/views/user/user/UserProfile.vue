<template>
  <div class="min-h-screen bg-[#F5F7FA] pt-20 pb-8">
    <div class="max-w-4xl mx-auto px-4">
      <!-- 用户信息头部 -->
      <div class="bg-white rounded-2xl shadow-sm overflow-hidden mb-6">
        <div class="h-28 bg-gradient-to-r from-blue-400 via-cyan-400 to-teal-400 relative">
          <div class="absolute inset-0 opacity-20"
            style="background-image: radial-gradient(circle at 2px 2px, white 1px, transparent 0); background-size: 20px 20px;">
          </div>
        </div>
        <div class="relative px-6 pb-6">
          <div class="-mt-12 flex items-end gap-5">
            <img :src="userInfo.avatar || defaultAvatar"
              class="w-24 h-24 rounded-2xl object-cover border-4 border-white shadow-lg" />
            <div class="pb-1.5">
              <h1 class="text-xl font-bold text-gray-800 flex items-center gap-2">
                {{ userInfo.nickname || userInfo.username }}
                <span v-if="isMe" class="px-2 py-0.5 bg-blue-50 text-blue-500 text-xs rounded-full">我</span>
              </h1>
              <p class="text-gray-500 text-sm mt-0.5">ID: {{ userId }}</p>
            </div>
            <div class="ml-auto pb-1 flex gap-2">
              <button v-if="!isMe" @click="toggleFollow"
                :class="[
                  'px-5 py-2 rounded-lg text-sm font-medium cursor-pointer transition-all',
                  isFollowed
                    ? 'bg-green-50 text-green-600 border border-green-200 hover:bg-green-100'
                    : 'bg-blue-500 text-white hover:bg-blue-600 shadow-md shadow-blue-500/20'
                ]">
                {{ isFollowed ? '已关注' : '+ 关注' }}
              </button>
              <button v-if="!isMe" @click="sendMessage"
                class="px-4 py-2 bg-gray-100 text-gray-600 rounded-lg hover:bg-gray-200 text-sm cursor-pointer transition-colors">
                <Mail class="w-4 h-4 inline mr-1" /> 私信
              </button>
              <router-link v-if="isMe" to="/profile"
                class="px-5 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 shadow-md shadow-blue-500/20 text-sm inline-flex items-center">
                <Edit class="w-4 h-4 inline mr-1" /> 编辑资料
              </router-link>
            </div>
          </div>

          <p v-if="userInfo.bio" class="mt-4 text-gray-600 text-sm leading-relaxed">{{ userInfo.bio }}</p>

          <!-- 统计 -->
          <div class="flex gap-8 mt-5 pt-5 border-t border-dashed">
            <div class="text-center cursor-pointer" @click="showFollowModal('following')">
              <div class="text-lg font-bold text-gray-800">{{ userInfo.followingCount || 0 }}</div>
              <div class="text-xs text-gray-400">关注</div>
            </div>
            <div class="text-center cursor-pointer" @click="showFollowModal('followers')">
              <div class="text-lg font-bold text-gray-800">{{ userInfo.followersCount || 0 }}</div>
              <div class="text-xs text-gray-400">粉丝</div>
            </div>
            <div class="text-center">
              <div class="text-lg font-bold text-gray-800">{{ userInfo.postsCount || 0 }}</div>
              <div class="text-xs text-gray-400">帖子</div>
            </div>
            <div class="text-center">
              <div class="text-lg font-bold text-blue-500">{{ userInfo.points || 0 }}</div>
              <div class="text-xs text-gray-400">积分</div>
            </div>
          </div>
        </div>
      </div>

      <!-- Tab内容 -->
      <div class="bg-white rounded-xl shadow-sm overflow-hidden">
        <div class="flex border-b border-gray-100">
          <button v-for="tab in tabs" :key="tab.key" @click="activeTab = tab.key"
            :class="['flex-1 py-3.5 text-sm font-medium text-center relative cursor-pointer transition-colors',
              activeTab === tab.key ? 'text-blue-600' : 'text-gray-500 hover:text-gray-700']">
            {{ tab.label }}
            <span v-if="activeTab === tab.key"
              class="absolute bottom-0 left-1/4 right-1/4 h-0.5 bg-blue-600 rounded-full"></span>
          </button>
        </div>

        <div class="p-5">
          <!-- 帖子列表 -->
          <div v-if="activeTab === 'posts'" class="space-y-3">
            <div v-for="post in posts" :key="post.id"
              @click="$router.push(`/post/${post.id}`)"
              class="group p-4 rounded-lg hover:bg-gray-50 cursor-pointer transition-colors border border-transparent hover:border-gray-100">
              <div class="flex items-start justify-between mb-2">
                <h4 class="font-medium text-gray-800 group-hover:text-blue-600 line-clamp-1 flex-1 pr-4">{{ post.title }}</h4>
                <span v-if="post.isTop" class="px-2 py-0.5 bg-red-50 text-red-500 text-xs rounded flex-shrink-0">置顶</span>
              </div>
              <p class="text-sm text-gray-500 line-clamp-2 mb-2.5">{{ post.summary || stripHtml(post.content)?.substring(0, 120) }}</p>
              <div class="flex items-center gap-4 text-xs text-gray-400">
                <span>{{ formatTime(post.createTime) }}</span>
                <span><Eye class="w-3.5 h-3.5 inline" /> {{ post.viewCount }}</span>
                <span><Heart class="w-3.5 h-3.5 inline" /> {{ post.likeCount }}</span>
                <span><MessageCircle class="w-3.5 h-3.5 inline" /> {{ post.commentCount }}</span>
              </div>
            </div>
            <div v-if="posts.length === 0" class="text-center py-12 text-gray-400 text-sm">
              该用户还没有发表过帖子
            </div>
          </div>

          <!-- 关注/粉丝列表 -->
          <div v-if="activeTab === 'following' || activeTab === 'followers'" class="space-y-2">
            <div v-for="uid in followList" :key="uid"
              class="flex items-center gap-3 p-3 rounded-lg hover:bg-gray-50 cursor-pointer transition-colors"
              @click="$router.push(`/user/${uid}`)">
              <div class="w-10 h-10 rounded-full bg-gradient-to-br from-blue-400 to-purple-400 flex items-center justify-center text-white text-sm font-medium">
                U{{ uid % 100 }}
              </div>
              <div class="flex-1 min-w-0">
                <div class="text-sm font-medium text-gray-800">用户{{ uid }}</div>
                <div class="text-xs text-gray-400">ID: {{ uid }}</div>
              </div>
              <button v-if="!isMe" class="px-3 py-1 text-xs border border-gray-200 text-gray-600 rounded-full hover:border-blue-400 hover:text-blue-600 cursor-pointer transition-colors">
                查看
              </button>
            </div>
            <div v-if="followList.length === 0" class="text-center py-12 text-gray-400 text-sm">
              暂无数据
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 关注/粉丝弹窗 -->
    <el-dialog v-model="followDialogVisible" :title="followDialogTitle" width="420px" destroy-on-close>
      <div class="space-y-2 max-h-80 overflow-y-auto">
        <div v-for="uid in followList" :key="uid"
          class="flex items-center gap-3 p-2.5 rounded-lg hover:bg-gray-50 cursor-pointer"
          @click="$router.push(`/profile/${uid}`); followDialogVisible = false">
          <div class="w-9 h-9 rounded-full bg-gradient-to-br from-blue-400 to-purple-400 flex items-center justify-center text-white text-xs font-medium">
            U{{ uid % 100 }}
          </div>
          <span class="text-sm text-gray-700">用户{{ uid }}</span>
        </div>
        <div v-if="followList.length === 0" class="text-center py-8 text-gray-400 text-sm">暂无数据</div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Edit, Mail, Eye, Heart, MessageCircle } from 'lucide-vue-next'
import { ElMessage } from 'element-plus'
import { userApi, postApi } from '../../../api'
import type { UserInfo } from '../../../types'
import { useUserStore } from '../../../store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const userId = computed(() => Number(route.params.id) || 0)
const isMe = computed(() => userId.value === Number(userStore.userId) || userId.value === 0)

const userInfo = reactive<UserInfo>({
  id: 0,
  username: '',
  nickname: '',
  avatar: '',
  bio: '',
  followersCount: 0,
  followingCount: 0,
  postsCount: 0,
  points: 0,
})

const activeTab = ref('posts')
const posts = ref<any[]>([])
const followList = ref<number[]>([])
const isFollowed = ref(false)
const followDialogVisible = ref(false)
const followDialogTitle = ref('')
const defaultAvatar = 'https://placehold.co/96x96/E8EAF6/9FA8DA?text=U'

const tabs = [
  { key: 'posts', label: 'TA的帖子' },
  { key: 'following', label: `关注 ${userInfo.followingCount || ''}` },
  { key: 'followers', label: `粉丝 ${userInfo.followersCount || ''}` },
]

function stripHtml(html: string | undefined): string {
  if (!html) return ''
  return html.replace(/<[^>]+>/g, '').replace(/\s+/g, ' ').trim()
}

function formatTime(dateStr: string): string {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  const diff = Date.now() - d.getTime()
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  if (diff < 2592000000) return `${Math.floor(diff / 86400000)}天前`
  return d.toLocaleDateString('zh-CN')
}

async function toggleFollow() {
  if (isFollowed.value) {
    try {
      await userApi.unfollow(userId.value)
      isFollowed.value = false
      userInfo.followersCount = (userInfo.followersCount || 0) - 1
      ElMessage.success('已取消关注')
    } catch { /* handled */ }
  } else {
    try {
      await userApi.follow(userId.value)
      isFollowed.value = true
      userInfo.followersCount = (userInfo.followersCount || 0) + 1
      ElMessage.success('关注成功')
    } catch { /* handled */ }
  }
}

function sendMessage() {
  router.push(`/message?toUserId=${userId.value}&toNickName=${encodeURIComponent(userInfo.nickname || userInfo.username)}`)
}

function showFollowModal(type: string) {
  followDialogTitle.value = type === 'following' ? '关注列表' : '粉丝列表'
  loadFollowList(type)
  followDialogVisible.value = true
}

async function loadFollowList(type: string) {
  try {
    const res: any = type === 'following'
      ? await userApi.following(userId.value)
      : await userApi.followers(userId.value)
    followList.value = Array.isArray(res) ? res : (res?.records || [])
  } catch (e) {
    console.error(e)
  }
}

watch(activeTab, async (val) => {
  if (val === 'following') await loadFollowList('following')
  else if (val === 'followers') await loadFollowList('followers')
})

onMounted(async () => {
  try {
    const targetId = userId.value || Number(userStore.userId)
    const [infoRes, followed]: any[] = await Promise.all([
      targetId ? userApi.info(targetId) : userApi.my(),
      targetId && !isMe.value ? userApi.isFollowing(targetId) : Promise.resolve(false),
    ])
    Object.assign(userInfo, infoRes || {})
    isFollowed.value = followed || false
  } catch (e) {
    console.error(e)
  }
})
</script>
