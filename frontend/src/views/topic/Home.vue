<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '../../utils/request'
import { useScrollLoad } from '../../composables/useScrollLoad'
import CommunityHeader from '../../components/community/CommunityHeader.vue'

const router = useRouter()
const scrollContainer = ref({ get scrollTop() { return window.pageYOffset || document.documentElement.scrollTop } })
const previewImage = ref('')
const previewVisible = ref(false)

const PAGE_SIZE = 10
const fetchTopicData = async (page) => {
  const start = (page - 1) * PAGE_SIZE
  const res = await request.get(`/topic/start/${start}/size/${PAGE_SIZE}`)
  const rawList = res?.data || []
  const list = Array.isArray(rawList) ? rawList.map((t) => ({
    id: t.id,
    topicTag: t.topic,
    content: t.content,
    userName: t.username || '匿名',
    userAvatar: '/img/avatar/default.png',
    time: t.publishTime,
    images: t.firstImageUrl ? [t.firstImageUrl] : [],
    likeCount: t.likeCount ?? 0,
    isLiked: t.liked === true
  })) : []
  return { list, hasMore: list.length >= PAGE_SIZE }
}

const { items: list, loading, finished, refreshing, pullY, loadData, handleTouchStart, handleTouchMove, handleTouchEnd } = useScrollLoad(fetchTopicData)

function handleLike(item) {
  if (item.isLiked) return
  request.post(`/topic/id/${item.id}/like`).then(() => {
    item.isLiked = true
    item.likeCount++
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
    <CommunityHeader title="话题" moduleColor="#6366f1" />

    <div class="community-pull-refresh" :style="{ height: pullY + 'px' }">
      <span v-if="refreshing" class="community-pull-refresh__text"><i class="community-loading-spinner"></i> 正在刷新...</span>
      <span v-else-if="pullY > 50" class="community-pull-refresh__text">释放立即刷新</span>
      <span v-else-if="pullY > 0" class="community-pull-refresh__text">下拉刷新</span>
    </div>

    <div class="topic-list">
      <div
        v-for="(item, index) in list"
        :key="item.id"
        class="topic-card"
        :style="{ animationDelay: (index * 0.05) + 's' }"
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

    <div v-if="!loading && !refreshing && list.length === 0" class="community-empty">
      <div class="community-empty__text">暂无话题</div>
    </div>
    <div v-if="loading && !refreshing" class="community-loadmore"><i class="community-loading-spinner"></i> 正在加载</div>
    <div v-if="finished && list.length > 0" class="community-loadmore">没有更多了</div>

    <!-- 图片预览 Lightbox -->
    <div v-if="previewVisible" class="community-lightbox" @click="closeImagePreview">
      <img :src="previewImage" />
    </div>
  </div>
</template>

<style scoped>
.topic-home {
  background: var(--c-bg);
  min-height: 100vh;
  padding-bottom: 60px;
}

.topic-list {
  padding: 15px;
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.topic-card {
  background: var(--c-card);
  border-radius: var(--radius-md);
  padding: 15px;
  box-shadow: var(--shadow-sm);
  animation: community-slide-up 0.3s ease both;
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
  font-size: var(--font-md);
  font-weight: 600;
  color: var(--c-text-1);
  margin-bottom: 4px;
}
.topic-card__time {
  font-size: var(--font-sm);
  color: var(--c-text-3);
}

.topic-card__content {
  margin-bottom: 12px;
  line-height: 1.6;
  font-size: var(--font-md);
  color: var(--c-text-1);
}
.topic-card__tag {
  color: var(--c-topic);
  font-size: var(--font-lg);
  font-weight: 500;
  margin-right: 6px;
}
.topic-card__text {
  color: var(--c-text-1);
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
  border-top: 1px solid var(--c-border);
}
.topic-like-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  background: none;
  border: none;
  font-size: var(--font-base);
  color: var(--c-text-2);
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: all 0.2s;
}
.topic-like-btn__icon {
  font-size: var(--font-xl);
  transition: transform 0.2s;
}
.topic-like-btn.is-liked {
  color: var(--c-topic);
}
.topic-like-btn.is-liked .topic-like-btn__icon {
  transform: scale(1.2);
  animation: community-like-bounce 0.3s ease;
}
</style>
