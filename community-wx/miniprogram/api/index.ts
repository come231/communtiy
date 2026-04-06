import { request } from '../utils/request'

// 帖子相关接口
export const postApi = {
  // 帖子列表
  list(params?: { pageNum?: number; pageSize?: number; sectionId?: number; topicId?: number }) {
    return request.get('/community/posts/list', params)
  },
  
  // 热门帖子
  hot(pageNum = 1, pageSize = 10) {
    return request.get('/community/posts/hot', { pageNum, pageSize })
  },
  
  // 帖子详情
  detail(id: number) {
    return request.get(`/community/posts/${id}`)
  },
  
  // 发布帖子
  create(data: { title: string; content: string; sectionId?: number; topicIds?: number[]; isAnonymous?: boolean }) {
    return request.post('/community/posts', data)
  },
  
  // 更新帖子
  update(id: number, data: any) {
    return request.put(`/community/posts/${id}`, data)
  },
  
  // 删除帖子
  delete(id: number) {
    return request.delete(`/community/posts/${id}`)
  },
  
  // 点赞帖子
  like(id: number) {
    return request.post(`/community/interact/likes/1/${id}`)
  },
  
  // 取消点赞
  unlike(id: number) {
    return request.delete(`/community/interact/likes/1/${id}`)
  },
  
  // 收藏帖子
  collect(id: number) {
    return request.post('/community/interact/collects', null, true)
  },
  
  // 取消收藏
  uncollect(id: number) {
    return request.delete(`/community/interact/collects/${id}`)
  },
}

// 评论相关接口
export const commentApi = {
  // 评论列表
  list(postId: number, pageNum = 1, pageSize = 20) {
    return request.get('/community/comments', { postId, pageNum, pageSize })
  },
  
  // 发布评论
  create(data: { postId: number; content: string; parentId?: number }) {
    return request.post('/community/comments', data)
  },
  
  // 删除评论
  delete(id: number) {
    return request.delete(`/community/comments/${id}`)
  },
}

// 用户相关接口
export const userApi = {
  // 登录
  login(data: { username: string; password: string }) {
    return request.post('/user/login', data)
  },
  
  // 注册
  register(data: { username: string; password: string; nickname?: string }) {
    return request.post('/user/register', data)
  },
  
  // 用户信息
  info(userId?: number) {
    if (userId) {
      return request.get('/user/user/info', { userId })
    }
    return request.get('/user/user/my')
  },
  
  // 更新用户信息
  update(data: { nickname?: string; bio?: string; avatar?: string }) {
    return request.put('/user/user/update', data)
  },
  
  // 关注用户
  follow(userId: number) {
    return request.post(`/user/user/follow/${userId}`)
  },
  
  // 取消关注
  unfollow(userId: number) {
    return request.delete(`/user/user/follow/${userId}`)
  },
  
  // 签到
  checkin() {
    return request.post('/user/user/checkin')
  },
  
  // 签到状态
  checkinStatus() {
    return request.get('/user/user/checkin/status')
  },
}

// 板块话题相关接口
export const sectionApi = {
  // 板块列表
  list() {
    return request.get('/community/sections')
  },
  
  // 话题列表
  topics() {
    return request.get('/community/topics')
  },
  
  // 热门话题
  hotTopics() {
    return request.get('/community/topics/hot')
  },
}

// 消息相关接口
export const messageApi = {
  // 通知列表
  notices(params?: { pageNum?: number; pageSize?: number; type?: number }) {
    return request.get('/message/notices', params)
  },
  
  // 未读数
  unreadCount() {
    return request.get('/message/notices/unread-count')
  },
  
  // 标记已读
  markRead(id: number) {
    return request.put(`/message/notices/${id}/read`)
  },
  
  // 私信会话列表
  conversations() {
    return request.get('/message/messages/conversations')
  },
  
  // 私信历史
  history(userId: number, pageNum = 1, pageSize = 20) {
    return request.get('/message/messages/history', { userId, pageNum, pageSize })
  },
  
  // 发送私信
  send(data: { toUserId: number; content: string }) {
    return request.post('/message/messages/send', data)
  },
}

// AI相关接口
export const aiApi = {
  // 发送消息
  chat(data: { content: string; chatId?: number }) {
    return request.post('/ai/ai/chat/send', data)
  },
  
  // 对话历史
  messages(chatId: number) {
    return request.get(`/ai/ai/chats/${chatId}/messages`)
  },
  
  // 对话列表
  chats() {
    return request.get('/ai/ai/chats')
  },
  
  // AI写作
  write(data: { content: string; style?: string }) {
    return request.post('/ai/ai/write', data)
  },
  
  // AI润色
  polish(content: string) {
    return request.post('/ai/ai/polish', { content })
  },
  
  // AI总结
  summarize(content: string) {
    return request.post('/ai/ai/summarize', { content })
  },
}

// 搜索相关接口
export const searchApi = {
  // 搜索
  all(keyword: string, pageNum = 1) {
    return request.get('/search/search/all', { keyword, pageNum })
  },
  
  // 热门搜索
  hot() {
    return request.get('/search/search/hot')
  },
  
  // 搜索历史
  history() {
    return request.get('/search/search/history')
  },
}
