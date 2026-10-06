<script setup>
import { computed } from 'vue'
import { cva } from 'class-variance-authority'
import { cn } from '@/lib/utils'

const props = defineProps({
  variant: {
    type: String,
    default: 'default',
    validator: (v) =>
      ['default', 'success', 'warning', 'danger', 'info', 'module'].includes(v),
  },
  color: {
    type: String,
    default: '',
  },
})

const badgeVariants = cva(
  'inline-flex items-center px-2 py-0.5 rounded-full text-[11px] font-semibold leading-4',
  {
    variants: {
      variant: {
        default: 'bg-[var(--c-fill-2)] text-[var(--c-text-2)]',
        success: 'bg-[var(--c-primary-soft)] text-[var(--c-primary)]',
        warning: 'bg-[color-mix(in_srgb,var(--c-warning)_14%,var(--c-surface))] text-[var(--c-warning)]',
        danger: 'bg-[color-mix(in_srgb,var(--c-danger)_12%,var(--c-surface))] text-[var(--c-danger)]',
        info: 'bg-[var(--c-fill-2)] text-[var(--c-text-1)]',
        module: '',
      },
    },
    defaultVariants: {
      variant: 'default',
    },
  },
)

const classes = computed(() =>
  cn(badgeVariants({ variant: props.variant })),
)

const moduleStyle = computed(() => {
  if (props.variant !== 'module' || !props.color) return {}
  return {
    backgroundColor: `color-mix(in srgb, ${props.color} 14%, var(--c-surface))`,
    color: props.color,
  }
})
</script>

<template>
  <span
    :class="classes"
    :style="moduleStyle"
  >
    <slot />
  </span>
</template>
