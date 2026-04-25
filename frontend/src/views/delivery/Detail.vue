<script setup>
import { computed, ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute, useRouter } from 'vue-router'
import request from '../../utils/request'
import CommunityHeader from '../../components/community/CommunityHeader.vue'
import {
  createDeliverySizeOptions,
  createDeliveryStatusMap,
  createDeliveryTypeMap
} from '../community/communityContent'
import { maskAddress, maskPhone } from '@/utils/mask'

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
  if (status === 0) return 'bg-amber-100 text-amber-800'
  if (status === 1) return 'bg-blue-100 text-blue-800'
  if (status === 2) return 'bg-green-100 text-green-800'
  return ''
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
    }
  } catch (e) {
    item.value = null
  }
})
</script>

<template>
  <div class="min-h-screen bg-[var(--c-bg)]" style="--module-color: #f59e0b">
    <CommunityHeader :title="t('delivery.detail.title')" moduleColor="#f59e0b" @back="router.back()" backTo="" />

    <div v-if="item" class="p-4 animate-[slide-up_0.4s_ease_both]">
      <div class="bg-[var(--c-surface)] rounded-xl shadow-sm p-6">
        <!-- Header -->
        <div class="flex justify-between items-center mb-5 pb-4 border-b border-[var(--c-border)]">
          <div class="text-xl font-semibold text-[var(--c-text-1)]">{{ getTypeText(item.type) }}</div>
          <div class="flex items-baseline text-red-500">
            <span class="text-xl font-bold mr-0.5">&#xffe5;</span>
            <span class="text-[28px] font-bold">{{ item.reward.toFixed(2) }}</span>
          </div>
        </div>

        <!-- Route -->
        <div class="mb-5 p-4 bg-[var(--c-bg)] rounded-lg">
          <div class="flex items-start mb-4">
            <span class="w-7 h-7 rounded-full flex items-center justify-center text-[13px] font-bold text-white mr-3 shrink-0 mt-0.5 bg-blue-500">{{ t('delivery.pickupBadge') }}</span>
            <div class="flex-1">
              <div class="text-lg text-[var(--c-text-1)] leading-relaxed mb-1.5 font-medium">{{ canViewSensitiveInfo ? item.pickupAddress : maskAddress(item.pickupAddress) }}</div>
              <div v-if="item.pickupCode && canViewSensitiveInfo" class="text-base text-[var(--c-text-2)] mt-1">{{ t('delivery.detail.pickupCode') }}{{ item.pickupCode }}</div>
              <div v-else-if="item.pickupCode" class="text-base text-[var(--c-text-2)] mt-1">{{ t('delivery.detail.pickupCode') }}***</div>
            </div>
          </div>
          <div class="flex items-start">
            <span class="w-7 h-7 rounded-full flex items-center justify-center text-[13px] font-bold text-white mr-3 shrink-0 mt-0.5 bg-amber-500">{{ t('delivery.deliveryBadge') }}</span>
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
            <div class="px-3.5 py-1.5 rounded-full text-[13px] font-medium" :class="getUserRole() === 'publisher' ? 'bg-blue-100 text-blue-800' : 'bg-amber-100 text-amber-800'">
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
            class="flex-1 h-11 border-none rounded-lg text-lg font-medium cursor-pointer transition-all bg-gradient-to-br from-amber-500 to-amber-600 text-white shadow-[0_2px_8px_rgba(245,158,11,0.25)] hover:shadow-[0_4px_12px_rgba(245,158,11,0.35)] disabled:opacity-60 disabled:cursor-not-allowed"
            :disabled="accepting"
            @click="handleAccept"
          >
            {{ accepting ? t('delivery.detail.accepting') : t('delivery.detail.acceptAction') }}
          </button>
          <button
            v-if="canComplete()"
            type="button"
            class="flex-1 h-11 border-none rounded-lg text-lg font-medium cursor-pointer transition-all bg-gradient-to-br from-emerald-500 to-emerald-600 text-white shadow-[0_2px_8px_rgba(16,185,129,0.25)] hover:shadow-[0_4px_12px_rgba(16,185,129,0.35)] disabled:opacity-60 disabled:cursor-not-allowed"
            :disabled="completing"
            @click="showCompleteConfirm"
          >
            {{ completing ? t('delivery.detail.completing') : t('delivery.detail.completeAction') }}
          </button>
        </div>
      </div>
    </div>

    <!-- Info dialog -->
    <div v-if="dialogVisible" class="fixed inset-0 bg-black/50 z-[1000]" @click="dialogVisible = false"></div>
    <div v-if="dialogVisible" class="fixed top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[280px] bg-[var(--c-surface)] rounded-xl overflow-hidden z-[1001] shadow-lg">
      <div class="text-center font-bold text-base py-4 text-[var(--c-text-1)]">{{ t('common.hint') }}</div>
      <div class="px-6 pb-4 text-center text-sm text-[var(--c-text-2)] leading-relaxed">{{ dialogMessage }}</div>
      <div class="border-t border-[var(--c-border)] flex">
        <a href="javascript:;" class="flex-1 text-center py-3 text-amber-500 font-medium no-underline" @click="dialogVisible = false">{{ t('common.confirm') }}</a>
      </div>
    </div>

    <!-- Complete confirmation dialog -->
    <div v-if="confirmCompleteVisible" class="fixed inset-0 bg-black/50 z-[1000]" @click="confirmCompleteVisible = false"></div>
    <div v-if="confirmCompleteVisible" class="fixed top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[280px] bg-[var(--c-surface)] rounded-xl overflow-hidden z-[1001] shadow-lg">
      <div class="text-center font-bold text-base py-4 text-[var(--c-text-1)]">{{ t('delivery.detail.completeConfirmTitle') }}</div>
      <div class="px-6 pb-4 text-center text-sm text-[var(--c-text-2)] leading-relaxed">{{ t('delivery.detail.completeConfirmMessage') }}</div>
      <div class="border-t border-[var(--c-border)] flex">
        <a href="javascript:;" class="flex-1 text-center py-3 text-[var(--c-text-2)] no-underline border-r border-[var(--c-border)]" @click="confirmCompleteVisible = false">{{ t('common.cancel') }}</a>
        <a href="javascript:;" class="flex-1 text-center py-3 text-amber-500 font-medium no-underline" @click="handleComplete">{{ t('common.confirm') }}</a>
      </div>
    </div>
  </div>
</template>
