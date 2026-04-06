<template>
  <div class="min-h-screen bg-[#F5F7FA] pt-20 pb-8">
    <div class="max-w-4xl mx-auto px-4">
      <!-- 面包屑 -->
      <div class="flex items-center gap-2 text-sm text-gray-500 mb-6">
        <router-link to="/" class="hover:text-blue-500 transition-colors">首页</router-link>
        <span>/</span>
        <span class="text-gray-800 font-medium">发布帖子</span>
      </div>

      <div class="bg-white rounded-xl shadow-sm p-8">
        <h1 class="text-2xl font-bold text-gray-800 mb-6 flex items-center gap-3">
          <Edit class="w-7 h-7 text-blue-500" />
          发布新帖子
        </h1>

        <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
          <!-- 标题 -->
          <el-form-item prop="title" label="标题">
            <el-input v-model="form.title" placeholder="请输入标题（5-100字）" maxlength="100" show-word-limit
              size="large" class="custom-input" />
          </el-form-item>

          <!-- 板块选择 -->
          <el-form-item label="所属板块">
            <el-select v-model="form.sectionId" placeholder="选择板块（可选）" clearable size="large"
              class="w-full custom-select">
              <el-option v-for="s in sections" :key="s.id" :label="s.name" :value="s.id">
                <span class="flex items-center gap-2">
                  <Folder class="w-4 h-4 text-blue-400" /> {{ s.name }}
                </span>
              </el-option>
            </el-select>
          </el-form-item>

          <!-- 话题标签 -->
          <el-form-item label="话题标签">
            <div class="flex flex-wrap gap-2 mb-3">
              <button v-for="t in hotTopics" :key="t.id" type="button"
                @click="toggleTopic(t.id)"
                :class="[
                  'px-3 py-1.5 rounded-full text-sm transition-all duration-200 cursor-pointer',
                  form.topicIds.includes(t.id)
                    ? 'bg-gradient-to-r from-blue-500 to-blue-600 text-white shadow-md shadow-blue-500/25'
                    : 'bg-gray-100 text-gray-600 hover:bg-blue-50 hover:text-blue-500'
                ]">
                #{{ t.name }}
              </button>
            </div>
          </el-form-item>

          <!-- Markdown编辑器 -->
          <el-form-item prop="content" label="正文内容">
            <div class="border border-gray-200 rounded-lg overflow-hidden editor-wrapper">
              <textarea v-model="form.content"
                placeholder="在此输入正文内容，支持 Markdown 语法..."
                rows="16"
                class="w-full px-4 py-3 resize-none focus:outline-none text-sm leading-relaxed bg-transparent"></textarea>
              <div class="bg-gray-50 px-4 py-2 flex items-center justify-between text-xs text-gray-400 border-t">
                <span>支持 **加粗** *斜体* [链接](url) ```代码``` 等Markdown语法</span>
                <span>{{ form.content.length }} 字符</span>
              </div>
            </div>
          </el-form-item>

          <!-- 封面图 -->
          <el-form-item label="封面图片">
            <div class="flex items-center gap-4">
              <el-input v-model="form.coverImage" placeholder="输入图片URL或上传" size="default"
                class="flex-1 max-w-md custom-input" />
              <button type="button" @click="ElMessage.info('上传功能待接入')"
                class="px-4 py-2 bg-gray-100 hover:bg-gray-200 text-gray-700 rounded-lg text-sm transition-colors cursor-pointer">
                <Upload class="w-4 h-4 inline mr-1" /> 上传
              </button>
            </div>
          </el-form-item>

          <!-- 匿名选项 -->
          <el-form-item>
            <el-checkbox v-model="form.isAnonymous">匿名发布（发布后将隐藏你的身份信息）</el-checkbox>
          </el-form-item>

          <!-- 操作按钮 -->
          <div class="flex items-center justify-between pt-4 border-t mt-6">
            <button type="button" @click="saveDraft"
              class="px-5 py-2.5 border border-gray-300 text-gray-600 rounded-lg hover:bg-gray-50 transition-all cursor-pointer text-sm">
              <Save class="w-4 h-4 inline mr-1" /> 存草稿
            </button>
            <div class="flex gap-3">
              <router-link to="/"
                class="px-5 py-2.5 border border-gray-300 text-gray-600 rounded-lg hover:bg-gray-50 transition-all text-sm inline-flex items-center">
                取消
              </router-link>
              <button type="button" @click="handleSubmit" :loading="submitting"
                class="px-8 py-2.5 bg-gradient-to-r from-blue-500 to-blue-600 text-white rounded-lg hover:from-blue-600 hover:to-blue-700 shadow-lg shadow-blue-500/30 hover:shadow-blue-500/40 transition-all cursor-pointer text-sm font-medium">
                <Send class="w-4 h-4 inline mr-1" /> 发布
              </button>
            </div>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Edit, Folder, Upload, Save, Send } from 'lucide-vue-next'
import { ElMessage } from 'element-plus'
import { postApi, sectionApi, topicApi } from '../../../api'

const router = useRouter()
const submitting = ref(false)
const sections = ref<any[]>([])
const hotTopics = ref<any[]>([])

const form = reactive({
  title: '',
  content: '',
  summary: '',
  coverImage: '',
  sectionId: null as number | null,
  isAnonymous: false,
  topicIds: [] as number[],
})

const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }, { min: 5, max: 100, message: '标题长度5-100字', trigger: 'blur' }],
  content: [{ required: true, message: '请输入正文内容', trigger: 'blur' }, { min: 10, message: '正文至少10个字符', trigger: 'blur' }],
}

function toggleTopic(id: number) {
  const idx = form.topicIds.indexOf(id)
  if (idx >= 0) {
    form.topicIds.splice(idx, 1)
  } else if (form.topicIds.length < 5) {
    form.topicIds.push(id)
  }
}

async function handleSubmit() {
  if (!form.title || !form.content) {
    ElMessage.warning('请填写标题和正文')
    return
  }
  if (form.content.length < 10) {
    ElMessage.warning('正文至少需要10个字符')
    return
  }
  submitting.value = true
  try {
    const id = await postApi.create({
      title: form.title,
      content: form.content,
      summary: form.content.substring(0, 200),
      coverImage: form.coverImage || undefined,
      sectionId: form.sectionId ?? undefined,
      isAnonymous: form.isAnonymous,
      topicIds: form.topicIds.length > 0 ? form.topicIds : undefined,
    })
    ElMessage.success('发布成功！')
    router.push(`/post/${id}`)
  } catch (e) {
    console.error(e)
  } finally {
    submitting.value = false
  }
}

async function saveDraft() {
  try {
    await postApi.saveDraft({ title: form.title, content: form.content })
    ElMessage.success('草稿已保存')
  } catch (e) {
    console.error(e)
  }
}

onMounted(async () => {
  try {
    const [secRes, topRes]: any[] = await Promise.all([
      sectionApi.list(),
      topicApi.hot(),
    ])
    sections.value = secRes || []
    hotTopics.value = (topRes || []).slice(0, 12)
  } catch (e) {
    console.error(e)
  }
})
</script>

<style scoped>
.editor-wrapper :deep(textarea) {
  min-height: 320px;
}
.custom-input :deep(.el-input__wrapper) {
  box-shadow: 0 0 0 1px #E4E7ED inset;
  border-radius: 8px;
}
.custom-input :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #C0C4CC inset;
}
.custom-select :deep(.el-select__wrapper) {
  box-shadow: 0 0 0 1px #E4E7ED inset;
  border-radius: 8px;
}
</style>
