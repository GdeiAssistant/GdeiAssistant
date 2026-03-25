<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { getBorrowedBooks, renewBook } from '@/api/collection'
import { useToast } from '@/composables/useToast'

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
  <div class="min-h-screen bg-[var(--c-bg)]">
    <div class="sticky top-0 z-30 flex items-center h-[52px] px-5 bg-[var(--c-surface)]/90 backdrop-blur-xl border-b border-[var(--c-border)]">
      <button @click="goBack" class="text-[var(--c-primary)] text-sm font-medium">&larr; {{ t('common.back') }}</button>
      <span class="flex-1 text-center text-sm font-bold">{{ t('libraryPage.borrow.title') }}</span>
      <div class="w-10"></div>
    </div>

    <div class="max-w-lg mx-auto px-4 py-6">
      <p class="text-center text-sm text-[var(--c-text-2)] mb-5">{{ t('libraryPage.borrow.intro') }}<br><span class="text-xs text-[var(--c-text-3)]">{{ t('libraryPage.borrow.mockPassword') }}</span></p>

      <!-- Password form -->
      <div class="bg-[var(--c-surface)] rounded-2xl p-5 shadow-sm border border-[var(--c-border)]">
        <div>
          <label class="text-sm font-medium text-[var(--c-text-2)] mb-1.5 block">{{ t('libraryPage.borrow.passwordLabel') }}</label>
          <input
            v-model="borrowPassword"
            type="password"
            :placeholder="t('libraryPage.borrow.passwordPlaceholder')"
            maxlength="20"
            class="w-full px-3 py-2.5 border border-[var(--c-border)] rounded-lg text-sm focus:border-[var(--c-primary)] focus:ring-2 focus:ring-[var(--c-primary)]/10 outline-none bg-[var(--c-surface)]"
            @keyup.enter="fetchBorrowed"
          />
        </div>

        <button
          type="button"
          class="w-full bg-[var(--c-primary)] text-white rounded-lg py-2.5 font-semibold mt-6 transition-opacity hover:opacity-90"
          @click="fetchBorrowed"
        >{{ hasQueriedBorrow ? t('libraryPage.borrow.refresh') : t('libraryPage.borrow.query') }}</button>
      </div>

      <!-- Loading state -->
      <template v-if="borrowLoading && borrowList.length === 0">
        <div class="flex items-center justify-center gap-2 py-10 text-sm text-[var(--c-text-2)]">
          <span class="inline-block w-5 h-5 border-2 border-[var(--c-primary)] border-t-transparent rounded-full animate-spin"></span>
          {{ t('common.loading') }}
        </div>
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
              class="bg-[var(--c-surface)] rounded-2xl p-5 shadow-sm border border-[var(--c-border)]"
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
                    :class="isReturnDateUrgent(item.returnDate) ? 'text-red-500' : 'text-[var(--c-text)]'"
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
      <div class="fixed inset-0 z-50 bg-black/40" @click="closePasswordDialog"></div>
      <div class="fixed z-50 top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[calc(100%-48px)] max-w-sm bg-[var(--c-surface)] rounded-2xl p-6 shadow-xl">
        <h3 class="text-base font-bold text-center text-[var(--c-text)]">{{ t('libraryPage.borrow.renewDialogTitle') }}</h3>
        <p class="mt-2 text-sm text-center text-[var(--c-text-2)]">{{ t('libraryPage.borrow.renewDialogDescription') }}</p>
        <input
          v-model="verifyPassword"
          type="password"
          :placeholder="t('libraryPage.borrow.renewDialogPlaceholder')"
          maxlength="20"
          class="mt-4 w-full px-3 py-2.5 border border-[var(--c-border)] rounded-lg text-sm focus:border-[var(--c-primary)] focus:ring-2 focus:ring-[var(--c-primary)]/10 outline-none bg-[var(--c-surface)]"
          @keyup.enter="confirmRenew"
        />
        <div class="flex gap-3 mt-5">
          <button
            type="button"
            class="flex-1 py-2.5 text-sm font-semibold border border-[var(--c-border)] rounded-lg hover:bg-[var(--c-surface-hover)] transition-colors"
            @click="closePasswordDialog"
          >{{ t('common.cancel') }}</button>
          <button
            type="button"
            class="flex-1 py-2.5 text-sm font-semibold text-white bg-[var(--c-primary)] rounded-lg hover:opacity-90 transition-opacity"
            @click="confirmRenew"
          >{{ t('common.confirm') }}</button>
        </div>
      </div>
    </template>
  </div>
</template>
