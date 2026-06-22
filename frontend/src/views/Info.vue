<template>
  <div class="flex flex-col gap-5">
    <!-- News section -->
    <AppCard>
      <template #header>
        <span class="text-sm font-semibold">{{ $t('info.news') }}</span>
      </template>
      <div class="p-4">
        <button
          type="button"
          class="w-full flex items-center gap-3 text-left cursor-pointer bg-transparent border-none"
          @click="router.push('/news')"
        >
          <div class="info-news-badge w-[42px] h-[42px] rounded-xl flex items-center justify-center text-[13px] font-bold shrink-0">
            {{ $t('info.newsBadge') }}
          </div>
          <div class="flex-1 min-w-0">
            <div class="text-[15px] font-semibold text-[var(--c-text-1)]">{{ $t('info.newsEntryTitle') }}</div>
            <div class="mt-1 text-[13px] text-[var(--c-text-2)]">{{ $t('info.newsDesc') }}</div>
          </div>
          <div class="info-news-chevron w-2 h-2 rotate-45 shrink-0" />
        </button>
      </div>
    </AppCard>

    <!-- System notices section -->
    <AppCard>
      <template #header>
        <span class="text-sm font-semibold">{{ $t('info.systemNotice') }}</span>
        <button
          type="button"
          class="text-xs text-[var(--c-primary)] bg-transparent border-none cursor-pointer"
          @click="router.push('/info/announcements')"
        >查看更多</button>
      </template>
      <div class="p-4">
        <NoticeBlock :notices="systemNoticeItems" />
      </div>
    </AppCard>

    <!-- Interactions section -->
    <AppCard>
      <template #header>
        <div class="flex items-center gap-2">
          <span class="text-sm font-semibold">{{ $t('info.interaction') }}</span>
          <span
            v-if="interactionUnreadCount > 0"
            class="info-interaction-badge min-w-[18px] h-[18px] px-1 rounded-full text-white text-[10px] font-bold flex items-center justify-center"
          >{{ interactionUnreadCount > 99 ? '99+' : interactionUnreadCount }}</span>
        </div>
        <button
          type="button"
          class="text-xs text-[var(--c-primary)] bg-transparent border-none cursor-pointer"
          @click="router.push('/info/interactions')"
        >查看更多</button>
      </template>
      <div class="p-4">
        <InteractionBlock
          :items="interactionItems"
          :unread-count="interactionUnreadCount"
          @select-item="handleInteractionSelect"
          @mark-all="handleMarkAllInteractionsRead"
        />
      </div>
    </AppCard>
  </div>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import request from '../utils/request'
import AppCard from '../components/ui/AppCard.vue'
import NoticeBlock from '../components/info/NoticeBlock.vue'
import InteractionBlock from '../components/info/InteractionBlock.vue'

const router = useRouter()
const { t } = useI18n()
const infoData = ref({})
const announcementList = ref([])
const interactionItems = ref([])
const interactionUnreadCount = ref(0)
const interactionHasMore = ref(false)
const interactionLoadingMore = ref(false)
const INTERACTION_PAGE_SIZE = 20

const systemNoticeItems = computed(() => {
  if (Array.isArray(announcementList.value) && announcementList.value.length > 0) {
    return announcementList.value
  }
  if (Array.isArray(infoData.value?.notices) && infoData.value.notices.length > 0) {
    return infoData.value.notices
  }
  if (infoData.value?.notice) {
    return [infoData.value.notice]
  }
  return []
})

function normalizeInteractionItems(rawList) {
  if (!Array.isArray(rawList)) {
    return []
  }
  return rawList.map((item) => ({
    id: item?.id ?? null,
    module: normalizeInteractionModule(item?.module),
    type: item?.type ?? null,
    title: item?.title ?? t('info.defaultInteractionTitle'),
    content: item?.content ?? t('info.defaultInteractionContent'),
    createdAt: item?.createdAt ?? '',
    isRead: item?.isRead === true,
    targetType: item?.targetType ?? null,
    targetId: item?.targetId ?? null,
    targetSubId: item?.targetSubId ?? null
  }))
}

function normalizeInteractionModule(module) {
  if (!module) {
    return null
  }
  const normalized = String(module).trim()
  if (normalized === 'ershou' || normalized === 'secondhand') {
    return 'marketplace'
  }
  if (normalized === 'lost_found' || normalized === 'lostfound') {
    return 'lostandfound'
  }
  if (normalized === 'roommate') {
    return 'dating'
  }
  return normalized
}

function resolveDatingCenterTab(item) {
  if (item?.targetType === 'sent' || ['pick_accepted', 'pick_rejected', 'pick_updated'].includes(item?.type)) {
    return 'sent'
  }
  if (item?.targetType === 'published' || item?.targetType === 'posts') {
    return 'posts'
  }
  return 'received'
}

function buildInteractionQuery(item, extra = {}) {
  const query = { ...extra }
  if (item?.targetType) {
    query.targetType = item.targetType
  }
  if (item?.targetId) {
    query.targetId = item.targetId
  }
  if (item?.targetSubId) {
    query.targetSubId = item.targetSubId
  }
  if (item?.id) {
    query.notificationId = item.id
  }
  return query
}

function resolveContentInteractionLocation(item, detailPath, fallbackPath) {
  return {
    path: item?.targetId ? detailPath(item.targetId) : fallbackPath,
    query: buildInteractionQuery(item)
  }
}

function resolveDatingInteractionLocation(item) {
  return {
    path: '/dating/center',
    query: buildInteractionQuery(item, {
      tab: resolveDatingCenterTab(item)
    })
  }
}

function resolveDeliveryInteractionLocation(item) {
  return {
    path: item?.targetId ? `/delivery/detail/${item.targetId}` : '/delivery/home',
    query: buildInteractionQuery(item)
  }
}

function resolveMarketplaceInteractionLocation(item) {
  return {
    path: item?.targetId ? `/marketplace/detail/${item.targetId}` : '/marketplace/home',
    query: buildInteractionQuery(item)
  }
}

function resolveLostAndFoundInteractionLocation(item) {
  return {
    path: item?.targetId ? `/lostandfound/detail/${item.targetId}` : '/lostandfound/home',
    query: buildInteractionQuery(item)
  }
}

function resolveInteractionLocation(item) {
  switch (item?.module) {
    case 'dating':
      return resolveDatingInteractionLocation(item)
    case 'delivery':
      return resolveDeliveryInteractionLocation(item)
    case 'secret':
      return resolveContentInteractionLocation(item, (id) => `/secret/detail/${id}`, '/secret/home')
    case 'express':
      return resolveContentInteractionLocation(item, (id) => `/express/detail/${id}`, '/express/home')
    case 'topic':
      return resolveContentInteractionLocation(item, (id) => `/topic/detail/${id}`, '/topic/home')
    case 'photograph':
      return resolveContentInteractionLocation(item, (id) => `/photograph/detail/${id}`, '/photograph/home')
    case 'marketplace':
      return resolveMarketplaceInteractionLocation(item)
    case 'lostandfound':
      return resolveLostAndFoundInteractionLocation(item)
    default:
      return null
  }
}

function markInteractionItemRead(item) {
  if (!item?.id || item?.isRead) {
    return
  }
  item.isRead = true
  interactionUnreadCount.value = Math.max(0, Number(interactionUnreadCount.value || 0) - 1)
  request.post(`/information/message/id/${item.id}/read`).catch(() => {})
}

function handleInteractionSelect(item) {
  markInteractionItemRead(item)
  const location = resolveInteractionLocation(item)
  if (location) {
    router.push(location)
  }
}

function handleMarkAllInteractionsRead() {
  if (interactionUnreadCount.value <= 0) {
    return
  }
  interactionUnreadCount.value = 0
  interactionItems.value = interactionItems.value.map((item) => ({
    ...item,
    isRead: true
  }))
  request.post('/information/message/readall').catch(() => {
    loadInfoPage()
  })
}

async function loadInfoPage() {
  const [announcementRes, informationRes, interactionRes, unreadRes] = await Promise.allSettled([
    request.get('/information/announcement/start/0/size/5'),
    request.get('/information/overview'),
    request.get('/information/message/interaction/start/0/size/20'),
    request.get('/information/message/unread')
  ])

  if (announcementRes.status === 'fulfilled' && announcementRes.value?.success) {
    announcementList.value = Array.isArray(announcementRes.value?.data) ? announcementRes.value.data : []
  }
  if (informationRes.status === 'fulfilled' && informationRes.value?.success && informationRes.value?.data) {
    infoData.value = informationRes.value.data
  }
  if (interactionRes.status === 'fulfilled') {
    const items = normalizeInteractionItems(interactionRes.value?.data || [])
    interactionItems.value = items
    interactionHasMore.value = items.length >= INTERACTION_PAGE_SIZE
  }
  if (unreadRes.status === 'fulfilled') {
    interactionUnreadCount.value = Number(unreadRes.value?.data || 0)
  }
}

async function loadMoreInteractions() {
  interactionLoadingMore.value = true
  try {
    const start = interactionItems.value.length
    const res = await request.get(`/information/message/interaction/start/${start}/size/${INTERACTION_PAGE_SIZE}`)
    const items = normalizeInteractionItems(res?.data || [])
    interactionItems.value = [...interactionItems.value, ...items]
    interactionHasMore.value = items.length >= INTERACTION_PAGE_SIZE
  } finally {
    interactionLoadingMore.value = false
  }
}

onMounted(() => {
  loadInfoPage()
})
</script>

<style scoped>
.info-news-badge {
  background: color-mix(in srgb, var(--c-primary) 12%, var(--c-surface));
  color: var(--c-primary);
  box-shadow: inset 0 0 0 1px color-mix(in srgb, var(--c-primary) 10%, transparent);
}

.info-news-chevron {
  border-right: 1px solid color-mix(in srgb, var(--c-text-3) 72%, transparent);
  border-top: 1px solid color-mix(in srgb, var(--c-text-3) 72%, transparent);
}

.info-interaction-badge {
  background: var(--c-danger);
  box-shadow: 0 6px 14px color-mix(in srgb, var(--c-danger) 22%, transparent);
}

[data-theme="dark"] .info-news-badge {
  background: color-mix(in srgb, var(--c-primary) 16%, rgba(24, 38, 53, 0.9));
  box-shadow: inset 0 0 0 1px color-mix(in srgb, var(--c-primary) 16%, rgba(103, 232, 249, 0.1));
}

[data-theme="dark"] .info-news-chevron {
  border-right-color: color-mix(in srgb, var(--c-text-2) 56%, transparent);
  border-top-color: color-mix(in srgb, var(--c-text-2) 56%, transparent);
}

[data-theme="dark"] .info-interaction-badge {
  box-shadow: 0 8px 18px color-mix(in srgb, var(--c-danger) 16%, transparent);
}
</style>
