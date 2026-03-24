<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useToast } from '@/composables/useToast'

const router = useRouter()
const { t } = useI18n()
const { error: showError } = useToast()
const name = ref('')
const candidateNo = ref('')
const idNo = ref('')

function doQuery() {
  if (!name.value.trim() || !candidateNo.value.trim() || !idNo.value.trim()) {
    showError(t('graduateExam.fillAllFields'))
    return
  }
  router.push({
    path: '/kaoyan/result',
    query: { name: name.value.trim(), candidateNo: candidateNo.value.trim(), idNo: idNo.value.trim() }
  })
}
</script>

<template>
  <div class="min-h-screen bg-[var(--c-bg)]">
    <!-- Sticky header -->
    <div class="sticky top-0 z-30 flex items-center h-[52px] px-5 bg-[var(--c-surface)]/90 backdrop-blur-xl border-b border-[var(--c-border)]">
      <button @click="$router.back()" class="text-[var(--c-primary)] text-sm font-medium">&larr; {{ t('graduateExam.back') }}</button>
      <span class="flex-1 text-center text-sm font-bold">{{ t('graduateExam.title') }}</span>
      <div class="w-10"></div>
    </div>

    <div class="max-w-lg mx-auto px-4 py-6">
      <!-- Form card -->
      <div class="bg-[var(--c-surface)] rounded-2xl border border-[var(--c-border)] divide-y divide-[var(--c-border)]">
        <!-- Name -->
        <div class="flex items-center px-4 h-[52px]">
          <label class="w-20 shrink-0 text-sm text-[var(--c-text)]">{{ t('graduateExam.name') }}</label>
          <input
            v-model="name"
            type="text"
            :placeholder="t('graduateExam.namePlaceholder')"
            class="flex-1 text-sm bg-transparent text-[var(--c-text)] placeholder:text-[var(--c-text-tertiary)] outline-none"
          />
        </div>
        <!-- Candidate No -->
        <div class="flex items-center px-4 h-[52px]">
          <label class="w-20 shrink-0 text-sm text-[var(--c-text)]">{{ t('graduateExam.candidateNo') }}</label>
          <input
            v-model="candidateNo"
            type="text"
            maxlength="15"
            :placeholder="t('graduateExam.candidateNoPlaceholder')"
            class="flex-1 text-sm bg-transparent text-[var(--c-text)] placeholder:text-[var(--c-text-tertiary)] outline-none"
          />
        </div>
        <!-- ID No -->
        <div class="flex items-center px-4 h-[52px]">
          <label class="w-20 shrink-0 text-sm text-[var(--c-text)]">{{ t('graduateExam.idNo') }}</label>
          <input
            v-model="idNo"
            type="text"
            maxlength="18"
            :placeholder="t('graduateExam.idNoPlaceholder')"
            class="flex-1 text-sm bg-transparent text-[var(--c-text)] placeholder:text-[var(--c-text-tertiary)] outline-none"
          />
        </div>
      </div>

      <!-- Search button -->
      <button
        type="button"
        class="mt-6 w-full py-3 rounded-xl bg-[var(--c-primary)] text-white text-[15px] font-medium active:opacity-80 transition-opacity"
        @click="doQuery"
      >
        {{ t('graduateExam.search') }}
      </button>

      <p class="mt-4 text-center text-sm text-[var(--c-text-secondary)]">{{ t('graduateExam.wish') }}</p>

      <!-- External link -->
      <div class="mt-8">
        <p class="text-xs text-[var(--c-text-secondary)] mb-2">{{ t('graduateExam.altEntry') }}</p>
        <a
          href="https://yz.chsi.com.cn/apply/cjcxa/"
          target="_blank"
          rel="noopener noreferrer"
          class="flex items-center justify-between bg-[var(--c-surface)] rounded-2xl border border-[var(--c-border)] px-4 py-3.5 text-sm text-[var(--c-text)] active:bg-black/5 transition-colors"
        >
          <span>{{ t('graduateExam.chsiLink') }}</span>
          <span class="text-[var(--c-text-tertiary)]">&rsaquo;</span>
        </a>
      </div>
    </div>
  </div>
</template>
