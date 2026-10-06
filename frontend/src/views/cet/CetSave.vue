<script setup>
import { ChevronLeft } from 'lucide-vue-next'
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { getCetNumber, saveCetNumber } from '@/api/cet'
import { useToast } from '@/composables/useToast'

const router = useRouter()
const { t } = useI18n()
const { success: showSuccess, error: showError } = useToast()

const examNumber = ref('')
const name = ref('')
const saving = ref(false)

function submitSave() {
  const num = (examNumber.value || '').trim()
  const n = (name.value || '').trim()
  if (!num) {
    showError(t('cetPage.examNumberPlaceholder'))
    return
  }
  if (num.length !== 15) {
    showError(t('cetPage.invalidNumber'))
    return
  }
  saving.value = true
  saveCetNumber({ number: num, name: n }).then((res) => {
    if (res && res.success) {
      showSuccess(t('cetPage.saveSuccess'))
      setTimeout(() => router.back(), 800)
    } else {
      showError(res && res.message ? res.message : t('cetPage.saveFailed'))
    }
  }).catch(() => {
    // 错误由 request.js 全局拦截器统一提示，此处仅关闭 Loading
  }).finally(() => {
    saving.value = false
  })
}

onMounted(() => {
  getCetNumber().then((res) => {
    if (res && res.success && res.data) {
      const d = res.data
      if (d.number != null) examNumber.value = String(d.number)
      if (d.name != null && d.name !== '') name.value = d.name
    }
  }).catch(() => {})
})
</script>

<template>
  <div class="subpage min-h-screen bg-[var(--c-bg)]">
    <div class="subpage-bar">
      <button type="button" class="subpage-bar__back" @click="$router.back()">
        <ChevronLeft :size="18" aria-hidden="true" />
        <span>{{ t('common.back') }}</span>
      </button>
      <span class="subpage-bar__title">{{ t('cetPage.saveTitle') }}</span>
      <span aria-hidden="true"></span>
    </div>

    <div class="subpage-body max-w-lg mx-auto px-4 py-6">
      <div class="ui-panel bg-[var(--c-surface)] rounded-2xl p-5 shadow-sm border border-[var(--c-border)]">
        <div class="space-y-4">
          <!-- 考号 -->
          <div>
            <label class="text-sm font-medium text-[var(--c-text-2)] mb-1.5 block">{{ t('cetPage.examNumberLabel') }}</label>
            <input
              v-model="examNumber"
              type="text"
              inputmode="numeric"
              maxlength="15"
              :placeholder="t('cetPage.examNumberPlaceholder')"
              class="ui-control w-full px-3 py-2.5 border border-[var(--c-border)] rounded-lg text-sm focus:border-[var(--c-primary)] focus:ring-2 focus:ring-[var(--c-primary)]/10 outline-none bg-[var(--c-surface)]"
            />
          </div>

          <!-- 姓名 -->
          <div>
            <label class="text-sm font-medium text-[var(--c-text-2)] mb-1.5 block">{{ t('cetPage.nameLabel') }}</label>
            <input
              v-model="name"
              type="text"
              maxlength="20"
              :placeholder="t('cetPage.namePlaceholder')"
              class="ui-control w-full px-3 py-2.5 border border-[var(--c-border)] rounded-lg text-sm focus:border-[var(--c-primary)] focus:ring-2 focus:ring-[var(--c-primary)]/10 outline-none bg-[var(--c-surface)]"
            />
          </div>
        </div>

        <button
          type="button"
          class="ui-btn-primary w-full bg-[var(--c-primary)] text-[var(--c-on-primary)] rounded-lg py-2.5 font-semibold mt-6 transition-opacity hover:opacity-90"
          :disabled="saving"
          @click="submitSave"
        >
          {{ saving ? t('cetPage.saving') : t('common.save') }}
        </button>
      </div>
    </div>
  </div>
</template>
