<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import request from '../../utils/request'
import { getNewsAttachmentTitle, getNewsSourceLabel } from './newsContent'

const route = useRoute()
const router = useRouter()
const { t } = useI18n()

const loading = ref(true)
const error = ref('')
const detail = ref(null)

const fallbackType = computed(() => Number(route.query.type || 1))
const fallbackTitle = computed(() => String(route.query.title || t('news.defaultTitle')))
const fallbackDate = computed(() => String(route.query.date || ''))

const sourceLabel = computed(() => getNewsSourceLabel(detail.value?.type || fallbackType.value, t))

const displayTitle = computed(() => detail.value?.title || fallbackTitle.value)
const displayDate = computed(() => detail.value?.publishDate || fallbackDate.value)
const displayContent = computed(() => detail.value?.content || '')
const sourceUrl = computed(() => detail.value?.sourceUrl || '')

function splitParagraphs(content) {
  return content
    .split(/\n{2,}/)
    .map((paragraph) => paragraph.trim())
    .filter(Boolean)
}

function parseAttachmentLine(line) {
  const match = line.trim().match(/^(.*?)(https?:\/\/\S+)$/)
  if (!match) {
    return null
  }
  const url = match[2]
  const title = getNewsAttachmentTitle(match[1], t)
  return { title, url }
}

const parsedContent = computed(() => {
  const content = displayContent.value.trim()
  if (!content) {
    return { paragraphs: [], attachments: [] }
  }

  const markerMatch = content.match(/^([\s\S]*?)附件链接[：:]\s*([\s\S]*)$/)
  if (!markerMatch) {
    return {
      paragraphs: splitParagraphs(content),
      attachments: [],
    }
  }

  const bodyText = markerMatch[1].trim()
  const attachmentLines = markerMatch[2]
    .split('\n')
    .map((line) => line.trim())
    .filter(Boolean)
  const attachments = attachmentLines
    .map(parseAttachmentLine)
    .filter(Boolean)

  if (!attachments.length) {
    return {
      paragraphs: splitParagraphs(content),
      attachments: [],
    }
  }

  return {
    paragraphs: splitParagraphs(bodyText),
    attachments,
  }
})

const contentParagraphs = computed(() => parsedContent.value.paragraphs)
const attachmentItems = computed(() => parsedContent.value.attachments)

async function loadDetail() {
  loading.value = true
  error.value = ''
  try {
    const res = await request.get(`/information/news/id/${route.params.id}`)
    detail.value = res?.data ?? null
    if (!detail.value) {
      error.value = t('news.detail.notFound')
    }
  } catch (e) {
    detail.value = null
    error.value = t('news.detail.loadFailed')
  } finally {
    loading.value = false
  }
}

function openSource() {
  if (sourceUrl.value) {
    window.open(sourceUrl.value, '_blank', 'noopener,noreferrer')
  }
}

onMounted(() => {
  loadDetail()
})
</script>

<template>
  <div class="min-h-screen bg-[var(--c-bg)]">
    <div class="sticky top-0 z-30 flex items-center h-[52px] px-5 bg-[var(--c-surface)]/90 backdrop-blur-xl border-b border-[var(--c-border)]">
      <button @click="router.back()" class="text-[var(--c-primary)] text-sm font-medium min-w-[48px]">&larr; {{ t('common.back') }}</button>
      <span class="flex-1 text-center text-sm font-bold">{{ sourceLabel }}</span>
      <div class="w-12"></div>
    </div>

    <div class="max-w-lg mx-auto px-4 py-6">
      <!-- Loading -->
      <div v-if="loading" class="flex flex-col items-center gap-3 py-14">
        <span class="inline-block w-5 h-5 border-2 border-[var(--c-primary)] border-t-transparent rounded-full animate-spin"></span>
        <span class="text-sm text-[var(--c-text-3)]">{{ t('news.detail.loading') }}</span>
      </div>

      <!-- Content card -->
      <div v-else class="bg-[var(--c-surface)] rounded-2xl p-5 shadow-sm border border-[var(--c-border)]">
        <p v-if="error" class="text-sm text-red-500 mb-3">{{ error }}</p>

        <p class="text-xs font-semibold text-[var(--c-primary)] mb-2.5">{{ sourceLabel }}</p>
        <h2 class="text-2xl font-bold text-[var(--c-text)] leading-snug">{{ displayTitle }}</h2>
        <p class="mt-3.5 text-xs text-[var(--c-text-3)]">{{ displayDate || t('news.detail.noDate') }}</p>

        <a
          v-if="sourceUrl"
          href="javascript:;"
          class="inline-block mt-3.5 text-sm font-semibold text-[var(--c-primary)] no-underline"
          @click="openSource"
        >{{ t('news.detail.openSource') }}</a>

        <div v-if="contentParagraphs.length" class="mt-5 text-[15px] leading-[1.9] text-[var(--c-text)]">
          <p
            v-for="(paragraph, index) in contentParagraphs"
            :key="`paragraph-${index}`"
            class="whitespace-pre-wrap"
            :class="{ 'mt-4': index > 0 }"
          >{{ paragraph }}</p>
        </div>

        <div v-if="attachmentItems.length" class="mt-5 p-4 rounded-xl bg-[var(--c-primary)]/5">
          <p class="text-xs font-semibold text-[var(--c-primary)] mb-2.5">{{ t('news.detail.attachments') }}</p>
          <a
            v-for="(item, index) in attachmentItems"
            :key="`attachment-${index}`"
            :href="item.url"
            class="block text-[var(--c-primary)] no-underline leading-relaxed break-all"
            :class="{ 'mt-2': index > 0 }"
            target="_blank"
            rel="noopener noreferrer"
          >{{ item.title }}</a>
        </div>

        <p
          v-if="!contentParagraphs.length && !attachmentItems.length"
          class="mt-5 text-sm text-[var(--c-text-3)]"
        >{{ t('news.detail.empty') }}</p>
      </div>
    </div>
  </div>
</template>
