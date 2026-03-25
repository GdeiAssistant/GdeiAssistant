<script setup>
import AppButton from '@/components/ui/AppButton.vue'
import { useI18n } from 'vue-i18n'

const props = defineProps({
  icon: {
    type: String,
    default: '',
  },
  title: {
    type: String,
    default: '',
  },
  description: {
    type: String,
    default: '',
  },
  actionText: {
    type: String,
    default: '',
  },
})

const emit = defineEmits(['action'])
const { t } = useI18n()
</script>

<template>
  <div class="flex flex-col items-center justify-center py-16 px-6">
    <div class="text-5xl text-[var(--c-text-3)] opacity-50 mb-4">
      <slot name="icon" />
    </div>
    <p class="text-base font-medium text-[var(--c-text-2)]">{{ props.title || t('common.noData') }}</p>
    <p
      v-if="description"
      class="text-sm text-[var(--c-text-3)] mt-1"
    >
      {{ description }}
    </p>
    <AppButton
      v-if="actionText"
      variant="secondary"
      class="mt-4"
      @click="emit('action')"
    >
      {{ actionText }}
    </AppButton>
  </div>
</template>
