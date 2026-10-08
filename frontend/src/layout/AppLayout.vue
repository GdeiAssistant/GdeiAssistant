<script setup>
import { computed, ref, provide, onMounted, onUnmounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { Home, Bell, UserRound } from 'lucide-vue-next'
import AppSidebar from './AppSidebar.vue'
import AppTopbar from './AppTopbar.vue'
import CommandPalette from '@/components/ui/CommandPalette.vue'

const router = useRouter()
const route = useRoute()
const { t } = useI18n()
import { messageBadge, refreshMessageUnread, resetMessageUnread } from '@/composables/useMessageUnread'
const sidebarOpen = ref(false)
let unreadTimer
const refreshVisibleUnread = () => { if (!document.hidden) refreshMessageUnread() }
watch(() => route.path, refreshVisibleUnread)
const showCommandPalette = ref(false)

provide('showCommandPalette', showCommandPalette)

const mobileTabs = computed(() => [
  { path: '/home', label: t('tab.home'), icon: Home },
  { path: '/info', label: t('tab.info'), icon: Bell },
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
  refreshVisibleUnread()
  unreadTimer = setInterval(refreshVisibleUnread, 30000)
  window.addEventListener('social-auth-changed', resetMessageUnread)
  document.addEventListener('visibilitychange', refreshVisibleUnread)
  window.addEventListener('keydown', handleCmdK)
  window.addEventListener('open-command-palette', handleOpenPalette)
})

onUnmounted(() => {
  clearInterval(unreadTimer)
  window.removeEventListener('social-auth-changed', resetMessageUnread)
  document.removeEventListener('visibilitychange', refreshVisibleUnread)
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
      :aria-label="t('navigationAccessibility.closeSidebar')"
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

    <nav class="campus-mobile-tabbar" :aria-label="t('navigationAccessibility.mainNavigation')">
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
        <span v-if="tab.path === '/info' && messageBadge" class="message-unread-badge">{{ messageBadge }}</span>
      </button>
    </nav>

    <CommandPalette :open="showCommandPalette" @close="showCommandPalette = false" />
  </div>
</template>

<style scoped>
.campus-app-shell {
  position: relative;
  min-height: 100vh;
  background: var(--c-bg);
}

.campus-main {
  min-height: 100vh;
  margin-left: var(--rail-width, 248px);
}

.campus-content {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 28px 40px 64px;
}

.campus-sidebar-backdrop {
  position: fixed;
  inset: 0;
  z-index: 45;
  display: none;
  border: 0;
  background: rgb(10 20 17 / 42%);
}

.campus-mobile-tabbar {
  display: none;
}

@media (max-width: 1023px) {
  .campus-main {
    margin-left: 0;
  }

  .campus-content {
    max-width: 42rem;
    padding: 24px 24px 56px;
  }

  .campus-sidebar-backdrop {
    display: block;
  }
}

@media (max-width: 767px) {
  .campus-content {
    max-width: 100%;
    padding: 16px 16px calc(88px + env(safe-area-inset-bottom, 0px));
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
    padding: 6px 8px calc(6px + env(safe-area-inset-bottom, 0px));
    border-top: 1px solid var(--c-border);
    background: var(--c-surface);
  }

  .campus-mobile-tabbar__item {
    position: relative;
    display: flex;
    min-height: 52px;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 3px;
    border: 0;
    border-radius: var(--radius-control);
    background: transparent;
    color: var(--c-text-3);
    font: inherit;
    font-size: 11px;
    font-weight: 600;
    cursor: pointer;
  }

  .campus-mobile-tabbar__icon {
    width: 22px;
    height: 22px;
    stroke-width: 1.75;
  }

  .campus-mobile-tabbar__item--active {
    color: var(--c-primary);
  }

  .campus-mobile-tabbar__item--active::before {
    position: absolute;
    top: -6px;
    left: 50%;
    width: 24px;
    height: 2px;
    border-radius: 2px;
    background: var(--c-primary);
    content: '';
    transform: translateX(-50%);
  }
}
</style>
