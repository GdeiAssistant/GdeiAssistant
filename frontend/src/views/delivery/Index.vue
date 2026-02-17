<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

const activeTab = computed(() => {
  const p = route.path
  if (p.includes('/delivery/publish')) return 'publish'
  if (p.includes('/delivery/mine')) return 'mine'
  return 'home'
})

function goTo(path) {
  router.push(path)
}
</script>

<template>
  <div class="delivery-page">
    <div class="delivery-container">
      <router-view />
    </div>
    <!-- 专属底部 Tabbar：跑腿橙主题 -->
    <div class="delivery-tabbar">
      <a
        href="javascript:;"
        class="delivery-tabbar__item"
        :class="{ active: activeTab === 'home' }"
        @click.prevent="goTo('/delivery/home')"
      >
        <i class="delivery-tabbar__icon">
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" width="22" height="22">
            <path d="M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z"/>
          </svg>
        </i>
        <p class="delivery-tabbar__label">大厅</p>
      </a>
      <a
        href="javascript:;"
        class="delivery-tabbar__item"
        :class="{ active: activeTab === 'publish' }"
        @click.prevent="goTo('/delivery/publish')"
      >
        <i class="delivery-tabbar__icon">
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" width="22" height="22">
            <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"/>
          </svg>
        </i>
        <p class="delivery-tabbar__label">发布</p>
      </a>
      <a
        href="javascript:;"
        class="delivery-tabbar__item"
        :class="{ active: activeTab === 'mine' }"
        @click.prevent="goTo('/delivery/mine')"
      >
        <i class="delivery-tabbar__icon">
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" width="22" height="22">
            <path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/>
          </svg>
        </i>
        <p class="delivery-tabbar__label">我的</p>
      </a>
    </div>
  </div>
</template>

<style scoped>
.delivery-page {
  min-height: 100vh;
  background-color: #f5f5f5;
}
.delivery-container {
  padding-bottom: 60px;
  box-sizing: border-box;
}

.delivery-tabbar {
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

.delivery-tabbar__item {
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

.delivery-tabbar__item.active {
  color: #fa8231;
  font-weight: 500;
}

.delivery-tabbar__item.active .delivery-tabbar__icon {
  color: #fa8231;
  transform: scale(1.1);
}

.delivery-tabbar__icon {
  display: block;
  margin-bottom: 4px;
  color: #999;
  transition: all 0.3s ease;
}

.delivery-tabbar__item.active .delivery-tabbar__icon svg {
  fill: #fa8231;
}

.delivery-tabbar__label {
  margin: 0;
  font-size: 12px;
  line-height: 1;
  color: inherit;
}
</style>
