<script setup>
import { computed, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import request from '../../utils/request'
import { useToast } from '../../composables/useToast'
import CommunityHeader from '../../components/community/CommunityHeader.vue'
import { createDeliverySizeOptions } from '../community/communityContent'

const router = useRouter()
const { t } = useI18n()
const { success: toastSuccess } = useToast()
const formData = ref({
  type: 'express',
  pickupAddress: '',
  pickupCode: '',
  pickupImage: '',
  deliveryAddress: '',
  contactPhone: '',
  size: 'small',
  description: '',
  reward: ''
})
const pickupImagePreview = ref('')
const pickupImageFile = ref(null)
const submitting = ref(false)
const dialogVisible = ref(false)
const dialogMessage = ref('')
const sizeOptions = computed(() => createDeliverySizeOptions(t))

function showDialog(msg) {
  dialogMessage.value = msg
  dialogVisible.value = true
}

function onPickupImageChange(e) {
  const file = e.target.files[0]
  if (!file) return
  if (!file.type.startsWith('image/')) {
    showDialog(t('delivery.publish.invalidImageType'))
    return
  }
  if (file.size > 5 * 1024 * 1024) {
    showDialog(t('delivery.publish.imageTooLarge'))
    return
  }
  pickupImageFile.value = file
  pickupImagePreview.value = URL.createObjectURL(file)
}

function removePickupImage() {
  pickupImagePreview.value = ''
  pickupImageFile.value = null
}

function selectSize(size) {
  formData.value.size = size
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
  const reward = parseFloat(formData.value.reward)
  if (!reward || reward < 0.01 || reward > 9999.99) {
    showDialog(t('delivery.publish.rewardInvalid'))
    return
  }

  submitting.value = true
  const payload = {
    name: t('delivery.publish.payloadName'),
    number: '00000000000',
    phone: formData.value.contactPhone.trim(),
    price: reward,
    company: formData.value.pickupAddress.trim() || t('delivery.publish.defaultCompany'),
    address: formData.value.deliveryAddress.trim(),
    remarks: formData.value.description?.trim() || ''
  }
  request.post('/delivery/order', payload)
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
        取件码、地址、手机号、姓名等通常属于高敏信息，请最小化填写和展示。请勿承接违法违规、危险、贵重或明显超出自身能力范围的任务。
      </div>

      <!-- Pickup info -->
      <div class="bg-[var(--c-surface)] rounded-xl shadow-sm p-5 mb-4">
        <div class="text-lg font-semibold text-[var(--c-text-1)] mb-4 pb-2.5 border-b border-[var(--c-border)]">{{ t('delivery.publish.pickupSection') }}</div>
        <div class="mb-4">
          <label class="block text-base text-[var(--c-text-2)] mb-2">{{ t('delivery.publish.pickupAddress') }}</label>
          <input type="text" class="community-delivery-input w-full p-3 border border-[var(--c-divider)] rounded-lg text-base text-[var(--c-text-1)] outline-none box-border transition-colors" :placeholder="t('delivery.publish.pickupPlaceholder')" v-model="formData.pickupAddress" />
        </div>
        <div class="mb-4">
          <label class="block text-base text-[var(--c-text-2)] mb-2">{{ t('delivery.publish.pickupCode') }}</label>
          <input type="text" class="community-delivery-input w-full p-3 border border-[var(--c-divider)] rounded-lg text-base text-[var(--c-text-1)] outline-none box-border transition-colors" :placeholder="t('delivery.publish.pickupCodePlaceholder')" v-model="formData.pickupCode" />
        </div>
        <div>
          <label class="block text-base text-[var(--c-text-2)] mb-2">{{ t('delivery.publish.pickupImage') }}</label>
          <div v-if="pickupImagePreview" class="relative w-[100px] h-[100px] rounded-lg overflow-hidden bg-[var(--c-border)]">
            <img :src="pickupImagePreview" :alt="t('delivery.publish.pickupImage')" class="w-full h-full object-cover" />
            <button type="button" class="absolute top-1 right-1 w-6 h-6 bg-black/60 text-white border-none rounded-full text-xl leading-none cursor-pointer flex items-center justify-center" @click="removePickupImage">x</button>
          </div>
          <div v-else class="community-delivery-upload-placeholder w-[100px] h-[100px] border-2 border-dashed border-[var(--c-divider)] rounded-lg flex flex-col items-center justify-center cursor-pointer bg-[var(--c-bg)] transition-colors" @click="$refs.pickupImageInput.click()">
            <input ref="pickupImageInput" type="file" accept="image/*" @change="onPickupImageChange" class="hidden" />
            <span class="text-[32px] text-[var(--c-text-3)] mb-1">+</span>
            <span class="text-sm text-[var(--c-text-3)]">{{ t('delivery.publish.uploadImage') }}</span>
          </div>
        </div>
      </div>

      <!-- Delivery info -->
      <div class="bg-[var(--c-surface)] rounded-xl shadow-sm p-5 mb-4">
        <div class="text-lg font-semibold text-[var(--c-text-1)] mb-4 pb-2.5 border-b border-[var(--c-border)]">{{ t('delivery.publish.deliverySection') }}</div>
        <div class="mb-4">
          <label class="block text-base text-[var(--c-text-2)] mb-2">{{ t('delivery.publish.deliveryAddress') }}</label>
          <input type="text" class="community-delivery-input w-full p-3 border border-[var(--c-divider)] rounded-lg text-base text-[var(--c-text-1)] outline-none box-border transition-colors" :placeholder="t('delivery.publish.deliveryPlaceholder')" v-model="formData.deliveryAddress" />
        </div>
        <div>
          <label class="block text-base text-[var(--c-text-2)] mb-2">{{ t('delivery.publish.contactPhone') }}</label>
          <input type="tel" class="community-delivery-input w-full p-3 border border-[var(--c-divider)] rounded-lg text-base text-[var(--c-text-1)] outline-none box-border transition-colors" :placeholder="t('delivery.publish.phonePlaceholder')" v-model="formData.contactPhone" maxlength="11" />
        </div>
      </div>

      <!-- Item description -->
      <div class="bg-[var(--c-surface)] rounded-xl shadow-sm p-5 mb-4">
        <div class="text-lg font-semibold text-[var(--c-text-1)] mb-4 pb-2.5 border-b border-[var(--c-border)]">{{ t('delivery.publish.itemSection') }}</div>
        <div class="mb-4">
          <label class="block text-base text-[var(--c-text-2)] mb-2">{{ t('delivery.publish.sizeLabel') }}</label>
          <div class="flex gap-2.5 flex-wrap">
            <button
              v-for="opt in sizeOptions"
              :key="opt.value"
              type="button"
              class="px-4 py-2 border rounded-full text-base cursor-pointer transition-all duration-300"
              :class="formData.size === opt.value ? 'community-delivery-size-chip--active text-white border-transparent' : 'bg-[var(--c-card)] text-[var(--c-text-2)] border-[var(--c-divider)]'"
              @click="selectSize(opt.value)"
            >
              {{ opt.label }}
            </button>
          </div>
        </div>
        <div>
          <label class="block text-base text-[var(--c-text-2)] mb-2">{{ t('delivery.publish.descriptionLabel') }}</label>
          <textarea class="community-delivery-input w-full p-3 border border-[var(--c-divider)] rounded-lg text-base text-[var(--c-text-1)] outline-none box-border resize-none min-h-[80px] transition-colors" :placeholder="t('delivery.publish.descriptionPlaceholder')" v-model="formData.description" rows="3" maxlength="100"></textarea>
        </div>
      </div>
    </div>

    <!-- Fixed bottom bar -->
    <div class="community-delivery-bottom-bar fixed bottom-14 left-0 right-0 flex items-center px-4 py-3 bg-[var(--c-card)] border-t border-[var(--c-border)] z-[100] shadow-[0_-2px_8px_rgba(0,0,0,0.04)]">
      <div class="flex items-center mr-4">
        <span class="text-base text-[var(--c-text-2)] mr-1">{{ t('delivery.publish.rewardLabel') }}</span>
        <span class="community-delivery-reward text-lg font-bold">&#xffe5;</span>
        <input type="number" class="community-delivery-reward-input community-delivery-input w-20 px-2 py-1.5 border border-[var(--c-divider)] rounded-lg text-lg font-bold outline-none ml-1 transition-colors" placeholder="0.00" v-model="formData.reward" step="0.01" min="0" max="99" />
      </div>
      <button type="button" class="community-delivery-submit-button flex-1 h-11 text-white border-none rounded-lg text-lg font-medium cursor-pointer transition-opacity disabled:opacity-60 disabled:cursor-not-allowed" :disabled="submitting" @click="submit">
        {{ submitting ? t('delivery.publish.submitting') : t('delivery.publish.submitAction') }}
      </button>
    </div>

    <!-- Dialog -->
    <div v-if="dialogVisible" class="fixed inset-0 bg-black/50 z-[1000]" @click="dialogVisible = false"></div>
    <div v-if="dialogVisible" class="community-delivery-dialog-shell fixed top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[280px] bg-[var(--c-surface)] rounded-xl overflow-hidden z-[1001] shadow-lg">
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
  border: 1px solid color-mix(in srgb, var(--c-delivery) 28%, var(--c-border));
  background:
    linear-gradient(135deg, color-mix(in srgb, var(--c-delivery) 12%, rgba(255, 255, 255, 0.96)), color-mix(in srgb, var(--c-delivery) 5%, rgba(255, 255, 255, 0.92)));
  color: color-mix(in srgb, var(--c-delivery) 46%, #7c2d12);
  box-shadow: 0 12px 28px color-mix(in srgb, var(--c-delivery) 8%, transparent);
}

.community-delivery-upload-placeholder {
  box-shadow: inset 0 0 0 1px color-mix(in srgb, var(--c-delivery) 10%, transparent);
}

.community-delivery-input {
  background: color-mix(in srgb, var(--c-delivery) 3%, rgba(255, 255, 255, 0.72));
  box-shadow: inset 0 0 0 1px color-mix(in srgb, var(--c-delivery) 8%, transparent);
}

.community-delivery-input::placeholder {
  color: color-mix(in srgb, var(--c-delivery) 14%, var(--c-text-3));
}

.community-delivery-submit-chip,
.community-delivery-submit-button,
.community-delivery-size-chip--active {
  background: linear-gradient(135deg, color-mix(in srgb, var(--c-delivery) 92%, #f59e0b), color-mix(in srgb, var(--c-delivery) 76%, #d97706));
}

.community-delivery-submit-button {
  box-shadow: 0 8px 20px color-mix(in srgb, var(--c-delivery) 24%, transparent);
}

.community-delivery-dialog-confirm {
  color: color-mix(in srgb, var(--c-delivery) 84%, #d97706);
}

.community-delivery-input:focus,
.community-delivery-upload-placeholder:active {
  border-color: color-mix(in srgb, var(--c-delivery) 82%, #f59e0b) !important;
}

.community-delivery-reward,
.community-delivery-reward-input {
  color: color-mix(in srgb, var(--c-delivery) 86%, #ea580c) !important;
}

.community-delivery-bottom-bar,
.community-delivery-dialog-shell {
  border-color: color-mix(in srgb, var(--c-delivery) 14%, rgba(202, 222, 226, 0.72));
}

.community-delivery-bottom-bar {
  backdrop-filter: blur(18px);
  background: linear-gradient(180deg, rgba(250, 246, 240, 0.92), rgba(255, 255, 255, 0.96));
}

.community-delivery-dialog-shell {
  box-shadow: 0 24px 56px rgba(48, 39, 18, 0.18);
}

[data-theme="dark"] .community-delivery-warning {
  border-color: color-mix(in srgb, var(--c-delivery) 20%, rgba(76, 101, 126, 0.72));
  background:
    radial-gradient(circle at 100% 0, color-mix(in srgb, var(--c-delivery) 8%, transparent), transparent 36%),
    linear-gradient(135deg, rgba(43, 47, 50, 0.94), rgba(31, 37, 43, 0.94));
  color: color-mix(in srgb, var(--c-delivery) 38%, #fff7ed);
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.03),
    0 14px 28px rgba(0, 0, 0, 0.14);
}

[data-theme="dark"] .community-delivery-submit-chip,
[data-theme="dark"] .community-delivery-submit-button,
[data-theme="dark"] .community-delivery-size-chip--active {
  background: linear-gradient(
    135deg,
    color-mix(in srgb, var(--c-delivery) 54%, #e7c67a),
    color-mix(in srgb, var(--c-delivery) 34%, #6d5532)
  );
  box-shadow:
    inset 0 0 0 1px color-mix(in srgb, var(--c-delivery) 18%, rgba(160, 142, 108, 0.52)),
    0 16px 30px rgba(0, 0, 0, 0.16);
}

[data-theme="dark"] .community-delivery-upload-placeholder {
  background: linear-gradient(180deg, rgba(38, 53, 70, 0.92), rgba(28, 40, 55, 0.96));
  border-color: color-mix(in srgb, var(--c-delivery) 18%, rgba(111, 132, 156, 0.48));
  box-shadow:
    inset 0 0 0 1px color-mix(in srgb, var(--c-delivery) 10%, rgba(160, 142, 108, 0.22)),
    0 12px 24px rgba(0, 0, 0, 0.12);
}

[data-theme="dark"] .community-delivery-input {
  background: linear-gradient(180deg, rgba(34, 47, 61, 0.88), rgba(29, 40, 53, 0.96));
  box-shadow:
    inset 0 0 0 1px color-mix(in srgb, var(--c-delivery) 12%, rgba(160, 142, 108, 0.16)),
    inset 0 1px 0 rgba(255, 255, 255, 0.02);
}

[data-theme="dark"] .community-delivery-input::placeholder {
  color: color-mix(in srgb, var(--c-delivery) 18%, rgba(214, 218, 226, 0.66));
}

[data-theme="dark"] .community-delivery-input:focus,
[data-theme="dark"] .community-delivery-upload-placeholder:active {
  border-color: color-mix(in srgb, var(--c-delivery) 34%, #e7c67a) !important;
}

[data-theme="dark"] .community-delivery-bottom-bar,
[data-theme="dark"] .community-delivery-dialog-shell {
  border-color: color-mix(in srgb, var(--c-delivery) 18%, rgba(111, 132, 156, 0.42));
  background:
    radial-gradient(circle at 0 0, color-mix(in srgb, var(--c-delivery) 8%, transparent), transparent 30%),
    linear-gradient(180deg, rgba(29, 38, 50, 0.94), rgba(22, 31, 43, 0.98));
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.03),
    0 20px 42px rgba(0, 0, 0, 0.26);
}

[data-theme="dark"] .community-delivery-dialog-confirm {
  color: color-mix(in srgb, var(--c-delivery) 72%, #fde68a);
}

[data-theme="dark"] .community-delivery-reward,
[data-theme="dark"] .community-delivery-reward-input {
  color: color-mix(in srgb, var(--c-delivery) 46%, #fff7ed) !important;
}
</style>
