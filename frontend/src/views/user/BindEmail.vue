<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '../../utils/request'
import { showErrorTopTips } from '@/utils/toast.js'
import { useToast } from '@/composables/useToast'

const router = useRouter()
const { success: toastSuccess } = useToast()

const currentEmail = ref('')
const isEditing = ref(false)

const formEmail = ref('')
const vcode = ref('')
const countdown = ref(0)
const isBinding = ref(false)
const sending = ref(false)
const showUnbindDialog = ref(false)
const isUnbinding = ref(false)
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
    showErrorTopTips('邮箱格式不正确')
    return
  }

  sending.value = true
  try {
    await request.post(`/email/verification?email=${encodeURIComponent(formEmail.value)}`)
    countdown.value = 60
    timerId = setInterval(() => {
      if (countdown.value > 0) {
        countdown.value -= 1
      } else if (timerId) {
        clearInterval(timerId)
        timerId = null
      }
    }, 1000)
    toastSuccess('验证码已发送，请检查邮箱')
  } catch (e) {
    showErrorTopTips('发送验证码失败，请稍后重试')
  } finally {
    sending.value = false
  }
}

async function handleSubmit() {
  if (isBinding.value) return
  if (!validateEmail(formEmail.value)) {
    showErrorTopTips('邮箱格式不正确')
    return
  }
  if (!vcode.value) {
    showErrorTopTips('请输入验证码')
    return
  }

  isBinding.value = true
  try {
    await request.post(`/email/bind?email=${encodeURIComponent(formEmail.value)}&randomCode=${encodeURIComponent(vcode.value)}`)
    currentEmail.value = maskEmail(formEmail.value)
    toastSuccess('绑定成功')
    isEditing.value = false
  } catch (e) {
    showErrorTopTips('绑定失败，请稍后重试')
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
    await request.post('/email/unbind')
    currentEmail.value = ''
    formEmail.value = ''
    vcode.value = ''
    isEditing.value = false
    toastSuccess('已解除绑定')
    showUnbindDialog.value = false
  } catch (e) {
    // 错误由 request.js 全局拦截器统一提示
  } finally {
    isUnbinding.value = false
  }
}

onMounted(async () => {
  try {
    const res = await request.get('/email/status')
    const data = res && res.data
    if (typeof data === 'string') {
      currentEmail.value = maskEmail(data)
    }
  } catch (e) {
    // ignore，保持默认未绑定状态
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
  <div class="min-h-screen bg-gray-50">
    <!-- Sticky Header -->
    <div class="sticky top-0 z-10 flex items-center h-12 bg-white border-b border-gray-200 px-4">
      <button type="button" class="w-15 text-sm text-gray-700 text-left cursor-pointer" @click="router.back()">返回</button>
      <h1 class="flex-1 text-center text-base font-medium text-gray-700 m-0">绑定邮箱</h1>
      <div class="w-15"></div>
    </div>

    <div class="max-w-lg mx-auto px-4 py-6">
      <!-- Bound status -->
      <div v-if="currentEmail && !isEditing" class="bg-white rounded-xl shadow-sm p-8 text-center">
        <div class="text-5xl text-green-500 mb-4">&#10003;</div>
        <h2 class="text-lg font-medium text-gray-700">已绑定邮箱</h2>
        <p class="text-sm text-gray-500 mt-2">您当前绑定的邮箱为：{{ currentEmail }}</p>
        <div class="mt-8 space-y-3">
          <button
            type="button"
            class="w-full rounded-lg bg-green-500 text-white font-medium py-2.5 active:bg-green-600 cursor-pointer"
            @click="startEdit"
          >修改绑定</button>
          <button
            type="button"
            class="w-full rounded-lg bg-white text-gray-700 font-medium py-2.5 border border-gray-300 cursor-pointer"
            @click="openUnbindDialog"
          >解除绑定</button>
        </div>
      </div>

      <!-- Unbound status -->
      <div v-else-if="!currentEmail && !isEditing" class="bg-white rounded-xl shadow-sm p-8 text-center">
        <div class="text-5xl text-blue-400 mb-4">i</div>
        <h2 class="text-lg font-medium text-gray-700">未绑定邮箱</h2>
        <p class="text-sm text-gray-500 mt-2">您尚未绑定电子邮箱，绑定后可用于接收重要通知。</p>
        <div class="mt-8">
          <button
            type="button"
            class="w-full rounded-lg bg-green-500 text-white font-medium py-2.5 active:bg-green-600 cursor-pointer"
            @click="startBind"
          >立即绑定</button>
        </div>
      </div>

      <!-- Edit/Bind form -->
      <div v-else>
        <p v-if="currentEmail" class="text-sm text-gray-400 mb-3">请输入新的邮箱地址进行绑定。</p>

        <div class="bg-white rounded-xl shadow-sm divide-y divide-gray-100">
          <!-- Email input -->
          <div class="flex items-center px-4 py-3 gap-3">
            <label class="w-[60px] text-sm text-gray-700 shrink-0">邮箱</label>
            <input
              v-model="formEmail"
              type="email"
              :placeholder="currentEmail ? '请输入新的电子邮箱' : '请输入您的电子邮箱'"
              class="flex-1 text-sm text-gray-700 outline-none placeholder-gray-400"
            />
          </div>

          <!-- Verification code -->
          <div class="flex items-center px-4 py-3 gap-3">
            <label class="w-[60px] text-sm text-gray-700 shrink-0">验证码</label>
            <input
              v-model="vcode"
              type="number"
              inputmode="numeric"
              placeholder="请输入邮箱验证码"
              class="flex-1 text-sm text-gray-700 outline-none placeholder-gray-400"
            />
            <button
              type="button"
              class="shrink-0 text-sm pl-3 border-l border-gray-200 cursor-pointer bg-transparent"
              :class="canSendCode ? 'text-green-500' : 'text-gray-400'"
              :disabled="!canSendCode"
              @click="handleSendCode"
            >
              {{ codeButtonText }}
            </button>
          </div>
        </div>

        <!-- Submit -->
        <div class="mt-8">
          <button
            type="button"
            class="w-full rounded-lg bg-green-500 text-white font-medium py-2.5 flex items-center justify-center active:bg-green-600 cursor-pointer disabled:opacity-60 disabled:cursor-not-allowed"
            :disabled="isBinding"
            @click="handleSubmit"
          >
            <template v-if="isBinding">
              <span class="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin mr-2"></span>
              绑定中...
            </template>
            <template v-else>确认绑定</template>
          </button>
        </div>

        <div v-if="currentEmail" class="mt-3">
          <button
            type="button"
            class="w-full rounded-lg bg-white text-gray-700 font-medium py-2.5 border border-gray-300 cursor-pointer"
            @click="cancelEdit"
          >取消</button>
        </div>
      </div>
    </div>

    <!-- Unbind dialog -->
    <Teleport to="body">
      <template v-if="showUnbindDialog">
        <div class="fixed inset-0 bg-black/60 z-[1000]" @click="closeUnbindDialog"></div>
        <div class="fixed top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[85%] max-w-[300px] bg-white rounded-xl z-[1001] overflow-hidden">
          <div class="px-5 pt-5 pb-2.5 text-center">
            <strong class="text-[17px] font-medium text-gray-700">解除绑定</strong>
          </div>
          <div class="px-5 pb-5 text-center text-[15px] text-gray-500 leading-relaxed">
            确定要解除绑定该邮箱吗？解除后将无法使用该邮箱找回账号。
          </div>
          <div class="flex border-t border-gray-200">
            <button
              type="button"
              class="flex-1 py-3.5 text-center text-[17px] text-gray-700 border-r border-gray-200 cursor-pointer bg-transparent"
              @click="closeUnbindDialog"
            >取消</button>
            <button
              type="button"
              class="flex-1 py-3.5 text-center text-[17px] text-red-500 font-medium cursor-pointer bg-transparent"
              @click="confirmUnbind"
            >确认解绑</button>
          </div>
        </div>
      </template>
    </Teleport>
  </div>
</template>
