<template>
  <!-- 单根包裹：与 UserLayout 的 page-fade 过渡配合，避免 flex/overflow 子树作为过渡根时偶发不可见 -->
  <div class="ai-chat-route w-full min-w-0">
  <!-- 统一深色渐变：侧栏与主区同一色系，避免「四周黑、中间白块」的割裂感 -->
  <div
    class="ai-chat-shell flex min-h-[calc(100vh-9rem)] md:h-[calc(100vh-11rem)] max-w-[1200px] w-full mx-auto my-4 md:my-5 px-2 sm:px-4 overflow-hidden rounded-[20px] border border-white/10 shadow-[0_20px_50px_-15px_rgba(15,23,42,0.85)] bg-gradient-to-b from-slate-950 via-indigo-950/95 to-slate-900 text-slate-200"
  >
    <aside
      class="w-72 border-r border-white/10 bg-slate-900/50 backdrop-blur-md flex flex-col shrink-0"
    >
      <div class="p-4 border-b border-white/10">
        <button
          type="button"
          class="w-full flex items-center justify-center gap-2 px-4 py-3 bg-gradient-to-r from-[#6366f1] to-[#a855f7] text-white rounded-[12px] text-sm font-semibold hover:opacity-95 hover:shadow-[0_8px_24px_rgba(99,102,241,0.35)] transition-all"
          @click="newChat"
        >
          <i class="fa fa-plus"></i>新对话
        </button>
      </div>

      <div class="flex-1 overflow-y-auto p-3 space-y-1 ai-scroll">
        <p class="px-3 pt-2 pb-2 text-[11px] text-slate-500 uppercase tracking-wider font-semibold">历史会话</p>
        <button
          v-for="chat in chatHistory"
          :key="chat.id"
          type="button"
          :class="[
            'w-full text-left px-3 py-3 rounded-[12px] text-sm transition-all group',
            activeChatId === chat.id
              ? 'bg-indigo-500/25 text-indigo-200 font-semibold ring-1 ring-indigo-400/30'
              : 'text-slate-400 hover:bg-white/5 hover:text-slate-200',
          ]"
          @click="loadChat(chat)"
        >
          <div class="flex items-center justify-between gap-2">
            <span class="truncate">{{ chat.title }}</span>
            <button
              type="button"
              class="opacity-0 group-hover:opacity-100 p-1 text-slate-500 hover:text-rose-400 transition-all shrink-0"
              @click.stop="deleteChat(chat.id)"
            >
              <i class="fa fa-trash-o text-xs"></i>
            </button>
          </div>
        </button>

        <div v-if="chatHistory.length === 0" class="text-center py-8 text-slate-500 text-sm">暂无历史记录</div>
      </div>

      <div class="border-t border-white/10 p-4 bg-slate-950/40">
        <p class="px-2 pb-3 text-xs text-slate-500 font-semibold">AI 功能</p>
        <div class="grid grid-cols-2 gap-2">
          <button
            v-for="feature in aiFeatures"
            :key="feature.name"
            type="button"
            :class="[
              'flex items-center gap-2 px-3 py-2.5 rounded-[10px] text-xs transition-all border',
              selectedFeatures.find(f => f.name === feature.name)
                ? 'bg-indigo-500/30 border-indigo-400/40 text-indigo-100'
                : 'bg-white/5 border-white/10 text-slate-400 hover:bg-white/10 hover:text-slate-200',
            ]"
            @click="toggleFeature(feature)"
          >
            <i :class="['fa', getFeatureIcon(feature.name)]"></i>
            {{ feature.name }}
          </button>
        </div>
      </div>
    </aside>

    <main class="flex-1 flex flex-col min-w-0 bg-slate-900/20">
      <header
        class="h-14 border-b border-white/10 bg-slate-900/40 backdrop-blur-sm flex items-center px-4 md:px-6 gap-3 shrink-0"
      >
        <div
          class="w-8 h-8 rounded-lg bg-gradient-to-br from-[#6366f1] to-[#a855f7] flex items-center justify-center shadow-lg shadow-indigo-900/50"
        >
          <i class="fa fa-robot text-white text-sm"></i>
        </div>
        <span class="font-bold text-slate-100">AI 助手</span>
        <span class="text-xs text-emerald-300 bg-emerald-500/15 border border-emerald-400/30 px-2 py-0.5 rounded-full font-medium"
          >在线</span
        >
        <span v-if="isLoading" class="ml-auto flex items-center gap-1.5 text-xs text-indigo-300">
          <i class="fa fa-spinner animate-spin"></i>思考中...
        </span>
      </header>

      <div ref="chatContainerRef" class="flex-1 overflow-y-auto p-4 md:p-6 space-y-6 ai-scroll bg-gradient-to-b from-transparent to-slate-950/30">
        <div
          v-if="messages.length === 0"
          class="flex flex-col items-center justify-center min-h-[280px] text-center px-2"
        >
          <div
            class="w-20 h-20 rounded-2xl bg-gradient-to-br from-indigo-500/30 to-fuchsia-500/20 border border-white/10 flex items-center justify-center mb-6"
          >
            <i class="fa fa-magic text-3xl text-indigo-300"></i>
          </div>
          <h3 class="text-xl font-bold text-slate-100 mb-2">有什么可以帮你的？</h3>
          <p class="text-sm text-slate-400 mb-8 max-w-md leading-relaxed">
            基于 Spring AI 的智能助手：写代码、润色、总结、话题灵感等
          </p>

          <div class="grid grid-cols-2 sm:grid-cols-3 gap-3 max-w-3xl w-full">
            <button
              v-for="suggestion in suggestions"
              :key="suggestion.text"
              type="button"
              class="p-4 rounded-xl text-left transition-all border border-white/10 bg-white/5 hover:bg-white/10 hover:border-indigo-400/30 group"
              @click="sendSuggestion(suggestion.text)"
            >
              <i
                :class="[
                  'fa',
                  getSuggestionIcon(suggestion.label),
                  'text-indigo-400 mb-2 group-hover:scale-110 transition-transform text-lg',
                ]"
              ></i>
              <p class="text-sm font-semibold text-slate-100">{{ suggestion.label }}</p>
              <p class="text-xs text-slate-500 mt-0.5 line-clamp-2">{{ suggestion.desc }}</p>
            </button>
          </div>
        </div>

        <template v-else>
          <div v-for="msg in messages" :key="msg.id" :class="['flex gap-3 md:gap-4', msg.role === 'user' ? 'justify-end' : '']">
            <template v-if="msg.role === 'assistant'">
              <div
                class="w-9 h-9 rounded-lg bg-gradient-to-br from-[#6366f1] to-[#a855f7] flex items-center justify-center shrink-0 mt-1 shadow-md"
              >
                <i class="fa fa-robot text-white text-sm"></i>
              </div>
              <div class="max-w-[85%] md:max-w-[80%]">
                <div
                  class="prose prose-sm max-w-none rounded-2xl rounded-tl-sm p-4 leading-relaxed border border-white/10 bg-slate-800/70 text-slate-300 shadow-inner shadow-black/20 ai-msg-md"
                  v-html="renderMarkdown(msg.content)"
                ></div>
                <div class="flex items-center gap-3 mt-2 ml-1">
                  <span class="text-xs text-slate-500">{{ formatTime(msg.createTime) }}</span>
                </div>
              </div>
            </template>

            <template v-else>
              <div class="max-w-[85%] md:max-w-[70%]">
                <div
                  class="bg-gradient-to-r from-[#6366f1] to-[#a855f7] rounded-2xl rounded-tr-sm px-4 py-3 text-white leading-relaxed shadow-lg shadow-indigo-900/40"
                >
                  {{ msg.content }}
                </div>
                <span class="text-xs text-slate-500 mt-1 block text-right pr-1">{{ formatTime(msg.createTime) }}</span>
              </div>
            </template>
          </div>

          <div v-if="isLoading" class="flex gap-3 md:gap-4">
            <div
              class="w-9 h-9 rounded-lg bg-gradient-to-br from-[#6366f1] to-[#a855f7] flex items-center justify-center shrink-0 mt-1"
            >
              <i class="fa fa-robot text-white text-sm"></i>
            </div>
            <div class="rounded-2xl rounded-tl-sm px-4 py-3 border border-white/10 bg-slate-800/60">
              <div class="flex gap-1.5">
                <span class="w-2 h-2 bg-indigo-400/80 rounded-full animate-bounce" style="animation-delay: 0ms"></span>
                <span class="w-2 h-2 bg-indigo-400/80 rounded-full animate-bounce" style="animation-delay: 150ms"></span>
                <span class="w-2 h-2 bg-indigo-400/80 rounded-full animate-bounce" style="animation-delay: 300ms"></span>
              </div>
            </div>
          </div>
        </template>
      </div>

      <div class="border-t border-white/10 p-4 shrink-0 bg-slate-950/50 backdrop-blur-md">
        <div class="max-w-4xl mx-auto">
          <div v-if="selectedFeatures.length > 0" class="flex flex-wrap gap-2 mb-3">
            <span
              v-for="f in selectedFeatures"
              :key="f.name"
              class="flex items-center gap-1 bg-indigo-500/20 text-indigo-200 border border-indigo-400/25 px-2 py-1 rounded-lg text-xs"
            >
              {{ f.name }}
              <button type="button" class="ml-1 text-indigo-300 hover:text-white" @click="removeFeature(f)">×</button>
            </span>
          </div>

          <div class="flex flex-wrap gap-2 mb-3">
            <button
              v-for="(q, i) in quickPrompts"
              :key="i"
              type="button"
              class="text-xs px-3 py-1.5 rounded-lg border border-white/10 text-slate-400 hover:text-indigo-200 hover:border-indigo-400/35 hover:bg-white/5 transition-colors bg-white/5"
              @click="inputMessage = q; inputRef?.focus()"
            >
              {{ ['翻译', '总结', '注释', '测试'][i] }}
            </button>
          </div>

          <div
            class="flex items-end gap-3 rounded-2xl border border-white/10 bg-slate-800/50 focus-within:border-indigo-400/40 focus-within:ring-2 focus-within:ring-indigo-500/20 transition-all p-2"
          >
            <textarea
              ref="inputRef"
              v-model="inputMessage"
              placeholder="输入你的问题... (Enter发送, Shift+Enter换行)"
              rows="1"
              class="flex-1 resize-none bg-transparent px-4 py-3 outline-none text-sm text-slate-100 max-h-32 placeholder:text-slate-500"
              @keydown.enter.exact.prevent="send"
              @input="autoResize($event)"
            ></textarea>
            <div class="flex items-center gap-2 pb-1 pr-1">
              <button type="button" class="p-2 text-slate-500 hover:text-indigo-300 transition-colors" title="上传文件">
                <i class="fa fa-paperclip"></i>
              </button>
              <button
                type="button"
                :disabled="!inputMessage.trim() || isLoading"
                class="px-4 py-2.5 bg-gradient-to-r from-[#6366f1] to-[#a855f7] text-white rounded-xl hover:shadow-lg hover:shadow-indigo-900/50 disabled:opacity-50 disabled:cursor-not-allowed transition-all font-medium"
                @click="send"
              >
                <i class="fa fa-paper-plane"></i>
              </button>
            </div>
          </div>
          <p class="text-xs text-center text-slate-500 mt-2">AI 助手可能产生不准确的信息，请核实重要内容</p>
        </div>
      </div>
    </main>
  </div>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick, onMounted } from 'vue'
import { aiApi } from '@/api'

interface ChatMsg { id: number; role: 'user' | 'assistant'; content: string; createTime: number }
interface ChatItem { id: number; title: string; messages: ChatMsg[]; sessionId?: string }
interface Feature {
  name: string
  /** 可选；图标由 getFeatureIcon(name) 按名称映射 */
  icon?: unknown
  prompt: string
}

const inputRef = ref<HTMLTextAreaElement>()
const chatContainerRef = ref<HTMLElement>()
const inputMessage = ref('')
const isLoading = ref(false)
const activeChatId = ref<number | null>(null)
const currentSessionId = ref<string | null>(null)

const messages = ref<ChatMsg[]>([])
const chatHistory = ref<ChatItem[]>([])

// 选中的功能角色
const selectedFeatures = ref<Feature[]>([])

const aiFeatures = [
  { name: '写作助手', prompt: '你是一名优秀的作家，非常擅长写作' },
  { name: '代码审查', prompt: '你是一名资深的程序员，可以完成复杂的代码审查工作' },
  { name: '内容总结', prompt: '你是一名记者，非常擅长内容总结' },
  { name: '文章润色', prompt: '你是一名文学家，非常擅长润色文章，使其更加专业流畅' },
  { name: '话题推荐', prompt: '你是一名记者，非常了解当今的局势，可以推荐与当今紧密相关的话题' },
  { name: '智能搜索', prompt: '你是一本百科全书，什么都知道都难不到你' },
]

const suggestions = [
  { label: '写代码', desc: '帮你编写或调试代码', text: '帮我写一个 Vue 3 + TypeScript 的列表组件，带加载态与空状态' },
  { label: '润色文章', desc: '优化你的文章表达', text: '请把下面文字改得更口语、更有感染力，保留原意：' },
  { label: '中英翻译', desc: '互译与术语统一', text: '将下面内容翻译成英文，技术术语保持常用译法：' },
  { label: '总结内容', desc: '提炼核心要点', text: '请用三条要点总结下面内容，并给出一个标题：' },
  { label: '推荐话题', desc: '获取热门话题灵感', text: '我是技术社区运营，请推荐 5 个适合发帖的话题并各给一句引导语' },
  { label: 'SQL / 接口', desc: '表结构与查询', text: '请根据描述设计 MySQL 表字段（含索引建议）并写一条示例查询：' },
]

const quickPrompts = [
  '将下面内容翻译成英文：\n',
  '用三句话总结下面内容：\n',
  '为下面代码添加简短中文注释：\n',
  '列出这段需求可能的边界情况与测试点：\n',
]

function getFeatureIcon(name: string): string {
  const icons: Record<string, string> = {
    '写作助手': 'fa-pencil',
    '代码审查': 'fa-code',
    '内容总结': 'fa-file-text-o',
    '文章润色': 'fa-magic',
    '话题推荐': 'fa-hashtag',
    '智能搜索': 'fa-search'
  }
  return icons[name] || 'fa-question'
}

function getSuggestionIcon(label: string): string {
  const icons: Record<string, string> = {
    '写代码': 'fa-code',
    '润色文章': 'fa-pencil',
    '中英翻译': 'fa-language',
    '总结内容': 'fa-file-text-o',
    '推荐话题': 'fa-book',
    'SQL / 接口': 'fa-database',
  }
  return icons[label] || 'fa-question'
}

// 切换功能（添加/移除）
function toggleFeature(feature: Feature) {
  const index = selectedFeatures.value.findIndex(f => f.name === feature.name)
  if (index >= 0) {
    selectedFeatures.value.splice(index, 1)
  } else {
    selectedFeatures.value.push(feature)
  }
}

// 移除单个功能
function removeFeature(feature: Feature) {
  selectedFeatures.value = selectedFeatures.value.filter(f => f.name !== feature.name)
}

// 拼接所有角色提示词
function buildSystemPrompt() {
  if (selectedFeatures.value.length === 0) return ''
  return selectedFeatures.value.map(f => f.prompt).join('。') + '。'
}

// 加载会话列表
async function loadChatHistory() {
  try {
    const res: any = await aiApi.chats()
    const list: ChatItem[] = []
    if (Array.isArray(res)) {
      res.forEach((item: any, index: number) => {
        const sessionId = item.session_id || item.sessionId
        let title = item.title || "新对话"
        list.push({ id: index + 1, title, sessionId, messages: [] })
      })
    }
    chatHistory.value = list
  } catch (e) {
    console.error('加载会话失败', e)
    // 即使API失败，也保持空数组，确保页面正常显示
    chatHistory.value = []
  }
}

function newChat() {
  messages.value = []
  activeChatId.value = null
  currentSessionId.value = null
  selectedFeatures.value = []
  setTimeout(() => inputRef.value?.focus(), 100)
}

async function loadChat(chat: ChatItem) {
  activeChatId.value = chat.id
  currentSessionId.value = chat.sessionId || null
  if (chat.sessionId) {
    try {
      const res: any = await aiApi.messages(chat.sessionId)
      const messageList = res.records || res
      if (Array.isArray(messageList)) {
        messages.value = messageList.map((item: any) => ({
          id: item.id || Date.now(),
          role: item.role === 'user' ? 'user' : 'assistant',
          content: item.content,
          createTime: new Date(item.createTime).getTime()
        }))
      }
    } catch (e) {
      console.error('加载聊天记录失败', e)
    }
  }
  scrollToBottom()
}

async function deleteChat(chatId: number) {
  const find = chatHistory.value.find(i => i.id === chatId)
  if (!find || !find.sessionId) return
  try {
    await aiApi.deleteChat(parseInt(find.sessionId))
    chatHistory.value = chatHistory.value.filter(i => i.id !== chatId)
    if (activeChatId.value === chatId) newChat()
  } catch (err) {
    console.error('删除失败', err)
  }
}

function getSessionId(sessionId: string | undefined): string | undefined {
  return sessionId || undefined
}

async function send() {
  if (!inputMessage.value.trim() || isLoading.value) return

  // 拼接角色 + 用户问题
  const systemPrompt = buildSystemPrompt()
  const userInput = systemPrompt + inputMessage.value.trim()

  const userMsg: ChatMsg = {
    id: Date.now(),
    role: 'user',
    content: inputMessage.value.trim(), // 输入框内容只显示用户输入
    createTime: Date.now(),
  }
  messages.value.push(userMsg)
  inputMessage.value = ''
  scrollToBottom()

  isLoading.value = true
  try {
    const response = await aiApi.send({
      sessionId: currentSessionId.value || undefined,
      content: userInput,
      type: 'chat'
    })

    const aiMsg: ChatMsg = {
      id: Date.now(),
      role: 'assistant',
      content: (response as any).reply || response,
      createTime: Date.now(),
    }
    messages.value.push(aiMsg)

    if (!currentSessionId.value) {
      const newSessionId = (response as any).sessionId
      const title = userMsg.content.length > 20 ? userMsg.content.slice(0, 20) + '...' : userMsg.content
      const newChat: ChatItem = { id: Date.now(), title, sessionId: String(newSessionId), messages: [] }
      chatHistory.value.unshift(newChat)
      activeChatId.value = newChat.id
      currentSessionId.value = String(newSessionId)
    }

  } catch (error) {
    messages.value.push({
      id: Date.now(),
      role: 'assistant',
      content: '抱歉，AI服务暂时不可用，请稍后重试。',
      createTime: Date.now(),
    })
  } finally {
    isLoading.value = false
    scrollToBottom()
  }
}

function sendSuggestion(text: string) {
  inputMessage.value = text
  nextTick(() => send())
}

function useFeature(prompt: string) {
  inputMessage.value = prompt
  newChat()
  nextTick(() => send())
}

function scrollToBottom() {
  nextTick(() => {
    if (chatContainerRef.value) {
      chatContainerRef.value.scrollTop = chatContainerRef.value.scrollHeight
    }
  })
}

function autoResize(event: Event) {
  const target = event.target as HTMLTextAreaElement
  target.style.height = 'auto'
  target.style.height = Math.min(target.scrollHeight, 128) + 'px'
}

function formatTime(time: number): string {
  return new Date(time).toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

function renderMarkdown(text: string): string {
  return text
    .replace(
      /```(\w*)\n?([\s\S]*?)```/g,
      '<pre class="ai-md-pre"><code>$2</code></pre>',
    )
    .replace(/\*\*(.*?)\*\*/g, '<strong class="ai-md-strong">$1</strong>')
    .replace(/^### (.*$)/gm, '<h4 class="ai-md-h4">$1</h4>')
    .replace(/^## (.*$)/gm, '<h3 class="ai-md-h3">$1</h3>')
    .replace(/^- (.*$)/gm, '<li class="ai-md-li">$1</li>')
    .replace(/\n/g, '<br/>')
}

onMounted(() => {
  loadChatHistory()
})
</script>

<style scoped>
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.ai-scroll {
  scrollbar-width: thin;
  scrollbar-color: rgba(99, 102, 241, 0.35) transparent;
}

.ai-scroll::-webkit-scrollbar {
  width: 6px;
}

.ai-scroll::-webkit-scrollbar-thumb {
  background: rgba(99, 102, 241, 0.35);
  border-radius: 3px;
}

.ai-scroll::-webkit-scrollbar-thumb:hover {
  background: rgba(129, 140, 248, 0.5);
}

:deep(.ai-md-pre) {
  background: rgba(15, 23, 42, 0.9) !important;
  color: #a7f3d0 !important;
  padding: 0.75rem 1rem;
  border-radius: 0.75rem;
  margin: 0.5rem 0;
  font-size: 0.75rem;
  overflow-x: auto;
  border: 1px solid rgba(255, 255, 255, 0.08);
}

:deep(.ai-md-pre code) {
  font-family: 'Fira Code', 'Consolas', monospace;
  background: transparent !important;
  color: inherit !important;
}

:deep(.ai-md-strong) {
  color: #e2e8f0;
  font-weight: 600;
}

:deep(.ai-md-h3) {
  color: #f1f5f9;
  font-weight: 700;
  margin-top: 0.75rem;
  margin-bottom: 0.25rem;
  font-size: 1rem;
}

:deep(.ai-md-h4) {
  color: #e2e8f0;
  font-weight: 600;
  margin-top: 0.5rem;
  margin-bottom: 0.25rem;
  font-size: 0.875rem;
}

:deep(.ai-md-li) {
  color: #cbd5e1;
  margin-left: 1rem;
  list-style: disc;
}
</style>
