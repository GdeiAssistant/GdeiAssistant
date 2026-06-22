<script setup>
import { computed, onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute, useRouter } from 'vue-router'
import request from '../../utils/request'
import { uploadFilesByPresignedUrl } from '../../utils/presignedUpload'
import { useToast } from '../../composables/useToast'
import CommunityHeader from '../../components/community/CommunityHeader.vue'
import { createLostAndFoundItemTypeNames } from '../community/communityContent'

const route = useRoute()
const router = useRouter()
const { t, locale } = useI18n()
const { loading: showLoadingToast, hideLoading } = useToast()
const formData = ref({
  type: 0,
  itemType: -1,
  title: '',
  desc: '',
  location: '',
  contact: {
    qq: '',
    wechat: '',
    phone: ''
  },
  images: []
})
const dialogVisible = ref(false)
const dialogMessage = ref('')
const submitting = ref(false)
const itemTypePickerVisible = ref(false)
const pageLoading = ref(false)

const editItemId = computed(() => {
  const value = Number(route.query.id)
  return Number.isInteger(value) && value > 0 ? value : null
})
const isEditMode = computed(() => route.query.edit === '1' && editItemId.value !== null)
const itemTypeNames = computed(() => createLostAndFoundItemTypeNames(locale.value))
const itemTypeDisplay = computed(() => formData.value.itemType >= 0 && formData.value.itemType <= 11
  ? itemTypeNames.value[formData.value.itemType]
  : '')

function showDialog(msg) {
  dialogMessage.value = msg
  dialogVisible.value = true
}

function goBack() {
  router.push(isEditMode.value ? '/lostandfound/profile' : '/lostandfound/home')
}

function openItemTypePicker() {
  itemTypePickerVisible.value = true
}

function closeItemTypePicker() {
  itemTypePickerVisible.value = false
}

function selectItemType(index) {
  formData.value.itemType = index
  closeItemTypePicker()
}

function onFileChange(e) {
  if (isEditMode.value) {
    e.target.value = ''
    return
  }
  const files = Array.from(e.target.files)
  if (files.length + formData.value.images.length > 4) {
    showDialog(t('lostandfound.publish.imageLimit'))
    return
  }
  files.forEach(file => {
    if (!file.type.startsWith('image/')) {
      showDialog(t('lostandfound.publish.invalidImageType'))
      return
    }
    if (file.size > 5 * 1024 * 1024) {
      showDialog(t('lostandfound.publish.imageTooLarge'))
      return
    }
    const reader = new FileReader()
    reader.onload = (event) => {
      formData.value.images.push({
        previewUrl: event.target.result,
        file
      })
    }
    reader.readAsDataURL(file)
  })
  e.target.value = ''
}

function removeImage(index) {
  if (isEditMode.value) {
    showDialog(t('lostandfound.publish.imageEditUnsupported'))
    return
  }
  formData.value.images.splice(index, 1)
}

function populateForm(item) {
  formData.value.type = Number.isInteger(item?.lostType) ? item.lostType : 0
  formData.value.itemType = Number.isInteger(item?.itemType) ? item.itemType : -1
  formData.value.title = item?.name || ''
  formData.value.desc = item?.description || ''
  formData.value.location = item?.location || ''
  formData.value.contact.qq = item?.qq || ''
  formData.value.contact.wechat = item?.wechat || ''
  formData.value.contact.phone = item?.phone || ''
  formData.value.images = Array.isArray(item?.pictureURL) ? item.pictureURL.map((url) => ({
    previewUrl: url,
    readonly: true
  })) : []
}

async function loadEditItem() {
  if (!isEditMode.value) return
  pageLoading.value = true
  try {
    const res = await request.get('/lostandfound/profile')
    const data = res?.data || {}
    const list = []
      .concat(Array.isArray(data.lost) ? data.lost : [])
      .concat(Array.isArray(data.found) ? data.found : [])
      .concat(Array.isArray(data.didfound) ? data.didfound : [])
    const item = list.find((entry) => Number(entry.id) === editItemId.value)
    if (!item) {
      showDialog(t('lostandfound.publish.itemNotFound'))
      return
    }
    populateForm(item)
  } finally {
    pageLoading.value = false
  }
}

function buildPayload() {
  const payload = new FormData()
  payload.append('name', formData.value.title.trim())
  payload.append('description', formData.value.desc.trim())
  payload.append('location', formData.value.location.trim())
  payload.append('lostType', String(formData.value.type))
  payload.append('itemType', String(formData.value.itemType))
  if (formData.value.contact.qq) payload.append('qq', formData.value.contact.qq.trim())
  if (formData.value.contact.wechat) payload.append('wechat', formData.value.contact.wechat.trim())
  if (formData.value.contact.phone) payload.append('phone', formData.value.contact.phone.trim())
  return payload
}

async function submit() {
  if (submitting.value || pageLoading.value) return
  if (!formData.value.title || formData.value.title.trim() === '') {
    showDialog(t('lostandfound.publish.titleRequired'))
    return
  }
  if (formData.value.title.length > 25) {
    showDialog(t('lostandfound.publish.titleTooLong'))
    return
  }
  if (!formData.value.desc || formData.value.desc.trim() === '') {
    showDialog(t('lostandfound.publish.descRequired'))
    return
  }
  if (formData.value.desc.length > 100) {
    showDialog(t('lostandfound.publish.descTooLong'))
    return
  }
  if (!formData.value.location || formData.value.location.trim() === '') {
    showDialog(t('lostandfound.publish.locationRequired'))
    return
  }
  if (formData.value.location.length > 30) {
    showDialog(t('lostandfound.publish.locationTooLong'))
    return
  }
  if (formData.value.itemType < 0 || formData.value.itemType > 11) {
    showDialog(t('lostandfound.publish.itemTypeRequired'))
    return
  }
  if (!isEditMode.value && formData.value.images.length === 0) {
    showDialog(t('lostandfound.publish.imageRequired'))
    return
  }
  if (!formData.value.contact.qq && !formData.value.contact.wechat && !formData.value.contact.phone) {
    showDialog(t('lostandfound.publish.contactRequired'))
    return
  }
  submitting.value = true
  showLoadingToast(isEditMode.value ? t('lostandfound.publish.saving') : t('lostandfound.publish.uploading'))
  try {
    const payload = buildPayload()
    if (isEditMode.value) {
      await request.post(`/lostandfound/item/id/${editItemId.value}`, payload)
      hideLoading()
      showDialog(t('common.saveSuccess'))
      setTimeout(() => {
        router.push('/lostandfound/profile')
      }, 1200)
      return
    }
    const imageKeys = await uploadFilesByPresignedUrl(formData.value.images.map(item => item.file).filter(Boolean))
    imageKeys.forEach((imageKey) => payload.append('imageKeys', imageKey))
    await request.post('/lostandfound/item', payload)
    hideLoading()
    showDialog(t('lostandfound.publish.publishSuccess'))
    setTimeout(() => {
      router.push('/lostandfound/home')
    }, 1500)
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
  <div class="community-lostandfound-page min-h-screen bg-[var(--c-bg)]">
    <CommunityHeader
      :title="isEditMode ? t('lostandfound.publish.editTitle') : t('lostandfound.publish.title')"
      moduleColor="var(--c-lostandfound)"
      @back="goBack()"
      backTo=""
    >
      <template #right>
        <a href="javascript:;" class="community-lostandfound-submit-link text-sm no-underline font-medium" @click.prevent="submit">
          {{ submitting ? t('lostandfound.publish.submitting') : (isEditMode ? t('common.save') : t('lostandfound.publish.finish')) }}
        </a>
      </template>
    </CommunityHeader>

    <div class="p-0">
      <div class="community-lostandfound-privacy-hint mx-4 mt-4 rounded-xl px-4 py-3 text-xs leading-6">
        证件、学生卡、照片、联系方式等信息请尽量打码或最小化展示，避免公开完整证件号、完整手机号、完整二维码和其他足以导致冒领的信息。
      </div>

      <p v-if="pageLoading" class="mt-4 text-center text-[var(--c-text-3)] text-sm">{{ t('lostandfound.publish.loading') }}</p>

      <section class="community-lostandfound-card mx-2.5 overflow-hidden rounded-[26px]">
        <!-- 寻找类型 -->
        <div class="relative pl-[90px] text-base min-h-[70px] border-b border-[var(--c-border)] bg-[var(--c-surface)]">
          <p class="absolute left-0 text-[var(--c-text-1)] h-[70px] leading-[70px] pl-4 text-base">{{ t('lostandfound.publish.modeTitle') }}</p>
          <div class="pt-[18px] leading-[34px] text-base w-full">
            <label class="inline-block mr-8 cursor-pointer">
              <input type="radio" name="lostType" :value="0" v-model="formData.type" class="mr-1.5 align-middle w-auto h-auto" />{{ t('lostandfound.tab.lost') }}
            </label>
            <label class="inline-block mr-8 cursor-pointer">
              <input type="radio" name="lostType" :value="1" v-model="formData.type" class="mr-1.5 align-middle w-auto h-auto" />{{ t('lostandfound.publish.modeFoundOwner') }}
            </label>
          </div>
        </div>

        <!-- 物品分类 -->
        <div class="relative pl-[90px] text-base min-h-[70px] border-b border-[var(--c-border)] bg-[var(--c-surface)]">
          <p class="absolute left-0 text-[var(--c-text-1)] h-[70px] leading-[70px] pl-4 text-base">{{ t('lostandfound.publish.itemTypeTitle') }}</p>
          <div class="h-[70px] flex items-center cursor-pointer relative pt-0" @click="openItemTypePicker">
            <span class="text-[var(--c-text-1)] text-base">{{ itemTypeDisplay || t('communityCommon.select') }}</span>
            <i class="community-lostandfound-picker-arrow absolute right-2.5 top-[29px] w-2 h-3"></i>
          </div>
        </div>

        <!-- 物品名称 -->
        <div class="relative pl-[90px] text-base min-h-[70px] border-b border-[var(--c-border)] bg-[var(--c-surface)]">
          <p class="absolute left-0 text-[var(--c-text-1)] h-[70px] leading-[70px] pl-4 text-base">{{ t('lostandfound.publish.field.title') }}</p>
          <div class="pt-[18px]">
            <input type="text" :placeholder="t('lostandfound.publish.placeholder.title')" v-model="formData.title" maxlength="25" class="community-lostandfound-control text-[var(--c-text-1)] bg-transparent h-[34px] leading-[34px] text-base w-full border-none outline-none" />
          </div>
        </div>

        <!-- 物品描述 -->
        <div class="relative pl-[90px] text-base min-h-[70px] border-b border-[var(--c-border)] bg-[var(--c-surface)]">
          <p class="absolute left-0 text-[var(--c-text-1)] h-[70px] leading-[70px] pl-4 text-base">{{ t('lostandfound.publish.field.desc') }}</p>
          <div class="pt-[18px]">
            <input type="text" v-model="formData.desc" maxlength="100" class="community-lostandfound-control text-[var(--c-text-1)] bg-transparent h-[34px] leading-[34px] text-base w-full border-none outline-none" />
          </div>
        </div>

        <!-- 地点 -->
        <div class="relative pl-[90px] text-base min-h-[70px] bg-[var(--c-surface)]">
          <p class="absolute left-0 text-[var(--c-text-1)] h-[70px] leading-[70px] pl-4 text-base">{{ formData.type === 0 ? t('lostandfound.publish.lostLocation') : t('lostandfound.publish.foundLocation') }}</p>
          <div class="pt-[18px]">
            <input type="text" v-model="formData.location" maxlength="30" class="community-lostandfound-control text-[var(--c-text-1)] bg-transparent h-[34px] leading-[34px] text-base w-full border-none outline-none" />
          </div>
        </div>
      </section>

      <section class="community-lostandfound-card mx-2.5 mt-5 overflow-hidden rounded-[26px]">
        <!-- Contact tip -->
        <div class="community-lostandfound-contact-tip text-center text-sm py-3">{{ t('lostandfound.publish.contactTip') }}</div>

        <!-- QQ -->
        <div class="relative pl-[90px] text-base min-h-[70px] border-t border-[var(--c-border)] bg-[var(--c-surface)]">
          <p class="absolute left-0 text-[var(--c-text-1)] h-[70px] leading-[70px] pl-4 text-base">{{ t('lostandfound.detail.qq') }}</p>
          <div class="pt-[18px]">
            <input type="text" v-model="formData.contact.qq" maxlength="20" class="community-lostandfound-control text-[var(--c-text-1)] bg-transparent h-[34px] leading-[34px] text-base w-full border-none outline-none" />
          </div>
        </div>

        <!-- 微信 -->
        <div class="relative pl-[90px] text-base min-h-[70px] border-t border-[var(--c-border)] bg-[var(--c-surface)]">
          <p class="absolute left-0 text-[var(--c-text-1)] h-[70px] leading-[70px] pl-4 text-base">{{ t('lostandfound.detail.wechat') }}</p>
          <div class="pt-[18px]">
            <input type="text" v-model="formData.contact.wechat" maxlength="20" class="community-lostandfound-control text-[var(--c-text-1)] bg-transparent h-[34px] leading-[34px] text-base w-full border-none outline-none" />
          </div>
        </div>

        <!-- 手机号 -->
        <div class="relative pl-[90px] text-base min-h-[70px] border-t border-[var(--c-border)] bg-[var(--c-surface)]">
          <p class="absolute left-0 text-[var(--c-text-1)] h-[70px] leading-[70px] pl-4 text-base">{{ t('lostandfound.detail.phone') }}</p>
          <div class="pt-[18px]">
            <input type="tel" v-model="formData.contact.phone" maxlength="11" class="community-lostandfound-control text-[var(--c-text-1)] bg-transparent h-[34px] leading-[34px] text-base w-full border-none outline-none" />
          </div>
        </div>
      </section>

      <!-- 图片上传区 -->
      <section class="community-lostandfound-upload-shell community-lostandfound-card mx-2.5 mt-5 overflow-hidden rounded-[26px]">
        <div class="px-4 pt-6 mb-1.5">
          <div v-for="(img, index) in formData.images" :key="index" class="w-[70px] h-[70px] relative inline-block mx-1.5 mb-2.5 align-top">
            <a v-if="!isEditMode" href="javascript:;">
              <i class="absolute top-0 right-0 w-[18px] h-[18px] bg-[url('data:image/svg+xml,%3Csvg%20xmlns=%27http://www.w3.org/2000/svg%27%20viewBox=%270%200%20352%20512%27%20fill=%27%23fff%27%3E%3Cpath%20d=%27M242.7%20256l100.1-100.1c12.3-12.3%2012.3-32.2%200-44.5l-22.2-22.2c-12.3-12.3-32.2-12.3-44.5%200L176%20189.3%2075.9%2089.2c-12.3-12.3-32.2-12.3-44.5%200L9.2%20111.4c-12.3%2012.3-12.3%2032.2%200%2044.5L109.3%20256%209.2%20356.1c-12.3%2012.3-12.3%2032.2%200%2044.5l22.2%2022.2c12.3%2012.3%2032.2%2012.3%2044.5%200L176%20322.7l100.1%20100.1c12.3%2012.3%2032.2%2012.3%2044.5%200l22.2-22.2c12.3-12.3%2012.3-32.2%200-44.5L242.7%20256z%27/%3E%3C/svg%3E')] bg-no-repeat bg-center bg-contain cursor-pointer z-[1]" @click="removeImage(index)"></i>
            </a>
            <i class="w-[70px] h-[70px] overflow-hidden block">
              <img :src="img.previewUrl" :alt="t('lostandfound.publish.previewAlt')" class="w-full h-full object-cover" />
            </i>
          </div>
          <span v-if="!isEditMode && formData.images.length < 4" class="w-[68px] h-[68px] border-2 border-white inline-block mx-1.5 mb-2.5 align-top relative">
            <i class="absolute w-6 h-6 top-1/2 left-1/2 -mt-3 -ml-3">
              <i class="w-full h-0.5 absolute top-[11px] bg-white block"></i>
              <i class="h-full w-0.5 absolute left-[11px] bg-white block"></i>
            </i>
            <input type="file" accept="image/*" id="file_input" @change="onFileChange" class="absolute top-0 left-0 w-[68px] h-[68px] opacity-0 z-[1] cursor-pointer" />
          </span>
        </div>
        <p class="h-6 leading-6 text-center text-white text-sm pb-1">{{ isEditMode ? t('lostandfound.publish.imageEditUnsupported') : t('lostandfound.publish.imageLimitHint') }}</p>
      </section>
    </div>

    <!-- Dialog -->
    <div v-if="dialogVisible">
      <div class="fixed inset-0 bg-black/50 z-[1000]" @click="dialogVisible = false"></div>
      <div class="community-lostandfound-dialog-shell fixed top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 bg-[var(--c-surface)] rounded-xl w-[280px] z-[1001] shadow-lg overflow-hidden">
        <div class="text-center font-semibold text-base text-[var(--c-text-1)] pt-5 pb-2">{{ t('common.hint') }}</div>
        <div class="text-center text-sm text-[var(--c-text-2)] px-5 pb-5">{{ dialogMessage }}</div>
        <div class="border-t border-[var(--c-border)]">
          <button class="community-lostandfound-dialog-confirm w-full py-3 text-center font-medium text-base border-none bg-transparent cursor-pointer" @click="dialogVisible = false">{{ t('common.confirm') }}</button>
        </div>
      </div>
    </div>

    <!-- Item Type Picker -->
    <div v-if="itemTypePickerVisible">
      <div class="fixed inset-0 bg-black/50 z-[1000]" @click="closeItemTypePicker"></div>
      <div class="community-lostandfound-picker-shell fixed top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 bg-[var(--c-surface)] rounded-xl w-[280px] max-w-[320px] z-[1001] shadow-lg overflow-hidden">
        <div class="community-lostandfound-picker-title text-center font-semibold text-base text-[var(--c-text-1)] pt-5 pb-2">{{ t('lostandfound.publish.itemTypePickerTitle') }}</div>
        <div class="max-h-[280px] overflow-y-auto p-0 text-left">
          <div
            v-for="(label, index) in itemTypeNames"
            :key="label"
            class="community-lostandfound-picker-item py-3.5 px-5 border-t border-[var(--c-border)] text-[var(--c-text-1)] cursor-pointer first:border-t-0 hover:bg-[var(--c-bg)]"
            @click="selectItemType(index)"
          >
            {{ label }}
          </div>
        </div>
        <div class="border-t border-[var(--c-border)]">
          <button class="w-full py-3 text-center text-[var(--c-text-3)] font-medium text-base border-none bg-transparent cursor-pointer" @click="closeItemTypePicker">{{ t('common.cancel') }}</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.community-lostandfound-card {
  border: 1px solid color-mix(in srgb, var(--c-lostandfound) 18%, rgba(202, 222, 226, 0.8));
  background: color-mix(in srgb, var(--c-lostandfound) 4%, rgba(255, 255, 255, 0.92));
  box-shadow: 0 16px 36px rgba(24, 52, 60, 0.08);
}

.community-lostandfound-submit-link,
.community-lostandfound-dialog-confirm {
  color: color-mix(in srgb, var(--c-lostandfound) 84%, var(--c-text-1));
}

.community-lostandfound-privacy-hint {
  border: 1px solid color-mix(in srgb, var(--c-lostandfound) 16%, var(--c-border));
  background:
    linear-gradient(135deg, color-mix(in srgb, var(--c-lostandfound) 10%, transparent), color-mix(in srgb, var(--c-lostandfound) 4%, var(--c-surface)));
  color: color-mix(in srgb, var(--c-lostandfound) 76%, var(--c-text-1));
  box-shadow: 0 12px 28px color-mix(in srgb, var(--c-lostandfound) 8%, transparent);
}

.community-lostandfound-upload-shell {
  background:
    linear-gradient(180deg, color-mix(in srgb, var(--c-lostandfound) 16%, var(--c-surface)), color-mix(in srgb, var(--c-lostandfound) 58%, #87a8bd));
}

.community-lostandfound-picker-arrow {
  background-color: color-mix(in srgb, var(--c-lostandfound) 74%, var(--c-text-1));
  mask-image: url("data:image/svg+xml,%3Csvg%20xmlns='http://www.w3.org/2000/svg'%20viewBox='0%200%20256%20512'%3E%3Cpath%20fill='currentColor'%20d='M224.3%20273l-136%20136c-9.4%209.4-24.6%209.4-33.9%200l-22.6-22.6c-9.4-9.4-9.4-24.6%200-33.9l96.4-96.4-96.4-96.4c-9.4-9.4-9.4-24.6%200-33.9L54.3%20103c9.4-9.4%2024.6-9.4%2033.9%200l136%20136c9.5%209.4%209.5%2024.6.1%2034z'/%3E%3C/svg%3E");
  mask-repeat: no-repeat;
  mask-position: center;
  mask-size: contain;
  -webkit-mask-image: url("data:image/svg+xml,%3Csvg%20xmlns='http://www.w3.org/2000/svg'%20viewBox='0%200%20256%20512'%3E%3Cpath%20fill='currentColor'%20d='M224.3%20273l-136%20136c-9.4%209.4-24.6%209.4-33.9%200l-22.6-22.6c-9.4-9.4-9.4-24.6%200-33.9l96.4-96.4-96.4-96.4c-9.4-9.4-9.4-24.6%200-33.9L54.3%20103c9.4-9.4%2024.6-9.4%2033.9%200l136%20136c9.5%209.4%209.5%2024.6.1%2034z'/%3E%3C/svg%3E");
  -webkit-mask-repeat: no-repeat;
  -webkit-mask-position: center;
  -webkit-mask-size: contain;
}

.community-lostandfound-contact-tip {
  color: color-mix(in srgb, var(--c-lostandfound) 70%, #b45309);
}

.community-lostandfound-control {
  display: block;
  width: 100%;
  padding: 0 12px;
  border-radius: 14px;
  background: color-mix(in srgb, var(--c-lostandfound) 4%, rgba(255, 255, 255, 0.58));
  box-shadow: inset 0 0 0 1px color-mix(in srgb, var(--c-lostandfound) 9%, transparent);
  transition: background-color 180ms ease, box-shadow 180ms ease;
}

.community-lostandfound-control::placeholder {
  color: color-mix(in srgb, var(--c-lostandfound) 16%, var(--c-text-3));
}

.community-lostandfound-control:focus {
  background: color-mix(in srgb, var(--c-lostandfound) 6%, rgba(255, 255, 255, 0.84));
  box-shadow: inset 0 0 0 1px color-mix(in srgb, var(--c-lostandfound) 22%, transparent);
}

.community-lostandfound-dialog-shell,
.community-lostandfound-picker-shell {
  border: 1px solid color-mix(in srgb, var(--c-lostandfound) 16%, rgba(202, 222, 226, 0.76));
  box-shadow: 0 24px 54px rgba(18, 35, 48, 0.18);
}

.community-lostandfound-picker-title {
  border-top: 4px solid color-mix(in srgb, var(--c-lostandfound) 48%, rgba(119, 170, 205, 0.56));
}

.community-lostandfound-picker-item {
  transition: background-color 180ms ease, color 180ms ease;
}

.community-lostandfound-picker-item:hover {
  background: color-mix(in srgb, var(--c-lostandfound) 8%, rgba(255, 255, 255, 0.84));
  color: color-mix(in srgb, var(--c-lostandfound) 76%, var(--c-text-1));
}

[data-theme="dark"] .community-lostandfound-submit-link,
[data-theme="dark"] .community-lostandfound-dialog-confirm {
  color: color-mix(in srgb, var(--c-lostandfound) 54%, var(--c-text-1));
}

[data-theme="dark"] .community-lostandfound-card {
  border-color: rgba(68, 89, 112, 0.72);
  background:
    radial-gradient(circle at 0 0, color-mix(in srgb, var(--c-lostandfound) 7%, transparent), transparent 28%),
    linear-gradient(180deg, rgba(23, 34, 47, 0.96), rgba(18, 28, 41, 0.98));
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.03),
    0 18px 34px rgba(0, 0, 0, 0.16);
}

[data-theme="dark"] .community-lostandfound-privacy-hint {
  border-color: rgba(68, 89, 112, 0.72);
  background:
    radial-gradient(circle at 100% 0, color-mix(in srgb, var(--c-lostandfound) 8%, transparent), transparent 36%),
    linear-gradient(135deg, rgba(28, 43, 58, 0.94), rgba(22, 35, 49, 0.9));
  color: color-mix(in srgb, var(--c-lostandfound) 24%, var(--c-text-1));
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.03),
    0 14px 28px rgba(0, 0, 0, 0.14);
}

[data-theme="dark"] .community-lostandfound-upload-shell {
  background:
    linear-gradient(180deg, rgba(20, 30, 43, 0.96), color-mix(in srgb, var(--c-lostandfound) 20%, rgba(58, 78, 96, 0.92)));
}

[data-theme="dark"] .community-lostandfound-picker-arrow {
  background-color: color-mix(in srgb, var(--c-lostandfound) 52%, var(--c-text-1));
}

[data-theme="dark"] .community-lostandfound-contact-tip {
  color: color-mix(in srgb, var(--c-lostandfound) 48%, #fcd34d);
}

[data-theme="dark"] .community-lostandfound-control {
  background: linear-gradient(180deg, rgba(33, 46, 61, 0.88), rgba(28, 39, 52, 0.96));
  box-shadow:
    inset 0 0 0 1px color-mix(in srgb, var(--c-lostandfound) 12%, rgba(120, 153, 180, 0.18)),
    inset 0 1px 0 rgba(255, 255, 255, 0.02);
}

[data-theme="dark"] .community-lostandfound-control::placeholder {
  color: color-mix(in srgb, var(--c-lostandfound) 18%, rgba(206, 214, 224, 0.66));
}

[data-theme="dark"] .community-lostandfound-control:focus {
  background: linear-gradient(180deg, rgba(37, 52, 68, 0.92), rgba(30, 44, 58, 0.98));
  box-shadow:
    inset 0 0 0 1px color-mix(in srgb, var(--c-lostandfound) 24%, rgba(154, 201, 233, 0.2)),
    0 0 0 3px color-mix(in srgb, var(--c-lostandfound) 9%, transparent);
}

[data-theme="dark"] .community-lostandfound-dialog-shell,
[data-theme="dark"] .community-lostandfound-picker-shell {
  border-color: color-mix(in srgb, var(--c-lostandfound) 18%, rgba(111, 132, 156, 0.44));
  background:
    radial-gradient(circle at 0 0, color-mix(in srgb, var(--c-lostandfound) 8%, transparent), transparent 28%),
    linear-gradient(180deg, rgba(26, 38, 52, 0.98), rgba(20, 31, 43, 0.98));
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.03),
    0 26px 56px rgba(0, 0, 0, 0.34);
}

[data-theme="dark"] .community-lostandfound-picker-title {
  border-top-color: color-mix(in srgb, var(--c-lostandfound) 28%, rgba(154, 201, 233, 0.32));
}

[data-theme="dark"] .community-lostandfound-picker-item:hover {
  background: linear-gradient(180deg, rgba(37, 52, 68, 0.92), rgba(30, 44, 58, 0.98));
  color: color-mix(in srgb, var(--c-lostandfound) 56%, #eff6ff);
}
</style>
