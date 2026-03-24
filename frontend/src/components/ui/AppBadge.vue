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
  'inline-flex items-center px-2 py-0.5 rounded-full text-[11px] font-medium',
  {
    variants: {
      variant: {
        default: 'bg-[var(--c-border-light)] text-[var(--c-text-2)]',
        success: 'bg-[var(--c-primary-50)] text-[var(--c-primary)]',
        warning: 'bg-amber-50 text-amber-600',
        danger: 'bg-red-50 text-red-600',
        info: 'bg-blue-50 text-blue-600',
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
    backgroundColor: `color-mix(in srgb, ${props.color} 10%, transparent)`,
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
