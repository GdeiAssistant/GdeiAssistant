<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '../../utils/request'
import { useScrollLoad } from '../../composables/useScrollLoad'
import CommunityHeader from '../../components/community/CommunityHeader.vue'

const route = useRoute()
const router = useRouter()
const scrollContainer = ref(null)

const TYPE_NAMES = ['校园代步', '手机', '电脑', '数码配件', '数码', '电器', '运动健身', '衣物伞帽', '图书教材', '租赁', '生活娱乐', '其他']
const PAGE_SIZE = 10

const typeId = computed(() => {
  const t = route.query.type
  const num = parseInt(t, 10)
  return isNaN(num) || num < 0 || num > 11 ? null : num
})

const typeName = computed(() => {
  if (typeId.value === null) return '分类'
  return TYPE_NAMES[typeId.value] ?? '分类'
})

function mapErshouItemToCard(item) {
  return {
    id: item.id,
    title: item.name,
    desc: item.description,
    price: item.price,
    coverImg: Array.isArray(item.pictureURL) && item.pictureURL.length > 0 ? item.pictureURL[0] : ''
  }
}

const fetchTypeData = async (page) => {
  if (typeId.value === null) {
    return { list: [], hasMore: false }
  }
  const start = (page - 1) * PAGE_SIZE
  const res = await request.get(`/marketplace/item/type/${typeId.value}/start/${start}`)
  const rawList = res?.data || []
  const list = Array.isArray(rawList) ? rawList.map(mapErshouItemToCard) : []
  return {
    list,
    hasMore: list.length >= PAGE_SIZE
  }
}

const { items: list, loading, finished, refreshing, pullY, loadData, handleScroll, handleTouchStart, handleTouchMove, handleTouchEnd } = useScrollLoad(fetchTypeData)

function goDetail(id) {
  router.push(`/marketplace/detail/${id}`)
}

onMounted(() => {
  loadData(true)
})

watch(
  () => route.query.type,
  () => {
    loadData(true)
  }
)
</script>

<template>
  <div class="ershou-type-page">
    <CommunityHeader :title="typeName" moduleColor="#10b981" :showBack="true" @back="router.back()" backTo="" />

    <!-- 滚动容器 -->
    <div
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
        <p class="community-empty__text">暂无该分类的商品</p>
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
  </div>
</template>

<style scoped>
.ershou-type-page {
  min-height: 100vh;
  background: var(--c-bg);
  padding-bottom: 20px;
}

/* 滚动容器 */
.ershou-scroll-container {
  height: calc(100vh - 51px);
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
