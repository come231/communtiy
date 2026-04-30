// 星云社区 — 统一 HTTP 封装（经网关访问各微服务，响应体为 Result<T>：code / message / data）
var appConfig = require('../config.js')
var cfg = appConfig.getConfig()

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

  Request.prototype.getBaseUrl = function() {
    return cfg.baseUrl
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

  /** 解析网关 JSON；非 JSON（如网关 HTML 错误页）直接提示 */
  Request.prototype.parseResult = function(raw) {
    if (raw == null || typeof raw !== 'object') {
      return { ok: false, err: '服务返回格式异常' }
    }
    if (typeof raw.code !== 'number') {
      return { ok: false, err: '服务返回格式异常' }
    }
    return { ok: true, payload: raw }
  }

  Request.prototype.request = function(options) {
    var _this2 = this
    var url = options.url
    var method = options.method || 'GET'
    var data = options.data
    var header = options.header || {}
    var loading = options.loading !== false

    return new Promise(function(resolve, reject) {
      if (loading) {
        wx.showLoading({ title: '加载中...', mask: true })
      }

      wx.request({
        url: cfg.baseUrl + url,
        method: method,
        data: data,
        header: Object.assign({}, _this2.getHeader(), header),
        timeout: cfg.requestTimeoutMs || 30000,
        success: function(res) {
          if (loading) {
            wx.hideLoading()
          }
          if (res.statusCode >= 200 && res.statusCode < 300) {
            var parsed = _this2.parseResult(res.data)
            if (!parsed.ok) {
              wx.showToast({ title: parsed.err, icon: 'none' })
              reject(new Error(parsed.err))
              return
            }
            var response = parsed.payload
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
              reject(new Error(response.message || '请求失败'))
            }
          } else {
            wx.showToast({ title: 'HTTP ' + res.statusCode, icon: 'none' })
            reject(res)
          }
        },
        fail: function(err) {
          if (loading) {
            wx.hideLoading()
          }
          if (err.errMsg && err.errMsg.indexOf('request:fail') !== -1) {
            wx.showToast({
              title: '网络请求失败，请检查网关地址与域名白名单',
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
