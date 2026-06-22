<script setup>
import { computed, defineAsyncComponent } from 'vue'
import { useRoute } from 'vue-router'
import AppToast from '@/components/ui/AppToast.vue'

const route = useRoute()
const AppSidebar = defineAsyncComponent(() => import('@/layout/AppSidebar.vue'))

const COMMUNITY_PREFIXES = ['/ershou', '/marketplace', '/lostandfound', '/secret', '/express', '/topic', '/delivery', '/dating', '/photograph']

const isCommunityRoute = computed(() => {
  const p = route.path
  return COMMUNITY_PREFIXES.some(prefix => p === prefix || p.startsWith(prefix + '/'))
})

const transitionName = computed(() => {
  return isCommunityRoute.value ? 'community-fade' : ''
})
</script>

<template>
  <router-view v-slot="{ Component }">
    <transition :name="transitionName" mode="out-in">
      <div :class="{ 'community-route-shell': isCommunityRoute }">
        <AppSidebar v-if="isCommunityRoute" class="community-route-shell__sidebar" />
        <div :class="{ 'community-route-shell__content': isCommunityRoute }">
          <component :is="Component" />
        </div>
      </div>
    </transition>
  </router-view>
  <AppToast />
</template>

<style scoped>
.community-fade-enter-active {
  animation: community-page-in 0.25s ease;
}
.community-fade-leave-active {
  animation: community-page-out 0.15s ease;
}

.community-route-shell {
  min-height: 100vh;
}

@media (min-width: 768px) {
  .community-route-shell {
    min-height: 100vh;
    padding-left: 232px;
    background:
      radial-gradient(circle at 14% 0, rgba(45, 212, 191, 0.12), transparent 28%),
      radial-gradient(circle at 84% 10%, rgba(96, 165, 250, 0.1), transparent 28%),
      var(--c-bg);
  }

  .community-route-shell__content {
    min-height: 100vh;
  }
}

@media (max-width: 767px) {
  .community-route-shell__sidebar {
    display: none;
  }
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
