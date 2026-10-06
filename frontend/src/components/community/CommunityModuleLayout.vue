<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ArrowRight, Send, ShieldCheck, Sparkles } from 'lucide-vue-next'
import CommunityTabbar from './CommunityTabbar.vue'

const props = defineProps({
  title: { type: String, required: true },
  subtitle: { type: String, default: '' },
  basePath: { type: String, required: true },
  moduleColor: { type: String, default: 'var(--c-ershou)' },
  tabs: { type: Array, required: true },
  publishPath: { type: String, default: '' }
})

const router = useRouter()
const { t } = useI18n()

const publishTarget = computed(() => {
  return props.publishPath || props.tabs.find((tab) => tab.key === 'publish')?.path || props.basePath
})

const activeModuleLabel = computed(() => {
  return props.tabs.find((tab) => tab.path === publishTarget.value)?.label || t('community.defaultPublish')
})

function goTo(path) {
  router.push(path)
}
</script>

<template>
  <div class="community-module-layout" :style="{ '--module-color': moduleColor }">
    <section class="community-module-layout__hero" aria-labelledby="community-module-title">
      <div class="community-module-layout__hero-copy">
        <h1 id="community-module-title">{{ title }}</h1>
        <p v-if="subtitle">{{ subtitle }}</p>
      </div>

      <div class="community-module-layout__hero-actions">
        <button type="button" class="community-module-layout__primary" @click="goTo(publishTarget)">
          <Send class="w-4 h-4" />
          {{ activeModuleLabel }}
        </button>
        <button type="button" class="community-module-layout__secondary" @click="goTo('/home')">
          {{ t('community.backHome') }}
          <ArrowRight class="w-4 h-4" />
        </button>
      </div>
    </section>

    <CommunityTabbar :basePath="basePath" :moduleColor="moduleColor" :tabs="tabs" />

    <div class="community-module-layout__body">
      <main class="community-module-layout__main">
        <slot />
      </main>

      <aside class="community-module-layout__rail" :aria-label="t('community.railAriaLabel')">
        <section class="community-module-layout__rail-card community-module-layout__rail-card--manage">
          <div class="community-module-layout__rail-head">
            <h2>{{ t('community.myPosts') }}</h2>
          </div>
          <div class="community-module-layout__quick-grid">
            <button type="button" @click="goTo(publishTarget)">
              <Send class="w-5 h-5" />
              <span>{{ t('community.defaultPublish') }}</span>
            </button>
            <button type="button" @click="goTo(basePath)">
              <Sparkles class="w-5 h-5" />
              <span>{{ t('community.latest') }}</span>
            </button>
          </div>
        </section>

        <section class="community-module-layout__rail-card community-module-layout__rail-card--safe">
          <div class="community-module-layout__rail-head">
            <h2>{{ t('community.safetyTitle') }}</h2>
            <ShieldCheck class="community-module-layout__safe-icon" />
          </div>
          <p>{{ t('community.safetyTip') }}</p>
        </section>
      </aside>
    </div>
  </div>
</template>

<style scoped>
.community-module-layout {
  width: 100%;
  min-height: 100vh;
  max-width: 1200px;
  margin: 0 auto;
  padding: 28px 40px 64px;
  color: var(--c-text-1);
  background: var(--c-bg);
}

.community-module-layout__hero {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 24px;
  padding: 0 0 20px;
  border-bottom: 1px solid var(--c-border);
}

.community-module-layout__hero-copy {
  min-width: 0;
}

.community-module-layout__hero h1 {
  margin: 0;
  color: var(--c-text-1);
  font-size: 28px;
  font-weight: 700;
  letter-spacing: -0.01em;
  line-height: 1.2;
}

.community-module-layout__hero p {
  max-width: 560px;
  margin: 6px 0 0;
  color: var(--c-text-2);
  font-size: 14px;
  line-height: 1.6;
}

.community-module-layout__hero-actions {
  display: flex;
  flex-shrink: 0;
  gap: 8px;
}

.community-module-layout__primary,
.community-module-layout__secondary {
  display: inline-flex;
  min-height: 40px;
  align-items: center;
  gap: 8px;
  border-radius: var(--radius-control);
  cursor: pointer;
  font: inherit;
  font-size: 14px;
  font-weight: 600;
  padding: 0 16px;
  transition: background-color 0.15s ease, border-color 0.15s ease, color 0.15s ease;
}

.community-module-layout__primary {
  border: 0;
  background: var(--c-primary);
  color: var(--c-on-primary);
}

.community-module-layout__primary:hover {
  background: var(--c-primary-hover);
}

.community-module-layout__secondary {
  border: 1px solid var(--c-border);
  background: var(--c-surface);
  color: var(--c-text-1);
}

.community-module-layout__secondary:hover {
  border-color: color-mix(in srgb, var(--c-primary) 45%, var(--c-border));
  color: var(--c-primary);
}

.community-module-layout__body {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 280px;
  gap: 24px;
  align-items: start;
  margin: 20px 0 0;
}

.community-module-layout__main {
  min-width: 0;
  overflow: hidden;
  border: 1px solid var(--c-border);
  border-radius: var(--radius-card);
  background: var(--c-surface);
  box-shadow: var(--shadow-sm);
}

.community-module-layout__rail {
  position: sticky;
  top: 88px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.community-module-layout__rail-card {
  overflow: hidden;
  border: 1px solid var(--c-border);
  border-radius: var(--radius-card);
  background: var(--c-surface);
}

.community-module-layout__rail-head {
  display: flex;
  min-height: 48px;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 0 16px;
  border-bottom: 1px solid var(--c-divider);
}

.community-module-layout__rail-head h2 {
  margin: 0;
  color: var(--c-text-1);
  font-size: 14px;
  font-weight: 650;
}

.community-module-layout__quick-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
  padding: 12px;
}

.community-module-layout__quick-grid button {
  display: flex;
  min-height: 64px;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  border: 1px solid var(--c-border);
  border-radius: var(--radius-control);
  background: var(--c-bg);
  color: var(--c-text-1);
  cursor: pointer;
  font: inherit;
  font-size: 13px;
  font-weight: 600;
  transition: border-color 0.15s ease, color 0.15s ease;
}

.community-module-layout__quick-grid button:hover {
  border-color: color-mix(in srgb, var(--c-primary) 45%, var(--c-border));
  color: var(--c-primary);
}

.community-module-layout__rail-card--safe p {
  margin: 0;
  padding: 12px 16px 16px;
  color: var(--c-text-2);
  font-size: 13px;
  line-height: 1.65;
}

.community-module-layout__safe-icon {
  width: 18px;
  height: 18px;
  color: var(--c-primary);
}

.community-module-layout__main :deep(.community-header) {
  display: none;
}

.community-module-layout__main :deep(.community-header--with-right) {
  display: grid;
  max-width: none;
  margin: 0;
}

.community-module-layout__main :deep(.min-h-screen) {
  min-height: auto;
}

.community-module-layout__main :deep(.pb-14),
.community-module-layout__main :deep(.pb-20) {
  padding-bottom: 0 !important;
}

.community-module-layout__main :deep(.overflow-y-auto) {
  height: auto !important;
  max-height: none !important;
  overflow: visible !important;
}

.community-module-layout__main :deep(.grid.grid-cols-2) {
  grid-template-columns: repeat(auto-fill, minmax(190px, 1fr)) !important;
  gap: 16px !important;
  padding: 16px !important;
}

.community-module-layout__main :deep(.grid.grid-cols-4) {
  grid-template-columns: repeat(auto-fit, minmax(86px, 1fr)) !important;
  margin: 16px !important;
  border-radius: var(--radius-card) !important;
}

@media (max-width: 1100px) {
  .community-module-layout {
    padding: 24px 24px 56px;
  }

  .community-module-layout__body {
    grid-template-columns: 1fr;
  }

  .community-module-layout__rail {
    display: none;
  }
}

@media (max-width: 767px) {
  .community-module-layout {
    padding: 0;
  }

  .community-module-layout__hero {
    display: none;
  }

  .community-module-layout__body {
    display: block;
    margin: 0;
  }

  .community-module-layout__main {
    overflow: visible;
    padding-bottom: calc(80px + env(safe-area-inset-bottom, 0px));
    border: 0;
    border-radius: 0;
    background: transparent;
    box-shadow: none;
  }

  .community-module-layout__main :deep(.community-header) {
    display: grid;
  }

  .community-module-layout__main :deep(.min-h-screen) {
    min-height: 100vh;
  }

  .community-module-layout__main :deep(.overflow-y-auto) {
    overflow-y: auto !important;
  }

  .community-module-layout__main :deep(.pb-14),
  .community-module-layout__main :deep(.pb-20) {
    padding-bottom: calc(80px + env(safe-area-inset-bottom, 0px)) !important;
  }
}

@media (min-width: 768px) {
  .community-module-layout__main :deep(.flex.flex-wrap > .inline-block) {
    width: calc(33.333% - 14px) !important;
    margin: 7px !important;
  }
}

@media (prefers-reduced-motion: reduce) {
  .community-module-layout__primary,
  .community-module-layout__secondary,
  .community-module-layout__quick-grid button {
    transition: none;
  }
}
</style>
