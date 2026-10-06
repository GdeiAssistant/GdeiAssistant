<template>
  <div class="social-page social-chat flex flex-col">
    <header class="social-chat-header">
      <button type="button" class="social-icon-button" :aria-label="$t('common.back')" @click="router.back()"><ArrowLeft :size="20" /></button>
      <RouterLink v-if="conversation?.peer?.id" :to="`/social/users/${conversation.peer.id}`" class="social-chat-contact">
        <AuthAvatar :url="conversation.peer.avatarUrl" :alt="''" :placeholder="conversation.peer.nickname?.slice(0, 1) || '?'" img-class="w-10 h-10 shrink-0 rounded-full" />
        <h1 class="min-w-0 font-semibold truncate">{{ conversation.peer.nickname }}</h1>
      </RouterLink>
      <h1 v-else class="font-semibold">{{ $t('social.chatTitle') }}</h1>
      <span class="w-11 shrink-0" />
    </header>
    <div ref="listEl" class="social-message-list min-h-0 flex-1 overflow-y-auto" @scroll="onScroll">
      <div v-if="loadingOlder" class="text-center text-xs text-[var(--c-text-tertiary)] py-2">{{ $t('common.loading') }}</div>
      <div v-if="loading" class="social-chat-empty">{{ $t('common.loading') }}</div>
      <div v-else-if="!messages.length" class="social-chat-empty"><MessageCircle :size="28" /><p>{{ $t('social.noMessages') }}</p></div>
      <template v-for="(msg, index) in messages" :key="msg.localKey || msg.id">
        <div v-if="showDate(msg, index)" class="social-date-separator">{{ messageDate(msg.createdAt) }}</div>
        <div class="social-message-row" :class="isMine(msg) ? 'is-mine' : 'is-peer'">
          <AuthAvatar v-if="!isMine(msg)" :url="conversation?.peer?.avatarUrl" :alt="''" :placeholder="conversation?.peer?.nickname?.slice(0, 1) || '?'" img-class="w-7 h-7 shrink-0 rounded-full" />
          <div class="social-message" :class="[isMine(msg) ? 'social-primary text-white' : 'social-peer-message', msg.type === 'IMAGE' ? 'is-image' : '']">
            <ChatImage v-if="msg.type === 'IMAGE' && msg.image" :image="msg.image" :local-preview="msg.localPreview || ''" @open="openViewer" />
            <div v-else class="whitespace-pre-wrap">{{ msg.content }}</div>
            <div class="social-message-meta">
              <time v-if="msg.createdAt" :datetime="msg.createdAt">{{ messageTime(msg.createdAt) }}</time>
              <span v-if="msg.status === 'pending'" role="status">{{ $t('social.pending') }}</span>
              <button v-else-if="msg.status === 'failed'" type="button" class="social-retry-button" @click="retry(msg)">{{ $t('social.failedRetry') }}</button>
              <Check v-else-if="isMine(msg) && msg.status === 'sent'" :size="12" :aria-label="$t('social.sent')" />
            </div>
          </div>
        </div>
      </template>
    </div>
    <div class="social-composer">
      <p v-if="conversation && !conversation.canSend" class="social-composer-notice">{{ $t('social.cannotMessage') }}</p>
      <p v-if="imageError" class="social-composer-error" role="alert">{{ imageError }}</p>
      <div v-if="selectedImage" class="social-attachment-preview">
        <img :src="selectedImage.preview" :alt="$t('social.imageMessage')" />
        <div class="min-w-0 flex-1"><span>{{ $t('social.imageReady') }}</span><small>{{ Math.max(1, Math.round(selectedImage.file.size / 1024)) }} KB</small></div>
        <button type="button" class="social-icon-button" :aria-label="$t('social.cancelImage')" @click="clearSelection"><X :size="18" /></button>
      </div>
      <div class="social-composer-row">
        <input ref="fileInput" type="file" accept="image/jpeg,image/png" class="hidden" tabindex="-1" @change="selectImage" />
        <button type="button" class="social-icon-button social-image-picker" :aria-label="$t('social.chooseImage')" :title="conversation?.imageMessagingEnabled ? $t('social.chooseImage') : $t('social.imagesUnavailable')" :disabled="loading || preparingImage || !conversation?.canSend || !conversation?.imageMessagingEnabled" @click="fileInput?.click()"><ImagePlus :size="21" /></button>
        <input v-model="draft" type="text" class="social-chat-input min-w-0 flex-1" :placeholder="$t('social.inputPlaceholder')" :aria-label="$t('social.inputPlaceholder')" :disabled="loading || !conversation || !conversation.canSend || !!selectedImage" @keydown.enter="event => { if (!event.isComposing) send() }" />
        <button type="button" class="social-send-button social-primary" :disabled="loading || preparingImage || !conversation || (!draft.trim() && !selectedImage) || !conversation.canSend" @click="send"><Send :size="17" /><span>{{ $t('social.send') }}</span></button>
      </div>
    </div>
    <Teleport to="body"><dialog ref="viewer" class="social-image-viewer" @cancel="closeViewer" @click="event => { if (event.target === viewer) closeViewer() }"><button type="button" class="social-viewer-close social-icon-button" :aria-label="$t('common.close')" @click="closeViewer"><X :size="24" /></button><img v-if="viewerSrc" :src="viewerSrc" :alt="$t('social.imageMessage')" /></dialog></Teleport>
  </div>
</template>

<script setup>
import { ref, watch, onMounted, onUnmounted, nextTick } from 'vue'
import { useI18n } from 'vue-i18n'
import { ArrowLeft, Check, ImagePlus, MessageCircle, Send, X } from 'lucide-vue-next'
import AuthAvatar from '../../components/social/AuthAvatar.vue'
import ChatImage from '../../components/social/ChatImage.vue'
import { prepareChatImage } from './chatImageSupport.js'
import { useRoute, useRouter } from 'vue-router'
import {
  fetchConversation, fetchMessages, sendMessage, sendImageMessage, markConversationRead, fetchSocialMe
} from '../../api/social.js'
import { useSocialRealtime } from '../../composables/useSocialRealtime.js'
import {
  isCommitted, findMergeIndex, upsertMessage as mergeUpsert, lastCommittedSeq
} from './chatMessageMerge.js'

const { t, locale } = useI18n()
const selectedImage = ref(null), preparingImage = ref(false), imageError = ref('')
const fileInput = ref(null), viewer = ref(null), viewerSrc = ref('')
const imageAttempts = new Map()
let selectionEpoch = 0
let viewerObjectUrl = null
const route = useRoute()
const router = useRouter()
const conversation = ref(null)
const messages = ref([])
const draft = ref('')
const loading = ref(false)
const loadingOlder = ref(false)
const hasMoreOlder = ref(false)
const meId = ref('')
const listEl = ref(null)
const { onEvent, ensureConnected, ready } = useSocialRealtime()

let pollTimer = null
let dispose = null
let alive = true
let loadEpoch = 0
let visibilityHandler = null

function upsertMessage(incoming, status = 'sent') {
  messages.value = mergeUpsert(messages.value, incoming, status, route.params.id)
  if (status === 'sent' && incoming.senderId === meId.value) {
    const attempt = imageAttempts.get(incoming.clientMessageId)
    if (attempt) {
      if (viewerSrc.value === attempt.preview) closeViewer()
      URL.revokeObjectURL(attempt.preview); imageAttempts.delete(incoming.clientMessageId)
    }
    const index = findMergeIndex(messages.value, incoming, route.params.id)
    if (index >= 0) messages.value[index].localPreview = ''
  }
}

function mergeMessages(incoming) {
  for (const msg of incoming) {
    upsertMessage(msg, 'sent')
  }
}

function earliestCommittedSeq() {
  const committed = messages.value.filter(isCommitted)
  if (!committed.length) return null
  return committed[0].seq
}

function isMine(msg) {
  return msg.senderId === meId.value || msg.mine === true
}

function uuid() {
  if (crypto?.randomUUID) return crypto.randomUUID()
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
    const r = Math.random() * 16 | 0
    const v = c === 'x' ? r : (r & 0x3 | 0x8)
    return v.toString(16)
  })
}

async function safeMarkRead() {
  if (!alive || document.hidden) return
  const last = lastCommittedSeq(messages.value)
  if (!last) return
  try {
    await markConversationRead(route.params.id, last)
  } catch (_) {}
}

async function loadConversation(epoch) {
  const res = await fetchConversation(route.params.id)
  if (!alive || epoch !== loadEpoch) return
  conversation.value = res?.data || null
}

async function loadMessages({ afterSeq, beforeSeq, replace } = {}, epoch = loadEpoch) {
  const params = { limit: 50 }
  if (afterSeq) params.afterSeq = afterSeq
  if (beforeSeq) params.beforeSeq = beforeSeq
  const res = await fetchMessages(route.params.id, params)
  if (!alive || epoch !== loadEpoch) return null
  const page = res?.data || {}
  const items = page.items || []
  if (replace) {
    messages.value = messages.value.filter((m) => m.status === 'pending' || m.status === 'failed')
  }
  mergeMessages(items)
  if (beforeSeq) {
    hasMoreOlder.value = !!page.hasMore
  } else if (!afterSeq) {
    hasMoreOlder.value = !!page.hasMore
  }
  await nextTick()
  return page
}

async function loadOlder() {
  if (!alive || loadingOlder.value || !hasMoreOlder.value || document.hidden) return
  const before = earliestCommittedSeq()
  if (!before) return
  loadingOlder.value = true
  const el = listEl.value
  const prevHeight = el ? el.scrollHeight : 0
  try {
    await loadMessages({ beforeSeq: before }, loadEpoch)
    await nextTick()
    if (el) {
      el.scrollTop = el.scrollHeight - prevHeight
    }
  } finally {
    loadingOlder.value = false
  }
}

function onScroll() {
  const el = listEl.value
  if (!el || el.scrollTop > 40) return
  loadOlder()
}

function messageTime(raw) {
  const date = new Date(raw)
  return Number.isNaN(date.getTime()) ? '' : new Intl.DateTimeFormat(locale.value, { hour: '2-digit', minute: '2-digit' }).format(date)
}
function messageDate(raw) {
  const date = new Date(raw)
  return Number.isNaN(date.getTime()) ? '' : new Intl.DateTimeFormat(locale.value, { month: 'short', day: 'numeric' }).format(date)
}
function showDate(msg, index) {
  return !!msg.createdAt && (index === 0 || new Date(messages.value[index - 1]?.createdAt).toDateString() !== new Date(msg.createdAt).toDateString())
}
async function openViewer(src) {
  closeViewer()
  // A pending bubble's preview is released on commit; the open viewer owns its copy.
  const attempt = [...imageAttempts.values()].find(item => item.preview === src)
  if (attempt) viewerObjectUrl = URL.createObjectURL(attempt.file)
  viewerSrc.value = viewerObjectUrl || src
  await nextTick(); viewer.value?.showModal()
}
function closeViewer() {
  viewer.value?.close(); viewerSrc.value = ''
  if (viewerObjectUrl) URL.revokeObjectURL(viewerObjectUrl)
  viewerObjectUrl = null
}
function clearSelection() {
  selectionEpoch++
  if (selectedImage.value) URL.revokeObjectURL(selectedImage.value.preview)
  selectedImage.value = null; preparingImage.value = false
  if (fileInput.value) fileInput.value.value = ''
}
async function selectImage(event) {
  const file = event.target.files?.[0]
  if (!file || !conversation.value?.canSend || !conversation.value?.imageMessagingEnabled) return
  clearSelection(); imageError.value = ''; preparingImage.value = true
  const run = selectionEpoch, token = localStorage.getItem('token')
  try {
    const prepared = await prepareChatImage(file)
    if (!alive || run !== selectionEpoch || token !== localStorage.getItem('token')) return
    selectedImage.value = { ...prepared, preview: URL.createObjectURL(prepared.file) }
  } catch (error) { if (run === selectionEpoch) imageError.value = t(`social.${error.message || 'imageLoadError'}`) }
  finally { if (run === selectionEpoch) preparingImage.value = false }
}
function clearImages() {
  clearSelection(); closeViewer(); imageError.value = ''
  for (const item of imageAttempts.values()) URL.revokeObjectURL(item.preview)
  imageAttempts.clear()
}
function invalidateSession() { loadEpoch++; clearImages(); messages.value = []; draft.value = ''; conversation.value = null; meId.value = '' }
async function sendSelectedImage() {
  if (preparingImage.value || loading.value || !meId.value || !selectedImage.value || !conversation.value?.canSend || !conversation.value?.imageMessagingEnabled) return
  const selected = selectedImage.value
  const clientMessageId = uuid(), epoch = loadEpoch
  const local = { localKey: `c:${route.params.id}:${meId.value}:${clientMessageId}`, clientMessageId,
    conversationId: String(route.params.id), senderId: meId.value, type: 'IMAGE', content: '', mine: true,
    image: { width: selected.width, height: selected.height, size: selected.file.size, contentType: selected.contentType },
    localPreview: selected.preview, status: 'pending' }
  imageAttempts.set(clientMessageId, selected)
  selectedImage.value = null; if (fileInput.value) fileInput.value.value = ''
  upsertMessage(local, 'pending'); await nextTick()
  if (listEl.value) listEl.value.scrollTop = listEl.value.scrollHeight
  try {
    const response = await sendImageMessage(local.conversationId, clientMessageId, selected.file)
    if (!alive || epoch !== loadEpoch) return
    const data = response?.data
    if (data?.id && data?.seq != null && data.type === 'IMAGE' && data.image?.url) upsertMessage({ ...data, mine: true }, 'sent')
    else throw new Error('incomplete image response')
    await loadConversation(epoch)
  } catch (_) {
    if (!alive || epoch !== loadEpoch) return
    const index = findMergeIndex(messages.value, local, local.conversationId)
    if (index >= 0 && messages.value[index].status !== 'sent') messages.value[index].status = 'failed'
    await loadConversation(epoch)
  }
}

async function send() {
  if (selectedImage.value) return sendSelectedImage()
  const epoch = loadEpoch
  const conversationId = String(route.params.id)
  const content = draft.value.trim()
  if (loading.value || !conversation.value || !meId.value) return
  if (!content) return
  if (conversation.value && conversation.value.canSend === false) return
  const clientMessageId = uuid()
  const local = {
    localKey: `c:${route.params.id}:${meId.value}:${clientMessageId}`,
    clientMessageId,
    conversationId: String(route.params.id),
    content,
    senderId: meId.value,
    mine: true,
    status: 'pending'
    // 无 fractional seq：pending 不参与 afterSeq/已读
  }
  upsertMessage(local, 'pending')
  const keptDraft = content
  draft.value = ''
  await nextTick()
  if (listEl.value) listEl.value.scrollTop = listEl.value.scrollHeight
  try {
    const res = await sendMessage(conversationId, { clientMessageId, content })
    if (!alive || epoch !== loadEpoch) return
    const data = res?.data
    if (data?.id && data?.seq != null && data?.clientMessageId) {
      upsertMessage({ ...data, mine: true }, 'sent')
    } else {
      // 关键字段不完整：保留 pending，不降为 failed 以免超时已提交误报
      throw new Error('incomplete send response')
    }
    await loadConversation(epoch)
  } catch (err) {
    if (!alive || epoch !== loadEpoch) return
    const errorCode = err?.response?.data?.errorCode || err?.errorCode
    const idx = findMergeIndex(messages.value, local, route.params.id)
    if (errorCode === 'PRIVACY_RESTRICTED' || errorCode === 'CONTACT_UNAVAILABLE') {
      if (idx >= 0 && messages.value[idx].status !== 'sent') messages.value.splice(idx, 1)
      draft.value = keptDraft
      await loadConversation(epoch)
      return
    }
    if (idx >= 0 && messages.value[idx].status !== 'sent') {
      messages.value[idx].status = 'failed'
    }
    await loadConversation(epoch)
  }
}

async function retry(msg) {
  const epoch = loadEpoch
  const conversationId = String(route.params.id)
  if (!msg?.clientMessageId || !alive || msg.status === 'sent') return
  msg.status = 'pending'
  try {
    const attempt = imageAttempts.get(msg.clientMessageId)
    if (msg.type === 'IMAGE' && !attempt) throw new Error('missing original image')
    const res = msg.type === 'IMAGE'
      ? await sendImageMessage(conversationId, msg.clientMessageId, attempt.file)
      : await sendMessage(conversationId, { clientMessageId: msg.clientMessageId, content: msg.content })
    if (!alive || epoch !== loadEpoch) return
    const data = res?.data
    if (data?.id && data?.seq != null && data?.clientMessageId) {
      upsertMessage({ ...data, mine: true }, 'sent')
    } else {
      throw new Error('incomplete')
    }
  } catch (err) {
    if (!alive || epoch !== loadEpoch) return
    const errorCode = err?.response?.data?.errorCode || err?.errorCode
    if (msg.type !== 'IMAGE' && (errorCode === 'PRIVACY_RESTRICTED' || errorCode === 'CONTACT_UNAVAILABLE')) {
      draft.value = msg.content || draft.value
      const idx = findMergeIndex(messages.value, msg, route.params.id)
      if (idx >= 0 && messages.value[idx].status !== 'sent') messages.value.splice(idx, 1)
      return
    }
    const idx = findMergeIndex(messages.value, msg, conversationId)
    if (idx >= 0 && messages.value[idx].status !== 'sent') messages.value[idx].status = 'failed'
  }
}

let refreshingEpoch = null
async function refreshNewer(epoch = loadEpoch) {
  if (!alive || document.hidden || epoch !== loadEpoch || refreshingEpoch === epoch) return
  refreshingEpoch = epoch
  try {
    let cursor = lastCommittedSeq(messages.value)
    for (let pageNumber = 0; pageNumber < 8; pageNumber += 1) {
      const page = await loadMessages(cursor ? { afterSeq: cursor } : { replace: true }, epoch)
      if (!alive || document.hidden || epoch !== loadEpoch) return
      const next = lastCommittedSeq(messages.value)
      if (!cursor || !page?.hasMore || !next || next === cursor) break
      cursor = next
    }
    await safeMarkRead()
    await loadConversation(epoch)
  } catch (_) {
    // 下一次前台轮询继续补拉；保留已取得的消息。
  } finally {
    if (refreshingEpoch === epoch) refreshingEpoch = null
  }
}

function stopPoll() {
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
}

function startPoll() {
  stopPoll()
  if (!alive || document.hidden) return
  pollTimer = setInterval(() => {
    refreshNewer(loadEpoch)
  }, 10000)
}

async function resetSession() {
  clearImages()
  loadEpoch += 1
  const epoch = loadEpoch
  stopPoll()
  messages.value = []
  conversation.value = null
  hasMoreOlder.value = false
  draft.value = ''
  loading.value = true
  ensureConnected()
  try {
    const me = await fetchSocialMe()
    if (!alive || epoch !== loadEpoch) return
    meId.value = me?.data?.id || ''
    await loadConversation(epoch)
    await loadMessages({ replace: true }, epoch)
    await nextTick()
    if (listEl.value) listEl.value.scrollTop = listEl.value.scrollHeight
    await safeMarkRead()
  } finally {
    if (alive && epoch === loadEpoch) loading.value = false
  }
  if (alive && epoch === loadEpoch) startPoll()
}

watch(() => route.params.id, () => {
  if (alive) resetSession()
})

watch(ready, (v) => {
  if (v && alive && !document.hidden) {
    refreshNewer(loadEpoch)
  }
})

onMounted(async () => {
  alive = true
  window.addEventListener('social-auth-changed', invalidateSession)
  visibilityHandler = () => {
    if (document.hidden) {
      stopPoll()
    } else if (alive) {
      refreshNewer(loadEpoch)
      startPoll()
    }
  }
  document.addEventListener('visibilitychange', visibilityHandler)
  dispose = onEvent(async (evt) => {
    if (!alive || document.hidden) return
    if (evt?.type === 'realtime.ready') {
      await refreshNewer(loadEpoch)
      return
    }
    if (evt?.type === 'message.created' && String(evt.conversationId) === String(route.params.id)) {
      await refreshNewer(loadEpoch)
    }
    if (evt?.type === 'social.changed' || evt?.type === 'conversation.read') {
      await loadConversation(loadEpoch)
    }
  })
  await resetSession()
})

onUnmounted(() => {
  clearImages()
  window.removeEventListener('social-auth-changed', invalidateSession)
  alive = false
  loadEpoch += 1
  stopPoll()
  if (visibilityHandler) document.removeEventListener('visibilitychange', visibilityHandler)
  if (dispose) dispose()
})
</script>
