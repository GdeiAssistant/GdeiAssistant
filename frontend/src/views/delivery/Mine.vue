<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '../../utils/request'
import CommunityHeader from '../../components/community/CommunityHeader.vue'

const router = useRouter()
const route = useRoute()
const activeTab = ref('published') // published, accepted
const publishedList = ref([])
const acceptedList = ref([])
const loading = ref(false)

function getStatusText(status) {
  const map = { 0: '待接单', 1: '配送中', 2: '已完成' }
  return map[status] || '未知'
}

function getStatusClass(status) {
  const map = { 0: 'status-pending', 1: 'status-delivering', 2: 'status-completed' }
  return map[status] || ''
}

function getTypeText(type) {
  const map = { 'express': '代取快递', 'food': '买饭', 'other': '跑腿' }
  return map[type] || '跑腿'
}

function getQueryTab() {
  const value = route.query.tab
  if (Array.isArray(value)) {
    return value[0] || ''
  }
  return typeof value === 'string' ? value : ''
}

function normalizeTab(tab) {
  return tab === 'accepted' ? 'accepted' : 'published'
}

function applyRouteState() {
  activeTab.value = normalizeTab(getQueryTab())
}

function switchTab(tab) {
  const normalized = normalizeTab(tab)
  if (activeTab.value === normalized && getQueryTab() === normalized) {
    return
  }
  router.replace({
    path: '/delivery/mine',
    query: {
      ...route.query,
      tab: normalized
    }
  })
}

function goDetail(id) {
  router.push(`/delivery/detail/${id}`)
}


async function loadData() {
  loading.value = true
  try {
    const res = await request.get('/delivery/mine')
    const data = res?.data || {}
    const published = data.published || []
    const accepted = data.accepted || []
    publishedList.value = Array.isArray(published) ? published.map((o) => ({
      id: o.orderId,
      orderId: o.orderId,
      status: o.state,
      state: o.state,
      reward: o.price ?? 0,
      time: o.orderTime,
      type: 'express',
      pickupAddress: o.company ? `${o.company} 取件` : '取件',
      deliveryAddress: o.address || ''
    })) : []
    acceptedList.value = Array.isArray(accepted) ? accepted.map((o) => ({
      id: o.orderId,
      orderId: o.orderId,
      status: o.state,
      state: o.state,
      reward: o.price ?? 0,
      time: o.orderTime,
      type: 'express',
      pickupAddress: o.company ? `${o.company} 取件` : '取件',
      deliveryAddress: o.address || ''
    })) : []
  } catch (e) {
    publishedList.value = []
    acceptedList.value = []
  } finally {
    loading.value = false
  }
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
  <div class="delivery-mine" style="--module-color: #f59e0b">
    <CommunityHeader title="我的跑腿" moduleColor="#f59e0b" backTo="/" />

    <!-- Tab切换 -->
    <div class="delivery-mine__tabs">
      <div
        :class="['mine-tab', { active: activeTab === 'published' }]"
        @click="switchTab('published')"
      >
        我发布的
      </div>
      <div
        :class="['mine-tab', { active: activeTab === 'accepted' }]"
        @click="switchTab('accepted')"
      >
        我接的单
      </div>
    </div>

    <!-- 我发布的 -->
    <div v-if="activeTab === 'published'" class="delivery-mine__content">
      <div v-if="loading" class="community-loadmore"><i class="community-loading-spinner"></i> 加载中</div>
      <div v-else-if="publishedList.length === 0" class="community-empty">
        <div class="community-empty__text">暂无发布的任务</div>
      </div>
      <div v-else class="delivery-list">
        <div
          v-for="(item, index) in publishedList"
          :key="item.id"
          class="delivery-card community-card"
          :style="{ animationDelay: (index * 0.05) + 's' }"
          @click="goDetail(item.id)"
        >
          <div class="delivery-card__header">
            <div class="delivery-card__type">{{ getTypeText(item.type) }}</div>
            <div class="delivery-card__reward">
              <span class="reward-symbol">&#xffe5;</span>
              <span class="reward-amount">{{ item.reward.toFixed(2) }}</span>
            </div>
          </div>
          <div class="delivery-card__route">
            <div class="route-item route-item--pickup">
              <span class="route-icon route-icon--pickup">取</span>
              <span class="route-text">{{ item.pickupAddress }}</span>
            </div>
            <div class="route-item route-item--delivery">
              <span class="route-icon route-icon--delivery">送</span>
              <span class="route-text">{{ item.deliveryAddress }}</span>
            </div>
          </div>
          <div class="delivery-card__footer">
            <div class="delivery-card__meta">
              <span class="meta-time">{{ item.time }}</span>
            </div>
            <div :class="['delivery-badge', getStatusClass(item.status)]">
              {{ getStatusText(item.status) }}
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 我接的单 -->
    <div v-if="activeTab === 'accepted'" class="delivery-mine__content">
      <div v-if="loading" class="community-loadmore"><i class="community-loading-spinner"></i> 加载中</div>
      <div v-else-if="acceptedList.length === 0" class="community-empty">
        <div class="community-empty__text">暂无接单的任务</div>
      </div>
      <div v-else class="delivery-list">
        <div
          v-for="(item, index) in acceptedList"
          :key="item.id"
          class="delivery-card community-card"
          :style="{ animationDelay: (index * 0.05) + 's' }"
          @click="goDetail(item.id)"
        >
          <div class="delivery-card__header">
            <div class="delivery-card__type">{{ getTypeText(item.type) }}</div>
            <div class="delivery-card__reward">
              <span class="reward-symbol">&#xffe5;</span>
              <span class="reward-amount">{{ item.reward.toFixed(2) }}</span>
            </div>
          </div>
          <div class="delivery-card__route">
            <div class="route-item route-item--pickup">
              <span class="route-icon route-icon--pickup">取</span>
              <span class="route-text">{{ item.pickupAddress }}</span>
            </div>
            <div class="route-item route-item--delivery">
              <span class="route-icon route-icon--delivery">送</span>
              <span class="route-text">{{ item.deliveryAddress }}</span>
            </div>
          </div>
          <div class="delivery-card__footer">
            <div class="delivery-card__meta">
              <span class="meta-time">{{ item.time }}</span>
            </div>
            <div :class="['delivery-badge', getStatusClass(item.status)]">
              {{ getStatusText(item.status) }}
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.delivery-mine {
  background: var(--c-bg);
  min-height: 100vh;
  padding-bottom: 60px;
}

.delivery-mine__tabs {
  display: flex;
  background: var(--c-card);
  border-bottom: 1px solid var(--c-divider);
  padding: 0 var(--space-lg);
}
.mine-tab {
  flex: 1;
  text-align: center;
  padding: var(--space-md) 0;
  font-size: var(--font-md);
  color: var(--c-text-2);
  cursor: pointer;
  position: relative;
  transition: all 0.3s;
}
.mine-tab.active {
  color: var(--c-delivery);
  font-weight: 500;
}
.mine-tab.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: var(--c-delivery);
  border-radius: 3px 3px 0 0;
}

.delivery-mine__content {
  padding: var(--space-lg);
  animation: community-fade-in 0.3s ease both;
}

.delivery-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-lg);
}

.delivery-card {
  padding: var(--space-lg);
  cursor: pointer;
  animation: community-slide-up 0.4s ease both;
}

.delivery-card__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-lg);
}
.delivery-card__type {
  font-size: var(--font-lg);
  font-weight: 600;
  color: var(--c-text-1);
}
.delivery-card__reward {
  display: flex;
  align-items: baseline;
  color: #ef4444;
}
.reward-symbol {
  font-size: var(--font-lg);
  font-weight: bold;
  margin-right: 2px;
}
.reward-amount {
  font-size: var(--font-2xl);
  font-weight: bold;
}

.delivery-card__route {
  margin-bottom: var(--space-lg);
  padding: var(--space-md);
  background: var(--c-bg);
  border-radius: var(--radius-sm);
}
.route-item {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}
.route-item:last-child {
  margin-bottom: 0;
}
.route-icon {
  width: 24px;
  height: 24px;
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--font-sm);
  font-weight: bold;
  color: #fff;
  margin-right: 10px;
  flex-shrink: 0;
}
.route-icon--pickup {
  background: #3b82f6;
}
.route-icon--delivery {
  background: var(--c-delivery);
}
.route-text {
  flex: 1;
  font-size: var(--font-md);
  color: var(--c-text-1);
  line-height: 1.5;
}

.delivery-card__footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: var(--space-md);
  border-top: 1px solid var(--c-border);
}
.delivery-card__meta {
  display: flex;
  gap: var(--space-md);
  font-size: var(--font-base);
  color: var(--c-text-3);
}
.meta-time {
  display: inline-block;
}
.delivery-badge {
  padding: var(--space-xs) var(--space-md);
  border-radius: var(--radius-full);
  font-size: var(--font-sm);
  font-weight: 500;
}
.delivery-badge.status-pending {
  background: #fef3c7;
  color: #92400e;
}
.delivery-badge.status-delivering {
  background: #dbeafe;
  color: #1e40af;
}
.delivery-badge.status-completed {
  background: #d1fae5;
  color: #065f46;
}
</style>
