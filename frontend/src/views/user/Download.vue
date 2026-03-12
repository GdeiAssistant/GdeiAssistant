<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import request from '../../utils/request'

const router = useRouter()

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

function showToast(message) {
  const toast = document.createElement('div')
  toast.style.cssText =
    'position:fixed;left:50%;top:50%;transform:translate(-50%,-50%);background:rgba(0,0,0,0.75);color:#fff;padding:12px 22px;border-radius:6px;z-index:9999;font-size:14px;max-width:80%;text-align:center;'
  toast.textContent = message
  document.body.appendChild(toast)
  setTimeout(() => {
    if (toast.parentNode) document.body.removeChild(toast)
  }, 2000)
}

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
    showToast('导出任务已提交，请稍候返回下载')
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
    showToast('下载已开始')
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
  <div class="download-data-page">
    <div class="download-header unified-header">
      <span class="download-header__back" @click="router.back()">返回</span>
      <h1 class="download-header__title">下载个人数据</h1>
      <span class="download-header__placeholder"></span>
    </div>

    <div class="download-content">
      <div class="info-card">
        <div class="status-icon-wrapper">
          <i v-if="loadingState || exportStatus === EXPORT_STATUS.EXPORTING" class="weui-loading weui-icon_toast"></i>
          <svg v-else-if="exportStatus === EXPORT_STATUS.EXPORTED" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M20 6L9 17L4 12" stroke="#07c160" stroke-width="3" stroke-linecap="round" stroke-linejoin="round" />
          </svg>
          <svg v-else viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M14 2H6C5.46957 2 4.96086 2.21071 4.58579 2.58579C4.21071 2.96086 4 3.46957 4 4V20C4 20.5304 4.21071 21.0391 4.58579 21.4142C4.96086 21.7893 5.46957 22 6 22H18C18.5304 22 19.0391 21.7893 19.4142 21.4142C19.7893 21.0391 20 20.5304 20 20V8L14 2Z" stroke="#10aeff" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
            <path d="M14 2V8H20" stroke="#10aeff" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
            <path d="M16 13H8" stroke="#10aeff" stroke-width="2" stroke-linecap="round" />
            <path d="M16 17H8" stroke="#10aeff" stroke-width="2" stroke-linecap="round" />
            <path d="M10 9H8" stroke="#10aeff" stroke-width="2" stroke-linecap="round" />
          </svg>
        </div>
        <h2 class="hero-title">{{ statusTitle }}</h2>
        <p class="info-text">
          {{ statusDescription }}
        </p>
        <p class="info-text info-text--highlight">
          为了保护您的隐私，该文件仅供您本人下载，下载链接有效期为 90 分钟。
        </p>
        <p class="info-text info-text--note">
          注意：24 小时内只能导出一次。若您刚完成导出，后续可直接回到本页继续下载。
        </p>
      </div>

      <div class="action-section">
        <div v-if="loadingState" class="action-card">
          <div class="weui-btn-area">
            <a href="javascript:;" class="weui-btn weui-btn_primary weui-btn_disabled" style="cursor: not-allowed;">
              正在检查状态...
            </a>
          </div>
        </div>

        <div v-else-if="exportStatus === EXPORT_STATUS.NOT_EXPORT" class="action-card">
          <div class="weui-btn-area">
            <a href="javascript:;" class="weui-btn weui-btn_primary" :class="{ 'weui-btn_disabled': exporting }" @click.prevent="handleStartExport">
              {{ exporting ? '提交中...' : '开始导出数据' }}
            </a>
          </div>
        </div>

        <div v-else-if="exportStatus === EXPORT_STATUS.EXPORTING" class="action-card">
          <div class="weui-btn-area">
            <a href="javascript:;" class="weui-btn weui-btn_primary weui-btn_disabled" style="cursor: not-allowed;">
              <span class="btn-loading">
                <span class="weui-loading weui-icon_toast"></span>
                <span style="margin-left: 8px;">数据打包中，请稍候...</span>
              </span>
            </a>
          </div>
          <div class="weui-btn-area secondary-action">
            <a href="javascript:;" class="weui-btn weui-btn_default" @click.prevent="loadExportState">刷新状态</a>
          </div>
        </div>

        <div v-else class="action-card">
          <div class="success-message">
            <p class="success-text">✅ 您的数据副本已生成完毕，请点击下方按钮下载</p>
          </div>
          <div class="weui-btn-area">
            <a href="javascript:;" class="weui-btn weui-btn_primary" :class="{ 'weui-btn_disabled': downloading }" @click.prevent="handleDownload">
              {{ downloading ? '正在获取下载链接...' : '点击下载文件' }}
            </a>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.download-data-page {
  background: #f8f8f8;
  min-height: 100vh;
  padding-bottom: 24px;
}

.download-header.unified-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 44px;
  padding: 0 12px;
  background: #ffffff;
  border-bottom: 1px solid #e5e5e5;
}

.download-header__back {
  font-size: 14px;
  color: #333;
  cursor: pointer;
  min-width: 48px;
}

.download-header__title {
  flex: 1;
  text-align: center;
  font-size: 16px;
  font-weight: 500;
  margin: 0;
  color: #333;
}

.download-header__placeholder {
  min-width: 48px;
}

.download-content {
  padding: 0;
}

.info-card {
  background: #ffffff;
  padding: 30px 15px 20px;
  margin-bottom: 10px;
  border-top: 1px solid #e5e5e5;
  border-bottom: 1px solid #e5e5e5;
  text-align: center;
}

.status-icon-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  margin: 30px auto 20px;
  width: 80px !important;
  height: 80px !important;
  overflow: visible !important;
  box-sizing: border-box;
}

.status-icon-wrapper svg,
.status-icon-wrapper img {
  width: 100% !important;
  height: 100% !important;
  max-width: 80px !important;
  max-height: 80px !important;
  object-fit: contain;
  display: block;
}

.status-icon-wrapper .weui-loading {
  width: 80px !important;
  height: 80px !important;
  font-size: 80px !important;
  color: #07c160;
  transform: scale(0.8) !important;
  transform-origin: center center;
}

.hero-title {
  font-size: 18px;
  font-weight: 500;
  color: #333;
  margin: 0 0 20px;
}

.info-text {
  font-size: 14px;
  line-height: 1.6;
  color: #666;
  margin: 0 0 12px;
  text-align: left;
}

.info-text:last-child {
  margin-bottom: 0;
}

.info-text--highlight {
  color: #333;
  font-weight: 500;
}

.info-text--note {
  font-size: 13px;
  color: #999;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #e5e5e5;
}

.action-section {
  padding: 0 15px;
}

.action-card {
  background: #fff;
  border-radius: 8px;
  padding: 18px 15px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.secondary-action {
  margin-top: 10px;
}

.success-message {
  margin-bottom: 12px;
}

.success-text {
  margin: 0;
  font-size: 14px;
  color: #07c160;
  text-align: center;
}

.btn-loading {
  display: inline-flex;
  align-items: center;
  justify-content: center;
}
</style>
