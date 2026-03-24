<script setup>
defineOptions({
  inheritAttrs: true,
})
</script>

<template>
  <div
    class="rounded-lg bg-[var(--c-border-light)] animate-pulse"
    v-bind="$attrs"
  />
</template>
