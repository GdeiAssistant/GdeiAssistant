<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const props = defineProps({
  /** 模块基础路径，如 '/ershou' */
  basePath: { type: String, required: true },
  /** 模块主色，如 '#10b981' */
  moduleColor: { type: String, default: '#6366f1' },
  /** Tab 配置列表 [{key, label, path, icon}] */
  tabs: { type: Array, required: true }
})

const router = useRouter()
const route = useRoute()

const activeTab = computed(() => {
  const p = route.path
  for (const tab of props.tabs) {
    if (p.includes(tab.path)) return tab.key
  }
  return props.tabs[0]?.key || ''
})

function goTo(path) {
  router.push(path)
}
</script>

<template>
  <div
    class="fixed bottom-0 left-0 right-0 w-full h-14 bg-[var(--c-surface)] border-t border-[var(--c-border)] flex z-[500] shadow-[0_-1px_8px_rgba(0,0,0,0.04)]"
    :style="{ '--module-color': moduleColor }"
  >
    <button
      v-for="tab in tabs"
      :key="tab.key"
      type="button"
      class="flex-1 flex flex-col items-center justify-center text-[var(--c-text-3)] text-xs py-1.5 border-none bg-transparent cursor-pointer font-[inherit] transition-colors duration-200 focus-visible:outline-2 focus-visible:outline-[var(--module-color)] focus-visible:outline-offset-[-2px]"
      :class="{ '!text-[var(--module-color)]': activeTab === tab.key }"
      @click="goTo(tab.path)"
    >
      <i
        class="flex items-center justify-center w-6 h-6 mb-0.5 transition-transform duration-200 [&>svg]:w-[22px] [&>svg]:h-[22px] [&>svg]:fill-current"
        :class="{ 'scale-110': activeTab === tab.key }"
        v-html="tab.icon"
      ></i>
      <p
        class="m-0 text-[11px] leading-none text-inherit"
        :class="{ 'font-medium': activeTab === tab.key }"
      >{{ tab.label }}</p>
    </button>
  </div>
</template>
