<script setup>
import { postEvaluateSubmit } from "../../api/evaluateEndpoints.js"

import { ChevronLeft } from 'lucide-vue-next'
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'

import { useToast } from '@/composables/useToast'

const router = useRouter()
const { t } = useI18n()
const { success, error: showError, loading: showLoading, hideLoading } = useToast()
const isDirectSubmit = ref(false)
const isLoading = ref(false)
const showEvaluateConfirmDialog = ref(false)

function doEvaluate() {
  showEvaluateConfirmDialog.value = true
}

function closeEvaluateConfirmDialog() {
  showEvaluateConfirmDialog.value = false
}

function confirmEvaluate() {
  if (isLoading.value) return
  closeEvaluateConfirmDialog()
  isLoading.value = true
  showLoading(t('evaluatePage.loading'))
  const formData = { directSubmit: isDirectSubmit.value }
  postEvaluateSubmit(formData)
    .then(() => {
      isLoading.value = false
      hideLoading()
      success(t('evaluatePage.success'))
    })
    .catch(() => {
      isLoading.value = false
      hideLoading()
      showError(t('evaluatePage.error'))
    })
}
</script>

<template>
  <div class="subpage min-h-screen bg-[var(--c-bg)]">
    <!-- Sticky header -->
    <div class="subpage-bar">
      <button type="button" class="subpage-bar__back" @click="$router.back()">
        <ChevronLeft :size="18" aria-hidden="true" />
        <span>{{ t('common.back') }}</span>
      </button>
      <span class="subpage-bar__title">{{ t('evaluatePage.title') }}</span>
      <span aria-hidden="true"></span>
    </div>

    <!-- Content -->
    <div class="subpage-body max-w-lg mx-auto px-4 py-6">
      <div class="ui-panel bg-[var(--c-surface)] rounded-2xl border border-[var(--c-border)] overflow-hidden">
        <!-- Toggle row -->
        <div class="flex items-center justify-between px-4 py-4">
          <span class="text-[15px] text-[var(--c-text)]">{{ t('evaluatePage.directSubmit') }}</span>
          <label class="relative inline-flex items-center cursor-pointer">
            <input type="checkbox" v-model="isDirectSubmit" class="sr-only peer" />
            <div class="w-11 h-6 bg-[var(--c-fill-3)] rounded-full peer-checked:bg-[var(--c-primary)] transition-colors after:content-[''] after:absolute after:top-0.5 after:left-[2px] after:bg-white after:rounded-full after:h-5 after:w-5 after:shadow after:transition-transform peer-checked:after:translate-x-5"></div>
          </label>
        </div>
      </div>

      <!-- Submit button -->
      <button
        type="button"
        :disabled="isLoading"
        class="ui-btn-primary mt-6 w-full py-3 rounded-xl bg-[var(--c-primary)] text-[var(--c-on-primary)] text-[15px] font-medium active:opacity-80 disabled:opacity-50 transition-opacity"
        @click="doEvaluate"
      >
        {{ t('evaluatePage.submitAction') }}
      </button>

      <p class="mt-4 text-center text-xs text-[var(--c-text-secondary)]">
        {{ t('evaluatePage.riskNotice') }}
      </p>
    </div>

    <!-- Confirm dialog -->
    <Teleport to="body">
      <template v-if="showEvaluateConfirmDialog">
        <div class="ui-scrim fixed inset-0 z-50 bg-black/50" @click="closeEvaluateConfirmDialog"></div>
        <div class="ui-modal fixed z-50 top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[300px] bg-[var(--c-surface)] rounded-2xl overflow-hidden shadow-xl">
          <div class="pt-6 pb-3 px-5 text-center">
            <h3 class="text-base font-semibold text-[var(--c-text)]">{{ t('common.hint') }}</h3>
          </div>
          <div class="px-5 pb-5 text-center text-sm text-[var(--c-text-secondary)] leading-relaxed">
            {{ t('evaluatePage.confirmMessage') }}
          </div>
          <div class="flex border-t border-[var(--c-border)]">
            <button
              class="flex-1 py-3.5 text-center text-[15px] text-[var(--c-text-secondary)] border-r border-[var(--c-border)] hover:bg-[var(--c-surface-hover)]"
              @click="closeEvaluateConfirmDialog"
            >{{ t('common.cancel') }}</button>
            <button
              class="flex-1 py-3.5 text-center text-[15px] text-[var(--c-primary)] font-medium hover:bg-[var(--c-surface-hover)] disabled:opacity-40"
              :disabled="isLoading"
              @click="confirmEvaluate"
            >{{ t('common.confirm') }}</button>
          </div>
        </div>
      </template>
    </Teleport>
  </div>
</template>
