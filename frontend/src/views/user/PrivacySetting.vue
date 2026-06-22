<template>
  <div class="privacy-setting-page min-h-screen">
    <!-- Sticky Header -->
    <div class="privacy-setting-header sticky top-0 z-10 flex items-center h-12 px-4">
      <button type="button" class="privacy-setting-back w-15 text-base text-left" @click="goBack">{{ t('common.back') }}</button>
      <div class="privacy-setting-title flex-1 text-center text-lg font-medium">{{ t('profile.privacySetting') }}</div>
      <div class="w-15"></div>
    </div>

    <!-- Content -->
    <div class="max-w-lg mx-auto px-4 py-6">
      <div class="privacy-setting-card rounded-xl shadow-sm divide-y">
        <label
          v-for="item in privacyList"
          :key="item.key"
          class="privacy-setting-row flex items-center justify-between px-4 py-3 cursor-pointer"
        >
          <span class="privacy-setting-label text-base">{{ item.name }}</span>
          <div class="relative inline-flex items-center">
            <input
              type="checkbox"
              class="sr-only peer"
              :checked="item.status"
              @change="handlePrivacyChange(item)"
            />
            <div class="privacy-setting-switch w-11 h-6 rounded-full transition-colors after:content-[''] after:absolute after:top-0.5 after:left-[2px] after:bg-white after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:after:translate-x-full"></div>
          </div>
        </label>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { getPrivacySettings, updatePrivacySettings } from '../../api/privacy.js'
import { useToast } from '@/composables/useToast'
import { createPrivacyItems } from './settingsContent'

const router = useRouter()
const { t } = useI18n()
const { success: toastSuccess } = useToast()

const privacyList = ref(createPrivacyItems(t))

const fieldMapping = {
  faculty: 'facultyOpen',
  major: 'majorOpen',
  location: 'locationOpen',
  hometown: 'hometownOpen',
  introduction: 'introductionOpen',
  enrollment: 'enrollmentOpen',
  age: 'ageOpen',
  cache: 'cacheAllow',
  robots: 'robotsIndexAllow'
}

function buildPayload() {
  const payload = {}
  privacyList.value.forEach((item) => {
    const field = fieldMapping[item.key]
    if (field) payload[field] = item.status === true
  })
  return payload
}

async function loadPrivacySettings() {
  try {
    const res = await getPrivacySettings()
    if (res && res.success && res.data) {
      const d = res.data
      privacyList.value.forEach((item) => {
        const field = fieldMapping[item.key]
        if (field && d[field] !== undefined) {
          item.status = d[field] === true
        }
      })
    }
  } catch (e) {
    // 错误提示由 request.js 全局拦截器统一展示，此处仅静默处理
  }
}

const CODE_PARTIAL_SUCCESS = 206

async function handlePrivacyChange(item) {
  const prevStatus = item.status
  item.status = !item.status
  const payload = buildPayload()
  try {
    const res = await updatePrivacySettings(payload)
    if (!res || !res.success) {
      item.status = prevStatus
      return
    }
    if (res.code === CODE_PARTIAL_SUCCESS) {
      toastSuccess(res.message || t('privacy.partialSuccess'))
    }
  } catch (e) {
    item.status = prevStatus
    // 错误提示由 request.js 全局拦截器统一展示，此处仅还原开关状态
  }
}

function goBack() {
  router.back()
}

onMounted(() => {
  loadPrivacySettings()
})
</script>

<style scoped>
.privacy-setting-page {
  background:
    radial-gradient(circle at top, color-mix(in srgb, var(--c-primary) 8%, transparent), transparent 30%),
    var(--c-bg-soft);
}

.privacy-setting-header {
  background: color-mix(in srgb, var(--c-surface) 94%, var(--c-bg));
  border-bottom: 1px solid var(--c-border-light);
  backdrop-filter: blur(18px);
}

.privacy-setting-back,
.privacy-setting-title,
.privacy-setting-label {
  color: var(--c-text-1);
}

.privacy-setting-card {
  background: var(--c-surface);
  border: 1px solid color-mix(in srgb, var(--c-primary) 8%, var(--c-border-light));
  box-shadow: 0 12px 28px color-mix(in srgb, var(--c-primary) 8%, rgba(15, 23, 42, 0.06));
}

.privacy-setting-row + .privacy-setting-row {
  border-top: 1px solid var(--c-border-light);
}

.privacy-setting-switch {
  background: color-mix(in srgb, var(--c-text-3) 18%, var(--c-border));
}

.peer:checked + .privacy-setting-switch {
  background: linear-gradient(135deg, color-mix(in srgb, var(--c-primary) 88%, #2dd4bf), color-mix(in srgb, var(--c-primary) 72%, #0f766e));
}

[data-theme="dark"] .privacy-setting-page {
  background:
    radial-gradient(circle at top, color-mix(in srgb, var(--c-primary) 10%, transparent), transparent 30%),
    var(--c-bg);
}

[data-theme="dark"] .privacy-setting-header {
  background: color-mix(in srgb, var(--c-surface) 88%, rgba(10, 20, 32, 0.9));
  border-bottom-color: rgba(68, 89, 112, 0.72);
}

[data-theme="dark"] .privacy-setting-card {
  border-color: rgba(68, 89, 112, 0.72);
  box-shadow: 0 18px 36px rgba(0, 0, 0, 0.2);
}

[data-theme="dark"] .privacy-setting-row + .privacy-setting-row {
  border-top-color: rgba(68, 89, 112, 0.72);
}

[data-theme="dark"] .privacy-setting-switch {
  background: rgba(36, 52, 69, 0.88);
}

[data-theme="dark"] .peer:checked + .privacy-setting-switch {
  background: linear-gradient(135deg, color-mix(in srgb, var(--c-primary) 68%, #22d3ee), color-mix(in srgb, var(--c-primary) 54%, #0f766e));
}
</style>
