<script setup>
import { computed, ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute, useRouter } from 'vue-router'
import request from '../../utils/request'
import CommunityHeader from '../../components/community/CommunityHeader.vue'
import AppEmpty from '@/components/ui/AppEmpty.vue'
import {
  createDeliverySizeOptions,
  createDeliveryStatusMap,
  createDeliveryTypeMap
} from '../community/communityContent'
import { maskAddress, maskPhone } from '@/utils/mask'
import { PackageOpen } from 'lucide-vue-next'

const route = useRoute()
const router = useRouter()
const { t } = useI18n()
const item = ref(null)
const detailType = ref(null)
const trade = ref(null)
const accepting = ref(false)
const completing = ref(false)
const dialogVisible = ref(false)
const dialogMessage = ref('')
const confirmCompleteVisible = ref(false)
const loading = ref(true)
const errorMessage = ref('')
const canViewSensitiveInfo = computed(() => detailType.value === 0 || detailType.value === 3)

function showDialog(msg) {
  dialogMessage.value = msg
  dialogVisible.value = true
}

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

function getSizeText(size) {
  const map = Object.fromEntries(createDeliverySizeOptions(t).map((option) => [option.value, option.label]))
  return map[size] || t('delivery.size.small')
}

function handleAccept() {
  if (accepting.value || !item.value) return
  accepting.value = true
  request.post('/delivery/acceptorder', null, { params: { orderId: item.value.orderId } })
    .then(() => {
      item.value.state = 1
      showDialog(t('delivery.detail.acceptSuccess'))
      setTimeout(() => router.push('/delivery/mine'), 1500)
    })
    .catch(() => { accepting.value = false })
}

function getUserRole() {
  if (detailType.value === 0) return 'publisher'
  if (detailType.value === 3) return 'runner'
  return null
}

function canAccept() {
  return detailType.value === 1
}

function canComplete() {
  return detailType.value === 0 && item.value && item.value.state === 1
}

function showCompleteConfirm() {
  confirmCompleteVisible.value = true
}

function handleComplete() {
  confirmCompleteVisible.value = false
  if (completing.value || !trade.value || trade.value.tradeId == null) return
  completing.value = true
  request.post(`/delivery/trade/id/${trade.value.tradeId}/finishtrade`)
    .then(() => {
      item.value.state = 2
      showDialog(t('delivery.detail.completeSuccess'))
      setTimeout(() => router.push('/delivery/mine'), 1500)
    })
    .catch(() => { completing.value = false })
}

onMounted(async () => {
  loading.value = true
  try {
    const res = await request.get(`/delivery/order/id/${route.params.id}`)
    const data = res?.data
    if (data && res.success !== false) {
      const o = data.order || {}
      item.value = {
        orderId: o.orderId,
        id: o.orderId,
        status: o.state,
        state: o.state,
        reward: o.price ?? 0,
        time: o.orderTime,
        size: 'small',
        type: 'express',
        pickupAddress: o.company ? t('delivery.pickupAddressWithCompany', { company: o.company }) : t('delivery.pickupShort'),
        deliveryAddress: o.address || '',
        remarks: o.remarks,
        description: o.remarks,
        pickupCode: o.number || null,
        contactPhone: o.phone || null
      }
      detailType.value = data.detailType
      trade.value = data.trade || null
    } else {
      item.value = null
      errorMessage.value = t('common.noData')
    }
  } catch (e) {
    item.value = null
    errorMessage.value = t('communityCommon.loadFailed')
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="community-delivery-page min-h-screen bg-[var(--c-bg)]" style="--module-color: var(--c-delivery)">
    <CommunityHeader :title="t('delivery.detail.title')" moduleColor="var(--c-delivery)" @back="router.back()" backTo="" />

    <div v-if="loading" class="community-delivery-detail-state-shell p-4">
      <div class="community-delivery-detail-state-card">
        <div class="flex flex-col items-center justify-center py-16 px-5 text-[var(--c-text-3)]">
          <span class="w-6 h-6 border-2 border-[var(--c-border)] border-t-[var(--c-delivery)] rounded-full animate-spin"></span>
          <p class="mt-3 text-sm">{{ t('communityCommon.loading') }}</p>
        </div>
      </div>
    </div>

    <div v-else-if="item" class="p-4 animate-[slide-up_0.4s_ease_both]">
      <div class="bg-[var(--c-surface)] rounded-xl shadow-sm p-6">
        <!-- Header -->
        <div class="flex justify-between items-center mb-5 pb-4 border-b border-[var(--c-border)]">
          <div class="text-xl font-semibold text-[var(--c-text-1)]">{{ getTypeText(item.type) }}</div>
          <div class="community-delivery-reward-block flex items-baseline">
            <span class="text-xl font-bold mr-0.5">&#xffe5;</span>
            <span class="text-[28px] font-bold">{{ item.reward.toFixed(2) }}</span>
          </div>
        </div>

        <!-- Route -->
        <div class="mb-5 p-4 bg-[var(--c-bg)] rounded-lg">
          <div class="flex items-start mb-4">
            <span class="community-delivery-pickup-badge w-7 h-7 rounded-full flex items-center justify-center text-[13px] font-bold text-white mr-3 shrink-0 mt-0.5">{{ t('delivery.pickupBadge') }}</span>
            <div class="flex-1">
              <div class="text-lg text-[var(--c-text-1)] leading-relaxed mb-1.5 font-medium">{{ canViewSensitiveInfo ? item.pickupAddress : maskAddress(item.pickupAddress) }}</div>
              <div v-if="item.pickupCode && canViewSensitiveInfo" class="text-base text-[var(--c-text-2)] mt-1">{{ t('delivery.detail.pickupCode') }}{{ item.pickupCode }}</div>
              <div v-else-if="item.pickupCode" class="text-base text-[var(--c-text-2)] mt-1">{{ t('delivery.detail.pickupCode') }}***</div>
            </div>
          </div>
          <div class="flex items-start">
            <span class="community-delivery-dropoff-badge w-7 h-7 rounded-full flex items-center justify-center text-[13px] font-bold text-white mr-3 shrink-0 mt-0.5">{{ t('delivery.deliveryBadge') }}</span>
            <div class="flex-1">
              <div class="text-lg text-[var(--c-text-1)] leading-relaxed mb-1.5 font-medium">{{ canViewSensitiveInfo ? item.deliveryAddress : maskAddress(item.deliveryAddress) }}</div>
              <div v-if="item.contactPhone && canViewSensitiveInfo" class="text-base text-[var(--c-text-2)] mt-1">{{ t('delivery.detail.contactPhone') }}{{ item.contactPhone }}</div>
              <div v-else-if="item.contactPhone" class="text-base text-[var(--c-text-2)] mt-1">{{ t('delivery.detail.contactPhone') }}{{ maskPhone(item.contactPhone) }}</div>
            </div>
          </div>
        </div>

        <!-- Pickup image -->
        <div v-if="item.pickupImage" class="mb-5 rounded-lg overflow-hidden bg-[var(--c-border)]">
          <img :src="item.pickupImage" :alt="t('delivery.publish.pickupImage')" class="w-full h-auto max-h-[300px] object-cover" />
        </div>

        <!-- Info rows -->
        <div class="pt-4 border-t border-[var(--c-border)]">
          <!-- Role -->
          <div v-if="getUserRole()" class="flex items-center mb-4 pb-3 border-b border-[var(--c-border)]">
            <span class="text-base text-[var(--c-text-2)] min-w-[80px]">{{ t('delivery.detail.myRole') }}</span>
            <div class="community-delivery-role-badge px-3.5 py-1.5 rounded-full text-[13px] font-medium" :class="getUserRole() === 'publisher' ? 'community-delivery-role-badge--publisher' : 'community-delivery-role-badge--runner'">
              {{ getUserRole() === 'publisher' ? t('delivery.detail.publisherRole') : t('delivery.detail.runnerRole') }}
            </div>
          </div>
          <div class="flex items-center mb-3">
            <span class="text-base text-[var(--c-text-2)] min-w-[80px]">{{ t('delivery.detail.sizeLabel') }}</span>
            <span class="flex-1 text-base text-[var(--c-text-1)]">{{ getSizeText(item.size) }}</span>
          </div>
          <div v-if="item.description" class="flex items-center mb-3">
            <span class="text-base text-[var(--c-text-2)] min-w-[80px]">{{ t('delivery.detail.descriptionLabel') }}</span>
            <span class="flex-1 text-base text-[var(--c-text-1)]">{{ item.description }}</span>
          </div>
          <div class="flex items-center mb-3">
            <span class="text-base text-[var(--c-text-2)] min-w-[80px]">{{ t('delivery.detail.publishTime') }}</span>
            <span class="flex-1 text-base text-[var(--c-text-1)]">{{ item.time }}</span>
          </div>
          <div class="flex items-center">
            <span class="text-base text-[var(--c-text-2)] min-w-[80px]">{{ t('delivery.detail.statusLabel') }}</span>
            <div class="px-3 py-1 rounded-full text-sm font-medium" :class="getStatusClass(item.status)">
              {{ getStatusText(item.status) }}
            </div>
          </div>
        </div>

        <!-- Action buttons -->
        <div v-if="canAccept() || canComplete()" class="mt-6 pt-6 border-t border-[var(--c-border)] flex gap-3">
          <button
            v-if="canAccept()"
            type="button"
            class="community-delivery-action community-delivery-action--primary flex-1 h-11 border-none rounded-lg text-lg font-medium cursor-pointer transition-all text-white disabled:opacity-60 disabled:cursor-not-allowed"
            :disabled="accepting"
            @click="handleAccept"
          >
            {{ accepting ? t('delivery.detail.accepting') : t('delivery.detail.acceptAction') }}
          </button>
          <button
            v-if="canComplete()"
            type="button"
            class="community-delivery-action community-delivery-action--success flex-1 h-11 border-none rounded-lg text-lg font-medium cursor-pointer transition-all text-white disabled:opacity-60 disabled:cursor-not-allowed"
            :disabled="completing"
            @click="showCompleteConfirm"
          >
            {{ completing ? t('delivery.detail.completing') : t('delivery.detail.completeAction') }}
          </button>
        </div>
      </div>
    </div>

    <div v-else class="community-delivery-detail-state-shell p-4">
      <div class="community-delivery-detail-state-card">
        <AppEmpty
          :title="t('common.noData')"
          :description="errorMessage || t('communityCommon.loadFailed')"
          :action-text="t('common.back')"
          accent="var(--c-delivery)"
          action-variant="primary"
          @action="router.back()"
        >
          <template #icon>
            <PackageOpen class="community-delivery-detail-state-icon" aria-hidden="true" />
          </template>
        </AppEmpty>
      </div>
    </div>

    <!-- Info dialog -->
    <div v-if="dialogVisible" class="fixed inset-0 bg-black/50 z-[1000]" @click="dialogVisible = false"></div>
    <div v-if="dialogVisible" class="fixed top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[280px] bg-[var(--c-surface)] rounded-xl overflow-hidden z-[1001] shadow-lg">
      <div class="text-center font-bold text-base py-4 text-[var(--c-text-1)]">{{ t('common.hint') }}</div>
      <div class="px-6 pb-4 text-center text-sm text-[var(--c-text-2)] leading-relaxed">{{ dialogMessage }}</div>
      <div class="border-t border-[var(--c-border)] flex">
        <a href="javascript:;" class="community-delivery-dialog-confirm flex-1 text-center py-3 font-medium no-underline" @click="dialogVisible = false">{{ t('common.confirm') }}</a>
      </div>
    </div>

    <!-- Complete confirmation dialog -->
    <div v-if="confirmCompleteVisible" class="fixed inset-0 bg-black/50 z-[1000]" @click="confirmCompleteVisible = false"></div>
    <div v-if="confirmCompleteVisible" class="fixed top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[280px] bg-[var(--c-surface)] rounded-xl overflow-hidden z-[1001] shadow-lg">
      <div class="text-center font-bold text-base py-4 text-[var(--c-text-1)]">{{ t('delivery.detail.completeConfirmTitle') }}</div>
      <div class="px-6 pb-4 text-center text-sm text-[var(--c-text-2)] leading-relaxed">{{ t('delivery.detail.completeConfirmMessage') }}</div>
      <div class="border-t border-[var(--c-border)] flex">
        <a href="javascript:;" class="flex-1 text-center py-3 text-[var(--c-text-2)] no-underline border-r border-[var(--c-border)]" @click="confirmCompleteVisible = false">{{ t('common.cancel') }}</a>
        <a href="javascript:;" class="community-delivery-dialog-confirm flex-1 text-center py-3 font-medium no-underline" @click="handleComplete">{{ t('common.confirm') }}</a>
      </div>
    </div>
  </div>
</template>

<style scoped>
.community-delivery-detail-state-shell {
  max-width: 1180px;
  margin: 0 auto;
}

.community-delivery-detail-state-card {
  border: 1px solid color-mix(in srgb, var(--c-delivery) 10%, rgba(205, 222, 226, 0.82));
  border-radius: 24px;
  background: color-mix(in srgb, var(--c-delivery) 3%, rgba(255, 255, 255, 0.94));
  box-shadow: 0 18px 36px rgba(32, 69, 78, 0.08);
}

.community-delivery-detail-state-icon {
  width: 30px;
  height: 30px;
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

.community-delivery-role-badge--publisher {
  background: color-mix(in srgb, var(--c-delivery) 18%, #dbeafe);
  color: color-mix(in srgb, var(--c-delivery) 62%, #1d4ed8);
}

.community-delivery-role-badge--runner {
  background: color-mix(in srgb, var(--c-delivery) 18%, #fef3c7);
  color: color-mix(in srgb, var(--c-delivery) 78%, #b45309);
}

.community-delivery-action--primary {
  background: linear-gradient(135deg, color-mix(in srgb, var(--c-delivery) 92%, #f59e0b), color-mix(in srgb, var(--c-delivery) 76%, #d97706));
  box-shadow: 0 2px 8px color-mix(in srgb, var(--c-delivery) 28%, transparent);
}

.community-delivery-action--success {
  background: linear-gradient(135deg, color-mix(in srgb, var(--c-delivery) 72%, #10b981), color-mix(in srgb, var(--c-delivery) 54%, #059669));
  box-shadow: 0 2px 8px color-mix(in srgb, var(--c-delivery) 24%, transparent);
}

.community-delivery-dialog-confirm {
  color: color-mix(in srgb, var(--c-delivery) 84%, #d97706);
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

[data-theme="dark"] .community-delivery-role-badge--publisher {
  background: rgba(32, 48, 68, 0.88);
  color: color-mix(in srgb, var(--c-delivery) 46%, #bfdbfe);
}

[data-theme="dark"] .community-delivery-role-badge--runner {
  background: rgba(36, 52, 69, 0.88);
  color: color-mix(in srgb, var(--c-delivery) 70%, #fde68a);
}

[data-theme="dark"] .community-delivery-dialog-confirm {
  color: color-mix(in srgb, var(--c-delivery) 72%, #fde68a);
}

[data-theme="dark"] .community-delivery-detail-state-card {
  border-color: color-mix(in srgb, var(--c-delivery) 12%, rgba(97, 122, 147, 0.66));
  background: rgba(24, 38, 53, 0.88);
  box-shadow: 0 22px 38px rgba(0, 0, 0, 0.22);
}
</style>
