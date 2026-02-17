<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import request from '../../utils/request'

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

function fetchList() {
  loading.value = true
  request.get('/card/query', { params: { date: queryDate.value } })
    .then((res) => {
      loading.value = false
      if (res && Array.isArray(res)) {
        list.value = res
      } else if (res && res.data && Array.isArray(res.data)) {
        list.value = res.data
      } else {
        list.value = []
      }
    })
    .catch(() => {
      loading.value = false
      list.value = []
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
            <div class="card-cell__line1">{{ item.location }} 【{{ item.type }}】</div>
            <div class="card-cell__line2">{{ item.time }}</div>
          </div>
          <div class="weui-cell__ft">
            <span :class="amountClass(item.amount)">{{ amountText(item.amount) }}</span>
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
