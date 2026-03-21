<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getCetNumber, saveCetNumber } from '@/api/cet'
import { showErrorTopTips } from '@/utils/toast.js'

const router = useRouter()

const examNumber = ref('')
const name = ref('')
const saving = ref(false)

function getWeui() {
  return typeof window !== 'undefined' ? window.weui : null
}

function goBack() {
  router.back()
}

function submitSave() {
  const num = (examNumber.value || '').trim()
  const n = (name.value || '').trim()
  if (!num) {
    showErrorTopTips('请输入15位准考证号')
    return
  }
  if (num.length !== 15) {
    showErrorTopTips('准考证号必须为15位')
    return
  }
  saving.value = true
  saveCetNumber({ number: num, name: n }).then((res) => {
    if (res && res.success) {
      const weui = getWeui()
      if (weui && typeof weui.toast === 'function') {
        weui.toast('保存成功')
      }
      setTimeout(() => router.back(), 800)
    } else {
      showErrorTopTips(res && res.message ? res.message : '保存失败')
    }
  }).catch(() => {
    // 错误由 request.js 全局拦截器统一提示，此处仅关闭 Loading
  }).finally(() => {
    saving.value = false
  })
}

onMounted(() => {
  getCetNumber().then((res) => {
    if (res && res.success && res.data) {
      const d = res.data
      if (d.number != null) examNumber.value = String(d.number)
      if (d.name != null && d.name !== '') name.value = d.name
    }
  }).catch(() => {})
})
</script>

<template>
  <div class="cet-save-page">
    <template v-if="saving">
      <div class="weui-mask_transparent" aria-hidden="true"></div>
      <div class="weui-toast__wrp">
        <div class="weui-toast">
          <span class="weui-primary-loading weui-icon_toast" aria-label="加载中"></span>
          <p class="weui-toast__content">保存中</p>
        </div>
      </div>
    </template>

    <div class="top-nav-bar">
      <div class="nav-btn-back" @click="goBack">返回</div>
    </div>
    <h1 class="page-title-green">保存考号</h1>

    <div class="weui-cells weui-cells_form">
      <div class="weui-cell">
        <div class="weui-cell__hd">
          <label class="weui-label">考号</label>
        </div>
        <div class="weui-cell__bd weui-cell_primary">
          <input
            v-model="examNumber"
            class="weui-input"
            type="text"
            inputmode="numeric"
            maxlength="15"
            placeholder="请输入15位准考证号"
          />
        </div>
      </div>
      <div class="weui-cell">
        <div class="weui-cell__hd">
          <label class="weui-label">姓名</label>
        </div>
        <div class="weui-cell__bd weui-cell_primary">
          <input
            v-model="name"
            class="weui-input"
            type="text"
            maxlength="20"
            placeholder="姓名超过3个字可只输入前3个"
          />
        </div>
      </div>
    </div>

    <div class="weui-btn_area">
      <button type="button" class="weui-btn weui-btn_primary" @click="submitSave">保存</button>
    </div>
  </div>
</template>

<style scoped>
.cet-save-page {
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

.cet-save-page .weui-cells_form {
  margin-top: 0;
}

.cet-save-page .weui-btn_area {
  margin-top: 24px;
  padding: 0 15px;
}

.cet-save-page .weui-btn_area .weui-btn {
  width: 100%;
}
</style>
