<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

function getTodayYYYYMMDD() {
  const d = new Date()
  return d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0') + '-' + String(d.getDate()).padStart(2, '0')
}

const router = useRouter()
const today = getTodayYYYYMMDD()
const queryDate = ref(today)
const showTopTips = ref(false)
const errorMsg = ref('')
let topTipsTimer = null

const showWeuiTopTips = (msg) => {
  errorMsg.value = msg
  showTopTips.value = true
  if (topTipsTimer) clearTimeout(topTipsTimer)
  topTipsTimer = setTimeout(() => {
    showTopTips.value = false
  }, 2000)
}

const doSearch = () => {
  const d = (queryDate.value || '').trim()
  if (!d) {
    showWeuiTopTips('请选择查询日期！')
    return
  }
  router.push({ path: '/card/list', query: { date: d } })
}

function goBack() {
  router.back()
}
</script>

<template>
  <div class="weui-toptips weui-toptips_warn" v-show="showTopTips">{{ errorMsg }}</div>
  <div class="card-search-page">
    <div class="top-nav-bar">
      <div class="nav-btn-back" @click="goBack">返回</div>
    </div>
    <h1 class="page-title-green">饭卡消费查询</h1>

    <div class="weui-cells weui-cells_form">
      <div class="weui-cell">
        <div class="weui-cell__hd">
          <label class="weui-label">查询日期</label>
        </div>
        <div class="weui-cell__bd weui-cell_primary">
          <input
            v-model="queryDate"
            class="weui-input"
            type="date"
            :max="today"
            placeholder="请选择日期"
          />
        </div>
      </div>
    </div>

    <div class="weui-btn_area">
      <button type="button" class="weui-btn weui-btn_primary" @click="doSearch">查询</button>
    </div>
  </div>
</template>

<style scoped>
.card-search-page {
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

.page-title-green {
  text-align: center;
  font-size: 34px;
  color: #09bb07;
  font-weight: 400;
  margin: 10px 0 20px 0;
  line-height: 1.2;
}

.card-search-page .weui-cells_form {
  margin-top: 0;
}

.card-search-page .weui-btn_area {
  margin-top: 24px;
  padding: 0 15px;
}

.card-search-page .weui-btn_area .weui-btn {
  width: 100%;
}

.weui-toptips {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  padding: 10px;
  font-size: 14px;
  text-align: center;
  color: #FFF !important;
  background-color: #E64340 !important;
  z-index: 99999 !important;
  opacity: 1 !important;
  visibility: visible !important;
  display: block;
  transition: opacity 0.3s;
  word-wrap: break-word;
  word-break: break-all;
}
</style>
