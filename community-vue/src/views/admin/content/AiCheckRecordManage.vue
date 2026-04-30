<template>
  <div class="space-y-6">
    <!-- 说明与统计 -->
    <div class="rounded-xl border border-slate-100 bg-slate-50/80 px-4 py-3 text-sm text-slate-700">
      <div class="flex flex-wrap items-start justify-between gap-3">
        <div>
          <p class="font-semibold mb-1">说明</p>
          <p class="text-slate-600">
            <strong>审核原文</strong>为当时送审的标题+正文拼接文本；<strong>命中规则</strong>为内置规则或向量语义匹配；<strong>违规度</strong>与
            <strong>AI 建议</strong>来自模型输出。
          </p>
          <p class="text-xs text-slate-500 mt-2">
            💡 人工复核后点击「纠正」可驱动数据飞轮：确认违规的样本会自动进入向量库，系统越用越准。
          </p>
        </div>
        <div class="text-right">
          <p class="text-xs text-slate-500">向量样本库</p>
          <p class="text-lg font-semibold text-primary">{{ vectorStats.totalSize || 0 }}</p>
          <p class="text-xs text-slate-400">条历史违规样本</p>
        </div>
      </div>
    </div>

    <!-- 筛选栏 -->
    <div class="bg-white rounded-xl p-5 border border-gray-100 shadow-sm">
      <div class="flex flex-wrap items-center gap-3">
        <input
          v-model.number="filterUserId"
          type="number"
          placeholder="用户 ID"
          class="px-4 py-2 bg-gray-50 border border-gray-200 rounded-lg text-sm outline-none focus:border-primary w-36"
          @keyup.enter="load"
        />
        <input
          v-model.number="filterPostId"
          type="number"
          placeholder="帖子 ID"
          class="px-4 py-2 bg-gray-50 border border-gray-200 rounded-lg text-sm outline-none focus:border-primary w-36"
          @keyup.enter="load"
        />
        <select v-model.number="filterResult" class="px-3 py-2 bg-gray-50 border border-gray-200 rounded-lg text-sm outline-none">
          <option :value="undefined">全部结果</option>
          <option :value="0">正常</option>
          <option :value="1">转人工</option>
          <option :value="2">违规拦截</option>
        </select>
        <select v-model.number="filterManualStatus" class="px-3 py-2 bg-gray-50 border border-gray-200 rounded-lg text-sm outline-none">
          <option :value="undefined">全部复核状态</option>
          <option :value="0">未复核</option>
          <option :value="1">已确认正确</option>
          <option :value="2">纠正为违规</option>
          <option :value="3">纠正为正常</option>
        </select>
        <button type="button" class="px-4 py-2 bg-primary text-white text-sm rounded-lg hover:bg-blue-600" @click="load">查询</button>
        <button type="button" class="px-4 py-2 border border-gray-200 text-gray-600 text-sm rounded-lg hover:bg-gray-50" @click="reset">重置</button>
      </div>
    </div>

    <!-- 记录列表 -->
    <div class="space-y-4">
      <div
        v-for="row in records"
        :key="row.id"
        class="bg-white rounded-xl border border-gray-100 shadow-sm overflow-hidden"
      >
        <!-- 头部信息 -->
        <div class="flex flex-wrap items-start justify-between gap-3 px-5 py-4 border-b border-gray-50 bg-gray-50/40">
          <div class="flex flex-wrap items-center gap-2 text-sm">
            <span class="font-mono text-gray-500">#{{ row.id }}</span>
            <span class="text-gray-400">|</span>
            <span>用户 {{ row.userId ?? '—' }}</span>
            <span class="text-gray-400">|</span>
            <span>{{ contentTypeLabel(row.contentType) }}</span>
            <span class="text-gray-400">|</span>
            <span class="text-gray-500 text-xs">{{ formatTime(row.createTime) }}</span>
            <!-- 人工复核状态标记 -->
            <span v-if="row.manualReviewStatus" :class="['text-xs px-2 py-0.5 rounded-full', manualStatusClass(row.manualReviewStatus)]">
              {{ manualStatusLabel(row.manualReviewStatus) }}
            </span>
          </div>
          <div class="flex flex-wrap items-center gap-2">
            <span :class="['text-xs px-2.5 py-1 rounded-full font-medium', resultClass(row.checkResult)]">
              {{ resultLabel(row.checkResult) }}
            </span>
            <span class="text-xs text-gray-600 tabular-nums">违规度 {{ formatViolation(row.violationPercent) }}</span>
            <template v-if="row.contentType === 1 && row.contentId">
              <a :href="`/post/${row.contentId}`" target="_blank" class="text-xs text-primary hover:underline"> 帖子 {{ row.contentId }} ↗ </a>
            </template>
            <span v-else class="text-xs text-gray-400">未绑定帖子</span>
          </div>
        </div>

        <!-- 内容区 -->
        <div class="px-5 py-4 space-y-3">
          <div>
            <p class="text-xs font-semibold text-gray-500 uppercase tracking-wide mb-1.5">审核原文</p>
            <p class="text-sm text-gray-800 whitespace-pre-wrap break-words rounded-lg bg-slate-50 border border-slate-100 px-3 py-2 max-h-40 overflow-y-auto">
              {{ row.originalContent?.trim() || '（无文本，可能为纯媒体或未记录）' }}
            </p>
          </div>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-3">
            <div>
              <p class="text-xs font-semibold text-gray-500 uppercase tracking-wide mb-1">命中规则 / 语义匹配</p>
              <p class="text-sm text-amber-900/90 bg-amber-50/80 border border-amber-100 rounded-lg px-3 py-2 min-h-[2.75rem]">
                {{ row.sensitiveWords?.trim() || '—' }}
              </p>
            </div>
            <div>
              <p class="text-xs font-semibold text-gray-500 uppercase tracking-wide mb-1">AI 建议 / 处置说明</p>
              <p class="text-sm text-slate-800 bg-blue-50/60 border border-blue-100 rounded-lg px-3 py-2 min-h-[2.75rem]">
                {{ row.aiSuggestion?.trim() || '—' }}
              </p>
            </div>
          </div>

          <!-- 人工复核区 -->
          <div v-if="row.manualReviewStatus" class="border-t border-gray-100 pt-3 mt-3">
            <p class="text-xs font-semibold text-gray-500 uppercase tracking-wide mb-1">人工复核记录</p>
            <p class="text-sm text-gray-700">
              <span class="font-medium">{{ manualStatusLabel(row.manualReviewStatus) }}</span>
              <span v-if="row.manualLabel" class="text-primary ml-2">标签：{{ row.manualLabel }}</span>
              <span v-if="row.manualRemark" class="text-gray-500 ml-2">备注：{{ row.manualRemark }}</span>
              <span class="text-gray-400 text-xs ml-2">{{ formatTime(row.reviewTime) }}</span>
            </p>
          </div>

          <!-- 操作按钮 -->
          <div class="flex flex-wrap gap-2 pt-2">
            <button
              type="button"
              class="px-3 py-1.5 text-xs bg-green-50 text-green-700 border border-green-200 rounded-lg hover:bg-green-100"
              @click="openFeedbackDialog(row, 1)"
              :disabled="row.manualReviewStatus === 1"
            >
              ✓ 确认AI正确
            </button>
            <button
              type="button"
              class="px-3 py-1.5 text-xs bg-red-50 text-red-700 border border-red-200 rounded-lg hover:bg-red-100"
              @click="openFeedbackDialog(row, 2)"
              :disabled="row.manualReviewStatus === 2"
            >
              ✗ 纠正为违规
            </button>
            <button
              type="button"
              class="px-3 py-1.5 text-xs bg-gray-50 text-gray-700 border border-gray-200 rounded-lg hover:bg-gray-100"
              @click="openFeedbackDialog(row, 3)"
              :disabled="row.manualReviewStatus === 3"
            >
              ○ 纠正为正常
            </button>
          </div>
        </div>
      </div>

      <div v-if="records.length === 0" class="bg-white rounded-xl border border-gray-100 py-16 text-center text-gray-400 text-sm">暂无数据</div>
    </div>

    <!-- 分页 -->
    <div class="flex items-center justify-between px-1">
      <span class="text-sm text-gray-500">共 {{ total }} 条</span>
      <div class="flex items-center gap-2">
        <button
          type="button"
          :disabled="currentPage <= 1"
          class="px-3 py-1.5 text-sm border border-gray-200 rounded-lg disabled:opacity-40 hover:bg-gray-50 bg-white"
          @click="goPage(currentPage - 1)"
        >
          上一页
        </button>
        <span class="text-sm text-gray-600">{{ currentPage }} / {{ totalPages || 1 }}</span>
        <button
          type="button"
          :disabled="currentPage >= totalPages"
          class="px-3 py-1.5 text-sm border border-gray-200 rounded-lg disabled:opacity-40 hover:bg-gray-50 bg-white"
          @click="goPage(currentPage + 1)"
        >
          下一页
        </button>
      </div>
    </div>

    <!-- 人工复核弹窗 -->
    <el-dialog v-model="feedbackDialogVisible" title="人工复核" width="480px" destroy-on-close>
      <div class="space-y-4">
        <div>
          <p class="text-sm text-gray-600 mb-2">操作：{{ feedbackActionText }}</p>
          <p class="text-xs text-gray-500">审核原文前100字：</p>
          <p class="text-sm text-gray-800 bg-slate-50 border border-slate-100 rounded-lg px-3 py-2 mt-1">
            {{ currentRecord?.originalContent?.substring(0, 100) }}...
          </p>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">标签分类</label>
          <el-select v-model="feedbackForm.label" class="w-full" placeholder="选择标签（纠正为违规时必填）">
            <el-option label="严重违规-危化DIY" value="严重违规-危化DIY" />
            <el-option label="严重违规-校园霸凌" value="严重违规-校园霸凌" />
            <el-option label="严重违规-暴力教唆" value="严重违规-暴力教唆" />
            <el-option label="严重违规-色情低俗" value="严重违规-色情低俗" />
            <el-option label="严重违规-诈骗灰产" value="严重违规-诈骗灰产" />
            <el-option label="误判-正常讨论" value="误判-正常讨论" />
            <el-option label="误判-口语化误触" value="误判-口语化误触" />
            <el-option label="其他" value="其他" />
          </el-select>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">备注</label>
          <el-input v-model="feedbackForm.remark" type="textarea" :rows="2" placeholder="补充说明..." />
        </div>
      </div>
      <template #footer>
        <el-button @click="feedbackDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitFeedback" :loading="feedbackSubmitting">
          确认提交
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { adminApi } from '../../../api'
import type { AiCheckRecord } from '../../../types'

const pageSize = 10
const records = ref<AiCheckRecord[]>([])
const total = ref(0)
const currentPage = ref(1)
const vectorStats = ref({ totalSize: 0, description: '' })

const filterUserId = ref<number | undefined>(undefined)
const filterPostId = ref<number | undefined>(undefined)
const filterResult = ref<number | undefined>(undefined)
const filterManualStatus = ref<number | undefined>(undefined)

const totalPages = computed(() => Math.ceil(total.value / pageSize) || 1)

// 人工复核弹窗
const feedbackDialogVisible = ref(false)
const feedbackSubmitting = ref(false)
const currentRecord = ref<AiCheckRecord | null>(null)
const currentFeedbackStatus = ref<number>(0)
const feedbackForm = ref({ label: '', remark: '' })

const feedbackActionText = computed(() => {
  switch (currentFeedbackStatus.value) {
    case 1: return '确认AI判定正确（样本有效，不修改判定）'
    case 2: return '纠正为违规（将触发数据飞轮，样本入库）'
    case 3: return '纠正为正常（AI误判，放行）'
    default: return ''
  }
})

function contentTypeLabel(t?: number) {
  if (t === 1) return '帖子'
  if (t === 2) return '评论'
  if (t === 3) return '私信'
  return '—'
}

function resultLabel(r?: number) {
  if (r === 0) return '正常'
  if (r === 1) return '转人工'
  if (r === 2) return '违规拦截'
  return '—'
}

function resultClass(r?: number) {
  if (r === 0) return 'bg-green-50 text-green-700'
  if (r === 1) return 'bg-amber-50 text-amber-800'
  if (r === 2) return 'bg-red-50 text-red-700'
  return 'bg-gray-100 text-gray-600'
}

function manualStatusLabel(s?: number) {
  if (s === 1) return '已确认正确'
  if (s === 2) return '纠正为违规'
  if (s === 3) return '纠正为正常'
  return '未复核'
}

function manualStatusClass(s?: number) {
  if (s === 1) return 'bg-green-100 text-green-700'
  if (s === 2) return 'bg-red-100 text-red-700'
  if (s === 3) return 'bg-blue-100 text-blue-700'
  return 'bg-gray-100 text-gray-500'
}

function formatTime(t: string | number[] | undefined) {
  if (t == null) return '—'
  if (Array.isArray(t)) {
    if (t.length < 3) return '—'
    const pad = (n: number) => String(Math.trunc(n)).padStart(2, '0')
    const y = t[0]
    const mo = pad(Number(t[1]))
    const d = pad(Number(t[2]))
    const h = t.length > 3 ? pad(Number(t[3])) : '00'
    const mi = t.length > 4 ? pad(Number(t[4])) : '00'
    const s = t.length > 5 ? pad(Number(t[5])) : '00'
    return `${y}-${mo}-${d} ${h}:${mi}:${s}`
  }
  return String(t).replace('T', ' ').slice(0, 19)
}

function formatViolation(v: number | null | undefined) {
  if (v == null || Number.isNaN(Number(v))) return '—'
  const n = Number(v)
  return `${n}%`
}

async function load() {
  const pr = await adminApi.aiCheckRecords({
    page: currentPage.value,
    size: pageSize,
    userId: filterUserId.value,
    postId: filterPostId.value,
    checkResult: filterResult.value,
    // manualStatus: filterManualStatus.value, // 后端需要添加该参数
  })
  records.value = pr.records || []
  total.value = pr.total ?? 0
}

async function loadVectorStats() {
  try {
    const res = await adminApi.aiVectorStats()
    vectorStats.value = res || { totalSize: 0, description: '' }
  } catch (e) {
    // 接口可能不存在，静默失败
  }
}

function goPage(p: number) {
  if (p < 1 || p > totalPages.value) return
  currentPage.value = p
  load()
}

function reset() {
  filterUserId.value = undefined
  filterPostId.value = undefined
  filterResult.value = undefined
  filterManualStatus.value = undefined
  currentPage.value = 1
  load()
}

function openFeedbackDialog(row: AiCheckRecord, status: number) {
  currentRecord.value = row
  currentFeedbackStatus.value = status
  feedbackForm.value = { label: row.manualLabel || '', remark: '' }
  feedbackDialogVisible.value = true
}

async function submitFeedback() {
  if (!currentRecord.value) return

  // 纠正为违规时强制要求标签
  if (currentFeedbackStatus.value === 2 && !feedbackForm.value.label) {
    ElMessage.warning('纠正为违规时请选择标签分类，用于后续向量匹配')
    return
  }

  feedbackSubmitting.value = true
  try {
    await adminApi.aiCheckFeedback(currentRecord.value.id, {
      manualStatus: currentFeedbackStatus.value,
      label: feedbackForm.value.label,
      remark: feedbackForm.value.remark,
    })
    ElMessage.success('复核反馈已提交，样本将进入数据飞轮')
    feedbackDialogVisible.value = false
    load() // 刷新列表
  } catch (e: any) {
    ElMessage.error(e.message || '提交失败')
  } finally {
    feedbackSubmitting.value = false
  }
}

onMounted(() => {
  load()
  loadVectorStats()
})
</script>
