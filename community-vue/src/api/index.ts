import request from '../utils/request'
import type { Post, PostDetail, PostPublishResult, AiCheckRecord, Comment, Section, Topic, PageResult, UserInfo, Notice, Conversation, PrivateMessage, SearchResult, HotSearch, DashboardStats, AdminUser, AdminPost, SensitiveWord, SystemConfig, PostReviewRule } from '../types'

// ==================== 帖子 API ====================
export const postApi = {
  // 帖子列表
  list(params?: { keyword?: string; sectionId?: number; topicId?: number; sort?: string; pageNum?: number; pageSize?: number }) {
    return request.get<PageResult<Post>>('/posts/list', { params })
  },
  // 热门帖子
  hot(pageNum = 1, pageSize = 10) {
    return request.get<PageResult<Post>>('/posts/hot', { params: { pageNum, pageSize } })
  },
  // 帖子详情
  detail(id: number) {
    return request.get<PostDetail>(`/posts/${id}`)
  },
  // 创建帖子
  create(data: {
    title: string
    content: string
    images?: string
    coverImage?: string
    coverFileId?: string
    video?: string
    media?: { fileId: string; mediaType: number; sortOrder: number }[]
    sectionId?: number
    isAnonymous?: boolean
    isPublic?: boolean
    topicIds?: number[]
  }) {
    return request.post<PostPublishResult>('/posts', data)
  },
  // 更新帖子（传 media 数组可替换图库；传空数组清空图库）
  update(
    id: number,
    data: {
      title: string
      content: string
      images?: string
      coverImage?: string
      coverFileId?: string
      video?: string
      media?: { fileId: string; mediaType: number; sortOrder: number }[]
      sectionId?: number
      isAnonymous?: boolean
      isPublic?: boolean
      topicIds?: number[]
      tags?: string
    }
  ) {
    return request.put(`/posts/${id}`, data)
  },
  // 删除帖子
  delete(id: number) {
    return request.delete(`/posts/${id}`)
  },
  // 草稿
  saveDraft(data: {
    draftId?: number
    title?: string
    content?: string
    images?: string
    coverImage?: string
    sectionId?: number
    topicIds?: number[]
    isAnonymous?: boolean
  }) {
    return request.post<number>('/posts/drafts/save', data)
  },
  drafts(pageNum = 1, pageSize = 10) {
    return request.get<PageResult<any>>('/posts/drafts', { params: { pageNum, pageSize } })
  },
  draftDetail(draftId: number) {
    return request.get<any>(`/posts/drafts/${draftId}`)
  },
  deleteDraft(id: number) {
    return request.delete(`/posts/drafts/${id}`)
  },
  report(postId: number, reason: string, description?: string) {
    return request.post(`/posts/${postId}/report`, { reason, description })
  },
  my(pageNum = 1, pageSize = 20, status?: number) {
    const params: Record<string, unknown> = { pageNum, pageSize }
    if (status != null) params.status = status
    return request.get<PageResult<Post>>('/posts/my', { params })
  },
  recommended(pageNum = 1, pageSize = 10) {
    return request.get<PageResult<Post>>('/posts/recommended', { params: { pageNum, pageSize } })
  },
}

// ==================== 评论 API ====================
export const commentApi = {
  list(postId: number, pageNum = 1, pageSize = 10) {
    return request.get<PageResult<Comment>>('/comments', { params: { postId, pageNum, pageSize } })
  },
  create(data: { postId: number; content?: string; parentId?: number; replyToUserId?: number; imageFileIds?: string[] }) {
    return request.post<number>('/comments', data)
  },
  delete(id: number) {
    return request.delete(`/comments/${id}`)
  },
}

// ==================== 互动 API (点赞/收藏/关注) ====================
export const interactApi = {
  // 点赞
  isLiked(type: number, targetId: number) {
    return request.get<boolean>('/likes/check', { params: { type, targetId } })
  },
  like(type: number, targetId: number) {
    return request.post(`/likes/${type}/${targetId}`)
  },
  unlike(type: number, targetId: number) {
    return request.delete(`/likes/${type}/${targetId}`)
  },
  // 收藏
  isCollected(postId: number) {
    return request.get<boolean>(`/collects/check/${postId}`)
  },
  collect(postId: number, folderId?: number) {
    return request.post('/collects', { postId, folderId })
  },
  uncollect(postId: number) {
    return request.delete(`/collects/${postId}`)
  },
  collects(pageNum = 1, pageSize = 10, folderId?: number) {
    return request.get<PageResult<any>>('/collects', { params: { pageNum, pageSize, folderId } })
  },
  userCollects(userId: number, pageNum = 1, pageSize = 10) {
    return request.get<PageResult<any>>(`/collects/user/${userId}`, { params: { pageNum, pageSize } })
  },
  toggleCollectHidden(id: number, isHidden: number) {
    return request.put(`/collects/${id}/hidden`, { isHidden })
  },
  // 收藏夹
  folders() {
    return request.get<any[]>('/folders')
  },
  createFolder(name: string) {
    return request.post('/folders', { name })
  },
  deleteFolder(id: number) {
    return request.delete(`/folders/${id}`)
  },
}

// ==================== 板块/话题 API ====================
export const sectionApi = {
  list() {
    return request.get<Section[]>('/sections')
  },
  parents() {
    return request.get<Section[]>('/sections/parents')
  },
  detail(id: number) {
    return request.get<Section>(`/sections/${id}`)
  },
  create(data: { name: string; intro?: string; icon?: string; status?: number; categoryId?: number | null; customCategory?: string }) {
    return request.post('/sections', data)
  },
  categories() {
    return request.get<{ categoryId: number; name: string; sort: number }[]>('/sections/categories')
  },
  userSections() {
    return request.get<Section[]>('/sections/user')
  },
  joinedSections() {
    return request.get<Section[]>('/sections/user/joined')
  },
  /** 板块视图：成员数、是否可发帖、申请状态等 */
  context(id: number) {
    return request.get<{
      section: Section
      memberCount: number
      moderator: boolean
      member: boolean
      joinPending: boolean
      canPost: boolean
    }>(`/sections/${id}/context`)
  },
  members(sectionId: number, pageNum = 1, pageSize = 30) {
    return request.get<PageResult<{ userId: number; role: number; joinTime?: string }>>(`/sections/${sectionId}/members`, {
      params: { pageNum, pageSize },
    })
  },
  applyJoin(sectionId: number) {
    return request.post(`/sections/${sectionId}/join`)
  },
  joinRequests(sectionId: number) {
    return request.get<any[]>(`/sections/${sectionId}/join-requests`)
  },
  approveJoin(sectionId: number, userId: number) {
    return request.post(`/sections/${sectionId}/join-requests/${userId}/approve`)
  },
  rejectJoin(sectionId: number, userId: number) {
    return request.post(`/sections/${sectionId}/join-requests/${userId}/reject`)
  },
  kickMember(sectionId: number, userId: number) {
    return request.delete(`/sections/${sectionId}/members/${userId}`)
  },
}

export const topicApi = {
  list() {
    return request.get<Topic[]>('/topics')
  },
  /** 分页搜索话题（名称/简介） */
  search(keyword: string, pageNum = 1, pageSize = 10) {
    return request.get<PageResult<Topic>>('/topics/keyword-search', { params: { keyword, pageNum, pageSize } })
  },
  hot() {
    return request.get<Topic[]>('/topics/hot')
  },
  detail(id: number) {
    return request.get<Topic>(`/topics/${id}`)
  },
  follow(id: number) {
    return request.post(`/topics/${id}/follow`)
  },
  unfollow(id: number) {
    return request.delete(`/topics/${id}/follow`)
  },
  isFollowed(id: number) {
    return request.get<boolean>(`/topics/${id}/isFollowed`)
  },
}

// ==================== 用户 API ====================
export const userApi = {
  info(userId: number) {
    return request.get<UserInfo>('/user/info', { params: { userId } })
  },
  my() {
    return request.get<UserInfo>('/user/my')
  },
  update(data: any) {
    return request.put<UserInfo>('/user/update', data)
  },
  search(keyword: string, pageNum = 1, pageSize = 10) {
    return request.get<PageResult<UserInfo>>('/user/search', { params: { keyword, pageNum, pageSize } })
  },
  follow(userId: number) {
    return request.post(`/user/follow/${userId}`)
  },
  unfollow(userId: number) {
    return request.delete(`/user/follow/${userId}`)
  },
  following(userId: number) {
    return request.get<any[]>(`/user/following/${userId}`)
  },
  followers(userId: number) {
    return request.get<any[]>(`/user/followers/${userId}`)
  },
  isFollowing(userId: number) {
    return request.get<boolean>(`/user/isFollowing/${userId}`)
  },
  checkin() {
    return request.post<any>('/user/checkin')
  },
  checkinStatus() {
    return request.get<any>('/user/checkin/status')
  },
  // 个人标签
  getTags() {
    return request.get<string[]>('/user/tags')
  },
  updateTags(tags: string[]) {
    return request.put<void>('/user/tags', tags)
  },
  // 隐私设置
  getPrivacy() {
    return request.get<{ hideLikes: boolean; hideCollects: boolean }>('/user/privacy')
  },
  updatePrivacy(settings: { hideLikes?: number; hideCollects?: number }) {
    return request.put<void>('/user/privacy', settings)
  },
  // 喜欢列表
  getMyLikedPosts(pageNum = 1, pageSize = 10) {
    return request.get<PageResult<any>>('/user/likes', { params: { pageNum, pageSize } })
  },
  getUserLikedPosts(userId: number, pageNum = 1, pageSize = 10) {
    return request.get<PageResult<any>>(`/user/${userId}/likes`, { params: { pageNum, pageSize } })
  },
}

// ==================== 消息通知 API ====================
export const noticeApi = {
  list(params?: { type?: number; pageNum?: number; pageSize?: number }) {
    return request.get<PageResult<Notice>>('/notice/list', { params })
  },
  unreadCount() {
    return request.get<number>('/notice/unread-count')
  },
  markRead(id: number) {
    return request.put(`/notice/${id}/read`)
  },
  markAllRead() {
    return request.put('/notice/read-all')
  },
  delete(id: number) {
    return request.delete(`/notice/${id}`)
  },
}

export const messageApi = {
  // 会话列表
  conversations() {
    return request.get<Conversation[]>('/chat/users')
  },
  // 聊天记录
  history(userId: number, pageNum = 1, pageSize = 20) {
    return request.get<PageResult<PrivateMessage>>('/chat/list', { params: { otherUserId: userId, page: pageNum, size: pageSize } })
  },
  // 发送私信
  send(data: { toUserId: number; content?: string; sharePostId?: number; sharePostTitle?: string }) {
    return request.post<PrivateMessage>('/chat/send', data)
  },
  // 未读数
  unreadCount() {
    return request.get<number>('/chat/unread-count')
  },
  markRead(fromUserId: number) {
    return request.put('/chat/read', null, { params: { fromUserId } })
  },
}

// ==================== AI对话 API ====================
export const aiApi = {
  // 对话会话列表
  chats() {
    return request.get<any[]>('/ai/chat/sessions')
  },
  // 创建会话
  createChat(title?: string) {
    return request.post<string>('/ai/chat/new-session', { title })
  },
  // 删除会话
  deleteChat(id: number) {
    return request.delete(`/ai/chat/session/${id}`)
  },
  // 聊天记录
  messages(sessionId: string) {
    return request.get<any>(`/ai/chat/history`, { params: { sessionId } })
  },
  // 发送消息
  send(data: { sessionId?: string; content: string; type?: string }) {
    return request.post<any>('/ai/chat', data)
  },
  // 快捷功能
  write(params: { content: string; style?: string }) {
    return request.post<string>('/ai/write/article', params)
  },
  polish(content: string) {
    return request.post<string>('/ai/write/polish', { content })
  },
  summarize(content: string) {
    return request.post<string>('/ai/write/summarize', { content })
  },
  check(content: string) {
    return request.post<any>('/ai/review/check', { content })
  },
}

// ==================== 搜索 API ====================
export const searchApi = {
  all(keyword: string, pageNum = 1, pageSize = 10) {
    return request.get<SearchResult>('/search/posts', { params: { keyword, page: pageNum, size: pageSize } })
  },
  posts(keyword: string, pageNum = 1, pageSize = 10) {
    return request.get<PageResult<Post>>('/search/posts', { params: { keyword, page: pageNum, size: pageSize } })
  },
  users(keyword: string, pageNum = 1, pageSize = 10) {
    return request.get<PageResult<UserInfo>>('/user/search', { params: { keyword, pageNum, pageSize } })
  },
  hot() {
    return request.get<HotSearch[]>('/search/hot-keywords')
  },
  history() {
    return request.get<string[]>('/search/history')
  },
  addHistory(keyword: string) {
    return request.post('/search/history', { keyword })
  },
  clearHistory() {
    return request.post('/search/clear-history')
  },
}

// ==================== 文件上传 API ====================
export const fileApi = {
  /**
   * @param biz OSS 对象键前缀
   * @param naming `avatar` 时对象键为 {biz}/{userId}{ext} 并覆盖同用户历史；需登录（X-User-Id 或 uploaderId）
   */
  upload(
    formData: FormData,
    onProgress?: (percent: number) => void,
    biz?: string,
    naming?: 'avatar'
  ) {
    const q = new URLSearchParams()
    if (biz) {
      q.set('biz', biz)
    }
    if (naming) {
      q.set('naming', naming)
    }
    // 与请求头 X-User-Id 双通道：网关头未透传时仍能把上传者写入 file_record，否则 userId=0 会导致评论「仅可使用本人上传的图片」失败
    const rawUid = typeof localStorage !== 'undefined' ? localStorage.getItem('userId') : null
    if (rawUid) {
      const n = Number(rawUid)
      if (Number.isFinite(n) && n > 0) {
        q.set('uploaderId', String(Math.floor(n)))
      }
    }
    const path = q.toString() ? `/file/upload?${q.toString()}` : '/file/upload'
    // 不手写 Content-Type，交给浏览器/axios 为 FormData 自动带 boundary，避免无 boundary 导致异常或元数据丢失
    return request.post<{ id: string; url: string; filePath?: string; mimeType?: string }>(path, formData, {
      onUploadProgress: (e: { loaded: number; total?: number }) =>
        onProgress?.(Math.round((e.loaded * 100) / (e.total || 1))),
    })
  },
  uploadImage(
    file: File,
    onProgress?: (percent: number) => void,
    /** 例如 community/images/avatar，对应 OSS 中该目录下按日期分桶 */
    biz?: string
  ) {
    const formData = new FormData()
    formData.append('file', file)
    return fileApi.upload(formData, onProgress, biz)
  },
  download(id: number) {
    return request.get(`/file/download/${id}`, { responseType: 'blob' })
  },
  info(id: number) {
    return request.get<any>(`/file/info/${id}`)
  },
}

// ==================== 管理后台 API ====================
export const adminApi = {
  // 仪表盘
  dashboard() {
    return request.get<DashboardStats>('/dashboard/stats')
  },
  // 用户管理
  users(params: { keyword?: string; status?: number; page?: number; size?: number }) {
    return request.get<PageResult<AdminUser>>('/admin/users', { params })
  },
  updateUserStatus(userId: number, status: number) {
    if (!userId) {
      return Promise.reject(new Error('用户ID不能为空'))
    }
    if (status === 0) {
      return request.post(`/admin/users/${userId}/unban`)
    } else {
      return request.post(`/admin/users/${userId}/ban`, { days: 30, reason: '违规操作' })
    }
  },
  // 内容管理
  posts(params: { keyword?: string; status?: number; page?: number; size?: number }) {
    return request.get<PageResult<AdminPost>>('/admin/posts', { params })
  },
  postDetail(id: number) {
    return request.get<AdminPost>(`/admin/posts/${id}`)
  },
  reviewPost(id: number, status: number, reason?: string) {
    return request.put(`/admin/posts/${id}/review`, null, { params: { status, reason } })
  },
  deletePost(id: number) {
    return request.delete(`/admin/posts/${id}`)
  },
  // 举报管理
  reports(params: { type?: number; status?: number; page?: number; size?: number }) {
    return request.get<PageResult<Report>>('/admin/reports-manage', { params })
  },
  handleReport(id: number, status: number, result?: string) {
    return request.put(`/admin/reports-manage/${id}/handle`, { handleResult: result ?? '', status })
  },
  reportCount() {
    return request.get<Map<number, number>>('/admin/reports-manage/count')
  },
  // 敏感词管理
  sensitiveWords(params?: { keyword?: string; page?: number; size?: number }) {
    return request.get<PageResult<SensitiveWord>>('/admin/sensitive-words', { params })
  },
  addSensitiveWord(data: { word: string; level: number; category: string; replacement?: string }) {
    return request.post('/admin/sensitive-word', data)
  },
  updateSensitiveWord(id: number, data: any) {
    return request.put(`/admin/sensitive-word/${id}`, data)
  },
  deleteSensitiveWord(id: number) {
    return request.delete(`/admin/sensitive-word/${id}`)
  },
  batchImportSensitiveWords(words: string[]) {
    return request.post('/admin/sensitive-words/import', { words })
  },
  // 系统配置
  configs(groupKey?: string) {
    return request.get<SystemConfig[]>('/admin/configs', { params: { groupKey } })
  },
  /** 标准库为数字主键；旧版 sys_config 为配置键字符串（如 site.name） */
  updateConfig(id: number | string, value: string) {
    const seg = encodeURIComponent(String(id))
    return request.put(`/admin/configs/${seg}`, { configValue: value })
  },
  // 消息管理
  messages(params: { page?: number; size?: number; keyword?: string }) {
    return request.get<any>('/admin/messages', { params })
  },

  // 根据用户ID查询消息（新增）
  messagesByUserId(userId: number, params: { page?: number; size?: number }) {
    return request.get(`/admin/messages/user/${userId}`, { params })
  },

  // 删除消息（已修复）
  deleteMessage(id: number) {
    return request.delete(`/admin/messages/${id}`)
  },
  // 通知管理
  notices(params: { page?: number; size?: number; type?: number }) {
    return request.get<any>('/admin/notices', { params })
  },
  deleteNotice(id: number) {
    return request.delete(`/admin/notices/${id}`)
  },
  // AI管理
  aiConfig() {
    return request.get<any>('/admin/ai/config')
  },
  updateAiConfig(data: any) {
    return request.put('/admin/ai/config', data)
  },
  aiChatRecords(params: { page?: number; size?: number; keyword?: string }) {
    return request.get<any>('/admin/ai/chat-records', { params })
  },
  aiStats() {
    return request.get<any>('/admin/ai/stats')
  },
  /** 帖子 AI 审核规则 */
  postReviewRules() {
    return request.get<PostReviewRule[]>('/admin/ai/post-review-rules')
  },
  createPostReviewRule(data: Partial<PostReviewRule>) {
    return request.post<PostReviewRule>('/admin/ai/post-review-rules', data)
  },
  updatePostReviewRule(id: number, data: Partial<PostReviewRule>) {
    return request.put<PostReviewRule>(`/admin/ai/post-review-rules/${id}`, data)
  },
  deletePostReviewRule(id: number) {
    return request.delete(`/admin/ai/post-review-rules/${id}`)
  },
  /** AI 审核记录（内容追踪） */
  aiCheckRecords(params: { page?: number; size?: number; userId?: number; postId?: number; checkResult?: number }) {
    return request.get<PageResult<AiCheckRecord>>('/admin/ai-check-records', { params })
  },
  /** 人工审核反馈（数据飞轮） */
  aiCheckFeedback(checkId: number, data: { manualStatus: number; label?: string; remark?: string }) {
    return request.post(`/ai/internal/feedback/${checkId}`, null, { params: data })
  },
  /** 向量库统计 */
  aiVectorStats() {
    return request.get<{ totalSize: number; description: string }>('/ai/internal/vector-stats')
  },
}
