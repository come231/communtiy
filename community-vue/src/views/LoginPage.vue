<template>
  <div
    class="relative flex min-h-screen items-center justify-center overflow-hidden p-4"
    style="
      background: radial-gradient(ellipse 80% 50% at 50% -20%, rgba(99, 102, 241, 0.25), transparent),
        radial-gradient(ellipse 50% 40% at 100% 60%, rgba(236, 72, 153, 0.12), transparent),
        #0b1220;
    "
  >
    <div class="pointer-events-none absolute inset-0 overflow-hidden">
      <div class="absolute -left-32 top-1/4 h-72 w-72 rounded-full bg-violet-600/20 blur-[100px]" />
      <div class="absolute -right-24 bottom-1/4 h-80 w-80 rounded-full bg-indigo-600/20 blur-[100px]" />
    </div>

    <div class="relative w-full max-w-md">
      <div
        class="rounded-2xl border border-white/10 bg-slate-900/70 p-8 shadow-glow backdrop-blur-xl sm:p-10"
      >
        <div class="mb-8 text-center">
          <div
            class="mx-auto mb-4 flex h-16 w-16 items-center justify-center rounded-2xl bg-gradient-to-br from-indigo-500 via-violet-500 to-fuchsia-500 shadow-glow-sm"
          >
            <span class="text-xl font-bold text-white">星</span>
          </div>
          <h1 class="text-2xl font-bold tracking-tight text-white">欢迎回来</h1>
          <p class="mt-1.5 text-sm text-slate-400">登录星云社区，继续你的讨论</p>
        </div>

        <el-radio-group v-model="loginType" class="mb-5 !flex w-full rounded-xl !bg-slate-800/80 !p-1">
          <el-radio-button value="user" class="!flex-1 !rounded-lg">用户登录</el-radio-button>
          <el-radio-button value="admin" class="!flex-1 !rounded-lg">管理员</el-radio-button>
        </el-radio-group>

        <el-form ref="formRef" :model="form" :rules="rules" class="login-form" @keyup.enter="handleLogin">
          <el-form-item prop="username">
            <el-input v-model="form.username" placeholder="用户名" size="large" :prefix-icon="User" />
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="密码"
              size="large"
              :prefix-icon="Lock"
              show-password
            />
          </el-form-item>

          <el-button
            type="primary"
            size="large"
            class="login-submit w-full !h-12 !rounded-xl !text-base !font-semibold !border-0"
            :loading="loading"
            @click="handleLogin"
          >
            {{ loading ? '登录中…' : '登 录' }}
          </el-button>
        </el-form>

        <div class="mt-6 text-center text-xs text-slate-500">
          <template v-if="loginType === 'user'"> 测试账号：user001 / 123456 </template>
          <template v-else> 管理员：admin / 123456 </template>
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

<style scoped>
.login-form :deep(.el-input__wrapper) {
  border-radius: 0.75rem;
  background: rgba(15, 23, 42, 0.65);
  box-shadow: none;
  border: 1px solid rgba(148, 163, 184, 0.15);
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}
.login-form :deep(.el-input__wrapper:hover) {
  border-color: rgba(129, 140, 248, 0.35);
}
.login-form :deep(.el-input__wrapper.is-focus) {
  border-color: rgba(129, 140, 248, 0.55);
  box-shadow: 0 0 0 1px rgba(99, 102, 241, 0.25);
}
.login-form :deep(.el-form-item) {
  margin-bottom: 1.1rem;
}
.login-submit {
  background-image: linear-gradient(120deg, #6366f1 0%, #7c3aed 55%, #a855f7 100%) !important;
}
.login-submit:hover {
  filter: brightness(1.06);
}
:deep(.el-radio-button__inner) {
  width: 100%;
  border: none !important;
  background: transparent !important;
  color: #94a3b8 !important;
  font-weight: 500;
}
:deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background: rgba(99, 102, 241, 0.25) !important;
  color: #e2e8f0 !important;
  box-shadow: none !important;
}
</style>
