// 请求工具类
var BASE_URL = 'http://localhost:8095'

var Request = (function() {
  function Request() {
    this.instance = null
  }

  Request.getInstance = function() {
    if (!Request.instance) {
      Request.instance = new Request()
    }
    return Request.instance
  }

  Request.prototype.getHeader = function() {
    var header = {
      'Content-Type': 'application/json',
    }
    var token = wx.getStorageSync('accessToken')
    if (token) {
      header['Authorization'] = 'Bearer ' + token
    }
    var userInfo = wx.getStorageSync('userInfo')
    if (userInfo && userInfo.id) {
      header['X-User-Id'] = String(userInfo.id)
    }
    return header
  }

  Request.prototype.request = function(options) {
    var _this = this
    var url = options.url
    var method = options.method || 'GET'
    var data = options.data
    var header = options.header || {}
    var loading = options.loading !== false

    var _this2 = this
    return new Promise(function(resolve, reject) {
      if (loading) {
        wx.showLoading({ title: '加载中...', mask: true })
      }

      wx.request({
        url: BASE_URL + url,
        method: method,
        data: data,
        header: Object.assign({}, _this2.getHeader(), header),
        success: function(res) {
          if (loading) {
            wx.hideLoading()
          }
          if (res.statusCode >= 200 && res.statusCode < 300) {
            var response = res.data
            if (response.code === 200) {
              resolve(response.data)
            } else if (response.code === 401) {
              wx.removeStorageSync('accessToken')
              wx.removeStorageSync('userInfo')
              wx.redirectTo({ url: '/pages/login/login' })
              reject(new Error('登录已过期'))
            } else {
              wx.showToast({
                title: response.message || '请求失败',
                icon: 'none',
                duration: 2000,
              })
              reject(new Error(response.message))
            }
          } else {
            reject(res)
          }
        },
        fail: function(err) {
          if (loading) {
            wx.hideLoading()
          }
          if (err.errMsg && err.errMsg.indexOf('request:fail') !== -1) {
            wx.showToast({
              title: '网络请求失败',
              icon: 'none',
            })
          }
          reject(err)
        },
      })
    })
  }

  Request.prototype.get = function(url, data, loading) {
    if (loading === undefined) loading = true
    return this.request({ url: url, method: 'GET', data: data, loading: loading })
  }

  Request.prototype.post = function(url, data, loading) {
    if (loading === undefined) loading = true
    return this.request({ url: url, method: 'POST', data: data, loading: loading })
  }

  Request.prototype.put = function(url, data, loading) {
    if (loading === undefined) loading = true
    return this.request({ url: url, method: 'PUT', data: data, loading: loading })
  }

  Request.prototype.delete = function(url, data, loading) {
    if (loading === undefined) loading = true
    return this.request({ url: url, method: 'DELETE', data: data, loading: loading })
  }

  return Request
})()

module.exports = {
  request: Request.getInstance(),
  Request: Request,
}
