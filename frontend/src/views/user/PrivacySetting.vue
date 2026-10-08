<template>
  <div class="subpage privacy-setting-page min-h-screen">
    <!-- Sticky Header -->
    <div class="subpage-bar privacy-setting-header">
      <button type="button" class="subpage-bar__back privacy-setting-back" @click="goBack">
        <ChevronLeft :size="18" aria-hidden="true" />
        <span>{{ t('common.back') }}</span>
      </button>
      <div class="subpage-bar__title privacy-setting-title">{{ t('profile.privacySetting') }}</div>
      <span aria-hidden="true"></span>
    </div>

    <!-- Content -->
    <div class="max-w-lg mx-auto px-4 py-6 space-y-4">
      <div class="privacy-setting-card rounded-xl shadow-sm divide-y">
        <label
          v-for="item in privacyList"
          :key="item.key"
          class="privacy-setting-row flex items-center justify-between min-h-11 px-4 py-3 cursor-pointer"
        >
          <span class="privacy-setting-label text-base">{{ t(`privacy.field.${item.key}`) }}</span>
          <div class="relative inline-flex items-center">
            <input
              type="checkbox"
              class="sr-only peer"
              :checked="item.status"
              @change="handlePrivacyChange(item)"
            />
            <div class="privacy-setting-switch w-11 h-6 rounded-full transition-colors after:content-[''] after:absolute after:top-0.5 after:left-[2px] after:bg-[var(--c-surface)] after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:after:translate-x-full"></div>
          </div>
        </label>
      </div>

      <div class="privacy-setting-card rounded-xl shadow-sm divide-y">
        <RouterLink
          to="/social/privacy"
          class="privacy-setting-row flex items-center justify-between min-h-11 px-4 py-3 no-underline text-inherit"
        >
          <span class="privacy-setting-label text-base">{{ t('social.dmPrivacyTitle') }}</span>
          <ChevronRight class="w-4 h-4 text-[var(--c-text-3)]" />
        </RouterLink>
        <RouterLink
          to="/social/blocks"
          class="privacy-setting-row flex items-center justify-between min-h-11 px-4 py-3 no-underline text-inherit"
        >
          <span class="privacy-setting-label text-base">{{ t('social.blocksTitle') }}</span>
          <ChevronRight class="w-4 h-4 text-[var(--c-text-3)]" />
        </RouterLink>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ChevronRight, ChevronLeft } from 'lucide-vue-next'
import { getPrivacySettings, updatePrivacySettings } from '../../api/privacy.js'
import { useToast } from '@/composables/useToast'
import { createPrivacyItems } from './settingsContent'

const router = useRouter()
const { t } = useI18n()
const { success: toastSuccess } = useToast()

const privacyList = ref(createPrivacyItems(t).map(({ key, status }) => ({ key, status })))

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
  background: var(--c-bg);
}

.privacy-setting-back,
.privacy-setting-title,
.privacy-setting-label {
  color: var(--c-text-1);
}

.privacy-setting-card {
  background: var(--c-surface);
  border: 1px solid var(--c-border);
  box-shadow: none;
}

.privacy-setting-row + .privacy-setting-row {
  border-top: 1px solid var(--c-border-light);
}

.privacy-setting-switch {
  background: color-mix(in srgb, var(--c-text-3) 18%, var(--c-border));
}

.peer:checked + .privacy-setting-switch {
  background: var(--c-primary);
}
</style>
