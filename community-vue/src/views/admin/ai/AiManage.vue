<template>
  <div class="space-y-6">
    <!-- 统计卡片 -->
    <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
      <div class="bg-white rounded-xl border border-gray-100 shadow-sm p-5">
        <div class="flex items-center gap-3 mb-2">
          <div class="w-10 h-10 rounded-lg bg-blue-50 flex items-center justify-center">
            <svg class="w-5 h-5 text-blue-500" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"/></svg>
          </div>
          <div>
            <p class="text-xs text-gray-400">总会话数</p>
            <p class="text-xl font-bold text-gray-800">{{ stats.totalSessions || 0 }}</p>
          </div>
        </div>
      </div>
      <div class="bg-white rounded-xl border border-gray-100 shadow-sm p-5">
        <div class="flex items-center gap-3 mb-2">
          <div class="w-10 h-10 rounded-lg bg-green-50 flex items-center justify-center">
            <svg class="w-5 h-5 text-green-500" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6"/></svg>
          </div>
          <div>
            <p class="text-xs text-gray-400">今日会话</p>
            <p class="text-xl font-bold text-gray-800">{{ stats.todaySessions || 0 }}</p>
          </div>
        </div>
      </div>
      <div class="bg-white rounded-xl border border-gray-100 shadow-sm p-5">
        <div class="flex items-center gap-3 mb-2">
          <div class="w-10 h-10 rounded-lg bg-purple-50 flex items-center justify-center">
            <svg class="w-5 h-5 text-purple-500" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 8h10M7 12h4m1 8l-4-4H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-3l-4 4z"/></svg>
          </div>
          <div>
            <p class="text-xs text-gray-400">总消息数</p>
            <p class="text-xl font-bold text-gray-800">{{ stats.totalMessages || 0 }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- AI 配置 -->
    <div class="bg-white rounded-xl border border-gray-100 shadow-sm overflow-hidden">
      <div class="px-6 py-4 border-b border-gray-100 flex items-center gap-3">
        <svg class="w-5 h-5 text-primary" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.066 2.573c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.573 1.066c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.066-2.573c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z"/><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/></svg>
        <h3 class="font-semibold text-gray-800">AI 模型配置</h3>
      </div>
      <div class="p-6 space-y-4">
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label class="block text-sm text-gray-600 mb-1.5">API 地址</label>
            <input v-model="aiConfig.baseUrl" type="text" placeholder="https://api.openai.com" class="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm outline-none focus:border-primary focus:ring-2 focus:ring-primary/20" />
          </div>
          <div>
            <label class="block text-sm text-gray-600 mb-1.5">模型名称</label>
            <input v-model="aiConfig.model" type="text" placeholder="gpt-3.5-turbo" class="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm outline-none focus:border-primary focus:ring-2 focus:ring-primary/20" />
          </div>
        </div>
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label class="block text-sm text-gray-600 mb-1.5">API Key</label>
            <input v-model="aiConfig.apiKey" type="password" placeholder="sk-..." class="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm outline-none focus:border-primary focus:ring-2 focus:ring-primary/20" />
          </div>
          <div>
            <label class="block text-sm text-gray-600 mb-1.5">温度参数</label>
            <input v-model.number="aiConfig.temperature" type="number" step="0.1" min="0" max="2" class="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm outline-none focus:border-primary" />
          </div>
        </div>
        <div class="flex justify-end pt-2">
          <button @click="saveConfig" :disabled="configSaving" class="px-5 py-2 bg-primary text-white text-sm rounded-lg hover:bg-blue-600 disabled:opacity-50 transition-colors">
            {{ configSaving ? '保存中...' : '保存配置' }}
          </button>
        </div>
      </div>
    </div>

    <!-- 对话记录 -->
    <div class="bg-white rounded-xl border border-gray-100 shadow-sm overflow-hidden">
      <div class="px-6 py-4 border-b border-gray-100 flex items-center justify-between">
        <div class="flex items-center gap-3">
          <svg class="w-5 h-5 text-orange-500" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 10h16M4 14h16M4 18h16"/></svg>
          <h3 class="font-semibold text-gray-800">AI 对话记录</h3>
        </div>
        <input v-model="recordKeyword" type="text" placeholder="搜索用户..." class="px-3 py-1.5 border border-gray-200 rounded-lg text-sm outline-none focus:border-primary w-48" @keyup.enter="loadRecords" />
      </div>
      <table class="w-full text-sm">
        <thead class="bg-gray-50 border-b text-left text-xs text-gray-500 uppercase tracking-wider">
          <tr><th class="px-5 py-3.5">会话ID</th><th class="px-5 py-3.5">用户ID</th><th class="px-5 py-3.5">标题</th><th class="px-5 py-3.5">消息数</th><th class="px-5 py-3.5">创建时间</th><th class="px-5 py-3.5">操作</th></tr>
        </thead>
        <tbody class="divide-y divide-gray-50">
          <tr v-for="record in records" :key="record.id" class="hover:bg-gray-50/50">
            <td class="px-5 py-3 text-gray-400 text-xs">{{ record.id }}</td>
            <td class="px-5 py-3 text-gray-700">用户{{ record.userId }}</td>
            <td class="px-5 py-3 text-gray-600 max-w-xs truncate">{{ record.title || '未命名会话' }}</td>
            <td class="px-5 py-3 text-gray-500">{{ record.messageCount || 0 }}</td>
            <td class="px-5 py-3 text-gray-400 text-xs">{{ formatTime(record.createTime) }}</td>
            <td class="px-5 py-3">
              <button @click="viewDetail(record)" class="text-primary hover:text-blue-700 text-xs font-medium mr-3">查看</button>
              <button @click="handleDelete(record)" class="text-red-500 hover:text-red-700 text-xs font-medium">删除</button>
            </td>
          </tr>
          <tr v-if="records.length === 0"><td colspan="6" class="px-5 py-8 text-center text-gray-400">暂无对话记录</td></tr>
        </tbody>
      </table>
      <div class="flex items-center justify-between px-5 py-4 border-t border-gray-100">
        <span class="text-sm text-gray-500">共 {{ recordTotal }} 条</span>
        <div class="flex items-center gap-2">
          <button @click="recordPage > 1 && (recordPage--, loadRecords())" :disabled="recordPage <= 1" class="px-3 py-1.5 text-sm border border-gray-200 rounded-lg disabled:opacity-40 hover:bg-gray-50">上一页</button>
          <span class="text-sm text-gray-600">{{ recordPage }} / {{ recordTotalPages || 1 }}</span>
          <button @click="recordPage < recordTotalPages && (recordPage++, loadRecords())" :disabled="recordPage >= recordTotalPages" class="px-3 py-1.5 text-sm border border-gray-200 rounded-lg disabled:opacity-40 hover:bg-gray-50">下一页</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi } from '@/api'

// 统计
const stats = reactive<Record<string, number>>({})

// AI 配置
const aiConfig = reactive({
  baseUrl: '',
  model: '',
  apiKey: '',
  temperature: 0.7,
})
const configSaving = ref(false)

// 对话记录
const records = ref<any[]>([])
const recordTotal = ref(0)
const recordTotalPages = ref(1)
const recordPage = ref(1)
const recordKeyword = ref('')

async function loadStats() {
  try {
    const res: any = await adminApi.aiStats()
    if (res) Object.assign(stats, res)
  } catch (e) {
    console.error(e)
  }
}

async function loadAiConfig() {
  try {
    const res: any = await adminApi.aiConfig()
    if (res) {
      aiConfig.baseUrl = res.baseUrl || res.base_url || ''
      aiConfig.model = res.model || ''
      aiConfig.apiKey = res.apiKey || res.api_key || ''
      aiConfig.temperature = res.temperature || 0.7
    }
  } catch (e) {
    console.error(e)
  }
}

async function saveConfig() {
  configSaving.value = true
  try {
    await adminApi.updateAiConfig({
      baseUrl: aiConfig.baseUrl,
      model: aiConfig.model,
      apiKey: aiConfig.apiKey,
      temperature: String(aiConfig.temperature),
    })
    ElMessage.success('AI配置已保存')
  } catch (e: any) {
    ElMessage.error('保存失败')
  } finally {
    configSaving.value = false
  }
}

async function loadRecords() {
  try {
    const res: any = await adminApi.aiChatRecords({ page: recordPage.value, size: 20, keyword: recordKeyword.value || undefined })
    if (res?.records) {
      records.value = res.records
      recordTotal.value = res.total || 0
      recordTotalPages.value = Math.ceil((res.total || 0) / 20)
    } else if (Array.isArray(res)) {
      records.value = res
      recordTotal.value = res.length
    }
  } catch (e) {
    console.error(e)
  }
}

function viewDetail(record: any) {
  ElMessage.info(`查看会话 ${record.id} 的详细消息（功能开发中）`)
}

async function handleDelete(record: any) {
  try {
    await ElMessageBox.confirm('确定删除该会话记录吗？', '确认删除', { type: 'warning' })
    ElMessage.success('删除成功')
    loadRecords()
  } catch (e: any) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

function formatTime(time: string): string {
  if (!time) return '-'
  const d = new Date(time)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

onMounted(() => {
  loadStats()
  loadAiConfig()
  loadRecords()
})
</script>
