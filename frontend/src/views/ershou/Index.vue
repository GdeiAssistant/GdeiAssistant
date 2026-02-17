<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

const activeTab = computed(() => {
  const p = route.path
  if (p.includes('/ershou/publish')) return 'publish'
  if (p.includes('/ershou/profile')) return 'profile'
  return 'home'
})

function goTo(path) {
  router.push(path)
}
</script>

<template>
  <div class="ershou-page weui-tab">
    <div class="ershou-container weui-tab__panel">
      <router-view />
    </div>
    <!-- 原版 JSP：Tabbar 使用 <img>，强制吸底 -->
    <div class="ershou-tabbar weui-tabbar main-nav">
      <a
        href="javascript:;"
        class="ershou-tabbar__item"
        :class="{ on: activeTab === 'home' }"
        @click.prevent="goTo('/ershou/home')"
      >
        <i class="ibar weui-tabbar__icon"><img :src="activeTab === 'home' ? '/img/ershou/home_selected.png' : '/img/ershou/home_normal.png'" alt="首页"></i>
        <p class="ershou-tabbar__label weui-tabbar__label">首页</p>
      </a>
      <a
        href="javascript:;"
        class="ershou-tabbar__item"
        :class="{ on: activeTab === 'publish' }"
        @click.prevent="goTo('/ershou/publish')"
      >
        <i class="ibar weui-tabbar__icon"><img src="/img/ershou/publish.png" alt="发布"></i>
        <p class="ershou-tabbar__label weui-tabbar__label">发布</p>
      </a>
      <a
        href="javascript:;"
        class="ershou-tabbar__item"
        :class="{ on: activeTab === 'profile' }"
        @click.prevent="goTo('/ershou/profile')"
      >
        <i class="ibar weui-tabbar__icon"><img :src="activeTab === 'profile' ? '/img/ershou/personal_selected.png' : '/img/ershou/personal_normal.png'" alt="个人中心"></i>
        <p class="ershou-tabbar__label weui-tabbar__label">个人中心</p>
      </a>
    </div>
  </div>
</template>

<style scoped>
.ershou-page {
  min-height: 100vh;
  background-color: #f5f5f5;
}
.ershou-container {
  padding-bottom: 20px;
  box-sizing: border-box;
}
.ershou-tabbar.weui-tabbar {
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
}
.ershou-tabbar.main-nav {
  height: 2.7rem;
  color: #fff;
}
.ershou-tabbar__item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: rgba(255, 255, 255, 0.85);
  text-decoration: none;
  font-size: 12px;
}
.ershou-tabbar__item.on {
  color: #fff;
  font-weight: 500;
}
.ibar {
  display: block;
  margin-bottom: 2px;
}
.ibar img {
  height: 1.4rem;
  display: block;
}
.ershou-tabbar .weui-tabbar__icon {
  width: 24px !important;
  height: 24px !important;
  margin: 0 auto 2px auto !important;
  display: block;
}
.ershou-tabbar .weui-tabbar__icon img {
  width: 100%;
  height: 100%;
  display: block;
}
.ershou-tabbar .weui-tabbar__label {
  text-align: center !important;
  font-size: 12px !important;
  line-height: 1 !important;
  margin: 0 !important;
  padding: 0;
  color: #fff !important;
}
</style>
