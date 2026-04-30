import type { Conversation } from '../types'

/** 统一后端会话列表项（camelCase / snake_case）→ 前端 Conversation */
export function normalizeConversation(raw: Record<string, unknown>): Conversation {
  return {
    otherUserId: Number(raw.otherUserId ?? raw.other_user_id),
    nickName: String(raw.nickName ?? raw.nick_name ?? '用户'),
    avatar: String(raw.avatar ?? ''),
    lastMessage: String(raw.lastMessage ?? raw.last_message ?? ''),
    lastTime: String(raw.lastTime ?? raw.last_time ?? ''),
    unreadCount: Number(raw.unreadCount ?? raw.unread_count ?? 0),
    online: typeof raw.online === 'boolean' ? raw.online : undefined,
  }
}

export function normalizeConversationList(rawList: unknown): Conversation[] {
  if (!Array.isArray(rawList)) return []
  return rawList
    .map((item) => normalizeConversation(item as Record<string, unknown>))
    .filter((c) => c.otherUserId > 0)
}
