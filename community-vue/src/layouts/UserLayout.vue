<template>
  <div class="min-h-screen bg-[#F5F7FA]">
    <!-- 顶部导航栏 -->
    <header class="fixed top-0 left-0 right-0 h-16 bg-white/95 backdrop-blur-sm border-b border-gray-200 z-50">
      <div class="max-w-7xl mx-auto h-full flex items-center justify-between px-4">
        <!-- Logo -->
        <div class="flex items-center gap-2 cursor-pointer" @click="$router.push('/')">
          <div class="w-9 h-9 rounded-xl bg-gradient-to-br from-primary to-blue-600 flex items-center justify-center">
            <span class="text-white font-bold text-lg">TC</span>
          </div>
          <span class="text-xl font-bold bg-gradient-to-r from-primary to-blue-600 bg-clip-text text-transparent">TechCommunity</span>
        </div>

        <!-- 导航链接 -->
        <nav class="hidden md:flex items-center gap-6">
          <router-link to="/home" class="text-sm font-medium text-gray-600 hover:text-primary transition-colors" active-class="!text-primary">首页</router-link>
          <router-link to="/section/1" class="text-sm font-medium text-gray-600 hover:text-primary transition-colors" active-class="!text-primary">板块</router-link>
          <router-link to="/search" class="text-sm font-medium text-gray-600 hover:text-primary transition-colors" active-class="!text-primary">搜索</router-link>
          <router-link to="/ai" class="text-sm font-medium text-gray-600 hover:text-primary transition-colors" active-class="!text-primary">AI助手</router-link>
        </nav>

        <!-- 用户区域 -->
        <div class="flex items-center gap-3">
          <template v-if="userStore.isLoggedIn">
            <!-- 消息入口 -->
            <button @click="$router.push('/message')" class="relative p-2 rounded-lg hover:bg-gray-100 transition-colors">
              <svg class="w-5 h-5 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9"/></svg>
              <span class="absolute -top-0.5 -right-0.5 w-2 h-2 bg-red-500 rounded-full"></span>
            </button>
            <!-- 发布按钮 -->
            <el-button type="primary" size="small" @click="$router.push('/post/create')">
              <span class="mr-1">+</span>发布
            </el-button>
            <!-- 用户头像下拉 -->
            <el-dropdown trigger="click">
              <div class="flex items-center gap-2 cursor-pointer p-1 pr-3 rounded-full hover:bg-gray-100 transition-colors">
                <img :src="userStore.avatar || 'https://placehold.co/32x33/e0e7ff/409EFF?text=U'" class="w-8 h-8 rounded-full object-cover border-2 border-white shadow-sm" />
                <span class="text-sm font-medium text-gray-700 max-w-[80px] truncate">{{ userStore.nickname || userStore.username }}</span>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="$router.push('/profile')">个人中心</el-dropdown-item>
                  <el-dropdown-item @click="$router.push(`/user/${userStore.userId}`)">我的主页</el-dropdown-item>
                  <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <el-button type="primary" @click="$router.push('/login')" round>登录 / 注册</el-button>
          </template>
        </div>
      </div>
    </header>

    <!-- 主内容区 pt-16 防止被导航栏遮挡 -->
    <main class="pt-16">
      <router-view />
    </main>
  </div>
</template>

<script setup lang="ts">
import { useUserStore } from '../store/user'

const userStore = useUserStore()

function handleLogout() {
  userStore.logout()
  window.location.href = '/login'
}
</script>
