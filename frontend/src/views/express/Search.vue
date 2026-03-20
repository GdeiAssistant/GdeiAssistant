<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '../../utils/request'
import { useScrollLoad } from '../../composables/useScrollLoad'
import CommunityHeader from '../../components/community/CommunityHeader.vue'

const route = useRoute()
const router = useRouter()
const keywordInput = ref('')
const dialogVisible = ref(false)
const dialogMessage = ref('')
const scrollContainer = ref(null)
const PAGE_SIZE = 10

function showDialog(msg) {
  dialogMessage.value = msg
  dialogVisible.value = true
}

const keyword = computed(() => route.query.keyword ?? '')

const fetchSearchData = async (page) => {
  const k = (route.query.keyword ?? '').trim()
  if (k === '') {
    return { list: [], hasMore: false }
  }
  const start = (page - 1) * PAGE_SIZE
  const res = await request.get(`/express/keyword/${encodeURIComponent(k)}/start/${start}/size/${PAGE_SIZE}`)
  const rawList = res?.data || []
  const list = Array.isArray(rawList) ? rawList.map((item) => ({
    id: item.id,
    content: item.content,
    senderName: item.nickname,
    receiverName: item.name,
    senderGender: item.selfGender === 0 ? 'male' : item.selfGender === 1 ? 'female' : 'secret',
    receiverGender: item.personGender === 0 ? 'male' : item.personGender === 1 ? 'female' : 'secret',
    time: item.publishTime,
    likeCount: item.likeCount ?? 0,
    commentCount: item.commentCount ?? 0,
    isLiked: item.liked === true,
    canGuess: item.canGuess === true,
    guessCount: item.guessSum ?? 0
  })) : []
  return { list, hasMore: list.length >= PAGE_SIZE }
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
  if (item.isLiked) return
  request.post(`/express/id/${item.id}/like`).then(() => {
    item.isLiked = true
    item.likeCount = (item.likeCount || 0) + 1
  })
}

function handleGuess(item) {
  router.push(`/express/detail/${item.id}`)
}

function handleComment(item) {
  router.push(`/express/detail/${item.id}`)
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
  <div class="express-search-page">
    <CommunityHeader title="搜索表白" moduleColor="#f43f5e" backTo="/express/home" />

    <!-- 搜索栏 -->
    <div class="express-search-bar">
      <div class="search-input-wrap">
        <svg class="search-icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="#9ca3af" width="16" height="16">
          <path d="M15.5 14h-.79l-.28-.27C15.41 12.59 16 11.11 16 9.5 16 5.91 13.09 3 9.5 3S3 5.91 3 9.5 5.91 16 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z"/>
        </svg>
        <input type="text" placeholder="搜索发件人/收件人/内容" v-model="keywordInput" @keyup.enter="doSearch" />
      </div>
      <span class="search-btn" @click="doSearch">搜索</span>
    </div>

    <!-- 浅粉色标题 -->
    <h2 class="express-main-title">广东第二师范学院表白墙</h2>

    <!-- 滚动容器 -->
    <div
      class="express-scroll-container"
      ref="scrollContainer"
      @scroll="handleScroll"
      @touchstart="handleTouchStart"
      @touchmove="handleTouchMove($event, scrollContainer)"
      @touchend="handleTouchEnd"
    >
      <div class="community-pull-refresh" :style="{ height: pullY + 'px' }">
        <span v-if="refreshing" class="community-pull-refresh__text">
          <i class="community-loading-spinner"></i> 正在刷新...
        </span>
        <span v-else-if="pullY > 50" class="community-pull-refresh__text">释放立即刷新</span>
        <span v-else-if="pullY > 0" class="community-pull-refresh__text">下拉刷新</span>
      </div>

      <div class="express-list">
        <div
          v-for="(item, index) in list"
          :key="item.id"
          class="community-card express-card"
          :style="{ animationDelay: (index % 10) * 0.05 + 's' }"
        >
          <div class="express-card__content">
            <p class="express-card__names">
              <span :class="getGenderClass(item.senderGender)">{{ item.senderName }}</span>
              <span class="express-card__connector">≡❤</span>
              <span :class="getGenderClass(item.receiverGender)">{{ item.receiverName }}</span>
            </p>
            <p class="express-card__text">{{ item.content }}</p>
            <p class="express-card__time">{{ item.time }}</p>
          </div>
          <div class="express-card__actions">
            <button type="button" class="express-action" @click="handleLike(item)">
              <span class="express-action__icon">♡</span>
              <span class="express-action__text">{{ item.likeCount || 0 }}</span>
            </button>
            <button type="button" class="express-action" @click="handleGuess(item)">
              <span class="express-action__icon">☆</span>
              <span class="express-action__text">{{ item.guessCount || 0 }}</span>
            </button>
            <button type="button" class="express-action" @click="handleComment(item)">
              <span class="express-action__icon">💬</span>
              <span class="express-action__text">{{ item.commentCount || 0 }}</span>
            </button>
          </div>
        </div>
      </div>

      <div class="express-legend">
        蓝色下划线：男生 / 红色下划线：女生 / 黑色下划线：其他或保密
      </div>

      <div v-if="!loading && !refreshing && list.length === 0" class="community-empty">
        <div class="community-empty__icon">🔍</div>
        <p class="community-empty__text">{{ keyword ? '未找到相关表白' : '输入关键词搜索' }}</p>
      </div>

      <div v-if="loading && !refreshing" class="community-loadmore">
        <i class="community-loading-spinner"></i>
        <span>正在加载</span>
      </div>
      <div v-if="finished && list.length > 0" class="community-loadmore">
        <span>没有更多了</span>
      </div>
    </div>

    <!-- 提示 Dialog -->
    <div v-if="dialogVisible">
      <div class="community-dialog-mask" @click="dialogVisible = false"></div>
      <div class="community-dialog">
        <div class="community-dialog__title">提示</div>
        <div class="community-dialog__body">{{ dialogMessage }}</div>
        <div class="community-dialog__footer">
          <button class="community-dialog__btn community-dialog__btn--confirm" @click="dialogVisible = false">确定</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.express-search-page {
  min-height: 100vh;
  background: var(--c-bg);
}

.express-search-bar {
  display: flex;
  align-items: center;
  padding: 10px var(--space-lg);
  background-color: var(--c-bg);
}
.search-input-wrap {
  flex: 1;
  display: flex;
  align-items: center;
  background-color: var(--c-card);
  border-radius: var(--radius-full);
  padding: 8px 12px;
  box-shadow: var(--shadow-sm);
}
.search-icon {
  flex-shrink: 0;
  margin-right: 8px;
}
.search-input-wrap input {
  flex: 1;
  border: none;
  outline: none;
  font-size: var(--font-base);
  background: transparent;
  min-width: 0;
  color: var(--c-text-1);
}
.search-btn {
  color: #f43f5e;
  font-size: var(--font-md);
  margin-left: var(--space-lg);
  white-space: nowrap;
  cursor: pointer;
  font-weight: 500;
}

.express-main-title {
  text-align: center;
  font-size: 20px;
  font-weight: bold;
  color: #ffb3ba;
  margin: var(--space-md) var(--space-lg) var(--space-lg);
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
  border-bottom: 2px dashed var(--c-text-1);
  color: var(--c-text-1);
}

.express-scroll-container {
  height: calc(100vh - 180px);
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
  overscroll-behavior-y: contain;
}

.express-list {
  padding: 0 var(--space-lg) var(--space-xl);
}

.express-card {
  margin-bottom: var(--space-md);
  overflow: hidden;
  animation: community-slide-up 0.4s ease both;
}
.express-card__content {
  padding: var(--space-lg) var(--space-lg) var(--space-md);
}
.express-card__names {
  margin: 0 0 10px;
  font-size: var(--font-lg);
  line-height: 1.5;
}
.express-card__names span {
  display: inline;
}
.express-card__connector {
  margin: 0 6px;
  color: #f43f5e;
  font-weight: bold;
  border: none !important;
}
.express-card__text {
  margin: 0 0 var(--space-sm);
  font-size: var(--font-md);
  color: var(--c-text-1);
  line-height: 1.6;
  word-break: break-word;
}
.express-card__time {
  margin: 0;
  font-size: var(--font-sm);
  color: var(--c-text-3);
}
.express-card__actions {
  display: flex;
  border-top: 1px solid var(--c-border);
}
.express-action {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-xs);
  padding: 10px 0;
  background: transparent;
  border: none;
  border-right: 1px solid var(--c-border);
  font-size: var(--font-base);
  color: var(--c-text-2);
  cursor: pointer;
}
.express-action:last-child {
  border-right: none;
}
.express-action__icon {
  font-size: var(--font-lg);
}

.express-legend {
  text-align: center;
  font-size: var(--font-sm);
  color: var(--c-text-3);
  padding: var(--space-lg) var(--space-lg) var(--space-xl);
  line-height: 1.5;
}
</style>
