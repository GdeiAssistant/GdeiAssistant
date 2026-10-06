<script setup>
import { useToast } from '@/composables/useToast'
import { Check, X, Loader2 } from 'lucide-vue-next'

const { toasts } = useToast()
</script>

<template>
  <Teleport to="body">
    <div class="toast-stack">
      <TransitionGroup
        enter-active-class="toast-enter-active"
        leave-active-class="toast-leave-active"
        enter-from-class="toast-enter-from"
        leave-to-class="toast-leave-to"
      >
        <div v-for="t in toasts" :key="t.id" class="toast-item" :class="`toast-item--${t.type}`">
          <Check v-if="t.type === 'success'" class="toast-icon toast-icon--success size-4 shrink-0" />
          <X v-else-if="t.type === 'error'" class="toast-icon toast-icon--error size-4 shrink-0" />
          <Loader2 v-else-if="t.type === 'loading'" class="toast-icon toast-icon--loading size-4 shrink-0 animate-spin" />
          <span>{{ t.message }}</span>
        </div>
      </TransitionGroup>
    </div>
  </Teleport>
</template>

<style scoped>
.toast-stack {
  position: fixed;
  right: 24px;
  bottom: 24px;
  z-index: 6000;
  display: flex;
  pointer-events: none;
  flex-direction: column;
  gap: 8px;
}

.toast-item {
  display: flex;
  min-height: 44px;
  max-width: 420px;
  align-items: center;
  gap: 10px;
  border: 1px solid var(--c-border);
  border-radius: var(--radius-control);
  background: var(--c-text-1);
  box-shadow: var(--shadow-md);
  color: var(--c-bg);
  font-size: 14px;
  font-weight: 500;
  line-height: 1.5;
  padding: 10px 16px;
  pointer-events: auto;
}

.toast-icon--success,
.toast-icon--loading {
  color: color-mix(in srgb, var(--c-primary) 70%, var(--c-bg));
}

.toast-icon--error {
  color: color-mix(in srgb, var(--c-danger) 70%, var(--c-bg));
}

.toast-enter-active { animation: toast-in 0.2s ease-out; }
.toast-leave-active { animation: toast-out 0.16s ease-in forwards; }
.toast-enter-from,
.toast-leave-to {
  opacity: 0;
  transform: translateY(6px);
}

@keyframes toast-in {
  from { opacity: 0; transform: translateY(6px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes toast-out {
  from { opacity: 1; transform: translateY(0); }
  to { opacity: 0; transform: translateY(6px); }
}

@media (max-width: 767px) {
  .toast-stack {
    right: 16px;
    bottom: calc(80px + env(safe-area-inset-bottom, 0px));
    left: 16px;
    align-items: center;
  }
}
</style>
