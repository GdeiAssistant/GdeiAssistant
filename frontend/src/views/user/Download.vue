<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import request from '../../utils/request'
import { useToast } from '@/composables/useToast'
import {
  EXPORT_STATUS,
  getDownloadStatusDescription,
  getDownloadStatusTitle,
} from './downloadSupport'

const router = useRouter()
const { success: toastSuccess } = useToast()
const { t } = useI18n()

const exportStatus = ref(EXPORT_STATUS.NOT_EXPORT)
const loadingState = ref(true)
const exporting = ref(false)
const downloading = ref(false)
let pollTimerId = null

function stopPolling() {
  if (pollTimerId) {
    clearInterval(pollTimerId)
    pollTimerId = null
  }
}

function startPolling() {
  if (pollTimerId) return
  pollTimerId = setInterval(() => {
    void loadExportState()
  }, 3000)
}

async function loadExportState() {
  try {
    const res = await request.get('/userdata/state')
    const nextStatus = Number(res?.data ?? EXPORT_STATUS.NOT_EXPORT)
    exportStatus.value = nextStatus
    if (nextStatus === EXPORT_STATUS.EXPORTING) {
      startPolling()
    } else {
      stopPolling()
    }
  } finally {
    loadingState.value = false
  }
}

async function handleStartExport() {
  if (exporting.value || exportStatus.value === EXPORT_STATUS.EXPORTING) return
  exporting.value = true
  try {
    await request.post('/userdata/export')
    exportStatus.value = EXPORT_STATUS.EXPORTING
    startPolling()
    toastSuccess(t('downloadPage.toast.exportStarted'))
  } catch (e) {
    await loadExportState()
  } finally {
    exporting.value = false
  }
}

async function handleDownload() {
  if (downloading.value) return
  downloading.value = true
  try {
    const res = await request.post('/userdata/download')
    const url = typeof res?.data === 'string' ? res.data : ''
    if (!url) return
    const popup = window.open(url, '_blank', 'noopener')
    if (!popup) {
      window.location.href = url
    }
    toastSuccess(t('downloadPage.toast.downloadStarted'))
  } finally {
    downloading.value = false
  }
}

const statusTitle = computed(() => getDownloadStatusTitle(t, exportStatus.value, loadingState.value))
const statusDescription = computed(() => getDownloadStatusDescription(t, exportStatus.value, loadingState.value))

onMounted(async () => {
  await loadExportState()
})

onUnmounted(() => {
  stopPolling()
})
</script>

<template>
  <div class="min-h-screen bg-gray-50 pb-6">
    <div class="sticky top-0 z-10 flex items-center h-12 bg-white border-b border-gray-200 px-4">
      <button type="button" class="w-15 text-sm text-gray-700 text-left cursor-pointer" @click="router.back()">{{ t('about.back') }}</button>
      <h1 class="flex-1 text-center text-base font-medium text-gray-700 m-0">{{ t('profile.downloadData') }}</h1>
      <div class="w-15"></div>
    </div>

    <div class="max-w-lg mx-auto px-4 py-6">
      <div class="bg-white rounded-xl shadow-sm p-6 text-center mb-4">
        <div class="flex justify-center items-center w-20 h-20 mx-auto mb-5">
          <div v-if="loadingState || exportStatus === EXPORT_STATUS.EXPORTING" class="w-12 h-12 border-3 border-gray-200 border-t-green-500 rounded-full animate-spin"></div>
          <svg v-else-if="exportStatus === EXPORT_STATUS.EXPORTED" class="w-full h-full" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M20 6L9 17L4 12" stroke="#07c160" stroke-width="3" stroke-linecap="round" stroke-linejoin="round" />
          </svg>
          <svg v-else class="w-full h-full" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M14 2H6C5.46957 2 4.96086 2.21071 4.58579 2.58579C4.21071 2.96086 4 3.46957 4 4V20C4 20.5304 4.21071 21.0391 4.58579 21.4142C4.96086 21.7893 5.46957 22 6 22H18C18.5304 22 19.0391 21.7893 19.4142 21.4142C19.7893 21.0391 20 20.5304 20 20V8L14 2Z" stroke="#10aeff" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
            <path d="M14 2V8H20" stroke="#10aeff" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
            <path d="M16 13H8" stroke="#10aeff" stroke-width="2" stroke-linecap="round" />
            <path d="M16 17H8" stroke="#10aeff" stroke-width="2" stroke-linecap="round" />
            <path d="M10 9H8" stroke="#10aeff" stroke-width="2" stroke-linecap="round" />
          </svg>
        </div>

        <h2 class="text-lg font-medium text-gray-800 mb-5">{{ statusTitle }}</h2>
        <p class="text-sm leading-relaxed text-gray-500 text-left mb-3">{{ statusDescription }}</p>
        <p class="text-sm leading-relaxed text-gray-800 font-medium text-left mb-3">{{ t('downloadPage.privacyNotice') }}</p>
        <p class="text-[13px] leading-relaxed text-gray-400 text-left mt-4 pt-4 border-t border-gray-100">{{ t('downloadPage.frequencyNotice') }}</p>
      </div>

      <div class="bg-white rounded-xl shadow-sm p-5">
        <div v-if="loadingState">
          <button type="button" class="w-full rounded-lg bg-green-500 text-white font-medium py-2.5 opacity-60 cursor-not-allowed" disabled>
            {{ t('downloadPage.actions.checking') }}
          </button>
        </div>

        <div v-else-if="exportStatus === EXPORT_STATUS.NOT_EXPORT">
          <button
            type="button"
            class="w-full rounded-lg bg-green-500 text-white font-medium py-2.5 active:bg-green-600 cursor-pointer disabled:opacity-60 disabled:cursor-not-allowed"
            :disabled="exporting"
            @click="handleStartExport"
          >
            {{ exporting ? t('downloadPage.actions.starting') : t('downloadPage.actions.start') }}
          </button>
        </div>

        <div v-else-if="exportStatus === EXPORT_STATUS.EXPORTING" class="space-y-3">
          <button type="button" class="w-full rounded-lg bg-green-500 text-white font-medium py-2.5 opacity-60 cursor-not-allowed flex items-center justify-center" disabled>
            <span class="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin mr-2"></span>
            {{ t('downloadPage.actions.exporting') }}
          </button>
          <button
            type="button"
            class="w-full rounded-lg bg-white text-gray-700 font-medium py-2.5 border border-gray-300 cursor-pointer"
            @click="loadExportState"
          >{{ t('downloadPage.actions.refresh') }}</button>
        </div>

        <div v-else>
          <p class="text-sm text-green-500 text-center mb-3">&#10003; {{ t('downloadPage.exportedHint') }}</p>
          <button
            type="button"
            class="w-full rounded-lg bg-green-500 text-white font-medium py-2.5 active:bg-green-600 cursor-pointer disabled:opacity-60 disabled:cursor-not-allowed"
            :disabled="downloading"
            @click="handleDownload"
          >
            {{ downloading ? t('downloadPage.actions.downloading') : t('downloadPage.actions.download') }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
