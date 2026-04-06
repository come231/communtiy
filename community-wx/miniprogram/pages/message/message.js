var api = require('../../api/index')
var util = require('../../utils/util')

var messageApi = api.messageApi
var formatTime = util.formatTime
var app = getApp()

Page({
  data: {
    tabs: [
      { key: 'notice', label: '通知' },
      { key: 'chat', label: '私信' },
      { key: 'system', label: '系统' },
    ],
    activeTab: 'notice',
    notices: [],
    conversations: [],
    systemNotices: [],
    unreadCount: 0,
  },

  onLoad: function() {
    var _this = this
    if (!app.globalData.isLogin) {
      wx.showToast({ title: '请先登录', icon: 'none' })
      setTimeout(function() {
        wx.navigateTo({ url: '/pages/login/login' })
      }, 1500)
      return
    }
    this.loadData()
  },

  onShow: function() {
    if (app.globalData.isLogin) {
      this.loadData()
    }
  },

  loadData: function() {
    var _this = this
    Promise.all([
      this.loadNotices(),
      this.loadConversations(),
      this.loadUnreadCount(),
    ])
  },

  loadNotices: function() {
    var _this = this
    return messageApi.notices({ pageNum: 1, pageSize: 50 }).then(function(res) {
      var notices = (res && res.records || []).map(function(item) {
        return Object.assign({}, item, {
          createTimeStr: formatTime(item.createTime),
        })
      })
      _this.setData({
        notices: notices,
        systemNotices: notices.filter(function(n) { return n.type === 5 }),
      })
    }).catch(function(e) {
      console.error('加载通知失败', e)
    })
  },

  loadConversations: function() {
    var _this = this
    return messageApi.conversations().then(function(res) {
      var conversations = (res || []).map(function(item) {
        return Object.assign({}, item, {
          lastTimeStr: formatTime(item.lastTime),
        })
      })
      _this.setData({ conversations: conversations })
    }).catch(function(e) {
      console.error('加载私信失败', e)
    })
  },

  loadUnreadCount: function() {
    var _this = this
    return messageApi.unreadCount().then(function(count) {
      _this.setData({ unreadCount: count || 0 })
    }).catch(function(e) {
      console.error('加载未读数失败', e)
    })
  },

  switchTab: function(e) {
    var key = e.currentTarget.dataset.key
    this.setData({ activeTab: key })
  },

  onNoticeClick: function(e) {
    var _this = this
    var item = e.currentTarget.dataset.item
    if (!item.isRead) {
      messageApi.markRead(item.id).then(function() {
        item.isRead = true
        _this.setData({ notices: _this.data.notices.slice() })
        _this.loadUnreadCount()
      }).catch(function(e) {
        console.error('标记已读失败', e)
      })
    }
    
    if (item.targetId) {
      if (item.targetType === 'post') {
        wx.navigateTo({ url: '/pages/post/post?id=' + item.targetId })
      } else if (item.targetType === 'user') {
        wx.navigateTo({ url: '/pages/user/user?id=' + item.targetId })
      }
    }
  },

  openChat: function(e) {
    var user = e.currentTarget.dataset.user
    wx.navigateTo({
      url: '/pages/user/user?id=' + user.userId + '&chat=true'
    })
  },
})
