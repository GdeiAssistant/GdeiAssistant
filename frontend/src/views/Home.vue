<script setup>
import { useRouter } from 'vue-router'
import { onMounted, onActivated, ref, computed } from 'vue'
import { ALL_FEATURES, FEATURE_ICON_SRC } from '@/constants/features'

const STORAGE_KEY = 'user_features_config'
const MIGRATION_KEY = 'user_features_migrated_v2'

const router = useRouter()

/**
 * 迁移旧版 feature toggle 配置：
 * - 旧版 id `book` / `cardInfo` 已合并到 `collection`（图书馆）/ `card`（校园卡），
 *   如果用户曾隐藏 book 或 collection 之一，合并后的入口不应被意外隐藏，重置为可见。
 * - 移除已废弃的旧 id 键，避免残留数据干扰。
 */
function migrateFeatureConfig(config) {
  if (!config || typeof config !== 'object') return config
  if (localStorage.getItem(MIGRATION_KEY)) return config

  const oldLibraryIds = ['book', 'collection']
  const oldCardIds = ['cardInfo', 'card']
  const hasOldLibrary = oldLibraryIds.some((k) => k in config)
  const hasOldCard = oldCardIds.some((k) => k in config)

  if (hasOldLibrary) {
    // 只要任一旧入口曾可见，合并后保持可见
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

// 从 localStorage 实时读取功能开关；无配置时按 defaultVisible 兜底显示
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

// 首页展示列表：基于 ALL_FEATURES，仅展示在缓存中为 true 的项；无配置时按 defaultVisible 显示
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
      title: '校园服务',
      description: '查成绩、课表、四六级、图书馆、校园卡和常用查询工具',
      items: serviceItems
    },
    {
      id: 'life',
      title: '校园生活',
      description: '二手交易、快递代取、失物招领、树洞、卖室友、表白墙、话题和校园摄影',
      items: lifeItems
    }
  ].filter((section) => section.items.length > 0)
})

function handleMenuClick(item) {
  if (item.path) {
    router.push(item.path)
  }
}
</script>

<template>
  <div class="page">
    <div class="page__hero">
      <h1 class="page__title">广东二师助手</h1>
      <p class="page__desc">把校园服务和校园生活统一收在一个首页里，入口和移动端保持一致。</p>
    </div>

    <section
      v-for="section in featureSections"
      :key="section.id"
      class="feature-section"
    >
      <div class="feature-section__header">
        <div class="feature-section__title">{{ section.title }}</div>
        <div class="feature-section__desc">{{ section.description }}</div>
      </div>

      <div class="weui-grids feature-section__grid">
        <a
          v-for="(item, index) in section.items"
          :key="item.id || item.path || item.key || index"
          href="javascript:;"
          class="weui-grid"
          @click.prevent.stop="handleMenuClick(item)"
        >
          <div class="weui-grid__icon">
            <img :src="item.icon" :alt="item.title" />
          </div>
          <p class="weui-grid__label">{{ item.title }}</p>
        </a>
      </div>
    </section>
    <div v-if="featureSections.length === 0" class="feature-empty">
      当前没有可展示的功能入口
    </div>
  </div>
</template>

<style scoped>
.page {
  min-height: 100vh;
  padding: 0 16px 28px;
  background:
    radial-gradient(circle at top right, rgba(9, 187, 7, 0.12), transparent 32%),
    linear-gradient(180deg, #f6fbf6 0%, #ffffff 34%);
}

.page__hero {
  padding: 28px 4px 18px;
}

.page__title {
  font-size: 30px;
  font-weight: 700;
  margin: 0;
  color: #102110;
}

.page__desc {
  font-size: 14px;
  color: #6b766b;
  margin: 10px 0 0;
  line-height: 1.7;
}

.feature-section {
  margin-top: 16px;
  padding: 18px 16px 10px;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 16px 42px rgba(16, 33, 16, 0.06);
}

.feature-section__header {
  margin-bottom: 10px;
}

.feature-section__title {
  font-size: 20px;
  font-weight: 700;
  color: #102110;
}

.feature-section__desc {
  margin-top: 6px;
  font-size: 13px;
  line-height: 1.6;
  color: #6b766b;
}

.feature-section__grid {
  margin-top: 0;
  background-color: transparent;
}

.feature-empty {
  padding: 48px 16px;
  text-align: center;
  color: #999;
  font-size: 14px;
}

.weui-grid {
  cursor: pointer;
  -webkit-tap-highlight-color: rgba(0, 0, 0, 0.1);
  pointer-events: auto;
  position: relative;
  z-index: 1;
}

.weui-grid:active {
  background-color: rgba(0, 0, 0, 0.05);
}

.weui-grid__icon {
  pointer-events: none;
}

.weui-grid__icon img {
  width: 100%;
  height: 100%;
  display: block;
  object-fit: contain;
  pointer-events: none;
}

.weui-grid__label {
  pointer-events: none;
}

@media (max-width: 640px) {
  .page {
    padding-bottom: 24px;
  }
}
</style>
