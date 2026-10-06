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
  'inline-flex items-center justify-center gap-2 rounded-[var(--radius-control)] font-semibold transition-colors focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-[var(--c-primary)] disabled:opacity-50 disabled:pointer-events-none',
  {
    variants: {
      variant: {
        primary: 'bg-[var(--c-primary)] text-[var(--c-on-primary)] hover:bg-[var(--c-primary-hover)]',
        secondary:
          'bg-[var(--c-surface)] text-[var(--c-text-1)] border border-[var(--c-border)] hover:border-[color-mix(in_srgb,var(--c-primary)_45%,var(--c-border))] hover:text-[var(--c-primary)]',
        destructive: 'bg-[var(--c-danger)] text-[var(--c-on-primary)] hover:bg-[color-mix(in_srgb,var(--c-danger)_88%,var(--c-text-1))]',
      },
      size: {
        sm: 'text-[13px] min-h-9 px-3.5',
        md: 'text-sm min-h-11 px-5',
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
      class="h-4 w-4 animate-spin"
    />
    <slot />
  </button>
</template>
