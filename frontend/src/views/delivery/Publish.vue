<script setup>
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { publishDeliveryOrder } from '../../api/delivery'
import { buildDeliveryPayload } from './deliveryForm'
import { useToast } from '../../composables/useToast'
import CommunityHeader from '../../components/community/CommunityHeader.vue'

const router = useRouter()
const { t } = useI18n()
const { success: toastSuccess } = useToast()
const formData = ref({
  pickupAddress: '',
  pickupCode: '',
  deliveryAddress: '',
  contactPhone: '',
  description: '',
  reward: ''
})
const submitting = ref(false)
const dialogVisible = ref(false)
const dialogMessage = ref('')

function showDialog(msg) {
  dialogMessage.value = msg
  dialogVisible.value = true
}

function submit() {
  if (!formData.value.pickupAddress || !formData.value.pickupAddress.trim()) {
    showDialog(t('delivery.publish.pickupRequired'))
    return
  }
  if (!formData.value.deliveryAddress || !formData.value.deliveryAddress.trim()) {
    showDialog(t('delivery.publish.deliveryRequired'))
    return
  }
  if (!formData.value.contactPhone || !formData.value.contactPhone.trim()) {
    showDialog(t('delivery.publish.phoneRequired'))
    return
  }
  let payload
  try {
    payload = buildDeliveryPayload(formData.value, t('delivery.publish.payloadName'))
  } catch {
    showDialog(t('delivery.publish.rewardInvalid'))
    return
  }
  submitting.value = true
  publishDeliveryOrder(payload)
    .then(() => {
      toastSuccess(t('delivery.publish.publishSuccess'))
      setTimeout(() => router.push('/delivery/home'), 1500)
    })
    .catch(() => { submitting.value = false })
}
</script>

<template>
  <div class="community-delivery-page min-h-screen bg-[var(--c-bg)] pb-20" style="--module-color: var(--c-delivery)">
    <CommunityHeader :title="t('delivery.publish.title')" moduleColor="var(--c-delivery)" :showBack="false">
      <template #right>
        <button type="button" class="community-delivery-submit-chip px-5 py-1.5 text-white border-none rounded-full text-base cursor-pointer transition-opacity disabled:opacity-60 disabled:cursor-not-allowed" :disabled="submitting" @click="submit">
          {{ submitting ? t('delivery.publish.submitting') : t('delivery.publish.submitAction') }}
        </button>
      </template>
    </CommunityHeader>

    <div class="p-4 animate-[slide-up_0.4s_ease_both]">
      <div class="community-delivery-warning mb-4 rounded-xl px-4 py-3 text-xs leading-6">
        {{ t('delivery.publish.privacyHint') }}
      </div>

      <!-- Pickup info -->
      <div class="ui-panel bg-[var(--c-surface)] rounded-xl shadow-sm p-5 mb-4">
        <div class="text-lg font-semibold text-[var(--c-text-1)] mb-4 pb-2.5 border-b border-[var(--c-border)]">{{ t('delivery.publish.pickupSection') }}</div>
        <div class="mb-4">
          <label class="block text-base text-[var(--c-text-2)] mb-2">{{ t('delivery.publish.pickupAddress') }}</label>
          <input type="text" class="ui-control community-delivery-input w-full p-3 border border-[var(--c-divider)] rounded-lg text-base text-[var(--c-text-1)] outline-none box-border transition-colors" :placeholder="t('delivery.publish.pickupPlaceholder')" v-model="formData.pickupAddress" maxlength="100" />
        </div>
        <div class="mb-4">
          <label class="block text-base text-[var(--c-text-2)] mb-2">{{ t('delivery.publish.pickupCode') }}</label>
          <input type="text" class="ui-control community-delivery-input w-full p-3 border border-[var(--c-divider)] rounded-lg text-base text-[var(--c-text-1)] outline-none box-border transition-colors" :placeholder="t('delivery.publish.pickupCodePlaceholder')" v-model="formData.pickupCode" maxlength="64" />
        </div>

      </div>

      <!-- Delivery info -->
      <div class="ui-panel bg-[var(--c-surface)] rounded-xl shadow-sm p-5 mb-4">
        <div class="text-lg font-semibold text-[var(--c-text-1)] mb-4 pb-2.5 border-b border-[var(--c-border)]">{{ t('delivery.publish.deliverySection') }}</div>
        <div class="mb-4">
          <label class="block text-base text-[var(--c-text-2)] mb-2">{{ t('delivery.publish.deliveryAddress') }}</label>
          <input type="text" class="ui-control community-delivery-input w-full p-3 border border-[var(--c-divider)] rounded-lg text-base text-[var(--c-text-1)] outline-none box-border transition-colors" :placeholder="t('delivery.publish.deliveryPlaceholder')" v-model="formData.deliveryAddress" />
        </div>
        <div>
          <label class="block text-base text-[var(--c-text-2)] mb-2">{{ t('delivery.publish.contactPhone') }}</label>
          <input type="tel" class="ui-control community-delivery-input w-full p-3 border border-[var(--c-divider)] rounded-lg text-base text-[var(--c-text-1)] outline-none box-border transition-colors" :placeholder="t('delivery.publish.phonePlaceholder')" v-model="formData.contactPhone" maxlength="11" />
        </div>
      </div>

      <!-- Item description -->
      <div class="ui-panel bg-[var(--c-surface)] rounded-xl shadow-sm p-5 mb-4">
        <div class="text-lg font-semibold text-[var(--c-text-1)] mb-4 pb-2.5 border-b border-[var(--c-border)]">{{ t('delivery.publish.itemSection') }}</div>
        <div>
          <label class="block text-base text-[var(--c-text-2)] mb-2">{{ t('delivery.publish.descriptionLabel') }}</label>
          <textarea class="ui-control community-delivery-input w-full p-3 border border-[var(--c-divider)] rounded-lg text-base text-[var(--c-text-1)] outline-none box-border resize-none min-h-[80px] transition-colors" :placeholder="t('delivery.publish.descriptionPlaceholder')" v-model="formData.description" rows="3" maxlength="100"></textarea>
        </div>
      </div>
    </div>

    <!-- Fixed bottom bar -->
    <div class="community-delivery-bottom-bar fixed bottom-14 left-0 right-0 flex items-center px-4 py-3 bg-[var(--c-card)] border-t border-[var(--c-border)] z-[100] shadow-[0_-2px_8px_rgba(0,0,0,0.04)]">
      <div class="flex items-center mr-4">
        <span class="text-base text-[var(--c-text-2)] mr-1">{{ t('delivery.publish.rewardLabel') }}</span>
        <span class="community-delivery-reward text-lg font-bold">&#xffe5;</span>
        <input type="number" class="ui-control community-delivery-reward-input community-delivery-input w-20 px-2 py-1.5 border border-[var(--c-divider)] rounded-lg text-lg font-bold outline-none ml-1 transition-colors" placeholder="0.00" v-model="formData.reward" step="0.01" min="0" max="9999.99" />
      </div>
      <button type="button" class="community-delivery-submit-button flex-1 h-11 text-white border-none rounded-lg text-lg font-medium cursor-pointer transition-opacity disabled:opacity-60 disabled:cursor-not-allowed" :disabled="submitting" @click="submit">
        {{ submitting ? t('delivery.publish.submitting') : t('delivery.publish.submitAction') }}
      </button>
    </div>

    <!-- Dialog -->
    <div v-if="dialogVisible" class="ui-scrim fixed inset-0 bg-black/50 z-[1000]" @click="dialogVisible = false"></div>
    <div v-if="dialogVisible" class="ui-modal community-delivery-dialog-shell fixed top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[280px] bg-[var(--c-surface)] rounded-xl overflow-hidden z-[1001] shadow-lg">
      <div class="text-center font-bold text-base py-4 text-[var(--c-text-1)]">{{ t('common.hint') }}</div>
      <div class="px-6 pb-4 text-center text-sm text-[var(--c-text-2)] leading-relaxed">{{ dialogMessage }}</div>
      <div class="border-t border-[var(--c-border)] flex">
        <a href="javascript:;" class="community-delivery-dialog-confirm flex-1 text-center py-3 font-medium no-underline" @click="dialogVisible = false">{{ t('common.confirm') }}</a>
      </div>
    </div>
  </div>
</template>

<style scoped>
.community-delivery-warning {
  border: 1px solid var(--c-border);
  background: var(--c-primary-soft);
  color: var(--c-text-2);
  box-shadow: none;
}

.community-delivery-upload-placeholder {
  box-shadow: none;
}

.community-delivery-input {
  background: var(--c-surface);
  box-shadow: none;
}

.community-delivery-input::placeholder {
  color: var(--c-text-3);
}

.community-delivery-submit-chip,
.community-delivery-submit-button,
.community-delivery-size-chip--active {
  background: var(--c-primary);
}

.community-delivery-submit-button {
  box-shadow: none;
}

.community-delivery-dialog-confirm {
  color: var(--c-primary);
}

.community-delivery-input:focus,
.community-delivery-upload-placeholder:active {
  border-color: var(--c-primary) !important;
}

.community-delivery-reward,
.community-delivery-reward-input {
  color: var(--c-primary) !important;
}

.community-delivery-bottom-bar,
.community-delivery-dialog-shell {
  border-color: var(--c-border);
}

.community-delivery-bottom-bar {
  background: var(--c-surface);
}

.community-delivery-dialog-shell {
  box-shadow: var(--shadow-lg);
}
</style>
