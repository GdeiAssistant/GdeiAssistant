<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '../../utils/request'
import CommunityHeader from '../../components/community/CommunityHeader.vue'

const router = useRouter()
const route = useRoute()
const activeTab = ref('published') // published, accepted
const publishedList = ref([])
const acceptedList = ref([])
const loading = ref(false)

function getStatusText(status) {
  const map = { 0: '待接单', 1: '配送中', 2: '已完成' }
  return map[status] || '未知'
}

function getStatusClass(status) {
  if (status === 0) return 'bg-amber-100 text-amber-800'
  if (status === 1) return 'bg-blue-100 text-blue-800'
  if (status === 2) return 'bg-green-100 text-green-800'
  return ''
}

function getTypeText(type) {
  const map = { 'express': '代取快递', 'food': '买饭', 'other': '跑腿' }
  return map[type] || '跑腿'
}

function getQueryTab() {
  const value = route.query.tab
  if (Array.isArray(value)) {
    return value[0] || ''
  }
  return typeof value === 'string' ? value : ''
}

function normalizeTab(tab) {
  return tab === 'accepted' ? 'accepted' : 'published'
}

function applyRouteState() {
  activeTab.value = normalizeTab(getQueryTab())
}

function switchTab(tab) {
  const normalized = normalizeTab(tab)
  if (activeTab.value === normalized && getQueryTab() === normalized) {
    return
  }
  router.replace({
    path: '/delivery/mine',
    query: {
      ...route.query,
      tab: normalized
    }
  })
}

function goDetail(id) {
  router.push(`/delivery/detail/${id}`)
}


async function loadData() {
  loading.value = true
  try {
    const res = await request.get('/delivery/mine')
    const data = res?.data || {}
    const published = data.published || []
    const accepted = data.accepted || []
    publishedList.value = Array.isArray(published) ? published.map((o) => ({
      id: o.orderId,
      orderId: o.orderId,
      status: o.state,
      state: o.state,
      reward: o.price ?? 0,
      time: o.orderTime,
      type: 'express',
      pickupAddress: o.company ? `${o.company} 取件` : '取件',
      deliveryAddress: o.address || ''
    })) : []
    acceptedList.value = Array.isArray(accepted) ? accepted.map((o) => ({
      id: o.orderId,
      orderId: o.orderId,
      status: o.state,
      state: o.state,
      reward: o.price ?? 0,
      time: o.orderTime,
      type: 'express',
      pickupAddress: o.company ? `${o.company} 取件` : '取件',
      deliveryAddress: o.address || ''
    })) : []
  } catch (e) {
    publishedList.value = []
    acceptedList.value = []
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  applyRouteState()
  loadData()
})

watch(() => route.fullPath, () => {
  applyRouteState()
  loadData()
})
</script>

<template>
  <div class="min-h-screen bg-[var(--c-bg)] pb-16" style="--module-color: #f59e0b">
    <CommunityHeader title="我的跑腿" moduleColor="#f59e0b" backTo="/" />

    <!-- Tabs -->
    <div class="flex bg-[var(--c-card)] border-b border-[var(--c-divider)] px-4">
      <div
        class="flex-1 text-center py-3 text-base cursor-pointer relative transition-all duration-300"
        :class="activeTab === 'published' ? 'text-amber-500 font-medium' : 'text-[var(--c-text-2)]'"
        @click="switchTab('published')"
      >
        我发布的
        <div v-if="activeTab === 'published'" class="absolute bottom-0 left-0 right-0 h-[3px] bg-amber-500 rounded-t"></div>
      </div>
      <div
        class="flex-1 text-center py-3 text-base cursor-pointer relative transition-all duration-300"
        :class="activeTab === 'accepted' ? 'text-amber-500 font-medium' : 'text-[var(--c-text-2)]'"
        @click="switchTab('accepted')"
      >
        我接的单
        <div v-if="activeTab === 'accepted'" class="absolute bottom-0 left-0 right-0 h-[3px] bg-amber-500 rounded-t"></div>
      </div>
    </div>

    <!-- Published list -->
    <div v-if="activeTab === 'published'" class="p-4 animate-[fade-in_0.3s_ease_both]">
      <div v-if="loading" class="flex items-center justify-center gap-2 py-4 text-sm text-[var(--c-text-3)]">
        <i class="w-5 h-5 border-2 border-[var(--c-border)] border-t-amber-500 rounded-full animate-spin"></i> 加载中
      </div>
      <div v-else-if="publishedList.length === 0" class="flex flex-col items-center py-16 text-[var(--c-text-3)]">
        <div class="text-sm">暂无发布的任务</div>
      </div>
      <div v-else class="flex flex-col gap-4">
        <div
          v-for="(item, index) in publishedList"
          :key="item.id"
          class="bg-[var(--c-surface)] rounded-xl shadow-sm p-5 cursor-pointer animate-[slide-up_0.4s_ease_both]"
          :style="{ animationDelay: (index * 0.05) + 's' }"
          @click="goDetail(item.id)"
        >
          <div class="flex justify-between items-center mb-4">
            <div class="text-lg font-semibold text-[var(--c-text-1)]">{{ getTypeText(item.type) }}</div>
            <div class="flex items-baseline text-red-500">
              <span class="text-lg font-bold mr-0.5">&#xffe5;</span>
              <span class="text-2xl font-bold">{{ item.reward.toFixed(2) }}</span>
            </div>
          </div>
          <div class="mb-4 p-3 bg-[var(--c-bg)] rounded-lg">
            <div class="flex items-center mb-2.5">
              <span class="w-6 h-6 rounded-full flex items-center justify-center text-sm font-bold text-white mr-2.5 shrink-0 bg-blue-500">取</span>
              <span class="flex-1 text-base text-[var(--c-text-1)] leading-relaxed">{{ item.pickupAddress }}</span>
            </div>
            <div class="flex items-center">
              <span class="w-6 h-6 rounded-full flex items-center justify-center text-sm font-bold text-white mr-2.5 shrink-0 bg-amber-500">送</span>
              <span class="flex-1 text-base text-[var(--c-text-1)] leading-relaxed">{{ item.deliveryAddress }}</span>
            </div>
          </div>
          <div class="flex justify-between items-center pt-3 border-t border-[var(--c-border)]">
            <div class="flex gap-3 text-base text-[var(--c-text-3)]">
              <span>{{ item.time }}</span>
            </div>
            <div class="px-3 py-1 rounded-full text-sm font-medium" :class="getStatusClass(item.status)">
              {{ getStatusText(item.status) }}
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Accepted list -->
    <div v-if="activeTab === 'accepted'" class="p-4 animate-[fade-in_0.3s_ease_both]">
      <div v-if="loading" class="flex items-center justify-center gap-2 py-4 text-sm text-[var(--c-text-3)]">
        <i class="w-5 h-5 border-2 border-[var(--c-border)] border-t-amber-500 rounded-full animate-spin"></i> 加载中
      </div>
      <div v-else-if="acceptedList.length === 0" class="flex flex-col items-center py-16 text-[var(--c-text-3)]">
        <div class="text-sm">暂无接单的任务</div>
      </div>
      <div v-else class="flex flex-col gap-4">
        <div
          v-for="(item, index) in acceptedList"
          :key="item.id"
          class="bg-[var(--c-surface)] rounded-xl shadow-sm p-5 cursor-pointer animate-[slide-up_0.4s_ease_both]"
          :style="{ animationDelay: (index * 0.05) + 's' }"
          @click="goDetail(item.id)"
        >
          <div class="flex justify-between items-center mb-4">
            <div class="text-lg font-semibold text-[var(--c-text-1)]">{{ getTypeText(item.type) }}</div>
            <div class="flex items-baseline text-red-500">
              <span class="text-lg font-bold mr-0.5">&#xffe5;</span>
              <span class="text-2xl font-bold">{{ item.reward.toFixed(2) }}</span>
            </div>
          </div>
          <div class="mb-4 p-3 bg-[var(--c-bg)] rounded-lg">
            <div class="flex items-center mb-2.5">
              <span class="w-6 h-6 rounded-full flex items-center justify-center text-sm font-bold text-white mr-2.5 shrink-0 bg-blue-500">取</span>
              <span class="flex-1 text-base text-[var(--c-text-1)] leading-relaxed">{{ item.pickupAddress }}</span>
            </div>
            <div class="flex items-center">
              <span class="w-6 h-6 rounded-full flex items-center justify-center text-sm font-bold text-white mr-2.5 shrink-0 bg-amber-500">送</span>
              <span class="flex-1 text-base text-[var(--c-text-1)] leading-relaxed">{{ item.deliveryAddress }}</span>
            </div>
          </div>
          <div class="flex justify-between items-center pt-3 border-t border-[var(--c-border)]">
            <div class="flex gap-3 text-base text-[var(--c-text-3)]">
              <span>{{ item.time }}</span>
            </div>
            <div class="px-3 py-1 rounded-full text-sm font-medium" :class="getStatusClass(item.status)">
              {{ getStatusText(item.status) }}
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
