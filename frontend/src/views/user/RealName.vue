<script setup>
import { ChevronLeft } from 'lucide-vue-next'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'

const router = useRouter()
const { t } = useI18n()
</script>

<template>
  <div class="subpage min-h-screen bg-[var(--c-bg)]">
    <!-- Sticky Header -->
    <div class="subpage-bar">
      <button type="button" class="subpage-bar__back" @click="router.back()">
        <ChevronLeft :size="18" aria-hidden="true" />
        <span>{{ t('common.back') }}</span>
      </button>
      <h1 class="subpage-bar__title">{{ t('profile.realname') }}</h1>
      <span aria-hidden="true"></span>
    </div>

    <!-- Content -->
    <div class="max-w-lg mx-auto px-4 py-16 text-center">
      <div class="bg-[var(--c-surface)] rounded-xl shadow-sm p-8">
        <div class="realname-page-info text-5xl mb-4">i</div>
        <h2 class="text-lg font-medium text-[var(--c-text-1)] mb-2">{{ t('realname.unavailableTitle') }}</h2>
        <p class="text-sm text-[var(--c-text-2)] mb-8 leading-relaxed">
          {{ t('realname.unavailableDescription') }}
        </p>
        <button
          type="button"
          class="realname-page-action w-full max-w-xs mx-auto block rounded-lg text-white font-medium py-2.5 px-6 cursor-pointer"
          @click="router.back()"
        >
          {{ t('realname.back') }}
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.realname-page-info {
  color: var(--c-primary);
}

.realname-page-action {
  background: var(--c-primary);
  box-shadow: none;
}
</style>
