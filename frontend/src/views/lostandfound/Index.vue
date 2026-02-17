<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

const activeTab = computed(() => {
  const p = route.path
  if (p.includes('/lostandfound/publish')) return 'publish'
  if (p.includes('/lostandfound/profile')) return 'profile'
  return 'home'
})

function goTo(path) {
  router.push(path)
}
</script>

<template>
  <div class="lostandfound-page">
    <div class="lostandfound-container">
      <router-view />
    </div>
    <!-- 底部 Tabbar -->
    <div class="lostandfound-tabbar">
      <a
        href="javascript:;"
        class="tabbar-item"
        :class="{ on: activeTab === 'home' }"
        @click.prevent="goTo('/lostandfound/home')"
      >
        <i class="tabbar-icon">
          <img :src="activeTab === 'home' ? '/img/lostandfound/lost_selected.png' : '/img/lostandfound/lost_normal.png'" alt="首页" style="width: 24px; height: 24px; display: block;" />
        </i>
        <p class="tabbar-label">首页</p>
      </a>
      <a
        href="javascript:;"
        class="tabbar-item"
        :class="{ on: activeTab === 'publish' }"
        @click.prevent="goTo('/lostandfound/publish')"
      >
        <i class="tabbar-icon">
          <img src="/img/lostandfound/publish.png" alt="发布" style="width: 24px; height: 24px; display: block;" />
        </i>
        <p class="tabbar-label">发布</p>
      </a>
      <a
        href="javascript:;"
        class="tabbar-item"
        :class="{ on: activeTab === 'profile' }"
        @click.prevent="goTo('/lostandfound/profile')"
      >
        <i class="tabbar-icon">
          <img :src="activeTab === 'profile' ? '/img/lostandfound/personal_selected.png' : '/img/lostandfound/personal_normal.png'" alt="个人中心" style="width: 24px; height: 24px; display: block;" />
        </i>
        <p class="tabbar-label">个人中心</p>
      </a>
    </div>
  </div>
</template>

<style scoped>
.lostandfound-page {
  min-height: 100vh;
  background-color: #f5f5f5;
}
.lostandfound-container {
  padding-bottom: 20px;
  box-sizing: border-box;
}
.lostandfound-tabbar {
  position: fixed !important;
  bottom: 0 !important;
  left: 0 !important;
  right: 0 !important;
  width: 100vw !important;
  max-width: 100%;
  margin: 0 !important;
  padding: 0 !important;
  z-index: 500;
  background-color: #3cb395;
  display: flex;
  height: 2.7rem;
}
.tabbar-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: rgba(255, 255, 255, 0.85);
  text-decoration: none;
  font-size: 12px;
}
.tabbar-item.on {
  color: #fff;
  font-weight: 500;
}
.tabbar-icon {
  width: 24px !important;
  height: 24px !important;
  margin: 0 auto 2px auto !important;
  display: block;
}
.tabbar-icon img {
  width: 100%;
  height: 100%;
  display: block;
}
.tabbar-label {
  text-align: center !important;
  font-size: 12px !important;
  line-height: 1 !important;
  margin: 0 !important;
  padding: 0;
  color: #fff !important;
}
</style>
