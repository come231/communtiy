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
      { path: 'section/:id', name: 'Section', component: () => import('../views/user/section/SectionPage.vue'), meta: { title: '板块' } },
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
  
  
  document.title = `${to.meta.title || '社区'} - TechCommunity`
  next()
})

export default router
