<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '../../utils/request'

const router = useRouter()
const activeTab = ref('published') // published, accepted
const publishedList = ref([])
const acceptedList = ref([])
const loading = ref(false)
const currentUserId = ref('user123') // Mock: 当前用户ID

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

function switchTab(tab) {
  activeTab.value = tab
  loadData()
}

function goDetail(id) {
  router.push(`/delivery/detail/${id}`)
}


async function loadData() {
  loading.value = true
  try {
    if (activeTab.value === 'published') {
      const res = await request.get('/delivery/my/published')
      publishedList.value = res?.data || res || []
    } else {
      const res = await request.get('/delivery/my/accepted')
      acceptedList.value = res?.data || res || []
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="delivery-mine">
    <div class="delivery-header unified-header">
      <span class="delivery-header__back" @click="router.push('/')">返回</span>
      <h1 class="delivery-header__title">我的跑腿</h1>
      <span class="delivery-header__placeholder"></span>
    </div>

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
      <div v-if="loading" class="delivery-loading">加载中...</div>
      <div v-else-if="publishedList.length === 0" class="delivery-empty">暂无发布的任务</div>
      <div v-else class="delivery-list">
        <div
          v-for="item in publishedList"
          :key="item.id"
          class="delivery-card"
          @click="goDetail(item.id)"
        >
          <div class="delivery-card__header">
            <div class="delivery-card__type">{{ getTypeText(item.type) }}</div>
            <div class="delivery-card__reward">
              <span class="reward-symbol">￥</span>
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
      <div v-if="loading" class="delivery-loading">加载中...</div>
      <div v-else-if="acceptedList.length === 0" class="delivery-empty">暂无接单的任务</div>
      <div v-else class="delivery-list">
        <div
          v-for="item in acceptedList"
          :key="item.id"
          class="delivery-card"
          @click="goDetail(item.id)"
        >
          <div class="delivery-card__header">
            <div class="delivery-card__type">{{ getTypeText(item.type) }}</div>
            <div class="delivery-card__reward">
              <span class="reward-symbol">￥</span>
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

.delivery-mine__tabs {
  display: flex;
  background: #fff;
  border-bottom: 1px solid #e5e5e5;
}
.mine-tab {
  flex: 1;
  text-align: center;
  padding: 12px 0;
  font-size: 15px;
  color: #666;
  cursor: pointer;
  position: relative;
  transition: all 0.3s;
}
.mine-tab.active {
  color: #fa8231;
  font-weight: 500;
}
.mine-tab.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: #fa8231;
}

.delivery-mine__content {
  padding: 15px;
}

.delivery-loading,
.delivery-empty {
  text-align: center;
  padding: 40px 20px;
  color: #999;
  font-size: 14px;
}

.delivery-list {
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
.meta-time {
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
</style>
