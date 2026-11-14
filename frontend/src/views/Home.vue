<script setup>
import { RouterLink, useRouter } from 'vue-router'
import { onMounted, onActivated, ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { ALL_FEATURES, getLocalizedFeatures } from '@/constants/features'
import { getSchedule } from '@/api/schedule'
import request from '@/utils/request'
import { useLatestRequest } from '@/composables/useLatestRequest'
import { campusDateLabel, selectTodayCourses, todayColumn } from './homeToday'
import {
  ArrowRight, Bell, Calendar, CalendarCheck, Database, DoorOpen,
  Dumbbell, Eye, FileText, GraduationCap, Heart, Info as InfoIcon, MessageCircle,
  PenLine, Search, ShoppingCart, Star, Truck, Users, BookOpen,
  Camera, WalletCards
} from 'lucide-vue-next'

const STORAGE_KEY = 'user_features_config'
const MIGRATION_KEY = 'user_features_migrated_v2'

const router = useRouter()
const { t, locale } = useI18n()

const localizedFeatures = computed(() => getLocalizedFeatures(ALL_FEATURES, t))

function migrateFeatureConfig(config) {
  if (!config || typeof config !== 'object') return config
  if (localStorage.getItem(MIGRATION_KEY)) return config

  const oldLibraryIds = ['book', 'collection']
  const oldCardIds = ['cardInfo', 'card']
  const hasOldLibrary = oldLibraryIds.some((k) => k in config)
  const hasOldCard = oldCardIds.some((k) => k in config)

  if (hasOldLibrary) {
    const anyVisible = oldLibraryIds.some((k) => config[k] !== false)
    config['collection'] = anyVisible
    delete config['book']
  }
  if (hasOldCard) {
    const anyVisible = oldCardIds.some((k) => config[k] !== false)
    config['card'] = anyVisible
    delete config['cardInfo']
  }

  localStorage.setItem(STORAGE_KEY, JSON.stringify(config))
  localStorage.setItem(MIGRATION_KEY, '1')
  return config
}

const featuresConfig = ref(null)
function loadFeaturesConfig() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    let config = raw ? JSON.parse(raw) : {}
    config = migrateFeatureConfig(config)
    featuresConfig.value = config
  } catch (_) {
    featuresConfig.value = {}
  }
}
onMounted(loadFeaturesConfig)
onActivated(loadFeaturesConfig)

// Load today's schedule silently so failures degrade gracefully into an entry link.
const todayState = ref('loading') // loading | ready | unavailable
const todayCourses = ref([])
const scheduleWeek = ref(null)
const todayDate = ref(new Date())
const todayRequest = useLatestRequest()

const cardSummary = ref(null)
const recentNotices = ref([])

async function loadTodayCourses() {
  const task = todayRequest.begin()
  if (todayCourses.value.length === 0) todayState.value = 'loading'
  try {
    const res = await getSchedule(undefined, { signal: task.signal, skipErrorTip: true })
    if (!task.isCurrent()) return
    const date = new Date()
    todayDate.value = date
    const week = res?.data?.week
    scheduleWeek.value = Number.isInteger(week) ? week : null
    const courses = res?.success && res.data
      ? selectTodayCourses(res.data.scheduleList, week, todayColumn(date))
      : null
    if (courses !== null) {
      todayCourses.value = courses
      todayState.value = 'ready'
    } else {
      todayState.value = 'unavailable'
    }
  } catch (error) {
    if (task.isCurrent() && !error?.cancelled) todayState.value = 'unavailable'
  }
}

async function loadEditorialRailData() {
  try {
    const [cardRes, noticeRes] = await Promise.allSettled([
      request.get('/card/info', { skipErrorTip: true }),
      request.get('/information/announcement/start/0/size/3', { skipErrorTip: true })
    ])
    if (cardRes.status === 'fulfilled' && cardRes.value?.data) {
      const payload = cardRes.value.data?.data ?? cardRes.value.data
      if (payload && typeof payload === 'object') {
        cardSummary.value = payload
      }
    }
    if (noticeRes.status === 'fulfilled' && Array.isArray(noticeRes.value?.data)) {
      recentNotices.value = noticeRes.value.data.slice(0, 3)
    }
  } catch (_) {}
}

onMounted(() => {
  loadTodayCourses()
  loadEditorialRailData()
})
onActivated(() => {
  loadTodayCourses()
})

const visibleMenuList = computed(() => {
  const config = featuresConfig.value
  return localizedFeatures.value.filter((f) => {
    if (!config || Object.keys(config).length === 0) return f.defaultVisible !== false
    return config[f.id] !== false
  }).map((f) => ({
    id: f.id,
    title: f.name,
    path: f.path,
    type: f.type,
    key: f.key,
  }))
})

const SERVICE_FEATURE_IDS = new Set(['grade', 'schedule', 'cet', 'kaoyan', 'spare', 'collection', 'card', 'pe', 'data', 'evaluate'])
const LIFE_FEATURE_IDS = new Set(['ershou', 'delivery', 'lostandfound', 'secret', 'dating', 'express', 'topic', 'photograph'])

const featureSections = computed(() => {
  const serviceItems = visibleMenuList.value.filter((item) => SERVICE_FEATURE_IDS.has(item.id))
  const lifeItems = visibleMenuList.value.filter((item) => LIFE_FEATURE_IDS.has(item.id))

  return [
    {
      id: 'service',
      title: t('home.sectionService'),
      description: t('home.sectionServiceDesc'),
      items: serviceItems
    },
    {
      id: 'life',
      title: t('home.sectionLife'),
      description: t('home.sectionLifeDesc'),
      items: lifeItems
    }
  ].filter((section) => section.items.length > 0)
})

const iconMap = {
  grade: Star, schedule: Calendar, card: WalletCards, cet: FileText,
  kaoyan: GraduationCap, collection: BookOpen, spare: DoorOpen,
  pe: Dumbbell, evaluate: PenLine, data: Database, about: InfoIcon,
  ershou: ShoppingCart, lostandfound: Search, express: Heart,
  secret: Eye, dating: Users, topic: MessageCircle,
  photograph: Camera, delivery: Truck,
}

function handleMenuClick(item) {
  if (item.path) {
    router.push(item.path)
  }
}

function goTo(path) {
  router.push(path)
}

function openPalette() {
  window.dispatchEvent(new CustomEvent('open-command-palette'))
}

function resolveFeatureIcon(id) {
  return iconMap[id] || InfoIcon
}

const greetingKey = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6 || hour >= 18) return 'home.greetingEvening'
  if (hour < 12) return 'home.greetingMorning'
  return 'home.greetingAfternoon'
})

const todayLabel = computed(() => {
  try {
    return campusDateLabel(todayDate.value, locale.value)
  } catch (_) {
    return ''
  }
})
</script>

<template>
  <div class="home-broadside home-page">
    <div class="home-broadside__main">
      <!-- Editorial Masthead -->
      <header class="home-masthead">
        <p v-if="todayLabel || scheduleWeek" class="home-masthead__eyebrow editorial-eyebrow tabular-num">
          <span v-if="todayLabel" class="home-hero__date">{{ todayLabel }}</span>
          <span v-if="todayLabel && scheduleWeek" aria-hidden="true">·</span>
          <span v-if="scheduleWeek">{{ $t('home.weekLabel', { week: scheduleWeek }) }}</span>
        </p>
        <div class="home-masthead__row">
          <h1 class="home-masthead__title editorial-serif">{{ $t(greetingKey) }}</h1>
          <div class="home-masthead__triggers">
            <RouterLink to="/schedule" class="home-chip home-chip--accent campus-press">
              <CalendarCheck class="w-3.5 h-3.5" aria-hidden="true" />
              <span>{{ $t('home.todayScheduleTitle') }}</span>
            </RouterLink>
            <RouterLink to="/card" class="home-chip campus-press">
              <WalletCards class="w-3.5 h-3.5" aria-hidden="true" />
              <span>{{ $t('home.todayCardTitle') }}</span>
            </RouterLink>
            <button type="button" class="home-chip campus-press" @click="openPalette">
              <Search class="w-3.5 h-3.5" aria-hidden="true" />
              <span>{{ $t('home.openCommandPalette') }}</span>
              <kbd class="home-chip__kbd tabular-num">⌘K</kbd>
            </button>
          </div>
        </div>
      </header>

      <!-- Single Elevated Primary Focal Card: Today's Course Overview -->
      <section class="home-focal-card" aria-labelledby="home-today-title" :aria-busy="todayState === 'loading'">
        <header class="home-focal-card__header">
          <div class="home-focal-card__heading home-today__heading">
            <span class="home-focal-card__icon" aria-hidden="true">
              <CalendarCheck class="w-5 h-5" />
            </span>
            <div>
              <h2 id="home-today-title" class="editorial-serif">{{ $t('home.todayScheduleTitle') }}</h2>
              <p v-if="todayState === 'ready' && todayCourses.length" class="tabular-num">
                {{ $t('home.todayCourseCount', { count: todayCourses.length }) }}
              </p>
            </div>
          </div>
          <button type="button" class="home-focal-card__action campus-press" @click="goTo('/schedule')">
            {{ $t('home.todayOpenSchedule') }}
            <ArrowRight class="w-4 h-4" aria-hidden="true" />
          </button>
        </header>

        <ul v-if="todayState === 'loading'" class="home-today__list" aria-hidden="true">
          <li v-for="n in 2" :key="n" class="home-today__item home-today__item--skeleton">
            <span class="home-today__skeleton home-today__skeleton--short"></span>
            <span class="home-today__skeleton"></span>
          </li>
        </ul>
        <ol v-else-if="todayState === 'ready' && todayCourses.length" class="home-today__list">
          <li
            v-for="(course, idx) in todayCourses"
            :key="course.key"
            class="home-today__item"
            :class="{ 'home-today__item--emphasis': idx === 0 }"
          >
            <span class="home-today__lesson tabular-num">{{ course.lesson }}</span>
            <span class="home-today__course">
              <strong>{{ course.name }}</strong>
              <small v-if="course.location">{{ course.location }}</small>
            </span>
          </li>
        </ol>
        <p v-else class="home-today__empty">
          {{ todayState === 'ready' ? $t('home.todayNoCourse') : $t('home.todayUnavailable') }}
        </p>
      </section>

      <!-- Anti-Cardification Secondary Canvas Directory -->
      <section
        v-for="section in featureSections"
        :id="`home-${section.id}`"
        :key="section.id"
        class="home-directory feature-section"
      >
        <header class="home-directory__header">
          <div class="home-directory__heading">
            <h2 class="editorial-serif">{{ section.title }}</h2>
            <span class="home-directory__count tabular-num">{{ section.items.length }}</span>
          </div>
          <p v-if="section.description" class="home-directory__desc">{{ section.description }}</p>
        </header>

        <div class="home-directory__list">
          <button
            v-for="(item, index) in section.items"
            :key="item.id || item.path || item.key || index"
            type="button"
            :aria-label="item.title"
            class="home-directory__item feature-card campus-press"
            @click="handleMenuClick(item)"
          >
            <span class="home-directory__icon" aria-hidden="true">
              <component :is="resolveFeatureIcon(item.id)" class="w-4 h-4" />
            </span>
            <span class="home-directory__title">{{ item.title }}</span>
            <ArrowRight class="home-directory__arrow" aria-hidden="true" />
          </button>
        </div>
      </section>

      <div v-if="featureSections.length === 0" class="home-empty">
        {{ $t('home.noFeatures') }}
      </div>
    </div>

    <!-- Editorial Right Utility Rail -->
    <aside class="home-rail">
      <section class="home-rail__card" @click="goTo('/card')">
        <div class="home-rail__card-top">
          <span class="home-rail__eyebrow">{{ $t('home.todayCardTitle') }}</span>
          <WalletCards class="w-4 h-4 opacity-80" aria-hidden="true" />
        </div>
        <div class="home-rail__balance tabular-num">
          <template v-if="cardSummary?.cardBalance !== undefined && cardSummary?.cardBalance !== null">
            <span class="home-rail__balance-num">{{ cardSummary.cardBalance }}</span>
            <span class="home-rail__balance-unit">{{ $t('card.amountUnit') }}</span>
          </template>
          <span v-else class="home-rail__balance-fallback">{{ $t('home.todayCardTitle') }}</span>
        </div>
        <p class="home-rail__card-desc">
          {{ cardSummary?.cardLostState || $t('home.todayCardDesc') }}
        </p>
        <div class="home-rail__card-link">
          <span>{{ $t('home.todayCardMeta') }}</span>
          <ArrowRight class="w-3.5 h-3.5" aria-hidden="true" />
        </div>
      </section>

      <section v-if="recentNotices.length" class="home-rail__ledger">
        <div class="home-rail__ledger-head">
          <h3 class="editorial-eyebrow">{{ $t('home.railNoticeTitle') }}</h3>
          <RouterLink to="/info" class="home-rail__more">
            {{ $t('home.todayMore') }}
          </RouterLink>
        </div>
        <ul class="home-rail__notice-list">
          <li v-for="notice in recentNotices" :key="notice.id">
            <RouterLink
              :to="notice.id ? `/info/announcements/${notice.id}` : '/info'"
              class="home-rail__notice-item"
            >
              <Bell class="w-3.5 h-3.5 shrink-0 text-[var(--c-primary)] mt-0.5" aria-hidden="true" />
              <span class="home-rail__notice-body">
                <strong>{{ notice.title }}</strong>
                <small v-if="notice.publishTime">{{ notice.publishTime }}</small>
              </span>
            </RouterLink>
          </li>
        </ul>
      </section>
    </aside>
  </div>
</template>

<style scoped>
.home-broadside {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 300px;
  gap: 32px;
  align-items: start;
}

.home-broadside__main {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 28px;
}

/* Editorial Masthead */
.home-masthead {
  padding-top: 4px;
}

.home-masthead__eyebrow {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  margin: 0 0 8px;
  color: var(--c-primary);
}

.home-masthead__row {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-end;
  justify-content: space-between;
  gap: 14px 20px;
}

.home-masthead__title {
  margin: 0;
  color: var(--c-text-1);
  font-size: clamp(28px, 3.6vw, 38px);
  font-weight: 700;
  line-height: 1.15;
}

.home-masthead__triggers {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
}

.home-chip {
  display: inline-flex;
  min-height: 36px;
  align-items: center;
  gap: 6px;
  padding: 0 12px;
  border: 1px solid var(--c-border);
  border-radius: 999px;
  background: var(--c-surface);
  color: var(--c-text-2);
  font: inherit;
  font-size: 12px;
  font-weight: 600;
  text-decoration: none;
  cursor: pointer;
  transition: border-color 0.15s ease, color 0.15s ease, background-color 0.15s ease;
}

.home-chip:hover {
  border-color: color-mix(in srgb, var(--c-primary) 45%, var(--c-border));
  color: var(--c-primary);
}

.home-chip--accent {
  border-color: color-mix(in srgb, var(--c-primary) 26%, var(--c-border));
  background: var(--c-primary-soft);
  color: var(--c-primary);
}

.home-chip__kbd {
  font-family: var(--font-mono);
  font-size: 10px;
  color: var(--c-text-3);
}

/* Single Elevated Primary Focal Card */
.home-focal-card {
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: 20px 24px;
  border: 1px solid var(--c-border);
  border-radius: var(--radius-card);
  background: var(--c-surface);
}

.home-focal-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.home-focal-card__heading {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}

.home-focal-card__icon {
  display: grid;
  width: 42px;
  height: 42px;
  flex: none;
  place-items: center;
  border-radius: var(--radius-control);
  background: var(--c-primary-soft);
  color: var(--c-primary);
}

.home-focal-card__heading h2 {
  margin: 0;
  color: var(--c-text-1);
  font-size: 18px;
  font-weight: 700;
}

.home-focal-card__heading p {
  margin: 2px 0 0;
  color: var(--c-text-2);
  font-size: 12px;
}

.home-focal-card__action {
  display: inline-flex;
  min-height: 38px;
  flex: none;
  align-items: center;
  gap: 6px;
  padding: 0 14px;
  border: 0;
  border-radius: var(--radius-control);
  background: var(--c-primary);
  color: var(--c-on-primary);
  font: inherit;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.15s ease;
}

.home-focal-card__action:hover {
  background: var(--c-primary-hover);
}

.home-today__list {
  display: flex;
  flex-direction: column;
  margin: 0;
  padding: 0;
  list-style: none;
}

.home-today__item {
  position: relative;
  display: grid;
  grid-template-columns: 92px minmax(0, 1fr);
  align-items: center;
  gap: 12px;
  border-top: 1px solid var(--c-divider);
  padding: 13px 0 13px 12px;
}

.home-today__item--emphasis::before {
  content: '';
  position: absolute;
  left: 0;
  top: 12px;
  bottom: 12px;
  width: 2.5px;
  border-radius: 2px;
  background: var(--c-primary);
}

.home-today__lesson {
  color: var(--c-primary);
  font-family: var(--font-mono);
  font-size: 12px;
  font-weight: 600;
}

.home-today__course {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 2px;
}

.home-today__course strong {
  overflow: hidden;
  color: var(--c-text-1);
  font-size: 15px;
  font-weight: 600;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.home-today__course small {
  color: var(--c-text-2);
  font-size: 12px;
}

.home-today__empty {
  margin: 0;
  border-top: 1px solid var(--c-divider);
  color: var(--c-text-2);
  font-size: 14px;
  padding: 14px 0 2px;
}

.home-today__skeleton {
  display: block;
  height: 14px;
  border-radius: 6px;
  background: var(--c-fill-2);
}

.home-today__skeleton--short {
  width: 64px;
}

/* Anti-Cardification Secondary Canvas Directory */
.home-directory {
  padding-top: 18px;
  border-top: 1px solid var(--c-border);
  scroll-margin-top: 88px;
}

.home-directory__header {
  margin-bottom: 12px;
}

.home-directory__heading {
  display: flex;
  align-items: center;
  gap: 8px;
}

.home-directory__heading h2 {
  margin: 0;
  color: var(--c-text-1);
  font-size: 19px;
  font-weight: 700;
}

.home-directory__count {
  font-family: var(--font-mono);
  font-size: 11px;
  font-weight: 600;
  color: var(--c-text-3);
}

.home-directory__desc {
  margin: 4px 0 0;
  color: var(--c-text-2);
  font-size: 13px;
  line-height: 1.5;
}

.home-directory__list {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  column-gap: 24px;
  border-top: 1px solid var(--c-divider);
}

.home-directory__item {
  display: flex;
  align-items: center;
  gap: 12px;
  min-height: 52px;
  padding: 10px 8px;
  border: 0;
  border-bottom: 1px solid var(--c-divider);
  border-radius: 0;
  background: transparent;
  color: var(--c-text-1);
  font: inherit;
  text-align: left;
  cursor: pointer;
  transition: background-color 0.14s ease, color 0.14s ease, padding-inline 0.14s ease;
}

.home-directory__item:hover {
  background: var(--c-surface);
  color: var(--c-primary);
  padding-inline: 12px;
}

.home-directory__icon {
  display: grid;
  width: 30px;
  height: 30px;
  flex: none;
  place-items: center;
  border-radius: 8px;
  background: var(--c-primary-soft);
  color: var(--c-primary);
}

.home-directory__title {
  flex: 1;
  min-width: 0;
  font-size: 14px;
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.home-directory__arrow {
  width: 15px;
  height: 15px;
  flex: none;
  color: var(--c-text-3);
  transition: transform 0.14s ease, color 0.14s ease;
}

.home-directory__item:hover .home-directory__arrow {
  color: var(--c-primary);
  transform: translateX(2px);
}

/* Right Utility Rail */
.home-rail {
  position: sticky;
  top: 88px;
  display: flex;
  flex-direction: column;
  gap: 22px;
}

.home-rail__card {
  padding: 20px;
  border-radius: var(--radius-card);
  background: var(--c-brand-deep);
  color: var(--c-on-brand-deep);
  cursor: pointer;
  transition: transform 0.15s ease, opacity 0.15s ease;
}

.home-rail__card:hover {
  opacity: 0.96;
}

.home-rail__card-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.04em;
  opacity: 0.9;
}

.home-rail__balance {
  display: flex;
  align-items: baseline;
  gap: 6px;
  margin-top: 14px;
}

.home-rail__balance-num {
  font-family: var(--font-serif);
  font-size: 32px;
  font-weight: 700;
  line-height: 1;
}

.home-rail__balance-unit {
  font-size: 13px;
  opacity: 0.85;
}

.home-rail__balance-fallback {
  font-family: var(--font-serif);
  font-size: 20px;
  font-weight: 700;
}

.home-rail__card-desc {
  margin: 8px 0 0;
  font-size: 12px;
  line-height: 1.5;
  opacity: 0.82;
}

.home-rail__card-link {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  margin-top: 14px;
  font-size: 12px;
  font-weight: 600;
}

.home-rail__ledger {
  border-top: 1px solid var(--c-border);
  padding-top: 14px;
}

.home-rail__ledger-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}

.home-rail__more {
  border: 0;
  background: transparent;
  color: var(--c-primary);
  font: inherit;
  font-size: 12px;
  font-weight: 600;
  text-decoration: none;
  cursor: pointer;
  padding: 2px 4px;
}

.home-rail__notice-list {
  margin: 0;
  padding: 0;
  list-style: none;
}

.home-rail__notice-list li + li {
  border-top: 1px solid var(--c-divider);
}

.home-rail__notice-item {
  display: flex;
  width: 100%;
  align-items: flex-start;
  gap: 8px;
  padding: 10px 2px;
  border: 0;
  background: transparent;
  color: var(--c-text-1);
  text-align: left;
  text-decoration: none;
  cursor: pointer;
}

.home-rail__notice-item:hover strong {
  color: var(--c-primary);
}

.home-rail__notice-body {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.home-rail__notice-body strong {
  font-size: 13px;
  font-weight: 600;
  line-height: 1.45;
  transition: color 0.14s ease;
}

.home-rail__notice-body small {
  font-size: 11px;
  color: var(--c-text-3);
}

.home-empty {
  border-top: 1px solid var(--c-border);
  color: var(--c-text-3);
  font-size: 14px;
  padding: 48px 20px;
  text-align: center;
}

.home-directory__item:focus-visible,
.home-focal-card__action:focus-visible,
.home-chip:focus-visible {
  outline: 2px solid var(--c-primary);
  outline-offset: 2px;
}

@media (max-width: 1023px) {
  .home-broadside {
    grid-template-columns: 1fr;
    gap: 24px;
  }

  .home-rail {
    position: static;
  }
}

@media (max-width: 639px) {
  .home-broadside__main {
    gap: 20px;
  }

  .home-focal-card {
    padding: 16px;
  }

  .home-today__item {
    grid-template-columns: 76px minmax(0, 1fr);
  }

  .home-directory__list {
    grid-template-columns: 1fr;
  }
}

@media (prefers-reduced-motion: reduce) {
  .home-directory__item,
  .home-directory__arrow,
  .home-focal-card__action,
  .home-chip {
    transition: none;
  }
}
</style>
