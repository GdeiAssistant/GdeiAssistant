<script setup>
import { computed, ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { BookOpen, ChevronRight } from 'lucide-vue-next'
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
  <aside class="campus-sidebar" aria-label="侧边导航">
    <div class="campus-sidebar__brand">
      <div class="campus-sidebar__mark" aria-hidden="true">
        <BookOpen class="w-5 h-5" />
      </div>
      <span class="campus-sidebar__brand-name">{{ $t('about.appName') }}</span>
    </div>

    <nav class="campus-sidebar__nav" aria-label="主要导航">
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
  left: 14px;
  top: 14px;
  bottom: 14px;
  z-index: 48;
  display: flex;
  width: 204px;
  flex-direction: column;
  overflow: hidden;
  border: 1px solid rgba(205, 222, 226, 0.78);
  border-radius: 26px;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.94), rgba(250, 255, 253, 0.9)),
    radial-gradient(circle at 45% 100%, rgba(173, 234, 202, 0.28), transparent 46%);
  box-shadow: 0 22px 56px rgba(32, 69, 78, 0.1);
  backdrop-filter: blur(22px);
}

.campus-sidebar::after {
  position: absolute;
  right: 0;
  bottom: 0;
  left: 0;
  height: 168px;
  pointer-events: none;
  content: '';
  background:
    linear-gradient(145deg, transparent 42%, rgba(42, 157, 128, 0.07) 42% 44%, transparent 44%),
    radial-gradient(ellipse at 24% 88%, rgba(86, 192, 160, 0.18), transparent 46%),
    radial-gradient(ellipse at 65% 100%, rgba(75, 153, 201, 0.12), transparent 42%);
}

.campus-sidebar__brand {
  position: relative;
  display: flex;
  align-items: center;
  gap: 11px;
  padding: 22px 18px 18px;
}

.campus-sidebar__mark {
  display: grid;
  width: 38px;
  height: 38px;
  place-items: center;
  border-radius: 14px;
  color: #fff;
  background: linear-gradient(135deg, #14b985, #3aa7e8);
  box-shadow: 0 12px 24px rgba(20, 185, 133, 0.22);
}

.campus-sidebar__brand-name {
  min-width: 0;
  color: var(--c-text-1);
  font-size: 17px;
  font-weight: 850;
  letter-spacing: -0.01em;
  white-space: nowrap;
}

.campus-sidebar__nav {
  position: relative;
  z-index: 1;
  flex: 1;
  padding: 4px 12px 16px;
}

.campus-sidebar__list {
  margin: 0;
  padding: 0;
  list-style: none;
}

.campus-sidebar__list > li + li {
  margin-top: 7px;
}

.campus-sidebar__item {
  display: flex;
  width: 100%;
  min-height: 46px;
  align-items: center;
  gap: 11px;
  border: 0;
  border-radius: 16px;
  background: transparent;
  color: var(--c-text-2);
  cursor: pointer;
  font: inherit;
  font-size: 14px;
  font-weight: 760;
  padding: 0 13px;
  transition: background 0.18s ease, color 0.18s ease, transform 0.18s ease;
}

.campus-sidebar__item:hover {
  color: var(--c-text-1);
  background: rgba(255, 255, 255, 0.72);
  transform: translateX(2px);
}

.campus-sidebar__item--active {
  color: var(--c-primary);
  background: linear-gradient(135deg, rgba(20, 185, 133, 0.15), rgba(58, 167, 232, 0.12));
  box-shadow: inset 0 0 0 1px rgba(32, 170, 130, 0.12);
}

.campus-sidebar__icon {
  width: 19px;
  height: 19px;
  flex: none;
}

.campus-sidebar__label {
  flex: 1;
  text-align: left;
}

.campus-sidebar__dot {
  min-width: 20px;
  border-radius: 999px;
  background: #ff6b6b;
  color: #fff;
  font-size: 11px;
  font-weight: 800;
  line-height: 20px;
  text-align: center;
}

.campus-sidebar__footer {
  position: relative;
  z-index: 1;
  padding: 14px 12px 16px;
  border-top: 1px solid rgba(211, 225, 229, 0.72);
}

.campus-sidebar__profile {
  display: flex;
  width: 100%;
  min-height: 56px;
  align-items: center;
  gap: 10px;
  margin-top: 10px;
  border: 0;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.68);
  color: inherit;
  cursor: pointer;
  font: inherit;
  padding: 8px;
  text-align: left;
  box-shadow: inset 0 0 0 1px rgba(216, 229, 232, 0.68);
}

.campus-sidebar__avatar {
  display: grid;
  width: 36px;
  height: 36px;
  flex: none;
  place-items: center;
  border-radius: 14px;
  color: #fff;
  background: linear-gradient(135deg, #1fb981, #4aa5f0);
  font-size: 13px;
  font-weight: 850;
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
  font-weight: 760;
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
    transform: translateX(calc(-100% - 20px));
    transition: transform 0.22s ease;
  }

  .campus-sidebar--open {
    transform: translateX(0);
  }
}

[data-theme="dark"] .campus-sidebar {
  border-color: rgba(45, 58, 73, 0.88);
  background:
    linear-gradient(180deg, rgba(20, 27, 37, 0.94), rgba(16, 23, 31, 0.9)),
    radial-gradient(circle at 45% 100%, rgba(45, 212, 191, 0.12), transparent 46%);
  box-shadow: 0 22px 56px rgba(0, 0, 0, 0.32);
}

[data-theme="dark"] .campus-sidebar__item:hover,
[data-theme="dark"] .campus-sidebar__profile {
  background: rgba(31, 41, 55, 0.72);
}

[data-theme="dark"] .campus-sidebar__footer {
  border-top-color: rgba(45, 58, 73, 0.88);
}
</style>
