<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useToast } from '@/composables/useToast'

const router = useRouter()
const { t } = useI18n()
const { error: showError } = useToast()
const keyword = ref('')

const doSearch = () => {
  const k = keyword.value.trim()
  if (!k) {
    showError(t('libraryPage.search.keywordRequired'))
    return
  }
  router.push({ path: '/library/list', query: { keyword: k } })
}

function goBack() {
  router.back()
}
</script>

<template>
  <div class="min-h-screen bg-[var(--c-bg)]">
    <div class="sticky top-0 z-30 flex items-center h-[52px] px-5 bg-[var(--c-surface)]/90 backdrop-blur-xl border-b border-[var(--c-border)]">
      <button @click="goBack" class="text-[var(--c-primary)] text-sm font-medium">&larr; {{ t('common.back') }}</button>
      <span class="flex-1 text-center text-sm font-bold">{{ t('libraryPage.search.title') }}</span>
      <div class="w-10"></div>
    </div>

    <div class="max-w-lg mx-auto px-4 py-6">
      <p class="text-center text-sm text-[var(--c-text-2)] mb-5">{{ t('libraryPage.search.description') }}</p>

      <div class="bg-[var(--c-surface)] rounded-2xl p-5 shadow-sm border border-[var(--c-border)]">
        <div>
          <label class="text-sm font-medium text-[var(--c-text-2)] mb-1.5 block">{{ t('libraryPage.search.keywordLabel') }}</label>
          <input
            v-model="keyword"
            type="text"
            :placeholder="t('libraryPage.search.keywordPlaceholder')"
            class="w-full px-3 py-2.5 border border-[var(--c-border)] rounded-lg text-sm focus:border-[var(--c-primary)] focus:ring-2 focus:ring-[var(--c-primary)]/10 outline-none bg-[var(--c-surface)]"
            @keyup.enter="doSearch"
          />
        </div>

        <button
          type="button"
          class="w-full bg-[var(--c-primary)] text-white rounded-lg py-2.5 font-semibold mt-6 transition-opacity hover:opacity-90"
          @click="doSearch"
        >{{ t('libraryPage.search.submit') }}</button>
      </div>
    </div>
  </div>
</template>
