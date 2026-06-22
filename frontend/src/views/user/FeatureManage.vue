<template>
  <div class="feature-manage-page min-h-screen">
    <!-- Sticky Header -->
    <div class="feature-manage-header sticky top-0 z-10 flex items-center h-12 px-4">
      <button type="button" class="feature-manage-back w-15 text-base text-left" @click="goBack">{{ t('common.back') }}</button>
      <div class="feature-manage-title flex-1 text-center text-lg font-medium">{{ t('profile.featureManage') }}</div>
      <div class="w-15"></div>
    </div>

    <!-- Content -->
    <div class="max-w-lg mx-auto px-4 py-6">
      <p class="feature-manage-description text-sm mb-3">
        {{ t('featureManage.description') }}
      </p>

      <div class="feature-manage-card rounded-xl shadow-sm divide-y">
        <label
          v-for="item in featureList"
          :key="item.id"
          class="feature-manage-row flex items-center justify-between px-4 py-3 cursor-pointer"
        >
          <span class="feature-manage-label text-base">{{ item.name }}</span>
          <div class="relative inline-flex items-center">
            <input
              type="checkbox"
              class="sr-only peer"
              v-model="item.visible"
              @change="handleToggle"
            />
            <div class="feature-manage-switch w-11 h-6 rounded-full transition-colors after:content-[''] after:absolute after:top-0.5 after:left-[2px] after:bg-white after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:after:translate-x-full"></div>
          </div>
        </label>
      </div>
    </div>

    <!-- Toast -->
    <Teleport to="body">
      <Transition name="fade">
        <div v-if="showToast" class="fixed inset-0 z-[9999] flex items-center justify-center pointer-events-none">
          <div class="bg-black/70 text-white text-sm px-5 py-3 rounded-lg">
            {{ toastMessage }}
          </div>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ALL_FEATURES, getLocalizedFeatures } from '@/constants/features'

const STORAGE_KEY = 'user_features_config'

const router = useRouter()
const { t } = useI18n()
const featureList = ref([])
const showToast = ref(false)
const toastMessage = ref('')

/** 初始化：从 localStorage 读取；没有则按 ALL_FEATURES 的 defaultVisible 初始化并保存 */
function loadFromStorage() {
  let config = {}
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (raw) {
      config = JSON.parse(raw)
    } else {
      // 未读到则按 defaultVisible 初始化并写入
      config = {}
      ALL_FEATURES.forEach((f) => {
        config[f.id] = f.defaultVisible !== false
      })
      localStorage.setItem(STORAGE_KEY, JSON.stringify(config))
    }
  } catch (_) {
    config = {}
    ALL_FEATURES.forEach((f) => {
      config[f.id] = f.defaultVisible !== false
    })
    localStorage.setItem(STORAGE_KEY, JSON.stringify(config))
  }
  featureList.value = getLocalizedFeatures(ALL_FEATURES, t).map((item) => ({
    ...item,
    visible: config[item.id] !== false,
  }))
}

/** 用户点击开关：更新布尔值并立即同步到 localStorage，再弹出 toast */
function handleToggle() {
  const config = {}
  featureList.value.forEach((item) => {
    config[item.id] = item.visible
  })
  localStorage.setItem(STORAGE_KEY, JSON.stringify(config))
  toastMessage.value = t('featureManage.saved')
  showToast.value = true
  setTimeout(() => {
    showToast.value = false
  }, 2000)
}

function goBack() {
  router.back()
}

onMounted(() => {
  loadFromStorage()
})
</script>

<style scoped>
.feature-manage-page {
  background:
    radial-gradient(circle at top, color-mix(in srgb, var(--c-primary) 8%, transparent), transparent 30%),
    var(--c-bg-soft);
}

.feature-manage-header {
  background: color-mix(in srgb, var(--c-surface) 94%, var(--c-bg));
  border-bottom: 1px solid var(--c-border-light);
  backdrop-filter: blur(18px);
}

.feature-manage-back,
.feature-manage-title,
.feature-manage-label {
  color: var(--c-text-1);
}

.feature-manage-description {
  color: var(--c-text-3);
}

.feature-manage-card {
  background: var(--c-surface);
  border: 1px solid color-mix(in srgb, var(--c-primary) 8%, var(--c-border-light));
  box-shadow: 0 12px 28px color-mix(in srgb, var(--c-primary) 8%, rgba(15, 23, 42, 0.06));
  --tw-divide-opacity: 1;
  border-color: color-mix(in srgb, var(--c-primary) 8%, var(--c-border-light));
}

.feature-manage-row + .feature-manage-row {
  border-top: 1px solid var(--c-border-light);
}

.feature-manage-switch {
  background: color-mix(in srgb, var(--c-text-3) 18%, var(--c-border));
}

.peer:checked + .feature-manage-switch {
  background: linear-gradient(135deg, color-mix(in srgb, var(--c-primary) 88%, #2dd4bf), color-mix(in srgb, var(--c-primary) 72%, #0f766e));
}

[data-theme="dark"] .feature-manage-page {
  background:
    radial-gradient(circle at top, color-mix(in srgb, var(--c-primary) 10%, transparent), transparent 30%),
    var(--c-bg);
}

[data-theme="dark"] .feature-manage-header {
  background: color-mix(in srgb, var(--c-surface) 88%, rgba(10, 20, 32, 0.9));
  border-bottom-color: rgba(68, 89, 112, 0.72);
}

[data-theme="dark"] .feature-manage-card {
  border-color: rgba(68, 89, 112, 0.72);
  box-shadow: 0 18px 36px rgba(0, 0, 0, 0.2);
}

[data-theme="dark"] .feature-manage-row + .feature-manage-row {
  border-top-color: rgba(68, 89, 112, 0.72);
}

[data-theme="dark"] .feature-manage-switch {
  background: rgba(36, 52, 69, 0.88);
}

[data-theme="dark"] .peer:checked + .feature-manage-switch {
  background: linear-gradient(135deg, color-mix(in srgb, var(--c-primary) 68%, #22d3ee), color-mix(in srgb, var(--c-primary) 54%, #0f766e));
}
</style>
