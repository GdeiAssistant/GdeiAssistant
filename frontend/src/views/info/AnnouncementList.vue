<template>
  <div class="space-y-3">
    <div v-if="loading" class="flex justify-center py-12">
      <div class="w-6 h-6 border-2 border-[var(--c-primary)] border-t-transparent rounded-full animate-spin"></div>
    </div>

    <template v-else>
      <div
        v-for="item in list"
        :key="item.id"
        class="bg-[var(--c-surface)] border border-[var(--c-border)] rounded-[14px] px-4 py-3.5 cursor-pointer hover:bg-[var(--c-surface-hover)] transition-colors"
        @click="router.push(`/info/announcements/${item.id}`)"
      >
        <div class="flex items-start justify-between gap-3">
          <span class="text-[15px] font-semibold text-[var(--c-text-1)] flex-1">{{ item.title }}</span>
          <span class="text-[12px] text-[var(--c-text-3)] shrink-0 mt-0.5">{{ item.publishTime }}</span>
        </div>
        <p class="mt-1.5 text-[13px] text-[var(--c-text-2)] line-clamp-2">{{ item.content }}</p>
      </div>

      <div v-if="!list.length" class="py-16 text-center text-sm text-[var(--c-text-3)]">暂无通知公告</div>

      <button
        v-if="hasMore"
        type="button"
        class="w-full py-3 text-sm text-[var(--c-text-2)] bg-[var(--c-surface)] border border-[var(--c-border)] rounded-[14px] hover:bg-[var(--c-surface-hover)] transition-colors"
        :disabled="loadingMore"
        @click="loadMore"
      >
        {{ loadingMore ? '加载中...' : '加载更多' }}
      </button>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request'

const router = useRouter()
const list = ref([])
const loading = ref(true)
const loadingMore = ref(false)
const hasMore = ref(false)
const PAGE_SIZE = 10

async function loadPage(start) {
  const res = await request.get(`/information/announcement/start/${start}/size/${PAGE_SIZE}`)
  if (res?.success && Array.isArray(res.data)) {
    return res.data
  }
  return []
}

onMounted(async () => {
  try {
    const items = await loadPage(0)
    list.value = items
    hasMore.value = items.length >= PAGE_SIZE
  } finally {
    loading.value = false
  }
})

async function loadMore() {
  loadingMore.value = true
  try {
    const items = await loadPage(list.value.length)
    list.value = [...list.value, ...items]
    hasMore.value = items.length >= PAGE_SIZE
  } finally {
    loadingMore.value = false
  }
}
</script>
