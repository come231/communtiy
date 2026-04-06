import { userApi, postApi } from '../../api/index'
import { formatTime } from '../../utils/util'

const app = getApp<IAppOption>()

Page({
  data: {
    userId: 0,
    userInfo: null as any,
    posts: [] as any[],
    pageNum: 1,
    loading: false,
    hasMore: true,
    isMe: false,
    isFollowing: false,
  },

  onLoad(options: any) {
    const id = Number(options.id)
    if (id) {
      this.setData({ userId: id })
      this.loadUserInfo(id)
      this.loadUserPosts(id)
      
      if (app.globalData.userInfo?.id === id) {
        this.setData({ isMe: true })
      }
    }
  },

  async loadUserInfo(id: number) {
    try {
      const userInfo = await userApi.info(id)
      this.setData({ userInfo })
    } catch (e) {
      console.error('加载用户信息失败', e)
      wx.showToast({ title: '加载失败', icon: 'none' })
    }
  },

  async loadUserPosts(id: number, page = 1) {
    this.setData({ loading: true })
    
    try {
      const res = await postApi.list({ pageNum: page, pageSize: 10 })
      const posts = (res?.records || [])
        .filter((p) => p.userId === id)
        .map((item) => ({
          ...item,
          createTimeStr: formatTime(item.createTime),
        }))
      
      this.setData({
        posts: page === 1 ? posts : [...this.data.posts, ...posts],
        pageNum: page,
        loading: false,
        hasMore: posts.length >= 10,
      })
    } catch (e) {
      console.error('加载帖子失败', e)
      this.setData({ loading: false })
    }
  },

  loadMore() {
    if (this.data.hasMore && !this.data.loading) {
      this.loadUserPosts(this.data.userId, this.data.pageNum + 1)
    }
  },

  async toggleFollow() {
    if (!app.globalData.isLogin) {
      wx.navigateTo({ url: '/pages/login/login' })
      return
    }
    
    try {
      if (this.data.isFollowing) {
        await userApi.unfollow(this.data.userId)
        this.setData({
          isFollowing: false,
          'userInfo.followersCount': Math.max(0, (this.data.userInfo.followersCount || 0) - 1),
        })
      } else {
        await userApi.follow(this.data.userId)
        this.setData({
          isFollowing: true,
          'userInfo.followersCount': (this.data.userInfo.followersCount || 0) + 1,
        })
      }
    } catch (e) {
      console.error('关注失败', e)
      wx.showToast({ title: '操作失败', icon: 'none' })
    }
  },

  sendMessage() {
    if (!app.globalData.isLogin) {
      wx.navigateTo({ url: '/pages/login/login' })
      return
    }
    wx.showToast({ title: '私信功能开发中', icon: 'none' })
  },

  goToPost(e) {
    wx.navigateTo({ url: `/pages/post/post?id=${e.currentTarget.dataset.id}` })
  },
})
