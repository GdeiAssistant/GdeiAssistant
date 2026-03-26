<script setup>
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'

const router = useRouter()
const { t } = useI18n()
</script>

<template>
  <div class="min-h-screen bg-[var(--c-bg)]">
    <div class="sticky top-0 z-30 flex items-center h-[52px] px-5 bg-[var(--c-surface)]/90 backdrop-blur-xl border-b border-[var(--c-border)]">
      <button @click="router.back()" class="text-[var(--c-primary)] text-sm font-medium">&larr; {{ t('about.back') }}</button>
      <span class="flex-1 text-center text-sm font-bold">{{ t('feature.data.name') }}</span>
      <div class="w-10"></div>
    </div>

    <div class="max-w-lg mx-auto px-4 py-6">
      <div class="bg-[var(--c-surface)] rounded-xl border border-[var(--c-border)] divide-y divide-[var(--c-border)]">
        <button
          type="button"
          class="w-full flex items-center justify-between px-4 py-3.5 text-sm hover:bg-[var(--c-surface-hover)] transition-colors text-[var(--c-text)]"
          @click="router.push('/data/electricity')"
        >
          <span>{{ t('electricityFees.title') }}</span>
          <span class="text-[var(--c-text-3)]">›</span>
        </button>
        <button
          type="button"
          class="w-full flex items-center justify-between px-4 py-3.5 text-sm hover:bg-[var(--c-surface-hover)] transition-colors text-[var(--c-text)]"
          @click="router.push('/data/yellowpage')"
        >
          <span>{{ t('yellowPage.title') }}</span>
          <span class="text-[var(--c-text-3)]">›</span>
        </button>
      </div>
    </div>
  </div>
</template>
