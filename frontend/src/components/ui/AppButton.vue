<script setup>
import { computed } from 'vue'
import { cva } from 'class-variance-authority'
import { cn } from '@/lib/utils'
import { Loader2 } from 'lucide-vue-next'

const props = defineProps({
  variant: {
    type: String,
    default: 'primary',
    validator: (v) => ['primary', 'secondary', 'destructive'].includes(v),
  },
  size: {
    type: String,
    default: 'md',
    validator: (v) => ['sm', 'md'].includes(v),
  },
  loading: {
    type: Boolean,
    default: false,
  },
  disabled: {
    type: Boolean,
    default: false,
  },
})

const buttonVariants = cva(
  'inline-flex items-center justify-center rounded-[15px] font-extrabold tracking-[-0.01em] transition-all focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[var(--c-primary)]/30 disabled:opacity-50 disabled:pointer-events-none',
  {
    variants: {
      variant: {
        primary: 'bg-[var(--c-primary)] text-white shadow-[0_14px_26px_rgba(16,185,129,0.22)] hover:bg-[var(--c-primary-hover)] hover:-translate-y-0.5',
        secondary:
          'bg-white/75 text-[var(--c-text-1)] border border-[var(--c-border)] hover:border-[var(--c-primary)]/30 hover:bg-white hover:-translate-y-0.5',
        destructive: 'bg-[var(--c-danger)] text-white shadow-[0_14px_26px_rgba(239,68,68,0.18)] hover:opacity-90 hover:-translate-y-0.5',
      },
      size: {
        sm: 'text-xs px-3.5 py-2',
        md: 'text-sm px-5 py-3',
      },
    },
    defaultVariants: {
      variant: 'primary',
      size: 'md',
    },
  },
)

const classes = computed(() =>
  cn(buttonVariants({ variant: props.variant, size: props.size })),
)
</script>

<template>
  <button
    :class="classes"
    :disabled="disabled || loading"
  >
    <Loader2
      v-if="loading"
      class="mr-2 h-4 w-4 animate-spin"
    />
    <slot />
  </button>
</template>
