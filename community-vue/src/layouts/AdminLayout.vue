<template>
  <div class="min-h-screen bg-gray-50 flex">
    <!-- 侧边栏 -->
    <aside class="w-64 bg-slate-900 min-h-screen fixed left-0 top-0 bottom-0 text-white">
      <!-- Logo区 -->
      <div class="h-16 flex items-center px-6 border-b border-slate-700">
        <div class="flex items-center gap-2">
          <div class="w-8 h-8 rounded-lg bg-gradient-to-br from-blue-400 to-blue-600 flex items-center justify-center">
            <span class="text-white font-bold text-sm">TC</span>
          </div>
          <span class="font-bold text-lg">管理后台</span>
        </div>
      </div>

      <!-- 菜单 -->
      <nav class="p-4 space-y-1">
        <router-link to="/admin/dashboard" class="menu-item" :class="{ active: $route.path === '/admin/dashboard' }">
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2V6zM14 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2V6zM4 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2v-2zM14 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2v-2z"/></svg>
          数据仪表盘
        </router-link>
        <router-link to="/admin/users" class="menu-item" :class="{ active: $route.path === '/admin/users' }">
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z"/></svg>
          用户管理
        </router-link>
        <router-link to="/admin/posts" class="menu-item" :class="{ active: $route.path === '/admin/posts' }">
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 20H5a2 2 0 01-2-2V6a2 2 0 012-2h10a2 2 0 012 2v1m2 13a2 2 0 01-2-2V7m2 13a2 2 0 002-2V9a2 2 0 00-2-2h-2m-4-3H9M7 16h6M7 8h6v4H7V8z"/></svg>
          内容管理
        </router-link>
        <router-link to="/admin/reports" class="menu-item" :class="{ active: $route.path === '/admin/reports' }">
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L4.082 16.5c-.77.833.192 2.5 1.732 2.5z"/></svg>
          举报处理
        </router-link>
        <router-link to="/admin/sensitive" class="menu-item" :class="{ active: $route.path === '/admin/sensitive' }">
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M18.364 18.364A9 9 0 005.636 5.636m12.728 12.728A9 9 0 015.636 5.636m12.728 12.728L5.636 5.636m12.728 12.728L5.636 5.636"/></svg>
          敏感词管理
        </router-link>
        <router-link to="/admin/messages" class="menu-item" :class="{ active: $route.path === '/admin/messages' }">
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"/></svg>
          消息管理
        </router-link>
        <router-link to="/admin/notices" class="menu-item" :class="{ active: $route.path === '/admin/notices' }">
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9"/></svg>
          通知管理
        </router-link>
        <router-link to="/admin/ai" class="menu-item" :class="{ active: $route.path === '/admin/ai' }">
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9.75 17L9 20l-1 1h8l-1-1-.75-3M3 13h18M5 17h14a2 2 0 002-2V5a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"/></svg>
          AI管理
        </router-link>
        <router-link to="/admin/config" class="menu-item" :class="{ active: $route.path === '/admin/config' }">
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.066 2.573c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.573 1.066c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.066-2.573c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z"/><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/></svg>
          系统配置
        </router-link>
      </nav>
    </aside>

    <!-- 右侧内容 -->
    <div class="ml-64 flex-1 min-h-screen">
      <!-- 顶栏 -->
      <header class="h-16 bg-white border-b border-gray-200 flex items-center justify-between px-8 sticky top-0 z-30">
        <div class="flex items-center gap-2 text-sm text-gray-500">
          <span>管理后台</span>
          <span>/</span>
          <span class="text-gray-800 font-medium">{{ $route.meta.title || '仪表盘' }}</span>
        </div>
        <div class="flex items-center gap-4">
          <el-dropdown trigger="click">
            <div class="flex items-center gap-2 cursor-pointer hover:bg-gray-100 rounded-lg px-3 py-1.5 transition-colors">
              <img src="https://placehold.co/28x28/409EFF/fff?text=A" class="w-7 h-7 rounded-full" />
              <span class="text-sm font-medium">管理员</span>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item>修改密码</el-dropdown-item>
                <el-dropdown-item divided @click="$router.push('/login')">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>
      <!-- 页面内容 -->
      <main class="p-8">
        <router-view />
      </main>
    </div>
  </div>
</template>

<style scoped>
.menu-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.625rem 0.875rem;
  border-radius: 0.5rem;
  font-size: 0.875rem;
  color: #94a3b8;
  transition: all 0.2s;
}
.menu-item:hover {
  background: rgba(255,255,255,0.08);
  color: #e2e8f0;
}
.menu-item.active {
  background: linear-gradient(135deg, #409EFF, #337ECC);
  color: white;
}
</style>
