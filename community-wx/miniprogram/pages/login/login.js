var api = require('../../api/index')

var userApi = api.userApi
var app = getApp()

Page({
  data: {
    form: {
      username: '',
      password: '',
    },
    loading: false,
    canLogin: false,
    canWechatLogin: false,
  },

  onLoad: function() {
    this.checkWechatLogin()
    
    if (app.globalData.isLogin) {
      wx.switchTab({ url: '/pages/index/index' })
    }
  },

  checkWechatLogin: function() {
    var canWechatLogin = wx.canIUse('button[type="getPhoneNumber"]')
    this.setData({ canWechatLogin: canWechatLogin })
  },

  onUsernameInput: function(e) {
    this.setData({
      'form.username': e.detail.value,
      canLogin: e.detail.value.length > 0 && this.data.form.password.length >= 6,
    })
  },

  onPasswordInput: function(e) {
    this.setData({
      'form.password': e.detail.value,
      canLogin: this.data.form.username.length > 0 && e.detail.value.length >= 6,
    })
  },

  onLogin: function() {
    var _this = this
    if (!this.data.canLogin || this.data.loading) return
    
    var username = this.data.form.username
    var password = this.data.form.password
    
    if (!username.trim()) {
      wx.showToast({ title: '请输入用户名', icon: 'none' })
      return
    }
    
    if (password.length < 6) {
      wx.showToast({ title: '密码至少6位', icon: 'none' })
      return
    }
    
    this.setData({ loading: true })
    
    userApi.login({
      username: username.trim(),
      password: password,
    }).then(function(res) {
      app.setUserInfo(res, res.accessToken)
      wx.showToast({ title: '登录成功', icon: 'success' })
      setTimeout(function() {
        wx.switchTab({ url: '/pages/index/index' })
      }, 1500)
    }).catch(function(e) {
      wx.showToast({ title: e.message || '登录失败', icon: 'none' })
    }).finally(function() {
      _this.setData({ loading: false })
    })
  },

  onWechatLogin: function() {
    wx.showToast({ title: '微信登录开发中', icon: 'none' })
  },

  onRegister: function() {
    wx.showToast({ title: '注册功能开发中', icon: 'none' })
  },
})
