import request from '../utils/request'
import type { Post, PostDetail, Comment, Section, Topic, PageResult, UserInfo, Notice, Conversation, PrivateMessage, AiChat, AiMessage, SearchResult, HotSearch, DashboardStats, AdminUser, AdminPost, SensitiveWord, SystemConfig } from '../types'

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
  create(data: { title: string; content: string; summary?: string; coverImage?: string; sectionId?: number; isAnonymous?: boolean; topicIds?: number[] }) {
    return request.post<number>('/posts', data)
  },
  // 更新帖子
  update(id: number, data: any) {
    return request.put(`/posts/${id}`, data)
  },
  // 删除帖子
  delete(id: number) {
    return request.delete(`/posts/${id}`)
  },
  // 草稿
  saveDraft(data: any) {
    return request.post<number>('/posts/drafts/save', data)
  },
  drafts(pageNum = 1, pageSize = 10) {
    return request.get<PageResult<any>>('/posts/drafts', { params: { pageNum, pageSize } })
  },
  deleteDraft(id: number) {
    return request.delete(`/posts/drafts/${id}`)
  },
}

// ==================== 评论 API ====================
export const commentApi = {
  list(postId: number, pageNum = 1, pageSize = 10) {
    return request.get<PageResult<Comment>>('/comments', { params: { postId, pageNum, pageSize } })
  },
  create(data: { postId: number; content: string; parentId?: number; replyToUserId?: number }) {
    return request.post<number>('/comments', data)
  },
  delete(id: number) {
    return request.delete(`/comments/${id}`)
  },
}

// ==================== 互动 API (点赞/收藏) ====================
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
  create(data: { name: string; intro?: string; icon?: string }) {
    return request.post('/sections', data)
  },
}

export const topicApi = {
  list() {
    return request.get<Topic[]>('/topics')
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
    return request.get<number[]>(`/user/following/${userId}`)
  },
  followers(userId: number) {
    return request.get<number[]>(`/user/followers/${userId}`)
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
  send(data: { toUserId: number; content: string }) {
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
    return request.get<AiChat[]>('/ai/chat/sessions')
  },
  // 创建会话
  createChat(title?: string) {
    return request.post<AiChat>('/ai/chat/new-session', { title })
  },
  // 删除会话
  deleteChat(id: number) {
    return request.delete(`/ai/chat/session/${id}`)
  },
  // 聊天记录
  messages(chatId: number) {
    return request.get<AiMessage[]>(`/ai/chat/history`, { params: { sessionId: chatId } })
  },
  // 发送消息
  send(data: { sessionId?: number; content: string; type?: string }) {
    return request.post<string>('/ai/chat', data)
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
  upload(formData: FormData, onProgress?: (percent: number) => void) {
    return request.post<string>('/file/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      onUploadProgress: (e) => onProgress?.(Math.round((e.loaded * 100) / (e.total || 1))),
    })
  },
  uploadImage(file: File, onProgress?: (percent: number) => void) {
    const formData = new FormData()
    formData.append('file', file)
    return fileApi.upload(formData, onProgress)
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
    return request.put(`/admin/reports-manage/${id}/handle`, { status, result })
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
  updateConfig(id: number, value: string) {
    return request.put(`/admin/configs/${id}`, { configValue: value })
  },
  // 消息管理
  messages(params: { page?: number; size?: number; keyword?: string }) {
    return request.get('/admin/messages', { params })
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
}
