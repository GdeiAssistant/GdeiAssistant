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
  border: 1px solid rgba(205, 222, 226, 0.78);
  border-radius: 24px;
  background:
    linear-gradient(135deg, rgba(255, 255, 255, 0.92), rgba(250, 255, 253, 0.84)),
    radial-gradient(circle at 100% 0, rgba(155, 216, 255, 0.12), transparent 32%);
  box-shadow: 0 16px 38px rgba(32, 69, 78, 0.07);
  backdrop-filter: blur(14px);
  transition: border-color 0.18s ease, box-shadow 0.18s ease, transform 0.18s ease;
}

.app-card:hover {
  border-color: rgba(32, 170, 130, 0.18);
  box-shadow: 0 22px 50px rgba(32, 69, 78, 0.1);
}

.app-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  min-height: 54px;
  padding: 14px 18px;
  border-bottom: 1px solid rgba(220, 232, 236, 0.72);
  background: rgba(255, 255, 255, 0.42);
}

[data-theme="dark"] .app-card {
  border-color: rgba(45, 58, 73, 0.86);
  background: rgba(20, 27, 37, 0.78);
  box-shadow: 0 16px 38px rgba(0, 0, 0, 0.28);
}

[data-theme="dark"] .app-card__header {
  border-bottom-color: rgba(45, 58, 73, 0.86);
  background: rgba(31, 41, 55, 0.34);
}
</style>
