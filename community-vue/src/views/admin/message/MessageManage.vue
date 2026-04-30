<template>
  <div class="space-y-6">
    <!-- 搜索 -->
    <div class="bg-white rounded-xl border border-gray-100 shadow-sm p-5">
      <input
        v-model="keyword"
        type="text"
        placeholder="搜索消息内容..."
        class="px-4 py-2 bg-gray-50 border border-gray-200 rounded-lg text-sm outline-none focus:border-primary w-80"
        @keyup.enter="handleSearch"
      />
      <button @click="handleSearch" class="ml-3 px-4 py-2 bg-primary text-white text-sm rounded-lg hover:bg-blue-600">
        搜索
      </button>
      <button @click="handleReset" class="ml-2 px-4 py-2 border border-gray-200 text-gray-600 text-sm rounded-lg hover:bg-gray-50">
        重置
      </button>
    </div>

    <!-- 消息列表 -->
    <div class="bg-white rounded-xl border border-gray-100 shadow-sm overflow-hidden">
      <table class="w-full text-sm">
        <thead class="bg-gray-50 border-b text-left text-xs text-gray-500 uppercase tracking-wider">
          <tr>
            <th class="px-5 py-3.5">消息ID</th>
            <th class="px-5 py-3.5">发送者</th>
            <th class="px-5 py-3.5">接收者</th>
            <th class="px-5 py-3.5">内容</th>
            <th class="px-5 py-3.5">时间</th>
            <th class="px-5 py-3.5">操作</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-50">
          <tr v-for="msg in messages" :key="msg.id" class="hover:bg-gray-50/50">
            <td class="px-5 py-3 text-gray-400 text-xs">{{ msg.id }}</td>
            <td class="px-5 py-3 text-gray-700">用户 {{ msg.fromUserId }}</td>
            <td class="px-5 py-3 text-gray-700">用户 {{ msg.toUserId }}</td>
            <td class="px-5 py-3 text-gray-600 max-w-xs truncate">{{ msg.content }}</td>
            <td class="px-5 py-3 text-gray-400 text-xs">{{ formatTime(msg.createTime) }}</td>
            <td class="px-5 py-3">
              <button @click="handleDelete(msg)" class="text-red-500 hover:text-red-700 text-xs font-medium">
                删除
              </button>
            </td>
          </tr>
          <tr v-if="messages.length === 0">
            <td colspan="6" class="px-5 py-8 text-center text-gray-400">暂无消息数据</td>
          </tr>
        </tbody>
      </table>

      <!-- 分页 -->
      <div class="flex items-center justify-between px-5 py-4 border-t border-gray-100">
        <span class="text-sm text-gray-500">共 {{ total }} 条</span>
        <div class="flex items-center gap-2">
          <button
            @click="currentPage > 1 && (currentPage--, loadMessages())"
            :disabled="currentPage <= 1"
            class="px-3 py-1.5 text-sm border border-gray-200 rounded-lg disabled:opacity-40 hover:bg-gray-50"
          >
            上一页
          </button>
          <span class="text-sm text-gray-600">{{ currentPage }} / {{ totalPages || 1 }}</span>
          <button
            @click="currentPage < totalPages && (currentPage++, loadMessages())"
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

const messages = ref<any[]>([])
const total = ref(0)
const totalPages = ref(1)
const currentPage = ref(1)
const keyword = ref('')

// 加载消息列表（已修复）
async function loadMessages() {
  try {
    const res: any = await adminApi.messages({
      page: currentPage.value,
      size: 20,
      keyword: keyword.value || undefined
    })

    if (res && typeof res === 'object') {
      messages.value = res.records || []
      total.value = res.total || 0
      totalPages.value = Math.ceil((total.value) / 20)
    }
  } catch (e) {
    console.error('加载消息失败', e)
    ElMessage.error('加载消息失败')
  }
}

// 搜索
function handleSearch() {
  currentPage.value = 1
  loadMessages()
}

// 重置
function handleReset() {
  keyword.value = ''
  currentPage.value = 1
  loadMessages()
}

// 删除
async function handleDelete(msg: any) {
  try {
    await ElMessageBox.confirm('确定删除该消息吗？', '确认删除', { type: 'warning' })
    await adminApi.deleteMessage(msg.id)
    ElMessage.success('删除成功')
    loadMessages()
  } catch (e: any) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

// 时间格式化
function formatTime(time: string): string {
  if (!time) return '-'
  const d = new Date(time)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

onMounted(() => loadMessages())
</script>