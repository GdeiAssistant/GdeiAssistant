<template>
  <div class="weui-tab news-weui-tab">
    <div class="page-header">
      <a href="javascript:;" class="news-back" @click="goBack">返回</a>
      <h1 class="news-title">新闻通知</h1>
    </div>
    <div
      class="weui-tab__panel"
      @scroll="handleScroll"
    >
      <div class="weui-cells weui-cells_after-title" style="margin-top: 0;">
        <a
          v-for="item in newsList"
          :key="item.id"
          class="weui-cell weui-cell_access"
          href="javascript:;"
        >
          <div class="weui-cell__bd">{{ item.title }}</div>
          <div class="weui-cell__ft news-date">{{ item.date }}</div>
        </a>
      </div>
      <div v-if="isLoading" class="weui-loadmore">
        <i class="weui-loading"></i>
        <span class="weui-loadmore__tips">正在加载</span>
      </div>
      <div v-else-if="finished && newsList.length > 0" class="weui-loadmore weui-loadmore_line">
        <span class="weui-loadmore__tips">没有更多数据了</span>
      </div>
    </div>
    <div class="weui-tabbar news-tabbar">
      <a
        v-for="tab in tabs"
        :key="tab.type"
        href="javascript:;"
        class="weui-tabbar__item"
        :class="{ 'weui-bar__item_on': activeType === tab.type }"
        @click="switchTab(tab.type)"
      >
        <img
          :src="tab.icon"
          alt=""
          class="weui-tabbar__icon"
        />
        <p class="weui-tabbar__label">{{ tab.label }}</p>
      </a>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '../../utils/request'

const router = useRouter()

const tabs = [
  { type: 1, label: '教学信息', icon: '/img/news/course.png' },
  { type: 2, label: '考试信息', icon: '/img/news/examine.png' },
  { type: 3, label: '教务信息', icon: '/img/news/study.png' },
  { type: 4, label: '行政通知', icon: '/img/news/admin.png' },
  { type: 5, label: '综合信息', icon: '/img/news/school.png' }
]

const activeType = ref(1)
const newsList = ref([])
const page = ref(1)
const isLoading = ref(false)
const finished = ref(false)

function goBack() {
  router.back()
}

function loadNews() {
  if (isLoading.value || finished.value) return
  isLoading.value = true
  request
    .get(`/news/list?type=${activeType.value}&page=${page.value}`)
    .then((res) => {
      const list = res.data?.list || []
      newsList.value.push(...list)
      if (res.data?.hasMore === false) {
        finished.value = true
      }
    })
    .finally(() => {
      isLoading.value = false
    })
}

function switchTab(type) {
  if (type === activeType.value) return
  activeType.value = type
  newsList.value = []
  page.value = 1
  finished.value = false
  loadNews()
}

function handleScroll(e) {
  const { scrollTop, clientHeight, scrollHeight } = e.target

  // 1. 消除小数误差 2. 阈值放宽到 100 防止滑太快漏掉
  const distanceToBottom = scrollHeight - Math.ceil(scrollTop) - clientHeight
  const isBottom = distanceToBottom <= 100

  // 滚动日志（调试用，F12 控制台可见）
  // console.log(`滚动检测: 距离底部 ${distanceToBottom}px, 触底: ${isBottom}, loading: ${isLoading.value}, finish: ${finished.value}`)

  if (isBottom && !isLoading.value && !finished.value) {
    page.value++
    loadNews()
  }
}

onMounted(() => {
  loadNews()
})
</script>

<style scoped>
.weui-tab.news-weui-tab {
  height: 100vh !important;
  width: 100vw;
  overflow: hidden !important; /* 关键：锁死外层 */
  display: flex;
  flex-direction: column;
  background-color: #f8f8f8; /* WEUI 默认底色 */
}
.page-header {
  flex-shrink: 0;
  padding: 12px 16px 16px;
}
.news-back {
  color: #999;
  font-size: 15px;
  text-decoration: none;
}
.news-title {
  margin: 16px 0 0;
  font-size: 22px;
  font-weight: 500;
  text-align: center;
  color: #09bb07;
}
.weui-tab__panel {
  flex: 1;
  overflow-y: auto !important; /* 关键：强制内部滚动 */
  -webkit-overflow-scrolling: touch;
  box-sizing: border-box;
}
.weui-cells {
  margin-top: 0;
}
.weui-cell__bd {
  white-space: normal;
  word-break: break-word;
}
.news-date {
  flex-shrink: 0;
  margin-left: 8px;
  font-size: 13px;
  color: #999;
}
.weui-loadmore {
  padding: 16px 0;
  text-align: center;
}
.weui-loadmore_line .weui-loadmore__tips {
  background-color: #fff;
}
.news-tabbar {
  flex-shrink: 0;
  background: #f7f7fa;
  border-top: 1px solid #e5e5e5;
}
.news-tabbar .weui-bar__item_on .weui-tabbar__label {
  color: #09bb07;
}
/* 原版无 _active 图时，用滤镜高亮为绿色 #09bb07 */
.news-tabbar .weui-bar__item_on .weui-tabbar__icon {
  filter: brightness(0) saturate(100%) invert(48%) sepia(79%) saturate(2476%) hue-rotate(86deg);
}
.news-tabbar .weui-tabbar__icon {
  width: 27px;
  height: 27px;
  display: block;
  margin: 0 auto 2px;
}
</style>
