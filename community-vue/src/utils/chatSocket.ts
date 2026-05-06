import { Client, IMessage, StompSubscription } from '@stomp/stompjs'
import { ElMessage } from 'element-plus'

/**
 * WebSocket聊天服务
 * 使用STOMP协议与后端通信
 * 使用原生WebSocket（ws://）而非SockJS，简化连接流程
 */

// WebSocket连接配置
// 使用相对路径，通过Vite代理转发到Gateway (ws://localhost:8095)
const WS_BASE_URL = 'ws://localhost:3000' // 开发环境通过Vite代理

// 连接状态
export enum ConnectionStatus {
  DISCONNECTED = 'disconnected',
  CONNECTING = 'connecting',
  CONNECTED = 'connected',
  RECONNECTING = 'reconnecting',
  ERROR = 'error'
}

// 消息类型
export interface ChatMessage {
  fromUserId: number
  toUserId: number
  content: string
  sharePostId?: number
  sharePostTitle?: string
  timestamp: number
}

export interface ReadReceipt {
  readerId: number
  timestamp: number
}

export interface UnreadCountUpdate {
  unreadCount: number
  timestamp: number
}

// 回调函数类型
type MessageCallback = (message: ChatMessage) => void
type ReadReceiptCallback = (receipt: ReadReceipt) => void
type UnreadCallback = (update: UnreadCountUpdate) => void
type StatusCallback = (status: ConnectionStatus) => void

class ChatSocketService {
  private client: Client | null = null
  private status: ConnectionStatus = ConnectionStatus.DISCONNECTED
  private reconnectAttempts = 0
  private maxReconnectAttempts = 5
  private reconnectDelay = 3000
  private subscriptions: StompSubscription[] = []

  // 回调函数集合
  private messageCallbacks: MessageCallback[] = []
  private readReceiptCallbacks: ReadReceiptCallback[] = []
  private unreadCallbacks: UnreadCallback[] = []
  private statusCallbacks: StatusCallback[] = []

  /**
   * 获取当前连接状态
   */
  getStatus(): ConnectionStatus {
    return this.status
  }

  /**
   * 是否已连接
   */
  isConnected(): boolean {
    return this.status === ConnectionStatus.CONNECTED && this.client?.active === true
  }

  /**
   * 建立WebSocket连接
   */
  connect(): void {
    if (this.client?.active) {
      console.log('[ChatSocket] 已经连接，无需重复连接')
      return
    }

    const token = localStorage.getItem('accessToken')
    if (!token) {
      console.error('[ChatSocket] 未找到Token，无法连接')
      this.setStatus(ConnectionStatus.ERROR)
      return
    }

    this.setStatus(ConnectionStatus.CONNECTING)
    this.reconnectAttempts = 0

    // 构建原生WebSocket URL
    // 使用原生ws://协议，避免SockJS的复杂性
    const wsUrl = `${WS_BASE_URL}/ws/chat`

    console.log('[ChatSocket] 正在连接:', wsUrl)

    this.client = new Client({
      // 使用原生WebSocket而不是SockJS
      webSocketFactory: () => new WebSocket(wsUrl),
      connectHeaders: {
        Authorization: `Bearer ${token}`,
        token: token
      },
      debug: (str) => {
        if (import.meta.env.DEV) {
          console.log('[STOMP]', str)
        }
      },
      reconnectDelay: this.reconnectDelay,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,

      onConnect: () => {
        console.log('[ChatSocket] 连接成功')
        this.setStatus(ConnectionStatus.CONNECTED)
        this.reconnectAttempts = 0
        this.subscribeToChannels()
      },

      onDisconnect: () => {
        console.log('[ChatSocket] 连接断开')
        this.setStatus(ConnectionStatus.DISCONNECTED)
        this.unsubscribeAll()
      },

      onStompError: (frame) => {
        console.error('[ChatSocket] STOMP错误:', frame)
        this.setStatus(ConnectionStatus.ERROR)
      },

      onWebSocketError: (event) => {
        console.error('[ChatSocket] WebSocket错误:', event)
        this.setStatus(ConnectionStatus.ERROR)
        this.handleReconnect()
      },

      onWebSocketClose: () => {
        console.log('[ChatSocket] WebSocket关闭')
        if (this.status !== ConnectionStatus.DISCONNECTED) {
          this.setStatus(ConnectionStatus.RECONNECTING)
          this.handleReconnect()
        }
      }
    })

    this.client.activate()
  }

  /**
   * 断开连接
   */
  disconnect(): void {
    if (this.client) {
      this.unsubscribeAll()
      this.client.deactivate()
      this.client = null
    }
    this.setStatus(ConnectionStatus.DISCONNECTED)
    console.log('[ChatSocket] 已断开连接')
  }

  /**
   * 订阅频道
   */
  private subscribeToChannels(): void {
    if (!this.client?.active) {
      console.error('[ChatSocket] 未连接，无法订阅')
      return
    }

    // 订阅私聊消息 - /user/queue/chat
    const chatSub = this.client.subscribe('/user/queue/chat', (message: IMessage) => {
      try {
        const body: ChatMessage = JSON.parse(message.body)
        console.log('[ChatSocket] 收到消息:', body)
        this.messageCallbacks.forEach(cb => cb(body))
      } catch (error) {
        console.error('[ChatSocket] 解析消息失败:', error)
      }
    })
    this.subscriptions.push(chatSub)

    // 订阅消息发送确认 - /user/queue/chat/ack
    const ackSub = this.client.subscribe('/user/queue/chat/ack', (message: IMessage) => {
      console.log('[ChatSocket] 消息发送成功:', message.body)
    })
    this.subscriptions.push(ackSub)

    // 订阅已读回执 - /user/queue/chat/read
    const readSub = this.client.subscribe('/user/queue/chat/read', (message: IMessage) => {
      try {
        const body: ReadReceipt = JSON.parse(message.body)
        console.log('[ChatSocket] 收到已读回执:', body)
        this.readReceiptCallbacks.forEach(cb => cb(body))
      } catch (error) {
        console.error('[ChatSocket] 解析已读回执失败:', error)
      }
    })
    this.subscriptions.push(readSub)

    // 订阅未读数更新 - /user/queue/unread
    const unreadSub = this.client.subscribe('/user/queue/unread', (message: IMessage) => {
      try {
        const body: UnreadCountUpdate = JSON.parse(message.body)
        console.log('[ChatSocket] 未读数更新:', body)
        this.unreadCallbacks.forEach(cb => cb(body))
      } catch (error) {
        console.error('[ChatSocket] 解析未读数失败:', error)
      }
    })
    this.subscriptions.push(unreadSub)

    console.log('[ChatSocket] 已订阅所有频道')
  }

  /**
   * 取消所有订阅
   */
  private unsubscribeAll(): void {
    this.subscriptions.forEach(sub => sub.unsubscribe())
    this.subscriptions = []
    console.log('[ChatSocket] 已取消所有订阅')
  }

  /**
   * 处理重连
   */
  private handleReconnect(): void {
    if (this.reconnectAttempts >= this.maxReconnectAttempts) {
      console.error('[ChatSocket] 重连次数超过限制，停止重连')
      this.setStatus(ConnectionStatus.ERROR)
      ElMessage.error('聊天服务连接失败，请刷新页面重试')
      return
    }

    this.reconnectAttempts++
    console.log(`[ChatSocket] 第 ${this.reconnectAttempts} 次重连...`)
    this.setStatus(ConnectionStatus.RECONNECTING)

    // 使用指数退避
    const delay = this.reconnectDelay * Math.pow(1.5, this.reconnectAttempts - 1)
    setTimeout(() => {
      if (this.status !== ConnectionStatus.CONNECTED) {
        this.connect()
      }
    }, delay)
  }

  /**
   * 发送聊天消息
   */
  sendMessage(toUserId: number, content: string, sharePostId?: number, sharePostTitle?: string): boolean {
    if (!this.isConnected()) {
      console.error('[ChatSocket] 未连接，无法发送消息')
      ElMessage.warning('聊天服务未连接，请稍后重试')
      return false
    }

    const message = {
      toUserId,
      content,
      sharePostId,
      sharePostTitle
    }

    this.client?.publish({
      destination: '/app/chat/send',
      body: JSON.stringify(message)
    })

    console.log('[ChatSocket] 消息已发送:', message)
    return true
  }

  /**
   * 标记消息已读
   */
  markAsRead(fromUserId: number): boolean {
    if (!this.isConnected()) {
      return false
    }

    this.client?.publish({
      destination: '/app/chat/read',
      body: JSON.stringify({ fromUserId })
    })

    console.log('[ChatSocket] 已读标记已发送:', fromUserId)
    return true
  }

  /**
   * 发送心跳
   */
  sendPing(): boolean {
    if (!this.isConnected()) {
      return false
    }

    this.client?.publish({
      destination: '/app/chat/ping',
      body: JSON.stringify({ time: Date.now() })
    })

    return true
  }

  /**
   * 获取联系人列表（通过WebSocket）
   */
  requestUserList(): boolean {
    if (!this.isConnected()) {
      return false
    }

    this.client?.publish({
      destination: '/app/chat/users',
      body: '{}'
    })

    return true
  }

  // ============== 事件监听 ==============

  /**
   * 监听新消息
   */
  onMessage(callback: MessageCallback): () => void {
    this.messageCallbacks.push(callback)
    return () => {
      this.messageCallbacks = this.messageCallbacks.filter(cb => cb !== callback)
    }
  }

  /**
   * 监听已读回执
   */
  onReadReceipt(callback: ReadReceiptCallback): () => void {
    this.readReceiptCallbacks.push(callback)
    return () => {
      this.readReceiptCallbacks = this.readReceiptCallbacks.filter(cb => cb !== callback)
    }
  }

  /**
   * 监听未读数更新
   */
  onUnreadUpdate(callback: UnreadCallback): () => void {
    this.unreadCallbacks.push(callback)
    return () => {
      this.unreadCallbacks = this.unreadCallbacks.filter(cb => cb !== callback)
    }
  }

  /**
   * 监听连接状态变化
   */
  onStatusChange(callback: StatusCallback): () => void {
    this.statusCallbacks.push(callback)
    // 立即通知当前状态
    callback(this.status)
    return () => {
      this.statusCallbacks = this.statusCallbacks.filter(cb => cb !== callback)
    }
  }

  /**
   * 设置状态并通知监听者
   */
  private setStatus(status: ConnectionStatus): void {
    if (this.status !== status) {
      this.status = status
      this.statusCallbacks.forEach(cb => cb(status))
    }
  }
}

// 导出单例
export const chatSocket = new ChatSocketService()
export default chatSocket
