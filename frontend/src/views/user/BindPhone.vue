<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '../../utils/request'

const router = useRouter()

// 当前已绑定手机号（mock 初始值，实际从接口获取）
const currentPhone = ref('138****1234')
const boundCountryCode = ref('+86')
const isEditing = ref(false)

// 区号相关
const countryCodes = ref([])
const currentCountryCode = ref('+86')

const formPhone = ref('')
const vcode = ref('')
const countdown = ref(0)
const isBinding = ref(false)
const sending = ref(false)
const showUnbindDialog = ref(false)
const isUnbinding = ref(false)
let timerId = null

function showToast(message) {
  const toast = document.createElement('div')
  toast.style.cssText =
    'position:fixed;left:50%;top:50%;transform:translate(-50%,-50%);background:rgba(0,0,0,0.75);color:#fff;padding:12px 22px;border-radius:6px;z-index:9999;font-size:14px;max-width:80%;text-align:center;'
  toast.textContent = message
  document.body.appendChild(toast)
  setTimeout(() => {
    if (toast.parentNode) document.body.removeChild(toast)
  }, 2000)
}

const codeButtonText = computed(() => {
  if (countdown.value > 0) {
    return `${countdown.value}s 后重试`
  }
  return '获取验证码'
})

const canSendCode = computed(() => countdown.value === 0 && !sending.value)

// 工具函数：将 ISO 国家二字码转换为国旗 Emoji
function getFlagEmoji(isoCode) {
  if (!isoCode || isoCode.length !== 2) return ''
  const codePoints = isoCode
    .toUpperCase()
    .split('')
    .map(char => 127397 + char.charCodeAt())
  return String.fromCodePoint(...codePoints)
}

function validatePhone(value, countryCode) {
  if (countryCode === '+86') {
    // 中国大陆：严格校验11位手机号
    const pattern = /^1[3-9]\d{9}$/
    return pattern.test(value)
  } else {
    // 国际手机号：仅限数字，长度5-15位
    const pattern = /^\d{5,15}$/
    return pattern.test(value)
  }
}

function maskPhone(phone) {
  if (!phone) return ''
  if (phone.length === 11) {
    // 11位手机号（中国大陆）
    return `${phone.substring(0, 3)}****${phone.substring(7)}`
  } else if (phone.length >= 5) {
    // 国际手机号：显示前3位和后2位
    return `${phone.substring(0, 3)}****${phone.substring(phone.length - 2)}`
  }
  return phone
}

async function loadCountryCodes() {
  try {
    const response = await fetch('/country_codes.xml')
    const text = await response.text()
    const parser = new DOMParser()
    const xmlDoc = parser.parseFromString(text, 'text/xml')
    
    const items = xmlDoc.getElementsByTagName('Attribution')
    const codes = []
    for (let i = 0; i < items.length; i++) {
      const code = items[i].getAttribute('Code')
      const name = items[i].getAttribute('Name')
      const flag = items[i].getAttribute('Flag') || ''
      
      if (code) {
        // 直接使用 XML 中的 Flag 属性（已经是 Emoji）
        // 如果需要 ISO 码，可以从 Flag Emoji 反向提取（用于其他用途）
        let iso = ''
        let emoji = flag
        
        // 如果 XML 中没有 Flag，尝试从国家名称推断 ISO 码并生成 Emoji（兜底）
        if (!emoji && name) {
          const nameToISO = {
            '中国': 'CN',
            '美国': 'US',
            '日本': 'JP',
            '英国': 'GB',
            '韩国': 'KR',
            '法国': 'FR',
            '德国': 'DE',
            '加拿大': 'CA',
            '澳大利亚': 'AU',
            '中国台湾': 'TW',
            '中国香港': 'HK',
            '中国澳门': 'MO'
          }
          const mappedISO = nameToISO[name] || ''
          if (mappedISO) {
            iso = mappedISO
            emoji = getFlagEmoji(mappedISO)
          }
        }
        
        codes.push({
          iso: iso,
          code: `+${code}`,
          emoji: emoji || '',
          name: name || ''
        })
      }
    }
    
    // 严格保持 XML 原始顺序，不进行任何排序
    if (codes.length > 0) {
      countryCodes.value = codes
    } else {
      // 降级兜底方案
      countryCodes.value = [{ 
        iso: 'CN',
        code: '+86', 
        emoji: getFlagEmoji('CN'),
        name: '中国大陆' 
      }]
    }
  } catch (error) {
    console.error('加载区号 XML 失败', error)
    // 降级兜底方案
    countryCodes.value = [{ 
      iso: 'CN',
      code: '+86', 
      emoji: getFlagEmoji('CN'),
      name: '中国大陆' 
    }]
  }
}

async function handleSendCode() {
  if (!canSendCode.value) return
  if (!validatePhone(formPhone.value, currentCountryCode.value)) {
    if (currentCountryCode.value === '+86') {
      showToast('请输入正确的国内手机号')
    } else {
      showToast('请输入正确的国际手机号')
    }
    return
  }

  sending.value = true
  try {
    await request.post('/api/user/send-phone-code', {
      phone: formPhone.value,
      countryCode: currentCountryCode.value
    })
    countdown.value = 60
    timerId = setInterval(() => {
      if (countdown.value > 0) {
        countdown.value -= 1
      } else if (timerId) {
        clearInterval(timerId)
        timerId = null
      }
    }, 1000)
    showToast('验证码已发送，请查看短信')
  } catch (e) {
    showToast('发送验证码失败，请稍后重试')
  } finally {
    sending.value = false
  }
}

async function handleSubmit() {
  if (isBinding.value) return
  if (!validatePhone(formPhone.value, currentCountryCode.value)) {
    if (currentCountryCode.value === '+86') {
      showToast('请输入正确的国内手机号')
    } else {
      showToast('请输入正确的国际手机号')
    }
    return
  }
  if (!vcode.value) {
    showToast('请输入验证码')
    return
  }

  isBinding.value = true
  try {
    await request.post('/api/user/bind-phone', {
      phone: formPhone.value,
      countryCode: currentCountryCode.value,
      code: vcode.value
    })
    currentPhone.value = maskPhone(formPhone.value)
    boundCountryCode.value = currentCountryCode.value
    showToast('绑定成功')
    // 绑定成功后返回状态页
    isEditing.value = false
  } catch (e) {
    showToast('绑定失败，请稍后重试')
  } finally {
    isBinding.value = false
  }
}

function startEdit() {
  isEditing.value = true
  formPhone.value = ''
  vcode.value = ''
  currentCountryCode.value = boundCountryCode.value || '+86'
}

function startBind() {
  isEditing.value = true
  formPhone.value = ''
  vcode.value = ''
  currentCountryCode.value = '+86'
}

function cancelEdit() {
  isEditing.value = false
  formPhone.value = ''
  vcode.value = ''
}

function openUnbindDialog() {
  showUnbindDialog.value = true
}

function closeUnbindDialog() {
  if (isUnbinding.value) return
  showUnbindDialog.value = false
}

async function confirmUnbind() {
  if (isUnbinding.value) return
  isUnbinding.value = true
  try {
    await request.post('/api/user/unbind-phone')
    currentPhone.value = ''
    boundCountryCode.value = '+86'
    formPhone.value = ''
    vcode.value = ''
    isEditing.value = false
    showToast('已解除绑定')
    showUnbindDialog.value = false
  } catch (e) {
    showToast('解除绑定失败，请稍后重试')
  } finally {
    isUnbinding.value = false
  }
}

onMounted(async () => {
  // 加载区号列表
  await loadCountryCodes()
  
  // 加载绑定状态
  try {
    const res = await request.get('/api/user/phone-status')
    const data = res && (res.data || res)
    if (data && typeof data === 'object') {
      if (data.phone) {
        currentPhone.value = maskPhone(data.phone)
      }
      if (data.countryCode) {
        boundCountryCode.value = data.countryCode
      }
    } else if (typeof data === 'string') {
      currentPhone.value = maskPhone(data)
      boundCountryCode.value = '+86'
    }
  } catch (e) {
    // ignore, use default mock
  }
})

onUnmounted(() => {
  if (timerId) {
    clearInterval(timerId)
    timerId = null
  }
})
</script>

<template>
  <div class="bind-phone-page">
    <!-- 统一头部 -->
    <div class="phone-header unified-header">
      <span class="phone-header__back" @click="router.back()">返回</span>
      <h1 class="phone-header__title">绑定手机</h1>
      <span class="phone-header__placeholder"></span>
    </div>

    <div class="phone-content">
      <!-- 已绑定状态视图 -->
      <div v-if="currentPhone && !isEditing" class="status-view">
        <div class="weui-msg">
          <div class="weui-msg__icon-area">
            <i class="weui-icon-success weui-icon_msg"></i>
          </div>
          <div class="weui-msg__text-area">
            <h2 class="weui-msg__title">已绑定手机</h2>
            <p class="weui-msg__desc">您当前绑定的手机号为：{{ boundCountryCode }} {{ currentPhone }}</p>
          </div>
          <div class="weui-msg__opr-area">
            <p class="weui-btn-area">
              <a href="javascript:;" class="weui-btn weui-btn_primary" @click.prevent="startEdit">更换手机</a>
              <a href="javascript:;" class="weui-btn weui-btn_default" @click.prevent="openUnbindDialog">解除绑定</a>
            </p>
          </div>
        </div>
      </div>

      <!-- 未绑定状态视图 -->
      <div v-else-if="!currentPhone && !isEditing" class="status-view">
        <div class="weui-msg">
          <div class="weui-msg__icon-area">
            <i class="weui-icon-info weui-icon_msg"></i>
          </div>
          <div class="weui-msg__text-area">
            <h2 class="weui-msg__title">未绑定手机号</h2>
            <p class="weui-msg__desc">您尚未绑定手机号码，绑定后可提升账号安全性及用于找回密码。</p>
          </div>
          <div class="weui-msg__opr-area">
            <p class="weui-btn-area">
              <a href="javascript:;" class="weui-btn weui-btn_primary" @click.prevent="startBind">立即绑定</a>
            </p>
          </div>
        </div>
      </div>

      <!-- 表单视图（修改中/绑定中） -->
      <div v-else class="edit-view">
        <p v-if="currentPhone" class="edit-tip">请输入新的手机号码进行绑定。</p>

        <!-- 表单区域 -->
        <div class="weui-cells weui-cells_form">
          <!-- 手机号输入（带区号选择） -->
          <div class="weui-cell weui-cell_select weui-cell_select-before">
            <div class="weui-cell__hd">
              <select class="weui-select" v-model="currentCountryCode">
                <option v-for="(item, index) in countryCodes" :key="index" :value="item.code">
                  {{ item.emoji }} {{ item.code }}
                </option>
              </select>
            </div>
            <div class="weui-cell__bd">
              <input
                v-model="formPhone"
                class="weui-input"
                type="tel"
                :maxlength="currentCountryCode === '+86' ? 11 : 15"
                :placeholder="currentPhone ? '请输入新的手机号' : '请输入您的手机号'"
              />
            </div>
          </div>

          <!-- 验证码输入 -->
          <div class="weui-cell weui-cell_vcode">
            <div class="weui-cell__hd">
              <label class="weui-label">验证码</label>
            </div>
            <div class="weui-cell__bd">
              <input
                v-model="vcode"
                class="weui-input"
                type="number"
                inputmode="numeric"
                placeholder="请输入手机验证码"
              />
            </div>
            <div class="weui-cell__ft">
              <button
                type="button"
                class="weui-vcode-btn"
                :class="{ 'weui-vcode-btn--disabled': !canSendCode }"
                :disabled="!canSendCode"
                @click="handleSendCode"
              >
                {{ codeButtonText }}
              </button>
            </div>
          </div>
        </div>

        <!-- 底部按钮 -->
        <div class="weui-btn-area">
          <a
            href="javascript:;"
            class="weui-btn weui-btn_primary"
            :class="{ 'weui-btn_disabled': isBinding, 'weui-btn_loading': isBinding }"
            @click.prevent="handleSubmit"
          >
            <span v-if="isBinding" class="btn-loading">
              <span class="weui-loading"></span>
              <span class="btn-text">绑定中...</span>
            </span>
            <span v-else>确认绑定</span>
          </a>
        </div>

        <div v-if="currentPhone" class="weui-btn-area edit-cancel-area">
          <a
            href="javascript:;"
            class="weui-btn weui-btn_default"
            @click.prevent="cancelEdit"
          >
            取消
          </a>
        </div>
      </div>
    </div>

    <!-- 解绑二次确认弹窗 -->
    <div v-if="showUnbindDialog" class="weui-mask" @click="closeUnbindDialog"></div>
    <div v-if="showUnbindDialog" class="weui-dialog">
      <div class="weui-dialog__hd">
        <strong class="weui-dialog__title">解除绑定</strong>
      </div>
      <div class="weui-dialog__bd">
        确定要解除绑定该手机号吗？解除后将无法使用该手机号找回账号。
      </div>
      <div class="weui-dialog__ft">
        <a
          href="javascript:;"
          class="weui-dialog__btn weui-dialog__btn_default"
          @click.prevent="closeUnbindDialog"
        >取消</a>
        <a
          href="javascript:;"
          class="weui-dialog__btn weui-dialog__btn_primary"
          @click.prevent="confirmUnbind"
        >确认解绑</a>
      </div>
    </div>
  </div>
</template>

<style scoped>
.bind-phone-page {
  background: #f8f8f8;
  min-height: 100vh;
}

.phone-header.unified-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 44px;
  padding: 0 12px;
  background: #ffffff;
  border-bottom: 1px solid #e5e5e5;
}
.phone-header__back {
  font-size: 14px;
  color: #333;
  cursor: pointer;
  min-width: 48px;
}
.phone-header__title {
  flex: 1;
  text-align: center;
  font-size: 16px;
  font-weight: 500;
  margin: 0;
  color: #333;
}
.phone-header__placeholder {
  min-width: 48px;
}

.phone-content {
  padding-top: 10px;
}

.status-view {
  padding: 20px 15px 0;
}

.weui-msg__desc {
  color: #666;
}

/* 修复已绑定视图的按钮间距 */
.weui-msg__opr-area .weui-btn-area .weui-btn + .weui-btn {
  margin-top: 16px !important;
}

.edit-view {
  padding-top: 0;
}

.edit-tip {
  margin: 12px 15px 0;
  font-size: 13px;
  color: #999;
}

.weui-cells {
  margin-top: 0;
}

.weui-cell {
  padding: 16px !important;
}

/* 修复区号选择器的超大间距 */
.weui-cell_select-before .weui-cell__hd {
  width: 110px !important;
  padding: 0 !important;
}
.weui-cell_select-before .weui-select {
  width: 100% !important;
  padding-left: 16px !important;
  padding-right: 28px !important;
  box-sizing: border-box;
  font-size: 15px;
  color: #333;
  border: none;
  outline: none;
  background: transparent;
  appearance: none;
  -webkit-appearance: none;
  -moz-appearance: none;
}

/* 统一左侧 Label 的宽度和字体，实现上下对齐 */
.weui-cell_vcode .weui-cell__hd {
  width: 110px !important;
  padding-left: 0 !important;
}

.weui-label {
  width: 100% !important;
  font-weight: normal !important;
  color: #333;
  text-align: left !important;
  display: block;
}

.weui-input {
  font-size: 15px;
}

/* 确保输入框撑满剩余空间 */
.weui-cell__bd {
  flex: 1 !important;
}
.weui-cell__bd .weui-input {
  width: 100% !important;
}

/* 让验证码输入区域与手机号行保持相似宽度 */
.weui-cell_vcode {
  padding-right: 16px !important;
}
.weui-cell_vcode .weui-cell__bd {
  flex: 1 !important;
}

/* 规整右侧获取验证码按钮 */
.weui-cell_vcode .weui-cell__ft {
  display: flex;
  align-items: center;
}
.weui-vcode-btn {
  width: auto !important;
  padding: 0 12px !important;
  margin-left: auto !important;
  height: auto;
  line-height: 1.5;
  font-size: 15px !important;
  color: #07c160;
  border: none;
  background: transparent;
  border-left: 1px solid #e5e5e5;
  border-radius: 0;
  outline: none;
}
.weui-vcode-btn--disabled {
  color: #999;
}

/* 底部按钮区域 */
.weui-btn-area {
  margin: 30px 15px 0;
}

.weui-btn {
  width: 100%;
  max-width: 360px;
  margin: 0 auto;
  background-color: #07c160;
  color: #ffffff;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
  padding: 10px 20px;
  border: none;
  display: flex;
  justify-content: center;
  align-items: center;
  text-decoration: none;
  cursor: pointer;
}
.weui-btn_primary:active {
  background-color: #06ad56;
}
.weui-btn.weui-btn_disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.weui-btn.weui-btn_default {
  background-color: #ffffff;
  color: #333;
  border: 1px solid #d9d9d9;
}

/* 强制修复 WEUI 按钮 loading 态的居中和尺寸问题 */
.weui-btn.weui-btn_loading {
  display: flex !important;
  justify-content: center;
  align-items: center;
  height: auto;
  min-height: 48px;
  line-height: 1.4;
}
.weui-btn.weui-btn_loading .weui-loading {
  margin-right: 8px;
  width: 20px;
  height: 20px;
  display: inline-block;
  vertical-align: middle;
}

.btn-loading {
  display: flex;
  align-items: center;
  justify-content: center;
}

.btn-text {
  font-size: 16px;
}

/* Dialog 样式，与注销页保持一致的 WEUI 风格 */
.weui-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  z-index: 1000;
}
.weui-dialog {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 85%;
  max-width: 300px;
  background: #ffffff;
  border-radius: 8px;
  z-index: 1001;
  overflow: hidden;
}
.weui-dialog__hd {
  padding: 20px 20px 10px;
  text-align: center;
}
.weui-dialog__title {
  font-size: 17px;
  font-weight: 500;
  color: #333;
}
.weui-dialog__bd {
  padding: 10px 20px 20px;
  text-align: center;
  font-size: 15px;
  color: #666;
  line-height: 1.5;
}
.weui-dialog__ft {
  display: flex;
  border-top: 1px solid #e5e5e5;
}
.weui-dialog__btn {
  flex: 1;
  padding: 15px 0;
  text-align: center;
  font-size: 17px;
  color: #333;
  text-decoration: none;
  border-right: 1px solid #e5e5e5;
}
.weui-dialog__btn:last-child {
  border-right: none;
}
.weui-dialog__btn_primary {
  color: #fa5151;
  font-weight: 500;
}
</style>
