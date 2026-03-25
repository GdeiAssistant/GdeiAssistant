<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { queryCardRecord } from '@/api/card'
import { useToast } from '@/composables/useToast'
import { formatCardAmount } from './cardContent'

const router = useRouter()
const route = useRoute()
const { t } = useI18n()
const { error: showError, loading: showLoading, hideLoading } = useToast()

const list = ref([])
const loading = ref(false)
const queryDate = computed(() => (route.query.date || '').trim())

function reQuery() {
  router.back()
}

function parseDateToPayload(dateStr) {
  const s = (dateStr || '').trim()
  if (!s) return null
  // 期望格式：YYYY-MM-DD
  const m = /^(\d{4})-(\d{1,2})-(\d{1,2})$/.exec(s)
  if (!m) return null
  const year = parseInt(m[1], 10)
  const month = parseInt(m[2], 10)
  const date = parseInt(m[3], 10)
  if (!year || !month || !date) return null
  return { year, month, date }
}

function fetchList() {
  const payload = parseDateToPayload(queryDate.value)
  if (!payload) {
    showError(t('card.list.invalidDate'))
    list.value = []
    return
  }

  loading.value = true
  showLoading(t('common.loading'))
  queryCardRecord(payload)
    .then((res) => {
      const body = res && res.data ? res.data : res
      const result =
        body && typeof body === 'object'
          ? body.data !== undefined
            ? body.data
            : body
          : null
      const records = result && Array.isArray(result.cardList) ? result.cardList : []
      list.value = records
    })
    .catch(() => {
      list.value = []
    })
    .finally(() => {
      loading.value = false
      hideLoading()
    })
}

function formatDisplayDate(d) {
  if (!d) return ''
  const s = String(d)
  if (/^\d{4}-\d{2}-\d{2}$/.test(s)) {
    return s.replace(/-/g, '/')
  }
  return s
}

function isPositive(amount) {
  const n = parseFloat(amount)
  return !isNaN(n) && n >= 0
}

function amountText(amount) {
  return formatCardAmount(amount, t)
}

onMounted(() => {
  if (!queryDate.value) {
    router.replace('/card/records')
    return
  }
  fetchList()
})
</script>

<template>
  <div class="min-h-screen bg-[var(--c-bg)]">
    <template v-if="queryDate">
      <div class="sticky top-0 z-30 flex items-center h-[52px] px-5 bg-[var(--c-surface)]/90 backdrop-blur-xl border-b border-[var(--c-border)]">
        <button @click="$router.back()" class="text-[var(--c-primary)] text-sm font-medium">&larr; {{ t('common.back') }}</button>
        <span class="flex-1 text-center text-sm font-bold">{{ t('card.action.records.title') }}</span>
        <div class="w-10"></div>
      </div>

      <div class="max-w-lg mx-auto px-4 py-6">
        <div class="text-sm text-[var(--c-text-2)] mb-4">
          {{ t('card.list.queryDate') }}<span class="font-medium text-[var(--c-text)]">{{ formatDisplayDate(queryDate) }}</span>
        </div>

        <!-- Empty state -->
        <div v-if="!loading && list.length === 0" class="text-center py-12 text-sm text-[var(--c-text-3)]">
          {{ t('card.list.empty') }}
        </div>

        <!-- Transaction list -->
        <div v-if="list.length > 0" class="bg-[var(--c-surface)] rounded-2xl shadow-sm border border-[var(--c-border)]">
          <div
            v-for="(item, index) in list"
            :key="index"
            class="flex items-center justify-between px-4 py-3"
            :class="{ 'border-b border-[var(--c-border-light)]': index < list.length - 1 }"
          >
            <div class="min-w-0 flex-1">
              <div class="text-sm font-medium truncate">{{ item.merchantName || t('card.list.unknownMerchant') }} <span class="text-[var(--c-text-3)]">{{ item.tradeName || '' }}</span></div>
              <div class="text-xs text-[var(--c-text-3)] mt-0.5">{{ item.tradeTime || '' }}</div>
            </div>
            <div class="shrink-0 ml-3 font-mono text-sm font-semibold"
                 :class="isPositive(item.tradePrice) ? 'text-green-600' : 'text-[var(--c-text-2)]'"
            >{{ amountText(item.tradePrice) }}</div>
          </div>
        </div>

        <button
          type="button"
          class="w-full bg-[var(--c-primary)] text-white rounded-lg py-2.5 font-semibold mt-6 transition-opacity hover:opacity-90"
          @click="reQuery"
        >{{ t('card.list.retry') }}</button>
      </div>
    </template>
  </div>
</template>
