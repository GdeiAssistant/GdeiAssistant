<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowRight, Megaphone, Send, ShieldCheck, Sparkles } from 'lucide-vue-next'
import CommunityTabbar from './CommunityTabbar.vue'
import request from '../../utils/request'

const props = defineProps({
  title: { type: String, required: true },
  subtitle: { type: String, default: '' },
  basePath: { type: String, required: true },
  moduleColor: { type: String, default: 'var(--c-ershou)' },
  tabs: { type: Array, required: true },
  publishPath: { type: String, default: '' }
})

const router = useRouter()
const campusItems = ref([
  { id: 'campus-music', title: '校园歌手大赛决赛', meta: '19:00 · 音乐厅', path: '/info' },
  { id: 'library-night', title: '图书馆夜读打卡', meta: '20:00 · 图书馆', path: '/info' }
])

const publishTarget = computed(() => {
  return props.publishPath || props.tabs.find((tab) => tab.key === 'publish')?.path || props.basePath
})

const activeModuleLabel = computed(() => {
  return props.tabs.find((tab) => tab.path === publishTarget.value)?.label || '发布信息'
})

function goTo(path) {
  router.push(path)
}

onMounted(async () => {
  try {
    const res = await request.get('/information/announcement/start/0/size/2')
    const list = Array.isArray(res?.data) ? res.data : []
    if (list.length > 0) {
      campusItems.value = list.map((item) => ({
        id: item.id || item.announcementId || item.title,
        title: item.title,
        meta: item.publishDate || item.createTime || '校内通知',
        path: item.id ? `/info/announcements/${item.id}` : '/info'
      }))
    }
  } catch (_) {}
})
</script>

<template>
  <div class="community-module-layout" :style="{ '--module-color': moduleColor }">
    <section class="community-module-layout__hero" aria-labelledby="community-module-title">
      <div class="community-module-layout__hero-copy">
        <h1 id="community-module-title">{{ title }}</h1>
        <p>{{ subtitle || '发现校园精彩，连接你我生活' }}</p>
      </div>

      <div class="community-module-layout__hero-actions">
        <button type="button" class="community-module-layout__primary" @click="goTo(publishTarget)">
          <Send class="w-4 h-4" />
          {{ activeModuleLabel }}
        </button>
        <button type="button" class="community-module-layout__secondary" @click="goTo('/home')">
          返回功能主页
          <ArrowRight class="w-4 h-4" />
        </button>
      </div>
    </section>

    <CommunityTabbar :basePath="basePath" :moduleColor="moduleColor" :tabs="tabs" />

    <div class="community-module-layout__body">
      <main class="community-module-layout__main">
        <slot />
      </main>

      <aside class="community-module-layout__rail" aria-label="校园生活侧栏">
        <section class="community-module-layout__rail-card">
          <div class="community-module-layout__rail-head">
            <h2>今日校园</h2>
            <span>去看看</span>
          </div>
          <ul class="community-module-layout__activity-list">
            <li v-for="(item, index) in campusItems" :key="item.id" @click="goTo(item.path)">
              <component
                :is="index === 0 ? Megaphone : Sparkles"
                class="community-module-layout__rail-icon"
                :class="index === 0 ? 'community-module-layout__rail-icon--blue' : 'community-module-layout__rail-icon--green'"
              />
              <div>
                <strong>{{ item.title }}</strong>
                <small>{{ item.meta }}</small>
              </div>
            </li>
          </ul>
        </section>

        <section class="community-module-layout__rail-card">
          <div class="community-module-layout__rail-head">
            <h2>我的发布</h2>
            <span>管理</span>
          </div>
          <div class="community-module-layout__quick-grid">
            <button type="button" @click="goTo(publishTarget)">
              <Send class="w-5 h-5" />
              <span>发布信息</span>
            </button>
            <button type="button" @click="goTo(basePath)">
              <Sparkles class="w-5 h-5" />
              <span>最新动态</span>
            </button>
          </div>
        </section>

        <section class="community-module-layout__rail-card community-module-layout__rail-card--safe">
          <div class="community-module-layout__rail-head">
            <h2>安全提示</h2>
            <ShieldCheck class="community-module-layout__safe-icon" />
          </div>
          <p>交易和代取请优先选择校内当面确认，避免泄露个人隐私。</p>
        </section>
      </aside>
    </div>
  </div>
</template>

<style scoped>
.community-module-layout {
  width: 100%;
  min-height: 100vh;
  padding: 24px;
  color: var(--c-text-1);
  background:
    radial-gradient(circle at 14% 0, color-mix(in srgb, var(--module-color) 18%, transparent), transparent 28%),
    radial-gradient(circle at 86% 8%, rgba(58, 167, 232, 0.12), transparent 30%),
    var(--c-bg);
}

.community-module-layout__hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  margin: 0 auto 16px;
  padding: 24px;
  border: 1px solid color-mix(in srgb, var(--module-color) 22%, var(--c-border));
  border-radius: 28px;
  background:
    linear-gradient(135deg, rgba(255, 255, 255, 0.9), rgba(255, 255, 255, 0.68)),
    radial-gradient(circle at 100% 0, color-mix(in srgb, var(--module-color) 18%, transparent), transparent 36%);
  box-shadow: 0 22px 58px rgba(32, 69, 78, 0.08);
  backdrop-filter: blur(18px);
}

.community-module-layout__hero-copy {
  min-width: 0;
}

.community-module-layout__hero h1 {
  margin: 0;
  color: var(--c-text-1);
  font-size: clamp(30px, 4vw, 44px);
  font-weight: 920;
  letter-spacing: -0.045em;
  line-height: 1.08;
}

.community-module-layout__hero p {
  max-width: 560px;
  margin: 10px 0 0;
  color: var(--c-text-2);
  font-size: 15px;
  font-weight: 650;
  line-height: 1.7;
}

.community-module-layout__hero-actions {
  display: flex;
  flex: none;
  gap: 12px;
}

.community-module-layout__primary,
.community-module-layout__secondary {
  display: inline-flex;
  min-height: 46px;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border-radius: 16px;
  cursor: pointer;
  font: inherit;
  font-size: 14px;
  font-weight: 850;
  padding: 0 18px;
  transition: transform 0.18s ease, box-shadow 0.18s ease, border-color 0.18s ease;
}

.community-module-layout__primary {
  border: 0;
  background: linear-gradient(135deg, var(--module-color), color-mix(in srgb, var(--module-color) 72%, #0ea5e9));
  color: #fff;
  box-shadow: 0 18px 34px color-mix(in srgb, var(--module-color) 24%, transparent);
}

.community-module-layout__secondary {
  border: 1px solid color-mix(in srgb, var(--module-color) 26%, var(--c-border));
  background: rgba(255, 255, 255, 0.7);
  color: var(--c-text-2);
}

.community-module-layout__primary:hover,
.community-module-layout__secondary:hover {
  transform: translateY(-2px);
}

.community-module-layout__body {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 300px;
  gap: 16px;
  align-items: start;
  margin: 16px auto 0;
}

.community-module-layout__main {
  min-width: 0;
  overflow: hidden;
  border: 1px solid rgba(205, 222, 226, 0.72);
  border-radius: 26px;
  background: rgba(255, 255, 255, 0.78);
  box-shadow: 0 20px 48px rgba(32, 69, 78, 0.08);
  backdrop-filter: blur(16px);
}

.community-module-layout__rail {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.community-module-layout__rail-card {
  border: 1px solid rgba(205, 222, 226, 0.72);
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.78);
  box-shadow: 0 16px 36px rgba(32, 69, 78, 0.07);
  padding: 18px;
  backdrop-filter: blur(16px);
}

.community-module-layout__rail-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.community-module-layout__rail-head h2 {
  margin: 0;
  color: var(--c-text-1);
  font-size: 17px;
  font-weight: 900;
}

.community-module-layout__rail-head span {
  color: var(--module-color);
  font-size: 12px;
  font-weight: 850;
}

.community-module-layout__activity-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
  margin: 16px 0 0;
  padding: 0;
  list-style: none;
}

.community-module-layout__activity-list li {
  display: grid;
  grid-template-columns: 42px minmax(0, 1fr);
  gap: 12px;
  align-items: center;
  cursor: pointer;
}

.community-module-layout__activity-list strong,
.community-module-layout__activity-list small {
  display: block;
}

.community-module-layout__activity-list strong {
  color: var(--c-text-1);
  font-size: 14px;
  font-weight: 850;
}

.community-module-layout__activity-list small {
  margin-top: 3px;
  color: var(--c-text-3);
  font-size: 12px;
}

.community-module-layout__rail-icon {
  width: 42px;
  height: 42px;
  padding: 10px;
  border-radius: 14px;
}

.community-module-layout__rail-icon--blue {
  color: color-mix(in srgb, var(--module-color) 76%, #3b82f6);
  background: color-mix(in srgb, var(--module-color) 11%, transparent);
}

.community-module-layout__rail-icon--green {
  color: color-mix(in srgb, var(--module-color) 74%, #f59e0b);
  background: color-mix(in srgb, var(--module-color) 9%, transparent);
}

.community-module-layout__quick-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
  margin-top: 16px;
}

.community-module-layout__quick-grid button {
  display: flex;
  min-height: 78px;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border: 1px solid color-mix(in srgb, var(--module-color) 18%, var(--c-border));
  border-radius: 18px;
  background: color-mix(in srgb, var(--module-color) 8%, transparent);
  color: var(--module-color);
  cursor: pointer;
  font: inherit;
  font-size: 12px;
  font-weight: 850;
}

.community-module-layout__rail-card--safe p {
  margin: 12px 0 0;
  color: var(--c-text-2);
  font-size: 13px;
  line-height: 1.65;
}

.community-module-layout__safe-icon {
  width: 20px;
  height: 20px;
  color: var(--module-color);
}

.community-module-layout__main :deep(.community-header) {
  display: none;
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
  grid-template-columns: repeat(auto-fill, minmax(178px, 1fr)) !important;
  gap: 14px !important;
  padding: 16px !important;
}

.community-module-layout__main :deep(.grid.grid-cols-4) {
  grid-template-columns: repeat(auto-fit, minmax(86px, 1fr)) !important;
  margin: 14px 16px !important;
  border-radius: 18px !important;
}

@media (max-width: 1100px) {
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
    background: var(--c-bg);
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
    border: 0;
    border-radius: 0;
    background: transparent;
    box-shadow: none;
    backdrop-filter: none;
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
}

@media (min-width: 768px) {
  .community-module-layout__main :deep(.flex.flex-wrap > .inline-block) {
    width: calc(33.333% - 14px) !important;
    margin: 7px !important;
  }
}

[data-theme="dark"] .community-module-layout {
  --community-module-dark-accent: color-mix(in srgb, var(--module-color) 40%, #94a3b8);
  --community-module-dark-accent-soft: color-mix(in srgb, var(--module-color) 6%, rgba(32, 48, 68, 0.68));
  --community-module-dark-border: color-mix(in srgb, var(--module-color) 10%, rgba(74, 96, 120, 0.68));

  background:
    radial-gradient(circle at 14% 0, color-mix(in srgb, var(--module-color) 6%, transparent), transparent 30%),
    radial-gradient(circle at 88% 8%, rgba(96, 165, 250, 0.09), transparent 32%),
    linear-gradient(180deg, #101923 0%, #0f1822 100%);
}

[data-theme="dark"] .community-module-layout__hero,
[data-theme="dark"] .community-module-layout__main,
[data-theme="dark"] .community-module-layout__rail-card {
  border-color: rgba(68, 89, 112, 0.72);
  background: rgba(24, 38, 53, 0.84);
  box-shadow: 0 22px 58px rgba(0, 0, 0, 0.28);
}

[data-theme="dark"] .community-module-layout__hero {
  background:
    linear-gradient(135deg, rgba(24, 38, 53, 0.88), rgba(18, 30, 42, 0.78)),
    radial-gradient(circle at 100% 0, color-mix(in srgb, var(--module-color) 8%, transparent), transparent 38%);
}

[data-theme="dark"] .community-module-layout__primary {
  background: linear-gradient(135deg, var(--community-module-dark-accent), color-mix(in srgb, var(--community-module-dark-accent) 76%, #60a5fa));
  box-shadow: 0 18px 34px color-mix(in srgb, var(--module-color) 18%, transparent);
}

[data-theme="dark"] .community-module-layout__secondary {
  border-color: rgba(74, 96, 120, 0.78);
  background: rgba(27, 43, 60, 0.72);
  color: var(--c-text-2);
}

[data-theme="dark"] .community-module-layout__rail-head span,
[data-theme="dark"] .community-module-layout__safe-icon {
  color: var(--community-module-dark-accent);
}

[data-theme="dark"] .community-module-layout__rail-icon--blue {
  color: var(--community-module-dark-accent);
  background: color-mix(in srgb, var(--module-color) 8%, rgba(32, 48, 68, 0.82));
}

[data-theme="dark"] .community-module-layout__rail-icon--green {
  color: color-mix(in srgb, var(--community-module-dark-accent) 76%, #f6c768);
  background: color-mix(in srgb, var(--module-color) 7%, rgba(32, 48, 68, 0.82));
}

[data-theme="dark"] .community-module-layout__quick-grid button {
  border-color: var(--community-module-dark-border);
  background: var(--community-module-dark-accent-soft);
  color: var(--community-module-dark-accent);
}
</style>
