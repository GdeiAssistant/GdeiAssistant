<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import request from '../../utils/request'

const router = useRouter()
const { t } = useI18n()

const tabs = [
  { type: 1, labelKey: 'news.tab.school', icon: '/img/news/school.png' },
  { type: 2, labelKey: 'news.tab.department', icon: '/img/news/admin.png' },
  { type: 3, labelKey: 'news.tab.notice', icon: '/img/news/course.png' },
  { type: 4, labelKey: 'news.tab.academic', icon: '/img/news/study.png' }
]

const PAGE_SIZE = 15
const activeType = ref(1)
const newsList = ref([])
const page = ref(1)
const isLoading = ref(false)
const finished = ref(false)
const loadError = ref(false)

function goBack() {
  router.back()
}

function openDetail(item) {
  if (!item?.id) return
  router.push({
    name: 'NewsDetail',
    params: { id: item.id },
    query: {
      type: String(activeType.value),
      title: item.title || '',
      date: item.date || ''
    }
  })
}

function loadNews() {
  if (isLoading.value || finished.value) return
  isLoading.value = true
  loadError.value = false
  const start = (page.value - 1) * PAGE_SIZE
  const type = activeType.value
  request
    .get(`/information/news/type/${type}/start/${start}/size/${PAGE_SIZE}`)
    .then((res) => {
      const list = res?.data ?? []
      const mapped = list.map((item) => ({
        id: item.id,
        title: item.title,
        date: item.publishDate || ''
      }))
      newsList.value.push(...mapped)
      if (mapped.length < PAGE_SIZE) {
        finished.value = true
      }
    })
    .catch(() => {
      loadError.value = true
      if (page.value === 1) finished.value = true
    })
    .finally(() => {
      isLoading.value = false
    })
}

function switchTab(type) {
  if (type === activeType.value) return
  activeType.value = type
  newsList.value = []
  page.value = 1
  finished.value = false
  loadError.value = false
  loadNews()
}

function handleScroll(e) {
  const { scrollTop, clientHeight, scrollHeight } = e.target
  const distanceToBottom = scrollHeight - Math.ceil(scrollTop) - clientHeight
  const isBottom = distanceToBottom <= 100

  if (isBottom && !isLoading.value && !finished.value) {
    page.value++
    loadNews()
  }
}

onMounted(() => {
  loadNews()
})
</script>

<template>
  <div class="h-screen w-screen overflow-hidden flex flex-col bg-[var(--c-bg)]">
    <!-- Sticky header -->
    <div class="shrink-0 sticky top-0 z-30 flex items-center h-[52px] px-5 bg-[var(--c-surface)]/90 backdrop-blur-xl border-b border-[var(--c-border)]">
      <button @click="goBack" class="text-[var(--c-primary)] text-sm font-medium">&larr; {{ t('common.back') }}</button>
      <span class="flex-1 text-center text-sm font-bold">{{ t('info.news') }}</span>
      <div class="w-10"></div>
    </div>

    <!-- Scrollable content area -->
    <div
      class="flex-1 overflow-y-auto"
      style="-webkit-overflow-scrolling: touch;"
      @scroll="handleScroll"
    >
      <div class="max-w-lg mx-auto px-4 py-4">
        <div class="bg-[var(--c-surface)] rounded-xl border border-[var(--c-border)] divide-y divide-[var(--c-border)]">
          <a
            v-for="item in newsList"
            :key="item.id"
            href="javascript:;"
            class="flex items-center justify-between px-4 py-3.5 hover:bg-[var(--c-surface-hover)] transition-colors no-underline"
            @click="openDetail(item)"
          >
            <span class="text-sm text-[var(--c-text)] break-words mr-3">{{ item.title }}</span>
            <span class="shrink-0 text-xs text-[var(--c-text-3)]">{{ item.date }}</span>
          </a>
        </div>

        <!-- Status indicators -->
        <div v-if="loadError && newsList.length === 0" class="text-center py-6 text-sm text-[var(--c-text-3)]">
          {{ t('news.loadFailed') }}
        </div>
        <div v-else-if="isLoading" class="flex items-center justify-center gap-2 py-6 text-sm text-[var(--c-text-2)]">
          <span class="inline-block w-4 h-4 border-2 border-[var(--c-primary)] border-t-transparent rounded-full animate-spin"></span>
          {{ t('common.loading') }}
        </div>
        <div v-else-if="finished && newsList.length > 0" class="text-center py-6 text-sm text-[var(--c-text-3)]">
          {{ t('news.noMore') }}
        </div>
        <div v-else-if="!isLoading && finished && newsList.length === 0 && !loadError" class="text-center py-6 text-sm text-[var(--c-text-3)]">
          {{ t('news.empty') }}
        </div>
      </div>
    </div>

    <!-- Bottom tab bar -->
    <div class="shrink-0 flex border-t border-[var(--c-border)] bg-[var(--c-surface)]">
      <button
        v-for="tab in tabs"
        :key="tab.type"
        type="button"
        class="flex-1 flex flex-col items-center py-2 transition-colors"
        :class="activeType === tab.type ? 'text-[var(--c-primary)]' : 'text-[var(--c-text-3)]'"
        @click="switchTab(tab.type)"
      >
        <img
          :src="tab.icon"
          alt=""
          class="w-[27px] h-[27px] mb-0.5"
          :class="{ 'brightness-0 saturate-100 invert-[48%] sepia-[79%] saturate-[2476%] hue-rotate-[86deg]': activeType === tab.type }"
        />
        <span class="text-xs">{{ t(tab.labelKey) }}</span>
      </button>
    </div>
  </div>
</template>
