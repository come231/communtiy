<template>
  <div
    class="user-page-shell min-h-[calc(100vh-4.5rem)] flex flex-col bg-gradient-to-br from-[#f0f4ff] to-[#f8faff] text-[#1f2937]"
  >
    <header
      v-if="showHeader"
      class="shrink-0 bg-white shadow-[0_2px_20px_rgba(0,0,0,0.08)] py-4"
      :class="stickyHeader ? 'sticky top-0 z-[50]' : ''"
    >
      <div :class="[maxWidthClass, 'mx-auto px-5 flex flex-wrap items-center justify-between gap-3']">
        <div class="min-w-0">
          <div
            class="text-[22px] font-bold flex items-center gap-2 bg-gradient-to-r from-[#6366f1] to-[#a855f7] bg-clip-text text-transparent"
          >
            <i v-if="icon" :class="[icon, 'text-[#6366f1] shrink-0 not-italic']" aria-hidden="true" />
            <span>{{ title }}</span>
          </div>
          <p v-if="subtitle" class="text-sm text-[#6b7280] mt-1">{{ subtitle }}</p>
        </div>
        <div class="flex items-center gap-3 shrink-0">
          <slot name="actions" />
        </div>
      </div>
    </header>
    <div :class="[maxWidthClass, 'w-full mx-auto px-4 md:px-5 py-5 pb-10 flex-1 min-h-0', contentClass]">
      <slot />
    </div>
  </div>
</template>

<script setup lang="ts">
withDefaults(
  defineProps<{
    title?: string
    icon?: string
    subtitle?: string
    /** 默认与私信页一致 */
    maxWidthClass?: string
    stickyHeader?: boolean
    showHeader?: boolean
    contentClass?: string
  }>(),
  {
    title: '',
    icon: '',
    subtitle: '',
    maxWidthClass: 'max-w-[1200px]',
    stickyHeader: true,
    showHeader: true,
    contentClass: '',
  },
)
</script>
