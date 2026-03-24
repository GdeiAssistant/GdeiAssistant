<script setup>
import { useRouter } from 'vue-router'
import { onMounted, onActivated, ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { ALL_FEATURES, FEATURE_ICON_SRC } from '@/constants/features'
import {
  Star, Calendar, CreditCard, FileText, GraduationCap, BookOpen,
  DoorOpen, Dumbbell, PenLine, Database, ShoppingCart, Search,
  MessageCircle, Eye, Users, Heart, Camera, Truck, Info as InfoIcon
} from 'lucide-vue-next'

const STORAGE_KEY = 'user_features_config'
const MIGRATION_KEY = 'user_features_migrated_v2'

const router = useRouter()
const { t } = useI18n()

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
  return ALL_FEATURES.filter((f) => {
    if (!config || Object.keys(config).length === 0) return f.defaultVisible !== false
    return config[f.id] !== false
  }).map((f) => ({
    id: f.id,
    title: f.name,
    icon: FEATURE_ICON_SRC[f.id] || '/img/function/data.png',
    path: f.path,
    type: f.type,
    key: f.key,
  }))
})

const SERVICE_FEATURE_IDS = new Set(['grade', 'schedule', 'cet', 'kaoyan', 'spare', 'collection', 'card', 'pe', 'data', 'evaluate', 'about'])
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

function handleMenuClick(item) {
  if (item.path) {
    router.push(item.path)
  }
}

// Icon mapping
const iconMap = {
  grade: Star, schedule: Calendar, card: CreditCard, cet: FileText,
  kaoyan: GraduationCap, collection: BookOpen, spare: DoorOpen,
  pe: Dumbbell, evaluate: PenLine, data: Database, about: InfoIcon,
  ershou: ShoppingCart, lostandfound: Search, express: Heart,
  secret: Eye, dating: Users, topic: MessageCircle,
  photograph: Camera, delivery: Truck,
}

const iconColors = {
  grade: '#047857', schedule: '#2563EB', card: '#D97706', cet: '#7C3AED',
  kaoyan: '#0891B2', collection: '#4F46E5', spare: '#06B6D4',
  pe: '#DC2626', evaluate: '#DB2777', data: '#0D9488', about: '#6B7280',
  ershou: '#047857', lostandfound: '#2563EB', express: '#F43F5E',
  secret: '#8B5CF6', dating: '#EC4899', topic: '#6366F1',
  photograph: '#06B6D4', delivery: '#F59E0B',
}

function iconBgStyle(id) {
  const color = iconColors[id] || '#6B7280'
  return { backgroundColor: color + '14' }
}

function iconColorStyle(id) {
  return { color: iconColors[id] || '#6B7280' }
}

// Feature descriptions
const featureDescriptions = {
  grade: '查每学年每学期成绩和绩点',
  schedule: '一眼看清这周每天上什么课',
  cet: '输入考号和验证码即可查分',
  kaoyan: '查询考研成绩和相关信息',
  spare: '看看现在还有哪些空教室',
  collection: '搜馆藏、看借阅、办续借',
  card: '查余额、查消费、办挂失',
  pe: '查看体测成绩和数据',
  data: '查电费，也能找校内常用号码',
  evaluate: '需要评教时直接来这里',
  about: '关于广东二师助手',
  ershou: '闲置物品转让，校内面交',
  delivery: '发跑腿需求，帮取快递',
  lostandfound: '丢东西、捡东西都来这',
  secret: '匿名倾诉，说出心里话',
  dating: '把室友介绍给全世界',
  express: '勇敢说出你的心意',
  topic: '热门讨论，校园八卦',
  photograph: '用镜头记录校园美好',
}
</script>

<template>
  <div>
    <!-- Greeting section -->
    <div class="mb-6">
      <h1 class="text-2xl font-extrabold">{{ $t('home.title') }}</h1>
      <p class="text-sm text-[var(--c-text-2)] mt-1">{{ $t('home.subtitle') }}</p>
    </div>

    <!-- Feature sections -->
    <section
      v-for="section in featureSections"
      :key="section.id"
      class="mb-6"
    >
      <div class="bg-[var(--c-surface)] border border-[var(--c-border)] rounded-[14px] p-5">
        <h2 class="text-lg font-extrabold mb-1">{{ section.title }}</h2>
        <p class="text-sm text-[var(--c-text-3)] mb-4">{{ section.description }}</p>
        <div class="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-3">
          <button
            v-for="(item, index) in section.items"
            :key="item.id || item.path || item.key || index"
            :aria-label="item.title"
            class="flex flex-col bg-[var(--c-bg)] rounded-xl p-3.5 text-left transition hover:bg-[var(--c-border-light)] active:scale-[0.97]"
            @click="handleMenuClick(item)"
          >
            <div
              class="w-9 h-9 rounded-lg flex items-center justify-center mb-2.5"
              :style="iconBgStyle(item.id)"
            >
              <component
                :is="iconMap[item.id]"
                class="w-[18px] h-[18px]"
                :style="iconColorStyle(item.id)"
              />
            </div>
            <div class="text-sm font-bold text-[var(--c-text-1)]">{{ item.title }}</div>
            <div class="text-[11px] text-[var(--c-text-3)] mt-0.5 line-clamp-2">{{ featureDescriptions[item.id] }}</div>
          </button>
        </div>
      </div>
    </section>

    <!-- Empty state -->
    <div v-if="featureSections.length === 0" class="py-12 text-center text-sm text-[var(--c-text-3)]">
      {{ $t('home.noFeatures') }}
    </div>
  </div>
</template>
