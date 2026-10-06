<script setup>
import { ChevronLeft } from 'lucide-vue-next'
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'

const router = useRouter()
const { t } = useI18n()

const actions = computed(() => [
  {
    id: 'search',
    title: t('libraryPage.home.searchTitle'),
    description: t('libraryPage.home.searchDescription'),
    path: '/library/search'
  },
  {
    id: 'borrow',
    title: t('libraryPage.home.borrowTitle'),
    description: t('libraryPage.home.borrowDescription'),
    path: '/library/borrow'
  }
])

function goBack() {
  router.back()
}

function openAction(path) {
  if (path) {
    router.push(path)
  }
}
</script>

<template>
  <div class="subpage min-h-screen bg-[var(--c-bg)]">
    <div class="subpage-bar">
      <button type="button" class="subpage-bar__back" @click="goBack">
        <ChevronLeft :size="18" aria-hidden="true" />
        <span>{{ t('common.back') }}</span>
      </button>
      <span class="subpage-bar__title">{{ t('libraryPage.title') }}</span>
      <span aria-hidden="true"></span>
    </div>

    <div class="subpage-body max-w-lg mx-auto px-4 py-6">
      <!-- Hero card -->
      <div class="rounded-2xl bg-[var(--c-surface)] p-6 border border-[var(--c-border)]">
        <h1 class="text-3xl font-bold text-[var(--c-text)]">{{ t('libraryPage.heroTitle') }}</h1>
        <p class="mt-2 text-sm leading-relaxed text-[var(--c-text-2)]">{{ t('libraryPage.heroDescription') }}</p>
      </div>

      <!-- Action grid -->
      <div class="grid grid-cols-1 sm:grid-cols-2 gap-3.5 mt-5">
        <button
          v-for="item in actions"
          :key="item.id"
          type="button"
          class="ui-panel flex flex-col items-start p-5 rounded-2xl bg-[var(--c-surface)] border border-[var(--c-border)] shadow-sm text-left hover:bg-[var(--c-surface-hover)] transition-colors"
          @click="openAction(item.path)"
        >
          <span class="text-xl font-bold text-[var(--c-text)]">{{ item.title }}</span>
          <span class="mt-2.5 text-sm leading-relaxed text-[var(--c-text-2)]">{{ item.description }}</span>
        </button>
      </div>
    </div>
  </div>
</template>
