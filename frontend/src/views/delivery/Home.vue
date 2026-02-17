<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '../../utils/request'
import { useScrollLoad } from '../../composables/useScrollLoad'

const router = useRouter()
const activeStatus = ref('all') // all, pending, delivering, completed
const scrollContainer = ref({ get scrollTop() { return window.pageYOffset || document.documentElement.scrollTop } })

const fetchDeliveryData = async (page) => {
  const res = await request.get('/delivery/items', {
    params: { page, limit: 10, status: activeStatus.value === 'all' ? '' : activeStatus.value }
  })
  return res
}

const { items: list, loading, finished, refreshing, pullY, loadData, handleTouchStart, handleTouchMove, handleTouchEnd } = useScrollLoad(fetchDeliveryData)

function switchStatus(status) {
  activeStatus.value = status
  list.value = []
  loadData(true)
}

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
    <div class="delivery-header unified-header">
      <span class="delivery-header__back" @click="router.push('/')">返回</span>
      <h1 class="delivery-header__title">全民快递</h1>
      <span class="delivery-header__placeholder"></span>
    </div>

    <div class="pull-refresh-indicator" :style="{ height: pullY + 'px' }">
      <span v-if="refreshing" class="pull-refresh-text"><i class="weui-loading"></i> 正在刷新...</span>
      <span v-else-if="pullY > 50" class="pull-refresh-text">释放立即刷新</span>
      <span v-else-if="pullY > 0" class="pull-refresh-text">下拉刷新</span>
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
        v-for="item in list"
        :key="item.id"
        class="delivery-card"
        @click="goDetail(item.id)"
      >
        <!-- 卡片头部 -->
        <div class="delivery-card__header">
          <div class="delivery-card__type">{{ getTypeText(item.type) }}</div>
          <div class="delivery-card__reward">
            <span class="reward-symbol">￥</span>
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

    <div v-if="!loading && !refreshing && list.length === 0" class="delivery-empty">暂无任务</div>
    <div v-if="loading && !refreshing" class="delivery-loadmore"><i class="weui-loading"></i> 正在加载</div>
    <div v-if="finished && list.length > 0" class="delivery-loadmore">没有更多了</div>
  </div>
</template>

<style scoped>
.delivery-home {
  background: #f5f5f5;
  min-height: 100vh;
  padding-bottom: 60px;
}

.delivery-header.unified-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 44px;
  padding: 0 12px;
  background: #fff;
  border-bottom: 1px solid #e5e5e5;
}
.delivery-header__back { color: #333; cursor: pointer; min-width: 48px; font-size: 14px; }
.delivery-header__title { flex: 1; text-align: center; font-size: 16px; font-weight: 500; margin: 0; color: #333; }
.delivery-header__placeholder { min-width: 48px; }

.pull-refresh-indicator { display: flex; align-items: center; justify-content: center; overflow: hidden; transition: height 0.3s; }
.pull-refresh-text { font-size: 14px; color: #999; }
.pull-refresh-text .weui-loading {
  width: 16px; height: 16px; border: 2px solid #e5e5e5; border-top-color: #fa8231; border-radius: 50%;
  animation: spin 0.8s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

.delivery-status-tabs {
  display: flex;
  background: #fff;
  border-bottom: 1px solid #e5e5e5;
  padding: 0 15px;
}
.status-tab {
  flex: 1;
  text-align: center;
  padding: 12px 0;
  font-size: 15px;
  color: #666;
  cursor: pointer;
  position: relative;
  transition: all 0.3s;
}
.status-tab.active {
  color: #fa8231;
  font-weight: 500;
}
.status-tab.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: #fa8231;
}

.delivery-list {
  padding: 15px;
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.delivery-card {
  background: #fff;
  border-radius: 12px;
  padding: 15px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.03);
  cursor: pointer;
  transition: box-shadow 0.3s;
}
.delivery-card:active {
  box-shadow: 0 4px 16px rgba(0,0,0,0.08);
}

.delivery-card__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}
.delivery-card__type {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}
.delivery-card__reward {
  display: flex;
  align-items: baseline;
  color: #ff5252;
}
.reward-symbol {
  font-size: 16px;
  font-weight: bold;
  margin-right: 2px;
}
.reward-amount {
  font-size: 24px;
  font-weight: bold;
}

.delivery-card__route {
  margin-bottom: 15px;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 8px;
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
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: bold;
  color: #fff;
  margin-right: 10px;
  flex-shrink: 0;
}
.route-icon--pickup {
  background: #4a90e2;
}
.route-icon--delivery {
  background: #fa8231;
}
.route-text {
  flex: 1;
  font-size: 15px;
  color: #333;
  line-height: 1.5;
}

.delivery-card__footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}
.delivery-card__meta {
  display: flex;
  gap: 12px;
  font-size: 13px;
  color: #999;
}
.meta-time,
.meta-size {
  display: inline-block;
}
.delivery-badge {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}
.delivery-badge.status-pending {
  background: #fff3cd;
  color: #856404;
}
.delivery-badge.status-delivering {
  background: #d1ecf1;
  color: #0c5460;
}
.delivery-badge.status-completed {
  background: #d4edda;
  color: #155724;
}

.delivery-empty,
.delivery-loadmore {
  text-align: center;
  padding: 40px 20px;
  color: #999;
  font-size: 14px;
}
.delivery-loadmore .weui-loading {
  width: 16px; height: 16px; border: 2px solid #e5e5e5; border-top-color: #fa8231; border-radius: 50%;
  animation: spin 0.8s linear infinite;
  display: inline-block; vertical-align: middle; margin-right: 6px;
}
</style>
