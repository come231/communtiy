<template>
  <div class="space-y-6">
    <!-- 搜索和筛选 -->
    <div class="bg-white rounded-xl p-5 border border-gray-100 shadow-sm">
      <div class="flex flex-wrap items-center gap-4">
        <input
          v-model="searchKeyword"
          type="text"
          placeholder="搜索用户名/昵称..."
          class="px-4 py-2 bg-gray-50 border border-gray-200 rounded-lg text-sm outline-none focus:border-primary focus:ring-2 focus:ring-primary/20 w-64 transition-all"
          @keyup.enter="handleSearch"
        />
        <select v-model="filterStatus" class="px-3 py-2 bg-gray-50 border border-gray-200 rounded-lg text-sm outline-none focus:border-primary">
          <option :value="undefined">全部状态</option>
          <option :value="1">正常</option>
          <option :value="0">已禁用</option>
        </select>
        <button @click="handleSearch" class="px-4 py-2 bg-primary text-white text-sm rounded-lg hover:bg-blue-600 transition-colors">搜索</button>
        <button @click="handleReset" class="px-4 py-2 border border-gray-200 text-gray-600 text-sm rounded-lg hover:bg-gray-50 transition-colors">重置</button>
      </div>
    </div>

    <!-- 用户列表 -->
    <div class="bg-white rounded-xl border border-gray-100 shadow-sm overflow-hidden">
      <div class="overflow-x-auto">
        <table class="w-full text-sm">
          <thead class="bg-gray-50 border-b">
            <tr class="text-left text-xs text-gray-500 uppercase tracking-wider">
              <th class="px-5 py-3.5">用户</th>
              <th class="px-5 py-3.5">注册时间</th>
              <th class="px-5 py-3.5">帖子数</th>
              <th class="px-5 py-3.5">状态</th>
              <th class="px-5 py-3.5">操作</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-gray-50">
            <tr v-for="user in users" :key="user.id" class="hover:bg-gray-50/50 transition-colors">
              <td class="px-5 py-4">
                <div class="flex items-center gap-3">
                  <img :src="user.avatar || defaultAvatar" class="w-10 h-10 rounded-full object-cover bg-gray-100" alt="" />
                  <div>
                    <p class="font-medium text-gray-800">{{ user.nickname || user.username }}</p>
                    <p class="text-xs text-gray-400 mt-0.5">{{ user.phone || user.email || '-' }}</p>
                  </div>
                </div>
              </td>
              <td class="px-5 py-4 text-gray-600 text-xs">{{ formatTime(user.createTime || user.registerTime) }}</td>
              <td class="px-5 py-4">
                <div class="flex gap-3 text-xs">
                  <span class="text-gray-600">{{ user.postCount || 0 }} 帖子</span>
                  <span class="text-gray-400">{{ user.followerCount || 0 }} 粉丝</span>
                </div>
              </td>
              <td class="px-5 py-4">
                <span :class="['text-xs px-2.5 py-1 rounded-full font-medium', getStatusClass(user.status)]">
                  {{ getStatusText(user.status) }}
                </span>
              </td>
              <td class="px-5 py-4">
                <div class="flex items-center gap-2">
                  <button @click="showDetail(user)" class="text-primary hover:text-blue-700 text-xs font-medium">详情</button>
                  <button
                    v-if="user.status === 1"
                    @click="handleBan(user)"
                    class="text-orange-500 hover:text-orange-700 text-xs font-medium"
                  >禁用</button>
                  <button
                    v-else
                    @click="handleUnban(user)"
                    class="text-green-500 hover:text-green-700 text-xs font-medium"
                  >启用</button>
                </div>
              </td>
            </tr>
            <tr v-if="users.length === 0">
              <td colspan="5" class="px-5 py-8 text-center text-gray-400">暂无数据</td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- 分页 -->
      <div class="flex items-center justify-between px-5 py-4 border-t border-gray-100">
        <span class="text-sm text-gray-500">共 {{ total }} 条记录</span>
        <div class="flex items-center gap-2">
          <button 
            @click="handlePageChange(currentPage - 1)" 
            :disabled="currentPage <= 1" 
            class="px-3 py-1.5 text-sm border border-gray-200 rounded-lg disabled:opacity-40 hover:bg-gray-50"
          >上一页</button>
          <span class="text-sm text-gray-600">{{ currentPage }} / {{ totalPages || 1 }}</span>
          <button 
            @click="handlePageChange(currentPage + 1)" 
            :disabled="currentPage >= totalPages" 
            class="px-3 py-1.5 text-sm border border-gray-200 rounded-lg disabled:opacity-40 hover:bg-gray-50"
          >下一页</button>
        </div>
      </div>
    </div>

    <!-- 用户详情弹窗 -->
    <el-dialog v-model="detailVisible" title="用户详情" width="600px">
      <div v-if="currentUser" class="space-y-4">
        <div class="flex items-center gap-4">
          <img :src="currentUser.avatar || defaultAvatar" class="w-16 h-16 rounded-full object-cover bg-gray-100" alt="" />
          <div>
            <p class="font-medium text-lg">{{ currentUser.nickname || currentUser.username }}</p>
            <p class="text-sm text-gray-500">ID: {{ currentUser.id }}</p>
          </div>
        </div>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="用户名">{{ currentUser.username }}</el-descriptions-item>
          <el-descriptions-item label="昵称">{{ currentUser.nickname || '-' }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ currentUser.phone || '-' }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ currentUser.email || '-' }}</el-descriptions-item>
          <el-descriptions-item label="注册时间">{{ formatTime(currentUser.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="currentUser.status === 1 ? 'success' : 'danger'">
              {{ currentUser.status === 1 ? '正常' : '已禁用' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="帖子数">{{ currentUser.postCount || 0 }}</el-descriptions-item>
          <el-descriptions-item label="粉丝数">{{ currentUser.followerCount || 0 }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi } from '@/api'

const defaultAvatar = 'https://placehold.co/40x40/409EFF/fff?text=U'

interface User {
  id: number
  username: string
  nickname?: string
  phone?: string
  email?: string
  avatar?: string
  status: number
  createTime?: string
  postCount?: number
  followerCount?: number
}

const searchKeyword = ref('')
const filterStatus = ref<number | undefined>(undefined)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const totalPages = ref(1)
const users = ref<User[]>([])
const detailVisible = ref(false)
const currentUser = ref<User | null>(null)

async function loadUsers() {
  try {
    const res: any = await adminApi.users({
      keyword: searchKeyword.value || undefined,
      status: filterStatus.value,
      page: currentPage.value,
      size: pageSize.value
    })
    
    if (res?.records) {
      users.value = res.records
      total.value = res.total || 0
      totalPages.value = Math.ceil((res.total || 0) / pageSize.value)
    } else if (Array.isArray(res)) {
      users.value = res
      total.value = res.length
      totalPages.value = 1
    }
  } catch (e: any) {
    ElMessage.error('加载用户列表失败')
    console.error(e)
  }
}

function handleSearch() {
  currentPage.value = 1
  loadUsers()
}

function handleReset() {
  searchKeyword.value = ''
  filterStatus.value = undefined
  currentPage.value = 1
  loadUsers()
}

function handlePageChange(page: number) {
  currentPage.value = page
  loadUsers()
}

function getStatusClass(status: number): string {
  return status === 1 ? 'bg-green-50 text-green-600' : 'bg-red-50 text-red-600'
}

function getStatusText(status: number): string {
  return status === 1 ? '正常' : '已禁用'
}

function formatTime(dateStr?: string): string {
  if (!dateStr) return '-'
  const d = new Date(dateStr)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

function showDetail(user: User) {
  currentUser.value = user
  detailVisible.value = true
}

async function handleBan(user: User) {
  try {
    await ElMessageBox.confirm(`确定要禁用用户 "${user.nickname || user.username}" 吗？`, '确认禁用', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await adminApi.updateUserStatus(user.id, 0)
    ElMessage.success('禁用成功')
    loadUsers()
  } catch (e: any) {
    if (e !== 'cancel') {
      ElMessage.error('禁用失败')
    }
  }
}

async function handleUnban(user: User) {
  try {
    await ElMessageBox.confirm(`确定要启用用户 "${user.nickname || user.username}" 吗？`, '确认启用', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    await adminApi.updateUserStatus(user.id, 1)
    ElMessage.success('启用成功')
    loadUsers()
  } catch (e: any) {
    if (e !== 'cancel') {
      ElMessage.error('启用失败')
    }
  }
}

onMounted(() => {
  loadUsers()
})
</script>
