<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { Moon, Sun, Menu } from 'lucide-vue-next'

defineProps({ sidebarOpen: Boolean })
const route = useRoute()
const emit = defineEmits(['open-command-palette', 'toggle-sidebar'])

const pageTitle = computed(() => {
  return route.meta?.title || route.name?.toString() || ''
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
  <header
    class="sticky top-0 z-40 flex items-center justify-between h-[52px] px-7 bg-[var(--c-bg)]/85 backdrop-blur-xl border-b border-[var(--c-border)]"
  >
    <!-- Left: hamburger (mobile) + Page title -->
    <div class="flex items-center gap-3">
      <button
        class="md:hidden w-[34px] h-[34px] rounded-lg border border-[var(--c-border)] bg-[var(--c-surface)] flex items-center justify-center text-[var(--c-text-2)]"
        @click="emit('toggle-sidebar')"
      >
        <Menu class="w-4 h-4" />
      </button>
      <span class="font-bold text-sm">{{ pageTitle }}</span>
    </div>

    <!-- Right: Actions -->
    <div class="flex items-center gap-2">
      <!-- Theme toggle -->
      <button
        class="w-[34px] h-[34px] rounded-lg border border-[var(--c-border)] bg-[var(--c-surface)] flex items-center justify-center text-[var(--c-text-2)] hover:bg-[var(--c-surface-hover)]"
        @click="toggleTheme"
      >
        <Moon v-if="!isDark" class="w-4 h-4" />
        <Sun v-else class="w-4 h-4" />
      </button>
    </div>
  </header>
</template>
