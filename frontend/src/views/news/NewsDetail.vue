<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '../../utils/request'

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const error = ref('')
const detail = ref(null)

const fallbackType = computed(() => Number(route.query.type || 1))
const fallbackTitle = computed(() => String(route.query.title || '新闻通知'))
const fallbackDate = computed(() => String(route.query.date || ''))

const sourceLabel = computed(() => {
  switch (detail.value?.type || fallbackType.value) {
    case 1:
      return '学校要闻'
    case 2:
      return '院部通知'
    case 3:
      return '通知公告'
    case 4:
      return '学术动态'
    default:
      return '新闻通知'
  }
})

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
  const title = match[1].trim().replace(/[：:]+$/, '') || '打开附件'
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
      error.value = '未找到对应的新闻通知'
    }
  } catch (e) {
    detail.value = null
    error.value = '新闻详情加载失败，请稍后重试'
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
  <div class="news-detail-page">
    <div class="news-detail-header">
      <a href="javascript:;" class="news-detail-back" @click="router.back()">返回</a>
      <h1 class="news-detail-nav-title">{{ sourceLabel }}</h1>
      <span class="news-detail-spacer"></span>
    </div>

    <div class="news-detail-body">
      <div v-if="loading" class="news-detail-loading">
        <div class="weui-loading"></div>
        <p>正在加载新闻详情</p>
      </div>

      <div v-else class="news-detail-card">
        <p v-if="error" class="news-detail-error">{{ error }}</p>
        <p class="news-detail-source">{{ sourceLabel }}</p>
        <h2 class="news-detail-title">{{ displayTitle }}</h2>
        <p class="news-detail-date">{{ displayDate || '—' }}</p>
        <a
          v-if="sourceUrl"
          href="javascript:;"
          class="news-detail-link"
          @click="openSource"
        >
          打开原文链接
        </a>
        <div v-if="contentParagraphs.length" class="news-detail-content">
          <p
            v-for="(paragraph, index) in contentParagraphs"
            :key="`paragraph-${index}`"
            class="news-detail-paragraph"
          >
            {{ paragraph }}
          </p>
        </div>
        <div v-if="attachmentItems.length" class="news-detail-attachments">
          <p class="news-detail-attachment-title">附件链接</p>
          <a
            v-for="(item, index) in attachmentItems"
            :key="`attachment-${index}`"
            :href="item.url"
            class="news-detail-attachment-link"
            target="_blank"
            rel="noopener noreferrer"
          >
            {{ item.title }}
          </a>
        </div>
        <p
          v-if="!contentParagraphs.length && !attachmentItems.length"
          class="news-detail-empty"
        >
          暂无详细内容
        </p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.news-detail-page {
  min-height: 100vh;
  background: #f8f8f8;
}

.news-detail-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  background: #fff;
  border-bottom: 1px solid #ececec;
}

.news-detail-back,
.news-detail-spacer {
  min-width: 48px;
}

.news-detail-back {
  color: #666;
  text-decoration: none;
  font-size: 14px;
}

.news-detail-nav-title {
  margin: 0;
  font-size: 17px;
  font-weight: 600;
  color: #222;
}

.news-detail-body {
  padding: 16px;
}

.news-detail-card {
  background: #fff;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.06);
}

.news-detail-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 56px 0;
  color: #666;
}

.news-detail-error {
  margin: 0 0 12px;
  color: #c53f3f;
  font-size: 14px;
}

.news-detail-source {
  margin: 0 0 10px;
  color: #0f8f5f;
  font-size: 13px;
  font-weight: 600;
}

.news-detail-title {
  margin: 0;
  color: #222;
  font-size: 24px;
  line-height: 1.5;
}

.news-detail-date {
  margin: 14px 0 0;
  color: #888;
  font-size: 13px;
}

.news-detail-link {
  display: inline-block;
  margin-top: 14px;
  color: #0f8f5f;
  text-decoration: none;
  font-size: 14px;
  font-weight: 600;
}

.news-detail-content,
.news-detail-empty {
  margin: 20px 0 0;
  color: #333;
  font-size: 15px;
  line-height: 1.9;
}

.news-detail-empty {
  color: #999;
}

.news-detail-paragraph {
  margin: 0;
  white-space: pre-wrap;
}

.news-detail-paragraph + .news-detail-paragraph {
  margin-top: 16px;
}

.news-detail-attachments {
  margin-top: 20px;
  padding: 16px;
  border-radius: 14px;
  background: #f4fbf8;
}

.news-detail-attachment-title {
  margin: 0 0 10px;
  color: #0f8f5f;
  font-size: 13px;
  font-weight: 600;
}

.news-detail-attachment-link {
  display: block;
  color: #0f8f5f;
  text-decoration: none;
  line-height: 1.7;
  word-break: break-all;
}

.news-detail-attachment-link + .news-detail-attachment-link {
  margin-top: 8px;
}
</style>
