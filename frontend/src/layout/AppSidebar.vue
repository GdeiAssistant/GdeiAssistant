<script setup>
import { computed, ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ChevronRight } from 'lucide-vue-next'
import { getCurrentUserProfile } from '@/api/user'
import { createFooterItems, createNavItems } from './navigation'

const emit = defineEmits(['navigate'])
const router = useRouter()
const route = useRoute()
const { t } = useI18n()

const profile = ref(null)

onMounted(async () => {
  try {
    const res = await getCurrentUserProfile()
    profile.value = res?.data || null
  } catch (_) {}
})

const navItems = computed(() => createNavItems(t))
const footerItems = computed(() => createFooterItems(t))

function isActive(path) {
  return route.path === path || route.path.startsWith(path + '/')
}

function navigate(path) {
  router.push(path)
  emit('navigate')
}

function avatarInitial() {
  const name = profile.value?.nickname || profile.value?.username || t('sidebar.notLoggedIn')
  return name.charAt(0)
}
</script>

<template>
  <aside class="campus-sidebar" :aria-label="t('navigationAccessibility.sidebarNavigation')">
    <div class="campus-sidebar__brand">
      <img class="campus-sidebar__mark" src="/favicon.svg" alt="" aria-hidden="true" width="32" height="32" />
      <span class="campus-sidebar__brand-name">{{ $t('about.appName') }}</span>
    </div>

    <nav class="campus-sidebar__nav" :aria-label="t('navigationAccessibility.primaryNavigation')">
      <ul class="campus-sidebar__list">
        <li v-for="item in navItems" :key="item.path">
          <button
            class="campus-sidebar__item"
            :class="{ 'campus-sidebar__item--active': isActive(item.path) }"
            :aria-current="isActive(item.path) ? 'page' : undefined"
            @click="navigate(item.path)"
          >
            <component :is="item.icon" class="campus-sidebar__icon" />
            <span class="campus-sidebar__label">{{ item.label }}</span>
            <span v-if="item.path === '/info'" class="campus-sidebar__dot">3</span>
          </button>
        </li>
      </ul>
    </nav>

    <div class="campus-sidebar__footer">
      <ul class="campus-sidebar__list">
        <li v-for="item in footerItems" :key="item.path">
          <button class="campus-sidebar__item" @click="navigate(item.path)">
            <component :is="item.icon" class="campus-sidebar__icon" />
            <span class="campus-sidebar__label">{{ item.label }}</span>
          </button>
        </li>
      </ul>

      <button class="campus-sidebar__profile" type="button" @click="navigate('/profile')">
        <div class="campus-sidebar__avatar">{{ avatarInitial() }}</div>
        <div class="campus-sidebar__profile-text">
          <p>{{ profile?.nickname || profile?.username || $t('sidebar.notLoggedIn') }}</p>
        </div>
        <ChevronRight class="campus-sidebar__chevron" />
      </button>
    </div>
  </aside>
</template>

<style scoped>
.campus-sidebar {
  position: fixed;
  top: 0;
  bottom: 0;
  left: 0;
  z-index: 48;
  display: flex;
  width: var(--rail-width, 248px);
  flex-direction: column;
  border-right: 1px solid var(--c-border);
  background: var(--c-surface);
}

.campus-sidebar__brand {
  display: flex;
  align-items: center;
  gap: 10px;
  height: 64px;
  padding: 0 20px;
  border-bottom: 1px solid var(--c-divider);
}

.campus-sidebar__mark {
  width: 32px;
  height: 32px;
  flex: none;
  border-radius: 8px;
}

.campus-sidebar__brand-name {
  min-width: 0;
  overflow: hidden;
  color: var(--c-text-1);
  font-size: 15px;
  font-weight: 650;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.campus-sidebar__nav {
  flex: 1;
  overflow-y: auto;
  padding: 16px 12px;
}

.campus-sidebar__list {
  display: flex;
  flex-direction: column;
  gap: 2px;
  margin: 0;
  padding: 0;
  list-style: none;
}

.campus-sidebar__item {
  position: relative;
  display: flex;
  width: 100%;
  min-height: 40px;
  align-items: center;
  gap: 12px;
  border: 0;
  border-radius: var(--radius-control);
  background: transparent;
  color: var(--c-text-2);
  cursor: pointer;
  font: inherit;
  font-size: 14px;
  font-weight: 500;
  padding: 0 12px;
  text-align: left;
  transition: background-color 0.15s ease, color 0.15s ease;
}

.campus-sidebar__item:hover {
  color: var(--c-text-1);
  background: var(--c-surface-hover);
}

.campus-sidebar__item--active,
.campus-sidebar__item--active:hover {
  color: var(--c-primary);
  background: var(--c-primary-soft);
  font-weight: 600;
}

.campus-sidebar__icon {
  width: 18px;
  height: 18px;
  flex: none;
  stroke-width: 1.75;
}

.campus-sidebar__label {
  flex: 1;
  min-width: 0;
}

.campus-sidebar__dot {
  min-width: 20px;
  height: 20px;
  padding: 0 6px;
  border-radius: 999px;
  background: var(--c-danger);
  color: #fff;
  font-size: 11px;
  font-weight: 600;
  line-height: 20px;
  text-align: center;
}

[data-theme="dark"] .campus-sidebar__dot {
  color: #2A0C0C;
}

.campus-sidebar__footer {
  padding: 12px;
  border-top: 1px solid var(--c-divider);
}

.campus-sidebar__profile {
  display: flex;
  width: 100%;
  min-height: 52px;
  align-items: center;
  gap: 10px;
  margin-top: 8px;
  border: 1px solid var(--c-border);
  border-radius: var(--radius-control);
  background: var(--c-bg);
  color: inherit;
  cursor: pointer;
  font: inherit;
  padding: 8px 10px;
  text-align: left;
  transition: border-color 0.15s ease;
}

.campus-sidebar__profile:hover {
  border-color: color-mix(in srgb, var(--c-primary) 45%, var(--c-border));
}

.campus-sidebar__avatar {
  display: grid;
  width: 32px;
  height: 32px;
  flex: none;
  place-items: center;
  border-radius: 999px;
  background: var(--c-primary-soft);
  color: var(--c-primary);
  font-size: 13px;
  font-weight: 650;
}

.campus-sidebar__profile-text {
  min-width: 0;
  flex: 1;
}

.campus-sidebar__profile-text p {
  overflow: hidden;
  margin: 0;
  color: var(--c-text-1);
  font-size: 13px;
  font-weight: 600;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.campus-sidebar__chevron {
  width: 16px;
  height: 16px;
  flex: none;
  color: var(--c-text-3);
}

@media (max-width: 767px) {
  .campus-sidebar {
    width: min(280px, 84vw);
    box-shadow: var(--shadow-lg);
    transform: translateX(-100%);
    visibility: hidden;
    transition: transform 0.22s ease, visibility 0s linear 0.22s;
  }

  .campus-sidebar--open {
    transform: translateX(0);
    visibility: visible;
    transition: transform 0.22s ease, visibility 0s;
  }
}
</style>
