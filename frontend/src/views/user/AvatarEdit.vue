<template>
  <div class="avatar-edit-page min-h-screen" :class="{ 'bg-black': isCropping }">
    <!-- Sticky Header -->
    <div class="avatar-edit-header sticky top-0 z-10 flex items-center h-12 px-4">
      <button type="button" class="avatar-edit-back w-15 text-base text-left cursor-pointer" @click="goBack">{{ t('common.back') }}</button>
      <div class="avatar-edit-title flex-1 text-center text-lg font-medium">{{ t('avatarEdit.title') }}</div>
      <div class="w-15"></div>
    </div>

    <!-- Display mode: current avatar + buttons -->
    <template v-if="!isCropping">
      <div class="max-w-lg mx-auto px-4 py-6">
        <div class="avatar-edit-card rounded-xl shadow-sm p-6">
          <div class="avatar-edit-tip mb-4 rounded-xl px-4 py-3 text-xs leading-6">
            上传头像或图片前，请自行确认其中不包含不必要的人脸、位置、证件、学生卡、联系方式或其他高敏信息。
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
import { ref, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import Cropper from 'cropperjs'
import request from '@/utils/request'
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
      aspectRatio: 1,
      viewMode: 1,
      dragMode: 'move',
      background: false,
      autoCropArea: 0.8
    })
  })
}

const confirmCrop = async () => {
  if (!cropperInstance) return
  const canvas = cropperInstance.getCroppedCanvas({ width: 200, height: 200 })
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

    await request.post('/profile/avatar', formData)

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
    await request.delete('/profile/avatar')
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
    const res = await request.get('/profile/avatar')
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
@import 'cropperjs/dist/cropper.css';
</style>

<style scoped>
.avatar-edit-page {
  background:
    radial-gradient(circle at top, color-mix(in srgb, var(--c-primary) 8%, transparent), transparent 30%),
    var(--c-bg-soft);
}

.avatar-edit-header {
  background: color-mix(in srgb, var(--c-surface) 94%, var(--c-bg));
  border-bottom: 1px solid var(--c-border-light);
  backdrop-filter: blur(18px);
}

.avatar-edit-back,
.avatar-edit-title {
  color: var(--c-text-1);
}

.avatar-edit-card {
  background: var(--c-surface);
  border: 1px solid color-mix(in srgb, var(--c-primary) 8%, var(--c-border-light));
  box-shadow: 0 12px 28px color-mix(in srgb, var(--c-primary) 8%, rgba(15, 23, 42, 0.06));
}

.avatar-edit-tip {
  border: 1px solid color-mix(in srgb, var(--c-primary) 10%, var(--c-border-light));
  background: color-mix(in srgb, var(--c-bg-soft) 76%, var(--c-surface));
  color: var(--c-text-2);
}

.avatar-edit-primary-action {
  background: linear-gradient(135deg, color-mix(in srgb, var(--c-primary) 88%, #2dd4bf), color-mix(in srgb, var(--c-primary) 72%, #0f766e));
  border: 1px solid transparent;
  box-shadow: 0 12px 28px color-mix(in srgb, var(--c-primary) 22%, transparent);
}

[data-theme="dark"] .avatar-edit-page {
  background:
    radial-gradient(circle at top, color-mix(in srgb, var(--c-primary) 10%, transparent), transparent 30%),
    var(--c-bg);
}

[data-theme="dark"] .avatar-edit-header {
  background: color-mix(in srgb, var(--c-surface) 88%, rgba(10, 20, 32, 0.9));
  border-bottom-color: rgba(68, 89, 112, 0.72);
}

[data-theme="dark"] .avatar-edit-card {
  border-color: rgba(68, 89, 112, 0.72);
  box-shadow: 0 18px 36px rgba(0, 0, 0, 0.2);
}

[data-theme="dark"] .avatar-edit-tip {
  border-color: rgba(68, 89, 112, 0.72);
  background: rgba(24, 38, 53, 0.84);
  color: var(--c-text-2);
}

[data-theme="dark"] .avatar-edit-primary-action {
  background: linear-gradient(135deg, color-mix(in srgb, var(--c-primary) 68%, #22d3ee), color-mix(in srgb, var(--c-primary) 54%, #0f766e));
}
</style>
