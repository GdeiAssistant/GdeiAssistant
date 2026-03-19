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

async function loadDetail() {
  loading.value = true
  error.value = ''
  try {
    const res = await request.get(`/news/id/${route.params.id}`)
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
        <p v-if="displayContent" class="news-detail-content">{{ displayContent }}</p>
        <p v-else class="news-detail-empty">暂无详细内容</p>
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
  white-space: pre-wrap;
}

.news-detail-empty {
  color: #999;
}
</style>
