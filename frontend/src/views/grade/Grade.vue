<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '../../utils/request'

const router = useRouter()

const activeYear = ref(0)
const loading = ref(false)
const gradeResult = ref(null)
const showActionSheet = ref(false)
const toastMessage = ref('')
const showToast = ref(false)
let toastTimer = null

const yearTabs = [
  { value: 0, label: '大一' },
  { value: 1, label: '大二' },
  { value: 2, label: '大三' },
  { value: 3, label: '大四' }
]

async function fetchGrade() {
  loading.value = true
  gradeResult.value = null
  try {
    const res = await request.post('/gradequery', { year: activeYear.value })
    if (res && res.data) {
      gradeResult.value = res.data
    }
  } finally {
    loading.value = false
  }
}

function switchYear(year) {
  if (activeYear.value === year) return
  activeYear.value = year
  fetchGrade()
}

function goBack() {
  router.back()
}

/** 显示更多菜单（与旧版 grade.js showOptionMenu 一致：管理缓存配置、更新实时数据 + 取消） */
function showOptionMenu() {
  showActionSheet.value = true
}

function closeActionSheet() {
  showActionSheet.value = false
}

/** 管理缓存配置：旧版跳转 /privacy，Mock 阶段用 Toast 提示 */
function onManageCache() {
  closeActionSheet()
  showWeuiToast('该功能将在后续模块迁移中实现')
}

/** 更新实时数据：旧版 refreshGradeData，这里触发 fetchGrade 并显示加载动画 */
function onRefreshGrade() {
  closeActionSheet()
  fetchGrade()
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

onMounted(() => {
  fetchGrade()
})
</script>

<template>
  <div class="grade-page">
    <!-- WEUI Loading -->
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
    <h1 class="page-title-green">我的成绩单</h1>

    <!-- WEUI ActionSheet：与旧版 showOptionMenu 一致，含取消按钮 -->
    <template v-if="showActionSheet">
      <div class="weui-mask" @click="closeActionSheet" aria-hidden="true"></div>
      <div class="weui-actionsheet weui-actionsheet_toggle" role="dialog" aria-label="更多选项">
        <div class="weui-actionsheet__menu">
          <div class="weui-actionsheet__cell" @click="onManageCache">管理缓存配置</div>
          <div class="weui-actionsheet__cell" @click="onRefreshGrade">更新实时数据</div>
        </div>
        <div class="weui-actionsheet__action">
          <div class="weui-actionsheet__cell" @click="closeActionSheet">取消</div>
        </div>
      </div>
    </template>

    <!-- Toast 提示（后续模块迁移等） -->
    <template v-if="showToast">
      <div class="weui-mask_transparent" aria-hidden="true"></div>
      <div class="weui-toast__wrp">
        <div class="weui-toast weui-toast_text">
          <p class="weui-toast__content">{{ toastMessage }}</p>
        </div>
      </div>
    </template>

    <!-- 第一部分：顶部 Tab 切换（大一到大四） -->
    <div class="grade-navbar">
      <div
        v-for="tab in yearTabs"
        :key="tab.value"
        role="tab"
        class="grade-navbar__item"
        :class="{ 'grade-navbar__item_on': activeYear === tab.value }"
        @click="switchYear(tab.value)"
      >
        {{ tab.label }}
      </div>
    </div>

    <!-- 第二部分 + 第三部分：学期与绩点摘要 + 成绩表格 -->
    <template v-if="gradeResult">
      <!-- 第一学期 -->
      <div class="grades">
        <div class="term">第一学期</div>
        <p class="page_desc">平均学分绩点：{{ gradeResult.firstTermGPA != null ? gradeResult.firstTermGPA : '—' }}</p>
        <div class="table">
          <table>
            <thead>
              <tr>
                <th class="col-course">课程</th>
                <th class="col-credit">学分</th>
                <th class="col-score">成绩</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(item, index) in (gradeResult.firstTermGradeList || [])" :key="'1-' + index">
                <td class="col-course">{{ item.gradeName }}</td>
                <td class="col-credit">{{ item.gradeCredit }}</td>
                <td class="col-score">{{ item.gradeScore }}</td>
              </tr>
            </tbody>
          </table>
        </div>

        <div class="term">第二学期</div>
        <p class="page_desc">平均学分绩点：{{ gradeResult.secondTermGPA != null ? gradeResult.secondTermGPA : '—' }}</p>
        <div class="table">
          <table>
            <thead>
              <tr>
                <th class="col-course">课程</th>
                <th class="col-credit">学分</th>
                <th class="col-score">成绩</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(item, index) in (gradeResult.secondTermGradeList || [])" :key="'2-' + index">
                <td class="col-course">{{ item.gradeName }}</td>
                <td class="col-credit">{{ item.gradeCredit }}</td>
                <td class="col-score">{{ item.gradeScore }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
.grade-page {
  background-color: #fff;
  min-height: 100vh;
  padding-bottom: 24px;
}

/* 导航栏：与 Schedule 完全一致，保证返回/更多/标题视觉高度统一 */
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

/* 顶部 Tab：Flex 布局，选中项绿色粗下划线 */
.grade-navbar {
  display: flex;
  position: relative;
  background-color: #fff;
  border-bottom: 1px solid #e5e5e5;
}

.grade-navbar__item {
  flex: 1;
  padding: 16px 0;
  text-align: center;
  font-size: 17px;
  color: #999;
  cursor: pointer;
  position: relative;
  -webkit-tap-highlight-color: transparent;
}

.grade-navbar__item_on {
  color: #000;
  font-weight: 600;
}

.grade-navbar__item_on::after {
  content: '';
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  height: 3px;
  background-color: #09bb07;
}

/* 学期与绩点摘要：居中，第一行绿色，第二行灰色 */
.grades {
  padding: 15px 16px 24px;
}

.term {
  padding-top: 15px;
  font-size: 16px;
  color: #09bb07;
  text-align: center;
  margin-bottom: 4px;
}

.grades .term:first-child {
  padding-top: 0;
}

.page_desc {
  margin: 0 0 8px;
  font-size: 14px;
  color: #999;
  text-align: center;
}

/* 表格：表头加粗，内容行分割线 */
.table {
  width: 100%;
  overflow-x: auto;
}

.table table {
  width: 100%;
  border-collapse: collapse;
}

.table thead th {
  font-weight: 600;
  color: #000;
  padding: 12px 8px;
  border-bottom: 1px solid #e5e5e5;
}

.table .col-course {
  width: 65%;
  text-align: left;
}

.table .col-credit {
  width: 60px;
  text-align: center;
}

.table .col-score {
  width: 60px;
  text-align: right;
}

.table tbody td {
  padding: 12px 8px;
  border-bottom: 1px solid #e5e5e5;
  font-size: 14px;
  color: #333;
}

.table tbody .col-course {
  text-align: left;
}

.table tbody .col-credit {
  text-align: center;
}

.table tbody .col-score {
  text-align: right;
}
</style>
