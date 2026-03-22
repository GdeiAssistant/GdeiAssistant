<script setup>
import { ref, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'

const router = useRouter()
const route = useRoute()
const { t } = useI18n()

const tabs = [
  { path: '/home', labelKey: 'tab.home', icon: '/img/index/home.png' },
  { path: '/info', labelKey: 'tab.info', icon: '/img/index/info.png' },
  { path: '/profile', labelKey: 'tab.profile', icon: '/img/index/personal.png' }
]

const activeIndex = ref(0)

function goTo(path, index) {
  activeIndex.value = index
  router.push(path)
}

watch(
  () => route.path,
  (path) => {
    const i = tabs.findIndex((tab) => tab.path === path)
    if (i >= 0) activeIndex.value = i
  },
  { immediate: true }
)
</script>

<template>
  <div class="weui-tab">
    <div class="weui-tab__panel">
      <router-view />
    </div>
    <div class="weui-tabbar" role="tablist">
      <button
        v-for="(tab, index) in tabs"
        :key="tab.path"
        type="button"
        role="tab"
        :aria-label="$t(tab.labelKey)"
        :aria-selected="activeIndex === index"
        class="weui-tabbar__item"
        :class="{ 'weui-bar__item_on': activeIndex === index }"
        @click="goTo(tab.path, index)"
      >
        <img :src="tab.icon" alt="" class="weui-tabbar__icon" />
        <p class="weui-tabbar__label">{{ $t(tab.labelKey) }}</p>
      </button>
    </div>
  </div>
</template>

<style scoped>
.weui-tab {
  width: 100%;
  min-height: 100vh;
  padding-bottom: 55px;
  box-sizing: border-box;
}
.weui-tab__panel {
  width: 100%;
  min-height: calc(100vh - 55px);
  box-sizing: border-box;
}
.weui-tabbar {
  position: fixed;
  right: 0;
  bottom: 0;
  left: 0;
  z-index: 500;
  padding-bottom: env(safe-area-inset-bottom, 0);
}

.weui-tabbar__icon {
  width: 28px !important;
  height: 28px !important;
}
.weui-tabbar .weui-bar__item_on .weui-tabbar__icon {
  filter: brightness(0) saturate(100%) invert(48%) sepia(79%) saturate(2476%) hue-rotate(86deg);
}
.weui-tabbar .weui-bar__item_on .weui-tabbar__label {
  color: var(--color-primary);
}
.weui-tabbar {
  background: var(--color-surface);
  border-top: 1px solid var(--color-divider);
}
.weui-tabbar__item {
  border: none;
  background: transparent;
  cursor: pointer;
  font: inherit;
  color: inherit;
  padding: 0;
}
.weui-tabbar__item:focus-visible {
  outline: 2px solid var(--color-primary);
  outline-offset: -2px;
}
</style>
