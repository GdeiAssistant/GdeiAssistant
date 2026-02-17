<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

const activeTab = computed(() => {
  const p = route.path
  if (p.includes('/express/publish')) return 'publish'
  if (p.includes('/express/search')) return 'search'
  return 'home'
})

function goTo(path) {
  router.push(path)
}
</script>

<template>
  <div class="express-page weui-tab">
    <div class="express-container weui-tab__panel">
      <router-view />
    </div>
    <!-- 专属黑底 Tabbar：选中态红色，非选中态灰色 -->
    <div class="express-tabbar weui-tabbar">
      <a
        href="javascript:;"
        class="express-tabbar__item"
        :class="{ on: activeTab === 'home' }"
        @click.prevent="goTo('/express/home')"
      >
        <i class="express-tabbar__icon">
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" width="22" height="22">
            <path d="M14 2H6c-1.1 0-2 .9-2 2v16c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V8l-6-6zm2 14H8v-2h8v2zm0-4H8v-2h8v2zm-3-5V3.5L18.5 9H13z"/>
          </svg>
        </i>
        <p class="express-tabbar__label">首页</p>
      </a>
      <a
        href="javascript:;"
        class="express-tabbar__item"
        :class="{ on: activeTab === 'publish' }"
        @click.prevent="goTo('/express/publish')"
      >
        <i class="express-tabbar__icon">
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" width="22" height="22">
            <path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/>
          </svg>
        </i>
        <p class="express-tabbar__label">表白</p>
      </a>
      <a
        href="javascript:;"
        class="express-tabbar__item"
        :class="{ on: activeTab === 'search' }"
        @click.prevent="goTo('/express/search')"
      >
        <i class="express-tabbar__icon">
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" width="22" height="22">
            <path d="M15.5 14h-.79l-.28-.27A6.471 6.471 0 0016 9.5 6.5 6.5 0 109.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z"/>
          </svg>
        </i>
        <p class="express-tabbar__label">搜索</p>
      </a>
    </div>
  </div>
</template>

<style scoped>
.express-page {
  background-color: #f5f5f5;
}
.express-container {
  padding-bottom: 60px;
  box-sizing: border-box;
}
/* 纯黑背景 Tabbar */
.express-tabbar.weui-tabbar {
  position: fixed !important;
  bottom: 0 !important;
  left: 0 !important;
  right: 0 !important;
  width: 100vw !important;
  max-width: 100%;
  margin: 0 !important;
  padding: 0 !important;
  z-index: 500;
  background-color: #000 !important;
  display: flex;
}
.express-tabbar__item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #999;
  text-decoration: none;
  font-size: 12px;
  padding: 8px 0;
}
.express-tabbar__item.on {
  color: #e53935 !important;
  font-weight: 500;
}
.express-tabbar__item.on .express-tabbar__icon {
  color: #e53935 !important;
}
.express-tabbar__icon {
  display: block;
  margin-bottom: 4px;
  color: #999;
}
.express-tabbar__item.on .express-tabbar__icon svg {
  fill: #e53935;
}
.express-tabbar__label {
  margin: 0;
  font-size: 12px;
  line-height: 1;
  color: inherit;
}
</style>
