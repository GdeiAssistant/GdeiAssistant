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
  z-index: 200;
  display: flex;
  pointer-events: none;
  flex-direction: column;
  gap: 10px;
}

.toast-item {
  display: flex;
  min-height: 44px;
  align-items: center;
  gap: 9px;
  border: 1px solid rgba(255, 255, 255, 0.18);
  border-radius: 16px;
  background: rgba(16, 32, 51, 0.92);
  box-shadow: 0 18px 38px rgba(15, 39, 49, 0.18);
  color: #fff;
  font-size: 14px;
  font-weight: 760;
  padding: 0 16px;
  pointer-events: auto;
  backdrop-filter: blur(16px);
}

.toast-icon--success {
  color: color-mix(in srgb, var(--c-primary) 78%, #67e8f9);
}

.toast-icon--error {
  color: color-mix(in srgb, var(--c-danger, #ef4444) 82%, #fda4af);
}

.toast-icon--loading {
  color: color-mix(in srgb, var(--c-primary) 72%, #e0f2fe);
}

.toast-enter-active { animation: toast-in 0.25s ease-out; }
.toast-leave-active { animation: toast-out 0.2s ease-in forwards; }
.toast-enter-from,
.toast-leave-to {
  opacity: 0;
  transform: translateY(8px);
}

@keyframes toast-in {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes toast-out {
  from { opacity: 1; transform: translateY(0); }
  to { opacity: 0; transform: translateY(8px); }
}

@media (max-width: 767px) {
  .toast-stack {
    right: 14px;
    bottom: 92px;
    left: 14px;
  }

  .toast-item {
    justify-content: center;
  }
}
</style>
