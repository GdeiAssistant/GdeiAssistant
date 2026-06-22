<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import request from '../../utils/request'
import { useScrollLoad } from '../../composables/useScrollLoad'
import CommunityHeader from '../../components/community/CommunityHeader.vue'
import AppEmpty from '@/components/ui/AppEmpty.vue'
import { Send } from 'lucide-vue-next'
import { createCommunityPullMessages, createDeliveryStatusMap, createDeliveryTypeMap } from '../community/communityContent'
import { maskAddress } from '@/utils/mask'

const router = useRouter()
const { t } = useI18n()
const scrollContainer = ref({ get scrollTop() { return window.pageYOffset || document.documentElement.scrollTop } })
const pullMessages = computed(() => createCommunityPullMessages(t))
const deliveryStatusMap = computed(() => createDeliveryStatusMap(t))
const deliveryTypeMap = computed(() => createDeliveryTypeMap(t))

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
    size: t('delivery.smallSize'),
    type: 'express',
    pickupAddress: o.company ? t('delivery.pickupAddressWithCompany', { company: o.company }) : t('delivery.pickupShort'),
    deliveryAddress: maskAddress(o.address || '')
  })) : []
  return { list, hasMore: list.length >= PAGE_SIZE }
}

const { items: list, loading, finished, refreshing, pullY, loadData, handleTouchStart, handleTouchMove, handleTouchEnd } = useScrollLoad(fetchDeliveryData)

function goDetail(id) {
  router.push(`/delivery/detail/${id}`)
}

function getStatusText(status) {
  return deliveryStatusMap.value[status] || t('delivery.unknownStatus')
}

function getStatusClass(status) {
  if (status === 0) return 'community-delivery-status-badge community-delivery-status-badge--pending'
  if (status === 1) return 'community-delivery-status-badge community-delivery-status-badge--active'
  if (status === 2) return 'community-delivery-status-badge community-delivery-status-badge--completed'
  return 'community-delivery-status-badge'
}

function getTypeText(type) {
  return deliveryTypeMap.value[type] || t('delivery.type.other')
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
    class="community-delivery-page min-h-screen bg-[var(--c-bg)]"
    style="--module-color: var(--c-delivery)"
    @touchstart="handleTouchStart"
    @touchmove="handleTouchMove($event, scrollContainer)"
    @touchend="handleTouchEnd"
  >
    <CommunityHeader :title="t('delivery.title')" moduleColor="var(--c-delivery)" backTo="/" />

    <!-- Pull refresh -->
    <div class="flex items-center justify-center overflow-hidden text-sm text-[var(--c-text-3)]" :style="{ height: pullY + 'px' }">
      <span v-if="refreshing" class="flex items-center gap-2"><i class="w-5 h-5 border-2 border-[var(--c-border)] border-t-[var(--c-delivery)] rounded-full animate-spin"></i> {{ pullMessages.refreshing }}</span>
      <span v-else-if="pullY > 50">{{ pullMessages.releaseToRefresh }}</span>
      <span v-else-if="pullY > 0">{{ pullMessages.pullToRefresh }}</span>
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
          <div class="community-delivery-reward-block flex items-baseline">
            <span class="text-lg font-bold mr-0.5">&#xffe5;</span>
            <span class="text-2xl font-bold">{{ item.reward.toFixed(2) }}</span>
          </div>
        </div>

        <!-- Route -->
        <div class="mb-4 p-3 bg-[var(--c-bg)] rounded-lg">
          <div class="flex items-center mb-2.5">
            <span class="community-delivery-pickup-badge w-6 h-6 rounded-full flex items-center justify-center text-sm font-bold text-white mr-2.5 shrink-0">{{ t('delivery.pickupBadge') }}</span>
            <span class="flex-1 text-base text-[var(--c-text-1)] leading-relaxed">{{ item.pickupAddress }}</span>
          </div>
          <div class="flex items-center">
            <span class="community-delivery-dropoff-badge w-6 h-6 rounded-full flex items-center justify-center text-sm font-bold text-white mr-2.5 shrink-0">{{ t('delivery.deliveryBadge') }}</span>
            <span class="flex-1 text-base text-[var(--c-text-1)] leading-relaxed">{{ item.deliveryAddress }}</span>
          </div>
        </div>

        <!-- Footer -->
        <div class="flex justify-between items-center pt-3 border-t border-[var(--c-border)]">
          <div class="flex gap-3 text-base text-[var(--c-text-3)]">
            <span>{{ item.time }}</span>
            <span>{{ item.size || t('delivery.smallSize') }}</span>
          </div>
          <div class="px-3 py-1 rounded-full text-sm font-medium" :class="getStatusClass(item.status)">
            {{ getStatusText(item.status) }}
          </div>
        </div>
      </div>
    </div>

    <!-- Empty -->
    <div v-if="!loading && !refreshing && list.length === 0" class="community-delivery-empty-shell">
      <AppEmpty
        :title="t('delivery.empty')"
        :action-text="t('delivery.publish.title')"
        accent="var(--c-delivery)"
        action-variant="primary"
        @action="router.push('/delivery/publish')"
      >
        <template #icon>
          <Send class="community-delivery-empty-icon" aria-hidden="true" />
        </template>
      </AppEmpty>
    </div>

    <!-- Loading -->
    <div v-if="loading && !refreshing" class="flex items-center justify-center gap-2 py-4 text-sm text-[var(--c-text-3)]">
      <i class="w-5 h-5 border-2 border-[var(--c-border)] border-t-[var(--c-delivery)] rounded-full animate-spin"></i> {{ pullMessages.loading }}
    </div>
    <div v-if="finished && list.length > 0" class="text-center py-4 text-sm text-[var(--c-text-3)]">{{ pullMessages.noMore }}</div>
  </div>
</template>

<style scoped>
.community-delivery-reward-block {
  color: color-mix(in srgb, var(--c-delivery) 74%, #d97706);
}

.community-delivery-pickup-badge {
  background: color-mix(in srgb, var(--c-delivery) 68%, #0ea5e9);
}

.community-delivery-dropoff-badge {
  background: color-mix(in srgb, var(--c-delivery) 88%, #f59e0b);
}

.community-delivery-status-badge {
  border: 1px solid color-mix(in srgb, var(--c-delivery) 18%, var(--c-border));
}

.community-delivery-status-badge--pending {
  background: color-mix(in srgb, var(--c-delivery) 14%, var(--c-surface));
  color: color-mix(in srgb, var(--c-delivery) 80%, #b45309);
}

.community-delivery-status-badge--active {
  background: color-mix(in srgb, var(--c-delivery) 18%, #e0f2fe);
  color: color-mix(in srgb, var(--c-delivery) 70%, #0f766e);
}

.community-delivery-status-badge--completed {
  background: color-mix(in srgb, var(--c-delivery) 16%, #ecfdf5);
  color: color-mix(in srgb, var(--c-delivery) 76%, #0f766e);
}

.community-delivery-empty-shell {
  margin: 16px;
  border: 1px solid color-mix(in srgb, var(--c-delivery) 10%, var(--c-border));
  border-radius: 24px;
  background: color-mix(in srgb, var(--c-delivery) 3%, var(--c-surface));
  box-shadow: 0 18px 36px color-mix(in srgb, var(--c-delivery) 8%, transparent);
}

.community-delivery-empty-icon {
  width: 28px;
  height: 28px;
}

[data-theme="dark"] .community-delivery-reward-block {
  color: color-mix(in srgb, var(--c-delivery) 72%, #fde68a);
}

[data-theme="dark"] .community-delivery-pickup-badge {
  background: color-mix(in srgb, var(--c-delivery) 42%, #38bdf8);
}

[data-theme="dark"] .community-delivery-dropoff-badge {
  background: color-mix(in srgb, var(--c-delivery) 70%, #fbbf24);
}

[data-theme="dark"] .community-delivery-status-badge {
  border-color: rgba(68, 89, 112, 0.72);
}

[data-theme="dark"] .community-delivery-status-badge--pending {
  background: rgba(36, 52, 69, 0.88);
  color: color-mix(in srgb, var(--c-delivery) 72%, #fde68a);
}

[data-theme="dark"] .community-delivery-status-badge--active {
  background: color-mix(in srgb, var(--c-delivery) 18%, rgba(24, 38, 53, 0.9));
  color: color-mix(in srgb, var(--c-delivery) 50%, #bfdbfe);
}

[data-theme="dark"] .community-delivery-status-badge--completed {
  background: color-mix(in srgb, var(--c-delivery) 20%, rgba(24, 38, 53, 0.9));
  color: color-mix(in srgb, var(--c-delivery) 58%, #ccfbf1);
}

[data-theme="dark"] .community-delivery-empty-shell {
  border-color: color-mix(in srgb, var(--c-delivery) 12%, rgba(111, 132, 156, 0.44));
  background: rgba(24, 38, 53, 0.86);
  box-shadow: 0 20px 38px rgba(0, 0, 0, 0.2);
}
</style>
