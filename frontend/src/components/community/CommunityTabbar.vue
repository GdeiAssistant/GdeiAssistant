<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'

const props = defineProps({
  /** 模块基础路径，如 '/marketplace' */
  basePath: { type: String, required: true },
  /** 模块主色，如 'var(--c-ershou)' */
  moduleColor: { type: String, default: 'var(--c-topic)' },
  /** Tab 配置列表 [{key, label, path, icon}] */
  /** ⚠️ icon 通过 v-html 渲染，必须为硬编码 SVG 字符串，禁止来自 API 或用户输入 */
  tabs: { type: Array, required: true }
})

const router = useRouter()
const route = useRoute()
const { t } = useI18n()

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
  <nav class="community-tabbar" :style="{ '--module-color': moduleColor }" :aria-label="t('community.navAriaLabel')">
    <button
      v-for="tab in tabs"
      :key="tab.key"
      type="button"
      class="community-tabbar__item"
      :class="{ 'community-tabbar__item--active': activeTab === tab.key }"
      @click="goTo(tab.path)"
    >
      <i class="community-tabbar__icon" v-html="tab.icon" />
      <p>{{ tab.label }}</p>
    </button>
  </nav>
</template>

<style scoped>
.community-tabbar {
  position: fixed;
  right: 0;
  bottom: 0;
  left: 0;
  z-index: 500;
  display: flex;
  padding: 0 8px env(safe-area-inset-bottom, 0px);
  border-top: 1px solid var(--c-border);
  background: var(--c-surface);
}

.community-tabbar__item {
  position: relative;
  display: flex;
  flex: 1;
  min-height: 56px;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  border: 0;
  background: transparent;
  color: var(--c-text-3);
  cursor: pointer;
  font: inherit;
  transition: color 0.15s ease;
}

.community-tabbar__item--active {
  color: var(--c-primary);
}

.community-tabbar__item--active::before {
  position: absolute;
  top: 0;
  left: 50%;
  width: 24px;
  height: 2px;
  border-radius: 0 0 2px 2px;
  background: var(--c-primary);
  content: '';
  transform: translateX(-50%);
}

.community-tabbar__item:focus-visible {
  outline: 2px solid var(--c-primary);
  outline-offset: -4px;
  border-radius: var(--radius-control);
}

.community-tabbar__icon {
  display: grid;
  width: 22px;
  height: 22px;
  place-items: center;
}

.community-tabbar__icon :deep(svg),
.community-tabbar__icon :deep(*) {
  width: 22px;
  height: 22px;
  fill: currentColor;
}

.community-tabbar__item p {
  margin: 0;
  color: inherit;
  font-size: 11px;
  font-weight: 600;
  line-height: 1;
}

@media (min-width: 768px) {
  .community-tabbar {
    position: static;
    display: flex;
    gap: 4px;
    margin: 0;
    padding: 0;
    border-top: 0;
    border-bottom: 1px solid var(--c-border);
    background: transparent;
  }

  .community-tabbar__item {
    flex: 0 0 auto;
    min-height: 48px;
    flex-direction: row;
    gap: 8px;
    padding: 0 14px;
  }

  .community-tabbar__item:hover {
    color: var(--c-text-1);
  }

  .community-tabbar__item--active,
  .community-tabbar__item--active:hover {
    color: var(--c-primary);
  }

  .community-tabbar__item--active::before {
    top: auto;
    bottom: -1px;
    left: 14px;
    right: 14px;
    width: auto;
    border-radius: 2px 2px 0 0;
    transform: none;
  }

  .community-tabbar__icon,
  .community-tabbar__icon :deep(svg),
  .community-tabbar__icon :deep(*) {
    width: 18px;
    height: 18px;
  }

  .community-tabbar__item p {
    font-size: 14px;
  }
}

@media (prefers-reduced-motion: reduce) {
  .community-tabbar__item {
    transition: none;
  }
}
</style>
