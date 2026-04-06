import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000,
})

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    const url = config.url || ''
    if (url.startsWith('/admin')) {
      const adminToken = localStorage.getItem('adminToken')
      if (adminToken) {
        config.headers.Authorization = `Bearer ${adminToken}`
      }
      const adminId = localStorage.getItem('adminId')
      if (adminId) {
        config.headers['X-Admin-Id'] = adminId
      }
    } else {
      const token = localStorage.getItem('accessToken')
      if (token) {
        config.headers.Authorization = `Bearer ${token}`
      }
      const userId = localStorage.getItem('userId')
      if (userId) {
        config.headers['X-User-Id'] = userId
      }
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截器 → 【只修改这里：管理员响应头】
request.interceptors.response.use(
  (response) => {
    const config = response.config
    const url = config.url || ''
    const data = response.data

    // ======================
    // 管理员：正确响应头
    // ======================
    if (url.startsWith('/admin')) {
      const newAccessToken = response.headers['new-access-token']
      const newRefreshToken = response.headers['new-refresh-token']
      if (newAccessToken) {
        localStorage.setItem('adminToken', newAccessToken)
      }
      if (newRefreshToken) {
        localStorage.setItem('adminRefreshToken', newRefreshToken)
      }
    }

    // 用户端不动
    else {
      const newToken = response.headers['x-new-token']
      if (newToken) {
        localStorage.setItem('accessToken', newToken)
      }
    }

    if (data.code === 200) {
      return data.data
    }
    ElMessage.error(data.message || '请求失败')
    return Promise.reject(new Error(data.message || '请求失败'))
  },
  (error) => {
    const url = error.config?.url || ''
    if (error.response?.status === 401) {
      if (url.startsWith('/admin')) {
        localStorage.removeItem('adminToken')
        localStorage.removeItem('adminRefreshToken')
        localStorage.removeItem('adminId')
        localStorage.removeItem('adminUsername')
      } else {
        localStorage.removeItem('accessToken')
        localStorage.removeItem('refreshToken')
        localStorage.removeItem('userId')
        localStorage.removeItem('username')
      }
      ElMessage.error('登录已过期，请重新登录')
      router.push('/login')
    } else if (error.response?.status === 429) {
      ElMessage.warning('操作过于频繁，请稍后再试')
    } else {
      ElMessage.error(error.response?.data?.message || '网络错误')
    }
    return Promise.reject(error)
  }
)

export default request