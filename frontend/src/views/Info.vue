<template>
  <div class="weui-tab info-weui-tab">
    <div class="weui-tab__panel info-container">
      <section class="info-section">
        <div class="section-title">新闻</div>
        <div class="modern-card entry-card">
          <button type="button" class="entry-link" @click="router.push('/news')">
            <div class="entry-link__icon">新闻</div>
            <div class="entry-link__bd">
              <div class="entry-link__title">新闻</div>
              <div class="entry-link__desc">查看学校公开发布的校园新闻</div>
            </div>
            <div class="entry-link__ft"></div>
          </button>
        </div>
      </section>

      <section class="info-section">
        <div class="section-title">系统公告</div>
        <NoticeBlock :notices="systemNoticeItems" />
        <HistoryBlock :festival="infoData.festival" :today-label="todayLabel" />
        <div v-if="!systemNoticeItems.length && !infoData.festival" class="modern-card empty-card">暂无系统公告</div>
      </section>

      <section class="info-section">
        <div class="section-title">互动消息</div>
        <InteractionBlock
          :items="interactionItems"
          :unread-count="interactionUnreadCount"
          @select-item="handleInteractionSelect"
          @mark-all="handleMarkAllInteractionsRead"
        />
      </section>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '../utils/request'
import NoticeBlock from '../components/info/NoticeBlock.vue'
import HistoryBlock from '../components/info/HistoryBlock.vue'
import InteractionBlock from '../components/info/InteractionBlock.vue'

const router = useRouter()
const infoData = ref({})
const announcementList = ref([])
const interactionItems = ref([])
const interactionUnreadCount = ref(0)

const todayLabel = computed(() => {
  const d = new Date()
  return `${d.getMonth() + 1}月${d.getDate()}日`
})

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
    title: item?.title ?? '互动消息',
    content: item?.content ?? '你有一条新的互动消息',
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
  const weui = typeof window !== 'undefined' && window.weui
  if (weui && typeof weui.loading === 'function') {
    weui.loading('正在加载资讯信息...')
  }
  try {
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
      interactionItems.value = normalizeInteractionItems(interactionRes.value?.data || [])
    }
    if (unreadRes.status === 'fulfilled') {
      interactionUnreadCount.value = Number(unreadRes.value?.data || 0)
    }
  } finally {
    if (weui && typeof weui.hideLoading === 'function') {
      weui.hideLoading()
    }
  }
}

onMounted(() => {
  loadInfoPage()
})
</script>

<style scoped>
.info-weui-tab {
  height: 100vh !important;
  width: 100vw;
  overflow: hidden !important;
  display: flex;
  flex-direction: column;
  background-color: #f3f4f6;
}
.weui-tab__panel.info-container {
  flex: 1;
  overflow-y: auto !important;
  -webkit-overflow-scrolling: touch;
  box-sizing: border-box;
  padding: 12px;
  padding-bottom: 60px;
  background-color: #f3f4f6;
  min-height: 100vh;
}

.info-section {
  margin-bottom: 8px;
}

.section-title {
  font-size: 18px;
  font-weight: 700;
  color: #2f3b52;
  margin: 4px 4px 12px;
}

.modern-card {
  background: #ffffff;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.03);
}

.entry-card {
  padding-top: 8px;
  padding-bottom: 8px;
}

.entry-link {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 0;
  border: none;
  border-bottom: 1px solid #f0f0f0;
  background: transparent;
  text-align: left;
  cursor: pointer;
}

.entry-link:last-child {
  border-bottom: none;
}

.entry-link__icon {
  width: 42px;
  height: 42px;
  border-radius: 12px;
  background: #e8f7ef;
  color: #09bb07;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 700;
  flex-shrink: 0;
}

.entry-link__bd {
  flex: 1;
  min-width: 0;
}

.entry-link__title {
  font-size: 15px;
  font-weight: 600;
  color: #333333;
}

.entry-link__desc {
  margin-top: 4px;
  font-size: 13px;
  color: #999999;
}

.entry-link__ft {
  width: 8px;
  height: 8px;
  border-right: 1px solid #c8c8c8;
  border-top: 1px solid #c8c8c8;
  transform: rotate(45deg);
  flex-shrink: 0;
}

.empty-card {
  font-size: 14px;
  color: #999999;
  text-align: center;
}
</style>
