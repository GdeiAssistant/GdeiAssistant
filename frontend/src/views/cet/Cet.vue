<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '../../utils/request'

const router = useRouter()

const examNumber = ref('')
const name = ref('')
const vcode = ref('')
const loading = ref(false)
const loadingText = ref('查询中')
const topTipsMessage = ref('')
const showTopTips = ref(false)
const vcodeUrl = ref('')

function goBack() {
  router.back()
}

function showWeuiTopTips(message) {
  topTipsMessage.value = message
  showTopTips.value = true
  setTimeout(() => {
    showTopTips.value = false
  }, 2000)
}

function refreshVcode() {
  vcode.value = ''
  request.get('/cet/vcode').then((res) => {
    if (res && res.data) {
      vcodeUrl.value = res.data
    } else {
      vcodeUrl.value = 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="120" height="50"%3E%3Crect fill="%23f0f0f0" width="120" height="50"/%3E%3Ctext x="60" y="28" text-anchor="middle" fill="%23999" font-size="12"%3E验证码%3C/text%3E%3C/svg%3E'
    }
  }).catch(() => {
    vcodeUrl.value = 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="120" height="50"%3E%3Crect fill="%23f0f0f0" width="120" height="50"/%3E%3Ctext x="60" y="28" text-anchor="middle" fill="%23999" font-size="12"%3E验证码%3C/text%3E%3C/svg%3E'
  })
}

function importNumber() {
  loadingText.value = '导入中'
  loading.value = true
  request.get('/cet/number').then((res) => {
    loading.value = false
    if (res && res.success && res.data) {
      examNumber.value = res.data.number != null ? String(res.data.number) : ''
      name.value = res.data.name != null ? String(res.data.name) : ''
      if (!examNumber.value && !name.value) {
        showWeuiTopTips('你未保存准考证号')
      }
    } else {
      showWeuiTopTips(res && res.message ? res.message : '你未保存准考证号')
    }
  }).catch((err) => {
    loading.value = false
    showWeuiTopTips(err && err.message ? err.message : '你未保存准考证号')
  })
}

function submitQuery() {
  const num = (examNumber.value || '').trim()
  const n = (name.value || '').trim()
  const code = (vcode.value || '').trim()
  if (!num || !n || !code) {
    showWeuiTopTips('请将信息填写完整！')
    return
  }
  if (num.length !== 15) {
    showWeuiTopTips('准考证号长度不正确！')
    return
  }
  loadingText.value = '查询中'
  loading.value = true
  request.post('/cet/query', {
    number: num,
    name: n,
    checkcode: code
  }).then((res) => {
    loading.value = false
    if (res && res.success) {
      refreshVcode()
      console.log('校验通过，准备跳转结果页', res)
      showWeuiTopTips('查询成功（结果页后续接入）')
    } else {
      refreshVcode()
      showWeuiTopTips(res && res.message ? res.message : '查询失败')
    }
  }).catch(() => {
    loading.value = false
    refreshVcode()
  })
}

function onSaveNumber() {
  router.push('/cet/save').catch(() => {})
}

function openChsi() {
  window.open('http://www.chsi.com.cn/cet/', '_blank')
}

function openNeea() {
  window.open('http://cet.neea.edu.cn/cet/', '_blank')
}

onMounted(() => {
  refreshVcode()
})
</script>

<template>
  <div class="cet-page">
    <template v-if="loading">
      <div class="weui-mask_transparent" aria-hidden="true"></div>
      <div class="weui-toast__wrp">
        <div class="weui-toast">
          <span class="weui-primary-loading weui-icon_toast" aria-label="加载中"></span>
          <p class="weui-toast__content">{{ loadingText }}</p>
        </div>
      </div>
    </template>

    <div class="top-nav-bar">
      <div class="nav-btn-back" @click="goBack">返回</div>
    </div>
    <h1 class="page-title-green">四六级查询</h1>

    <div class="weui-cells weui-cells_form">
      <div class="weui-cell weui-cell_vcode">
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
        <div class="weui-cell__ft">
          <button type="button" class="weui-vcode-btn" @click="importNumber">导入考号</button>
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
      <div class="weui-cell weui-cell_vcode">
        <div class="weui-cell__hd">
          <label class="weui-label">验证码</label>
        </div>
        <div class="weui-cell__bd weui-cell_primary">
          <input
            v-model="vcode"
            class="weui-input"
            type="text"
            maxlength="10"
            placeholder="请输入验证码"
          />
        </div>
        <div class="weui-cell__ft">
          <img
            class="weui-vcode-img"
            :src="vcodeUrl"
            alt="验证码"
            @click="refreshVcode"
          />
        </div>
      </div>
    </div>

    <div class="weui-btn_area">
      <button type="button" class="weui-btn weui-btn_primary" @click="submitQuery">查询</button>
    </div>

    <p class="cet-page-desc">
      担心遗忘准考证号？点击
      <a href="javascript:" class="cet-page-desc__link" @click.prevent="onSaveNumber">保存考号</a>
    </p>

    <div v-show="showTopTips" class="weui-toptips weui_warn" role="alert">
      {{ topTipsMessage }}
    </div>

    <div class="weui-cells__title">备用查询入口</div>
    <div class="weui-cells">
      <a href="javascript:" class="weui-cell weui-cell_access" @click.prevent="openChsi">
        <div class="weui-cell__bd">
          <p>学信网四六级查分</p>
        </div>
        <div class="weui-cell__ft"></div>
      </a>
      <a href="javascript:" class="weui-cell weui-cell_access" @click.prevent="openNeea">
        <div class="weui-cell__bd">
          <p>中国教育考试网查询</p>
        </div>
        <div class="weui-cell__ft"></div>
      </a>
    </div>
  </div>
</template>

<style scoped>
.cet-page {
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

.cet-page .weui-cells_form {
  margin-top: 0;
}

.cet-page .weui-btn_area {
  margin-top: 24px;
  padding: 0 15px;
}

.cet-page .weui-btn_area .weui-btn {
  width: 100%;
}

.cet-page-desc {
  margin-top: 25px;
  text-align: center;
  font-size: 14px;
  color: #888;
}

.cet-page-desc__link {
  color: #09bb07;
  text-decoration: none;
}

.weui-toptips {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  padding: 10px;
  font-size: 14px;
  text-align: center;
  color: #fff;
  background-color: #e64340;
  z-index: 5000;
}

.weui-vcode-img {
  width: 120px;
  height: 50px;
  display: block;
  cursor: pointer;
  background: #f5f5f5;
}
</style>
