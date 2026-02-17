<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import request from '../../utils/request'
import { useScrollLoad } from '../../composables/useScrollLoad'

const router = useRouter()
const activeType = ref(0) // 0: 寻物, 1: 招领
const scrollContainer = ref(null)

const fetchLostData = async (page) => {
  const res = await request.get('/lostandfound/items', {
    params: { page, limit: 10, type: activeType.value }
  })
  return res
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
    <div class="unified-header">
      <span class="unified-header__back" @click="router.push('/')">返回</span>
      <h1 class="unified-header__title">失物招领</h1>
      <span class="unified-header__placeholder"></span>
    </div>

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
      <div class="pull-refresh-indicator" :style="{ height: pullY + 'px' }">
        <span v-if="refreshing" class="pull-refresh-text">
          <i class="weui-loading"></i> 正在刷新...
        </span>
        <span v-else-if="pullY > 50" class="pull-refresh-text">释放立即刷新</span>
        <span v-else-if="pullY > 0" class="pull-refresh-text">下拉刷新</span>
      </div>

      <!-- 失物招领列表（双列网格，参考原版 base.css .list width: 46.5%） -->
      <div class="lostandfound-list">
        <div
          v-for="item in list"
          :key="item.id"
          class="lostandfound-item"
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
      <div v-if="!loading && !refreshing && list.length === 0" class="lostandfound-empty">
        <div class="lostandfound-empty__icon"></div>
        <p class="lostandfound-empty__text">暂无{{ activeType === 0 ? '寻物' : '招领' }}信息</p>
      </div>

      <!-- 上拉加载更多 -->
      <div v-if="loading && !refreshing" class="lostandfound-loadmore">
        <i class="weui-loading"></i>
        <span class="weui-loadmore__tips">正在加载</span>
      </div>
      <div v-if="finished && list.length > 0" class="lostandfound-loadmore">
        <span class="weui-loadmore__tips">没有更多了</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.lostandfound-home {
  background-color: #f5f5f5;
  min-height: 100vh;
}

.unified-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 44px;
  padding: 0 12px;
  background-color: #f8f8f8;
  border-bottom: 1px solid #eee;
}
.unified-header__back {
  font-size: 14px;
  color: #333;
  cursor: pointer;
  min-width: 48px;
  text-align: left;
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
  text-align: right;
}

/* WEUI Navbar Tab 切换 */
.weui-navbar {
  display: flex;
  background-color: #fff;
  border-bottom: 1px solid #e5e5e5;
}
.weui-navbar__item {
  flex: 1;
  padding: 13px 0;
  text-align: center;
  font-size: 15px;
  color: #666;
  cursor: pointer;
  position: relative;
}
.weui-navbar__item.weui-bar__item_on {
  color: #3cb395;
}
.weui-navbar__item.weui-bar__item_on::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 2px;
  background-color: #3cb395;
}

/* 滚动容器 */
.lostandfound-scroll-container {
  height: calc(100vh - 88px);
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

/* 失物招领列表（双列网格，参考原版 base.css .list width: 46.5%） */
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
  background: #fff;
  border-radius: 4px;
  overflow: hidden;
  cursor: pointer;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}
.lostandfound-item__img-wrap {
  width: 100%;
  height: 170px;
  position: relative;
  overflow: hidden;
  background: #f0f0f0;
}
.lostandfound-item__img-wrap img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.lostandfound-item__placeholder {
  width: 100%;
  height: 100%;
  background: #f0f0f0;
}
.lostandfound-item__tag {
  position: absolute;
  top: 8px;
  right: 8px;
  padding: 2px 8px;
  font-size: 11px;
  color: #fff;
  background-color: #ff9800;
  border-radius: 3px;
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
  color: #333;
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
  color: #999;
  margin: 0;
  padding: 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.4;
}

/* 空状态 */
.lostandfound-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
}
.lostandfound-empty__icon {
  width: 80px;
  height: 80px;
  background: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='%23d8d8d8'%3E%3Cpath d='M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z'/%3E%3C/svg%3E") center/contain no-repeat;
}
.lostandfound-empty__text {
  margin: 16px 0 0;
  font-size: 14px;
  color: #999;
}

/* 加载更多 */
.lostandfound-loadmore {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
  color: #999;
  font-size: 14px;
  gap: 8px;
}
.lostandfound-loadmore .weui-loading {
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
</style>
