<script setup>
import { ChevronLeft } from 'lucide-vue-next'
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
  <div class="subpage min-h-screen bg-[var(--c-bg)]">
    <div class="subpage-bar">
      <button type="button" class="subpage-bar__back" @click="goBack">
        <ChevronLeft :size="18" aria-hidden="true" />
        <span>{{ t('common.back') }}</span>
      </button>
      <span class="subpage-bar__title">{{ t('libraryPage.search.title') }}</span>
      <span aria-hidden="true"></span>
    </div>

    <div class="subpage-body max-w-lg mx-auto px-4 py-6">
      <p class="text-center text-sm text-[var(--c-text-2)] mb-5">{{ t('libraryPage.search.description') }}</p>

      <div class="ui-panel bg-[var(--c-surface)] rounded-2xl p-5 shadow-sm border border-[var(--c-border)]">
        <div>
          <label class="text-sm font-medium text-[var(--c-text-2)] mb-1.5 block">{{ t('libraryPage.search.keywordLabel') }}</label>
          <input
            v-model="keyword"
            type="text"
            :placeholder="t('libraryPage.search.keywordPlaceholder')"
            class="ui-control w-full px-3 py-2.5 border border-[var(--c-border)] rounded-lg text-sm focus:border-[var(--c-primary)] focus:ring-2 focus:ring-[var(--c-primary)]/10 outline-none bg-[var(--c-surface)]"
            @keyup.enter="doSearch"
          />
        </div>

        <button
          type="button"
          class="ui-btn-primary w-full bg-[var(--c-primary)] text-[var(--c-on-primary)] rounded-lg py-2.5 font-semibold mt-6 transition-opacity hover:opacity-90"
          @click="doSearch"
        >{{ t('libraryPage.search.submit') }}</button>
      </div>
    </div>
  </div>
</template>
