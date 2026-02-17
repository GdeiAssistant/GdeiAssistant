<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import request from '../../utils/request'
import { useScrollLoad } from '../../composables/useScrollLoad'

const router = useRouter()
const scrollContainer = ref(null)
const activeType = ref(1) // 1: 最美生活照, 2: 最美校园照

const fetchPhotographList = async (page) => {
  const res = await request.get('/photograph/items', {
    params: { page, limit: 10, type: activeType.value }
  })
  return res
}

const {
  items: list,
  loading,
  finished,
  refreshing,
  pullY,
  loadData,
  handleScroll,
  handleTouchStart,
  handleTouchMove,
  handleTouchEnd
} = useScrollLoad(fetchPhotographList)

const setType = (type) => {
  if (activeType.value === type) return
  activeType.value = type
  loadData(true)
}

const toggleLike = (item, e) => {
  if (e) e.stopPropagation()
  if (!item) return
  if (!Object.prototype.hasOwnProperty.call(item, 'isLiked')) {
    item.isLiked = false
  }
  if (!Object.prototype.hasOwnProperty.call(item, 'likeCount')) {
    item.likeCount = item.likes ?? 0
  }
  item.isLiked = !item.isLiked
  item.likeCount += item.isLiked ? 1 : -1
  if (item.likeCount < 0) item.likeCount = 0
}

const goDetail = (id) => {
  router.push(`/photograph/detail/${id}`)
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="photograph-home">
    <!-- 统一顶部导航栏：左侧返回，中间标题，右侧留空 -->
    <div class="unified-header">
      <div class="header-left" @click="router.push('/')">‹</div>
      <div class="header-title">拍好校园</div>
      <div class="header-right"></div>
    </div>

    <!-- 列表滚动容器：单列卡片列表 -->
    <div
      class="photograph-scroll-container"
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

      <!-- 照片卡片列表：参考 .card 结构 -->
      <div id="card-box">
        <div
          v-for="item in list"
          :key="item.id"
          class="card"
          @click="goDetail(item.id)"
        >
          <div class="card-img">
            <figure class="card-img-tag">
              <img :src="item.imgUrl" :alt="item.title" />
            </figure>
            <!-- 多图角标：右下角显示 N图（仅多图时展示） -->
            <div class="tags" v-if="(item.photoCount || 1) > 1">
              <span class="img-num">{{ item.photoCount || 1 }}图</span>
            </div>
          </div>
          <div class="card-name">
            {{ item.title }}
          </div>
          <div class="card-say">
            {{ item.description }}
          </div>

          <!-- 卡片下方按钮组：点赞 + 评论 -->
          <div class="card-btn-group">
            <div class="am-btn-group am-btn-group-justify">
              <a
                class="am-btn am-btn-photo"
                :class="{ liked: item.isLiked }"
                href="javascript:;"
                role="button"
                @click.stop="toggleLike(item, $event)"
              >
                <i :class="item.isLiked ? 'am-icon-check-square' : 'am-icon-check-square-o'"></i
                >{{ item.likeCount ?? item.likes }} 点赞
              </a>
              <a class="am-btn am-btn-photo" href="javascript:;" role="button">
                <i class="am-icon-th-list"></i>0 评论
              </a>
            </div>
          </div>

        </div>
      </div>

      <!-- 空状态 -->
      <div v-if="!loading && !refreshing && list.length === 0" class="photograph-empty">
        <p>暂无照片作品</p>
      </div>

      <!-- 上拉加载更多 -->
      <div v-if="loading && !refreshing" class="photograph-loadmore">
        <i class="weui-loading"></i>
        <span class="weui-loadmore__tips">正在加载</span>
      </div>
      <div v-if="finished && list.length > 0" class="photograph-loadmore">
        <span class="weui-loadmore__tips">没有更多了</span>
      </div>
    </div>

    <!-- 底部三色操作栏 -->
    <footer class="photo-toolbar">
      <div class="toolbar-btn life" :class="{ active: activeType === 1 }" @click="setType(1)">
        <span>最美生活照</span>
      </div>
      <div class="toolbar-btn campus" :class="{ active: activeType === 2 }" @click="setType(2)">
        <span>最美校园照</span>
      </div>
      <div class="toolbar-btn upload" @click="router.push('/photograph/publish')">
        <span>我要晒照</span>
      </div>
    </footer>
  </div>
</template>

<style scoped>
.photograph-home {
  min-height: 100vh;
  background: #f8f8f8;
}

/* 统一顶部导航栏 */
.unified-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 44px;
  padding: 0 12px;
  background-color: #f8f8f8;
  border-bottom: 1px solid #eee;
}
.header-left {
  padding: 0 10px;
  display: flex;
  align-items: center;
  cursor: pointer;
  min-width: 48px;
  font-size: 24px;
  font-weight: 300 !important;
  color: #333;
}
.header-left i,
.header-left svg,
.header-left img {
  font-size: 24px !important;
  width: 24px !important;
  height: 24px !important;
  font-weight: 300 !important;
  transform: scale(1.4);
  transform-origin: center center;
}
.header-left svg {
  stroke-width: 1 !important;
}
.header-right {
  width: 48px;
  text-align: center;
}
.header-title {
  flex: 1;
  text-align: center;
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

/* 滚动容器 */
.photograph-scroll-container {
  height: calc(100vh - 44px - 80px);
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
  padding-bottom: 80px;
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
  to {
    transform: rotate(360deg);
  }
}

/* 卡片样式：参考 photograph/index.css .card */
.card {
  background-color: #fff;
  border: 1px solid transparent;
  border-radius: 0;
  box-shadow: 0 1px 1px rgba(0, 0, 0, 0.28);
  width: 95%;
  height: auto;
  margin: 0 auto 30px auto;
}
.card-img-tag img {
  width: 100%;
  height: auto;
  display: block;
}
.card-img {
  position: relative;
}
.card-img {
  position: relative;
}
.card-name {
  margin: 10px;
  font-size: 20px;
  font-family: sans-serif;
  clear: both;
}
.card-say {
  margin: 10px;
  font-size: 15px;
}

/* 多图角标：参考 .tags + .img-num */
.tags {
  position: absolute;
  right: 10px;
  bottom: 10px;
  display: inline-flex;
}
.img-num {
  background: #f39c12;
  color: #fff;
  border-radius: 2px;
  padding: 2px 6px;
  font-size: 12px;
}

/* 卡片按钮组：参考 .card-btn-group + .am-btn-photo */
.card-btn-group {
  font-size: 20px;
  padding: 0 10px 10px 10px;
}
.am-btn-group-justify {
  display: flex;
}
.am-btn {
  flex: 1;
  text-align: center;
  padding: 8px 0;
  border: none;
  cursor: pointer;
}
.am-btn-photo {
  color: #fff;
  background-color: #518379;
  border-color: #ffffff;
  font-size: 80%;
}
.am-btn-photo:hover {
  color: #ffeb3b !important;
}
.am-btn-photo.liked {
  background-color: #2e8b57 !important;
  color: #fff !important;
}
.am-btn-photo.liked:hover {
  color: #fff !important;
}
.am-btn-photo i {
  margin-right: 4px;
}

/* 底部三色操作栏 */
.photo-toolbar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  box-shadow: 0 -1px 5px #989898;
}
.photo-toolbar .toolbar-btn {
  flex: 1;
  text-align: center;
  padding: 8px 0;
  color: #fff;
  font-size: 14px;
  cursor: pointer;
}
.photo-toolbar .life {
  background-color: #e84c3d; /* 类似 am-btn-danger */
}
.photo-toolbar .campus {
  background-color: #3498db; /* 类似 am-btn-primary */
}
.photo-toolbar .upload {
  background-color: #27ae60; /* 类似 am-btn-success */
}

/* 空状态与加载更多 */
.photograph-empty {
  text-align: center;
  padding: 60px 20px;
  color: #999;
  font-size: 14px;
}
.photograph-loadmore {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
  color: #999;
  font-size: 14px;
  gap: 8px;
}
.photograph-loadmore .weui-loading {
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

