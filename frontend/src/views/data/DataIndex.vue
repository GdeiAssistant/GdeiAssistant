<script setup>
import { ChevronLeft } from 'lucide-vue-next'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'

const router = useRouter()
const { t } = useI18n()
</script>

<template>
  <div class="subpage min-h-screen bg-[var(--c-bg)]">
    <div class="subpage-bar">
      <button type="button" class="subpage-bar__back" @click="router.back()">
        <ChevronLeft :size="18" aria-hidden="true" />
        <span>{{ t('about.back') }}</span>
      </button>
      <span class="subpage-bar__title">{{ t('feature.data.name') }}</span>
      <span aria-hidden="true"></span>
    </div>

    <div class="subpage-body max-w-lg mx-auto px-4 py-6">
      <div class="ui-panel bg-[var(--c-surface)] rounded-xl border border-[var(--c-border)] divide-y divide-[var(--c-border)]">
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
