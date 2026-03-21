<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getCetNumber, queryCetScore } from '@/api/cet'
import { showErrorTopTips } from '@/utils/toast.js'
import request from '@/utils/request'

const router = useRouter()

const examNumber = ref('')
const name = ref('')
const vcode = ref('')
const loading = ref(false)
const vcodeUrl = ref('')
const cetResult = ref(null)
const showResult = ref(false)

function getWeui() {
  return typeof window !== 'undefined' ? window.weui : null
}

function goBack() {
  router.back()
}

function onExamNumberInput(e) {
  const v = (e.target.value || '').replace(/\D/g, '').slice(0, 15)
  examNumber.value = v
}

function refreshVcode() {
  vcode.value = ''
  request.get('/cet/checkcode').then((res) => {
    const payload = res && res.data
    const base64 = typeof payload === 'string' ? payload : (payload && payload.data)
    vcodeUrl.value = base64 ? 'data:image/jpg;base64,' + base64 : ''
  }).catch(() => {
    vcodeUrl.value = ''
  })
}

function importNumber() {
  loading.value = true
  const weui = getWeui()
  const loadingInstance = weui && typeof weui.loading === 'function' ? weui.loading('导入中') : null
  getCetNumber().then((res) => {
    if (res && res.success && res.data) {
      const d = res.data
      examNumber.value = (d.number != null) ? String(d.number) : ''
      name.value = (d.name != null && d.name !== '') ? d.name : ''
      if (!examNumber.value && !name.value) {
        showErrorTopTips('你未保存准考证号')
      }
    } else {
      showErrorTopTips('你未保存准考证号')
    }
  }).catch(() => {
    // 错误由 request.js 全局拦截器统一提示，此处仅关闭 Loading
  }).finally(() => {
    loading.value = false
    if (loadingInstance && typeof loadingInstance.hide === 'function') {
      loadingInstance.hide()
    }
  })
}

function submitQuery() {
  const num = String(examNumber.value || '').trim()
  const n = String(name.value || '').trim()
  const code = String(vcode.value || '').trim()
  if (!num || !n) {
    showErrorTopTips('请填写准考证号和姓名')
    return
  }
  if (num.length !== 15) {
    showErrorTopTips('准考证号必须为15位')
    return
  }
  if (!code) {
    showErrorTopTips('请输入验证码')
    return
  }

  loading.value = true
  const weui = getWeui()
  const loadingInstance = weui && typeof weui.loading === 'function' ? weui.loading('正在查询...') : null
  queryCetScore(num, n, code).then((res) => {
    if (res && res.success && res.data) {
      cetResult.value = res.data
      showResult.value = true
      refreshVcode()
    }
  }).catch(() => {
    // 错误由 request.js 全局拦截器统一提示，此处仅关闭 Loading
    refreshVcode()
  }).finally(() => {
    loading.value = false
    if (loadingInstance && typeof loadingInstance.hide === 'function') {
      loadingInstance.hide()
    }
  })
}

function reQuery() {
  showResult.value = false
  cetResult.value = null
  name.value = ''
  examNumber.value = ''
  vcode.value = ''
  refreshVcode()
}

function onSaveNumber() {
  router.push('/cet/save').catch(() => {})
}

function openChsi() {
  window.open('https://www.chsi.com.cn/cet/', '_blank')
}

function openNeea() {
  window.open('https://cet.neea.edu.cn/cet/', '_blank')
}

onMounted(() => {
  refreshVcode()
})
</script>

<template>
  <div class="cet-page">
    <!-- 查询表单 -->
    <template v-if="!showResult">
      <div class="top-nav-bar">
        <div class="nav-btn-back" @click="goBack">返回</div>
      </div>
      <h1 class="page-title-green">四六级查询</h1>

      <div class="weui-cells weui-cells_form">
        <div class="weui-cell weui-cell_vcode">
          <div class="weui-cell__hd">
            <label class="weui-label" style="width: 65px; flex-shrink: 0;">考号</label>
          </div>
          <div class="weui-cell__bd" style="flex: 1; min-width: 0;">
            <input
              :value="examNumber"
              class="weui-input"
              type="text"
              inputmode="numeric"
              maxlength="15"
              placeholder="请输入15位准考证号"
              style="width: 100%; box-sizing: border-box;"
              @input="onExamNumberInput"
            />
          </div>
          <div class="weui-cell__ft" style="white-space: nowrap; flex-shrink: 0;">
            <button
              type="button"
              class="weui-vcode-btn"
              style="color: #576b95; font-size: 15px; padding: 0 15px; margin: 0; border: none; background: transparent; border-left: 1px solid #e5e5e5;"
              @click.prevent="importNumber"
            >
              导入考号
            </button>
          </div>
        </div>
        <div class="weui-cell">
          <div class="weui-cell__hd">
            <label class="weui-label" style="width: 65px; flex-shrink: 0;">姓名</label>
          </div>
          <div class="weui-cell__bd" style="flex: 1; min-width: 0;">
            <input
              v-model="name"
              class="weui-input"
              type="text"
              maxlength="20"
              placeholder="姓名超过3个字可只输入前3个"
              style="width: 100%; box-sizing: border-box;"
            />
          </div>
        </div>
        <div class="weui-cell">
          <div class="weui-cell__hd">
            <label class="weui-label" style="width: 65px; flex-shrink: 0;">验证码</label>
          </div>
          <div class="weui-cell__bd" style="flex: 1; min-width: 0;">
            <input
              v-model="vcode"
              class="weui-input"
              type="text"
              maxlength="10"
              placeholder="请输入验证码"
              style="width: 100%; box-sizing: border-box;"
            />
          </div>
          <div class="weui-cell__ft" style="white-space: nowrap; flex-shrink: 0; margin-left: 10px;">
            <img
              v-if="vcodeUrl"
              class="weui-vcode-img"
              :src="vcodeUrl"
              alt="验证码"
              @click="refreshVcode"
            />
            <span v-else class="weui-vcode-placeholder" @click="refreshVcode">点击获取</span>
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
    </template>

    <!-- 查询结果 -->
    <template v-else>
      <div class="top-nav-bar">
        <div class="nav-btn-back" @click="goBack">返回</div>
      </div>
      <div class="cet-result">
        <h1 class="page-title-green">查询结果</h1>
        <p class="cet-result-desc">成绩仅供参考，请以成绩单为准</p>
        <div class="weui-msg" v-if="cetResult">
          <div class="weui_text_area">
            <h2 class="weui-msg_title">{{ cetResult.name }}</h2>
            <p class="weui-msg_desc">考试类型：{{ cetResult.type }}</p>
            <p class="weui-msg_desc">考生学校：{{ cetResult.school }}</p>
            <br />
            <p class="weui-msg_desc cet-total">考试总分：{{ cetResult.totalScore }}</p>
            <p class="weui-msg_desc">听力分数：{{ cetResult.listeningScore }}</p>
            <p class="weui-msg_desc">阅读分数：{{ cetResult.readingScore }}</p>
            <p class="weui-msg_desc">写作翻译：{{ cetResult.writingAndTranslatingScore }}</p>
          </div>
          <br />
          <div class="weui_opr_area">
            <p class="weui-btn_area">
              <button type="button" class="weui-btn weui-btn_primary" @click="reQuery">重新查询</button>
              <button type="button" class="weui-btn weui-btn_default" @click="router.push('/')">返回主页</button>
            </p>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
.cet-page {
  background-color: var(--color-surface);
  min-height: 100vh;
  padding-bottom: 24px;
}

.top-nav-bar {
  display: flex;
  justify-content: flex-start;
  align-items: center;
  min-height: 44px;
  padding: 10px 15px;
  background-color: var(--color-surface);
  box-sizing: border-box;
}

.nav-btn-back {
  font-size: 16px;
  line-height: 24px;
  color: var(--color-text-tertiary);
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
  color: var(--color-text-tertiary);
}

.cet-page-desc__link {
  color: var(--color-primary);
  text-decoration: none;
}

.weui-vcode-img {
  width: 120px;
  height: 50px;
  display: block;
  cursor: pointer;
  background: var(--color-bg-secondary);
}

.weui-vcode-placeholder {
  font-size: 14px;
  color: var(--color-primary);
  cursor: pointer;
}

.cet-result {
  padding: 0 15px;
}

.cet-result-desc {
  text-align: center;
  font-size: 14px;
  color: var(--color-text-tertiary);
  margin: 0 0 20px 0;
}

.cet-result .weui-msg_title {
  margin-bottom: 8px;
}

.cet-result .weui-msg_desc {
  margin: 4px 0;
}

.cet-total {
  color: #e64340;
  font-weight: 500;
}

.cet-result .weui-btn_area {
  margin-top: 24px;
}

.cet-result .weui-btn_area .weui-btn {
  margin-bottom: 10px;
}
</style>
