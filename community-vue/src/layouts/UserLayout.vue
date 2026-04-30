<template>
  <div class="page-user-shell min-h-screen text-light overflow-x-hidden">
    <div id="particles-js" class="fixed inset-0 z-0 pointer-events-none opacity-90" aria-hidden="true" />

    <header
      class="navbar w-full top-0 z-50 py-3 border-b border-white/[0.06] bg-dark/40 backdrop-blur-xl supports-[backdrop-filter]:bg-dark/25"
      id="navbar"
    >
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-3 flex items-center justify-between gap-4">
        <a href="#" class="flex items-center gap-3 shrink-0 group" @click.prevent="$router.push('/')">
          <div
            class="w-10 h-10 rounded-2xl bg-gradient-to-br from-indigo-500 via-violet-500 to-fuchsia-500 flex items-center justify-center shadow-glow-sm transition-transform duration-300 group-hover:scale-105"
          >
            <i class="fa fa-star-o text-lg text-white/95" />
          </div>
          <div class="leading-tight">
            <h1 class="text-lg sm:text-xl font-bold text-gradient tracking-tight">星云社区</h1>
            <p class="text-[10px] sm:text-xs text-slate-500 font-medium hidden sm:block tracking-wide">连接 · 分享 · 成长</p>
          </div>
        </a>

        <nav class="hidden md:flex items-center gap-1">
          <router-link
            v-for="link in navLinks"
            :key="link.to"
            :to="link.to"
            class="nav-pill px-3.5 py-2 rounded-xl text-sm font-medium text-slate-300 hover:text-white hover:bg-white/[0.06] transition-all duration-200"
            active-class="nav-pill-active !text-white"
          >
            <span class="inline-flex items-center gap-1.5">
              <i v-if="link.icon" :class="[link.icon, 'text-xs opacity-80']" />
              {{ link.label }}
            </span>
          </router-link>
        </nav>

        <div class="flex items-center gap-2 sm:gap-3">
          <template v-if="userStore.isLoggedIn">
            <button
              type="button"
              @click="$router.push('/message')"
              class="relative p-2.5 rounded-xl text-slate-300 hover:text-white hover:bg-white/[0.08] transition-colors focus-visible:ring-2 focus-visible:ring-indigo-400/50"
              aria-label="消息"
            >
              <i class="fa fa-bell text-lg" />
              <span class="absolute top-1.5 right-1.5 w-2 h-2 bg-rose-500 rounded-full ring-2 ring-dark" />
            </button>
            <button
              type="button"
              class="hidden sm:inline-flex items-center gap-1.5 px-4 py-2 rounded-xl text-sm font-medium border border-indigo-500/35 text-indigo-200 hover:bg-indigo-500/15 hover:border-indigo-400/50 transition-all"
              @click="$router.push('/post/create')"
            >
              <i class="fa fa-plus text-xs" />发布
            </button>
            <el-dropdown trigger="click" :teleported="true">
              <div
                class="flex items-center gap-2 cursor-pointer pl-1 pr-2 py-1 rounded-xl hover:bg-white/[0.06] transition-colors max-w-[140px]"
              >
                <img
                  :src="resolveFileUrl(userStore.avatar) || 'https://placehold.co/32x33/e0e7ff/6366f1?text=U'"
                  class="w-8 h-8 rounded-xl object-cover ring-2 ring-white/10"
                  alt=""
                  referrerpolicy="no-referrer"
                />
                <span class="text-sm font-medium text-slate-200 truncate">{{ userStore.nickname || userStore.username }}</span>
              </div>
              <template #dropdown>
                <el-dropdown-menu class="!bg-slate-900/95 !border-slate-700/80 backdrop-blur-xl">
                  <el-dropdown-item @click="$router.push('/profile')">个人中心</el-dropdown-item>
                  <el-dropdown-item @click="$router.push(`/user/${userStore.userId}`)">我的主页</el-dropdown-item>
                  <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <button
              type="button"
              class="px-4 py-2 rounded-xl text-sm font-medium border border-white/15 text-slate-200 hover:bg-white/[0.06] transition-all"
              @click="$router.push('/login')"
            >
              登录
            </button>
            <button type="button" class="btn-gradient hidden sm:inline-flex text-sm" @click="$router.push('/login')">注册</button>
          </template>
          <button
            type="button"
            class="md:hidden p-2 rounded-xl text-slate-200 hover:bg-white/10 transition-colors"
            id="menuBtn"
            aria-label="菜单"
            @click="toggleMobileMenu"
          >
            <i class="fa text-xl" :class="mobileMenuOpen ? 'fa-times' : 'fa-bars'" />
          </button>
        </div>
      </div>
    </header>

    <transition name="fade">
      <div
        v-if="mobileMenuOpen"
        class="fixed inset-0 z-40 bg-black/50 backdrop-blur-sm md:hidden"
        @click="mobileMenuOpen = false"
      />
    </transition>

    <div
      id="mobileMenu"
      class="fixed top-[4.25rem] right-0 w-[min(100%,20rem)] glass rounded-l-2xl p-5 z-50 transform translate-x-full transition-transform duration-300 ease-out shadow-card md:hidden border-r-0 border-y border-l border-white/10"
      :class="{ '!translate-x-0': mobileMenuOpen }"
    >
      <nav class="flex flex-col gap-1">
        <router-link
          v-for="link in navLinks"
          :key="'m-' + link.to"
          :to="link.to"
          class="py-3 px-3 rounded-xl text-slate-200 hover:bg-white/[0.06] transition-colors"
          active-class="bg-indigo-500/20 text-white font-medium"
          @click="mobileMenuOpen = false"
        >
          {{ link.label }}
        </router-link>
        <template v-if="userStore.isLoggedIn">
          <router-link
            to="/message"
            class="py-3 px-3 rounded-xl text-slate-200 hover:bg-white/[0.06]"
            @click="mobileMenuOpen = false"
            >消息通知</router-link
          >
          <router-link
            to="/chat"
            class="py-3 px-3 rounded-xl text-slate-200 hover:bg-white/[0.06]"
            @click="mobileMenuOpen = false"
            >私信聊天</router-link
          >
          <router-link
            to="/post/create"
            class="py-3 px-3 rounded-xl text-slate-200 hover:bg-white/[0.06]"
            @click="mobileMenuOpen = false"
            >发布</router-link
          >
          <router-link
            to="/profile"
            class="py-3 px-3 rounded-xl text-slate-200 hover:bg-white/[0.06]"
            @click="mobileMenuOpen = false"
            >个人中心</router-link
          >
          <router-link
            :to="`/user/${userStore.userId}`"
            class="py-3 px-3 rounded-xl text-slate-200 hover:bg-white/[0.06]"
            @click="mobileMenuOpen = false"
            >我的主页</router-link
          >
          <div class="border-t border-white/10 mt-2 pt-2">
            <button
              type="button"
              class="w-full text-left py-3 px-3 rounded-xl text-rose-300/90 hover:bg-rose-500/10"
              @click="
                handleLogout();
                mobileMenuOpen = false;
              "
            >
              退出登录
            </button>
          </div>
        </template>
        <template v-else>
          <router-link
            to="/login"
            class="py-3 px-3 rounded-xl text-slate-200 hover:bg-white/[0.06]"
            @click="mobileMenuOpen = false"
            >登录 / 注册</router-link
          >
        </template>
      </nav>
    </div>

    <main class="relative z-10 pt-[4.25rem] min-h-[calc(100vh-4.25rem)]">
      <router-view v-slot="{ Component, route }">
        <!-- 不使用 mode=out-in：旧页先完全淡出再切入新页时，部分含 Dialog/复杂结构的页面会与 Transition 配合异常，导致整区长期停留在 opacity:0（黑屏），需刷新才恢复 -->
        <transition name="page-fade">
          <component :is="Component" :key="route.fullPath" />
        </transition>
      </router-view>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useUserStore } from '../store/user'
import { resolveFileUrl } from '../utils/resolveFileUrl'

const userStore = useUserStore()
const mobileMenuOpen = ref(false)

const navLinks = [
  { to: '/home', label: '首页' },
  { to: '/section', label: '板块' },
  { to: '/topic', label: '话题' },
  { to: '/search', label: '搜索', icon: 'fa fa-search' },
  { to: '/ai', label: 'AI助手' },
]

function handleLogout() {
  userStore.logout()
  window.location.href = '/login'
}

function toggleMobileMenu() {
  mobileMenuOpen.value = !mobileMenuOpen.value
}

function handleScroll() {
  const navbar = document.getElementById('navbar')
  if (!navbar) return
  if (window.scrollY > 24) {
    navbar.classList.add('shadow-card', 'border-white/[0.08]')
    navbar.classList.add('py-2')
    navbar.classList.remove('py-3')
  } else {
    navbar.classList.remove('shadow-card', 'border-white/[0.08]')
    navbar.classList.add('py-3')
    navbar.classList.remove('py-2')
  }
}

onMounted(() => {
  window.addEventListener('scroll', handleScroll, { passive: true })
  if (typeof window !== 'undefined' && (window as any).particlesJS) {
    ;(window as any).particlesJS('particles-js', {
      particles: {
        number: { value: 56, density: { enable: true, value_area: 900 } },
        color: { value: '#a5b4fc' },
        shape: { type: 'circle' },
        opacity: { value: 0.35, random: true },
        size: { value: 2.2, random: true },
        line_linked: {
          enable: true,
          distance: 140,
          color: '#818cf8',
          opacity: 0.12,
          width: 1,
        },
        move: {
          enable: true,
          speed: 0.65,
          direction: 'none',
          random: true,
          straight: false,
          out_mode: 'out',
          bounce: false,
        },
      },
      interactivity: {
        detect_on: 'canvas',
        events: {
          onhover: { enable: true, mode: 'grab' },
          onclick: { enable: false, mode: 'push' },
          resize: true,
        },
      },
      retina_detect: true,
    })
  }
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<style scoped>
.nav-pill.router-link-active,
.nav-pill-active {
  background: rgba(99, 102, 241, 0.18);
  box-shadow: 0 0 0 1px rgba(129, 140, 248, 0.25);
}

.page-fade-enter-active,
.page-fade-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}
.page-fade-enter-from {
  opacity: 0;
  transform: translateY(6px);
}
.page-fade-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.25s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
