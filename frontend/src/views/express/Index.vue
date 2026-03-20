<script setup>
import CommunityTabbar from '../../components/community/CommunityTabbar.vue'
</script>

<template>
  <div class="community-page" style="--module-color: #f43f5e">
    <div style="padding-bottom: 56px">
      <router-view />
    </div>
    <CommunityTabbar
      basePath="/express"
      moduleColor="#f43f5e"
      :tabs="[
        { key: 'home', label: '首页', path: '/express/home', icon: '<svg xmlns=&quot;http://www.w3.org/2000/svg&quot; viewBox=&quot;0 0 24 24&quot; fill=&quot;currentColor&quot;><path d=&quot;M14 2H6c-1.1 0-2 .9-2 2v16c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V8l-6-6zm2 14H8v-2h8v2zm0-4H8v-2h8v2zm-3-5V3.5L18.5 9H13z&quot;/></svg>' },
        { key: 'publish', label: '表白', path: '/express/publish', icon: '<svg xmlns=&quot;http://www.w3.org/2000/svg&quot; viewBox=&quot;0 0 24 24&quot; fill=&quot;currentColor&quot;><path d=&quot;M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z&quot;/></svg>' },
        { key: 'search', label: '搜索', path: '/express/search', icon: '<svg xmlns=&quot;http://www.w3.org/2000/svg&quot; viewBox=&quot;0 0 24 24&quot; fill=&quot;currentColor&quot;><path d=&quot;M15.5 14h-.79l-.28-.27A6.471 6.471 0 0016 9.5 6.5 6.5 0 109.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z&quot;/></svg>' }
      ]"
    />
  </div>
</template>
