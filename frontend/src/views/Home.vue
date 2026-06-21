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
const { t } = useI18n()

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
    path: '/schedule',
    color: '#10B981'
  },
  {
    id: 'card',
    title: t('home.todayCardTitle'),
    description: t('home.todayCardDesc'),
    meta: t('home.todayCardMeta'),
    icon: WalletCards,
    path: '/card',
    color: '#3B82F6'
  },
  {
    id: 'notice',
    title: t('home.todayNoticeTitle'),
    description: t('home.todayNoticeDesc'),
    meta: t('home.todayNoticeMeta'),
    icon: Bell,
    path: '/info',
    color: '#8B5CF6'
  },
  {
    id: 'delivery',
    title: t('home.todayDeliveryTitle'),
    description: t('home.todayDeliveryDesc'),
    meta: t('home.todayDeliveryMeta'),
    icon: PackageCheck,
    path: '/delivery',
    color: '#F59E0B'
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

const iconColors = {
  grade: '#0B8F6A', schedule: '#2563EB', card: '#D97706', cet: '#7C3AED',
  kaoyan: '#0891B2', collection: '#4F46E5', spare: '#06B6D4',
  pe: '#DC2626', evaluate: '#DB2777', data: '#0D9488', about: '#6B7280',
  ershou: 'var(--c-ershou)', lostandfound: 'var(--c-lostandfound)', express: 'var(--c-express)',
  secret: 'var(--c-secret)', dating: 'var(--c-dating)', topic: 'var(--c-topic)',
  photograph: 'var(--c-photograph)', delivery: 'var(--c-delivery)',
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

function featureCardStyle(id) {
  const color = iconColors[id] || '#6B7280'
  const softColor = color.startsWith('var(')
    ? `color-mix(in srgb, ${color} 13%, transparent)`
    : `${color}17`

  return {
    '--feature-color': color,
    '--feature-color-soft': softColor
  }
}

function todayItemStyle(item) {
  return {
    '--today-color': item.color,
    '--today-color-soft': `${item.color}16`
  }
}
</script>

<template>
  <div class="home-page">
    <section class="home-hero-grid" aria-labelledby="home-title">
      <article class="home-hero-card">
        <div class="home-hero-card__shade" />
        <div class="home-hero-card__content">
          <h1 id="home-title">{{ $t('home.title') }}</h1>
          <p>{{ $t('home.subtitle') }}</p>
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
            <ArrowRight class="w-4 h-4" />
          </button>
        </div>

        <div class="today-panel__list">
          <button
            v-for="item in todayItems"
            :key="item.id"
            type="button"
            class="today-panel__item"
            :style="todayItemStyle(item)"
            @click="goTo(item.path)"
          >
            <span class="today-panel__icon">
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
          <div>
            <h2>{{ section.title }}</h2>
            <p>{{ section.description }}</p>
          </div>
        </header>

        <div class="feature-section__grid">
          <button
            v-for="(item, index) in section.items"
            :key="item.id || item.path || item.key || index"
            :aria-label="item.title"
            class="feature-card"
            :style="featureCardStyle(item.id)"
            @click="handleMenuClick(item)"
          >
            <span class="feature-card__icon">
              <component :is="resolveFeatureIcon(item.id)" class="w-6 h-6" />
            </span>
            <span class="feature-card__body">
              <strong>{{ item.title }}</strong>
              <small>{{ item.description }}</small>
            </span>
            <ArrowRight class="feature-card__arrow" />
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
  gap: 18px;
}

.home-hero-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.68fr) minmax(330px, 0.82fr);
  gap: 20px;
  align-items: stretch;
}

.home-hero-card,
.today-panel,
.feature-section {
  border: 1px solid rgba(202, 222, 226, 0.76);
  background: rgba(255, 255, 255, 0.82);
  box-shadow: 0 20px 46px rgba(33, 74, 84, 0.08);
  backdrop-filter: blur(16px);
}

.home-hero-card {
  position: relative;
  min-height: 392px;
  overflow: hidden;
  border-radius: 28px;
  background-image: url('/img/landing/campus-hero.jpg');
  background-position: 56% center;
  background-size: cover;
}

.home-hero-card__shade {
  position: absolute;
  inset: 0;
  background:
    linear-gradient(90deg, rgba(255, 255, 255, 0.94) 0%, rgba(255, 255, 255, 0.74) 34%, rgba(255, 255, 255, 0.16) 66%, rgba(255, 255, 255, 0.04) 100%),
    linear-gradient(180deg, rgba(255, 255, 255, 0.02), rgba(220, 246, 237, 0.18));
}

.home-hero-card__content {
  position: relative;
  z-index: 1;
  display: flex;
  max-width: 560px;
  min-height: 392px;
  flex-direction: column;
  justify-content: center;
  padding: 46px;
}

.home-hero-card h1 {
  margin: 0;
  color: #102033;
  font-size: clamp(42px, 6vw, 66px);
  font-weight: 900;
  letter-spacing: -0.055em;
  line-height: 1.02;
}

.home-hero-card p {
  max-width: 440px;
  margin: 18px 0 0;
  color: #3f5368;
  font-size: 18px;
  font-weight: 650;
  line-height: 1.7;
}

.home-hero-card__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
  margin-top: 34px;
}

.home-hero-card__primary,
.home-hero-card__secondary {
  min-height: 50px;
  border-radius: 16px;
  cursor: pointer;
  font: inherit;
  font-size: 15px;
  font-weight: 850;
  padding: 0 23px;
  transition: transform 0.18s ease, box-shadow 0.18s ease, border-color 0.18s ease;
}

.home-hero-card__primary:hover,
.home-hero-card__secondary:hover {
  transform: translateY(-2px);
}

.home-hero-card__primary {
  border: 0;
  background: linear-gradient(135deg, #12b981, #0f9f76);
  color: #fff;
  box-shadow: 0 16px 28px rgba(16, 185, 129, 0.26);
}

.home-hero-card__secondary {
  border: 1px solid rgba(18, 145, 104, 0.38);
  background: rgba(255, 255, 255, 0.82);
  color: #0f7a5d;
}

.today-panel {
  display: flex;
  flex-direction: column;
  border-radius: 26px;
  padding: 22px;
}

.today-panel__header,
.feature-section__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.today-panel__header h2,
.feature-section__header h2 {
  margin: 0;
  color: var(--c-text-1);
  font-size: 22px;
  font-weight: 900;
  letter-spacing: -0.025em;
}

.today-panel__header button {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  border: 0;
  background: transparent;
  color: var(--c-text-2);
  cursor: pointer;
  font: inherit;
  font-size: 13px;
  font-weight: 760;
  padding: 3px 0;
}

.today-panel__list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  flex: 1;
  justify-content: space-between;
  margin-top: 16px;
}

.today-panel__item {
  display: grid;
  grid-template-columns: 46px minmax(0, 1fr) auto;
  align-items: center;
  gap: 13px;
  min-height: 76px;
  border: 1px solid rgba(210, 225, 229, 0.78);
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.66);
  color: inherit;
  cursor: pointer;
  font: inherit;
  padding: 12px;
  text-align: left;
  transition: transform 0.18s ease, border-color 0.18s ease, box-shadow 0.18s ease;
}

.today-panel__item:hover {
  border-color: color-mix(in srgb, var(--today-color) 28%, rgba(210, 225, 229, 0.78));
  box-shadow: 0 14px 28px rgba(32, 69, 78, 0.08);
  transform: translateY(-2px);
}

.today-panel__icon {
  display: grid;
  width: 44px;
  height: 44px;
  place-items: center;
  border-radius: 14px;
  color: var(--today-color);
  background: var(--today-color-soft);
}

.today-panel__text {
  min-width: 0;
}

.today-panel__text strong,
.feature-card__body strong {
  display: block;
  color: var(--c-text-1);
  font-weight: 860;
}

.today-panel__text strong {
  font-size: 15px;
}

.today-panel__text small,
.feature-card__body small {
  display: block;
  overflow: hidden;
  color: var(--c-text-2);
  text-overflow: ellipsis;
}

.today-panel__text small {
  margin-top: 3px;
  font-size: 12px;
  white-space: nowrap;
}

.today-panel__meta {
  color: var(--today-color);
  font-size: 12px;
  font-weight: 820;
  white-space: nowrap;
}

.feature-section {
  border-radius: 28px;
  padding: 22px;
  scroll-margin-top: 88px;
}

.feature-sections-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px;
}

.feature-section--service {
  background:
    linear-gradient(135deg, rgba(255, 255, 255, 0.9), rgba(241, 255, 250, 0.82)),
    radial-gradient(circle at 100% 0, rgba(155, 216, 255, 0.22), transparent 34%);
}

.feature-section--life {
  background:
    linear-gradient(135deg, rgba(255, 255, 255, 0.92), rgba(254, 248, 255, 0.82)),
    radial-gradient(circle at 0 0, rgba(251, 191, 36, 0.12), transparent 28%),
    radial-gradient(circle at 100% 100%, rgba(96, 165, 250, 0.14), transparent 34%);
}

.feature-section__header p {
  margin: 6px 0 0;
  color: var(--c-text-2);
  font-size: 14px;
  line-height: 1.55;
}

.feature-section__grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(176px, 1fr));
  gap: 14px;
  margin-top: 18px;
}

.feature-card {
  display: grid;
  grid-template-columns: 48px minmax(0, 1fr) 18px;
  align-items: center;
  gap: 14px;
  min-height: 86px;
  border: 1px solid rgba(210, 225, 229, 0.78);
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.72);
  color: inherit;
  cursor: pointer;
  font: inherit;
  padding: 14px 16px;
  text-align: left;
  transition: transform 0.18s ease, border-color 0.18s ease, box-shadow 0.18s ease, background 0.18s ease;
}

.feature-card:hover {
  border-color: color-mix(in srgb, var(--feature-color) 26%, rgba(210, 225, 229, 0.78));
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 16px 30px rgba(33, 74, 84, 0.08);
  transform: translateY(-2px);
}

.feature-card__icon {
  display: grid;
  width: 48px;
  height: 48px;
  place-items: center;
  border-radius: 16px;
  color: var(--feature-color);
  background: var(--feature-color-soft);
}

.feature-card__body {
  min-width: 0;
}

.feature-card__body strong {
  font-size: 15px;
}

.feature-card__body small {
  display: -webkit-box;
  margin-top: 3px;
  font-size: 12px;
  line-height: 1.35;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.feature-card__arrow {
  width: 17px;
  height: 17px;
  color: var(--feature-color);
  opacity: 0.42;
  transition: opacity 0.18s ease, transform 0.18s ease;
}

.feature-card:hover .feature-card__arrow {
  opacity: 0.88;
  transform: translateX(2px);
}

.home-empty {
  border: 1px dashed var(--c-border);
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.68);
  color: var(--c-text-3);
  font-size: 14px;
  padding: 48px 20px;
  text-align: center;
}

@media (max-width: 1180px) {
  .home-hero-grid {
    grid-template-columns: 1fr;
  }

  .feature-sections-grid {
    grid-template-columns: 1fr;
  }

  .feature-section__grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 767px) {
  .home-page {
    gap: 14px;
  }

  .home-hero-grid {
    gap: 14px;
  }

  .home-hero-card {
    min-height: 358px;
    border-radius: 28px;
    background-position: 62% center;
  }

  .home-hero-card__shade {
    background:
      linear-gradient(180deg, rgba(255, 255, 255, 0.18) 0%, rgba(255, 255, 255, 0.62) 54%, rgba(255, 255, 255, 0.92) 100%),
      linear-gradient(90deg, rgba(255, 255, 255, 0.72), rgba(255, 255, 255, 0.08));
  }

  .home-hero-card__content {
    min-height: 358px;
    justify-content: flex-end;
    padding: 28px 24px;
  }

  .home-hero-card h1 {
    font-size: clamp(38px, 12vw, 54px);
  }

  .home-hero-card p {
    margin-top: 12px;
    font-size: 15px;
    line-height: 1.6;
  }

  .home-hero-card__actions {
    margin-top: 22px;
  }

  .home-hero-card__primary,
  .home-hero-card__secondary {
    min-height: 46px;
    flex: 1;
    padding: 0 14px;
  }

  .today-panel,
  .feature-section {
    border-radius: 24px;
    padding: 18px;
  }

  .today-panel__list {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 12px;
  }

  .today-panel__item {
    display: flex;
    min-height: 118px;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 9px;
    padding: 12px 8px;
    text-align: center;
  }

  .today-panel__text strong {
    font-size: 14px;
  }

  .today-panel__text small,
  .today-panel__meta {
    display: none;
  }

  .feature-section__grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: 10px;
  }

  .feature-card {
    min-height: 110px;
    grid-template-columns: 1fr;
    justify-items: center;
    gap: 10px;
    padding: 12px 8px;
    text-align: center;
  }

  .feature-card__icon {
    width: 52px;
    height: 52px;
    border-radius: 18px;
  }

  .feature-card__body strong {
    font-size: 14px;
  }

  .feature-card__body small,
  .feature-card__arrow {
    display: none;
  }
}


[data-theme="dark"] .home-hero-card,
[data-theme="dark"] .today-panel,
[data-theme="dark"] .feature-section,
[data-theme="dark"] .home-empty {
  border-color: rgba(68, 89, 112, 0.74);
  box-shadow: 0 22px 58px rgba(0, 0, 0, 0.28);
}

[data-theme="dark"] .home-hero-card {
  background-color: rgba(18, 30, 42, 0.92);
  background-image: url('/img/landing/campus-hero.jpg');
  background-position: center;
  background-repeat: no-repeat;
  background-size: cover;
}

[data-theme="dark"] .today-panel,
[data-theme="dark"] .feature-section,
[data-theme="dark"] .home-empty {
  background: rgba(24, 38, 53, 0.84);
}

[data-theme="dark"] .home-hero-card__shade {
  background:
    linear-gradient(90deg, rgba(11, 20, 31, 0.84) 0%, rgba(12, 23, 35, 0.58) 34%, rgba(12, 23, 35, 0.14) 64%, rgba(12, 23, 35, 0.02) 100%),
    linear-gradient(180deg, rgba(16, 25, 35, 0.02), rgba(16, 25, 35, 0.2)),
    radial-gradient(circle at 78% 22%, rgba(96, 165, 250, 0.12), transparent 38%),
    radial-gradient(circle at 25% 88%, rgba(45, 212, 191, 0.11), transparent 34%);
}

[data-theme="dark"] .home-hero-card h1 {
  color: var(--c-text-1);
}

[data-theme="dark"] .home-hero-card p {
  color: var(--c-text-2);
}

[data-theme="dark"] .home-hero-card__secondary,
[data-theme="dark"] .today-panel__item,
[data-theme="dark"] .feature-card {
  border-color: rgba(68, 89, 112, 0.68);
  background: rgba(27, 43, 60, 0.76);
}

[data-theme="dark"] .home-hero-card__secondary {
  border-color: rgba(94, 234, 212, 0.34);
  background: rgba(17, 54, 56, 0.62);
  color: #99F6E4;
  box-shadow: 0 14px 28px rgba(0, 0, 0, 0.2);
}

[data-theme="dark"] .feature-section--service {
  background:
    linear-gradient(135deg, rgba(24, 38, 53, 0.88), rgba(18, 30, 42, 0.82)),
    radial-gradient(circle at 100% 0, rgba(45, 212, 191, 0.1), transparent 36%);
}

[data-theme="dark"] .feature-section--life {
  background:
    linear-gradient(135deg, rgba(24, 38, 53, 0.88), rgba(22, 29, 44, 0.82)),
    radial-gradient(circle at 0 0, rgba(251, 191, 36, 0.08), transparent 30%),
    radial-gradient(circle at 100% 100%, rgba(167, 139, 250, 0.09), transparent 36%);
}
</style>
