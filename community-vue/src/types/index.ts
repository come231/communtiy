// 用户相关类型
export interface UserInfo {
  id: number
  username: string
  nickname: string
  avatar: string
  bio?: string
  email?: string
  followersCount?: number
  followingCount?: number
  postsCount?: number
  points?: number
}

// 帖子相关类型
export interface Post {
  id: number
  title: string
  content: string
  summary?: string
  coverImage?: string
  authorId: number
  authorName: string
  authorAvatar: string
  sectionId?: number
  sectionName?: string
  viewCount: number
  likeCount: number
  commentCount: number
  collectCount: number
  isTop: boolean
  isEssence: boolean
  isAnonymous: boolean
  status: number
  createTime: string
  tags?: string[]
}

export interface PostDetail extends Post {
  isLiked: boolean
  isCollected: boolean
}

// 评论类型
export interface Comment {
  id: number
  content: string
  postId: number
  userId: number
  userName: string
  userAvatar: string
  parentId?: number
  replyToUserId?: number
  replyToUserName?: string
  floorNumber: number
  likeCount: number
  isLiked: boolean
  createTime: string
  replies?: Comment[]
}

// 板块 / 话题 / 分页响应 / 收藏夹
export interface Section { id: number; name: string; icon?: string
  description?: string; postCount: number; sort: number }
export interface Topic { id: number; name: string; description?: string;
  postCount: number; isHot: boolean; isFollowed?: boolean }
export interface PageResult<T> { records: T[]; total: number; size: number;
  current: number; pages: number }
export interface CollectFolder { id: number; name: string; count: number; createTime: string }

// 签到状态
export interface CheckinStatus { checkedToday: boolean; continuousDays: number; totalDays: number }

// ==================== 消息通知类型 ====================
export interface Notice {
  id: number
  type: number // 1:点赞 2:评论 3:关注 4:收藏 5:系统通知
  title: string
  content: string
  fromUserId?: number
  fromUserName?: string
  fromUserAvatar?: string
  targetId?: number
  targetType?: string
  isRead: boolean
  createTime: string
}

export interface Conversation {
  otherUserId: number
  nickName: string
  avatar: string
  lastMessage: string
  lastTime: string
  unreadCount: number
  online?: boolean
}

export interface PrivateMessage {
  id: number
  fromUserId: number
  fromUserName: string
  fromUserAvatar: string
  toUserId: number
  content: string
  isRead: boolean
  createTime: string
}

// ==================== AI对话类型 ====================
export interface AiChat {
  id: number
  title: string
  createTime: string
  updateTime: string
}

export interface AiMessage {
  id: string
  role: 'user' | 'assistant' | 'system'
  content: string
  createTime: string
}

// ==================== 搜索类型 ====================
export interface SearchResult {
  posts?: Post[]
  users?: UserInfo[]
  topics?: Topic[]
  total: number
}

export interface HotSearch {
  keyword: string
  count: number
}

// ==================== 管理后台类型 ====================
export interface Report {
  id: number
  type: number // 1:帖子 2:评论 3:用户
  targetId: number
  reason: string
  description?: string
  reporterId: number
  reporterName: string
  reporterAvatar?: string
  targetUserId: number
  targetUserName: string
  targetContent?: string
  status: number // 0:待处理 1:已处理 2:已驳回
  handleResult?: string
  createTime: string
  handleTime?: string
}

export interface SensitiveWord {
  id: number
  word: string
  level: number // 1:警告 2:替换 3:拦截
  category: string
  replacement?: string
  hitCount: number
  createTime: string
}

export interface SystemConfig {
  id: number
  groupKey: string
  configKey: string
  configValue: string
  description?: string
  updateTime: string
}

export interface DashboardStats {
  totalUsers: number
  totalPosts: number
  totalComments: number
  totalReports: number
  pendingReports: number
  todayNewUsers: number
  todayNewPosts: number
  hotPosts: { title: string; viewCount: number }[]
  hotSections: { name: string; postCount: number }[]
}

export interface AdminUser extends UserInfo {
  status: number // 0:正常 1:禁用 2:审核中
  role: string
  registerTime: string
  lastLoginTime?: string
  postsCount: number
}

export interface AdminPost extends Post {
  reportCount: number
}
