<script setup>
import { useRouter, useRoute } from 'vue-router'
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { getCollectionDetail } from '@/api/collection'
import { useToast } from '@/composables/useToast'

const router = useRouter()
const route = useRoute()
const { t } = useI18n()
const { loading: showLoading, hideLoading } = useToast()
const detail = ref(null)
const loading = ref(true)

function goBack() {
  router.back()
}

onMounted(() => {
  const detailURL = typeof route.query.detailURL === 'string' ? route.query.detailURL.trim() : ''
  if (!detailURL) {
    loading.value = false
    return
  }
  showLoading(t('common.loading'))
  getCollectionDetail(detailURL).then((res) => {
    loading.value = false
    hideLoading()
    if (res?.success && res.data) detail.value = res.data
  }).catch(() => {
    loading.value = false
    hideLoading()
  })
})
</script>

<template>
  <div class="min-h-screen bg-[var(--c-bg)]">
    <div class="sticky top-0 z-30 flex items-center h-[52px] px-5 bg-[var(--c-surface)]/90 backdrop-blur-xl border-b border-[var(--c-border)]">
      <button @click="goBack" class="text-[var(--c-primary)] text-sm font-medium">&larr; {{ t('common.back') }}</button>
      <span class="flex-1 text-center text-sm font-bold">{{ t('libraryPage.detail.title') }}</span>
      <div class="w-10"></div>
    </div>

    <div class="max-w-lg mx-auto px-4 py-6">
      <template v-if="!loading && detail">
        <div class="bg-[var(--c-surface)] rounded-2xl p-5 shadow-sm border border-[var(--c-border)]">
          <h2 class="text-lg font-bold text-[var(--c-text)] mb-3">{{ detail.bookname || '—' }}</h2>
          <div class="divide-y divide-[var(--c-border-light)]">
            <div class="flex justify-between py-3">
              <span class="text-sm text-[var(--c-text-2)]">{{ t('libraryPage.detail.author') }}</span>
              <span class="text-sm font-medium text-[var(--c-text)]">{{ detail.author || '—' }}</span>
            </div>
            <div class="flex justify-between py-3">
              <span class="text-sm text-[var(--c-text-2)]">{{ t('libraryPage.detail.principal') }}</span>
              <span class="text-sm font-medium text-[var(--c-text)] text-right max-w-[60%] break-all">{{ detail.principal || '—' }}</span>
            </div>
            <div class="flex justify-between py-3">
              <span class="text-sm text-[var(--c-text-2)]">{{ t('libraryPage.detail.publisher') }}</span>
              <span class="text-sm font-medium text-[var(--c-text)]">{{ detail.publishingHouse || '—' }}</span>
            </div>
          </div>
        </div>
      </template>
      <div v-else-if="!loading" class="flex items-center justify-center py-10 text-sm text-[var(--c-text-3)]">
        {{ t('libraryPage.detail.empty') }}
      </div>
    </div>
  </div>
</template>
