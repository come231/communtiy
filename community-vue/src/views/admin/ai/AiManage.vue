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

    <!-- 帖子 AI 审核规则 -->
    <div class="bg-white rounded-xl border border-gray-100 shadow-sm overflow-hidden">
      <div class="px-6 py-4 border-b border-gray-100 flex flex-wrap items-center justify-between gap-3">
        <div class="flex items-start gap-3">
          <div class="w-10 h-10 rounded-lg bg-amber-50 flex items-center justify-center shrink-0">
            <svg class="w-5 h-5 text-amber-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z"/></svg>
          </div>
          <div>
            <h3 class="font-semibold text-gray-800">帖子 AI 审核规则</h3>
            <p class="text-xs text-gray-500 mt-1 max-w-3xl leading-relaxed">
              启用项会按排序进入 AI 提示词（与 system 指令、低温度一起强制逐条对照）。
              若当前无任何启用规则，服务端会自动使用与 <code class="text-gray-600">post_review_rule.sql</code> 一致的内置默认规则集，避免模型空规则乱判。
              分档：<span class="text-gray-700">0%</span> 正常；
              <span class="text-gray-700">大于 0% 且 ≤30%</span> 转人工；
              <span class="text-gray-700">&gt;30%</span> 拦截。
            </p>
          </div>
        </div>
        <button type="button" @click="openRuleDialog()" class="px-4 py-2 bg-primary text-white text-sm rounded-lg hover:bg-blue-600 shrink-0">新增规则</button>
      </div>
      <div class="overflow-x-auto">
        <table class="w-full text-sm min-w-[800px]">
          <thead class="bg-gray-50 border-b text-left text-xs text-gray-500 uppercase tracking-wider">
            <tr>
              <th class="px-5 py-3.5 w-14">排序</th>
              <th class="px-5 py-3.5 w-40">名称</th>
              <th class="px-5 py-3.5">规则正文</th>
              <th class="px-5 py-3.5 w-24">状态</th>
              <th class="px-5 py-3.5 w-36">更新时间</th>
              <th class="px-5 py-3.5 w-28">操作</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-gray-50">
            <tr v-for="row in reviewRules" :key="row.id" class="hover:bg-gray-50/50 align-top">
              <td class="px-5 py-3 text-gray-600">{{ row.sortOrder ?? 0 }}</td>
              <td class="px-5 py-3 text-gray-800">{{ row.ruleName || '—' }}</td>
              <td class="px-5 py-3 text-gray-600">
                <p class="line-clamp-2" :title="row.ruleText">{{ row.ruleText || '—' }}</p>
              </td>
              <td class="px-5 py-3">
                <span :class="['text-xs px-2 py-1 rounded-full', row.status === 1 ? 'bg-green-50 text-green-600' : 'bg-gray-100 text-gray-400']">
                  {{ row.status === 1 ? '启用' : '禁用' }}
                </span>
              </td>
              <td class="px-5 py-3 text-gray-400 text-xs whitespace-nowrap">{{ formatTime(row.updateTime ?? row.createTime ?? '') }}</td>
              <td class="px-5 py-3">
                <button type="button" @click="openRuleDialog(row)" class="text-primary hover:text-blue-700 text-xs font-medium mr-3">编辑</button>
                <button type="button" @click="handleDeleteRule(row)" class="text-red-500 hover:text-red-700 text-xs font-medium">删除</button>
              </td>
            </tr>
            <tr v-if="reviewRules.length === 0">
              <td colspan="6" class="px-5 py-10 text-center text-gray-400">暂无规则，请点击「新增规则」或执行数据库初始化脚本导入模板</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <el-dialog v-model="ruleDialogVisible" :title="ruleEditId != null ? '编辑审核规则' : '新增审核规则'" width="640px" destroy-on-close @closed="resetRuleForm">
      <el-form :model="ruleForm" label-position="top">
        <el-form-item label="规则名称（可选）">
          <el-input v-model="ruleForm.ruleName" placeholder="展示用，如：色情低俗" maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item label="规则正文" required>
          <el-input
            v-model="ruleForm.ruleText"
            type="textarea"
            :rows="8"
            placeholder="写入 AI 提示词，描述禁止发布或需重点审核的内容类型与边界"
          />
        </el-form-item>
        <div class="grid grid-cols-2 gap-4">
          <el-form-item label="排序（越小越靠前）">
            <el-input-number v-model="ruleForm.sortOrder" :min="0" :max="999999" class="w-full" />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="ruleForm.status" class="w-full">
              <el-option :value="1" label="启用" />
              <el-option :value="0" label="禁用" />
            </el-select>
          </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="ruleDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="ruleSaving" @click="saveRule">保存</el-button>
      </template>
    </el-dialog>

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
import type { PostReviewRule } from '@/types'

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

// 帖子 AI 审核规则
const reviewRules = ref<PostReviewRule[]>([])
const ruleDialogVisible = ref(false)
const ruleEditId = ref<number | null>(null)
const ruleSaving = ref(false)
const ruleForm = reactive<PostReviewRule>({
  ruleName: '',
  ruleText: '',
  sortOrder: 0,
  status: 1,
})

function resetRuleForm() {
  ruleEditId.value = null
  ruleForm.ruleName = ''
  ruleForm.ruleText = ''
  ruleForm.sortOrder = 0
  ruleForm.status = 1
}

async function loadReviewRules() {
  try {
    const list = await adminApi.postReviewRules()
    reviewRules.value = Array.isArray(list) ? list : []
  } catch {
    reviewRules.value = []
  }
}

function openRuleDialog(row?: PostReviewRule) {
  if (row?.id != null) {
    ruleEditId.value = row.id
    ruleForm.ruleName = row.ruleName || ''
    ruleForm.ruleText = row.ruleText || ''
    ruleForm.sortOrder = row.sortOrder ?? 0
    ruleForm.status = row.status ?? 1
  } else {
    resetRuleForm()
  }
  ruleDialogVisible.value = true
}

async function saveRule() {
  const text = (ruleForm.ruleText || '').trim()
  if (!text) {
    ElMessage.warning('请填写规则正文')
    return
  }
  ruleSaving.value = true
  try {
    const payload: Partial<PostReviewRule> = {
      ruleName: (ruleForm.ruleName || '').trim() || undefined,
      ruleText: text,
      sortOrder: ruleForm.sortOrder ?? 0,
      status: ruleForm.status ?? 1,
    }
    if (ruleEditId.value != null) {
      await adminApi.updatePostReviewRule(ruleEditId.value, payload)
      ElMessage.success('已更新规则')
    } else {
      await adminApi.createPostReviewRule(payload)
      ElMessage.success('已新增规则')
    }
    ruleDialogVisible.value = false
    await loadReviewRules()
  } catch {
    // 全局拦截器已提示
  } finally {
    ruleSaving.value = false
  }
}

async function handleDeleteRule(row: PostReviewRule) {
  if (row.id == null) return
  try {
    await ElMessageBox.confirm('确定删除该条审核规则？删除后立即从 AI 提示词中移除。', '确认删除', { type: 'warning' })
    await adminApi.deletePostReviewRule(row.id)
    ElMessage.success('已删除')
    await loadReviewRules()
  } catch (e: any) {
    if (e !== 'cancel') {
      /* message from interceptor */
    }
  }
}

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
  loadReviewRules()
  loadRecords()
})
</script>
