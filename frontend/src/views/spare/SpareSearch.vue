<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

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

const showTopTips = ref(false)
const errorMsg = ref('')
let topTipsTimer = null

const showWeuiTopTips = (msg) => {
  errorMsg.value = msg
  showTopTips.value = true
  if (topTipsTimer) clearTimeout(topTipsTimer)
  topTipsTimer = setTimeout(() => {
    showTopTips.value = false
  }, 2000)
}

function goBack() {
  router.back()
}

function doSearch() {
  const start = startDate.value
  const end = endDate.value
  if (end && start && end < start) {
    showWeuiTopTips('结束时间不能早于起始时间！')
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
  <div class="weui-toptips weui-toptips_warn" v-show="showTopTips">{{ errorMsg }}</div>
  <div class="spare-search-page">
    <div class="top-nav-bar">
      <div class="nav-btn-back" @click="goBack">返回</div>
    </div>
    <h1 class="page-title-green">空课室查询</h1>

    <div class="weui-cells__title">查询条件</div>
    <div class="weui-cells weui-cells_form">
      <div class="weui-cell weui-cell_select weui-cell_select-after">
        <div class="weui-cell__hd"><label class="weui-label">校区</label></div>
        <div class="weui-cell__bd weui-cell_primary">
          <select class="weui-select spare-select" v-model="zone">
            <option v-for="opt in campusOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
          </select>
        </div>
      </div>
      <div class="weui-cell weui-cell_select weui-cell_select-after">
        <div class="weui-cell__hd"><label class="weui-label">教室类别</label></div>
        <div class="weui-cell__bd weui-cell_primary">
          <select class="weui-select spare-select" v-model="type">
            <option v-for="opt in roomTypeOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
          </select>
        </div>
      </div>
      <div class="weui-cell">
        <div class="weui-cell__hd"><label class="weui-label">座位数</label></div>
        <div class="weui-cell__bd weui-cell_primary">
          <input class="weui-input" type="number" v-model="seatsMin" placeholder="大于等于，选填" />
        </div>
      </div>
      <div class="weui-cell">
        <div class="weui-cell__hd"><label class="weui-label">座位数</label></div>
        <div class="weui-cell__bd weui-cell_primary">
          <input class="weui-input" type="number" v-model="seatsMax" placeholder="小于等于，选填" />
        </div>
      </div>
      <a href="javascript:" class="weui-cell weui-cell_access" @click.prevent="openStartDatePicker">
        <div class="weui-cell__hd"><label class="weui-label">起始时间</label></div>
        <div class="weui-cell__bd weui-cell_primary"></div>
        <div class="weui-cell__ft">{{ startDate }}</div>
        <input ref="inputStartDate" v-model="startDate" type="date" class="spare-input-date" />
      </a>
      <a href="javascript:" class="weui-cell weui-cell_access" @click.prevent="openEndDatePicker">
        <div class="weui-cell__hd"><label class="weui-label">结束时间</label></div>
        <div class="weui-cell__bd weui-cell_primary"></div>
        <div class="weui-cell__ft">{{ endDate }}</div>
        <input ref="inputEndDate" v-model="endDate" type="date" class="spare-input-date" />
      </a>
      <div class="weui-cell weui-cell_select weui-cell_select-after">
        <div class="weui-cell__hd"><label class="weui-label">星期</label></div>
        <div class="weui-cell__bd weui-cell_primary">
          <select class="weui-select spare-select" v-model="minWeek">
            <option v-for="opt in weekDayOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
          </select>
        </div>
      </div>
      <div class="weui-cell weui-cell_select weui-cell_select-after">
        <div class="weui-cell__hd"><label class="weui-label">单双周</label></div>
        <div class="weui-cell__bd weui-cell_primary">
          <select class="weui-select spare-select" v-model="weekType">
            <option v-for="opt in singleDoubleOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
          </select>
        </div>
      </div>
      <div class="weui-cell weui-cell_select weui-cell_select-after">
        <div class="weui-cell__hd"><label class="weui-label">节数</label></div>
        <div class="weui-cell__bd weui-cell_primary">
          <select class="weui-select spare-select" v-model="classNumber">
            <option v-for="opt in periodOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
          </select>
        </div>
      </div>
    </div>

    <div class="weui-btn_area">
      <button type="button" class="weui-btn weui-btn_primary" @click="doSearch">查询</button>
    </div>
  </div>
</template>

<style scoped>
.spare-search-page {
  background-color: #fff;
  min-height: 100vh;
  padding-bottom: 24px;
}

.top-nav-bar {
  display: flex;
  justify-content: flex-start;
  align-items: center;
  min-height: 44px;
  padding: 10px 15px;
  background-color: #fff;
  box-sizing: border-box;
}

.nav-btn-back {
  font-size: 16px;
  line-height: 24px;
  color: #888;
  cursor: pointer;
}

.page-title-green {
  text-align: center;
  font-size: 34px;
  color: #09bb07;
  font-weight: 400;
  margin: 10px 0 20px 0;
  line-height: 1.2;
}

.spare-search-page .weui-cells__title {
  padding: 12px 15px 8px;
  font-size: 14px;
  color: #888;
}

.spare-search-page .weui-cells_form {
  margin-top: 0;
}

.spare-search-page .weui-cell__hd .weui-label {
  width: 5em;
  min-width: 80px;
}

.spare-search-page .weui-cell__ft {
  color: #333;
}

.spare-select {
  color: #333;
  text-align: right;
  direction: rtl;
}

.spare-input-date {
  position: absolute;
  opacity: 0;
  width: 0;
  height: 0;
  pointer-events: none;
}

.spare-search-page .weui-btn_area {
  margin-top: 24px;
  padding: 0 15px;
}

.spare-search-page .weui-btn_area .weui-btn {
  width: 100%;
}

.weui-toptips {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  padding: 10px;
  font-size: 14px;
  text-align: center;
  color: #FFF !important;
  background-color: #E64340 !important;
  z-index: 99999 !important;
  opacity: 1 !important;
  visibility: visible !important;
  word-wrap: break-word;
  word-break: break-all;
}
</style>
