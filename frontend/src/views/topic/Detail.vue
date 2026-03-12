<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '../../utils/request'

const route = useRoute()
const router = useRouter()

const topic = ref(null)
const loading = ref(true)
const likeButtonRef = ref(null)

async function loadDetail() {
  try {
    loading.value = true
    const res = await request.get(`/topic/id/${route.params.id}`)
    const data = res?.data
    if (data && res.success !== false) {
      topic.value = {
        id: data.id,
        topic: data.topic || '',
        content: data.content || '',
        publishTime: data.publishTime || '',
        likeCount: data.likeCount ?? 0,
        liked: data.liked === true,
        images: Array.isArray(data.imageUrls) ? data.imageUrls : []
      }
    } else {
      topic.value = null
    }
  } catch (_) {
    topic.value = null
  } finally {
    loading.value = false
  }
}

function handleLike() {
  if (!topic.value || topic.value.liked) {
    return
  }
  request.post(`/topic/id/${topic.value.id}/like`).then(() => {
    topic.value.liked = true
    topic.value.likeCount++
  }).catch(() => {})
}

function notificationTargetType() {
  return route.query?.targetType ? String(route.query.targetType) : ''
}

function openedFromNotification() {
  return !!route.query?.notificationId
}

function isHighlightedLike() {
  return openedFromNotification() && notificationTargetType() === 'like'
}

async function focusNotificationTarget() {
  if (!openedFromNotification()) {
    return
  }
  await nextTick()
  likeButtonRef.value?.scrollIntoView?.({ behavior: 'smooth', block: 'center' })
}

onMounted(async () => {
  await loadDetail()
  await focusNotificationTarget()
})
</script>

<template>
  <div class="topic-detail">
    <div class="topic-header unified-header">
      <span class="topic-header__back" @click="router.back()">返回</span>
      <h1 class="topic-header__title">话题详情</h1>
      <span class="topic-header__placeholder"></span>
    </div>

    <div v-if="loading" class="topic-loading">
      <i class="weui-loading"></i>
      <span>加载中...</span>
    </div>

    <div v-else-if="topic" class="topic-detail__content">
      <div class="topic-card">
        <div class="topic-card__meta">
          <span class="topic-card__tag">#{{ topic.topic || '校园话题' }}</span>
          <span class="topic-card__time">{{ topic.publishTime || '最近更新' }}</span>
        </div>
        <p class="topic-card__text">{{ topic.content || '暂无内容' }}</p>
        <div v-if="topic.images.length" class="topic-card__images">
          <img
            v-for="(image, index) in topic.images"
            :key="`${topic.id}-${index}`"
            :src="image"
            :alt="`${topic.topic || '话题'}-${index + 1}`"
            class="topic-card__image"
          >
        </div>
        <div class="topic-card__footer">
          <button ref="likeButtonRef" type="button" class="topic-like-btn" :class="{ 'is-liked': topic.liked, 'is-highlighted': isHighlightedLike() }" @click="handleLike">
            {{ topic.liked ? '已点赞' : '点赞' }} {{ topic.likeCount || 0 }}
          </button>
        </div>
      </div>
    </div>

    <div v-else class="topic-empty">话题不存在或已被删除</div>
  </div>
</template>

<style scoped>
.topic-detail {
  min-height: 100vh;
  background: #f5f5f5;
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

.topic-header__back {
  color: #333;
  cursor: pointer;
  min-width: 48px;
  font-size: 14px;
}

.topic-header__title {
  flex: 1;
  text-align: center;
  font-size: 16px;
  font-weight: 500;
  margin: 0;
  color: #333;
}

.topic-header__placeholder {
  min-width: 48px;
}

.topic-loading,
.topic-empty {
  padding: 40px 16px;
  color: #999;
  font-size: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.topic-detail__content {
  padding: 16px;
}

.topic-card {
  background: #fff;
  border-radius: 14px;
  padding: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.04);
}

.topic-card__meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.topic-card__tag {
  color: #10b981;
  font-size: 16px;
  font-weight: 600;
}

.topic-card__time {
  color: #999;
  font-size: 12px;
  flex-shrink: 0;
}

.topic-card__text {
  margin-top: 16px;
  font-size: 15px;
  line-height: 1.7;
  color: #333;
  white-space: pre-wrap;
}

.topic-card__images {
  margin-top: 16px;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.topic-card__image {
  width: 100%;
  border-radius: 10px;
  object-fit: cover;
  background: #f3f4f6;
}

.topic-card__footer {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.topic-like-btn {
  border: none;
  border-radius: 999px;
  padding: 8px 14px;
  font-size: 13px;
  color: #10b981;
  background: #ecfdf5;
}

.topic-like-btn.is-liked {
  color: #999;
  background: #f3f4f6;
}

.topic-like-btn.is-highlighted {
  box-shadow: 0 0 0 2px rgba(16, 185, 129, 0.18), 0 8px 16px rgba(16, 185, 129, 0.12);
}
</style>
