var request = require('../utils/request').request

// 帖子相关接口
var postApi = {
  // 帖子列表
  list: function(params) {
    return request.get('/posts/list', params)
  },
  
  // 热门帖子
  hot: function(pageNum, pageSize) {
    pageNum = pageNum || 1
    pageSize = pageSize || 10
    return request.get('/posts/hot', { pageNum: pageNum, pageSize: pageSize })
  },
  
  // 帖子详情
  detail: function(id) {
    return request.get('/posts/' + id)
  },
  
  // 发布帖子
  create: function(data) {
    return request.post('/posts', data)
  },
  
  // 更新帖子
  update: function(id, data) {
    return request.put('/posts/' + id, data)
  },
  
  // 删除帖子
  delete: function(id) {
    return request.delete('/posts/' + id)
  },
  
  // 点赞帖子
  like: function(id) {
    return request.post('/likes/1/' + id)
  },
  
  // 取消点赞
  unlike: function(id) {
    return request.delete('/likes/1/' + id)
  },
  
  // 收藏帖子
  collect: function(id) {
    return request.post('/collects', { postId: id })
  },
  
  // 取消收藏
  uncollect: function(id) {
    return request.delete('/collects/' + id)
  },
}

// 评论相关接口
var commentApi = {
  // 评论列表
  list: function(postId, pageNum, pageSize) {
    pageNum = pageNum || 1
    pageSize = pageSize || 20
    return request.get('/comments', { postId: postId, pageNum: pageNum, pageSize: pageSize })
  },
  
  // 发布评论
  create: function(data) {
    return request.post('/comments', data)
  },
  
  // 删除评论
  delete: function(id) {
    return request.delete('/comments/' + id)
  },
}

// 用户相关接口
var userApi = {
  // 登录
  login: function(data) {
    return request.post('/user/login', data)
  },
  
  // 注册
  register: function(data) {
    return request.post('/user/register', data)
  },
  
  // 用户信息
  info: function(userId) {
    if (userId) {
      return request.get('/user/info', { userId: userId })
    }
    return request.get('/user/my')
  },
  
  // 更新用户信息
  update: function(data) {
    return request.put('/user/update', data)
  },
  
  // 关注用户
  follow: function(userId) {
    return request.post('/user/follow/' + userId)
  },
  
  // 取消关注
  unfollow: function(userId) {
    return request.delete('/user/follow/' + userId)
  },
  
  // 签到
  checkin: function() {
    return request.post('/user/checkin')
  },
  
  // 签到状态
  checkinStatus: function() {
    return request.get('/user/checkin/status')
  },
}

// 板块话题相关接口
var sectionApi = {
  // 板块列表
  list: function() {
    return request.get('/sections')
  },
  
  // 话题列表
  topics: function() {
    return request.get('/topics')
  },
  
  // 热门话题
  hotTopics: function() {
    return request.get('/topics/hot')
  },
}

// 消息相关接口
var messageApi = {
  // 通知列表
  notices: function(params) {
    return request.get('/notice/list', params)
  },
  
  // 未读数
  unreadCount: function() {
    return request.get('/notice/unread-count')
  },
  
  // 标记已读
  markRead: function(id) {
    return request.put('/notice/' + id + '/read')
  },
  
  // 私信会话列表
  conversations: function() {
    return request.get('/chat/users')
  },
  
  // 私信历史
  history: function(userId, pageNum, pageSize) {
    pageNum = pageNum || 1
    pageSize = pageSize || 20
    return request.get('/chat/list', { otherUserId: userId, page: pageNum, size: pageSize })
  },
  
  // 发送私信
  send: function(data) {
    return request.post('/chat/send', data)
  },
}

// AI相关接口
var aiApi = {
  // 发送消息
  chat: function(data) {
    return request.post('/ai/chat', data)
  },
  
  // 对话历史
  messages: function(sessionId) {
    return request.get('/ai/chat/history', { sessionId: sessionId })
  },
  
  // 对话列表
  chats: function() {
    return request.get('/ai/chat/sessions')
  },
  
  // AI写作
  write: function(data) {
    return request.post('/ai/write/article', data)
  },
  
  // AI润色
  polish: function(content) {
    return request.post('/ai/write/polish', { content: content })
  },
  
  // AI总结
  summarize: function(content) {
    return request.post('/ai/write/summarize', { content: content })
  },
}

// 搜索相关接口
var searchApi = {
  // 搜索
  all: function(keyword, pageNum) {
    pageNum = pageNum || 1
    return request.get('/search/posts', { keyword: keyword, pageNum: pageNum })
  },
  
  // 热门搜索
  hot: function() {
    return request.get('/search/hot-keywords')
  },
  
  // 搜索历史
  history: function() {
    return request.get('/search/history')
  },
}

module.exports = {
  postApi: postApi,
  commentApi: commentApi,
  userApi: userApi,
  sectionApi: sectionApi,
  messageApi: messageApi,
  aiApi: aiApi,
  searchApi: searchApi,
}
