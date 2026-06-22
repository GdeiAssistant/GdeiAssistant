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
      <DialogOverlay class="dialog-overlay fixed inset-0 z-[300] bg-slate-950/45 backdrop-blur-[3px]" />
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
  width: 390px;
  max-width: 90vw;
  transform: translate(-50%, -50%);
  border: 1px solid rgba(205, 222, 226, 0.82);
  border-radius: 26px;
  background: rgba(255, 255, 255, 0.94);
  box-shadow: 0 28px 80px rgba(15, 39, 49, 0.18);
  padding: 24px;
  outline: none;
  backdrop-filter: blur(18px);
}

.dialog-title {
  margin: 0 0 8px;
  color: var(--c-text-1);
  font-size: 19px;
  font-weight: 900;
  letter-spacing: -0.02em;
}

.dialog-description,
.dialog-body {
  margin-bottom: 22px;
  color: var(--c-text-2);
  font-size: 14px;
  line-height: 1.7;
}

.dialog-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.dialog-actions--single .dialog-button {
  min-width: 120px;
}

.dialog-button {
  min-height: 42px;
  border-radius: 14px;
  cursor: pointer;
  font: inherit;
  font-size: 14px;
  font-weight: 820;
  padding: 0 18px;
}

.dialog-button--secondary {
  border: 1px solid var(--c-border);
  background: rgba(255, 255, 255, 0.75);
  color: var(--c-text-2);
}

.dialog-button--primary {
  border: 0;
  background: var(--c-primary);
  color: #fff;
  box-shadow: 0 12px 24px color-mix(in srgb, var(--c-primary) 18%, transparent);
}

.dialog-button--danger {
  border: 0;
  background: linear-gradient(
    135deg,
    color-mix(in srgb, var(--c-danger) 92%, #ef4444),
    color-mix(in srgb, var(--c-danger) 76%, #991b1b)
  );
  color: #fff;
  box-shadow: 0 12px 24px color-mix(in srgb, var(--c-danger) 18%, transparent);
}

.dialog-overlay {
  animation: overlay-in 0.2s ease-out;
}

.dialog-content {
  animation: content-in 0.2s ease-out;
}

@keyframes overlay-in {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes content-in {
  from {
    opacity: 0;
    transform: translate(-50%, -50%) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translate(-50%, -50%) scale(1);
  }
}

[data-theme="dark"] .dialog-content {
  border-color: rgba(45, 58, 73, 0.9);
  background: rgba(20, 27, 37, 0.94);
  box-shadow: 0 28px 80px rgba(0, 0, 0, 0.46);
}

[data-theme="dark"] .dialog-button--secondary {
  background: rgba(31, 41, 55, 0.86);
}

[data-theme="dark"] .dialog-button--danger {
  background: linear-gradient(
    135deg,
    color-mix(in srgb, var(--c-danger) 88%, rgba(24, 38, 53, 0.12)),
    color-mix(in srgb, var(--c-danger) 68%, rgba(24, 38, 53, 0.3))
  );
  box-shadow: 0 14px 28px color-mix(in srgb, var(--c-danger) 22%, transparent);
}
</style>
