<template>
  <UserPageShell title="编辑帖子" icon="fa fa-pencil" subtitle="更新标题、正文与配图" max-width-class="max-w-4xl">
    <div class="flex items-center gap-2 text-sm text-[#6b7280] mb-5">
      <router-link to="/home" class="hover:text-[#6366f1]">首页</router-link>
      <span>/</span>
      <router-link :to="`/post/${postId}`" class="hover:text-[#6366f1]">帖子</router-link>
      <span>/</span>
      <span class="text-[#1f2937] font-medium">编辑</span>
    </div>

    <div v-if="pageLoading" class="user-surface-card p-12 text-center text-[#6b7280]">加载中…</div>

    <div v-else class="user-surface-card overflow-hidden">
      <div class="flex flex-wrap items-center gap-2 px-4 py-3 border-b border-[#e5e7eb] bg-[#fafbff]">
        <span class="text-xs text-[#9ca3af] mr-2 hidden sm:inline">编辑</span>
        <button type="button" class="tb" @click="wrapSelection('**', '**')"><strong>B</strong></button>
        <button type="button" class="tb" @click="wrapSelection('*', '*')"><em>I</em></button>
      </div>

      <div class="p-6 md:p-8 space-y-6 bg-white">
        <div>
          <label class="block text-sm font-medium text-[#374151] mb-2">封面图片</label>
          <div class="flex flex-wrap items-start gap-4">
            <input ref="coverInputRef" type="file" accept="image/*" class="hidden" @change="onCoverFile" />
            <button
              type="button"
              :disabled="coverUploading"
              class="px-4 py-2 rounded-xl text-sm border border-[#e5e7eb] bg-white text-[#6366f1] hover:bg-[#f8faff]"
              @click="coverInputRef?.click()"
            >
              {{ coverUploading ? '上传中…' : coverUrl ? '更换封面' : '上传封面' }}
            </button>
            <button v-if="coverUrl" type="button" class="text-xs text-rose-600" @click="coverUrl = ''; coverFileId = ''">移除封面</button>
            <div v-if="coverUrl" class="h-28 w-44 rounded-lg overflow-hidden border border-[#e5e7eb]">
              <img :src="resolveFileUrl(coverUrl)" alt="" class="w-full h-full object-cover" referrerpolicy="no-referrer" />
            </div>
          </div>
        </div>

        <div>
          <label class="block text-sm font-medium text-[#374151] mb-2">本帖图库与视频</label>
          <div class="flex flex-wrap gap-2 mb-3">
            <input ref="galleryImageInputRef" type="file" accept="image/*" class="hidden" @change="onGalleryImage" />
            <input ref="galleryVideoInputRef" type="file" accept="video/*" class="hidden" @change="onGalleryVideo" />
            <button type="button" class="px-3 py-1.5 text-sm rounded-lg border border-[#e5e7eb] bg-[#f9fafb]" :disabled="galleryUploading" @click="galleryImageInputRef?.click()">+ 图片</button>
            <button type="button" class="px-3 py-1.5 text-sm rounded-lg border border-[#e5e7eb] bg-[#f9fafb]" :disabled="galleryUploading" @click="galleryVideoInputRef?.click()">+ 视频</button>
          </div>
          <div v-if="mediaGallery.length" class="flex flex-wrap gap-3">
            <div v-for="(g, i) in mediaGallery" :key="i + g.fileId" class="relative w-32 h-32 rounded-lg border border-[#e5e7eb] overflow-hidden group">
              <img v-if="g.mediaType === 1" :src="g.preview" alt="" class="w-full h-full object-cover" />
              <div v-else class="w-full h-full flex flex-col items-center justify-center bg-[#1f2937] text-xs text-white/90 p-1">
                <i class="fa fa-film text-2xl mb-1 text-primary"></i>
                <span class="line-clamp-2">{{ g.name || '视频' }}</span>
              </div>
              <button type="button" class="absolute top-1 right-1 w-6 h-6 rounded bg-black/60 text-white text-xs opacity-0 group-hover:opacity-100" @click="removeGallery(i)">×</button>
            </div>
          </div>
          <p class="text-xs text-[#9ca3af] mt-2">保存时将按当前列表更新图库行；不覆盖正文内粘贴的图。</p>
        </div>

        <div>
          <label class="text-sm text-[#374151]">标题</label>
          <el-input v-model="form.title" maxlength="100" show-word-limit size="large" class="glass-input mt-1" />
        </div>

        <div>
          <label class="text-sm text-[#374151]">正文</label>
          <div class="mt-2 mb-2 flex flex-wrap items-center gap-2">
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
            <span v-if="aiBusy" class="text-xs text-[#6b7280]">AI处理中...</span>
          </div>
          <textarea
            ref="contentRef"
            v-model="form.content"
            @input="onContentInput"
            rows="16"
            class="w-full mt-1 px-4 py-3 rounded-xl border border-[#e5e7eb] bg-[#f9fafb] text-[#1f2937] text-sm focus:outline-none focus:ring-2 focus:ring-[#818cf8]/40"
          />
          <div v-if="aiSuggestion" class="mt-2 text-xs text-[#6b7280] flex items-center gap-2">
            <span class="px-1.5 py-0.5 rounded bg-[#eef2ff] text-[#6366f1]">AI补全</span>
            <button type="button" class="hover:underline text-left" @click="applyAiSuggestion">{{ aiSuggestion }}</button>
          </div>
        </div>

        <el-checkbox v-model="form.isAnonymous" class="glass-checkbox">匿名展示</el-checkbox>

        <div class="flex justify-end gap-3 pt-4 border-t border-[#e5e7eb]">
          <router-link :to="`/post/${postId}`" class="px-5 py-2.5 rounded-xl text-sm border border-[#e5e7eb] bg-white text-[#1f2937] hover:bg-[#f9fafb] inline-flex items-center">取消</router-link>
          <button type="button" :disabled="submitting" class="px-8 py-2.5 btn-gradient text-sm" @click="handleSave">保存修改</button>
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
import { ref, reactive, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { postApi, fileApi, aiApi } from '../../../api'
import { resolveFileUrl } from '../../../utils/resolveFileUrl'
import UserPageShell from '../../../components/user/UserPageShell.vue'

const route = useRoute()
const router = useRouter()

function parsePostIdParam(): number {
  const raw = route.params.id
  const s = raw == null ? '' : String(raw)
  if (s === '' || s === 'NaN' || !/^\d+$/.test(s)) {
    return 0
  }
  return Number(s)
}
const postId = parsePostIdParam()
const pageLoading = ref(true)
const submitting = ref(false)
const coverUploading = ref(false)
const galleryUploading = ref(false)
const coverInputRef = ref<HTMLInputElement | null>(null)
const galleryImageInputRef = ref<HTMLInputElement | null>(null)
const galleryVideoInputRef = ref<HTMLInputElement | null>(null)
const contentRef = ref<HTMLTextAreaElement | null>(null)
const coverUrl = ref('')
const coverFileId = ref('')
const mediaGallery = ref<{ fileId: string; mediaType: 1 | 2; preview?: string; name?: string }[]>([])
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

const form = reactive({ title: '', content: '', isAnonymous: false })

function uploadResultToUrl(res: unknown): string | null {
  if (!res || typeof res !== 'object') return null
  const o = res as { id?: string; url?: string }
  if (o.id != null && String(o.id).trim() !== '') return `/api/file/download/${String(o.id).trim()}`
  if (o.url && String(o.url).toLowerCase().startsWith('http')) return o.url
  return null
}

async function onCoverFile(e: Event) {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  input.value = ''
  if (!file || !file.type.startsWith('image/')) return
  coverUploading.value = true
  try {
    const res = await fileApi.uploadImage(file, undefined, 'community/images/post')
    const url = uploadResultToUrl(res)
    if (url) {
      coverUrl.value = url
      const o = res as { id?: string }
      coverFileId.value = o?.id != null ? String(o.id).trim() : ''
    }
  } finally {
    coverUploading.value = false
  }
}

async function onGalleryImage(e: Event) {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  input.value = ''
  if (!file || !file.type.startsWith('image/')) return
  galleryUploading.value = true
  try {
    const res = await fileApi.uploadImage(file, undefined, 'community/images/post')
    const id = (res as { id?: string })?.id
    if (id == null) return
    const sid = String(id).trim()
    const url = uploadResultToUrl(res) || `/api/file/download/${sid}`
    mediaGallery.value.push({ fileId: sid, mediaType: 1, preview: resolveFileUrl(url) || url })
  } finally {
    galleryUploading.value = false
  }
}

async function onGalleryVideo(e: Event) {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  input.value = ''
  if (!file || !file.type.startsWith('video/')) return
  galleryUploading.value = true
  try {
    const formData = new FormData()
    formData.append('file', file)
    const res = await fileApi.upload(formData, undefined, 'community/videos/post')
    const id = (res as { id?: string })?.id
    if (id == null) return
    mediaGallery.value.push({ fileId: String(id).trim(), mediaType: 2, name: file.name })
  } finally {
    galleryUploading.value = false
  }
}

function removeGallery(i: number) {
  mediaGallery.value.splice(i, 1)
}

function wrapSelection(before: string, after: string) {
  const el = contentRef.value
  if (!el) return
  const s = el.selectionStart
  const e = el.selectionEnd
  const v = form.content
  form.content = v.slice(0, s) + before + v.slice(s, e) + after + v.slice(e)
  nextTick(() => {
    el.focus()
    el.setSelectionRange(s + before.length, e + before.length)
  })
}

function boardNameText(): string {
  return '社区交流'
}

async function handleAiGenerate() {
  const keyword = (form.title || form.content || '').trim()
  if (!keyword) {
    ElMessage.warning('请先输入标题或关键词')
    return
  }
  aiBusy.value = true
  try {
    const generated = await aiApi.generatePost({
      boardName: boardNameText(),
      keyword,
      lengthRange: aiConfig.lengthRange,
      style: aiConfig.style,
    })
    form.content = String(generated || '').trim()
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
    const appended = await aiApi.continuePost({
      boardName: boardNameText(),
      content,
      style: aiConfig.style,
    })
    const tail = String(appended || '').trim()
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
    const polished = await aiApi.polishPost({
      boardName: boardNameText(),
      content,
      polishMode: aiConfig.polishMode,
      style: aiConfig.style,
    })
    form.content = String(polished || '').trim() || form.content
    aiLastOperation.value = 'polish'
    pushAiHistory(form.content)
    ElMessage.success('AI润色成功')
  } finally {
    aiBusy.value = false
  }
}

function onContentInput() {
  if (completeTimer) {
    window.clearTimeout(completeTimer)
  }
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
  aiLastOperation.value = 'complete-apply'
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
      scene: 'edit',
      operation: aiLastOperation.value,
    })
    ElMessage.success(type === 'like' ? '感谢反馈' : '已记录，将持续优化')
  } catch {
    ElMessage.warning('反馈提交失败，请稍后重试')
  }
}

async function handleSave() {
  const title = (form.title || '').trim()
  const content = (form.content || '').trim()
  if (title.length < 5) {
    ElMessage.warning('标题需至少 5 字')
    return
  }
  if (content.length < 10) {
    ElMessage.warning('正文至少 10 字')
    return
  }
  const media = mediaGallery.value.map((g, i) => ({
    fileId: String(g.fileId).trim(),
    mediaType: g.mediaType,
    sortOrder: i,
  }))
  submitting.value = true
  try {
    await postApi.update(postId, {
      title,
      content,
      coverImage: coverUrl.value || undefined,
      coverFileId: coverFileId.value ? String(coverFileId.value).trim() : undefined,
      isAnonymous: form.isAnonymous,
      media,
    } as any)
    ElMessage.success('已保存')
    router.push(`/post/${postId}`)
  } catch {
    /* */
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  if (postId <= 0) {
    ElMessage.error('无效的帖子链接')
    router.push('/')
    return
  }
  try {
    const res: any = await postApi.detail(postId)
    form.title = res.title
    form.content = res.content
    form.isAnonymous = !!res.isAnonymous
    if (res.coverImage) coverUrl.value = res.coverImage
    if (res.coverFileId != null) coverFileId.value = String(res.coverFileId)
    if (res.media && Array.isArray(res.media)) {
      for (const m of res.media) {
        const fid = m.fileId != null ? String(m.fileId) : ''
        if (!fid) continue
        if (m.mediaType === 2) {
          mediaGallery.value.push({ fileId: fid, mediaType: 2, name: '视频' })
        } else {
          const p = m.downloadPath || `/api/file/download/${fid}`
          mediaGallery.value.push({ fileId: fid, mediaType: 1, preview: resolveFileUrl(p) || p })
        }
      }
    }
  } catch {
    ElMessage.error('加载失败')
    router.push('/')
  } finally {
    pageLoading.value = false
    pushAiHistory(form.content)
  }
})
</script>

<style scoped>
.tb {
  padding: 0.25rem 0.625rem;
  border-radius: 0.375rem;
  font-size: 0.875rem;
  color: #374151;
  background: #f9fafb;
  border: 1px solid #e5e7eb;
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
.glass-input :deep(.el-input__wrapper) {
  background-color: #f9fafb !important;
  border: 1px solid #e5e7eb !important;
  box-shadow: none !important;
}
.glass-checkbox :deep(.el-checkbox__label) {
  color: #374151 !important;
}
</style>
