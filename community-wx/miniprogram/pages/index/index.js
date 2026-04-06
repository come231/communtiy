var api = require('../../api/index')
var util = require('../../utils/util')

var postApi = api.postApi
var formatTime = util.formatTime

var app = getApp()

Page({
  data: {
    tabs: [
      { key: 'recommend', label: '推荐' },
      { key: 'hot', label: '热门' },
      { key: 'latest', label: '最新' },
    ],
    activeTab: 'recommend',
    posts: [],
    pageNum: 1,
    pageSize: 10,
    loading: false,
    noMore: false,
  },

  onLoad: function() {
    this.loadPosts()
  },

  onShow: function() {
    if (!app.globalData.isLogin) {
      this.checkLogin()
    }
  },

  onPullDownRefresh: function() {
    var _this = this
    this.setData({ pageNum: 1, posts: [], noMore: false })
    this.loadPosts().finally(function() {
      wx.stopPullDownRefresh()
    })
  },

  checkLogin: function() {
    var token = wx.getStorageSync('accessToken')
    var userInfo = wx.getStorageSync('userInfo')
    if (token && userInfo) {
      app.globalData.token = token
      app.globalData.userInfo = userInfo
      app.globalData.isLogin = true
    }
  },

  loadPosts: function() {
    var _this = this
    if (this.data.loading || this.data.noMore) return
    
    this.setData({ loading: true })
    
    return postApi.list({
      pageNum: this.data.pageNum,
      pageSize: this.data.pageSize,
    }).then(function(res) {
      var records = (res && res.records || []).map(function(item) {
        return Object.assign({}, item, {
          createTimeStr: formatTime(item.createTime),
        })
      })
      
      _this.setData({
        posts: _this.data.pageNum === 1 ? records : _this.data.posts.concat(records),
        loading: false,
        noMore: records.length < _this.data.pageSize,
      })
    }).catch(function(e) {
      console.error('加载帖子失败', e)
      _this.setData({ loading: false })
    })
  },

  loadMore: function() {
    if (this.data.noMore || this.data.loading) return
    this.setData({ pageNum: this.data.pageNum + 1 })
    this.loadPosts()
  },

  switchTab: function(e) {
    var key = e.currentTarget.dataset.key
    if (key === this.data.activeTab) return
    this.setData({
      activeTab: key,
      pageNum: 1,
      posts: [],
      noMore: false,
    })
    this.loadPosts()
  },

  goToSearch: function() {
    wx.navigateTo({ url: '/pages/search/search' })
  },

  goToPost: function(e) {
    var id = e.currentTarget.dataset.id
    wx.navigateTo({ url: '/pages/post/post?id=' + id })
  },

  goToUser: function(e) {
    var id = e.currentTarget.dataset.id
    wx.navigateTo({ url: '/pages/user/user?id=' + id })
  },

  toggleLike: function(e) {
    var _this = this
    var id = e.currentTarget.dataset.id
    var index = e.currentTarget.dataset.index
    
    if (!app.globalData.isLogin) {
      wx.navigateTo({ url: '/pages/login/login' })
      return
    }
    
    var posts = _this.data.posts.slice()
    var post = posts[index]
    
    ;(post.isLiked ? postApi.unlike(id) : postApi.like(id)).then(function() {
      post.isLiked = !post.isLiked
      post.likeCount = Math.max(0, (post.likeCount || 0) + (post.isLiked ? 1 : -1))
      _this.setData({ posts: posts })
    }).catch(function(e) {
      console.error('点赞失败', e)
    })
  },
})
