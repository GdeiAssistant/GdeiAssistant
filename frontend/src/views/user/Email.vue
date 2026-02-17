<script setup>
import { ref, computed, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '../../utils/request'

const router = useRouter()

const email = ref('')
const code = ref('')
const countdown = ref(0)
const sending = ref(false)
const submitting = ref(false)
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

async function handleSendCode() {
  if (!canSendCode.value) return
  if (!validateEmail(email.value)) {
    showToast('邮箱格式不正确')
    return
  }

  sending.value = true
  try {
    await request.post('/user/send-email-code', { email: email.value })
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
  if (submitting.value) return
  if (!validateEmail(email.value)) {
    showToast('邮箱格式不正确')
    return
  }
  if (!code.value) {
    showToast('请输入验证码')
    return
  }

  submitting.value = true
  try {
    await request.post('/user/bind-email', { email: email.value, code: code.value })
    showToast('绑定成功')
    setTimeout(() => {
      router.back()
    }, 800)
  } catch (e) {
    showToast('绑定失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

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
      <!-- 表单区域 -->
      <div class="weui-cells weui-cells_form">
        <!-- 邮箱输入 -->
        <div class="weui-cell">
          <div class="weui-cell__hd">
            <label class="weui-label">邮箱</label>
          </div>
          <div class="weui-cell__bd">
            <input
              v-model="email"
              class="weui-input"
              type="email"
              placeholder="请输入您的电子邮箱"
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
              v-model="code"
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
          :class="{ 'weui-btn_disabled': submitting }"
          @click.prevent="handleSubmit"
        >
          <span v-if="submitting" class="btn-loading">
            <span class="weui-loading weui-icon_toast"></span>
            <span style="margin-left: 8px;">绑定中...</span>
          </span>
          <span v-else>确认绑定</span>
        </a>
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

.weui-cells {
  margin-top: 0;
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
  padding: 0 8px;
  height: 30px;
  line-height: 30px;
  font-size: 13px;
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

.btn-loading {
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
