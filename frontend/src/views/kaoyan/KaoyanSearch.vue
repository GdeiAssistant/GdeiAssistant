<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const name = ref('')
const candidateNo = ref('')
const idNo = ref('')
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

function goBack() {
  router.back()
}

function doQuery() {
  if (!name.value.trim() || !candidateNo.value.trim() || !idNo.value.trim()) {
    showWeuiTopTips('请完整填写查询信息！')
    return
  }
  router.push({
    path: '/kaoyan/result',
    query: { name: name.value.trim(), candidateNo: candidateNo.value.trim(), idNo: idNo.value.trim() }
  })
}
</script>

<template>
  <div class="kaoyan-search-root">
    <div class="weui-toptips weui-toptips_warn" v-show="showTopTips">{{ errorMsg }}</div>
    <div class="kaoyan-search-page">
    <div class="top-nav-bar">
      <div class="nav-btn-back" @click="goBack">返回</div>
    </div>
    <h1 class="page-title-green">考研成绩查询</h1>

    <div class="weui-cells weui-cells_form">
      <div class="weui-cell">
        <div class="weui-cell__hd">
          <label class="weui-label">姓名</label>
        </div>
        <div class="weui-cell__bd weui-cell_primary">
          <input
            v-model="name"
            class="weui-input"
            type="text"
            placeholder="请输入姓名"
          />
        </div>
      </div>
      <div class="weui-cell">
        <div class="weui-cell__hd">
          <label class="weui-label">考号</label>
        </div>
        <div class="weui-cell__bd weui-cell_primary">
          <input
            v-model="candidateNo"
            class="weui-input"
            type="text"
            maxlength="15"
            placeholder="请输入准考证号"
          />
        </div>
      </div>
      <div class="weui-cell">
        <div class="weui-cell__hd">
          <label class="weui-label">证件号</label>
        </div>
        <div class="weui-cell__bd weui-cell_primary">
          <input
            v-model="idNo"
            class="weui-input"
            type="text"
            maxlength="18"
            placeholder="请输入证件号码"
          />
        </div>
      </div>
    </div>

    <div class="weui-btn_area">
      <button type="button" class="weui-btn weui-btn_primary" @click="doQuery">查询</button>
    </div>
    <p class="kaoyan-wish">祝2019考研er金榜题名</p>

    <div class="weui-cells__title">备用查询入口</div>
    <div class="weui-cells">
      <a
        class="weui-cell weui-cell_access"
        href="https://yz.chsi.com.cn/apply/cjcxa/"
        target="_blank"
        rel="noopener noreferrer"
      >
        <div class="weui-cell__bd">
          <p>研招网硕士初试成绩查询</p>
        </div>
        <div class="weui-cell__ft"></div>
      </a>
    </div>
    </div>
  </div>
</template>

<style scoped>
.kaoyan-search-root {
  min-height: 100vh;
}

.kaoyan-search-page {
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

.kaoyan-search-page .weui-cells_form .weui-label {
  width: 5em;
}

.kaoyan-search-page .weui-cells_form {
  margin-top: 0;
}

.kaoyan-search-page .weui-btn_area {
  margin-top: 24px;
  padding: 0 15px;
}

.kaoyan-search-page .weui-btn_area .weui-btn {
  width: 100%;
}

.kaoyan-wish {
  margin-top: 16px;
  text-align: center;
  font-size: 14px;
  color: #999;
  padding: 0 15px;
}

.kaoyan-search-page .weui-cells__title {
  padding: 12px 15px 8px;
  font-size: 14px;
  color: #888;
}

.kaoyan-search-page .weui-cells {
  margin-top: 0;
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
  display: block;
  word-wrap: break-word;
  word-break: break-all;
}
</style>
