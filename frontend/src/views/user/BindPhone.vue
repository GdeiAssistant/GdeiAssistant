<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '../../utils/request'
import { showErrorTopTips } from '@/utils/toast.js'
import { useToast } from '@/composables/useToast'

const router = useRouter()
const { success: toastSuccess } = useToast()

const currentPhone = ref('')
const boundCountryCode = ref('+86')
const isEditing = ref(false)
const loadingStatus = ref(true)

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
    const pattern = /^1[3-9]\d{9}$/
    return pattern.test(value)
  } else {
    const pattern = /^\d{5,15}$/
    return pattern.test(value)
  }
}

function maskPhone(phone) {
  if (!phone) return ''
  if (phone.length === 11) {
    return `${phone.substring(0, 3)}****${phone.substring(7)}`
  } else if (phone.length >= 5) {
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
        let iso = ''
        let emoji = flag

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

    if (codes.length > 0) {
      countryCodes.value = codes
    } else {
      countryCodes.value = [{
        iso: 'CN',
        code: '+86',
        emoji: getFlagEmoji('CN'),
        name: '中国大陆'
      }]
    }
  } catch (error) {
    console.error('加载区号 XML 失败', error)
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
      showErrorTopTips('请输入正确的国内手机号')
    } else {
      showErrorTopTips('请输入正确的国际手机号')
    }
    return
  }

  sending.value = true
  try {
    const numericCode = parseInt((currentCountryCode.value || '+86').replace('+', ''), 10) || 86
    await request.post(`/phone/verification?code=${encodeURIComponent(numericCode)}&phone=${encodeURIComponent(formPhone.value)}`)
    countdown.value = 60
    timerId = setInterval(() => {
      if (countdown.value > 0) {
        countdown.value -= 1
      } else if (timerId) {
        clearInterval(timerId)
        timerId = null
      }
    }, 1000)
    toastSuccess('验证码已发送，请查看短信')
  } catch (e) {
    // 错误由 request.js 全局拦截器统一提示
  } finally {
    sending.value = false
  }
}

async function handleSubmit() {
  if (isBinding.value) return
  if (!validatePhone(formPhone.value, currentCountryCode.value)) {
    if (currentCountryCode.value === '+86') {
      showErrorTopTips('请输入正确的国内手机号')
    } else {
      showErrorTopTips('请输入正确的国际手机号')
    }
    return
  }
  if (!vcode.value) {
    showErrorTopTips('请输入验证码')
    return
  }

  isBinding.value = true
  try {
    const numericCode = parseInt((currentCountryCode.value || '+86').replace('+', ''), 10) || 86
    await request.post(`/phone/attach?code=${encodeURIComponent(numericCode)}&phone=${encodeURIComponent(formPhone.value)}&randomCode=${encodeURIComponent(vcode.value)}`)
    currentPhone.value = maskPhone(formPhone.value)
    boundCountryCode.value = currentCountryCode.value
    toastSuccess('绑定成功')
    isEditing.value = false
  } catch (e) {
    // 错误由 request.js 全局拦截器统一提示
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
    await request.post('/phone/unattach')
    currentPhone.value = ''
    boundCountryCode.value = '+86'
    formPhone.value = ''
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
  await loadCountryCodes()

  try {
    const res = await request.get('/phone/status')
    const data = res && res.data
    if (data && typeof data === 'object') {
      if (data.phone) {
        currentPhone.value = maskPhone(String(data.phone))
      }
      if (typeof data.code === 'number') {
        boundCountryCode.value = `+${data.code}`
      }
    }
  } catch (e) {
    // ignore，保持默认未绑定状态
  } finally {
    loadingStatus.value = false
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
      <h1 class="flex-1 text-center text-base font-medium text-gray-700 m-0">绑定手机</h1>
      <div class="w-15"></div>
    </div>

    <div class="max-w-lg mx-auto px-4 py-6">
      <!-- Loading -->
      <div v-if="loadingStatus" class="bg-white rounded-xl shadow-sm p-8 text-center">
        <div class="w-8 h-8 border-2 border-gray-200 border-t-green-500 rounded-full animate-spin mx-auto"></div>
        <h2 class="text-lg font-medium text-gray-700 mt-4">加载中</h2>
        <p class="text-sm text-gray-500 mt-1">正在获取当前绑定状态，请稍候。</p>
      </div>

      <!-- Bound status -->
      <div v-else-if="currentPhone && !isEditing" class="bg-white rounded-xl shadow-sm p-8 text-center">
        <div class="text-5xl text-green-500 mb-4">&#10003;</div>
        <h2 class="text-lg font-medium text-gray-700">已绑定手机</h2>
        <p class="text-sm text-gray-500 mt-2">您当前绑定的手机号为：{{ boundCountryCode }} {{ currentPhone }}</p>
        <div class="mt-8 space-y-3">
          <button
            type="button"
            class="w-full rounded-lg bg-green-500 text-white font-medium py-2.5 active:bg-green-600 cursor-pointer"
            @click="startEdit"
          >更换手机</button>
          <button
            type="button"
            class="w-full rounded-lg bg-white text-gray-700 font-medium py-2.5 border border-gray-300 cursor-pointer"
            @click="openUnbindDialog"
          >解除绑定</button>
        </div>
      </div>

      <!-- Unbound status -->
      <div v-else-if="!currentPhone && !isEditing" class="bg-white rounded-xl shadow-sm p-8 text-center">
        <div class="text-5xl text-blue-400 mb-4">i</div>
        <h2 class="text-lg font-medium text-gray-700">未绑定手机号</h2>
        <p class="text-sm text-gray-500 mt-2">您尚未绑定手机号码，绑定后可提升账号安全性及用于找回密码。</p>
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
        <p v-if="currentPhone" class="text-sm text-gray-400 mb-3">请输入新的手机号码进行绑定。</p>

        <div class="bg-white rounded-xl shadow-sm divide-y divide-gray-100">
          <!-- Phone input with country code -->
          <div class="flex items-center px-4 py-3 gap-3">
            <select
              v-model="currentCountryCode"
              class="w-[100px] bg-transparent text-sm text-gray-700 border-none outline-none appearance-none cursor-pointer"
            >
              <option v-for="(item, index) in countryCodes" :key="index" :value="item.code">
                {{ item.emoji }} {{ item.code }}
              </option>
            </select>
            <input
              v-model="formPhone"
              type="tel"
              :maxlength="currentCountryCode === '+86' ? 11 : 15"
              :placeholder="currentPhone ? '请输入新的手机号' : '请输入您的手机号'"
              class="flex-1 text-sm text-gray-700 outline-none placeholder-gray-400"
            />
          </div>

          <!-- Verification code -->
          <div class="flex items-center px-4 py-3 gap-3">
            <label class="w-[100px] text-sm text-gray-700 shrink-0">验证码</label>
            <input
              v-model="vcode"
              type="number"
              inputmode="numeric"
              placeholder="请输入手机验证码"
              class="flex-1 text-sm text-gray-700 outline-none placeholder-gray-400"
            />
            <button
              type="button"
              class="shrink-0 text-sm pl-3 border-l border-gray-200 cursor-pointer"
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

        <div v-if="currentPhone" class="mt-3">
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
            确定要解除绑定该手机号吗？解除后将无法使用该手机号找回账号。
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
