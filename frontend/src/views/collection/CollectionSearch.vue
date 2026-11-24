<script setup>
import { ChevronLeft } from 'lucide-vue-next'
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useToast } from '@/composables/useToast'
import AppButton from '@/components/ui/AppButton.vue'
import AppInput from '@/components/ui/AppInput.vue'

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

      <div class="ui-panel bg-[var(--c-surface)] rounded-[var(--radius-card)] p-5 border border-[var(--c-border)]">
        <div>
          <label class="text-sm font-medium text-[var(--c-text-2)] mb-1.5 block">{{ t('libraryPage.search.keywordLabel') }}</label>
          <AppInput
            v-model="keyword"
            type="text"
            :placeholder="t('libraryPage.search.keywordPlaceholder')"
            @keyup.enter="doSearch"
          />
        </div>

        <AppButton
          type="button"
          class="ui-btn-primary w-full py-2.5 mt-6 transition-opacity"
          @click="doSearch"
        >{{ t('libraryPage.search.submit') }}</AppButton>
      </div>
    </div>
  </div>
</template>
