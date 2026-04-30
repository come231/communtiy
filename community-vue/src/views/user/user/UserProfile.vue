<template>
  <!-- 单根 + 通栏用成对 div：避免自闭合 div 与路由 transition 在部分环境下的异常；与 Profile / AI 等页一致 -->
  <div class="user-public-profile-route">
  <div class="min-h-screen bg-gradient-to-br from-[#f0f4ff] via-[#f8faff] to-[#f0f4ff] text-[#1f2937]">
    <div
      class="w-full h-[200px] md:h-[240px] bg-gradient-to-r from-[#6366f1] via-[#a855f7] to-[#ec4899] shadow-[0_8px_30px_rgba(99,102,241,0.2)]"
    ></div>

    <div class="max-w-[1000px] mx-auto px-5 relative z-10 pb-10" style="margin-top: -5rem">
      <div
        class="bg-white rounded-[24px] p-6 md:p-8 shadow-[0_10px_30px_-5px_rgba(99,102,241,0.15)] transition-all duration-300 hover:shadow-[0_20px_40px_-5px_rgba(99,102,241,0.25)]"
      >
        <div
          class="flex flex-col sm:flex-row flex-wrap items-center justify-center text-center sm:text-left gap-6 md:gap-8 mb-6"
        >
          <img
            :src="resolveFileUrl(userInfo.avatar) || defaultAvatar"
            referrerpolicy="no-referrer"
            class="w-[140px] h-[140px] rounded-full object-cover border-[6px] border-white shadow-[0_0_0_4px_#818cf8] shrink-0 bg-white"
          />
          <div class="flex-1 min-w-0 w-full sm:w-auto">
            <h1
              class="text-[28px] md:text-[32px] font-bold bg-gradient-to-r from-[#6366f1] to-[#a855f7] bg-clip-text text-transparent mb-2 flex flex-wrap items-center justify-center sm:justify-start gap-2"
            >
              {{ userInfo.nickname || userInfo.username }}
              <span v-if="isMe" class="text-sm px-3 py-0.5 bg-[#f0f4ff] text-[#6366f1] rounded-full font-semibold">我</span>
            </h1>
            <p class="text-[14px] text-[#9ca3af] mb-1">ID: {{ userId }}</p>
            <p v-if="userInfo.bio" class="text-[16px] text-[#6b7280] mt-3 leading-relaxed">
              {{ userInfo.bio }}
            </p>
            <p v-else class="text-[16px] text-[#9ca3af] mt-3">暂无简介</p>
          </div>
        </div>

        <div class="flex justify-center flex-wrap gap-8 md:gap-10 my-4">
          <button
            type="button"
            class="text-center cursor-pointer transition-all duration-300 hover:-translate-y-1"
            @click="loadFollowingList"
          >
            <div class="text-[26px] font-bold text-[#6366f1]">{{ userInfo.followingCount || 0 }}</div>
            <div class="text-[14px] text-[#6b7280] mt-1">关注</div>
          </button>
          <button
            type="button"
            class="text-center cursor-pointer transition-all duration-300 hover:-translate-y-1"
            @click="loadFollowersList"
          >
            <div class="text-[26px] font-bold text-[#6366f1]">{{ userInfo.followersCount || 0 }}</div>
            <div class="text-[14px] text-[#6b7280] mt-1">粉丝</div>
          </button>
          <div class="text-center">
            <div class="text-[26px] font-bold text-[#6366f1]">{{ userInfo.postsCount || 0 }}</div>
            <div class="text-[14px] text-[#6b7280] mt-1">帖子</div>
          </div>
          <div class="text-center">
            <div class="text-[26px] font-bold text-[#ec4899]">{{ userInfo.points || 0 }}</div>
            <div class="text-[14px] text-[#6b7280] mt-1">积分</div>
          </div>
        </div>

        <div class="flex flex-wrap gap-3 md:gap-4 justify-center mt-2">
          <router-link
            v-if="isMe"
            to="/profile"
            class="px-7 py-3 rounded-[14px] font-semibold text-[15px] bg-gradient-to-r from-[#6366f1] to-[#a855f7] text-white transition-all duration-300 hover:scale-105 hover:shadow-[0_8px_20px_rgba(99,102,241,0.3)] inline-flex items-center gap-2"
          >
            <i class="fa fa-edit" /> 我的主页
          </router-link>
          <template v-if="!isMe">
            <button
              type="button"
              :class="[
                'px-7 py-3 rounded-[14px] font-semibold text-[15px] transition-all duration-300 inline-flex items-center gap-2',
                isFollowed
                  ? 'border-2 border-[#6366f1] text-[#6366f1] bg-transparent hover:bg-[#6366f1] hover:text-white'
                  : 'bg-gradient-to-r from-[#6366f1] to-[#a855f7] text-white hover:scale-105 hover:shadow-[0_8px_20px_rgba(99,102,241,0.3)]',
              ]"
              @click="toggleFollow"
            >
              <i :class="['fa', isFollowed ? 'fa-check' : 'fa-plus']" />
              {{ isFollowed ? '已关注' : '关注 TA' }}
            </button>
            <button
              type="button"
              class="px-7 py-3 rounded-[14px] font-semibold text-[15px] bg-transparent border-2 border-[#6366f1] text-[#6366f1] transition-all duration-300 hover:bg-[#6366f1] hover:text-white inline-flex items-center gap-2"
              @click="sendMessage"
            >
              <i class="fa fa-comment" /> 发私信
            </button>
          </template>
        </div>
      </div>

      <div class="flex gap-2.5 my-6 md:my-8 flex-wrap justify-center">
        <button
          v-for="tab in visibleTabs"
          :key="tab.key"
          type="button"
          class="px-5 py-3 rounded-[14px] font-semibold text-[15px] transition-all duration-300 shadow-[0_4px_10px_rgba(0,0,0,0.05)]"
          :class="
            activeTab === tab.key
              ? 'bg-gradient-to-r from-[#6366f1] to-[#a855f7] text-white shadow-md'
              : 'bg-white text-[#6b7280] hover:bg-[#f0f4ff] hover:text-[#6366f1] hover:-translate-y-0.5'
          "
          @click="activeTab = tab.key"
        >
          {{ tab.label }}
        </button>
      </div>

      <div class="min-h-[200px]">
        <div v-if="activeTab === 'posts'" class="space-y-4 animate-fade">
          <div
            v-for="(post, pidx) in posts"
            :key="postRowId(post) || `up-${pidx}`"
            class="bg-white rounded-[18px] p-5 md:p-[22px] shadow-[0_10px_30px_-5px_rgba(99,102,241,0.15)] transition-all duration-300 hover:-translate-y-1 hover:shadow-[0_20px_40px_-5px_rgba(99,102,241,0.25)] cursor-pointer"
            @click="$router.push(`/post/${postRowId(post)}`)"
          >
            <div class="flex items-start justify-between gap-2 mb-2">
              <h4 class="font-semibold text-[19px] text-[#1f2937] line-clamp-2 pr-2">{{ post.title }}</h4>
              <span v-if="post.isTop" class="px-2 py-0.5 bg-rose-100 text-rose-600 text-xs rounded-full shrink-0">置顶</span>
            </div>
            <p class="text-[13px] text-[#6b7280] mb-3 flex flex-wrap gap-x-4 gap-y-1">
              <span>{{ formatTime(post.createTime) }}</span>
            </p>
            <div class="flex flex-wrap gap-5 text-[14px] text-[#6b7280]">
              <span><i class="fa fa-eye mr-1" /> {{ post.viewCount ?? 0 }}</span>
              <span><i class="fa fa-comment mr-1" /> {{ post.commentCount ?? 0 }}</span>
              <span class="text-[#ec4899]"><i class="fa fa-heart mr-1" /> {{ post.likeCount ?? 0 }}</span>
            </div>
          </div>
          <div v-if="posts.length === 0" class="text-center py-12 text-[#6b7280] text-sm bg-white rounded-[18px] shadow-[0_4px_10px_rgba(0,0,0,0.05)]">
            暂无帖子
          </div>
        </div>

        <div v-if="activeTab === 'collects'" class="space-y-4 animate-fade">
          <div
            v-for="item in collects"
            :key="item.id || item.postId"
            class="bg-white rounded-[18px] p-5 md:p-[22px] shadow-[0_10px_30px_-5px_rgba(99,102,241,0.15)] transition-all duration-300 hover:-translate-y-1 hover:shadow-[0_20px_40px_-5px_rgba(99,102,241,0.25)] cursor-pointer"
            @click="$router.push(`/post/${item.postId}`)"
          >
            <h4 class="font-semibold text-[19px] text-[#1f2937] mb-2">{{ item.postTitle || '未命名帖子' }}</h4>
            <p class="text-[13px] text-[#6b7280]">收藏于 {{ formatTime(item.createTime) }}</p>
          </div>
          <div v-if="collects.length === 0" class="text-center py-12 text-[#6b7280] text-sm bg-white rounded-[18px] shadow-[0_4px_10px_rgba(0,0,0,0.05)]">
            暂无收藏
          </div>
        </div>
      </div>
    </div>

    <el-dialog v-model="unfollowDialogVisible" title="取消关注" width="350px" class="!rounded-[18px]">
      <p class="text-[#4b5563]">确定要取消关注 {{ userInfo.nickname || userInfo.username }} 吗？</p>
      <template #footer>
        <el-button @click="unfollowDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmUnfollow">确认取消</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showFollowingDialog" title="关注列表" width="500px" class="!rounded-[18px]">
      <div class="space-y-2 max-h-96 overflow-y-auto">
        <div
          v-for="user in followingList"
          :key="user.id"
          class="flex items-center justify-between p-3 rounded-[14px] hover:bg-[#f0f4ff] cursor-pointer border border-[#e5e7eb] transition-all"
          @click="$router.push(`/user/${user.id}`)"
        >
          <div class="flex items-center gap-3">
            <img
              :src="resolveFileUrl(user.avatar) || defaultAvatar"
              referrerpolicy="no-referrer"
              class="w-10 h-10 rounded-full object-cover"
            />
            <div>
              <div class="font-medium text-[#1f2937]">{{ user.nickname || user.username }}</div>
              <div class="text-xs text-[#9ca3af]">@{{ user.username }}</div>
            </div>
          </div>
          <button
            v-if="isMe"
            type="button"
            class="text-sm text-rose-600 hover:text-rose-700"
            @click.stop="handleUnfollow(user.id)"
          >
            取消关注
          </button>
        </div>
        <div v-if="followingList.length === 0" class="text-center py-8 text-[#9ca3af] text-sm">暂无关注</div>
      </div>
    </el-dialog>

    <el-dialog v-model="showFollowersDialog" title="粉丝列表" width="500px" class="!rounded-[18px]">
      <div class="space-y-2 max-h-96 overflow-y-auto">
        <div
          v-for="user in followersList"
          :key="user.id"
          class="flex items-center justify-between p-3 rounded-[14px] hover:bg-[#f0f4ff] cursor-pointer border border-[#e5e7eb] transition-all"
          @click="$router.push(`/user/${user.id}`)"
        >
          <div class="flex items-center gap-3">
            <img
              :src="resolveFileUrl(user.avatar) || defaultAvatar"
              referrerpolicy="no-referrer"
              class="w-10 h-10 rounded-full object-cover"
            />
            <div>
              <div class="font-medium text-[#1f2937]">{{ user.nickname || user.username }}</div>
              <div class="text-xs text-[#9ca3af]">@{{ user.username }}</div>
            </div>
          </div>
        </div>
        <div v-if="followersList.length === 0" class="text-center py-8 text-[#9ca3af] text-sm">暂无粉丝</div>
      </div>
    </el-dialog>
  </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { userApi, postApi, interactApi } from '../../../api'
import type { UserInfo } from '../../../types'
import { postRowId } from '../../../utils/postRowId'
import { useUserStore } from '../../../store/user'
import { resolveFileUrl } from '../../../utils/resolveFileUrl'

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
const collects = ref<any[]>([])
const isFollowed = ref(false)
const unfollowDialogVisible = ref(false)
const showFollowingDialog = ref(false)
const showFollowersDialog = ref(false)
const followingList = ref<any[]>([])
const followersList = ref<any[]>([])
const defaultAvatar = 'https://placehold.co/112x112/E8EAF6/9FA8DA?text=U'

const visibleTabs = computed(() => {
  return [
    { key: 'posts', label: '全部帖子' },
    { key: 'collects', label: '收藏夹' },
  ]
})

function formatTime(dateStr: string): string {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  const diff = Date.now() - d.getTime()
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  if (diff < 2592000000) return `${Math.floor(diff / 86400000)}天前`
  return d.toLocaleDateString('zh-CN')
}

function toggleFollow() {
  if (isFollowed.value) {
    unfollowDialogVisible.value = true
  } else {
    doFollow()
  }
}

async function doFollow() {
  try {
    await userApi.follow(userId.value)
    isFollowed.value = true
    userInfo.followersCount = (userInfo.followersCount || 0) + 1
    ElMessage.success('关注成功')
  } catch {
    /* handled */
  }
}

async function confirmUnfollow() {
  try {
    await userApi.unfollow(userId.value)
    isFollowed.value = false
    userInfo.followersCount = Math.max(0, (userInfo.followersCount || 0) - 1)
    ElMessage.success('已取消关注')
    unfollowDialogVisible.value = false
  } catch {
    ElMessage.error('取消关注失败')
  }
}

function sendMessage() {
  router.push({
    path: '/chat',
    query: { userId: String(userId.value), nickName: userInfo.nickname || userInfo.username || '' },
  })
}

async function loadPosts() {
  try {
    const res: any = await postApi.list({ pageNum: 1, pageSize: 20 })
    const allPosts = res?.records || []
    posts.value = allPosts.filter((p: any) => p.userId === userId.value || p.authorId === userId.value)
  } catch (e) {
    console.error(e)
  }
}

async function loadCollects() {
  try {
    const res: any = await interactApi.collects(1, 30)
    const raw = Array.isArray(res) ? res : (res?.records || [])
    collects.value = raw.map((c: any) => ({
      ...c,
      id: c.id || c.postId,
      postTitle: c.postTitle && String(c.postTitle).trim() ? c.postTitle : '未命名帖子',
    }))
  } catch (e) {
    console.error(e)
  }
}

async function loadFollowingList() {
  try {
    const res: any = await userApi.following(userId.value)
    followingList.value = Array.isArray(res) ? res : []
    showFollowingDialog.value = true
  } catch (e) {
    console.error('加载关注列表失败', e)
    ElMessage.error('加载关注列表失败')
  }
}

async function loadFollowersList() {
  try {
    const res: any = await userApi.followers(userId.value)
    followersList.value = Array.isArray(res) ? res : []
    showFollowersDialog.value = true
  } catch (e) {
    console.error('加载粉丝列表失败', e)
    ElMessage.error('加载粉丝列表失败')
  }
}

async function handleUnfollow(unfollowUserId: number) {
  try {
    await userApi.unfollow(unfollowUserId)
    ElMessage.success('已取消关注')
    followingList.value = followingList.value.filter(u => u.id !== unfollowUserId)
    userInfo.followingCount = Math.max(0, (userInfo.followingCount || 0) - 1)
  } catch (e) {
    ElMessage.error('取消关注失败')
  }
}

watch(activeTab, async (val) => {
  if (val === 'posts') await loadPosts()
  else if (val === 'collects') await loadCollects()
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
    await loadPosts()
  } catch (e) {
    console.error(e)
  }
})
</script>

<style scoped>
@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}
.animate-fade {
  animation: fadeIn 0.5s ease;
}
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
