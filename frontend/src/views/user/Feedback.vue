<script setup>
import { ChevronLeft } from 'lucide-vue-next'
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
  <div class="subpage feedback-page min-h-screen pb-6">
    <!-- Sticky Header -->
    <div class="subpage-bar feedback-header">
      <button type="button" class="subpage-bar__back feedback-back-btn" @click="router.back()">
        <ChevronLeft :size="18" aria-hidden="true" />
        <span>{{ t('common.back') }}</span>
      </button>
      <h1 class="subpage-bar__title feedback-header-title">{{ t('feedback.title') }}</h1>
      <span aria-hidden="true"></span>
    </div>

    <!-- Tabs -->
    <div class="feedback-tabs flex">
      <button
        type="button"
        class="feedback-tab flex-1 text-center py-3 text-[15px] cursor-pointer relative bg-transparent transition-colors"
        :class="{ 'feedback-tab--active': activeTab === 'faq' }"
        @click="activeTab = 'faq'"
      >{{ t('feedback.tab.faq') }}</button>
      <button
        type="button"
        class="feedback-tab flex-1 text-center py-3 text-[15px] cursor-pointer relative bg-transparent transition-colors"
        :class="{ 'feedback-tab--active': activeTab === 'form' }"
        @click="activeTab = 'form'"
      >{{ t('feedback.tab.form') }}</button>
    </div>

    <div class="max-w-lg mx-auto px-4 py-4">
      <!-- FAQ section -->
      <section v-if="activeTab === 'faq'" class="space-y-3">
        <div
          v-for="(item, index) in faqList"
          :key="item.title"
          class="feedback-card rounded-xl shadow-sm overflow-hidden"
        >
          <button
            type="button"
            class="feedback-card__button w-full px-4 py-3.5 flex items-center justify-between cursor-pointer border-none"
            @click="toggleFaq(index)"
          >
            <span class="feedback-card__title text-[15px] text-left">{{ item.title }}</span>
            <span
              class="feedback-card__chevron text-sm transition-transform duration-200"
              :class="{ 'rotate-180': openedFaqIndex === index }"
            >&#9662;</span>
          </button>
          <div
            v-if="openedFaqIndex === index"
            class="feedback-card__content px-4 py-3 border-t"
          >
            <p class="feedback-card__copy m-0 text-sm leading-relaxed">{{ item.content }}</p>
          </div>
        </div>

        <p class="feedback-footnote mt-2 text-[13px] text-center">
          {{ t('feedback.faq.moreHelpPrefix') }}
          <span class="feedback-footnote__accent font-semibold">「{{ t('feedback.tab.form') }}」</span>
          {{ t('feedback.faq.moreHelpSuffix') }}
        </p>
      </section>

      <!-- Feedback form section -->
      <section v-else class="space-y-3">
        <!-- Type selection -->
        <div class="feedback-card rounded-xl shadow-sm p-3.5">
          <h2 class="feedback-section-title text-[15px] font-semibold mb-2.5">{{ t('feedback.form.typeTitle') }}</h2>
          <div class="flex flex-wrap gap-2">
            <button
              v-for="type in feedbackTypes"
              :key="type"
              type="button"
              class="feedback-type-chip rounded-full px-3.5 py-1.5 text-[13px] border cursor-pointer transition-colors"
              :class="{ 'feedback-type-chip--active': feedbackType === type }"
              @click="handleSelectType(type)"
            >{{ type }}</button>
          </div>
        </div>

        <!-- Content -->
        <div class="feedback-card rounded-xl shadow-sm p-3.5">
          <h2 class="feedback-section-title text-[15px] font-semibold mb-2.5">{{ t('feedback.form.contentTitle') }}</h2>
          <div class="relative">
            <textarea
              :placeholder="t('feedback.form.contentPlaceholder')"
              :value="content"
              @input="handleContentInput"
              rows="5"
              class="ui-control feedback-input w-full rounded-xl border p-3 text-sm resize-none min-h-[120px] box-border focus:outline-none"
            ></textarea>
            <div
              class="feedback-counter absolute right-2.5 bottom-2 text-xs"
              :class="{ 'feedback-counter--warning': contentLength > maxLength }"
            >{{ contentLength }}/{{ maxLength }}</div>
          </div>
        </div>

        <!-- Contact -->
        <div class="feedback-card rounded-xl shadow-sm p-3.5">
          <h2 class="feedback-section-title text-[15px] font-semibold mb-2.5">{{ t('feedback.form.contactTitle') }}</h2>
          <input
            type="text"
            v-model="contact"
            :placeholder="t('feedback.form.contactPlaceholder')"
            class="ui-control feedback-input w-full rounded-full border py-2.5 px-3.5 text-sm box-border focus:outline-none"
          />
        </div>

        <!-- Screenshots -->
        <div class="feedback-card rounded-xl shadow-sm p-3.5">
          <h2 class="feedback-section-title text-[15px] font-semibold mb-2.5">
            {{ t('feedback.form.screenshotTitle') }}
            <span class="feedback-section-note ml-1 text-xs font-normal">{{ t('feedback.form.screenshotLimit', { count: maxScreenshots }) }}</span>
          </h2>
          <div class="flex gap-2.5 flex-wrap">
            <div
              v-for="(url, index) in screenshotPreviews"
              :key="url"
              class="feedback-preview relative w-20 h-20 rounded-lg overflow-hidden"
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
              class="feedback-upload w-20 h-20 rounded-lg border border-dashed flex flex-col items-center justify-center text-xs cursor-pointer"
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
            class="feedback-submit w-full h-[46px] rounded-full border-none text-white text-base font-semibold cursor-pointer transition-opacity disabled:opacity-60 disabled:cursor-not-allowed"
            :disabled="submitting"
            @click="handleSubmit"
          >{{ submitting ? t('feedback.form.submitting') : t('feedback.form.submit') }}</button>
        </div>
      </section>
    </div>
  </div>
</template>

<style scoped>
.feedback-page {
  background: var(--c-bg);
}

.feedback-header,
.feedback-tabs {
  background: color-mix(in srgb, var(--c-surface) 94%, var(--c-bg));
  border-bottom: 1px solid var(--c-border-light);
}

.feedback-back-btn,
.feedback-header-title,
.feedback-section-title,
.feedback-card__title {
  color: var(--c-text-1);
}

.feedback-tab {
  color: var(--c-text-3);
}

.feedback-tab--active {
  color: var(--c-primary);
  font-weight: 650;
}

.feedback-tab--active::after {
  content: '';
  position: absolute;
  left: 50%;
  bottom: 0;
  width: 40%;
  height: 3px;
  border-radius: 999px;
  transform: translateX(-50%);
  background: var(--c-primary);
}

.feedback-card {
  background: var(--c-surface);
  border: 1px solid var(--c-border);
  box-shadow: none;
}

.feedback-card__button {
  background: transparent;
}

.feedback-card__chevron,
.feedback-footnote,
.feedback-section-note,
.feedback-counter {
  color: var(--c-text-3);
}

.feedback-card__content,
.feedback-upload {
  background: color-mix(in srgb, var(--c-bg) 78%, var(--c-surface));
  border-color: var(--c-border-light);
}

.feedback-card__copy {
  color: var(--c-text-2);
}

.feedback-footnote__accent,
.feedback-type-chip--active {
  color: var(--c-primary);
}

.feedback-type-chip {
  border-color: var(--c-border-light);
  background: color-mix(in srgb, var(--c-bg) 78%, var(--c-surface));
  color: var(--c-text-2);
}

.feedback-type-chip--active {
  border-color: color-mix(in srgb, var(--c-primary) 30%, var(--c-border));
  background: color-mix(in srgb, var(--c-primary) 12%, var(--c-surface));
  font-weight: 600;
}

.feedback-input {
  border-color: var(--c-border-light);
  background: color-mix(in srgb, var(--c-bg) 70%, var(--c-surface));
  color: var(--c-text-1);
}

.feedback-input::placeholder {
  color: var(--c-text-3);
}

.feedback-input:focus {
  border-color: color-mix(in srgb, var(--c-primary) 30%, var(--c-border));
  background: var(--c-surface);
  box-shadow: 0 0 0 3px color-mix(in srgb, var(--c-primary) 12%, transparent);
}

.feedback-counter--warning {
  color: var(--c-warning);
}

.feedback-preview {
  background: color-mix(in srgb, var(--c-bg) 82%, var(--c-surface));
}

.feedback-upload {
  color: var(--c-text-3);
}

.feedback-submit {
  background: var(--c-primary);
  box-shadow: none;
}
</style>
