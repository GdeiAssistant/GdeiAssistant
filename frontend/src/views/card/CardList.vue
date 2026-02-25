<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { queryCardRecord } from '@/api/card'
import { showErrorTopTips } from '@/utils/toast'

const router = useRouter()
const route = useRoute()

const list = ref([])
const loading = ref(false)
const queryDate = computed(() => (route.query.date || '').trim())

function goBack() {
  router.back()
}

function reQuery() {
  router.back()
}

function parseDateToPayload(dateStr) {
  const s = (dateStr || '').trim()
  if (!s) return null
  // 期望格式：YYYY-MM-DD
  const m = /^(\d{4})-(\d{1,2})-(\d{1,2})$/.exec(s)
  if (!m) return null
  const year = parseInt(m[1], 10)
  const month = parseInt(m[2], 10)
  const date = parseInt(m[3], 10)
  if (!year || !month || !date) return null
  return { year, month, date }
}

function fetchList() {
  const payload = parseDateToPayload(queryDate.value)
  if (!payload) {
    showErrorTopTips('查询日期格式不正确，请重新选择')
    list.value = []
    return
  }

  loading.value = true
  queryCardRecord(payload)
    .then((res) => {
      const body = res && res.data ? res.data : res
      const result =
        body && typeof body === 'object'
          ? body.data !== undefined
            ? body.data
            : body
          : null
      const records = result && Array.isArray(result.cardList) ? result.cardList : []
      list.value = records
    })
    .catch(() => {
      list.value = []
    })
    .finally(() => {
      loading.value = false
    })
}

function formatDisplayDate(d) {
  if (!d) return ''
  const s = String(d)
  if (/^\d{4}-\d{2}-\d{2}$/.test(s)) {
    return s.replace(/-/g, '/')
  }
  return s
}

function amountClass(amount) {
  const n = parseFloat(amount)
  if (isNaN(n)) return 'card-amount'
  return n >= 0 ? 'card-amount card-amount--positive' : 'card-amount card-amount--negative'
}

function amountText(amount) {
  const n = parseFloat(amount)
  if (isNaN(n)) return String(amount)
  if (n >= 0) return `+${n}元`
  return `${n}元`
}

onMounted(() => {
  if (!queryDate.value) {
    router.replace('/card')
    return
  }
  fetchList()
})
</script>

<template>
  <div class="card-list-page">
    <template v-if="queryDate">
      <template v-if="loading">
        <div class="weui-mask_transparent" aria-hidden="true"></div>
        <div class="weui-toast__wrp">
          <div class="weui-toast">
            <span class="weui-primary-loading weui-icon_toast" aria-label="加载中"></span>
            <p class="weui-toast__content">加载中</p>
          </div>
        </div>
      </template>

      <div class="top-nav-bar">
        <div class="nav-btn-back" @click="goBack">返回</div>
      </div>

      <div class="page-header">
        <h1 class="page-title-green">饭卡消费查询</h1>
      </div>

      <div class="weui-cells__title">当前查询日期：{{ formatDisplayDate(queryDate) }}</div>

      <div class="weui-cells card-cells">
        <div
          v-for="(item, index) in list"
          :key="index"
          class="weui-cell card-cell"
        >
          <div class="weui-cell__bd">
            <div class="card-cell__line1">{{ item.merchantName || '未知商户' }} 【{{ item.tradeName || '未知类型' }}】</div>
            <div class="card-cell__line2">{{ item.tradeTime || '' }}</div>
          </div>
          <div class="weui-cell__ft">
            <span :class="amountClass(item.tradePrice)">{{ amountText(item.tradePrice) }}</span>
          </div>
        </div>
      </div>

      <div class="weui-btn_area">
        <button type="button" class="weui-btn weui-btn_primary" @click="reQuery">重新查询</button>
      </div>
    </template>
  </div>
</template>

<style scoped>
.card-list-page {
  background-color: #fff;
  min-height: 100vh;
  padding-bottom: 24px;
}

.top-nav-bar {
  display: flex;
  justify-content: flex-start;
  align-items: center;
  min-height: 44px;
  padding: 10px 15px;
  background-color: #fff;
  box-sizing: border-box;
}

.nav-btn-back {
  font-size: 16px;
  line-height: 24px;
  color: #888;
  cursor: pointer;
}

.page-header {
  text-align: center;
  padding: 0 0 20px;
  background-color: #fff;
}

.page-title-green {
  font-size: 34px;
  color: #09bb07;
  font-weight: 400;
  margin: 0 0 20px 0;
  line-height: 1.2;
}

.card-list-page .weui-cells__title {
  padding: 12px 15px 8px;
  font-size: 14px;
  color: #888;
}

.card-list-page .card-cells {
  margin-top: 0;
}

.card-cell {
  position: relative;
  padding: 12px 15px !important;
  box-sizing: border-box;
}

.card-cell::after {
  content: " ";
  position: absolute;
  left: 15px;
  right: 15px;
  bottom: 0;
  height: 1px;
  border-bottom: 1px solid #E5E5E5;
  transform-origin: 0 100%;
  transform: scaleY(0.5);
}

.card-cell:last-child::after {
  display: none !important;
}

.card-cell__line1 {
  font-size: 16px;
  color: #000;
  margin-bottom: 4px;
  word-wrap: break-word;
  word-break: break-all;
}

.card-cell__line2 {
  font-size: 13px;
  color: #888;
  line-height: 1.4;
}

.card-cell .weui-cell__ft {
  flex-shrink: 0;
  margin-left: 10px;
  text-align: right;
}

.card-amount {
  font-size: 15px;
  white-space: nowrap;
}

.card-amount--negative {
  color: #999;
}

.card-amount--positive {
  color: #333;
}

.card-list-page .weui-btn_area {
  margin-top: 24px;
  padding: 0 15px;
}

.card-list-page .weui-btn_area .weui-btn {
  width: 100%;
}
</style>
