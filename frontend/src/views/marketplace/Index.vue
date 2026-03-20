<script setup>
import CommunityTabbar from '../../components/community/CommunityTabbar.vue'
</script>

<template>
  <div class="community-page" style="--module-color: #10b981">
    <div class="ershou-container" style="padding-bottom: 56px">
      <router-view />
    </div>
    <CommunityTabbar
      basePath="/marketplace"
      moduleColor="#10b981"
      :tabs="[
        { key: 'home', label: '首页', path: '/marketplace/home', icon: '<svg xmlns=&quot;http://www.w3.org/2000/svg&quot; viewBox=&quot;0 0 24 24&quot; fill=&quot;currentColor&quot;><path d=&quot;M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z&quot;/></svg>' },
        { key: 'publish', label: '发布', path: '/marketplace/publish', icon: '<svg xmlns=&quot;http://www.w3.org/2000/svg&quot; viewBox=&quot;0 0 24 24&quot; fill=&quot;currentColor&quot;><path d=&quot;M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z&quot;/></svg>' },
        { key: 'profile', label: '我的', path: '/marketplace/profile', icon: '<svg xmlns=&quot;http://www.w3.org/2000/svg&quot; viewBox=&quot;0 0 24 24&quot; fill=&quot;currentColor&quot;><path d=&quot;M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z&quot;/></svg>' }
      ]"
    />
  </div>
</template>

<style scoped>
.ershou-container {
  box-sizing: border-box;
}
</style>
