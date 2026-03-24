<script setup>
import CommunityTabbar from '../../components/community/CommunityTabbar.vue'

const tabs = [
  { key: 'home', label: '大厅', path: '/delivery/home', icon: '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor"><path d="M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z"/></svg>' },
  { key: 'publish', label: '发布', path: '/delivery/publish', icon: '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor"><path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"/></svg>' },
  { key: 'mine', label: '我的', path: '/delivery/mine', icon: '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor"><path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/></svg>' }
]
</script>

<template>
  <div class="min-h-screen" style="--module-color: #f59e0b">
    <div class="pb-14 box-border">
      <router-view />
    </div>
    <CommunityTabbar
      basePath="/delivery"
      moduleColor="#f59e0b"
      :tabs="tabs"
    />
  </div>
</template>
