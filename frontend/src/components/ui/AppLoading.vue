<script setup>
import { useI18n } from 'vue-i18n'
import AppSkeleton from './AppSkeleton.vue'

defineProps({
  label: {
    type: String,
    default: '',
  },
  lines: {
    type: Number,
    default: 3,
  },
})

const { t } = useI18n()
// Bar widths step down so the block reads as a short paragraph, not a form field.
const widths = ['w-full', 'w-5/6', 'w-3/5']
</script>

<template>
  <div role="status" aria-live="polite" class="w-full">
    <span class="sr-only">{{ label || t('common.loading') }}</span>
    <div aria-hidden="true" class="mx-auto flex w-full max-w-[240px] flex-col gap-3">
      <AppSkeleton
        v-for="n in lines"
        :key="n"
        class="h-3"
        :class="widths[(n - 1) % widths.length]"
      />
    </div>
  </div>
</template>
