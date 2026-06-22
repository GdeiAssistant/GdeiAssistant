<script setup>
import { computed, onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute, useRouter } from 'vue-router'
import request from '../../utils/request'
import { uploadFilesByPresignedUrl } from '../../utils/presignedUpload'
import { useToast } from '../../composables/useToast'
import CommunityHeader from '../../components/community/CommunityHeader.vue'
import { createMarketplaceCategoryNames } from '../community/communityContent'

const route = useRoute()
const router = useRouter()
const { t, locale } = useI18n()
const { toast, loading: showLoadingToast, hideLoading } = useToast()
const name = ref('')
const description = ref('')
const price = ref('')
const location = ref('')
const typeId = ref(-1)
const qq = ref('')
const phone = ref('')
const images = ref([])
const typePickerVisible = ref(false)
const frmErrors = ref({})
const dialogVisible = ref(false)
const dialogMessage = ref('')
const submitting = ref(false)

function formRowClass(hasError) {
  return hasError ? 'marketplace-form-row marketplace-form-row--error' : 'marketplace-form-row'
}
const pageLoading = ref(false)

const editItemId = computed(() => {
  const value = Number(route.query.id)
  return Number.isInteger(value) && value > 0 ? value : null
})
const isEditMode = computed(() => route.query.edit === '1' && editItemId.value !== null)

function showDialog(msg) {
  dialogMessage.value = msg
  dialogVisible.value = true
}

const typeNames = computed(() => createMarketplaceCategoryNames(locale.value))
const typeNameDisplay = computed(() => typeId.value >= 0 && typeId.value <= 11 ? typeNames.value[typeId.value] : '')

const MAX_IMAGES = 4
const MAX_SIZE = 5 * 1024 * 1024
const ALLOW_TYPES = ['image/jpeg', 'image/png', 'image/gif', 'image/webp']

function goBack() {
  router.push(isEditMode.value ? '/marketplace/profile' : '/marketplace/home')
}

function triggerFileInput() {
  if (isEditMode.value) {
    showDialog(t('marketplace.publish.imageEditUnsupported'))
    return
  }
  if (images.value.length >= MAX_IMAGES) return
  const input = document.getElementById('publish_file_input')
  if (input) input.click()
}

function onFileChange(e) {
  if (isEditMode.value) {
    e.target.value = ''
    return
  }
  const files = e.target.files
  if (!files || files.length === 0) return
  const file = files[0]
  if (ALLOW_TYPES.indexOf(file.type) === -1) {
    showDialog(t('marketplace.publish.invalidImageType'))
    return
  }
  if (file.size > MAX_SIZE) {
    showDialog(t('marketplace.publish.imageTooLarge'))
    return
  }
  if (images.value.length >= MAX_IMAGES) {
    showDialog(t('marketplace.publish.imageLimit'))
    return
  }
  const reader = new FileReader()
  reader.onload = (ev) => {
    images.value = [...images.value, { dataUrl: ev.target.result, file }]
  }
  reader.readAsDataURL(file)
  e.target.value = ''
}

function removeImage(index) {
  if (isEditMode.value) {
    showDialog(t('marketplace.publish.imageEditUnsupported'))
    return
  }
  images.value = images.value.filter((_, i) => i !== index)
}

function openTypePicker() {
  typePickerVisible.value = true
}

function closeTypePicker() {
  typePickerVisible.value = false
}

function selectType(id) {
  typeId.value = id
  closeTypePicker()
}

function validate() {
  const err = {}
  if (!name.value.trim()) err.name = true
  if (!description.value.trim()) err.description = true
  if (!price.value || parseFloat(price.value) <= 0 || parseFloat(price.value) > 9999.99) err.price = true
  if (!location.value.trim()) err.location = true
  if (typeId.value < 0 || typeId.value > 11) err.type = true
  if (!qq.value.trim()) err.qq = true
  if (phone.value.length > 11) err.phone = true
  frmErrors.value = err
  return Object.keys(err).length === 0
}

function buildPayload() {
  const formData = new FormData()
  formData.append('name', name.value.trim())
  formData.append('description', description.value.trim())
  formData.append('price', price.value)
  formData.append('location', location.value.trim())
  formData.append('type', String(typeId.value))
  formData.append('qq', qq.value.trim())
  formData.append('phone', phone.value.trim())
  return formData
}

function populateForm(item) {
  name.value = item?.name || ''
  description.value = item?.description || ''
  price.value = item?.price != null ? String(item.price) : ''
  location.value = item?.location || ''
  typeId.value = Number.isInteger(item?.type) ? item.type : -1
  qq.value = item?.qq || ''
  phone.value = item?.phone || ''
  images.value = Array.isArray(item?.pictureURL) ? item.pictureURL.map((url) => ({ dataUrl: url, readonly: true })) : []
}

async function loadEditItem() {
  if (!isEditMode.value) return
  pageLoading.value = true
  try {
    const res = await request.get('/marketplace/profile')
    const data = res?.data || {}
    const list = []
      .concat(Array.isArray(data.doing) ? data.doing : [])
      .concat(Array.isArray(data.sold) ? data.sold : [])
      .concat(Array.isArray(data.off) ? data.off : [])
    const item = list.find((entry) => Number(entry.id) === editItemId.value)
    if (!item) {
      showDialog(t('marketplace.publish.itemNotFound'))
      return
    }
    populateForm(item)
  } finally {
    pageLoading.value = false
  }
}

async function submit() {
  if (submitting.value || pageLoading.value) return
  if (!isEditMode.value && images.value.length < 1) {
    showDialog(t('marketplace.publish.selectImageRequired'))
    return
  }
  if (!name.value || name.value.trim() === '') {
    showDialog(t('marketplace.publish.nameRequired'))
    return
  }
  if (!description.value || description.value.trim() === '') {
    showDialog(t('marketplace.publish.descriptionRequired'))
    return
  }
  if (!price.value || parseFloat(price.value) <= 0 || parseFloat(price.value) > 9999.99) {
    showDialog(t('marketplace.publish.priceInvalid'))
    return
  }
  if (!location.value || location.value.trim() === '') {
    showDialog(t('marketplace.publish.locationRequired'))
    return
  }
  if (typeId.value === null || typeId.value < 0 || typeId.value > 11) {
    showDialog(t('marketplace.publish.typeRequired'))
    return
  }
  if (!qq.value || qq.value.trim() === '') {
    showDialog(t('marketplace.publish.qqRequired'))
    return
  }
  if (!validate()) {
    showDialog(t('marketplace.publish.formInvalid'))
    return
  }
  submitting.value = true
  showLoadingToast(isEditMode.value ? t('marketplace.publish.saving') : t('marketplace.publish.uploading'))
  try {
    const formData = buildPayload()
    if (isEditMode.value) {
      await request.post(`/marketplace/item/id/${editItemId.value}`, formData)
      hideLoading()
      showDialog(t('common.saveSuccess'))
      setTimeout(() => router.push('/marketplace/profile'), 1200)
      return
    }
    const imageKeys = await uploadFilesByPresignedUrl(images.value.map(item => item.file).filter(Boolean))
    imageKeys.forEach((imageKey) => formData.append('imageKeys', imageKey))
    await request.post('/marketplace/item', formData)
    hideLoading()
    showDialog(t('marketplace.publish.publishSuccess'))
    setTimeout(() => router.push('/marketplace/home'), 1500)
  } catch (_) {
    submitting.value = false
    hideLoading()
  }
}

onMounted(() => {
  loadEditItem()
})
</script>

<template>
  <div class="marketplace-publish-page bg-[var(--c-surface)] min-h-screen">
    <CommunityHeader :title="isEditMode ? t('marketplace.publish.editTitle') : t('marketplace.publish.title')" moduleColor="var(--c-ershou)" :showBack="true" :backTo="isEditMode ? '/marketplace/profile' : '/marketplace/home'">
      <template #right>
        <a href="javascript:;" class="marketplace-publish-submit text-sm no-underline font-medium" @click.prevent="submit">
          {{ submitting ? t('marketplace.publish.submitting') : (isEditMode ? t('common.save') : t('marketplace.publish.finish')) }}
        </a>
      </template>
    </CommunityHeader>

    <div class="marketplace-publish-alert mx-4 mt-4 rounded-xl px-4 py-3 text-xs leading-6">
      请勿发布违法违规物品、危险品、证件原件、账号凭证或侵权盗版内容。请谨慎公开手机号、微信号、住址等联系方式，并自行核验商品状态、价格和线下交易风险。
    </div>

    <section class="marketplace-publish-panel mx-4 mt-4 overflow-hidden rounded-[28px]">
      <!-- 图片上传区 -->
      <section class="marketplace-publish-uploader">
        <div class="px-4 pt-6 mb-1.5">
          <div v-for="(img, index) in images" :key="index" class="w-[70px] h-[70px] relative inline-block mx-1.5 mb-2.5 align-top">
            <a v-if="!isEditMode" href="javascript:;" class="absolute -bottom-3.5 left-1/2 -ml-[19px] w-[18px] h-[18px] p-2.5 block" @click.prevent="removeImage(index)">
              <i class="w-[18px] h-[18px] block bg-[url('data:image/svg+xml,%3Csvg%20xmlns=%27http://www.w3.org/2000/svg%27%20viewBox=%270%200%20352%20512%27%20fill=%27%23fff%27%3E%3Cpath%20d=%27M242.7%20256l100.1-100.1c12.3-12.3%2012.3-32.2%200-44.5l-22.2-22.2c-12.3-12.3-32.2-12.3-44.5%200L176%20189.3%2075.9%2089.2c-12.3-12.3-32.2-12.3-44.5%200L9.2%20111.4c-12.3%2012.3-12.3%2032.2%200%2044.5L109.3%20256%209.2%20356.1c-12.3%2012.3-12.3%2032.2%200%2044.5l22.2%2022.2c12.3%2012.3%2032.2%2012.3%2044.5%200L176%20322.7l100.1%20100.1c12.3%2012.3%2032.2%2012.3%2044.5%200l22.2-22.2c12.3-12.3%2012.3-32.2%200-44.5L242.7%20256z%27/%3E%3C/svg%3E')] bg-no-repeat bg-center bg-contain"></i>
            </a>
            <i class="w-[70px] h-[70px] overflow-hidden block rounded"><img :src="img.dataUrl" alt="" class="w-full h-full object-cover block"></i>
          </div>
          <span v-if="!isEditMode && images.length < MAX_IMAGES" class="marketplace-publish-uploader__add w-[68px] h-[68px] border-2 border-white rounded inline-block mx-1.5 mb-2.5 align-top relative cursor-pointer" @click="triggerFileInput">
            <i class="absolute w-6 h-6 top-1/2 left-1/2 -mt-3 -ml-3">
              <i class="w-full h-0.5 absolute top-[11px] bg-white block"></i>
              <i class="h-full w-0.5 absolute left-[11px] bg-white block"></i>
            </i>
            <input type="file" accept="image/*" id="publish_file_input" @change="onFileChange" class="absolute top-0 left-0 w-[68px] h-[68px] opacity-0 z-[1] cursor-pointer">
          </span>
        </div>
        <p class="h-6 leading-6 text-center text-white text-sm pb-1">{{ isEditMode ? t('marketplace.publish.imageEditUnsupported') : t('marketplace.publish.imageLimitHint') }}</p>
      </section>

      <!-- 表单 -->
      <section class="marketplace-publish-form px-5">
        <p v-if="pageLoading" class="mt-4 text-[var(--c-text-3)] text-sm text-center">{{ t('marketplace.publish.loading') }}</p>

        <div :class="formRowClass(frmErrors.name)" class="relative pl-[90px] text-base min-h-[70px] pb-2.5">
          <p class="absolute left-0 text-[var(--c-text-1)] h-[70px] leading-[70px]">{{ t('marketplace.publish.field.name') }}</p>
          <div class="pt-[18px]">
            <input v-model="name" type="text" :placeholder="t('marketplace.publish.placeholder.name')" maxlength="25" class="marketplace-form-control text-[var(--c-text-1)] bg-transparent h-[34px] leading-[34px] text-base w-full border-none outline-none">
          </div>
          <p class="marketplace-form-error absolute bottom-0 left-0 h-[15px] leading-[15px] text-xs text-white px-1" :class="frmErrors.name ? 'block' : 'hidden'">{{ t('marketplace.publish.error.name') }}</p>
        </div>

        <div :class="formRowClass(frmErrors.description)" class="relative pl-[90px] text-base min-h-[70px] pb-2.5">
          <p class="absolute left-0 text-[var(--c-text-1)] h-[70px] leading-[70px]">{{ t('marketplace.publish.field.description') }}</p>
          <div class="pt-[18px]">
            <textarea v-model="description" name="description" :placeholder="t('marketplace.publish.placeholder.description')" maxlength="100" rows="3" class="marketplace-form-control marketplace-form-control--textarea resize-none border-none bg-transparent w-full text-base text-[var(--c-text-1)] p-0 mt-3.5 outline-none"></textarea>
          </div>
          <p class="marketplace-form-error absolute bottom-0 left-0 h-[15px] leading-[15px] text-xs text-white px-1" :class="frmErrors.description ? 'block' : 'hidden'">{{ t('marketplace.publish.error.description') }}</p>
        </div>

        <div :class="formRowClass(frmErrors.price)" class="relative pl-[90px] text-base min-h-[70px] pb-2.5">
          <p class="absolute left-0 text-[var(--c-text-1)] h-[70px] leading-[70px]">{{ t('marketplace.publish.field.price') }}</p>
          <div class="pt-[18px]">
            <input v-model="price" type="number" :placeholder="t('marketplace.publish.placeholder.price')" step="0.01" class="marketplace-form-control text-[var(--c-text-1)] bg-transparent h-[34px] leading-[34px] text-base w-full border-none outline-none">
          </div>
          <p class="marketplace-form-error absolute bottom-0 left-0 h-[15px] leading-[15px] text-xs text-white px-1" :class="frmErrors.price ? 'block' : 'hidden'">{{ t('marketplace.publish.error.price') }}</p>
        </div>

        <div :class="formRowClass(frmErrors.location)" class="relative pl-[90px] text-base min-h-[70px] pb-2.5">
          <p class="absolute left-0 text-[var(--c-text-1)] h-[70px] leading-[70px]">{{ t('marketplace.publish.field.location') }}</p>
          <div class="pt-[18px]">
            <input v-model="location" type="text" maxlength="30" class="marketplace-form-control text-[var(--c-text-1)] bg-transparent h-[34px] leading-[34px] text-base w-full border-none outline-none">
          </div>
          <p class="marketplace-form-error absolute bottom-0 left-0 h-[15px] leading-[15px] text-xs text-white px-1" :class="frmErrors.location ? 'block' : 'hidden'">{{ t('marketplace.publish.error.location') }}</p>
        </div>

        <div :class="formRowClass(frmErrors.type)" class="relative pl-[90px] text-base min-h-[70px] pb-2.5">
          <p class="absolute left-0 text-[var(--c-text-1)] h-[70px] leading-[70px]">{{ t('marketplace.publish.field.type') }}</p>
          <div class="pt-[18px]">
            <b class="marketplace-form-picker h-[34px] leading-[34px] text-base w-full text-[var(--c-text-1)] block relative cursor-pointer" @click="openTypePicker">
              <span>{{ typeNameDisplay || t('communityCommon.select') }}</span>
              <i class="marketplace-form-chevron absolute right-2.5 top-2.5 w-2 h-3 bg-no-repeat bg-center bg-contain"></i>
            </b>
          </div>
          <p class="marketplace-form-error absolute bottom-0 left-0 h-[15px] leading-[15px] text-xs text-white px-1" :class="frmErrors.type ? 'block' : 'hidden'">{{ t('marketplace.publish.error.type') }}</p>
        </div>

        <div :class="formRowClass(frmErrors.qq)" class="relative pl-[90px] text-base min-h-[70px] pb-2.5">
          <p class="absolute left-0 text-[var(--c-text-1)] h-[70px] leading-[70px]">{{ t('marketplace.publish.field.qq') }}</p>
          <div class="pt-[18px]">
            <input v-model="qq" type="text" maxlength="20" class="marketplace-form-control text-[var(--c-text-1)] bg-transparent h-[34px] leading-[34px] text-base w-full border-none outline-none">
          </div>
          <p class="marketplace-form-error absolute bottom-0 left-0 h-[15px] leading-[15px] text-xs text-white px-1" :class="frmErrors.qq ? 'block' : 'hidden'">{{ t('marketplace.publish.error.qq') }}</p>
        </div>

        <div class="marketplace-form-row relative pl-[90px] text-base min-h-[70px] pb-2.5">
          <p class="absolute left-0 text-[var(--c-text-1)] h-[70px] leading-[70px]">{{ t('marketplace.publish.field.phone') }}</p>
          <div class="pt-[18px]">
            <input v-model="phone" type="text" :placeholder="t('marketplace.publish.placeholder.phone')" maxlength="11" class="marketplace-form-control text-[var(--c-text-1)] bg-transparent h-[34px] leading-[34px] text-base w-full border-none outline-none">
          </div>
        </div>
      </section>
    </section>

    <!-- Dialog -->
    <div v-if="dialogVisible">
      <div class="fixed inset-0 bg-black/50 z-[1000]" @click="dialogVisible = false"></div>
      <div class="marketplace-dialog-shell fixed top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 bg-[var(--c-surface)] rounded-xl w-[280px] z-[1001] shadow-lg overflow-hidden">
        <div class="text-center font-semibold text-base text-[var(--c-text-1)] pt-5 pb-2">{{ t('common.hint') }}</div>
        <div class="text-center text-sm text-[var(--c-text-2)] px-5 pb-5">{{ dialogMessage }}</div>
        <div class="border-t border-[var(--c-border)]">
          <button class="marketplace-dialog-confirm w-full py-3 text-center font-medium text-base border-none bg-transparent cursor-pointer" @click="dialogVisible = false">{{ t('common.confirm') }}</button>
        </div>
      </div>
    </div>

    <!-- Type Picker -->
    <div v-if="typePickerVisible" class="fixed inset-0 z-[900]" @click.self="closeTypePicker">
      <div class="absolute inset-0 bg-black/50 z-[99]" @click="closeTypePicker"></div>
      <div class="marketplace-type-picker-shell w-[225px] fixed bg-[var(--c-surface)] left-1/2 -ml-[112px] top-1/2 -mt-[210px] z-[999] rounded-lg overflow-hidden shadow-lg">
        <div class="marketplace-type-picker-header h-10 border-b border-[var(--c-border)] relative leading-10 text-base text-[var(--c-text-1)] text-center">
          <a href="javascript:;" class="absolute right-1.5 top-0 block p-2.5" @click.prevent="closeTypePicker">
            <i class="block w-[11px] h-[13px] bg-[url('data:image/svg+xml,%3Csvg%20xmlns=%27http://www.w3.org/2000/svg%27%20viewBox=%270%200%20352%20512%27%20fill=%27%23999%27%3E%3Cpath%20d=%27M242.7%20256l100.1-100.1c12.3-12.3%2012.3-32.2%200-44.5l-22.2-22.2c-12.3-12.3-32.2-12.3-44.5%200L176%20189.3%2075.9%2089.2c-12.3-12.3-32.2-12.3-44.5%200L9.2%20111.4c-12.3%2012.3-12.3%2032.2%200%2044.5L109.3%20256%209.2%20356.1c-12.3%2012.3-12.3%2032.2%200%2044.5l22.2%2022.2c12.3%2012.3%2032.2%2012.3%2044.5%200L176%20322.7l100.1%20100.1c12.3%2012.3%2032.2%2012.3%2044.5%200l22.2-22.2c12.3-12.3%2012.3-32.2%200-44.5L242.7%20256z%27/%3E%3C/svg%3E')] bg-no-repeat bg-center bg-contain"></i>
          </a>
          <p>{{ t('marketplace.publish.typePickerTitle') }}</p>
        </div>
        <div class="py-2.5">
          <ul>
            <li v-for="(label, id) in typeNames" :key="id" class="marketplace-type-picker-item leading-[30px] h-[30px] text-center text-[var(--c-text-2)] text-sm">
              <a href="javascript:;" class="text-[var(--c-text-2)] block no-underline" @click.prevent="selectType(id)">{{ label }}</a>
            </li>
          </ul>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.marketplace-publish-panel {
  border: 1px solid color-mix(in srgb, var(--c-ershou) 20%, rgba(202, 222, 226, 0.78));
  background: color-mix(in srgb, var(--c-ershou) 4%, rgba(255, 255, 255, 0.92));
  box-shadow: 0 18px 40px rgba(24, 52, 60, 0.08);
}

.marketplace-publish-submit {
  color: color-mix(in srgb, var(--c-ershou) 84%, var(--c-text-1));
}

.marketplace-publish-submit:hover {
  color: color-mix(in srgb, var(--c-ershou) 92%, var(--c-text-1));
}

.marketplace-publish-alert {
  border: 1px solid color-mix(in srgb, var(--c-ershou) 18%, rgba(202, 222, 226, 0.82));
  background:
    linear-gradient(135deg, color-mix(in srgb, var(--c-ershou) 10%, rgba(255, 255, 255, 0.96)), color-mix(in srgb, var(--c-ershou) 4%, rgba(255, 255, 255, 0.92)));
  color: color-mix(in srgb, var(--c-ershou) 36%, var(--c-text-1));
  box-shadow: 0 12px 28px color-mix(in srgb, var(--c-ershou) 8%, transparent);
}

.marketplace-publish-uploader {
  border-bottom: 1px solid color-mix(in srgb, var(--c-ershou) 16%, rgba(202, 222, 226, 0.78));
  background: linear-gradient(180deg, color-mix(in srgb, var(--c-ershou) 86%, #6f9a7f), color-mix(in srgb, var(--c-ershou) 70%, #567b66));
}

.marketplace-publish-form {
  background: color-mix(in srgb, var(--c-ershou) 6%, rgba(255, 255, 255, 0.88));
}

.marketplace-form-row {
  border-bottom: 2px solid color-mix(in srgb, var(--c-ershou) 54%, rgba(180, 208, 214, 0.88));
}

.marketplace-form-control,
.marketplace-form-picker {
  display: block;
  width: 100%;
  padding: 0 12px;
  border-radius: 14px;
  background: color-mix(in srgb, var(--c-ershou) 4%, rgba(255, 255, 255, 0.56));
  box-shadow: inset 0 0 0 1px color-mix(in srgb, var(--c-ershou) 10%, transparent);
  transition: background-color 180ms ease, box-shadow 180ms ease, color 180ms ease;
}

.marketplace-form-control::placeholder {
  color: color-mix(in srgb, var(--c-ershou) 18%, var(--c-text-3));
}

.marketplace-form-control--textarea {
  min-height: 88px;
  padding: 10px 12px;
  line-height: 1.55;
}

.marketplace-form-control:focus,
.marketplace-form-picker:hover {
  background: color-mix(in srgb, var(--c-ershou) 6%, rgba(255, 255, 255, 0.84));
  box-shadow: inset 0 0 0 1px color-mix(in srgb, var(--c-ershou) 24%, transparent);
}

.marketplace-form-row--error {
  border-bottom-color: color-mix(in srgb, var(--c-danger) 72%, #fb7185);
}

.marketplace-form-error {
  background: color-mix(in srgb, var(--c-danger) 78%, #fb7185);
}

.marketplace-form-chevron {
  background-image: url("data:image/svg+xml,%3Csvg%20xmlns='http://www.w3.org/2000/svg'%20viewBox='0%200%20256%20512'%20fill='%230b9a72'%3E%3Cpath%20d='M224.3%20273l-136%20136c-9.4%209.4-24.6%209.4-33.9%200l-22.6-22.6c-9.4-9.4-9.4-24.6%200-33.9l96.4-96.4-96.4-96.4c-9.4-9.4-9.4-24.6%200-33.9L54.3%20103c9.4-9.4%2024.6-9.4%2033.9%200l136%20136c9.5%209.4%209.5%2024.6.1%2034z'/%3E%3C/svg%3E");
}

.marketplace-dialog-confirm {
  color: color-mix(in srgb, var(--c-ershou) 84%, var(--c-text-1));
}

.marketplace-dialog-shell,
.marketplace-type-picker-shell {
  border: 1px solid color-mix(in srgb, var(--c-ershou) 16%, rgba(202, 222, 226, 0.78));
  box-shadow: 0 24px 54px rgba(18, 41, 48, 0.16);
}

.marketplace-type-picker-header {
  border-top: 4px solid color-mix(in srgb, var(--c-ershou) 88%, var(--c-text-1));
}

.marketplace-type-picker-item a {
  border-radius: 10px;
  transition: background-color 180ms ease, color 180ms ease;
}

.marketplace-type-picker-item a:hover {
  background: color-mix(in srgb, var(--c-ershou) 10%, rgba(255, 255, 255, 0.84));
  color: color-mix(in srgb, var(--c-ershou) 78%, var(--c-text-1));
}

[data-theme="dark"] .marketplace-publish-alert {
  border-color: color-mix(in srgb, var(--c-ershou) 28%, rgba(68, 89, 112, 0.74));
  background:
    radial-gradient(circle at 100% 0, color-mix(in srgb, var(--c-ershou) 7%, transparent), transparent 36%),
    linear-gradient(135deg, rgba(31, 46, 58, 0.94), rgba(24, 38, 53, 0.9));
  color: color-mix(in srgb, var(--c-ershou) 36%, var(--c-text-1));
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.03),
    0 14px 28px rgba(0, 0, 0, 0.14);
}

[data-theme="dark"] .marketplace-publish-panel {
  border-color: color-mix(in srgb, var(--c-ershou) 22%, rgba(68, 89, 112, 0.74));
  background:
    radial-gradient(circle at 0 0, color-mix(in srgb, var(--c-ershou) 6%, transparent), transparent 28%),
    linear-gradient(180deg, rgba(23, 34, 47, 0.96), rgba(17, 27, 39, 0.98));
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.03),
    0 18px 36px rgba(0, 0, 0, 0.16);
}

[data-theme="dark"] .marketplace-publish-uploader {
  border-bottom-color: color-mix(in srgb, var(--c-ershou) 16%, rgba(68, 89, 112, 0.74));
  background:
    radial-gradient(circle at 12% 0, color-mix(in srgb, var(--c-ershou) 9%, transparent), transparent 30%),
    linear-gradient(180deg, rgba(24, 48, 46, 0.98), rgba(17, 33, 39, 0.96));
  box-shadow: inset 0 1px 0 color-mix(in srgb, var(--c-ershou) 22%, transparent);
}

[data-theme="dark"] .marketplace-publish-form {
  background: linear-gradient(180deg, rgba(28, 41, 56, 0.94), rgba(24, 36, 49, 0.96));
}

[data-theme="dark"] .marketplace-publish-uploader__add {
  border-color: color-mix(in srgb, var(--c-ershou) 42%, rgba(148, 163, 184, 0.7));
  background: rgba(32, 48, 68, 0.42);
}

[data-theme="dark"] .marketplace-form-row {
  border-bottom-color: color-mix(in srgb, var(--c-ershou) 34%, rgba(68, 89, 112, 0.74));
}

[data-theme="dark"] .marketplace-form-control,
[data-theme="dark"] .marketplace-form-picker {
  background: linear-gradient(180deg, rgba(34, 51, 65, 0.86), rgba(29, 43, 57, 0.94));
  box-shadow:
    inset 0 0 0 1px color-mix(in srgb, var(--c-ershou) 14%, rgba(91, 117, 129, 0.4)),
    inset 0 1px 0 rgba(255, 255, 255, 0.02);
}

[data-theme="dark"] .marketplace-form-control::placeholder {
  color: color-mix(in srgb, var(--c-ershou) 20%, rgba(196, 208, 223, 0.7));
}

[data-theme="dark"] .marketplace-form-control:focus,
[data-theme="dark"] .marketplace-form-picker:hover {
  background: linear-gradient(180deg, rgba(38, 57, 72, 0.92), rgba(31, 47, 61, 0.98));
  box-shadow:
    inset 0 0 0 1px color-mix(in srgb, var(--c-ershou) 28%, rgba(104, 213, 176, 0.24)),
    0 0 0 3px color-mix(in srgb, var(--c-ershou) 10%, transparent);
}

[data-theme="dark"] .marketplace-form-row--error {
  border-bottom-color: color-mix(in srgb, var(--c-danger) 48%, #fb7185);
}

[data-theme="dark"] .marketplace-form-error {
  background: color-mix(in srgb, var(--c-danger) 44%, rgba(190, 24, 93, 0.94));
}

[data-theme="dark"] .marketplace-form-chevron {
  background-image: url("data:image/svg+xml,%3Csvg%20xmlns='http://www.w3.org/2000/svg'%20viewBox='0%200%20256%20512'%20fill='%2368d5b0'%3E%3Cpath%20d='M224.3%20273l-136%20136c-9.4%209.4-24.6%209.4-33.9%200l-22.6-22.6c-9.4-9.4-9.4-24.6%200-33.9l96.4-96.4-96.4-96.4c-9.4-9.4-9.4-24.6%200-33.9L54.3%20103c9.4-9.4%2024.6-9.4%2033.9%200l136%20136c9.5%209.4%209.5%2024.6.1%2034z'/%3E%3C/svg%3E");
}

[data-theme="dark"] .marketplace-dialog-shell,
[data-theme="dark"] .marketplace-type-picker-shell {
  border-color: color-mix(in srgb, var(--c-ershou) 18%, rgba(93, 121, 134, 0.56));
  background:
    radial-gradient(circle at 0 0, color-mix(in srgb, var(--c-ershou) 8%, transparent), transparent 28%),
    linear-gradient(180deg, rgba(27, 39, 53, 0.98), rgba(20, 30, 42, 0.98));
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.03),
    0 26px 56px rgba(0, 0, 0, 0.34);
}

[data-theme="dark"] .marketplace-type-picker-header {
  border-top-color: color-mix(in srgb, var(--c-ershou) 42%, rgba(104, 213, 176, 0.55));
}

[data-theme="dark"] .marketplace-type-picker-item a:hover {
  background: linear-gradient(180deg, rgba(38, 57, 72, 0.92), rgba(31, 47, 61, 0.98));
  color: color-mix(in srgb, var(--c-ershou) 64%, #ecfdf5);
}
</style>
