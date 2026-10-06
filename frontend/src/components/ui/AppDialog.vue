<script setup>
import {
  DialogRoot,
  DialogOverlay,
  DialogContent,
  DialogTitle,
  DialogDescription,
  DialogClose,
  DialogPortal,
} from 'radix-vue'
import { useI18n } from 'vue-i18n'

const props = defineProps({
  open: { type: Boolean, default: false },
  title: { type: String, default: '' },
  description: { type: String, default: '' },
  confirmText: { type: String, default: '' },
  cancelText: { type: String, default: '' },
  showCancel: { type: Boolean, default: true },
  showActions: { type: Boolean, default: true },
  confirmTone: {
    type: String,
    default: 'primary',
    validator: (value) => ['primary', 'danger'].includes(value),
  },
})

const emit = defineEmits(['close', 'confirm'])
const { t } = useI18n()

function onOpenChange(val) {
  if (!val) emit('close')
}
</script>

<template>
  <DialogRoot :open="open" @update:open="onOpenChange">
    <DialogPortal>
      <DialogOverlay class="dialog-overlay fixed inset-0 z-[300] bg-[rgb(10_20_17/48%)]" />
      <DialogContent class="dialog-content">
        <DialogTitle v-if="title" class="dialog-title">
          {{ title }}
        </DialogTitle>

        <DialogDescription
          v-if="description && !$slots.default"
          class="dialog-description"
        >
          {{ description }}
        </DialogDescription>

        <DialogDescription
          v-else-if="$slots.default"
          class="dialog-description sr-only"
        >
          {{ title || t('common.confirm') }}
        </DialogDescription>

        <div v-if="$slots.default" class="dialog-body">
          <slot />
        </div>

        <div
          v-if="showActions"
          class="dialog-actions"
          :class="{ 'dialog-actions--single': !showCancel }"
        >
          <DialogClose v-if="showCancel" as-child>
            <button class="dialog-button dialog-button--secondary" @click="emit('close')">
              {{ cancelText || t('common.cancel') }}
            </button>
          </DialogClose>
          <button
            class="dialog-button"
            :class="confirmTone === 'danger' ? 'dialog-button--danger' : 'dialog-button--primary'"
            @click="emit('confirm')"
          >
            {{ confirmText || t('common.confirm') }}
          </button>
        </div>
      </DialogContent>
    </DialogPortal>
  </DialogRoot>
</template>

<style scoped>
.dialog-content {
  position: fixed;
  left: 50%;
  top: 50%;
  z-index: 301;
  width: 400px;
  max-width: calc(100vw - 32px);
  max-height: calc(100dvh - 48px);
  overflow-y: auto;
  transform: translate(-50%, -50%);
  border: 1px solid var(--c-border);
  border-radius: var(--radius-card);
  background: var(--c-surface);
  box-shadow: var(--shadow-lg);
  padding: 24px;
  outline: none;
}

.dialog-title {
  margin: 0 0 8px;
  color: var(--c-text-1);
  font-size: 17px;
  font-weight: 650;
}

.dialog-description,
.dialog-body {
  margin-bottom: 24px;
  color: var(--c-text-2);
  font-size: 14px;
  line-height: 1.7;
}

.dialog-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.dialog-actions--single .dialog-button {
  min-width: 120px;
}

.dialog-button {
  min-height: 42px;
  border-radius: var(--radius-control);
  cursor: pointer;
  font: inherit;
  font-size: 14px;
  font-weight: 600;
  padding: 0 18px;
  transition: background-color 0.15s ease, border-color 0.15s ease;
}

.dialog-button--secondary {
  border: 1px solid var(--c-border);
  background: var(--c-surface);
  color: var(--c-text-1);
}

.dialog-button--secondary:hover {
  background: var(--c-surface-hover);
}

.dialog-button--primary {
  border: 0;
  background: var(--c-primary);
  color: var(--c-on-primary);
}

.dialog-button--primary:hover {
  background: var(--c-primary-hover);
}

.dialog-button--danger {
  border: 0;
  background: var(--c-danger);
  color: var(--c-on-primary);
}

.dialog-overlay {
  animation: overlay-in 0.18s ease-out;
}

.dialog-content {
  animation: content-in 0.18s ease-out;
}

@keyframes overlay-in {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes content-in {
  from {
    opacity: 0;
    transform: translate(-50%, -48%);
  }
  to {
    opacity: 1;
    transform: translate(-50%, -50%);
  }
}

@media (max-width: 480px) {
  .dialog-actions {
    flex-direction: column-reverse;
  }

  .dialog-button {
    width: 100%;
  }
}
</style>
