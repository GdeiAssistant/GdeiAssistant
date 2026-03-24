<script setup>
import { ref, computed, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '../../utils/request'
import { useToast } from '@/composables/useToast'

const router = useRouter()
const { success: showSuccess, error: showError } = useToast()

const email = ref('')
const code = ref('')
const countdown = ref(0)
const sending = ref(false)
const submitting = ref(false)
let timerId = null

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
    showError('邮箱格式不正确')
    return
  }

  sending.value = true
  try {
    await request.post(`/email/verification?email=${encodeURIComponent(email.value)}`)
    countdown.value = 60
    timerId = setInterval(() => {
      if (countdown.value > 0) {
        countdown.value -= 1
      } else if (timerId) {
        clearInterval(timerId)
        timerId = null
      }
    }, 1000)
    showSuccess('验证码已发送，请检查邮箱')
  } catch (e) {
    // 错误由 request.js 全局拦截器统一提示
  } finally {
    sending.value = false
  }
}

async function handleSubmit() {
  if (submitting.value) return
  if (!validateEmail(email.value)) {
    showError('邮箱格式不正确')
    return
  }
  if (!code.value) {
    showError('请输入验证码')
    return
  }

  submitting.value = true
  try {
    await request.post(`/email/bind?email=${encodeURIComponent(email.value)}&randomCode=${encodeURIComponent(code.value)}`)
    showSuccess('绑定成功')
    setTimeout(() => {
      router.back()
    }, 800)
  } catch (e) {
    // 错误由 request.js 全局拦截器统一提示
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
  <div class="min-h-screen bg-[var(--c-bg)]">
    <!-- Header -->
    <div class="sticky top-0 z-30 flex items-center h-[52px] px-5 bg-[var(--c-surface)]/90 backdrop-blur-xl border-b border-[var(--c-border)]">
      <button @click="$router.back()" class="text-[var(--c-primary)] text-sm font-medium">← 返回</button>
      <span class="flex-1 text-center text-sm font-bold">绑定邮箱</span>
      <div class="w-10"></div>
    </div>

    <div class="max-w-lg mx-auto px-4 pt-6">
      <!-- Form Card -->
      <div class="rounded-xl bg-[var(--c-surface)] border border-[var(--c-border)] overflow-hidden">
        <!-- Email input -->
        <div class="flex items-center px-4 py-3 border-b border-[var(--c-border)]">
          <label class="w-16 text-sm text-[var(--c-text-2)] shrink-0">邮箱</label>
          <input
            v-model="email"
            type="email"
            placeholder="请输入您的电子邮箱"
            class="flex-1 bg-transparent text-sm text-[var(--c-text-primary)] placeholder-[var(--c-text-quaternary)] outline-none"
          />
        </div>

        <!-- Verification code input -->
        <div class="flex items-center px-4 py-3">
          <label class="w-16 text-sm text-[var(--c-text-2)] shrink-0">验证码</label>
          <input
            v-model="code"
            type="number"
            inputmode="numeric"
            placeholder="请输入邮箱验证码"
            class="flex-1 bg-transparent text-sm text-[var(--c-text-primary)] placeholder-[var(--c-text-quaternary)] outline-none"
          />
          <button
            type="button"
            :disabled="!canSendCode"
            @click="handleSendCode"
            class="ml-3 pl-3 border-l border-[var(--c-border)] text-sm whitespace-nowrap transition-colors"
            :class="canSendCode ? 'text-[var(--c-primary)] cursor-pointer' : 'text-[var(--c-text-quaternary)] cursor-not-allowed'"
          >
            {{ codeButtonText }}
          </button>
        </div>
      </div>

      <!-- Submit button -->
      <div class="mt-8 px-2">
        <button
          type="button"
          :disabled="submitting"
          @click="handleSubmit"
          class="w-full py-3 rounded-xl text-white text-base font-medium bg-[var(--c-primary)] transition-opacity"
          :class="submitting ? 'opacity-60 cursor-not-allowed' : 'active:opacity-80 cursor-pointer'"
        >
          <span v-if="submitting" class="flex items-center justify-center gap-2">
            <svg class="animate-spin h-4 w-4" viewBox="0 0 24 24"><circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" fill="none"/><path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/></svg>
            绑定中...
          </span>
          <span v-else>确认绑定</span>
        </button>
      </div>
    </div>
  </div>
</template>
