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
      <DialogOverlay class="dialog-overlay fixed inset-0 z-[300] bg-black/40" />
      <DialogContent
        class="dialog-content fixed left-1/2 top-1/2 z-[301] w-[380px] max-w-[90vw] -translate-x-1/2 -translate-y-1/2 rounded-[20px] bg-[var(--c-surface)] p-6 shadow-lg focus:outline-none"
      >
        <DialogTitle v-if="title" class="text-lg font-bold mb-2">
          {{ title }}
        </DialogTitle>

        <DialogDescription
          v-if="description && !$slots.default"
          class="text-sm text-[var(--c-text-2)] mb-6"
        >
          {{ description }}
        </DialogDescription>

        <div v-if="$slots.default" class="mb-6">
          <slot />
        </div>

        <div class="flex justify-end gap-2">
          <DialogClose as-child>
            <button
              class="px-4 py-2 rounded-lg text-sm font-medium bg-[var(--c-fill-2)] text-[var(--c-text-1)] hover:bg-[var(--c-fill-3)] transition cursor-pointer"
              @click="emit('close')"
            >
              {{ t('common.cancel') }}
            </button>
          </DialogClose>
          <button
            class="px-4 py-2 rounded-lg text-sm font-medium bg-[var(--c-primary)] text-white hover:opacity-90 transition cursor-pointer"
            @click="emit('confirm')"
          >
            {{ t('common.confirm') }}
          </button>
        </div>
      </DialogContent>
    </DialogPortal>
  </DialogRoot>
</template>

<style scoped>
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
</style>
