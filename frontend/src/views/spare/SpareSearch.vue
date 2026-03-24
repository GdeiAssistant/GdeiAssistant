<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useToast } from '@/composables/useToast'

// 与后端 spare 模块一致的数据字典

/** 校区 (zone) - name="zone", 默认 value="0" selected */
const campusOptions = [
  { label: '不限', value: '0' },
  { label: '海珠', value: '1' },
  { label: '花都', value: '2' },
  { label: '广东轻工南海校区', value: '3' },
  { label: '业余函授校区', value: '4' }
]

/** 教室类别 (type) - name="type", 默认第一项 不限 value="0" */
const roomTypeOptions = [
  { label: '不限', value: '0' },
  { label: '不用课室的课程', value: '1' },
  { label: '操场', value: '2' },
  { label: '大多媒体', value: '3' },
  { label: '电脑专业机房', value: '4' },
  { label: '雕塑室', value: '5' },
  { label: '多媒体教室', value: '6' },
  { label: '翻译室', value: '7' },
  { label: '服装实验室', value: '8' },
  { label: '钢琴室', value: '9' },
  { label: '钢琴室', value: '10' },
  { label: '公共机房', value: '11' },
  { label: '国画临摹室', value: '12' },
  { label: '画室', value: '13' },
  { label: '化学实验室', value: '14' },
  { label: '机房', value: '15' },
  { label: '教具室', value: '16' },
  { label: '教育实验室', value: '17' },
  { label: '解剖实验室', value: '18' },
  { label: '金融数学实验室', value: '19' },
  { label: '美术课室', value: '20' },
  { label: '蒙氏教学法专用课室', value: '21' },
  { label: '模型制作实验室', value: '22' },
  { label: '平面制作实验室', value: '23' },
  { label: '琴房', value: '24' },
  { label: '摄影实验室', value: '25' },
  { label: '声乐课室', value: '26' },
  { label: '生物实验室', value: '27' },
  { label: '实训室', value: '28' },
  { label: '视唱练耳室', value: '29' },
  { label: '陶艺室', value: '30' },
  { label: '体操房', value: '31' },
  { label: '网络实验室', value: '32' },
  { label: '微格课室', value: '33' },
  { label: '无须课室', value: '34' },
  { label: '舞蹈室', value: '35' },
  { label: '舞蹈室', value: '36' },
  { label: '物理实验室', value: '37' },
  { label: '小多媒体', value: '38' },
  { label: '小多媒体(<70)', value: '39' },
  { label: '小普通课室(<70)', value: '40' },
  { label: '小组课室', value: '41' },
  { label: '形体房', value: '42' },
  { label: '音乐室', value: '43' },
  { label: '音乐专业课室', value: '44' },
  { label: '语音室', value: '45' },
  { label: '智能录像室', value: '46' },
  { label: '中多媒体(70-100)', value: '47' },
  { label: '专业课教室', value: '48' },
  { label: '专业理论课室', value: '49' },
  { label: '专业实验室', value: '50' },
  { label: '咨询室', value: '51' },
  { label: '综合绘画实验室', value: '52' }
]

/** 星期 (minWeek) - value 0=一...6=日，原版 minWeek 默认 value="0" selected */
const weekDayOptions = [
  { label: '一', value: '0' },
  { label: '二', value: '1' },
  { label: '三', value: '2' },
  { label: '四', value: '3' },
  { label: '五', value: '4' },
  { label: '六', value: '5' },
  { label: '日', value: '6' }
]

/** 单双周 (weekType) - 默认 value="0" selected */
const singleDoubleOptions = [
  { label: '不限', value: '0' },
  { label: '单', value: '1' },
  { label: '双', value: '2' }
]

/** 节数 (classNumber) - 默认 value="0" selected 第1,2节 */
const periodOptions = [
  { label: '第1,2节', value: '0' },
  { label: '第3节', value: '1' },
  { label: '第4,5节', value: '2' },
  { label: '第6,7节', value: '3' },
  { label: '第8,9节', value: '4' },
  { label: '第10,11,12节', value: '5' },
  { label: '上午', value: '6' },
  { label: '下午', value: '7' },
  { label: '晚上', value: '8' },
  { label: '白天', value: '9' },
  { label: '整天', value: '10' }
]

// ========== 响应式状态（绑定 value，与 JSP 默认 selected 一致）==========
const router = useRouter()
const { error: showError } = useToast()
const zone = ref('0')
const type = ref('0')
const seatsMin = ref('')
const seatsMax = ref('')
const startDate = ref(getTodayYYYYMMDD())
const endDate = ref(getTodayYYYYMMDD())
const minWeek = ref('0')
const weekType = ref('0')
const classNumber = ref('0')
const inputStartDate = ref(null)
const inputEndDate = ref(null)

function getTodayYYYYMMDD() {
  const d = new Date()
  return d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0') + '-' + String(d.getDate()).padStart(2, '0')
}

function openStartDatePicker() {
  inputStartDate.value?.click()
}
function openEndDatePicker() {
  inputEndDate.value?.click()
}

function goBack() {
  router.back()
}

function doSearch() {
  const start = startDate.value
  const end = endDate.value
  if (end && start && end < start) {
    showError('结束时间不能早于起始时间！')
    return
  }
  router.push({
    path: '/spare/list',
    query: {
      zone: zone.value,
      type: type.value,
      minSeating: seatsMin.value,
      maxSeating: seatsMax.value,
      startDate: startDate.value,
      endDate: endDate.value,
      minWeek: minWeek.value,
      weekType: weekType.value,
      classNumber: classNumber.value
    }
  })
}
</script>

<template>
  <div class="min-h-screen bg-[var(--c-bg)]">
    <!-- Header -->
    <div class="sticky top-0 z-30 flex items-center h-[52px] px-5 bg-[var(--c-surface)]/90 backdrop-blur-xl border-b border-[var(--c-border)]">
      <button @click="goBack" class="text-[var(--c-primary)] text-sm font-medium">← 返回</button>
      <span class="flex-1 text-center text-sm font-bold">空课室查询</span>
      <div class="w-10"></div>
    </div>

    <div class="max-w-lg mx-auto px-4 py-4">
      <p class="text-xs text-[var(--c-text-quaternary)] mb-3 px-1">查询条件</p>

      <!-- Form Card -->
      <div class="rounded-xl bg-[var(--c-surface)] border border-[var(--c-border)] overflow-hidden">
        <!-- 校区 -->
        <div class="flex items-center px-4 py-3 border-b border-[var(--c-border)]">
          <label class="w-20 text-sm text-[var(--c-text-2)] shrink-0">校区</label>
          <select v-model="zone" class="flex-1 bg-transparent text-sm text-[var(--c-text-primary)] text-right outline-none appearance-none cursor-pointer">
            <option v-for="opt in campusOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
          </select>
        </div>

        <!-- 教室类别 -->
        <div class="flex items-center px-4 py-3 border-b border-[var(--c-border)]">
          <label class="w-20 text-sm text-[var(--c-text-2)] shrink-0">教室类别</label>
          <select v-model="type" class="flex-1 bg-transparent text-sm text-[var(--c-text-primary)] text-right outline-none appearance-none cursor-pointer">
            <option v-for="opt in roomTypeOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
          </select>
        </div>

        <!-- 座位数 min -->
        <div class="flex items-center px-4 py-3 border-b border-[var(--c-border)]">
          <label class="w-20 text-sm text-[var(--c-text-2)] shrink-0">座位数</label>
          <input v-model="seatsMin" type="number" placeholder="大于等于，选填" class="flex-1 bg-transparent text-sm text-[var(--c-text-primary)] text-right placeholder-[var(--c-text-quaternary)] outline-none" />
        </div>

        <!-- 座位数 max -->
        <div class="flex items-center px-4 py-3 border-b border-[var(--c-border)]">
          <label class="w-20 text-sm text-[var(--c-text-2)] shrink-0">座位数</label>
          <input v-model="seatsMax" type="number" placeholder="小于等于，选填" class="flex-1 bg-transparent text-sm text-[var(--c-text-primary)] text-right placeholder-[var(--c-text-quaternary)] outline-none" />
        </div>

        <!-- 起始时间 -->
        <div class="relative flex items-center px-4 py-3 border-b border-[var(--c-border)] cursor-pointer" @click="openStartDatePicker">
          <label class="w-20 text-sm text-[var(--c-text-2)] shrink-0">起始时间</label>
          <span class="flex-1 text-sm text-[var(--c-text-primary)] text-right">{{ startDate }}</span>
          <input ref="inputStartDate" v-model="startDate" type="date" class="absolute opacity-0 w-0 h-0 pointer-events-none" />
        </div>

        <!-- 结束时间 -->
        <div class="relative flex items-center px-4 py-3 border-b border-[var(--c-border)] cursor-pointer" @click="openEndDatePicker">
          <label class="w-20 text-sm text-[var(--c-text-2)] shrink-0">结束时间</label>
          <span class="flex-1 text-sm text-[var(--c-text-primary)] text-right">{{ endDate }}</span>
          <input ref="inputEndDate" v-model="endDate" type="date" class="absolute opacity-0 w-0 h-0 pointer-events-none" />
        </div>

        <!-- 星期 -->
        <div class="flex items-center px-4 py-3 border-b border-[var(--c-border)]">
          <label class="w-20 text-sm text-[var(--c-text-2)] shrink-0">星期</label>
          <select v-model="minWeek" class="flex-1 bg-transparent text-sm text-[var(--c-text-primary)] text-right outline-none appearance-none cursor-pointer">
            <option v-for="opt in weekDayOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
          </select>
        </div>

        <!-- 单双周 -->
        <div class="flex items-center px-4 py-3 border-b border-[var(--c-border)]">
          <label class="w-20 text-sm text-[var(--c-text-2)] shrink-0">单双周</label>
          <select v-model="weekType" class="flex-1 bg-transparent text-sm text-[var(--c-text-primary)] text-right outline-none appearance-none cursor-pointer">
            <option v-for="opt in singleDoubleOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
          </select>
        </div>

        <!-- 节数 -->
        <div class="flex items-center px-4 py-3">
          <label class="w-20 text-sm text-[var(--c-text-2)] shrink-0">节数</label>
          <select v-model="classNumber" class="flex-1 bg-transparent text-sm text-[var(--c-text-primary)] text-right outline-none appearance-none cursor-pointer">
            <option v-for="opt in periodOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
          </select>
        </div>
      </div>

      <!-- Search button -->
      <div class="mt-6 px-2">
        <button
          type="button"
          @click="doSearch"
          class="w-full py-3 rounded-xl text-white text-base font-medium bg-[var(--c-primary)] active:opacity-80 cursor-pointer transition-opacity"
        >
          查询
        </button>
      </div>
    </div>
  </div>
</template>
