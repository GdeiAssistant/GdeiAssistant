<script setup>
import AppButton from '@/components/ui/AppButton.vue'
import { computed } from 'vue'
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
  accent: {
    type: String,
    default: 'var(--c-primary)',
  },
  actionVariant: {
    type: String,
    default: 'secondary',
    validator: (value) => ['primary', 'secondary'].includes(value),
  },
})

const emit = defineEmits(['action'])
const { t } = useI18n()

const actionStyle = computed(() => {
  if (props.actionVariant !== 'primary') return null
  return {
    '--c-primary': 'var(--empty-accent)',
    '--c-primary-hover': 'color-mix(in srgb, var(--empty-accent) 82%, black)',
  }
})
</script>

<template>
  <div class="app-empty" :style="{ '--empty-accent': props.accent }">
    <div class="app-empty__icon">
      <slot name="icon" />
    </div>
    <p class="app-empty__title">{{ props.title || t('common.noData') }}</p>
    <p v-if="description" class="app-empty__description">
      {{ description }}
    </p>
    <AppButton
      v-if="actionText"
      :variant="props.actionVariant"
      class="app-empty__action mt-4"
      :style="actionStyle"
      @click="emit('action')"
    >
      {{ actionText }}
    </AppButton>
  </div>
</template>

<style scoped>
.app-empty {
  display: flex;
  min-height: 220px;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 48px 24px;
  text-align: center;
}

.app-empty__icon {
  display: grid;
  width: 68px;
  height: 68px;
  margin-bottom: 16px;
  place-items: center;
  border-radius: 24px;
  background: linear-gradient(135deg, color-mix(in srgb, var(--empty-accent) 14%, transparent), color-mix(in srgb, var(--empty-accent) 8%, rgba(59, 130, 246, 0.12)));
  color: var(--empty-accent);
  font-size: 34px;
}

.app-empty__title {
  margin: 0;
  color: var(--c-text-1);
  font-size: 15px;
  font-weight: 820;
}

.app-empty__description {
  max-width: 360px;
  margin: 6px 0 0;
  color: var(--c-text-2);
  font-size: 13px;
  line-height: 1.6;
}

.app-empty__action {
  box-shadow: 0 14px 26px color-mix(in srgb, var(--empty-accent) 24%, transparent);
}
</style>
