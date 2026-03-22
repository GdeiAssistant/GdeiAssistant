<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '../../utils/request'
import CommunityHeader from '../../components/community/CommunityHeader.vue'

const route = useRoute()
const router = useRouter()
const item = ref(null)
const detailType = ref(null)
const trade = ref(null)
const accepting = ref(false)
const completing = ref(false)
const dialogVisible = ref(false)
const dialogMessage = ref('')
const confirmCompleteVisible = ref(false)

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
  if (accepting.value || !item.value) return
  accepting.value = true
  request.post('/delivery/acceptorder', null, { params: { orderId: item.value.orderId } })
    .then(() => {
      item.value.state = 1
      showDialog('接单成功！')
      setTimeout(() => router.push('/delivery/mine'), 1500)
    })
    .catch(() => { accepting.value = false })
}

function getUserRole() {
  if (detailType.value === 0) return 'publisher'
  if (detailType.value === 3) return 'runner'
  return null
}

function canAccept() {
  return detailType.value === 1
}

function canComplete() {
  return detailType.value === 0 && item.value && item.value.state === 1
}

function showCompleteConfirm() {
  confirmCompleteVisible.value = true
}

function handleComplete() {
  confirmCompleteVisible.value = false
  if (completing.value || !trade.value || trade.value.tradeId == null) return
  completing.value = true
  request.post(`/delivery/trade/id/${trade.value.tradeId}/finishtrade`)
    .then(() => {
      item.value.state = 2
      showDialog('订单已完成！')
      setTimeout(() => router.push('/delivery/mine'), 1500)
    })
    .catch(() => { completing.value = false })
}

onMounted(async () => {
  try {
    const res = await request.get(`/delivery/order/id/${route.params.id}`)
    const data = res?.data
    if (data && res.success !== false) {
      const o = data.order || {}
      item.value = {
        orderId: o.orderId,
        id: o.orderId,
        status: o.state,
        state: o.state,
        reward: o.price ?? 0,
        time: o.orderTime,
        size: '小件',
        type: 'express',
        pickupAddress: o.company ? `${o.company} 取件` : '取件',
        deliveryAddress: o.address || '',
        remarks: o.remarks,
        description: o.remarks,
        pickupCode: o.number || null,
        contactPhone: o.phone || null
      }
      detailType.value = data.detailType
      trade.value = data.trade || null
    } else {
      item.value = null
    }
  } catch (e) {
    item.value = null
  }
})
</script>

<template>
  <div class="delivery-detail" style="--module-color: #f59e0b">
    <CommunityHeader title="任务详情" moduleColor="#f59e0b" @back="router.back()" backTo="" />

    <div v-if="item" class="delivery-detail__content">
      <!-- 任务卡片 -->
      <div class="detail-card community-card">
        <div class="detail-card__header">
          <div class="detail-card__type">{{ getTypeText(item.type) }}</div>
          <div class="detail-card__reward">
            <span class="reward-symbol">&#xffe5;</span>
            <span class="reward-amount">{{ item.reward.toFixed(2) }}</span>
          </div>
        </div>

        <div class="detail-card__route">
          <div class="route-item route-item--pickup">
            <span class="route-icon route-icon--pickup">取</span>
            <div class="route-content">
              <div class="route-text">{{ item.pickupAddress }}</div>
              <div v-if="item.pickupCode && (detailType === 0 || detailType === 3)" class="route-code">
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
              <div v-if="item.contactPhone && (detailType === 0 || detailType === 3)" class="route-phone">
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
    <div v-if="dialogVisible" class="community-dialog-mask" @click="dialogVisible = false"></div>
    <div v-if="dialogVisible" class="community-dialog">
      <div class="community-dialog__title">提示</div>
      <div class="community-dialog__body">{{ dialogMessage }}</div>
      <div class="community-dialog__footer">
        <a href="javascript:;" class="community-dialog__btn community-dialog__btn--confirm" @click="dialogVisible = false">确定</a>
      </div>
    </div>

    <!-- 确认完成对话框 -->
    <div v-if="confirmCompleteVisible" class="community-dialog-mask" @click="confirmCompleteVisible = false"></div>
    <div v-if="confirmCompleteVisible" class="community-dialog">
      <div class="community-dialog__title">确认完成</div>
      <div class="community-dialog__body">确定要完成这个订单吗？完成后将无法撤销。</div>
      <div class="community-dialog__footer">
        <a href="javascript:;" class="community-dialog__btn community-dialog__btn--cancel" @click="confirmCompleteVisible = false">取消</a>
        <a href="javascript:;" class="community-dialog__btn community-dialog__btn--confirm" @click="handleComplete">确定</a>
      </div>
    </div>
  </div>
</template>

<style scoped>
.delivery-detail {
  background: var(--c-bg);
  min-height: 100vh;
}

.delivery-detail__content {
  padding: var(--space-lg);
  animation: community-slide-up 0.4s ease both;
}

.detail-card {
  padding: var(--space-xl);
}

.detail-card__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-xl);
  padding-bottom: var(--space-lg);
  border-bottom: 1px solid var(--c-border);
}
.detail-card__type {
  font-size: var(--font-xl);
  font-weight: 600;
  color: var(--c-text-1);
}
.detail-card__reward {
  display: flex;
  align-items: baseline;
  color: #ef4444;
}
.reward-symbol {
  font-size: var(--font-xl);
  font-weight: bold;
  margin-right: 2px;
}
.reward-amount {
  font-size: 28px;
  font-weight: bold;
}

.detail-card__route {
  margin-bottom: var(--space-xl);
  padding: var(--space-lg);
  background: var(--c-bg);
  border-radius: var(--radius-sm);
}
.route-item {
  display: flex;
  align-items: flex-start;
  margin-bottom: var(--space-lg);
}
.route-item:last-child {
  margin-bottom: 0;
}
.route-icon {
  width: 28px;
  height: 28px;
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: bold;
  color: #fff;
  margin-right: var(--space-md);
  flex-shrink: 0;
  margin-top: 2px;
}
.route-icon--pickup {
  background: #3b82f6;
}
.route-icon--delivery {
  background: var(--c-delivery);
}
.route-content {
  flex: 1;
}
.route-text {
  font-size: var(--font-lg);
  color: var(--c-text-1);
  line-height: 1.5;
  margin-bottom: 6px;
  font-weight: 500;
}
.route-code,
.route-phone {
  font-size: var(--font-base);
  color: var(--c-text-2);
  margin-top: var(--space-xs);
}

.detail-card__image {
  margin-bottom: var(--space-xl);
  border-radius: var(--radius-sm);
  overflow: hidden;
  background: var(--c-border);
}
.detail-card__image img {
  width: 100%;
  height: auto;
  max-height: 300px;
  object-fit: cover;
}

.detail-card__info {
  padding-top: var(--space-lg);
  border-top: 1px solid var(--c-border);
}
.info-row {
  display: flex;
  align-items: center;
  margin-bottom: var(--space-md);
}
.info-row:last-child {
  margin-bottom: 0;
}
.info-label {
  font-size: var(--font-base);
  color: var(--c-text-2);
  min-width: 80px;
}
.info-value {
  flex: 1;
  font-size: var(--font-base);
  color: var(--c-text-1);
}
.info-row--role {
  margin-bottom: var(--space-lg);
  padding-bottom: var(--space-md);
  border-bottom: 1px solid var(--c-border);
}
.role-badge {
  padding: 6px 14px;
  border-radius: var(--radius-full);
  font-size: 13px;
  font-weight: 500;
}
.role-publisher {
  background: #dbeafe;
  color: #1e40af;
}
.role-runner {
  background: #fef3c7;
  color: #92400e;
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

.detail-card__actions {
  margin-top: var(--space-xl);
  padding-top: var(--space-xl);
  border-top: 1px solid var(--c-border);
  display: flex;
  gap: var(--space-md);
}
.action-btn {
  flex: 1;
  height: 44px;
  border: none;
  border-radius: var(--radius-sm);
  font-size: var(--font-lg);
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
}
.action-btn--accept {
  background: linear-gradient(135deg, var(--c-delivery) 0%, #d97706 100%);
  color: #fff;
  box-shadow: 0 2px 8px rgba(245, 158, 11, 0.25);
}
.action-btn--accept:hover {
  box-shadow: 0 4px 12px rgba(245, 158, 11, 0.35);
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
</style>
