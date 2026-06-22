<script setup>
import { ref, onMounted, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute, useRouter } from 'vue-router'
import request from '../../utils/request'
import CommunityHeader from '../../components/community/CommunityHeader.vue'
import AppEmpty from '@/components/ui/AppEmpty.vue'
import { createDeliveryStatusMap, createDeliveryTypeMap } from '../community/communityContent'

const router = useRouter()
const route = useRoute()
const { t } = useI18n()
const activeTab = ref('published') // published, accepted
const publishedList = ref([])
const acceptedList = ref([])
const loading = ref(false)

function getStatusText(status) {
  const map = createDeliveryStatusMap(t)
  return map[status] || t('delivery.unknownStatus')
}

function getStatusClass(status) {
  if (status === 0) return 'community-delivery-status-badge community-delivery-status-badge--pending'
  if (status === 1) return 'community-delivery-status-badge community-delivery-status-badge--active'
  if (status === 2) return 'community-delivery-status-badge community-delivery-status-badge--completed'
  return 'community-delivery-status-badge'
}

function getTypeText(type) {
  const map = createDeliveryTypeMap(t)
  return map[type] || t('delivery.type.other')
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
      pickupAddress: o.company ? t('delivery.pickupAddressWithCompany', { company: o.company }) : t('delivery.pickupShort'),
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
      pickupAddress: o.company ? t('delivery.pickupAddressWithCompany', { company: o.company }) : t('delivery.pickupShort'),
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
  <div class="community-delivery-page min-h-screen bg-[var(--c-bg)] pb-16" style="--module-color: var(--c-delivery)">
    <CommunityHeader :title="t('delivery.mine.title')" moduleColor="var(--c-delivery)" backTo="/" />

    <!-- Tabs -->
    <div class="flex bg-[var(--c-card)] border-b border-[var(--c-divider)] px-4">
      <div
        class="community-delivery-tab flex-1 text-center py-3 text-base cursor-pointer relative transition-all duration-300"
        :class="{ 'community-delivery-tab--active': activeTab === 'published' }"
        @click="switchTab('published')"
      >
        {{ t('delivery.mine.publishedTab') }}
        <div v-if="activeTab === 'published'" class="community-delivery-tab__indicator absolute bottom-0 left-0 right-0 h-[3px] rounded-t"></div>
      </div>
      <div
        class="community-delivery-tab flex-1 text-center py-3 text-base cursor-pointer relative transition-all duration-300"
        :class="{ 'community-delivery-tab--active': activeTab === 'accepted' }"
        @click="switchTab('accepted')"
      >
        {{ t('delivery.mine.acceptedTab') }}
        <div v-if="activeTab === 'accepted'" class="community-delivery-tab__indicator absolute bottom-0 left-0 right-0 h-[3px] rounded-t"></div>
      </div>
    </div>

    <!-- Published list -->
    <div v-if="activeTab === 'published'" class="p-4 animate-[fade-in_0.3s_ease_both]">
      <div v-if="loading" class="flex items-center justify-center gap-2 py-4 text-sm text-[var(--c-text-3)]">
        <i class="w-5 h-5 border-2 border-[var(--c-border)] border-t-[var(--c-delivery)] rounded-full animate-spin"></i> {{ t('common.loading') }}
      </div>
      <div v-else-if="publishedList.length === 0" class="community-delivery-empty-shell">
        <AppEmpty
          :title="t('delivery.mine.emptyPublished')"
          :description="t('feature.delivery.description')"
          :action-text="t('delivery.publish.title')"
          accent="var(--c-delivery)"
          action-variant="primary"
          @action="router.push('/delivery/publish')"
        >
          <template #icon>
            <span class="community-delivery-empty-icon" aria-hidden="true">↗</span>
          </template>


        </AppEmpty>
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
            <div class="community-delivery-reward-block flex items-baseline">
              <span class="text-lg font-bold mr-0.5">&#xffe5;</span>
              <span class="text-2xl font-bold">{{ item.reward.toFixed(2) }}</span>
            </div>
          </div>
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
        <i class="w-5 h-5 border-2 border-[var(--c-border)] border-t-[var(--c-delivery)] rounded-full animate-spin"></i> {{ t('common.loading') }}
      </div>
      <div v-else-if="acceptedList.length === 0" class="community-delivery-empty-shell">
        <AppEmpty
          :title="t('delivery.mine.emptyAccepted')"
          :description="t('feature.delivery.description')"
          :action-text="t('tab.home')"
          accent="var(--c-delivery)"
          @action="router.push('/delivery/home')"
        >
          <template #icon>
            <span class="community-delivery-empty-icon" aria-hidden="true">◎</span>
          </template>


        </AppEmpty>
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
            <div class="community-delivery-reward-block flex items-baseline">
              <span class="text-lg font-bold mr-0.5">&#xffe5;</span>
              <span class="text-2xl font-bold">{{ item.reward.toFixed(2) }}</span>
            </div>
          </div>
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

<style scoped>
.community-delivery-tab {
  color: var(--c-text-2);
}

.community-delivery-tab--active {
  color: color-mix(in srgb, var(--c-delivery) 86%, #d97706);
  font-weight: 700;
}

.community-delivery-tab__indicator {
  background: color-mix(in srgb, var(--c-delivery) 88%, #f59e0b);
}

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
  border: 1px solid color-mix(in srgb, var(--c-delivery) 16%, var(--c-border));
  border-radius: 24px;
  background:
    radial-gradient(circle at 50% 0, color-mix(in srgb, var(--c-delivery) 10%, transparent), transparent 42%),
    color-mix(in srgb, var(--c-delivery) 3%, var(--c-surface));
  box-shadow: 0 14px 32px color-mix(in srgb, var(--c-delivery) 10%, transparent);
}

.community-delivery-empty-icon {
  font-size: 28px;
  font-weight: 900;
  line-height: 1;
}

[data-theme="dark"] .community-delivery-empty-shell {
  border-color: rgba(68, 89, 112, 0.72);
  background:
    radial-gradient(circle at 50% 0, color-mix(in srgb, var(--c-delivery) 8%, transparent), transparent 42%),
    rgba(24, 38, 53, 0.84);
  box-shadow: 0 18px 36px rgba(0, 0, 0, 0.2);
}

[data-theme="dark"] .community-delivery-tab--active,
[data-theme="dark"] .community-delivery-reward-block {
  color: color-mix(in srgb, var(--c-delivery) 58%, #f6e1b2);
}

[data-theme="dark"] .community-delivery-tab__indicator {
  background: linear-gradient(90deg, color-mix(in srgb, var(--c-delivery) 54%, #e7c67a), color-mix(in srgb, var(--c-delivery) 34%, #8c6b3b));
}

[data-theme="dark"] .community-delivery-pickup-badge {
  background: color-mix(in srgb, var(--c-delivery) 34%, #78b8d8);
}

[data-theme="dark"] .community-delivery-dropoff-badge {
  background: color-mix(in srgb, var(--c-delivery) 52%, #d8b46a);
}

[data-theme="dark"] .community-delivery-status-badge {
  border-color: rgba(68, 89, 112, 0.72);
}

[data-theme="dark"] .community-delivery-status-badge--pending {
  background: rgba(36, 52, 69, 0.88);
  color: color-mix(in srgb, var(--c-delivery) 56%, #f6e1b2);
}

[data-theme="dark"] .community-delivery-status-badge--active {
  background: color-mix(in srgb, var(--c-delivery) 18%, rgba(24, 38, 53, 0.9));
  color: color-mix(in srgb, var(--c-delivery) 42%, #c7d9e8);
}

[data-theme="dark"] .community-delivery-status-badge--completed {
  background: color-mix(in srgb, var(--c-delivery) 20%, rgba(24, 38, 53, 0.9));
  color: color-mix(in srgb, var(--c-delivery) 44%, #d7e7dc);
}
</style>
