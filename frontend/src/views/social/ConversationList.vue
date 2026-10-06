<template>
  <div class="social-page space-y-3">
    <div class="flex items-center justify-between">
      <h1 class="text-lg font-semibold">{{ $t('social.chatsTitle') }}</h1>
      <RouterLink to="/social/search" class="social-text-action text-sm text-[var(--c-primary)]">{{ $t('social.searchAction') }}</RouterLink>
    </div>
    <div v-if="loading" class="text-sm text-[var(--c-text-tertiary)]">{{ $t('common.loading') }}</div>
    <div v-else-if="error" class="text-sm text-[var(--c-text-tertiary)]">
      {{ $t('common.networkError') }}
      <button type="button" class="social-text-action text-sm text-[var(--c-primary)]" @click="load(loadEpoch)">{{ $t('common.retry') }}</button>
    </div>
    <div v-else-if="!items.length" class="text-sm text-[var(--c-text-tertiary)]">
      {{ $t('social.chatsEmpty') }}
      <RouterLink to="/social/search" class="social-text-action text-sm text-[var(--c-primary)]">{{ $t('social.searchAction') }}</RouterLink>
    </div>
    <div v-if="items.length" class="social-inbox-list">
      <button v-for="item in items" :key="item.id" type="button" class="social-person-row w-full text-left" @click="router.push(`/social/chat/${item.id}`)">
        <AuthAvatar :url="item.peer?.avatarUrl" :alt="''" :placeholder="item.peer?.nickname?.slice(0, 1) || '?'" img-class="w-12 h-12 shrink-0 rounded-full" />
        <div class="min-w-0 flex-1">
          <div class="flex items-center justify-between gap-2"><div class="font-medium truncate">{{ item.peer?.nickname || $t('social.unknownUser') }}</div><time class="social-inbox-time" :datetime="item.updatedAt">{{ inboxTime(item.updatedAt) }}</time></div>
          <div class="social-inbox-preview"><p>{{ chatMessagePreview(item.lastMessage, $t('social.imageMessage'), $t('social.noMessages')) }}</p><span v-if="item.unreadCount > 0" class="social-inbox-unread social-primary">{{ item.unreadCount > 99 ? '99+' : item.unreadCount }}</span></div>
        </div>
      </button>
    </div>
    <button v-if="hasMore" type="button" :disabled="loading" class="social-text-action text-sm text-[var(--c-primary)]" @click="loadMore">{{ $t('social.loadMore') }}</button>
  </div>
</template>

<script setup>
import AuthAvatar from '../../components/social/AuthAvatar.vue'
import { useI18n } from 'vue-i18n'
import { chatMessagePreview } from './chatImageSupport.js'
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { fetchConversations } from '../../api/social.js'
import { useSocialRealtime } from '../../composables/useSocialRealtime.js'

const { locale } = useI18n()
function inboxTime(raw) {
  const date = new Date(raw)
  if (Number.isNaN(date.getTime())) return ''
  const today = new Date()
  const sameDay = date.toDateString() === today.toDateString()
  return new Intl.DateTimeFormat(locale.value, sameDay ? { hour: '2-digit', minute: '2-digit' } : { month: 'short', day: 'numeric' }).format(date)
}
const router = useRouter()
const items = ref([])
const loading = ref(false)
const error = ref(false)
const nextCursor = ref(null)
const hasMore = ref(false)
const { onEvent, ensureConnected } = useSocialRealtime()
let pollTimer = null
let dispose = null
let alive = true
let visibilityHandler = null
let loadEpoch = 0

async function load(epoch = loadEpoch, append = false) {
  if (loading.value) return
  if (!alive || document.hidden) return
  loading.value = true
  try {
    const res = await fetchConversations({ limit: 20, cursor: append ? nextCursor.value : undefined })
    if (!alive || epoch !== loadEpoch) return
    const page = res?.data || {}
    const rows = Array.isArray(page.items) ? page.items : []
    error.value = false
    items.value = append ? [...new Map([...items.value, ...rows].map(item => [item.id, item])).values()] : rows
    nextCursor.value = page.nextCursor || null
    hasMore.value = !!page.hasMore && !!nextCursor.value
  } catch (_) {
    if (alive && epoch === loadEpoch) error.value = true
  } finally {
    if (alive && epoch === loadEpoch) loading.value = false
  }
}

function loadMore() { if (hasMore.value) return load(loadEpoch, true) }

function stopPoll() {
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
}

function startPoll() {
  stopPoll()
  if (!alive || document.hidden) return
  pollTimer = setInterval(() => load(loadEpoch), 30000)
}

onMounted(() => {
  alive = true
  ensureConnected()
  visibilityHandler = () => {
    if (document.hidden) {
      stopPoll()
    } else if (alive) {
      load(loadEpoch)
      startPoll()
    }
  }
  document.addEventListener('visibilitychange', visibilityHandler)
  dispose = onEvent((evt) => {
    if (!alive || document.hidden) return
    if (evt?.type === 'realtime.ready' || evt?.type === 'message.created'
        || evt?.type === 'conversation.read' || evt?.type === 'social.changed') {
      load(loadEpoch)
    }
  })
  load(loadEpoch)
  startPoll()
})

onUnmounted(() => {
  alive = false
  loadEpoch += 1
  stopPoll()
  if (visibilityHandler) document.removeEventListener('visibilitychange', visibilityHandler)
  if (dispose) dispose()
})
</script>
