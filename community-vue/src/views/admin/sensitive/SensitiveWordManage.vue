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
          />
          <select v-model="levelFilter" class="px-3 py-2 bg-gray-50 border border-gray-200 rounded-lg text-sm outline-none">
            <option value="">全部级别</option>
            <option value="high">高危</option>
            <option value="medium">中危</option>
            <option value="low">低危</option>
          </select>
          <button class="px-4 py-2 bg-primary text-white text-sm rounded-lg hover:bg-blue-600">搜索</button>
        </div>
        <div class="flex items-center gap-3">
          <button class="px-4 py-2 bg-gradient-to-r from-primary to-blue-600 text-white text-sm rounded-lg hover:shadow-lg hover:shadow-primary/25 flex items-center gap-1.5" @click="showAddDialog = true">
            <Plus class="w-4 h-4" />添加敏感词
          </button>
          <button class="px-4 py-2 border border-gray-200 text-gray-600 text-sm rounded-lg hover:bg-gray-50 flex items-center gap-1.5">
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
          <div><p class="text-2xl font-bold text-gray-800">{{ totalWords }}</p><p class="text-xs text-gray-500">敏感词总量</p></div>
        </div>
      </div>
      <div class="bg-white rounded-xl p-5 border border-gray-100 shadow-sm">
        <div class="flex items-center gap-3">
          <div class="w-10 h-10 bg-orange-50 rounded-lg flex items-center justify-center"><Eye class="w-5 h-5 text-orange-500" /></div>
          <div><p class="text-2xl font-bold text-gray-800">{{ todayHits }}</p><p class="text-xs text-gray-500">今日拦截次数</p></div>
        </div>
      </div>
      <div class="bg-white rounded-xl p-5 border border-gray-100 shadow-sm">
        <div class="flex items-center gap-3">
          <div class="w-10 h-10 bg-green-50 rounded-lg flex items-center justify-center"><CheckCircle class="w-5 h-5 text-green-500" /></div>
          <div><p class="text-2xl font-bold text-gray-800">{{ todayNew }}</p><p class="text-xs text-gray-500">今日新增</p></div>
        </div>
      </div>
    </div>

    <!-- 敏感词列表 -->
    <div class="bg-white rounded-xl border border-gray-100 shadow-sm overflow-hidden">
      <table class="w-full text-sm">
        <thead class="bg-gray-50 border-b text-left text-xs text-gray-500 uppercase tracking-wider">
          <tr><th class="px-5 py-3.5 w-16">#</th><th class="px-5 py-3.5">敏感词</th><th class="px-5 py-3.5">级别</th><th class="px-5 py-3.5">分类</th><th class="px-5 py-3.5">替换为</th><th class="px-5 py-3.5">命中次数</th><th class="px-5 py-3.5">状态</th><th class="px-5 py-3.5">操作</th></tr>
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
            <td class="px-5 py-3.5 text-xs text-gray-600">{{ word.category }}</td>
            <td class="px-5 py-3.5">
              <code class="text-xs text-gray-500 bg-gray-50 px-2 py-0.5 rounded">{{ word.replaceWith || '-' }}</code>
            </td>
            <td class="px-5 py-3.5">
              <span class="text-sm font-medium text-gray-800">{{ word.hitCount.toLocaleString() }}</span>
              <span v-if="word.todayHits > 0" class="text-xs text-orange-500 ml-1">+{{ word.todayHits }}</span>
            </td>
            <td class="px-5 py-3.5">
              <span :class="['text-xs px-2 py-1 rounded-full', word.enabled ? 'bg-green-50 text-green-600' : 'bg-gray-100 text-gray-400']">
                {{ word.enabled ? '启用' : '禁用' }}
              </span>
            </td>
            <td class="px-5 py-3.5">
              <div class="flex items-center gap-2">
                <button class="text-primary hover:text-blue-700 text-xs font-medium">编辑</button>
                <button class="text-red-500 hover:text-red-700 text-xs font-medium">删除</button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
      <div class="flex items-center justify-between px-5 py-4 border-t border-gray-100">
        <span class="text-sm text-gray-500">共 {{ totalWords }} 条</span>
        <div class="flex items-center gap-2">
          <button class="px-3 py-1.5 text-sm border border-gray-200 rounded-lg disabled:opacity-40" disabled>上一页</button>
          <span class="text-sm text-gray-600">1 / {{ Math.ceil(totalWords / pageSize) }}</span>
          <button class="px-3 py-1.5 text-sm border border-gray-200 rounded-lg disabled:opacity-40" disabled>下一页</button>
        </div>
      </div>
    </div>

    <!-- 添加弹窗 -->
    <div v-if="showAddDialog" class="fixed inset-0 bg-black/40 flex items-center justify-center z-50" @click.self="showAddDialog = false">
      <div class="bg-white rounded-2xl p-6 w-full max-w-lg shadow-2xl">
        <h3 class="text-lg font-semibold text-gray-800 mb-4">添加敏感词</h3>
        <div class="space-y-4">
          <div><label class="block text-sm font-medium text-gray-700 mb-1.5">敏感词</label>
            <input type="text" placeholder="输入要过滤的词汇..." class="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm outline-none focus:border-primary" /></div>
          <div><label class="block text-sm font-medium text-gray-700 mb-1.5">级别</label>
            <select class="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm outline-none focus:border-primary">
              <option value="high">高危 - 直接拒绝</option>
              <option value="medium">中危 - 替换处理</option>
              <option value="low">低危 - 标记警告</option>
            </select>
          </div>
          <div><label class="block text-sm font-medium text-gray-700 mb-1.5">分类</label>
            <select class="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm outline-none focus:border-primary">
              <option>政治敏感</option><option>色情低俗</option><option>暴力恐吓</option><option>广告营销</option><option>人身攻击</option><option>其他</option>
            </select>
          </div>
          <div><label class="block text-sm font-medium text-gray-700 mb-1.5">替换为（选填）</label>
            <input type="text" placeholder="留空则使用默认替换符号 ***" class="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm outline-none focus:border-primary" /></div>
        </div>
        <div class="flex justify-end gap-3 mt-6">
          <button class="px-4 py-2 border border-gray-200 text-gray-600 text-sm rounded-lg hover:bg-gray-50" @click="showAddDialog = false">取消</button>
          <button class="px-4 py-2 bg-primary text-white text-sm rounded-lg hover:bg-blue-600" @click="showAddDialog = false">确认添加</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { Plus, Upload, ShieldAlert, Eye, CheckCircle } from 'lucide-vue-next'

const searchWord = ref('')
const levelFilter = ref('')
const showAddDialog = ref(false)
const totalWords = ref(356)
const todayHits = ref(128)
const todayNew = ref(12)
const startIndex = ref(0)
const pageSize = 15

const words = [
  { id: 1, word: '违禁词示例1', level: 'high', category: '政治敏感', replaceWith: '***', hitCount: 15234, todayHits: 45, enabled: true },
  { id: 2, word: '广告推广', level: 'medium', category: '广告营销', replaceWith: '[广告]', hitCount: 8921, todayHits: 23, enabled: true },
  { id: 3, word: '辱骂词汇', level: 'high', category: '人身攻击', replaceWith: '', hitCount: 5672, todayHits: 18, enabled: true },
  { id: 4, word: '低俗用语', level: 'medium', category: '色情低俗', replaceWith: '**', hitCount: 3456, todayHits: 12, enabled: true },
  { id: 5, word: '虚假宣传', level: 'low', category: '广告营销', replaceWith: null, hitCount: 2134, todayHits: 5, enabled: true },
  { id: 6, word: '已禁用的词', level: 'low', category: '其他', replaceWith: null, hitCount: 0, todayHits: 0, enabled: false },
  { id: 7, word: '暴力威胁', level: 'high', category: '暴力恐吓', replaceWith: '', hitCount: 987, todayHits: 3, enabled: true },
]

function getLevelClass(level: string): string {
  const c: Record<string, string> = { high: 'bg-red-50 text-red-600', medium: 'bg-orange-50 text-orange-600', low: 'bg-yellow-50 text-yellow-600' }
  return c[level] || 'bg-gray-100'
}
function getLevelText(level: string): string {
  const t: Record<string, string> = { high: '高危', medium: '中危', low: '低危' }
  return t[level] || '未知'
}
</script>
