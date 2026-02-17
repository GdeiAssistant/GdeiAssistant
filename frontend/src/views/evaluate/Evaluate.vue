<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import request from '../../utils/request'

const router = useRouter()
const isDirectSubmit = ref(false)
const loading = ref(false)
const toastMsg = ref('')
const showToast = ref(false)
let toastTimer = null

function goBack() {
  router.back()
}

function doEvaluate() {
  if (!window.confirm('确定要进行一键评教吗？此操作不可逆。')) return
  loading.value = true
  request.post('/evaluate', { directSubmit: isDirectSubmit.value })
    .then(() => {
      setTimeout(() => {
        loading.value = false
        toastMsg.value = '评教完成'
        showToast.value = true
        if (toastTimer) clearTimeout(toastTimer)
        toastTimer = setTimeout(() => {
          showToast.value = false
        }, 2000)
      }, 1500)
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
  color: #09bb07;
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

/* WEUI 风格开关 */
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
  border-color: #09bb07;
  background-color: #09bb07;
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
</style>
