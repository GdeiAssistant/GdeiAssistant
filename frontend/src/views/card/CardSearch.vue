<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useToast } from '@/composables/useToast'

function getTodayYYYYMMDD() {
  const d = new Date()
  return d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0') + '-' + String(d.getDate()).padStart(2, '0')
}

const router = useRouter()
const { t } = useI18n()
const { error: showError } = useToast()
const today = getTodayYYYYMMDD()
const queryDate = ref(today)

const doSearch = () => {
  const d = (queryDate.value || '').trim()
  if (!d) {
    showError(t('card.search.dateRequired'))
    return
  }
  router.push({ path: '/card/list', query: { date: d } })
}
</script>

<template>
  <div class="min-h-screen bg-[var(--c-bg)]">
    <div class="sticky top-0 z-30 flex items-center h-[52px] px-5 bg-[var(--c-surface)]/90 backdrop-blur-xl border-b border-[var(--c-border)]">
      <button @click="$router.back()" class="text-[var(--c-primary)] text-sm font-medium">&larr; {{ t('common.back') }}</button>
      <span class="flex-1 text-center text-sm font-bold">{{ t('card.action.records.title') }}</span>
      <div class="w-10"></div>
    </div>

    <div class="max-w-lg mx-auto px-4 py-6">
      <p class="text-center text-sm text-[var(--c-text-2)] mb-5">{{ t('card.search.description') }}</p>

      <div class="bg-[var(--c-surface)] rounded-2xl p-5 shadow-sm border border-[var(--c-border)]">
        <div>
          <label class="text-sm font-medium text-[var(--c-text-2)] mb-1.5 block">{{ t('card.search.dateLabel') }}</label>
          <input
            v-model="queryDate"
            type="date"
            :max="today"
            :placeholder="t('card.search.datePlaceholder')"
            class="w-full px-3 py-2.5 border border-[var(--c-border)] rounded-lg text-sm focus:border-[var(--c-primary)] focus:ring-2 focus:ring-[var(--c-primary)]/10 outline-none bg-[var(--c-surface)]"
          />
        </div>

        <button
          type="button"
          class="w-full bg-[var(--c-primary)] text-white rounded-lg py-2.5 font-semibold mt-6 transition-opacity hover:opacity-90"
          @click="doSearch"
        >{{ t('card.search.submit') }}</button>
      </div>
    </div>
  </div>
</template>
