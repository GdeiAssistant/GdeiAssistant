<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { queryCardInfo, reportCardLost } from '@/api/card'
import { useToast } from '@/composables/useToast'

const router = useRouter()
const { t } = useI18n()
const { success: showSuccess, error: showError, loading: showLoading, hideLoading } = useToast()
const info = ref(null)
const loading = ref(false)
const submitLoading = ref(false)
const showPasswordDialog = ref(false)
const verifyPassword = ref('')

function onReportLoss() {
  verifyPassword.value = ''
  showPasswordDialog.value = true
}

function closePasswordDialog() {
  showPasswordDialog.value = false
  verifyPassword.value = ''
}

function confirmReportLoss() {
  const pwd = (verifyPassword.value || '').trim()
  if (!pwd) {
    showError(t('card.info.passwordRequired'))
    return
  }
  if (!/^\d+$/.test(pwd)) {
    showError(t('card.info.passwordDigitsOnly'))
    return
  }
  closePasswordDialog()
  submitLoading.value = true
  showLoading(t('card.info.submitting'))
  reportCardLost(pwd)
    .then(() => {
      submitLoading.value = false
      hideLoading()
      showSuccess(t('card.info.reportSuccess'))
      info.value = { ...info.value, cardLostState: t('card.info.reportedState') }
    })
    .catch(() => {
      submitLoading.value = false
      hideLoading()
    })
}

onMounted(() => {
  loading.value = true
  showLoading(t('common.loading'))
  queryCardInfo()
    .then((res) => {
      const body = res && res.data ? res.data : res
      const data =
        body && typeof body === 'object'
          ? body.data !== undefined
            ? body.data
            : body
          : null
      info.value = data || {}
    })
    .catch(() => {
      info.value = {}
    })
    .finally(() => {
      loading.value = false
      hideLoading()
    })
})
</script>

<template>
  <div class="min-h-screen bg-[var(--c-bg)]">
    <div class="sticky top-0 z-30 flex items-center h-[52px] px-5 bg-[var(--c-surface)]/90 backdrop-blur-xl border-b border-[var(--c-border)]">
      <button @click="$router.back()" class="text-[var(--c-primary)] text-sm font-medium">&larr; {{ t('common.back') }}</button>
      <span class="flex-1 text-center text-sm font-bold">{{ t('card.info.title') }}</span>
      <div class="w-10"></div>
    </div>

    <div class="max-w-lg mx-auto px-4 py-6" v-if="info">
      <!-- 余额卡片 -->
      <div class="bg-[var(--c-surface)] rounded-2xl p-5 shadow-sm border border-[var(--c-border)] mb-4">
        <div class="text-center">
          <div class="text-xs text-[var(--c-text-2)] mb-1">{{ t('card.info.balanceTitle') }}</div>
          <div class="font-mono text-2xl font-bold text-[var(--c-primary)]">{{ info.cardBalance ?? '--' }}</div>
          <div class="text-xs text-[var(--c-text-3)] mt-1">{{ t('card.info.interimBalance') }}{{ info.cardInterimBalance ?? '--' }}</div>
        </div>
      </div>

      <!-- 基本信息 -->
      <div class="bg-[var(--c-surface)] rounded-2xl shadow-sm border border-[var(--c-border)] mb-4">
        <div class="px-4 py-2.5 border-b border-[var(--c-border)]">
          <span class="text-xs font-semibold text-[var(--c-text-2)] uppercase tracking-wide">{{ t('card.info.basicInfoTitle') }}</span>
        </div>
        <div class="divide-y divide-[var(--c-border-light)]">
          <div class="flex justify-between px-4 py-3">
            <span class="text-sm text-[var(--c-text-2)]">{{ t('card.info.field.name') }}</span>
            <span class="text-sm font-medium">{{ info.name || '--' }}</span>
          </div>
          <div class="flex justify-between px-4 py-3">
            <span class="text-sm text-[var(--c-text-2)]">{{ t('card.info.field.number') }}</span>
            <span class="text-sm font-medium">{{ info.number ?? '--' }}</span>
          </div>
          <div class="flex justify-between px-4 py-3">
            <span class="text-sm text-[var(--c-text-2)]">{{ t('card.info.field.cardNumber') }}</span>
            <span class="text-sm font-medium">{{ info.cardNumber ?? '--' }}</span>
          </div>
        </div>
      </div>

      <!-- 状态信息 -->
      <div class="bg-[var(--c-surface)] rounded-2xl shadow-sm border border-[var(--c-border)] mb-6">
        <div class="px-4 py-2.5 border-b border-[var(--c-border)]">
          <span class="text-xs font-semibold text-[var(--c-text-2)] uppercase tracking-wide">{{ t('card.info.statusTitle') }}</span>
        </div>
        <div class="divide-y divide-[var(--c-border-light)]">
          <div class="flex justify-between px-4 py-3">
            <span class="text-sm text-[var(--c-text-2)]">{{ t('card.info.field.lostState') }}</span>
            <span class="text-sm font-medium">{{ info.cardLostState ?? '--' }}</span>
          </div>
          <div class="flex justify-between px-4 py-3">
            <span class="text-sm text-[var(--c-text-2)]">{{ t('card.info.field.freezeState') }}</span>
            <span class="text-sm font-medium">{{ info.cardFreezeState ?? '--' }}</span>
          </div>
        </div>
      </div>

      <p class="text-center text-sm text-[var(--c-text-2)]">
        {{ t('card.info.reportPromptPrefix') }}
        <a href="javascript:" class="text-[var(--c-primary)] font-medium" @click.prevent="onReportLoss">{{ t('card.info.reportPromptLink') }}</a>
      </p>
    </div>

    <!-- 挂失验证弹窗 -->
    <Teleport to="body">
      <template v-if="showPasswordDialog">
        <div class="fixed inset-0 z-50 bg-black/50 backdrop-blur-sm" @click="closePasswordDialog"></div>
        <div class="fixed z-50 top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[320px] bg-[var(--c-surface)] rounded-2xl overflow-hidden shadow-xl">
          <div class="px-5 pt-6 pb-3 text-center">
            <h3 class="text-base font-bold">{{ t('card.info.dialogTitle') }}</h3>
          </div>
          <div class="px-5 pb-5">
            <p class="text-sm text-[var(--c-text-2)] mb-3">{{ t('card.info.dialogDescription') }}</p>
            <input
              v-model="verifyPassword"
              type="password"
              :placeholder="t('card.info.dialogPlaceholder')"
              maxlength="20"
              class="w-full px-3 py-2.5 border border-[var(--c-border)] rounded-lg text-sm focus:border-[var(--c-primary)] focus:ring-2 focus:ring-[var(--c-primary)]/10 outline-none bg-[var(--c-surface)]"
              @keyup.enter="confirmReportLoss"
            />
          </div>
          <div class="flex border-t border-[var(--c-border)]">
            <button
              class="flex-1 py-3.5 text-center text-sm text-[var(--c-text-2)] border-r border-[var(--c-border)]"
              @click="closePasswordDialog"
            >{{ t('common.cancel') }}</button>
            <button
              class="flex-1 py-3.5 text-center text-sm font-semibold text-[var(--c-primary)]"
              @click="confirmReportLoss"
            >{{ t('common.confirm') }}</button>
          </div>
        </div>
      </template>
    </Teleport>
  </div>
</template>
