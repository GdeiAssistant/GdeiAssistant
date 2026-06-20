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
  <div class="app-empty">
    <div class="app-empty__icon">
      <slot name="icon" />
    </div>
    <p class="app-empty__title">{{ props.title || t('common.noData') }}</p>
    <p v-if="description" class="app-empty__description">
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
  background: linear-gradient(135deg, rgba(20, 185, 133, 0.12), rgba(59, 130, 246, 0.1));
  color: var(--c-primary);
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
</style>
