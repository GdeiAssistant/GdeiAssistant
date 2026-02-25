<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { queryCardInfo, reportCardLost } from '@/api/card'
import { showErrorTopTips } from '@/utils/toast'

const router = useRouter()
const info = ref(null)
const loading = ref(false)
const submitLoading = ref(false)
const toastMsg = ref('')
const showToast = ref(false)
const showPasswordDialog = ref(false)
const verifyPassword = ref('')
let toastTimer = null

function goBack() {
  router.back()
}

function onReportLoss() {
  verifyPassword.value = ''
  showPasswordDialog.value = true
}

function closePasswordDialog() {
  showPasswordDialog.value = false
  verifyPassword.value = ''
}

function confirmReportLoss() {
  const pwd = (verifyPassword.value || '').trim()
  if (!pwd) {
    showErrorTopTips('密码不能为空')
    return
  }
  closePasswordDialog()
  submitLoading.value = true
  reportCardLost(pwd)
    .then(() => {
      submitLoading.value = false
      toastMsg.value = '挂失成功'
      showToast.value = true
      if (toastTimer) clearTimeout(toastTimer)
      toastTimer = setTimeout(() => { showToast.value = false }, 2000)
      info.value = { ...info.value, cardLostState: '已挂失' }
    })
    .catch(() => {
      submitLoading.value = false
    })
}

onMounted(() => {
  loading.value = true
  queryCardInfo()
    .then((res) => {
      const body = res && res.data ? res.data : res
      const data =
        body && typeof body === 'object'
          ? body.data !== undefined
            ? body.data
            : body
          : null
      info.value = data || {}
    })
    .catch(() => {
      info.value = {}
    })
    .finally(() => {
      loading.value = false
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
          <div class="weui-cell__ft"><span class="info-value">{{ info.number ?? '—' }}</span></div>
        </div>
        <div class="weui-cell info-cell">
          <div class="weui-cell__bd"><span class="info-label">校园卡号</span></div>
          <div class="weui-cell__ft"><span class="info-value">{{ info.cardNumber ?? '—' }}</span></div>
        </div>
      </div>

      <div class="weui-cells__title">校园卡余额</div>
      <div class="weui-cells info-cells">
        <div class="weui-cell info-cell">
          <div class="weui-cell__bd"><span class="info-label">校园卡余额</span></div>
          <div class="weui-cell__ft"><span class="info-value">{{ info.cardBalance ?? '—' }}</span></div>
        </div>
        <div class="weui-cell info-cell">
          <div class="weui-cell__bd"><span class="info-label">校园卡过渡余额</span></div>
          <div class="weui-cell__ft"><span class="info-value">{{ info.cardInterimBalance ?? '—' }}</span></div>
        </div>
      </div>

      <div class="weui-cells__title">校园卡状态</div>
      <div class="weui-cells info-cells">
        <div class="weui-cell info-cell">
          <div class="weui-cell__bd"><span class="info-label">校园卡挂失状态</span></div>
          <div class="weui-cell__ft"><span class="info-value">{{ info.cardLostState ?? '—' }}</span></div>
        </div>
        <div class="weui-cell info-cell">
          <div class="weui-cell__bd"><span class="info-label">校园卡冻结状态</span></div>
          <div class="weui-cell__ft"><span class="info-value">{{ info.cardFreezeState ?? '—' }}</span></div>
        </div>
      </div>
    </template>

    <div class="card-info-footer">
      <span class="card-info-footer-text">校园卡遗失？点击 </span>
      <a href="javascript:" class="card-info-footer-link" @click.prevent="onReportLoss">校园卡挂失</a>
    </div>

    <!-- 挂失验证弹窗：直接输入密码，无前置确认 -->
    <div v-if="showPasswordDialog" class="weui-mask" @click="closePasswordDialog"></div>
    <div v-if="showPasswordDialog" class="weui-dialog weui-dialog--password">
      <div class="weui-dialog__hd">
        <strong class="weui-dialog__title">挂失校园卡验证</strong>
      </div>
      <div class="weui-dialog__bd">
        <p class="weui-dialog__tip">请输入查询密码进行验证</p>
        <input
          v-model="verifyPassword"
          type="password"
          class="weui-input weui-dialog__input"
          placeholder="校园卡查询密码"
          maxlength="20"
          @keyup.enter="confirmReportLoss"
        />
      </div>
      <div class="weui-dialog__ft">
        <a href="javascript:" class="weui-dialog__btn weui-dialog__btn_default" @click="closePasswordDialog">取消</a>
        <a href="javascript:" class="weui-dialog__btn weui-dialog__btn_primary" @click="confirmReportLoss">确定</a>
      </div>
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

.weui-mask {
  position: fixed;
  z-index: 1000;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
}

.weui-dialog--password.weui-dialog {
  position: fixed;
  z-index: 5000;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background: #fff;
  border-radius: 12px;
  width: 320px;
  overflow: hidden;
}

.weui-dialog__hd {
  padding: 24px 20px 12px;
  text-align: center;
}

.weui-dialog__title {
  font-size: 17px;
  color: #333;
}

.weui-dialog__bd {
  padding: 12px 20px 20px;
}

.weui-dialog__tip {
  margin: 0 0 12px;
  font-size: 14px;
  color: #666;
  line-height: 1.5;
}

.weui-dialog__input {
  width: 100%;
  padding: 12px;
  font-size: 15px;
  border: 1px solid #e5e5e5;
  border-radius: 6px;
  box-sizing: border-box;
}

.weui-dialog__ft {
  display: flex;
  border-top: 1px solid #e5e5e5;
}

.weui-dialog__btn {
  flex: 1;
  padding: 14px;
  text-align: center;
  color: #333;
  text-decoration: none;
  font-size: 17px;
}

.weui-dialog__btn_primary {
  color: #09bb07;
  border-left: 1px solid #e5e5e5;
}

.weui-dialog__btn_default {
  color: #888;
}
</style>

