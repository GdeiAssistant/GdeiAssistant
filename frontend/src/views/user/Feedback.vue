<script setup>
import { ref, computed, watchEffect } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { submitFeedback } from '@/api/feedback'
import { showErrorTopTips } from '@/utils/toast.js'
import { useToast } from '@/composables/useToast'
import { createFaqList, createFeedbackTypes } from './feedbackContent'

const router = useRouter()
const { success: toastSuccess } = useToast()
const { t } = useI18n()

const activeTab = ref('faq')

const faqList = computed(() => createFaqList(t))

const openedFaqIndex = ref(0)

function toggleFaq(index) {
  openedFaqIndex.value = openedFaqIndex.value === index ? null : index
}

// 反馈表单
const feedbackType = ref('')
const feedbackTypes = computed(() => createFeedbackTypes(t))

watchEffect(() => {
  if (!feedbackTypes.value.includes(feedbackType.value)) {
    feedbackType.value = feedbackTypes.value[5] ?? feedbackTypes.value[0] ?? ''
  }
})

const content = ref('')
const contact = ref('')
const maxLength = 500

const screenshots = ref([])
const screenshotPreviews = ref([])
const maxScreenshots = 3

const contentLength = computed(() => content.value.length)

function handleSelectType(type) {
  feedbackType.value = type
}

function handleContentInput(e) {
  const target = e.target
  if (target.value.length > maxLength) {
    content.value = target.value.slice(0, maxLength)
  } else {
    content.value = target.value
  }
}

function handleScreenshotChange(e) {
  const input = e.target
  const files = input.files
  if (!files || !files.length) return

  const remain = maxScreenshots - screenshots.value.length
  const list = Array.from(files).slice(0, remain)

  list.forEach((file) => {
    if (!file.type.startsWith('image/')) return
    screenshots.value.push(file)
    screenshotPreviews.value.push(URL.createObjectURL(file))
  })

  // 重置 input，方便重复选择相同文件
  input.value = ''
}

function removeScreenshot(index) {
  screenshots.value.splice(index, 1)
  const url = screenshotPreviews.value.splice(index, 1)[0]
  if (url) URL.revokeObjectURL(url)
}

const submitting = ref(false)

async function handleSubmit() {
  const trimmedContent = content.value.trim()
  if (!trimmedContent) {
    showErrorTopTips(t('feedback.form.contentRequired'))
    return
  }

  submitting.value = true
  try {
    await submitFeedback({
      content: trimmedContent,
      contact: contact.value?.trim() || undefined,
      type: feedbackType.value || undefined
    })
    toastSuccess(t('feedback.form.submitSuccess'))
    setTimeout(() => {
      content.value = ''
      contact.value = ''
      screenshots.value = []
      screenshotPreviews.value.forEach((url) => URL.revokeObjectURL(url))
      screenshotPreviews.value = []
      router.back()
    }, 1500)
  } catch (e) {
    // 错误由 request.js 全局拦截器统一提示
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="min-h-screen bg-gray-50 pb-6">
    <!-- Sticky Header -->
    <div class="sticky top-0 z-10 flex items-center h-12 bg-white border-b border-gray-200 px-4">
      <button type="button" class="w-15 text-sm text-gray-700 text-left cursor-pointer" @click="router.back()">{{ t('common.back') }}</button>
      <h1 class="flex-1 text-center text-base font-medium text-gray-700 m-0">{{ t('feedback.title') }}</h1>
      <div class="w-15"></div>
    </div>

    <!-- Tabs -->
    <div class="flex bg-white border-b border-gray-200">
      <button
        type="button"
        class="flex-1 text-center py-3 text-[15px] cursor-pointer relative bg-transparent transition-colors"
        :class="activeTab === 'faq' ? 'text-emerald-500 font-semibold after:content-[\'\'] after:absolute after:left-1/2 after:-translate-x-1/2 after:bottom-0 after:w-2/5 after:h-[3px] after:rounded-full after:bg-emerald-500' : 'text-gray-400'"
        @click="activeTab = 'faq'"
      >{{ t('feedback.tab.faq') }}</button>
      <button
        type="button"
        class="flex-1 text-center py-3 text-[15px] cursor-pointer relative bg-transparent transition-colors"
        :class="activeTab === 'form' ? 'text-emerald-500 font-semibold after:content-[\'\'] after:absolute after:left-1/2 after:-translate-x-1/2 after:bottom-0 after:w-2/5 after:h-[3px] after:rounded-full after:bg-emerald-500' : 'text-gray-400'"
        @click="activeTab = 'form'"
      >{{ t('feedback.tab.form') }}</button>
    </div>

    <div class="max-w-lg mx-auto px-4 py-4">
      <!-- FAQ section -->
      <section v-if="activeTab === 'faq'" class="space-y-3">
        <div
          v-for="(item, index) in faqList"
          :key="item.title"
          class="bg-white rounded-xl shadow-sm overflow-hidden"
        >
          <button
            type="button"
            class="w-full px-4 py-3.5 bg-white flex items-center justify-between cursor-pointer border-none"
            @click="toggleFaq(index)"
          >
            <span class="text-[15px] text-gray-900 text-left">{{ item.title }}</span>
            <span
              class="text-sm text-gray-400 transition-transform duration-200"
              :class="{ 'rotate-180': openedFaqIndex === index }"
            >&#9662;</span>
          </button>
          <div
            v-if="openedFaqIndex === index"
            class="px-4 py-3 bg-gray-50 border-t border-gray-100"
          >
            <p class="m-0 text-sm leading-relaxed text-gray-600">{{ item.content }}</p>
          </div>
        </div>

        <p class="mt-2 text-[13px] text-gray-400 text-center">
          {{ t('feedback.faq.moreHelpPrefix') }}
          <span class="text-emerald-500 font-semibold">「{{ t('feedback.tab.form') }}」</span>
          {{ t('feedback.faq.moreHelpSuffix') }}
        </p>
      </section>

      <!-- Feedback form section -->
      <section v-else class="space-y-3">
        <!-- Type selection -->
        <div class="bg-white rounded-xl shadow-sm p-3.5">
          <h2 class="text-[15px] font-semibold text-gray-900 mb-2.5">{{ t('feedback.form.typeTitle') }}</h2>
          <div class="flex flex-wrap gap-2">
            <button
              v-for="type in feedbackTypes"
              :key="type"
              type="button"
              class="rounded-full px-3.5 py-1.5 text-[13px] border cursor-pointer transition-colors"
              :class="feedbackType === type
                ? 'border-emerald-500 bg-emerald-50 text-emerald-700 font-medium'
                : 'border-gray-200 bg-gray-50 text-gray-600'"
              @click="handleSelectType(type)"
            >{{ type }}</button>
          </div>
        </div>

        <!-- Content -->
        <div class="bg-white rounded-xl shadow-sm p-3.5">
          <h2 class="text-[15px] font-semibold text-gray-900 mb-2.5">{{ t('feedback.form.contentTitle') }}</h2>
          <div class="relative">
            <textarea
              :placeholder="t('feedback.form.contentPlaceholder')"
              :value="content"
              @input="handleContentInput"
              rows="5"
              class="w-full rounded-xl border border-gray-200 bg-gray-50 p-3 text-sm text-gray-900 resize-none min-h-[120px] box-border focus:outline-none focus:border-emerald-500 focus:bg-white"
            ></textarea>
            <div
              class="absolute right-2.5 bottom-2 text-xs"
              :class="contentLength > maxLength ? 'text-orange-500' : 'text-gray-400'"
            >{{ contentLength }}/{{ maxLength }}</div>
          </div>
        </div>

        <!-- Contact -->
        <div class="bg-white rounded-xl shadow-sm p-3.5">
          <h2 class="text-[15px] font-semibold text-gray-900 mb-2.5">{{ t('feedback.form.contactTitle') }}</h2>
          <input
            type="text"
            v-model="contact"
            :placeholder="t('feedback.form.contactPlaceholder')"
            class="w-full rounded-full border border-gray-200 py-2.5 px-3.5 text-sm text-gray-900 box-border placeholder-gray-400 focus:outline-none focus:border-emerald-500 focus:bg-white"
          />
        </div>

        <!-- Screenshots -->
        <div class="bg-white rounded-xl shadow-sm p-3.5">
          <h2 class="text-[15px] font-semibold text-gray-900 mb-2.5">
            {{ t('feedback.form.screenshotTitle') }}
            <span class="ml-1 text-xs text-gray-400 font-normal">{{ t('feedback.form.screenshotLimit', { count: maxScreenshots }) }}</span>
          </h2>
          <div class="flex gap-2.5 flex-wrap">
            <div
              v-for="(url, index) in screenshotPreviews"
              :key="url"
              class="relative w-20 h-20 rounded-lg overflow-hidden bg-gray-100"
            >
              <img :src="url" :alt="t('feedback.form.screenshotPreview')" class="w-full h-full object-cover" />
              <button
                type="button"
                class="absolute top-1 right-1 w-5 h-5 rounded-full bg-black/50 text-white text-sm leading-none border-none cursor-pointer"
                @click="removeScreenshot(index)"
              >&times;</button>
            </div>
            <label
              v-if="screenshots.length < maxScreenshots"
              class="w-20 h-20 rounded-lg border border-dashed border-gray-300 bg-gray-50 flex flex-col items-center justify-center text-gray-400 text-xs cursor-pointer"
            >
              <span class="text-[22px] leading-none mb-0.5">+</span>
              <span>{{ t('feedback.form.addImage') }}</span>
              <input
                type="file"
                accept="image/*"
                multiple
                class="hidden"
                @change="handleScreenshotChange"
              />
            </label>
          </div>
        </div>

        <!-- Submit -->
        <div class="mt-3">
          <button
            type="button"
            class="w-full h-[46px] rounded-full border-none bg-gradient-to-br from-emerald-500 to-emerald-600 text-white text-base font-semibold shadow-lg shadow-emerald-500/35 cursor-pointer transition-opacity disabled:opacity-60 disabled:cursor-not-allowed"
            :disabled="submitting"
            @click="handleSubmit"
          >{{ submitting ? t('feedback.form.submitting') : t('feedback.form.submit') }}</button>
        </div>
      </section>
    </div>
  </div>
</template>
