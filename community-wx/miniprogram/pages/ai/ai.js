import { aiApi } from '../../api/index'

const app = getApp<IAppOption>()

Page({
  data: {
    messages: [] as any[],
    inputText: '',
    loading: false,
    scrollTop: 0,
    quickActions: [
      { type: 'write', label: 'AI写作', icon: '/assets/icons/write.png' },
      { type: 'polish', label: '文章润色', icon: '/assets/icons/polish.png' },
      { type: 'summarize', label: '内容总结', icon: '/assets/icons/summary.png' },
      { type: 'check', label: '违规检测', icon: '/assets/icons/check.png' },
    ],
    userAvatar: '',
  },

  onLoad() {
    this.setData({
      userAvatar: app.globalData.userInfo?.avatar || '',
    })
  },

  onInput(e: any) {
    this.setData({ inputText: e.detail.value })
  },

  async onSend() {
    const text = this.data.inputText.trim()
    if (!text || this.data.loading) return
    
    // 添加用户消息
    const messages = [...this.data.messages, {
      id: Date.now(),
      role: 'user',
      content: text,
    }]
    
    this.setData({
      messages,
      inputText: '',
      loading: true,
    })
    
    this.scrollToBottom()
    
    try {
      const res = await aiApi.chat({ content: text })
      
      // 添加AI回复
      const assistantMsg = {
        id: Date.now() + 1,
        role: 'assistant',
        content: res || '抱歉，我暂时无法回答这个问题。',
      }
      
      this.setData({
        messages: [...this.data.messages, assistantMsg],
        loading: false,
      })
      
      this.scrollToBottom()
    } catch (e) {
      console.error('AI回复失败', e)
      
      const errorMsg = {
        id: Date.now() + 1,
        role: 'assistant',
        content: '抱歉，服务出现问题，请稍后再试。',
      }
      
      this.setData({
        messages: [...this.data.messages, errorMsg],
        loading: false,
      })
    }
  },

  async onQuickAction(e: any) {
    const type = e.currentTarget.dataset.type
    let title = ''
    let placeholder = ''
    
    switch (type) {
      case 'write':
        title = 'AI写作'
        placeholder = '描述你想要写的内容...'
        break
      case 'polish':
        title = '文章润色'
        placeholder = '输入需要润色的文章...'
        break
      case 'summarize':
        title = '内容总结'
        placeholder = '输入需要总结的内容...'
        break
      case 'check':
        title = '违规检测'
        placeholder = '输入需要检测的内容...'
        break
    }
    
    wx.showModal({
      title,
      editable: true,
      placeholderText: placeholder,
      success: async (res) => {
        if (res.content && res.confirm) {
          await this.processQuickAction(type, res.content)
        }
      }
    })
  },

  async processQuickAction(type, content) {
    if (!content.trim()) return
    
    wx.showLoading({ title: '处理中...' })
    
    try {
      let result = ''
      
      switch (type) {
        case 'write':
          result = await aiApi.write({ content })
          break
        case 'polish':
          result = await aiApi.polish(content)
          break
        case 'summarize':
          result = await aiApi.summarize(content)
          break
        case 'check':
          const checkResult = await aiApi.check(content)
          result = checkResult.isValid ? '内容检测正常，没有违规内容。' : `检测到可能违规的内容：${checkResult.reason}`
          break
      }
      
      wx.hideLoading()
      
      // 添加到聊天
      const messages = [
        ...this.data.messages,
        { id: Date.now(), role: 'user', content },
        { id: Date.now() + 1, role: 'assistant', content: result },
      ]
      
      this.setData({ messages })
      this.scrollToBottom()
    } catch (e) {
      wx.hideLoading()
      wx.showToast({ title: '处理失败', icon: 'none' })
    }
  },

  scrollToBottom() {
    setTimeout(() => {
      this.setData({ scrollTop: this.data.messages.length * 1000 })
    }, 100)
  },
})
