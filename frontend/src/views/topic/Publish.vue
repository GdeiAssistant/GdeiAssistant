<script setup>
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import request from '../../utils/request'
import { uploadFilesByPresignedUrl } from '../../utils/presignedUpload'
import { useToast } from '@/composables/useToast'
import CommunityHeader from '../../components/community/CommunityHeader.vue'

const router = useRouter()
const { t } = useI18n()
const { success: toastSuccess, loading: toastLoading, hideLoading } = useToast()
const topicTag = ref('')
const content = ref('')
const images = ref([])
const imageFiles = ref([])
const submitting = ref(false)
const dialogVisible = ref(false)
const dialogMessage = ref('')

function showDialog(msg) {
  dialogMessage.value = msg
  dialogVisible.value = true
}

function onFileChange(e) {
  const files = Array.from(e.target.files)
  if (images.value.length + files.length > 9) {
    showDialog(t('topic.publish.imageLimit'))
    return
  }
  files.forEach(file => {
    if (!file.type.startsWith('image/')) {
      showDialog(t('topic.publish.invalidImageType'))
      return
    }
    if (file.size > 5 * 1024 * 1024) {
      showDialog(t('topic.publish.imageTooLarge'))
      return
    }
    imageFiles.value.push(file)
    images.value.push(URL.createObjectURL(file))
  })
}

function removeImage(index) {
  images.value.splice(index, 1)
  imageFiles.value.splice(index, 1)
}

async function submit() {
  if (!topicTag.value || !topicTag.value.trim()) {
    showDialog(t('topic.publish.topicRequired'))
    return
  }
  if (!content.value || !content.value.trim()) {
    showDialog(t('topic.publish.contentRequired'))
    return
  }
  if (content.value.trim().length > 250) {
    showDialog(t('topic.publish.contentTooLong'))
    return
  }

  submitting.value = true
  const topic = topicTag.value.trim()
  const contentVal = content.value.trim()
  const count = imageFiles.value.length
  toastLoading(count > 0 ? t('topic.publish.uploading') : t('topic.publish.publishing'))
  try {
    const imageKeys = count > 0 ? await uploadFilesByPresignedUrl(imageFiles.value) : []
    const fd = new FormData()
    fd.append('topic', topic)
    fd.append('content', contentVal)
    fd.append('count', String(imageKeys.length))
    imageKeys.forEach((imageKey) => fd.append('imageKeys', imageKey))
    await request.post('/topic', fd)
    hideLoading()
    toastSuccess(t('topic.publish.publishSuccess'))
    setTimeout(() => router.push('/topic/home'), 1500)
  } catch (_) {
    submitting.value = false
    hideLoading()
  }
}
</script>

<template>
  <div class="bg-[var(--c-bg)] min-h-screen pb-16">
    <CommunityHeader :title="t('topic.publish.title')" moduleColor="#6366f1" @back="router.back()" backTo="">
      <template #right>
        <button
          type="button"
          class="px-5 py-1.5 bg-[var(--c-topic)] text-white border-none rounded-full text-sm cursor-pointer transition-opacity duration-300 disabled:opacity-60 disabled:cursor-not-allowed"
          :disabled="submitting"
          @click="submit"
        >
          {{ submitting ? t('topic.publish.submitting') : t('topic.publish.submitAction') }}
        </button>
      </template>
    </CommunityHeader>

    <div class="p-4">
      <!-- 话题输入区 -->
      <div class="flex items-center bg-[var(--c-surface)] rounded-lg px-4 py-3 mb-4">
        <span class="text-xl text-[var(--c-topic)] font-semibold mr-2">#</span>
        <input
          type="text"
          class="flex-1 border-none outline-none text-sm text-[var(--c-text-1)] bg-transparent"
          :placeholder="t('topic.publish.topicPlaceholder')"
          v-model="topicTag"
          maxlength="20"
        />
      </div>

      <!-- 正文输入区 -->
      <div class="relative bg-[var(--c-surface)] rounded-lg p-4 mb-4">
        <textarea
          class="w-full border-none outline-none text-base text-[var(--c-text-1)] leading-relaxed resize-none bg-transparent min-h-[200px]"
          :placeholder="t('topic.publish.contentPlaceholder')"
          v-model="content"
          maxlength="250"
          rows="8"
        ></textarea>
        <div class="absolute bottom-2.5 right-4 text-xs" :class="content.length > 250 ? 'text-red-600' : 'text-[var(--c-text-3)]'">
          {{ content.length }}/250
        </div>
      </div>

      <!-- 图片上传区 -->
      <div class="bg-[var(--c-surface)] rounded-lg p-4">
        <div class="grid grid-cols-3 gap-2.5">
          <div
            v-for="(img, idx) in images"
            :key="idx"
            class="relative aspect-square rounded-lg overflow-hidden bg-gray-100"
          >
            <img :src="img" class="w-full h-full object-cover" />
            <button
              type="button"
              class="absolute top-1 right-1 w-6 h-6 bg-black/60 text-white border-none rounded-full text-lg leading-none cursor-pointer flex items-center justify-center"
              @click="removeImage(idx)"
            >×</button>
          </div>
          <div v-if="images.length < 9" class="aspect-square border-2 border-dashed border-[var(--c-border)] rounded-lg flex flex-col items-center justify-center cursor-pointer bg-[var(--c-bg)] relative" @click="$refs.fileInput.click()">
            <input
              type="file"
              accept="image/*"
              multiple
              :max="9"
              @change="onFileChange"
              ref="fileInput"
              class="hidden"
            />
            <span class="text-[32px] text-[var(--c-text-3)] mb-1">+</span>
            <span class="text-xs text-[var(--c-text-3)]">{{ t('topic.publish.addImage') }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 提示对话框 -->
    <div v-if="dialogVisible" class="fixed inset-0 bg-black/50 z-[1000]" @click="dialogVisible = false"></div>
    <div v-if="dialogVisible" class="fixed top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[80%] max-w-[320px] bg-[var(--c-surface)] rounded-xl z-[1001] overflow-hidden">
      <div class="text-center font-semibold text-base text-[var(--c-text-1)] py-4">{{ t('common.hint') }}</div>
      <div class="px-5 pb-4 text-sm text-[var(--c-text-1)] text-center">{{ dialogMessage }}</div>
      <div class="flex border-t border-[var(--c-border)]">
        <a href="javascript:;" class="flex-1 py-3 text-center text-sm text-[#6366f1] font-semibold no-underline cursor-pointer" @click="dialogVisible = false">{{ t('common.confirm') }}</a>
      </div>
    </div>
  </div>
</template>
