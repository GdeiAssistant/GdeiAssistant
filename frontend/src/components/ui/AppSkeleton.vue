<script setup>
defineOptions({
  inheritAttrs: true,
})
</script>

<template>
  <div
    class="rounded-[8px] bg-[var(--c-fill-2)] animate-pulse"
    v-bind="$attrs"
  />
</template>
