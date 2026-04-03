<script setup>
import { computed, ref, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import request from '../../utils/request'
import CommunityHeader from '../../components/community/CommunityHeader.vue'
import { getDatingCenterCopy } from './datingContent'

const router = useRouter()
const route = useRoute()
const { t, locale } = useI18n()
const activeTab = ref(0) // 0: 收到的撩, 1: 我发出的, 2: 我的发布
const receivedList = ref([])
const sentList = ref([])
const postsList = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const dialogMessage = ref('')
const deleteTargetId = ref(null)
const deleteDialogVisible = ref(false)
const copy = computed(() => getDatingCenterCopy(locale.value))
const tabLabels = computed(() => [t('info.targetReceived'), t('info.targetSent'), t('info.targetPosts')])

function showDialog(msg) {
  dialogMessage.value = msg
  dialogVisible.value = true
}

function getTabQueryName(index) {
  if (index === 1) {
    return 'sent'
  }
  if (index === 2) {
    return 'posts'
  }
  return 'received'
}

function switchTab(index) {
  const tab = getTabQueryName(index)
  if (activeTab.value === index && getQueryValue('tab') === tab) {
    return
  }
  router.replace({
    path: '/dating/center',
    query: {
      ...route.query,
      tab
    }
  })
}

function getQueryValue(key) {
  const value = route.query[key]
  if (Array.isArray(value)) {
    return value[0] || ''
  }
  return typeof value === 'string' ? value : ''
}

function getInteractionTabIndex() {
  const tab = getQueryValue('tab')
  if (tab === 'sent') {
    return 1
  }
  if (tab === 'posts') {
    return 2
  }
  if (getQueryValue('targetType') === 'published') {
    return 2
  }
  return getQueryValue('targetType') === 'sent' ? 1 : 0
}

function applyRouteState() {
  activeTab.value = getInteractionTabIndex()
}

function getProfile(item) {
  return item?.roommateProfile || item?.datingProfile || {}
}

function normalizeId(value) {
  return value === undefined || value === null ? '' : String(value)
}

async function loadData() {
  loading.value = true
  try {
    if (activeTab.value === 0) {
      const res = await request.get('/dating/pick/my/received')
      const raw = res?.data || []
      receivedList.value = Array.isArray(raw) ? raw.map((p) => {
        const profile = getProfile(p)
        return {
          id: normalizeId(p.pickId),
          senderName: profile.nickname || p.username || copy.value.anonymous,
          content: p.content || '',
          time: p.createTime || copy.value.recentUpdate,
          status: p.state,
          avatar: profile.pictureURL || null
        }
      }) : []
    } else if (activeTab.value === 1) {
      const res = await request.get('/dating/pick/my/sent')
      const raw = res?.data || []
      sentList.value = Array.isArray(raw) ? raw.map((p) => {
        const profile = getProfile(p)
        return {
          id: normalizeId(p.pickId),
          targetName: profile.nickname || copy.value.anonymous,
          content: p.content || '',
          status: p.state,
          targetQq: profile.qq,
          targetWechat: profile.wechat,
          targetAvatar: profile.pictureURL
        }
      }) : []
    } else {
      const res = await request.get('/dating/profile/my')
      const raw = res?.data || []
      postsList.value = Array.isArray(raw) ? raw.map((p) => ({
        id: normalizeId(p.profileId),
        name: p.nickname,
        image: p.pictureURL,
        publishTime: p.createTime || copy.value.published
      })) : []
    }
  } finally {
    loading.value = false
  }
}

function handleAccept(item) {
  request.post(`/dating/pick/id/${item.id}`, null, { params: { state: 1 } })
    .then(() => {
      item.status = 1
      showDialog(copy.value.acceptSuccess)
    })
    .catch(() => {})
}

function handleReject(item) {
  request.post(`/dating/pick/id/${item.id}`, null, { params: { state: -1 } })
    .then(() => {
      item.status = -1
      showDialog(copy.value.rejectSuccess)
    })
    .catch(() => {})
}

function confirmDelete() {
  if (!deleteTargetId.value) return
  request.post(`/dating/profile/id/${deleteTargetId.value}/state`, null, { params: { state: 0 } })
    .then(() => {
      postsList.value = postsList.value.filter((item) => item.id !== deleteTargetId.value)
      deleteDialogVisible.value = false
      deleteTargetId.value = null
      showDialog(copy.value.hideSuccess)
    })
    .catch(() => {})
}

function openDeleteDialog(id) {
  deleteTargetId.value = id
  deleteDialogVisible.value = true
}

function getStatusText(status) {
  const map = {
    0: copy.value.statusPending,
    1: copy.value.statusAccepted,
    2: copy.value.statusRejected,
    '-1': copy.value.statusRejected,
  }
  return map[status] ?? map[String(status)] ?? copy.value.statusUnknown
}

function getStatusClass(status) {
  const s = String(status)
  if (s === '0') return 'bg-amber-100 text-amber-800'
  if (s === '1') return 'bg-green-100 text-green-800'
  return 'bg-red-100 text-red-800'
}

onMounted(() => {
  applyRouteState()
  loadData()
})

watch(() => route.fullPath, () => {
  applyRouteState()
  loadData()
})
</script>

<template>
  <div class="min-h-screen bg-[var(--c-bg)] pb-6">
    <CommunityHeader :title="copy.title" moduleColor="#ec4899" backTo="/dating/home" />

    <!-- Tabs -->
    <div class="flex bg-[var(--c-card)] border-b border-[var(--c-border)]">
      <div
        v-for="(label, idx) in tabLabels"
        :key="idx"
        class="flex-1 text-center py-3 text-base cursor-pointer relative transition-all duration-300"
        :class="activeTab === idx ? 'text-pink-500 font-bold' : 'text-[var(--c-text-2)]'"
        @click="switchTab(idx)"
      >
        {{ label }}
        <div v-if="activeTab === idx" class="absolute bottom-0 left-0 right-0 h-[3px] bg-pink-500"></div>
      </div>
    </div>

    <!-- Tab 1: Received -->
    <div v-if="activeTab === 0" class="p-4">
      <div v-if="loading" class="flex items-center justify-center gap-2 py-4 text-sm text-[var(--c-text-3)]">
        <i class="w-5 h-5 border-2 border-[var(--c-border)] border-t-pink-500 rounded-full animate-spin"></i> {{ t('communityCommon.loading') }}
      </div>
      <div v-else-if="receivedList.length === 0" class="flex flex-col items-center py-16 text-[var(--c-text-3)]">
        <div class="text-4xl mb-2">💕</div>
        <div class="text-sm">{{ copy.emptyReceived }}</div>
      </div>
      <div v-else class="flex flex-col gap-3">
        <div
          v-for="(item, index) in receivedList"
          :key="item.id"
          class="bg-[var(--c-surface)] rounded-xl shadow-sm p-4 animate-[slide-up_0.4s_ease_both]"
          :style="{ animationDelay: (index % 10) * 0.05 + 's' }"
        >
          <div class="flex items-center mb-3">
            <img :src="item.avatar || '/img/dating/default-avatar.png'" class="w-12 h-12 rounded-full object-cover mr-3" />
            <div class="flex-1">
              <div class="text-base font-medium text-[var(--c-text-1)] mb-1">{{ item.senderName }}</div>
              <div class="text-xs text-[var(--c-text-3)]">{{ item.time }}</div>
            </div>
          </div>
          <div class="p-3 bg-[var(--c-bg)] rounded-lg text-sm text-[var(--c-text-2)] leading-relaxed mb-3">{{ item.content }}</div>
          <div v-if="item.status === 0" class="flex gap-3 mt-2">
            <button type="button" class="flex-1 py-2 border-none rounded-lg text-sm cursor-pointer transition-opacity active:opacity-70 bg-pink-500 text-white" @click="handleAccept(item)">{{ copy.acceptAction }}</button>
            <button type="button" class="flex-1 py-2 border-none rounded-lg text-sm cursor-pointer transition-opacity active:opacity-70 bg-[var(--c-border)] text-[var(--c-text-2)]" @click="handleReject(item)">{{ copy.rejectAction }}</button>
          </div>
          <div v-else-if="item.status === 1" class="mt-2 text-center text-sm font-medium text-pink-500">{{ copy.acceptedShown }}</div>
          <div v-else-if="item.status === -1 || item.status === 2" class="mt-2 text-center text-sm font-medium text-[var(--c-text-3)]">{{ copy.rejected }}</div>
        </div>
      </div>
    </div>

    <!-- Tab 2: Sent -->
    <div v-if="activeTab === 1" class="p-4">
      <div v-if="loading" class="flex items-center justify-center gap-2 py-4 text-sm text-[var(--c-text-3)]">
        <i class="w-5 h-5 border-2 border-[var(--c-border)] border-t-pink-500 rounded-full animate-spin"></i> {{ t('communityCommon.loading') }}
      </div>
      <div v-else-if="sentList.length === 0" class="flex flex-col items-center py-16 text-[var(--c-text-3)]">
        <div class="text-4xl mb-2">💕</div>
        <div class="text-sm">{{ copy.emptySent }}</div>
      </div>
      <div v-else class="flex flex-col gap-3">
        <div
          v-for="(item, index) in sentList"
          :key="item.id"
          class="bg-[var(--c-surface)] rounded-xl shadow-sm p-4 animate-[slide-up_0.4s_ease_both]"
          :style="{ animationDelay: (index % 10) * 0.05 + 's' }"
        >
          <div class="flex items-center mb-3">
            <img :src="item.targetAvatar || item.targetImage || '/img/dating/default-avatar.png'" class="w-12 h-12 rounded-full object-cover mr-3" />
            <div class="flex-1">
              <div class="text-base font-medium text-[var(--c-text-1)] mb-1">{{ item.targetName }}</div>
            </div>
            <div class="px-3 py-1 rounded-full text-xs font-medium" :class="getStatusClass(item.status)">
              {{ getStatusText(item.status) }}
            </div>
          </div>
          <div class="p-3 bg-[var(--c-bg)] rounded-lg text-sm text-[var(--c-text-2)] leading-relaxed mb-3">{{ item.content }}</div>
          <div v-if="item.status === 1" class="mt-2 p-3 bg-pink-50 rounded-lg border-l-[3px] border-pink-500">
            <div class="text-xs text-[var(--c-text-2)] mb-1.5">{{ copy.contactLabel }}</div>
            <div class="text-sm text-pink-500 font-medium leading-loose">
              <span v-if="item.targetQq">{{ copy.qqLabel }}: {{ item.targetQq }}</span>
              <span v-if="item.targetWechat" class="ml-2">{{ copy.wechatLabel }}: {{ item.targetWechat }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Tab 3: My posts -->
    <div v-if="activeTab === 2" class="p-4">
      <div v-if="loading" class="flex items-center justify-center gap-2 py-4 text-sm text-[var(--c-text-3)]">
        <i class="w-5 h-5 border-2 border-[var(--c-border)] border-t-pink-500 rounded-full animate-spin"></i> {{ t('communityCommon.loading') }}
      </div>
      <div v-else-if="postsList.length === 0" class="flex flex-col items-center py-16 text-[var(--c-text-3)]">
        <div class="text-4xl mb-2">💕</div>
        <div class="text-sm">{{ copy.emptyPosts }}</div>
      </div>
      <div v-else class="flex flex-col gap-3">
        <div
          v-for="(item, index) in postsList"
          :key="item.id"
          class="bg-[var(--c-surface)] rounded-xl shadow-sm p-4 flex gap-3 items-center animate-[slide-up_0.4s_ease_both]"
          :style="{ animationDelay: (index % 10) * 0.05 + 's' }"
        >
          <img :src="(item.images && item.images[0]) || item.image || '/img/dating/default-avatar.png'" class="w-20 h-20 rounded-lg object-cover shrink-0" />
          <div class="flex-1 flex flex-col gap-1.5">
            <div class="text-base font-medium text-[var(--c-text-1)]">{{ item.name }}</div>
            <div class="text-xs text-[var(--c-text-3)]">{{ item.publishTime }}</div>
            <button type="button" class="w-full py-2 bg-[var(--c-bg)] text-[var(--c-text-2)] border border-[var(--c-border)] rounded-lg text-sm cursor-pointer mt-2 active:bg-[var(--c-border)]" @click="openDeleteDialog(item.id)">{{ copy.hideAction }}</button>
          </div>
        </div>
      </div>
    </div>

    <!-- Info dialog -->
    <div v-if="dialogVisible" class="fixed inset-0 bg-black/50 z-[1000]" @click="dialogVisible = false"></div>
    <div v-if="dialogVisible" class="fixed top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[280px] bg-[var(--c-surface)] rounded-xl overflow-hidden z-[1001] shadow-lg">
      <div class="text-center font-bold text-base py-4 text-[var(--c-text-1)]">{{ t('common.hint') }}</div>
      <div class="px-6 pb-4 text-center text-sm text-[var(--c-text-2)] leading-relaxed">{{ dialogMessage }}</div>
      <div class="border-t border-[var(--c-border)] flex">
        <a href="javascript:;" class="flex-1 text-center py-3 text-pink-500 font-medium no-underline" @click="dialogVisible = false">{{ t('common.confirm') }}</a>
      </div>
    </div>

    <!-- Delete confirmation dialog -->
    <div v-if="deleteDialogVisible" class="fixed inset-0 bg-black/50 z-[1000]" @click="deleteDialogVisible = false"></div>
    <div v-if="deleteDialogVisible" class="fixed top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[280px] bg-[var(--c-surface)] rounded-xl overflow-hidden z-[1001] shadow-lg">
      <div class="text-center font-bold text-base py-4 text-[var(--c-text-1)]">{{ copy.hideTitle }}</div>
      <div class="px-6 pb-4 text-center text-sm text-[var(--c-text-2)] leading-relaxed">{{ copy.hideDescription }}</div>
      <div class="border-t border-[var(--c-border)] flex">
        <a href="javascript:;" class="flex-1 text-center py-3 text-[var(--c-text-2)] no-underline border-r border-[var(--c-border)]" @click="deleteDialogVisible = false">{{ t('common.cancel') }}</a>
        <a href="javascript:;" class="flex-1 text-center py-3 text-pink-500 font-medium no-underline" @click="confirmDelete">{{ t('common.confirm') }}</a>
      </div>
    </div>
  </div>
</template>
