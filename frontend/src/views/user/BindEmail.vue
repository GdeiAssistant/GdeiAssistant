<script setup>
import { postEmailVerificationemail, postEmailBindemailrandomCode, postEmailUnbind, getEmailStatus } from "../../api/emailEndpoints.js"

import { ChevronLeft } from 'lucide-vue-next'
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'

import { showErrorTopTips } from '@/utils/toast.js'
import { useToast } from '@/composables/useToast'
import { maskEmail } from '@/utils/mask'

const router = useRouter()
const { t } = useI18n()
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
    return t('bindEmail.retryIn', { seconds: countdown.value })
  }
  return t('bindEmail.getCode')
})

const canSendCode = computed(() => countdown.value === 0 && !sending.value)

function validateEmail(value) {
  const pattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return pattern.test(value)
}

async function handleSendCode() {
  if (!canSendCode.value) return
  if (!validateEmail(formEmail.value)) {
    showErrorTopTips(t('bindEmail.invalidEmail'))
    return
  }

  sending.value = true
  try {
    await postEmailVerificationemail(formEmail.value)
    countdown.value = 60
    timerId = setInterval(() => {
      if (countdown.value > 0) {
        countdown.value -= 1
      } else if (timerId) {
        clearInterval(timerId)
        timerId = null
      }
    }, 1000)
    toastSuccess(t('bindEmail.codeSent'))
  } catch (e) {
    showErrorTopTips(t('bindEmail.sendFailed'))
  } finally {
    sending.value = false
  }
}

async function handleSubmit() {
  if (isBinding.value) return
  if (!validateEmail(formEmail.value)) {
    showErrorTopTips(t('bindEmail.invalidEmail'))
    return
  }
  if (!vcode.value) {
    showErrorTopTips(t('bindEmail.codeRequired'))
    return
  }

  isBinding.value = true
  try {
    await postEmailBindemailrandomCode(formEmail.value, vcode.value)
    currentEmail.value = maskEmail(formEmail.value)
    toastSuccess(t('bindEmail.bindSuccess'))
    isEditing.value = false
  } catch (e) {
    showErrorTopTips(t('bindEmail.bindFailed'))
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
    await postEmailUnbind()
    currentEmail.value = ''
    formEmail.value = ''
    vcode.value = ''
    isEditing.value = false
    toastSuccess(t('bindEmail.unbindSuccess'))
    showUnbindDialog.value = false
  } catch (e) {
    // 错误由 request.js 全局拦截器统一提示
  } finally {
    isUnbinding.value = false
  }
}

onMounted(async () => {
  try {
    const res = await getEmailStatus()
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
  <div class="subpage min-h-screen bg-[var(--c-bg)]">
    <!-- Sticky Header -->
    <div class="subpage-bar">
      <button type="button" class="subpage-bar__back" @click="router.back()">
        <ChevronLeft :size="18" aria-hidden="true" />
        <span>{{ t('common.back') }}</span>
      </button>
      <h1 class="subpage-bar__title">{{ t('profile.bindEmail') }}</h1>
      <span aria-hidden="true"></span>
    </div>

    <div class="subpage-body max-w-lg mx-auto px-4 py-6">
      <!-- Bound status -->
      <div v-if="currentEmail && !isEditing" class="bg-[var(--c-surface)] rounded-xl shadow-sm p-8 text-center">
        <div class="bind-email-status bind-email-status--success text-5xl mb-4">&#10003;</div>
        <h2 class="text-lg font-medium text-[var(--c-text-1)]">{{ t('bindEmail.boundTitle') }}</h2>
        <p class="text-sm text-[var(--c-text-2)] mt-2">{{ t('bindEmail.boundDescription', { email: currentEmail }) }}</p>
        <div class="mt-8 space-y-3">
          <button
            type="button"
            class="bind-email-primary-action w-full rounded-lg text-white font-medium py-2.5 cursor-pointer"
            @click="startEdit"
          >{{ t('bindEmail.edit') }}</button>
          <button
            type="button"
            class="w-full rounded-lg bg-[var(--c-surface)] text-[var(--c-text-1)] font-medium py-2.5 border border-[var(--c-border)] cursor-pointer"
            @click="openUnbindDialog"
          >{{ t('bindEmail.unbind') }}</button>
        </div>
      </div>

      <!-- Unbound status -->
      <div v-else-if="!currentEmail && !isEditing" class="bg-[var(--c-surface)] rounded-xl shadow-sm p-8 text-center">
        <div class="bind-email-status bind-email-status--info text-5xl mb-4">i</div>
        <h2 class="text-lg font-medium text-[var(--c-text-1)]">{{ t('bindEmail.unboundTitle') }}</h2>
        <p class="text-sm text-[var(--c-text-2)] mt-2">{{ t('bindEmail.unboundDescription') }}</p>
        <div class="mt-8">
          <button
            type="button"
            class="bind-email-primary-action w-full rounded-lg text-white font-medium py-2.5 cursor-pointer"
            @click="startBind"
          >{{ t('bindEmail.bindNow') }}</button>
        </div>
      </div>

      <!-- Edit/Bind form -->
      <div v-else>
        <p v-if="currentEmail" class="text-sm text-[var(--c-text-3)] mb-3">{{ t('bindEmail.editDescription') }}</p>

        <div class="bg-[var(--c-surface)] rounded-xl shadow-sm divide-y divide-[var(--c-divider)]">
          <!-- Email input -->
          <div class="flex items-center px-4 py-3 gap-3">
            <label class="w-[60px] text-sm text-[var(--c-text-1)] shrink-0">{{ t('bindEmail.email') }}</label>
            <input
              v-model="formEmail"
              type="email"
              :placeholder="currentEmail ? t('bindEmail.newEmailPlaceholder') : t('bindEmail.emailPlaceholder')"
              class="flex-1 text-sm text-[var(--c-text-1)] outline-none placeholder-gray-400"
            />
          </div>

          <!-- Verification code -->
          <div class="flex items-center px-4 py-3 gap-3">
            <label class="w-[60px] text-sm text-[var(--c-text-1)] shrink-0">{{ t('bindEmail.code') }}</label>
            <input
              v-model="vcode"
              type="number"
              inputmode="numeric"
              :placeholder="t('bindEmail.codePlaceholder')"
              class="flex-1 text-sm text-[var(--c-text-1)] outline-none placeholder-gray-400"
            />
            <button
              type="button"
              class="shrink-0 text-sm pl-3 border-l border-[var(--c-border)] cursor-pointer bg-transparent"
              :class="canSendCode ? 'bind-email-code-link bind-email-code-link--active' : 'text-[var(--c-text-3)]'"
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
            class="bind-email-primary-action w-full rounded-lg text-white font-medium py-2.5 flex items-center justify-center cursor-pointer disabled:opacity-60 disabled:cursor-not-allowed"
            :disabled="isBinding"
            @click="handleSubmit"
          >
            <template v-if="isBinding">
              <span class="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin mr-2"></span>
              {{ t('bindEmail.binding') }}
            </template>
            <template v-else>{{ t('bindEmail.confirm') }}</template>
          </button>
        </div>

        <div v-if="currentEmail" class="mt-3">
          <button
            type="button"
            class="w-full rounded-lg bg-[var(--c-surface)] text-[var(--c-text-1)] font-medium py-2.5 border border-[var(--c-border)] cursor-pointer"
            @click="cancelEdit"
          >{{ t('common.cancel') }}</button>
        </div>
      </div>
    </div>

    <!-- Unbind dialog -->
    <Teleport to="body">
      <template v-if="showUnbindDialog">
        <div class="ui-scrim fixed inset-0 bg-black/60 z-[1000]" @click="closeUnbindDialog"></div>
        <div class="fixed top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[85%] max-w-[300px] bg-[var(--c-surface)] rounded-xl z-[1001] overflow-hidden">
          <div class="px-5 pt-5 pb-2.5 text-center">
            <strong class="text-[17px] font-medium text-[var(--c-text-1)]">{{ t('bindEmail.unbind') }}</strong>
          </div>
          <div class="px-5 pb-5 text-center text-[15px] text-[var(--c-text-2)] leading-relaxed">
            {{ t('bindEmail.unbindConfirm') }}
          </div>
          <div class="flex border-t border-[var(--c-border)]">
            <button
              type="button"
              class="flex-1 py-3.5 text-center text-[17px] text-[var(--c-text-1)] border-r border-[var(--c-border)] cursor-pointer bg-transparent"
              @click="closeUnbindDialog"
            >{{ t('common.cancel') }}</button>
            <button
              type="button"
              class="flex-1 py-3.5 text-center text-[17px] text-red-500 font-medium cursor-pointer bg-transparent"
              @click="confirmUnbind"
            >{{ t('bindEmail.confirmUnbind') }}</button>
          </div>
        </div>
      </template>
    </Teleport>
  </div>
</template>

<style scoped>
.bind-email-primary-action {
  background: var(--c-primary);
  box-shadow: none;
}

.bind-email-status--success {
  color: var(--c-primary);
}

.bind-email-status--info {
  color: var(--c-primary);
}

.bind-email-code-link--active {
  color: var(--c-primary);
}
</style>
