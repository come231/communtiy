<template>
  <div class="max-w-5xl mx-auto px-4 py-6">
    <!-- 加载状态 -->
    <div v-if="loading" class="bg-white rounded-xl shadow-sm border border-gray-100 p-12 text-center">
      <el-skeleton :rows="10" animated />
    </div>

    <template v-else-if="post">
      <div class="flex gap-6">
        <!-- 主内容 -->
        <div class="flex-1 min-w-0 space-y-4">
          <!-- 帖子卡片 -->
          <article class="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
            <!-- 标签行 -->
            <div class="px-6 pt-5 flex items-center gap-2 flex-wrap">
              <span v-if="post.isTop" class="px-2 py-0.5 text-xs bg-red-50 text-red-500 rounded font-medium">置顶</span>
              <span v-if="post.isEssence" class="px-2 py-0.5 text-xs bg-orange-50 text-orange-500 rounded font-medium">精华</span>
              <router-link :to="`/section/${post.sectionId}`" class="px-2 py-0.5 text-xs bg-blue-50 text-primary rounded hover:bg-blue-100 transition-colors">{{ post.sectionName || '默认板块' }}</router-link>
            </div>

            <!-- 标题 & 作者 -->
            <div class="px-6 pb-4">
              <h1 class="text-2xl font-bold text-gray-800 mb-4 leading-tight">{{ post.title }}</h1>
              <div class="flex items-center justify-between">
                <div class="flex items-center gap-3 cursor-pointer" @click="$router.push(`/user/${post.authorId}`)">
                  <img :src="post.authorAvatar || defaultAvatar" class="w-10 h-10 rounded-full object-cover border border-gray-200" />
                  <div>
                    <p class="text-sm font-medium text-gray-800">{{ post.isAnonymous ? '匿名用户' : post.authorName }}</p>
                    <p class="text-xs text-gray-400">{{ formatTime(post.createTime) }}</p>
                  </div>
                </div>
                <button v-if="!isAuthor" @click="handleFollowAuthor" :class="['px-4 py-1.5 text-sm rounded-full transition-all', isFollowing ? 'bg-gray-100 text-gray-500' : 'bg-primary text-white hover:bg-blue-600']">
                  {{ isFollowing ? '已关注' : '+ 关注' }}
                </button>
              </div>
            </div>

            <!-- 分割线 -->
            <div class="px-6"><div class="border-t border-gray-100"></div></div>

            <!-- 正文内容 (Markdown 渲染) -->
            <div class="px-6 py-5 prose prose-sm max-w-none text-gray-700 leading-relaxed" v-html="renderContent(post.content)"></div>

            <!-- 底部操作栏 -->
            <div class="px-6 py-4 border-t border-gray-100 flex items-center justify-between bg-gray-50/50">
              <div class="flex items-center gap-6">
                <!-- 点赞 -->
                <button @click="handleLike" class="flex items-center gap-1.5 group transition-all">
                  <svg :class="['w-5 h-5 transition-colors', post.isLiked ? 'text-red-500 fill-red-500' : 'text-gray-400 group-hover:text-red-400']" fill="currentColor" viewBox="0 0 24 24"><path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/></svg>
                  <span :class="['text-sm', post.isLiked ? 'text-red-500 font-medium' : 'text-gray-500']">{{ post.likeCount || 0 }}</span>
                </button>
                <!-- 收藏 -->
                <button @click="handleCollect" class="flex items-center gap-1.5 group transition-all">
                  <svg :class="['w-5 h-5 transition-colors', post.isCollected ? 'text-yellow-500 fill-yellow-500' : 'text-gray-400 group-hover:text-yellow-400']" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 5a2 2 0 012-2h10a2 2 0 012 2v16l-7-3.5L5 21V5z"/></svg>
                  <span :class="['text-sm', post.isCollected ? 'text-yellow-600 font-medium' : 'text-gray-500']">{{ post.collectCount || 0 }}</span>
                </button>
                <!-- 浏览量 -->
                <span class="flex items-center gap-1.5 text-gray-400 text-sm">
                  <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"/></svg>{{ post.viewCount }}
                </span>
              </div>

              <!-- 分享 & 删除 -->
              <div class="flex items-center gap-3">
                <el-button v-if="isAuthor" type="danger" size="small" plain @click="handleDelete">删除</el-button>
              </div>
            </div>
          </article>

          <!-- 评论区域 -->
          <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-6">
            <h3 class="text-base font-bold text-gray-800 mb-4 flex items-center gap-2">
              <svg class="w-5 h-5 text-primary" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"/></svg>
              评论 ({{ commentTotal }})
            </h3>

            <!-- 评论输入框 -->
            <div v-if="userStore.isLoggedIn" class="mb-6">
              <div class="flex gap-3">
                <img :src="userStore.avatar || defaultAvatar" class="w-9 h-9 rounded-full flex-shrink-0" />
                <div class="flex-1 relative">
                  <textarea
                    v-model="commentText"
                    placeholder="写下你的评论..."
                    class="w-full px-4 py-2.5 border border-gray-200 rounded-lg resize-none focus:border-primary focus:ring-1 focus:ring-primary/20 outline-none text-sm transition-all"
                    rows="2"
                  ></textarea>
                  <div class="flex justify-end mt-2">
                    <el-button type="primary" size="small" @click="submitComment" :loading="submitting">发表评论</el-button>
                  </div>
                </div>
              </div>
            </div>
            <div v-else class="mb-6 p-4 bg-blue-50/50 rounded-lg text-center text-sm text-gray-500">
              <router-link to="/login" class="text-primary hover:underline">登录后参与评论</router-link>
            </div>

            <!-- 评论列表 -->
            <div v-loading="commentLoading" class="space-y-4">
              <div v-for="comment in comments" :key="comment.id" class="flex gap-3 pb-4 border-b border-gray-50 last:border-0">
                <img :src="comment.userAvatar || defaultAvatar" class="w-9 h-9 rounded-full flex-shrink-0 cursor-pointer" @click="$router.push(`/user/${comment.userId}`)" />
                <div class="flex-1 min-w-0">
                  <div class="flex items-center gap-2 mb-1">
                    <span class="text-sm font-medium text-gray-800 cursor-pointer hover:text-primary" @click="$router.push(`/user/${comment.userId}`)">{{ comment.userName }}</span>
                    <span class="text-xs text-gray-300">#{{ comment.floorNumber }}</span>
                    <span class="text-xs text-gray-400 ml-auto">{{ formatTime(comment.createTime) }}</span>
                  </div>
                  <!-- 回复提示 -->
                  <p v-if="comment.replyToUserName" class="text-xs text-gray-400 mb-1">
                    回复 <span class="text-primary">@{{ comment.replyToUserName }}</span>
                  </p>
                  <p class="text-sm text-gray-700 leading-relaxed mb-2">{{ comment.content }}</p>
                  <div class="flex items-center gap-4 text-xs text-gray-400">
                    <button @click="showReplyInput(comment)" class="hover:text-primary transition-colors">回复</button>
                    <!-- 评论点赞爱心 -->
                    <button @click="handleLikeComment(comment)" class="flex items-center gap-1 hover:text-red-400 transition-colors">
                      <svg :class="['w-4 h-4 transition-colors', comment.isLiked ? 'text-red-500 fill-red-500' : '']" fill="currentColor" viewBox="0 0 24 24">
                        <path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/>
                      </svg>
                      <span :class="comment.isLiked ? 'text-red-500' : ''">{{ comment.likeCount }}</span>
                    </button>
                    <button v-if="canDelete(comment.userId)" class="hover:text-red-400 transition-colors" @click="deleteComment(comment.id)">删除</button>
                  </div>
                  <!-- 回复输入框 -->
                  <div v-if="replyToId === comment.id" class="mt-3 flex gap-2">
                    <input v-model="replyText" :placeholder="`回复 ${comment.userName}...`"
                      class="flex-1 px-3 py-1.5 text-sm border border-gray-200 rounded-lg focus:border-primary outline-none" />
                    <el-button size="small" type="primary" @click="submitReply(comment)">回复</el-button>
                    <el-button size="small" @click="replyToId = 0">取消</el-button>
                  </div>

                  <!-- 楼中楼回复 -->
                  <div v-if="comment.replies && comment.replies.length > 0" class="mt-3 bg-gray-50/80 rounded-lg p-3 space-y-3">
                    <div v-for="reply in comment.replies" :key="reply.id" class="flex gap-2">
                      <img :src="reply.userAvatar || defaultAvatar" class="w-7 h-7 rounded-full flex-shrink-0 cursor-pointer" @click="$router.push(`/user/${reply.userId}`)" />
                      <div class="flex-1 min-w-0">
                        <div class="flex items-center gap-2 mb-0.5">
                          <span class="text-xs font-medium text-gray-700 cursor-pointer hover:text-primary" @click="$router.push(`/user/${reply.userId}`)">{{ reply.userName }}</span>
                          <span v-if="reply.replyToUserName" class="text-xs text-gray-400">回复 <span class="text-primary">@{{ reply.replyToUserName }}</span></span>
                          <span class="text-xs text-gray-400 ml-auto">{{ formatTime(reply.createTime) }}</span>
                        </div>
                        <p class="text-xs text-gray-600 leading-relaxed mb-1">{{ reply.content }}</p>
                        <div class="flex items-center gap-3 text-xs text-gray-400">
                          <button @click="showReplyInput(comment, reply)" class="hover:text-primary transition-colors">回复</button>
                          <!-- 子回复点赞爱心 -->
                          <button @click="handleLikeComment(reply)" class="flex items-center gap-1 hover:text-red-400 transition-colors">
                            <svg :class="['w-3.5 h-3.5 transition-colors', reply.isLiked ? 'text-red-500 fill-red-500' : '']" fill="currentColor" viewBox="0 0 24 24">
                              <path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/>
                            </svg>
                            <span :class="reply.isLiked ? 'text-red-500' : ''">{{ reply.likeCount }}</span>
                          </button>
                          <button v-if="canDelete(reply.userId)" class="hover:text-red-400 transition-colors" @click="deleteComment(reply.id)">删除</button>
                        </div>
                        <!-- 子回复输入框 -->
                        <div v-if="replyToId === reply.id" class="mt-2 flex gap-2">
                          <input v-model="replyText" :placeholder="`回复 ${reply.userName}...`"
                            class="flex-1 px-2 py-1 text-xs border border-gray-200 rounded focus:border-primary outline-none" />
                          <el-button size="small" type="primary" @click="submitReply(comment, reply)">回复</el-button>
                          <el-button size="small" @click="replyToId = 0">取消</el-button>
                        </div>
                      </div>
                    </div>
                  </div>
                  <!-- 查看更多回复 -->
                  <button v-if="comment.replies && comment.replies.length > 0" class="text-xs text-primary hover:underline mt-2">展开 {{ comment.replies.length }} 条回复</button>
                </div>
              </div>

              <!-- 分页 -->
              <div v-if="commentTotal > 10" class="pt-4 flex justify-center">
                <el-pagination small layout="prev, pager, next" :total="commentTotal" :page-size="10" :current-page="commentPage" @current-change="loadComments" />
              </div>
            </div>
          </div>
        </div>

        <!-- 右侧边栏 -->
        <aside class="w-72 hidden xl:block flex-shrink-0 space-y-4">
          <!-- 作者卡片 -->
          <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-5">
            <div class="flex items-center gap-3">
              <img :src="post?.authorAvatar || defaultAvatar" class="w-14 h-14 rounded-full object-cover border border-gray-200" />
              <div>
                <p class="font-medium text-gray-800">{{ post?.isAnonymous ? '匿名用户' : post?.authorName }}</p>
                <p class="text-xs text-gray-400 mt-0.5">发布于 {{ formatTime(post?.createTime || '') }}</p>
              </div>
            </div>
            <div v-if="!post?.isAnonymous && !isAuthor" class="mt-4">
              <button @click="handleFollowAuthor" :class="['w-full py-2 rounded-lg text-sm font-medium transition-all', isFollowing ? 'bg-gray-100 text-gray-500' : 'bg-primary text-white hover:bg-blue-600']">
                {{ isFollowing ? '已关注' : '关注作者' }}
              </button>
            </div>
          </div>
        </aside>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '../../../store/user'
import { postApi, commentApi, interactApi, userApi } from '../../../api'
import type { PostDetail, Comment } from '../../../types'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const defaultAvatar = 'https://placehold.co/40x40/e0e7ff/409EFF?text=U'

const loading = ref(true)
const post = ref<PostDetail | null>(null)
const comments = ref<Comment[]>([])
const commentLoading = ref(false)
const commentPage = ref(1)
const commentTotal = ref(0)

const isFollowing = ref(false)
const commentText = ref('')
const submitting = ref(false)
const replyToId = ref(0)
const replyText = ref('')

const postId = computed(() => Number(route.params.id))
const isAuthor = computed(() => String(post.value?.authorId) === userStore.userId)

onMounted(() => {
  loadPost()
  loadComments()
})

async function loadPost() {
  loading.value = true
  try {
    const res: any = await postApi.detail(postId.value)
    // 映射后端字段到前端
    post.value = {
      ...res,
      id: res.postId,
      coverImage: res.images || '',
    }
    // 检查是否关注了作者
    if (userStore.isLoggedIn && !post.value!.isAnonymous) {
      try { 
        const followed: any = await userApi.isFollowing(post.value!.authorId)
        isFollowing.value = followed
      } catch {}
    }
    // 检查当前用户是否点赞/收藏
    if (userStore.isLoggedIn) {
      try {
        const [liked, collected]: any[] = await Promise.all([
          interactApi.isLiked(1, postId.value),
          interactApi.isCollected(postId.value)
        ])
        if (post.value) {
          post.value.isLiked = liked
          post.value.isCollected = collected
        }
      } catch {}
    }
  } catch { /* handled by interceptor */ }
  finally { loading.value = false }
}

async function loadComments(page = 1) {
  commentLoading.value = true
  commentPage.value = page
  try {
    const res: any = await commentApi.list(postId.value, page, 10)
    const records = res?.records || []
    // 映射后端字段到前端（commentId -> id）
    comments.value = records.map((c: any) => ({
      id: c.commentId,
      content: c.content,
      postId: c.postId,
      userId: c.userId,
      userName: c.authorName || '',
      userAvatar: c.authorAvatar || '',
      parentId: c.parentId,
      replyToUserId: c.replyUid,
      replyToUserName: c.replyUserName,
      floorNumber: c.floor,
      likeCount: c.likeCount || 0,
      isLiked: c.isLiked || false,
      createTime: c.createTime,
      replies: (c.replies || []).map((r: any) => ({
        id: r.commentId,
        content: r.content,
        postId: r.postId,
        userId: r.userId,
        userName: r.authorName || '',
        userAvatar: r.authorAvatar || '',
        parentId: r.parentId,
        replyToUserId: r.replyUid,
        replyToUserName: r.replyUserName,
        floorNumber: r.floor,
        likeCount: r.likeCount || 0,
        isLiked: r.isLiked || false,
        createTime: r.createTime,
      }))
    }))
    commentTotal.value = res?.total || 0
  } catch {}
  finally { commentLoading.value = false }
}

function renderContent(content: string): string {
  return content
    .replace(/### (.*$)/gim, '<h3>$1</h3>')
    .replace(/## (.*$)/gim, '<h2>$1</h2>')
    .replace(/\*\*(.*?)\*\*/gim, '<strong>$1</strong>')
    .replace(/\n/gim, '<br/>')
}

function formatTime(time: string): string {
  const now = Date.now(), t = new Date(time).getTime(), diff = now - t
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  if (diff < 604800000) return Math.floor(diff / 86400000) + '天前'
  return time.split(' ')[0]
}

async function handleLike() {
  if (!userStore.isLoggedIn) { ElMessage.warning('请先登录'); return }
  const p = post.value!
  try {
    if (p.isLiked) {
      await interactApi.unlike(1, p.id)
      p.isLiked = false; p.likeCount--
    } else {
      await interactApi.like(1, p.id)
      p.isLiked = true; p.likeCount++
    }
  } catch (e: any) {
    // 已点赞/已收藏等业务异常：回滚UI状态
    const errMsg = e?.message || ''
    if (errMsg.includes('已点赞') || errMsg.includes('2008')) {
      p.isLiked = true; p.likeCount++
    } else if (errMsg.includes('未点赞') || errMsg.includes('2009')) {
      p.isLiked = false; p.likeCount--
    } else {
      console.error('操作失败', e)
    }
  }
}

async function handleCollect() {
  if (!userStore.isLoggedIn) { ElMessage.warning('请先登录'); return }
  const p = post.value!
  try {
    if (p.isCollected) {
      await interactApi.uncollect(p.id)
      p.isCollected = false; p.collectCount--
      ElMessage.success('已取消收藏')
    } else {
      await interactApi.collect(p.id)
      p.isCollected = true; p.collectCount++
      ElMessage.success('收藏成功')
    }
  } catch (e: any) {
    const errMsg = e?.message || ''
    if (errMsg.includes('已收藏') || errMsg.includes('2010')) {
      p.isCollected = true; p.collectCount++
    } else {
      console.error('操作失败', e)
    }
  }
}

async function handleFollowAuthor() {
  if (!userStore.isLoggedIn) { ElMessage.warning('请先登录'); return }
  const authorId = post.value!.authorId
  try {
    if (isFollowing.value) {
      await userApi.unfollow(authorId)
      isFollowing.value = false; ElMessage.success('已取消关注')
    } else {
      await userApi.follow(authorId)
      isFollowing.value = true; ElMessage.success('关注成功')
    }
  } catch (e: any) {
    console.error('操作失败', e)
  }
}

async function submitComment() {
  const content = commentText.value.trim()
  if (!content) return
  submitting.value = true
  try {
    await commentApi.create({ postId: postId.value, content })
    commentText.value = ''
    ElMessage.success('评论成功')
    loadComments()
  } catch (e: any) {
    console.error('评论失败', e)
  }
  finally { submitting.value = false }
}

function showReplyInput(comment: Comment, reply?: Comment) {
  replyToId.value = reply ? reply.id : comment.id
  replyText.value = ''
}

async function submitReply(parent: Comment, reply?: Comment) {
  const content = replyText.value.trim()
  if (!content) return
  try {
    const parentId = parent.id
    const replyToUserId = reply ? reply.userId : parent.userId
    await commentApi.create({ postId: postId.value, content, parentId, replyToUserId })
    replyToId.value = 0; replyText.value = ''
    ElMessage.success('回复成功')
    loadComments()
  } catch (e: any) {
    console.error('回复失败', e)
  }
}

async function handleLikeComment(comment: Comment) {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }
  if (!comment.id) {
    console.error('Comment id is undefined')
    return
  }
  try {
    if (comment.isLiked) {
      await interactApi.unlike(2, comment.id)
      comment.isLiked = false
      comment.likeCount = Math.max(0, (comment.likeCount || 1) - 1)
    } else {
      await interactApi.like(2, comment.id)
      comment.isLiked = true
      comment.likeCount = (comment.likeCount || 0) + 1
    }
  } catch (e: any) {
    const errMsg = e?.message || ''
    if (errMsg.includes('已点赞') || errMsg.includes('2008')) {
      comment.isLiked = true
      comment.likeCount = (comment.likeCount || 0) + 1
    } else {
      console.error('点赞失败', e)
    }
  }
}

function canDelete(userId: number): boolean {
  return String(userId) === userStore.userId
}

async function deleteComment(id: number) {
  await ElMessageBox.confirm('确定删除该评论吗？', '提示', { type: 'warning' })
  try {
    await commentApi.delete(id)
    ElMessage.success('删除成功')
    loadComments()
  } catch (e: any) {
    if (e !== 'cancel') {
      console.error('删除评论失败', e)
    }
  }
}

async function handleDelete() {
  await ElMessageBox.confirm('确定删除该帖子吗？', '警告', { type: 'warning' })
  try {
    await postApi.delete(postId.value)
    ElMessage.success('删除成功')
    router.push('/home')
  } catch (e: any) {
    if (e !== 'cancel') {
      console.error('删除帖子失败', e)
    }
  }
}
</script>
