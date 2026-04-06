// 请求工具类
const BASE_URL = 'http://localhost:8095'

interface RequestOptions {
  url: string
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE'
  data?: any
  header?: Record<string, string>
  loading?: boolean
}

interface RequestSuccess {
  code: number
  data: any
  message: string
}

export class Request {
  private static instance: Request

  static getInstance(): Request {
    if (!Request.instance) {
      Request.instance = new Request()
    }
    return Request.instance
  }

  private getHeader(): Record<string, string> {
    const header: Record<string, string> = {
      'Content-Type': 'application/json',
    }
    const token = wx.getStorageSync('accessToken')
    if (token) {
      header['Authorization'] = `Bearer ${token}`
    }
    const userId = wx.getStorageSync('userInfo')?.id
    if (userId) {
      header['X-User-Id'] = String(userId)
    }
    return header
  }

  async request<T = any>(options: RequestOptions): Promise<T> {
    const { url, method = 'GET', data, header = {}, loading = true } = options

    if (loading) {
      wx.showLoading({ title: '加载中...', mask: true })
    }

    try {
      const response = await this.requestPromise<RequestSuccess>({
        url: BASE_URL + url,
        method,
        data,
        header: {
          ...this.getHeader(),
          ...header,
        },
      })

      if (loading) {
        wx.hideLoading()
      }

      if (response.code === 200) {
        return response.data
      }

      // 处理登录过期
      if (response.code === 401) {
        wx.removeStorageSync('accessToken')
        wx.removeStorageSync('userInfo')
        wx.redirectTo({ url: '/pages/login/login' })
        throw new Error('登录已过期')
      }

      wx.showToast({
        title: response.message || '请求失败',
        icon: 'none',
        duration: 2000,
      })
      throw new Error(response.message)
    } catch (error: any) {
      if (loading) {
        wx.hideLoading()
      }
      if (error.errMsg && error.errMsg.includes('request:fail')) {
        wx.showToast({
          title: '网络请求失败',
          icon: 'none',
        })
      }
      throw error
    }
  }

  private requestPromise<T>(options: {
    url: string
    method: string
    data?: any
    header?: Record<string, string>
  }): Promise<T> {
    return new Promise((resolve, reject) => {
      wx.request({
        ...options,
        success: (res) => {
          if (res.statusCode >= 200 && res.statusCode < 300) {
            resolve(res.data as T)
          } else {
            reject(res)
          }
        },
        fail: reject,
      })
    })
  }

  get<T = any>(url: string, data?: any, loading = true): Promise<T> {
    return this.request<T>({ url, method: 'GET', data, loading })
  }

  post<T = any>(url: string, data?: any, loading = true): Promise<T> {
    return this.request<T>({ url, method: 'POST', data, loading })
  }

  put<T = any>(url: string, data?: any, loading = true): Promise<T> {
    return this.request<T>({ url, method: 'PUT', data, loading })
  }

  delete<T = any>(url: string, data?: any, loading = true): Promise<T> {
    return this.request<T>({ url, method: 'DELETE', data, loading })
  }
}

export const request = Request.getInstance()
