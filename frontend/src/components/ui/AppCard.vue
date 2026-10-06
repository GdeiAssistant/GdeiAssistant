<script setup>
import { useSlots } from 'vue'

const slots = useSlots()
</script>

<template>
  <section class="app-card">
    <header v-if="slots.header" class="app-card__header">
      <slot name="header" />
    </header>
    <slot />
  </section>
</template>

<style scoped>
.app-card {
  overflow: hidden;
  border: 1px solid var(--c-border);
  border-radius: var(--radius-card);
  background: var(--c-surface);
  box-shadow: var(--shadow-sm);
}

.app-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  min-height: 52px;
  padding: 12px 20px;
  border-bottom: 1px solid var(--c-divider);
}
</style>
