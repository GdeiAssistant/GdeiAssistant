<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request'

const router = useRouter()
const isDirectSubmit = ref(false)
const loading = ref(false)
const toastMsg = ref('')
const showToast = ref(false)
const showEvaluateConfirmDialog = ref(false)
let toastTimer = null

function goBack() {
  router.back()
}

function doEvaluate() {
  showEvaluateConfirmDialog.value = true
}

function closeEvaluateConfirmDialog() {
  showEvaluateConfirmDialog.value = false
}

function confirmEvaluate() {
  closeEvaluateConfirmDialog()
  loading.value = true
  const formData = { directSubmit: isDirectSubmit.value }
  request.post('/evaluate/submit', formData)
    .then((res) => {
      loading.value = false
      toastMsg.value = '评价提交成功！'
      showToast.value = true
      if (toastTimer) clearTimeout(toastTimer)
      toastTimer = setTimeout(() => {
        showToast.value = false
      }, 2500)
    })
    .catch(() => {
      loading.value = false
    })
}
</script>

<template>
  <div class="evaluate-page">
    <template v-if="loading">
      <div class="weui-mask_transparent" aria-hidden="true"></div>
      <div class="weui-toast__wrp">
        <div class="weui-toast">
          <span class="weui-primary-loading weui-icon_toast" aria-label="自动评教中"></span>
          <p class="weui-toast__content">自动评教中...</p>
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
      <h1 class="page-title-green">教学质量评价</h1>
    </div>

    <div class="weui-cells weui-cells_form">
      <div class="weui-cell weui-cell_switch">
        <div class="weui-cell__bd">直接提交评教信息</div>
        <div class="weui-cell__ft">
          <input class="weui-switch" type="checkbox" v-model="isDirectSubmit" />
        </div>
      </div>
    </div>

    <div class="weui-btn_area">
      <button type="button" class="weui-btn weui-btn_primary" @click="doEvaluate">一键评教</button>
    </div>

    <p class="evaluate-tip">注意：评教信息提交后，将不能再作修改。</p>

    <!-- 一键评教确认弹窗（WeUI 风格） -->
    <div v-if="showEvaluateConfirmDialog" class="weui-mask" @click="closeEvaluateConfirmDialog"></div>
    <div v-if="showEvaluateConfirmDialog" class="weui-dialog weui-dialog--confirm">
      <div class="weui-dialog__hd">
        <strong class="weui-dialog__title">提示</strong>
      </div>
      <div class="weui-dialog__bd">确定要进行一键评教吗？此操作不可逆。</div>
      <div class="weui-dialog__ft">
        <a href="javascript:" class="weui-dialog__btn weui-dialog__btn_default" @click.prevent="closeEvaluateConfirmDialog">取消</a>
        <a href="javascript:" class="weui-dialog__btn weui-dialog__btn_primary" @click.prevent="confirmEvaluate">确定</a>
      </div>
    </div>
  </div>
</template>

<style scoped>
.evaluate-page {
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
  padding: 0 0 20px;
  background-color: #fff;
}

.page-title-green {
  font-size: 34px;
  color: var(--color-primary);
  font-weight: 400;
  margin: 0;
  line-height: 1.2;
}

.evaluate-page .weui-cells_form {
  margin-top: 0;
}

.evaluate-page .weui-cell__bd {
  font-size: 17px;
  color: #000;
}

.evaluate-page .weui-btn_area {
  margin-top: 24px;
  padding: 0 15px;
}

.evaluate-page .weui-btn_area .weui-btn {
  width: 100%;
}

.evaluate-tip {
  margin-top: 16px;
  padding: 0 15px;
  font-size: 13px;
  color: #999;
  text-align: center;
  line-height: 1.5;
}

.weui-switch {
  appearance: none;
  width: 51px;
  height: 31px;
  border: 1px solid #dfdfdf;
  border-radius: 31px;
  background-color: #dfdfdf;
  outline: none;
  cursor: pointer;
  position: relative;
  transition: background-color 0.3s, border-color 0.3s;
}
.weui-switch::after {
  content: " ";
  position: absolute;
  top: 2px;
  left: 2px;
  width: 25px;
  height: 25px;
  border-radius: 50%;
  background-color: #fff;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.4);
  transition: transform 0.3s;
}
.weui-switch:checked {
  border-color: var(--color-primary);
  background-color: var(--color-primary);
}
.weui-switch:checked::after {
  transform: translateX(20px);
}

.weui-toast__wrp--text {
  position: fixed;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  z-index: 6000;
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

.weui-dialog--confirm.weui-dialog {
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

.weui-dialog--confirm .weui-dialog__hd {
  padding: 24px 20px 12px;
  text-align: center;
}

.weui-dialog--confirm .weui-dialog__title {
  font-size: 17px;
  color: #333;
}

.weui-dialog--confirm .weui-dialog__bd {
  padding: 12px 20px 24px;
  font-size: 15px;
  color: #666;
  text-align: center;
  line-height: 1.5;
}

.weui-dialog--confirm .weui-dialog__ft {
  display: flex;
  border-top: 1px solid #e5e5e5;
}

.weui-dialog--confirm .weui-dialog__btn {
  flex: 1;
  padding: 14px;
  text-align: center;
  color: #333;
  text-decoration: none;
  font-size: 17px;
}

.weui-dialog--confirm .weui-dialog__btn_default {
  color: #888;
  border-right: 1px solid #e5e5e5;
}

.weui-dialog--confirm .weui-dialog__btn_primary {
  color: var(--color-primary);
}
</style>
