<script setup>
import { useRouter } from 'vue-router'
import { onMounted, onActivated, ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { ALL_FEATURES, getLocalizedFeatures } from '@/constants/features'
import { getSchedule } from '@/api/schedule'
import { useLatestRequest } from '@/composables/useLatestRequest'
import { selectTodayCourses, todayColumn } from './homeToday'
import {
  ArrowRight, Calendar, CalendarCheck, Database, DoorOpen,
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

// 今日课程：静默读取课表（后端有缓存），失败时只给出入口，不弹全局错误
const todayState = ref('loading') // loading | ready | unavailable
const todayCourses = ref([])
const todayRequest = useLatestRequest()

async function loadTodayCourses() {
  const task = todayRequest.begin()
  if (todayCourses.value.length === 0) todayState.value = 'loading'
  try {
    const res = await getSchedule(undefined, { signal: task.signal, skipErrorTip: true })
    if (!task.isCurrent()) return
    if (res && res.success && res.data) {
      todayCourses.value = selectTodayCourses(res.data.scheduleList, res.data.week ?? null, todayColumn())
      todayState.value = 'ready'
    } else {
      todayState.value = 'unavailable'
    }
  } catch (error) {
    if (task.isCurrent() && !error?.cancelled) todayState.value = 'unavailable'
  }
}
onMounted(loadTodayCourses)
onActivated(loadTodayCourses)

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
      items: serviceItems
    },
    {
      id: 'life',
      title: t('home.sectionLife'),
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
    return new Intl.DateTimeFormat(locale.value, { month: 'long', day: 'numeric', weekday: 'long' }).format(new Date())
  } catch (_) {
    return ''
  }
})
</script>

<template>
  <div class="home-page">
    <header class="home-hero">
      <p v-if="todayLabel" class="home-hero__date">{{ todayLabel }}</p>
      <h1 class="home-hero__title">{{ $t(greetingKey) }}</h1>
    </header>

    <div class="home-today-grid">
      <section class="campus-page-card home-today" aria-labelledby="home-today-title" :aria-busy="todayState === 'loading'">
        <header class="home-today__header">
          <div class="home-today__heading">
            <span class="home-today__icon" aria-hidden="true">
              <CalendarCheck class="w-5 h-5" />
            </span>
            <div>
              <h2 id="home-today-title">{{ $t('home.todayScheduleTitle') }}</h2>
              <p v-if="todayState === 'ready' && todayCourses.length">{{ $t('home.todayCourseCount', { count: todayCourses.length }) }}</p>
            </div>
          </div>
          <button type="button" class="home-today__action campus-press" @click="goTo('/schedule')">
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
          <li v-for="course in todayCourses" :key="course.key" class="home-today__item">
            <span class="home-today__lesson">{{ course.lesson }}</span>
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

    </div>

    <section
      v-for="section in featureSections"
      :id="`home-${section.id}`"
      :key="section.id"
      class="campus-page-card feature-section"
    >
      <header class="feature-section__header">
        <div class="feature-section__title">
          <h2>{{ section.title }}</h2>
        </div>
      </header>

      <div class="feature-section__grid">
        <button
          v-for="(item, index) in section.items"
          :key="item.id || item.path || item.key || index"
          :aria-label="item.title"
          class="feature-card campus-press"
          @click="handleMenuClick(item)"
        >
          <span class="feature-card__icon" aria-hidden="true">
            <component :is="resolveFeatureIcon(item.id)" class="w-6 h-6" />
          </span>
          <span class="feature-card__body">
            <strong>{{ item.title }}</strong>
          </span>
          <ArrowRight class="feature-card__arrow" aria-hidden="true" />
        </button>
      </div>
    </section>

    <div v-if="featureSections.length === 0" class="campus-page-card home-empty">
      {{ $t('home.noFeatures') }}
    </div>
  </div>
</template>

<style scoped>
.home-page {
  display: flex;
  flex-direction: column;
  gap: var(--space-20, 20px);
}

/* Greeting: plain header, hierarchy by weight and whitespace */
.home-hero {
  padding: var(--space-lg, 16px) 4px 0;
}

.home-hero__date {
  margin: 0 0 6px;
  color: var(--c-primary);
  font-size: 13px;
  font-weight: 600;
}

.home-hero__title {
  margin: 0;
  color: var(--c-text-1);
  font-size: clamp(26px, 3.4vw, 34px);
  font-weight: 700;
  letter-spacing: -0.01em;
  line-height: 1.2;
}

.home-hero__subtitle {
  margin: 8px 0 0;
  color: var(--c-text-2);
  font-size: 14px;
  line-height: 1.6;
}

/* Today: real courses for today */
.home-today-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr);
  gap: var(--space-lg, 16px);
  align-items: start;
}

.home-today {
  display: flex;
  flex-direction: column;
  gap: var(--space-md, 12px);
  padding: var(--space-lg, 16px) var(--space-xl, 24px);
}

.home-today__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-md, 12px);
}

.home-today__heading {
  display: flex;
  align-items: center;
  gap: var(--space-md, 12px);
  min-width: 0;
}

.home-today__icon {
  display: grid;
  width: 44px;
  height: 44px;
  flex: none;
  place-items: center;
  border-radius: var(--radius-control, 10px);
  background: var(--c-primary-soft);
  color: var(--c-primary);
}

.home-today__heading h2 {
  margin: 0;
  color: var(--c-text-1);
  font-size: 16px;
  font-weight: 650;
}

.home-today__heading p {
  margin: 2px 0 0;
  color: var(--c-text-2);
  font-size: 13px;
}

.home-today__action {
  display: inline-flex;
  min-height: 40px;
  flex: none;
  align-items: center;
  gap: 4px;
  border: 0;
  border-radius: var(--radius-control, 10px);
  background: var(--c-primary);
  color: var(--c-on-primary);
  cursor: pointer;
  font: inherit;
  font-size: 14px;
  font-weight: 600;
  padding: 0 var(--space-md, 12px);
}

.home-today__action:hover {
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
  display: grid;
  grid-template-columns: 88px minmax(0, 1fr);
  align-items: center;
  gap: var(--space-md, 12px);
  border-top: 1px solid var(--c-divider);
  padding: 12px 0;
}

.home-today__lesson {
  color: var(--c-primary-hover);
  font-size: 13px;
  font-weight: 600;
  font-variant-numeric: tabular-nums;
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
  font-size: 13px;
}

.home-today__empty {
  margin: 0;
  border-top: 1px solid var(--c-divider);
  color: var(--c-text-2);
  font-size: 14px;
  padding: 14px 0 4px;
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

/* Feature sections: one flat card per group, brand-primary icons only */
.feature-section {
  padding: var(--space-xl, 24px);
  scroll-margin-top: 88px;
}

.feature-section__title {
  display: flex;
  align-items: center;
  gap: 10px;
}

.feature-section__title h2 {
  margin: 0;
  color: var(--c-text-1);
  font-size: 18px;
  font-weight: 650;
}

.feature-section__count {
  display: inline-grid;
  min-width: 24px;
  height: 20px;
  place-items: center;
  border-radius: 999px;
  background: var(--c-primary-soft);
  color: var(--c-primary-hover);
  font-size: 12px;
  font-weight: 600;
  padding: 0 7px;
}

.feature-section__header p {
  margin: 6px 0 0;
  color: var(--c-text-2);
  font-size: 13px;
  line-height: 1.55;
}

.feature-section__grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: var(--space-md, 12px);
  margin-top: var(--space-lg, 16px);
}

.feature-card {
  display: grid;
  grid-template-columns: 44px minmax(0, 1fr) 16px;
  align-items: center;
  gap: 12px;
  min-height: 72px;
  border: 1px solid var(--c-border);
  border-radius: var(--radius-control, 10px);
  background: var(--c-surface);
  color: inherit;
  cursor: pointer;
  font: inherit;
  padding: 12px 12px 12px 14px;
  text-align: left;
}

.feature-card:hover {
  border-color: color-mix(in srgb, var(--c-primary) 40%, var(--c-border));
  background: var(--c-surface-hover);
}

.feature-card__icon {
  display: grid;
  width: 44px;
  height: 44px;
  place-items: center;
  border-radius: var(--radius-control, 10px);
  background: var(--c-primary-soft);
  color: var(--c-primary);
}

.feature-card__body {
  min-width: 0;
}

.feature-card__body strong {
  display: block;
  color: var(--c-text-1);
  font-size: 14px;
  font-weight: 650;
}

.feature-card__body small {
  display: block;
  overflow: hidden;
  margin-top: 2px;
  color: var(--c-text-2);
  font-size: 12px;
  line-height: 1.4;
  text-overflow: ellipsis;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  display: -webkit-box;
}

.feature-card__arrow {
  width: 16px;
  height: 16px;
  color: var(--c-text-3);
}

.home-empty {
  color: var(--c-text-3);
  font-size: 14px;
  padding: 48px 20px;
  text-align: center;
}

.feature-card:focus-visible,
.home-today__action:focus-visible {
  outline: 2px solid var(--c-primary);
  outline-offset: 2px;
}

@media (max-width: 767px) {
  .home-page {
    gap: var(--space-md, 12px);
  }

  .home-hero {
    padding-top: var(--space-sm, 8px);
  }

  .home-today-grid {
    grid-template-columns: minmax(0, 1fr);
    gap: var(--space-md, 12px);
  }

  .home-today {
    padding: var(--space-md, 12px) var(--space-lg, 16px);
  }

  .home-today__item {
    grid-template-columns: 72px minmax(0, 1fr);
  }

  .feature-section {
    padding: var(--space-lg, 16px);
  }

  .feature-section__grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: var(--space-sm, 8px);
  }

  .feature-card {
    grid-template-columns: minmax(0, 1fr);
    justify-items: center;
    gap: 8px;
    min-height: 96px;
    padding: 12px 6px;
    text-align: center;
  }

  .feature-card__body small,
  .feature-card__arrow {
    display: none;
  }
}

@media (prefers-reduced-motion: reduce) {
  .feature-card,
  .home-today__action {
    transition: none;
  }
}
</style>
