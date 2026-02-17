<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '../../utils/request'
import { useScrollLoad } from '../../composables/useScrollLoad'

const route = useRoute()
const router = useRouter()
const keywordInput = ref('')
const dialogVisible = ref(false)
const dialogMessage = ref('')
const scrollContainer = ref(null)

function showDialog(msg) {
  dialogMessage.value = msg
  dialogVisible.value = true
}

const keyword = computed(() => route.query.keyword ?? '')

const fetchSearchData = async (page) => {
  const k = (route.query.keyword ?? '').trim()
  if (k === '') {
    return { data: { list: [], hasMore: false } }
  }
  const res = await request.get('/express/items', {
    params: { page, limit: 10, keyword: k }
  })
  return res
}

const { items: list, loading, finished, refreshing, pullY, loadData, handleScroll, handleTouchStart, handleTouchMove, handleTouchEnd } = useScrollLoad(fetchSearchData)

function getGenderClass(gender) {
  if (gender === 'male') return 'gender-male'
  if (gender === 'female') return 'gender-female'
  return 'gender-secret'
}

function doSearch() {
  if (!keywordInput.value || keywordInput.value.trim() === '') {
    showDialog('请输入搜索关键词')
    return
  }
  const k = keywordInput.value.trim()
  router.push({ path: '/express/search', query: { keyword: k } })
}

function handleLike(item) {
  item.likeCount = (item.likeCount || 0) + 1
}

function handleGuess(item) {
  item.guessCount = (item.guessCount || 0) + 1
}

function handleComment(item) {
  // TODO: 跳转评论页
}

onMounted(() => {
  keywordInput.value = keyword.value
  if (keyword.value) {
    loadData(true)
  }
})

watch(
  keyword,
  (val) => {
    keywordInput.value = val
    if (val) {
      loadData(true)
    }
  },
  { immediate: false }
)
</script>

<template>
  <div class="dating-search-page">
    <div class="dating-header unified-header">
      <span class="dating-header__back" @click="router.push('/express/home')">返回</span>
      <h1 class="dating-header__title">搜索表白</h1>
      <span class="dating-header__placeholder"></span>
    </div>

    <!-- 搜索栏 -->
    <div class="dating-search-bar">
      <div class="search-input-wrap">
        <i class="weui-icon-search"></i>
        <input type="text" placeholder="搜索发件人/收件人/内容" v-model="keywordInput" @keyup.enter="doSearch" />
      </div>
      <span class="search-btn" @click="doSearch">搜索</span>
    </div>

    <!-- 巨大的浅粉色标题 -->
    <h2 class="dating-main-title">广东第二师范学院表白墙</h2>

    <!-- 滚动容器 -->
    <div
      class="dating-scroll-container"
      ref="scrollContainer"
      @scroll="handleScroll"
      @touchstart="handleTouchStart"
      @touchmove="handleTouchMove($event, scrollContainer)"
      @touchend="handleTouchEnd"
    >
      <div class="pull-refresh-indicator" :style="{ height: pullY + 'px' }">
        <span v-if="refreshing" class="pull-refresh-text">
          <i class="weui-loading"></i> 正在刷新...
        </span>
        <span v-else-if="pullY > 50" class="pull-refresh-text">释放立即刷新</span>
        <span v-else-if="pullY > 0" class="pull-refresh-text">下拉刷新</span>
      </div>

      <div class="dating-list">
        <div v-for="item in list" :key="item.id" class="dating-card">
          <div class="dating-card__content">
            <p class="dating-card__names">
              <span :class="getGenderClass(item.senderGender)">{{ item.senderName }}</span>
              <span class="dating-card__connector">≡❤</span>
              <span :class="getGenderClass(item.receiverGender)">{{ item.receiverName }}</span>
            </p>
            <p class="dating-card__text">{{ item.content }}</p>
            <p class="dating-card__time">{{ item.time }}</p>
          </div>
          <div class="dating-card__actions">
            <button type="button" class="dating-action" @click="handleLike(item)">
              <span class="dating-action__icon">♡</span>
              <span class="dating-action__text">{{ item.likeCount || 0 }}</span>
            </button>
            <button type="button" class="dating-action" @click="handleGuess(item)">
              <span class="dating-action__icon">☆</span>
              <span class="dating-action__text">{{ item.guessCount || 0 }}</span>
            </button>
            <button type="button" class="dating-action" @click="handleComment(item)">
              <span class="dating-action__icon">💬</span>
              <span class="dating-action__text">{{ item.commentCount || 0 }}</span>
            </button>
          </div>
        </div>
      </div>

      <div class="dating-legend">
        蓝色下划线：男生 / 红色下划线：女生 / 黑色下划线：其他或保密
      </div>

      <div v-if="!loading && !refreshing && list.length === 0" class="dating-empty">
        <p>{{ keyword ? '未找到相关表白' : '输入关键词搜索' }}</p>
      </div>

      <div v-if="loading && !refreshing" class="dating-loadmore">
        <i class="weui-loading"></i>
        <span class="weui-loadmore__tips">正在加载</span>
      </div>
      <div v-if="finished && list.length > 0" class="dating-loadmore">
        <span class="weui-loadmore__tips">没有更多了</span>
      </div>
    </div>

    <div v-if="dialogVisible">
      <div class="weui-mask" @click="dialogVisible = false"></div>
      <div class="weui-dialog">
        <div class="weui-dialog__hd"><strong class="weui-dialog__title">提示</strong></div>
        <div class="weui-dialog__bd">{{ dialogMessage }}</div>
        <div class="weui-dialog__ft">
          <a href="javascript:" class="weui-dialog__btn weui-dialog__btn_primary" @click="dialogVisible = false">确定</a>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.dating-search-page {
  min-height: 100vh;
  background: #f5f5f5;
}

.dating-header.unified-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 44px;
  padding: 0 12px;
  background-color: #f8f8f8;
  border-bottom: 1px solid #eee;
}
.dating-header__back {
  font-size: 14px;
  color: #333;
  cursor: pointer;
  min-width: 48px;
  text-align: left;
}
.dating-header__title {
  flex: 1;
  text-align: center;
  font-size: 16px;
  font-weight: 500;
  margin: 0;
  color: #333;
}
.dating-header__placeholder {
  min-width: 48px;
  text-align: right;
}

.dating-search-bar {
  display: flex;
  align-items: center;
  padding: 10px 15px;
  background-color: #f5f5f5;
}
.search-input-wrap {
  flex: 1;
  display: flex;
  align-items: center;
  background-color: #fff;
  border-radius: 20px;
  padding: 8px 12px;
}
.search-input-wrap .weui-icon-search {
  display: inline-block;
  width: 16px;
  height: 16px;
  margin-right: 8px;
  flex-shrink: 0;
  background: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='%23b2b2b2'%3E%3Cpath d='M15.5 14h-.79l-.28-.27C15.41 12.59 16 11.11 16 9.5 16 5.91 13.09 3 9.5 3S3 5.91 3 9.5 5.91 16 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z'/%3E%3C/svg%3E") center/contain no-repeat;
}
.search-input-wrap input {
  flex: 1;
  border: none;
  outline: none;
  font-size: 14px;
  background: transparent;
  min-width: 0;
}
.search-btn {
  color: #e53935;
  font-size: 15px;
  margin-left: 15px;
  white-space: nowrap;
  cursor: pointer;
}

.dating-main-title {
  text-align: center;
  font-size: 20px;
  font-weight: bold;
  color: #ffb3ba;
  margin: 12px 15px 16px;
  padding: 0;
  line-height: 1.3;
}

.gender-male {
  border-bottom: 2px dashed #4fc3f7;
  color: #4fc3f7;
}
.gender-female {
  border-bottom: 2px dashed #ff8a80;
  color: #ff8a80;
}
.gender-secret {
  border-bottom: 2px dashed #333333;
  color: #333333;
}

.dating-scroll-container {
  height: calc(100vh - 180px);
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
  overscroll-behavior-y: contain;
}

.pull-refresh-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  transition: height 0.3s;
}
.pull-refresh-text {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #999;
}
.pull-refresh-text .weui-loading {
  width: 16px;
  height: 16px;
  border: 2px solid #e5e5e5;
  border-top-color: #e53935;
  border-radius: 50%;
  animation: dating-spin 0.8s linear infinite;
}
@keyframes dating-spin {
  to { transform: rotate(360deg); }
}

.dating-list {
  padding: 0 15px 20px;
}
.dating-card {
  background: #fff;
  border-radius: 8px;
  margin-bottom: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}
.dating-card__content {
  padding: 16px 15px 12px;
}
.dating-card__names {
  margin: 0 0 10px;
  font-size: 16px;
  line-height: 1.5;
}
.dating-card__names span {
  display: inline;
}
.dating-card__connector {
  margin: 0 6px;
  color: #e53935;
  font-weight: bold;
  border: none !important;
}
.dating-card__text {
  margin: 0 0 8px;
  font-size: 15px;
  color: #333;
  line-height: 1.6;
  word-break: break-word;
}
.dating-card__time {
  margin: 0;
  font-size: 12px;
  color: #999;
}
.dating-card__actions {
  display: flex;
  border-top: 1px solid #eee;
}
.dating-action {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  padding: 10px 0;
  background: transparent;
  border: none;
  border-right: 1px solid #eee;
  font-size: 14px;
  color: #666;
  cursor: pointer;
}
.dating-action:last-child {
  border-right: none;
}
.dating-action__icon {
  font-size: 16px;
}

.dating-legend {
  text-align: center;
  font-size: 12px;
  color: #999;
  padding: 16px 15px 24px;
  line-height: 1.5;
}

.dating-empty {
  text-align: center;
  padding: 60px 20px;
  color: #999;
  font-size: 14px;
}

.dating-loadmore {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
  color: #999;
  font-size: 14px;
  gap: 8px;
}
.dating-loadmore .weui-loading {
  width: 16px;
  height: 16px;
  border: 2px solid #e5e5e5;
  border-top-color: #e53935;
  border-radius: 50%;
  animation: dating-spin 0.8s linear infinite;
}

.weui-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  z-index: 1000;
}
.weui-dialog {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 85%;
  max-width: 300px;
  background: #fff;
  border-radius: 8px;
  z-index: 1001;
  overflow: hidden;
}
.weui-dialog__hd {
  padding: 20px 20px 10px;
  text-align: center;
}
.weui-dialog__title {
  font-size: 17px;
  font-weight: 500;
  color: #333;
}
.weui-dialog__bd {
  padding: 10px 20px;
  text-align: center;
  font-size: 15px;
  color: #666;
}
.weui-dialog__ft {
  display: flex;
  border-top: 1px solid #d9d9d9;
}
.weui-dialog__btn {
  flex: 1;
  padding: 15px 0;
  text-align: center;
  font-size: 17px;
  color: #e53935;
  text-decoration: none;
}
.weui-dialog__btn_primary {
  font-weight: 500;
}
</style>
