<template>
  <div class="subpage avatar-edit-page min-h-screen" :class="{ 'bg-black': isCropping }">
    <!-- Sticky Header -->
    <div class="subpage-bar avatar-edit-header">
      <button type="button" class="subpage-bar__back avatar-edit-back" @click="goBack">
        <ChevronLeft :size="18" aria-hidden="true" />
        <span>{{ t('common.back') }}</span>
      </button>
      <div class="subpage-bar__title avatar-edit-title">{{ t('avatarEdit.title') }}</div>
      <span aria-hidden="true"></span>
    </div>

    <!-- Display mode: current avatar + buttons -->
    <template v-if="!isCropping">
      <div class="subpage-body max-w-lg mx-auto px-4 py-6">
        <div class="avatar-edit-card rounded-xl shadow-sm p-6">
          <div class="avatar-edit-tip mb-4 rounded-xl px-4 py-3 text-xs leading-6">
            {{ t('avatarEdit.privacyHint') }}
          </div>
          <div class="flex items-center justify-center min-h-[320px] w-full">
            <img :src="currentAvatar" class="w-[92%] max-w-[92%] h-auto max-h-[70vh] object-contain block" :alt="t('avatarEdit.currentAvatar')" />
          </div>
          <div class="flex flex-col items-center gap-4 mt-8 px-2">
            <button
              type="button"
              class="w-4/5 max-w-[300px] py-3 px-6 text-base rounded-lg border border-red-500 bg-transparent text-red-500 cursor-pointer"
              @click="handleDelete"
            >{{ t('avatarEdit.delete') }}</button>
            <button
              type="button"
              class="avatar-edit-primary-action w-4/5 max-w-[300px] py-3 px-6 text-base rounded-lg text-white cursor-pointer"
              @click="triggerSelect"
            >{{ t('avatarEdit.change') }}</button>
          </div>
        </div>
      </div>
      <input type="file" ref="fileInput" accept="image/*" class="hidden" @change="onFileChange" />
    </template>

    <!-- Crop mode -->
    <template v-else>
      <div class="bg-black min-h-[calc(100vh-48px)] flex flex-col">
        <div class="flex-1 min-h-0 w-full">
          <img ref="cropperImgRef" :src="tempImage" :alt="t('avatarEdit.crop')" class="block max-w-full max-h-full" />
        </div>
        <div class="py-5 px-4 flex justify-around bg-black/60">
          <button
            type="button"
            class="min-w-[120px] py-3 px-6 text-base rounded-lg cursor-pointer bg-transparent border border-white text-white"
            @click="cancelCrop"
          >{{ t('common.cancel') }}</button>
          <button
            type="button"
            class="avatar-edit-primary-action min-w-[120px] py-3 px-6 text-base rounded-lg cursor-pointer text-white"
            @click="confirmCrop"
          >{{ t('common.confirm') }}</button>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { postProfileAvatar, deleteProfileAvatar, getProfileAvatar } from "../../api/profileEndpoints.js"

import { ChevronLeft } from 'lucide-vue-next'
import { ref, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import Cropper from 'cropperjs'

import { uploadFileByPresignedUrl } from '@/utils/presignedUpload'
import { useToast } from '@/composables/useToast'

const router = useRouter()
const { t } = useI18n()
const { success: toastSuccess, error: toastError, loading: toastLoading, hideLoading } = useToast()
const defaultAvatar = '/img/login/qq.png'

const currentAvatar = ref(defaultAvatar)
const isCropping = ref(false)
const tempImage = ref('')
const cropperImgRef = ref(null)
const fileInput = ref(null)
let cropperInstance = null

const cropperTemplate = `
  <cropper-canvas>
    <cropper-image scalable translatable></cropper-image>
    <cropper-shade hidden></cropper-shade>
    <cropper-handle action="select" plain></cropper-handle>
    <cropper-selection initial-coverage="0.8" aspect-ratio="1" movable resizable>
      <cropper-grid role="grid" bordered covered></cropper-grid>
      <cropper-crosshair centered></cropper-crosshair>
      <cropper-handle action="move" theme-color="rgba(255, 255, 255, 0.35)"></cropper-handle>
      <cropper-handle action="n-resize"></cropper-handle>
      <cropper-handle action="e-resize"></cropper-handle>
      <cropper-handle action="s-resize"></cropper-handle>
      <cropper-handle action="w-resize"></cropper-handle>
      <cropper-handle action="ne-resize"></cropper-handle>
      <cropper-handle action="nw-resize"></cropper-handle>
      <cropper-handle action="se-resize"></cropper-handle>
      <cropper-handle action="sw-resize"></cropper-handle>
    </cropper-selection>
  </cropper-canvas>
`

const goBack = () => router.back()

const triggerSelect = () => fileInput.value?.click()

const onFileChange = (e) => {
  const file = e.target.files?.[0]
  if (!file) return
  const reader = new FileReader()
  reader.onload = (event) => {
    tempImage.value = event.target.result
    isCropping.value = true
    initCropper()
  }
  reader.readAsDataURL(file)
}

const initCropper = () => {
  nextTick(() => {
    if (!cropperImgRef.value) return
    if (cropperInstance) cropperInstance.destroy()
    cropperInstance = new Cropper(cropperImgRef.value, {
      template: cropperTemplate
    })
  })
}

const confirmCrop = async () => {
  if (!cropperInstance) return
  const selection = cropperInstance.getCropperSelection()
  const canvas = await selection?.$toCanvas({ width: 200, height: 200 })
  if (!canvas) return

  toastLoading(t('avatarEdit.uploading'))

  try {
    const blob = await new Promise((resolve, reject) => {
      canvas.toBlob((b) => (b ? resolve(b) : reject(new Error(t('avatarEdit.cropFailed')))), 'image/jpeg')
    })
    const file = new File([blob], 'avatar.jpg', { type: 'image/jpeg' })
    const [avatarKey, avatarHdKey] = await Promise.all([
      uploadFileByPresignedUrl(file),
      uploadFileByPresignedUrl(file, { fileName: 'avatar_hd.jpg' })
    ])
    const formData = new FormData()
    formData.append('avatarKey', avatarKey)
    formData.append('avatarHdKey', avatarHdKey)

    await postProfileAvatar(formData)

    toastSuccess(t('avatarEdit.updateSuccess'))
    router.back()
  } catch (_) {
    toastError(t('common.saveFailed'))
  } finally {
    hideLoading()
    if (cropperInstance) {
      cropperInstance.destroy()
      cropperInstance = null
    }
    isCropping.value = false
    tempImage.value = ''
    if (fileInput.value) fileInput.value.value = ''
  }
}

const cancelCrop = () => {
  isCropping.value = false
  tempImage.value = ''
  if (cropperInstance) {
    cropperInstance.destroy()
    cropperInstance = null
  }
  if (fileInput.value) fileInput.value.value = ''
}

const handleDelete = async () => {
  if (!confirm(t('avatarEdit.deleteConfirm'))) return
  toastLoading(t('avatarEdit.deleting'))
  try {
    await deleteProfileAvatar()
    currentAvatar.value = defaultAvatar
    toastSuccess(t('avatarEdit.deleteSuccess'))
    router.back()
  } catch (_) {
    toastError(t('avatarEdit.deleteFailed'))
  } finally {
    hideLoading()
  }
}

onMounted(async () => {
  try {
    const res = await getProfileAvatar()
    if (res && res.success && typeof res.data === 'string' && res.data) {
      currentAvatar.value = res.data
    } else {
      currentAvatar.value = defaultAvatar
    }
  } catch (_) {
    currentAvatar.value = defaultAvatar
  }
})
</script>

<style>
</style>

<style scoped>
.avatar-edit-page {
  background: var(--c-bg);
}

.avatar-edit-back,
.avatar-edit-title {
  color: var(--c-text-1);
}

.avatar-edit-card {
  background: var(--c-surface);
  border: 1px solid var(--c-border);
  box-shadow: none;
}

.avatar-edit-tip {
  border: 1px solid var(--c-border);
  background: color-mix(in srgb, var(--c-bg) 76%, var(--c-surface));
  color: var(--c-text-2);
}

.avatar-edit-primary-action {
  background: var(--c-primary);
  border: 1px solid transparent;
  box-shadow: none;
}
</style>
