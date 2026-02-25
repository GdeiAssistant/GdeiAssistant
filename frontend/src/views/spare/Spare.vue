<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { querySpareRoom } from '@/api/spare'
import { showErrorTopTips } from '@/utils/toast.js'

const router = useRouter()
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

function goBack() {
  router.back()
}

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
  <div class="spare-page">
    <template v-if="!showResult">
      <div class="top-nav-bar">
        <div class="nav-btn-back" @click="goBack">返回</div>
      </div>
      <h1 class="page-title-green">空课室查询</h1>
      <p class="page-subtitle">广东第二师范学院</p>

      <div class="search-form-wrap">
        <div class="weui-cells weui-cells_form">
          <div class="weui-cell spare-form-row spare-row-has-arrow">
            <div class="weui-cell__hd"><label class="weui-label">校区</label></div>
            <div class="weui-cell__bd">
              <select class="weui-select" v-model.number="zone">
                <option v-for="opt in campusOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
              </select>
            </div>
          </div>
          <div class="weui-cell spare-form-row spare-row-has-arrow">
            <div class="weui-cell__hd"><label class="weui-label">教室类别</label></div>
            <div class="weui-cell__bd">
              <select class="weui-select" v-model.number="type">
                <option v-for="opt in roomTypeOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
              </select>
            </div>
          </div>
          <div class="weui-cell spare-form-row">
            <div class="weui-cell__hd"><label class="weui-label">座位数≥</label></div>
            <div class="weui-cell__bd">
              <input class="weui-input" type="text" inputmode="numeric" placeholder="选填" v-model="seatsMin" @input="seatsMin = ($event.target.value || '').replace(/\D/g, '')" />
            </div>
          </div>
          <div class="weui-cell spare-form-row">
            <div class="weui-cell__hd"><label class="weui-label">座位数≤</label></div>
            <div class="weui-cell__bd">
              <input class="weui-input" type="text" inputmode="numeric" placeholder="选填" v-model="seatsMax" @input="seatsMax = ($event.target.value || '').replace(/\D/g, '')" />
            </div>
          </div>
          <div class="weui-cell spare-form-row spare-row-has-arrow">
            <div class="weui-cell__hd"><label class="weui-label">星期</label></div>
            <div class="weui-cell__bd">
              <select class="weui-select" v-model.number="dayOfWeek">
                <option v-for="opt in weekDayOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
              </select>
            </div>
          </div>
          <div class="weui-cell spare-form-row spare-row-has-arrow">
            <div class="weui-cell__hd"><label class="weui-label">单双周</label></div>
            <div class="weui-cell__bd">
              <select class="weui-select" v-model.number="weekType">
                <option v-for="opt in singleDoubleOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
              </select>
            </div>
          </div>
          <div class="weui-cell spare-form-row spare-row-has-arrow">
            <div class="weui-cell__hd"><label class="weui-label">节数</label></div>
            <div class="weui-cell__bd">
              <select class="weui-select" v-model.number="classNumber">
                <option v-for="opt in periodOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
              </select>
            </div>
          </div>
        </div>
      </div>

      <div class="weui-btn_area">
        <button type="button" class="weui-btn weui-btn_primary weui-btn_block" @click="doSearch">查询</button>
      </div>
    </template>

    <template v-else>
      <div class="top-nav-bar">
        <div class="nav-btn-back" @click="backToSearch">返回</div>
      </div>
      <h1 class="page-title-green">查询结果</h1>

      <template v-if="loading">
        <div class="weui-loadmore">
          <span class="weui-primary-loading"></span>
          <span class="weui-loadmore__tips">加载中</span>
        </div>
      </template>
      <template v-else>
        <div class="result-list-wrap">
          <div class="weui-cells__title">空课室列表</div>
          <div class="weui-panel weui-panel_access">
            <div class="weui-panel__bd">
              <div
                v-for="(item, index) in spareList"
                :key="item.number || index"
                class="weui-media-box weui-media-box_text"
              >
                <h4 class="weui-media-box__title">{{ item.name || item.number || '—' }}</h4>
                <p class="weui-media-box__desc">编号：{{ item.number || '—' }}</p>
                <p class="weui-media-box__desc">类型：{{ item.type || '—' }}</p>
                <p class="weui-media-box__desc">校区：{{ item.zone || '—' }}</p>
                <p class="weui-media-box__desc">座位：{{ item.classSeating || '—' }}</p>
              </div>
            </div>
          </div>
        </div>
        <div v-if="isEmpty" class="spare-empty">暂无空课室数据</div>
      </template>
    </template>
  </div>
</template>

<style scoped>
.spare-page {
  background-color: #fff;
  min-height: 100vh;
  padding-bottom: 24px;
}

.top-nav-bar {
  display: flex;
  align-items: center;
  min-height: 44px;
  padding: 10px 15px;
  background-color: #fff;
  box-sizing: border-box;
}

.nav-btn-back {
  font-size: 16px;
  color: #888;
  cursor: pointer;
}

.page-title-green {
  text-align: center;
  font-size: 22px;
  color: #3cc51f;
  font-weight: 400;
  margin: 0 0 4px 0;
}

.page-subtitle {
  text-align: center;
  font-size: 13px;
  color: #888;
  margin: 0 0 16px 0;
}

.search-form-wrap {
  padding: 0 16px;
  margin-top: 8px;
}

.spare-page .search-form-wrap .weui-cells_form {
  border-top: 1px solid #e5e5e5;
  border-bottom: 1px solid #e5e5e5;
}

.spare-page .search-form-wrap .weui-cell {
  padding: 12px 16px;
}

.spare-page .weui-btn_area {
  margin-top: 30px;
  padding: 12px 16px;
}

.result-list-wrap {
  padding: 0 16px 12px;
}

.spare-page .weui-media-box__title {
  margin-bottom: 8px;
  line-height: 1.4;
  white-space: normal;
}

.spare-page .weui-media-box__desc {
  margin: 4px 0;
  line-height: 1.5;
  font-size: 13px;
  color: #666;
}

.spare-empty {
  text-align: center;
  padding: 40px 15px;
  color: #888;
  font-size: 14px;
}

.weui-loadmore {
  padding: 20px;
  text-align: center;
}

/* 1. 锁死所有行的物理尺寸，确保高度、内边距绝对一致 */
.spare-form-row {
  height: 56px !important;
  padding: 0 16px !important;
  display: flex !important;
  align-items: center !important;
  background-color: #fff;
  box-sizing: border-box !important;
  position: relative;
}
/* 2. 锁死左侧标签宽度，解决左边对不齐的顽疾 */
.spare-form-row :deep(.weui-label) {
  width: 105px !important;
  min-width: 105px !important;
  margin: 0 !important;
  padding: 0 !important;
  text-align: left !important;
  color: #333;
}
/* 3. 右侧内容区（Select 和 Input）统一对齐 */
.spare-form-row :deep(.weui-cell__bd) {
  flex: 1 !important;
  text-align: right !important;
}
.spare-form-row :deep(.weui-select),
.spare-form-row :deep(.weui-input) {
  width: 100% !important;
  height: 56px !important;
  line-height: 56px !important;
  text-align: right !important;
  padding-right: 30px !important;
  color: #333 !important;
  border: none !important;
  background: transparent !important;
  appearance: none;
  direction: rtl;
}
/* 4. 手动绘制箭头：只给带 spare-row-has-arrow 的行加箭头 */
.spare-row-has-arrow::after {
  content: " ";
  display: inline-block;
  height: 8px;
  width: 8px;
  border-width: 2px 2px 0 0;
  border-color: #c8c8cd;
  border-style: solid;
  transform: matrix(0.71, 0.71, -0.71, 0.71, 0, 0);
  position: absolute;
  top: 50%;
  margin-top: -4px;
  right: 18px;
}
/* 5. 占位符颜色 */
.spare-form-row :deep(.weui-input::-webkit-input-placeholder),
.spare-form-row :deep(.weui-input::placeholder) {
  color: #b2b2b2 !important;
}
</style>
