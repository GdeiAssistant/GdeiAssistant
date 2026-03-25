<template>
  <div class="min-h-screen bg-gray-50">
    <!-- Sticky Header -->
    <div class="sticky top-0 z-10 flex items-center h-12 bg-white border-b border-gray-200 px-4">
      <button type="button" class="w-15 text-base text-gray-700 text-left cursor-pointer" @click="goBack">{{ t('common.back') }}</button>
      <div class="flex-1 text-center text-lg font-medium text-black">{{ t('profile.loginRecord') }}</div>
      <div class="w-15"></div>
    </div>

    <!-- Content -->
    <div class="max-w-lg mx-auto px-4 py-6">
      <!-- Loading -->
      <div v-if="isLoading" class="flex flex-col items-center justify-center py-16">
        <div class="w-5 h-5 border-2 border-gray-200 border-t-green-500 rounded-full animate-spin"></div>
        <p class="mt-3 text-sm text-gray-400">{{ t('common.loading') }}</p>
      </div>

      <!-- Empty -->
      <div v-else-if="records.length === 0" class="flex items-center justify-center py-16">
        <p class="text-sm text-gray-400">{{ t('loginRecord.empty') }}</p>
      </div>

      <!-- Records -->
      <div v-else class="bg-white rounded-xl shadow-sm divide-y divide-gray-100">
        <div v-for="record in records" :key="record.id" class="px-4 py-4">
          <div class="flex justify-between items-center mb-1">
            <span class="text-base font-medium text-gray-700">{{ record.loginTime }}</span>
            <span class="text-sm text-green-500">{{ t('loginRecord.success') }}</span>
          </div>
          <div class="text-[13px] text-gray-400 leading-relaxed mt-0.5">
            {{ record.location }} · {{ record.ip }}
          </div>
          <div class="text-[13px] text-gray-400 leading-relaxed mt-0.5">
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
