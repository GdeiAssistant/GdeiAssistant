<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '../../utils/request'
import { useScrollLoad } from '../../composables/useScrollLoad'

const route = useRoute()
const router = useRouter()
const scrollContainer = ref(null)

const TYPE_NAMES = ['校园代步', '手机', '电脑', '数码配件', '数码', '电器', '运动健身', '衣物伞帽', '图书教材', '租赁', '生活娱乐', '其他']

const typeId = computed(() => {
  const t = route.query.type
  const num = parseInt(t, 10)
  return isNaN(num) || num < 0 || num > 11 ? null : num
})

const typeName = computed(() => {
  if (typeId.value === null) return '分类'
  return TYPE_NAMES[typeId.value] ?? '分类'
})

const fetchTypeData = async (page) => {
  if (typeId.value === null) {
    return { data: { list: [], hasMore: false } }
  }
  const res = await request.get('/ershou/items', {
    params: { page, limit: 10, type: typeId.value }
  })
  return res
}

const { items: list, loading, finished, refreshing, pullY, loadData, handleScroll, handleTouchStart, handleTouchMove, handleTouchEnd } = useScrollLoad(fetchTypeData)

function goDetail(id) {
  router.push(`/ershou/detail/${id}`)
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
    <div class="unified-header">
      <span class="unified-header__back" @click="router.back()">返回</span>
      <h1 class="unified-header__title">{{ typeName }}</h1>
      <span class="unified-header__placeholder"></span>
    </div>

    <!-- 滚动容器：下拉刷新 + 上拉加载 -->
    <div
      class="ershou-scroll-container"
      ref="scrollContainer"
      @scroll="handleScroll"
      @touchstart="handleTouchStart"
      @touchmove="handleTouchMove($event, scrollContainer)"
      @touchend="handleTouchEnd"
    >
      <!-- 下拉刷新指示器 -->
      <div class="pull-refresh-indicator" :style="{ height: pullY + 'px' }">
        <span v-if="refreshing" class="pull-refresh-text">
          <i class="weui-loading"></i> 正在刷新...
        </span>
        <span v-else-if="pullY > 50" class="pull-refresh-text">释放立即刷新</span>
        <span v-else-if="pullY > 0" class="pull-refresh-text">下拉刷新</span>
      </div>

      <!-- 商品双列网格 -->
      <div v-if="list.length > 0" class="ershou-goods-grid">
        <div
          v-for="item in list"
          :key="item.id"
          class="ershou-goods-card"
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
      <div v-if="!loading && !refreshing && list.length === 0" class="ershou-empty">
        <div class="ershou-empty__icon"></div>
        <p class="ershou-empty__text">暂无该分类的商品</p>
      </div>

      <!-- 上拉加载更多 -->
      <div v-if="loading && !refreshing" class="ershou-loadmore">
        <i class="weui-loading"></i>
        <span class="weui-loadmore__tips">正在加载</span>
      </div>
      <div v-if="finished && list.length > 0" class="ershou-loadmore">
        <span class="weui-loadmore__tips">没有更多了</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.ershou-type-page {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 20px;
}

.unified-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 44px;
  padding: 0 12px;
  background: #f8f8f8;
  border-bottom: 1px solid #eee;
}
.unified-header__back {
  font-size: 14px;
  color: #333;
  cursor: pointer;
  min-width: 48px;
}
.unified-header__title {
  flex: 1;
  text-align: center;
  font-size: 16px;
  font-weight: 500;
  margin: 0;
  color: #333;
}
.unified-header__placeholder {
  min-width: 48px;
}

/* 滚动容器 */
.ershou-scroll-container {
  height: calc(100vh - 44px);
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
  overscroll-behavior-y: contain;
}

/* 下拉刷新指示器 */
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
  border-top-color: #3cb395;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}
@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 加载更多 */
.ershou-loadmore {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
  color: #999;
  font-size: 14px;
  gap: 8px;
}
.ershou-loadmore .weui-loading {
  width: 16px;
  height: 16px;
  border: 2px solid #e5e5e5;
  border-top-color: #3cb395;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}
.weui-loadmore__tips {
  font-size: 14px;
  color: #999;
}

.ershou-goods-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  padding: 10px;
}
.ershou-goods-card {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  cursor: pointer;
}
.ershou-goods-card__img-wrap {
  width: 100%;
  aspect-ratio: 1;
  overflow: hidden;
  background: #f0f0f0;
}
.ershou-goods-card__img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}
.ershou-goods-card__title {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin: 8px 8px 0;
  padding: 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.35;
}
.ershou-goods-card__desc {
  font-size: 12px;
  color: #999;
  margin: 4px 8px 0;
  padding: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.ershou-goods-card__price {
  display: block;
  font-size: 16px;
  font-weight: 600;
  color: #e4393c;
  margin: 6px 8px 8px;
  padding: 0;
  font-style: normal;
}

.ershou-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
}
.ershou-empty__icon {
  width: 80px;
  height: 80px;
  background: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='%23d8d8d8'%3E%3Cpath d='M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z'/%3E%3C/svg%3E") center/contain no-repeat;
}
.ershou-empty__text {
  margin: 16px 0 0;
  font-size: 14px;
  color: #999;
}
</style>
