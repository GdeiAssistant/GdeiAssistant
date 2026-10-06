<script setup>
import { useRouter } from 'vue-router'
import { onMounted, onActivated, ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { ALL_FEATURES, FEATURE_ICON_SRC, getLocalizedFeatures } from '@/constants/features'
import {
  ArrowRight, Bell, Calendar, CalendarCheck, Database, DoorOpen,
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

function scrollToSection(id) {
  const section = document.getElementById(`home-${id}`)
  const reduceMotion = typeof window !== 'undefined'
    && window.matchMedia?.('(prefers-reduced-motion: reduce)').matches
  section?.scrollIntoView({ behavior: reduceMotion ? 'auto' : 'smooth', block: 'start' })
}

// Soft per-module accents used only for icon tiles on the home page.
// [light icon, dark icon]; primary actions stay brand emerald.
const ACCENTS = {
  grade: ['#0E8F6E', '#5AD6AE'],
  schedule: ['#0F8A7E', '#63D4C0'],
  cet: ['#4A6FB5', '#9DBDEA'],
  kaoyan: ['#2F7F99', '#8ED3DF'],
  spare: ['#1E8C8F', '#7DD8D6'],
  collection: ['#3E74AE', '#96BAE4'],
  card: ['#2E7FA0', '#84CBDE'],
  pe: ['#B85F45', '#E9A990'],
  data: ['#1F8378', '#76D7CC'],
  evaluate: ['#B45A70', '#E8A9B8'],
  ershou: ['#0E8F6E', '#5AD6AE'],
  delivery: ['#B8661A', '#E8B06A'],
  lostandfound: ['#3B74B8', '#8FC0EC'],
  secret: ['#7656BA', '#BBA6EE'],
  dating: ['#1E8C8F', '#7DD8D6'],
  express: ['#BE4C77', '#EDA2BE'],
  topic: ['#2C7FAE', '#86C6E6'],
  photograph: ['#A8731C', '#E3C17D'],
  notice: ['#0F8A7E', '#63D4C0'],
}

function accentStyle(id) {
  const [light, dark] = ACCENTS[id] || ['#4E5F5A', '#A3B3AE']
  return { '--accent': light, '--accent-dark': dark }
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
    <section class="home-hero-grid" aria-labelledby="home-title">
      <article class="home-hero-card">
        <div class="home-hero-card__shade" aria-hidden="true" />
        <div class="home-hero-card__content">
          <p v-if="todayLabel" class="home-hero-card__date">{{ todayLabel }}</p>
          <h1 id="home-title">{{ $t('home.title') }}</h1>
          <p class="home-hero-card__subtitle">{{ $t('home.subtitle') }}</p>
          <div class="home-hero-card__actions">
            <button type="button" class="home-hero-card__primary" @click="scrollToSection('service')">
              {{ $t('home.heroPrimary') }}
            </button>
            <button type="button" class="home-hero-card__secondary" @click="scrollToSection('life')">
              {{ $t('home.heroSecondary') }}
            </button>
          </div>
        </div>
      </article>

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
            :style="accentStyle(item.id)"
            @click="goTo(item.path)"
          >
            <span class="today-panel__icon" aria-hidden="true">
              <component :is="item.icon" class="w-5 h-5" />
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
          <div class="feature-section__title">
            <h2>{{ section.title }}</h2>
            <span class="feature-section__count">{{ section.items.length }}</span>
          </div>
          <p>{{ section.description }}</p>
        </header>

        <div class="feature-section__grid">
          <button
            v-for="(item, index) in section.items"
            :key="item.id || item.path || item.key || index"
            :aria-label="item.title"
            class="feature-card"
            :style="accentStyle(item.id)"
            @click="handleMenuClick(item)"
          >
            <span class="feature-card__icon" aria-hidden="true">
              <component :is="resolveFeatureIcon(item.id)" class="w-6 h-6" />
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
/* Home-only glass language: photo hero, frosted panels, tinted icon tiles.
   Tokens are scoped to the page so the shell keeps its own look. */
.home-page {
  --glass-bg: rgb(255 255 255 / 74%);
  --glass-bg-strong: rgb(255 255 255 / 86%);
  --glass-bg-solid: #FFFFFF;
  --glass-border: rgb(204 224 218 / 78%);
  --glass-shadow: 0 1px 2px rgb(17 52 43 / 4%), 0 18px 44px rgb(20 62 52 / 8%);
  --tile-bg: rgb(255 255 255 / 72%);
  --tile-bg-hover: rgb(255 255 255 / 94%);
  --tile-border: rgb(210 226 221 / 84%);
  --hero-radius: 28px;
  --panel-radius: 26px;
  --tile-radius: 18px;

  position: relative;
  isolation: isolate;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* Soft mint-to-white wash behind the home content; fades out at every edge */
.home-page::before {
  content: '';
  position: absolute;
  inset: 0;
  z-index: -1;
  pointer-events: none;
  background:
    radial-gradient(60% 38% at 12% 4%, rgb(186 236 217 / 42%), transparent 70%),
    radial-gradient(48% 30% at 92% 16%, rgb(196 226 246 / 36%), transparent 72%),
    radial-gradient(56% 34% at 70% 78%, rgb(214 240 228 / 34%), transparent 74%);
}

.home-hero-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.68fr) minmax(320px, 0.82fr);
  gap: 20px;
  align-items: stretch;
}

.today-panel,
.feature-section,
.home-empty {
  border: 1px solid var(--glass-border);
  background: var(--glass-bg);
  box-shadow: var(--glass-shadow);
  -webkit-backdrop-filter: blur(16px) saturate(1.1);
  backdrop-filter: blur(16px) saturate(1.1);
}

/* Hero: campus photo with a frosted panel on the left */
.home-hero-card {
  position: relative;
  min-height: 392px;
  overflow: hidden;
  border: 1px solid var(--glass-border);
  border-radius: var(--hero-radius);
  background-color: #DCEBE5;
  background-image: url('/img/landing/campus-hero.jpg');
  background-position: 58% center;
  background-size: cover;
  box-shadow: var(--glass-shadow);
}

.home-hero-card__shade {
  position: absolute;
  inset: 0;
  background:
    linear-gradient(90deg, rgb(255 255 255 / 90%) 0%, rgb(255 255 255 / 66%) 34%, rgb(255 255 255 / 14%) 66%, rgb(255 255 255 / 0%) 100%),
    linear-gradient(180deg, rgb(255 255 255 / 0%), rgb(220 246 237 / 20%));
}

.home-hero-card__content {
  position: relative;
  z-index: 1;
  display: flex;
  max-width: 540px;
  min-height: 326px;
  flex-direction: column;
  justify-content: center;
  margin: 32px;
  padding: 34px 36px;
  border: 1px solid rgb(255 255 255 / 72%);
  border-radius: 24px;
  background:
    linear-gradient(145deg, rgb(255 255 255 / 76%), rgb(255 255 255 / 50%)),
    radial-gradient(circle at top left, rgb(196 241 224 / 30%), transparent 40%);
  box-shadow: 0 24px 52px rgb(20 62 52 / 12%);
  -webkit-backdrop-filter: blur(18px) saturate(1.15);
  backdrop-filter: blur(18px) saturate(1.15);
}

.home-hero-card__date {
  margin: 0 0 14px;
  color: var(--c-primary-hover);
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.02em;
}

.home-hero-card h1 {
  margin: 0;
  color: var(--c-text-1);
  font-size: clamp(40px, 5.4vw, 64px);
  font-weight: 900;
  letter-spacing: -0.045em;
  line-height: 1.04;
}

.home-hero-card__subtitle {
  max-width: 420px;
  margin: 16px 0 0;
  color: var(--c-text-2);
  font-size: 17px;
  font-weight: 600;
  line-height: 1.7;
}

.home-hero-card__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 30px;
}

.home-hero-card__primary,
.home-hero-card__secondary {
  min-height: 48px;
  border-radius: 14px;
  cursor: pointer;
  font: inherit;
  font-size: 15px;
  font-weight: 800;
  padding: 0 22px;
  transition: transform 0.18s ease, box-shadow 0.18s ease, border-color 0.18s ease, background-color 0.18s ease;
}

.home-hero-card__primary {
  border: 0;
  background: linear-gradient(135deg, #0C8566, #0A7559);
  color: #FFFFFF;
  box-shadow: 0 14px 26px rgb(14 143 110 / 26%);
}

.home-hero-card__secondary {
  border: 1px solid color-mix(in srgb, var(--c-primary) 26%, var(--glass-border));
  background: rgb(255 255 255 / 84%);
  color: var(--c-primary-hover);
}

.home-hero-card__primary:hover,
.home-hero-card__secondary:hover {
  transform: translateY(-2px);
}

.home-hero-card__primary:hover {
  box-shadow: 0 18px 32px rgb(14 143 110 / 32%);
}

/* Today panel: each shortcut is its own small card */
.today-panel {
  display: flex;
  flex-direction: column;
  border-radius: var(--panel-radius);
  padding: 22px;
}

.today-panel__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.today-panel__header h2,
.feature-section__title h2 {
  margin: 0;
  color: var(--c-text-1);
  font-size: 21px;
  font-weight: 850;
  letter-spacing: -0.02em;
}

.today-panel__header button {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  min-height: 32px;
  border: 0;
  background: transparent;
  color: var(--c-text-2);
  cursor: pointer;
  font: inherit;
  font-size: 13px;
  font-weight: 700;
  padding: 0;
}

.today-panel__header button:hover {
  color: var(--c-primary-hover);
}

.today-panel__list {
  display: flex;
  flex: 1;
  flex-direction: column;
  justify-content: space-between;
  gap: 12px;
  margin-top: 16px;
}

.today-panel__item {
  display: grid;
  grid-template-columns: 44px minmax(0, 1fr) auto;
  align-items: center;
  gap: 13px;
  min-height: 74px;
  border: 1px solid var(--tile-border);
  border-radius: var(--tile-radius);
  background: var(--tile-bg);
  color: inherit;
  cursor: pointer;
  font: inherit;
  padding: 12px 14px 12px 12px;
  text-align: left;
  transition: transform 0.18s ease, border-color 0.18s ease, box-shadow 0.18s ease, background-color 0.18s ease;
}

.today-panel__item:hover {
  border-color: color-mix(in srgb, var(--accent) 30%, var(--tile-border));
  background: var(--tile-bg-hover);
  box-shadow: 0 14px 28px rgb(20 62 52 / 8%);
  transform: translateY(-2px);
}

.today-panel__icon,
.feature-card__icon {
  display: grid;
  place-items: center;
  color: var(--accent);
  background: color-mix(in srgb, var(--accent) 12%, transparent);
}

.today-panel__icon {
  width: 44px;
  height: 44px;
  border-radius: 14px;
}

.today-panel__text,
.feature-card__body {
  min-width: 0;
}

.today-panel__text strong,
.feature-card__body strong {
  display: block;
  color: var(--c-text-1);
  font-size: 15px;
  font-weight: 800;
}

.today-panel__text small,
.feature-card__body small {
  display: block;
  overflow: hidden;
  margin-top: 3px;
  color: var(--c-text-2);
  font-size: 12px;
  text-overflow: ellipsis;
}

.today-panel__text small {
  white-space: nowrap;
}

.today-panel__meta {
  color: color-mix(in srgb, var(--accent) 78%, var(--c-text-1));
  font-size: 12px;
  font-weight: 800;
  white-space: nowrap;
}

/* Feature sections */
.feature-sections-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 20px;
}

.feature-section {
  border-radius: var(--panel-radius);
  padding: 22px;
  scroll-margin-top: 88px;
}

.feature-section--service {
  background:
    radial-gradient(circle at 100% 0, rgb(155 216 255 / 18%), transparent 36%),
    linear-gradient(135deg, rgb(255 255 255 / 88%), rgb(241 255 250 / 76%));
}

.feature-section--life {
  background:
    radial-gradient(circle at 0 0, rgb(251 191 36 / 10%), transparent 30%),
    radial-gradient(circle at 100% 100%, rgb(96 165 250 / 12%), transparent 36%),
    linear-gradient(135deg, rgb(255 255 255 / 90%), rgb(253 249 255 / 78%));
}

.feature-section__title {
  display: flex;
  align-items: center;
  gap: 10px;
}

.feature-section__count {
  display: inline-grid;
  min-width: 26px;
  height: 22px;
  place-items: center;
  border-radius: 999px;
  background: var(--c-primary-soft);
  color: var(--c-primary-hover);
  font-size: 12px;
  font-weight: 750;
  padding: 0 8px;
}

.feature-section__header p {
  max-width: 52ch;
  margin: 6px 0 0;
  color: var(--c-text-2);
  font-size: 14px;
  line-height: 1.55;
}

.feature-section__grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(190px, 1fr));
  gap: 12px;
  margin-top: 18px;
}

.feature-card {
  display: grid;
  grid-template-columns: 48px minmax(0, 1fr) 18px;
  align-items: center;
  gap: 14px;
  min-height: 84px;
  border: 1px solid var(--tile-border);
  border-radius: var(--tile-radius);
  background: var(--tile-bg);
  color: inherit;
  cursor: pointer;
  font: inherit;
  padding: 14px 14px 14px 16px;
  text-align: left;
  transition: transform 0.18s ease, border-color 0.18s ease, box-shadow 0.18s ease, background-color 0.18s ease;
}

.feature-card:hover {
  border-color: color-mix(in srgb, var(--accent) 28%, var(--tile-border));
  background: var(--tile-bg-hover);
  box-shadow: 0 16px 30px rgb(20 62 52 / 8%);
  transform: translateY(-2px);
}

.feature-card__icon {
  width: 48px;
  height: 48px;
  border-radius: 16px;
}

.feature-card__body small {
  display: -webkit-box;
  line-height: 1.4;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.feature-card__arrow {
  width: 17px;
  height: 17px;
  color: var(--accent);
  opacity: 0.5;
  transition: opacity 0.18s ease, transform 0.18s ease;
}

.feature-card:hover .feature-card__arrow {
  opacity: 0.9;
  transform: translateX(2px);
}

.today-panel__item:focus-visible,
.feature-card:focus-visible,
.home-hero-card__primary:focus-visible,
.home-hero-card__secondary:focus-visible,
.today-panel__header button:focus-visible {
  outline: 2px solid var(--c-primary);
  outline-offset: 2px;
}

.home-empty {
  border-style: dashed;
  border-radius: var(--panel-radius);
  color: var(--c-text-3);
  font-size: 14px;
  padding: 48px 20px;
  text-align: center;
}

/* No backdrop-filter: make glass surfaces opaque enough to stay legible */
@supports not ((backdrop-filter: blur(1px)) or (-webkit-backdrop-filter: blur(1px))) {
  .home-page {
    --glass-bg: var(--glass-bg-solid);
    --tile-bg: var(--glass-bg-solid);
  }

  .home-hero-card__content {
    background: var(--glass-bg-strong);
  }

  .feature-section--service,
  .feature-section--life {
    background: var(--glass-bg-solid);
  }
}

@media (max-width: 1180px) {
  .home-hero-grid,
  .feature-sections-grid {
    grid-template-columns: minmax(0, 1fr);
  }

  .today-panel__list {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .feature-section__grid {
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  }
}

@media (max-width: 767px) {
  .home-page {
    --hero-radius: 22px;
    --panel-radius: 20px;
    --tile-radius: 16px;
    gap: 12px;
  }

  .home-hero-grid,
  .feature-sections-grid {
    gap: 12px;
  }

  .home-hero-card {
    min-height: 300px;
    background-position: 62% center;
  }

  .home-hero-card__shade {
    background:
      linear-gradient(180deg, rgb(255 255 255 / 12%) 0%, rgb(255 255 255 / 56%) 50%, rgb(255 255 255 / 88%) 100%),
      linear-gradient(90deg, rgb(255 255 255 / 60%), rgb(255 255 255 / 6%));
  }

  .home-hero-card__content {
    min-height: 0;
    justify-content: flex-end;
    margin: 128px 12px 12px;
    padding: 18px 16px;
    border-radius: 18px;
  }

  .home-hero-card__date {
    margin-bottom: 8px;
    font-size: 12px;
  }

  .home-hero-card h1 {
    font-size: clamp(30px, 9.4vw, 40px);
  }

  .home-hero-card__subtitle {
    margin-top: 8px;
    font-size: 14px;
    line-height: 1.55;
  }

  .home-hero-card__actions {
    gap: 8px;
    margin-top: 14px;
  }

  .home-hero-card__primary,
  .home-hero-card__secondary {
    flex: 1 1 0;
    min-height: 44px;
    border-radius: 12px;
    font-size: 14px;
    padding: 0 10px;
  }

  .today-panel,
  .feature-section {
    padding: 14px;
  }

  .today-panel__header h2,
  .feature-section__title h2 {
    font-size: 18px;
  }

  .today-panel__list {
    gap: 10px;
    margin-top: 12px;
  }

  .today-panel__item {
    display: flex;
    min-height: 88px;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 6px;
    padding: 10px 6px;
    text-align: center;
  }

  .today-panel__icon {
    width: 38px;
    height: 38px;
    border-radius: 12px;
  }

  .today-panel__text strong {
    font-size: 13px;
  }

  .today-panel__text small,
  .today-panel__meta {
    display: none;
  }

  .feature-section__header p {
    display: -webkit-box;
    overflow: hidden;
    margin-top: 4px;
    font-size: 12px;
    line-height: 1.5;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 2;
  }

  .feature-section__grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: 10px;
    margin-top: 12px;
  }

  .feature-card {
    min-height: 104px;
    grid-template-columns: minmax(0, 1fr);
    justify-items: center;
    gap: 8px;
    padding: 12px 6px;
    text-align: center;
  }

  .feature-card__icon {
    width: 48px;
    height: 48px;
    border-radius: 16px;
  }

  .feature-card__body strong {
    font-size: 13px;
  }

  .feature-card__body small,
  .feature-card__arrow {
    display: none;
  }
}

@media (prefers-reduced-motion: reduce) {
  .home-hero-card__primary,
  .home-hero-card__secondary,
  .today-panel__item,
  .feature-card,
  .feature-card__arrow {
    transition: none;
  }

  .home-hero-card__primary:hover,
  .home-hero-card__secondary:hover,
  .today-panel__item:hover,
  .feature-card:hover,
  .feature-card:hover .feature-card__arrow {
    transform: none;
  }
}

/* ── Dark ─────────────────────────────────────────────── */
[data-theme="dark"] .home-page {
  --glass-bg: rgb(21 31 28 / 78%);
  --glass-bg-strong: rgb(18 27 24 / 92%);
  --glass-bg-solid: #151E1B;
  --glass-border: rgb(86 112 104 / 34%);
  --glass-shadow: 0 1px 2px rgb(0 0 0 / 24%), 0 22px 54px rgb(0 0 0 / 30%);
  --tile-bg: rgb(28 39 36 / 76%);
  --tile-bg-hover: rgb(34 47 43 / 92%);
  --tile-border: rgb(72 95 88 / 46%);
}

[data-theme="dark"] .home-page::before {
  background:
    radial-gradient(60% 38% at 12% 4%, rgb(52 199 154 / 10%), transparent 70%),
    radial-gradient(48% 30% at 92% 16%, rgb(96 165 250 / 8%), transparent 72%),
    radial-gradient(56% 34% at 70% 78%, rgb(52 199 154 / 6%), transparent 74%);
}

[data-theme="dark"] .today-panel__item,
[data-theme="dark"] .feature-card {
  --accent: var(--accent-dark);
}

[data-theme="dark"] .today-panel__icon,
[data-theme="dark"] .feature-card__icon {
  background: color-mix(in srgb, var(--accent) 14%, rgb(28 39 36 / 90%));
}

[data-theme="dark"] .today-panel__meta {
  color: var(--accent);
}

[data-theme="dark"] .today-panel__item:hover,
[data-theme="dark"] .feature-card:hover {
  border-color: color-mix(in srgb, var(--accent) 36%, var(--tile-border));
  box-shadow: 0 14px 30px rgb(0 0 0 / 28%);
}

[data-theme="dark"] .home-hero-card {
  background-color: #12201B;
}

/* Darken the photo enough that light text on the panel clears AA */
[data-theme="dark"] .home-hero-card__shade {
  background:
    linear-gradient(90deg, rgb(8 15 13 / 78%) 0%, rgb(8 15 13 / 54%) 38%, rgb(8 15 13 / 22%) 70%, rgb(8 15 13 / 12%) 100%),
    linear-gradient(180deg, rgb(8 15 13 / 6%), rgb(8 15 13 / 30%));
}

[data-theme="dark"] .home-hero-card__content {
  border-color: rgb(120 150 140 / 26%);
  background:
    linear-gradient(145deg, rgb(16 26 23 / 78%), rgb(18 32 28 / 62%)),
    radial-gradient(circle at top left, rgb(52 199 154 / 14%), transparent 40%);
  box-shadow: 0 24px 54px rgb(0 0 0 / 34%);
}

[data-theme="dark"] .home-hero-card__date,
[data-theme="dark"] .home-hero-card__secondary,
[data-theme="dark"] .feature-section__count {
  color: var(--c-primary-hover);
}

[data-theme="dark"] .home-hero-card__primary {
  background: linear-gradient(135deg, #3FD0A3, #2BB98D);
  color: #0B1A15;
  box-shadow: 0 14px 28px rgb(0 0 0 / 30%);
}

[data-theme="dark"] .home-hero-card__secondary {
  border-color: rgb(52 199 154 / 34%);
  background: rgb(18 40 33 / 72%);
}

[data-theme="dark"] .feature-section--service {
  background:
    radial-gradient(circle at 100% 0, rgb(52 199 154 / 8%), transparent 36%),
    linear-gradient(135deg, rgb(22 33 30 / 86%), rgb(17 26 23 / 80%));
}

[data-theme="dark"] .feature-section--life {
  background:
    radial-gradient(circle at 0 0, rgb(224 165 74 / 6%), transparent 30%),
    radial-gradient(circle at 100% 100%, rgb(96 165 250 / 7%), transparent 36%),
    linear-gradient(135deg, rgb(22 33 30 / 86%), rgb(19 25 30 / 80%));
}

@supports not ((backdrop-filter: blur(1px)) or (-webkit-backdrop-filter: blur(1px))) {
  [data-theme="dark"] .feature-section--service,
  [data-theme="dark"] .feature-section--life {
    background: var(--glass-bg-solid);
  }
}

@media (max-width: 767px) {
  [data-theme="dark"] .home-hero-card__shade {
    background:
      linear-gradient(180deg, rgb(8 15 13 / 18%) 0%, rgb(8 15 13 / 58%) 52%, rgb(8 15 13 / 86%) 100%);
  }
}
</style>
