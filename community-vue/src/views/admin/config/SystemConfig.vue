<template>
  <div class="space-y-6 max-w-4xl">
    <!-- 加载状态 -->
    <div v-if="loading" class="flex items-center justify-center py-20">
      <el-icon class="is-loading text-4xl text-primary"><Loading /></el-icon>
    </div>

    <template v-else>
      <!-- 配置分组 -->
      <div v-for="group in configGroups" :key="group.key" class="bg-white rounded-xl border border-gray-100 shadow-sm overflow-hidden">
        <div class="px-6 py-4 border-b border-gray-100 flex items-center gap-3 cursor-pointer hover:bg-gray-50/50 transition-colors" @click="toggleGroup(group.key)">
          <ChevronRight :class="['w-5 h-5 text-gray-400 transition-transform', expandedGroups.has(group.key) ? 'rotate-90' : '']" />
          <component :is="group.icon" class="w-5 h-5" :class="group.iconColor" />
          <h3 class="font-semibold text-gray-800">{{ group.title }}</h3>
          <span class="text-xs text-gray-400 ml-auto">{{ group.items.length }} 项配置</span>
        </div>

        <div v-show="expandedGroups.has(group.key)" class="divide-y divide-gray-50">
          <div v-for="item in group.items" :key="item.configKey" class="px-6 py-4 flex items-center justify-between gap-6">
            <div class="min-w-0">
              <p class="text-sm font-medium text-gray-800">{{ item.configName }}</p>
              <p class="text-xs text-gray-400 mt-0.5">{{ item.description }}</p>
            </div>
            <div class="shrink-0">
              <!-- 开关类型 -->
              <label v-if="item.configType === 'switch'" class="relative inline-flex items-center cursor-pointer">
                <input type="checkbox" :checked="item.configValue === 'true' || item.configValue === '1'" class="sr-only peer" @change="updateConfig(item, ($event.target as HTMLInputElement).checked ? 'true' : 'false')" />
                <div class="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-2 peer-focus:ring-primary/20 rounded-full peer peer-checked:after:translate-x-full after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-primary"></div>
              </label>
              <!-- 输入框类型 -->
              <input v-else-if="item.configType === 'input'" type="text" :value="item.configValue" class="w-48 px-3 py-1.5 border border-gray-200 rounded-lg text-sm outline-none focus:border-primary focus:ring-2 focus:ring-primary/20" @change="updateConfig(item, ($event.target as HTMLInputElement).value)" />
              <!-- 数字输入 -->
              <input v-else-if="item.configType === 'number'" type="number" :value="item.configValue" class="w-28 px-3 py-1.5 border border-gray-200 rounded-lg text-sm outline-none focus:border-primary" @change="updateConfig(item, ($event.target as HTMLInputElement).value)" />
              <!-- 选择器 -->
              <select v-else-if="item.configType === 'select'" :value="item.configValue" class="px-3 py-1.5 border border-gray-200 rounded-lg text-sm outline-none focus:border-primary" @change="updateConfig(item, ($event.target as HTMLSelectElement).value)">
                <option v-for="opt in (item.options || '').split(',')" :key="opt" :value="opt">{{ opt }}</option>
              </select>
              <!-- 文本域 -->
              <textarea v-else-if="item.configType === 'textarea'" :value="item.configValue" class="w-64 px-3 py-1.5 border border-gray-200 rounded-lg text-sm outline-none focus:border-primary" rows="3" @change="updateConfig(item, ($event.target as HTMLTextAreaElement).value)"></textarea>
              <!-- 默认显示 -->
              <span v-else class="text-sm text-gray-600">{{ item.configValue }}</span>
            </div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { ChevronRight, Globe, Shield, Bell, Palette, Image, Database, Clock } from 'lucide-vue-next'
import { Loading } from '@element-plus/icons-vue'
import { adminApi } from '@/api'

interface SysConfig {
  id: number
  configKey: string
  configName: string
  configValue: string
  configType: string
  groupKey: string
  description: string
  options?: string
}

const loading = ref(false)
const configList = ref<SysConfig[]>([])
const expandedGroups = reactive(new Set(['basic', 'security', 'content']))

const groupIcons: Record<string, any> = {
  basic: Globe,
  security: Shield,
  content: Bell,
  ui: Palette,
  upload: Image,
  system: Database,
}

const groupIconColors: Record<string, string> = {
  basic: 'text-blue-500',
  security: 'text-red-500',
  content: 'text-orange-500',
  ui: 'text-purple-500',
  upload: 'text-cyan-500',
  system: 'text-green-500',
}

const groupTitles: Record<string, string> = {
  basic: '站点基本设置',
  security: '安全设置',
  content: '内容管理设置',
  ui: '界面显示设置',
  upload: '文件上传设置',
  system: '系统运行参数',
}

// 按分组整理配置
const configGroups = reactive<{ key: string; title: string; icon: any; iconColor: string; items: SysConfig[] }[]>([])

function toggleGroup(key: string) {
  if (expandedGroups.has(key)) {
    expandedGroups.delete(key)
  } else {
    expandedGroups.add(key)
  }
}

async function loadConfigs() {
  loading.value = true
  try {
    const res: any = await adminApi.configs()
    if (Array.isArray(res)) {
      configList.value = res
      organizeConfigs(res)
    } else if (res?.records) {
      configList.value = res.records
      organizeConfigs(res.records)
    }
  } catch (e: any) {
    ElMessage.error('加载配置失败')
    console.error(e)
  } finally {
    loading.value = false
  }
}

function organizeConfigs(configs: SysConfig[]) {
  configGroups.length = 0
  const groupMap = new Map<string, SysConfig[]>()
  
  configs.forEach(config => {
    const groupKey = config.groupKey || 'basic'
    if (!groupMap.has(groupKey)) {
      groupMap.set(groupKey, [])
    }
    groupMap.get(groupKey)!.push(config)
  })
  
  groupMap.forEach((items, key) => {
    configGroups.push({
      key,
      title: groupTitles[key] || key,
      icon: groupIcons[key] || Database,
      iconColor: groupIconColors[key] || 'text-gray-500',
      items
    })
  })
}

async function updateConfig(item: SysConfig, value: string) {
  try {
    await adminApi.updateConfig(item.id, value)
    item.configValue = value
    ElMessage.success('配置已保存')
  } catch (e: any) {
    ElMessage.error('保存失败')
  }
}

onMounted(() => {
  loadConfigs()
})
</script>
