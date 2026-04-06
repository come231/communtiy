import { userApi } from '../../api/index'

const app = getApp()

Page({
  data: {
    userInfo: null as any,
    isLogin: false,
    isMe: false,
    isFollowing: false,
  },

  onLoad() {
    this.checkLoginStatus()
  },

  onShow() {
    this.checkLoginStatus()
    if (app.globalData.isLogin) {
      this.loadUserInfo()
    }
  },

  checkLoginStatus() {
    const isLogin = app.globalData.isLogin
    const userInfo = app.globalData.userInfo
    this.setData({
      isLogin,
      userInfo: isLogin ? userInfo : {
        username: '游客',
        nickname: '未登录',
        avatar: '/assets/default-avatar.png',
      },
      isMe: isLogin && userInfo?.id === app.globalData.userInfo?.id,
    })
  },

  async loadUserInfo() {
    if (!app.globalData.isLogin) return
    
    try {
      const userInfo = await userApi.info()
      this.setData({ userInfo })
      
      // 更新全局用户信息
      app.globalData.userInfo = userInfo
      wx.setStorageSync('userInfo', userInfo)
    } catch (e) {
      console.error('加载用户信息失败', e)
    }
  },

  goToLogin() {
    wx.navigateTo({ url: '/pages/login/login' })
  },

  goToEdit() {
    wx.showToast({ title: '编辑资料功能开发中', icon: 'none' })
  },

  goToCheckin() {
    if (!app.globalData.isLogin) {
      wx.navigateTo({ url: '/pages/login/login' })
      return
    }
    wx.showModal({
      title: '每日签到',
      content: '签到成功，获得10积分！',
      showCancel: false,
    })
  },

  goToCollections() {
    if (!app.globalData.isLogin) {
      wx.navigateTo({ url: '/pages/login/login' })
      return
    }
    wx.showToast({ title: '我的收藏功能开发中', icon: 'none' })
  },

  goToHistory() {
    if (!app.globalData.isLogin) {
      wx.navigateTo({ url: '/pages/login/login' })
      return
    }
    wx.showToast({ title: '浏览历史功能开发中', icon: 'none' })
  },

  goToDrafts() {
    if (!app.globalData.isLogin) {
      wx.navigateTo({ url: '/pages/login/login' })
      return
    }
    wx.showToast({ title: '草稿箱功能开发中', icon: 'none' })
  },

  goToAI() {
    wx.navigateTo({ url: '/pages/ai/ai' })
  },

  goToSettings() {
    if (!app.globalData.isLogin) {
      wx.navigateTo({ url: '/pages/login/login' })
      return
    }
    wx.showToast({ title: '设置功能开发中', icon: 'none' })
  },

  goToFollowing() {
    if (!app.globalData.isLogin) {
      wx.navigateTo({ url: '/pages/login/login' })
      return
    }
    wx.showToast({ title: '关注列表功能开发中', icon: 'none' })
  },

  goToFollowers() {
    if (!app.globalData.isLogin) {
      wx.navigateTo({ url: '/pages/login/login' })
      return
    }
    wx.showToast({ title: '粉丝列表功能开发中', icon: 'none' })
  },

  async toggleFollow() {
    if (!app.globalData.isLogin) {
      wx.navigateTo({ url: '/pages/login/login' })
      return
    }
    
    const userId = this.data.userInfo?.id
    if (!userId) return
    
    try {
      if (this.data.isFollowing) {
        await userApi.unfollow(userId)
        this.setData({ isFollowing: false })
      } else {
        await userApi.follow(userId)
        this.setData({ isFollowing: true })
      }
    } catch (e) {
      console.error('关注失败', e)
      wx.showToast({ title: '操作失败', icon: 'none' })
    }
  },

  reportUser() {
    if (!app.globalData.isLogin) {
      wx.navigateTo({ url: '/pages/login/login' })
      return
    }
    wx.showToast({ title: '举报功能开发中', icon: 'none' })
  },

  onLogout() {
    wx.showModal({
      title: '提示',
      content: '确定要退出登录吗？',
      success: (res) => {
        if (res.confirm) {
          app.logout()
          this.setData({
            userInfo: {
              username: '游客',
              nickname: '未登录',
              avatar: '/assets/default-avatar.png',
            },
            isLogin: false,
            isMe: false,
            isFollowing: false,
          })
          wx.showToast({ title: '已退出登录', icon: 'success' })
        }
      }
    })
  },
})
