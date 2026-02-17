<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '../../utils/request'

const router = useRouter()

const loading = ref(false)
const scheduleResult = ref(null)
const currentWeek = ref(1)
const showActionSheet = ref(false)
const showCourseDetail = ref(false)
const selectedCourse = ref(null)
const toastMessage = ref('')
const showToast = ref(false)
let toastTimer = null

const dayLabels = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']

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

async function fetchSchedule() {
  loading.value = true
  scheduleResult.value = null
  try {
    const res = await request.post('/schedulequery', { week: currentWeek.value })
    if (res && res.data) {
      scheduleResult.value = res.data
      currentWeek.value = res.data.week
    }
  } finally {
    loading.value = false
  }
}

function goBack() {
  router.back()
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
  showWeuiToast('该功能将在后续模块迁移中实现')
}

function onRefreshSchedule() {
  closeActionSheet()
  fetchSchedule()
}

function onAddCustomCourse() {
  closeActionSheet()
  showWeuiToast('该功能将在后续模块迁移中实现')
}

function showWeuiToast(message) {
  toastMessage.value = message
  showToast.value = true
  if (toastTimer) clearTimeout(toastTimer)
  toastTimer = setTimeout(() => {
    showToast.value = false
    toastTimer = null
  }, 2000)
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
  <div class="schedule-page">
    <template v-if="loading">
      <div class="weui-mask_transparent" aria-hidden="true"></div>
      <div class="weui-toast__wrp">
        <div class="weui-toast">
          <span class="weui-primary-loading weui-icon_toast" aria-label="加载中"></span>
          <p class="weui-toast__content">加载中</p>
        </div>
      </div>
    </template>

    <div class="top-nav-bar">
      <div class="nav-btn-back" @click="goBack">返回</div>
      <div class="nav-btn-more" @click="showOptionMenu">更多</div>
    </div>
    <h1 class="page-title-green">我的课程表</h1>

    <div class="schedule-week" @click="openWeekPicker" role="button" tabindex="0">
      <span class="schedule-week__text">{{ scheduleResult ? `第${scheduleResult.week}周` : '选择周数' }}</span>
      <span class="schedule-week__chevron" aria-hidden="true">▼</span>
    </div>

    <div class="schedule-grid-wrap">
      <div class="schedule-grid">
        <!-- 左上角空白：固定 (1,1) -->
        <div class="schedule-grid__cell schedule-grid__cell--head schedule-grid__cell--corner" style="grid-column: 1; grid-row: 1;"></div>
      <!-- 表头：周一至周日，今日列内联背景与下方格子一体化 -->
      <div
        v-for="(label, idx) in dayLabels"
        :key="'head-' + label"
        class="schedule-grid__cell schedule-grid__cell--head"
        :class="{ 'today-highlight': idx === todayIndex }"
        :style="{
          gridColumn: idx + 2,
          gridRow: 1,
          backgroundColor: idx === todayIndex ? 'rgba(9, 187, 7, 0.08)' : '#fff'
        }"
      >
        <span class="schedule-grid__head-label">{{ label }}</span>
        <span v-if="idx === todayIndex" class="schedule-grid__head-dot" aria-hidden="true"></span>
      </div>
      <!-- 节次列：1-12 纯白背景、灰色文字，无高亮 -->
      <div
        v-for="row in 12"
        :key="'index-' + row"
        class="schedule-grid__cell schedule-grid__cell--index"
        :style="{ gridColumn: 1, gridRow: row + 1 }"
      >
        {{ row }}
      </div>
      <!-- 84 格：index 0 = Col2 Row2，内联 backgroundColor 强制今日列（含首格）变绿 -->
      <div
        v-for="(_, index) in 84"
        :key="'slot-' + index"
        class="schedule-grid__cell schedule-grid__cell--slot"
        :class="{ 'schedule-grid__cell--today-col': (index % 7) === todayIndex }"
        :style="{
          gridColumn: (index % 7) + 2,
          gridRow: Math.floor(index / 7) + 2,
          backgroundColor: (index % 7) === todayIndex ? 'rgba(9, 187, 7, 0.08)' : '#fcfdfe'
        }"
      ></div>
      <!-- 空状态：无课周时作为“超级格子”占据右侧全部区域，与节次列同级 -->
      <div
        v-if="showEmptyState"
        class="empty-state"
        aria-live="polite"
      >
        <svg width="100" height="100" viewBox="0 0 24 24" fill="none" stroke="#dcdcdc" stroke-width="1.2" stroke-linecap="round" stroke-linejoin="round" xmlns="http://www.w3.org/2000/svg" aria-hidden="true">
          <rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect>
          <line x1="16" y1="2" x2="16" y2="6"></line>
          <line x1="8" y1="2" x2="8" y2="6"></line>
          <line x1="3" y1="10" x2="21" y2="10"></line>
          <path d="M8 14h.01"></path>
          <path d="M12 14h.01"></path>
          <path d="M16 14h.01"></path>
          <path d="M8 18h.01"></path>
          <path d="M12 18h.01"></path>
          <path d="M16 18h.01"></path>
        </svg>
        <p class="empty-state__text">本周暂无课程，好好休息吧</p>
      </div>
      <!-- 课程块：显式定位，column+2 跳过节次列，row+2 跳过表头行 -->
      <div
        v-for="(course, index) in filteredList"
        :key="'course-' + index"
        class="schedule-grid__course"
        role="button"
        tabindex="0"
        :style="{
          gridColumn: course.column + 2,
          gridRow: `${course.row + 2} / span ${course.scheduleLength}`,
          backgroundColor: course.colorCode || '#07b2ff'
        }"
        @click="openCourseDetail(course)"
      >
        <div class="schedule-grid__course-inner">
          <span class="schedule-grid__course-name">{{ course.scheduleName }}</span>
          <span class="schedule-grid__course-location">{{ course.scheduleLocation }}</span>
        </div>
      </div>
    </div>
    </div>

    <!-- 更多 ActionSheet（与旧版 schedule.js showOptionMenu 一致） -->
    <template v-if="showActionSheet">
      <div class="weui-mask" @click="closeActionSheet" aria-hidden="true"></div>
      <div class="weui-actionsheet weui-actionsheet_toggle" role="dialog" aria-label="更多选项">
        <div class="weui-actionsheet__menu">
          <div class="weui-actionsheet__cell" @click="onManageCache">管理缓存配置</div>
          <div class="weui-actionsheet__cell" @click="onRefreshSchedule">更新实时数据</div>
          <div class="weui-actionsheet__cell" @click="onAddCustomCourse">添加自定义课程</div>
        </div>
        <div class="weui-actionsheet__action">
          <div class="weui-actionsheet__cell" @click="closeActionSheet">取消</div>
        </div>
      </div>
    </template>

    <!-- 课程详情弹窗 -->
    <template v-if="showCourseDetail && selectedCourse">
      <div class="weui-mask" @click="closeCourseDetail" aria-hidden="true"></div>
      <div class="schedule-detail-dialog" role="dialog" aria-label="课程详情">
        <div class="schedule-detail-dialog__hd">
          <h3 class="schedule-detail-dialog__title">课程详细信息</h3>
          <div class="schedule-detail-dialog__close" @click="closeCourseDetail">关闭</div>
        </div>
        <div class="schedule-detail-dialog__bd weui-form-preview__bd">
          <div class="weui-form-preview__item">
            <label class="weui-form-preview__label">课程名称</label>
            <span class="weui-form-preview__value">{{ selectedCourse.scheduleName }}</span>
          </div>
          <div class="weui-form-preview__item">
            <label class="weui-form-preview__label">上课地点</label>
            <span class="weui-form-preview__value">{{ selectedCourse.scheduleLocation }}</span>
          </div>
          <div class="weui-form-preview__item">
            <label class="weui-form-preview__label">任课教师</label>
            <span class="weui-form-preview__value">{{ selectedCourse.scheduleTeacher || '—' }}</span>
          </div>
          <div class="weui-form-preview__item">
            <label class="weui-form-preview__label">上课周次</label>
            <span class="weui-form-preview__value">第{{ selectedCourse.minScheduleWeek }}周至第{{ selectedCourse.maxScheduleWeek }}周</span>
          </div>
        </div>
      </div>
    </template>

    <!-- 周次选择器（WEUI Picker 风格：底部弹出，1-20 周，选择后刷新） -->
    <template v-if="showWeekPicker">
      <div class="weui-mask" @click="closeWeekPicker" aria-hidden="true"></div>
      <div class="week-picker" role="dialog" aria-label="选择周数">
        <div class="week-picker__hd">
          <div class="week-picker__cancel" @click="closeWeekPicker">取消</div>
          <div class="week-picker__title">选择周数</div>
          <div class="week-picker__placeholder"></div>
        </div>
        <div class="week-picker__bd">
          <div
            v-for="opt in weekPickerOptions"
            :key="opt.value"
            class="week-picker__item"
            :class="{ 'week-picker__item--active': opt.value === currentWeek }"
            @click="selectWeek(opt.value)"
          >
            {{ opt.label }}
          </div>
        </div>
      </div>
    </template>

    <!-- Toast -->
    <template v-if="showToast">
      <div class="weui-mask_transparent" aria-hidden="true"></div>
      <div class="weui-toast__wrp">
        <div class="weui-toast weui-toast_text">
          <p class="weui-toast__content">{{ toastMessage }}</p>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
.schedule-page {
  background-color: #fff;
  min-height: 100vh;
  padding-bottom: 24px;
  width: 100%;
}

/* 导航栏：与 Grade 完全一致，返回/更多/标题视觉高度统一 */
.top-nav-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  min-height: 44px;
  padding: 10px 15px;
  background-color: #fff;
  box-sizing: border-box;
}

.nav-btn-back,
.nav-btn-more {
  font-size: 16px;
  line-height: 24px;
  color: #888;
  cursor: pointer;
  flex-shrink: 0;
}

.page-title-green {
  text-align: center;
  font-size: 34px;
  color: #09bb07;
  font-weight: 400;
  margin: 10px 0 20px 0;
  line-height: 1.2;
}

.schedule-week {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  height: 3rem;
  line-height: 1.2;
  text-align: center;
  margin-bottom: 10px;
  color: #979797;
  font-size: 14px;
  cursor: pointer;
  -webkit-tap-highlight-color: transparent;
}

.schedule-week__text {
  margin: 0;
}

.schedule-week__chevron {
  font-size: 10px;
  opacity: 0.8;
}

/* 网格外层 */
.schedule-grid-wrap {
  position: relative;
  width: 100%;
  min-height: 500px;
  background-color: #fff;
}

/* 课表网格：纸质底色 #fcfdfe，极细网格线 */
.schedule-grid {
  display: grid;
  grid-template-columns: 30px repeat(7, 1fr);
  grid-template-rows: 36px repeat(12, 1fr);
  width: 100%;
  min-height: 500px;
  position: relative;
  background-color: #fcfdfe;
}

.schedule-grid__cell {
  border: 0.5px solid #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: #5a5a5a;
  background: #fcfdfe;
  box-sizing: border-box;
}

.schedule-grid__cell--corner {
  background: #fcfdfe;
}

.schedule-grid__cell--head {
  padding: 8px 4px;
  font-size: 12px;
  color: #333;
  background: #fff;
  border: 0.5px solid #f0f0f0;
  flex-direction: column;
  gap: 4px;
}

.schedule-grid__head-label {
  display: block;
}

.schedule-grid__head-dot {
  display: block;
  width: 3px;
  height: 3px;
  border-radius: 50%;
  background-color: #09bb07;
}

.schedule-grid__cell--head.today-highlight {
  color: #09bb07;
  font-weight: 500;
  background: rgba(9, 187, 7, 0.08) !important;
}

/* 节次列：纯白背景、灰色文字，始终无高亮 */
.schedule-grid__cell--index {
  color: #5a5a5a;
  font-weight: 500;
  background: #fff;
  border: 0.5px solid #f0f0f0;
  border-right: 1px solid #f2f2f2;
  min-width: 30px;
}

.schedule-grid__cell--slot {
  background: #fcfdfe;
  border: 0.5px solid #f0f0f0;
}

/* 今日整列：与内联 backgroundColor 双保险，任意星期今日列从第一行起全部显绿 */
.schedule-grid__cell--slot.schedule-grid__cell--today-col {
  background-color: rgba(9, 187, 7, 0.08) !important;
}

/* 加固今日列格子，防止任何规则覆盖首行 */
.schedule-grid > .schedule-grid__cell.schedule-grid__cell--slot.schedule-grid__cell--today-col {
  background-color: rgba(9, 187, 7, 0.08) !important;
}

/* 课程块：便利贴悬浮感，14 色由 Mock colorCode 提供；略透明让今日列绿色透出 */
.schedule-grid__course {
  margin: 2px;
  padding: 6px 4px;
  border-radius: 6px;
  color: #fff;
  font-size: 12px;
  line-height: 1.3;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
  min-height: 0;
  box-sizing: border-box;
  border: none !important;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.05);
  opacity: 0.95;
}

/* 空状态：网格占位，透明背景，层级高于格子确保插画与文案不被今日列盖住 */
.empty-state {
  grid-column: 2 / -1;
  grid-row: 2 / -1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  z-index: 10;
  pointer-events: none;
  background: transparent;
}

.empty-state svg {
  flex-shrink: 0;
}

.empty-state__text {
  margin: 20px 0 0 0;
  font-size: 16px;
  color: #b0b0b0;
  line-height: 1.4;
}

.schedule-grid__course-inner {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  line-height: 1.3;
  gap: 2px;
  width: 100%;
  min-height: 0;
  font-size: 12px;
  white-space: normal;
  word-break: break-all;
}

.schedule-grid__course-name {
  display: block;
  max-width: 100%;
  font-weight: 500;
}

.schedule-grid__course-location {
  display: block;
  font-size: 12px;
  opacity: 0.95;
}

/* 课程详情弹窗（WEUI 风格） */
.schedule-detail-dialog {
  position: fixed;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  width: calc(100% - 32px);
  max-width: 340px;
  background: #fff;
  border-radius: 8px;
  z-index: 5001;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

.schedule-detail-dialog__hd {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-bottom: 1px solid #eee;
}

.schedule-detail-dialog__title {
  margin: 0;
  font-size: 17px;
  font-weight: 500;
  color: #000;
}

.schedule-detail-dialog__close {
  font-size: 16px;
  color: #09bb07;
  cursor: pointer;
}

.schedule-detail-dialog__bd {
  padding: 16px;
}

.schedule-detail-dialog__bd .weui-form-preview__item {
  margin-top: 8px;
}

.schedule-detail-dialog__bd .weui-form-preview__item:first-child {
  margin-top: 0;
}

.schedule-detail-dialog__bd .weui-form-preview__label {
  color: #999;
  margin-right: 8px;
}

.schedule-detail-dialog__bd .weui-form-preview__value {
  color: #333;
}

/* 周次选择器（WEUI Picker 风格：底部弹出） */
.week-picker {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  background: #fff;
  border-radius: 12px 12px 0 0;
  z-index: 5001;
  box-shadow: 0 -4px 20px rgba(0, 0, 0, 0.1);
  max-height: 70vh;
  display: flex;
  flex-direction: column;
}

.week-picker__hd {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-bottom: 1px solid #eee;
  flex-shrink: 0;
}

.week-picker__cancel {
  font-size: 16px;
  color: #888;
  cursor: pointer;
}

.week-picker__title {
  font-size: 17px;
  font-weight: 500;
  color: #000;
}

.week-picker__placeholder {
  width: 48px;
}

.week-picker__bd {
  overflow-y: auto;
  padding: 8px 0;
  -webkit-overflow-scrolling: touch;
}

.week-picker__item {
  padding: 14px 16px;
  font-size: 17px;
  color: #333;
  text-align: center;
  cursor: pointer;
}

.week-picker__item:active {
  background: #f5f5f5;
}

.week-picker__item--active {
  color: #09bb07;
  font-weight: 500;
}
</style>
