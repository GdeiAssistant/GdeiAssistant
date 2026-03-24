<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import request from '../../utils/request'
import { useToast } from '@/composables/useToast'

const router = useRouter()
const { success: toastSuccess } = useToast()

const EXPORT_STATUS = {
  NOT_EXPORT: 0,
  EXPORTING: 1,
  EXPORTED: 2,
}

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
    toastSuccess('导出任务已提交，请稍候返回下载')
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
    toastSuccess('下载已开始')
  } finally {
    downloading.value = false
  }
}

const statusTitle = computed(() => {
  if (loadingState.value) return '正在检查导出状态'
  if (exportStatus.value === EXPORT_STATUS.EXPORTING) return '正在为您打包数据'
  if (exportStatus.value === EXPORT_STATUS.EXPORTED) return '数据副本已可下载'
  return '获取您的账号数据副本'
})

const statusDescription = computed(() => {
  if (loadingState.value) return '正在获取当前导出任务状态，请稍候。'
  if (exportStatus.value === EXPORT_STATUS.EXPORTING) {
    return '导出任务已提交，系统正在整理您的个人资料、发帖记录和订单信息。页面会自动刷新状态。'
  }
  if (exportStatus.value === EXPORT_STATUS.EXPORTED) {
    return '您的数据副本已准备就绪，可直接下载。下载链接有效期为 90 分钟。'
  }
  return '我们将把您在广东第二师范学院助手上的个人资料、互动内容、订单与记录打包成压缩文件。'
})

onMounted(async () => {
  await loadExportState()
})

onUnmounted(() => {
  stopPolling()
})
</script>

<template>
  <div class="min-h-screen bg-gray-50 pb-6">
    <!-- Sticky Header -->
    <div class="sticky top-0 z-10 flex items-center h-12 bg-white border-b border-gray-200 px-4">
      <button type="button" class="w-15 text-sm text-gray-700 text-left cursor-pointer" @click="router.back()">返回</button>
      <h1 class="flex-1 text-center text-base font-medium text-gray-700 m-0">下载个人数据</h1>
      <div class="w-15"></div>
    </div>

    <div class="max-w-lg mx-auto px-4 py-6">
      <!-- Info card -->
      <div class="bg-white rounded-xl shadow-sm p-6 text-center mb-4">
        <!-- Status icon -->
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
        <p class="text-sm leading-relaxed text-gray-800 font-medium text-left mb-3">为了保护您的隐私，该文件仅供您本人下载，下载链接有效期为 90 分钟。</p>
        <p class="text-[13px] leading-relaxed text-gray-400 text-left mt-4 pt-4 border-t border-gray-100">注意：24 小时内只能导出一次。若您刚完成导出，后续可直接回到本页继续下载。</p>
      </div>

      <!-- Action area -->
      <div class="bg-white rounded-xl shadow-sm p-5">
        <!-- Loading state -->
        <div v-if="loadingState">
          <button type="button" class="w-full rounded-lg bg-green-500 text-white font-medium py-2.5 opacity-60 cursor-not-allowed" disabled>
            正在检查状态...
          </button>
        </div>

        <!-- Not exported -->
        <div v-else-if="exportStatus === EXPORT_STATUS.NOT_EXPORT">
          <button
            type="button"
            class="w-full rounded-lg bg-green-500 text-white font-medium py-2.5 active:bg-green-600 cursor-pointer disabled:opacity-60 disabled:cursor-not-allowed"
            :disabled="exporting"
            @click="handleStartExport"
          >
            {{ exporting ? '提交中...' : '开始导出数据' }}
          </button>
        </div>

        <!-- Exporting -->
        <div v-else-if="exportStatus === EXPORT_STATUS.EXPORTING" class="space-y-3">
          <button type="button" class="w-full rounded-lg bg-green-500 text-white font-medium py-2.5 opacity-60 cursor-not-allowed flex items-center justify-center" disabled>
            <span class="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin mr-2"></span>
            数据打包中，请稍候...
          </button>
          <button
            type="button"
            class="w-full rounded-lg bg-white text-gray-700 font-medium py-2.5 border border-gray-300 cursor-pointer"
            @click="loadExportState"
          >刷新状态</button>
        </div>

        <!-- Exported -->
        <div v-else>
          <p class="text-sm text-green-500 text-center mb-3">&#10003; 您的数据副本已生成完毕，请点击下方按钮下载</p>
          <button
            type="button"
            class="w-full rounded-lg bg-green-500 text-white font-medium py-2.5 active:bg-green-600 cursor-pointer disabled:opacity-60 disabled:cursor-not-allowed"
            :disabled="downloading"
            @click="handleDownload"
          >
            {{ downloading ? '正在获取下载链接...' : '点击下载文件' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
