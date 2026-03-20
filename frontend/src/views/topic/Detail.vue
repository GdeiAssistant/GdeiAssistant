<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '../../utils/request'
import CommunityHeader from '../../components/community/CommunityHeader.vue'

const route = useRoute()
const router = useRouter()

const topic = ref(null)
const loading = ref(true)

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

onMounted(async () => {
  await loadDetail()
})
</script>

<template>
  <div class="topic-detail">
    <CommunityHeader title="话题详情" moduleColor="#6366f1" @back="router.back()" backTo="" />

    <div v-if="loading" class="community-loadmore">
      <i class="community-loading-spinner"></i>
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
          <button type="button" class="topic-like-btn" :class="{ 'is-liked': topic.liked }" @click="handleLike">
            {{ topic.liked ? '已点赞' : '点赞' }} {{ topic.likeCount || 0 }}
          </button>
        </div>
      </div>
    </div>

    <div v-else class="community-empty">
      <div class="community-empty__text">话题不存在或已被删除</div>
    </div>
  </div>
</template>

<style scoped>
.topic-detail {
  min-height: 100vh;
  background: var(--c-bg);
}

.topic-detail__content {
  padding: var(--space-lg);
}

.topic-card {
  background: var(--c-card);
  border-radius: var(--radius-md);
  padding: var(--space-lg);
  box-shadow: var(--shadow-sm);
}

.topic-card__meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.topic-card__tag {
  color: var(--c-topic);
  font-size: var(--font-lg);
  font-weight: 600;
}

.topic-card__time {
  color: var(--c-text-3);
  font-size: var(--font-sm);
  flex-shrink: 0;
}

.topic-card__text {
  margin-top: var(--space-lg);
  font-size: var(--font-md);
  line-height: 1.7;
  color: var(--c-text-1);
  white-space: pre-wrap;
}

.topic-card__images {
  margin-top: var(--space-lg);
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.topic-card__image {
  width: 100%;
  border-radius: var(--radius-sm);
  object-fit: cover;
  background: var(--c-bg);
}

.topic-card__footer {
  margin-top: var(--space-lg);
  display: flex;
  justify-content: flex-end;
}

.topic-like-btn {
  border: none;
  border-radius: var(--radius-full);
  padding: 8px 14px;
  font-size: var(--font-base);
  color: var(--c-topic);
  background: #eeeff8;
  background: color-mix(in srgb, var(--c-topic) 10%, white);
  cursor: pointer;
  transition: all 0.2s;
}

.topic-like-btn.is-liked {
  color: var(--c-text-3);
  background: var(--c-bg);
}
</style>
