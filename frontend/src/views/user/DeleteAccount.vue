<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import request from '../../utils/request'
import { useToast } from '@/composables/useToast'
import AppDialog from '@/components/ui/AppDialog.vue'
import { AlertTriangle, ChevronLeft } from 'lucide-vue-next'

const router = useRouter()
const { t } = useI18n()
const { success: toastSuccess } = useToast()
const password = ref('')
const agreed = ref(false)
const showConfirmDialog = ref(false)
const deleting = ref(false)

function handleDeleteClick() {
  if (!agreed.value || !password.value) return
  showConfirmDialog.value = true
}

function handleCancel() {
  showConfirmDialog.value = false
}

async function handleConfirmDelete() {
  showConfirmDialog.value = false
  deleting.value = true

  try {
    await request.post('/close/submit', { password: password.value })
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
  <div class="subpage delete-account-page min-h-screen pb-6">
    <!-- Sticky Header -->
    <div class="subpage-bar delete-account-page__header">
      <button type="button" class="subpage-bar__back" @click="router.back()">
        <ChevronLeft :size="18" aria-hidden="true" />
        <span>{{ t('common.back') }}</span>
      </button>
      <h1 class="subpage-bar__title">{{ t('profile.deleteAccount') }}</h1>
      <span aria-hidden="true"></span>
    </div>

    <div class="subpage-body max-w-lg mx-auto px-4 py-6">
      <!-- Warning header -->
      <div class="delete-account-card rounded-xl p-8 text-center mb-3">
        <div class="delete-account-card__alert text-6xl mb-5"><AlertTriangle class="w-16 h-16 mx-auto" /></div>
        <h2 class="text-lg font-semibold text-[var(--c-text-1)] leading-snug">{{ t('deleteAccount.warningTitle') }}</h2>
      </div>

      <label class="block mb-3">
        <span>{{ t('loginPage.passwordLabel') }}</span>
        <input v-model="password" type="password" autocomplete="current-password" class="w-full rounded-lg p-3" />
      </label>
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
  background: var(--c-bg);
}

.delete-account-card {
  border: 1px solid var(--c-border);
  background: var(--c-surface);
  box-shadow: none;
}

.delete-account-card:first-child {
  border-color: color-mix(in srgb, var(--c-danger) 28%, var(--c-border));
  background: color-mix(in srgb, var(--c-danger) 6%, var(--c-surface));
}

.delete-account-card__alert {
  color: var(--c-danger);
}

.delete-account-card__alert :deep(svg) {
  width: 44px;
  height: 44px;
}

.delete-account-risk-item {
  color: var(--c-text-2);
}

.delete-account-risk-item::before {
  content: '';
  position: absolute;
  top: 0.65em;
  left: 4px;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--c-danger);
}

.delete-account-card__warning {
  border-top: 1px solid var(--c-divider);
  color: var(--c-danger);
}

.delete-account-checkbox {
  border: 1.5px solid var(--c-border);
  background: var(--c-surface);
}

.peer:checked + .delete-account-checkbox {
  border-color: var(--c-danger);
  background: var(--c-danger);
}

.peer:focus-visible + .delete-account-checkbox {
  outline: 2px solid var(--c-danger);
  outline-offset: 2px;
}

.delete-account-button {
  min-height: 48px;
  border-radius: var(--radius-control);
  background: var(--c-fill-3);
  color: var(--c-text-3) !important;
}

.delete-account-button--enabled {
  background: var(--c-danger);
  color: #fff !important;
}

.delete-account-button--enabled:hover {
  background: color-mix(in srgb, var(--c-danger) 88%, #000);
}
</style>
