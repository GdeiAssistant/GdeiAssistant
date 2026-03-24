<script setup>
import CommunityTabbar from '../../components/community/CommunityTabbar.vue'
</script>

<template>
  <div class="min-h-screen bg-[var(--c-bg)]" style="--module-color: #6366f1">
    <div class="pb-14 box-border">
      <router-view />
    </div>
    <CommunityTabbar
      basePath="/topic"
      moduleColor="#6366f1"
      :tabs="[
        { key: 'home', label: '首页', path: '/topic/home', icon: '<svg xmlns=&quot;http://www.w3.org/2000/svg&quot; viewBox=&quot;0 0 24 24&quot; fill=&quot;currentColor&quot;><path d=&quot;M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z&quot;/></svg>' },
        { key: 'publish', label: '发布', path: '/topic/publish', icon: '<svg xmlns=&quot;http://www.w3.org/2000/svg&quot; viewBox=&quot;0 0 24 24&quot; fill=&quot;currentColor&quot;><path d=&quot;M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z&quot;/></svg>' },
        { key: 'search', label: '搜索', path: '/topic/search', icon: '<svg xmlns=&quot;http://www.w3.org/2000/svg&quot; viewBox=&quot;0 0 24 24&quot; fill=&quot;currentColor&quot;><path d=&quot;M15.5 14h-.79l-.28-.27A6.471 6.471 0 0016 9.5 6.5 6.5 0 109.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z&quot;/></svg>' }
      ]"
    />
  </div>
</template>
