<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import request from '../../utils/request'
import { showErrorTopTips } from '@/utils/toast.js'
import { useToast } from '@/composables/useToast'
import {
  getFallbackCountryCodeItems,
  localizeCountryCodes,
  normalizeCountryCodeItems,
  parseCountryCodesXml,
} from './bindPhoneSupport'

const router = useRouter()
const { success: toastSuccess } = useToast()
const { t, locale } = useI18n()

const currentPhone = ref('')
const boundCountryCode = ref('+86')
const isEditing = ref(false)
const loadingStatus = ref(true)

const rawCountryCodes = ref([])
const countryCodes = computed(() => localizeCountryCodes(rawCountryCodes.value, locale.value))
const currentCountryCode = ref('+86')

const formPhone = ref('')
const vcode = ref('')
const countdown = ref(0)
const isBinding = ref(false)
const sending = ref(false)
const showUnbindDialog = ref(false)
const isUnbinding = ref(false)
let timerId = null
const phoneAttributionApi = `${import.meta.env.VITE_APP_BASE_API ?? '/api'}/phone/attribution`

const codeButtonText = computed(() => {
  if (countdown.value > 0) {
    return t('bindPhonePage.retryAfter', { seconds: countdown.value })
  }
  return t('bindPhonePage.getCode')
})

const canSendCode = computed(() => countdown.value === 0 && !sending.value)

function validatePhone(value, countryCode) {
  if (countryCode === '+86') {
    return /^1[3-9]\d{9}$/.test(value)
  }
  return /^\d{7,11}$/.test(value)
}

function maskPhone(phone) {
  if (!phone) return ''
  if (phone.length === 11) {
    return `${phone.substring(0, 3)}****${phone.substring(7)}`
  }
  if (phone.length >= 5) {
    return `${phone.substring(0, 3)}****${phone.substring(phone.length - 2)}`
  }
  return phone
}

async function loadCountryCodes() {
  try {
    const response = await fetch(phoneAttributionApi, {
      headers: {
        'Accept-Language': locale.value,
      },
    })
    if (response.ok) {
      const payload = await response.json()
      const items = normalizeCountryCodeItems(payload?.data || [])
      if (items.length > 0) {
        rawCountryCodes.value = items
        return
      }
    }
  } catch (error) {
    console.error('Failed to load area codes from API, falling back to XML.', error)
  }

  try {
    const response = await fetch('/country_codes.xml')
    const text = await response.text()
    const items = parseCountryCodesXml(text)
    if (items.length > 0) {
      rawCountryCodes.value = items
      return
    }
  } catch (error) {
    console.error('Failed to load area codes from XML fallback.', error)
  }

  rawCountryCodes.value = getFallbackCountryCodeItems()
}

function normalizePhoneErrorMessage() {
  return currentCountryCode.value === '+86'
    ? t('bindPhonePage.invalidCnPhone')
    : t('bindPhonePage.invalidIntlPhone')
}

async function handleSendCode() {
  if (!canSendCode.value) return
  if (!validatePhone(formPhone.value, currentCountryCode.value)) {
    showErrorTopTips(normalizePhoneErrorMessage())
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
    toastSuccess(t('bindPhonePage.codeSent'))
  } catch (e) {
    // request.js handles user-facing errors globally
  } finally {
    sending.value = false
  }
}

async function handleSubmit() {
  if (isBinding.value) return
  if (!validatePhone(formPhone.value, currentCountryCode.value)) {
    showErrorTopTips(normalizePhoneErrorMessage())
    return
  }
  if (!vcode.value) {
    showErrorTopTips(t('bindPhonePage.verificationRequired'))
    return
  }

  isBinding.value = true
  try {
    const numericCode = parseInt((currentCountryCode.value || '+86').replace('+', ''), 10) || 86
    await request.post(`/phone/attach?code=${encodeURIComponent(numericCode)}&phone=${encodeURIComponent(formPhone.value)}&randomCode=${encodeURIComponent(vcode.value)}`)
    currentPhone.value = maskPhone(formPhone.value)
    boundCountryCode.value = currentCountryCode.value
    toastSuccess(t('bindPhonePage.bindSuccess'))
    isEditing.value = false
  } catch (e) {
    // request.js handles user-facing errors globally
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
    toastSuccess(t('bindPhonePage.unbindSuccess'))
    showUnbindDialog.value = false
  } catch (e) {
    // request.js handles user-facing errors globally
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
    // keep the default unbound state
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
    <div class="sticky top-0 z-10 flex items-center h-12 bg-white border-b border-gray-200 px-4">
      <button type="button" class="w-15 text-sm text-gray-700 text-left cursor-pointer" @click="router.back()">{{ t('about.back') }}</button>
      <h1 class="flex-1 text-center text-base font-medium text-gray-700 m-0">{{ t('profile.bindPhone') }}</h1>
      <div class="w-15"></div>
    </div>

    <div class="max-w-lg mx-auto px-4 py-6">
      <div v-if="loadingStatus" class="bg-white rounded-xl shadow-sm p-8 text-center">
        <div class="w-8 h-8 border-2 border-gray-200 border-t-green-500 rounded-full animate-spin mx-auto"></div>
        <h2 class="text-lg font-medium text-gray-700 mt-4">{{ t('bindPhonePage.loadingTitle') }}</h2>
        <p class="text-sm text-gray-500 mt-1">{{ t('bindPhonePage.loadingDescription') }}</p>
      </div>

      <div v-else-if="currentPhone && !isEditing" class="bg-white rounded-xl shadow-sm p-8 text-center">
        <div class="text-5xl text-green-500 mb-4">&#10003;</div>
        <h2 class="text-lg font-medium text-gray-700">{{ t('bindPhonePage.boundTitle') }}</h2>
        <p class="text-sm text-gray-500 mt-2">
          {{ t('bindPhonePage.boundDescription', { code: boundCountryCode, phone: currentPhone }) }}
        </p>
        <div class="mt-8 space-y-3">
          <button
            type="button"
            class="w-full rounded-lg bg-green-500 text-white font-medium py-2.5 active:bg-green-600 cursor-pointer"
            @click="startEdit"
          >{{ t('bindPhonePage.changeAction') }}</button>
          <button
            type="button"
            class="w-full rounded-lg bg-white text-gray-700 font-medium py-2.5 border border-gray-300 cursor-pointer"
            @click="openUnbindDialog"
          >{{ t('bindPhonePage.unbindAction') }}</button>
        </div>
      </div>

      <div v-else-if="!currentPhone && !isEditing" class="bg-white rounded-xl shadow-sm p-8 text-center">
        <div class="text-5xl text-blue-400 mb-4">i</div>
        <h2 class="text-lg font-medium text-gray-700">{{ t('bindPhonePage.unboundTitle') }}</h2>
        <p class="text-sm text-gray-500 mt-2">{{ t('bindPhonePage.unboundDescription') }}</p>
        <div class="mt-8">
          <button
            type="button"
            class="w-full rounded-lg bg-green-500 text-white font-medium py-2.5 active:bg-green-600 cursor-pointer"
            @click="startBind"
          >{{ t('bindPhonePage.bindNowAction') }}</button>
        </div>
      </div>

      <div v-else>
        <p v-if="currentPhone" class="text-sm text-gray-400 mb-3">{{ t('bindPhonePage.replaceHint') }}</p>

        <div class="bg-white rounded-xl shadow-sm divide-y divide-gray-100">
          <div class="flex items-center px-4 py-3 gap-3">
            <select
              v-model="currentCountryCode"
              class="w-[180px] bg-transparent text-sm text-gray-700 border-none outline-none appearance-none cursor-pointer"
            >
              <option v-for="item in countryCodes" :key="item.code" :value="item.code">
                {{ item.emoji }} {{ item.name }} {{ item.code }}
              </option>
            </select>
            <input
              v-model="formPhone"
              type="tel"
              maxlength="11"
              :placeholder="currentPhone ? t('bindPhonePage.phonePlaceholderNew') : t('bindPhonePage.phonePlaceholder')"
              class="flex-1 text-sm text-gray-700 outline-none placeholder-gray-400"
            />
          </div>

          <div class="flex items-center px-4 py-3 gap-3">
            <label class="w-[100px] text-sm text-gray-700 shrink-0">{{ t('bindPhonePage.verificationLabel') }}</label>
            <input
              v-model="vcode"
              type="number"
              inputmode="numeric"
              :placeholder="t('bindPhonePage.verificationPlaceholder')"
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

        <div class="mt-8">
          <button
            type="button"
            class="w-full rounded-lg bg-green-500 text-white font-medium py-2.5 flex items-center justify-center active:bg-green-600 cursor-pointer disabled:opacity-60 disabled:cursor-not-allowed"
            :disabled="isBinding"
            @click="handleSubmit"
          >
            <template v-if="isBinding">
              <span class="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin mr-2"></span>
              {{ t('bindPhonePage.submitting') }}
            </template>
            <template v-else>{{ t('bindPhonePage.confirmBind') }}</template>
          </button>
        </div>

        <div v-if="currentPhone" class="mt-3">
          <button
            type="button"
            class="w-full rounded-lg bg-white text-gray-700 font-medium py-2.5 border border-gray-300 cursor-pointer"
            @click="cancelEdit"
          >{{ t('common.cancel') }}</button>
        </div>
      </div>
    </div>

    <Teleport to="body">
      <template v-if="showUnbindDialog">
        <div class="fixed inset-0 bg-black/60 z-[1000]" @click="closeUnbindDialog"></div>
        <div class="fixed top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[85%] max-w-[300px] bg-white rounded-xl z-[1001] overflow-hidden">
          <div class="px-5 pt-5 pb-2.5 text-center">
            <strong class="text-[17px] font-medium text-gray-700">{{ t('bindPhonePage.unbindDialogTitle') }}</strong>
          </div>
          <div class="px-5 pb-5 text-center text-[15px] text-gray-500 leading-relaxed">
            {{ t('bindPhonePage.unbindDialogContent') }}
          </div>
          <div class="flex border-t border-gray-200">
            <button
              type="button"
              class="flex-1 py-3.5 text-center text-[17px] text-gray-700 border-r border-gray-200 cursor-pointer bg-transparent"
              @click="closeUnbindDialog"
            >{{ t('common.cancel') }}</button>
            <button
              type="button"
              class="flex-1 py-3.5 text-center text-[17px] text-red-500 font-medium cursor-pointer bg-transparent"
              @click="confirmUnbind"
            >{{ t('bindPhonePage.confirmUnbind') }}</button>
          </div>
        </div>
      </template>
    </Teleport>
  </div>
</template>
