<template>
  <div class="space-y-6">
    <!-- 统计卡片 -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-5">
      <div
        v-for="(stat, i) in stats"
        :key="i"
        class="bg-white rounded-xl p-5 border border-gray-100 shadow-sm hover:shadow-md transition-shadow group"
      >
        <div class="flex items-center justify-between mb-3">
          <span class="text-sm text-gray-500 font-medium">{{ stat.label }}</span>
          <div :class="['w-10 h-10 rounded-lg flex items-center justify-center', stat.bgColor]">
            <component :is="stat.icon" :class="['w-5 h-5', stat.iconColor]" />
          </div>
        </div>
        <p class="text-2xl font-bold text-gray-800">{{ stat.value.toLocaleString() }}</p>
        <div class="flex items-center gap-1 mt-2">
          <TrendingUp v-if="stat.trend > 0" class="w-3.5 h-3.5 text-green-500" />
          <TrendingDown v-else class="w-3.5 h-3.5 text-red-500" />
          <span :class="['text-xs font-medium', stat.trend > 0 ? 'text-green-500' : 'text-red-500']">
            {{ stat.trend > 0 ? '+' : '' }}{{ stat.trend }}%
          </span>
          <span class="text-xs text-gray-400 ml-1">较昨日</span>
        </div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
      <!-- 用户增长趋势 -->
      <div class="bg-white rounded-xl p-6 border border-gray-100 shadow-sm">
        <h3 class="font-semibold text-gray-800 mb-4 flex items-center gap-2">
          <BarChart class="w-5 h-5 text-primary" />用户增长趋势
        </h3>
        <div class="h-64 flex items-end gap-2 pt-8 px-2">
          <div
            v-for="(bar, i) in chartData.users"
            :key="i"
            class="flex-1 flex flex-col items-center gap-1"
          >
            <div class="w-full max-w-[40px] bg-gradient-to-t from-primary to-blue-400 rounded-t-md transition-all hover:opacity-80 cursor-pointer relative group"
                   :style="{ height: (bar / Math.max(...chartData.users, 1) * 180) + 'px' }">
              <span class="absolute -top-7 left-1/2 -translate-x-1/2 text-xs text-gray-600 opacity-0 group-hover:opacity-100 transition-opacity whitespace-nowrap">{{ bar }}</span>
            </div>
            <span class="text-xs text-gray-400">{{ chartData.labels[i] }}</span>
          </div>
        </div>
      </div>

      <!-- 帖子发布趋势 -->
      <div class="bg-white rounded-xl p-6 border border-gray-100 shadow-sm">
        <h3 class="font-semibold text-gray-800 mb-4 flex items-center gap-2">
          <LineChart class="w-5 h-5 text-green-500" />内容发布趋势
        </h3>
        <div class="h-64 flex items-end gap-2 pt-8 px-2">
          <div
            v-for="(bar, i) in chartData.posts"
            :key="i"
            class="flex-1 flex flex-col items-center gap-1"
          >
            <div class="w-full max-w-[40px] bg-gradient-to-t from-green-500 to-emerald-400 rounded-t-md transition-all hover:opacity-80 cursor-pointer"
                 :style="{ height: (bar / Math.max(...chartData.posts, 1) * 180) + 'px' }"></div>
            <span class="text-xs text-gray-400">{{ chartData.labels[i] }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 下方信息 -->
    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
      <!-- 最新举报 -->
      <div class="lg:col-span-2 bg-white rounded-xl p-6 border border-gray-100 shadow-sm">
        <h3 class="font-semibold text-gray-800 mb-4 flex items-center gap-2">
          <AlertTriangle class="w-5 h-5 text-orange-500" />待处理举报
          <span class="ml-auto text-xs text-orange-500 bg-orange-50 px-2 py-0.5 rounded-full">{{ reports.length }} 条</span>
        </h3>
        <div class="overflow-x-auto">
          <table class="w-full text-sm">
            <thead><tr class="text-left text-xs text-gray-500 uppercase tracking-wider border-b"><th class="pb-3">举报类型</th><th>内容摘要</th><th>举报人</th><th>时间</th><th>操作</th></tr></thead>
            <tbody class="divide-y divide-gray-50">
              <tr v-for="r in reports" :key="r.id" class="hover:bg-gray-50">
                <td class="py-3"><span :class="['text-xs px-2 py-0.5 rounded-full', getReportTagClass(r.type)]">{{ r.typeLabel }}</span></td>
                <td class="py-3 max-w-[200px] truncate text-gray-700">{{ r.content }}</td>
                <td class="py-3 text-gray-600">{{ r.reporter }}</td>
                <td class="py-3 text-gray-400 text-xs">{{ r.time }}</td>
                <td class="py-3"><button class="text-primary hover:text-blue-700 text-xs font-medium">处理</button></td>
              </tr>
              <tr v-if="reports.length === 0"><td colspan="5" class="py-8 text-center text-gray-400">暂无待处理举报</td></tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 热门板块 -->
      <div class="bg-white rounded-xl p-6 border border-gray-100 shadow-sm">
        <h3 class="font-semibold text-gray-800 mb-4 flex items-center gap-2">
          <LayoutGrid class="w-5 h-5 text-purple-500" />热门板块
        </h3>
        <div class="space-y-3">
          <div v-for="(section, i) in hotSections" :key="section.name" class="flex items-center gap-3">
            <span :class="['w-6 h-6 rounded-full flex items-center justify-center text-xs font-bold shrink-0', i < 3 ? 'bg-gradient-to-br from-yellow-400 to-orange-500 text-white' : 'bg-gray-100 text-gray-500']">{{ i + 1 }}</span>
            <div class="flex-1 min-w-0">
              <p class="text-sm font-medium text-gray-800 truncate">{{ section.name }}</p>
              <div class="flex items-center gap-2 mt-0.5">
                <div class="flex-1 h-1.5 bg-gray-100 rounded-full overflow-hidden">
                  <div class="h-full bg-gradient-to-r from-primary to-blue-400 rounded-full" :style="{ width: section.percent + '%' }"></div>
                </div>
                <span class="text-xs text-gray-400 w-16 text-right">{{ section.count }} 帖子</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Users, FileText, MessageSquare, Eye, TrendingUp, TrendingDown, BarChart, LineChart, AlertTriangle, LayoutGrid } from 'lucide-vue-next'
import { adminApi } from '@/api'

interface DashboardStats {
  userCount: number
  todayUsers: number
  postCount: number
  activeUsers: number
  userTrend: number
  postTrend: number
  userGrowth: number[]
  postGrowth: number[]
  labels: string[]
}

interface Report {
  id: number
  type: number
  typeLabel: string
  content: string
  reporter: string
  time: string
}

const stats = reactive([
  { label: '用户总数', value: 0, trend: 0, icon: Users, bgColor: 'bg-blue-50', iconColor: 'text-blue-500' },
  { label: '今日新增', value: 0, trend: 0, icon: Users, bgColor: 'bg-green-50', iconColor: 'text-green-500' },
  { label: '帖子总数', value: 0, trend: 0, icon: FileText, bgColor: 'bg-purple-50', iconColor: 'text-purple-500' },
  { label: '今日活跃', value: 0, trend: 0, icon: Eye, bgColor: 'bg-orange-50', iconColor: 'text-orange-500' },
])

const chartData = reactive({
  labels: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
  users: [0, 0, 0, 0, 0, 0, 0],
  posts: [0, 0, 0, 0, 0, 0, 0],
})

const reports = ref<Report[]>([])
const hotSections = ref<{ name: string; count: number; percent: number }[]>([])

function getReportTagClass(type: number): string {
  const cls: Record<number, string> = {
    1: 'bg-red-50 text-red-600',
    2: 'bg-orange-50 text-orange-600',
    3: 'bg-purple-50 text-purple-600',
    4: 'bg-red-100 text-red-700'
  }
  return cls[type] || 'bg-gray-100 text-gray-600'
}

function getReportTypeLabel(type: number): string {
  const labels: Record<number, string> = {
    1: '垃圾广告',
    2: '人身攻击',
    3: '版权问题',
    4: '违法内容'
  }
  return labels[type] || '其他'
}

onMounted(async () => {
  try {
    // 获取仪表盘统计数据
    const data: any = await adminApi.dashboard()
    
    if (data) {
      // 与 DashboardVO 字段对齐：totalUserCount / todayNewUsers / totalPostCount / todayActiveUser
      stats[0].value = Number(data.totalUserCount ?? data.userCount ?? 0)
      stats[0].trend = Number(data.userTrend ?? 0)
      stats[1].value = Number(data.todayNewUsers ?? data.todayUsers ?? 0)
      stats[1].trend = data.userGrowth?.[6] != null ? Number(data.userGrowth[6]) : 0
      stats[2].value = Number(data.totalPostCount ?? data.postCount ?? 0)
      stats[2].trend = Number(data.postTrend ?? 0)
      stats[3].value = Number(data.todayActiveUser ?? data.activeUsers ?? 0)
      
      if (Array.isArray(data.userGrowth) && data.userGrowth.length > 0) {
        chartData.users = data.userGrowth.map((n: any) => Number(n) || 0)
      }
      if (Array.isArray(data.postGrowth) && data.postGrowth.length > 0) {
        chartData.posts = data.postGrowth.map((n: any) => Number(n) || 0)
      }
      if (data.labels && data.labels.length > 0) {
        chartData.labels = data.labels
      }
    }
    
    // 获取待处理举报
    const reportRes: any = await adminApi.reports({ status: 0, size: 5 })
    if (reportRes?.records) {
      reports.value = reportRes.records.map((r: any) => ({
        id: r.id,
        type: r.type,
        typeLabel: getReportTypeLabel(r.type),
        content: r.content || r.description || '暂无内容',
        reporter: `用户${r.reporterId || '未知'}`,
        time: formatTime(r.createTime)
      }))
    }
    
    // 获取热门板块
    const sectionRes: any = await adminApi.posts({ status: 1, size: 100 })
    if (sectionRes?.records) {
      // 简单统计各板块帖子数
      const sectionMap = new Map<string, number>()
      sectionRes?.records?.forEach((p: any) => {
        const section = p.sectionName || '默认板块'
        sectionMap.set(section, (sectionMap.get(section) || 0) + 1)
      })
      const maxCount = Math.max(...Array.from(sectionMap.values()), 1)
      hotSections.value = Array.from(sectionMap.entries())
        .slice(0, 5)
        .map(([name, count]) => ({
          name,
          count,
          percent: Math.round((count / maxCount) * 100)
        }))
    }
  } catch (e) {
    console.error('加载仪表盘数据失败', e)
  }
})

function formatTime(dateStr: string): string {
  if (!dateStr) return '未知'
  const d = new Date(dateStr)
  const diff = Date.now() - d.getTime()
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  return `${Math.floor(diff / 86400000)}天前`
}
</script>
