<template>
  <div class="h-[calc(100vh-4rem)] flex bg-white rounded-xl shadow-sm border border-gray-100 m-4 overflow-hidden">
    <!-- 左侧边栏 - 会话历史 -->
    <aside class="w-72 border-r border-gray-200 bg-slate-50 flex flex-col shrink-0">
      <!-- 新建对话 -->
      <div class="p-3 border-b border-gray-200">
        <button
          class="w-full flex items-center justify-center gap-2 px-4 py-2.5 bg-gradient-to-r from-primary to-blue-600 text-white rounded-lg text-sm font-medium hover:shadow-lg hover:shadow-primary/25 transition-all"
          @click="newChat"
        >
          <Plus class="w-4 h-4" />新对话
        </button>
      </div>

      <!-- 历史记录 -->
      <div class="flex-1 overflow-y-auto p-2 space-y-0.5">
        <p class="px-3 pt-2 pb-1 text-xs text-gray-400 uppercase tracking-wider font-medium">历史记录</p>
        <button
          v-for="chat in chatHistory"
          :key="chat.id"
          :class="['w-full text-left px-3 py-2.5 rounded-lg text-sm transition-colors group', activeChatId === chat.id ? 'bg-blue-50 text-primary font-medium' : 'text-gray-600 hover:bg-gray-100']"
          @click="loadChat(chat)"
        >
          <div class="flex items-center justify-between">
            <span class="truncate">{{ chat.title }}</span>
            <button class="opacity-0 group-hover:opacity-100 p-0.5 hover:text-red-500 transition-all" @click.stop="deleteChat(chat.id)">
              <Trash2 class="w-3.5 h-3.5" />
            </button>
          </div>
        </button>

        <div v-if="chatHistory.length === 0" class="text-center py-8 text-gray-400 text-sm">暂无历史记录</div>
      </div>

      <!-- AI功能快捷入口 -->
      <div class="border-t border-gray-200 p-3">
        <p class="px-2 pb-2 text-xs text-gray-400 font-medium">AI 功能</p>
        <div class="grid grid-cols-2 gap-1.5">
          <button
            v-for="feature in aiFeatures" :key="feature.name"
            class="flex items-center gap-1.5 px-2 py-2 rounded-lg text-xs text-gray-600 hover:bg-blue-50 hover:text-primary transition-colors"
            @click="useFeature(feature.prompt)"
          >
            <component :is="feature.icon" :class="'w-3.5 h-3.5'" />
            {{ feature.name }}
          </button>
        </div>
      </div>
    </aside>

    <!-- 右侧主区域 -->
    <main class="flex-1 flex flex-col min-w-0">
      <!-- 顶部栏 -->
      <header class="h-14 border-b border-gray-200 flex items-center px-6 gap-3 shrink-0">
        <Bot class="w-6 h-6 text-primary" />
        <span class="font-semibold text-gray-800">AI 助手</span>
        <span class="text-xs text-green-500 bg-green-50 px-2 py-0.5 rounded-full font-medium">在线</span>
        <span v-if="isLoading" class="ml-auto flex items-center gap-1.5 text-xs text-primary">
          <Loader2 class="w-3.5 h-3.5 animate-spin" />思考中...
        </span>
      </header>

      <!-- 消息区域 -->
      <div ref="chatContainerRef" class="flex-1 overflow-y-auto p-6 space-y-6">
        <!-- 欢迎消息 -->
        <div v-if="messages.length === 0" class="flex flex-col items-center justify-center h-full text-center">
          <div class="w-20 h-20 rounded-2xl bg-gradient-to-br from-primary/10 to-blue-500/10 flex items-center justify-center mb-6">
            <Sparkles class="w-10 h-10 text-primary" />
          </div>
          <h3 class="text-xl font-semibold text-gray-800 mb-2">有什么可以帮你的？</h3>
          <p class="text-sm text-gray-500 mb-8 max-w-md">我是基于 Spring AI 的智能助手，可以帮你写代码、润色文章、总结内容、生成话题等</p>

          <div class="grid grid-cols-2 gap-3 max-w-lg w-full">
            <button
              v-for="suggestion in suggestions" :key="suggestion.text"
              class="p-4 rounded-xl border border-gray-200 text-left hover:border-primary/30 hover:bg-blue-50/30 transition-all group"
              @click="sendSuggestion(suggestion.text)"
            >
              <component :is="suggestion.icon" class="w-5 h-5 text-primary mb-2 group-hover:scale-110 transition-transform" />
              <p class="text-sm font-medium text-gray-700">{{ suggestion.label }}</p>
              <p class="text-xs text-gray-400 mt-0.5 line-clamp-2">{{ suggestion.desc }}</p>
            </button>
          </div>
        </div>

        <!-- 消息列表 -->
        <template v-else>
          <div
            v-for="msg in messages" :key="msg.id"
            :class="['flex gap-4', msg.role === 'user' ? 'justify-end' : '']"
          >
            <!-- AI消息 -->
            <template v-if="msg.role === 'assistant'">
              <div class="w-9 h-9 rounded-lg bg-gradient-to-br from-primary to-blue-600 flex items-center justify-center shrink-0 mt-1">
                <Bot class="w-5 h-5 text-white" />
              </div>
              <div class="max-w-[80%]">
                <div class="prose prose-sm max-w-none bg-gray-50 rounded-2xl rounded-tl-sm p-4 text-gray-800 leading-relaxed" v-html="renderMarkdown(msg.content)"></div>
                <div class="flex items-center gap-3 mt-2 ml-1">
                  <button class="text-xs text-gray-400 hover:text-primary transition-colors flex items-center gap-1" title="复制"><Copy class="w-3.5 h-3.5" /></button>
                  <button class="text-xs text-gray-400 hover:text-primary transition-colors flex items-center gap-1" title="重新生成"><RefreshCw class="w-3.5 h-3.5" /></button>
                  <span class="text-xs text-gray-300">{{ formatTime(msg.createTime) }}</span>
                </div>
              </div>
            </template>

            <!-- 用户消息 -->
            <template v-else>
              <div class="max-w-[70%]">
                <div class="bg-gradient-to-r from-primary to-blue-600 rounded-2xl rounded-tr-sm px-4 py-3 text-white leading-relaxed">{{ msg.content }}</div>
                <span class="text-xs text-gray-400 mt-1 block text-right pr-1">{{ formatTime(msg.createTime) }}</span>
              </div>
            </template>
          </div>

          <!-- AI正在输入指示器 -->
          <div v-if="isLoading" class="flex gap-4">
            <div class="w-9 h-9 rounded-lg bg-gradient-to-br from-primary to-blue-600 flex items-center justify-center shrink-0 mt-1">
              <Bot class="w-5 h-5 text-white" />
            </div>
            <div class="bg-gray-50 rounded-2xl rounded-tl-sm px-4 py-3">
              <div class="flex gap-1.5">
                <span class="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style="animation-delay: 0ms"></span>
                <span class="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style="animation-delay: 150ms"></span>
                <span class="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style="animation-delay: 300ms"></span>
              </div>
            </div>
          </div>
        </template>
      </div>

      <!-- 输入区域 -->
      <div class="border-t border-gray-200 p-4 shrink-0">
        <div class="max-w-4xl mx-auto">
          <div class="flex items-end gap-3 bg-gray-50 rounded-2xl border border-gray-200 focus-within:border-primary focus-within:ring-2 focus-within:ring-primary/20 transition-all">
            <textarea
              ref="inputRef"
              v-model="inputMessage"
              placeholder="输入你的问题... (Enter发送, Shift+Enter换行)"
              rows="1"
              class="flex-1 resize-none bg-transparent px-4 py-3 outline-none text-sm max-h-32 placeholder:text-gray-400"
              @keydown.enter.exact.prevent="send"
              @input="autoResize($event)"
            ></textarea>
            <div class="flex items-center gap-2 pb-2 pr-2">
              <button class="p-2 text-gray-400 hover:text-primary transition-colors" title="上传文件">
                <Paperclip class="w-4.5 h-4.5" />
              </button>
              <button
                :disabled="!inputMessage.trim() || isLoading"
                class="p-2.5 bg-gradient-to-r from-primary to-blue-600 text-white rounded-xl hover:shadow-lg hover:shadow-primary/25 disabled:opacity-50 disabled:cursor-not-allowed transition-all"
                @click="send"
              >
                <Send class="w-4.5 h-4.5" />
              </button>
            </div>
          </div>
          <p class="text-xs text-center text-gray-400 mt-2">AI 助手可能产生不准确的信息，请核实重要内容</p>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick } from 'vue'
import {
  Bot, Plus, Trash2, Sparkles, Loader2, Copy, RefreshCw,
  Send, Paperclip, Code, PenTool, FileText, Wand2, BookOpen, Hash, Search
} from 'lucide-vue-next'
import { aiApi } from '@/api'

interface ChatMsg { id: number; role: 'user' | 'assistant'; content: string; createTime: number }
interface ChatItem { id: number; title: string; messages: ChatMsg[]; sessionId?: string }

const inputRef = ref<HTMLTextAreaElement>()
const chatContainerRef = ref<HTMLElement>()
const inputMessage = ref('')
const isLoading = ref(false)
const activeChatId = ref<number | null>(null)
const currentSessionId = ref<string | null>(null)

const messages = ref<ChatMsg[]>([])

const chatHistory = ref<ChatItem[]>([])

const aiFeatures = [
  { name: '写作助手', icon: PenTool, prompt: '帮我写一篇关于技术分享的文章大纲' },
  { name: '代码审查', icon: Code, prompt: '帮我检查以下代码的问题：\n\n' },
  { name: '内容总结', icon: FileText, prompt: '请总结以下文章的核心观点：\n\n' },
  { name: '文章润色', icon: Wand2, prompt: '请润色以下文字，使其更加专业流畅：\n\n' },
  { name: '话题推荐', icon: Hash, prompt: '为我的社区推荐一些热门技术话题' },
  { name: '智能搜索', icon: Search, prompt: '搜索关于微服务架构的最佳实践' },
]

const suggestions = [
  { label: '写代码', desc: '帮你编写或调试代码', icon: Code, text: '帮我写一个Vue组件' },
  { label: '润色文章', desc: '优化你的文章表达', icon: PenTool, text: '请润色以下文字' },
  { label: '总结内容', desc: '提炼核心要点', icon: FileText, text: '请总结以下内容' },
  { label: '推荐话题', desc: '获取热门话题灵感', icon: BookOpen, text: '推荐一些热门话题' },
]

// 加载会话历史
async function loadChatHistory() {
  try {
    const res = await aiApi.chats()
    chatHistory.value = res.map((item: any, index: number) => ({
      id: index + 1,
      title: item.session_id || `会话${index + 1}`,
      messages: [],
      sessionId: item.session_id
    }))
  } catch (e) {
    console.error('加载会话历史失败', e)
  }
}

function newChat() {
  messages.value = []
  activeChatId.value = null
  currentSessionId.value = null
  setTimeout(() => (inputRef.value as HTMLTextAreaElement)?.focus(), 100)
}

async function loadChat(chat: ChatItem) {
  activeChatId.value = chat.id
  currentSessionId.value = chat.sessionId || null
  
  if (chat.sessionId) {
    try {
      const res = await aiApi.messages(chat.sessionId)
      messages.value = res.map((item: any, index: number) => ({
        id: index + 1,
        role: item.role === 'user' ? 'user' : 'assistant',
        content: item.content,
        createTime: new Date(item.createTime).getTime()
      }))
    } catch (e) {
      console.error('加载聊天记录失败', e)
    }
  }
  scrollToBottom()
}

async function deleteChat(id: number) {
  const idx = chatHistory.value.findIndex(c => c.id === id)
  if (idx > -1) {
    const chat = chatHistory.value[idx]
    if (chat.sessionId) {
      try {
        await aiApi.deleteChat(parseInt(chat.sessionId))
      } catch (e) {
        console.error('删除会话失败', e)
      }
    }
    chatHistory.value.splice(idx, 1)
  }
  if (activeChatId.value === id) newChat()
}

async function send() {
  if (!inputMessage.value.trim() || isLoading.value) return

  const userMsg: ChatMsg = {
    id: Date.now(),
    role: 'user',
    content: inputMessage.value.trim(),
    createTime: Date.now(),
  }
  messages.value.push(userMsg)
  const userInput = inputMessage.value.trim()
  inputMessage.value = ''
  scrollToBottom()

  // 调用真实API
  isLoading.value = true
  try {
    const response = await aiApi.send({
      sessionId: currentSessionId.value ? parseInt(currentSessionId.value) : undefined,
      content: userInput,
      type: 'chat'
    })
    
    const aiResponse: ChatMsg = {
      id: Date.now() + 1,
      role: 'assistant',
      content: response.data || response,
      createTime: Date.now(),
    }
    messages.value.push(aiResponse)
    updateChatTitle(userInput)
  } catch (error: any) {
    const errorMsg: ChatMsg = {
      id: Date.now() + 1,
      role: 'assistant',
      content: '抱歉，AI服务暂时不可用，请稍后重试。',
      createTime: Date.now(),
    }
    messages.value.push(errorMsg)
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

function updateChatTitle(firstMsg: string) {
  if (activeChatId.value) return
  const title = firstMsg.length > 20 ? firstMsg.slice(0, 20) + '...' : firstMsg
  
  // 创建新会话
  aiApi.createChat(title).then((res) => {
    const newSessionId = res.data || res
    const newChatItem: ChatItem = {
      id: Date.now(),
      title,
      messages: [...messages.value],
      sessionId: newSessionId
    }
    chatHistory.value.unshift(newChatItem)
    activeChatId.value = newChatItem.id
    currentSessionId.value = newSessionId
  }).catch(() => {
    // 如果创建会话失败，仍然保存本地
    const newChatItem: ChatItem = {
      id: Date.now(),
      title,
      messages: [...messages.value],
    }
    chatHistory.value.unshift(newChatItem)
    activeChatId.value = newChatItem.id
  })
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
    .replace(/```(\w*)\n?([\s\S]*?)```/g, '<pre class="bg-gray-800 text-green-400 p-3 rounded-lg my-2 text-xs overflow-x-auto"><code>$2</code></pre>')
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/^### (.*$)/gm, '<h4 class="font-semibold mt-3 mb-1">$1</h4>')
    .replace(/^## (.*$)/gm, '<h3 class="font-bold mt-3 mb-1">$1</h3>')
    .replace(/^- (.*$)/gm, '<li class="ml-4">$1</li>')
    .replace(/\n/g, '<br/>')
}

// 页面加载时获取历史记录
loadChatHistory()
</script>
