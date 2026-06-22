<script setup>
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'

const router = useRouter()
const { t } = useI18n()
</script>

<template>
  <div class="min-h-screen bg-gray-50">
    <!-- Sticky Header -->
    <div class="sticky top-0 z-10 flex items-center h-12 bg-white border-b border-gray-200 px-4">
      <button type="button" class="w-15 text-sm text-gray-700 text-left cursor-pointer" @click="router.back()">{{ t('common.back') }}</button>
      <h1 class="flex-1 text-center text-base font-medium text-gray-700 m-0">{{ t('profile.changePassword') }}</h1>
      <div class="w-15"></div>
    </div>

    <!-- Content -->
    <div class="max-w-lg mx-auto px-4 py-16 text-center">
      <div class="bg-white rounded-xl shadow-sm p-8">
        <div class="password-page-info text-5xl mb-4">i</div>
        <h2 class="text-lg font-medium text-gray-800 mb-2">{{ t('profile.changePassword') }}</h2>
        <p class="text-sm text-gray-500 mb-8 leading-relaxed">
          {{ t('profile.passwordNotAvailable') }}
        </p>
        <button
          type="button"
          class="password-page-action w-full max-w-xs mx-auto block rounded-lg text-white font-medium py-2.5 px-6 cursor-pointer"
          @click="router.back()"
        >
          {{ t('common.back') }}
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.password-page-info {
  color: color-mix(in srgb, var(--c-primary) 62%, #67e8f9);
}

.password-page-action {
  background: linear-gradient(135deg, color-mix(in srgb, var(--c-primary) 88%, #2dd4bf), color-mix(in srgb, var(--c-primary) 72%, #0f766e));
  box-shadow: 0 12px 28px color-mix(in srgb, var(--c-primary) 22%, transparent);
}

[data-theme="dark"] .password-page-info {
  color: color-mix(in srgb, var(--c-primary) 58%, #67e8f9);
}

[data-theme="dark"] .password-page-action {
  background: linear-gradient(135deg, color-mix(in srgb, var(--c-primary) 68%, #22d3ee), color-mix(in srgb, var(--c-primary) 54%, #0f766e));
}
</style>
