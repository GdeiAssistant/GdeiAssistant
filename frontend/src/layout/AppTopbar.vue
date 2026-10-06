<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { Moon, Sun, Menu } from 'lucide-vue-next'
import { resolveRouteTitle } from './navigation'

defineProps({ sidebarOpen: Boolean })
const route = useRoute()
const { t } = useI18n()
const emit = defineEmits(['toggle-sidebar'])

const pageTitle = computed(() => {
  return resolveRouteTitle(route, t)
})

const isDark = ref(false)

onMounted(() => {
  const saved = localStorage.getItem('theme')
  if (saved) {
    isDark.value = saved === 'dark'
  } else {
    isDark.value = document.documentElement.dataset.theme === 'dark'
  }
  applyTheme()
})

function applyTheme() {
  document.documentElement.dataset.theme = isDark.value ? 'dark' : 'light'
  localStorage.setItem('theme', isDark.value ? 'dark' : 'light')
}

function toggleTheme() {
  isDark.value = !isDark.value
  applyTheme()
}
</script>

<template>
  <header class="campus-topbar">
    <div class="campus-topbar__left">
      <button
        class="campus-topbar__icon-button campus-topbar__menu"
        type="button"
        :aria-expanded="sidebarOpen"
        :aria-label="t('navigationAccessibility.openSidebar')"
        @click="emit('toggle-sidebar')"
      >
        <Menu class="w-4 h-4" />
      </button>
      <span class="campus-topbar__title"><span class="campus-topbar__desktop-title">{{ pageTitle }}</span><span class="campus-topbar__mobile-title">{{ $t('about.appName') }}</span></span>
    </div>

    <div class="campus-topbar__actions">
      <button
        class="campus-topbar__icon-button"
        type="button"
        :aria-label="t(isDark ? 'navigationAccessibility.lightMode' : 'navigationAccessibility.darkMode')"
        @click="toggleTheme"
      >
        <Moon v-if="!isDark" class="w-4 h-4" />
        <Sun v-else class="w-4 h-4" />
      </button>
    </div>
  </header>
</template>

<style scoped>
.campus-topbar {
  position: sticky;
  top: 0;
  z-index: 42;
  display: flex;
  height: 64px;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 0 40px;
  border-bottom: 1px solid var(--c-border);
  background: color-mix(in srgb, var(--c-bg) 92%, transparent);
  backdrop-filter: saturate(1.2) blur(10px);
}

.campus-topbar__left,
.campus-topbar__actions {
  display: flex;
  min-width: 0;
  align-items: center;
  gap: 12px;
}

.campus-topbar__title {
  overflow: hidden;
  color: var(--c-text-1);
  font-size: 16px;
  font-weight: 650;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.campus-topbar__mobile-title {
  display: none;
}

.campus-topbar__icon-button {
  display: inline-flex;
  width: 40px;
  height: 40px;
  flex: none;
  align-items: center;
  justify-content: center;
  border: 1px solid var(--c-border);
  border-radius: var(--radius-control);
  background: var(--c-surface);
  color: var(--c-text-2);
  cursor: pointer;
  font: inherit;
  transition: border-color 0.15s ease, color 0.15s ease;
}

.campus-topbar__icon-button:hover {
  border-color: color-mix(in srgb, var(--c-primary) 45%, var(--c-border));
  color: var(--c-primary);
}

.campus-topbar__menu {
  display: none;
}

@media (max-width: 1023px) {
  .campus-topbar {
    padding: 0 24px;
  }
}

@media (max-width: 767px) {
  .campus-topbar {
    height: 56px;
    padding: 0 16px;
  }

  .campus-topbar__menu {
    display: inline-flex;
  }

  .campus-topbar__desktop-title {
    display: none;
  }

  .campus-topbar__mobile-title {
    display: inline;
  }
}
</style>
