<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'

const router = useRouter()
const { t } = useI18n()
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
    showWeuiTopTips(t('graduateExam.fillAllFields'))
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
      <div class="nav-btn-back" @click="goBack">{{ t('graduateExam.back') }}</div>
    </div>
    <h1 class="page-title-green">{{ t('graduateExam.title') }}</h1>

    <div class="weui-cells weui-cells_form">
      <div class="weui-cell">
        <div class="weui-cell__hd">
          <label class="weui-label">{{ t('graduateExam.name') }}</label>
        </div>
        <div class="weui-cell__bd weui-cell_primary">
          <input
            v-model="name"
            class="weui-input"
            type="text"
            :placeholder="t('graduateExam.namePlaceholder')"
          />
        </div>
      </div>
      <div class="weui-cell">
        <div class="weui-cell__hd">
          <label class="weui-label">{{ t('graduateExam.candidateNo') }}</label>
        </div>
        <div class="weui-cell__bd weui-cell_primary">
          <input
            v-model="candidateNo"
            class="weui-input"
            type="text"
            maxlength="15"
            :placeholder="t('graduateExam.candidateNoPlaceholder')"
          />
        </div>
      </div>
      <div class="weui-cell">
        <div class="weui-cell__hd">
          <label class="weui-label">{{ t('graduateExam.idNo') }}</label>
        </div>
        <div class="weui-cell__bd weui-cell_primary">
          <input
            v-model="idNo"
            class="weui-input"
            type="text"
            maxlength="18"
            :placeholder="t('graduateExam.idNoPlaceholder')"
          />
        </div>
      </div>
    </div>

    <div class="weui-btn_area">
      <button type="button" class="weui-btn weui-btn_primary" @click="doQuery">{{ t('graduateExam.search') }}</button>
    </div>
    <p class="kaoyan-wish">{{ t('graduateExam.wish') }}</p>

    <div class="weui-cells__title">{{ t('graduateExam.altEntry') }}</div>
    <div class="weui-cells">
      <a
        class="weui-cell weui-cell_access"
        href="https://yz.chsi.com.cn/apply/cjcxa/"
        target="_blank"
        rel="noopener noreferrer"
      >
        <div class="weui-cell__bd">
          <p>{{ t('graduateExam.chsiLink') }}</p>
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
  color: var(--color-primary);
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
