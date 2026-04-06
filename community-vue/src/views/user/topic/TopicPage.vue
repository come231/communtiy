<template>
  <div class="min-h-screen bg-[#F5F7FA] pt-20 pb-8">
    <div class="max-w-6xl mx-auto px-4">
      <!-- 页头 -->
      <div class="flex items-center justify-between mb-8">
        <div>
          <h1 class="text-2xl font-bold text-gray-800 flex items-center gap-3">
            <Hash class="w-7 h-7 text-orange-500" />
            热门话题
          </h1>
          <p class="text-gray-500 mt-2">发现社区热门讨论话题，参与感兴趣的话题讨论</p>
        </div>
        <button @click="showAll = !showAll"
          class="px-4 py-2 bg-white rounded-lg shadow-sm hover:shadow-md text-sm text-gray-700 transition-all cursor-pointer border border-gray-100">
          {{ showAll ? '收起' : '查看全部' }}
        </button>
      </div>

      <!-- Tab切换 -->
      <div class="flex gap-6 mb-6 border-b border-gray-200 pb-0">
        <button v-for="tab in tabs" :key="tab.key" @click="activeTab = tab.key"
          :class="['pb-4 px-1 text-sm font-medium relative transition-colors cursor-pointer border-b-2',
            activeTab === tab.key ? 'text-blue-600 border-blue-600' : 'text-gray-500 hover:text-gray-700 border-transparent']">
          {{ tab.label }}
        </button>
      </div>

      <!-- 话题列表 -->
      <div class="space-y-4">
        <div v-for="(topic, idx) in displayList" :key="topic.id"
          @click="goPosts(topic)"
          class="group bg-white rounded-xl p-5 shadow-sm hover:shadow-md transition-all duration-200 cursor-pointer border-l-4"
          :class="{ 'border-l-blue-500': idx < 3, 'border-l-transparent': idx >= 3 }">

          <div class="flex items-start gap-4">
            <!-- 排名 -->
            <div class="flex-shrink-0 w-8 h-8 rounded-lg flex items-center justify-center text-sm font-bold mt-0.5"
              :class="idx < 3 ? 'bg-gradient-to-br from-yellow-400 to-orange-500 text-white' : 'bg-gray-100 text-gray-400'">
              {{ idx + 1 }}
            </div>

            <!-- 内容 -->
            <div class="flex-1 min-w-0">
              <div class="flex items-center gap-3 mb-1.5">
                <h3 class="font-semibold text-gray-800 group-hover:text-blue-600 transition-colors truncate">
                  #{{ topic.name }}
                </h3>
                <span v-if="topic.isHot" class="px-2 py-0.5 bg-red-50 text-red-500 text-xs rounded-full font-medium flex-shrink-0">
                  HOT
                </span>
                <span v-if="topic.isFollowed" class="px-2 py-0.5 bg-blue-50 text-blue-500 text-xs rounded-full flex-shrink-0">
                  已关注
                </span>
              </div>
              <p class="text-sm text-gray-500 line-clamp-1 mb-3">{{ topic.description || '参与话题讨论，分享你的观点' }}</p>

              <div class="flex items-center gap-4 text-xs text-gray-400">
                <span class="flex items-center gap-1">
                  <FileText class="w-3.5 h-3.5" /> {{ topic.postCount || 0 }} 帖子
                </span>
                <span class="flex items-center gap-1">
                  <TrendingUp class="w-3.5 h-3.5" /> 趋势上升
                </span>
              </div>
            </div>

            <!-- 关注按钮 -->
            <button @click.stop.prevent="toggleFollow(topic)" type="button"
              :class="[
                'flex-shrink-0 px-4 py-1.5 rounded-full text-sm font-medium transition-all cursor-pointer',
                topic.isFollowed
                  ? 'bg-green-50 text-green-600 hover:bg-green-100 border border-green-200'
                  : 'bg-blue-50 text-blue-600 hover:bg-blue-100 border border-blue-200'
              ]">
              {{ topic.isFollowed ? '已关注' : '+ 关注' }}
            </button>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-if="displayList.length === 0 && !loading" class="text-center py-20">
        <Hash class="w-16 h-16 text-gray-300 mx-auto mb-4" />
        <p class="text-gray-400">暂无话题数据</p>
      </div>

      <!-- 加载骨架 -->
      <div v-if="loading" class="space-y-4">
        <div v-for="i in 10" :key="i" class="bg-white rounded-xl p-5 animate-pulse">
          <div class="flex gap-4">
            <div class="w-8 h-8 rounded-lg bg-gray-200"></div>
            <div class="flex-1">
              <div class="h-5 bg-gray-200 rounded w-40 mb-2"></div>
              <div class="h-4 bg-gray-100 rounded w-72"></div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Hash, FileText, TrendingUp } from 'lucide-vue-next'
import { topicApi } from '../../../api'

const router = useRouter()
const activeTab = ref('hot')
const loading = ref(true)
const showAll = ref(false)
const topics = ref<any[]>([])

const tabs = [
  { key: 'hot', label: '热门推荐' },
  { key: 'new', label: '最新话题' },
  { key: 'followed', label: '我关注的' },
]

const displayList = computed(() => {
  let list: any[] = []
  switch (activeTab.value) {
    case 'hot': list = topics.value; break
    case 'new': list = [...topics.value].reverse(); break
    case 'followed': list = topics.value.filter((t: any) => t.isFollowed); break
  }
  return showAll.value ? list : list.slice(0, 15)
})

async function toggleFollow(topic: any) {
  try {
    if (topic.isFollowed) {
      await topicApi.unfollow(topic.id)
      topic.isFollowed = false
    } else {
      await topicApi.follow(topic.id)
      topic.isFollowed = true
    }
  } catch (e) {
    console.error(e)
  }
}

function goPosts(topic: any) {
  router.push({ path: '/', query: { topicId: String(topic.id), topicName: topic.name } })
}

onMounted(async () => {
  try {
    const res: any = await topicApi.hot()
    topics.value = res || []
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
})
</script>
