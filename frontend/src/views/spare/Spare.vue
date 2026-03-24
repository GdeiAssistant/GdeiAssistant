<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { querySpareRoom } from '@/api/spare'
import { useToast } from '@/composables/useToast'

const router = useRouter()
const { error: showError } = useToast()
const loading = ref(false)
const showResult = ref(false)
const spareList = ref([])

/** 节次选项 value -> [startTime, endTime] 对应后端 1-20 */
const PERIOD_MAP = [
  [1, 2], [3, 3], [4, 5], [6, 7], [8, 9], [10, 12],
  [1, 4], [6, 9], [10, 12], [1, 9], [1, 12]
]

const campusOptions = [
  { label: '不限', value: 0 },
  { label: '海珠', value: 1 },
  { label: '花都', value: 2 },
  { label: '广东轻工南海校区', value: 3 },
  { label: '业余函授校区', value: 4 }
]

const roomTypeOptions = [
  { label: '不限', value: 0 },
  { label: '不用课室的课程', value: 1 },
  { label: '操场', value: 2 },
  { label: '大多媒体', value: 3 },
  { label: '电脑专业机房', value: 4 },
  { label: '雕塑室', value: 5 },
  { label: '多媒体教室', value: 6 },
  { label: '翻译室', value: 7 },
  { label: '服装实验室', value: 8 },
  { label: '钢琴室', value: 9 },
  { label: '钢琴室', value: 10 },
  { label: '公共机房', value: 11 },
  { label: '国画临摹室', value: 12 },
  { label: '画室', value: 13 },
  { label: '化学实验室', value: 14 },
  { label: '机房', value: 15 },
  { label: '教具室', value: 16 },
  { label: '教育实验室', value: 17 },
  { label: '解剖实验室', value: 18 },
  { label: '金融数学实验室', value: 19 },
  { label: '美术课室', value: 20 },
  { label: '蒙氏教学法专用课室', value: 21 },
  { label: '模型制作实验室', value: 22 },
  { label: '平面制作实验室', value: 23 },
  { label: '琴房', value: 24 },
  { label: '摄影实验室', value: 25 },
  { label: '声乐课室', value: 26 },
  { label: '生物实验室', value: 27 },
  { label: '实训室', value: 28 },
  { label: '视唱练耳室', value: 29 },
  { label: '陶艺室', value: 30 },
  { label: '体操房', value: 31 },
  { label: '网络实验室', value: 32 },
  { label: '微格课室', value: 33 },
  { label: '无须课室', value: 34 },
  { label: '舞蹈室', value: 35 },
  { label: '舞蹈室', value: 36 },
  { label: '物理实验室', value: 37 },
  { label: '小多媒体', value: 38 },
  { label: '小多媒体(<70)', value: 39 },
  { label: '小普通课室(<70)', value: 40 },
  { label: '小组课室', value: 41 },
  { label: '形体房', value: 42 },
  { label: '音乐室', value: 43 },
  { label: '音乐专业课室', value: 44 },
  { label: '语音室', value: 45 },
  { label: '智能录像室', value: 46 },
  { label: '中多媒体(70-100)', value: 47 },
  { label: '专业课教室', value: 48 },
  { label: '专业理论课室', value: 49 },
  { label: '专业实验室', value: 50 },
  { label: '咨询室', value: 51 },
  { label: '综合绘画实验室', value: 52 }
]

const weekDayOptions = [
  { label: '不限', value: -1 },
  { label: '一', value: 0 },
  { label: '二', value: 1 },
  { label: '三', value: 2 },
  { label: '四', value: 3 },
  { label: '五', value: 4 },
  { label: '六', value: 5 },
  { label: '日', value: 6 }
]

const singleDoubleOptions = [
  { label: '不限', value: 0 },
  { label: '单', value: 1 },
  { label: '双', value: 2 }
]

const periodOptions = [
  { label: '第1,2节', value: 0 },
  { label: '第3节', value: 1 },
  { label: '第4,5节', value: 2 },
  { label: '第6,7节', value: 3 },
  { label: '第8,9节', value: 4 },
  { label: '第10,11,12节', value: 5 },
  { label: '上午', value: 6 },
  { label: '下午', value: 7 },
  { label: '晚上', value: 8 },
  { label: '白天', value: 9 },
  { label: '整天', value: 10 }
]

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
        <button @click="$router.back()" class="text-[var(--c-primary)] text-sm font-medium">&larr; 返回</button>
        <span class="flex-1 text-center text-sm font-bold">空课室查询</span>
        <div class="w-10"></div>
      </div>

      <div class="max-w-lg mx-auto px-4 py-6">
        <p class="text-center text-xs text-[var(--c-text-secondary)] mb-5">广东第二师范学院</p>

        <!-- Form card -->
        <div class="bg-[var(--c-surface)] rounded-2xl border border-[var(--c-border)] divide-y divide-[var(--c-border)]">
          <!-- Campus -->
          <div class="flex items-center px-4 h-[52px]">
            <label class="w-24 shrink-0 text-sm text-[var(--c-text)]">校区</label>
            <select v-model.number="zone" class="flex-1 text-right text-sm bg-transparent text-[var(--c-text)] outline-none appearance-none pr-1">
              <option v-for="opt in campusOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
            </select>
          </div>
          <!-- Room type -->
          <div class="flex items-center px-4 h-[52px]">
            <label class="w-24 shrink-0 text-sm text-[var(--c-text)]">教室类别</label>
            <select v-model.number="type" class="flex-1 text-right text-sm bg-transparent text-[var(--c-text)] outline-none appearance-none pr-1">
              <option v-for="opt in roomTypeOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
            </select>
          </div>
          <!-- Seats min -->
          <div class="flex items-center px-4 h-[52px]">
            <label class="w-24 shrink-0 text-sm text-[var(--c-text)]">座位数&ge;</label>
            <input
              type="text"
              inputmode="numeric"
              placeholder="选填"
              v-model="seatsMin"
              @input="seatsMin = ($event.target.value || '').replace(/\D/g, '')"
              class="flex-1 text-right text-sm bg-transparent text-[var(--c-text)] placeholder:text-[var(--c-text-tertiary)] outline-none"
            />
          </div>
          <!-- Seats max -->
          <div class="flex items-center px-4 h-[52px]">
            <label class="w-24 shrink-0 text-sm text-[var(--c-text)]">座位数&le;</label>
            <input
              type="text"
              inputmode="numeric"
              placeholder="选填"
              v-model="seatsMax"
              @input="seatsMax = ($event.target.value || '').replace(/\D/g, '')"
              class="flex-1 text-right text-sm bg-transparent text-[var(--c-text)] placeholder:text-[var(--c-text-tertiary)] outline-none"
            />
          </div>
          <!-- Day of week -->
          <div class="flex items-center px-4 h-[52px]">
            <label class="w-24 shrink-0 text-sm text-[var(--c-text)]">星期</label>
            <select v-model.number="dayOfWeek" class="flex-1 text-right text-sm bg-transparent text-[var(--c-text)] outline-none appearance-none pr-1">
              <option v-for="opt in weekDayOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
            </select>
          </div>
          <!-- Week type -->
          <div class="flex items-center px-4 h-[52px]">
            <label class="w-24 shrink-0 text-sm text-[var(--c-text)]">单双周</label>
            <select v-model.number="weekType" class="flex-1 text-right text-sm bg-transparent text-[var(--c-text)] outline-none appearance-none pr-1">
              <option v-for="opt in singleDoubleOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
            </select>
          </div>
          <!-- Period -->
          <div class="flex items-center px-4 h-[52px]">
            <label class="w-24 shrink-0 text-sm text-[var(--c-text)]">节数</label>
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
          查询
        </button>
      </div>
    </template>

    <!-- Result view -->
    <template v-else>
      <!-- Sticky header -->
      <div class="sticky top-0 z-30 flex items-center h-[52px] px-5 bg-[var(--c-surface)]/90 backdrop-blur-xl border-b border-[var(--c-border)]">
        <button @click="backToSearch" class="text-[var(--c-primary)] text-sm font-medium">&larr; 返回</button>
        <span class="flex-1 text-center text-sm font-bold">查询结果</span>
        <div class="w-10"></div>
      </div>

      <div class="max-w-lg mx-auto px-4 py-6">
        <!-- Loading -->
        <div v-if="loading" class="flex flex-col items-center justify-center py-16 text-[var(--c-text-secondary)]">
          <div class="w-8 h-8 border-2 border-[var(--c-primary)] border-t-transparent rounded-full animate-spin mb-3"></div>
          <span class="text-sm">加载中</span>
        </div>

        <!-- Results list -->
        <template v-else>
          <p class="text-xs text-[var(--c-text-secondary)] mb-3">空课室列表</p>
          <div class="space-y-3">
            <div
              v-for="(item, index) in spareList"
              :key="item.number || index"
              class="bg-[var(--c-surface)] rounded-2xl border border-[var(--c-border)] p-4"
            >
              <h4 class="text-[15px] font-medium text-[var(--c-text)] mb-2">{{ item.name || item.number || '—' }}</h4>
              <div class="space-y-1 text-xs text-[var(--c-text-secondary)]">
                <p>编号：{{ item.number || '—' }}</p>
                <p>类型：{{ item.type || '—' }}</p>
                <p>校区：{{ item.zone || '—' }}</p>
                <p>座位：{{ item.classSeating || '—' }}</p>
              </div>
            </div>
          </div>

          <div v-if="isEmpty" class="text-center py-16 text-sm text-[var(--c-text-secondary)]">
            暂无空课室数据
          </div>
        </template>
      </div>
    </template>
  </div>
</template>
