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
  <div class="min-h-screen bg-[var(--c-bg)] pb-20" style="--module-color: #f59e0b">
    <CommunityHeader :title="t('delivery.publish.title')" moduleColor="#f59e0b" :showBack="false">
      <template #right>
        <button type="button" class="px-5 py-1.5 bg-amber-500 text-white border-none rounded-full text-base cursor-pointer transition-opacity disabled:opacity-60 disabled:cursor-not-allowed" :disabled="submitting" @click="submit">
          {{ submitting ? t('delivery.publish.submitting') : t('delivery.publish.submitAction') }}
        </button>
      </template>
    </CommunityHeader>

    <div class="p-4 animate-[slide-up_0.4s_ease_both]">
      <!-- Pickup info -->
      <div class="bg-[var(--c-surface)] rounded-xl shadow-sm p-5 mb-4">
        <div class="text-lg font-semibold text-[var(--c-text-1)] mb-4 pb-2.5 border-b border-[var(--c-border)]">{{ t('delivery.publish.pickupSection') }}</div>
        <div class="mb-4">
          <label class="block text-base text-[var(--c-text-2)] mb-2">{{ t('delivery.publish.pickupAddress') }}</label>
          <input type="text" class="w-full p-3 border border-[var(--c-divider)] rounded-lg text-base text-[var(--c-text-1)] outline-none box-border transition-colors focus:border-amber-500" :placeholder="t('delivery.publish.pickupPlaceholder')" v-model="formData.pickupAddress" />
        </div>
        <div class="mb-4">
          <label class="block text-base text-[var(--c-text-2)] mb-2">{{ t('delivery.publish.pickupCode') }}</label>
          <input type="text" class="w-full p-3 border border-[var(--c-divider)] rounded-lg text-base text-[var(--c-text-1)] outline-none box-border transition-colors focus:border-amber-500" :placeholder="t('delivery.publish.pickupCodePlaceholder')" v-model="formData.pickupCode" />
        </div>
        <div>
          <label class="block text-base text-[var(--c-text-2)] mb-2">{{ t('delivery.publish.pickupImage') }}</label>
          <div v-if="pickupImagePreview" class="relative w-[100px] h-[100px] rounded-lg overflow-hidden bg-[var(--c-border)]">
            <img :src="pickupImagePreview" :alt="t('delivery.publish.pickupImage')" class="w-full h-full object-cover" />
            <button type="button" class="absolute top-1 right-1 w-6 h-6 bg-black/60 text-white border-none rounded-full text-xl leading-none cursor-pointer flex items-center justify-center" @click="removePickupImage">x</button>
          </div>
          <div v-else class="w-[100px] h-[100px] border-2 border-dashed border-[var(--c-divider)] rounded-lg flex flex-col items-center justify-center cursor-pointer bg-[var(--c-bg)] transition-colors active:border-amber-500" @click="$refs.pickupImageInput.click()">
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
          <input type="text" class="w-full p-3 border border-[var(--c-divider)] rounded-lg text-base text-[var(--c-text-1)] outline-none box-border transition-colors focus:border-amber-500" :placeholder="t('delivery.publish.deliveryPlaceholder')" v-model="formData.deliveryAddress" />
        </div>
        <div>
          <label class="block text-base text-[var(--c-text-2)] mb-2">{{ t('delivery.publish.contactPhone') }}</label>
          <input type="tel" class="w-full p-3 border border-[var(--c-divider)] rounded-lg text-base text-[var(--c-text-1)] outline-none box-border transition-colors focus:border-amber-500" :placeholder="t('delivery.publish.phonePlaceholder')" v-model="formData.contactPhone" maxlength="11" />
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
              :class="formData.size === opt.value ? 'bg-amber-500 text-white border-amber-500' : 'bg-[var(--c-card)] text-[var(--c-text-2)] border-[var(--c-divider)]'"
              @click="selectSize(opt.value)"
            >
              {{ opt.label }}
            </button>
          </div>
        </div>
        <div>
          <label class="block text-base text-[var(--c-text-2)] mb-2">{{ t('delivery.publish.descriptionLabel') }}</label>
          <textarea class="w-full p-3 border border-[var(--c-divider)] rounded-lg text-base text-[var(--c-text-1)] outline-none box-border resize-none min-h-[80px] transition-colors focus:border-amber-500" :placeholder="t('delivery.publish.descriptionPlaceholder')" v-model="formData.description" rows="3" maxlength="100"></textarea>
        </div>
      </div>
    </div>

    <!-- Fixed bottom bar -->
    <div class="fixed bottom-14 left-0 right-0 flex items-center px-4 py-3 bg-[var(--c-card)] border-t border-[var(--c-border)] z-[100] shadow-[0_-2px_8px_rgba(0,0,0,0.04)]">
      <div class="flex items-center mr-4">
        <span class="text-base text-[var(--c-text-2)] mr-1">{{ t('delivery.publish.rewardLabel') }}</span>
        <span class="text-lg text-red-500 font-bold">&#xffe5;</span>
        <input type="number" class="w-20 px-2 py-1.5 border border-[var(--c-divider)] rounded-lg text-lg text-red-500 font-bold outline-none ml-1 transition-colors focus:border-amber-500" placeholder="0.00" v-model="formData.reward" step="0.01" min="0" max="99" />
      </div>
      <button type="button" class="flex-1 h-11 bg-amber-500 text-white border-none rounded-lg text-lg font-medium cursor-pointer transition-opacity disabled:opacity-60 disabled:cursor-not-allowed" :disabled="submitting" @click="submit">
        {{ submitting ? t('delivery.publish.submitting') : t('delivery.publish.submitAction') }}
      </button>
    </div>

    <!-- Dialog -->
    <div v-if="dialogVisible" class="fixed inset-0 bg-black/50 z-[1000]" @click="dialogVisible = false"></div>
    <div v-if="dialogVisible" class="fixed top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[280px] bg-[var(--c-surface)] rounded-xl overflow-hidden z-[1001] shadow-lg">
      <div class="text-center font-bold text-base py-4 text-[var(--c-text-1)]">{{ t('common.hint') }}</div>
      <div class="px-6 pb-4 text-center text-sm text-[var(--c-text-2)] leading-relaxed">{{ dialogMessage }}</div>
      <div class="border-t border-[var(--c-border)] flex">
        <a href="javascript:;" class="flex-1 text-center py-3 text-amber-500 font-medium no-underline" @click="dialogVisible = false">{{ t('common.confirm') }}</a>
      </div>
    </div>
  </div>
</template>
