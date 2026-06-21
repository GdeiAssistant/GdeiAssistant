<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const props = defineProps({
  /** 模块基础路径，如 '/ershou' */
  basePath: { type: String, required: true },
  /** 模块主色，如 'var(--c-ershou)' */
  moduleColor: { type: String, default: 'var(--c-topic)' },
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
  <nav class="community-tabbar" :style="{ '--module-color': moduleColor }" aria-label="社区导航">
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
  padding: 8px 18px calc(8px + env(safe-area-inset-bottom, 0));
  border-top: 1px solid rgba(207, 221, 225, 0.72);
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 -16px 34px rgba(15, 39, 49, 0.08);
  backdrop-filter: blur(18px);
}

.community-tabbar__item {
  display: flex;
  flex: 1;
  min-height: 54px;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  border: 0;
  border-radius: 18px;
  background: transparent;
  color: var(--c-text-3);
  cursor: pointer;
  font: inherit;
  transition: background 0.18s ease, color 0.18s ease, transform 0.18s ease;
}

.community-tabbar__item--active {
  background: color-mix(in srgb, var(--module-color) 12%, transparent);
  color: var(--module-color);
}

.community-tabbar__item:active {
  transform: scale(0.98);
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
  font-weight: 760;
  line-height: 1;
}

@media (min-width: 768px) {
  .community-tabbar {
    position: static;
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(136px, 1fr));
    gap: 8px;
    margin: 0 auto 16px;
    padding: 8px;
    border: 1px solid color-mix(in srgb, var(--module-color) 20%, rgba(205, 222, 226, 0.76));
    border-radius: 24px;
    background: rgba(255, 255, 255, 0.74);
    box-shadow: 0 16px 38px rgba(32, 69, 78, 0.07);
  }

  .community-tabbar__item {
    min-height: 58px;
    flex-direction: row;
    gap: 10px;
    justify-content: flex-start;
    border-radius: 18px;
    padding: 0 16px;
    text-align: left;
  }

  .community-tabbar__item:hover {
    color: var(--module-color);
    background: color-mix(in srgb, var(--module-color) 8%, transparent);
  }

  .community-tabbar__item--active {
    box-shadow: inset 0 0 0 1px color-mix(in srgb, var(--module-color) 24%, transparent);
  }

  .community-tabbar__icon,
  .community-tabbar__icon :deep(svg),
  .community-tabbar__icon :deep(*) {
    width: 20px;
    height: 20px;
  }

  .community-tabbar__item p {
    font-size: 14px;
    font-weight: 850;
  }
}

[data-theme="dark"] .community-tabbar {
  border-top-color: rgba(68, 89, 112, 0.74);
  background: rgba(18, 30, 42, 0.92);
  box-shadow: 0 -16px 34px rgba(0, 0, 0, 0.28);
}

[data-theme="dark"] .community-tabbar__item:hover {
  background: rgba(32, 48, 68, 0.56);
}

[data-theme="dark"] .community-tabbar__item--active {
  background: rgba(32, 48, 68, 0.76);
  color: var(--c-text-1);
  box-shadow: inset 0 0 0 1px color-mix(in srgb, var(--module-color) 22%, rgba(74, 96, 120, 0.74));
}

[data-theme="dark"] .community-tabbar__item--active .community-tabbar__icon {
  color: var(--module-color);
}

@media (min-width: 768px) {
  [data-theme="dark"] .community-tabbar {
    border-color: rgba(68, 89, 112, 0.72);
    background: rgba(24, 38, 53, 0.84);
    box-shadow: 0 18px 42px rgba(0, 0, 0, 0.24);
  }
}
</style>
