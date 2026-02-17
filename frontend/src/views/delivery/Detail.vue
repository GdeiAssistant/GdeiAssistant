<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '../../utils/request'

const route = useRoute()
const router = useRouter()
const item = ref(null)
const accepting = ref(false)
const completing = ref(false)
const dialogVisible = ref(false)
const dialogMessage = ref('')
const confirmCompleteVisible = ref(false)
const currentUserId = ref('user123') // Mock: 当前用户ID（与Mock数据保持一致）

function showDialog(msg) {
  dialogMessage.value = msg
  dialogVisible.value = true
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

function getSizeText(size) {
  const map = { 'small': '小件(外卖/文件)', 'medium': '中件(鞋服)', 'large': '大件(重物)' }
  return map[size] || '小件'
}

function handleAccept() {
  if (accepting.value) return
  accepting.value = true
  request.post('/delivery/accept', { id: route.params.id })
    .then(() => {
      item.value.status = 1
      item.value.runnerId = currentUserId.value
      showDialog('接单成功！')
      setTimeout(() => {
        router.push('/delivery/mine')
      }, 1500)
    })
    .catch(() => {
      accepting.value = false
    })
}

function isOwner() {
  return item.value && item.value.publisherId === currentUserId.value
}

function isRunner() {
  return item.value && item.value.runnerId === currentUserId.value
}

function getUserRole() {
  if (!item.value) return null
  if (isOwner()) return 'publisher' // 发布者
  if (isRunner()) return 'runner' // 接单者
  return 'visitor' // 访客
}

function canAccept() {
  return item.value && item.value.status === 0 && !isOwner() && !isRunner()
}

function canComplete() {
  // 只有发布者可以完成订单，且订单状态必须是配送中
  return item.value && item.value.status === 1 && isOwner()
}

function showCompleteConfirm() {
  confirmCompleteVisible.value = true
}

function handleComplete() {
  confirmCompleteVisible.value = false
  if (completing.value) return
  completing.value = true
  request.post('/delivery/complete', { id: route.params.id })
    .then(() => {
      item.value.status = 2
      showDialog('订单已完成！')
      setTimeout(() => {
        router.push('/delivery/mine')
      }, 1500)
    })
    .catch(() => {
      completing.value = false
    })
}

onMounted(async () => {
  try {
    const res = await request.get(`/delivery/item/${route.params.id}`)
    item.value = res?.data || res
  } catch (e) {
    showDialog('加载失败')
  }
})
</script>

<template>
  <div class="delivery-detail">
    <div class="delivery-header unified-header">
      <span class="delivery-header__back" @click="router.back()">返回</span>
      <h1 class="delivery-header__title">任务详情</h1>
      <span class="delivery-header__placeholder"></span>
    </div>

    <div v-if="item" class="delivery-detail__content">
      <!-- 任务卡片 -->
      <div class="detail-card">
        <div class="detail-card__header">
          <div class="detail-card__type">{{ getTypeText(item.type) }}</div>
          <div class="detail-card__reward">
            <span class="reward-symbol">￥</span>
            <span class="reward-amount">{{ item.reward.toFixed(2) }}</span>
          </div>
        </div>

        <div class="detail-card__route">
          <div class="route-item route-item--pickup">
            <span class="route-icon route-icon--pickup">取</span>
            <div class="route-content">
              <div class="route-text">{{ item.pickupAddress }}</div>
              <div v-if="item.pickupCode && (isOwner() || isRunner() || item.status !== 0)" class="route-code">
                取件码：{{ item.pickupCode }}
              </div>
              <div v-else-if="item.pickupCode" class="route-code">
                取件码：***
              </div>
            </div>
          </div>
          <div class="route-item route-item--delivery">
            <span class="route-icon route-icon--delivery">送</span>
            <div class="route-content">
              <div class="route-text">{{ item.deliveryAddress }}</div>
              <div v-if="item.contactPhone && (isOwner() || isRunner() || item.status !== 0)" class="route-phone">
                联系电话：{{ item.contactPhone }}
              </div>
              <div v-else-if="item.contactPhone" class="route-phone">
                联系电话：***
              </div>
            </div>
          </div>
        </div>

        <div v-if="item.pickupImage" class="detail-card__image">
          <img :src="item.pickupImage" alt="取件凭证" />
        </div>

        <div class="detail-card__info">
          <!-- 角色标识 -->
          <div v-if="getUserRole()" class="info-row info-row--role">
            <span class="info-label">我的角色：</span>
            <div :class="['role-badge', getUserRole() === 'publisher' ? 'role-publisher' : 'role-runner']">
              {{ getUserRole() === 'publisher' ? '发布者' : '接单者' }}
            </div>
          </div>
          <div class="info-row">
            <span class="info-label">物品大小：</span>
            <span class="info-value">{{ getSizeText(item.size) }}</span>
          </div>
          <div v-if="item.description" class="info-row">
            <span class="info-label">备注说明：</span>
            <span class="info-value">{{ item.description }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">发布时间：</span>
            <span class="info-value">{{ item.time }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">任务状态：</span>
            <div :class="['delivery-badge', getStatusClass(item.status)]">
              {{ getStatusText(item.status) }}
            </div>
          </div>
        </div>

        <!-- 操作按钮区域（嵌入卡片内部） -->
        <div v-if="canAccept() || canComplete()" class="detail-card__actions">
          <button
            v-if="canAccept()"
            type="button"
            class="action-btn action-btn--accept"
            :disabled="accepting"
            @click="handleAccept"
          >
            {{ accepting ? '接单中...' : '立即抢单' }}
          </button>
          <button
            v-if="canComplete()"
            type="button"
            class="action-btn action-btn--complete"
            :disabled="completing"
            @click="showCompleteConfirm"
          >
            {{ completing ? '确认中...' : '确认完成' }}
          </button>
        </div>
      </div>
    </div>

    <!-- 提示对话框 -->
    <div v-if="dialogVisible" class="weui-mask" @click="dialogVisible = false"></div>
    <div v-if="dialogVisible" class="weui-dialog">
      <div class="weui-dialog__hd"><strong class="weui-dialog__title">提示</strong></div>
      <div class="weui-dialog__bd">{{ dialogMessage }}</div>
      <div class="weui-dialog__ft">
        <a href="javascript:;" class="weui-dialog__btn weui-dialog__btn_primary" @click="dialogVisible = false">确定</a>
      </div>
    </div>

    <!-- 确认完成对话框 -->
    <div v-if="confirmCompleteVisible" class="weui-mask" @click="confirmCompleteVisible = false"></div>
    <div v-if="confirmCompleteVisible" class="weui-dialog">
      <div class="weui-dialog__hd"><strong class="weui-dialog__title">确认完成</strong></div>
      <div class="weui-dialog__bd">确定要完成这个订单吗？完成后将无法撤销。</div>
      <div class="weui-dialog__ft">
        <a href="javascript:;" class="weui-dialog__btn" @click="confirmCompleteVisible = false">取消</a>
        <a href="javascript:;" class="weui-dialog__btn weui-dialog__btn_primary" @click="handleComplete">确定</a>
      </div>
    </div>
  </div>
</template>

<style scoped>
.delivery-detail {
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

.delivery-detail__content {
  padding: 15px;
}

.detail-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.03);
}

.detail-card__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #f0f0f0;
}
.detail-card__type {
  font-size: 18px;
  font-weight: 600;
  color: #333;
}
.detail-card__reward {
  display: flex;
  align-items: baseline;
  color: #ff5252;
}
.reward-symbol {
  font-size: 18px;
  font-weight: bold;
  margin-right: 2px;
}
.reward-amount {
  font-size: 28px;
  font-weight: bold;
}

.detail-card__route {
  margin-bottom: 20px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
}
.route-item {
  display: flex;
  align-items: flex-start;
  margin-bottom: 15px;
}
.route-item:last-child {
  margin-bottom: 0;
}
.route-icon {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: bold;
  color: #fff;
  margin-right: 12px;
  flex-shrink: 0;
  margin-top: 2px;
}
.route-icon--pickup {
  background: #4a90e2;
}
.route-icon--delivery {
  background: #fa8231;
}
.route-content {
  flex: 1;
}
.route-text {
  font-size: 16px;
  color: #333;
  line-height: 1.5;
  margin-bottom: 6px;
  font-weight: 500;
}
.route-code,
.route-phone {
  font-size: 14px;
  color: #666;
  margin-top: 4px;
}

.detail-card__image {
  margin-bottom: 20px;
  border-radius: 8px;
  overflow: hidden;
  background: #f0f0f0;
}
.detail-card__image img {
  width: 100%;
  height: auto;
  max-height: 300px;
  object-fit: cover;
}

.detail-card__info {
  padding-top: 15px;
  border-top: 1px solid #f0f0f0;
}
.info-row {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}
.info-row:last-child {
  margin-bottom: 0;
}
.info-label {
  font-size: 14px;
  color: #666;
  min-width: 80px;
}
.info-value {
  flex: 1;
  font-size: 14px;
  color: #333;
}
.info-row--role {
  margin-bottom: 15px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}
.role-badge {
  padding: 6px 14px;
  border-radius: 16px;
  font-size: 13px;
  font-weight: 500;
}
.role-publisher {
  background: #e3f2fd;
  color: #1976d2;
}
.role-runner {
  background: #fff3e0;
  color: #f57c00;
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

.detail-card__actions {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
  display: flex;
  gap: 12px;
}
.action-btn {
  flex: 1;
  height: 44px;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
}
.action-btn--accept {
  background: linear-gradient(135deg, #fa8231 0%, #ff6b35 100%);
  color: #fff;
  box-shadow: 0 2px 8px rgba(250, 130, 49, 0.25);
}
.action-btn--accept:hover {
  box-shadow: 0 4px 12px rgba(250, 130, 49, 0.35);
}
.action-btn--accept:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
.action-btn--complete {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  color: #fff;
  box-shadow: 0 2px 8px rgba(16, 185, 129, 0.25);
}
.action-btn--complete:hover {
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.35);
}
.action-btn--complete:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.weui-mask {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.6);
  z-index: 1000;
}
.weui-dialog {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 85%;
  max-width: 300px;
  background: #fff;
  border-radius: 8px;
  z-index: 1001;
  overflow: hidden;
}
.weui-dialog__hd {
  padding: 16px;
  text-align: center;
}
.weui-dialog__title {
  font-size: 17px;
  color: #333;
}
.weui-dialog__bd {
  padding: 10px 20px;
  text-align: center;
  font-size: 15px;
  color: #666;
}
.weui-dialog__ft {
  display: flex;
  border-top: 1px solid #eee;
}
.weui-dialog__btn {
  flex: 1;
  padding: 14px;
  text-align: center;
  color: #fa8231;
  text-decoration: none;
  font-weight: 500;
}
</style>
