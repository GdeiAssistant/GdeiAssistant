<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import request from '../../utils/request'
import { useToast } from '@/composables/useToast'
import AppDialog from '@/components/ui/AppDialog.vue'
import { AlertTriangle } from 'lucide-vue-next'

const router = useRouter()
const { t } = useI18n()
const { success: toastSuccess } = useToast()
const agreed = ref(false)
const showConfirmDialog = ref(false)
const deleting = ref(false)

function handleDeleteClick() {
  if (!agreed.value) return
  showConfirmDialog.value = true
}

function handleCancel() {
  showConfirmDialog.value = false
}

async function handleConfirmDelete() {
  showConfirmDialog.value = false
  deleting.value = true

  try {
    await request.post('/close/submit')
    toastSuccess(t('deleteAccount.success'))

    // 清除登录态
    localStorage.clear()
    sessionStorage.clear()

    // 延迟跳转，让Toast有时间显示
    setTimeout(() => {
      router.replace('/login')
    }, 1500)
  } catch (e) {
    deleting.value = false
    // 错误由 request.js 全局拦截器统一提示
  }
}
</script>

<template>
  <div class="delete-account-page min-h-screen pb-6">
    <!-- Sticky Header -->
    <div class="delete-account-page__header sticky top-0 z-10 flex items-center h-12 px-4">
      <button type="button" class="w-15 text-sm text-[var(--c-text-2)] text-left cursor-pointer bg-transparent border-0" @click="router.back()">{{ t('common.back') }}</button>
      <h1 class="flex-1 text-center text-base font-medium text-[var(--c-text-1)] m-0">{{ t('profile.deleteAccount') }}</h1>
      <div class="w-15"></div>
    </div>

    <div class="max-w-lg mx-auto px-4 py-6">
      <!-- Warning header -->
      <div class="delete-account-card rounded-xl p-8 text-center mb-3">
        <div class="delete-account-card__alert text-6xl mb-5"><AlertTriangle class="w-16 h-16 mx-auto" /></div>
        <h2 class="text-lg font-semibold text-[var(--c-text-1)] leading-snug">{{ t('deleteAccount.warningTitle') }}</h2>
      </div>

      <!-- Risk list -->
      <div class="delete-account-card rounded-xl p-5 mb-3">
        <p class="text-[15px] font-medium text-[var(--c-text-1)] mb-4">{{ t('deleteAccount.riskTitle') }}</p>
        <ul class="space-y-2 mb-4">
          <li class="delete-account-risk-item text-sm leading-relaxed pl-5 relative">{{ t('deleteAccount.risk.account') }}</li>
          <li class="delete-account-risk-item text-sm leading-relaxed pl-5 relative">{{ t('deleteAccount.risk.posts') }}</li>
          <li class="delete-account-risk-item text-sm leading-relaxed pl-5 relative">{{ t('deleteAccount.risk.interactions') }}</li>
          <li class="delete-account-risk-item text-sm leading-relaxed pl-5 relative">{{ t('deleteAccount.risk.academic') }}</li>
          <li class="delete-account-risk-item text-sm leading-relaxed pl-5 relative">{{ t('deleteAccount.risk.community') }}</li>
          <li class="delete-account-risk-item text-sm leading-relaxed pl-5 relative">{{ t('deleteAccount.risk.identity') }}</li>
        </ul>
        <p class="delete-account-card__warning text-[13px] font-medium pt-4">{{ t('deleteAccount.irreversible') }}</p>
      </div>

      <!-- Agreement checkbox -->
      <div class="delete-account-card rounded-xl p-4 mb-5">
        <label class="flex items-start gap-3 cursor-pointer">
          <div class="relative mt-0.5">
            <input
              type="checkbox"
              class="sr-only peer"
              v-model="agreed"
            />
            <div class="delete-account-checkbox w-5 h-5 rounded flex items-center justify-center after:content-[''] after:hidden peer-checked:after:block after:w-[5px] after:h-[10px] after:border-white after:border-r-2 after:border-b-2 after:rotate-45 after:-mt-0.5"></div>
          </div>
          <span class="text-sm text-[var(--c-text-2)] leading-relaxed">{{ t('deleteAccount.agreement') }}</span>
        </label>
      </div>

      <!-- Delete button -->
      <button
        type="button"
        class="delete-account-button w-full rounded-lg text-white text-[17px] font-medium py-3 flex items-center justify-center cursor-pointer disabled:opacity-60 disabled:cursor-not-allowed"
        :class="{ 'delete-account-button--enabled': agreed && !deleting }"
        :disabled="!agreed || deleting"
        @click="handleDeleteClick"
      >
        <template v-if="deleting">
          <span class="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin mr-2"></span>
          {{ t('deleteAccount.deleting') }}
        </template>
        <template v-else>{{ t('deleteAccount.confirm') }}</template>
      </button>
    </div>

    <AppDialog
      :open="showConfirmDialog"
      :title="t('deleteAccount.finalConfirmTitle')"
      :description="t('deleteAccount.finalConfirmDescription')"
      :confirm-text="t('deleteAccount.finalConfirmAction')"
      confirm-tone="danger"
      @close="handleCancel"
      @confirm="handleConfirmDelete"
    />
  </div>
</template>

<style scoped>
.delete-account-page {
  background:
    radial-gradient(circle at 18% 10%, rgba(255, 214, 214, 0.22), transparent 26%),
    radial-gradient(circle at 82% 8%, rgba(255, 234, 214, 0.18), transparent 24%),
    var(--c-bg);
}

.delete-account-page__header {
  border-bottom: 1px solid color-mix(in srgb, var(--c-danger) 10%, var(--c-border));
  background: color-mix(in srgb, rgba(255, 255, 255, 0.88) 88%, transparent);
  backdrop-filter: blur(14px);
}

.delete-account-card {
  border: 1px solid color-mix(in srgb, var(--c-danger) 8%, var(--c-border));
  background:
    linear-gradient(135deg, rgba(255, 255, 255, 0.92), rgba(255, 250, 250, 0.88)),
    radial-gradient(circle at 100% 0, rgba(248, 113, 113, 0.08), transparent 32%);
  box-shadow: 0 16px 36px rgba(36, 48, 63, 0.08);
}

.delete-account-card__alert {
  color: color-mix(in srgb, var(--c-danger) 82%, #dc2626);
}

.delete-account-risk-item {
  color: var(--c-text-2);
}

.delete-account-risk-item::before {
  content: '•';
  position: absolute;
  left: 0;
  color: color-mix(in srgb, var(--c-danger) 44%, var(--c-text-3));
  font-weight: 700;
}

.delete-account-card__warning {
  border-top: 1px solid color-mix(in srgb, var(--c-danger) 10%, var(--c-border));
  color: color-mix(in srgb, var(--c-danger) 72%, #b91c1c);
}

.delete-account-checkbox {
  border: 1px solid color-mix(in srgb, var(--c-danger) 10%, var(--c-border));
  background: rgba(255, 255, 255, 0.92);
}

.peer:checked + .delete-account-checkbox {
  border-color: color-mix(in srgb, var(--c-danger) 30%, transparent);
  background: linear-gradient(135deg, #ef4444, #dc2626);
}

.delete-account-button {
  background: color-mix(in srgb, var(--c-text-3) 22%, #cbd5e1);
}

.delete-account-button--enabled {
  background: linear-gradient(135deg, #ef4444, #dc2626);
  box-shadow: 0 14px 30px rgba(220, 38, 38, 0.18);
}

[data-theme="dark"] .delete-account-page {
  background:
    radial-gradient(circle at 16% 8%, rgba(127, 29, 29, 0.16), transparent 28%),
    radial-gradient(circle at 84% 10%, rgba(180, 83, 9, 0.1), transparent 24%),
    var(--c-bg);
}

[data-theme="dark"] .delete-account-page__header {
  border-bottom-color: color-mix(in srgb, var(--c-danger) 12%, rgba(68, 89, 112, 0.74));
  background: rgba(20, 27, 37, 0.82);
}

[data-theme="dark"] .delete-account-card {
  border-color: color-mix(in srgb, var(--c-danger) 12%, rgba(68, 89, 112, 0.74));
  background:
    linear-gradient(135deg, rgba(20, 27, 37, 0.9), rgba(28, 22, 24, 0.9)),
    radial-gradient(circle at 100% 0, rgba(248, 113, 113, 0.1), transparent 32%);
  box-shadow: 0 18px 40px rgba(0, 0, 0, 0.26);
}

[data-theme="dark"] .delete-account-checkbox {
  border-color: color-mix(in srgb, var(--c-danger) 10%, rgba(68, 89, 112, 0.74));
  background: rgba(31, 41, 55, 0.88);
}

[data-theme="dark"] .delete-account-button {
  background: rgba(71, 85, 105, 0.78);
}

[data-theme="dark"] .delete-account-button--enabled {
  background: linear-gradient(135deg, rgba(220, 38, 38, 0.92), rgba(153, 27, 27, 0.96));
  box-shadow: 0 16px 32px rgba(127, 29, 29, 0.24);
}
</style>
