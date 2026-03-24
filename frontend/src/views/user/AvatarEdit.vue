<template>
  <div class="min-h-screen bg-gray-50" :class="{ 'bg-black': isCropping }">
    <!-- Sticky Header -->
    <div class="sticky top-0 z-10 flex items-center h-12 bg-white border-b border-gray-200 px-4">
      <button type="button" class="w-15 text-base text-gray-700 text-left cursor-pointer" @click="goBack">返回</button>
      <div class="flex-1 text-center text-lg font-medium text-black">个人头像</div>
      <div class="w-15"></div>
    </div>

    <!-- Display mode: current avatar + buttons -->
    <template v-if="!isCropping">
      <div class="max-w-lg mx-auto px-4 py-6">
        <div class="bg-white rounded-xl shadow-sm p-6">
          <div class="flex items-center justify-center min-h-[320px] w-full">
            <img :src="currentAvatar" class="w-[92%] max-w-[92%] h-auto max-h-[70vh] object-contain block" alt="当前头像" />
          </div>
          <div class="flex flex-col items-center gap-4 mt-8 px-2">
            <button
              type="button"
              class="w-4/5 max-w-[300px] py-3 px-6 text-base rounded-lg border border-red-500 bg-transparent text-red-500 cursor-pointer"
              @click="handleDelete"
            >删除头像</button>
            <button
              type="button"
              class="w-4/5 max-w-[300px] py-3 px-6 text-base rounded-lg border border-green-500 bg-green-500 text-white cursor-pointer"
              @click="triggerSelect"
            >更换头像</button>
          </div>
        </div>
      </div>
      <input type="file" ref="fileInput" accept="image/*" class="hidden" @change="onFileChange" />
    </template>

    <!-- Crop mode -->
    <template v-else>
      <div class="bg-black min-h-[calc(100vh-48px)] flex flex-col">
        <div class="flex-1 min-h-0 w-full">
          <img ref="cropperImgRef" :src="tempImage" alt="裁剪" class="block max-w-full max-h-full" />
        </div>
        <div class="py-5 px-4 flex justify-around bg-black/60">
          <button
            type="button"
            class="min-w-[120px] py-3 px-6 text-base rounded-lg cursor-pointer bg-transparent border border-white text-white"
            @click="cancelCrop"
          >取消</button>
          <button
            type="button"
            class="min-w-[120px] py-3 px-6 text-base rounded-lg cursor-pointer bg-green-500 border-green-500 text-white"
            @click="confirmCrop"
          >完成</button>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import Cropper from 'cropperjs'
import request from '@/utils/request'
import { uploadFileByPresignedUrl } from '@/utils/presignedUpload'
import { useToast } from '@/composables/useToast'

const router = useRouter()
const { success: toastSuccess, loading: toastLoading, hideLoading } = useToast()
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

  toastLoading('正在上传头像...')

  try {
    const blob = await new Promise((resolve, reject) => {
      canvas.toBlob((b) => (b ? resolve(b) : reject(new Error('裁剪失败'))), 'image/jpeg')
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

    toastSuccess('更新头像完成')
    router.back()
  } catch (e) {
    console.error('头像上传失败', e)
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
  if (!confirm('确定要删除头像并恢复默认吗？')) return
  toastLoading('正在删除头像...')
  try {
    await request.delete('/profile/avatar')
    currentAvatar.value = defaultAvatar
    toastSuccess('已恢复默认头像')
    router.back()
  } catch (e) {
    console.error('删除头像失败', e)
    toastSuccess('删除头像失败，请稍后再试')
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
