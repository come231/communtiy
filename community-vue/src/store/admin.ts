import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import request from '../utils/request'

interface AdminLoginResponse {
  accessToken: string
  refreshToken: string
  adminId: number
  username: string
  realName?: string
  role?: string
}

export const useAdminStore = defineStore('admin', () => {
  const adminId = ref<string | null>(localStorage.getItem('adminId'))
  const username = ref<string | null>(localStorage.getItem('adminUsername'))
  const realName = ref<string>('')
  const role = ref<string>('')
  const token = ref<string | null>(localStorage.getItem('adminToken'))
  const refreshToken = ref<string | null>(localStorage.getItem('adminRefreshToken'))

  const isLoggedIn = computed(() => !!token.value && !!adminId.value)

  async function login(loginForm: { username: string; password: string }) {
    const res = await request.post<AdminLoginResponse>('/admin/login', loginForm) as any
    token.value = res.accessToken
    refreshToken.value = res.refreshToken
    adminId.value = String(res.adminId)
    username.value = res.username
    realName.value = res.realName || ''
    role.value = res.role || ''
    localStorage.setItem('adminToken', res.accessToken)
    localStorage.setItem('adminRefreshToken', res.refreshToken)
    localStorage.setItem('adminId', String(res.adminId))
    localStorage.setItem('adminUsername', res.username)
    localStorage.setItem('adminRealName', res.realName || '')
    localStorage.setItem('adminRole', res.role || '')
    return res
  }

  function logout() {
    request.post('/admin/logout').catch(() => {})
    token.value = null
    refreshToken.value = null
    adminId.value = null
    username.value = null
    realName.value = ''
    role.value = ''
    localStorage.removeItem('adminToken')
    localStorage.removeItem('adminRefreshToken')
    localStorage.removeItem('adminId')
    localStorage.removeItem('adminUsername')
    localStorage.removeItem('adminRealName')
    localStorage.removeItem('adminRole')
  }

  return { adminId, username, realName, role, token, refreshToken, isLoggedIn, login, logout }
})
