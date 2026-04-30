<template>
  <div class="space-y-6">
    <!-- 统计概览 -->
    <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
      <div class="bg-white rounded-xl p-4 border border-gray-100 shadow-sm">
        <div class="flex items-center gap-3">
          <div class="w-10 h-10 rounded-lg flex items-center justify-center bg-orange-50">
            <AlertTriangle class="w-5 h-5 text-orange-500" />
          </div>
          <div>
            <p class="text-2xl font-bold text-gray-800">{{ stats.pending }}</p>
            <p class="text-xs text-gray-500">待处理举报</p>
          </div>
        </div>
      </div>
      <div class="bg-white rounded-xl p-4 border border-gray-100 shadow-sm">
        <div class="flex items-center gap-3">
          <div class="w-10 h-10 rounded-lg flex items-center justify-center bg-blue-50">
            <FileWarning class="w-5 h-5 text-blue-500" />
          </div>
          <div>
            <p class="text-2xl font-bold text-gray-800">{{ stats.today }}</p>
            <p class="text-xs text-gray-500">今日新增</p>
          </div>
        </div>
      </div>
      <div class="bg-white rounded-xl p-4 border border-gray-100 shadow-sm">
        <div class="flex items-center gap-3">
          <div class="w-10 h-10 rounded-lg flex items-center justify-center bg-green-50">
            <CheckCircle class="w-5 h-5 text-green-500" />
          </div>
          <div>
            <p class="text-2xl font-bold text-gray-800">{{ stats.resolved }}</p>
            <p class="text-xs text-gray-500">本周处理</p>
          </div>
        </div>
      </div>
      <div class="bg-white rounded-xl p-4 border border-gray-100 shadow-sm">
        <div class="flex items-center gap-3">
          <div class="w-10 h-10 rounded-lg flex items-center justify-center bg-red-50">
            <Ban class="w-5 h-5 text-red-500" />
          </div>
          <div>
            <p class="text-2xl font-bold text-gray-800">{{ stats.rejected }}</p>
            <p class="text-xs text-gray-500">驳回数量</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 筛选和列表 -->
    <div class="bg-white rounded-xl border border-gray-100 shadow-sm">
      <div class="p-5 border-b border-gray-100">
        <div class="flex flex-wrap items-center gap-3">
          <select v-model="typeFilter" class="px-3 py-2 bg-gray-50 border border-gray-200 rounded-lg text-sm outline-none">
            <option :value="undefined">全部类型</option>
            <option :value="1">垃圾广告</option>
            <option :value="2">人身攻击</option>
            <option :value="3">版权问题</option>
            <option :value="4">违法内容</option>
            <option :value="5">其他</option>
          </select>
          <select v-model="statusFilter" class="px-3 py-2 bg-gray-50 border border-gray-200 rounded-lg text-sm outline-none">
            <option :value="undefined">全部状态</option>
            <option :value="0">待处理</option>
            <option :value="1">已处理</option>
            <option :value="2">已驳回</option>
          </select>
          <button @click="handleSearch" class="px-4 py-2 bg-primary text-white text-sm rounded-lg hover:bg-blue-600">筛选</button>
          <button @click="handleReset" class="px-4 py-2 border border-gray-200 text-gray-600 text-sm rounded-lg hover:bg-gray-50">重置</button>
        </div>
      </div>

      <div class="divide-y divide-gray-50">
        <div
          v-for="report in reportList"
          :key="report.id"
          class="p-5 hover:bg-gray-50/50 transition-colors"
        >
          <div class="flex items-start gap-4">
            <div :class="['w-10 h-10 rounded-full flex items-center justify-center shrink-0', getTypeBg(report.type)]">
              <AlertTriangle class="w-5 h-5" :class="getTypeColor(report.type)" />
            </div>
            <div class="flex-1 min-w-0">
              <div class="flex items-start justify-between mb-2">
                <div>
                  <div class="flex items-center gap-2 mb-1">
                    <span :class="['text-xs px-2 py-0.5 rounded-full font-medium', getTypeTagClass(report.type)]">{{ getTypeLabel(report.type) }}</span>
                    <span :class="['text-xs px-2 py-0.5 rounded-full', getStatusTagClass(report.status)]">{{ getStatusLabel(report.status) }}</span>
                  </div>
                  <p class="font-medium text-gray-800">{{ report.title || '举报内容' }}</p>
                  <p class="text-sm text-gray-600 mt-1 line-clamp-2">{{ report.reason || report.description }}</p>
                </div>
                <div class="text-right shrink-0 ml-4">
                  <p class="text-xs text-gray-400">{{ formatTime(report.createTime) }}</p>
                  <p class="text-xs text-gray-500 mt-1">举报人：{{ report.reporterId ? `用户${report.reporterId}` : '未知' }}</p>
                </div>
              </div>

              <!-- 被举报内容预览 -->
              <div class="mt-3 bg-gray-50 rounded-lg p-3">
                <p class="text-xs text-gray-400 mb-1">被举报内容：</p>
                <p class="text-sm text-gray-700 line-clamp-2">{{ report.targetContent || report.content || '暂无内容' }}</p>
              </div>

              <!-- 操作按钮 -->
              <div class="flex items-center gap-2 mt-3" v-if="report.status === 0">
                <button @click="handleResolve(report, 1)" class="px-3 py-1.5 bg-green-500 text-white text-xs rounded-lg hover:bg-green-600 transition-colors">通过并处理</button>
                <button @click="handleReject(report)" class="px-3 py-1.5 bg-gray-200 text-gray-600 text-xs rounded-lg hover:bg-gray-300 transition-colors">驳回举报</button>
                <button @click="showDetail(report)" class="px-3 py-1.5 border border-gray-200 text-gray-600 text-xs rounded-lg hover:bg-gray-50 transition-colors">查看详情</button>
              </div>
              <div class="flex items-center gap-2 mt-3" v-else-if="report.status === 1">
                <span class="text-xs text-green-600">处理结果：{{ report.result || '已处理' }}</span>
                <span class="text-xs text-gray-400">处理人：{{ report.handlerId ? `管理员${report.handlerId}` : '系统' }}</span>
              </div>
            </div>
          </div>
        </div>
        <div v-if="reportList.length === 0" class="p-8 text-center text-gray-400">
          暂无举报数据
        </div>
      </div>

      <!-- 分页 -->
      <div class="flex items-center justify-between px-5 py-4 border-t border-gray-100">
        <span class="text-sm text-gray-500">共 {{ total }} 条</span>
        <div class="flex items-center gap-2">
          <button @click="handlePageChange(currentPage - 1)" :disabled="currentPage <= 1" class="px-3 py-1.5 text-sm border border-gray-200 rounded-lg disabled:opacity-40 hover:bg-gray-50">上一页</button>
          <span class="text-sm text-gray-600">{{ currentPage }} / {{ totalPages || 1 }}</span>
          <button @click="handlePageChange(currentPage + 1)" :disabled="currentPage >= totalPages" class="px-3 py-1.5 text-sm border border-gray-200 rounded-lg disabled:opacity-40 hover:bg-gray-50">下一页</button>
        </div>
      </div>
    </div>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="举报详情" width="600px">
      <div v-if="currentReport" class="space-y-4">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="举报类型">{{ getTypeLabel(currentReport.type) }}</el-descriptions-item>
          <el-descriptions-item label="处理状态">
            <el-tag :type="currentReport.status === 0 ? 'warning' : currentReport.status === 1 ? 'success' : 'info'">
              {{ getStatusLabel(currentReport.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="举报人">用户{{ currentReport.reporterId }}</el-descriptions-item>
          <el-descriptions-item label="被举报人">用户{{ currentReport.targetUserId }}</el-descriptions-item>
          <el-descriptions-item label="举报时间">{{ formatTime(currentReport.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="目标ID">ID: {{ currentReport.targetId }}</el-descriptions-item>
        </el-descriptions>
        <div>
          <p class="text-sm font-medium text-gray-700 mb-2">举报原因：</p>
          <p class="text-sm text-gray-600 bg-gray-50 p-3 rounded">{{ currentReport.reason || currentReport.description || '暂无' }}</p>
        </div>
        <div>
          <p class="text-sm font-medium text-gray-700 mb-2">被举报内容：</p>
          <p class="text-sm text-gray-600 bg-gray-50 p-3 rounded">{{ currentReport.targetContent || currentReport.content || '暂无' }}</p>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { AlertTriangle, FileWarning, Copyright, Ban, CheckCircle } from 'lucide-vue-next'
import { adminApi } from '@/api'

interface Report {
  id: number
  type: number
  status: number
  title?: string
  reason?: string
  description?: string
  content?: string
  targetContent?: string
  reporterId?: number
  targetUserId?: number
  targetId?: number
  result?: string
  handlerId?: number
  createTime?: string
}

const typeFilter = ref<number | undefined>(undefined)
const statusFilter = ref<number | undefined>(undefined)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const totalPages = ref(1)
const reportList = ref<Report[]>([])
const detailVisible = ref(false)
const currentReport = ref<Report | null>(null)

const stats = reactive({
  pending: 0,
  today: 0,
  resolved: 0,
  rejected: 0
})

async function loadReports() {
  try {
    const res: any = await adminApi.reports({
      status: statusFilter.value,
      page: currentPage.value,
      size: pageSize.value
    })
    
    if (res?.records) {
      reportList.value = res.records
      total.value = res.total || 0
      totalPages.value = Math.ceil((res.total || 0) / pageSize.value)
      
      // 统计待处理数量
      stats.pending = res.records.filter((r: Report) => r.status === 0).length
    } else if (Array.isArray(res)) {
      reportList.value = res
      total.value = res.length
      totalPages.value = 1
    }
  } catch (e: any) {
    ElMessage.error('加载举报列表失败')
    console.error(e)
  }
}

function handleSearch() {
  currentPage.value = 1
  loadReports()
}

function handleReset() {
  typeFilter.value = undefined
  statusFilter.value = undefined
  currentPage.value = 1
  loadReports()
}

async function loadReportCount() {
  try {
    const res: any = await adminApi.reportCount()
    if (res && typeof res === 'object') {
      stats.pending = Number(res[0] ?? res['0'] ?? 0)
      stats.resolved = Number(res[1] ?? res['1'] ?? 0)
      stats.rejected = Number(res[2] ?? res['2'] ?? 0)
    }
  } catch (e: any) {
    console.error('加载举报统计失败', e)
  }
}

function handlePageChange(page: number) {
  currentPage.value = page
  loadReports()
}

function getTypeBg(type: number): string {
  const b: Record<number, string> = {
    1: 'bg-red-50',
    2: 'bg-orange-50',
    3: 'bg-purple-50',
    4: 'bg-red-100',
    5: 'bg-gray-50'
  }
  return b[type] || 'bg-gray-50'
}

function getTypeColor(type: number): string {
  const c: Record<number, string> = {
    1: 'text-red-500',
    2: 'text-orange-500',
    3: 'text-purple-500',
    4: 'text-red-600',
    5: 'text-gray-500'
  }
  return c[type] || 'text-gray-500'
}

function getTypeLabel(type: number): string {
  const l: Record<number, string> = {
    1: '垃圾广告',
    2: '人身攻击',
    3: '版权问题',
    4: '违法内容',
    5: '其他'
  }
  return l[type] || '其他'
}

function getTypeTagClass(type: number): string {
  const c: Record<number, string> = {
    1: 'bg-red-50 text-red-600',
    2: 'bg-orange-50 text-orange-600',
    3: 'bg-purple-50 text-purple-600',
    4: 'bg-red-100 text-red-700',
    5: 'bg-gray-100 text-gray-600'
  }
  return c[type] || 'bg-gray-100 text-gray-600'
}

function getStatusTagClass(status: number): string {
  const c: Record<number, string> = {
    0: 'bg-yellow-50 text-yellow-600',
    1: 'bg-green-50 text-green-600',
    2: 'bg-gray-50 text-gray-600'
  }
  return c[status] || 'bg-gray-100'
}

function getStatusLabel(status: number): string {
  const l: Record<number, string> = {
    0: '待处理',
    1: '已处理',
    2: '已驳回'
  }
  return l[status] || '未知'
}

function formatTime(dateStr?: string): string {
  if (!dateStr) return '-'
  const d = new Date(dateStr)
  const diff = Date.now() - d.getTime()
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  if (diff < 604800000) return `${Math.floor(diff / 86400000)}天前`
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

function showDetail(report: Report) {
  currentReport.value = report
  detailVisible.value = true
}

async function handleResolve(report: Report, status: number) {
  try {
    await ElMessageBox.confirm(`确定要处理此举报吗？`, '确认处理', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    await adminApi.handleReport(report.id, status, '已处理')
    ElMessage.success('处理成功')
    loadReports()
  } catch (e: any) {
    if (e !== 'cancel') {
      ElMessage.error('处理失败')
    }
  }
}

async function handleReject(report: Report) {
  try {
    await ElMessageBox.confirm(`确定要驳回此举报吗？`, '确认驳回', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await adminApi.handleReport(report.id, 2, '举报无效')
    ElMessage.success('已驳回')
    loadReports()
  } catch (e: any) {
    if (e !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

onMounted(() => {
  loadReports()
  loadReportCount()
})
</script>
