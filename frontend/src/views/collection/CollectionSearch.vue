<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const keyword = ref('')
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
  const k = keyword.value.trim()
  if (!k) {
    showWeuiTopTips('请输入查询关键词！')
    return
  }
  router.push({ path: '/collection/list', query: { keyword: k } })
}

function goBack() {
  router.back()
}
</script>

<template>
  <div class="weui-toptips weui-toptips_warn" v-show="showTopTips">{{ errorMsg }}</div>
  <div class="collection-search-page">
    <div class="top-nav-bar">
      <div class="nav-btn-back" @click="goBack">返回</div>
    </div>
    <h1 class="page-title-green">馆藏图书查询</h1>
    <p class="page-subtitle">广东第二师范学院移动图书馆</p>

    <div class="weui-cells weui-cells_form">
      <div class="weui-cell">
        <div class="weui-cell__hd">
          <label class="weui-label">关键词</label>
        </div>
        <div class="weui-cell__bd weui-cell_primary">
          <input
            v-model="keyword"
            class="weui-input"
            type="text"
            placeholder="请输入书名、作者等"
            @keyup.enter="doSearch"
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
.collection-search-page {
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
  margin: 10px 0 4px 0;
  line-height: 1.2;
}

.page-subtitle {
  text-align: center;
  font-size: 14px;
  color: #888;
  margin: 0 0 20px 0;
}

.collection-search-page .weui-cells_form {
  margin-top: 0;
}

.collection-search-page .weui-btn_area {
  margin-top: 24px;
  padding: 0 15px;
}

.collection-search-page .weui-btn_area .weui-btn {
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
  display: block; /* 可见时覆盖 WEUI 默认，隐藏由 v-show 的 display:none 控制 */
  transition: opacity 0.3s;
  word-wrap: break-word;
  word-break: break-all;
}
</style>
