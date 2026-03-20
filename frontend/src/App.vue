<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

const COMMUNITY_PREFIXES = ['/ershou', '/lostandfound', '/secret', '/express', '/topic', '/delivery', '/dating', '/photograph']

const transitionName = computed(() => {
  const p = route.path
  return COMMUNITY_PREFIXES.some(prefix => p === prefix || p.startsWith(prefix + '/'))
    ? 'community-fade'
    : ''
})
</script>

<template>
  <router-view v-slot="{ Component }">
    <transition :name="transitionName" mode="out-in">
      <component :is="Component" />
    </transition>
  </router-view>
</template>

<style scoped>
.community-fade-enter-active {
  animation: community-page-in 0.25s ease;
}
.community-fade-leave-active {
  animation: community-page-out 0.15s ease;
}
@keyframes community-page-in {
  from {
    opacity: 0;
    transform: translateY(8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
@keyframes community-page-out {
  from { opacity: 1; }
  to { opacity: 0; }
}
</style>
