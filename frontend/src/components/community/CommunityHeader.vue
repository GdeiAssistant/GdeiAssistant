<script setup>
import { useRouter } from 'vue-router'

const props = defineProps({
  /** 页面标题 */
  title: { type: String, default: '' },
  /** 模块主色 */
  moduleColor: { type: String, default: '#6366f1' },
  /** 返回目标路径，默认 / */
  backTo: { type: String, default: '/' },
  /** 是否显示返回按钮 */
  showBack: { type: Boolean, default: true }
})

const emit = defineEmits(['back'])
const router = useRouter()

function handleBack() {
  emit('back')
  if (props.backTo) {
    router.push(props.backTo)
  }
}
</script>

<template>
  <div class="sticky top-0 z-[100] bg-[var(--c-surface)]" :style="{ '--module-color': moduleColor }">
    <!-- Accent bar -->
    <div class="h-[3px]" :style="{ background: `linear-gradient(90deg, ${moduleColor}, color-mix(in srgb, ${moduleColor} 60%, white))` }"></div>
    <!-- Content -->
    <div class="flex items-center justify-between h-12 px-3 border-b border-[var(--c-border)]">
      <span
        v-if="showBack"
        class="flex items-center justify-center w-9 h-9 rounded-full cursor-pointer text-[var(--c-text-1)] transition-colors active:bg-[var(--c-bg)]"
        @click="handleBack"
      >
        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <polyline points="15 18 9 12 15 6"/>
        </svg>
      </span>
      <h1 class="flex-1 text-center text-base font-semibold m-0 text-[var(--c-text-1)] tracking-wide">{{ title }}</h1>
      <span class="min-w-[36px] flex items-center justify-end">
        <slot name="right" />
      </span>
    </div>
  </div>
</template>
