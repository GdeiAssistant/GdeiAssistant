<template>
  <div class="space-y-3">
    <div v-if="loading" class="flex justify-center py-12">
      <div class="w-6 h-6 border-2 border-[var(--c-primary)] border-t-transparent rounded-full animate-spin"></div>
    </div>

    <div v-else-if="loadError" role="alert" class="p-4">{{ t('common.networkError') }}<button type="button" @click="reload">{{ t('common.retry') }}</button></div>
    <template v-else>
      <div v-if="!items.length" class="py-16 text-center text-sm text-[var(--c-text-3)]">{{ t('info.noInteraction') }}</div>

      <button
        v-for="item in items"
        :key="item.id || `${item.module}-${item.targetId}`"
        type="button"
        class="ui-panel w-full bg-[var(--c-surface)] border border-[var(--c-border)] rounded-[14px] px-4 py-3.5 text-left hover:bg-[var(--c-surface-hover)] transition-colors"
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
          class="ui-panel flex-1 py-3 text-sm text-[var(--c-text-2)] bg-[var(--c-surface)] border border-[var(--c-border)] rounded-[14px] hover:bg-[var(--c-surface-hover)] transition-colors"
          @click="markAllRead"
        >{{ t('info.markAllRead') }}</button>
        <button
          v-if="hasMore"
          type="button"
          class="ui-panel flex-1 py-3 text-sm text-[var(--c-text-2)] bg-[var(--c-surface)] border border-[var(--c-border)] rounded-[14px] hover:bg-[var(--c-surface-hover)] transition-colors"
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
import { postInformationMessageByIdRead } from "../../api/informationEndpoints.js"

import request from '@/utils/request'
import { refreshMessageUnread } from '@/composables/useMessageUnread'
import { ref, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'

import {
  getInfoModuleLabel,
  getInteractionLoadMoreLabel,
  normalizeInfoModule
} from './infoContent'

const router = useRouter()
const route = useRoute()
const category = computed(() => route.path === '/info/services' ? 'service' : 'community')
const loadError = ref(false)
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
  const res = await request.get(`/information/message/${category.value}/start/${start}/size/${PAGE_SIZE}`)
  if (!res?.success) throw new Error()
  return normalize(res.data || [])
}

let loadEpoch = 0
async function reload() {
  const epoch = ++loadEpoch
  loading.value = true
  loadError.value = false
  try {
    const page = await loadPage(0)
    if (epoch !== loadEpoch) return
    items.value = page
    hasMore.value = page.length >= PAGE_SIZE
    unreadCount.value = page.filter(item => !item.isRead).length
  } catch (_) { if (epoch === loadEpoch) loadError.value = true }
  finally { if (epoch === loadEpoch) loading.value = false }
}
watch(category, reload, { immediate: true })

async function loadMore() {
  loadingMore.value = true
  try {
    const newItems = await loadPage(items.value.length)
    items.value = [...items.value, ...newItems]
    unreadCount.value = items.value.filter(item => !item.isRead).length
    hasMore.value = newItems.length >= PAGE_SIZE
  } catch (_) {
    loadError.value = true
  } finally {
    loadingMore.value = false
  }
}

async function markAllRead() {
  try {
    const response = await request.post(`/information/message/${category.value}/readall`)
    if (!response?.success) throw new Error()
    unreadCount.value = 0
    items.value = items.value.map(item => ({ ...item, isRead: true }))
    await refreshMessageUnread()
  } catch (_) { loadError.value = true }
}

async function handleSelect(item) {
  // Navigate based on module (same logic as Info.vue)
  const { module, targetId } = item
  const paths = {
    marketplace: targetId ? `/marketplace/detail/${targetId}` : '/marketplace/home',
    lostandfound: targetId ? `/lostandfound/detail/${targetId}` : '/lostandfound/home',
    secret: targetId ? `/secret/detail/${targetId}` : '/secret/home',
    express: targetId ? `/express/detail/${targetId}` : '/express/home',
    topic: targetId ? `/topic/detail/${targetId}` : '/topic/home',
    photograph: targetId ? `/photograph/detail/${targetId}` : '/photograph/home',
    dating: '/dating/center',
    delivery: targetId ? `/delivery/detail/${targetId}` : '/delivery/home'
  }
  const path = paths[module]
  if (!path) { loadError.value = true; return }
  if (!item.isRead) {
    try {
      const response = await postInformationMessageByIdRead(item.id)
      if (!response?.success) throw new Error()
      item.isRead = true
      unreadCount.value = Math.max(0, unreadCount.value - 1)
      await refreshMessageUnread()
    } catch (_) { loadError.value = true; return }
  }
  router.push({ path, query: { targetId: targetId || '', tab: module === 'dating' ? (item.targetType === 'received' ? 'received' : 'sent') : '', targetType: item.targetType || '', targetSubId: item.targetSubId || '', notificationId: item.id || '' } })
}
</script>
