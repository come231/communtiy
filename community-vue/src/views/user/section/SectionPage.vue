<template>
  <div class="min-h-screen bg-[#F5F7FA] pt-20 pb-8">
    <div class="max-w-6xl mx-auto px-4">
      <!-- 页头 -->
      <div class="mb-8">
        <h1 class="text-2xl font-bold text-gray-800 flex items-center gap-3">
          <LayoutGrid class="w-7 h-7 text-blue-500" />
          社区板块
        </h1>
        <p class="text-gray-500 mt-2">探索不同领域的精彩内容，找到你感兴趣的板块</p>
      </div>

      <!-- 搜索栏 -->
      <div class="relative mb-8 max-w-md">
        <Search class="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400 pointer-events-none" />
        <input v-model="keyword" placeholder="搜索板块..." type="search"
          class="w-full pl-12 pr-4 py-3 bg-white rounded-xl border-none shadow-sm focus:ring-2 focus:ring-blue-400 focus:shadow-md transition-all outline-none text-sm" />
      </div>

      <!-- 板块网格 -->
      <div v-if="filtered.length > 0" class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-5">
        <div v-for="section in filtered" :key="section.id" @click="goSection(section)"
          class="group bg-white rounded-xl p-6 shadow-sm hover:shadow-lg hover:-translate-y-1 transition-all duration-300 cursor-pointer border border-transparent hover:border-blue-100">
          <div class="flex items-start justify-between mb-4">
            <div class="w-14 h-14 rounded-2xl bg-gradient-to-br flex items-center justify-center text-2xl shadow-inner"
              :style="{ background: getGradient(section.id) }">
              {{ section.icon || section.name.charAt(0) }}
            </div>
            <span class="text-xs text-gray-400 bg-gray-50 px-2.5 py-1 rounded-full">{{ section.postCount || 0 }} 帖子</span>
          </div>
          <h3 class="font-semibold text-gray-800 group-hover:text-blue-600 transition-colors mb-1.5">{{ section.name }}</h3>
          <p class="text-sm text-gray-500 line-clamp-2 leading-relaxed">{{ section.description || '探索更多精彩内容' }}</p>
          <div class="mt-4 pt-4 border-t border-dashed border-gray-100 flex items-center justify-between">
            <div class="flex -space-x-2">
              <div v-for="i in 3" :key="i"
                class="w-6 h-6 rounded-full bg-gradient-to-br ring-2 ring-white"
                :style="{ background: `hsl(${(section.id * 47 + i * 90) % 360}, 70%, 75%)` }"></div>
            </div>
            <ArrowRight class="w-4 h-4 text-gray-300 group-hover:text-blue-500 group-hover:translate-x-1 transition-all" />
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-if="filtered.length === 0 && !loading" class="text-center py-20">
        <Inbox class="w-16 h-16 text-gray-300 mx-auto mb-4" />
        <p class="text-gray-400">未找到相关板块</p>
      </div>

      <!-- 加载中 -->
      <div v-if="loading" class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-5">
        <div v-for="i in 8" :key="i" class="bg-white rounded-xl p-6 animate-pulse">
          <div class="w-14 h-14 rounded-2xl bg-gray-200 mb-4"></div>
          <div class="h-5 bg-gray-200 rounded w-24 mb-2"></div>
          <div class="h-4 bg-gray-100 rounded w-full mb-1"></div>
          <div class="h-4 bg-gray-100 rounded w-2/3"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { LayoutGrid, Search, ArrowRight, Inbox } from 'lucide-vue-next'
import { sectionApi, postApi } from '../../../api'

const router = useRouter()
const route = useRoute()
const loading = ref(true)
const keyword = ref('')
const sections = ref<any[]>([])

const filtered = computed(() => {
  if (!keyword.value.trim()) return sections.value
  return sections.value.filter((s: any) =>
    s.name.includes(keyword.value.trim()) ||
    (s.description && s.description.includes(keyword.value.trim()))
  )
})

const gradients = [
  'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
  'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
  'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
  'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
  'linear-gradient(135deg, #fa709a 0%, #fee140 100%)',
  'linear-gradient(135deg, #a18cd1 0%, #fbc2eb 100%)',
  'linear-gradient(135deg, #fccb90 0%, #d57eeb 100%)',
  'linear-gradient(135deg, #f6d365 0%, #fda085 100%)',
  'linear-gradient(135deg, #84fab0 0%, #8fd3f4 100%)',
  'linear-gradient(135deg, #a1c4fd 0%, #c2e9fb 100%)',
]

function getGradient(id: number): string {
  return gradients[id % gradients.length]
}

function goSection(section: any) {
  router.push({ path: '/', query: { sectionId: String(section.id), sectionName: section.name } })
}

onMounted(async () => {
  try {
    const res: any = await sectionApi.list()
    sections.value = res || []
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }

  // 从路由query获取初始筛选
  if (route.query.keyword) {
    keyword.value = route.query.keyword as string
  }
})
</script>
