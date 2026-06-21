<script setup>
import { computed, ref, provide, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { Home, Newspaper, UserRound } from 'lucide-vue-next'
import AppSidebar from './AppSidebar.vue'
import AppTopbar from './AppTopbar.vue'
import CommandPalette from '@/components/ui/CommandPalette.vue'

const router = useRouter()
const route = useRoute()
const { t } = useI18n()
const sidebarOpen = ref(false)
const showCommandPalette = ref(false)

provide('showCommandPalette', showCommandPalette)

const mobileTabs = computed(() => [
  { path: '/home', label: t('tab.home'), icon: Home },
  { path: '/info', label: t('tab.info'), icon: Newspaper },
  { path: '/profile', label: t('tab.profile'), icon: UserRound }
])

function handleCmdK(e) {
  if ((e.metaKey || e.ctrlKey) && e.key === 'k') {
    e.preventDefault()
    showCommandPalette.value = true
  }
}

function handleOpenPalette() {
  showCommandPalette.value = true
}

function closeSidebar() {
  sidebarOpen.value = false
}

function isMobileTabActive(path) {
  return route.path === path || route.path.startsWith(path + '/')
}

function navigateMobile(path) {
  closeSidebar()
  router.push(path)
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
  <div class="campus-app-shell">
    <button
      v-if="sidebarOpen"
      type="button"
      class="campus-sidebar-backdrop"
      aria-label="关闭侧边栏"
      @click="closeSidebar"
    />

    <AppSidebar
      :class="{ 'campus-sidebar--open': sidebarOpen }"
      @navigate="closeSidebar"
    />

    <div class="campus-main">
      <AppTopbar
        :sidebar-open="sidebarOpen"
        @toggle-sidebar="sidebarOpen = !sidebarOpen"
      />

      <main class="campus-content">
        <router-view />
      </main>
    </div>

    <nav class="campus-mobile-tabbar" aria-label="主导航">
      <button
        v-for="tab in mobileTabs"
        :key="tab.path"
        type="button"
        class="campus-mobile-tabbar__item"
        :class="{ 'campus-mobile-tabbar__item--active': isMobileTabActive(tab.path) }"
        :aria-current="isMobileTabActive(tab.path) ? 'page' : undefined"
        @click="navigateMobile(tab.path)"
      >
        <component :is="tab.icon" class="campus-mobile-tabbar__icon" />
        <span>{{ tab.label }}</span>
      </button>
    </nav>

    <CommandPalette :open="showCommandPalette" @close="showCommandPalette = false" />
  </div>
</template>

<style scoped>
.campus-app-shell {
  position: relative;
  min-height: 100vh;
  background:
    radial-gradient(circle at 18% 10%, rgba(175, 225, 255, 0.42), transparent 28%),
    radial-gradient(circle at 82% 6%, rgba(183, 238, 207, 0.38), transparent 26%),
    linear-gradient(180deg, #f6fcff 0%, var(--c-bg) 44%, #f8fbf9 100%);
}

.campus-main {
  min-height: 100vh;
  margin-left: 232px;
}

.campus-content {
  width: 100%;
  max-width: 1360px;
  margin: 0 auto;
  padding: 26px 32px 56px;
}

.campus-sidebar-backdrop {
  position: fixed;
  inset: 0;
  z-index: 45;
  display: none;
  border: 0;
  background: rgba(15, 23, 42, 0.28);
  backdrop-filter: blur(6px);
}

.campus-mobile-tabbar {
  display: none;
}

@media (max-width: 767px) {
  .campus-main {
    margin-left: 0;
  }

  .campus-content {
    padding: 16px 14px 104px;
  }

  .campus-sidebar-backdrop {
    display: block;
  }

  .campus-mobile-tabbar {
    position: fixed;
    right: 0;
    bottom: 0;
    left: 0;
    z-index: 50;
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    padding: 9px 18px calc(9px + env(safe-area-inset-bottom, 0));
    border-top: 1px solid rgba(207, 221, 225, 0.72);
    background: rgba(255, 255, 255, 0.92);
    box-shadow: 0 -16px 34px rgba(15, 39, 49, 0.08);
    backdrop-filter: blur(18px);
  }

  .campus-mobile-tabbar__item {
    display: flex;
    min-height: 54px;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 4px;
    border: 0;
    border-radius: 18px;
    background: transparent;
    color: var(--c-text-3);
    font: inherit;
    font-size: 12px;
    font-weight: 700;
  }

  .campus-mobile-tabbar__icon {
    width: 22px;
    height: 22px;
  }

  .campus-mobile-tabbar__item--active {
    color: var(--c-primary);
    background: var(--c-primary-50);
  }
}

@media (max-width: 480px) {
  .campus-content {
    padding-right: 12px;
    padding-left: 12px;
  }
}

[data-theme="dark"] .campus-app-shell {
  background:
    radial-gradient(circle at 15% 8%, rgba(45, 212, 191, 0.1), transparent 28%),
    radial-gradient(circle at 84% 10%, rgba(96, 165, 250, 0.1), transparent 26%),
    linear-gradient(180deg, #101923 0%, #0f1822 100%);
}

[data-theme="dark"] .campus-mobile-tabbar {
  border-top-color: rgba(68, 89, 112, 0.74);
  background: rgba(18, 30, 42, 0.92);
  box-shadow: 0 -16px 34px rgba(0, 0, 0, 0.28);
}
</style>
