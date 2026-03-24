<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { queryKaoyanScore } from '@/api/graduateExam'
import { useToast } from '@/composables/useToast'

const router = useRouter()
const route = useRoute()
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
  showLoading('加载中...')
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
  <div class="min-h-screen bg-[var(--c-bg)]">
    <!-- Sticky header -->
    <div class="sticky top-0 z-30 flex items-center h-[52px] px-5 bg-[var(--c-surface)]/90 backdrop-blur-xl border-b border-[var(--c-border)]">
      <button @click="$router.back()" class="text-[var(--c-primary)] text-sm font-medium">&larr; 返回</button>
      <span class="flex-1 text-center text-sm font-bold">考研成绩查询</span>
      <div class="w-10"></div>
    </div>

    <div class="max-w-lg mx-auto px-4 py-6">
      <!-- Loading -->
      <div v-if="isLoading" class="flex flex-col items-center justify-center py-16 text-[var(--c-text-secondary)]">
        <div class="w-8 h-8 border-2 border-[var(--c-primary)] border-t-transparent rounded-full animate-spin mb-3"></div>
        <span class="text-sm">加载中</span>
      </div>

      <!-- Has data -->
      <template v-if="!isLoading && hasData">
        <!-- Total score highlight -->
        <div class="text-center mb-6">
          <p class="text-sm text-[var(--c-primary)] mb-1">初试总分</p>
          <p class="text-4xl font-semibold text-[var(--c-primary)]">{{ scoreData.totalScore }}</p>
        </div>

        <!-- Score details card -->
        <div class="bg-[var(--c-surface)] rounded-2xl border border-[var(--c-border)] divide-y divide-[var(--c-border)]">
          <div class="flex items-center justify-between px-4 py-3">
            <span class="text-sm text-[var(--c-text-secondary)]">姓名</span>
            <span class="text-sm text-[var(--c-text)]">{{ scoreData.name ?? '—' }}</span>
          </div>
          <div class="flex items-center justify-between px-4 py-3">
            <span class="text-sm text-[var(--c-text-secondary)]">考号</span>
            <span class="text-sm text-[var(--c-text)]">{{ scoreData.candidateNo ?? '—' }}</span>
          </div>
          <div class="flex items-center justify-between px-4 py-3">
            <span class="text-sm text-[var(--c-text-secondary)]">思想政治理论</span>
            <span class="text-sm text-[var(--c-text)]">{{ scoreData.politics ?? '—' }}</span>
          </div>
          <div class="flex items-center justify-between px-4 py-3">
            <span class="text-sm text-[var(--c-text-secondary)]">外国语</span>
            <span class="text-sm text-[var(--c-text)]">{{ scoreData.foreignLanguage ?? '—' }}</span>
          </div>
          <div class="flex items-center justify-between px-4 py-3">
            <span class="text-sm text-[var(--c-text-secondary)]">业务课一</span>
            <span class="text-sm text-[var(--c-text)]">{{ scoreData.business1 ?? '—' }}</span>
          </div>
          <div class="flex items-center justify-between px-4 py-3">
            <span class="text-sm text-[var(--c-text-secondary)]">业务课二</span>
            <span class="text-sm text-[var(--c-text)]">{{ scoreData.business2 ?? '—' }}</span>
          </div>
        </div>

        <!-- Re-query button -->
        <button
          type="button"
          class="mt-6 w-full py-3 rounded-xl bg-[var(--c-primary)] text-white text-[15px] font-medium active:opacity-80 transition-opacity"
          @click="reQuery"
        >
          重新查询
        </button>
      </template>

      <!-- No data -->
      <div v-if="!isLoading && !hasData" class="text-center py-16 text-sm text-[var(--c-text-secondary)]">
        暂无成绩数据
      </div>
    </div>
  </div>
</template>
