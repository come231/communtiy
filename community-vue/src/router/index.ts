import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    component: () => import('../layouts/UserLayout.vue'),
    children: [
      { path: '', redirect: '/home' },
      { path: 'home', name: 'Home', component: () => import('../views/user/home/HomePage.vue'), meta: { title: '首页' } },
      { path: 'post/:id', name: 'PostDetail', component: () => import('../views/user/post/PostDetail.vue'), meta: { title: '帖子详情' } },
      { path: 'post/create', name: 'PostCreate', component: () => import('../views/user/post/PostCreate.vue'), meta: { title: '发布帖子' } },
      { path: 'section', name: 'Section', component: () => import('../views/user/section/SectionPage.vue'), meta: { title: '板块列表' } },
      { path: 'section/:id', name: 'SectionDetail', component: () => import('../views/user/section/SectionDetail.vue'), meta: { title: '板块详情' } },
      { path: 'topic/:id', name: 'Topic', component: () => import('../views/user/topic/TopicPage.vue'), meta: { title: '话题' } },
      { path: 'search', name: 'Search', component: () => import('../views/user/search/SearchPage.vue'), meta: { title: '搜索' } },
      { path: 'profile', name: 'Profile', component: () => import('../views/user/profile/ProfilePage.vue'), meta: { title: '个人中心' } },
      { path: 'user/:id', name: 'UserProfile', component: () => import('../views/user/user/UserProfile.vue'), meta: { title: '用户主页' } },
      { path: 'message', name: 'Message', component: () => import('../views/user/message/MessagePage.vue'), meta: { title: '消息' } },
      { path: 'ai', name: 'AiChat', component: () => import('../views/user/ai/AiChatPage.vue'), meta: { title: 'AI助手' } },
    ],
  },
  {
    path: '/admin',
    component: () => import('../layouts/AdminLayout.vue'),
    children: [
      { path: '', redirect: '/admin/dashboard' },
      { path: 'dashboard', name: 'AdminDashboard', component: () => import('../views/admin/dashboard/DashboardPage.vue'), meta: { title: '数据仪表盘' } },
      { path: 'users', name: 'AdminUsers', component: () => import('../views/admin/user/UserManage.vue'), meta: { title: '用户管理' } },
      { path: 'posts', name: 'AdminPosts', component: () => import('../views/admin/content/PostManage.vue'), meta: { title: '帖子管理' } },
      { path: 'reports', name: 'AdminReports', component: () => import('../views/admin/report/ReportManage.vue'), meta: { title: '举报处理' } },
      { path: 'sensitive', name: 'AdminSensitive', component: () => import('../views/admin/sensitive/SensitiveWordManage.vue'), meta: { title: '敏感词管理' } },
      { path: 'config', name: 'AdminConfig', component: () => import('../views/admin/config/SystemConfig.vue'), meta: { title: '系统配置' } },
      { path: 'messages', name: 'AdminMessages', component: () => import('../views/admin/message/MessageManage.vue'), meta: { title: '消息管理' } },
      { path: 'notices', name: 'AdminNotices', component: () => import('../views/admin/notice/NoticeManage.vue'), meta: { title: '通知管理' } },
      { path: 'ai', name: 'AdminAi', component: () => import('../views/admin/ai/AiManage.vue'), meta: { title: 'AI管理' } },
    ],
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/LoginPage.vue'),
    meta: { title: '登录' },
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  },
})

router.beforeEach((to, _from, next) => {
  const userToken = localStorage.getItem('accessToken')
  const adminToken = localStorage.getItem('adminToken')

  // 管理后台路由守卫
  if (to.path.startsWith('/admin') && to.path !== '/login') {
    if (!adminToken) {
      next('/login')
      return
    }
  }

  document.title = `${to.meta.title || '社区'} - TechCommunity`
  next()
})

export default router
