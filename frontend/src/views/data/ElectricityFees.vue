<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useToast } from '@/composables/useToast'
import request from '../../utils/request'
import { createElectricityResultFields } from './dataContent'

const router = useRouter()
const { t } = useI18n()
const { error: showError, loading: showLoading, hideLoading } = useToast()

const form = ref({
  year: new Date().getFullYear(),
  name: '',
  number: ''
})

const isLoading = ref(false)
const isQueried = ref(false)
const result = ref({})
const resultFields = computed(() => createElectricityResultFields(t))

// 生成年份选项（2016-当前年份）
const years = computed(() => {
  const currentYear = new Date().getFullYear()
  const yearList = []
  for (let i = 2016; i <= currentYear; i++) {
    yearList.push(i)
  }
  return yearList.reverse()
})

const submitQuery = () => {
  // 依次检查所有必填字段，显示具体错误
  if (!form.value.year) {
    showError(t('electricityFees.validation.yearRequired'))
    return
  }

  const name = String(form.value.name || '').trim()
  const number = String(form.value.number || '').trim()

  if (!name) {
    showError(t('electricityFees.validation.nameRequired'))
    return
  }

  if (!number) {
    showError(t('electricityFees.validation.numberRequired'))
    return
  }

  // 学号校验（11位数字）
  if (!/^\d{11}$/.test(number)) {
    showError(t('electricityFees.validation.numberInvalid'))
    return
  }

  // 年份校验
  const year = Number(form.value.year)
  if (year < 2016 || year > 2050) {
    showError(t('electricityFees.validation.yearInvalid'))
    return
  }

  // 开始查询
  isLoading.value = true
  showLoading(t('electricityFees.loading'))

  request
    .post('/data/electricfees', {
      name: name,
      number: number,
      year: year
    })
    .then((res) => {
      isLoading.value = false
      hideLoading()
      if (res && res.success && res.data) {
        result.value = res.data
        isQueried.value = true
      } else {
        showError(res?.message || t('electricityFees.queryFailed'))
      }
    })
    .catch(() => {
      isLoading.value = false
      hideLoading()
      // 错误由 request.js 全局拦截器统一提示
    })
}

const resetQuery = () => {
  isQueried.value = false
  form.value = {
    year: new Date().getFullYear(),
    name: '',
    number: ''
  }
  result.value = {}
}
</script>

<template>
  <div class="min-h-screen bg-[var(--c-bg)]">
    <div class="sticky top-0 z-30 flex items-center h-[52px] px-5 bg-[var(--c-surface)]/90 backdrop-blur-xl border-b border-[var(--c-border)]">
      <button @click="router.back()" class="text-[var(--c-primary)] text-sm font-medium">&larr; {{ t('common.back') }}</button>
      <span class="flex-1 text-center text-sm font-bold">{{ t('electricityFees.title') }}</span>
      <div class="w-10"></div>
    </div>

    <div class="max-w-lg mx-auto px-4 py-6">
      <!-- Query form -->
      <div v-if="!isQueried" class="bg-[var(--c-surface)] rounded-2xl p-5 shadow-sm border border-[var(--c-border)]">
        <form class="space-y-4" @submit.prevent="submitQuery">
          <!-- 年份 -->
          <div>
            <label class="text-sm font-medium text-[var(--c-text-2)] mb-1.5 block">{{ t('electricityFees.field.year') }}</label>
            <select
              v-model="form.year"
              class="w-full px-3 py-2.5 border border-[var(--c-border)] rounded-lg text-sm focus:border-[var(--c-primary)] focus:ring-2 focus:ring-[var(--c-primary)]/10 outline-none bg-[var(--c-surface)] appearance-none"
            >
              <option v-for="y in years" :key="y" :value="y">{{ y }}</option>
            </select>
          </div>

          <!-- 姓名 -->
          <div>
            <label class="text-sm font-medium text-[var(--c-text-2)] mb-1.5 block">{{ t('electricityFees.field.name') }}</label>
            <input
              v-model="form.name"
              type="text"
              maxlength="10"
              :placeholder="t('electricityFees.placeholder.name')"
              class="w-full px-3 py-2.5 border border-[var(--c-border)] rounded-lg text-sm focus:border-[var(--c-primary)] focus:ring-2 focus:ring-[var(--c-primary)]/10 outline-none bg-[var(--c-surface)]"
            />
          </div>

          <!-- 学号 -->
          <div>
            <label class="text-sm font-medium text-[var(--c-text-2)] mb-1.5 block">{{ t('electricityFees.field.number') }}</label>
            <input
              v-model="form.number"
              type="text"
              maxlength="11"
              :placeholder="t('electricityFees.placeholder.number')"
              inputmode="numeric"
              class="w-full px-3 py-2.5 border border-[var(--c-border)] rounded-lg text-sm focus:border-[var(--c-primary)] focus:ring-2 focus:ring-[var(--c-primary)]/10 outline-none bg-[var(--c-surface)]"
            />
          </div>
        </form>

        <button
          type="button"
          class="w-full bg-[var(--c-primary)] text-white rounded-lg py-2.5 font-semibold mt-6 transition-opacity hover:opacity-90"
          @click="submitQuery"
        >{{ t('electricityFees.submit') }}</button>
      </div>

      <!-- Query result -->
      <div v-if="isQueried">
        <h3 class="text-xs font-semibold text-[var(--c-text-2)] uppercase tracking-wide mb-2">{{ t('electricityFees.result.title') }}</h3>

        <div class="bg-[var(--c-surface)] rounded-2xl p-5 shadow-sm border border-[var(--c-border)]">
          <div class="divide-y divide-[var(--c-border-light)]">
            <div v-for="field in resultFields" :key="field.key" class="flex justify-between py-3">
              <span class="text-sm text-[var(--c-text-2)]">{{ field.label }}</span>
              <span class="text-sm font-semibold">
                {{ field.key === 'dormitory' ? `${result.buildingNumber}${result.roomNumber}` : result[field.key] }}
              </span>
            </div>
          </div>
        </div>

        <button
          type="button"
          class="w-full mt-5 bg-[var(--c-surface)] text-[var(--c-text)] border border-[var(--c-border)] rounded-lg py-2.5 font-semibold transition-opacity hover:opacity-80"
          @click="resetQuery"
        >{{ t('electricityFees.retry') }}</button>
      </div>
    </div>
  </div>
</template>
