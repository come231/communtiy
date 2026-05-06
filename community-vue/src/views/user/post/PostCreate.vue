<template>
  <UserPageShell title="发布帖子" icon="fa fa-edit" subtitle="分享你的观点与创作" max-width-class="max-w-4xl">
    <div class="flex items-center gap-2 text-sm text-[#6b7280] mb-5">
      <router-link to="/home" class="hover:text-[#6366f1] transition-colors">首页</router-link>
      <span>/</span>
      <span class="text-[#1f2937] font-medium">发布帖子</span>
    </div>

    <div class="user-surface-card overflow-hidden">
        <!-- 顶部工具栏（轻量 Markdown 辅助，正文内图片以粘贴为主） -->
        <div class="flex flex-wrap items-center gap-2 px-4 py-3 border-b border-[#e5e7eb] bg-[#fafbff]">
          <span class="text-xs text-[#9ca3af] mr-2 hidden sm:inline">编辑</span>
              <button type="button" class="tb" title="加粗" @click="wrapSelection('**', '**')"><strong>B</strong></button>
              <button type="button" class="tb" title="斜体" @click="wrapSelection('*', '*')"><em>I</em></button>
              <button type="button" class="tb font-mono text-xs" title="行内代码" @click="wrapSelection('`', '`')">&lt;/&gt;</button>
              <button type="button" class="tb font-mono text-xs" title="代码块（黑框展示）" @click="insertCodeFence">代码块</button>
          <span class="text-[#d1d5db] mx-1">|</span>
          <span class="text-xs text-[#6b7280]">正文中可直接 <kbd class="px-1 rounded bg-[#e5e7eb] text-[#1f2937]">Ctrl+V</kbd> 粘贴截图，将自动上传并插入</span>
        </div>

        <div class="p-6 md:p-8 space-y-6 bg-white">
          <!-- 封面（单独上传；与正文内粘贴图片区分） -->
          <div>
            <label class="block text-sm font-medium text-[#374151] mb-2">封面图片</label>
            <div class="flex flex-wrap items-start gap-4">
              <input ref="coverInputRef" type="file" accept="image/*" class="hidden" @change="onCoverFile" />
              <button
                type="button"
                :disabled="coverUploading"
                class="px-4 py-2 rounded-xl text-sm border border-[#e5e7eb] bg-white text-[#6366f1] hover:bg-[#f8faff] transition-colors disabled:opacity-50"
                @click="coverInputRef?.click()"
              >
                {{ coverUploading ? '上传中…' : coverUrl ? '更换封面' : '上传封面' }}
              </button>
              <button v-if="coverUrl" type="button" class="text-xs text-rose-600 hover:underline" @click="coverUrl = ''; coverFileId = ''">移除封面</button>
              <div v-if="coverUrl" class="h-28 w-44 rounded-lg overflow-hidden border border-[#e5e7eb] shrink-0">
                <img :src="resolveFileUrl(coverUrl)" alt="封面预览" referrerpolicy="no-referrer" class="w-full h-full object-cover" />
              </div>
            </div>
            <p class="text-xs text-[#9ca3af] mt-2">用于列表与详情页顶部展示；正文配图请直接在下方编辑区粘贴。</p>
          </div>

          <!-- 本帖图库与视频（UUID 新文件，整帖与封面合计不超过 100MB；编辑时旧图旧视频不删，仅改本列表会替换图库行） -->
          <div>
            <label class="block text-sm font-medium text-[#374151] mb-2">本帖图库与视频</label>
            <p class="text-xs text-[#9ca3af] mb-3">可添加多图、多段视频；与封面、粘贴图合计体积勿超过 100MB。</p>
            <div class="flex flex-wrap gap-2 mb-3">
              <input ref="galleryImageInputRef" type="file" accept="image/*" class="hidden" @change="onGalleryImage" />
              <input ref="galleryVideoInputRef" type="file" accept="video/*" class="hidden" @change="onGalleryVideo" />
              <button type="button" class="px-3 py-1.5 text-sm rounded-lg border border-[#e5e7eb] bg-[#f9fafb] text-[#1f2937] hover:bg-[#f0f4ff]" :disabled="galleryUploading" @click="galleryImageInputRef?.click()">+ 图片</button>
              <button type="button" class="px-3 py-1.5 text-sm rounded-lg border border-[#e5e7eb] bg-[#f9fafb] text-[#1f2937] hover:bg-[#f0f4ff]" :disabled="galleryUploading" @click="galleryVideoInputRef?.click()">+ 视频</button>
            </div>
            <div v-if="mediaGallery.length" class="flex flex-wrap gap-3">
              <div v-for="(g, i) in mediaGallery" :key="i + g.fileId" class="relative w-32 h-32 rounded-lg border border-[#e5e7eb] overflow-hidden group">
                <img v-if="g.mediaType === 1" :src="g.preview" alt="" class="w-full h-full object-cover" />
                <div v-else class="w-full h-full flex flex-col items-center justify-center bg-[#1f2937] text-xs text-white/90 p-1">
                  <i class="fa fa-film text-2xl mb-1 text-primary"></i>
                  <span class="line-clamp-2">{{ g.name || '视频' }}</span>
                </div>
                <button
                  type="button"
                  class="absolute top-1 right-1 w-6 h-6 rounded bg-black/60 text-white text-xs opacity-0 group-hover:opacity-100"
                  @click="removeGallery(i)"
                >×</button>
              </div>
            </div>
          </div>

          <!-- 标题 -->
          <div>
            <div class="flex items-center justify-between mb-2">
              <label class="text-sm font-medium text-[#374151]">文章标题</label>
              <span class="text-xs" :class="titleHintClass">{{ titleHintText }}</span>
            </div>
            <el-input
              v-model="form.title"
              placeholder="请输入文章标题（5～100 字）"
              maxlength="100"
              show-word-limit
              size="large"
              class="glass-input"
            />
          </div>

          <!-- 话题 -->
          <div>
            <label class="block text-sm font-medium text-[#374151] mb-2">话题标签</label>
            <div class="flex flex-wrap gap-2">
              <button
                v-for="t in hotTopics"
                :key="t.topicId"
                type="button"
                @click="toggleTopic(t.topicId)"
                :class="[
                  'px-3 py-1.5 rounded-full text-sm transition-all duration-200 cursor-pointer',
                  form.topicIds.includes(t.topicId)
                    ? 'btn-gradient shadow-md shadow-primary/25'
                    : 'border border-[#e5e7eb] bg-[#f9fafb] text-[#6b7280] hover:border-[#c7d2fe] hover:text-[#6366f1]',
                ]"
              >
                #{{ t.name }}
              </button>
            </div>
          </div>

          <!-- 正文 -->
          <div>
            <div class="flex items-center justify-between mb-2 gap-2 flex-wrap">
              <label class="block text-sm font-medium text-[#374151]">正文</label>
              <div class="flex flex-wrap items-center gap-2">
                <button type="button" class="tb-ai" :disabled="aiBusy" @click="handleAiGenerate">AI一键生成</button>
                <button type="button" class="tb-ai" :disabled="aiBusy || !(form.content || '').trim()" @click="handleAiContinue">AI续写</button>
                <button type="button" class="tb-ai" :disabled="aiBusy || !(form.content || '').trim()" @click="handleAiPolish">AI润色</button>
                <button type="button" class="tb-ai" @click="showAiSettings = true">AI参数</button>
                <button type="button" class="tb-ai" :disabled="aiBusy || !(form.content || '').trim()" @click="retryAiStyle('简洁')">更简洁</button>
                <button type="button" class="tb-ai" :disabled="aiBusy || !(form.content || '').trim()" @click="retryAiStyle('活泼')">更活泼</button>
                <button type="button" class="tb-ai" :disabled="aiBusy || !(form.content || '').trim()" @click="retryAiStyle('正式')">更正式</button>
                <button type="button" class="tb-ai" :disabled="aiHistory.length <= 1 || aiHistoryIndex <= 0" @click="rollbackAiVersion">回退版本</button>
                <button type="button" class="tb-ai" :disabled="aiHistory.length <= 1 || aiHistoryIndex >= aiHistory.length - 1" @click="forwardAiVersion">前进版本</button>
                <button type="button" class="tb-ai" :disabled="aiBusy" @click="submitAiFeedback('like')">满意</button>
                <button type="button" class="tb-ai" :disabled="aiBusy" @click="submitAiFeedback('dislike')">不满意</button>
              </div>
            </div>
            <div class="editor-shell rounded-xl border border-[#e5e7eb] overflow-hidden bg-[#f9fafb]">
              <textarea
                ref="contentRef"
                v-model="form.content"
                placeholder="在此编写正文，支持 Markdown。代码请用 ``` 包裹，将以深色方框展示。图片可直接粘贴插入。"
                rows="18"
                class="content-textarea w-full px-4 py-4 resize-y min-h-[320px] focus:outline-none focus:ring-2 focus:ring-[#818cf8]/40 text-sm leading-relaxed bg-white text-[#1f2937] placeholder:text-[#9ca3af]"
                @paste="onPaste"
                @input="onContentInput"
              />
            </div>
            <div class="flex items-center justify-between mt-2 text-xs text-[#9ca3af]">
              <span>支持 **加粗** *斜体* `代码` [链接](url) ![图](url) 与 ```代码块```</span>
              <span>共 {{ wordCount }} 字</span>
            </div>
            <div v-if="aiSuggestion" class="mt-2 text-xs text-[#6b7280] flex items-center gap-2">
              <span class="px-1.5 py-0.5 rounded bg-[#eef2ff] text-[#6366f1]">AI补全</span>
              <button type="button" class="hover:underline text-left" @click="applyAiSuggestion">{{ aiSuggestion }}</button>
            </div>
          </div>

          <div class="flex flex-wrap gap-6">
            <el-checkbox v-model="form.isAnonymous" class="glass-checkbox">匿名发布（发布后将隐藏你的身份信息）</el-checkbox>
            <el-checkbox v-model="form.isPublic" class="glass-checkbox">
              <span v-if="form.isPublic">公开帖子（所有人可见）</span>
              <span v-else class="text-amber-700 font-medium">私密帖子（仅自己可见）</span>
            </el-checkbox>
          </div>

          <!-- 底部操作 -->
          <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 pt-4 border-t border-[#e5e7eb]">
            <div class="flex flex-wrap items-center gap-3 text-xs text-[#9ca3af]">
              <span>发文设置：匿名见上</span>
              <span v-if="pasteUploading" class="text-primary">正在上传粘贴的图片…</span>
              <span v-if="galleryUploading" class="text-primary">图库上传中…</span>
            </div>
            <div class="flex flex-wrap items-center gap-3 justify-end">
              <button
                type="button"
                :disabled="draftSaving"
                class="px-5 py-2.5 rounded-xl text-sm border border-[#e5e7eb] bg-white text-[#1f2937] hover:bg-[#f9fafb] transition-colors disabled:opacity-50"
                @click="saveDraft"
              >
                <i class="fa fa-save mr-1"></i> {{ draftSaving ? '保存中…' : '存草稿' }}
              </button>
              <router-link to="/home" class="px-5 py-2.5 rounded-xl text-sm border border-[#e5e7eb] bg-white text-[#1f2937] hover:bg-[#f9fafb] inline-flex items-center">取消</router-link>
              <button
                type="button"
                :disabled="submitting"
                class="px-8 py-2.5 btn-gradient shadow-lg shadow-primary/30 hover:shadow-primary/40 transition-all text-sm font-medium disabled:opacity-60"
                @click="handleSubmit"
              >
                <i class="fa fa-paper-plane mr-1"></i> {{ submitting ? '发布中…' : '发布帖子' }}
              </button>
            </div>
          </div>
        </div>
    </div>
    <el-dialog v-model="showAiSettings" title="AI参数设置" width="420px">
      <div class="space-y-4">
        <div>
          <label class="block text-sm text-[#374151] mb-1">生成风格</label>
          <el-select v-model="aiConfig.style" class="w-full">
            <el-option label="亲切" value="亲切" />
            <el-option label="活泼" value="活泼" />
            <el-option label="简洁" value="简洁" />
            <el-option label="正式" value="正式" />
          </el-select>
        </div>
        <div>
          <label class="block text-sm text-[#374151] mb-1">字数范围</label>
          <el-select v-model="aiConfig.lengthRange" class="w-full">
            <el-option label="50-100字" value="50-100字" />
            <el-option label="100-200字" value="100-200字" />
            <el-option label="200字以上" value="200字以上" />
          </el-select>
        </div>
        <div>
          <label class="block text-sm text-[#374151] mb-1">润色方向</label>
          <el-select v-model="aiConfig.polishMode" class="w-full">
            <el-option label="一键润色" value="一键润色" />
            <el-option label="语气调整" value="语气调整" />
            <el-option label="段落优化" value="段落优化" />
            <el-option label="合规优化" value="合规优化" />
            <el-option label="亮点优化" value="亮点优化" />
          </el-select>
        </div>
      </div>
      <template #footer>
        <el-button @click="showAiSettings = false">关闭</el-button>
      </template>
    </el-dialog>
  </UserPageShell>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { postApi, topicApi, fileApi, aiApi } from '../../../api'
import { resolveFileUrl } from '../../../utils/resolveFileUrl'
import UserPageShell from '../../../components/user/UserPageShell.vue'

const router = useRouter()
const route = useRoute()
const editingDraftId = ref<number | null>(null)
const submitting = ref(false)
const draftSaving = ref(false)
const coverUploading = ref(false)
const pasteUploading = ref(false)
const coverInputRef = ref<HTMLInputElement | null>(null)
const galleryImageInputRef = ref<HTMLInputElement | null>(null)
const galleryVideoInputRef = ref<HTMLInputElement | null>(null)
const contentRef = ref<HTMLTextAreaElement | null>(null)
const coverUrl = ref('')
const coverFileId = ref('')
const galleryUploading = ref(false)
/** 与后端 media 列表对应：1=图 2=视频 */
const mediaGallery = ref<{ fileId: string; mediaType: 1 | 2; preview?: string; name?: string }[]>([])
const hotTopics = ref<{ topicId: number; name: string }[]>([])
const aiBusy = ref(false)
const aiSuggestion = ref('')
const showAiSettings = ref(false)
const aiConfig = reactive({
  style: '亲切',
  lengthRange: '100-200字',
  polishMode: '一键润色',
})
const aiLastOperation = ref('generate')
const aiHistory = ref<string[]>([])
const aiHistoryIndex = ref(-1)
let completeTimer: number | null = null

const form = reactive({
  title: '',
  content: '',
  isAnonymous: false,
  isPublic: true,
  topicIds: [] as number[],
})

const wordCount = computed(() => {
  const s = form.content || ''
  return s.replace(/\s/g, '').length || s.length
})

const titleHintText = computed(() => {
  const n = (form.title || '').length
  if (n === 0) return '标题 5～100 字'
  if (n < 5) return `还需 ${5 - n} 个字符`
  return `${n} / 100`
})

const titleHintClass = computed(() => {
  const n = (form.title || '').length
  if (n > 0 && n < 5) return 'text-amber-600'
  return 'text-[#9ca3af]'
})

function toggleTopic(id: number) {
  const idx = form.topicIds.indexOf(id)
  if (idx >= 0) form.topicIds.splice(idx, 1)
  else if (form.topicIds.length < 5) form.topicIds.push(id)
}

function insertAtCursor(text: string) {
  const el = contentRef.value
  if (!el) {
    form.content += text
    return
  }
  const start = el.selectionStart
  const end = el.selectionEnd
  const v = form.content
  form.content = v.slice(0, start) + text + v.slice(end)
  nextTick(() => {
    el.focus()
    const pos = start + text.length
    el.setSelectionRange(pos, pos)
  })
}

function wrapSelection(before: string, after: string) {
  const el = contentRef.value
  if (!el) return
  const start = el.selectionStart
  const end = el.selectionEnd
  const v = form.content
  const selected = v.slice(start, end)
  const inner = selected || '文字'
  const insert = before + inner + after
  form.content = v.slice(0, start) + insert + v.slice(end)
  nextTick(() => {
    el.focus()
    const s = start + before.length
    const e2 = s + inner.length
    el.setSelectionRange(s, e2)
  })
}

function insertCodeFence() {
  insertAtCursor('\n```\n\n```\n')
}

async function onCoverFile(e: Event) {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  input.value = ''
  if (!file || !file.type.startsWith('image/')) return
  if (file.size > 8 * 1024 * 1024) {
    ElMessage.warning('封面图片不超过 8MB')
    return
  }
  coverUploading.value = true
  try {
    const res = await fileApi.uploadImage(file, undefined, 'community/images/post')
    const url = uploadResultToUrl(res)
    if (url) {
      coverUrl.value = url
      const o = res as { id?: string }
      coverFileId.value = o?.id != null ? String(o.id).trim() : ''
      ElMessage.success('封面上传成功')
    }
  } catch {
    ElMessage.error('封面上传失败')
  } finally {
    coverUploading.value = false
  }
}

function uploadResultToUrl(res: unknown): string | null {
  if (!res || typeof res !== 'object') return null
  const o = res as { url?: string; id?: string }
  if (o.id != null && String(o.id).trim() !== '') {
    return `/api/file/download/${String(o.id).trim()}`
  }
  if (o.url && String(o.url).trim().toLowerCase().startsWith('http')) {
    return o.url
  }
  return null
}

async function uploadImageFile(file: File): Promise<string | null> {
  if (file.size > 8 * 1024 * 1024) {
    ElMessage.warning('单张图片不超过 8MB')
    return null
  }
  const res = await fileApi.uploadImage(file, undefined, 'community/images/post')
  return uploadResultToUrl(res)
}

async function onPaste(e: ClipboardEvent) {
  const items = e.clipboardData?.items
  if (!items?.length) return
  for (let i = 0; i < items.length; i++) {
    const it = items[i]
    if (it.kind === 'file' && it.type.startsWith('image/')) {
      e.preventDefault()
      const file = it.getAsFile()
      if (!file) return
      pasteUploading.value = true
      try {
        const url = await uploadImageFile(file)
        if (url) {
          insertAtCursor(`![图片](${url})`)
          ElMessage.success('图片已插入正文')
        }
      } catch {
        ElMessage.error('图片上传失败')
      } finally {
        pasteUploading.value = false
      }
      return
    }
  }
}

async function onGalleryImage(e: Event) {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  input.value = ''
  if (!file || !file.type.startsWith('image/')) return
  if (file.size > 50 * 1024 * 1024) {
    ElMessage.warning('单张图片不超过 50MB')
    return
  }
  galleryUploading.value = true
  try {
    const res = await fileApi.uploadImage(file, undefined, 'community/images/post')
    const id = (res as { id?: string })?.id
    if (id == null || String(id).trim() === '') {
      ElMessage.error('上传失败')
      return
    }
    const sid = String(id).trim()
    const url = uploadResultToUrl(res) || `/api/file/download/${sid}`
    mediaGallery.value.push({ fileId: sid, mediaType: 1, preview: resolveFileUrl(url) || url })
  } catch {
    ElMessage.error('上传失败')
  } finally {
    galleryUploading.value = false
  }
}

async function onGalleryVideo(e: Event) {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  input.value = ''
  if (!file || !file.type.startsWith('video/')) return
  if (file.size > 100 * 1024 * 1024) {
    ElMessage.warning('单段视频不超过 100MB')
    return
  }
  galleryUploading.value = true
  try {
    const formData = new FormData()
    formData.append('file', file)
    const res = await fileApi.upload(formData, undefined, 'community/videos/post')
    const id = (res as { id?: string })?.id
    if (id == null || String(id).trim() === '') {
      ElMessage.error('上传失败')
      return
    }
    mediaGallery.value.push({ fileId: String(id).trim(), mediaType: 2, name: file.name })
  } catch {
    ElMessage.error('视频上传失败')
  } finally {
    galleryUploading.value = false
  }
}

function removeGallery(i: number) {
  mediaGallery.value.splice(i, 1)
}

function boardNameText(): string {
  return '社区交流'
}

async function handleAiGenerate() {
  const keyword = (form.title || form.content || '').trim()
  if (!keyword) {
    ElMessage.warning('请先输入标题或主题关键词')
    return
  }
  aiBusy.value = true
  try {
    const text = await aiApi.generatePost({
      boardName: boardNameText(),
      keyword,
      lengthRange: aiConfig.lengthRange,
      style: aiConfig.style,
    })
    form.content = String(text || '').trim()
    aiLastOperation.value = 'generate'
    pushAiHistory(form.content)
    ElMessage.success('AI生成完成')
  } finally {
    aiBusy.value = false
  }
}

async function handleAiContinue() {
  const content = (form.content || '').trim()
  if (!content) return
  aiBusy.value = true
  try {
    const text = await aiApi.continuePost({
      boardName: boardNameText(),
      content,
      style: aiConfig.style,
    })
    const tail = String(text || '').trim()
    if (tail) {
      form.content = `${form.content}${form.content.endsWith('\n') ? '' : '\n'}${tail}`
      aiLastOperation.value = 'continue'
      pushAiHistory(form.content)
    }
    ElMessage.success('AI续写完成')
  } finally {
    aiBusy.value = false
  }
}

async function handleAiPolish() {
  const content = (form.content || '').trim()
  if (!content) return
  aiBusy.value = true
  try {
    const text = await aiApi.polishPost({
      boardName: boardNameText(),
      content,
      polishMode: aiConfig.polishMode,
      style: aiConfig.style,
    })
    form.content = String(text || '').trim() || form.content
    aiLastOperation.value = 'polish'
    pushAiHistory(form.content)
    ElMessage.success('AI润色成功')
  } finally {
    aiBusy.value = false
  }
}

function onContentInput() {
  if (completeTimer) window.clearTimeout(completeTimer)
  completeTimer = window.setTimeout(async () => {
    const prefix = (form.content || '').trim()
    if (!prefix || prefix.length < 6 || aiBusy.value) {
      aiSuggestion.value = ''
      return
    }
    try {
      const text = await aiApi.completePost({
        boardName: boardNameText(),
        prefix: prefix.slice(-60),
      })
      aiSuggestion.value = String(text || '').trim().slice(0, 40)
      aiLastOperation.value = 'complete'
    } catch {
      aiSuggestion.value = ''
    }
  }, 500)
}

function applyAiSuggestion() {
  if (!aiSuggestion.value) return
  form.content = `${form.content}${aiSuggestion.value}`
  pushAiHistory(form.content)
  aiSuggestion.value = ''
}

async function retryAiStyle(style: '简洁' | '活泼' | '正式') {
  const content = (form.content || '').trim()
  if (!content) return
  aiBusy.value = true
  try {
    const text = await aiApi.polishPost({
      boardName: boardNameText(),
      content,
      polishMode: '语气调整',
      style,
    })
    const next = String(text || '').trim()
    if (next) {
      form.content = next
      aiLastOperation.value = 'style-retry'
      pushAiHistory(form.content)
    }
    ElMessage.success(`已调整为${style}风格`)
  } finally {
    aiBusy.value = false
  }
}

function pushAiHistory(content: string) {
  const text = String(content || '')
  if (!text.trim()) return
  if (aiHistoryIndex.value >= 0 && aiHistory.value[aiHistoryIndex.value] === text) return
  const next = aiHistory.value.slice(0, aiHistoryIndex.value + 1)
  next.push(text)
  if (next.length > 6) next.shift()
  aiHistory.value = next
  aiHistoryIndex.value = aiHistory.value.length - 1
}

function rollbackAiVersion() {
  if (aiHistoryIndex.value <= 0) return
  aiHistoryIndex.value -= 1
  form.content = aiHistory.value[aiHistoryIndex.value] || form.content
}

function forwardAiVersion() {
  if (aiHistoryIndex.value >= aiHistory.value.length - 1) return
  aiHistoryIndex.value += 1
  form.content = aiHistory.value[aiHistoryIndex.value] || form.content
}

async function submitAiFeedback(type: 'like' | 'dislike') {
  let reason = ''
  if (type === 'dislike') {
    reason = window.prompt('请输入不满意原因（如：太啰嗦/不够亲切/信息不全）', '') || ''
  }
  try {
    await aiApi.feedback({
      feedbackType: type,
      feedbackReason: reason || undefined,
      scene: 'create',
      operation: aiLastOperation.value,
    })
    ElMessage.success(type === 'like' ? '感谢反馈' : '已记录，将持续优化')
  } catch {
    ElMessage.warning('反馈提交失败，请稍后重试')
  }
}

async function handleSubmit() {
  const title = (form.title || '').trim()
  const content = (form.content || '').trim()
  if (title.length < 5 || title.length > 100) {
    ElMessage.warning('标题长度需为 5～100 字')
    return
  }
  if (content.length < 10) {
    ElMessage.warning('正文至少 10 个字符')
    return
  }
  const media = mediaGallery.value.map((g, i) => ({
    fileId: String(g.fileId).trim(),
    mediaType: g.mediaType,
    sortOrder: i,
  }))
  submitting.value = true
  try {
    const res = await postApi.create({
      title,
      content,
      coverImage: coverUrl.value || undefined,
      coverFileId: coverFileId.value ? String(coverFileId.value).trim() : undefined,
      media: media.length > 0 ? media : undefined,
      isAnonymous: form.isAnonymous,
      isPublic: form.isPublic,
      topicIds: form.topicIds.length > 0 ? form.topicIds : undefined,
    })
    const pid = res.postId
    if (res.status === 2) {
      ElMessage.warning(res.message || '内容已提交，正在等待人工审核通过后将对外展示')
    } else {
      ElMessage.success(res.message || '发布成功')
    }
    router.push(`/post/${pid}`)
  } catch {
    /* 拦截器 */
  } finally {
    submitting.value = false
  }
}

async function saveDraft() {
  draftSaving.value = true
  try {
    const payload: Parameters<typeof postApi.saveDraft>[0] = {
      title: form.title,
      content: form.content,
      coverImage: coverUrl.value || undefined,
      topicIds: form.topicIds.length > 0 ? form.topicIds : undefined,
      isAnonymous: form.isAnonymous,
    }
    if (editingDraftId.value != null) {
      payload.draftId = editingDraftId.value
    }
    await postApi.saveDraft(payload)
    ElMessage.success('草稿已保存')
  } catch {
    /* */
  } finally {
    draftSaving.value = false
  }
}

onMounted(async () => {
  try {
    const topRes: any = await topicApi.hot()
    hotTopics.value = (topRes || []).slice(0, 12)
  } catch {
    /* */
  }
  const dq = route.query.draftId
  if (dq != null && String(dq).trim() !== '') {
    const did = Number(dq)
    if (Number.isFinite(did) && did > 0) {
      editingDraftId.value = did
      try {
        const d: any = await postApi.draftDetail(did)
        if (d) {
          form.title = d.title || ''
          form.content = d.content || ''
          form.isAnonymous = !!d.isAnonymous
          if (d.topicIds) {
            const parts = String(d.topicIds)
              .split(',')
              .map((s: string) => Number(s.trim()))
              .filter((n: number) => Number.isFinite(n) && n > 0)
            form.topicIds = parts
          } else {
            form.topicIds = []
          }
          if (d.coverImage) {
            coverUrl.value = d.coverImage
          }
        }
      } catch {
        ElMessage.error('加载草稿失败')
      }
    }
  }
  pushAiHistory(form.content)
})
</script>

<style scoped>
.container {
  max-width: 56rem;
  margin: 0 auto;
}

.tb {
  padding: 0.25rem 0.625rem;
  border-radius: 0.375rem;
  font-size: 0.875rem;
  color: #374151;
  background: #f9fafb;
  border: 1px solid #e5e7eb;
  transition: background-color 0.15s ease, border-color 0.15s ease;
}
.tb:hover {
  background: #f0f4ff;
  border-color: #c7d2fe;
}
.tb-ai {
  padding: 0.25rem 0.625rem;
  border-radius: 9999px;
  font-size: 0.75rem;
  color: #4f46e5;
  background: #eef2ff;
  border: 1px solid #c7d2fe;
}
.tb-ai:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.editor-shell {
  background: #f9fafb;
}

.content-textarea::placeholder {
  color: #9ca3af;
}

.glass-input {
  background-color: #f9fafb !important;
  border: 1px solid #e5e7eb !important;
  color: #1f2937 !important;
}

.glass-input :deep(.el-input__wrapper) {
  background-color: #f9fafb !important;
  border: 1px solid #e5e7eb !important;
  box-shadow: none !important;
}

.glass-checkbox :deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
  background-color: var(--primary) !important;
  border-color: var(--primary) !important;
}

.glass-checkbox :deep(.el-checkbox__label) {
  color: #374151 !important;
}

kbd {
  font-family: ui-monospace, monospace;
}
</style>
