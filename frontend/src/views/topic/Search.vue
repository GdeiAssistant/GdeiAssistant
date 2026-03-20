<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import request from '../../utils/request'
import { showErrorTopTips } from '@/utils/toast.js'
import CommunityHeader from '../../components/community/CommunityHeader.vue'

const router = useRouter()
const keyword = ref('')
const searchResults = ref([])
const searching = ref(false)

function handleLike(item) {
  if (item.isLiked) return
  request.post(`/topic/id/${item.id}/like`)
    .then(() => {
      item.isLiked = true
      item.likeCount = (item.likeCount || 0) + 1
    })
}

function doSearch() {
  const trimmedKeyword = keyword.value && keyword.value.trim()
  if (!trimmedKeyword) {
    showErrorTopTips('请输入您要搜索的话题或内容！')
    return
  }
  searching.value = true
  request.get(`/topic/keyword/${encodeURIComponent(trimmedKeyword)}/start/0/size/50`)
    .then((res) => {
      const rawList = res?.data || []
      searchResults.value = Array.isArray(rawList) ? rawList.map((item) => ({
        id: item.id,
        topicTag: item.topic,
        content: item.content,
        userName: item.username || '匿名',
        userAvatar: '/img/avatar/default.png',
        time: item.publishTime,
        images: item.firstImageUrl ? [item.firstImageUrl] : [],
        likeCount: item.likeCount ?? 0,
        isLiked: item.liked === true
      })) : []
      searching.value = false
    })
    .catch(() => {
      searching.value = false
    })
}
</script>

<template>
  <div class="topic-search">
    <CommunityHeader title="搜索话题" moduleColor="#6366f1" @back="router.back()" backTo="" showBack />

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

    <div v-if="searching" class="community-loadmore"><i class="community-loading-spinner"></i> 搜索中...</div>
    <div v-else-if="searchResults.length === 0 && keyword" class="community-empty">
      <div class="community-empty__text">暂无结果</div>
    </div>
    <div v-else class="topic-list">
      <div
        v-for="(item, index) in searchResults"
        :key="item.id"
        class="topic-card"
        :style="{ animationDelay: (index * 0.05) + 's' }"
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
  background: var(--c-bg);
  min-height: 100vh;
  padding-bottom: 60px;
}

.topic-search__bar {
  display: flex;
  gap: 10px;
  padding: 15px;
  background: var(--c-card);
  border-bottom: 1px solid var(--c-border);
}
.topic-search__input {
  flex: 1;
  height: 36px;
  padding: 0 12px;
  border: 1px solid var(--c-divider);
  border-radius: var(--radius-full);
  font-size: var(--font-base);
  outline: none;
}
.topic-search__input:focus {
  border-color: var(--c-topic);
}
.topic-search__btn {
  padding: 0 20px;
  height: 36px;
  background: var(--c-topic);
  color: #fff;
  border: none;
  border-radius: var(--radius-full);
  font-size: var(--font-base);
  cursor: pointer;
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
