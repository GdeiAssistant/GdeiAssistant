<template>
  <div class="subpage feature-manage-page min-h-screen">
    <!-- Sticky Header -->
    <div class="subpage-bar feature-manage-header">
      <button type="button" class="subpage-bar__back feature-manage-back" @click="goBack">
        <ChevronLeft :size="18" aria-hidden="true" />
        <span>{{ t('common.back') }}</span>
      </button>
      <div class="subpage-bar__title feature-manage-title">{{ t('profile.featureManage') }}</div>
      <span aria-hidden="true"></span>
    </div>

    <!-- Content -->
    <div class="subpage-body max-w-lg mx-auto px-4 py-6">
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
import { ChevronLeft } from 'lucide-vue-next'
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
  background: var(--c-bg);
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
  border: 1px solid var(--c-border);
  box-shadow: none;
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
  background: var(--c-primary);
}
</style>
