<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '../../utils/request'
import { useScrollLoad } from '../../composables/useScrollLoad'
import CommunityHeader from '../../components/community/CommunityHeader.vue'

const router = useRouter()
const activeStatus = ref('all') // all, pending, delivering, completed
const scrollContainer = ref({ get scrollTop() { return window.pageYOffset || document.documentElement.scrollTop } })

const PAGE_SIZE = 10
const fetchDeliveryData = async (page) => {
  const start = (page - 1) * PAGE_SIZE
  const res = await request.get(`/delivery/order/start/${start}/size/${PAGE_SIZE}`)
  const rawList = res?.data || []
  const list = Array.isArray(rawList) ? rawList.map((o) => ({
    id: o.orderId,
    status: o.state,
    reward: o.price ?? 0,
    time: o.orderTime,
    size: '小件',
    type: 'express',
    pickupAddress: o.company ? `${o.company} 取件` : '取件',
    deliveryAddress: o.address || ''
  })) : []
  return { list, hasMore: list.length >= PAGE_SIZE }
}

const { items: list, loading, finished, refreshing, pullY, loadData, handleTouchStart, handleTouchMove, handleTouchEnd } = useScrollLoad(fetchDeliveryData)

function switchStatus(status) {
  activeStatus.value = status
  list.value = []
  loadData(true)
}
// 后端列表接口暂无状态筛选，前端保留 Tab 切换 UI，数据仍为全部

function goDetail(id) {
  router.push(`/delivery/detail/${id}`)
}

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

function onWindowScroll() {
  if (refreshing.value || loading.value || finished.value) return
  const scrollTop = window.pageYOffset || document.documentElement.scrollTop
  const winH = window.innerHeight
  const docH = document.documentElement.scrollHeight
  if (scrollTop + winH >= docH - 80) loadData()
}

onMounted(() => {
  loadData()
  window.addEventListener('scroll', onWindowScroll)
})
onUnmounted(() => {
  window.removeEventListener('scroll', onWindowScroll)
})
</script>

<template>
  <div
    class="delivery-home"
    @touchstart="handleTouchStart"
    @touchmove="handleTouchMove($event, scrollContainer)"
    @touchend="handleTouchEnd"
  >
    <CommunityHeader title="全民快递" moduleColor="#f59e0b" backTo="/" />

    <div class="community-pull-refresh" :style="{ height: pullY + 'px' }">
      <span v-if="refreshing" class="community-pull-refresh__text"><i class="community-loading-spinner"></i> 正在刷新...</span>
      <span v-else-if="pullY > 50" class="community-pull-refresh__text">释放立即刷新</span>
      <span v-else-if="pullY > 0" class="community-pull-refresh__text">下拉刷新</span>
    </div>

    <!-- 状态筛选 Tab -->
    <div class="delivery-status-tabs">
      <div
        :class="['status-tab', { active: activeStatus === 'all' }]"
        @click="switchStatus('all')"
      >
        全部
      </div>
      <div
        :class="['status-tab', { active: activeStatus === 'pending' }]"
        @click="switchStatus('pending')"
      >
        待接单
      </div>
      <div
        :class="['status-tab', { active: activeStatus === 'delivering' }]"
        @click="switchStatus('delivering')"
      >
        配送中
      </div>
      <div
        :class="['status-tab', { active: activeStatus === 'completed' }]"
        @click="switchStatus('completed')"
      >
        已完成
      </div>
    </div>

    <!-- 任务卡片列表 -->
    <div class="delivery-list">
      <div
        v-for="(item, index) in list"
        :key="item.id"
        class="delivery-card community-card"
        :style="{ animationDelay: (index * 0.05) + 's' }"
        @click="goDetail(item.id)"
      >
        <!-- 卡片头部 -->
        <div class="delivery-card__header">
          <div class="delivery-card__type">{{ getTypeText(item.type) }}</div>
          <div class="delivery-card__reward">
            <span class="reward-symbol">&#xffe5;</span>
            <span class="reward-amount">{{ item.reward.toFixed(2) }}</span>
          </div>
        </div>

        <!-- 路线区 -->
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

        <!-- 卡片底部 -->
        <div class="delivery-card__footer">
          <div class="delivery-card__meta">
            <span class="meta-time">{{ item.time }}</span>
            <span class="meta-size">{{ item.size || '小件' }}</span>
          </div>
          <div :class="['delivery-badge', getStatusClass(item.status)]">
            {{ getStatusText(item.status) }}
          </div>
        </div>
      </div>
    </div>

    <div v-if="!loading && !refreshing && list.length === 0" class="community-empty">
      <div class="community-empty__text">暂无任务</div>
    </div>
    <div v-if="loading && !refreshing" class="community-loadmore"><i class="community-loading-spinner"></i> 正在加载</div>
    <div v-if="finished && list.length > 0" class="community-loadmore">没有更多了</div>
  </div>
</template>

<style scoped>
.delivery-home {
  background: var(--c-bg);
  min-height: 100vh;
  --module-color: #f59e0b;
}

.delivery-status-tabs {
  display: flex;
  background: var(--c-card);
  border-bottom: 1px solid var(--c-divider);
  padding: 0 var(--space-lg);
}
.status-tab {
  flex: 1;
  text-align: center;
  padding: var(--space-md) 0;
  font-size: var(--font-md);
  color: var(--c-text-2);
  cursor: pointer;
  position: relative;
  transition: all 0.3s;
}
.status-tab.active {
  color: var(--c-delivery);
  font-weight: 500;
}
.status-tab.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: var(--c-delivery);
  border-radius: 3px 3px 0 0;
}

.delivery-list {
  padding: var(--space-lg);
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
.meta-time,
.meta-size {
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
