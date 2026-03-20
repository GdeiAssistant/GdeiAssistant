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
  <div class="community-tabbar" :style="{ '--module-color': moduleColor }">
    <a
      v-for="tab in tabs"
      :key="tab.key"
      href="javascript:;"
      class="community-tabbar__item"
      :class="{ active: activeTab === tab.key }"
      @click.prevent="goTo(tab.path)"
    >
      <i class="community-tabbar__icon" v-html="tab.icon"></i>
      <p class="community-tabbar__label">{{ tab.label }}</p>
    </a>
  </div>
</template>

<style scoped>
.community-tabbar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  width: 100%;
  height: 56px;
  background: #fff;
  border-top: 1px solid #f3f4f6;
  display: flex;
  z-index: 500;
  box-shadow: 0 -1px 8px rgba(0, 0, 0, 0.04);
}

.community-tabbar__item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #9ca3af;
  text-decoration: none;
  font-size: 12px;
  padding: 6px 0;
  transition: color 0.25s ease;
  -webkit-tap-highlight-color: transparent;
}

.community-tabbar__item.active {
  color: var(--module-color, #6366f1);
}

.community-tabbar__icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  margin-bottom: 3px;
  transition: transform 0.25s ease, color 0.25s ease;
}

.community-tabbar__item.active .community-tabbar__icon {
  transform: scale(1.1);
  color: var(--module-color, #6366f1);
}

.community-tabbar__icon :deep(svg) {
  width: 22px;
  height: 22px;
  fill: currentColor;
}

.community-tabbar__label {
  margin: 0;
  font-size: 11px;
  line-height: 1;
  color: inherit;
  font-weight: 400;
}

.community-tabbar__item.active .community-tabbar__label {
  font-weight: 500;
}
</style>
