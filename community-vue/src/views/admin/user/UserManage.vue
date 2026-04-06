<template>
  <div class="space-y-6">
    <!-- 搜索和筛选 -->
    <div class="bg-white rounded-xl p-5 border border-gray-100 shadow-sm">
      <div class="flex flex-wrap items-center gap-4">
        <input
          v-model="searchKeyword"
          type="text"
          placeholder="搜索用户名/手机号..."
          class="px-4 py-2 bg-gray-50 border border-gray-200 rounded-lg text-sm outline-none focus:border-primary focus:ring-2 focus:ring-primary/20 w-64 transition-all"
        />
        <select v-model="filterStatus" class="px-3 py-2 bg-gray-50 border border-gray-200 rounded-lg text-sm outline-none focus:border-primary">
          <option value="">全部状态</option>
          <option value="normal">正常</option>
          <option value="banned">已禁用</option>
          <option value="pending">待审核</option>
        </select>
        <button class="px-4 py-2 bg-primary text-white text-sm rounded-lg hover:bg-blue-600 transition-colors">搜索</button>
        <button class="px-4 py-2 border border-gray-200 text-gray-600 text-sm rounded-lg hover:bg-gray-50 transition-colors">重置</button>
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
                  <img :src="user.avatar" class="w-10 h-10 rounded-full object-cover" alt="" />
                  <div>
                    <p class="font-medium text-gray-800">{{ user.nickname }}</p>
                    <p class="text-xs text-gray-400 mt-0.5">{{ user.phone }}</p>
                  </div>
                  <span v-if="user.vip" class="text-xs bg-gradient-to-r from-yellow-400 to-orange-400 text-white px-1.5 py-0.5 rounded font-medium">VIP</span>
                </div>
              </td>
              <td class="px-5 py-4 text-gray-600 text-xs">{{ user.registerTime }}</td>
              <td class="px-5 py-4">
                <div class="flex gap-3 text-xs">
                  <span class="text-gray-600">{{ user.postCount }} 帖子</span>
                  <span class="text-gray-400">{{ user.followerCount }} 粉丝</span>
                </div>
              </td>
              <td class="px-5 py-4">
                <span :class="['text-xs px-2.5 py-1 rounded-full font-medium', getStatusClass(user.status)]">{{ getStatusText(user.status) }}</span>
              </td>
              <td class="px-5 py-4">
                <div class="flex items-center gap-2">
                  <button class="text-primary hover:text-blue-700 text-xs font-medium">详情</button>
                  <button
                    :class="['text-xs font-medium', user.status === 'normal' ? 'text-orange-500 hover:text-orange-700' : 'text-green-500 hover:text-green-700']"
                  >{{ user.status === 'normal' ? '禁用' : '启用' }}</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- 分页 -->
      <div class="flex items-center justify-between px-5 py-4 border-t border-gray-100">
        <span class="text-sm text-gray-500">共 {{ total }} 条记录</span>
        <div class="flex items-center gap-2">
          <button :disabled="currentPage <= 1" class="px-3 py-1.5 text-sm border border-gray-200 rounded-lg disabled:opacity-40 hover:bg-gray-50">上一页</button>
          <span class="text-sm text-gray-600">{{ currentPage }} / {{ totalPages }}</span>
          <button :disabled="currentPage >= totalPages" class="px-3 py-1.5 text-sm border border-gray-200 rounded-lg disabled:opacity-40 hover:bg-gray-50">下一页</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const searchKeyword = ref('')
const filterStatus = ref('')
const currentPage = ref(1)
const total = ref(128)
const totalPages = ref(13)

const users = [
  { id: 1, nickname: '张三', phone: '138****1234', avatar: 'https://placehold.co/40x40/409EFF/fff?text=Z', vip: true, registerTime: '2025-06-12', postCount: 86, followerCount: 1234, status: 'normal' },
  { id: 2, nickname: '李四', phone: '139****5678', avatar: 'https://placehold.co/40x40/67C23A/fff?text=L', vip: false, registerTime: '2025-07-20', postCount: 42, followerCount: 567, status: 'normal' },
  { id: 3, nickname: '王五', phone: '137****9012', avatar: 'https://placehold.co/40x40/E6A23C/fff?text=W', vip: false, registerTime: '2025-08-03', postCount: 28, followerCount: 234, status: 'banned' },
  { id: 4, nickname: '赵六', phone: '136****3456', avatar: 'https://placehold.co/40x40/F56C6C/fff?text=Z', vip: true, registerTime: '2025-09-15', postCount: 156, followerCount: 3456, status: 'normal' },
  { id: 5, nickname: '孙七', phone: '135****7890', avatar: 'https://placehold.co/40x40/909399/fff?text=S', vip: false, registerTime: '2025-11-01', postCount: 12, followerCount: 89, status: 'pending' },
  { id: 6, nickname: '周八', phone: '134****1122', avatar: 'https://placehold.co/40x40/9B59B6/fff?text=Z', vip: false, registerTime: '2026-01-20', postCount: 67, followerCount: 890, status: 'normal' },
  { id: 7, nickname: '吴九', phone: '133****3344', avatar: 'https://placehold.co/40x40/1ABC9C/fff?text=W', vip: true, registerTime: '2026-02-14', postCount: 201, followerCount: 5678, status: 'normal' },
  { id: 8, nickname: '郑十', phone: '132****5566', avatar: 'https://placehold.co/40x40/E74C3C/fff?text=Z', vip: false, registerTime: '2026-03-05', postCount: 5, followerCount: 12, status: 'banned' },
]

function getStatusClass(status: string): string {
  const cls: Record<string, string> = { normal: 'bg-green-50 text-green-600', banned: 'bg-red-50 text-red-600', pending: 'bg-yellow-50 text-yellow-600' }
  return cls[status] || 'bg-gray-100 text-gray-600'
}
function getStatusText(status: string): string {
  const txt: Record<string, string> = { normal: '正常', banned: '已禁用', pending: '待审核' }
  return txt[status] || '未知'
}
</script>
