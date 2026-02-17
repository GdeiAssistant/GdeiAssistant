<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '../../utils/request'
import { useScrollLoad } from '../../composables/useScrollLoad'

const router = useRouter()
const scrollContainer = ref({ get scrollTop() { return window.pageYOffset || document.documentElement.scrollTop } })
const previewImage = ref('')
const previewVisible = ref(false)

const fetchTopicData = async (page) => {
  const res = await request.get('/topic/items', {
    params: { page, limit: 10 }
  })
  return res
}

const { items: list, loading, finished, refreshing, pullY, loadData, handleTouchStart, handleTouchMove, handleTouchEnd } = useScrollLoad(fetchTopicData)

function handleLike(item) {
  if (item.isLiked === undefined) item.isLiked = false
  item.isLiked = !item.isLiked
  const delta = item.isLiked ? 1 : -1
  item.likeCount = (item.likeCount || 0) + delta
  if (item.likeCount < 0) item.likeCount = 0
  
  // 调用点赞接口
  request.post('/topic/like', { id: item.id, like: item.isLiked })
    .catch(() => {
      // 失败回滚
      item.isLiked = !item.isLiked
      item.likeCount = (item.likeCount || 0) - delta
    })
}

function openImagePreview(url) {
  previewImage.value = url
  previewVisible.value = true
}

function closeImagePreview() {
  previewVisible.value = false
  previewImage.value = ''
}

function getImageGridClass(count) {
  if (count === 1) return 'grid-1'
  if (count === 2) return 'grid-2'
  if (count >= 3 && count <= 4) return 'grid-3'
  if (count >= 5 && count <= 9) return 'grid-9'
  return 'grid-3'
}

function onWindowScroll() {
  if (refreshing.value || loading.value || finished.value) return
  const scrollTop = window.pageYOffset || document.documentElement.scrollTop
  const winH = window.innerHeight
  const docH = document.documentElement.scrollHeight
  if (scrollTop + winH >= docH - 80) loadData()
}

onMounted(() => {
  loadData()
  window.addEventListener('scroll', onWindowScroll)
})
onUnmounted(() => {
  window.removeEventListener('scroll', onWindowScroll)
})
</script>

<template>
  <div
    class="topic-home"
    @touchstart="handleTouchStart"
    @touchmove="handleTouchMove($event, scrollContainer)"
    @touchend="handleTouchEnd"
  >
    <div class="topic-header unified-header">
      <span class="topic-header__back" @click="router.push('/')">返回</span>
      <h1 class="topic-header__title">话题</h1>
      <span class="topic-header__placeholder"></span>
    </div>

    <div class="pull-refresh-indicator" :style="{ height: pullY + 'px' }">
      <span v-if="refreshing" class="pull-refresh-text"><i class="weui-loading"></i> 正在刷新...</span>
      <span v-else-if="pullY > 50" class="pull-refresh-text">释放立即刷新</span>
      <span v-else-if="pullY > 0" class="pull-refresh-text">下拉刷新</span>
    </div>

    <div class="topic-list">
      <div
        v-for="item in list"
        :key="item.id"
        class="topic-card"
      >
        <!-- 卡片头部 -->
        <div class="topic-card__header">
          <img :src="item.userAvatar || '/img/avatar/default.png'" class="topic-card__avatar" />
          <div class="topic-card__user">
            <div class="topic-card__name">{{ item.userName }}</div>
            <div class="topic-card__time">{{ item.time }}</div>
          </div>
        </div>

        <!-- 卡片内容 -->
        <div class="topic-card__content">
          <span class="topic-card__tag">{{ item.topicTag }}</span>
          <span class="topic-card__text">{{ item.content }}</span>
        </div>

        <!-- 图片网格 -->
        <div v-if="item.images && item.images.length > 0" :class="['topic-card__images', getImageGridClass(item.images.length)]">
          <div
            v-for="(img, idx) in item.images"
            :key="idx"
            class="topic-image-item"
            @click="openImagePreview(img)"
          >
            <img :src="img" :alt="`图片${idx + 1}`" />
          </div>
        </div>

        <!-- 互动区 -->
        <div class="topic-card__actions">
          <button
            type="button"
            class="topic-like-btn"
            :class="{ 'is-liked': item.isLiked }"
            @click="handleLike(item)"
          >
            <span class="topic-like-btn__icon">{{ item.isLiked ? '❤️' : '🤍' }}</span>
            <span class="topic-like-btn__count">{{ item.likeCount || 0 }}</span>
          </button>
        </div>
      </div>
    </div>

    <div v-if="!loading && !refreshing && list.length === 0" class="topic-empty">暂无话题</div>
    <div v-if="loading && !refreshing" class="topic-loadmore"><i class="weui-loading"></i> 正在加载</div>
    <div v-if="finished && list.length > 0" class="topic-loadmore">没有更多了</div>

    <!-- 图片预览 Lightbox -->
    <div v-if="previewVisible" class="topic-lightbox" @click="closeImagePreview">
      <img :src="previewImage" class="topic-lightbox__img" />
    </div>
  </div>
</template>

<style scoped>
.topic-home {
  background: #f5f5f5;
  min-height: 100vh;
  padding-bottom: 60px;
}

.topic-header.unified-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 44px;
  padding: 0 12px;
  background: #fff;
  border-bottom: 1px solid #e5e5e5;
}
.topic-header__back { color: #333; cursor: pointer; min-width: 48px; font-size: 14px; }
.topic-header__title { flex: 1; text-align: center; font-size: 16px; font-weight: 500; margin: 0; color: #333; }
.topic-header__placeholder { min-width: 48px; }

.pull-refresh-indicator { display: flex; align-items: center; justify-content: center; overflow: hidden; transition: height 0.3s; }
.pull-refresh-text { font-size: 14px; color: #999; }
.pull-refresh-text .weui-loading {
  width: 16px; height: 16px; border: 2px solid #e5e5e5; border-top-color: #10b981; border-radius: 50%;
  animation: spin 0.8s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

.topic-list {
  padding: 15px;
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.topic-card {
  background: #fff;
  border-radius: 12px;
  padding: 15px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.03);
}

.topic-card__header {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}
.topic-card__avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
  margin-right: 12px;
}
.topic-card__user {
  flex: 1;
}
.topic-card__name {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
}
.topic-card__time {
  font-size: 12px;
  color: #999;
}

.topic-card__content {
  margin-bottom: 12px;
  line-height: 1.6;
  font-size: 15px;
  color: #333;
}
.topic-card__tag {
  color: #10b981;
  font-size: 16px;
  font-weight: 500;
  margin-right: 6px;
}
.topic-card__text {
  color: #333;
}

.topic-card__images {
  margin-bottom: 12px;
  display: grid;
  gap: 6px;
}
.topic-card__images.grid-1 {
  grid-template-columns: 1fr;
}
.topic-card__images.grid-1 .topic-image-item {
  max-width: 80%;
  margin: 0 auto;
  aspect-ratio: auto;
}
.topic-card__images.grid-1 .topic-image-item img {
  width: 100%;
  height: auto;
  max-height: 400px;
  object-fit: contain;
}
.topic-card__images.grid-2 {
  grid-template-columns: repeat(2, 1fr);
}
.topic-card__images.grid-3 {
  grid-template-columns: repeat(3, 1fr);
}
.topic-card__images.grid-9 {
  grid-template-columns: repeat(3, 1fr);
}
.topic-image-item {
  position: relative;
  aspect-ratio: 1;
  border-radius: 6px;
  overflow: hidden;
  cursor: pointer;
  background: #f0f0f0;
}
.topic-image-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.topic-card__actions {
  display: flex;
  justify-content: flex-end;
  padding-top: 8px;
  border-top: 1px solid #f0f0f0;
}
.topic-like-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  background: none;
  border: none;
  font-size: 14px;
  color: #666;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: all 0.2s;
}
.topic-like-btn__icon {
  font-size: 18px;
  transition: transform 0.2s;
}
.topic-like-btn.is-liked {
  color: #10b981;
}
.topic-like-btn.is-liked .topic-like-btn__icon {
  transform: scale(1.2);
  animation: like-bounce 0.3s ease;
}
@keyframes like-bounce {
  0%, 100% { transform: scale(1.2); }
  50% { transform: scale(1.4); }
}

.topic-empty,
.topic-loadmore {
  text-align: center;
  padding: 40px 20px;
  color: #999;
  font-size: 14px;
}
.topic-loadmore .weui-loading {
  width: 16px; height: 16px; border: 2px solid #e5e5e5; border-top-color: #10b981; border-radius: 50%;
  animation: spin 0.8s linear infinite;
  display: inline-block; vertical-align: middle; margin-right: 6px;
}

.topic-lightbox {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.9);
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}
.topic-lightbox__img {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
}
</style>
