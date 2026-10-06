<script setup>
import { useRouter, useRoute } from 'vue-router'
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { getCollectionDetail } from '@/api/collection'
import { useToast } from '@/composables/useToast'
import AppEmpty from '@/components/ui/AppEmpty.vue'
import { BookOpenText, ChevronLeft } from 'lucide-vue-next'

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
  <div class="subpage min-h-screen bg-[var(--c-bg)]">
    <div class="subpage-bar">
      <button type="button" class="subpage-bar__back" @click="goBack">
        <ChevronLeft :size="18" aria-hidden="true" />
        <span>{{ t('common.back') }}</span>
      </button>
      <span class="subpage-bar__title">{{ t('libraryPage.detail.title') }}</span>
      <span aria-hidden="true"></span>
    </div>

    <div class="subpage-body max-w-lg mx-auto px-4 py-6">
      <template v-if="!loading && detail">
        <div class="ui-panel bg-[var(--c-surface)] rounded-2xl p-5 shadow-sm border border-[var(--c-border)]">
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
      <div v-else-if="!loading" class="collection-empty-shell">
        <AppEmpty
          :title="t('libraryPage.detail.empty')"
          :description="t('libraryPage.detail.emptyDescription')"
        >
          <template #icon>
            <BookOpenText :size="30" />
          </template>
        </AppEmpty>
      </div>
    </div>
  </div>
</template>

<style scoped>
.collection-empty-shell {
  border: 1px solid var(--c-border);
  border-radius: var(--radius-card);
  background: var(--c-primary-soft);
  box-shadow: none;
}
</style>
