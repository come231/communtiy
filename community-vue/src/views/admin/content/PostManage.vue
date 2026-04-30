<template>
  <div class="space-y-6">
    <!-- 审核流程说明：敏感词拦截 → AI 审核 → 待人工 / 通过 / 拒绝 -->
    <div class="rounded-xl border border-amber-100 bg-amber-50/80 px-4 py-3 text-sm text-amber-900">
      <p class="font-semibold mb-1">帖子发布与审核流程</p>
      <ol class="list-decimal list-inside space-y-0.5 text-amber-800/90">
        <li>用户发帖：先过<strong>敏感词</strong>，再经 <strong>AI 内容审核</strong>。</li>
        <li>AI 判定存疑 → 状态为 <strong>待审核</strong>，需在本页<strong>人工通过/拒绝</strong>。</li>
        <li>人工<strong>通过</strong>后帖子对外展示并同步搜索；<strong>拒绝</strong>须填写原因（已上线帖子可被下架）。</li>
      </ol>
    </div>

    <div class="bg-white rounded-xl border border-gray-100 shadow-sm">
      <!-- 快捷 Tab：人工审核队列 -->
      <div class="border-b border-gray-100">
        <div class="flex flex-wrap">
          <button
            v-for="tab in queueTabs"
            :key="String(tab.status ?? 'all')"
            type="button"
            :class="[
              'px-5 py-3 text-sm font-medium border-b-2 transition-colors',
              statusFilter === tab.status
                ? 'text-primary border-primary'
                : 'text-gray-500 border-transparent hover:text-gray-700',
            ]"
            @click="selectQueueTab(tab.status)"
          >
            {{ tab.label }}
            <span
              v-if="tab.badge != null && tab.badge > 0"
              class="ml-1.5 rounded-full bg-red-500 px-1.5 py-0.5 text-[10px] font-bold text-white"
            >
              {{ tab.badge > 99 ? '99+' : tab.badge }}
            </span>
          </button>
        </div>
      </div>

      <!-- 搜索栏 -->
      <div class="p-5 border-b border-gray-100">
        <div class="flex flex-wrap items-center gap-3">
          <input
            v-model="keyword"
            type="text"
            placeholder="搜索标题/正文..."
            class="px-4 py-2 bg-gray-50 border border-gray-200 rounded-lg text-sm outline-none focus:border-primary w-64"
            @keyup.enter="handleSearch"
          />
          <select
            v-model.number="statusFilterSelect"
            class="px-3 py-2 bg-gray-50 border border-gray-200 rounded-lg text-sm outline-none"
            @change="onStatusSelectChange"
          >
            <option v-for="opt in statusOptions" :key="String(opt.value ?? 'all')" :value="opt.value === undefined ? '' : opt.value">
              {{ opt.label }}
            </option>
          </select>
          <button
            type="button"
            class="px-4 py-2 bg-primary text-white text-sm rounded-lg hover:bg-blue-600 transition-colors"
            @click="handleSearch"
          >
            搜索
          </button>
          <button type="button" class="px-4 py-2 border border-gray-200 text-gray-600 text-sm rounded-lg hover:bg-gray-50" @click="handleReset">
            重置
          </button>
        </div>
      </div>

      <!-- 列表 -->
      <div class="overflow-x-auto">
        <table class="w-full text-sm">
          <thead class="bg-gray-50 border-b text-left text-xs text-gray-500 uppercase tracking-wider">
            <tr>
              <th class="px-5 py-3.5">帖子信息</th>
              <th class="px-5 py-3.5">作者</th>
              <th class="px-5 py-3.5">板块</th>
              <th class="px-5 py-3.5">互动</th>
              <th class="px-5 py-3.5">状态</th>
              <th class="px-5 py-3.5 min-w-[140px]">驳回/备注</th>
              <th class="px-5 py-3.5">操作</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-gray-50">
            <tr v-for="post in posts" :key="postRowId(post)" class="hover:bg-gray-50/50 transition-colors">
              <td class="px-5 py-4">
                <div class="max-w-[280px]">
                  <p class="font-medium text-gray-800 line-clamp-1">{{ post.title }}</p>
                  <p class="text-xs text-gray-400 mt-1 line-clamp-1">{{ post.summary || stripHtml(post.content)?.slice(0, 80) }}</p>
                </div>
              </td>
              <td class="px-5 py-4">
                <div class="flex items-center gap-2">
                  <img :src="post.authorAvatar || defaultAvatar" class="w-8 h-8 rounded-full object-cover bg-gray-100" alt="" />
                  <span class="text-gray-600 text-xs">{{ post.authorName || post.nickname || '未知' }}</span>
                </div>
              </td>
              <td class="px-5 py-4">
                <span class="text-xs bg-gray-100 text-gray-600 px-2 py-1 rounded">{{ post.sectionName || '-' }}</span>
              </td>
              <td class="px-5 py-4">
                <div class="flex gap-3 text-xs text-gray-500">
                  <span>{{ post.viewCount ?? 0 }} 浏览</span>
                  <span>{{ post.likeCount ?? 0 }} 赞</span>
                  <span>{{ post.commentCount ?? 0 }} 评</span>
                </div>
              </td>
              <td class="px-5 py-4">
                <span :class="['text-xs px-2.5 py-1 rounded-full font-medium', getStatusStyle(post.status)]">{{
                  getStatusLabel(post.status)
                }}</span>
              </td>
              <td class="px-5 py-4 text-xs text-gray-600 max-w-[200px]">
                <span v-if="post.rejectReason" class="line-clamp-2 text-orange-700" :title="post.rejectReason">{{ post.rejectReason }}</span>
                <span v-else class="text-gray-300">—</span>
              </td>
              <td class="px-5 py-4">
                <div class="flex flex-wrap items-center gap-2">
                  <button type="button" class="text-primary hover:text-blue-700 text-xs font-medium" @click="viewPost(post)">查看</button>
                  <button
                    v-if="canManualApprove(post.status)"
                    type="button"
                    class="text-green-600 hover:text-green-800 text-xs font-medium"
                    @click="handleApprove(post)"
                  >
                    通过
                  </button>
                  <button
                    v-if="canManualReject(post.status)"
                    type="button"
                    class="text-orange-600 hover:text-orange-800 text-xs font-medium"
                    @click="handleReject(post)"
                  >
                    拒绝
                  </button>
                  <button type="button" class="text-red-500 hover:text-red-700 text-xs font-medium" @click="handleDelete(post)">删除</button>
                </div>
              </td>
            </tr>
            <tr v-if="posts.length === 0">
              <td colspan="7" class="px-5 py-8 text-center text-gray-400">暂无数据</td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- 分页 -->
      <div class="flex items-center justify-between px-5 py-4 border-t border-gray-100">
        <span class="text-sm text-gray-500">共 {{ total }} 条</span>
        <div class="flex items-center gap-2">
          <button
            type="button"
            class="px-3 py-1.5 text-sm border border-gray-200 rounded-lg disabled:opacity-40 hover:bg-gray-50"
            :disabled="currentPage <= 1"
            @click="handlePageChange(currentPage - 1)"
          >
            上一页
          </button>
          <span class="text-sm text-gray-600">{{ currentPage }} / {{ totalPages || 1 }}</span>
          <button
            type="button"
            class="px-3 py-1.5 text-sm border border-gray-200 rounded-lg disabled:opacity-40 hover:bg-gray-50"
            :disabled="currentPage >= totalPages"
            @click="handlePageChange(currentPage + 1)"
          >
            下一页
          </button>
        </div>
      </div>
    </div>

    <!-- 帖子详情 + 人工审核 -->
    <el-dialog v-model="detailVisible" title="帖子详情与人工审核" width="820px" destroy-on-close @closed="onDetailClosed">
      <div v-if="detailLoading" class="py-12 text-center text-gray-400 text-sm">加载中…</div>
      <div v-else-if="currentPost" class="space-y-4">
        <div class="flex flex-wrap items-start justify-between gap-3">
          <h3 class="text-lg font-medium text-gray-900 pr-4">{{ currentPost.title }}</h3>
          <span :class="['text-xs px-2.5 py-1 rounded-full font-medium shrink-0', getStatusStyle(currentPost.status)]">{{
            getStatusLabel(currentPost.status)
          }}</span>
        </div>
        <div class="flex flex-wrap gap-4 text-sm text-gray-500">
          <span>作者: {{ currentPost.authorName || currentPost.nickname || '未知' }}</span>
          <span>板块: {{ currentPost.sectionName || '-' }}</span>
          <span>时间: {{ formatTime(currentPost.createTime) }}</span>
        </div>
        <div v-if="currentPost.rejectReason" class="rounded-lg border border-orange-200 bg-orange-50 px-3 py-2 text-sm text-orange-800">
          <span class="font-medium">驳回原因：</span>{{ currentPost.rejectReason }}
        </div>
        <div class="rounded-lg bg-gray-50 p-4 text-sm text-gray-800 max-h-[360px] overflow-y-auto whitespace-pre-wrap break-words">
          {{ stripHtml(currentPost.content) || '（无正文）' }}
        </div>
        <div class="flex flex-wrap gap-4 text-sm text-gray-500">
          <span>{{ currentPost.viewCount ?? 0 }} 浏览</span>
          <span>{{ currentPost.likeCount ?? 0 }} 赞</span>
          <span>{{ currentPost.commentCount ?? 0 }} 评论</span>
        </div>
      </div>
      <template #footer>
        <div class="flex flex-wrap justify-end gap-2">
          <el-button @click="detailVisible = false">关闭</el-button>
          <el-button
            v-if="currentPost && canManualApprove(currentPost.status)"
            type="success"
            :loading="reviewSubmitting"
            @click="handleApprove(currentPost)"
          >
            人工通过
          </el-button>
          <el-button
            v-if="currentPost && canManualReject(currentPost.status)"
            type="warning"
            :loading="reviewSubmitting"
            @click="handleReject(currentPost)"
          >
            拒绝 / 下架
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi } from '@/api'
import type { AdminPost } from '@/types'

const defaultAvatar = 'https://placehold.co/32x32/409EFF/fff?text=U'

function postRowId(post: AdminPost): number {
  const v = post.id ?? post.postId
  return Number(v)
}

function stripHtml(html: string | undefined): string {
  if (!html) return ''
  return html.replace(/<[^>]+>/g, ' ').replace(/\s+/g, ' ').trim()
}

/** 待审核、已拒绝 → 可人工通过 */
function canManualApprove(status: number): boolean {
  return status === 2 || status === 3
}

/** 待审核、已通过 → 可拒绝（含下架） */
function canManualReject(status: number): boolean {
  return status === 2 || status === 1
}

const keyword = ref('')
const statusFilter = ref<number | undefined>(undefined)
/** 与下拉里空值同步：用空字符串表示「全部」 */
const statusFilterSelect = ref<number | ''>('')

const queueTabs = ref<{ label: string; status: number | undefined; badge?: number }[]>([
  { label: '全部', status: undefined },
  { label: '待人工审核', status: 2, badge: 0 },
  { label: '已通过', status: 1 },
  { label: '已拒绝', status: 3 },
  { label: '已删除', status: 0 },
])

const statusOptions: { label: string; value: number | undefined }[] = [
  { label: '全部状态', value: undefined },
  { label: '待人工审核', value: 2 },
  { label: '已通过', value: 1 },
  { label: '已拒绝', value: 3 },
  { label: '已删除', value: 0 },
]

const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const totalPages = ref(1)
const posts = ref<AdminPost[]>([])
const detailVisible = ref(false)
const currentPost = ref<AdminPost | null>(null)
const detailLoading = ref(false)
const reviewSubmitting = ref(false)

async function loadPosts() {
  try {
    const res: any = await adminApi.posts({
      keyword: keyword.value || undefined,
      status: statusFilter.value,
      page: currentPage.value,
      size: pageSize.value,
    })

    if (res?.records) {
      posts.value = res.records
      total.value = res.total || 0
      totalPages.value = Math.max(1, Math.ceil((res.total || 0) / pageSize.value))
    } else if (Array.isArray(res)) {
      posts.value = res
      total.value = res.length
      totalPages.value = 1
    }
  } catch (e: any) {
    ElMessage.error('加载帖子列表失败')
    console.error(e)
  }
}

/** 待审核数量角标（轻量：多查一条 status=2 的 total） */
async function loadPendingBadge() {
  try {
    const res: any = await adminApi.posts({ status: 2, page: 1, size: 1 })
    const n = Number(res?.total ?? 0)
    const tab = queueTabs.value.find(t => t.status === 2)
    if (tab) tab.badge = n
  } catch {
    /* ignore */
  }
}

function syncSelectFromFilter() {
  statusFilterSelect.value = statusFilter.value === undefined ? '' : statusFilter.value
}

function selectQueueTab(status: number | undefined) {
  statusFilter.value = status
  syncSelectFromFilter()
  currentPage.value = 1
  loadPosts()
}

function onStatusSelectChange() {
  const v = statusFilterSelect.value
  statusFilter.value = v === '' ? undefined : Number(v)
  currentPage.value = 1
  loadPosts()
}

function handleSearch() {
  currentPage.value = 1
  loadPosts()
}

function handleReset() {
  keyword.value = ''
  statusFilter.value = undefined
  syncSelectFromFilter()
  currentPage.value = 1
  loadPosts()
}

function handlePageChange(page: number) {
  if (page < 1 || page > totalPages.value) return
  currentPage.value = page
  loadPosts()
}

function getStatusStyle(status: number): string {
  const s: Record<number, string> = {
    0: 'bg-gray-100 text-gray-600',
    1: 'bg-green-50 text-green-600',
    2: 'bg-yellow-50 text-yellow-700',
    3: 'bg-red-50 text-red-600',
  }
  return s[status] || 'bg-gray-100'
}

function getStatusLabel(status: number): string {
  const l: Record<number, string> = {
    0: '已删除',
    1: '已通过',
    2: '待人工审核',
    3: '已拒绝',
  }
  return l[status] ?? '未知'
}

function formatTime(dateStr?: string): string {
  if (!dateStr) return '-'
  const d = new Date(dateStr)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(
    d.getHours(),
  ).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

async function viewPost(post: AdminPost) {
  currentPost.value = { ...post }
  detailVisible.value = true
  detailLoading.value = true
  try {
    const d = await adminApi.postDetail(postRowId(post))
    currentPost.value = d
  } catch {
    ElMessage.warning('拉取详情失败，已显示列表中的摘要')
  } finally {
    detailLoading.value = false
  }
}

function onDetailClosed() {
  currentPost.value = null
}

async function handleApprove(post: AdminPost) {
  const id = postRowId(post)
  if (!id) {
    ElMessage.error('无效帖子 ID')
    return
  }
  try {
    await ElMessageBox.confirm(`确定人工通过帖子「${post.title}」？通过后将对用户公开展示。`, '人工审核 - 通过', {
      confirmButtonText: '通过',
      cancelButtonText: '取消',
      type: 'success',
    })
    reviewSubmitting.value = true
    await adminApi.reviewPost(id, 1)
    ElMessage.success('已通过审核')
    detailVisible.value = false
    await loadPosts()
    await loadPendingBadge()
  } catch (e: any) {
    if (e !== 'cancel') {
      ElMessage.error(e?.response?.data?.message || e?.message || '操作失败')
    }
  } finally {
    reviewSubmitting.value = false
  }
}

async function handleReject(post: AdminPost) {
  const id = postRowId(post)
  if (!id) {
    ElMessage.error('无效帖子 ID')
    return
  }
  try {
    const { value: reason } = await ElMessageBox.prompt(
      post.status === 1 ? '该帖已上线，拒绝后将下架。请填写原因：' : '请填写拒绝原因（将展示给作者）：',
      '人工审核 - 拒绝',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputPlaceholder: '必填，简要说明原因',
        inputValidator: (v: string) => {
          if (!v || !v.trim()) return '请填写原因'
          return true
        },
      },
    )
    reviewSubmitting.value = true
    await adminApi.reviewPost(id, 3, reason?.trim())
    ElMessage.success('已拒绝')
    detailVisible.value = false
    await loadPosts()
    await loadPendingBadge()
  } catch (e: any) {
    if (e !== 'cancel') {
      ElMessage.error(e?.response?.data?.message || e?.message || '操作失败')
    }
  } finally {
    reviewSubmitting.value = false
  }
}

async function handleDelete(post: AdminPost) {
  const id = postRowId(post)
  try {
    await ElMessageBox.confirm(`确定删除帖子「${post.title}」？此操作将标记为已删除。`, '确认删除', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await adminApi.deletePost(id)
    ElMessage.success('已删除')
    detailVisible.value = false
    await loadPosts()
    await loadPendingBadge()
  } catch (e: any) {
    if (e !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

watch(statusFilter, () => {
  syncSelectFromFilter()
})

onMounted(async () => {
  syncSelectFromFilter()
  await loadPosts()
  await loadPendingBadge()
})
</script>
