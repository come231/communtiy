App({
  globalData: {
    userInfo: null,
    token: '',
    isLogin: false,
    baseUrl: 'http://localhost:8095',
  },

  onLaunch() {
    // 检查登录状态
    this.checkLoginStatus()
    
    // 获取系统信息
    const systemInfo = wx.getSystemInfoSync()
    this.globalData.systemInfo = systemInfo
    
    // 设置TabBar高度
    const menuButtonInfo = wx.getMenuButtonBoundingClientRect()
    this.globalData.menuButtonInfo = menuButtonInfo
  },

  checkLoginStatus() {
    const token = wx.getStorageSync('accessToken')
    const userInfo = wx.getStorageSync('userInfo')
    
    if (token && userInfo) {
      this.globalData.token = token
      this.globalData.userInfo = userInfo
      this.globalData.isLogin = true
    } else {
      this.globalData.isLogin = false
    }
  },

  setUserInfo(userInfo, token) {
    this.globalData.userInfo = userInfo
    this.globalData.token = token
    this.globalData.isLogin = true
    
    wx.setStorageSync('accessToken', token)
    wx.setStorageSync('refreshToken', wx.getStorageSync('refreshToken') || '')
    wx.setStorageSync('userInfo', userInfo)
  },

  logout() {
    this.globalData.userInfo = null
    this.globalData.token = ''
    this.globalData.isLogin = false
    
    wx.removeStorageSync('accessToken')
    wx.removeStorageSync('refreshToken')
    wx.removeStorageSync('userInfo')
  },
})
