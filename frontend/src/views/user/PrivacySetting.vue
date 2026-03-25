<template>
  <div class="min-h-screen bg-gray-50">
    <!-- Sticky Header -->
    <div class="sticky top-0 z-10 flex items-center h-12 bg-white border-b border-gray-200 px-4">
      <button type="button" class="w-15 text-base text-gray-700 text-left" @click="goBack">{{ t('common.back') }}</button>
      <div class="flex-1 text-center text-lg font-medium text-black">{{ t('profile.privacySetting') }}</div>
      <div class="w-15"></div>
    </div>

    <!-- Content -->
    <div class="max-w-lg mx-auto px-4 py-6">
      <div class="bg-white rounded-xl shadow-sm divide-y divide-gray-100">
        <label
          v-for="item in privacyList"
          :key="item.key"
          class="flex items-center justify-between px-4 py-3 cursor-pointer"
        >
          <span class="text-base text-gray-700">{{ item.name }}</span>
          <div class="relative inline-flex items-center">
            <input
              type="checkbox"
              class="sr-only peer"
              :checked="item.status"
              @change="handlePrivacyChange(item)"
            />
            <div class="w-11 h-6 bg-gray-200 rounded-full peer-checked:bg-green-500 transition-colors after:content-[''] after:absolute after:top-0.5 after:left-[2px] after:bg-white after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:after:translate-x-full"></div>
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
