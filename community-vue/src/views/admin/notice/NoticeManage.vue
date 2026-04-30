<template>
  <div class="space-y-6">
    <!-- 筛选 -->
    <div class="bg-white rounded-xl border border-gray-100 shadow-sm p-5">
      <div class="flex items-center gap-3">
        <select v-model="typeFilter" class="px-3 py-2 bg-gray-50 border border-gray-200 rounded-lg text-sm outline-none">
          <option :value="undefined">全部类型</option>
          <option :value="1">点赞</option>
          <option :value="2">评论</option>
          <option :value="3">关注</option>
          <option :value="4">收藏</option>
          <option :value="5">系统</option>
        </select>
        <button @click="handleSearch" class="ml-3 px-4 py-2 bg-primary text-white text-sm rounded-lg hover:bg-blue-600">
          筛选
        </button>
        <button @click="handleReset" class="ml-2 px-4 py-2 border border-gray-200 text-gray-600 text-sm rounded-lg hover:bg-gray-50">
          重置
        </button>
      </div>
    </div>

    <!-- 通知列表 -->
    <div class="bg-white rounded-xl border border-gray-100 shadow-sm overflow-hidden">
      <table class="w-full text-sm">
        <thead class="bg-gray-50 border-b text-left text-xs text-gray-500 uppercase tracking-wider">
          <tr>
            <th class="px-5 py-3.5">ID</th>
            <th class="px-5 py-3.5">接收用户</th>
            <th class="px-5 py-3.5">类型</th>
            <th class="px-5 py-3.5">内容</th>
            <th class="px-5 py-3.5">已读</th>
            <th class="px-5 py-3.5">时间</th>
            <th class="px-5 py-3.5">操作</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-50">
          <tr v-for="notice in notices" :key="notice.id" class="hover:bg-gray-50/50">
            <td class="px-5 py-3 text-gray-400 text-xs">{{ notice.id }}</td>
            <td class="px-5 py-3 text-gray-700">用户 {{ notice.userId }}</td>
            <td class="px-5 py-3">
              <span :class="['text-xs px-2 py-0.5 rounded-full font-medium', getTypeClass(notice.type)]">
                {{ getTypeLabel(notice.type) }}
              </span>
            </td>
            <td class="px-5 py-3 text-gray-600 max-w-xs truncate">{{ notice.content }}</td>
            <td class="px-5 py-3">
              <span :class="['text-xs px-2 py-0.5 rounded-full', notice.isRead ? 'bg-green-50 text-green-600' : 'bg-red-50 text-red-500']">
                {{ notice.isRead ? '已读' : '未读' }}
              </span>
            </td>
            <td class="px-5 py-3 text-gray-400 text-xs">{{ formatTime(notice.createTime) }}</td>
            <td class="px-5 py-3">
              <button @click="handleDelete(notice)" class="text-red-500 hover:text-red-700 text-xs font-medium">删除</button>
            </td>
          </tr>
          <tr v-if="notices.length === 0">
            <td colspan="7" class="px-5 py-8 text-center text-gray-400">暂无通知数据</td>
          </tr>
        </tbody>
      </table>

      <!-- 分页 -->
      <div class="flex items-center justify-between px-5 py-4 border-t border-gray-100">
        <span class="text-sm text-gray-500">共 {{ total }} 条</span>
        <div class="flex items-center gap-2">
          <button
            @click="currentPage > 1 && (currentPage--, loadNotices())"
            :disabled="currentPage <= 1"
            class="px-3 py-1.5 text-sm border border-gray-200 rounded-lg disabled:opacity-40 hover:bg-gray-50"
          >
            上一页
          </button>
          <span class="text-sm text-gray-600">{{ currentPage }} / {{ totalPages || 1 }}</span>
          <button
            @click="currentPage < totalPages && (currentPage++, loadNotices())"
            :disabled="currentPage >= totalPages"
            class="px-3 py-1.5 text-sm border border-gray-200 rounded-lg disabled:opacity-40 hover:bg-gray-50"
          >
            下一页
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi } from '@/api'

const notices = ref<any[]>([])
const total = ref(0)
const totalPages = ref(1)
const currentPage = ref(1)
const typeFilter = ref<number | undefined>(undefined)

// ✅ 修复加载逻辑
async function loadNotices() {
  try {
    const res: any = await adminApi.notices({
      page: currentPage.value,
      size: 20,
      type: typeFilter.value
    })

    if (res && typeof res === 'object') {
      notices.value = res.records || []
      total.value = res.total || 0
      totalPages.value = Math.ceil(total.value / 20)
    }
  } catch (e) {
    console.error('加载通知失败', e)
    ElMessage.error('加载失败')
  }
}

function handleSearch() {
  currentPage.value = 1
  loadNotices()
}

function handleReset() {
  typeFilter.value = undefined
  currentPage.value = 1
  loadNotices()
}

function getTypeLabel(type: number): string {
  const m: Record<number, string> = { 1: '点赞', 2: '评论', 3: '关注', 4: '收藏', 5: '系统' }
  return m[type] || '其他'
}
function getTypeClass(type: number): string {
  const c: Record<number, string> = {
    1: 'bg-red-50 text-red-600',
    2: 'bg-blue-50 text-blue-600',
    3: 'bg-green-50 text-green-600',
    4: 'bg-yellow-50 text-yellow-600',
    5: 'bg-purple-50 text-purple-600'
  }
  return c[type] || 'bg-gray-100 text-gray-600'
}

async function handleDelete(notice: any) {
  try {
    await ElMessageBox.confirm('确定删除该通知吗？', '确认删除', { type: 'warning' })
    await adminApi.deleteNotice(notice.id)
    ElMessage.success('删除成功')
    loadNotices()
  } catch (e: any) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

function formatTime(time: string): string {
  if (!time) return '-'
  const d = new Date(time)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

onMounted(() => loadNotices())
</script>