<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { queryKaoyanScore } from '@/api/graduateExam'
import { useToast } from '@/composables/useToast'
import AppEmpty from '@/components/ui/AppEmpty.vue'
import { GraduationCap, ChevronLeft } from 'lucide-vue-next'

const router = useRouter()
const route = useRoute()
const { t } = useI18n()
const { loading: showLoading, hideLoading } = useToast()
const scoreData = ref({})
const hasData = ref(false)
const isLoading = ref(true)

function reQuery() {
  router.back()
}

onMounted(() => {
  const { name, candidateNo, idNo } = route.query
  if (!name || !candidateNo || !idNo) {
    isLoading.value = false
    return
  }

  isLoading.value = true
  showLoading(t('common.loading'))
  const payload = {
    name,
    examNumber: candidateNo,
    idNumber: idNo
  }

  queryKaoyanScore(payload)
    .then((res) => {
      const body = res && res.data ? res.data : res
      const targetData = body && typeof body === 'object'
        ? (body.data !== undefined ? body.data : body)
        : null

      if (targetData && targetData.totalScore !== undefined) {
        scoreData.value = targetData
        hasData.value = true
      } else {
        hasData.value = false
      }
    })
    .catch(() => {
      hasData.value = false
    })
    .finally(() => {
      isLoading.value = false
      hideLoading()
    })
})
</script>

<template>
  <div class="subpage min-h-screen bg-[var(--c-bg)]">
    <!-- Sticky header -->
    <div class="subpage-bar">
      <button type="button" class="subpage-bar__back" @click="$router.back()">
        <ChevronLeft :size="18" aria-hidden="true" />
        <span>{{ t('common.back') }}</span>
      </button>
      <span class="subpage-bar__title">{{ t('graduateExam.title') }}</span>
      <span aria-hidden="true"></span>
    </div>

    <div class="subpage-body max-w-lg mx-auto px-4 py-6">
      <!-- Loading -->
      <div v-if="isLoading" class="flex flex-col items-center justify-center py-16 text-[var(--c-text-secondary)]">
        <div class="w-8 h-8 border-2 border-[var(--c-primary)] border-t-transparent rounded-full animate-spin mb-3"></div>
        <span class="text-sm">{{ t('common.loading') }}</span>
      </div>

      <!-- Has data -->
      <template v-if="!isLoading && hasData">
        <!-- Total score highlight -->
        <div class="text-center mb-6">
          <p class="text-sm text-[var(--c-primary)] mb-1">{{ t('graduateExam.totalScore') }}</p>
          <p class="text-4xl font-semibold text-[var(--c-primary)]">{{ scoreData.totalScore }}</p>
        </div>

        <!-- Score details card -->
        <div class="ui-panel bg-[var(--c-surface)] rounded-2xl border border-[var(--c-border)] divide-y divide-[var(--c-border)]">
          <div class="flex items-center justify-between px-4 py-3">
            <span class="text-sm text-[var(--c-text-secondary)]">{{ t('graduateExam.name') }}</span>
            <span class="text-sm text-[var(--c-text)]">{{ scoreData.name ?? '—' }}</span>
          </div>
          <div class="flex items-center justify-between px-4 py-3">
            <span class="text-sm text-[var(--c-text-secondary)]">{{ t('graduateExam.candidateNo') }}</span>
            <span class="text-sm text-[var(--c-text)]">{{ scoreData.candidateNo ?? '—' }}</span>
          </div>
          <div class="flex items-center justify-between px-4 py-3">
            <span class="text-sm text-[var(--c-text-secondary)]">{{ t('graduateExam.politics') }}</span>
            <span class="text-sm text-[var(--c-text)]">{{ scoreData.politics ?? '—' }}</span>
          </div>
          <div class="flex items-center justify-between px-4 py-3">
            <span class="text-sm text-[var(--c-text-secondary)]">{{ t('graduateExam.foreignLanguage') }}</span>
            <span class="text-sm text-[var(--c-text)]">{{ scoreData.foreignLanguage ?? '—' }}</span>
          </div>
          <div class="flex items-center justify-between px-4 py-3">
            <span class="text-sm text-[var(--c-text-secondary)]">{{ t('graduateExam.business1') }}</span>
            <span class="text-sm text-[var(--c-text)]">{{ scoreData.business1 ?? '—' }}</span>
          </div>
          <div class="flex items-center justify-between px-4 py-3">
            <span class="text-sm text-[var(--c-text-secondary)]">{{ t('graduateExam.business2') }}</span>
            <span class="text-sm text-[var(--c-text)]">{{ scoreData.business2 ?? '—' }}</span>
          </div>
        </div>

        <!-- Re-query button -->
        <button
          type="button"
          class="ui-btn-primary mt-6 w-full py-3 rounded-xl bg-[var(--c-primary)] text-[var(--c-on-primary)] text-[15px] font-medium active:opacity-80 transition-opacity"
          @click="reQuery"
        >
          {{ t('graduateExam.reQuery') }}
        </button>
      </template>

      <!-- No data -->
      <div v-if="!isLoading && !hasData" class="graduate-empty-shell">
        <AppEmpty
          :title="t('graduateExam.emptyTitle')"
          :description="t('graduateExam.emptyDescription')"
          :action-text="t('graduateExam.emptyAction')"
          @action="reQuery"
        >
          <template #icon>
            <GraduationCap :size="30" />
          </template>
        </AppEmpty>
      </div>
    </div>
  </div>
</template>

<style scoped>
.graduate-empty-shell {
  border: 1px solid var(--c-border);
  border-radius: var(--radius-card);
  background: var(--c-primary-soft);
  box-shadow: none;
}
</style>
