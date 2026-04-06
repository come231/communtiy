import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import request from '../utils/request'
import type { UserInfo } from '../types'

interface LoginResponse {
  accessToken: string
  refreshToken: string
  userId: number
  username: string
  nickname: string
  avatar: string
  role: number
}

interface UserInfoResponse {
  nickname: string
  avatar: string
  username: string
  [key: string]: any
}

export const useUserStore = defineStore('user', () => {
  const userId = ref<string | null>(localStorage.getItem('userId'))
  const username = ref<string | null>(localStorage.getItem('username'))
  const nickname = ref<string>('')
  const avatar = ref<string>('')
  const token = ref<string | null>(localStorage.getItem('accessToken'))

  const isLoggedIn = computed(() => !!token.value && !!userId.value)

  async function login(loginForm: { username: string; password: string }) {
    const res = await request.post<LoginResponse>('/user/login', loginForm) as any
    token.value = res.accessToken
    userId.value = String(res.userId)
    username.value = res.username
    localStorage.setItem('accessToken', res.accessToken)
    localStorage.setItem('refreshToken', res.refreshToken)
    localStorage.setItem('userId', String(res.userId))
    localStorage.setItem('username', res.username)
    return res
  }

  function logout() {
    token.value = null
    userId.value = null
    username.value = null
    nickname.value = ''
    avatar.value = ''
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
    localStorage.removeItem('userId')
    localStorage.removeItem('username')
  }

  async function fetchUserInfo() {
    if (!userId.value) return
    try {
      const res = await request.get<UserInfoResponse>('/user/my') as any
      nickname.value = res.nickname || ''
      avatar.value = res.avatar || ''
    } catch {
      // ignore
    }
  }

  return { userId, username, nickname, avatar, token, isLoggedIn, login, logout, fetchUserInfo }
})
