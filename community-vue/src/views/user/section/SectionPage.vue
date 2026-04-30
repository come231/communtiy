<template>
  <div class="min-h-screen bg-gradient-to-br from-[#f0f4ff] to-[#f8faff] py-8">
    <div class="max-w-[1100px] mx-auto px-5">
      <!-- 搜索框 -->
      <div class="mb-8">
        <div class="bg-white rounded-[12px] px-4 py-3 flex items-center gap-3 shadow-sm w-full md:w-[320px]">
          <i class="fa fa-search text-[#6b7280]"></i>
          <input
            v-model="searchKeyword"
            type="text"
            placeholder="搜索板块..."
            class="bg-transparent outline-none flex-1 text-sm text-[#1f2937]"
            @input="handleSearch"
          />
        </div>
      </div>

      <!-- 页面标题 -->
      <div class="mb-8">
        <h2 class="text-[28px] font-bold text-[#1f2937] flex items-center gap-2.5 mb-2">
          <i class="fa fa-th-large text-[#6366f1]"></i> 全部板块
        </h2>
        <p class="text-[#6b7280]">找到你感兴趣的圈子，加入一起交流</p>
      </div>

      <!-- 热门板块推荐 -->
      <div class="mb-8">
        <div class="flex items-center justify-between mb-4">
          <div class="text-lg font-semibold flex items-center gap-2">
            <i class="fa fa-fire text-[#ec4899]"></i> 🔥 热门板块推荐
          </div>
          <div class="text-xs text-[#6b7280]">根据成员数和帖子活跃度排序</div>
        </div>

        <!-- 热门板块卡片网格 -->
        <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div
            v-for="(hot, index) in topHotSections"
            :key="hot.sectionId"
            @click="goSection(hot)"
            class="relative bg-white rounded-[16px] overflow-hidden shadow-[0_10px_30px_-5px_rgba(99,102,241,0.15)] cursor-pointer transition-all duration-300 hover:-translate-y-1 hover:shadow-[0_20px_40px_-5px_rgba(99,102,241,0.25)] group"
          >
            <!-- 排名徽章 -->
            <div
              class="absolute top-3 left-3 w-7 h-7 rounded-full flex items-center justify-center text-white font-bold text-sm z-10 shadow-md"
              :class="index === 0 ? 'bg-gradient-to-r from-[#fbbf24] to-[#f59e0b]' : index === 1 ? 'bg-gradient-to-r from-[#9ca3af] to-[#6b7280]' : 'bg-gradient-to-r from-[#b45309] to-[#92400e]'"
            >
              {{ index + 1 }}
            </div>

            <!-- 封面图 -->
            <div class="h-[100px] overflow-hidden">
              <img
                v-if="hot.cover"
                :src="resolveFileUrl(hot.cover)"
                class="w-full h-full object-cover transition-transform duration-500 group-hover:scale-110"
              />
              <div
                v-else
                class="w-full h-full flex items-center justify-center"
                :style="{ background: `linear-gradient(135deg, hsl(${((hot.sectionId || 0) * 47) % 360}, 70%, 60%), hsl(${((hot.sectionId || 0) * 47 + 40) % 360}, 70%, 50%))` }"
              >
                <span class="text-3xl font-bold text-white/80">{{ (hot.name || '板块')[0] }}</span>
              </div>
            </div>

            <!-- 内容 -->
            <div class="p-4">
              <h4 class="font-bold text-[#1f2937] mb-1 truncate">{{ hot.name }}</h4>
              <p class="text-[#6b7280] text-xs mb-3 line-clamp-1">{{ hot.intro || '暂无介绍' }}</p>

              <!-- 统计 -->
              <div class="flex items-center gap-3 text-xs text-[#6b7280]">
                <span class="flex items-center gap-1">
                  <i class="fa fa-users text-[#6366f1]"></i>
                  {{ formatNumber(hot.memberCount || 0) }}
                </span>
                <span class="flex items-center gap-1">
                  <i class="fa fa-file-text text-[#a855f7]"></i>
                  {{ formatNumber(hot.postCount || 0) }}
                </span>
                <span v-if="hot.categoryName" class="ml-auto px-2 py-0.5 rounded-full bg-[#f0f4ff] text-[#6366f1] text-[10px]">
                  {{ hot.categoryName }}
                </span>
              </div>

              <!-- 热度条 -->
              <div class="mt-3">
                <div class="h-1.5 bg-[#e5e7eb] rounded-full overflow-hidden">
                  <div
                    class="h-full rounded-full bg-gradient-to-r from-[#ec4899] to-[#f97316]"
                    :style="{ width: calculateHeatPercent(hot) + '%' }"
                  ></div>
                </div>
                <div class="text-[10px] text-[#6b7280] mt-1 text-right">热度 {{ calculateHeat(hot) }}</div>
              </div>
            </div>
          </div>
        </div>

        <!-- 更多热门列表 -->
        <div class="mt-4 bg-white rounded-[14px] p-4 shadow-[0_4px_15px_rgba(0,0,0,0.05)]">
          <div class="flex items-center gap-4 overflow-x-auto pb-2">
            <div
              v-for="hot in moreHotSections"
              :key="hot.sectionId"
              @click="goSection(hot)"
              class="flex items-center gap-3 px-3 py-2 rounded-[10px] hover:bg-[#f0f4ff] cursor-pointer transition-all whitespace-nowrap min-w-[180px]"
            >
              <img
                :src="hot.cover || 'https://picsum.photos/100/100?' + hot.sectionId"
                class="w-8 h-8 rounded-lg object-cover"
              />
              <div class="flex-1 min-w-0">
                <div class="font-medium text-sm text-[#1f2937] truncate">{{ hot.name }}</div>
                <div class="text-xs text-[#6b7280]">
                  {{ formatNumber(hot.memberCount || 0) }} 成员 · {{ formatNumber(hot.postCount || 0) }} 帖子
                </div>
              </div>
              <i class="fa fa-chevron-right text-[#d1d5db] text-xs"></i>
            </div>
            <div v-if="moreHotSections.length === 0 && topHotSections.length === 0" class="text-center py-2 text-[#6b7280] text-sm w-full">
              暂无更多热门板块
            </div>
          </div>
        </div>
      </div>

      <!-- 分类标签 -->
      <div class="flex flex-wrap gap-3 mb-9">
        <div
          v-for="cat in categories"
          :key="cat.categoryId || cat.name"
          @click="selectCategory(cat)"
          :class="[
            'px-5 py-2.5 rounded-[12px] cursor-pointer transition-all font-semibold text-sm',
            selectedCategory?.categoryId === cat.categoryId || selectedCategory?.name === cat.name
              ? 'bg-gradient-to-r from-[#6366f1] to-[#a855f7] text-white'
              : 'bg-white text-[#6b7280] hover:bg-[#f0f4ff] hover:text-[#6366f1] hover:-translate-y-0.5'
          ]"
          class="shadow-[0_3px_10px_rgba(0,0,0,0.05)]"
        >
          {{ cat.name }}
        </div>
      </div>

      <!-- 板块网格 -->
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 mb-8">
        <div
          v-for="section in filteredSections"
          :key="section.sectionId"
          class="bg-white rounded-[20px] overflow-hidden shadow-[0_10px_30px_-5px_rgba(99,102,241,0.15)] transition-all duration-300 hover:-translate-y-2 hover:shadow-[0_20px_40px_-5px_rgba(99,102,241,0.25)]"
        >
          <!-- 封面图 -->
          <div @click="goSection(section)" class="cursor-pointer">
            <img
              v-if="section.cover"
              :src="resolveFileUrl(section.cover)"
              class="w-full h-[120px] object-cover"
            />
            <div
              v-else
              class="w-full h-[120px] flex items-center justify-center"
              :style="{ background: `linear-gradient(135deg, hsl(${((section.sectionId || section.id || 0) * 47) % 360}, 70%, 60%), hsl(${((section.sectionId || section.id || 0) * 47 + 40) % 360}, 70%, 50%))` }"
            >
              <span class="text-2xl font-bold text-white/80">{{ (section.name || '板块')[0] }}</span>
            </div>
          </div>

          <!-- 内容区 -->
          <div class="p-5">
            <div class="flex justify-between items-start mb-3">
              <h3
                @click="goSection(section)"
                class="text-lg font-bold text-[#1f2937] cursor-pointer hover:text-[#6366f1] transition-colors"
              >
                {{ section.name }}
              </h3>
              <!-- 分类标签 -->
              <span
                v-if="section.categoryName"
                class="text-xs px-2 py-1 rounded-full bg-[#f0f4ff] text-[#6366f1]"
              >
                {{ section.categoryName }}
              </span>
            </div>
            <p class="text-[#6b7280] text-[13px] mb-4 leading-relaxed line-clamp-2">
              {{ section.intro || '暂无介绍' }}
            </p>

            <!-- 统计信息 -->
            <div class="flex gap-4 mb-4 text-[13px] text-[#6b7280]">
              <span><i class="fa fa-users mr-1"></i> {{ section.memberCount || 0 }} 成员</span>
              <span><i class="fa fa-file-text mr-1"></i> {{ section.postCount || 0 }} 帖子</span>
            </div>

            <!-- 加入/已加入按钮 -->
            <button
              v-if="section.isJoined"
              @click="leaveSection(section)"
              class="w-full py-2.5 rounded-[12px] border-none font-semibold cursor-pointer transition-all bg-[#e5e7eb] text-[#6b7280] hover:bg-[#d1d5db]"
            >
              已加入
            </button>
            <button
              v-else
              @click="joinSection(section)"
              class="w-full py-2.5 rounded-[12px] border-none font-semibold cursor-pointer transition-all bg-gradient-to-r from-[#6366f1] to-[#a855f7] text-white hover:scale-[1.03]"
            >
              加入板块
            </button>
          </div>
        </div>
      </div>

      <!-- 创建板块按钮 -->
      <div class="text-center mb-8">
        <button
          @click="showCreateDialog"
          class="px-6 py-3 rounded-[12px] bg-gradient-to-r from-[#6366f1] to-[#a855f7] text-white font-semibold shadow-lg shadow-indigo-200 hover:shadow-xl hover:-translate-y-0.5 transition-all"
        >
          <i class="fa fa-plus mr-2"></i> 创建板块
        </button>
      </div>

      <!-- 空状态 -->
      <div v-if="filteredSections.length === 0" class="text-center py-16">
        <i class="fa fa-folder-open text-4xl text-[#d1d5db] mb-4"></i>
        <p class="text-[#6b7280]">暂无板块，快来创建第一个板块吧~</p>
      </div>
    </div>

    <!-- 创建板块对话框 -->
    <el-dialog
      v-model="createDialogVisible"
      title="创建板块"
      width="520px"
      destroy-on-close
      class="!rounded-[18px]"
    >
      <el-form :model="createForm" label-width="90px">
        <el-form-item label="板块名称">
          <el-input v-model="createForm.name" placeholder="请输入板块名称" class="!rounded-[10px]" />
        </el-form-item>
        <el-form-item label="板块介绍">
          <el-input v-model="createForm.intro" type="textarea" :rows="3" placeholder="介绍一下板块" class="!rounded-[10px]" />
        </el-form-item>
        <el-form-item label="封面图">
          <el-input v-model="createForm.icon" placeholder="输入图片URL" class="!rounded-[10px]" />
        </el-form-item>

        <!-- 分类选择 -->
        <el-form-item label="板块分类">
          <div class="space-y-3">
            <el-radio-group v-model="useCustomCategory" class="flex gap-4">
              <el-radio :label="false">选择现有分类</el-radio>
              <el-radio :label="true">自定义分类</el-radio>
            </el-radio-group>

            <el-select
              v-if="!useCustomCategory"
              v-model="createForm.categoryId"
              placeholder="请选择分类"
              class="w-full !rounded-[10px]"
              clearable
            >
              <el-option
                v-for="cat in categoryList"
                :key="cat.categoryId"
                :label="cat.name"
                :value="cat.categoryId"
              />
            </el-select>

            <el-input
              v-else
              v-model="createForm.customCategory"
              placeholder="请输入自定义分类名称"
              class="!rounded-[10px]"
              maxlength="20"
              show-word-limit
            />
          </div>
        </el-form-item>

        <el-form-item label="板块状态">
          <el-select v-model="createForm.status" class="w-full !rounded-[10px]">
            <el-option :value="1" label="公开" />
            <el-option :value="0" label="私密" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false" class="!rounded-[10px]">取消</el-button>
        <el-button type="primary" @click="handleCreate" :loading="creating" class="!bg-[#6366f1] !border-[#6366f1] !rounded-[10px]">
          创建
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { sectionApi } from '../../../api'
import { resolveFileUrl } from '../../../utils/resolveFileUrl'
import type { Section } from '../../../types'

const router = useRouter()
const sections = ref<Section[]>([])
const hotSections = ref<any[]>([])
const categoryList = ref<any[]>([])
const searchKeyword = ref('')
const selectedCategory = ref<any>({ categoryId: 0, name: '全部板块' })
const createDialogVisible = ref(false)
const creating = ref(false)
const useCustomCategory = ref(false)

const createForm = ref({
  name: '',
  intro: '',
  icon: '',
  status: 1,
  categoryId: null as number | null,
  customCategory: ''
})

// 分类列表（包含全部选项）
const categories = computed(() => {
  const list = [{ categoryId: 0, name: '全部板块' }]
  categoryList.value.forEach(cat => {
    list.push({ categoryId: cat.categoryId, name: cat.name })
  })
  // 添加自定义分类选项
  const customCats = sections.value
    .filter(s => s.categoryName && !categoryList.value.find(c => c.name === s.categoryName))
    .map(s => ({ categoryId: -1, name: s.categoryName || '' }))
    .filter(c => c.name)
  // 去重
  const seen = new Set(list.map(c => c.name))
  customCats.forEach(cat => {
    if (cat.name && !seen.has(cat.name)) {
      list.push(cat)
      seen.add(cat.name)
    }
  })
  return list
})

// 热门板块前3个（大卡片展示）
const topHotSections = computed(() => {
  return hotSections.value.slice(0, 3)
})

// 更多热门板块（横向滚动列表）
const moreHotSections = computed(() => {
  return hotSections.value.slice(3, 8)
})

// 过滤后的板块列表
const filteredSections = computed(() => {
  let result = sections.value

  // 按关键词搜索
  if (searchKeyword.value.trim()) {
    const kw = searchKeyword.value.trim().toLowerCase()
    result = result.filter(s =>
      s.name?.toLowerCase().includes(kw) ||
      s.intro?.toLowerCase().includes(kw)
    )
  }

  // 按分类筛选
  if (selectedCategory.value && selectedCategory.value.categoryId !== 0) {
    if (selectedCategory.value.categoryId === -1) {
      // 自定义分类
      result = result.filter(s => s.categoryName === selectedCategory.value.name)
    } else {
      result = result.filter(s =>
        s.categoryId === selectedCategory.value.categoryId ||
        s.categoryName === selectedCategory.value.name
      )
    }
  }

  return result
})

function goSection(section: Section) {
  if (section.sectionId) {
    router.push(`/section/${section.sectionId}`)
  }
}

function selectCategory(cat: any) {
  selectedCategory.value = cat
}

function handleSearch() {
  // 搜索是实时过滤的，这里可以添加防抖
}

// 格式化数字（超过1000显示为1k）
function formatNumber(num: number): string {
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + 'w'
  } else if (num >= 1000) {
    return (num / 1000).toFixed(1) + 'k'
  }
  return num.toString()
}

// 计算热度（综合成员数和帖子数）
function calculateHeat(section: any): number {
  const members = section.memberCount || 0
  const posts = section.postCount || 0
  // 成员权重40%，帖子权重60%
  return Math.floor(members * 0.4 + posts * 0.6)
}

// 计算热度百分比（用于进度条）
function calculateHeatPercent(section: any): number {
  if (hotSections.value.length === 0) return 0
  const maxHeat = calculateHeat(hotSections.value[0])
  if (maxHeat === 0) return 0
  const heat = calculateHeat(section)
  return Math.min(100, Math.round((heat / maxHeat) * 100))
}

async function joinSection(section: Section) {
  if (!section.sectionId) return
  try {
    await sectionApi.applyJoin(section.sectionId)
    ElMessage.success('已申请加入板块')
    section.isJoined = true
    // 刷新数据
    loadSections()
  } catch (e: any) {
    if (e.response?.data?.message) {
      ElMessage.warning(e.response.data.message)
    } else {
      ElMessage.error('加入失败')
    }
  }
}

async function leaveSection(section: Section) {
  if (!section.sectionId) return
  try {
    await ElMessageBox.confirm('确定要退出该板块吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    // 调用退出接口（如果有）
    ElMessage.success('已退出板块')
    section.isJoined = false
    loadSections()
  } catch {
    // 取消操作
  }
}

async function handleCreate() {
  if (!createForm.value.name) {
    ElMessage.warning('请输入板块名称')
    return
  }

  if (useCustomCategory.value && !createForm.value.customCategory?.trim()) {
    ElMessage.warning('请输入自定义分类名称')
    return
  }

  creating.value = true
  try {
    await sectionApi.create({
      name: createForm.value.name,
      intro: createForm.value.intro,
      icon: createForm.value.icon,
      status: createForm.value.status,
      categoryId: useCustomCategory.value ? null : createForm.value.categoryId,
      customCategory: useCustomCategory.value ? createForm.value.customCategory.trim() : ''
    })
    ElMessage.success('板块创建成功')
    createDialogVisible.value = false
    loadSections()
  } catch (e: any) {
    if (e.response?.data?.message) {
      ElMessage.error(e.response.data.message)
    } else {
      ElMessage.error('创建失败')
    }
  } finally {
    creating.value = false
  }
}

function showCreateDialog() {
  createForm.value = { name: '', intro: '', icon: '', status: 1, categoryId: null, customCategory: '' }
  useCustomCategory.value = false
  // 加载分类列表
  loadCategories()
  createDialogVisible.value = true
}

async function loadCategories() {
  try {
    const res: any = await sectionApi.categories()
    categoryList.value = res.data || []
  } catch (e) {
    categoryList.value = []
  }
}

async function loadSections() {
  try {
    const data: any = await sectionApi.list()
    sections.value = data || []
    // 加载每个板块的详情以获取成员数和加入状态
    await loadSectionDetails()
  } catch (e) {
    console.error(e)
  }
}

async function loadSectionDetails() {
  // 获取每个板块的详细信息（包括成员数）
  const sectionsWithDetails = await Promise.all(
    sections.value.map(async (s) => {
      try {
        const context: any = await sectionApi.context(s.sectionId || 0)
        return {
          ...s,
          memberCount: context.data?.memberCount || 0,
          isJoined: context.data?.member || false
        }
      } catch (e) {
        return { ...s, memberCount: 0, isJoined: false }
      }
    })
  )
  sections.value = sectionsWithDetails

  // 计算热度并排序（综合成员数和帖子数）
  const sorted = [...sectionsWithDetails].sort((a, b) => {
    const heatA = (a.memberCount || 0) * 0.4 + (a.postCount || 0) * 0.6
    const heatB = (b.memberCount || 0) * 0.4 + (b.postCount || 0) * 0.6
    return heatB - heatA
  })

  // 热门板块前8个
  hotSections.value = sorted.slice(0, 8)

  // 获取已加入的板块列表（用于刷新状态）
  try {
    const joined: any = await sectionApi.joinedSections()
    const joinedIds = new Set((joined || []).map((s: Section) => s.sectionId))
    sections.value.forEach(s => {
      if (joinedIds.has(s.sectionId)) {
        s.isJoined = true
      }
    })
    hotSections.value.forEach(s => {
      if (joinedIds.has(s.sectionId)) {
        s.isJoined = true
      }
    })
  } catch (e) {
    // 忽略错误
  }
}

onMounted(() => {
  loadSections()
  loadCategories()
})
</script>

<style scoped>
.line-clamp-1 {
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* 隐藏滚动条但保留滚动功能 */
.overflow-x-auto {
  scrollbar-width: thin;
  scrollbar-color: #d1d5db transparent;
}

.overflow-x-auto::-webkit-scrollbar {
  height: 4px;
}

.overflow-x-auto::-webkit-scrollbar-track {
  background: transparent;
}

.overflow-x-auto::-webkit-scrollbar-thumb {
  background: #d1d5db;
  border-radius: 2px;
}

.overflow-x-auto::-webkit-scrollbar-thumb:hover {
  background: #9ca3af;
}
</style>
