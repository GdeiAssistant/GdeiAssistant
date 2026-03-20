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

function showDialog(msg) {
  dialogMessage.value = msg
  dialogVisible.value = true
}

const keyword = computed(() => route.query.keyword ?? '')
const PAGE_SIZE = 10

function mapErshouItemToCard(item) {
  return {
    id: item.id,
    title: item.name,
    desc: item.description,
    price: item.price,
    coverImg: Array.isArray(item.pictureURL) && item.pictureURL.length > 0 ? item.pictureURL[0] : ''
  }
}

const fetchSearchData = async (page) => {
  const k = (route.query.keyword ?? '').trim()
  if (k === '') {
    return { list: [], hasMore: false }
  }
  const start = (page - 1) * PAGE_SIZE
  const res = await request.get(`/ershou/keyword/${encodeURIComponent(k)}/start/${start}`)
  const rawList = res?.data || []
  const list = Array.isArray(rawList) ? rawList.map(mapErshouItemToCard) : []
  return {
    list,
    hasMore: list.length >= PAGE_SIZE
  }
}

const { items: list, loading, finished, refreshing, pullY, loadData, handleScroll, handleTouchStart, handleTouchMove, handleTouchEnd } = useScrollLoad(fetchSearchData)

function doSearch() {
  if (!keywordInput.value || keywordInput.value.trim() === '') {
    showDialog('请输入搜索关键词')
    return
  }
  const k = keywordInput.value.trim()
  router.push({ path: '/ershou/search', query: { keyword: k } })
}

function goDetail(id) {
  router.push(`/ershou/detail/${id}`)
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
  <div class="ershou-search-page">
    <CommunityHeader title="搜索结果" moduleColor="#10b981" :showBack="true" @back="router.back()" backTo="" />

    <div class="ershou-search-bar">
      <div class="search-input-wrap">
        <i class="search-icon"></i>
        <input type="text" placeholder="搜搜看" v-model="keywordInput" @keyup.enter="doSearch" />
      </div>
      <span class="search-btn" @click="doSearch">搜索</span>
    </div>

    <!-- 滚动容器 -->
    <div
      v-if="keyword"
      class="ershou-scroll-container"
      ref="scrollContainer"
      @scroll="handleScroll"
      @touchstart="handleTouchStart"
      @touchmove="handleTouchMove($event, scrollContainer)"
      @touchend="handleTouchEnd"
    >
      <!-- 下拉刷新指示器 -->
      <div class="community-pull-refresh" :style="{ height: pullY + 'px' }">
        <span v-if="refreshing" class="community-pull-refresh__text">
          <i class="community-loading-spinner"></i> 正在刷新...
        </span>
        <span v-else-if="pullY > 50" class="community-pull-refresh__text">释放立即刷新</span>
        <span v-else-if="pullY > 0" class="community-pull-refresh__text">下拉刷新</span>
      </div>

      <!-- 商品双列网格 -->
      <div v-if="list.length > 0" class="ershou-goods-grid">
        <div
          v-for="item in list"
          :key="item.id"
          class="ershou-goods-card community-card"
          @click="goDetail(item.id)"
        >
          <div class="ershou-goods-card__img-wrap">
            <img :src="item.coverImg" :alt="item.title" class="ershou-goods-card__img" />
          </div>
          <h3 class="ershou-goods-card__title">{{ item.title }}</h3>
          <p class="ershou-goods-card__desc">{{ item.desc }}</p>
          <em class="ershou-goods-card__price">￥{{ item.price }}</em>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-if="!loading && !refreshing && list.length === 0" class="community-empty">
        <div class="community-empty__icon">?</div>
        <p class="community-empty__text">未搜索到相关商品</p>
      </div>

      <!-- 上拉加载更多 -->
      <div v-if="loading && !refreshing" class="community-loadmore">
        <i class="community-loading-spinner"></i>
        <span>正在加载</span>
      </div>
      <div v-if="finished && list.length > 0" class="community-loadmore">
        <span>没有更多了</span>
      </div>
    </div>

    <!-- 未输入关键词提示 -->
    <div v-if="!keyword" class="community-empty">
      <p class="community-empty__text">请输入关键词搜索</p>
    </div>

    <!-- Dialog -->
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
.ershou-search-page {
  min-height: 100vh;
  background: var(--c-bg);
  padding-bottom: 20px;
}

.ershou-search-bar {
  display: flex;
  align-items: center;
  padding: 10px 15px;
  background-color: var(--c-bg);
}
.search-input-wrap {
  flex: 1;
  display: flex;
  align-items: center;
  background-color: var(--c-card);
  border-radius: var(--radius-full);
  padding: 5px 12px;
  box-shadow: var(--shadow-sm);
}
.search-icon {
  display: inline-block;
  width: 16px;
  height: 16px;
  font-size: 16px;
  color: var(--c-text-3);
  margin-right: 5px;
  flex-shrink: 0;
  background: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='%239ca3af'%3E%3Cpath d='M15.5 14h-.79l-.28-.27C15.41 12.59 16 11.11 16 9.5 16 5.91 13.09 3 9.5 3S3 5.91 3 9.5 5.91 16 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z'/%3E%3C/svg%3E") center/contain no-repeat;
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
  color: var(--c-ershou);
  font-size: var(--font-md);
  margin-left: 15px;
  white-space: nowrap;
  cursor: pointer;
  font-weight: 500;
}

/* 滚动容器 */
.ershou-scroll-container {
  height: calc(100vh - 100px);
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
  overscroll-behavior-y: contain;
}

.ershou-goods-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  padding: 10px;
}
.ershou-goods-card {
  overflow: hidden;
  cursor: pointer;
}
.ershou-goods-card__img-wrap {
  width: 100%;
  aspect-ratio: 1;
  overflow: hidden;
  background: var(--c-border);
}
.ershou-goods-card__img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}
.ershou-goods-card__title {
  font-size: var(--font-base);
  font-weight: 500;
  color: var(--c-text-1);
  margin: 8px 8px 0;
  padding: 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.35;
}
.ershou-goods-card__desc {
  font-size: var(--font-sm);
  color: var(--c-text-3);
  margin: 4px 8px 0;
  padding: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.ershou-goods-card__price {
  display: block;
  font-size: var(--font-lg);
  font-weight: 600;
  color: #e4393c;
  margin: 6px 8px 8px;
  padding: 0;
  font-style: normal;
}
</style>
