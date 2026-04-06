<template>
  <div class="max-w-6xl mx-auto px-4 py-6">
    <div class="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
      <!-- Tab 导航 -->
      <div class="border-b border-gray-100">
        <div class="flex">
          <button
            v-for="tab in tabs" :key="tab.key"
            :class="['px-6 py-4 text-sm font-medium border-b-2 transition-colors', currentTab === tab.key ? 'text-primary border-primary' : 'text-gray-500 border-transparent hover:text-gray-700']"
            @click="currentTab = tab.key"
          >
            {{ tab.label }}
            <span v-if="tab.count !== undefined && tab.count > 0" class="ml-1.5 text-xs bg-red-500 text-white rounded-full px-1.5 py-0.5 min-w-[18px] inline-block text-center">{{ tab.count > 99 ? '99+' : tab.count }}</span>
          </button>
        </div>
      </div>

      <!-- 通知列表 -->
      <div v-if="currentTab === 'notice'" class="p-4">
        <div class="space-y-2">
          <div
            v-for="notice in notices"
            :key="notice.id"
            :class="['flex items-start gap-3 p-4 rounded-lg cursor-pointer hover:bg-blue-50/50 transition-colors', !notice.isRead ? 'bg-blue-50/30' : '']"
            @click="handleNoticeClick(notice)"
          >
            <div :class="['w-10 h-10 rounded-full flex items-center justify-center shrink-0', getNoticeIconBg(notice.type)]">
              <component :is="getNoticeIcon(notice.type)" :class="['w-5 h-5', getNoticeIconColor(notice.type)]" />
            </div>
            <div class="flex-1 min-w-0">
              <p class="text-sm text-gray-800 leading-relaxed">{{ notice.content }}</p>
              <p class="text-xs text-gray-400 mt-1.5">{{ formatTime(notice.createTime) }}</p>
            </div>
            <span v-if="!notice.isRead" class="w-2 h-2 bg-primary rounded-full mt-2 shrink-0"></span>
          </div>

          <div v-if="notices.length === 0" class="text-center py-16 text-gray-400">
            <Bell class="w-12 h-12 mx-auto mb-3 opacity-30" />
            <p>暂无通知</p>
          </div>
        </div>

        <div v-if="noticeTotal > noticePageSize" class="mt-4 flex justify-center">
          <el-button text type="primary" @click="loadMoreNotices">加载更多</el-button>
        </div>
      </div>

      <!-- 私信列表 -->
      <div v-if="currentTab === 'message'" class="h-[calc(100vh-220px)] flex">
        <!-- 会话列表 -->
        <div class="w-72 border-r border-gray-100 flex flex-col">
          <div class="p-3 border-b border-gray-100">
            <input
              v-model="searchKeyword"
              type="text"
              placeholder="搜索联系人..."
              class="w-full px-3 py-2 bg-gray-50 border-0 rounded-lg text-sm outline-none focus:bg-white focus:ring-2 focus:ring-primary/20 transition-all"
            />
          </div>
          <div class="flex-1 overflow-y-auto">
            <div
              v-for="conv in filteredConversations"
              :key="conv.userId"
              :class="['flex items-center gap-3 px-4 py-3 cursor-pointer hover:bg-gray-50 transition-colors border-l-2', activeConv?.userId === conv.userId ? 'bg-blue-50 border-primary' : 'border-transparent']"
              @click="openConversation(conv)"
            >
              <div class="relative shrink-0">
                <img :src="conv.avatar || defaultAvatar" class="w-11 h-11 rounded-full object-cover" alt="" />
                <span v-if="conv.online" class="absolute bottom-0 right-0 w-3 h-3 bg-green-500 rounded-full border-2 border-white"></span>
              </div>
              <div class="flex-1 min-w-0">
                <div class="flex items-center justify-between">
                  <span class="text-sm font-medium text-gray-800 truncate">{{ conv.nickname }}</span>
                  <span class="text-xs text-gray-400 shrink-0 ml-2">{{ formatMsgTime(conv.lastTime) }}</span>
                </div>
                <div class="flex items-center justify-between mt-0.5">
                  <p class="text-xs text-gray-500 truncate">{{ conv.lastMessage }}</p>
                  <span v-if="conv.unread > 0" class="ml-2 text-xs bg-red-500 text-white rounded-full w-4 h-4 flex items-center justify-center shrink-0">{{ conv.unread > 9 ? '9+' : conv.unread }}</span>
                </div>
              </div>
            </div>

            <div v-if="conversations.length === 0" class="text-center py-12 text-gray-400">
              <MessageSquare class="w-10 h-10 mx-auto mb-2 opacity-30" />
              <p class="text-sm">暂无私信</p>
            </div>
          </div>
        </div>

        <!-- 聊天区域 -->
        <div class="flex-1 flex flex-col">
          <template v-if="activeConv">
            <div class="h-14 border-b border-gray-100 flex items-center px-5 gap-3">
              <img :src="activeConv.avatar || defaultAvatar" class="w-8 h-8 rounded-full object-cover" alt="" />
              <span class="font-medium text-gray-800 text-sm">{{ activeConv.nickname }}</span>
              <span v-if="activeConv.online" class="text-xs text-green-500">在线</span>
            </div>

            <div ref="chatContainerRef" class="flex-1 overflow-y-auto p-5 space-y-4">
              <div v-for="msg in messages" :key="msg.id" :class="['flex gap-3', msg.fromSelf ? 'justify-end' : '']">
                <template v-if="!msg.fromSelf">
                  <img :src="activeConv.avatar || defaultAvatar" class="w-8 h-8 rounded-full object-cover shrink-0 mt-1" alt="" />
                  <div class="max-w-[70%]">
                    <div class="bg-gray-100 rounded-2xl rounded-tl-sm px-4 py-2.5 text-sm text-gray-800">{{ msg.content }}</div>
                    <p class="text-xs text-gray-400 mt-1 pl-1">{{ formatTime(msg.createTime) }}</p>
                  </div>
                </template>
                <template v-else>
                  <div class="max-w-[70%]">
                    <div class="bg-gradient-to-r from-primary to-blue-600 rounded-2xl rounded-tr-sm px-4 py-2.5 text-sm text-white">{{ msg.content }}</div>
                    <p class="text-xs text-gray-400 mt-1 pr-1 text-right">{{ formatTime(msg.createTime) }}</p>
                  </div>
                </template>
              </div>

              <div v-if="messages.length === 0" class="text-center py-8 text-gray-400 text-sm">暂无消息，发送第一条消息吧</div>
            </div>

            <div class="border-t border-gray-100 p-4">
              <div class="flex items-end gap-3">
                <div class="flex-1 relative">
                  <textarea
                    v-model="inputMessage"
                    placeholder="输入消息... (Enter发送, Shift+Enter换行)"
                    rows="1"
                    class="w-full resize-none px-4 py-2.5 bg-gray-50 border border-gray-200 rounded-xl text-sm outline-none focus:border-primary focus:ring-2 focus:ring-primary/20 transition-all max-h-32"
                    @keydown.enter.exact.prevent="sendMessage"
                    @input="autoResize($event)"
                  ></textarea>
                </div>
                <button
                  :disabled="!inputMessage.trim() || sending"
                  class="px-5 py-2.5 bg-gradient-to-r from-primary to-blue-600 text-white rounded-xl text-sm font-medium hover:shadow-lg hover:shadow-primary/25 disabled:opacity-50 disabled:cursor-not-allowed transition-all"
                  @click="sendMessage"
                >
                  发送
                </button>
              </div>
            </div>
          </template>

          <div v-else class="flex-1 flex items-center justify-center text-gray-400">
            <div class="text-center"><MessageSquare class="w-16 h-16 mx-auto mb-3 opacity-20" /><p>选择一个会话开始聊天</p></div>
          </div>
        </div>
      </div>

      <!-- 系统通知 -->
      <div v-if="currentTab === 'system'" class="p-4 space-y-3">
        <div
          v-for="sys in systemNotices"
          :key="sys.id"
          class="p-4 rounded-lg border border-gray-100 hover:bg-gray-50 transition-colors"
        >
          <div class="flex items-center gap-2 mb-2">
            <span class="text-xs font-medium px-2 py-0.5 rounded" :class="getSysTagClass(sys.tag)">{{ sys.tagText }}</span>
            <span class="text-xs text-gray-400">{{ sys.time }}</span>
          </div>
          <p class="text-sm text-gray-700">{{ sys.title }}</p>
          <p class="text-xs text-gray-500 mt-1 line-clamp-2">{{ sys.desc }}</p>
        </div>
        <div v-if="systemNotices.length === 0" class="text-center py-16 text-gray-400">
          <Info class="w-12 h-12 mx-auto mb-3 opacity-30" /><p>暂无系统通知</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick } from 'vue'
import { Bell, Heart, MessageSquare, Star, UserPlus, Share2, Info, CheckCircle, AlertCircle } from 'lucide-vue-next'
import { noticeApi, messageApi } from '../../../api'
import { useUserStore } from '../../../store/user'
import type { Notice, Conversation } from '../../../types'

const userStore = useUserStore()
const defaultAvatar = 'https://placehold.co/80x80/E8F3FF/409EFF?text=U'

const tabs = computed(() => [
  { key: 'notice', label: '通知', count: notices.value.filter(n => !n.isRead).length },
  { key: 'message', label: '私信', count: conversations.value.filter(c => (c.unread || 0) > 0).reduce((sum, c) => sum + (c.unread || 0), 0) },
  { key: 'system', label: '系统通知' },
])

const currentTab = ref('notice')
const searchKeyword = ref('')
const inputMessage = ref('')
const sending = ref(false)
const chatContainerRef = ref<HTMLElement>()

// 通知数据
const notices = ref<Notice[]>([])
const noticeTotal = ref(0)
const noticePageSize = ref(10)

// 私信会话
const conversations = ref<Conversation[]>([])

const activeConv = ref<Conversation | null>(null)
const messages = ref<{ id: number; content: string; fromSelf: boolean; createTime: string }[]>([])

// 系统通知
const systemNotices = ref([
  { id: 1, tag: 'update', tagText: '更新', time: '2026-04-04 10:00', title: '社区平台升级公告', desc: '我们进行了重大升级，新增AI助手功能、优化搜索体验、修复已知问题。' },
  { id: 2, tag: 'activity', tagText: '活动', time: '2026-04-01 09:00', title: '春季创作大赛开始啦', desc: '参与话题#春季创作大赛#，分享你的技术心得，赢取丰厚奖励！' },
  { id: 3, tag: 'notice', tagText: '通知', time: '2026-03-28 14:00', title: '关于内容规范的提醒', desc: '请各位用户注意遵守社区规范，发布优质原创内容。' },
])

const filteredConversations = computed(() => {
  if (!searchKeyword.value.trim()) return conversations.value
  return conversations.value.filter(c => (c.nickname || c.userName || '').includes(searchKeyword.value.trim()))
})

onMounted(() => {
  if (userStore.isLoggedIn) {
    loadNotices()
    loadConversations()
  }
})

async function loadNotices() {
  try {
    const res: any = await noticeApi.list({ pageNum: 1, pageSize: noticePageSize.value })
    notices.value = res?.records || []
    noticeTotal.value = res?.total || 0
  } catch (e) {
    console.error('加载通知失败', e)
  }
}

async function loadConversations() {
  try {
    const res: any = await messageApi.conversations()
    conversations.value = res || []
  } catch (e) {
    console.error('加载会话列表失败', e)
  }
}

async function openConversation(conv: Conversation) {
  activeConv.value = conv
  conv.unread = 0
  // 加载聊天记录
  try {
    const res: any = await messageApi.history(conv.userId, 1, 50)
    messages.value = (res?.records || []).map((m: any) => ({
      ...m,
      fromSelf: String(m.fromUserId) === String(userStore.userId),
    }))
  } catch (e) {
    messages.value = []
  }
  nextTick(() => scrollToBottom())
}

async function sendMessage() {
  if (!inputMessage.value.trim() || !activeConv.value) return
  sending.value = true
  try {
    await messageApi.send({
      toUserId: activeConv.value.userId,
      content: inputMessage.value.trim(),
    })
    messages.value.push({
      id: Date.now(),
      content: inputMessage.value.trim(),
      fromSelf: true,
      createTime: new Date().toISOString(),
    })
    inputMessage.value = ''
    nextTick(() => scrollToBottom())
  } catch (e) {
    console.error('发送消息失败', e)
  } finally {
    sending.value = false
  }
}

function scrollToBottom() {
  if (chatContainerRef.value) {
    chatContainerRef.value.scrollTop = chatContainerRef.value.scrollHeight
  }
}

async function handleNoticeClick(notice: Notice) {
  if (!notice.isRead) {
    try {
      await noticeApi.markRead(notice.id)
      notice.isRead = true
    } catch (e) {
      console.error('标记已读失败', e)
    }
  }
}

async function loadMoreNotices() {
  noticePageSize.value += 10
  await loadNotices()
}

function autoResize(event: Event) {
  const target = event.target as HTMLTextAreaElement
  target.style.height = 'auto'
  target.style.height = Math.min(target.scrollHeight, 128) + 'px'
}

function formatTime(time: string | number): string {
  const t = typeof time === 'string' ? new Date(time).getTime() : time
  const diff = Date.now() - t
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  return new Date(t).toLocaleDateString('zh-CN')
}

function formatMsgTime(time: string | number): string {
  const t = typeof time === 'string' ? new Date(time).getTime() : time
  const d = new Date(t)
  const now = new Date()
  if (d.toDateString() === now.toDateString()) {
    return d.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  }
  return `${d.getMonth() + 1}/${d.getDate()}`
}

function getNoticeIcon(type: string) {
  const icons: Record<string, any> = { like: Heart, comment: MessageSquare, follow: UserPlus, collect: Star, system: Bell }
  return icons[type] || Bell
}
function getNoticeIconBg(type: string) {
  const bgs: Record<string, string> = { like: 'bg-red-50', comment: 'bg-blue-50', follow: 'bg-green-50', collect: 'bg-yellow-50', system: 'bg-purple-50' }
  return bgs[type] || 'bg-gray-50'
}
function getNoticeIconColor(type: string) {
  const colors: Record<string, string> = { like: 'text-red-500', comment: 'text-blue-500', follow: 'text-green-500', collect: 'text-yellow-600', system: 'text-purple-500' }
  return colors[type] || 'text-gray-500'
}

function getSysTagClass(tag: string) {
  const cls: Record<string, string> = { update: 'bg-blue-100 text-blue-700', activity: 'bg-orange-100 text-orange-700', notice: 'bg-gray-100 text-gray-700' }
  return cls[tag] || 'bg-gray-100 text-gray-600'
}
</script>
