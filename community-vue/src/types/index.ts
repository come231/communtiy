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
  /** 获赞总数 */
  likeCount?: number
  /** 是否隐藏喜欢列表: 0公开 1隐藏 */
  hideLikes?: number
  /** 是否隐藏收藏列表: 0公开 1隐藏 */
  hideCollects?: number
  /** 个人标签 */
  bioTags?: string
}

// 帖子相关类型
export interface PostMediaItem {
  fileId?: string
  mediaType: number
  sortOrder?: number
  sizeBytes?: number
  downloadPath?: string
}

export interface Post {
  /** 与后端 PostVO.postId 一致；列表页优先用此字段 */
  postId?: number
  id: number
  title: string
  content: string
  summary?: string
  /** JSON 数组字符串，图片 URL 列表 */
  images?: string
  /** 封面图 URL */
  coverImage?: string
  coverFileId?: number
  video?: string
  totalMediaBytes?: number
  media?: PostMediaItem[]
  authorId: number
  authorName: string
  /** 部分列表接口可能只返回 nickname */
  nickname?: string
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
  /** 是否公开: true公开 false私密(仅自己可见) */
  isPublic?: boolean
  status: number
  /** 违规打回时管理端填写的说明 */
  rejectReason?: string
  createTime: string
  tags?: string[]
}

export interface PostDetail extends Post {
  isLiked: boolean
  isCollected: boolean
}

/** 发帖接口返回：1 已公开 2 待人工审核 */
export interface PostPublishResult {
  postId: number
  status: number
  message: string
}

/** AI 内容审核流水（与后端 AiCheckRecordVO 一致） */
export interface AiCheckRecord {
  id: number
  userId?: number
  contentType?: number
  contentId?: number
  originalContent?: string
  checkResult?: number
  violationPercent?: number
  sensitiveWords?: string
  aiSuggestion?: string
  /** 后端 LocalDateTime 可能为字符串或数组 */
  createTime?: string | number[]

  // ========== 人机回环数据飞轮字段 ==========
  /** 人工复核标记：0 未复核 1 确认AI正确 2 纠正为违规 3 纠正为正常 */
  manualReviewStatus?: number
  /** 人工复核标签：如"严重违规-危化DIY" */
  manualLabel?: string
  /** 人工复核备注 */
  manualRemark?: string
  /** 复核管理员ID */
  reviewerId?: number
  /** 复核时间 */
  reviewTime?: string | number[]
  /** 是否已加入向量样本库：0 未加入 1 已加入 */
  inVectorStore?: number
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
  /** 评论配图 /api/file/download/... */
  imageUrls?: string[]
  replies?: Comment[]
}

// 板块 / 话题 / 分页响应 / 收藏夹
export interface SectionCategory {
  categoryId: number
  name: string
  sort: number
  status: number
}

export interface Section {
  /** 与 Java 端 sectionId 对齐；部分列表接口可能只返回 id */
  id?: number
  sectionId?: number
  name: string
  icon?: string
  cover?: string
  intro?: string
  description?: string
  postCount: number
  sort: number
  /** 板块分类ID */
  categoryId?: number
  /** 板块分类名称 */
  categoryName?: string
  /** 是否已加入 */
  isJoined?: boolean
  /** 成员数 */
  memberCount?: number
}
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
  /** 关联帖子标题等（后端可选返回） */
  targetTitle?: string
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
  msgType?: number
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
  id?: number
  groupKey: string
  configKey: string
  configValue: string
  description?: string
  updateTime: string
}

/** 帖子 AI 审核规则（与后端 PostReviewRuleDTO 一致） */
export interface PostReviewRule {
  id?: number
  ruleName?: string
  ruleText: string
  sortOrder: number
  /** 1 启用 0 禁用 */
  status: number
  createTime?: string
  updateTime?: string
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
