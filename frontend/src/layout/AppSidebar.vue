<script setup>
import { computed, ref, onMounted, onBeforeUnmount } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ChevronRight } from 'lucide-vue-next'
import { getCurrentUserProfile } from '@/api/user'
import { getThemeAccent, setThemeAccent } from '@/theme'
import { createFooterItems, createNavItems } from './navigation'

const emit = defineEmits(['navigate'])
const router = useRouter()
const route = useRoute()
const { t } = useI18n()

const profile = ref(null)
const currentAccent = ref(getThemeAccent())
let accentObserver = null

const accentSwatches = [
  { id: 'banyan', lightHex: '#0E7C61', darkHex: '#2DD4BF' },
  { id: 'kapok', lightHex: '#C85A32', darkHex: '#FB923C' },
  { id: 'haizhu', lightHex: '#1D4ED8', darkHex: '#60A5FA' },
  { id: 'bauhinia', lightHex: '#6D28D9', darkHex: '#A78BFA' },
]

function syncAccent() {
  currentAccent.value = document.documentElement.dataset.accent || getThemeAccent()
}

function selectAccent(id) {
  currentAccent.value = setThemeAccent(id)
}

onMounted(async () => {
  syncAccent()
  accentObserver = new MutationObserver(syncAccent)
  accentObserver.observe(document.documentElement, {
    attributes: true,
    attributeFilter: ['data-accent']
  })
  try {
    const res = await getCurrentUserProfile()
    profile.value = res?.data || null
  } catch (_) {}
})

onBeforeUnmount(() => {
  accentObserver?.disconnect()
  accentObserver = null
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
      <img class="campus-sidebar__mark" src="/favicon.svg" alt="" aria-hidden="true" width="30" height="30" />
      <span class="campus-sidebar__brand-name editorial-serif">{{ $t('about.appName') }}</span>
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
          </button>
        </li>
      </ul>
    </nav>

    <div class="campus-sidebar__footer">
      <div class="campus-sidebar__accents" role="group" :aria-label="t('sidebar.accentLabel')">
        <span class="campus-sidebar__accents-label">{{ t('sidebar.accentLabel') }}</span>
        <div class="campus-sidebar__accents-dots">
          <button
            v-for="swatch in accentSwatches"
            :key="swatch.id"
            type="button"
            class="campus-sidebar__accent-btn"
            :class="{ 'campus-sidebar__accent-btn--active': currentAccent === swatch.id }"
            :aria-label="t(`appearance.accent.${swatch.id}`)"
            :aria-pressed="currentAccent === swatch.id"
            :title="t(`appearance.accent.${swatch.id}`)"
            @click="selectAccent(swatch.id)"
          >
            <span
              class="campus-sidebar__accent-dot"
              :style="{ backgroundColor: swatch.lightHex, '--swatch-dark': swatch.darkHex }"
              aria-hidden="true"
            />
          </button>
        </div>
      </div>

      <ul class="campus-sidebar__list">
        <li v-for="item in footerItems" :key="item.path">
          <button
            class="campus-sidebar__item"
            :class="{ 'campus-sidebar__item--active': isActive(item.path) }"
            @click="navigate(item.path)"
          >
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
  width: var(--rail-width, 232px);
  flex-direction: column;
  border-right: 1px solid var(--c-border);
  background: color-mix(in srgb, var(--c-surface) 45%, var(--c-bg));
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
  width: 30px;
  height: 30px;
  flex: none;
  border-radius: 8px;
}

.campus-sidebar__brand-name {
  min-width: 0;
  overflow: hidden;
  color: var(--c-text-1);
  font-size: 16px;
  font-weight: 700;
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
  min-height: 44px;
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

.campus-sidebar__item--active::before {
  content: '';
  position: absolute;
  left: 0;
  top: 9px;
  bottom: 9px;
  width: 2.5px;
  border-radius: 2px;
  background: var(--c-primary);
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

.campus-sidebar__footer {
  padding: 12px;
  border-top: 1px solid var(--c-divider);
}

.campus-sidebar__accents {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  padding: 4px 10px 10px;
}

.campus-sidebar__accents-label {
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  color: var(--c-text-3);
}

.campus-sidebar__accents-dots {
  display: flex;
  align-items: center;
  gap: 4px;
}

.campus-sidebar__accent-btn {
  display: inline-flex;
  width: 24px;
  height: 24px;
  align-items: center;
  justify-content: center;
  border: 1px solid transparent;
  border-radius: 999px;
  background: transparent;
  cursor: pointer;
  padding: 0;
  transition: border-color 0.15s ease, transform 0.15s ease;
}

.campus-sidebar__accent-btn:hover {
  transform: scale(1.08);
}

.campus-sidebar__accent-btn--active {
  border-color: var(--c-text-2);
}

.campus-sidebar__accent-dot {
  width: 12px;
  height: 12px;
  border-radius: 999px;
}

[data-theme="dark"] .campus-sidebar__accent-dot {
  background-color: var(--swatch-dark) !important;
}

.campus-sidebar__profile {
  display: flex;
  width: 100%;
  min-height: 48px;
  align-items: center;
  gap: 10px;
  margin-top: 8px;
  border: 1px solid var(--c-border);
  border-radius: var(--radius-control);
  background: var(--c-surface);
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
  width: 30px;
  height: 30px;
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

@media (max-width: 1023px) {
  .campus-sidebar {
    width: min(280px, 84vw);
    background: var(--c-surface);
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
