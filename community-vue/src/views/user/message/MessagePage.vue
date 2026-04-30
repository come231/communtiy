<template>
  <div class="min-h-screen bg-gradient-to-br from-[#f0f4ff] to-[#f8faff] text-[#1f2937]">
    <!-- 顶部栏（对齐 HTML 模板） -->
    <header class="sticky top-0 z-[100] bg-white shadow-[0_2px_20px_rgba(0,0,0,0.08)] py-4">
      <div class="max-w-[1200px] mx-auto px-5 flex items-center justify-between gap-4 flex-wrap">
        <div class="text-[22px] font-bold flex items-center gap-2 bg-gradient-to-r from-[#6366f1] to-[#a855f7] bg-clip-text text-transparent">
          <i class="fa fa-bell text-[#6366f1]"></i>
          <span>消息通知</span>
        </div>
        <div class="flex items-center gap-3">
          <router-link
            to="/chat"
            class="text-sm text-[#6366f1] font-medium hover:underline flex items-center gap-1"
          >
            <i class="fa fa-envelope-o"></i> 私信聊天
          </router-link>
          <span
            v-if="headerUnread > 0"
            class="px-2.5 py-1 rounded-full bg-[#ec4899] text-white text-xs font-semibold"
          >
            {{ headerUnread > 99 ? '99+' : headerUnread }} 未读
          </span>
        </div>
      </div>
    </header>

    <div class="max-w-[1200px] mx-auto px-5 pb-16 pt-8">
      <h2 class="text-[26px] font-bold flex items-center gap-2.5 mb-2">
        <i class="fa fa-bell-o text-[#6366f1]"></i> 消息中心
      </h2>
      <p class="text-[#6b7280] mb-6">查看互动通知与私信会话</p>

      <!-- 操作栏 -->
      <div class="flex justify-between items-center mb-5">
        <span class="text-sm text-[#6b7280]">
          <template v-if="currentTab !== 'dm'">
            共 {{ currentTotal }} 条
            <span v-if="currentUnread > 0" class="text-[#6366f1]">（本页未读 {{ currentUnread }}）</span>
          </template>
          <template v-else>共 {{ conversations.length }} 个会话</template>
        </span>
        <button
          v-if="currentTab !== 'dm' && currentUnread > 0"
          type="button"
          class="read-all-btn"
          @click="markAllRead"
        >
          <i class="fa fa-check-circle"></i> 全部标为已读
        </button>
      </div>

      <!-- 分类标签 -->
      <div class="flex flex-wrap gap-3 mb-6">
        <button
          v-for="tab in tabList"
          :key="tab.key"
          type="button"
          class="msg-tab"
          :class="{ active: currentTab === tab.key }"
          @click="switchTab(tab.key)"
        >
          {{ tab.label }}
          <span v-if="tab.badge && tab.badge > 0" class="tab-badge">{{ tab.badge > 99 ? '99+' : tab.badge }}</span>
        </button>
      </div>

      <!-- 通知列表 -->
      <div v-if="currentTab !== 'dm'" class="flex flex-col gap-3 animate-[fadePanel_0.4s_ease]">
        <button
          v-for="item in currentList"
          :key="'n-' + item.id"
          type="button"
          class="msg-item text-left w-full border-0"
          :class="{ unread: !item.isRead }"
          @click="handleNoticeClick(item)"
        >
          <div v-if="item.fromUserAvatar" class="msg-avatar-wrap">
            <img :src="resolveFileUrl(item.fromUserAvatar)" alt="" class="msg-avatar" />
          </div>
          <div v-else class="msg-icon">
            <i :class="['fa', getNoticeIcon(item.type)]"></i>
          </div>
          <div class="msg-content">
            <div class="msg-title">{{ getNoticeLineTitle(item) }}</div>
            <div class="msg-text line-clamp-2">{{ item.content || item.title }}</div>
            <div class="msg-time">
              {{ formatTime(item.createTime) }}
              <span v-if="item.targetTitle" class="ml-2 opacity-80">· {{ item.targetTitle }}</span>
            </div>
          </div>
          <div class="flex flex-col gap-1 shrink-0" @click.stop>
            <button
              v-if="!item.isRead"
              type="button"
              class="text-xs px-2 py-1 rounded-lg text-[#6366f1] bg-[#eef2ff] hover:bg-[#6366f1] hover:text-white transition-colors"
              @click="markReadOne(item)"
            >
              已读
            </button>
            <button
              type="button"
              class="text-xs px-2 py-1 rounded-lg text-[#9ca3af] hover:text-red-500 hover:bg-red-50 transition-colors"
              @click="deleteNotice(item)"
            >
              <i class="fa fa-trash-o"></i>
            </button>
          </div>
        </button>

        <div v-if="!loading && currentList.length === 0" class="empty-state">
          <i :class="['fa', emptyIcon, 'empty-icon']"></i>
          <p>{{ emptyText }}</p>
        </div>

        <div v-if="loading" class="space-y-3">
          <div v-for="i in 3" :key="i" class="msg-item animate-pulse">
            <div class="w-[50px] h-[50px] rounded-[14px] bg-[#e5e7eb] shrink-0"></div>
            <div class="flex-1 space-y-2">
              <div class="h-4 bg-[#e5e7eb] rounded w-1/3"></div>
              <div class="h-3 bg-[#f3f4f6] rounded w-full"></div>
            </div>
          </div>
        </div>

        <div v-if="currentTotalPages > 1" class="flex justify-center gap-2 pt-4">
          <button type="button" class="page-btn" :disabled="currentPage <= 1" @click="currentPage--">上一页</button>
          <span class="text-sm text-[#6b7280] self-center px-2">{{ currentPage }} / {{ currentTotalPages }}</span>
          <button type="button" class="page-btn" :disabled="currentPage >= currentTotalPages" @click="currentPage++">下一页</button>
        </div>
      </div>

      <!-- 私信会话 -->
      <div v-else class="flex flex-col gap-3 animate-[fadePanel_0.4s_ease]">
        <button
          v-for="c in conversations"
          :key="'c-' + c.otherUserId"
          type="button"
          class="msg-item text-left w-full border-0"
          :class="{ unread: (c.unreadCount || 0) > 0 }"
          @click="openChat(c)"
        >
          <img
            :src="resolveFileUrl(c.avatar) || defaultAvatar"
            alt=""
            class="msg-avatar"
          />
          <div class="msg-content min-w-0">
            <div class="msg-title truncate">{{ c.nickName || '用户' }}</div>
            <div class="msg-text line-clamp-1">{{ c.lastMessage || '暂无消息' }}</div>
            <div class="msg-time">{{ formatDmTime(c.lastTime) }}</div>
          </div>
          <span
            v-if="c.unreadCount && c.unreadCount > 0"
            class="min-w-[22px] h-[22px] rounded-full bg-[#ec4899] text-white text-xs flex items-center justify-center font-semibold shrink-0"
          >
            {{ c.unreadCount > 99 ? '99+' : c.unreadCount }}
          </span>
        </button>

        <div v-if="!dmLoading && conversations.length === 0" class="empty-state">
          <i class="fa fa-envelope-o empty-icon"></i>
          <p>暂无私信会话</p>
        </div>
        <div v-if="dmLoading" class="msg-item animate-pulse">
          <div class="w-[50px] h-[50px] rounded-full bg-[#e5e7eb]"></div>
          <div class="flex-1 h-10 bg-[#f3f4f6] rounded"></div>
        </div>
      </div>
    </div>

    <!-- 私信抽屉 -->
    <el-drawer
      v-model="chatDrawerVisible"
      direction="rtl"
      size="min(420px, 100%)"
      class="dm-drawer"
      destroy-on-close
      :show-close="true"
      @closed="onChatClosed"
    >
      <template #header>
        <div class="flex items-center justify-between gap-3 w-full pr-2">
          <span class="text-base font-semibold text-[#1f2937] truncate">{{ chatTitle }}</span>
          <router-link
            v-if="chatPeer"
            :to="{
              path: '/chat',
              query: {
                userId: String(chatPeer.otherUserId),
                ...(chatPeer.nickName ? { nickName: chatPeer.nickName } : {}),
              },
            }"
            class="shrink-0 text-xs font-medium text-[#6366f1] hover:underline"
            @click="chatDrawerVisible = false"
          >
            展开全页
          </router-link>
        </div>
      </template>
      <div class="flex flex-col h-full -mt-2">
        <div ref="chatScrollRef" class="flex-1 overflow-y-auto space-y-3 pb-4 px-1 max-h-[calc(100vh-180px)]">
          <div
            v-for="m in chatMessages"
            :key="m.id"
            class="flex"
            :class="isMine(m) ? 'justify-end' : 'justify-start'"
          >
            <div
              class="max-w-[85%] rounded-2xl px-3 py-2 text-sm leading-relaxed"
              :class="isMine(m) ? 'bg-gradient-to-r from-[#6366f1] to-[#a855f7] text-white' : 'bg-[#f3f4f6] text-[#1f2937]'"
            >
              {{ m.content }}
            </div>
          </div>
          <div v-if="chatMessages.length === 0 && !chatLoading" class="text-center text-[#9ca3af] text-sm py-8">
            暂无聊天记录，发一句问候吧
          </div>
        </div>
        <div class="flex gap-2 pt-3 border-t border-[#e5e7eb]">
          <input
            v-model="chatInput"
            type="text"
            class="flex-1 rounded-xl border border-[#e5e7eb] px-3 py-2.5 text-sm outline-none focus:border-[#818cf8]"
            placeholder="输入消息…"
            @keyup.enter="sendChat"
          />
          <button
            type="button"
            class="px-4 py-2 rounded-xl bg-gradient-to-r from-[#6366f1] to-[#a855f7] text-white text-sm font-semibold disabled:opacity-50"
            :disabled="!chatInput.trim() || chatSending"
            @click="sendChat"
          >
            发送
          </button>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { noticeApi, messageApi } from '../../../api'
import { resolveFileUrl } from '../../../utils/resolveFileUrl'
import { normalizeConversationList } from '../../../utils/conversation'
import { useUserStore } from '../../../store/user'
import type { Notice, Conversation, PrivateMessage } from '../../../types'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const defaultAvatar = 'https://placehold.co/80x80/E8EAF6/9FA8DA?text=U'

const currentTab = ref<'all' | 'system' | 'comment' | 'like' | 'follow' | 'dm'>('all')
const currentPage = ref(1)
const pageSize = ref(10)
const loading = ref(false)

const allNotices = ref<Notice[]>([])
const systemNotices = ref<Notice[]>([])
const commentNotices = ref<Notice[]>([])
const likeNotices = ref<Notice[]>([])
const followNotices = ref<Notice[]>([])

const allTotal = ref(0)
const systemTotal = ref(0)
const commentTotal = ref(0)
const likeTotal = ref(0)
const followTotal = ref(0)

const allUnread = ref(0)
const systemUnread = ref(0)
const commentUnread = ref(0)
const likeUnread = ref(0)
const followUnread = ref(0)
const noticeUnreadTotal = ref(0)

const conversations = ref<Conversation[]>([])
const dmLoading = ref(false)
const dmUnreadTotal = ref(0)

const chatDrawerVisible = ref(false)
const chatPeer = ref<Conversation | null>(null)
const chatMessages = ref<PrivateMessage[]>([])
const chatInput = ref('')
const chatLoading = ref(false)
const chatSending = ref(false)
const chatScrollRef = ref<HTMLElement | null>(null)

const myUserId = computed(() => (userStore.userId ? Number(userStore.userId) : 0))

const headerUnread = computed(() => (noticeUnreadTotal.value || 0) + (dmUnreadTotal.value || 0))

const tabList = computed(() => [
  { key: 'all' as const, label: '全部消息', badge: allUnread.value },
  { key: 'system' as const, label: '系统通知', badge: systemUnread.value },
  { key: 'comment' as const, label: '评论我的', badge: commentUnread.value },
  { key: 'like' as const, label: '点赞我的', badge: likeUnread.value },
  { key: 'follow' as const, label: '关注我的', badge: followUnread.value },
  { key: 'dm' as const, label: '私信', badge: dmUnreadTotal.value },
])

const currentList = computed(() => {
  switch (currentTab.value) {
    case 'all': return allNotices.value
    case 'system': return systemNotices.value
    case 'comment': return commentNotices.value
    case 'like': return likeNotices.value
    case 'follow': return followNotices.value
    default: return []
  }
})

const currentTotal = computed(() => {
  switch (currentTab.value) {
    case 'all': return allTotal.value
    case 'system': return systemTotal.value
    case 'comment': return commentTotal.value
    case 'like': return likeTotal.value
    case 'follow': return followTotal.value
    default: return 0
  }
})

const currentUnread = computed(() => {
  switch (currentTab.value) {
    case 'all': return allUnread.value
    case 'system': return systemUnread.value
    case 'comment': return commentUnread.value
    case 'like': return likeUnread.value
    case 'follow': return followUnread.value
    default: return 0
  }
})

const currentTotalPages = computed(() => Math.max(1, Math.ceil(currentTotal.value / pageSize.value)))

const chatTitle = computed(() => (chatPeer.value ? `与 ${chatPeer.value.nickName || '用户'} 的对话` : '私信'))

const emptyText = computed(() => {
  const m: Record<string, string> = {
    all: '暂无消息',
    system: '暂无系统通知',
    comment: '暂无评论消息',
    like: '暂无点赞消息',
    follow: '暂无关注消息',
  }
  return m[currentTab.value] || '暂无消息'
})

const emptyIcon = computed(() => {
  const m: Record<string, string> = {
    all: 'fa-bell-o',
    system: 'fa-bullhorn',
    comment: 'fa-comment-o',
    like: 'fa-heart-o',
    follow: 'fa-user-o',
  }
  return m[currentTab.value] || 'fa-bell-o'
})

function normalizeNotice(raw: any): Notice {
  return {
    ...raw,
    isRead: raw.isRead === true || raw.isRead === 1,
  }
}

function switchTab(key: typeof currentTab.value) {
  currentTab.value = key
  currentPage.value = 1
  if (key === 'dm') {
    loadConversations()
  } else {
    loadNotices()
  }
}

function getNoticeIcon(type: number): string {
  const map: Record<number, string> = {
    1: 'fa-heart',
    2: 'fa-comment',
    3: 'fa-user-plus',
    4: 'fa-star',
    5: 'fa-bullhorn',
  }
  return map[type] || 'fa-bell'
}

function getNoticeLineTitle(item: Notice): string {
  if (item.type === 5) return item.title || '系统通知'
  if (item.fromUserName) {
    const action: Record<number, string> = {
      1: '赞了你的内容',
      2: '评论了你',
      3: '关注了你',
      4: '收藏了你的内容',
    }
    return `${item.fromUserName} ${action[item.type] || '与你互动'}`
  }
  return item.title || '新消息'
}

function formatTime(time: string | number): string {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  if (diff < 3600000) {
    const minutes = Math.floor(diff / 60000)
    return minutes < 1 ? '刚刚' : `${minutes}分钟前`
  }
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  if (diff < 604800000) return `${Math.floor(diff / 86400000)}天前`
  return date.toLocaleString('zh-CN', { month: 'numeric', day: 'numeric', hour: '2-digit', minute: '2-digit' })
}

function formatDmTime(t: string): string {
  if (!t) return ''
  return formatTime(t)
}

async function loadNotices() {
  loading.value = true
  try {
    const typeMap: Record<string, number | undefined> = {
      all: undefined,
      system: 5,
      comment: 2,
      like: 1,
      follow: 3,
    }
    const res: any = await noticeApi.list({
      type: typeMap[currentTab.value],
      pageNum: currentPage.value,
      pageSize: pageSize.value,
    })
    const records = (res?.records || []).map(normalizeNotice)
    const total = Number(res?.total) || 0
    const unreadOnPage = records.filter((n: Notice) => !n.isRead).length

    switch (currentTab.value) {
      case 'all':
        allNotices.value = records
        allTotal.value = total
        allUnread.value = unreadOnPage
        break
      case 'system':
        systemNotices.value = records
        systemTotal.value = total
        systemUnread.value = unreadOnPage
        break
      case 'comment':
        commentNotices.value = records
        commentTotal.value = total
        commentUnread.value = unreadOnPage
        break
      case 'like':
        likeNotices.value = records
        likeTotal.value = total
        likeUnread.value = unreadOnPage
        break
      case 'follow':
        followNotices.value = records
        followTotal.value = total
        followUnread.value = unreadOnPage
        break
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

async function loadNoticeUnread() {
  try {
    const n: any = await noticeApi.unreadCount()
    noticeUnreadTotal.value = Number(n) || 0
  } catch {
    noticeUnreadTotal.value = 0
  }
}

async function loadDmUnread() {
  try {
    const n: any = await messageApi.unreadCount()
    dmUnreadTotal.value = Number(n) || 0
  } catch {
    dmUnreadTotal.value = 0
  }
}

async function loadConversations() {
  dmLoading.value = true
  try {
    const list: unknown = await messageApi.conversations()
    conversations.value = normalizeConversationList(list)
    dmUnreadTotal.value = conversations.value.reduce((s, c) => s + (c.unreadCount || 0), 0)
  } catch (e) {
    console.error(e)
    conversations.value = []
  } finally {
    dmLoading.value = false
  }
}

async function markReadOne(item: Notice) {
  try {
    await noticeApi.markRead(item.id)
    item.isRead = true
    await loadNoticeUnread()
    ElMessage.success('已标为已读')
  } catch {
    ElMessage.error('操作失败')
  }
}

async function markAllRead() {
  try {
    await ElMessageBox.confirm('将所有通知标为已读？', '确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await noticeApi.markAllRead()
    currentList.value.forEach((item) => { item.isRead = true })
    allUnread.value = systemUnread.value = commentUnread.value = likeUnread.value = followUnread.value = 0
    await loadNoticeUnread()
    ElMessage.success('已全部标为已读')
  } catch {
    /* cancel */
  }
}

async function deleteNotice(item: Notice) {
  try {
    await ElMessageBox.confirm('删除这条通知？', '确认', { type: 'warning' })
    await noticeApi.delete(item.id)
    const list = currentList.value
    const i = list.findIndex((n) => n.id === item.id)
    if (i > -1) list.splice(i, 1)
    await loadNoticeUnread()
    ElMessage.success('已删除')
  } catch {
    /* cancel */
  }
}

function handleNoticeClick(item: Notice) {
  if (!item.isRead) {
    markReadOne(item)
  }
  switch (item.type) {
    case 1:
    case 2:
    case 4:
      if (item.targetId) router.push(`/post/${item.targetId}`)
      break
    case 3:
      if (item.fromUserId) router.push(`/user/${item.fromUserId}`)
      break
    case 5:
      if (item.targetType === 'post' && item.targetId) router.push(`/post/${item.targetId}`)
      break
    default:
      break
  }
}

function isMine(m: PrivateMessage): boolean {
  return myUserId.value > 0 && m.fromUserId === myUserId.value
}

async function openChat(c: Conversation) {
  chatPeer.value = c
  chatDrawerVisible.value = true
  chatInput.value = ''
  await loadChatHistory(c.otherUserId)
  try {
    await messageApi.markRead(c.otherUserId)
    c.unreadCount = 0
    await loadDmUnread()
  } catch {
    /* ignore */
  }
}

async function loadChatHistory(otherUserId: number) {
  chatLoading.value = true
  try {
    const res: any = await messageApi.history(otherUserId, 1, 50)
    const records = res?.records || res?.list || []
    chatMessages.value = Array.isArray(records) ? records : []
    await nextTick()
    scrollChatBottom()
  } catch (e) {
    console.error(e)
    chatMessages.value = []
  } finally {
    chatLoading.value = false
  }
}

function scrollChatBottom() {
  const el = chatScrollRef.value
  if (el) el.scrollTop = el.scrollHeight
}

async function sendChat() {
  const text = chatInput.value.trim()
  if (!text || !chatPeer.value || chatSending.value) return
  chatSending.value = true
  try {
    const sent: any = await messageApi.send({ toUserId: chatPeer.value.otherUserId, content: text })
    chatInput.value = ''
    if (sent && sent.id) {
      chatMessages.value.push(sent)
    } else {
      await loadChatHistory(chatPeer.value.otherUserId)
    }
    await loadConversations()
    await nextTick()
    scrollChatBottom()
  } catch (e) {
    console.error(e)
    ElMessage.error('发送失败')
  } finally {
    chatSending.value = false
  }
}

function onChatClosed() {
  chatPeer.value = null
  chatMessages.value = []
  if (route.query.toUserId) {
    router.replace({ path: '/message', query: {} })
  }
}

async function openChatFromQuery(userId: number, nickName?: string) {
  currentTab.value = 'dm'
  await loadConversations()
  let c = conversations.value.find((x) => x.otherUserId === userId)
  if (!c) {
    c = {
      otherUserId: userId,
      nickName: nickName || `用户${userId}`,
      avatar: '',
      lastMessage: '',
      lastTime: '',
      unreadCount: 0,
    }
    conversations.value.unshift(c)
  }
  await openChat(c)
}

watch(currentPage, () => {
  if (currentTab.value !== 'dm') loadNotices()
})

watch(
  () => route.query.toUserId,
  (uid) => {
    if (!uid) return
    const id = Number(uid)
    if (!id) return
    const name = typeof route.query.toNickName === 'string' ? decodeURIComponent(route.query.toNickName) : undefined
    openChatFromQuery(id, name)
  },
  { immediate: true },
)

onMounted(() => {
  if (currentTab.value !== 'dm') loadNotices()
  loadNoticeUnread()
  loadDmUnread()
})
</script>

<style scoped>
.read-all-btn {
  padding: 8px 16px;
  background: white;
  color: #6366f1;
  border: 1px solid #818cf8;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  align-items: center;
  gap: 6px;
}
.read-all-btn:hover {
  background: #6366f1;
  color: white;
}

.msg-tab {
  padding: 10px 18px;
  background: white;
  border-radius: 12px;
  font-weight: 600;
  font-size: 14px;
  color: #6b7280;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 3px 8px rgba(0, 0, 0, 0.05);
  position: relative;
  border: none;
}
.msg-tab.active {
  background: linear-gradient(90deg, #6366f1, #a855f7);
  color: white;
}
.msg-tab:hover:not(.active) {
  background: #f0f4ff;
  color: #6366f1;
  transform: translateY(-2px);
}

.tab-badge {
  position: absolute;
  top: -5px;
  right: -5px;
  min-width: 18px;
  height: 18px;
  padding: 0 4px;
  background: #ec4899;
  color: white;
  border-radius: 50%;
  font-size: 11px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.msg-item {
  background: white;
  border-radius: 16px;
  padding: 18px;
  display: flex;
  align-items: center;
  gap: 15px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
  box-shadow: 0 10px 30px -5px rgba(99, 102, 241, 0.12);
}
.msg-item:hover {
  transform: translateX(4px);
  box-shadow: 0 20px 40px -5px rgba(99, 102, 241, 0.22);
}
.msg-item.unread {
  background: #eef2ff;
  border-left: 4px solid #6366f1;
}

.msg-avatar-wrap {
  flex-shrink: 0;
}
.msg-avatar {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid white;
  flex-shrink: 0;
}

.msg-icon {
  width: 50px;
  height: 50px;
  border-radius: 14px;
  background: linear-gradient(135deg, #818cf8, #a855f7);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 20px;
  flex-shrink: 0;
}

.msg-content {
  flex: 1;
  min-width: 0;
}
.msg-title {
  font-weight: 600;
  font-size: 15px;
  margin-bottom: 5px;
  color: #1f2937;
}
.msg-text {
  font-size: 14px;
  color: #6b7280;
  line-height: 1.5;
}
.msg-time {
  font-size: 12px;
  color: #6b7280;
  margin-top: 6px;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #6b7280;
}
.empty-icon {
  font-size: 50px;
  margin-bottom: 15px;
  opacity: 0.4;
  display: block;
  margin-left: auto;
  margin-right: auto;
}

.page-btn {
  padding: 8px 16px;
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  font-size: 13px;
  color: #6b7280;
  cursor: pointer;
}
.page-btn:hover:not(:disabled) {
  border-color: #6366f1;
  color: #6366f1;
}
.page-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

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

@keyframes fadePanel {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
