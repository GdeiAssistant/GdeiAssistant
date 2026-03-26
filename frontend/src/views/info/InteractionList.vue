<template>
  <div class="space-y-3">
    <div v-if="loading" class="flex justify-center py-12">
      <div class="w-6 h-6 border-2 border-[var(--c-primary)] border-t-transparent rounded-full animate-spin"></div>
    </div>

    <template v-else>
      <div v-if="!items.length" class="py-16 text-center text-sm text-[var(--c-text-3)]">{{ t('info.noInteraction') }}</div>

      <button
        v-for="item in items"
        :key="item.id || `${item.module}-${item.targetId}`"
        type="button"
        class="w-full bg-[var(--c-surface)] border border-[var(--c-border)] rounded-[14px] px-4 py-3.5 text-left hover:bg-[var(--c-surface-hover)] transition-colors"
        @click="handleSelect(item)"
      >
        <div class="flex items-start justify-between gap-3">
          <span class="text-[14px] font-semibold text-[var(--c-text-1)] flex-1 leading-snug">{{ item.title }}</span>
          <span class="text-[11px] text-[var(--c-text-3)] shrink-0 mt-0.5">{{ item.createdAt }}</span>
        </div>
        <p class="mt-1 text-[13px] text-[var(--c-text-2)] line-clamp-2">{{ item.content }}</p>
        <div class="mt-2 flex items-center justify-between">
          <span class="text-[11px] text-[var(--c-text-3)]">{{ getModuleLabel(item.module) }}</span>
          <span
            :class="['text-[11px] font-semibold flex items-center gap-1', item.isRead ? 'text-[var(--c-text-3)]' : 'text-[var(--c-primary)]']"
          >
            <span v-if="!item.isRead" class="w-1.5 h-1.5 rounded-full bg-[var(--c-primary)]"></span>
            {{ item.isRead ? t('info.read') : t('info.unread') }}
          </span>
        </div>
      </button>

      <div v-if="hasMore || unreadCount > 0" class="flex gap-2">
        <button
          v-if="unreadCount > 0"
          type="button"
          class="flex-1 py-3 text-sm text-[var(--c-text-2)] bg-[var(--c-surface)] border border-[var(--c-border)] rounded-[14px] hover:bg-[var(--c-surface-hover)] transition-colors"
          @click="markAllRead"
        >{{ t('info.markAllRead') }}</button>
        <button
          v-if="hasMore"
          type="button"
          class="flex-1 py-3 text-sm text-[var(--c-text-2)] bg-[var(--c-surface)] border border-[var(--c-border)] rounded-[14px] hover:bg-[var(--c-surface-hover)] transition-colors"
          :disabled="loadingMore"
          @click="loadMore"
        >
          {{ getInteractionLoadMoreLabel(loadingMore, t) }}
        </button>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import request from '@/utils/request'
import {
  getInfoModuleLabel,
  getInteractionLoadMoreLabel,
  normalizeInfoModule
} from './infoContent'

const router = useRouter()
const { t } = useI18n()

const items = ref([])
const unreadCount = ref(0)
const loading = ref(true)
const loadingMore = ref(false)
const hasMore = ref(false)
const PAGE_SIZE = 20

function normalize(raw) {
  if (!Array.isArray(raw)) return []
  return raw.map(item => ({
    id: item?.id ?? null,
    module: normalizeInfoModule(item?.module),
    type: item?.type ?? null,
    title: item?.title || t('info.defaultInteractionTitle'),
    content: item?.content || t('info.defaultInteractionContent'),
    createdAt: item?.createdAt || t('common.recentUpdate'),
    isRead: item?.isRead === true,
    targetId: item?.targetId ?? null,
    targetSubId: item?.targetSubId ?? null,
    targetType: item?.targetType ?? null
  }))
}

function getModuleLabel(module) {
  return getInfoModuleLabel(module, t)
}

async function loadPage(start) {
  const res = await request.get(`/information/message/interaction/start/${start}/size/${PAGE_SIZE}`)
  return normalize(res?.data || [])
}

onMounted(async () => {
  try {
    const [pageRes, unreadRes] = await Promise.allSettled([
      loadPage(0),
      request.get('/information/message/unread')
    ])
    if (pageRes.status === 'fulfilled') {
      items.value = pageRes.value
      hasMore.value = pageRes.value.length >= PAGE_SIZE
    }
    if (unreadRes.status === 'fulfilled') {
      unreadCount.value = Number(unreadRes.value?.data || 0)
    }
  } finally {
    loading.value = false
  }
})

async function loadMore() {
  loadingMore.value = true
  try {
    const newItems = await loadPage(items.value.length)
    items.value = [...items.value, ...newItems]
    hasMore.value = newItems.length >= PAGE_SIZE
  } finally {
    loadingMore.value = false
  }
}

async function markAllRead() {
  unreadCount.value = 0
  items.value = items.value.map(i => ({ ...i, isRead: true }))
  request.post('/information/message/readall').catch(() => {})
}

function handleSelect(item) {
  if (!item.isRead) {
    item.isRead = true
    unreadCount.value = Math.max(0, unreadCount.value - 1)
    request.post(`/information/message/id/${item.id}/read`).catch(() => {})
  }
  // Navigate based on module (same logic as Info.vue)
  const { module, targetId } = item
  const paths = {
    marketplace: targetId ? `/marketplace/detail/${targetId}` : '/ershou',
    lostandfound: targetId ? `/lostandfound/detail/${targetId}` : '/lostandfound',
    secret: targetId ? `/secret/detail/${targetId}` : '/secret',
    express: targetId ? `/express/detail/${targetId}` : '/express',
    topic: targetId ? `/topic/detail/${targetId}` : '/topic',
    photograph: targetId ? `/photograph/detail/${targetId}` : '/photograph',
    dating: '/dating/home',
    delivery: targetId ? `/delivery/detail/${targetId}` : '/delivery'
  }
  const path = paths[module]
  if (path) router.push(path)
}
</script>
