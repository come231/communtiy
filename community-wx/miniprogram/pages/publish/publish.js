import { postApi, sectionApi } from '../../api/index'

const app = getApp<IAppOption>()

Page({
  data: {
    form: {
      title: '',
      content: '',
      sectionId: null as number | null,
      sectionName: '',
      topicIds: [] as number[],
      isAnonymous: false,
    },
    images: [] as string[],
    topics: [] as any[],
    canPublish: false,
    sections: [] as any[],
  },

  onLoad() {
    this.checkLogin()
    this.loadTopics()
    this.loadSections()
  },

  checkLogin() {
    if (!app.globalData.isLogin) {
      wx.showModal({
        title: '提示',
        content: '请先登录',
        success: (res) => {
          if (res.confirm) {
            wx.navigateTo({ url: '/pages/login/login' })
          } else {
            wx.navigateBack()
          }
        }
      })
    }
  },

  async loadTopics() {
    try {
      const topics = await sectionApi.hotTopics()
      this.setData({ topics: topics || [] })
    } catch (e) {
      console.error('加载话题失败', e)
    }
  },

  async loadSections() {
    try {
      const sections = await sectionApi.list()
      this.setData({ sections: sections || [] })
    } catch (e) {
      console.error('加载板块失败', e)
    }
  },

  onTitleInput(e: any) {
    const title = e.detail.value
    this.setData({
      'form.title': title,
      canPublish: title.length >= 5 && this.data.form.content.length > 0,
    })
  },

  onContentInput(e: any) {
    const content = e.detail.value
    this.setData({
      'form.content': content,
      canPublish: this.data.form.title.length >= 5 && content.length > 0,
    })
  },

  onAnonymousChange(e: any) {
    this.setData({
      'form.isAnonymous': e.detail.value,
    })
  },

  toggleTopic(e: any) {
    const topicId = e.currentTarget.dataset.id
    const topicIds = [...this.data.form.topicIds]
    const index = topicIds.indexOf(topicId)
    
    if (index > -1) {
      topicIds.splice(index, 1)
    } else {
      topicIds.push(topicId)
    }
    
    this.setData({
      'form.topicIds': topicIds,
    })
  },

  showSectionPicker() {
    const sections = this.data.sections
    if (sections.length === 0) {
      wx.showToast({ title: '暂无板块', icon: 'none' })
      return
    }
    
    wx.showActionSheet({
      itemList: sections.map((s: any) => s.name),
      success: (res) => {
        const section = sections[res.tapIndex]
        this.setData({
          'form.sectionId': section.sectionId || section.id,
          'form.sectionName': section.name,
        })
      }
    })
  },

  chooseImage() {
    wx.chooseMedia({
      count: 9 - this.data.images.length,
      mediaType: ['image'],
      sourceType: ['album', 'camera'],
      success: (res) => {
        const newImages = res.tempFiles.map((f: any) => f.tempFilePath)
        this.setData({
          images: [...this.data.images, ...newImages],
        })
      }
    })
  },

  deleteImage(e) {
    const index = e.currentTarget.dataset.index
    const images = [...this.data.images]
    images.splice(index, 1)
    this.setData({ images })
  },

  async onPublish() {
    if (!this.data.canPublish) {
      wx.showToast({ title: '请填写标题和内容', icon: 'none' })
      return
    }
    
    wx.showLoading({ title: '发布中...' })
    
    try {
      const { title, content, sectionId, topicIds, isAnonymous } = this.data.form
      
      await postApi.create({
        title: title.trim(),
        content: content.trim(),
        sectionId: sectionId || undefined,
        topicIds: topicIds.length > 0 ? topicIds : undefined,
        isAnonymous: isAnonymous ? 1 : 0,
      })
      
      wx.hideLoading()
      wx.showToast({ title: '发布成功', icon: 'success' })
      
      setTimeout(() => {
        wx.navigateBack()
      }, 1500)
    } catch (e: any) {
      wx.hideLoading()
      wx.showToast({ title: e.message || '发布失败', icon: 'none' })
    }
  },

  onCancel() {
    if (this.data.form.title || this.data.form.content) {
      wx.showModal({
        title: '提示',
        content: '确定放弃发布吗？',
        success: (res) => {
          if (res.confirm) {
            wx.navigateBack()
          }
        }
      })
    } else {
      wx.navigateBack()
    }
  },
})
