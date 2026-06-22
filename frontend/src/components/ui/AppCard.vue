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
  border-color: rgba(68, 89, 112, 0.74);
  background:
    radial-gradient(circle at 0 0, rgba(45, 212, 191, 0.07), transparent 26%),
    linear-gradient(180deg, rgba(23, 33, 46, 0.96), rgba(18, 27, 38, 0.98));
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.03),
    0 18px 42px rgba(0, 0, 0, 0.24);
}

[data-theme="dark"] .app-card__header {
  border-bottom-color: rgba(68, 89, 112, 0.58);
  background:
    linear-gradient(180deg, rgba(35, 46, 61, 0.68), rgba(30, 40, 53, 0.36));
}
</style>
