<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import request from '../../utils/request'
import { uploadFilesByPresignedUrl } from '../../utils/presignedUpload'
import { useToast } from '../../composables/useToast'
import CommunityHeader from '../../components/community/CommunityHeader.vue'
import { getPhotographCopy } from './photographContent'

const router = useRouter()
const { t, locale } = useI18n()
const { success: toastSuccess, loading: toastLoading, hideLoading } = useToast()
const copy = computed(() => getPhotographCopy(locale.value))

const form = ref({
  title: '',
  type: 1,
  content: ''
})

const mainImageUrl = ref('')
const subImages = ref(['', '', ''])
const mainImageFile = ref(null)
const subImageFiles = ref([null, null, null])
const dialogVisible = ref(false)
const dialogMessage = ref('')
const submitting = ref(false)

const showDialog = (msg) => {
  dialogMessage.value = msg
  dialogVisible.value = true
}

const goBack = () => {
  router.back()
}

const onMainImageChange = (e) => {
  const file = e.target.files?.[0]
  if (file) {
    mainImageFile.value = file
    mainImageUrl.value = URL.createObjectURL(file)
  }
  e.target.value = ''
}

const onSubImageChange = (e, idx) => {
  const file = e.target.files?.[0]
  if (file) {
    subImageFiles.value[idx] = file
    subImages.value[idx] = URL.createObjectURL(file)
  }
  e.target.value = ''
}

const clearImages = () => {
  mainImageUrl.value = ''
  subImages.value = ['', '', '']
  mainImageFile.value = null
  subImageFiles.value = [null, null, null]
}

const submit = async () => {
  const title = form.value.title?.trim()
  if (!title) {
    showDialog(copy.value.titleRequired)
    return
  }
  if (!mainImageFile.value) {
    showDialog(copy.value.mainImageRequired)
    return
  }
  if (submitting.value) return
  submitting.value = true
  toastLoading(copy.value.uploadLoading)
  try {
    const files = [mainImageFile.value, ...subImageFiles.value.filter(Boolean)]
    const imageKeys = await uploadFilesByPresignedUrl(files)
    const fd = new FormData()
    fd.append('title', title)
    fd.append('content', form.value.content?.trim() || '')
    fd.append('count', String(imageKeys.length))
    fd.append('type', String(Number(form.value.type) || 1))
    imageKeys.forEach((imageKey) => {
      fd.append('imageKeys', imageKey)
    })
    await request.post('/photograph', fd)
    hideLoading()
    toastSuccess(copy.value.publishSuccess)
    setTimeout(() => router.push('/photograph/home'), 1500)
  } catch (_) {
    submitting.value = false
    hideLoading()
  }
}
</script>

<template>
  <div class="min-h-screen bg-[var(--c-bg)]" :style="{ '--module-color': '#06b6d4' }">
    <CommunityHeader :title="t('feature.photograph.name')" moduleColor="#06b6d4" @back="goBack" :backTo="''" :showBack="true" />

    <!-- Upload form -->
    <section class="p-6">
      <!-- Title -->
      <div class="mb-4">
        <label class="block text-base font-medium text-[var(--c-text-1)] mb-2">{{ copy.titleLabel }}<span class="text-red-500 font-bold ml-0.5">*</span></label>
        <input
          type="text"
          maxlength="25"
          class="w-full box-border px-3 py-2 border border-[var(--c-divider)] rounded-lg text-base text-[var(--c-text-1)] bg-[var(--c-card)] transition-colors focus:outline-none focus:border-cyan-500"
          :placeholder="copy.titlePlaceholder"
          v-model="form.title"
        />
      </div>

      <!-- Type -->
      <div class="mb-4">
        <label class="block text-base font-medium text-[var(--c-text-1)] mb-2">{{ copy.typeLabel }}</label>
        <div class="flex gap-4">
          <label class="flex items-center gap-1 text-base text-[var(--c-text-2)] cursor-pointer">
            <input type="radio" name="type" value="1" v-model="form.type" class="accent-cyan-500" />
            <span>{{ copy.typeLife }}</span>
          </label>
          <label class="flex items-center gap-1 text-base text-[var(--c-text-2)] cursor-pointer">
            <input type="radio" name="type" value="2" v-model="form.type" class="accent-cyan-500" />
            <span>{{ copy.typeCampus }}</span>
          </label>
        </div>
      </div>

      <!-- Main image -->
      <label class="block text-base font-medium text-[var(--c-text-1)] mb-3">{{ copy.mainImageLabel }}<span class="text-red-500 font-bold ml-0.5">*</span></label>
      <div class="relative overflow-hidden border-2 border-dashed border-cyan-500 rounded-xl h-40 flex items-center justify-center mb-4 cursor-pointer transition-colors">
        <input type="file" accept="image/*" class="absolute top-0 left-0 w-full h-full opacity-0 cursor-pointer z-10" @change="onMainImageChange" />
        <div v-if="!mainImageUrl" class="text-[40px] text-[var(--c-text-3)] z-[1] pointer-events-none">+</div>
        <img v-else :src="mainImageUrl" class="absolute inset-0 w-full h-full object-cover z-[5]" :alt="copy.mainPreviewAlt" />
      </div>

      <!-- Sub images -->
      <label class="block text-base font-medium text-[var(--c-text-1)] mb-3">{{ copy.subImagesLabel }}</label>
      <div class="flex gap-3 mb-4">
        <div v-for="(_, idx) in 3" :key="idx" class="flex-1 aspect-square relative overflow-hidden border-2 border-dashed border-[var(--c-divider)] rounded-xl flex items-center justify-center cursor-pointer transition-colors hover:border-cyan-500">
          <input type="file" accept="image/*" class="absolute top-0 left-0 w-full h-full opacity-0 cursor-pointer z-10" @change="onSubImageChange($event, idx)" />
          <div v-if="!subImages[idx]" class="text-2xl text-[var(--c-text-3)] z-[1] pointer-events-none">+</div>
          <img v-else :src="subImages[idx]" class="absolute inset-0 w-full h-full object-cover z-[5]" :alt="copy.subPreviewAlt" />
        </div>
      </div>

      <!-- Clear button -->
      <button class="w-full bg-[var(--c-bg)] text-[var(--c-text-2)] border border-[var(--c-divider)] rounded-lg px-3 py-2 text-base cursor-pointer transition-colors active:bg-[var(--c-border)]" type="button" @click="clearImages">{{ copy.clearImagesAction }}</button>

      <!-- Content textarea -->
      <div class="mt-4 mb-4">
        <label class="block text-base font-medium text-[var(--c-text-1)] mb-2">{{ copy.contentLabel }}</label>
        <textarea
          class="w-full box-border px-3 py-2 border border-[var(--c-divider)] rounded-lg text-base text-[var(--c-text-1)] bg-[var(--c-card)] resize-y transition-colors focus:outline-none focus:border-cyan-500"
          rows="4"
          :placeholder="copy.contentPlaceholder"
          v-model="form.content"
        ></textarea>
        <span class="float-right text-sm text-[var(--c-text-3)] mt-1">{{ copy.formatContentCount((form.content || '').length) }}</span>
      </div>

      <!-- Submit -->
      <button type="button" class="block mx-auto mt-6 w-full bg-cyan-500 text-white border-none rounded-lg p-3 text-lg font-medium cursor-pointer transition-opacity active:opacity-85" @click="submit">{{ copy.submitAction }}</button>
    </section>

    <!-- Dialog -->
    <div v-if="dialogVisible">
      <div class="fixed inset-0 bg-black/50 z-[1000]" @click="dialogVisible = false"></div>
      <div class="fixed top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[280px] bg-[var(--c-surface)] rounded-xl overflow-hidden z-[1001] shadow-lg">
        <div class="text-center font-bold text-base py-4 text-[var(--c-text-1)]">{{ t('common.hint') }}</div>
        <div class="px-6 pb-4 text-center text-sm text-[var(--c-text-2)] leading-relaxed">{{ dialogMessage }}</div>
        <div class="border-t border-[var(--c-border)] flex">
          <a href="javascript:" class="flex-1 text-center py-3 text-cyan-500 font-medium no-underline" @click="dialogVisible = false">{{ t('common.confirm') }}</a>
        </div>
      </div>
    </div>
  </div>
</template>
