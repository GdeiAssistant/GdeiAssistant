<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { querySpareRoom } from '@/api/spare'
import { useToast } from '@/composables/useToast'
import {
  createCampusOptions,
  createPeriodOptions,
  createRoomTypeOptions,
  createSingleDoubleOptions,
  createWeekDayOptions
} from './spareContent'

const router = useRouter()
const { error: showError } = useToast()
const { t } = useI18n()
const loading = ref(false)
const showResult = ref(false)
const spareList = ref([])

/** 节次选项 value -> [startTime, endTime] 对应后端 1-20 */
const PERIOD_MAP = [
  [1, 2], [3, 3], [4, 5], [6, 7], [8, 9], [10, 12],
  [1, 4], [6, 9], [10, 12], [1, 9], [1, 12]
]

const campusOptions = computed(() => createCampusOptions(t))
const roomTypeOptions = computed(() => createRoomTypeOptions(t))
const weekDayOptions = computed(() => createWeekDayOptions(t))
const singleDoubleOptions = computed(() => createSingleDoubleOptions(t))
const periodOptions = computed(() => createPeriodOptions(t))

const zone = ref(0)
const type = ref(0)
const seatsMin = ref('')
const seatsMax = ref('')
/** 星期几：-1 不限(后端收 minWeek=0,maxWeek=6)，0 周一 … 6 周日 */
const dayOfWeek = ref(-1)
const weekType = ref(0)

onMounted(() => {
  dayOfWeek.value = -1
})
const classNumber = ref(0)

function buildQuery() {
  const [startTime, endTime] = PERIOD_MAP[classNumber.value] ?? [1, 2]
  const dw = dayOfWeek.value
  const minWeek = dw === -1 ? 0 : dw
  const maxWeek = dw === -1 ? 6 : dw
  const data = {
    zone: Number(zone.value),
    type: Number(type.value),
    startTime,
    endTime,
    minWeek,
    maxWeek,
    weekType: Number(weekType.value),
    classNumber: Number(classNumber.value)
  }
  const min = seatsMin.value !== '' && seatsMin.value != null ? parseInt(seatsMin.value, 10) : null
  const max = seatsMax.value !== '' && seatsMax.value != null ? parseInt(seatsMax.value, 10) : null
  if (min != null && !isNaN(min)) data.minSeating = min
  if (max != null && !isNaN(max)) data.maxSeating = max
  return data
}

function doSearch() {
  loading.value = true
  showResult.value = true
  const data = buildQuery()
  querySpareRoom(data)
    .then((res) => {
      const list = res?.data?.data ?? res?.data ?? []
      spareList.value = Array.isArray(list) ? list : []
    })
    .catch(() => {
      spareList.value = []
    })
    .finally(() => {
      loading.value = false
    })
}

function backToSearch() {
  showResult.value = false
}

const isEmpty = computed(() => !loading.value && spareList.value.length === 0)
</script>

<template>
  <div class="min-h-screen bg-[var(--c-bg)]">
    <!-- Search view -->
    <template v-if="!showResult">
      <!-- Sticky header -->
      <div class="sticky top-0 z-30 flex items-center h-[52px] px-5 bg-[var(--c-surface)]/90 backdrop-blur-xl border-b border-[var(--c-border)]">
        <button @click="$router.back()" class="text-[var(--c-primary)] text-sm font-medium">&larr; {{ t('common.back') }}</button>
        <span class="flex-1 text-center text-sm font-bold">{{ t('spare.pageTitle') }}</span>
        <div class="w-10"></div>
      </div>

      <div class="max-w-lg mx-auto px-4 py-6">
        <p class="text-center text-xs text-[var(--c-text-secondary)] mb-5">{{ t('about.appName') }}</p>

        <!-- Form card -->
        <div class="bg-[var(--c-surface)] rounded-2xl border border-[var(--c-border)] divide-y divide-[var(--c-border)]">
          <!-- Campus -->
          <div class="flex items-center px-4 h-[52px]">
            <label class="w-24 shrink-0 text-sm text-[var(--c-text)]">{{ t('spare.field.campus') }}</label>
            <select v-model.number="zone" class="flex-1 text-right text-sm bg-transparent text-[var(--c-text)] outline-none appearance-none pr-1">
              <option v-for="opt in campusOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
            </select>
          </div>
          <!-- Room type -->
          <div class="flex items-center px-4 h-[52px]">
            <label class="w-24 shrink-0 text-sm text-[var(--c-text)]">{{ t('spare.field.roomType') }}</label>
            <select v-model.number="type" class="flex-1 text-right text-sm bg-transparent text-[var(--c-text)] outline-none appearance-none pr-1">
              <option v-for="opt in roomTypeOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
            </select>
          </div>
          <!-- Seats min -->
          <div class="flex items-center px-4 h-[52px]">
            <label class="w-24 shrink-0 text-sm text-[var(--c-text)]">{{ t('spare.field.seatsMin') }}</label>
            <input
              type="text"
              inputmode="numeric"
              :placeholder="t('spare.optional')"
              v-model="seatsMin"
              @input="seatsMin = ($event.target.value || '').replace(/\D/g, '')"
              class="flex-1 text-right text-sm bg-transparent text-[var(--c-text)] placeholder:text-[var(--c-text-tertiary)] outline-none"
            />
          </div>
          <!-- Seats max -->
          <div class="flex items-center px-4 h-[52px]">
            <label class="w-24 shrink-0 text-sm text-[var(--c-text)]">{{ t('spare.field.seatsMax') }}</label>
            <input
              type="text"
              inputmode="numeric"
              :placeholder="t('spare.optional')"
              v-model="seatsMax"
              @input="seatsMax = ($event.target.value || '').replace(/\D/g, '')"
              class="flex-1 text-right text-sm bg-transparent text-[var(--c-text)] placeholder:text-[var(--c-text-tertiary)] outline-none"
            />
          </div>
          <!-- Day of week -->
          <div class="flex items-center px-4 h-[52px]">
            <label class="w-24 shrink-0 text-sm text-[var(--c-text)]">{{ t('spare.field.weekday') }}</label>
            <select v-model.number="dayOfWeek" class="flex-1 text-right text-sm bg-transparent text-[var(--c-text)] outline-none appearance-none pr-1">
              <option v-for="opt in weekDayOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
            </select>
          </div>
          <!-- Week type -->
          <div class="flex items-center px-4 h-[52px]">
            <label class="w-24 shrink-0 text-sm text-[var(--c-text)]">{{ t('spare.field.weekType') }}</label>
            <select v-model.number="weekType" class="flex-1 text-right text-sm bg-transparent text-[var(--c-text)] outline-none appearance-none pr-1">
              <option v-for="opt in singleDoubleOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
            </select>
          </div>
          <!-- Period -->
          <div class="flex items-center px-4 h-[52px]">
            <label class="w-24 shrink-0 text-sm text-[var(--c-text)]">{{ t('spare.field.period') }}</label>
            <select v-model.number="classNumber" class="flex-1 text-right text-sm bg-transparent text-[var(--c-text)] outline-none appearance-none pr-1">
              <option v-for="opt in periodOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
            </select>
          </div>
        </div>

        <!-- Search button -->
        <button
          type="button"
          class="mt-6 w-full py-3 rounded-xl bg-[var(--c-primary)] text-white text-[15px] font-medium active:opacity-80 transition-opacity"
          @click="doSearch"
        >
          {{ t('spare.search') }}
        </button>
      </div>
    </template>

    <!-- Result view -->
    <template v-else>
      <!-- Sticky header -->
      <div class="sticky top-0 z-30 flex items-center h-[52px] px-5 bg-[var(--c-surface)]/90 backdrop-blur-xl border-b border-[var(--c-border)]">
        <button @click="backToSearch" class="text-[var(--c-primary)] text-sm font-medium">&larr; {{ t('common.back') }}</button>
        <span class="flex-1 text-center text-sm font-bold">{{ t('spare.resultTitle') }}</span>
        <div class="w-10"></div>
      </div>

      <div class="max-w-lg mx-auto px-4 py-6">
        <!-- Loading -->
        <div v-if="loading" class="flex flex-col items-center justify-center py-16 text-[var(--c-text-secondary)]">
          <div class="w-8 h-8 border-2 border-[var(--c-primary)] border-t-transparent rounded-full animate-spin mb-3"></div>
          <span class="text-sm">{{ t('common.loading') }}</span>
        </div>

        <!-- Results list -->
        <template v-else>
          <p class="text-xs text-[var(--c-text-secondary)] mb-3">{{ t('spare.resultList') }}</p>
          <div class="space-y-3">
            <div
              v-for="(item, index) in spareList"
              :key="item.number || index"
              class="bg-[var(--c-surface)] rounded-2xl border border-[var(--c-border)] p-4"
            >
              <h4 class="text-[15px] font-medium text-[var(--c-text)] mb-2">{{ item.name || item.number || '—' }}</h4>
              <div class="space-y-1 text-xs text-[var(--c-text-secondary)]">
                <p>{{ t('spare.result.number') }}：{{ item.number || '—' }}</p>
                <p>{{ t('spare.result.type') }}：{{ item.type || '—' }}</p>
                <p>{{ t('spare.result.campus') }}：{{ item.zone || '—' }}</p>
                <p>{{ t('spare.result.seats') }}：{{ item.classSeating || '—' }}</p>
              </div>
            </div>
          </div>

          <div v-if="isEmpty" class="text-center py-16 text-sm text-[var(--c-text-secondary)]">
            {{ t('spare.empty') }}
          </div>
        </template>
      </div>
    </template>
  </div>
</template>
