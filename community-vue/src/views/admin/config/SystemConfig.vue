<template>
  <div class="space-y-6 max-w-4xl">
    <!-- 配置分组 -->
    <div v-for="group in configGroups" :key="group.key" class="bg-white rounded-xl border border-gray-100 shadow-sm overflow-hidden">
      <div class="px-6 py-4 border-b border-gray-100 flex items-center gap-3 cursor-pointer hover:bg-gray-50/50 transition-colors" @click="toggleGroup(group.key)">
        <ChevronRight :class="['w-5 h-5 text-gray-400 transition-transform', expandedGroups.has(group.key) ? 'rotate-90' : '']" />
        <component :is="group.icon" class="w-5 h-5" :class="group.iconColor" />
        <h3 class="font-semibold text-gray-800">{{ group.title }}</h3>
        <span class="text-xs text-gray-400 ml-auto">{{ group.items.length }} 项配置</span>
      </div>

      <div v-show="expandedGroups.has(group.key)" class="divide-y divide-gray-50">
        <div v-for="item in group.items" :key="item.key" class="px-6 py-4 flex items-center justify-between gap-6">
          <div class="min-w-0">
            <p class="text-sm font-medium text-gray-800">{{ item.label }}</p>
            <p class="text-xs text-gray-400 mt-0.5">{{ item.description }}</p>
          </div>
          <div class="shrink-0">
            <!-- 开关类型 -->
            <label v-if="item.type === 'switch'" class="relative inline-flex items-center cursor-pointer">
              <input type="checkbox" :checked="item.value" class="sr-only peer" @change="updateConfig(group.key, item.key, ($event.target as HTMLInputElement).checked)" />
              <div class="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-2 peer-focus:ring-primary/20 rounded-full peer peer-checked:after:translate-x-full after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-primary"></div>
            </label>
            <!-- 输入框类型 -->
            <input v-else-if="item.type === 'input'" type="text" :value="item.value" class="w-48 px-3 py-1.5 border border-gray-200 rounded-lg text-sm outline-none focus:border-primary focus:ring-2 focus:ring-primary/20" @change="updateConfig(group.key, item.key, ($event.target as HTMLInputElement).value)" />
            <!-- 数字输入 -->
            <input v-else-if="item.type === 'number'" type="number" :value="item.value" class="w-28 px-3 py-1.5 border border-gray-200 rounded-lg text-sm outline-none focus:border-primary" @change="updateConfig(group.key, item.key, Number(($event.target as HTMLInputElement).value))" />
            <!-- 选择器 -->
            <select v-else-if="item.type === 'select'" :value="item.value" class="px-3 py-1.5 border border-gray-200 rounded-lg text-sm outline-none focus:border-primary" @change="updateConfig(group.key, item.key, ($event.target as HTMLSelectElement).value)">
              <option v-for="opt in item.options" :key="opt" :value="opt">{{ opt }}</option>
            </select>
          </div>
        </div>
      </div>
    </div>

    <!-- 保存按钮 -->
    <div class="flex justify-end">
      <button class="px-6 py-2.5 bg-gradient-to-r from-primary to-blue-600 text-white rounded-lg text-sm font-medium hover:shadow-lg hover:shadow-primary/25 flex items-center gap-2" @click="saveConfigs">
        <Save class="w-4 h-4" />保存所有配置
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ChevronRight, Globe, Shield, Bell, Palette, Image, Database, Clock } from 'lucide-vue-next'

const expandedGroups = reactive(new Set(['site', 'security', 'content']))

interface ConfigItem { key: string; label: string; description: string; type: string; value: any; options?: string[] }

interface ConfigGroup { key: string; title: string; icon: any; iconColor: string; items: ConfigItem[] }

const configGroups: ConfigGroup[] = [
  {
    key: 'site', title: '站点基本设置', icon: Globe, iconColor: 'text-blue-500',
    items: [
      { key: 'name', label: '站点名称', description: '显示在浏览器标题栏的站点名称', type: 'input', value: 'TechCommunity 技术社区' },
      { key: 'description', label: '站点描述', description: '站点的SEO描述信息', type: 'input', value: '一个面向技术爱好者的开源社区平台' },
      { key: 'url', label: '站点地址', description: '站点的主域名地址', type: 'input', value: 'https://tech-community.com' },
      { key: 'logo', label: '站点Logo', description: '站点的Logo图片URL', type: 'input', value: '/logo.png' },
      { key: 'openRegister', label: '开放注册', description: '是否允许新用户自主注册', type: 'switch', value: true },
      { key: 'defaultRole', label: '默认角色', description: '新注册用户的默认角色', type: 'select', value: '普通会员', options: ['普通会员', '认证用户', 'VIP会员'] },
    ],
  },
  {
    key: 'security', title: '安全设置', icon: Shield, iconColor: 'text-red-500',
    items: [
      { key: 'jwtExpire', label: 'Token有效期(分钟)', description: '访问令牌的有效时长', type: 'number', value: 30 },
      { key: 'refreshExpire', label: '刷新令牌有效期(天)', description: '刷新令牌的有效天数', type: 'number', value: 7 },
      { key: 'maxLoginFail', label: '最大登录失败次数', description: '超过此次数将锁定账号', type: 'number', value: 5 },
      { key: 'lockDuration', label: '锁定时长(分钟)', description: '登录失败过多后的锁定时长', type: 'number', value: 30 },
      { key: 'enableCaptcha', label: '开启验证码', description: '登录时是否需要图形验证码', type: 'switch', value: true },
      { key: 'enableIpBlacklist', label: 'IP黑名单', description: '是否启用IP黑名单功能', type: 'switch', value: true },
    ],
  },
  {
    key: 'content', title: '内容管理设置', icon: Bell, iconColor: 'text-orange-500',
    items: [
      { key: 'enableReview', label: '内容审核', description: '发布的内容是否需要先审核', type: 'switch', value: false },
      { key: 'sensitiveFilter', label: '敏感词过滤', description: '是否自动过滤敏感词', type: 'switch', value: true },
      { key: 'allowAnonymous', label: '匿名发帖', description: '是否允许用户匿名发布帖子', type: 'switch', value: true },
      { key: 'maxTitleLen', label: '标题最大长度', description: '帖子标题的最大字符数限制', type: 'number', value: 100 },
      { key: 'maxContentLen', label: '正文最大长度', description: '帖子正文的最大字符数限制', type: 'number', value: 50000 },
      { key: 'postInterval', label: '发帖间隔(秒)', description: '两次发帖之间的最小间隔', type: 'number', value: 30 },
    ],
  },
  {
    key: 'ui', title: '界面显示设置', icon: Palette, iconColor: 'text-purple-500',
    items: [
      { key: 'theme', label: '默认主题', description: '用户首次访问时的主题风格', type: 'select', value: '浅色模式', options: ['浅色模式', '深色模式', '跟随系统'] },
      { key: 'pageSize', label: '分页大小', description: '列表每页显示的数据条数', type: 'number', value: 20 },
      { key: 'hotPostCount', label: '热榜展示数', description: '首页热榜展示的帖子数量', type: 'number', value: 10 },
      { key: 'enableWatermark', label: '图片水印', description: '上传的图片是否自动添加水印', type: 'switch', value: true },
    ],
  },
  {
    key: 'upload', title: '文件上传设置', icon: Image, iconColor: 'text-cyan-500',
    items: [
      { key: 'maxFileSize', label: '文件大小上限(MB)', description: '单个文件的最大体积', type: 'number', value: 10 },
      { key: 'allowedTypes', label: '允许的格式', description: '逗号分隔的文件扩展名', type: 'input', value: 'jpg,jpeg,png,gif,pdf,doc,docx,xlsx' },
      { key: 'storageType', label: '存储方式', description: '文件的存储位置', type: 'select', value: '本地存储', options: ['本地存储', 'OSS对象存储', 'COS对象存储'] },
      { key: 'imageCompress', label: '图片压缩', description: '自动压缩上传的图片', type: 'switch', value: true },
    ],
  },
  {
    key: 'system', title: '系统运行参数', icon: Database, iconColor: 'text-green-500',
    items: [
      { key: 'cacheExpire', label: '缓存过期时间(分钟)', description: 'Redis缓存的默认过期时间', type: 'number', value: 60 },
      { key: 'mqTimeout', label: 'MQ消息超时(秒)', description: 'RabbitMQ消息的超时时长', type: 'number', value: 30 },
      { key: 'taskScheduleTime', label: '定时任务执行时间', description: '每日定时任务的小时:分钟', type: 'input', value: '02:00' },
      { key: 'logLevel', label: '日志级别', description: '系统的日志输出级别', type: 'select', value: 'INFO', options: ['DEBUG', 'INFO', 'WARN', 'ERROR'] },
    ],
  },
]

function toggleGroup(key: string) {
  if (expandedGroups.has(key)) {
    expandedGroups.delete(key)
  } else {
    expandedGroups.add(key)
  }
}

function updateConfig(groupKey: string, itemKey: string, value: any) {
  const group = configGroups.find(g => g.key === groupKey)
  if (group) {
    const item = group.items.find(i => i.key === itemKey)
    if (item) item.value = value
  }
}

function saveConfigs() {}
</script>
