<script setup>
import { useRouter } from 'vue-router'
import { onMounted, onActivated, ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { ALL_FEATURES, FEATURE_ICON_SRC, getLocalizedFeatures } from '@/constants/features'
import {
  ArrowRight, Bell, Calendar, CalendarCheck, CreditCard, Database, DoorOpen,
  Dumbbell, Eye, FileText, GraduationCap, Heart, Info as InfoIcon, MessageCircle,
  PackageCheck, PenLine, Search, ShoppingCart, Star, Truck, Users, BookOpen,
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

const visibleMenuList = computed(() => {
  const config = featuresConfig.value
  return localizedFeatures.value.filter((f) => {
    if (!config || Object.keys(config).length === 0) return f.defaultVisible !== false
    return config[f.id] !== false
  }).map((f) => ({
    id: f.id,
    title: f.name,
    description: f.description,
    icon: FEATURE_ICON_SRC[f.id] || '/img/function/data.png',
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

const todayItems = computed(() => [
  {
    id: 'schedule',
    title: t('home.todayScheduleTitle'),
    description: t('home.todayScheduleDesc'),
    meta: t('home.todayScheduleMeta'),
    icon: CalendarCheck,
    path: '/schedule'
  },
  {
    id: 'card',
    title: t('home.todayCardTitle'),
    description: t('home.todayCardDesc'),
    meta: t('home.todayCardMeta'),
    icon: WalletCards,
    path: '/card'
  },
  {
    id: 'notice',
    title: t('home.todayNoticeTitle'),
    description: t('home.todayNoticeDesc'),
    meta: t('home.todayNoticeMeta'),
    icon: Bell,
    path: '/info'
  },
  {
    id: 'delivery',
    title: t('home.todayDeliveryTitle'),
    description: t('home.todayDeliveryDesc'),
    meta: t('home.todayDeliveryMeta'),
    icon: PackageCheck,
    path: '/delivery'
  }
])

const iconMap = {
  grade: Star, schedule: Calendar, card: CreditCard, cet: FileText,
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

function scrollToSection(id) {
  const section = document.getElementById(`home-${id}`)
  section?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

function resolveFeatureIcon(id) {
  return iconMap[id] || InfoIcon
}

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
    <section class="home-intro" aria-labelledby="home-title">
      <div class="home-intro__lead">
        <p v-if="todayLabel" class="home-intro__date">{{ todayLabel }}</p>
        <h1 id="home-title">{{ $t('home.title') }}</h1>
        <p class="home-intro__subtitle">{{ $t('home.subtitle') }}</p>
        <div class="home-intro__actions">
          <button type="button" class="home-btn home-btn--primary" @click="scrollToSection('service')">
            {{ $t('home.heroPrimary') }}
          </button>
          <button type="button" class="home-btn home-btn--ghost" @click="scrollToSection('life')">
            {{ $t('home.heroSecondary') }}
          </button>
        </div>
      </div>

      <aside class="today-panel" aria-labelledby="today-title">
        <div class="today-panel__header">
          <h2 id="today-title">{{ $t('home.todayTitle') }}</h2>
          <button type="button" @click="goTo('/info')">
            {{ $t('home.todayMore') }}
            <ArrowRight class="w-4 h-4" aria-hidden="true" />
          </button>
        </div>

        <div class="today-panel__list">
          <button
            v-for="item in todayItems"
            :key="item.id"
            type="button"
            class="today-panel__item"
            @click="goTo(item.path)"
          >
            <span class="today-panel__icon" aria-hidden="true">
              <component :is="item.icon" class="w-[18px] h-[18px]" />
            </span>
            <span class="today-panel__text">
              <strong>{{ item.title }}</strong>
              <small>{{ item.description }}</small>
            </span>
            <span class="today-panel__meta">{{ item.meta }}</span>
          </button>
        </div>
      </aside>
    </section>

    <div class="feature-sections-grid">
      <section
        v-for="section in featureSections"
        :id="`home-${section.id}`"
        :key="section.id"
        class="feature-section"
        :class="`feature-section--${section.id}`"
      >
        <header class="feature-section__header">
          <h2>{{ section.title }}</h2>
          <p>{{ section.description }}</p>
          <span class="feature-section__count">{{ section.items.length }}</span>
        </header>

        <div class="feature-section__grid">
          <button
            v-for="(item, index) in section.items"
            :key="item.id || item.path || item.key || index"
            :aria-label="item.title"
            class="feature-card"
            @click="handleMenuClick(item)"
          >
            <span class="feature-card__icon" aria-hidden="true">
              <component :is="resolveFeatureIcon(item.id)" class="w-5 h-5" />
            </span>
            <span class="feature-card__body">
              <strong>{{ item.title }}</strong>
              <small>{{ item.description }}</small>
            </span>
            <ArrowRight class="feature-card__arrow" aria-hidden="true" />
          </button>
        </div>
      </section>
    </div>

    <div v-if="featureSections.length === 0" class="home-empty">
      {{ $t('home.noFeatures') }}
    </div>
  </div>
</template>

<style scoped>
.home-page {
  display: flex;
  flex-direction: column;
  gap: 40px;
}

/* Intro: left-aligned greeting with the day's shortcuts beside it */
.home-intro {
  display: grid;
  grid-template-columns: minmax(0, 1.25fr) minmax(320px, 1fr);
  gap: 40px;
  align-items: start;
  padding-bottom: 32px;
  border-bottom: 1px solid var(--c-border);
}

.home-intro__lead {
  padding-bottom: 4px;
}

.home-intro__date {
  margin: 0 0 12px;
  color: var(--c-primary);
  font-size: 13px;
  font-weight: 600;
}

.home-intro h1 {
  margin: 0;
  color: var(--c-text-1);
  font-size: clamp(30px, 4vw, 44px);
  font-weight: 700;
  letter-spacing: -0.01em;
  line-height: 1.15;
}

.home-intro__subtitle {
  max-width: 440px;
  margin: 12px 0 0;
  color: var(--c-text-2);
  font-size: 16px;
  line-height: 1.7;
}

.home-intro__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 24px;
}

.home-btn {
  display: inline-flex;
  min-height: 44px;
  align-items: center;
  justify-content: center;
  padding: 0 18px;
  border-radius: var(--radius-control);
  font: inherit;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.15s ease, border-color 0.15s ease, color 0.15s ease;
}

.home-btn--primary {
  border: 1px solid var(--c-primary);
  background: var(--c-primary);
  color: var(--c-on-primary);
}

.home-btn--primary:hover {
  border-color: var(--c-primary-hover);
  background: var(--c-primary-hover);
}

.home-btn--ghost {
  border: 1px solid var(--c-border);
  background: var(--c-surface);
  color: var(--c-text-1);
}

.home-btn--ghost:hover {
  border-color: color-mix(in srgb, var(--c-primary) 45%, var(--c-border));
  color: var(--c-primary);
}

.today-panel {
  overflow: hidden;
  border: 1px solid var(--c-border);
  border-radius: var(--radius-card);
  background: var(--c-surface);
  box-shadow: var(--shadow-sm);
}

.today-panel__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 14px 16px 10px 18px;
}

.today-panel__header h2 {
  margin: 0;
  color: var(--c-text-1);
  font-size: 15px;
  font-weight: 650;
}

.today-panel__header button {
  display: inline-flex;
  min-height: 32px;
  align-items: center;
  gap: 4px;
  padding: 0 8px;
  border: 0;
  border-radius: 8px;
  background: transparent;
  color: var(--c-text-2);
  font: inherit;
  font-size: 13px;
  cursor: pointer;
}

.today-panel__header button:hover {
  color: var(--c-primary);
}

.today-panel__list {
  display: flex;
  flex-direction: column;
}

.today-panel__item {
  display: grid;
  grid-template-columns: 36px minmax(0, 1fr) auto;
  gap: 12px;
  align-items: center;
  min-height: 60px;
  padding: 10px 18px;
  border: 0;
  border-top: 1px solid var(--c-divider);
  background: transparent;
  color: inherit;
  font: inherit;
  text-align: left;
  cursor: pointer;
  transition: background-color 0.15s ease;
}

.today-panel__item:hover {
  background: var(--c-surface-hover);
}

.today-panel__icon {
  display: grid;
  width: 36px;
  height: 36px;
  place-items: center;
  border-radius: 10px;
  background: var(--c-primary-soft);
  color: var(--c-primary);
}

.today-panel__text {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 2px;
}

.today-panel__text strong {
  color: var(--c-text-1);
  font-size: 14px;
  font-weight: 600;
}

.today-panel__text small {
  overflow: hidden;
  color: var(--c-text-2);
  font-size: 12px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.today-panel__meta {
  color: var(--c-primary);
  font-size: 12px;
  font-weight: 600;
  white-space: nowrap;
}

/* Feature index: section label column on the left, one bordered grid on the right */
.feature-sections-grid {
  display: flex;
  flex-direction: column;
  gap: 40px;
}

.feature-section {
  display: grid;
  grid-template-columns: 220px minmax(0, 1fr);
  gap: 32px;
  align-items: start;
  scroll-margin-top: 88px;
}

.feature-section__header {
  position: sticky;
  top: 88px;
}

.feature-section__header h2 {
  margin: 0;
  color: var(--c-text-1);
  font-size: 18px;
  font-weight: 650;
}

.feature-section__header p {
  margin: 8px 0 0;
  color: var(--c-text-2);
  font-size: 13px;
  line-height: 1.7;
}

.feature-section__count {
  display: inline-flex;
  margin-top: 14px;
  padding: 2px 10px;
  border-radius: 999px;
  background: var(--c-fill-2);
  color: var(--c-text-2);
  font-size: 12px;
  font-weight: 600;
  font-variant-numeric: tabular-nums;
}

.feature-section__grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  overflow: hidden;
  border: 1px solid var(--c-border);
  border-radius: var(--radius-card);
  background: var(--c-surface);
}

.feature-card {
  position: relative;
  display: grid;
  grid-template-columns: 40px minmax(0, 1fr) 16px;
  gap: 14px;
  align-items: center;
  min-height: 80px;
  padding: 16px 18px;
  border: 0;
  background: var(--c-surface);
  box-shadow: 1px 0 0 var(--c-divider), 0 1px 0 var(--c-divider);
  color: inherit;
  font: inherit;
  text-align: left;
  cursor: pointer;
  transition: background-color 0.15s ease;
}

.feature-card:hover {
  background: var(--c-surface-hover);
}

.feature-card:focus-visible {
  z-index: 1;
  outline-offset: -2px;
}

.feature-card__icon {
  display: grid;
  width: 40px;
  height: 40px;
  place-items: center;
  border-radius: 10px;
  background: var(--c-fill-2);
  color: var(--c-text-2);
  transition: background-color 0.15s ease, color 0.15s ease;
}

.feature-card:hover .feature-card__icon {
  background: var(--c-primary-soft);
  color: var(--c-primary);
}

.feature-card__body {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 3px;
}

.feature-card__body strong {
  color: var(--c-text-1);
  font-size: 14px;
  font-weight: 600;
}

.feature-card__body small {
  display: -webkit-box;
  overflow: hidden;
  color: var(--c-text-2);
  font-size: 12px;
  line-height: 1.5;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.feature-card__arrow {
  width: 16px;
  height: 16px;
  color: var(--c-text-3);
  transition: transform 0.15s ease, color 0.15s ease;
}

.feature-card:hover .feature-card__arrow {
  color: var(--c-primary);
  transform: translateX(2px);
}

.home-empty {
  padding: 48px 24px;
  border: 1px dashed var(--c-border);
  border-radius: var(--radius-card);
  color: var(--c-text-2);
  font-size: 14px;
  text-align: center;
}

@media (max-width: 1023px) {
  .home-intro {
    grid-template-columns: minmax(0, 1fr);
    gap: 28px;
    align-items: stretch;
  }

  .feature-section {
    grid-template-columns: minmax(0, 1fr);
    gap: 16px;
  }

  .feature-section__header {
    position: static;
    display: grid;
    grid-template-columns: minmax(0, 1fr) auto;
    column-gap: 12px;
  }

  .feature-section__header p {
    grid-column: 1 / -1;
    grid-row: 2;
  }

  .feature-section__count {
    grid-column: 2;
    grid-row: 1;
    align-self: center;
    margin-top: 0;
  }
}

@media (max-width: 767px) {
  .home-page,
  .feature-sections-grid {
    gap: 28px;
  }

  .home-intro {
    padding-bottom: 24px;
  }

  .home-intro__subtitle {
    font-size: 15px;
  }

  .home-intro__actions .home-btn {
    flex: 1 1 140px;
  }

  .feature-section__grid {
    grid-template-columns: minmax(0, 1fr);
  }

  .feature-card {
    min-height: 68px;
    padding: 12px 14px;
  }
}

@media (prefers-reduced-motion: reduce) {
  .feature-card:hover .feature-card__arrow {
    transform: none;
  }
}
</style>
