<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '../../utils/request'

const router = useRouter()
const info = ref(null)
const loading = ref(false)
const submitLoading = ref(false)
const toastMsg = ref('')
const showToast = ref(false)
let toastTimer = null

function goBack() {
  router.back()
}

function onReportLoss() {
  if (!window.confirm('确定要挂失校园卡吗？')) return
  submitLoading.value = true
  request.post('/card/report_loss')
    .then(() => {
      setTimeout(() => {
        submitLoading.value = false
        toastMsg.value = '挂失成功'
        showToast.value = true
        if (toastTimer) clearTimeout(toastTimer)
        toastTimer = setTimeout(() => {
          showToast.value = false
        }, 2000)
      }, 1500)
    })
    .catch(() => {
      submitLoading.value = false
    })
}

onMounted(() => {
  loading.value = true
  request.get('/card/info')
    .then((res) => {
      loading.value = false
      if (res && (res.name || res.studentId || res.cardNo != null)) {
        info.value = res
      } else if (res && res.data) {
        info.value = res.data
      } else {
        info.value = {}
      }
    })
    .catch(() => {
      loading.value = false
      info.value = {}
    })
})
</script>

<template>
  <div class="card-info-page">
    <template v-if="loading">
      <div class="weui-mask_transparent" aria-hidden="true"></div>
      <div class="weui-toast__wrp">
        <div class="weui-toast">
          <span class="weui-primary-loading weui-icon_toast" aria-label="加载中"></span>
          <p class="weui-toast__content">加载中</p>
        </div>
      </div>
    </template>
    <template v-if="submitLoading">
      <div class="weui-mask_transparent" aria-hidden="true"></div>
      <div class="weui-toast__wrp">
        <div class="weui-toast">
          <span class="weui-primary-loading weui-icon_toast" aria-label="提交中"></span>
          <p class="weui-toast__content">提交中</p>
        </div>
      </div>
    </template>
    <div v-show="showToast" class="weui-toast__wrp weui-toast__wrp--text">
      <div class="weui-toast weui-toast_text">
        <p class="weui-toast__content">{{ toastMsg }}</p>
      </div>
    </div>

    <div class="top-nav-bar">
      <div class="nav-btn-back" @click="goBack">返回</div>
    </div>

    <div class="page-header">
      <h1 class="page-title-green">我的校园卡</h1>
    </div>

    <template v-if="info">
      <div class="weui-cells__title">校园卡基本信息</div>
      <div class="weui-cells info-cells">
        <div class="weui-cell info-cell">
          <div class="weui-cell__bd"><span class="info-label">姓名</span></div>
          <div class="weui-cell__ft"><span class="info-value">{{ info.name || '—' }}</span></div>
        </div>
        <div class="weui-cell info-cell">
          <div class="weui-cell__bd"><span class="info-label">学号</span></div>
          <div class="weui-cell__ft"><span class="info-value">{{ info.studentId || '—' }}</span></div>
        </div>
        <div class="weui-cell info-cell">
          <div class="weui-cell__bd"><span class="info-label">校园卡号</span></div>
          <div class="weui-cell__ft"><span class="info-value">{{ info.cardNo ?? '—' }}</span></div>
        </div>
      </div>

      <div class="weui-cells__title">校园卡余额</div>
      <div class="weui-cells info-cells">
        <div class="weui-cell info-cell">
          <div class="weui-cell__bd"><span class="info-label">校园卡余额</span></div>
          <div class="weui-cell__ft"><span class="info-value">{{ info.balance ?? '—' }}</span></div>
        </div>
        <div class="weui-cell info-cell">
          <div class="weui-cell__bd"><span class="info-label">校园卡过渡余额</span></div>
          <div class="weui-cell__ft"><span class="info-value">{{ info.transitionBalance ?? '—' }}</span></div>
        </div>
      </div>

      <div class="weui-cells__title">校园卡状态</div>
      <div class="weui-cells info-cells">
        <div class="weui-cell info-cell">
          <div class="weui-cell__bd"><span class="info-label">校园卡挂失状态</span></div>
          <div class="weui-cell__ft"><span class="info-value">{{ info.lossStatus ?? '—' }}</span></div>
        </div>
        <div class="weui-cell info-cell">
          <div class="weui-cell__bd"><span class="info-label">校园卡冻结状态</span></div>
          <div class="weui-cell__ft"><span class="info-value">{{ info.freezeStatus ?? '—' }}</span></div>
        </div>
      </div>
    </template>

    <div class="card-info-footer">
      <span class="card-info-footer-text">校园卡遗失？点击 </span>
      <a href="javascript:" class="card-info-footer-link" @click.prevent="onReportLoss">校园卡挂失</a>
    </div>
  </div>
</template>

<style scoped>
.card-info-page {
  background-color: #fff;
  min-height: 100vh;
  padding-bottom: 32px;
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
  padding: 0 0 6px;
  background-color: #fff;
}

.page-title-green {
  font-size: 34px;
  color: #09bb07;
  font-weight: 400;
  margin: 0;
  line-height: 1.2;
}

.card-info-page .weui-cells__title {
  padding: 12px 15px 8px;
  font-size: 14px;
  color: #888;
}

.card-info-page .info-cells {
  margin-top: 0;
}

.info-cell {
  position: relative;
  padding: 12px 15px !important;
  box-sizing: border-box;
}

.info-cell::after {
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

.info-cell:last-child::after {
  display: none !important;
}

.info-cell .weui-cell__bd {
  color: #888;
}

.info-label {
  color: #888;
}

.info-cell .weui-cell__ft {
  color: #000;
  text-align: right;
  flex-shrink: 0;
}

.info-value {
  color: #000;
}

.weui-toast__wrp--text {
  position: fixed;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  z-index: 6000;
}

.card-info-footer {
  margin-top: 32px;
  padding: 0 15px;
  font-size: 14px;
  color: #888;
  text-align: center;
}

.card-info-footer-link {
  color: #09bb07;
  text-decoration: none;
}
</style>
