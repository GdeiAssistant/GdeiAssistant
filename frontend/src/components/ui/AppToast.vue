<script setup>
import { useToast } from '@/composables/useToast'
import { Check, X, Loader2 } from 'lucide-vue-next'

const { toasts } = useToast()
</script>

<template>
  <Teleport to="body">
    <div class="fixed bottom-6 right-8 z-[200] flex flex-col gap-2 pointer-events-none">
      <TransitionGroup
        enter-active-class="toast-enter-active"
        leave-active-class="toast-leave-active"
        enter-from-class="toast-enter-from"
        leave-to-class="toast-leave-to"
      >
        <div
          v-for="t in toasts"
          :key="t.id"
          class="pointer-events-auto flex items-center gap-2 px-4 py-2.5 rounded-xl text-sm font-medium shadow-lg bg-[var(--c-text-1)] text-white"
        >
          <Check
            v-if="t.type === 'success'"
            class="size-4 shrink-0 text-green-400"
          />
          <X
            v-else-if="t.type === 'error'"
            class="size-4 shrink-0 text-red-400"
          />
          <Loader2
            v-else-if="t.type === 'loading'"
            class="size-4 shrink-0 animate-spin"
          />
          <span>{{ t.message }}</span>
        </div>
      </TransitionGroup>
    </div>
  </Teleport>
</template>

<style scoped>
.toast-enter-active {
  animation: toast-in 0.25s ease-out;
}

.toast-leave-active {
  animation: toast-out 0.2s ease-in forwards;
}

.toast-enter-from {
  opacity: 0;
  transform: translateY(8px);
}

.toast-leave-to {
  opacity: 0;
  transform: translateY(8px);
}

@keyframes toast-in {
  from {
    opacity: 0;
    transform: translateY(8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes toast-out {
  from {
    opacity: 1;
    transform: translateY(0);
  }
  to {
    opacity: 0;
    transform: translateY(8px);
  }
}
</style>
