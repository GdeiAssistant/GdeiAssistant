<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { Moon, Sun, Menu, Search } from 'lucide-vue-next'
import { useThemeMode } from '@/composables/useThemeMode'
import { setThemeMode } from '@/theme'
import { resolveRouteTitle } from './navigation'

defineProps({ sidebarOpen: Boolean })
const route = useRoute()
const { t } = useI18n()
const emit = defineEmits(['toggle-sidebar'])

const isDark = useThemeMode()

const pageTitle = computed(() => {
  return resolveRouteTitle(route, t)
})

function toggleTheme() {
  setThemeMode(isDark.value ? 'light' : 'dark')
}

function openPalette() {
  window.dispatchEvent(new CustomEvent('open-command-palette'))
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
      <span class="campus-topbar__title editorial-serif">
        <span class="campus-topbar__desktop-title">{{ pageTitle }}</span>
        <span class="campus-topbar__mobile-title">{{ $t('about.appName') }}</span>
      </span>
    </div>

    <div class="campus-topbar__actions">
      <button
        type="button"
        class="campus-topbar__search-trigger"
        :aria-label="t('home.openCommandPalette')"
        @click="openPalette"
      >
        <Search class="w-3.5 h-3.5 shrink-0" aria-hidden="true" />
        <span class="campus-topbar__search-label">{{ t('home.openCommandPalette') }}</span>
        <kbd class="campus-topbar__kbd tabular-num">⌘K</kbd>
      </button>
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
  padding: 0 44px;
  border-bottom: 1px solid var(--c-border);
  background: color-mix(in srgb, var(--c-bg) 90%, transparent);
  backdrop-filter: saturate(1.1) blur(10px);
}

.campus-topbar__left,
.campus-topbar__actions {
  display: flex;
  min-width: 0;
  align-items: center;
  gap: 10px;
}

.campus-topbar__title {
  overflow: hidden;
  color: var(--c-text-1);
  font-size: 16px;
  font-weight: 700;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.campus-topbar__mobile-title {
  display: none;
}

.campus-topbar__search-trigger {
  display: inline-flex;
  min-height: 36px;
  align-items: center;
  gap: 8px;
  padding: 0 10px;
  border: 1px solid var(--c-border);
  border-radius: var(--radius-control);
  background: var(--c-surface);
  color: var(--c-text-2);
  cursor: pointer;
  font: inherit;
  font-size: 12px;
  font-weight: 500;
  transition: border-color 0.15s ease, color 0.15s ease;
}

.campus-topbar__search-trigger:hover {
  border-color: color-mix(in srgb, var(--c-primary) 45%, var(--c-border));
  color: var(--c-primary);
}

.campus-topbar__kbd {
  border: 1px solid var(--c-divider);
  border-radius: 5px;
  background: var(--c-bg);
  padding: 1px 5px;
  font-family: var(--font-mono);
  font-size: 10px;
  color: var(--c-text-3);
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

  .campus-topbar__menu {
    display: inline-flex;
  }
}

@media (max-width: 767px) {
  .campus-topbar {
    height: 56px;
    padding: 0 16px;
  }

  .campus-topbar__desktop-title {
    display: none;
  }

  .campus-topbar__mobile-title {
    display: inline;
  }

  .campus-topbar__search-label,
  .campus-topbar__kbd {
    display: none;
  }

  .campus-topbar__search-trigger {
    width: 40px;
    height: 40px;
    justify-content: center;
    padding: 0;
  }
}
</style>
