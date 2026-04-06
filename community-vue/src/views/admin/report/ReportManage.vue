<template>
  <div class="space-y-6">
    <!-- 统计概览 -->
    <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
      <div v-for="(item, i) in statCards" :key="i" class="bg-white rounded-xl p-4 border border-gray-100 shadow-sm">
        <div class="flex items-center gap-3">
          <div :class="['w-10 h-10 rounded-lg flex items-center justify-center', item.bg]">
            <component :is="item.icon" :class="['w-5 h-5', item.iconColor]" />
          </div>
          <div>
            <p class="text-2xl font-bold text-gray-800">{{ item.value }}</p>
            <p class="text-xs text-gray-500">{{ item.label }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 筛选和列表 -->
    <div class="bg-white rounded-xl border border-gray-100 shadow-sm">
      <div class="p-5 border-b border-gray-100">
        <div class="flex flex-wrap items-center gap-3">
          <select v-model="typeFilter" class="px-3 py-2 bg-gray-50 border border-gray-200 rounded-lg text-sm outline-none">
            <option value="">全部类型</option>
            <option value="spam">垃圾广告</option>
            <option value="abuse">人身攻击</option>
            <option value="copyright">版权问题</option>
            <option value="illegal">违法内容</option>
            <option value="other">其他</option>
          </select>
          <select v-model="statusFilter" class="px-3 py-2 bg-gray-50 border border-gray-200 rounded-lg text-sm outline-none">
            <option value="">全部状态</option>
            <option value="pending">待处理</option>
            <option value="processing">处理中</option>
            <option value="resolved">已解决</option>
            <option value="rejected">已驳回</option>
          </select>
          <button class="px-4 py-2 bg-primary text-white text-sm rounded-lg hover:bg-blue-600">筛选</button>
          <button class="px-4 py-2 border border-gray-200 text-gray-600 text-sm rounded-lg hover:bg-gray-50">重置</button>
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
                    <span :class="['text-xs px-2 py-0.5 rounded-full font-medium', getTypeTagClass(report.type)]">{{ report.typeLabel }}</span>
                    <span :class="['text-xs px-2 py-0.5 rounded-full', getStatusTagClass(report.status)]">{{ report.statusLabel }}</span>
                  </div>
                  <p class="font-medium text-gray-800">{{ report.title }}</p>
                  <p class="text-sm text-gray-600 mt-1 line-clamp-2">{{ report.reason }}</p>
                </div>
                <div class="text-right shrink-0 ml-4">
                  <p class="text-xs text-gray-400">{{ report.time }}</p>
                  <p class="text-xs text-gray-500 mt-1">举报人：{{ report.reporter }}</p>
                </div>
              </div>

              <!-- 被举报内容预览 -->
              <div class="mt-3 bg-gray-50 rounded-lg p-3">
                <p class="text-xs text-gray-400 mb-1">被举报内容：</p>
                <p class="text-sm text-gray-700 line-clamp-2">{{ report.targetContent }}</p>
              </div>

              <!-- 操作按钮 -->
              <div class="flex items-center gap-2 mt-3" v-if="report.status === 'pending'">
                <button class="px-3 py-1.5 bg-green-500 text-white text-xs rounded-lg hover:bg-green-600 transition-colors">通过并删除内容</button>
                <button class="px-3 py-1.5 bg-orange-500 text-white text-xs rounded-lg hover:bg-orange-600 transition-colors">警告处理</button>
                <button class="px-3 py-1.5 bg-gray-200 text-gray-600 text-xs rounded-lg hover:bg-gray-300 transition-colors">驳回举报</button>
                <button class="px-3 py-1.5 border border-gray-200 text-gray-600 text-xs rounded-lg hover:bg-gray-50 transition-colors">查看详情</button>
              </div>
              <div class="flex items-center gap-2 mt-3" v-else-if="report.status === 'resolved'">
                <span class="text-xs text-green-600">处理结果：{{ report.result }}</span>
                <span class="text-xs text-gray-400">处理人：{{ report.handler }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="flex items-center justify-center p-4 border-t border-gray-100">
        <button class="px-4 py-2 text-sm text-primary hover:text-blue-700">加载更多</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { AlertTriangle, Shield, FileWarning, Copyright, Ban, CheckCircle } from 'lucide-vue-next'

const typeFilter = ref('')
const statusFilter = ref('')

const statCards = [
  { label: '待处理举报', value: 23, icon: AlertTriangle, bg: 'bg-orange-50', iconColor: 'text-orange-500' },
  { label: '今日新增', value: 8, icon: FileWarning, bg: 'bg-blue-50', iconColor: 'text-blue-500' },
  { label: '本周处理', value: 45, icon: CheckCircle, bg: 'bg-green-50', iconColor: 'text-green-500' },
  { label: '驳回数量', value: 12, icon: Ban, bg: 'bg-red-50', iconColor: 'text-red-500' },
]

const reportList = [
  {
    id: 1, type: 'spam', typeLabel: '垃圾广告',
    status: 'pending', statusLabel: '待处理',
    title: '帖子《免费领取XX福利》涉嫌违规推广',
    reason: '该帖子多次出现外部链接引导，疑似为商业推广，且链接指向非正规网站。',
    targetContent: '【福利来袭】限时免费领取XXX，点击下方链接立即获取...',
    time: '10分钟前', reporter: 'user_0088',
  },
  {
    id: 2, type: 'abuse', typeLabel: '人身攻击',
    status: 'processing', statusLabel: '处理中',
    title: '评论中存在辱骂性语言',
    reason: '用户在评论区对其他用户使用了不当言辞，造成了不良影响。',
    targetContent: '你这种人根本不懂，别在这里瞎说，脑子有问题吧...',
    time: '30分钟前', reporter: 'user_0156',
  },
  {
    id: 3, type: 'copyright', typeLabel: '版权问题',
    status: 'resolved', statusLabel: '已解决',
    title: '文章涉嫌抄袭他人原创内容',
    reason: '经对比发现，该文章与我发布的原创内容高度相似，超过80%重复率。',
    targetContent: '在当今快速发展的技术领域，我们需要不断学习和探索新的解决方案...',
    time: '2小时前', reporter: 'tech_writer',
    result: '已删除侵权内容并对作者进行警告', handler: 'admin_001',
  },
  {
    id: 4, type: 'illegal', typeLabel: '违法内容',
    status: 'pending', statusLabel: '待处理',
    title: '帖子包含敏感政治内容',
    reason: '系统自动检测到该帖子包含敏感关键词，需要人工审核。',
    targetContent: '关于某些政策的讨论，我认为应该从多个角度来看待...',
    time: '3小时前', reporter: 'system_auto',
  },
]

function getTypeBg(type: string): string {
  const b: Record<string, string> = { spam: 'bg-red-50', abuse: 'bg-orange-50', copyright: 'bg-purple-50', illegal: 'bg-red-100', other: 'bg-gray-50' }
  return b[type] || 'bg-gray-50'
}
function getTypeColor(type: string): string {
  const c: Record<string, string> = { spam: 'text-red-500', abuse: 'text-orange-500', copyright: 'text-purple-500', illegal: 'text-red-600', other: 'text-gray-500' }
  return c[type] || 'text-gray-500'
}
function getTypeTagClass(type: string): string {
  const c: Record<string, string> = { spam: 'bg-red-50 text-red-600', abuse: 'bg-orange-50 text-orange-600', copyright: 'bg-purple-50 text-purple-600', illegal: 'bg-red-100 text-red-700', other: 'bg-gray-100 text-gray-600' }
  return c[type] || 'bg-gray-100 text-gray-600'
}
function getStatusTagClass(status: string): string {
  const c: Record<string, string> = { pending: 'bg-yellow-50 text-yellow-600', processing: 'bg-blue-50 text-blue-600', resolved: 'bg-green-50 text-green-600', rejected: 'bg-red-50 text-red-600' }
  return c[status] || 'bg-gray-100'
}
</script>
