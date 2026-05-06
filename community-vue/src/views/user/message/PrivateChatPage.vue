<template>
  <div class="private-chat-page min-h-[calc(100vh-4.5rem)] flex flex-col bg-gradient-to-br from-[#f0f4ff] to-[#f8faff] text-[#1f2937]">
    <!-- 页内顶栏（对齐私信聊天.html） -->
    <header class="sticky top-0 z-[100] bg-white shadow-[0_2px_20px_rgba(0,0,0,0.08)] py-4 shrink-0">
      <div class="max-w-[1200px] mx-auto px-5 flex items-center justify-between">
        <div class="text-[22px] font-bold flex items-center gap-2 bg-gradient-to-r from-[#6366f1] to-[#a855f7] bg-clip-text text-transparent">
          <i class="fa fa-envelope text-[#6366f1]"></i>
          <span>私信聊天</span>
        </div>
        <router-link
          to="/message"
          class="text-sm text-[#6366f1] font-medium hover:underline flex items-center gap-1"
        >
          <i class="fa fa-bell-o"></i> 消息通知
        </router-link>
      </div>
    </header>

    <!-- 主布局：左列表 + 右聊天 -->
    <div
      class="layout-card flex flex-1 max-w-[1200px] w-full mx-auto my-5 mb-8 px-4 md:px-5 min-h-0 md:h-[calc(100vh-11rem)] h-[calc(100vh-8rem)]"
    >
      <div
        class="flex flex-1 min-h-0 bg-white rounded-[20px] overflow-hidden shadow-[0_10px_30px_-5px_rgba(99,102,241,0.15)] w-full"
      >
        <!-- 左侧会话 -->
        <aside
          class="sidebar w-full md:w-[320px] shrink-0 border-r border-[#e5e7eb] flex flex-col min-h-0"
          :class="{ 'hidden md:flex': compact && mobileChatOpen }"
        >
          <div class="sidebar-head px-5 pt-5 pb-4 border-b border-[#f3f4f6] shrink-0">
            <div class="text-lg font-bold mb-3">我的私信</div>
            <div class="search-bar relative">
              <i class="fa fa-search absolute left-[15px] top-1/2 -translate-y-1/2 text-[#6b7280] text-sm"></i>
              <input
                v-model="searchKw"
                type="search"
                class="w-full py-2.5 pl-10 pr-3 rounded-xl border border-[#e5e7eb] outline-none text-sm bg-[#f9fafb] focus:border-[#818cf8]"
                placeholder="搜索联系人..."
              />
            </div>
          </div>

          <div class="chat-list flex-1 overflow-y-auto min-h-0">
            <button
              v-for="c in filteredConversations"
              :key="c.otherUserId"
              type="button"
              class="chat-item w-full text-left border-0 bg-transparent"
              :class="{ active: selectedPeer?.otherUserId === c.otherUserId }"
              @click="selectPeer(c)"
            >
              <img
                :src="resolveFileUrl(c.avatar) || defaultAvatar"
                alt=""
                class="avatar-img"
              />
              <div class="chat-info min-w-0">
                <div class="chat-name">{{ c.nickName || '用户' }}</div>
                <div class="chat-last-msg">{{ c.lastMessage || '暂无消息' }}</div>
              </div>
              <div class="chat-time">{{ formatShortTime(c.lastTime) }}</div>
              <div
                v-if="c.unreadCount && c.unreadCount > 0"
                class="unread-badge"
              >
                {{ c.unreadCount > 99 ? '99+' : c.unreadCount }}
              </div>
            </button>

            <div v-if="!listLoading && filteredConversations.length === 0" class="py-12 text-center text-[#9ca3af] text-sm px-4">
              <i class="fa fa-envelope-o text-3xl mb-2 opacity-50"></i>
              <p>暂无私信，去消息页或用户主页发起对话吧</p>
            </div>
            <div v-if="listLoading" class="p-4 space-y-3">
              <div v-for="i in 4" :key="i" class="flex gap-3 animate-pulse">
                <div class="w-[50px] h-[50px] rounded-full bg-[#e5e7eb]"></div>
                <div class="flex-1 space-y-2">
                  <div class="h-3 bg-[#e5e7eb] rounded w-1/3"></div>
                  <div class="h-2 bg-[#f3f4f6] rounded w-full"></div>
                </div>
              </div>
            </div>
          </div>
        </aside>

        <!-- 右侧聊天 -->
        <section
          class="chat-box flex-1 flex flex-col min-h-0 min-w-0"
          :class="{ 'hidden md:flex': compact && !mobileChatOpen }"
        >
          <template v-if="selectedPeer">
            <div class="chat-header px-6 py-4 border-b border-[#f3f4f6] flex items-center gap-3 bg-white shrink-0">
              <button
                v-if="compact"
                type="button"
                class="md:hidden p-2 -ml-2 text-[#6366f1]"
                aria-label="返回"
                @click="mobileChatOpen = false"
              >
                <i class="fa fa-angle-left text-xl"></i>
              </button>
              <img
                :src="resolveFileUrl(selectedPeer.avatar) || defaultAvatar"
                alt=""
                class="w-10 h-10 rounded-full object-cover border-2 border-white shadow-sm"
              />
              <div class="min-w-0">
                <div class="chat-header-name font-semibold text-base truncate">
                  {{ selectedPeer.nickName || '用户' }}
                </div>
                <div class="chat-header-status text-xs flex items-center gap-1"
                     :class="wsConnected ? 'text-emerald-500' : 'text-amber-500'">
                  <i class="fa fa-circle text-[6px]"></i>
                  {{ wsConnected ? '在线' : connectionStatusText }}
                </div>
              </div>
              <router-link
                :to="`/user/${selectedPeer.otherUserId}`"
                class="ml-auto text-xs text-[#6366f1] hover:underline shrink-0"
              >
                主页
              </router-link>
            </div>

            <div ref="msgAreaRef" class="message-area flex-1 overflow-y-auto min-h-0">
              <div
                v-for="m in messages"
                :key="m.id"
                class="msg"
                :class="isMine(m) ? 'out' : 'in'"
              >
                <div class="msg-bubble">{{ m.content }}</div>
                <div class="msg-time">{{ formatMsgTime(m.createTime) }}</div>
              </div>
              <div v-if="!msgLoading && messages.length === 0" class="empty-chat">
                <i class="fa fa-comments-o"></i>
                <p>还没有消息，打个招呼吧</p>
              </div>
            </div>

            <div class="input-area px-5 py-4 border-t border-[#f3f4f6] flex gap-3 items-end bg-white shrink-0">
              <textarea
                v-model="inputText"
                class="msg-input"
                placeholder="输入消息...（Enter 发送，Shift+Enter 换行）"
                rows="1"
                @keydown.enter.exact.prevent="send"
                @input="autoGrow"
              ></textarea>
              <button type="button" class="send-btn shrink-0" :disabled="sending || !inputText.trim()" @click="send">
                <i class="fa fa-paper-plane"></i>
              </button>
            </div>
          </template>

          <div v-else class="empty-chat flex-1 bg-[#fafbff]">
            <i class="fa fa-envelope-open-o"></i>
            <p>选择左侧会话开始聊天</p>
          </div>
        </section>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { messageApi } from '../../../api'
import { resolveFileUrl } from '../../../utils/resolveFileUrl'
import { normalizeConversationList } from '../../../utils/conversation'
import { useUserStore } from '../../../store/user'
import { chatSocket, ConnectionStatus, type ChatMessage } from '../../../utils/chatSocket'
import type { Conversation, PrivateMessage } from '../../../types'

const route = useRoute()
const userStore = useUserStore()

const defaultAvatar = 'https://placehold.co/80x80/E8EAF6/9FA8DA?text=U'

const searchKw = ref('')
const listLoading = ref(true)
const conversations = ref<Conversation[]>([])
const selectedPeer = ref<Conversation | null>(null)
const mobileChatOpen = ref(false)
const compact = ref(false)
const wsConnected = ref(false) // WebSocket连接状态

const messages = ref<PrivateMessage[]>([])
const msgLoading = ref(false)
const inputText = ref('')
const sending = ref(false)
const msgAreaRef = ref<HTMLElement | null>(null)

const myId = computed(() => (userStore.userId ? Number(userStore.userId) : 0))

// WebSocket连接状态显示
const connectionStatusText = computed(() => {
  switch (chatSocket.getStatus()) {
    case ConnectionStatus.CONNECTED:
      return '已连接'
    case ConnectionStatus.CONNECTING:
      return '连接中...'
    case ConnectionStatus.RECONNECTING:
      return '重连中...'
    case ConnectionStatus.ERROR:
      return '连接错误'
    default:
      return '未连接'
  }
})

const filteredConversations = computed(() => {
  const kw = searchKw.value.trim().toLowerCase()
  if (!kw) return conversations.value
  return conversations.value.filter((c) => (c.nickName || '').toLowerCase().includes(kw))
})

function updateCompact() {
  compact.value = typeof window !== 'undefined' && window.matchMedia('(max-width: 767px)').matches
}

async function loadList() {
  listLoading.value = true
  try {
    const res: unknown = await messageApi.conversations()
    conversations.value = normalizeConversationList(res)
    const sid = selectedPeer.value?.otherUserId
    if (sid) {
      const found = conversations.value.find((x) => x.otherUserId === sid)
      if (found) selectedPeer.value = found
    }
  } catch (e) {
    console.error(e)
    conversations.value = []
  } finally {
    listLoading.value = false
  }
}

function formatShortTime(t: string): string {
  if (!t) return ''
  const d = new Date(t)
  if (Number.isNaN(d.getTime())) return t
  const now = new Date()
  if (d.toDateString() === now.toDateString()) {
    return d.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  }
  const y = new Date(now)
  y.setDate(y.getDate() - 1)
  if (d.toDateString() === y.toDateString()) return '昨天'
  return d.toLocaleDateString('zh-CN', { month: 'numeric', day: 'numeric' })
}

function formatMsgTime(t: string | number): string {
  if (!t) return ''
  const d = new Date(t)
  return d.toLocaleString('zh-CN', { month: 'numeric', day: 'numeric', hour: '2-digit', minute: '2-digit' })
}

function isMine(m: PrivateMessage): boolean {
  return myId.value > 0 && m.fromUserId === myId.value
}

async function loadMessages(peerId: number) {
  msgLoading.value = true
  try {
    const res: any = await messageApi.history(peerId, 1, 80)
    const records = res?.records || res?.list || []
    messages.value = Array.isArray(records) ? records : []
    await nextTick()
    scrollBottom()
  } catch (e) {
    console.error(e)
    messages.value = []
  } finally {
    msgLoading.value = false
  }
}

function scrollBottom() {
  const el = msgAreaRef.value
  if (el) el.scrollTop = el.scrollHeight
}

async function selectPeer(c: Conversation) {
  selectedPeer.value = c
  if (compact.value) mobileChatOpen.value = true
  await loadMessages(c.otherUserId)

  // 优先使用WebSocket标记已读
  if (chatSocket.isConnected()) {
    chatSocket.markAsRead(c.otherUserId)
    c.unreadCount = 0
  } else {
    // 回退到HTTP
    try {
      await messageApi.markRead(c.otherUserId)
      c.unreadCount = 0
    } catch {
      /* ignore */
    }
  }
}

function autoGrow(e: Event) {
  const ta = e.target as HTMLTextAreaElement
  ta.style.height = 'auto'
  ta.style.height = Math.min(ta.scrollHeight, 120) + 'px'
}

async function send() {
  const text = inputText.value.trim()
  if (!text || !selectedPeer.value || sending.value) return

  // 优先使用WebSocket发送
  if (chatSocket.isConnected()) {
    sending.value = true
    try {
      const success = chatSocket.sendMessage(selectedPeer.value.otherUserId, text)
      if (success) {
        inputText.value = ''
        // 乐观更新：立即在本地显示消息
        const tempMsg: PrivateMessage = {
          id: Date.now(), // 临时ID
          fromUserId: myId.value,
          toUserId: selectedPeer.value.otherUserId,
          content: text,
          createTime: new Date().toISOString(),
          isRead: false
        }
        messages.value.push(tempMsg)
        selectedPeer.value.lastMessage = text
        selectedPeer.value.lastTime = new Date().toISOString()
        await nextTick()
        scrollBottom()
      } else {
        ElMessage.error('发送失败，请重试')
      }
    } finally {
      sending.value = false
    }
  } else {
    // WebSocket未连接时回退到HTTP
    sending.value = true
    try {
      const sent: any = await messageApi.send({ toUserId: selectedPeer.value.otherUserId, content: text })
      inputText.value = ''
      if (sent?.id) {
        messages.value.push(sent)
      } else {
        await loadMessages(selectedPeer.value.otherUserId)
      }
      selectedPeer.value.lastMessage = text
      selectedPeer.value.lastTime = new Date().toISOString()
      await loadList()
      await nextTick()
      scrollBottom()
    } catch (e) {
      console.error(e)
      ElMessage.error('发送失败')
    } finally {
      sending.value = false
    }
  }
}

/**
 * 处理收到的WebSocket消息
 */
function handleIncomingMessage(msg: ChatMessage) {
  // 判断是否是当前选中的对话
  if (selectedPeer.value &&
      (msg.fromUserId === selectedPeer.value.otherUserId || msg.toUserId === selectedPeer.value.otherUserId)) {
    // 添加到消息列表
    const newMsg: PrivateMessage = {
      id: Date.now(), // 临时ID，后续可通过后端确认更新
      fromUserId: msg.fromUserId,
      toUserId: msg.toUserId,
      content: msg.content,
      createTime: new Date(msg.timestamp).toISOString(),
      isRead: true // 当前正在查看，标记为已读
    }
    messages.value.push(newMsg)
    nextTick(() => scrollBottom())

    // 如果是对方发来的消息，标记为已读
    if (msg.fromUserId === selectedPeer.value.otherUserId) {
      chatSocket.markAsRead(msg.fromUserId)
    }
  } else {
    // 不是当前对话，更新未读数
    const conv = conversations.value.find(c => c.otherUserId === msg.fromUserId)
    if (conv) {
      conv.unreadCount = (conv.unreadCount || 0) + 1
      conv.lastMessage = msg.content
      conv.lastTime = new Date(msg.timestamp).toISOString()
    }
    // 播放提示音（可选）
    // new Audio('/notification.mp3').play().catch(() => {})
  }
}

/**
 * 处理已读回执
 */
function handleReadReceipt(receipt: { readerId: number; timestamp: number }) {
  // 更新消息的已读状态
  messages.value.forEach(msg => {
    if (msg.toUserId === receipt.readerId) {
      msg.isRead = true
    }
  })
}

function trySelectFromQuery() {
  const uid = route.query.userId ?? route.query.toUserId
  if (!uid) return
  const id = Number(uid)
  if (!id) return
  const name = typeof route.query.nickName === 'string' ? route.query.nickName : undefined
  let c = conversations.value.find((x) => x.otherUserId === id)
  if (!c) {
    c = {
      otherUserId: id,
      nickName: name || `用户${id}`,
      avatar: '',
      lastMessage: '',
      lastTime: '',
      unreadCount: 0,
    }
    conversations.value.unshift(c)
  }
  selectPeer(c)
}

watch(
  () => [route.query.userId, route.query.toUserId],
  () => {
    trySelectFromQuery()
  },
)

// WebSocket事件监听清理函数
let unsubscribeMessage: (() => void) | null = null
let unsubscribeReadReceipt: (() => void) | null = null
let unsubscribeStatus: (() => void) | null = null

onMounted(async () => {
  updateCompact()
  window.addEventListener('resize', updateCompact)
  await loadList()
  trySelectFromQuery()

  // 建立WebSocket连接
  chatSocket.connect()

  // 监听WebSocket消息
  unsubscribeMessage = chatSocket.onMessage(handleIncomingMessage)
  unsubscribeReadReceipt = chatSocket.onReadReceipt(handleReadReceipt)
  unsubscribeStatus = chatSocket.onStatusChange((status) => {
    wsConnected.value = status === ConnectionStatus.CONNECTED
    if (status === ConnectionStatus.CONNECTED) {
      console.log('[PrivateChat] WebSocket已连接')
    } else if (status === ConnectionStatus.ERROR) {
      ElMessage.error('聊天服务连接异常')
    }
  })
})

onUnmounted(() => {
  window.removeEventListener('resize', updateCompact)

  // 清理WebSocket监听
  unsubscribeMessage?.()
  unsubscribeReadReceipt?.()
  unsubscribeStatus?.()

  // 断开WebSocket连接
  chatSocket.disconnect()
})
</script>

<style scoped>
.layout-card {
  box-sizing: border-box;
}

.chat-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 20px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
}
.chat-item:hover {
  background: #f9fafb;
}
.chat-item.active {
  background: #eef2ff;
  border-left: 4px solid #6366f1;
}

.avatar-img {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
  border: 2px solid white;
}

.chat-name {
  font-weight: 600;
  font-size: 15px;
  margin-bottom: 3px;
  color: #1f2937;
}
.chat-last-msg {
  font-size: 13px;
  color: #6b7280;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.chat-time {
  font-size: 11px;
  color: #6b7280;
  position: absolute;
  right: 20px;
  top: 18px;
}
.unread-badge {
  min-width: 18px;
  height: 18px;
  background: #ec4899;
  color: white;
  border-radius: 50%;
  font-size: 11px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 4px;
  position: absolute;
  right: 20px;
  bottom: 18px;
}

.message-area {
  padding: 25px;
  background: #fafbff;
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.msg {
  max-width: 70%;
  animation: fadeIn 0.3s ease;
  display: flex;
  flex-direction: column;
}
.msg.in {
  align-self: flex-start;
}
.msg.out {
  align-self: flex-end;
}

.msg-bubble {
  padding: 12px 16px;
  border-radius: 18px;
  font-size: 14px;
  line-height: 1.5;
}
.msg.in .msg-bubble {
  background: white;
  border: 1px solid #e5e7eb;
  border-bottom-left-radius: 6px;
}
.msg.out .msg-bubble {
  background: linear-gradient(90deg, #6366f1, #a855f7);
  color: white;
  border-bottom-right-radius: 6px;
}
.msg .msg-time {
  font-size: 11px;
  color: #6b7280;
  margin-top: 4px;
  text-align: right;
}

.msg-input {
  flex: 1;
  border: 1px solid #e5e7eb;
  border-radius: 18px;
  padding: 14px 18px;
  outline: none;
  resize: none;
  min-height: 50px;
  max-height: 120px;
  font-size: 14px;
  line-height: 1.4;
}
.msg-input:focus {
  border-color: #818cf8;
}

.send-btn {
  width: 50px;
  height: 50px;
  border-radius: 16px;
  background: linear-gradient(90deg, #6366f1, #a855f7);
  color: white;
  border: none;
  font-size: 18px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}
.send-btn:hover:not(:disabled) {
  transform: scale(1.05);
  box-shadow: 0 6px 15px rgba(99, 102, 241, 0.25);
}
.send-btn:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

.empty-chat {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 200px;
  color: #6b7280;
}
.empty-chat i {
  font-size: 50px;
  margin-bottom: 15px;
  opacity: 0.4;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.chat-list {
  scrollbar-width: thin;
  scrollbar-color: #d1d5db transparent;
}
.chat-list::-webkit-scrollbar {
  width: 6px;
}
.chat-list::-webkit-scrollbar-thumb {
  background: #d1d5db;
  border-radius: 3px;
}
.message-area {
  scrollbar-width: thin;
  scrollbar-color: #d1d5db transparent;
}
.message-area::-webkit-scrollbar {
  width: 6px;
}
.message-area::-webkit-scrollbar-thumb {
  background: #d1d5db;
  border-radius: 3px;
}
</style>
