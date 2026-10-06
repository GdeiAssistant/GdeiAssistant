<script setup>
import { ChevronLeft } from 'lucide-vue-next'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { createCardActions } from './cardContent'

const router = useRouter()
const { t } = useI18n()
const actions = createCardActions(t)
</script>

<template>
  <div class="subpage min-h-screen bg-[var(--c-bg)]">
    <div class="subpage-bar">
      <button type="button" class="subpage-bar__back" @click="$router.back()">
        <ChevronLeft :size="18" aria-hidden="true" />
        <span>{{ t('common.back') }}</span>
      </button>
      <span class="subpage-bar__title">{{ t('card.title') }}</span>
      <span aria-hidden="true"></span>
    </div>

    <div class="subpage-body max-w-lg mx-auto px-4 py-6">
      <!-- Hero card -->
      <div class="rounded-2xl bg-[var(--c-surface)] p-6 border border-[var(--c-border)]">
        <span class="inline-flex items-center h-7 px-3 rounded-full bg-[var(--c-primary)]/10 text-[var(--c-primary)] text-xs font-semibold">{{ t('card.title') }}</span>
        <h1 class="mt-4 text-3xl font-bold text-[var(--c-text)]">{{ t('card.title') }}</h1>
        <p class="mt-2 text-sm leading-relaxed text-[var(--c-text-2)]">{{ t('card.description') }}</p>
      </div>

      <!-- Action grid -->
      <div class="grid grid-cols-1 sm:grid-cols-2 gap-3.5 mt-5">
        <button
          v-for="item in actions"
          :key="item.id"
          type="button"
          class="ui-panel flex flex-col items-start p-5 rounded-2xl bg-[var(--c-surface)] border border-[var(--c-border)] shadow-sm text-left hover:bg-[var(--c-surface-hover)] transition-colors"
          @click="router.push(item.path)"
        >
          <span class="text-xl font-bold text-[var(--c-text)]">{{ item.title }}</span>
          <span class="mt-2.5 text-sm leading-relaxed text-[var(--c-text-2)]">{{ item.description }}</span>
        </button>
      </div>
    </div>
  </div>
</template>
