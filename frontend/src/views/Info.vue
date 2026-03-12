<template>
  <div class="weui-tab info-weui-tab">
    <div class="weui-tab__panel info-container">
      <section class="info-section">
        <div class="section-title">新闻 / 阅读</div>
        <div class="modern-card entry-card">
          <button type="button" class="entry-link" @click="router.push('/news')">
            <div class="entry-link__icon">新闻</div>
            <div class="entry-link__bd">
              <div class="entry-link__title">新闻通知</div>
              <div class="entry-link__desc">查看校园新闻与通知列表</div>
            </div>
            <div class="entry-link__ft"></div>
          </button>
          <button type="button" class="entry-link" @click="router.push('/reading')">
            <div class="entry-link__icon">阅读</div>
            <div class="entry-link__bd">
              <div class="entry-link__title">专题阅读</div>
              <div class="entry-link__desc">查看阅读专题与内容推荐</div>
            </div>
            <div class="entry-link__ft"></div>
          </button>
        </div>
        <TopicBlock :topics="infoData.topics || []" @view-all-topics="handleViewAllTopics" />
        <AccountBlock :accounts="infoData.accounts || []" @view-all-accounts="handleViewAllAccounts" />
      </section>

      <section class="info-section">
        <div class="section-title">系统通知 / 公告</div>
        <NoticeBlock :notice="systemNotice" />
        <HistoryBlock :festival="infoData.festival" :today-label="todayLabel" />
        <div v-if="!systemNotice && !infoData.festival" class="modern-card empty-card">暂无系统通知</div>
      </section>

      <section class="info-section">
        <div class="section-title">互动消息</div>
        <InteractionBlock :items="interactionItems" :unread-count="interactionUnreadCount" @select-item="handleInteractionSelect" />
      </section>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '../utils/request'
import NoticeBlock from '../components/info/NoticeBlock.vue'
import AccountBlock from '../components/info/AccountBlock.vue'
import TopicBlock from '../components/info/TopicBlock.vue'
import HistoryBlock from '../components/info/HistoryBlock.vue'
import InteractionBlock from '../components/info/InteractionBlock.vue'

const router = useRouter()
const infoData = ref({})
const announcementData = ref(null)
const interactionItems = ref([])
const interactionUnreadCount = ref(0)

const todayLabel = computed(() => {
  const d = new Date()
  return `${d.getMonth() + 1}月${d.getDate()}日`
})

const systemNotice = computed(() => announcementData.value || infoData.value.notice || null)

function handleViewAllAccounts() {
  router.push('/wechataccount')
}

function handleViewAllTopics() {
  router.push('/reading')
}

function normalizeInteractionItems(rawList) {
  if (!Array.isArray(rawList)) {
    return []
  }
  return rawList.map((item) => ({
    id: item?.id ?? null,
    module: item?.module ?? null,
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

function handleInteractionSelect(item) {
  if (item?.module === 'dating') {
    const query = {
      tab: item?.targetType === 'sent' ? 'sent' : 'received',
      targetType: item?.targetType ?? ''
    }
    if (item?.targetId) {
      query.focusedPickId = item.targetId
    }
    if (item?.targetSubId) {
      query.focusedProfileId = item.targetSubId
    }
    if (item?.id) {
      query.messageId = item.id
    }
    router.push({
      path: '/dating/center',
      query
    })
    return
  }
  const moduleRouteMap = {
    secret: {
      detail: (id) => `/secret/detail/${id}`,
      fallback: '/secret/home'
    },
    express: {
      detail: (id) => `/express/detail/${id}`,
      fallback: '/express/home'
    },
    topic: {
      detail: (id) => `/topic/detail/${id}`,
      fallback: '/topic/home'
    },
    photograph: {
      detail: (id) => `/photograph/detail/${id}`,
      fallback: '/photograph/home'
    },
    delivery: {
      detail: (id) => `/delivery/detail/${id}`,
      fallback: '/delivery/home'
    }
  }
  const moduleRoute = moduleRouteMap[item?.module]
  if (moduleRoute) {
    const query = {}
    if (item?.targetType) {
      query.targetType = item.targetType
    }
    if (item?.targetSubId) {
      query.targetSubId = item.targetSubId
    }
    if (item?.id) {
      query.notificationId = item.id
    }
    router.push({
      path: item?.targetId ? moduleRoute.detail(item.targetId) : moduleRoute.fallback,
      query
    })
  }
}

async function loadInfoPage() {
  const weui = typeof window !== 'undefined' && window.weui
  if (weui && typeof weui.loading === 'function') {
    weui.loading('正在加载资讯信息...')
  }
  try {
    const [announcementRes, informationRes, interactionRes, unreadRes] = await Promise.allSettled([
      request.get('/announcement'),
      request.get('/information/list'),
      request.get('/message/interaction/start/0/size/20'),
      request.get('/message/unread')
    ])

    if (announcementRes.status === 'fulfilled' && announcementRes.value?.success && announcementRes.value?.data) {
      announcementData.value = announcementRes.value.data
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
