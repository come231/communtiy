<template>
  <div class="min-h-screen bg-gradient-to-br from-blue-50 via-white to-indigo-50 flex items-center justify-center p-4">
    <!-- 背景装饰 -->
    <div class="absolute inset-0 overflow-hidden pointer-events-none">
      <div class="absolute -top-40 -right-40 w-80 h-80 bg-blue-200/30 rounded-full blur-3xl"></div>
      <div class="absolute -bottom-40 -left-40 w-80 h-80 bg-indigo-200/30 rounded-full blur-3xl"></div>
    </div>

    <div class="relative w-full max-w-md">
      <!-- 登录卡片 -->
      <div class="bg-white/90 backdrop-blur-xl rounded-2xl shadow-2xl p-8 border border-white/50">
        <!-- Logo & 标题 -->
        <div class="text-center mb-8">
          <div class="inline-flex items-center justify-center w-16 h-16 rounded-2xl bg-gradient-to-br from-primary to-blue-600 shadow-lg shadow-primary/25 mb-4">
            <span class="text-white font-bold text-2xl">TC</span>
          </div>
          <h1 class="text-2xl font-bold text-gray-800">欢迎回来</h1>
          <p class="text-gray-500 mt-1 text-sm">登录 TechCommunity 继续探索</p>
        </div>

        <!-- 角色选择 -->
        <el-radio-group v-model="loginType" class="w-full mb-4">
          <el-radio-button value="user">用户登录</el-radio-button>
          <el-radio-button value="admin">管理员登录</el-radio-button>
        </el-radio-group>

        <!-- 表单 -->
        <el-form ref="formRef" :model="form" :rules="rules" @keyup.enter="handleLogin">
          <el-form-item prop="username">
            <el-input v-model="form.username" placeholder="用户名" size="large" :prefix-icon="User" />
          </el-form-item>

          <el-form-item prop="password">
            <el-input v-model="form.password" type="password" placeholder="密码" size="large" :prefix-icon="Lock" show-password />
          </el-form-item>

          <el-button type="primary" size="large" class="w-full !rounded-xl !h-11 !text-base font-medium" 
            :loading="loading" @click="handleLogin">
            {{ loading ? '登录中...' : '登 录' }}
          </el-button>
        </el-form>

        <!-- 底部提示 -->
        <div class="mt-6 text-center text-sm text-gray-400">
          <template v-if="loginType === 'user'">
            测试账号: user001 / 123456
          </template>
          <template v-else>
            管理员账号: admin / 123456
          </template>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useUserStore } from '../store/user'
import { useAdminStore } from '../store/admin'

const router = useRouter()
const userStore = useUserStore()
const adminStore = useAdminStore()
const formRef = ref()
const loading = ref(false)
const loginType = ref<'user' | 'admin'>('user')

const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

async function handleLogin() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    if (loginType.value === 'user') {
      await userStore.login(form)
      ElMessage.success('登录成功！')
      router.push('/home')
    } else {
      await adminStore.login(form)
      ElMessage.success('管理员登录成功！')
      
      router.push('/admin')
    }
  } catch (e: any) {
    // 错误已在拦截器处理
  } finally {
    loading.value = false
  }
}
</script>
