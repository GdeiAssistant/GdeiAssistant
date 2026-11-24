<script setup>
import AppLoading from '@/components/ui/AppLoading.vue'
import { ChevronLeft } from 'lucide-vue-next'
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { getBorrowedBooks, renewBook } from '@/api/collection'
import { useToast } from '@/composables/useToast'
import AppButton from '@/components/ui/AppButton.vue'
import AppInput from '@/components/ui/AppInput.vue'

const router = useRouter()
const { t } = useI18n()
const { error: showError, success: showSuccess, loading: showLoading, hideLoading } = useToast()

const borrowLoading = ref(false)
const borrowList = ref([])
const borrowPassword = ref('')
const hasQueriedBorrow = ref(false)
const showPasswordDialog = ref(false)
const verifyPassword = ref('')
const renewingItem = ref(null)

function goBack() {
  router.back()
}

function fetchBorrowed() {
  const password = (borrowPassword.value || '').trim()
  if (!password) {
    showError(t('libraryPage.borrow.passwordRequired'))
    return
  }
  borrowLoading.value = true
  hasQueriedBorrow.value = true
  getBorrowedBooks(password).then((res) => {
    if (res && res.success && Array.isArray(res.data)) {
      borrowList.value = res.data
    } else {
      borrowList.value = []
    }
  }).catch(() => {
    borrowList.value = []
  }).finally(() => {
    borrowLoading.value = false
  })
}

function isReturnDateUrgent(returnDateStr) {
  if (!returnDateStr) return false
  const s = String(returnDateStr).trim()
  const match = s.match(/(\d{4})-(\d{2})-(\d{2})/)
  if (!match) return false
  const due = new Date(parseInt(match[1], 10), parseInt(match[2], 10) - 1, parseInt(match[3], 10))
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  due.setHours(0, 0, 0, 0)
  const diffDays = Math.ceil((due - today) / (24 * 60 * 60 * 1000))
  return diffDays < 0 || diffDays <= 3
}

function onRenew(item) {
  if (!item || item.sn == null || item.code == null) return
  renewingItem.value = item
  verifyPassword.value = ''
  showPasswordDialog.value = true
}

function closePasswordDialog() {
  showPasswordDialog.value = false
  verifyPassword.value = ''
  renewingItem.value = null
}

function confirmRenew() {
  const pwd = (verifyPassword.value || '').trim()
  if (!pwd) {
    showError(t('libraryPage.borrow.passwordRequired'))
    return
  }
  const item = renewingItem.value
  if (!item || item.sn == null || item.code == null) return
  closePasswordDialog()
  showLoading(t('libraryPage.borrow.renewLoading'))
  renewBook({ sn: item.sn, code: item.code, password: pwd }).then(() => {
    borrowPassword.value = pwd
    hideLoading()
    showSuccess(t('libraryPage.borrow.renewSuccess'))
    fetchBorrowed()
  }).catch(() => {
    hideLoading()
  })
}
</script>

<template>
  <div class="subpage min-h-screen bg-[var(--c-bg)]">
    <div class="subpage-bar">
      <button type="button" class="subpage-bar__back" @click="goBack">
        <ChevronLeft :size="18" aria-hidden="true" />
        <span>{{ t('common.back') }}</span>
      </button>
      <span class="subpage-bar__title">{{ t('libraryPage.borrow.title') }}</span>
      <span aria-hidden="true"></span>
    </div>

    <div class="subpage-body max-w-lg mx-auto px-4 py-6">
      <p class="text-center text-sm text-[var(--c-text-2)] mb-5">{{ t('libraryPage.borrow.intro') }}</p>

      <!-- Password form -->
      <div class="ui-panel bg-[var(--c-surface)] rounded-2xl p-5 shadow-sm border border-[var(--c-border)]">
        <div>
          <label class="text-sm font-medium text-[var(--c-text-2)] mb-1.5 block">{{ t('libraryPage.borrow.passwordLabel') }}</label>
          <AppInput
            v-model="borrowPassword"
            type="password"
            :placeholder="t('libraryPage.borrow.passwordPlaceholder')"
            maxlength="20"
            @keyup.enter="fetchBorrowed"
          />
        </div>

        <AppButton
          type="button"
          class="ui-btn-primary w-full py-2.5 mt-6 transition-opacity"
          @click="fetchBorrowed"
        >{{ hasQueriedBorrow ? t('libraryPage.borrow.refresh') : t('libraryPage.borrow.query') }}</AppButton>
      </div>

      <!-- Loading state -->
      <template v-if="borrowLoading && borrowList.length === 0">
        <AppLoading :label="t('common.loading')" class="py-10" />
      </template>

      <template v-else>
        <!-- Initial prompt -->
        <div v-if="!hasQueriedBorrow" class="flex items-center justify-center py-10 text-sm text-[var(--c-text-3)]">
          {{ t('libraryPage.borrow.initialHint') }}
        </div>

        <template v-else>
          <h3 class="text-xs font-semibold text-[var(--c-text-2)] uppercase tracking-wide mt-6 mb-2">{{ t('libraryPage.borrow.recordsTitle') }}</h3>

          <div class="space-y-3">
            <div
              v-for="(item, index) in borrowList"
              :key="item.id || index"
              class="ui-panel bg-[var(--c-surface)] rounded-2xl p-5 shadow-sm border border-[var(--c-border)]"
            >
              <div class="divide-y divide-[var(--c-border-light)]">
                <div class="flex justify-between py-2.5">
                  <span class="text-sm text-[var(--c-text-2)] shrink-0">{{ t('libraryPage.borrow.bookName') }}</span>
                  <span class="text-sm font-medium text-[var(--c-text)] text-right ml-3 break-all">{{ item.name || '—' }}</span>
                </div>
                <div class="flex justify-between py-2.5">
                  <span class="text-sm text-[var(--c-text-2)]">{{ t('libraryPage.borrow.author') }}</span>
                  <span class="text-sm font-medium text-[var(--c-text)]">{{ item.author || '—' }}</span>
                </div>
                <div class="flex justify-between py-2.5">
                  <span class="text-sm text-[var(--c-text-2)]">{{ t('libraryPage.borrow.borrowDate') }}</span>
                  <span class="text-sm font-medium text-[var(--c-text)]">{{ item.borrowDate || '—' }}</span>
                </div>
                <div class="flex justify-between py-2.5">
                  <span class="text-sm text-[var(--c-text-2)]">{{ t('libraryPage.borrow.returnDate') }}</span>
                  <span
                    class="text-sm font-medium"
                    :class="isReturnDateUrgent(item.returnDate) ? 'text-danger' : 'text-[var(--c-text)]'"
                  >{{ item.returnDate || '—' }}</span>
                </div>
                <div class="flex justify-between py-2.5">
                  <span class="text-sm text-[var(--c-text-2)]">{{ t('libraryPage.borrow.renewCount') }}</span>
                  <span class="text-sm font-medium text-[var(--c-text)]">{{ item.renewTime != null ? item.renewTime : '—' }}</span>
                </div>
              </div>
              <button
                type="button"
                class="w-full mt-4 py-2 text-sm font-semibold text-[var(--c-primary)] border border-[var(--c-primary)]/20 rounded-lg hover:bg-[var(--c-primary)]/5 transition-colors"
                @click="onRenew(item)"
              >{{ t('libraryPage.borrow.renew') }}</button>
            </div>
          </div>

          <div v-if="!borrowLoading && borrowList.length === 0" class="flex items-center justify-center py-10 text-sm text-[var(--c-text-3)]">
            {{ t('libraryPage.borrow.empty') }}
          </div>
        </template>
      </template>
    </div>

    <!-- Renew password dialog -->
    <template v-if="showPasswordDialog">
      <div class="ui-scrim fixed inset-0 z-50 bg-black/40" @click="closePasswordDialog"></div>
      <div class="ui-modal fixed z-50 top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[calc(100%-48px)] max-w-sm bg-[var(--c-surface)] rounded-2xl p-6 shadow-xl">
        <h3 class="text-base font-bold text-center text-[var(--c-text)]">{{ t('libraryPage.borrow.renewDialogTitle') }}</h3>
        <p class="mt-2 text-sm text-center text-[var(--c-text-2)]">{{ t('libraryPage.borrow.renewDialogDescription') }}</p>
        <AppInput
          v-model="verifyPassword"
          type="password"
          :placeholder="t('libraryPage.borrow.renewDialogPlaceholder')"
          maxlength="20"
          class="mt-4"
          @keyup.enter="confirmRenew"
        />
        <div class="flex gap-3 mt-5">
          <button
            type="button"
            class="flex-1 py-2.5 text-sm font-semibold border border-[var(--c-border)] rounded-lg hover:bg-[var(--c-surface-hover)] transition-colors"
            @click="closePasswordDialog"
          >{{ t('common.cancel') }}</button>
          <AppButton
            type="button"
            class="ui-btn-primary flex-1 py-2.5 transition-opacity"
            @click="confirmRenew"
          >{{ t('common.confirm') }}</AppButton>
        </div>
      </div>
    </template>
  </div>
</template>
