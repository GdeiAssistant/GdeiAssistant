<script setup>
import { ref, provide, onMounted, onUnmounted } from 'vue'
import AppSidebar from './AppSidebar.vue'
import AppTopbar from './AppTopbar.vue'
import CommandPalette from '@/components/ui/CommandPalette.vue'

const sidebarOpen = ref(false)
const showCommandPalette = ref(false)

provide('showCommandPalette', showCommandPalette)

function handleCmdK(e) {
  if ((e.metaKey || e.ctrlKey) && e.key === 'k') {
    e.preventDefault()
    showCommandPalette.value = true
  }
}

function handleOpenPalette() {
  showCommandPalette.value = true
}

onMounted(() => {
  window.addEventListener('keydown', handleCmdK)
  window.addEventListener('open-command-palette', handleOpenPalette)
})

onUnmounted(() => {
  window.removeEventListener('keydown', handleCmdK)
  window.removeEventListener('open-command-palette', handleOpenPalette)
})
</script>

<template>
  <div class="flex min-h-screen">
    <AppSidebar :class="['hidden md:flex', { '!flex': sidebarOpen }]" />

    <div class="flex-1 md:ml-[220px] ml-0">
      <AppTopbar
        :sidebar-open="sidebarOpen"
        @toggle-sidebar="sidebarOpen = !sidebarOpen"
        @open-command-palette="showCommandPalette = true"
      />

      <main class="p-7 max-w-[1160px]">
        <router-view />
      </main>
    </div>

    <CommandPalette :open="showCommandPalette" @close="showCommandPalette = false" />
  </div>
</template>
