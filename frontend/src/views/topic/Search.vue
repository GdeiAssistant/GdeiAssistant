<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '../../utils/request'
import { showErrorTopTips } from '@/utils/toast.js'

const router = useRouter()
const keyword = ref('')
const searchResults = ref([])
const searching = ref(false)

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

function doSearch() {
  const trimmedKeyword = keyword.value && keyword.value.trim()
  if (!trimmedKeyword) {
    showErrorTopTips('请输入您要搜索的话题或内容！')
    return
  }
  searching.value = true
  request.get('/topic/search', {
    params: { keyword: trimmedKeyword }
  })
    .then((res) => {
      const data = res.data || res
      searchResults.value = Array.isArray(data) ? data : (data.list || [])
      searching.value = false
    })
    .catch(() => {
      searching.value = false
    })
}
</script>

<template>
  <div class="topic-search">
    <div class="topic-header unified-header">
      <span class="topic-header__back" @click="router.back()">返回</span>
      <h1 class="topic-header__title">搜索话题</h1>
      <span class="topic-header__placeholder"></span>
    </div>

    <div class="topic-search__bar">
      <input
        type="text"
        class="topic-search__input"
        placeholder="搜索话题、内容..."
        v-model="keyword"
        @keyup.enter="doSearch"
      />
      <button type="button" class="topic-search__btn" @click="doSearch">搜索</button>
    </div>

    <div v-if="searching" class="topic-search__loading">搜索中...</div>
    <div v-else-if="searchResults.length === 0 && keyword" class="topic-search__empty">暂无结果</div>
    <div v-else class="topic-list">
      <div
        v-for="item in searchResults"
        :key="item.id"
        class="topic-card"
      >
        <div class="topic-card__header">
          <img :src="item.userAvatar || '/img/avatar/default.png'" class="topic-card__avatar" />
          <div class="topic-card__user">
            <div class="topic-card__name">{{ item.userName }}</div>
            <div class="topic-card__time">{{ item.time }}</div>
          </div>
        </div>
        <div class="topic-card__content">
          <span class="topic-card__tag">{{ item.topicTag }}</span>
          <span class="topic-card__text">{{ item.content }}</span>
        </div>
        <div v-if="item.images && item.images.length > 0" :class="['topic-card__images', 'grid-' + Math.min(item.images.length, 3)]">
          <div
            v-for="(img, idx) in item.images.slice(0, 3)"
            :key="idx"
            class="topic-image-item"
          >
            <img :src="img" />
          </div>
        </div>
        <div class="topic-card__actions">
          <button type="button" class="topic-like-btn" :class="{ 'is-liked': item.isLiked }" @click="handleLike(item)">
            <span class="topic-like-btn__icon">{{ item.isLiked ? '❤️' : '🤍' }}</span>
            <span class="topic-like-btn__count">{{ item.likeCount || 0 }}</span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.topic-search {
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

.topic-search__bar {
  display: flex;
  gap: 10px;
  padding: 15px;
  background: #fff;
  border-bottom: 1px solid #e5e5e5;
}
.topic-search__input {
  flex: 1;
  height: 36px;
  padding: 0 12px;
  border: 1px solid #e5e5e5;
  border-radius: 18px;
  font-size: 14px;
  outline: none;
}
.topic-search__input:focus {
  border-color: #10b981;
}
.topic-search__btn {
  padding: 0 20px;
  height: 36px;
  background: #10b981;
  color: #fff;
  border: none;
  border-radius: 18px;
  font-size: 14px;
  cursor: pointer;
}

.topic-search__loading,
.topic-search__empty {
  text-align: center;
  padding: 40px 20px;
  color: #999;
  font-size: 14px;
}

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
.topic-card__images.grid-2 {
  grid-template-columns: repeat(2, 1fr);
}
.topic-card__images.grid-3 {
  grid-template-columns: repeat(3, 1fr);
}
.topic-image-item {
  aspect-ratio: 1;
  border-radius: 6px;
  overflow: hidden;
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
</style>
