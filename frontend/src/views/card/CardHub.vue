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
      <!-- Editorial header directly on paper canvas -->
      <div class="pb-5 border-b border-[var(--c-border)]">
        <span class="inline-flex items-center h-6 px-2.5 rounded-full bg-[var(--c-primary-soft)] text-[var(--c-primary)] text-xs font-semibold">{{ t('card.title') }}</span>
        <h1 class="mt-3 text-3xl font-bold editorial-serif text-[var(--c-text-1)]">{{ t('card.title') }}</h1>
        <p class="mt-2 text-sm leading-relaxed text-[var(--c-text-2)]">{{ t('card.description') }}</p>
      </div>

      <!-- Hairlined action grid -->
      <div class="grid grid-cols-1 sm:grid-cols-2 gap-3.5 mt-5">
        <button
          v-for="item in actions"
          :key="item.id"
          type="button"
          class="ui-panel flex flex-col items-start p-5 bg-[var(--c-surface)] border border-[var(--c-border)] text-left hover:bg-[var(--c-surface-hover)] transition-colors"
          @click="router.push(item.path)"
        >
          <span class="editorial-serif text-xl font-bold text-[var(--c-text-1)]">{{ item.title }}</span>
          <span class="mt-2 text-sm leading-relaxed text-[var(--c-text-2)]">{{ item.description }}</span>
        </button>
      </div>
    </div>
  </div>
</template>
