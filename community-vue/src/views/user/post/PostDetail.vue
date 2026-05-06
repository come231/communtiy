<template>
  <div class="min-h-[calc(100vh-4.5rem)] bg-gradient-to-br from-[#f0f4ff] to-[#f8faff] py-6 md:py-8">
    <div class="container mx-auto px-4 max-w-[1280px]">
    <div v-if="loading" class="user-surface-card p-12 text-center">
      <el-skeleton :rows="10" animated />
    </div>

    <template v-else-if="post">
      <div class="flex flex-col lg:flex-row gap-6">
        <div class="flex-1 min-w-0 space-y-6">
          <article class="user-surface-card overflow-hidden">
            <div v-if="post.coverImage" class="w-full border-b border-[#e5e7eb]">
              <img :src="resolveFileUrl(post.coverImage)" alt="" class="w-full max-h-[min(420px,70vh)] object-cover bg-[#f3f4f6]" loading="lazy" referrerpolicy="no-referrer" />
            </div>
            <div class="px-6 pt-5 flex items-center gap-2 flex-wrap">
              <span v-if="post.isTop" class="px-2 py-0.5 text-xs bg-rose-100 text-rose-700 rounded font-medium">置顶</span>
              <span v-if="post.isEssence" class="px-2 py-0.5 text-xs bg-amber-100 text-amber-800 rounded font-medium">精华</span>
              <router-link :to="`/section/${post.sectionId}`" class="px-2 py-0.5 text-xs bg-[#eef2ff] text-[#6366f1] rounded hover:bg-[#e0e7ff] transition-colors">{{ post.sectionName || '默认板块' }}</router-link>
            </div>

            <div class="px-6 pb-4">
              <h1 class="text-2xl md:text-3xl font-bold text-[#1f2937] mb-4 leading-tight">{{ post.title }}</h1>
              <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
                <div class="flex items-center gap-3 cursor-pointer" @click="$router.push(`/user/${post.authorId}`)">
                  <img :src="resolveFileUrl(post.authorAvatar) || defaultAvatar" referrerpolicy="no-referrer" class="w-10 h-10 rounded-full object-cover border-2 border-[#e5e7eb]" />
                  <div>
                    <p class="text-sm font-medium text-[#1f2937]">{{ post.isAnonymous ? '匿名用户' : post.authorName }}</p>
                    <p class="text-xs text-[#6b7280]">{{ formatTime(post.createTime) }}</p>
                  </div>
                </div>
                <div class="flex items-center gap-3">
                  <button v-if="!isAuthor" @click="handleFollowAuthor" :class="['px-4 py-1.5 text-sm rounded-full transition-all', isFollowing ? 'border border-[#e5e7eb] bg-[#f9fafb] text-[#6b7280]' : 'btn-gradient']">
                    {{ isFollowing ? '已关注' : '+ 关注' }}
                  </button>
                  <button v-if="!isAuthor && userStore.isLoggedIn" type="button" @click="showReportDialog = true" class="px-4 py-1.5 text-sm rounded-full border border-[#e5e7eb] bg-white text-[#6b7280] hover:bg-[#f9fafb] transition-all inline-flex items-center gap-1">
                    <i class="fa fa-flag mr-1"></i> 举报
                  </button>
                </div>
              </div>
            </div>

            <div class="px-6"><div class="border-t border-[#e5e7eb]"></div></div>

            <div class="px-6 py-5 prose prose-sm max-w-none text-[#374151] leading-relaxed post-md post-md-light" v-html="renderContent(post.content)"></div>

            <div v-if="detailVideo" class="px-6 pb-4">
              <p class="text-xs text-[#9ca3af] mb-2">视频</p>
              <video
                v-if="isVideoFile(detailVideo)"
                :src="resolveFileUrl(detailVideo)"
                class="w-full max-h-[min(480px,70vh)] rounded-xl border border-[#e5e7eb] bg-[#0f172a]"
                controls
                playsinline
                preload="metadata"
              />
              <a v-else :href="resolveFileUrl(detailVideo)" target="_blank" class="text-primary text-sm">打开视频</a>
            </div>

            <!-- 正文配图（多图固定区域） -->
            <div v-if="detailImages.length" class="px-6 pb-6">
              <p class="text-xs text-[#9ca3af] mb-3">配图</p>
              <div class="grid grid-cols-1 sm:grid-cols-2 gap-3">
                <a v-for="(u, i) in detailImages" :key="i" :href="resolveFileUrl(u)" target="_blank" rel="noopener noreferrer" class="block rounded-xl overflow-hidden border border-[#e5e7eb] hover:border-[#818cf8] transition-colors">
                  <img :src="resolveFileUrl(u)" alt="" class="w-full h-52 object-cover bg-[#f3f4f6]" loading="lazy" referrerpolicy="no-referrer" />
                </a>
              </div>
            </div>

            <!-- 举报对话框 -->
            <el-dialog v-model="showReportDialog" title="举报帖子" width="450px">
              <div class="space-y-4">
                <div>
                  <label class="block text-sm font-medium text-[#374151] mb-2">举报类型</label>
                  <div class="grid grid-cols-2 gap-2">
                    <button
                      v-for="type in reportTypes"
                      :key="type.value"
                      type="button"
                      :class="[
                        'px-4 py-2 text-sm rounded-lg border transition-all text-left',
                        reportReason === type.value
                          ? 'border-[#6366f1] bg-[#eef2ff] text-[#6366f1]'
                          : 'border-[#e5e7eb] text-[#6b7280] hover:border-[#c7d2fe]',
                      ]"
                      @click="reportReason = type.value"
                    >
                      {{ type.label }}
                    </button>
                  </div>
                </div>
                <div>
                  <label class="block text-sm font-medium text-[#374151] mb-2">详细描述（选填）</label>
                  <textarea
                    v-model="reportDescription"
                    placeholder="请描述您举报的具体内容..."
                    rows="3"
                    class="w-full px-4 py-2 rounded-xl text-sm resize-none border border-[#e5e7eb] bg-[#f9fafb] text-[#1f2937] outline-none focus:border-[#818cf8]"
                  ></textarea>
                </div>
              </div>
              <template #footer>
                <el-button @click="showReportDialog = false">取消</el-button>
                <el-button type="primary" @click="submitReport">提交举报</el-button>
              </template>
            </el-dialog>

            <el-dialog v-model="sharePmDialogVisible" title="通过私信分享帖子" width="460px" @opened="loadShareRecipients">
              <p class="text-sm text-[#6b7280] mb-3">选择「我关注的」或「我的粉丝」，向对方发送可点击打开的帖子卡片。</p>
              <el-tabs v-model="sharePmTab" class="share-pm-tabs mb-3">
                <el-tab-pane label="我关注的人" name="following"></el-tab-pane>
                <el-tab-pane label="我的粉丝" name="followers"></el-tab-pane>
              </el-tabs>
              <div v-loading="sharePmLoading" class="min-h-[140px]">
                <el-select
                  v-model="sharePmSelectedId"
                  filterable
                  placeholder="选择要分享的用户"
                  class="w-full"
                  size="large"
                  :class="'glass-input'"
                >
                  <el-option
                    v-for="u in currentShareUserOptions"
                    :key="u.userId"
                    :label="formatShareUserLabel(u)"
                    :value="u.userId"
                  >
                    <div class="flex items-center gap-2 py-1">
                      <img :src="resolveFileUrl(u.avatar) || defaultAvatar" referrerpolicy="no-referrer" class="w-8 h-8 rounded-full object-cover" alt="" />
                      <div class="flex flex-col">
                        <span class="text-[#1f2937]">{{ formatShareUserLabel(u) }}</span>
                        <span class="text-xs text-[#9ca3af]">ID {{ u.userId }}</span>
                      </div>
                    </div>
                  </el-option>
                </el-select>
                <p v-if="!sharePmLoading && currentShareUserOptions.length === 0" class="text-sm text-amber-700 mt-3">
                  {{ sharePmTab === 'following' ? '暂无关注用户，先去关注感兴趣的人吧。' : '暂无粉丝。' }}
                </p>
              </div>
              <template #footer>
                <el-button @click="sharePmDialogVisible = false">取消</el-button>
                <el-button type="primary" :disabled="!sharePmSelectedId" @click="sendSharePm">发送</el-button>
              </template>
            </el-dialog>

            <div class="px-6 py-4 border-t border-[#e5e7eb] flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 bg-[#fafbff]">
              <div class="flex items-center gap-6">
                <button type="button" @click="handleLike" class="flex items-center gap-1.5 group transition-all">
                  <i :class="['fa fa-heart text-xl transition-colors', post.isLiked ? 'text-red-500' : 'text-[#9ca3af] group-hover:text-red-400']"></i>
                  <span :class="['text-sm', post.isLiked ? 'text-red-500 font-medium' : 'text-[#6b7280]']">{{ post.likeCount || 0 }}</span>
                </button>
                <button type="button" @click="handleCollect" class="flex items-center gap-1.5 group transition-all">
                  <i :class="['fa fa-star text-xl transition-colors', post.isCollected ? 'text-yellow-500' : 'text-[#9ca3af] group-hover:text-yellow-500']"></i>
                  <span :class="['text-sm', post.isCollected ? 'text-yellow-600 font-medium' : 'text-[#6b7280]']">{{ post.collectCount || 0 }}</span>
                </button>
                <span class="flex items-center gap-1.5 text-[#6b7280] text-sm">
                  <i class="fa fa-eye text-xl mr-1"></i>{{ post.viewCount }}
                </span>
              </div>

              <div class="flex items-center gap-3">
                <button type="button" title="复制链接" @click="handleShare" class="text-[#9ca3af] hover:text-[#6366f1] transition-colors">
                  <i class="fa fa-share-alt text-xl"></i>
                </button>
                <button v-if="userStore.isLoggedIn" type="button" title="私信分享" @click="openSharePmDialog" class="text-[#9ca3af] hover:text-[#6366f1] transition-colors">
                  <i class="fa fa-paper-plane text-xl"></i>
                </button>
                <button v-if="!isAuthor && userStore.isLoggedIn" type="button" @click="goMessage" class="text-[#9ca3af] hover:text-[#6366f1] transition-colors">
                  <i class="fa fa-envelope text-xl"></i>
                </button>
                <button v-if="isAuthor" type="button" @click="router.push(`/post/${postId}/edit`)" class="px-3 py-1.5 text-sm rounded-xl border border-[#e5e7eb] bg-white text-[#1f2937] hover:bg-[#f9fafb]">编辑</button>
                <el-button v-if="isAuthor" type="danger" size="small" plain @click="handleDelete">删除</el-button>
              </div>
            </div>

            <div class="px-6 py-4 border-t border-[#e5e7eb] bg-white">
              <div class="flex flex-wrap items-center gap-2">
                <span class="text-sm text-[#6b7280] mr-1">AI辅助</span>
                <button type="button" class="ai-op-btn" :disabled="aiLoading" @click="handleAiSummary">生成摘要</button>
                <button type="button" class="ai-op-btn" :disabled="aiLoading" @click="handleAiKeypoints">解读重点</button>
                <button type="button" class="ai-op-btn" :disabled="aiLoading" @click="handleAiReplySuggestions">生成回复</button>
                <span v-if="aiLoading" class="text-xs text-[#9ca3af]">正在思考...</span>
              </div>
              <div v-if="aiResult" class="mt-3 rounded-xl border border-[#e5e7eb] bg-[#fafbff] p-3">
                <div class="text-xs text-[#6366f1] mb-1">AI结果</div>
                <pre class="text-sm text-[#374151] whitespace-pre-wrap leading-6">{{ aiResult }}</pre>
                <div class="mt-3 flex flex-wrap items-center gap-2">
                  <button type="button" class="ai-op-btn" @click="copyAiResult">复制结果</button>
                  <button type="button" class="ai-op-btn" :disabled="!userStore.isLoggedIn" @click="fillCommentWithAi">填入评论框</button>
                  <button type="button" class="ai-op-btn" :disabled="aiLoading" @click="submitAiFeedback('like')">满意</button>
                  <button type="button" class="ai-op-btn" :disabled="aiLoading" @click="submitAiFeedback('dislike')">不满意</button>
                  <span class="text-xs text-[#9ca3af] ml-1">不满意？</span>
                  <button type="button" class="ai-op-btn" :disabled="aiLoading" @click="retryAiWithStyle('简洁')">更简洁</button>
                  <button type="button" class="ai-op-btn" :disabled="aiLoading" @click="retryAiWithStyle('活泼')">更活泼</button>
                  <button type="button" class="ai-op-btn" :disabled="aiLoading" @click="retryAiWithStyle('正式')">更正式</button>
                </div>
              </div>
            </div>
          </article>

          <div class="user-surface-card p-6">
            <h3 class="text-xl font-bold text-[#1f2937] mb-6 flex items-center gap-2">
              <i class="fa fa-comments text-[#6366f1] mr-2"></i>
              评论 ({{ commentTotal }})
            </h3>

            <div v-if="userStore.isLoggedIn" class="mb-8">
              <div class="flex gap-3">
                <img :src="resolveFileUrl(userStore.avatar) || defaultAvatar" referrerpolicy="no-referrer" class="w-9 h-9 rounded-full flex-shrink-0 border border-[#e5e7eb]" />
                <div class="flex-1 relative">
                  <input ref="commentImageInputRef" type="file" accept="image/*" multiple class="hidden" @change="onCommentPics" />
                  <textarea
                    v-model="commentText"
                    placeholder="文字与配图可只填一种；多图可多次选择"
                    class="w-full px-4 py-3 rounded-xl resize-none border border-[#e5e7eb] bg-[#f9fafb] focus:border-[#818cf8] focus:ring-1 focus:ring-[#818cf8]/30 outline-none text-sm text-[#1f2937] placeholder:text-[#9ca3af]"
                    rows="2"
                  ></textarea>
                  <div v-if="commentPics.length" class="flex flex-wrap gap-2 mt-2">
                    <div v-for="(p, i) in commentPics" :key="i" class="relative w-16 h-16 rounded-lg border border-[#e5e7eb] overflow-hidden group">
                      <img :src="p.preview" class="w-full h-full object-cover" alt="" />
                      <button type="button" class="absolute inset-0 bg-black/50 text-white text-xs opacity-0 group-hover:opacity-100" @click="commentPics.splice(i,1)">移除</button>
                    </div>
                  </div>
                  <div class="flex justify-between items-center mt-3">
                    <button type="button" class="text-xs text-[#6366f1] hover:underline" @click="commentImageInputRef?.click()">+ 选择配图</button>
                    <el-button type="primary" size="small" @click="submitComment" :loading="submitting">发表评论</el-button>
                  </div>
                </div>
              </div>
            </div>
            <div v-else class="mb-8 p-4 rounded-xl border border-[#e5e7eb] bg-[#f9fafb] text-center text-sm text-[#6b7280]">
              <router-link to="/login" class="text-[#6366f1] hover:underline">登录后参与评论</router-link>
            </div>

            <div v-loading="commentLoading" class="space-y-6">
              <div v-for="comment in comments" :key="comment.id" class="flex gap-3 pb-6 border-b border-[#e5e7eb] last:border-0">
                <img
                  :src="avatarDisplayUrl(comment.userAvatar, defaultAvatar)"
                  referrerpolicy="no-referrer"
                  class="w-9 h-9 rounded-full flex-shrink-0 cursor-pointer ring-1 ring-[#e5e7eb] object-cover"
                  @click="$router.push(`/user/${comment.userId}`)"
                />
                <div class="flex-1 min-w-0">
                  <div class="flex items-center gap-2 mb-2">
                    <span class="text-sm font-medium text-[#1f2937] cursor-pointer hover:text-[#6366f1]" @click="$router.push(`/user/${comment.userId}`)">{{ comment.userName }}</span>
                    <span class="text-xs text-[#9ca3af]">#{{ comment.floorNumber }}</span>
                    <span class="text-xs text-[#9ca3af] ml-auto">{{ formatTime(comment.createTime) }}</span>
                  </div>
                  <p v-if="comment.replyToUserName" class="text-xs text-[#6b7280] mb-2">
                    回复 <span class="text-[#6366f1]">@{{ comment.replyToUserName }}</span>
                  </p>
                  <p v-if="comment.content && comment.content.trim()" class="text-sm text-[#374151] leading-relaxed mb-3">{{ comment.content }}</p>
                  <div v-if="comment.imageUrls && comment.imageUrls.length" class="flex flex-wrap gap-2 mb-3">
                    <a v-for="(iu, ii) in comment.imageUrls" :key="ii" :href="resolveFileUrl(iu)" target="_blank" class="block w-24 h-24 rounded-lg overflow-hidden border border-[#e5e7eb]">
                      <img :src="resolveFileUrl(iu)" class="w-full h-full object-cover" alt="" referrerpolicy="no-referrer" />
                    </a>
                  </div>
                  <div class="flex items-center gap-4 text-xs text-[#6b7280]">
                    <button type="button" @click="showReplyInput(comment)" class="hover:text-[#6366f1] transition-colors">回复</button>
                    <button type="button" @click="handleLikeComment(comment)" class="flex items-center gap-1 hover:text-red-500 transition-colors">
                      <i :class="['fa fa-heart transition-colors', comment.isLiked ? 'text-red-500' : '']"></i>
                      <span :class="comment.isLiked ? 'text-red-500' : ''">{{ comment.likeCount }}</span>
                    </button>
                    <button v-if="canDelete(comment.userId)" type="button" class="hover:text-red-500 transition-colors" @click="deleteComment(comment.id)">删除</button>
                  </div>
                  <div v-if="replyToId === comment.id" class="mt-4 flex gap-2 flex-wrap">
                    <input v-model="replyText" :placeholder="`回复 ${comment.userName}...`"
                      class="flex-1 min-w-[12rem] px-3 py-2 rounded-xl border border-[#e5e7eb] bg-[#f9fafb] text-sm outline-none focus:border-[#818cf8]" />
                    <el-button size="small" type="primary" @click="submitReply(comment)">回复</el-button>
                    <el-button size="small" @click="replyToId = 0">取消</el-button>
                  </div>

                  <div v-if="comment.replies && comment.replies.length > 0" class="mt-4 rounded-xl border border-[#e5e7eb] bg-[#fafbff] p-4 space-y-4">
                    <div v-for="reply in comment.replies" :key="reply.id" class="flex gap-2">
                      <img
                        :src="avatarDisplayUrl(reply.userAvatar, defaultAvatar)"
                        referrerpolicy="no-referrer"
                        class="w-7 h-7 rounded-full flex-shrink-0 cursor-pointer ring-1 ring-[#e5e7eb] object-cover"
                        @click="$router.push(`/user/${reply.userId}`)"
                      />
                      <div class="flex-1 min-w-0">
                        <div class="flex items-center gap-2 mb-1 flex-wrap">
                          <span class="text-xs font-medium text-[#1f2937] cursor-pointer hover:text-[#6366f1]" @click="$router.push(`/user/${reply.userId}`)">{{ reply.userName }}</span>
                          <span v-if="reply.replyToUserName" class="text-xs text-[#6b7280]">回复 <span class="text-[#6366f1]">@{{ reply.replyToUserName }}</span></span>
                          <span class="text-xs text-[#9ca3af] ml-auto">{{ formatTime(reply.createTime) }}</span>
                        </div>
                        <p v-if="reply.content && reply.content.trim()" class="text-xs text-[#4b5563] leading-relaxed mb-2">{{ reply.content }}</p>
                        <div v-if="reply.imageUrls && reply.imageUrls.length" class="flex flex-wrap gap-2 mb-2">
                          <a v-for="(iu, ii) in reply.imageUrls" :key="ii" :href="resolveFileUrl(iu)" target="_blank" class="block w-20 h-20 rounded overflow-hidden border border-[#e5e7eb]">
                            <img :src="resolveFileUrl(iu)" class="w-full h-full object-cover" alt="" referrerpolicy="no-referrer" />
                          </a>
                        </div>
                        <div class="flex items-center gap-3 text-xs text-[#6b7280]">
                          <button type="button" @click="showReplyInput(comment, reply)" class="hover:text-[#6366f1] transition-colors">回复</button>
                          <button type="button" @click="handleLikeComment(reply)" class="flex items-center gap-1 hover:text-red-500 transition-colors">
                            <i :class="['fa fa-heart transition-colors', reply.isLiked ? 'text-red-500' : '']"></i>
                            <span :class="reply.isLiked ? 'text-red-500' : ''">{{ reply.likeCount }}</span>
                          </button>
                          <button v-if="canDelete(reply.userId)" type="button" class="hover:text-red-500 transition-colors" @click="deleteComment(reply.id)">删除</button>
                        </div>
                        <div v-if="replyToId === reply.id" class="mt-3 flex gap-2 flex-wrap">
                          <input v-model="replyText" :placeholder="`回复 ${reply.userName}...`"
                            class="flex-1 min-w-[10rem] px-3 py-1.5 rounded-xl border border-[#e5e7eb] bg-white text-xs outline-none focus:border-[#818cf8]" />
                          <el-button size="small" type="primary" @click="submitReply(comment, reply)">回复</el-button>
                          <el-button size="small" @click="replyToId = 0">取消</el-button>
                        </div>
                      </div>
                    </div>
                  </div>
                  <button v-if="comment.replies && comment.replies.length > 0" type="button" class="text-xs text-[#6366f1] hover:underline mt-3">展开 {{ comment.replies.length }} 条回复</button>
                </div>
              </div>

              <div v-if="commentTotal > 10" class="pt-6 flex justify-center">
                <el-pagination small layout="prev, pager, next" :total="commentTotal" :page-size="10" :current-page="commentPage" @current-change="loadComments" />
              </div>
            </div>
          </div>
        </div>

        <aside class="w-full lg:w-72 flex-shrink-0 space-y-6">
          <div class="user-surface-card p-5">
            <div class="flex items-center gap-3">
              <img
                :src="avatarDisplayUrl(post?.authorAvatar, defaultAvatar)"
                referrerpolicy="no-referrer"
                class="w-14 h-14 rounded-full object-cover border-2 border-[#e5e7eb]"
              />
              <div>
                <p class="font-medium text-[#1f2937]">{{ post?.isAnonymous ? '匿名用户' : post?.authorName }}</p>
                <p class="text-xs text-[#6b7280] mt-0.5">发布于 {{ formatTime(post?.createTime || '') }}</p>
              </div>
            </div>
            <div v-if="!post?.isAnonymous && !isAuthor" class="mt-4">
              <button type="button" @click="handleFollowAuthor" :class="['w-full py-2.5 rounded-xl text-sm font-medium transition-all', isFollowing ? 'border border-[#e5e7eb] bg-[#f9fafb] text-[#6b7280]' : 'btn-gradient']">
                {{ isFollowing ? '已关注' : '关注作者' }}
              </button>
            </div>
          </div>
        </aside>
      </div>
    </template>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '../../../store/user'
import { postApi, commentApi, interactApi, userApi, messageApi, fileApi, aiApi } from '../../../api'
import type { PostDetail, Comment } from '../../../types'
import { renderMarkdownToHtml, parsePostImageList } from '../../../utils/renderMarkdown'
import { resolveFileUrl, avatarDisplayUrl } from '../../../utils/resolveFileUrl'

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
const commentPics = ref<{ fileId: string; preview: string }[]>([])
const commentImageInputRef = ref<HTMLInputElement | null>(null)
const submitting = ref(false)
const replyToId = ref(0)
const replyText = ref('')
const aiLoading = ref(false)
const aiResult = ref('')
const aiLastAction = ref<'summary' | 'keypoints' | 'reply' | ''>('')

// 举报相关
const showReportDialog = ref(false)
const reportReason = ref('')
const reportDescription = ref('')
const reportTypes = [
  { value: 'spam', label: '垃圾广告' },
  { value: 'abuse', label: '人身攻击' },
  { value: 'copyright', label: '版权问题' },
  { value: 'illegal', label: '违法内容' },
  { value: 'other', label: '其他' },
]

const sharePmDialogVisible = ref(false)
const sharePmTab = ref<'following' | 'followers'>('following')
const sharePmFollowing = ref<{ userId: number; nickname?: string; username?: string; avatar?: string }[]>([])
const sharePmFollowers = ref<{ userId: number; nickname?: string; username?: string; avatar?: string }[]>([])
const sharePmSelectedId = ref<number | null>(null)
const sharePmLoading = ref(false)

const currentShareUserOptions = computed(() =>
  sharePmTab.value === 'following' ? sharePmFollowing.value : sharePmFollowers.value
)

watch(sharePmTab, () => {
  sharePmSelectedId.value = null
})

const postId = computed(() => {
  const raw = route.params.id
  const s = raw == null ? '' : String(raw)
  if (s === '' || s === 'NaN' || !/^\d+$/.test(s)) {
    return 0
  }
  const n = Number(s)
  return Number.isFinite(n) && n > 0 ? n : 0
})
const detailImages = computed(() => {
  const p = post.value as any
  if (!p) return []
  if (p.media && Array.isArray(p.media) && p.media.length) {
    return p.media
      .filter((m: any) => m.mediaType === 1)
      .map((m: any) => m.downloadPath || (m.fileId != null ? `/api/file/download/${m.fileId}` : ''))
      .filter(Boolean)
  }
  return parsePostImageList(p.images)
})
const detailVideo = computed(() => {
  const p = post.value as any
  if (!p) return ''
  if (p.media && Array.isArray(p.media) && p.media.length) {
    const v = p.media.find((m: any) => m.mediaType === 2)
    if (v) return v.downloadPath || (v.fileId != null ? `/api/file/download/${v.fileId}` : '')
  }
  return p.video || ''
})

function isVideoFile(path: string) {
  if (!path) return false
  const l = path.toLowerCase()
  return l.includes('/file/download/') || l.endsWith('.mp4') || l.endsWith('.webm') || l.endsWith('.mov') || l.includes('video')
}
const isAuthor = computed(() => String(post.value?.authorId) === userStore.userId)

onMounted(() => {
  if (postId.value <= 0) {
    ElMessage.error('无效的帖子链接')
    router.push('/')
    return
  }
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
    }
    // 检查是否关注了作者
    if (userStore.isLoggedIn && !post.value!.isAnonymous) {
      try { 
        const followed: any = await userApi.isFollowing(post.value!.authorId)
        isFollowing.value = followed
      } catch {
        /* 关注状态非关键，忽略 */
      }
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
      } catch {
        /* 点赞/收藏状态非关键，忽略 */
      }
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
      imageUrls: c.imageUrls || [],
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
        imageUrls: r.imageUrls || [],
      }))
    }))
    commentTotal.value = res?.total || 0
  } catch {
    /* 由全局拦截器处理或静默 */
  } finally {
    commentLoading.value = false
  }
}

function renderContent(content: string): string {
  return renderMarkdownToHtml(content || '')
}

function aiPayload() {
  return {
    postTitle: post.value?.title || '',
    content: post.value?.content || '',
  }
}

async function handleAiSummary() {
  if (!post.value) return
  aiLoading.value = true
  try {
    const text = await aiApi.summarizePost(aiPayload())
    aiResult.value = String(text || '').trim()
    aiLastAction.value = 'summary'
    ElMessage.success('摘要生成完成')
  } finally {
    aiLoading.value = false
  }
}

async function handleAiKeypoints() {
  if (!post.value) return
  aiLoading.value = true
  try {
    const text = await aiApi.keypointsPost(aiPayload())
    aiResult.value = String(text || '').trim()
    aiLastAction.value = 'keypoints'
    ElMessage.success('重点解读完成')
  } finally {
    aiLoading.value = false
  }
}

async function handleAiReplySuggestions() {
  if (!post.value) return
  aiLoading.value = true
  try {
    const text = await aiApi.replySuggestions(aiPayload())
    aiResult.value = String(text || '').trim()
    aiLastAction.value = 'reply'
    ElMessage.success('回复建议已生成')
  } finally {
    aiLoading.value = false
  }
}

function copyAiResult() {
  const text = (aiResult.value || '').trim()
  if (!text) return
  navigator.clipboard.writeText(text).then(() => {
    ElMessage.success('已复制 AI 结果')
  }).catch(() => {
    ElMessage.warning('复制失败，请手动复制')
  })
}

function fillCommentWithAi() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录后评论')
    return
  }
  const text = (aiResult.value || '').trim()
  if (!text) return
  commentText.value = text
  ElMessage.success('已填入评论框')
}

async function retryAiWithStyle(style: '简洁' | '活泼' | '正式') {
  if (!post.value || !aiResult.value.trim()) return
  aiLoading.value = true
  try {
    const modeMap: Record<string, string> = {
      summary: '语气调整',
      keypoints: '亮点优化',
      reply: '语气调整',
    }
    const mode = modeMap[aiLastAction.value] || '语气调整'
    const text = await aiApi.polishPost({
      boardName: post.value.sectionName || '社区交流',
      content: aiResult.value,
      polishMode: mode,
      style,
    })
    aiResult.value = String(text || '').trim() || aiResult.value
    aiLastAction.value = 'keypoints'
    ElMessage.success(`已调整为${style}风格`)
  } finally {
    aiLoading.value = false
  }
}

async function submitAiFeedback(type: 'like' | 'dislike') {
  if (!aiResult.value.trim()) return
  let reason = ''
  if (type === 'dislike') {
    try {
      const ret = await ElMessageBox.prompt('请输入不满意原因（如：太啰嗦/不够亲切/信息不全）', 'AI反馈', {
        confirmButtonText: '提交',
        cancelButtonText: '取消',
        inputPlaceholder: '可选填',
      })
      reason = (ret.value || '').trim()
    } catch {
      return
    }
  }
  try {
    await aiApi.feedback({
      feedbackType: type,
      feedbackReason: reason || undefined,
      scene: 'detail',
      operation: aiLastAction.value || 'unknown',
    })
    ElMessage.success(type === 'like' ? '感谢反馈' : '已记录，将持续优化')
  } catch {
    ElMessage.warning('反馈提交失败，请稍后重试')
  }
}

function formatShareUserLabel(u: { nickname?: string; username?: string; userId?: number }) {
  return (u.nickname || u.username || `用户${u.userId || ''}`).trim()
}

async function loadShareRecipients() {
  if (!userStore.isLoggedIn) return
  const uid = Number(userStore.userId)
  if (!uid) return
  sharePmLoading.value = true
  try {
    const [f1, f2] = await Promise.all([
      userApi.following(uid).catch(() => []),
      userApi.followers(uid).catch(() => []),
    ])
    const filterSelf = (arr: any[]) =>
      (arr || []).filter((u: any) => u && Number(u.userId) !== uid)
    sharePmFollowing.value = filterSelf(Array.isArray(f1) ? f1 : [])
    sharePmFollowers.value = filterSelf(Array.isArray(f2) ? f2 : [])
  } finally {
    sharePmLoading.value = false
  }
}

function openSharePmDialog() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }
  sharePmTab.value = 'following'
  sharePmSelectedId.value = null
  sharePmDialogVisible.value = true
}

async function sendSharePm() {
  const uid = sharePmSelectedId.value
  if (!uid || uid <= 0) {
    ElMessage.warning('请选择要分享的用户')
    return
  }
  if (!post.value) return
  try {
    await messageApi.send({
      toUserId: uid,
      sharePostId: postId.value,
      sharePostTitle: post.value.title,
    })
    ElMessage.success('已发送分享卡片')
    sharePmDialogVisible.value = false
    sharePmSelectedId.value = null
    router.push({ path: '/message', query: { toUserId: String(uid), toNickName: '' } })
  } catch {
    ElMessage.error('发送失败')
  }
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
      await ElMessageBox.confirm('确定要取消关注该用户吗？', '取消关注', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      await userApi.unfollow(authorId)
      isFollowing.value = false
      ElMessage.success('已取消关注')
    } else {
      await userApi.follow(authorId)
      isFollowing.value = true
      ElMessage.success('关注成功')
    }
  } catch (e: any) {
    if (e !== 'cancel') {
      console.error('操作失败', e)
    }
  }
}

async function onCommentPics(e: Event) {
  const input = e.target as HTMLInputElement
  const files = input.files
  if (!files?.length) return
  input.value = ''
  for (const file of Array.from(files)) {
    if (!file.type.startsWith('image/')) continue
    if (file.size > 10 * 1024 * 1024) {
      ElMessage.warning('单张评论图不超过 10MB')
      continue
    }
    try {
      const res: any = await fileApi.uploadImage(file, undefined, 'community/images/comment')
      const id = res?.id
      if (id == null) {
        ElMessage.warning('上传未返回文件 ID，请重试')
        continue
      }
      const sid = String(id).trim()
      const url = `/api/file/download/${sid}`
      commentPics.value.push({ fileId: sid, preview: resolveFileUrl(url) || url })
    } catch (e: any) {
      ElMessage.error(e?.message || '图片上传失败')
    }
  }
}

async function submitComment() {
  const content = commentText.value.trim()
  if (!content && !commentPics.value.length) {
    ElMessage.warning('请输入评论或添加配图')
    return
  }
  submitting.value = true
  try {
    const ids = commentPics.value.map((p) => String(p.fileId).trim())
    await commentApi.create({
      postId: postId.value,
      content: content || undefined,
      imageFileIds: ids.length > 0 ? ids : undefined,
    })
    commentText.value = ''
    commentPics.value = []
    ElMessage.success('评论成功')
    loadComments()
  } catch (e: any) {
    // 业务/网络错误已由 request 拦截器弹窗；仅补充控制台便于排查
    console.error('评论失败', e)
  } finally {
    submitting.value = false
  }
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

async function submitReport() {
  if (!reportReason.value) {
    ElMessage.warning('请选择举报类型')
    return
  }
  try {
    await postApi.report(postId.value, reportReason.value, reportDescription.value)
    ElMessage.success('举报已提交，感谢您的反馈')
    showReportDialog.value = false
    reportReason.value = ''
    reportDescription.value = ''
  } catch (e: any) {
    console.error('举报失败', e)
    ElMessage.error('举报失败，请稍后重试')
  }
}

function handleShare() {
  const url = window.location.href
  navigator.clipboard.writeText(url).then(() => {
    ElMessage.success('链接已复制到剪贴板')
  }).catch(() => {
    ElMessage.warning('复制失败，请手动复制')
  })
}

function goMessage() {
  if (post.value && !post.value.isAnonymous) {
    router.push({
      path: '/chat',
      query: { userId: String(post.value.authorId), nickName: post.value.authorName || '' },
    })
  }
}
</script>

<style scoped>
/* 自定义样式 */
.container {
  max-width: 1280px;
  margin: 0 auto;
}

.post-md-light.prose h2 {
  color: #1f2937 !important;
  font-size: 1.5rem !important;
  margin-top: 1.5rem !important;
  margin-bottom: 1rem !important;
}

.post-md-light.prose h3 {
  color: #1f2937 !important;
  font-size: 1.25rem !important;
  margin-top: 1.25rem !important;
  margin-bottom: 0.75rem !important;
}

.post-md-light.prose p {
  color: #374151 !important;
  margin-bottom: 1rem !important;
}

.post-md-light.prose strong {
  color: #111827 !important;
  font-weight: 600 !important;
}

.post-md-light.prose a {
  color: #6366f1 !important;
}

.post-md :deep(.md-code-block) {
  white-space: pre-wrap;
  word-break: break-word;
}

.post-md :deep(.md-img) {
  max-height: 24rem;
}

.share-pm-tabs :deep(.el-tabs__item.is-active) {
  color: var(--el-color-primary);
}

.glass-input :deep(.el-select__wrapper),
.glass-input :deep(.el-input__wrapper) {
  background-color: #f9fafb !important;
  border: 1px solid #e5e7eb !important;
  box-shadow: none !important;
}

.ai-op-btn {
  padding: 0.25rem 0.625rem;
  border-radius: 9999px;
  font-size: 0.75rem;
  color: #4f46e5;
  background: #eef2ff;
  border: 1px solid #c7d2fe;
}

.ai-op-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>
