<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '../../utils/request'
import { useScrollLoad } from '../../composables/useScrollLoad'
import CommunityHeader from '../../components/community/CommunityHeader.vue'

const router = useRouter()
const scrollContainer = ref({ get scrollTop() { return window.pageYOffset || document.documentElement.scrollTop } })

const PAGE_SIZE = 10
const fetchDeliveryData = async (page) => {
  const start = (page - 1) * PAGE_SIZE
  const res = await request.get(`/delivery/order/start/${start}/size/${PAGE_SIZE}`)
  const rawList = res?.data || []
  const list = Array.isArray(rawList) ? rawList.map((o) => ({
    id: o.orderId,
    status: o.state,
    reward: o.price ?? 0,
    time: o.orderTime,
    size: '小件',
    type: 'express',
    pickupAddress: o.company ? `${o.company} 取件` : '取件',
    deliveryAddress: o.address || ''
  })) : []
  return { list, hasMore: list.length >= PAGE_SIZE }
}

const { items: list, loading, finished, refreshing, pullY, loadData, handleTouchStart, handleTouchMove, handleTouchEnd } = useScrollLoad(fetchDeliveryData)

function goDetail(id) {
  router.push(`/delivery/detail/${id}`)
}

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

function onWindowScroll() {
  if (refreshing.value || loading.value || finished.value) return
  const scrollTop = window.pageYOffset || document.documentElement.scrollTop
  const winH = window.innerHeight
  const docH = document.documentElement.scrollHeight
  if (scrollTop + winH >= docH - 80) loadData()
}

onMounted(() => {
  loadData()
  window.addEventListener('scroll', onWindowScroll)
})
onUnmounted(() => {
  window.removeEventListener('scroll', onWindowScroll)
})
</script>

<template>
  <div
    class="min-h-screen bg-[var(--c-bg)]"
    @touchstart="handleTouchStart"
    @touchmove="handleTouchMove($event, scrollContainer)"
    @touchend="handleTouchEnd"
  >
    <CommunityHeader title="全民快递" moduleColor="#f59e0b" backTo="/" />

    <!-- Pull refresh -->
    <div class="flex items-center justify-center overflow-hidden text-sm text-[var(--c-text-3)]" :style="{ height: pullY + 'px' }">
      <span v-if="refreshing" class="flex items-center gap-2"><i class="w-5 h-5 border-2 border-[var(--c-border)] border-t-amber-500 rounded-full animate-spin"></i> 正在刷新...</span>
      <span v-else-if="pullY > 50">释放立即刷新</span>
      <span v-else-if="pullY > 0">下拉刷新</span>
    </div>

    <!-- Task cards -->
    <div class="p-4 flex flex-col gap-4">
      <div
        v-for="(item, index) in list"
        :key="item.id"
        class="bg-[var(--c-surface)] rounded-xl shadow-sm p-5 cursor-pointer animate-[slide-up_0.4s_ease_both]"
        :style="{ animationDelay: (index * 0.05) + 's' }"
        @click="goDetail(item.id)"
      >
        <!-- Header -->
        <div class="flex justify-between items-center mb-4">
          <div class="text-lg font-semibold text-[var(--c-text-1)]">{{ getTypeText(item.type) }}</div>
          <div class="flex items-baseline text-red-500">
            <span class="text-lg font-bold mr-0.5">&#xffe5;</span>
            <span class="text-2xl font-bold">{{ item.reward.toFixed(2) }}</span>
          </div>
        </div>

        <!-- Route -->
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

        <!-- Footer -->
        <div class="flex justify-between items-center pt-3 border-t border-[var(--c-border)]">
          <div class="flex gap-3 text-base text-[var(--c-text-3)]">
            <span>{{ item.time }}</span>
            <span>{{ item.size || '小件' }}</span>
          </div>
          <div class="px-3 py-1 rounded-full text-sm font-medium" :class="getStatusClass(item.status)">
            {{ getStatusText(item.status) }}
          </div>
        </div>
      </div>
    </div>

    <!-- Empty -->
    <div v-if="!loading && !refreshing && list.length === 0" class="flex flex-col items-center py-16 text-[var(--c-text-3)]">
      <div class="text-sm">暂无任务</div>
    </div>

    <!-- Loading -->
    <div v-if="loading && !refreshing" class="flex items-center justify-center gap-2 py-4 text-sm text-[var(--c-text-3)]">
      <i class="w-5 h-5 border-2 border-[var(--c-border)] border-t-amber-500 rounded-full animate-spin"></i> 正在加载
    </div>
    <div v-if="finished && list.length > 0" class="text-center py-4 text-sm text-[var(--c-text-3)]">没有更多了</div>
  </div>
</template>
