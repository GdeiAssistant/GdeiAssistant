<script setup>
import { useSlots } from 'vue'

const slots = useSlots()
</script>

<template>
  <div
    class="bg-[var(--c-surface)] border border-[var(--c-border)] rounded-[14px] overflow-hidden transition hover:shadow-md hover:border-transparent"
  >
    <div
      v-if="slots.header"
      class="px-[18px] py-3 border-b border-[var(--c-border-light)] flex items-center justify-between"
    >
      <slot name="header" />
    </div>
    <slot />
  </div>
</template>
