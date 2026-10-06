<script setup>
import { ChevronLeft } from 'lucide-vue-next'
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
  <div class="subpage min-h-screen bg-[var(--c-bg)]">
    <!-- Sticky header -->
    <div class="subpage-bar">
      <button type="button" class="subpage-bar__back" @click="$router.back()">
        <ChevronLeft :size="18" aria-hidden="true" />
        <span>{{ t('graduateExam.back') }}</span>
      </button>
      <span class="subpage-bar__title">{{ t('graduateExam.title') }}</span>
      <span aria-hidden="true"></span>
    </div>

    <div class="subpage-body max-w-lg mx-auto px-4 py-6">
      <!-- Form card -->
      <div class="ui-panel bg-[var(--c-surface)] rounded-2xl border border-[var(--c-border)] divide-y divide-[var(--c-border)]">
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
        class="ui-btn-primary mt-6 w-full py-3 rounded-xl bg-[var(--c-primary)] text-[var(--c-on-primary)] text-[15px] font-medium active:opacity-80 transition-opacity"
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
          class="ui-panel flex items-center justify-between bg-[var(--c-surface)] rounded-2xl border border-[var(--c-border)] px-4 py-3.5 text-sm text-[var(--c-text)] hover:bg-[var(--c-surface-hover)] transition-colors"
        >
          <span>{{ t('graduateExam.chsiLink') }}</span>
          <span class="text-[var(--c-text-tertiary)]">&rsaquo;</span>
        </a>
      </div>
    </div>
  </div>
</template>
