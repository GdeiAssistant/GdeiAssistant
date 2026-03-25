<script setup>
import { computed, ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ChevronRight } from 'lucide-vue-next'
import { getCurrentUserProfile } from '@/api/user'
import { createFooterItems, createNavItems } from './navigation'

const router = useRouter()
const route = useRoute()
const { t } = useI18n()

const profile = ref(null)

onMounted(async () => {
  try {
    const res = await getCurrentUserProfile()
    profile.value = res?.data || null
  } catch (_) {}
})

const navItems = computed(() => createNavItems(t))
const footerItems = computed(() => createFooterItems(t))

function isActive(path) {
  return route.path.startsWith(path)
}

function navigate(path) {
  router.push(path)
}

function avatarInitial() {
  const name = profile.value?.nickname || profile.value?.username || t('sidebar.notLoggedIn')
  return name.charAt(0)
}
</script>

<template>
  <aside class="fixed left-0 top-0 h-full w-[200px] bg-[var(--c-surface)] border-r border-[var(--c-border)] flex flex-col z-40">
    <!-- Brand -->
    <div class="flex items-center gap-2.5 px-4 py-5">
      <div class="w-8 h-8 rounded-lg bg-[var(--c-primary)] flex items-center justify-center text-white font-bold text-sm shrink-0">
        G
      </div>
      <span class="text-base font-semibold text-[var(--c-text-1)] truncate">{{ $t('about.appName') }}</span>
    </div>

    <!-- Primary nav -->
    <nav class="flex-1 px-3 pb-4">
      <ul class="space-y-0.5">
        <li v-for="item in navItems" :key="item.path">
          <button
            class="flex items-center gap-2.5 w-full rounded-lg px-2.5 py-2 text-sm font-medium transition-colors"
            :class="isActive(item.path)
              ? 'bg-[var(--c-primary-50)] text-[var(--c-primary)] font-semibold'
              : 'text-[var(--c-text-2)] hover:bg-[var(--c-surface-hover)]'"
            @click="navigate(item.path)"
          >
            <component :is="item.icon" class="w-4 h-4 shrink-0" />
            <span class="flex-1 text-left">{{ item.label }}</span>
            <span
              v-if="item.path === '/info'"
              class="bg-red-500 text-white text-[10px] px-1.5 rounded-full leading-4"
            >3</span>
          </button>
        </li>
      </ul>
    </nav>

    <!-- Footer -->
    <div class="border-t border-[var(--c-border)] px-3 py-3 space-y-0.5">
      <li v-for="item in footerItems" :key="item.path" class="list-none">
        <button
          class="flex items-center gap-2.5 w-full rounded-lg px-2.5 py-2 text-sm font-medium text-[var(--c-text-2)] hover:bg-[var(--c-surface-hover)] transition-colors"
          @click="navigate(item.path)"
        >
          <component :is="item.icon" class="w-4 h-4 shrink-0" />
          <span>{{ item.label }}</span>
        </button>
      </li>

      <!-- User card -->
      <div
        class="flex items-center gap-2.5 mt-2 px-2.5 py-2 rounded-lg hover:bg-[var(--c-surface-hover)] cursor-pointer transition-colors"
        @click="navigate('/profile')"
      >
        <div class="w-8 h-8 rounded-full bg-[var(--c-primary)] flex items-center justify-center text-white text-xs font-semibold shrink-0">
          {{ avatarInitial() }}
        </div>
        <div class="flex-1 min-w-0">
          <p class="text-sm font-semibold text-[var(--c-text-1)] truncate">
            {{ profile?.nickname || profile?.username || $t('sidebar.notLoggedIn') }}
          </p>
        </div>
        <ChevronRight class="w-4 h-4 text-[var(--c-text-3)] shrink-0" />
      </div>
    </div>
  </aside>
</template>
