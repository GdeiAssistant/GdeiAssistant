<template>
  <div class="subpage login-record-page min-h-screen">
    <!-- Sticky Header -->
    <div class="subpage-bar login-record-header">
      <button type="button" class="subpage-bar__back login-record-back" @click="goBack">
        <ChevronLeft :size="18" aria-hidden="true" />
        <span>{{ t('common.back') }}</span>
      </button>
      <div class="subpage-bar__title login-record-title">{{ t('profile.loginRecord') }}</div>
      <span aria-hidden="true"></span>
    </div>

    <!-- Content -->
    <div class="subpage-body max-w-lg mx-auto px-4 py-6">
      <!-- Loading -->
      <div v-if="isLoading" class="flex flex-col items-center justify-center py-16">
        <div class="login-record-loading w-5 h-5 border-2 border-[var(--c-border)] rounded-full animate-spin"></div>
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
import { ChevronLeft } from 'lucide-vue-next'
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import request from '../../utils/request'
import { getLocationCatalog } from '../../catalog/locationCatalog'

const router = useRouter()
const { t, locale } = useI18n()

// 响应式数据
const rawRecords = ref([])
const records = computed(() => {
  const catalog = getLocationCatalog(locale.value)
  return rawRecords.value.map((item, index) => ({
    id: item.id ?? index,
    loginTime: formatTime(item.time),
    ip: item.ip || '',
    location: catalog.systemAreaLabel(item.area || [item.country, item.province, item.city].filter(Boolean).join(' ')),
    device: item.network ? t('loginRecord.clientDevice', { network: item.network }) : t('loginRecord.unknownDevice')
  }))
})
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
    rawRecords.value = list
  } catch (e) {
    rawRecords.value = []
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
  background: var(--c-bg);
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
  border-top-color: var(--c-primary);
}

.login-record-card {
  background: var(--c-surface);
  border: 1px solid var(--c-border);
  box-shadow: none;
}

.login-record-row + .login-record-row {
  border-top: 1px solid var(--c-border-light);
}

.login-record-status {
  color: var(--c-primary);
  font-weight: 600;
}
</style>
