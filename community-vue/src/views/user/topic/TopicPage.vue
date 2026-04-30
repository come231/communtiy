<template>
  <UserPageShell title="热门话题" icon="fa fa-hashtag" subtitle="发现社区热门讨论，参与感兴趣的话题">
    <template #actions>
      <button
        type="button"
        class="px-4 py-2 rounded-xl text-sm font-semibold border border-[#e5e7eb] bg-white text-[#6366f1] hover:bg-[#eef2ff] hover:border-[#c7d2fe] transition-all"
        @click="showAll = !showAll"
      >
        {{ showAll ? '收起' : '查看全部' }}
      </button>
    </template>

    <div class="user-surface-card p-5 md:p-6">
      <div class="flex gap-2 mb-6 overflow-x-auto pb-1">
        <button
          v-for="tab in tabs"
          :key="tab.key"
          type="button"
          class="user-tab-pill shrink-0"
          :class="{ 'user-tab-pill-active': activeTab === tab.key }"
          @click="activeTab = tab.key"
        >
          {{ tab.label }}
        </button>
      </div>

      <div class="space-y-3">
        <div
          v-for="(topic, idx) in displayList"
          :key="topic.id"
          class="rounded-2xl border border-[#e5e7eb] bg-white p-5 shadow-sm transition-all duration-200 hover:shadow-[0_10px_30px_-10px_rgba(99,102,241,0.2)] hover:-translate-y-0.5"
          :class="idx < 3 ? 'ring-1 ring-[#6366f1]/20 border-l-4 border-l-[#6366f1]' : ''"
        >
          <div class="flex items-start gap-4">
            <div
              class="flex-shrink-0 w-9 h-9 rounded-xl flex items-center justify-center text-sm font-bold cursor-pointer"
              :class="idx < 3 ? 'bg-gradient-to-br from-amber-400 to-orange-500 text-white shadow' : 'bg-[#f3f4f6] text-[#6b7280]'"
              @click="goPosts(topic)"
            >
              {{ idx + 1 }}
            </div>
            <div class="flex-1 min-w-0 cursor-pointer" @click="goPosts(topic)">
              <div class="flex items-center gap-2 flex-wrap mb-1">
                <h3 class="font-semibold text-[#1f2937]">#{{ topic.name }}</h3>
                <span v-if="topic.isHot" class="px-2 py-0.5 bg-rose-100 text-rose-600 text-xs rounded-full font-medium">HOT</span>
                <span v-if="topic.isFollowed" class="px-2 py-0.5 bg-[#eef2ff] text-[#6366f1] text-xs rounded-full">已关注</span>
              </div>
              <p class="text-sm text-[#6b7280] line-clamp-2 mb-3">{{ topic.description || '参与话题讨论，分享你的观点' }}</p>
              <div class="flex items-center gap-4 text-xs text-[#9ca3af]">
                <span><i class="fa fa-file-text-o mr-1" />{{ topic.postCount || 0 }} 帖子</span>
                <span><i class="fa fa-line-chart mr-1" />趋势上升</span>
              </div>
            </div>
            <button
              type="button"
              class="flex-shrink-0 px-4 py-1.5 rounded-full text-sm font-medium transition-all"
              :class="
                topic.isFollowed
                  ? 'bg-emerald-50 text-emerald-700 border border-emerald-200 hover:bg-emerald-100'
                  : 'bg-gradient-to-r from-[#6366f1] to-[#a855f7] text-white shadow-md hover:shadow-lg hover:scale-[1.02]'
              "
              @click.stop="toggleFollow(topic)"
            >
              {{ topic.isFollowed ? '已关注' : '+ 关注' }}
            </button>
          </div>
        </div>
      </div>

      <div v-if="displayList.length === 0 && !loading" class="text-center py-16 text-[#9ca3af]">
        <i class="fa fa-hashtag text-4xl mb-3 opacity-40" />
        <p>暂无话题数据</p>
      </div>

      <div v-if="loading" class="space-y-3 mt-2">
        <div v-for="i in 8" :key="i" class="rounded-2xl border border-[#e5e7eb] p-5 animate-pulse bg-[#f9fafb]">
          <div class="flex gap-4">
            <div class="w-9 h-9 rounded-xl bg-[#e5e7eb]" />
            <div class="flex-1 space-y-2">
              <div class="h-4 bg-[#e5e7eb] rounded w-1/3" />
              <div class="h-3 bg-[#f3f4f6] rounded w-full" />
            </div>
          </div>
        </div>
      </div>
    </div>
  </UserPageShell>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { topicApi } from '../../../api'
import UserPageShell from '../../../components/user/UserPageShell.vue'

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
    case 'hot':
      list = topics.value
      break
    case 'new':
      list = [...topics.value].reverse()
      break
    case 'followed':
      list = topics.value.filter((t: any) => t.isFollowed)
      break
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
  router.push({ path: '/home', query: { topicId: String(topic.id), topicName: topic.name } })
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
