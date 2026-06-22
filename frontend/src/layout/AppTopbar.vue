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
        aria-label="打开侧边栏"
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
        :aria-label="isDark ? '切换到浅色模式' : '切换到深色模式'"
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
  min-height: 66px;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  padding: 12px 32px;
  border-bottom: 1px solid rgba(211, 225, 229, 0.72);
  background: rgba(248, 253, 255, 0.76);
  backdrop-filter: blur(18px);
}

.campus-topbar__left,
.campus-topbar__actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.campus-topbar__title {
  color: var(--c-text-1);
  font-size: 17px;
  font-weight: 850;
  letter-spacing: -0.01em;
}

.campus-topbar__mobile-title {
  display: none;
}

.campus-topbar__icon-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: 1px solid rgba(199, 216, 221, 0.82);
  background: rgba(255, 255, 255, 0.82);
  color: var(--c-text-2);
  cursor: pointer;
  font: inherit;
  transition: border-color 0.18s ease, box-shadow 0.18s ease, color 0.18s ease, transform 0.18s ease;
}

.campus-topbar__icon-button:hover {
  border-color: rgba(30, 166, 123, 0.32);
  color: var(--c-primary);
  box-shadow: 0 12px 24px rgba(32, 69, 78, 0.08);
  transform: translateY(-1px);
}

.campus-topbar__icon-button {
  width: 40px;
  height: 40px;
  border-radius: 16px;
}

.campus-topbar__menu {
  display: none;
}

@media (max-width: 767px) {
  .campus-topbar {
    min-height: 56px;
    gap: 12px;
    padding: 8px 12px;
  }

  .campus-topbar__menu {
    display: inline-flex;
  }

  .campus-topbar__left,
  .campus-topbar__actions {
    gap: 10px;
  }

  .campus-topbar__title {
    font-size: 16px;
    font-weight: 820;
  }

  .campus-topbar__icon-button {
    width: 36px;
    height: 36px;
    border-radius: 14px;
  }

  .campus-topbar__desktop-title {
    display: none;
  }

  .campus-topbar__mobile-title {
    display: inline;
  }
}

[data-theme="dark"] .campus-topbar {
  border-bottom-color: rgba(68, 89, 112, 0.72);
  background: rgba(18, 30, 42, 0.82);
}

[data-theme="dark"] .campus-topbar__icon-button {
  border-color: rgba(74, 96, 120, 0.78);
  background: rgba(32, 48, 68, 0.82);
}
</style>
