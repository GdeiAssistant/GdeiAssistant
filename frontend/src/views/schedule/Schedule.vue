<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { getSchedule, updateScheduleCache, addCustomSchedule, deleteCustomSchedule } from '@/api/schedule'
import { useToast } from '@/composables/useToast'

const router = useRouter()
const { success: toastSuccess, error: toastError, loading: toastLoading, hideLoading } = useToast()

const loading = ref(false)
const scheduleResult = ref(null)
const currentWeek = ref(1)
const showActionSheet = ref(false)
const showCourseDetail = ref(false)
const selectedCourse = ref(null)

// 添加自定义课程弹窗
const showAddCustomDialog = ref(false)
const addCustomSubmitting = ref(false)
const addCustomForm = ref({
  scheduleName: '',
  scheduleLocation: '',
  dayOfWeek: 1,      // 1-7，对应周一到周日
  startSection: 1,   // 1-10，对应第1到第10节
  scheduleLength: 1,
  minScheduleWeek: 1,
  maxScheduleWeek: 20
})

const dayLabels = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']

// 开始节数变化时，若当前占用节数超出当天剩余节数，自动重置为合法最大值
watch(
  () => addCustomForm.value.startSection,
  (newStart) => {
    const maxLen = Math.min(5, 11 - (newStart || 1))
    if (addCustomForm.value.scheduleLength > maxLen) {
      addCustomForm.value.scheduleLength = maxLen
    }
  }
)

// 周次选择器数据：与 schedule.js 的 weekPicker 一致，1-20 周
const weekPickerOptions = Array.from({ length: 20 }, (_, i) => ({ label: `第${i + 1}周`, value: i + 1 }))

const showWeekPicker = ref(false)

// 动态获取今日索引：0=周一, 1=周二, … 6=周日，用于表头与整列高亮
const todayIndex = computed(() => (new Date().getDay() + 6) % 7)

// 仅当 currentWeek 在 [minScheduleWeek, maxScheduleWeek] 内时显示该课程
const filteredList = computed(() => {
  if (!scheduleResult.value || !scheduleResult.value.scheduleList) return []
  const week = currentWeek.value
  return scheduleResult.value.scheduleList.filter(
    (s) => s.minScheduleWeek != null && s.maxScheduleWeek != null && week >= s.minScheduleWeek && week <= s.maxScheduleWeek
  )
})

// 无课周（如第 19、20 周）时显示空状态
const showEmptyState = computed(() => {
  return !!scheduleResult.value && filteredList.value.length === 0
})

// 课程颜色配置
const courseColors = [
  '#3B82F6', '#10B981', '#F59E0B', '#EF4444', '#8B5CF6',
  '#EC4899', '#06B6D4', '#F97316', '#6366F1', '#14B8A6',
  '#E11D48', '#7C3AED', '#0EA5E9', '#84CC16'
]

function getCourseColor(course) {
  return course.colorCode || courseColors[(course.position || 0) % courseColors.length]
}

function getCourseStyle(course) {
  const color = getCourseColor(course)
  return {
    gridColumn: course.column + 2,
    gridRow: `${course.row + 2} / span ${course.scheduleLength}`,
    borderLeftColor: color,
    backgroundColor: `${color}14`,
  }
}

async function fetchSchedule() {
  loading.value = true
  scheduleResult.value = null
  try {
    const res = await getSchedule(currentWeek.value)
    if (res && res.success && res.data) {
      scheduleResult.value = res.data
      currentWeek.value = res.data.week != null ? res.data.week : currentWeek.value
    }
  } finally {
    loading.value = false
  }
}

function openWeekPicker() {
  showWeekPicker.value = true
}

function closeWeekPicker() {
  showWeekPicker.value = false
}

function selectWeek(weekValue) {
  currentWeek.value = weekValue
  closeWeekPicker()
  fetchSchedule()
}

function showOptionMenu() {
  showActionSheet.value = true
}

function closeActionSheet() {
  showActionSheet.value = false
}

function onManageCache() {
  closeActionSheet()
  router.push('/user/privacy-setting')
}

async function onRefreshSchedule() {
  closeActionSheet()
  toastLoading('正在同步课表...')
  try {
    const res = await updateScheduleCache()
    if (res && res.success) {
      hideLoading()
      toastSuccess('更新成功')
      await fetchSchedule()
    }
  } catch (e) {
    hideLoading()
    // 错误文案（含测试账号受限）由全局拦截器统一提示
  } finally {
    hideLoading()
  }
}

function onAddCustomCourse() {
  closeActionSheet()
  addCustomForm.value = {
    scheduleName: '',
    scheduleLocation: '',
    dayOfWeek: 1,
    startSection: 1,
    scheduleLength: 1,
    minScheduleWeek: currentWeek.value,
    maxScheduleWeek: currentWeek.value
  }
  showAddCustomDialog.value = true
}

function closeAddCustomDialog() {
  if (!addCustomSubmitting.value) showAddCustomDialog.value = false
}

async function submitAddCustom() {
  const f = addCustomForm.value
  const name = (f.scheduleName || '').trim()
  const location = (f.scheduleLocation || '').trim()
  if (!name) {
    toastError('请输入课程名称')
    return
  }
  if (!location) {
    toastError('请输入上课地点')
    return
  }

  const dayOfWeek = Math.max(1, Math.min(7, parseInt(f.dayOfWeek, 10) || 1))
  const startSection = Math.max(1, Math.min(10, parseInt(f.startSection, 10) || 1))
  const row = startSection - 1
  const column = dayOfWeek - 1
  const position = row * 7 + column

  const scheduleLength = Math.max(1, Math.min(5, parseInt(f.scheduleLength, 10) || 1))
  const minWeek = Math.max(1, Math.min(20, parseInt(f.minScheduleWeek, 10) || 1))
  const maxWeek = Math.max(1, Math.min(20, parseInt(f.maxScheduleWeek, 10) || 1))
  if (minWeek > maxWeek) {
    toastError('开始周不能大于结束周')
    return
  }
  if (startSection + scheduleLength - 1 > 10) {
    toastError('课程结束节数不能超过全天最大节数(10节)')
    return
  }

  const list = scheduleResult.value?.scheduleList
  if (Array.isArray(list)) {
    const newCol = position % 7
    const newRowEnd = row + scheduleLength - 1
    for (const s of list) {
      const exPos = s.position
      if (exPos == null) continue
      const exCol = exPos % 7
      if (newCol !== exCol) continue
      const exRow = s.row != null ? s.row : Math.floor(exPos / 7)
      const exLen = (s.scheduleLength != null ? s.scheduleLength : 1)
      const exRowEnd = exRow + exLen - 1
      if (newRowEnd < exRow || exRowEnd < row) continue
      const exMin = s.minScheduleWeek != null ? s.minScheduleWeek : 1
      const exMax = s.maxScheduleWeek != null ? s.maxScheduleWeek : 20
      if (minWeek <= exMax && maxWeek >= exMin) {
        toastError('该时间段已存在课程')
        return
      }
    }
  }

  const payload = {
    scheduleName: name,
    scheduleLocation: location,
    scheduleLength,
    position,
    minScheduleWeek: minWeek,
    maxScheduleWeek: maxWeek
  }

  addCustomSubmitting.value = true
  try {
    await addCustomSchedule(payload)
    toastSuccess('添加成功')
    showAddCustomDialog.value = false
    await fetchSchedule()
  } catch (e) {
    // 错误由 request.js 全局拦截器统一提示
  } finally {
    addCustomSubmitting.value = false
  }
}

async function handleDeleteCustomCourse() {
  if (!selectedCourse.value || !isCustomCourse(selectedCourse.value)) return
  const position = selectedCourse.value.position
  closeCourseDetail()

  if (window.confirm && window.confirm('确定要删除这节自定义课程吗？')) {
    try {
      await deleteCustomSchedule(position)
      toastSuccess('删除成功')
      await fetchSchedule()
    } catch (e) {
      // 错误由 request.js 全局拦截器统一提示
    }
  }
}

/** 仅自定义课程可删除：只认后端下发的显式标记 isCustom === true */
function isCustomCourse(course) {
  return course && course.isCustom === true
}

function openCourseDetail(course) {
  selectedCourse.value = course
  showCourseDetail.value = true
}

function closeCourseDetail() {
  showCourseDetail.value = false
  selectedCourse.value = null
}

onMounted(() => {
  fetchSchedule()
})
</script>

<template>
  <div class="min-h-screen bg-[var(--c-bg)] pb-6">
    <!-- Header -->
    <div class="sticky top-0 z-30 flex items-center h-[52px] px-5 bg-[var(--c-surface)]/90 backdrop-blur-xl border-b border-[var(--c-border)]">
      <button @click="$router.back()" class="text-[var(--c-primary)] text-sm font-medium">← 返回</button>
      <span class="flex-1 text-center text-sm font-bold">课程表</span>
      <button @click="showOptionMenu" class="text-[var(--c-primary)] text-sm font-medium w-10 text-right">更多</button>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="flex items-center justify-center py-20">
      <div class="flex flex-col items-center gap-2">
        <div class="w-8 h-8 border-2 border-[var(--c-primary)] border-t-transparent rounded-full animate-spin"></div>
        <span class="text-xs text-[var(--c-text-3)]">加载中</span>
      </div>
    </div>

    <!-- Week selector pill -->
    <div class="flex justify-center py-3">
      <button
        @click="openWeekPicker"
        class="inline-flex items-center gap-1.5 px-4 py-1.5 rounded-full bg-[var(--c-primary)]/8 text-[var(--c-primary)] text-sm font-medium transition active:scale-95"
      >
        <span>{{ scheduleResult ? `第${scheduleResult.week}周` : '选择周数' }}</span>
        <svg class="w-3.5 h-3.5 opacity-60" viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M5.23 7.21a.75.75 0 011.06.02L10 11.168l3.71-3.938a.75.75 0 111.08 1.04l-4.25 4.5a.75.75 0 01-1.08 0l-4.25-4.5a.75.75 0 01.02-1.06z" clip-rule="evenodd"/></svg>
      </button>
    </div>

    <!-- Schedule grid -->
    <div class="relative w-full min-h-[500px] bg-[var(--c-surface)]">
      <div class="grid w-full min-h-[500px]" style="grid-template-columns: 30px repeat(7, 1fr); grid-template-rows: 36px repeat(12, 1fr);">
        <!-- Corner cell -->
        <div class="flex items-center justify-center text-xs text-[var(--c-text-3)] border-[0.5px] border-[var(--c-divider)] bg-[var(--c-surface)]" style="grid-column: 1; grid-row: 1;"></div>

        <!-- Day headers -->
        <div
          v-for="(label, idx) in dayLabels"
          :key="'head-' + label"
          class="flex flex-col items-center justify-center gap-1 text-xs border-[0.5px] border-[var(--c-divider)] bg-[var(--c-surface)]"
          :class="idx === todayIndex ? 'text-[var(--c-primary)] font-medium' : 'text-[var(--c-text-1)]'"
          :style="{
            gridColumn: idx + 2,
            gridRow: 1,
            backgroundColor: idx === todayIndex ? 'rgba(4,120,87,0.06)' : undefined
          }"
        >
          <span>{{ label }}</span>
          <span v-if="idx === todayIndex" class="w-1 h-1 rounded-full bg-[var(--c-primary)]" aria-hidden="true"></span>
        </div>

        <!-- Row index (section numbers) -->
        <div
          v-for="row in 12"
          :key="'index-' + row"
          class="flex items-center justify-center text-xs font-mono text-[var(--c-text-3)] bg-[var(--c-surface)] border-[0.5px] border-[var(--c-divider)] border-r-[1px] min-w-[30px]"
          :style="{ gridColumn: 1, gridRow: row + 1 }"
        >
          {{ row }}
        </div>

        <!-- 84 empty grid slots -->
        <div
          v-for="(_, index) in 84"
          :key="'slot-' + index"
          class="border-[0.5px] border-[var(--c-divider)]"
          :style="{
            gridColumn: (index % 7) + 2,
            gridRow: Math.floor(index / 7) + 2,
            backgroundColor: (index % 7) === todayIndex ? 'rgba(4,120,87,0.06)' : 'var(--c-bg)'
          }"
        ></div>

        <!-- Empty state -->
        <div
          v-if="showEmptyState"
          class="flex flex-col items-center justify-center z-10 pointer-events-none"
          style="grid-column: 2 / -1; grid-row: 2 / -1;"
          aria-live="polite"
        >
          <svg class="shrink-0" width="80" height="80" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1" stroke-linecap="round" stroke-linejoin="round" style="color: var(--c-border)">
            <rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect>
            <line x1="16" y1="2" x2="16" y2="6"></line>
            <line x1="8" y1="2" x2="8" y2="6"></line>
            <line x1="3" y1="10" x2="21" y2="10"></line>
            <path d="M8 14h.01"></path><path d="M12 14h.01"></path><path d="M16 14h.01"></path>
            <path d="M8 18h.01"></path><path d="M12 18h.01"></path><path d="M16 18h.01"></path>
          </svg>
          <p class="mt-4 text-sm text-[var(--c-text-3)]">本周暂无课程，好好休息吧</p>
        </div>

        <!-- Course blocks -->
        <button
          v-for="(course, index) in filteredList"
          :key="'course-' + index"
          type="button"
          class="m-[2px] px-1 py-1.5 rounded-r-lg border-l-[3px] overflow-hidden flex items-center justify-center text-center cursor-pointer shadow-sm transition active:scale-[0.97]"
          :style="getCourseStyle(course)"
          @click="openCourseDetail(course)"
        >
          <div class="flex flex-col items-center justify-center gap-0.5 w-full min-h-0 break-all leading-tight">
            <span class="text-xs font-semibold text-[var(--c-text-1)]">{{ course.scheduleName }}</span>
            <span class="text-[10px] text-[var(--c-text-3)]">{{ course.scheduleLocation }}</span>
          </div>
        </button>
      </div>
    </div>

    <!-- Action Sheet -->
    <Teleport to="body">
      <template v-if="showActionSheet">
        <div class="fixed inset-0 bg-black/40 z-[5000]" @click="closeActionSheet"></div>
        <div class="fixed inset-x-0 bottom-0 z-[5001] rounded-t-2xl bg-[var(--c-surface)] shadow-2xl animate-slide-up">
          <div class="py-2">
            <button
              @click="onManageCache"
              class="w-full py-3.5 text-center text-[15px] text-[var(--c-text-1)] active:bg-[var(--c-bg)] transition"
            >管理缓存配置</button>
            <div class="mx-4 border-t border-[var(--c-divider)]"></div>
            <button
              @click="onRefreshSchedule"
              class="w-full py-3.5 text-center text-[15px] text-[var(--c-text-1)] active:bg-[var(--c-bg)] transition"
            >更新实时数据</button>
            <div class="mx-4 border-t border-[var(--c-divider)]"></div>
            <button
              @click="onAddCustomCourse"
              class="w-full py-3.5 text-center text-[15px] text-[var(--c-text-1)] active:bg-[var(--c-bg)] transition"
            >添加自定义课程</button>
          </div>
          <div class="border-t-[6px] border-[var(--c-bg)]">
            <button
              @click="closeActionSheet"
              class="w-full py-3.5 text-center text-[15px] font-medium text-[var(--c-text-3)] active:bg-[var(--c-bg)] transition"
            >取消</button>
          </div>
          <div class="pb-[env(safe-area-inset-bottom)]"></div>
        </div>
      </template>
    </Teleport>

    <!-- Course detail dialog -->
    <Teleport to="body">
      <template v-if="showCourseDetail && selectedCourse">
        <div class="fixed inset-0 bg-black/40 z-[5000]" @click="closeCourseDetail"></div>
        <div class="fixed left-1/2 top-1/2 -translate-x-1/2 -translate-y-1/2 w-[calc(100%-32px)] max-w-[340px] bg-[var(--c-surface)] rounded-2xl z-[5001] shadow-xl">
          <!-- Header -->
          <div class="flex items-center justify-between px-4 py-3 border-b border-[var(--c-divider)]">
            <h3 class="text-[15px] font-semibold text-[var(--c-text-1)]">课程详细信息</h3>
            <button @click="closeCourseDetail" class="text-sm text-[var(--c-primary)] font-medium">关闭</button>
          </div>
          <!-- Body -->
          <div class="px-4 py-4 space-y-3">
            <div class="flex items-baseline gap-2">
              <span class="text-xs text-[var(--c-text-3)] shrink-0 w-16">课程名称</span>
              <span class="text-sm text-[var(--c-text-1)]">{{ selectedCourse.scheduleName }}</span>
            </div>
            <div class="flex items-baseline gap-2">
              <span class="text-xs text-[var(--c-text-3)] shrink-0 w-16">上课地点</span>
              <span class="text-sm text-[var(--c-text-1)]">{{ selectedCourse.scheduleLocation }}</span>
            </div>
            <div class="flex items-baseline gap-2">
              <span class="text-xs text-[var(--c-text-3)] shrink-0 w-16">任课教师</span>
              <span class="text-sm text-[var(--c-text-1)]">{{ (selectedCourse.scheduleTeacher && String(selectedCourse.scheduleTeacher).trim() !== '' && String(selectedCourse.scheduleTeacher).trim() !== '—' && String(selectedCourse.scheduleTeacher).trim() !== '-') ? selectedCourse.scheduleTeacher : '无' }}</span>
            </div>
            <div class="flex items-baseline gap-2">
              <span class="text-xs text-[var(--c-text-3)] shrink-0 w-16">上课周次</span>
              <span class="text-sm text-[var(--c-text-1)]">第{{ selectedCourse.minScheduleWeek }}周至第{{ selectedCourse.maxScheduleWeek }}周</span>
            </div>
          </div>
          <!-- Footer -->
          <div v-if="isCustomCourse(selectedCourse)" class="px-4 pb-4 flex justify-center">
            <button
              @click="handleDeleteCustomCourse"
              class="w-full max-w-[200px] py-2 rounded-lg border border-red-500 text-red-500 text-sm font-medium transition active:bg-red-50"
            >删除</button>
          </div>
        </div>
      </template>
    </Teleport>

    <!-- Add custom course dialog -->
    <Teleport to="body">
      <template v-if="showAddCustomDialog">
        <div class="fixed inset-0 bg-black/40 z-[5000]" @click="closeAddCustomDialog"></div>
        <div class="fixed left-1/2 top-1/2 -translate-x-1/2 -translate-y-1/2 w-[calc(100%-32px)] max-w-[380px] max-h-[85vh] overflow-y-auto bg-[var(--c-surface)] rounded-2xl z-[5001] shadow-xl">
          <!-- Header -->
          <div class="flex items-center justify-between px-4 py-3 border-b border-[var(--c-divider)] sticky top-0 bg-[var(--c-surface)] rounded-t-2xl">
            <h3 class="text-[15px] font-semibold text-[var(--c-text-1)]">添加自定义课程</h3>
            <button @click="closeAddCustomDialog" class="text-sm text-[var(--c-primary)] font-medium">关闭</button>
          </div>
          <!-- Form -->
          <div class="px-4 py-4 space-y-4">
            <!-- 课程名称 -->
            <div>
              <label class="block text-xs text-[var(--c-text-3)] mb-1.5">课程名称</label>
              <input
                v-model="addCustomForm.scheduleName"
                type="text"
                maxlength="50"
                placeholder="请输入课程名称"
                class="w-full rounded-lg border border-[var(--c-border)] bg-[var(--c-bg)] px-3 py-2 text-sm text-[var(--c-text-1)] placeholder-[var(--c-text-3)] outline-none transition focus:border-[var(--c-primary)] focus:ring-2 focus:ring-[var(--c-primary)]/20"
              />
            </div>
            <!-- 上课地点 -->
            <div>
              <label class="block text-xs text-[var(--c-text-3)] mb-1.5">上课地点</label>
              <input
                v-model="addCustomForm.scheduleLocation"
                type="text"
                maxlength="25"
                placeholder="请输入上课地点"
                class="w-full rounded-lg border border-[var(--c-border)] bg-[var(--c-bg)] px-3 py-2 text-sm text-[var(--c-text-1)] placeholder-[var(--c-text-3)] outline-none transition focus:border-[var(--c-primary)] focus:ring-2 focus:ring-[var(--c-primary)]/20"
              />
            </div>
            <!-- 星期几 -->
            <div>
              <label class="block text-xs text-[var(--c-text-3)] mb-1.5">星期几</label>
              <select
                v-model.number="addCustomForm.dayOfWeek"
                class="w-full rounded-lg border border-[var(--c-border)] bg-[var(--c-bg)] px-3 py-2 text-sm text-[var(--c-text-1)] outline-none transition focus:border-[var(--c-primary)] focus:ring-2 focus:ring-[var(--c-primary)]/20 appearance-none"
              >
                <option v-for="d in 7" :key="d" :value="d">周{{ ['一','二','三','四','五','六','日'][d-1] }}</option>
              </select>
            </div>
            <!-- 开始节数 -->
            <div>
              <label class="block text-xs text-[var(--c-text-3)] mb-1.5">开始节数</label>
              <select
                v-model.number="addCustomForm.startSection"
                class="w-full rounded-lg border border-[var(--c-border)] bg-[var(--c-bg)] px-3 py-2 text-sm text-[var(--c-text-1)] outline-none transition focus:border-[var(--c-primary)] focus:ring-2 focus:ring-[var(--c-primary)]/20 appearance-none"
              >
                <option v-for="s in 10" :key="s" :value="s">第{{ s }}节</option>
              </select>
            </div>
            <!-- 占用节数 -->
            <div>
              <label class="block text-xs text-[var(--c-text-3)] mb-1.5">占用节数</label>
              <select
                v-model.number="addCustomForm.scheduleLength"
                class="w-full rounded-lg border border-[var(--c-border)] bg-[var(--c-bg)] px-3 py-2 text-sm text-[var(--c-text-1)] outline-none transition focus:border-[var(--c-primary)] focus:ring-2 focus:ring-[var(--c-primary)]/20 appearance-none"
              >
                <option v-for="len in Math.max(1, Math.min(5, 11 - (addCustomForm.startSection || 1)))" :key="len" :value="len">{{ len }}节</option>
              </select>
            </div>
            <!-- 周次范围 -->
            <div class="grid grid-cols-2 gap-3">
              <div>
                <label class="block text-xs text-[var(--c-text-3)] mb-1.5">开始周</label>
                <select
                  v-model.number="addCustomForm.minScheduleWeek"
                  class="w-full rounded-lg border border-[var(--c-border)] bg-[var(--c-bg)] px-3 py-2 text-sm text-[var(--c-text-1)] outline-none transition focus:border-[var(--c-primary)] focus:ring-2 focus:ring-[var(--c-primary)]/20 appearance-none"
                >
                  <option v-for="w in 20" :key="w" :value="w">第{{ w }}周</option>
                </select>
              </div>
              <div>
                <label class="block text-xs text-[var(--c-text-3)] mb-1.5">结束周</label>
                <select
                  v-model.number="addCustomForm.maxScheduleWeek"
                  class="w-full rounded-lg border border-[var(--c-border)] bg-[var(--c-bg)] px-3 py-2 text-sm text-[var(--c-text-1)] outline-none transition focus:border-[var(--c-primary)] focus:ring-2 focus:ring-[var(--c-primary)]/20 appearance-none"
                >
                  <option v-for="w in 20" :key="w" :value="w">第{{ w }}周</option>
                </select>
              </div>
            </div>
          </div>
          <!-- Submit -->
          <div class="px-4 pb-4 flex justify-center">
            <button
              type="button"
              :disabled="addCustomSubmitting"
              @click="submitAddCustom"
              class="w-full max-w-[200px] py-2.5 rounded-full bg-[var(--c-primary)] text-white text-sm font-semibold transition active:scale-95 disabled:opacity-50"
            >
              {{ addCustomSubmitting ? '提交中...' : '添加' }}
            </button>
          </div>
        </div>
      </template>
    </Teleport>

    <!-- Week picker bottom sheet -->
    <Teleport to="body">
      <template v-if="showWeekPicker">
        <div class="fixed inset-0 bg-black/40 z-[5000]" @click="closeWeekPicker"></div>
        <div class="fixed inset-x-0 bottom-0 z-[5001] rounded-t-2xl bg-[var(--c-surface)] shadow-2xl max-h-[70vh] flex flex-col animate-slide-up">
          <!-- Header -->
          <div class="flex items-center justify-between px-4 py-3 border-b border-[var(--c-divider)] shrink-0">
            <button @click="closeWeekPicker" class="text-sm text-[var(--c-text-3)]">取消</button>
            <span class="text-[15px] font-semibold text-[var(--c-text-1)]">选择周数</span>
            <div class="w-10"></div>
          </div>
          <!-- Options -->
          <div class="overflow-y-auto py-2" style="-webkit-overflow-scrolling: touch;">
            <button
              v-for="opt in weekPickerOptions"
              :key="opt.value"
              type="button"
              class="w-full py-3 text-center text-[15px] transition active:bg-[var(--c-bg)]"
              :class="opt.value === currentWeek ? 'text-[var(--c-primary)] font-semibold' : 'text-[var(--c-text-1)]'"
              @click="selectWeek(opt.value)"
            >
              {{ opt.label }}
            </button>
          </div>
          <div class="pb-[env(safe-area-inset-bottom)]"></div>
        </div>
      </template>
    </Teleport>
  </div>
</template>

<style>
@keyframes slide-up {
  from { transform: translateY(100%); }
  to { transform: translateY(0); }
}
.animate-slide-up {
  animation: slide-up 0.25s ease-out;
}
</style>
