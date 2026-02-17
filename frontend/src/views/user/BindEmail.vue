<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '../../utils/request'

const router = useRouter()

// 当前已绑定邮箱（mock 初始值，实际从接口获取）
const currentEmail = ref('123***@qq.com')
const isEditing = ref(false)

const formEmail = ref('')
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

function validateEmail(value) {
  const pattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return pattern.test(value)
}

function maskEmail(email) {
  if (!email) return ''
  const [localPart, domain] = email.split('@')
  if (!localPart || !domain) return email
  if (localPart.length <= 3) {
    return `${localPart[0]}***@${domain}`
  }
  const visibleStart = localPart.substring(0, 3)
  return `${visibleStart}***@${domain}`
}

async function handleSendCode() {
  if (!canSendCode.value) return
  if (!validateEmail(formEmail.value)) {
    showToast('邮箱格式不正确')
    return
  }

  sending.value = true
  try {
    await request.post('/api/user/send-email-code', { email: formEmail.value })
    countdown.value = 60
    timerId = setInterval(() => {
      if (countdown.value > 0) {
        countdown.value -= 1
      } else if (timerId) {
        clearInterval(timerId)
        timerId = null
      }
    }, 1000)
    showToast('验证码已发送，请检查邮箱')
  } catch (e) {
    showToast('发送验证码失败，请稍后重试')
  } finally {
    sending.value = false
  }
}

async function handleSubmit() {
  if (isBinding.value) return
  if (!validateEmail(formEmail.value)) {
    showToast('邮箱格式不正确')
    return
  }
  if (!vcode.value) {
    showToast('请输入验证码')
    return
  }

  isBinding.value = true
  try {
    await request.post('/api/user/bind-email', { email: formEmail.value, code: vcode.value })
    currentEmail.value = maskEmail(formEmail.value)
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
  formEmail.value = ''
  vcode.value = ''
}

function startBind() {
  isEditing.value = true
  formEmail.value = ''
  vcode.value = ''
}

function cancelEdit() {
  isEditing.value = false
  formEmail.value = ''
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
    await request.post('/api/user/unbind-email')
    currentEmail.value = ''
    formEmail.value = ''
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
  try {
    const res = await request.get('/api/user/email-status')
    const data = res && (res.data || res.email)
    if (typeof data === 'string') {
      currentEmail.value = maskEmail(data)
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
  <div class="bind-email-page">
    <!-- 统一头部 -->
    <div class="email-header unified-header">
      <span class="email-header__back" @click="router.back()">返回</span>
      <h1 class="email-header__title">绑定邮箱</h1>
      <span class="email-header__placeholder"></span>
    </div>

    <div class="email-content">
      <!-- 已绑定状态视图 -->
      <div v-if="currentEmail && !isEditing" class="status-view">
        <div class="weui-msg">
          <div class="weui-msg__icon-area">
            <i class="weui-icon-success weui-icon_msg"></i>
          </div>
          <div class="weui-msg__text-area">
            <h2 class="weui-msg__title">已绑定邮箱</h2>
            <p class="weui-msg__desc">您当前绑定的邮箱为：{{ currentEmail }}</p>
          </div>
          <div class="weui-msg__opr-area">
            <p class="weui-btn-area">
              <a href="javascript:;" class="weui-btn weui-btn_primary" @click.prevent="startEdit">修改绑定</a>
              <a href="javascript:;" class="weui-btn weui-btn_default" @click.prevent="openUnbindDialog">解除绑定</a>
            </p>
          </div>
        </div>
      </div>

      <!-- 未绑定状态视图 -->
      <div v-else-if="!currentEmail && !isEditing" class="status-view">
        <div class="weui-msg">
          <div class="weui-msg__icon-area">
            <i class="weui-icon-info weui-icon_msg"></i>
          </div>
          <div class="weui-msg__text-area">
            <h2 class="weui-msg__title">未绑定邮箱</h2>
            <p class="weui-msg__desc">您尚未绑定电子邮箱，绑定后可用于接收重要通知。</p>
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
        <p v-if="currentEmail" class="edit-tip">请输入新的邮箱地址进行绑定。</p>

        <!-- 表单区域 -->
        <div class="weui-cells weui-cells_form">
          <!-- 邮箱输入 -->
          <div class="weui-cell weui-cell_active">
            <div class="weui-cell__hd">
              <label class="weui-label">邮箱</label>
            </div>
            <div class="weui-cell__bd">
              <input
                v-model="formEmail"
                class="weui-input"
                type="email"
                :placeholder="currentEmail ? '请输入新的电子邮箱' : '请输入您的电子邮箱'"
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
                placeholder="请输入邮箱验证码"
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

        <div v-if="currentEmail" class="weui-btn-area edit-cancel-area">
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
        确定要解除绑定该邮箱吗？解除后将无法使用该邮箱找回账号。
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
.bind-email-page {
  background: #f8f8f8;
  min-height: 100vh;
}

.email-header.unified-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 44px;
  padding: 0 12px;
  background: #ffffff;
  border-bottom: 1px solid #e5e5e5;
}
.email-header__back {
  font-size: 14px;
  color: #333;
  cursor: pointer;
  min-width: 48px;
}
.email-header__title {
  flex: 1;
  text-align: center;
  font-size: 16px;
  font-weight: 500;
  margin: 0;
  color: #333;
}
.email-header__placeholder {
  min-width: 48px;
}

.email-content {
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

.weui-label {
  width: 60px;
}

.weui-input {
  font-size: 15px;
}

/* 让验证码输入区域与邮箱行保持相似宽度 */
.weui-cell_vcode .weui-cell__bd {
  flex: 1;
}

/* 验证码按钮 */
.weui-vcode-btn {
  padding: 0 12px;
  height: auto;
  line-height: 1.5;
  font-size: 15px;
  color: #07c160;
  border: none;
  background: transparent;
  border-left: 1px solid #e5e5e5;
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

