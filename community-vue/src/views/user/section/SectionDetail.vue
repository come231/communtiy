<template>
  <div class="min-h-[calc(100vh-4.5rem)] bg-gradient-to-br from-[#f0f4ff] to-[#f8faff] pb-10 pt-6">
    <div class="container mx-auto px-4 max-w-5xl">
      <div v-if="loading" class="user-surface-card p-12 text-center text-[#6b7280]">加载中…</div>

      <template v-else-if="section">
        <div class="user-surface-card p-6 mb-6">
          <div class="flex flex-col lg:flex-row lg:items-start justify-between gap-4">
            <div class="flex items-start gap-4 min-w-0">
              <div
                class="w-16 h-16 rounded-2xl flex items-center justify-center text-xl font-bold text-white shrink-0 shadow-lg"
                :style="{ background: getGradient(sid) }"
              >
                {{ (section.name || '?').charAt(0) }}
              </div>
              <div class="min-w-0">
                <h1 class="text-2xl font-bold text-[#1f2937] mb-1 truncate">{{ section.name }}</h1>
                <p class="text-sm text-[#6b7280] line-clamp-2 leading-relaxed">{{ section.intro || '暂无描述' }}</p>
                <div class="flex flex-wrap gap-3 mt-3 text-sm text-[#4b5563]">
                  <span class="inline-flex items-baseline gap-1 rounded-lg bg-[#f0f4ff] px-2 py-0.5 text-[#6366f1]"
                    ><i class="fa fa-file-text-o opacity-80" />{{ section.postCount ?? 0 }} 帖子</span
                  >
                  <span class="inline-flex items-baseline gap-1 rounded-lg bg-[#f0f4ff] px-2 py-0.5 text-[#6366f1]"
                    ><i class="fa fa-users opacity-80" />{{ memberCount }} 成员</span
                  >
                  <span v-if="moderatorUser" class="inline-flex items-baseline gap-1 text-[#6b7280]"
                    ><i class="fa fa-user-circle-o opacity-80" />版主 {{ moderatorUser.nickname || moderatorUser.username }}</span
                  >
                </div>
              </div>
            </div>

            <div class="flex flex-wrap gap-2 justify-end">
              <button
                v-if="canPost"
                type="button"
                class="btn-gradient inline-flex items-center gap-1.5 px-4 py-2 text-sm"
                @click="goCreatePost"
              >
                <i class="fa fa-plus" />发布新帖
              </button>
              <button
                v-if="canPost"
                type="button"
                class="px-4 py-2 rounded-xl text-sm text-[#6366f1] border border-[#e5e7eb] bg-white hover:bg-[#f8faff]"
                @click="openAttachDialog"
              >
                关联已有帖子
              </button>
              <button
                v-if="canPost && ctx?.moderator"
                type="button"
                class="px-4 py-2 rounded-xl text-sm text-amber-800 bg-amber-50 border border-amber-200 hover:bg-amber-100"
                @click="openApproveDialog"
              >
                审批申请
              </button>
              <button
                type="button"
                class="px-4 py-2 rounded-xl text-sm text-[#1f2937] border border-[#e5e7eb] bg-white hover:bg-[#f9fafb]"
                @click="openMembersDialog"
              >
                成员
              </button>
              <button
                v-if="showJoinButton"
                type="button"
                class="px-4 py-2 rounded-xl text-sm font-medium bg-[#eef2ff] text-[#6366f1] border border-[#c7d2fe] hover:bg-[#e0e7ff]"
                @click="applyJoin"
              >
                申请加入
              </button>
              <span v-if="joinPending" class="px-4 py-2 rounded-xl text-sm text-amber-800 bg-amber-50 border border-amber-200"
                >审核中</span
              >
            </div>
          </div>
          <p v-if="!canPost && userStore.isLoggedIn && memberOnly" class="mt-4 text-xs text-rose-600">
            本板块仅成员可发帖，请先申请加入并通过版主审核。
          </p>
        </div>

        <div class="user-surface-card p-6">
          <div class="flex items-center justify-between mb-4">
            <h2 class="text-lg font-bold text-[#1f2937]">板块帖子</h2>
            <span class="text-sm text-[#9ca3af]">共 {{ total }} 条</span>
          </div>
          <div v-loading="postsLoading" class="space-y-3">
            <div
              v-for="(post, pidx) in posts"
              :key="postRowId(post) || `sd-${pidx}`"
              class="rounded-xl p-4 border border-[#e5e7eb] bg-[#fafbff] hover:border-[#c7d2fe] hover:shadow-md cursor-pointer transition-all"
              @click="$router.push(`/post/${postRowId(post)}`)"
            >
              <h3 class="text-base font-semibold text-[#1f2937] truncate">{{ post.title }}</h3>
              <div class="flex flex-wrap items-center gap-2 mt-2 text-xs text-[#6b7280]">
                <span>{{ post.authorName || '用户' }}</span>
                <span>·</span>
                <span>{{ formatTime(post.createTime) }}</span>
                <span class="ml-auto flex gap-3">
                  <span><i class="fa fa-eye mr-0.5" />{{ post.viewCount ?? 0 }}</span>
                  <span><i class="fa fa-heart mr-0.5" />{{ post.likeCount ?? 0 }}</span>
                </span>
              </div>
            </div>
            <div v-if="!postsLoading && posts.length === 0" class="text-center py-12 text-[#9ca3af] text-sm">暂无帖子</div>
          </div>
          <div v-if="totalPages > 1" class="flex justify-center gap-3 mt-6 pt-4 border-t border-[#e5e7eb]">
            <button
              type="button"
              class="px-4 py-2 text-sm rounded-full border border-[#e5e7eb] bg-white text-[#1f2937] hover:bg-[#f9fafb] disabled:opacity-40"
              :disabled="currentPage <= 1"
              @click="currentPage > 1 && pagePrev()"
            >
              上一页
            </button>
            <span class="text-sm text-[#6b7280] py-2">{{ currentPage }} / {{ totalPages }}</span>
            <button
              type="button"
              class="px-4 py-2 text-sm rounded-full border border-[#e5e7eb] bg-white text-[#1f2937] hover:bg-[#f9fafb] disabled:opacity-40"
              :disabled="currentPage >= totalPages"
              @click="currentPage < totalPages && pageNext()"
            >
              下一页
            </button>
          </div>
        </div>
      </template>

      <!-- 成员 -->
      <el-dialog v-model="membersVisible" title="板块成员" width="520px" destroy-on-close>
        <div v-loading="membersLoading" class="space-y-2 max-h-96 overflow-y-auto">
          <div
            v-for="m in members"
            :key="m.userId"
            class="flex items-center justify-between gap-2 p-3 rounded-xl bg-[#f9fafb] border border-[#e5e7eb]"
          >
            <div class="min-w-0">
              <div class="text-sm font-medium text-[#1f2937] truncate">{{ memberNames[m.userId] || '用户' + m.userId }}</div>
              <div class="text-xs text-[#9ca3af]">角色 {{ roleLabel(m.role) }} · {{ formatTime(m.joinTime) }}</div>
            </div>
            <div class="flex items-center gap-1 shrink-0">
              <el-button size="small" @click="goMessage(m.userId)">私信</el-button>
              <el-button
                v-if="ctx?.moderator && moderatorId !== m.userId"
                type="danger"
                size="small"
                plain
                @click="kick(m.userId)"
                >移出</el-button
              >
            </div>
          </div>
          <div v-if="!membersLoading && members.length === 0" class="text-center text-slate-400 py-8">暂无成员数据</div>
        </div>
      </el-dialog>

      <!-- 审批 -->
      <el-dialog v-model="approveVisible" title="待审加入申请" width="520px" destroy-on-close>
        <div v-loading="pendingLoading" class="space-y-2 max-h-96 overflow-y-auto">
          <div
            v-for="req in pending"
            :key="req.id"
            class="flex items-center justify-between gap-2 p-3 rounded-xl bg-[#f9fafb] border border-[#e5e7eb]"
          >
            <div class="text-sm text-[#1f2937]">用户 {{ req.userId }} {{ applicantNames[req.userId] ? ' · ' + applicantNames[req.userId] : '' }}</div>
            <div class="flex gap-1">
              <el-button size="small" type="primary" @click="approve(req.userId)">通过</el-button>
              <el-button size="small" @click="reject(req.userId)">拒绝</el-button>
            </div>
          </div>
          <div v-if="!pendingLoading && pending.length === 0" class="text-center text-slate-400 py-8">暂无待审申请</div>
        </div>
      </el-dialog>

      <!-- 关联已有帖子 -->
      <el-dialog v-model="attachVisible" title="将我的帖子关联到本板块" width="560px" destroy-on-close @open="loadMyPosts">
        <div v-loading="myPostsLoading" class="space-y-2 max-h-80 overflow-y-auto">
          <div
            v-for="p in myPosts"
            :key="p.postId"
            class="flex items-center justify-between gap-2 p-3 rounded-xl bg-[#f9fafb] border border-[#e5e7eb] cursor-pointer hover:border-[#818cf8]"
            @click="attachPost(p)"
          >
            <span class="text-sm text-[#1f2937] truncate">{{ p.title }}</span>
            <span class="text-xs text-[#6366f1]">点击关联</span>
          </div>
          <div v-if="!myPostsLoading && myPosts.length === 0" class="text-center text-slate-400 py-8">暂无可用帖子</div>
        </div>
      </el-dialog>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { sectionApi, postApi, userApi } from '../../../api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '../../../store/user'
import { postRowId } from '../../../utils/postRowId'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(true)
const postsLoading = ref(false)
const section = ref<any>(null)
const ctx = ref<any>(null)
const moderatorUser = ref<any>(null)
const posts = ref<any[]>([])
const total = ref(0)
const totalPages = ref(1)
const currentPage = ref(1)

const membersVisible = ref(false)
const membersLoading = ref(false)
const members = ref<any[]>([])
const memberNames = ref<Record<number, string>>({})

const approveVisible = ref(false)
const pendingLoading = ref(false)
const pending = ref<any[]>([])
const applicantNames = ref<Record<number, string>>({})

const attachVisible = ref(false)
const myPostsLoading = ref(false)
const myPosts = ref<any[]>([])

const sid = computed(() => Number(section.value?.sectionId ?? section.value?.id ?? route.params.id))

const moderatorId = computed(() => section.value?.moderatorId ?? null)
const memberCount = computed(() => ctx.value?.memberCount ?? 0)
const canPost = computed(() => !!ctx.value?.canPost)
const joinPending = computed(() => !!ctx.value?.joinPending)
const memberOnly = computed(() => section.value?.memberOnlyPost === 1)

const showJoinButton = computed(() => {
  if (!userStore.isLoggedIn) return false
  if (!memberOnly.value) return false
  if (ctx.value?.moderator) return false
  if (ctx.value?.member) return false
  if (joinPending.value) return false
  return true
})

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

async function loadContext() {
  const id = Number(route.params.id)
  loading.value = true
  try {
    const c = await sectionApi.context(id)
    ctx.value = c
    section.value = c.section
    if (moderatorId.value) {
      try {
        moderatorUser.value = await userApi.info(moderatorId.value)
      } catch {
        moderatorUser.value = null
      }
    }
  } catch {
    ElMessage.error('板块不存在')
    section.value = null
  } finally {
    loading.value = false
  }
}

async function loadPosts() {
  const id = Number(route.params.id)
  postsLoading.value = true
  try {
    const res: any = await postApi.list({ sectionId: id, pageNum: currentPage.value, pageSize: 10 })
    const list = res.records || []
    posts.value = list.map((p: any) => ({ ...p, id: p.postId }))
    total.value = res.total ?? list.length
    totalPages.value = Math.max(1, Math.ceil((res.total || 0) / 10))
  } catch {
    posts.value = []
  } finally {
    postsLoading.value = false
  }
}

function pagePrev() {
  currentPage.value--
  loadPosts()
}
function pageNext() {
  currentPage.value++
  loadPosts()
}

function goCreatePost() {
  router.push(`/post/create?sectionId=${sid.value}`)
}

async function applyJoin() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }
  try {
    await sectionApi.applyJoin(sid.value)
    ElMessage.success('已提交申请')
    await loadContext()
  } catch {
    /* interceptor */
  }
}

async function openMembersDialog() {
  membersVisible.value = true
  await reloadMembersList()
}

function roleLabel(role: number) {
  if (role === 2) return '版主'
  if (role === 1) return '管理员'
  return '成员'
}

function goMessage(uid: number) {
  const name = memberNames.value[uid] || ''
  router.push({ path: '/chat', query: { userId: String(uid), nickName: name } })
}

async function kick(uid: number) {
  try {
    await ElMessageBox.confirm('确定将该用户移出板块？', '确认', { type: 'warning' })
    await sectionApi.kickMember(sid.value, uid)
    ElMessage.success('已移出')
    await reloadMembersList()
    await loadContext()
  } catch {
    /* cancel */
  }
}

async function reloadMembersList() {
  membersLoading.value = true
  memberNames.value = {}
  try {
    const res: any = await sectionApi.members(sid.value, 1, 50)
    members.value = res.records || []
    for (const m of members.value) {
      try {
        const u = await userApi.info(m.userId)
        memberNames.value[m.userId] = u.nickname || u.username
      } catch {
        /* ignore */
      }
    }
  } catch {
    members.value = []
  } finally {
    membersLoading.value = false
  }
}

async function openApproveDialog() {
  approveVisible.value = true
  pendingLoading.value = true
  applicantNames.value = {}
  try {
    pending.value = await sectionApi.joinRequests(sid.value)
    for (const r of pending.value) {
      try {
        const u = await userApi.info(r.userId)
        applicantNames.value[r.userId] = u.nickname || u.username
      } catch {
        /* */
      }
    }
  } catch {
    pending.value = []
  } finally {
    pendingLoading.value = false
  }
}

async function approve(uid: number) {
  try {
    await sectionApi.approveJoin(sid.value, uid)
    ElMessage.success('已通过')
    await openApproveDialog()
    await loadContext()
  } catch {
    /* */
  }
}

async function reject(uid: number) {
  try {
    await sectionApi.rejectJoin(sid.value, uid)
    ElMessage.success('已拒绝')
    await openApproveDialog()
  } catch {
    /* */
  }
}

async function loadMyPosts() {
  myPostsLoading.value = true
  try {
    const res: any = await postApi.my(1, 50)
    const cur = sid.value
    const rows = (res.records || []).filter((p: any) => (p.sectionId || 0) !== cur)
    myPosts.value = rows
  } catch {
    myPosts.value = []
  } finally {
    myPostsLoading.value = false
  }
}

function openAttachDialog() {
  if (!canPost.value) {
    ElMessage.warning('无权限在本板块关联帖子')
    return
  }
  attachVisible.value = true
}

async function attachPost(p: any) {
  try {
    const detail = await postApi.detail(p.postId)
    await postApi.update(p.postId, {
      sectionId: sid.value,
      title: detail.title,
      content: detail.content || '',
      images: detail.images,
      video: (detail as any).video,
      isAnonymous: !!detail.isAnonymous,
      tags: (detail as any).tags,
    })
    ElMessage.success('已关联到本板块')
    attachVisible.value = false
    await loadPosts()
    await loadContext()
  } catch {
    ElMessage.error('关联失败')
  }
}

function formatTime(time: string) {
  if (!time) return '-'
  return time.split(' ')[0]
}

onMounted(async () => {
  await loadContext()
  loadPosts()
})
</script>

<style scoped>
.container {
  max-width: 1280px;
  margin: 0 auto;
}
</style>
