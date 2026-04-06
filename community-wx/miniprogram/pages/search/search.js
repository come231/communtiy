var api = require('../../api/index')
var util = require('../../utils/util')

var searchApi = api.searchApi
var formatTime = util.formatTime

Page({
  data: {
    keyword: '',
    searched: false,
    hotList: [],
    history: [],
    postResults: [],
    userResults: [],
    topicResults: [],
    resultTabs: [
      { key: 'posts', label: '帖子', count: 0 },
      { key: 'users', label: '用户', count: 0 },
      { key: 'topics', label: '话题', count: 0 },
    ],
    activeResult: 'posts',
  },

  onLoad: function() {
    this.loadHotSearch()
    this.loadHistory()
  },

  onInput: function(e) {
    this.setData({ keyword: e.detail.value })
    if (!e.detail.value) {
      this.setData({ searched: false })
    }
  },

  onSearch: function() {
    var _this = this
    var keyword = this.data.keyword.trim()
    if (!keyword) return
    
    this.saveHistory(keyword)
    
    wx.showLoading({ title: '搜索中...' })
    
    searchApi.all(keyword).then(function(res) {
      var posts = (res && res.posts || []).map(function(item) {
        return Object.assign({}, item, {
          createTimeStr: formatTime(item.createTime),
        })
      })
      
      _this.setData({
        searched: true,
        postResults: posts,
        userResults: (res && res.users) || [],
        topicResults: (res && res.topics) || [],
        resultTabs: [
          { key: 'posts', label: '帖子', count: posts.length },
          { key: 'users', label: '用户', count: (res && res.users && res.users.length) || 0 },
          { key: 'topics', label: '话题', count: (res && res.topics && res.topics.length) || 0 },
        ],
      })
      
      wx.hideLoading()
    }).catch(function(e) {
      wx.hideLoading()
      wx.showToast({ title: '搜索失败', icon: 'none' })
    })
  },

  loadHotSearch: function() {
    var _this = this
    searchApi.hot().then(function(hotList) {
      _this.setData({ hotList: hotList || [] })
    }).catch(function(e) {
      console.error('加载热门搜索失败', e)
    })
  },

  loadHistory: function() {
    var history = wx.getStorageSync('searchHistory') || []
    this.setData({ history: history })
  },

  saveHistory: function(keyword) {
    var history = wx.getStorageSync('searchHistory') || []
    history = history.filter(function(h) { return h !== keyword })
    history.unshift(keyword)
    history = history.slice(0, 20)
    wx.setStorageSync('searchHistory', history)
    this.setData({ history: history })
  },

  clearHistory: function() {
    var _this = this
    wx.showModal({
      title: '提示',
      content: '确定清空搜索历史？',
      success: function(res) {
        if (res.confirm) {
          wx.removeStorageSync('searchHistory')
          _this.setData({ history: [] })
        }
      }
    })
  },

  quickSearch: function(e) {
    var keyword = e.currentTarget.dataset.keyword
    this.setData({ keyword: keyword })
    this.onSearch()
  },

  switchResult: function(e) {
    this.setData({ activeResult: e.currentTarget.dataset.key })
  },

  onCancel: function() {
    wx.navigateBack()
  },

  goToPost: function(e) {
    wx.navigateTo({ url: '/pages/post/post?id=' + e.currentTarget.dataset.id })
  },

  goToUser: function(e) {
    wx.navigateTo({ url: '/pages/user/user?id=' + e.currentTarget.dataset.id })
  },

  goToTopic: function() {
    wx.showToast({ title: '话题功能开发中', icon: 'none' })
  },
})
