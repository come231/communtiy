import { postApi, commentApi, userApi } from '../../api/index'
import { formatTime } from '../../utils/util'

const app = getApp<IAppOption>()

Page({
  data: {
    postId: 0,
    post: null as any,
    comments: [] as any[],
    commentPage: 1,
    commentCount: 0,
    hasMoreComments: false,
    commentText: '',
    loading: true,
    isFollowing: false,
    isAuthor: false,
  },

  onLoad(options: any) {
    const id = Number(options.id)
    if (id) {
      this.setData({ postId: id })
      this.loadPost(id)
      this.loadComments(id)
    }
  },

  async loadPost(id: number) {
    try {
      const post = await postApi.detail(id)
      this.setData({
        post: {
          ...post,
          createTimeStr: formatTime(post.createTime),
        },
        loading: false,
        isAuthor: app.globalData.userInfo?.id === post.userId,
      })
      
      // 检查是否点赞/收藏
      this.checkInteractStatus(id)
    } catch (e) {
      console.error('加载帖子失败', e)
      wx.showToast({ title: '加载失败', icon: 'none' })
      this.setData({ loading: false })
    }
  },

  async checkInteractStatus(postId: number) {
    if (!app.globalData.isLogin) return
    
    try {
      const [isLikedRes, isCollectedRes] = await Promise.all([
        postApi.list().then(() => false), // 简化
      ])
      // 更新点赞收藏状态
    } catch (e) {
      console.error('检查互动状态失败', e)
    }
  },

  async loadComments(postId: number, page = 1) {
    try {
      const res = await commentApi.list(postId, page, 20)
      const comments = (res?.records || []).map((item: any) => ({
        ...item,
        createTimeStr: formatTime(item.createTime),
      }))
      
      this.setData({
        comments: page === 1 ? comments : [...this.data.comments, ...comments],
        commentCount: res?.total || comments.length,
        commentPage: page,
        hasMoreComments: comments.length >= 20,
      })
    } catch (e) {
      console.error('加载评论失败', e)
    }
  },

  loadMoreComments() {
    if (!this.data.hasMoreComments) return
    this.loadComments(this.data.postId, this.data.commentPage + 1)
  },

  onCommentInput(e: any) {
    this.setData({ commentText: e.detail.value })
  },

  async submitComment() {
    if (!this.data.commentText.trim()) return
    
    if (!app.globalData.isLogin) {
      wx.navigateTo({ url: '/pages/login/login' })
      return
    }
    
    try {
      await commentApi.create({
        postId: this.data.postId,
        content: this.data.commentText.trim(),
      })
      
      wx.showToast({ title: '评论成功', icon: 'success' })
      this.setData({ commentText: '' })
      
      // 刷新评论列表
      this.loadComments(this.data.postId)
      
      // 更新评论数
      const post = this.data.post
      if (post) {
        this.setData({
          post: { ...post, commentCount: (post.commentCount || 0) + 1 },
        })
      }
    } catch (e) {
      console.error('评论失败', e)
      wx.showToast({ title: '评论失败', icon: 'none' })
    }
  },

  async toggleLike() {
    if (!app.globalData.isLogin) {
      wx.navigateTo({ url: '/pages/login/login' })
      return
    }
    
    const post = this.data.post
    if (!post) return
    
    try {
      if (post.isLiked) {
        await postApi.unlike(post.postId)
        post.isLiked = false
        post.likeCount = Math.max(0, (post.likeCount || 0) - 1)
      } else {
        await postApi.like(post.postId)
        post.isLiked = true
        post.likeCount = (post.likeCount || 0) + 1
      }
      this.setData({ post })
    } catch (e) {
      console.error('点赞失败', e)
    }
  },

  async toggleCollect() {
    if (!app.globalData.isLogin) {
      wx.navigateTo({ url: '/pages/login/login' })
      return
    }
    
    const post = this.data.post
    if (!post) return
    
    try {
      if (post.isCollected) {
        await postApi.uncollect(post.postId)
        post.isCollected = false
        post.collectCount = Math.max(0, (post.collectCount || 0) - 1)
      } else {
        await postApi.collect(post.postId)
        post.isCollected = true
        post.collectCount = (post.collectCount || 0) + 1
      }
      this.setData({ post })
    } catch (e) {
      console.error('收藏失败', e)
    }
  },

  async toggleFollow() {
    if (!app.globalData.isLogin) {
      wx.navigateTo({ url: '/pages/login/login' })
      return
    }
    
    const userId = this.data.post?.userId
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
    }
  },

  goToUser(e) {
    const { id } = e.currentTarget.dataset
    wx.navigateTo({ url: `/pages/user/user?id=${id}` })
  },

  async toggleCommentLike(e) {
    if (!app.globalData.isLogin) {
      wx.navigateTo({ url: '/pages/login/login' })
      return
    }
  },
})
