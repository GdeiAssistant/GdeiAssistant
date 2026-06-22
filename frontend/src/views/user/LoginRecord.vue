<template>
  <div class="login-record-page min-h-screen">
    <!-- Sticky Header -->
    <div class="login-record-header sticky top-0 z-10 flex items-center h-12 px-4">
      <button type="button" class="login-record-back w-15 text-base text-left cursor-pointer" @click="goBack">{{ t('common.back') }}</button>
      <div class="login-record-title flex-1 text-center text-lg font-medium">{{ t('profile.loginRecord') }}</div>
      <div class="w-15"></div>
    </div>

    <!-- Content -->
    <div class="max-w-lg mx-auto px-4 py-6">
      <!-- Loading -->
      <div v-if="isLoading" class="flex flex-col items-center justify-center py-16">
        <div class="login-record-loading w-5 h-5 border-2 border-gray-200 rounded-full animate-spin"></div>
        <p class="login-record-muted mt-3 text-sm">{{ t('common.loading') }}</p>
      </div>

      <!-- Empty -->
      <div v-else-if="records.length === 0" class="flex items-center justify-center py-16">
        <p class="login-record-muted text-sm">{{ t('loginRecord.empty') }}</p>
      </div>

      <!-- Records -->
      <div v-else class="login-record-card rounded-xl shadow-sm">
        <div v-for="record in records" :key="record.id" class="login-record-row px-4 py-4">
          <div class="flex justify-between items-center mb-1">
            <span class="login-record-time text-base font-medium">{{ record.loginTime }}</span>
            <span class="login-record-status text-sm">{{ t('loginRecord.success') }}</span>
          </div>
          <div class="login-record-meta text-[13px] leading-relaxed mt-0.5">
            {{ record.location }} · {{ record.ip }}
          </div>
          <div class="login-record-meta text-[13px] leading-relaxed mt-0.5">
            {{ record.device }}
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import request from '../../utils/request'

const router = useRouter()
const { t } = useI18n()

// 响应式数据
const records = ref([])
const isLoading = ref(true)

function formatTime(time) {
  if (!time) return ''
  try {
    const d = new Date(time)
    if (Number.isNaN(d.getTime())) return String(time)
    const y = d.getFullYear()
    const m = String(d.getMonth() + 1).padStart(2, '0')
    const day = String(d.getDate()).padStart(2, '0')
    const h = String(d.getHours()).padStart(2, '0')
    const mi = String(d.getMinutes()).padStart(2, '0')
    const s = String(d.getSeconds()).padStart(2, '0')
    return `${y}-${m}-${day} ${h}:${mi}:${s}`
  } catch {
    return String(time)
  }
}

// 加载数据
const loadRecords = async () => {
  isLoading.value = true
  try {
    const res = await request.get('/ip/start/0/size/20')
    const list = (res && res.data) || []
    records.value = list.map((item, index) => ({
      id: item.id ?? index,
      loginTime: formatTime(item.time),
      ip: item.ip || '',
      location: item.area || [item.country, item.province, item.city].filter(Boolean).join(''),
      device: item.network ? t('loginRecord.clientDevice', { network: item.network }) : t('loginRecord.unknownDevice')
    }))
  } catch (e) {
    records.value = []
  } finally {
    isLoading.value = false
  }
}

// 返回上一页
const goBack = () => {
  router.back()
}

// 组件挂载时加载数据
onMounted(() => {
  loadRecords()
})
</script>

<style scoped>
.login-record-page {
  background:
    radial-gradient(circle at top, color-mix(in srgb, var(--c-primary) 8%, transparent), transparent 30%),
    var(--c-bg-soft);
}

.login-record-header {
  background: color-mix(in srgb, var(--c-surface) 94%, var(--c-bg));
  border-bottom: 1px solid var(--c-border-light);
  backdrop-filter: blur(18px);
}

.login-record-back,
.login-record-title,
.login-record-time {
  color: var(--c-text-1);
}

.login-record-muted,
.login-record-meta {
  color: var(--c-text-3);
}

.login-record-loading {
  border-top-color: color-mix(in srgb, var(--c-primary) 82%, #2dd4bf);
}

.login-record-card {
  background: var(--c-surface);
  border: 1px solid color-mix(in srgb, var(--c-primary) 8%, var(--c-border-light));
  box-shadow: 0 12px 28px color-mix(in srgb, var(--c-primary) 8%, rgba(15, 23, 42, 0.06));
}

.login-record-row + .login-record-row {
  border-top: 1px solid var(--c-border-light);
}

.login-record-status {
  color: color-mix(in srgb, var(--c-primary) 76%, #14b8a6);
  font-weight: 600;
}

[data-theme="dark"] .login-record-page {
  background:
    radial-gradient(circle at top, color-mix(in srgb, var(--c-primary) 10%, transparent), transparent 30%),
    var(--c-bg);
}

[data-theme="dark"] .login-record-header {
  background: color-mix(in srgb, var(--c-surface) 88%, rgba(10, 20, 32, 0.9));
  border-bottom-color: rgba(68, 89, 112, 0.72);
}

[data-theme="dark"] .login-record-loading {
  border-top-color: color-mix(in srgb, var(--c-primary) 58%, #67e8f9);
}

[data-theme="dark"] .login-record-card {
  border-color: rgba(68, 89, 112, 0.72);
  box-shadow: 0 18px 36px rgba(0, 0, 0, 0.2);
}

[data-theme="dark"] .login-record-row + .login-record-row {
  border-top-color: rgba(68, 89, 112, 0.72);
}

[data-theme="dark"] .login-record-status {
  color: color-mix(in srgb, var(--c-primary) 58%, #67e8f9);
}
</style>
