<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

const activeTab = computed(() => {
  const p = route.path
  if (p.includes('/topic/publish')) return 'publish'
  if (p.includes('/topic/search')) return 'search'
  return 'home'
})

function goTo(path) {
  router.push(path)
}
</script>

<template>
  <div class="topic-page">
    <div class="topic-container">
      <router-view />
    </div>
    <!-- 专属底部 Tabbar：亮绿色主题 -->
    <div class="topic-tabbar">
      <a
        href="javascript:;"
        class="topic-tabbar__item"
        :class="{ active: activeTab === 'home' }"
        @click.prevent="goTo('/topic/home')"
      >
        <i class="topic-tabbar__icon">
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" width="22" height="22">
            <path d="M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z"/>
          </svg>
        </i>
        <p class="topic-tabbar__label">首页</p>
      </a>
      <a
        href="javascript:;"
        class="topic-tabbar__item"
        :class="{ active: activeTab === 'publish' }"
        @click.prevent="goTo('/topic/publish')"
      >
        <i class="topic-tabbar__icon">
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" width="22" height="22">
            <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"/>
          </svg>
        </i>
        <p class="topic-tabbar__label">发布</p>
      </a>
      <a
        href="javascript:;"
        class="topic-tabbar__item"
        :class="{ active: activeTab === 'search' }"
        @click.prevent="goTo('/topic/search')"
      >
        <i class="topic-tabbar__icon">
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" width="22" height="22">
            <path d="M15.5 14h-.79l-.28-.27A6.471 6.471 0 0016 9.5 6.5 6.5 0 109.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z"/>
          </svg>
        </i>
        <p class="topic-tabbar__label">搜索</p>
      </a>
    </div>
  </div>
</template>

<style scoped>
.topic-page {
  min-height: 100vh;
  background-color: #f5f5f5;
}
.topic-container {
  padding-bottom: 60px;
  box-sizing: border-box;
}

.topic-tabbar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  width: 100%;
  height: 60px;
  background: #fff;
  border-top: 1px solid #e5e5e5;
  display: flex;
  z-index: 500;
  box-shadow: 0 -2px 8px rgba(0,0,0,0.04);
}

.topic-tabbar__item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #999;
  text-decoration: none;
  font-size: 12px;
  padding: 6px 0;
  transition: all 0.3s ease;
}

.topic-tabbar__item.active {
  color: #10b981;
  font-weight: 500;
}

.topic-tabbar__item.active .topic-tabbar__icon {
  color: #10b981;
  transform: scale(1.1);
}

.topic-tabbar__icon {
  display: block;
  margin-bottom: 4px;
  color: #999;
  transition: all 0.3s ease;
}

.topic-tabbar__item.active .topic-tabbar__icon svg {
  fill: #10b981;
}

.topic-tabbar__label {
  margin: 0;
  font-size: 12px;
  line-height: 1;
  color: inherit;
}
</style>
