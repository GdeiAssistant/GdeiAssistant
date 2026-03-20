<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import request from '../../utils/request'
import { useScrollLoad } from '../../composables/useScrollLoad'
import CommunityHeader from '../../components/community/CommunityHeader.vue'

const router = useRouter()
const activeType = ref(0) // 0: 寻物, 1: 招领
const scrollContainer = ref(null)
const PAGE_SIZE = 10

function mapItemToCard(item) {
  return {
    id: item.id,
    title: item.name,
    desc: item.description,
    type: item.lostType,
    images: Array.isArray(item.pictureURL) ? item.pictureURL : []
  }
}

const fetchLostData = async (page) => {
  const start = (page - 1) * PAGE_SIZE
  const path = activeType.value === 0
    ? `/lostandfound/lostitem/start/${start}`
    : `/lostandfound/founditem/start/${start}`
  const res = await request.get(path)
  const rawList = res?.data || []
  const list = Array.isArray(rawList) ? rawList.map(mapItemToCard) : []
  return { list, hasMore: list.length >= PAGE_SIZE }
}

const { items: list, loading, finished, refreshing, pullY, loadData, handleScroll, handleTouchStart, handleTouchMove, handleTouchEnd } = useScrollLoad(fetchLostData)

function switchType(type) {
  activeType.value = type
  loadData(true)
}

function goDetail(id) {
  router.push(`/lostandfound/detail/${id}`)
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="lostandfound-home">
    <!-- 统一顶部导航栏 -->
    <CommunityHeader title="失物招领" moduleColor="#3b82f6" />

    <!-- Tab 切换：寻物启事 / 失物招领 -->
    <div class="weui-navbar">
      <div class="weui-navbar__item" :class="{ 'weui-bar__item_on': activeType === 0 }" @click="switchType(0)">
        <span>寻物启事</span>
      </div>
      <div class="weui-navbar__item" :class="{ 'weui-bar__item_on': activeType === 1 }" @click="switchType(1)">
        <span>失物招领</span>
      </div>
    </div>

    <!-- 滚动容器：下拉刷新 + 上拉加载 -->
    <div
      class="lostandfound-scroll-container"
      ref="scrollContainer"
      @scroll="handleScroll"
      @touchstart="handleTouchStart"
      @touchmove="handleTouchMove($event, scrollContainer)"
      @touchend="handleTouchEnd"
    >
      <!-- 下拉刷新指示器 -->
      <div class="community-pull-refresh" :style="{ height: pullY + 'px' }">
        <span v-if="refreshing" class="community-pull-refresh__text">
          <span class="community-loading-spinner" style="--module-color: #3b82f6"></span> 正在刷新...
        </span>
        <span v-else-if="pullY > 50" class="community-pull-refresh__text">释放立即刷新</span>
        <span v-else-if="pullY > 0" class="community-pull-refresh__text">下拉刷新</span>
      </div>

      <!-- 失物招领列表（双列网格，参考原版 base.css .list width: 46.5%） -->
      <div class="lostandfound-list">
        <div
          v-for="item in list"
          :key="item.id"
          class="lostandfound-item community-card"
          @click="goDetail(item.id)"
        >
          <div class="lostandfound-item__img-wrap">
            <img v-if="item.images && item.images.length > 0" :src="item.images[0]" :alt="item.title" />
            <div v-else class="lostandfound-item__placeholder"></div>
            <div class="lostandfound-item__tag" :class="{ 'tag-found': item.type === 1 }">
              {{ item.type === 0 ? '寻物' : '招领' }}
            </div>
          </div>
          <div class="lostandfound-item__content">
            <h5 class="lostandfound-item__title">{{ item.title }}</h5>
            <p class="lostandfound-item__desc">{{ item.desc }}</p>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-if="!loading && !refreshing && list.length === 0" class="community-empty">
        <div class="community-empty__icon">📭</div>
        <p class="community-empty__text">暂无{{ activeType === 0 ? '寻物' : '招领' }}信息</p>
      </div>

      <!-- 上拉加载更多 -->
      <div v-if="loading && !refreshing" class="community-loadmore">
        <span class="community-loading-spinner" style="--module-color: #3b82f6"></span>
        <span>正在加载</span>
      </div>
      <div v-if="finished && list.length > 0" class="community-loadmore">
        <span>没有更多了</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.lostandfound-home {
  background-color: var(--c-bg);
  min-height: 100vh;
}

/* WEUI Navbar Tab 切换 */
.weui-navbar {
  display: flex;
  background-color: var(--c-card);
  border-bottom: 1px solid var(--c-divider);
}
.weui-navbar__item {
  flex: 1;
  padding: 13px 0;
  text-align: center;
  font-size: 15px;
  color: var(--c-text-2);
  cursor: pointer;
  position: relative;
}
.weui-navbar__item.weui-bar__item_on {
  color: var(--c-lostandfound);
}
.weui-navbar__item.weui-bar__item_on::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 2px;
  background-color: var(--c-lostandfound);
}

/* 滚动容器 */
.lostandfound-scroll-container {
  height: calc(100vh - 95px);
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
  overscroll-behavior-y: contain;
}

/* 失物招领列表（双列网格） */
.lostandfound-list {
  margin: 10px auto;
  width: 100%;
  display: flex;
  flex-wrap: wrap;
  padding: 0 2%;
}
.lostandfound-item {
  display: inline-block;
  width: 46.5%;
  position: relative;
  margin: 4px 0 4px 2%;
  border-radius: var(--radius-md);
  overflow: hidden;
  cursor: pointer;
}
.lostandfound-item__img-wrap {
  width: 100%;
  height: 170px;
  position: relative;
  overflow: hidden;
  background: var(--c-border);
}
.lostandfound-item__img-wrap img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.lostandfound-item__placeholder {
  width: 100%;
  height: 100%;
  background: var(--c-border);
}
.lostandfound-item__tag {
  position: absolute;
  top: 8px;
  right: 8px;
  padding: 2px 8px;
  font-size: 11px;
  color: #fff;
  background-color: #ff9800;
  border-radius: var(--radius-sm);
  z-index: 1;
}
.lostandfound-item__tag.tag-found {
  background-color: #4caf50;
}
.lostandfound-item__content {
  padding: 8px;
}
.lostandfound-item__title {
  font-size: 14px;
  font-weight: 500;
  color: var(--c-text-1);
  margin: 0 0 4px 0;
  padding: 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.4;
}
.lostandfound-item__desc {
  font-size: 12px;
  color: var(--c-text-3);
  margin: 0;
  padding: 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.4;
}
</style>
