<script setup>
import { ref, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

const tabs = [
  { path: '/home', label: '功能主页', icon: '/img/index/home.png' },
  { path: '/info', label: '资讯信息', icon: '/img/index/info.png' },
  { path: '/profile', label: '个人中心', icon: '/img/index/personal.png' }
]

const activeIndex = ref(0)

function goTo(path, index) {
  activeIndex.value = index
  router.push(path)
}

watch(
  () => route.path,
  (path) => {
    const i = tabs.findIndex((t) => t.path === path)
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
    <div class="weui-tabbar">
      <div
        v-for="(tab, index) in tabs"
        :key="tab.path"
        class="weui-tabbar__item"
        :class="{ 'weui-bar__item_on': activeIndex === index }"
        @click="goTo(tab.path, index)"
      >
        <img :src="tab.icon" alt="" class="weui-tabbar__icon" />
        <p class="weui-tabbar__label">{{ tab.label }}</p>
      </div>
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
}

.weui-tabbar__icon {
  width: 28px !important;
  height: 28px !important;
}
/* 选中态：原版 PNG 无高亮图，用滤镜高亮为绿色 #09bb07 */
.weui-tabbar .weui-bar__item_on .weui-tabbar__icon {
  filter: brightness(0) saturate(100%) invert(48%) sepia(79%) saturate(2476%) hue-rotate(86deg);
}
.weui-tabbar .weui-bar__item_on .weui-tabbar__label {
  color: #09bb07;
}
</style>
