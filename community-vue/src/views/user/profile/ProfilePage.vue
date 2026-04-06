<template>
  <div class="min-h-screen bg-[#F5F7FA] pt-20 pb-8">
    <div class="max-w-4xl mx-auto px-4">
      <!-- 个人信息卡 -->
      <div class="bg-white rounded-2xl shadow-sm overflow-hidden mb-6">
        <!-- 背景横幅 -->
        <div class="h-32 bg-gradient-to-r from-blue-500 via-indigo-500 to-purple-600 relative">
          <div class="absolute inset-0 opacity-30"
            style="background-image: url(&quot;data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%23ffffff' fill-opacity='0.15'%3E%3Ccircle cx='30' cy='30' r='2'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E&quot;);">
          </div>
        </div>

        <!-- 头像和信息 -->
        <div class="relative px-6 pb-6">
          <div class="-mt-16 flex items-end gap-5">
            <img :src="userInfo.avatar || defaultAvatar"
              class="w-28 h-28 rounded-2xl object-cover border-4 border-white shadow-lg" />
            <div class="pb-2 flex-1">
              <div class="flex items-center gap-3">
                <h1 class="text-2xl font-bold text-gray-800">{{ userInfo.nickname || userInfo.username }}</h1>
                <span class="px-2.5 py-0.5 bg-blue-50 text-blue-600 text-xs rounded-full font-medium">
                  Lv.{{ level }}
                </span>
              </div>
              <p class="text-gray-500 mt-1 text-sm">@{{ userInfo.username }}</p>
            </div>
            <button @click="showEditDialog"
              class="px-4 py-2 bg-white border border-gray-200 text-gray-700 rounded-lg hover:border-blue-400 hover:text-blue-600 transition-all cursor-pointer text-sm shadow-sm">
              <Edit class="w-4 h-4 inline mr-1" /> 编辑资料
            </button>
          </div>

          <p v-if="userInfo.bio" class="mt-4 text-gray-600 leading-relaxed max-w-2xl">{{ userInfo.bio }}</p>
          <p v-else class="mt-4 text-gray-400 italic text-sm">这个人很懒，什么都没写~</p>

          <!-- 数据统计 -->
          <div class="grid grid-cols-4 gap-4 mt-6 pt-6 border-t border-dashed">
            <div class="text-center">
              <div class="text-2xl font-bold text-gray-800">{{ userInfo.postsCount || 0 }}</div>
              <div class="text-xs text-gray-400 mt-1">帖子</div>
            </div>
            <div class="text-center">
              <div class="text-2xl font-bold text-gray-800">{{ userInfo.followersCount || 0 }}</div>
              <div class="text-xs text-gray-400 mt-1">粉丝</div>
            </div>
            <div class="text-center">
              <div class="text-2xl font-bold text-gray-800">{{ userInfo.followingCount || 0 }}</div>
              <div class="text-xs text-gray-400 mt-1">关注</div>
            </div>
            <div class="text-center">
              <div class="text-2xl font-bold text-blue-500">{{ userInfo.points || 0 }}</div>
              <div class="text-xs text-gray-400 mt-1">积分</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 编辑资料对话框 -->
      <el-dialog v-model="editDialogVisible" title="编辑资料" width="500px" destroy-on-close>
        <el-form :model="editForm" label-width="80px">
          <el-form-item label="头像">
            <div class="flex items-center gap-4">
              <img :src="editForm.avatar || defaultAvatar" class="w-20 h-20 rounded-xl object-cover border border-gray-200" />
              <div>
                <el-button size="small" @click="triggerAvatarUpload" :loading="uploadingAvatar">
                  <Upload class="w-4 h-4 mr-1" />上传头像
                </el-button>
                <input ref="avatarInputRef" type="file" accept="image/*" class="hidden" @change="handleAvatarUpload" />
                <p class="text-xs text-gray-400 mt-1">支持 JPG、PNG 格式</p>
              </div>
            </div>
          </el-form-item>
          <el-form-item label="昵称">
            <el-input v-model="editForm.nickname" placeholder="请输入昵称" />
          </el-form-item>
          <el-form-item label="个人简介">
            <el-input v-model="editForm.bio" type="textarea" :rows="3" placeholder="介绍一下自己" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveProfile" :loading="saving">保存</el-button>
        </template>
      </el-dialog>

      <!-- 签到卡片 -->
      <div class="bg-gradient-to-r from-orange-400 via-pink-500 to-purple-500 rounded-2xl p-6 mb-6 text-white relative overflow-hidden">
        <div class="absolute right-0 top-0 w-32 h-32 bg-white/10 rounded-full -mr-10 -mt-10"></div>
        <div class="absolute left-0 bottom-0 w-24 h-24 bg-white/10 rounded-full -ml-8 -mb-8"></div>
        <div class="relative z-10 flex items-center justify-between">
          <div>
            <h3 class="text-lg font-semibold flex items-center gap-2 mb-1">
              <CalendarCheck class="w-5 h-5" /> 每日签到
            </h3>
            <p class="text-sm text-white/80">
              已连续签到 <strong class="text-yellow-300 text-base">{{ checkinStatus.continuousDays }}</strong> 天
              · 累计 <strong class="text-yellow-300">{{ checkinStatus.totalDays }}</strong> 天
            </p>
          </div>
          <button @click="doCheckin" :disabled="checkinStatus.checkedToday || checkining"
            :class="[
              'px-8 py-3 rounded-xl font-semibold text-sm cursor-pointer transition-all',
              checkinStatus.checkedToday
                ? 'bg-white/20 text-white/70 backdrop-blur-sm cursor-default'
                : 'bg-white text-pink-500 hover:shadow-lg hover:-translate-y-0.5'
            ]">
            {{ checkinStatus.checkedToday ? '今日已签 ✓' : (checkining ? '签到中...' : '立即签到 +5积分') }}
          </button>
        </div>
      </div>

      <!-- 主内容区 -->
      <div class="flex gap-6">
        <!-- 左侧：内容列表 -->
        <div class="flex-1">
          <div class="bg-white rounded-xl shadow-sm overflow-hidden">
            <!-- Tab导航 -->
            <div class="flex border-b border-gray-100">
              <button v-for="tab in contentTabs" :key="tab.key" @click="activeContentTab = tab.key"
                :class="['flex-1 py-4 text-sm font-medium text-center relative transition-colors cursor-pointer',
                  activeContentTab === tab.key ? 'text-blue-600' : 'text-gray-500 hover:text-gray-700']">
                {{ tab.label }}
                <span v-if="activeContentTab === tab.key"
                  class="absolute bottom-0 left-1/4 right-1/4 h-0.5 bg-blue-600 rounded-t"></span>
              </button>
            </div>

            <!-- 内容区域 -->
            <div class="p-5">
              <!-- 我的帖子 -->
              <div v-if="activeContentTab === 'posts'" class="space-y-3">
                <div v-for="post in myPosts" :key="post.id"
                  @click="$router.push(`/post/${post.id}`)"
                  class="group p-4 rounded-lg hover:bg-gray-50 cursor-pointer transition-colors border border-transparent hover:border-gray-100">
                  <h4 class="font-medium text-gray-800 group-hover:text-blue-600 line-clamp-1 mb-1.5">{{ post.title }}</h4>
                  <p class="text-sm text-gray-500 line-clamp-1 mb-2">{{ post.summary || post.content?.substring(0, 80) }}</p>
                  <div class="flex items-center gap-4 text-xs text-gray-400">
                    <span>{{ formatDate(post.createTime) }}</span>
                    <span class="flex items-center gap-1"><Eye class="w-3.5 h-3.5" /> {{ post.viewCount }}</span>
                    <span class="flex items-center gap-1"><Heart class="w-3.5 h-3.5" /> {{ post.likeCount }}</span>
                    <span class="flex items-center gap-1"><MessageCircle class="w-3.5 h-3.5" /> {{ post.commentCount }}</span>
                  </div>
                </div>
                <div v-if="myPosts.length === 0" class="text-center py-12 text-gray-400 text-sm">
                  还没有发布过帖子，去写第一篇吧~
                </div>
              </div>

              <!-- 我的收藏 -->
              <div v-if="activeContentTab === 'collects'" class="space-y-3">
                <div v-for="col in collects" :key="col.id"
                  @click="$router.push(`/post/${col.postId}`)"
                  class="group p-4 rounded-lg hover:bg-gray-50 cursor-pointer transition-colors border border-transparent hover:border-gray-100">
                  <h4 class="font-medium text-gray-800 group-hover:text-blue-600 line-clamp-1">{{ col.postTitle }}</h4>
                  <p class="text-xs text-gray-400 mt-1.5">收藏于 {{ col.collectTime }}</p>
                </div>
                <div v-if="collects.length === 0" class="text-center py-12 text-gray-400 text-sm">
                  还没有收藏过任何帖子
                </div>
              </div>

              <!-- 签到记录 -->
              <div v-if="activeContentTab === 'checkin'" class="space-y-3">
                <div v-for="(record, i) in checkinRecords" :key="i"
                  class="flex items-center justify-between p-3 rounded-lg bg-gray-50">
                  <div class="flex items-center gap-3">
                    <CalendarCheck class="w-5 h-5 text-green-500" />
                    <span class="text-sm text-gray-700">{{ record.date }}</span>
                  </div>
                    <span class="text-sm font-medium text-green-600">+{{ record.points }} 积分</span>
                  </div>
                <div v-if="checkinRecords.length === 0" class="text-center py-12 text-gray-400 text-sm">
                  暂无签到记录
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 右侧边栏 -->
        <div class="w-64 hidden lg:block space-y-4">
          <!-- 成就徽章 -->
          <div class="bg-white rounded-xl p-5 shadow-sm">
            <h3 class="text-sm font-semibold text-gray-700 mb-3 flex items-center gap-2">
              <Award class="w-4 h-4 text-yellow-500" /> 成就徽章
            </h3>
            <div class="grid grid-cols-3 gap-2">
              <div v-for="(badge, i) in badges" :key="i"
                class="aspect-square rounded-xl flex items-center justify-center text-2xl"
                :title="badge.name"
                :style="{ background: badge.bg }">
                {{ badge.icon }}
              </div>
            </div>
          </div>

          <!-- 快捷操作 -->
          <div class="bg-white rounded-xl p-5 shadow-sm">
            <h3 class="text-sm font-semibold text-gray-700 mb-3">快捷操作</h3>
            <div class="space-y-2">
              <router-link to="/post/create"
                class="flex items-center gap-3 p-2.5 rounded-lg hover:bg-blue-50 text-gray-600 hover:text-blue-600 transition-colors text-sm">
                <PenLine class="w-4 h-4" /> 写新帖
              </router-link>
              <router-link to="/message"
                class="flex items-center gap-3 p-2.5 rounded-lg hover:bg-blue-50 text-gray-600 hover:text-blue-600 transition-colors text-sm">
                <Mail class="w-4 h-4" /> 我的消息
              </router-link>
              <router-link to="/section"
                class="flex items-center gap-3 p-2.5 rounded-lg hover:bg-blue-50 text-gray-600 hover:text-blue-600 transition-colors text-sm">
                <LayoutGrid class="w-4 h-4" /> 浏览板块
              </router-link>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { Edit, CalendarCheck, Eye, Heart, MessageCircle, Award, PenLine, Mail, LayoutGrid, Upload } from 'lucide-vue-next'
import { ElMessage } from 'element-plus'
import { userApi, postApi, interactApi, fileApi } from '../../../api'
import type { UserInfo } from '../../../types'

const showEdit = ref(false)
const checkining = ref(false)
const activeContentTab = ref('posts')
const editDialogVisible = ref(false)
const saving = ref(false)
const uploadingAvatar = ref(false)
const avatarInputRef = ref<HTMLInputElement>()
const editForm = reactive({
  nickname: '',
  bio: '',
  avatar: '',
})

function triggerAvatarUpload() {
  avatarInputRef.value?.click()
}

async function handleAvatarUpload(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  
  if (!file.type.startsWith('image/')) {
    ElMessage.error('请选择图片文件')
    return
  }
  
  uploadingAvatar.value = true
  try {
    const formData = new FormData()
    formData.append('file', file)
    const res = await fileApi.upload(formData) as any
    // 后端返回 FileRecord 对象，需要构造访问 URL
    if (res && res.filePath) {
      editForm.avatar = `/api/file/download/${res.id}`
      ElMessage.success('头像上传成功')
    } else {
      // 如果返回格式不对，使用本地预览
      editForm.avatar = URL.createObjectURL(file)
      ElMessage.warning('头像已预览，建议刷新重试')
    }
  } catch (e) {
    ElMessage.error('头像上传失败')
  } finally {
    uploadingAvatar.value = false
    input.value = ''
  }
}

function showEditDialog() {
  editForm.nickname = userInfo.nickname || ''
  editForm.bio = userInfo.bio || ''
  editForm.avatar = userInfo.avatar || ''
  editDialogVisible.value = true
}

async function saveProfile() {
  saving.value = true
  try {
    await userApi.update(editForm) as any
    userInfo.nickname = editForm.nickname
    userInfo.bio = editForm.bio
    userInfo.avatar = editForm.avatar
    editDialogVisible.value = false
    ElMessage.success('资料更新成功')
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

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

const checkinStatus = reactive({
  checkedToday: false,
  continuousDays: 0,
  totalDays: 0,
})

const myPosts = ref<any[]>([])
const collects = ref<any[]>([])
const checkinRecords = ref<any[]>([])

const defaultAvatar = 'https://placehold.co/112x112/E8EAF6/9FA8DA?text=U'

const level = computed(() => {
  const pts = userInfo.points || 0
  if (pts < 100) return 1
  if (pts < 300) return 2
  if (pts < 600) return 3
  if (pts < 1000) return 4
  return 5
})

const contentTabs = [
  { key: 'posts', label: '我的帖子' },
  { key: 'collects', label: '我的收藏' },
  { key: 'checkin', label: '签到记录' },
]

const badges = [
  { name: '初来乍到', icon: '🌟', bg: 'linear-gradient(135deg, #FFE259 0%, #FFA751 100%)' },
  { name: '活跃用户', icon: '🔥', bg: 'linear-gradient(135deg, #F5576C 0%, #f093fb 100%)' },
  { name: '创作者', icon: '✍️', bg: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)' },
  { name: '热心评论', icon: '💬', bg: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)' },
  { name: '社交达人', icon: '🤝', bg: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)' },
  { name: '签到达人', icon: '✅', bg: 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)' },
]

async function doCheckin() {
  if (checkinStatus.checkedToday) return
  checkining.value = true
  try {
    const res = await userApi.checkin() as any
    ElMessage.success(`签到成功！获得${res.points || 5}积分`)
    checkinStatus.checkedToday = true
    checkinStatus.continuousDays++
    checkinStatus.totalDays++
    userInfo.points += res.points || 5
  } catch (e: any) {
    ElMessage.error(e?.message || '签到失败')
  } finally {
    checkining.value = false
  }
}

function formatDate(dateStr: string): string {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  const now = new Date()
  const diff = now.getTime() - d.getTime()
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  if (diff < 604800000) return `${Math.floor(diff / 86400000)}天前`
  return d.toLocaleDateString('zh-CN')
}

onMounted(async () => {
  // 先检查登录状态
  const token = localStorage.getItem('accessToken')
  if (!token) {
    ElMessage.warning('请先登录')
    return
  }
  
  try {
    const [infoRes, statusRes]: any[] = await Promise.all([
      userApi.my(),
      userApi.checkinStatus(),
    ])
    Object.assign(userInfo, infoRes || {})
    if (statusRes) {
      const s = statusRes as any
      checkinStatus.checkedToday = !!s.checkedToday
      checkinStatus.continuousDays = s.continuousDays || 0
      checkinStatus.totalDays = s.totalDays || 0
    }
  } catch (e: any) {
    console.error('获取用户信息失败', e)
    if (e?.response?.status === 401) {
      ElMessage.warning('登录已过期，请重新登录')
    }
    return
  }
  
  // 单独加载收藏列表
  try {
    const collectRes: any = await interactApi.collects(1, 20)
    collects.value = (collectRes?.records || []).map((c: any) => ({
      ...c,
      id: c.id || c.postId,
      postId: c.postId,
      postTitle: c.postTitle || `收藏帖子 #${c.postId}`,
      collectTime: c.createTime ? formatDate(c.createTime) : '',
    }))
  } catch (e) {
    console.error('获取收藏列表失败', e)
  }
})
</script>
