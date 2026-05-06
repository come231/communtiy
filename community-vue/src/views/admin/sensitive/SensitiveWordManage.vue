<template>
  <div class="space-y-6">
    <!-- 操作区 -->
    <div class="bg-white rounded-xl p-5 border border-gray-100 shadow-sm">
      <div class="flex flex-wrap items-center justify-between gap-4">
        <div class="flex items-center gap-3">
          <input
            v-model="searchWord"
            type="text"
            placeholder="搜索敏感词..."
            class="px-4 py-2 bg-gray-50 border border-gray-200 rounded-lg text-sm outline-none focus:border-primary w-64"
            @keyup.enter="handleSearch"
          />
          <select v-model="levelFilter" class="px-3 py-2 bg-gray-50 border border-gray-200 rounded-lg text-sm outline-none">
            <option :value="undefined">全部级别</option>
            <option :value="1">高危</option>
            <option :value="2">中危</option>
            <option :value="3">低危</option>
          </select>
          <select v-model="typeFilter" class="px-3 py-2 bg-gray-50 border border-gray-200 rounded-lg text-sm outline-none">
            <option :value="undefined">全部分类</option>
            <option v-for="item in categoryOptions" :key="item.value" :value="item.value">{{ item.label }}</option>
          </select>
          <button @click="handleSearch" class="px-4 py-2 bg-primary text-white text-sm rounded-lg hover:bg-blue-600">搜索</button>
          <button @click="handleReset" class="px-4 py-2 border border-gray-200 text-gray-600 text-sm rounded-lg hover:bg-gray-50">重置</button>
        </div>
        <div class="flex items-center gap-3">
          <button class="px-4 py-2 bg-gradient-to-r from-primary to-blue-600 text-white text-sm rounded-lg hover:shadow-lg hover:shadow-primary/25 flex items-center gap-1.5" @click="openAddDialog">
            <Plus class="w-4 h-4" />添加敏感词
          </button>
          <button class="px-4 py-2 border border-gray-200 text-gray-600 text-sm rounded-lg hover:bg-gray-50 flex items-center gap-1.5" @click="showBatchImportDialog = true">
            <Upload class="w-4 h-4" />批量导入
          </button>
        </div>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
      <div class="bg-white rounded-xl p-5 border border-gray-100 shadow-sm">
        <div class="flex items-center gap-3">
          <div class="w-10 h-10 bg-red-50 rounded-lg flex items-center justify-center"><ShieldAlert class="w-5 h-5 text-red-500" /></div>
          <div><p class="text-2xl font-bold text-gray-800">{{ stats.totalWords }}</p><p class="text-xs text-gray-500">敏感词总量</p></div>
        </div>
      </div>
      <div class="bg-white rounded-xl p-5 border border-gray-100 shadow-sm">
        <div class="flex items-center gap-3">
          <div class="w-10 h-10 bg-orange-50 rounded-lg flex items-center justify-center"><Eye class="w-5 h-5 text-orange-500" /></div>
          <div><p class="text-2xl font-bold text-gray-800">{{ stats.todayHits }}</p><p class="text-xs text-gray-500">今日拦截次数</p></div>
        </div>
      </div>
      <div class="bg-white rounded-xl p-5 border border-gray-100 shadow-sm">
        <div class="flex items-center gap-3">
          <div class="w-10 h-10 bg-green-50 rounded-lg flex items-center justify-center"><CheckCircle class="w-5 h-5 text-green-500" /></div>
          <div><p class="text-2xl font-bold text-gray-800">{{ stats.todayNew }}</p><p class="text-xs text-gray-500">今日新增</p></div>
        </div>
      </div>
    </div>

    <!-- 敏感词列表 -->
    <div class="bg-white rounded-xl border border-gray-100 shadow-sm overflow-hidden">
      <table class="w-full text-sm">
        <thead class="bg-gray-50 border-b text-left text-xs text-gray-500 uppercase tracking-wider">
          <tr>
            <th class="px-5 py-3.5 w-16">#</th><th class="px-5 py-3.5">敏感词</th><th class="px-5 py-3.5">级别</th><th class="px-5 py-3.5">分类</th>
            <th class="px-5 py-3.5">替换为</th><th class="px-5 py-3.5">总命中</th><th class="px-5 py-3.5">今日命中</th><th class="px-5 py-3.5">状态</th><th class="px-5 py-3.5">操作</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-50">
          <tr v-for="(word, index) in words" :key="word.id" class="hover:bg-gray-50/50 transition-colors">
            <td class="px-5 py-3.5 text-gray-400 text-xs">{{ startIndex + index + 1 }}</td>
            <td class="px-5 py-3.5">
              <code class="px-2 py-1 bg-red-50 text-red-600 rounded text-xs font-mono">{{ word.word }}</code>
            </td>
            <td class="px-5 py-3.5">
              <span :class="['text-xs px-2 py-1 rounded-full font-medium', getLevelClass(word.level)]">{{ getLevelText(word.level) }}</span>
            </td>
            <td class="px-5 py-3.5 text-xs text-gray-600">{{ word.category || getCategoryLabel(word.type) }}</td>
            <td class="px-5 py-3.5">
              <code class="text-xs text-gray-500 bg-gray-50 px-2 py-0.5 rounded">{{ word.replacement || '-' }}</code>
            </td>
            <td class="px-5 py-3.5 text-xs text-gray-700">{{ word.hitCount || 0 }}</td>
            <td class="px-5 py-3.5 text-xs text-gray-700">{{ word.todayHits || 0 }}</td>
            <td class="px-5 py-3.5">
              <span :class="['text-xs px-2 py-1 rounded-full', word.enabled ? 'bg-green-50 text-green-600' : 'bg-gray-100 text-gray-400']">
                {{ word.enabled ? '启用' : '禁用' }}
              </span>
            </td>
            <td class="px-5 py-3.5">
              <div class="flex items-center gap-2">
                <button @click="openEditDialog(word)" class="text-primary hover:text-blue-700 text-xs font-medium">编辑</button>
                <button @click="handleDelete(word)" class="text-red-500 hover:text-red-700 text-xs font-medium">删除</button>
              </div>
            </td>
          </tr>
          <tr v-if="words.length === 0">
            <td colspan="9" class="px-5 py-8 text-center text-gray-400">暂无敏感词数据</td>
          </tr>
        </tbody>
      </table>
      <div class="flex items-center justify-between px-5 py-4 border-t border-gray-100">
        <span class="text-sm text-gray-500">共 {{ total }} 条</span>
        <div class="flex items-center gap-2">
          <button @click="handlePageChange(currentPage - 1)" :disabled="currentPage <= 1" class="px-3 py-1.5 text-sm border border-gray-200 rounded-lg disabled:opacity-40 hover:bg-gray-50">上一页</button>
          <span class="text-sm text-gray-600">{{ currentPage }} / {{ totalPages || 1 }}</span>
          <button @click="handlePageChange(currentPage + 1)" :disabled="currentPage >= totalPages" class="px-3 py-1.5 text-sm border border-gray-200 rounded-lg disabled:opacity-40 hover:bg-gray-50">下一页</button>
        </div>
      </div>
    </div>

    <!-- 过滤记录表 -->
    <div class="bg-white rounded-xl border border-gray-100 shadow-sm overflow-hidden">
      <div class="flex items-center justify-between px-5 py-4 border-b border-gray-100">
        <h3 class="text-sm font-semibold text-gray-700">过滤评论记录</h3>
        <div class="flex items-center gap-2">
          <input
            v-model="recordKeyword"
            type="text"
            placeholder="搜索原文/过滤后/命中词/用户名"
            class="px-3 py-2 bg-gray-50 border border-gray-200 rounded-lg text-sm outline-none w-80"
            @keyup.enter="loadRecords"
          />
          <button @click="loadRecords" class="px-3 py-2 bg-primary text-white text-sm rounded-lg hover:bg-blue-600">查询</button>
        </div>
      </div>
      <table class="w-full text-sm">
        <thead class="bg-gray-50 border-b text-left text-xs text-gray-500 uppercase tracking-wider">
          <tr>
            <th class="px-5 py-3.5">用户</th>
            <th class="px-5 py-3.5">评论原文</th>
            <th class="px-5 py-3.5">过滤后内容</th>
            <th class="px-5 py-3.5">命中敏感词</th>
            <th class="px-5 py-3.5">时间</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-50">
          <tr v-for="record in records" :key="record.id" class="hover:bg-gray-50/50 transition-colors">
            <td class="px-5 py-3.5 text-xs text-gray-700">{{ record.username || (`用户${record.userId}`) }}</td>
            <td class="px-5 py-3.5 text-xs text-gray-600">{{ record.originalContent || '-' }}</td>
            <td class="px-5 py-3.5 text-xs text-gray-700">{{ record.filteredContent || '-' }}</td>
            <td class="px-5 py-3.5">
              <span
                v-for="w in record.hitWords"
                :key="`${record.id}-${w}`"
                class="inline-flex mr-1 mb-1 px-2 py-0.5 rounded bg-red-50 text-red-600 text-xs"
              >{{ w }}</span>
            </td>
            <td class="px-5 py-3.5 text-xs text-gray-500">{{ formatTime(record.createTime) }}</td>
          </tr>
          <tr v-if="records.length === 0">
            <td colspan="5" class="px-5 py-8 text-center text-gray-400">暂无过滤记录</td>
          </tr>
        </tbody>
      </table>
      <div class="flex items-center justify-between px-5 py-4 border-t border-gray-100">
        <span class="text-sm text-gray-500">共 {{ recordTotal }} 条</span>
        <div class="flex items-center gap-2">
          <button @click="changeRecordPage(recordPage - 1)" :disabled="recordPage <= 1" class="px-3 py-1.5 text-sm border border-gray-200 rounded-lg disabled:opacity-40 hover:bg-gray-50">上一页</button>
          <span class="text-sm text-gray-600">{{ recordPage }} / {{ recordTotalPages || 1 }}</span>
          <button @click="changeRecordPage(recordPage + 1)" :disabled="recordPage >= recordTotalPages" class="px-3 py-1.5 text-sm border border-gray-200 rounded-lg disabled:opacity-40 hover:bg-gray-50">下一页</button>
        </div>
      </div>
    </div>

    <!-- 添加/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑敏感词' : '添加敏感词'" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="敏感词" prop="word">
          <el-input v-model="form.word" placeholder="请输入敏感词" />
        </el-form-item>
        <el-form-item label="级别" prop="level">
          <el-select v-model="form.level" placeholder="请选择级别" class="w-full">
            <el-option :value="1" label="高危 - 直接拒绝" />
            <el-option :value="2" label="中危 - 替换处理" />
            <el-option :value="3" label="低危 - 标记警告" />
          </el-select>
        </el-form-item>
        <el-form-item label="分类" prop="type">
          <el-select v-model="form.type" placeholder="请选择分类" class="w-full">
            <el-option v-for="item in categoryOptions" :key="item.value" :value="item.value" :label="item.label" />
          </el-select>
        </el-form-item>
        <el-form-item label="替换词" prop="replacement">
          <el-input v-model="form.replacement" placeholder="留空使用 ***" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确认</el-button>
      </template>
    </el-dialog>

    <!-- 批量导入弹窗 -->
    <el-dialog v-model="showBatchImportDialog" title="批量导入敏感词" width="600px">
      <div class="space-y-4">
        <p class="text-sm text-gray-600">每行一个敏感词，可选格式：<code class="bg-gray-100 px-1">敏感词|级别|分类</code></p>
        <el-input
          v-model="batchText"
          type="textarea"
          :rows="10"
          placeholder="违禁词1|1|政治敏感&#10;违禁词2|2|广告营销&#10;违禁词3|3|其他"
        />
        <p class="text-xs text-gray-400">级别：1-高危，2-中危，3-低危</p>
      </div>
      <template #footer>
        <el-button @click="showBatchImportDialog = false">取消</el-button>
        <el-button type="primary" @click="handleBatchImport">导入</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Upload, ShieldAlert, Eye, CheckCircle } from 'lucide-vue-next'
import { adminApi } from '@/api'

interface SensitiveWord {
  id: number
  word: string
  level: number
  type?: number
  category: string
  replacement?: string
  enabled: boolean
  hitCount?: number
  todayHits?: number
}

interface SensitiveFilterRecord {
  id: number
  commentId?: number
  userId?: number
  username?: string
  originalContent?: string
  filteredContent?: string
  hitWords: string[]
  createTime?: string
}

const searchWord = ref('')
const levelFilter = ref<number | undefined>(undefined)
const typeFilter = ref<number | undefined>(undefined)
const currentPage = ref(1)
const pageSize = ref(15)
const total = ref(0)
const totalPages = ref(1)
const startIndex = ref(0)
const words = ref<SensitiveWord[]>([])
const stats = reactive({
  totalWords: 0,
  totalHits: 0,
  todayHits: 0,
  todayNew: 0
})

const records = ref<SensitiveFilterRecord[]>([])
const recordKeyword = ref('')
const recordPage = ref(1)
const recordPageSize = ref(8)
const recordTotal = ref(0)
const recordTotalPages = ref(1)

const dialogVisible = ref(false)
const showBatchImportDialog = ref(false)
const isEdit = ref(false)
const formRef = ref()

const form = reactive({
  id: 0,
  word: '',
  level: 1,
  type: 0,
  replacement: ''
})

const rules = {
  word: [{ required: true, message: '请输入敏感词', trigger: 'blur' }],
  level: [{ required: true, message: '请选择级别', trigger: 'change' }],
  type: [{ required: true, message: '请选择分类', trigger: 'change' }]
}

let batchText = ref('')
const categoryOptions = [
  { value: 0, label: '其他' },
  { value: 1, label: '广告营销' },
  { value: 2, label: '政治敏感' },
  { value: 3, label: '暴力恐吓' },
  { value: 4, label: '色情低俗' }
]

async function loadWords() {
  try {
    const res: any = await adminApi.sensitiveWords({
      keyword: searchWord.value || undefined,
      page: currentPage.value,
      size: pageSize.value,
      level: levelFilter.value,
      type: typeFilter.value
    })
    
    if (res?.records) {
      words.value = res.records.map((w: any) => ({
        ...w,
        type: w.type ?? 0,
        category: w.category || getCategoryLabel(w.type),
        replacement: w.replacement || w.replaceWord || '***',
        enabled: w.enabled !== false && w.status !== 0
      }))
      total.value = res.total || 0
      totalPages.value = Math.ceil((res.total || 0) / pageSize.value)
      startIndex.value = (currentPage.value - 1) * pageSize.value
    } else if (Array.isArray(res)) {
      words.value = res.map((w: any) => ({
        ...w,
        enabled: w.enabled !== false
      }))
      total.value = res.length
      totalPages.value = 1
    }
  } catch (e: any) {
    ElMessage.error('加载敏感词列表失败')
    console.error(e)
  }
}

function handleSearch() {
  currentPage.value = 1
  loadWords()
}

function handleReset() {
  searchWord.value = ''
  levelFilter.value = undefined
  typeFilter.value = undefined
  currentPage.value = 1
  loadWords()
}

function handlePageChange(page: number) {
  if (page < 1 || page > (totalPages.value || 1)) return
  currentPage.value = page
  loadWords()
}

function getLevelClass(level: number): string {
  const c: Record<number, string> = {
    1: 'bg-red-50 text-red-600',
    2: 'bg-orange-50 text-orange-600',
    3: 'bg-yellow-50 text-yellow-600'
  }
  return c[level] || 'bg-gray-100'
}

function getLevelText(level: number): string {
  const t: Record<number, string> = {
    1: '高危',
    2: '中危',
    3: '低危'
  }
  return t[level] || '未知'
}

function openAddDialog() {
  isEdit.value = false
  form.id = 0
  form.word = ''
  form.level = 1
  form.type = 0
  form.replacement = ''
  dialogVisible.value = true
}

function openEditDialog(word: SensitiveWord) {
  isEdit.value = true
  form.id = word.id
  form.word = word.word
  form.level = word.level
  form.type = word.type ?? 0
  form.replacement = word.replacement || ''
  dialogVisible.value = true
}

async function handleSubmit() {
  try {
    await formRef.value?.validate()
    const data = {
      word: form.word,
      level: form.level,
      type: form.type,
      replacement: form.replacement || undefined
    }
    
    if (isEdit.value) {
      await adminApi.updateSensitiveWord(form.id, data)
      ElMessage.success('修改成功')
    } else {
      await adminApi.addSensitiveWord(data)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    loadWords()
  } catch (e: any) {
    if (e !== false) {
      ElMessage.error(isEdit.value ? '修改失败' : '添加失败')
    }
  }
}

async function handleDelete(word: SensitiveWord) {
  try {
    await ElMessageBox.confirm(`确定要删除敏感词 "${word.word}" 吗？`, '确认删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await adminApi.deleteSensitiveWord(word.id)
    ElMessage.success('删除成功')
    loadWords()
  } catch (e: any) {
    if (e !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

async function handleBatchImport() {
  if (!batchText.value.trim()) {
    ElMessage.warning('请输入要导入的敏感词')
    return
  }
  
  try {
    const lines = batchText.value.trim().split('\n').filter(l => l.trim())
    const words = lines.map(line => {
      const parts = line.split('|')
      return {
        word: parts[0].trim(),
        level: parseInt(parts[1]) || 1,
        category: parts[2]?.trim() || '其他'
      }
    }).filter(w => w.word)
    
    if (words.length === 0) {
      ElMessage.warning('没有有效的敏感词')
      return
    }
    
    await adminApi.batchImportSensitiveWords(words.map(w => w.word))
    ElMessage.success(`成功导入 ${words.length} 个敏感词`)
    showBatchImportDialog.value = false
    batchText.value = ''
    loadWords()
  } catch (e: any) {
    ElMessage.error('导入失败')
    console.error(e)
  }
}

async function loadStats() {
  try {
    const res: any = await adminApi.sensitiveWordStats()
    stats.totalWords = res?.totalWords || 0
    stats.totalHits = res?.totalHits || 0
    stats.todayHits = res?.todayHits || 0
    stats.todayNew = res?.todayNew || 0
  } catch (e) {
    console.error(e)
  }
}

async function loadRecords() {
  try {
    const res: any = await adminApi.sensitiveWordRecords({
      page: recordPage.value,
      size: recordPageSize.value,
      keyword: recordKeyword.value || undefined
    })
    records.value = (res?.records || []).map((item: any) => ({
      ...item,
      hitWords: Array.isArray(item.hitWords)
        ? item.hitWords
        : typeof item.hitWords === 'string'
          ? item.hitWords.split(',').filter((v: string) => v?.trim())
          : []
    }))
    recordTotal.value = res?.total || 0
    recordTotalPages.value = Math.ceil((res?.total || 0) / recordPageSize.value)
  } catch (e) {
    ElMessage.error('加载过滤记录失败')
  }
}

function changeRecordPage(page: number) {
  if (page < 1 || page > (recordTotalPages.value || 1)) return
  recordPage.value = page
  loadRecords()
}

function getCategoryLabel(type?: number): string {
  const item = categoryOptions.find(v => v.value === (type ?? 0))
  return item?.label || '其他'
}

function formatTime(value?: string): string {
  if (!value) return '-'
  return value.replace('T', ' ')
}

onMounted(() => {
  loadWords()
  loadStats()
  loadRecords()
})
</script>
