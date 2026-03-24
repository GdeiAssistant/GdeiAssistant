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
  <div class="min-h-screen bg-[var(--c-bg)]">
    <CommunityHeader title="话题详情" moduleColor="#6366f1" @back="router.back()" backTo="" />

    <div v-if="loading" class="flex items-center justify-center gap-2 py-4 text-sm text-[var(--c-text-3)]">
      <i class="w-5 h-5 border-2 border-[var(--c-border)] border-t-[#6366f1] rounded-full animate-spin"></i>
      <span>加载中...</span>
    </div>

    <div v-else-if="topic" class="p-4">
      <div class="bg-[var(--c-surface)] rounded-xl p-4 shadow-sm">
        <div class="flex items-center justify-between gap-3">
          <span class="text-[var(--c-topic)] text-base font-semibold">#{{ topic.topic || '校园话题' }}</span>
          <span class="text-[var(--c-text-3)] text-xs shrink-0">{{ topic.publishTime || '最近更新' }}</span>
        </div>
        <p class="mt-4 text-sm leading-[1.7] text-[var(--c-text-1)] whitespace-pre-wrap">{{ topic.content || '暂无内容' }}</p>
        <div v-if="topic.images.length" class="mt-4 grid grid-cols-2 gap-2.5">
          <img
            v-for="(image, index) in topic.images"
            :key="`${topic.id}-${index}`"
            :src="image"
            :alt="`${topic.topic || '话题'}-${index + 1}`"
            class="w-full rounded-lg object-cover bg-[var(--c-bg)]"
          >
        </div>
        <div class="mt-4 flex justify-end">
          <button
            type="button"
            class="border-none rounded-full px-3.5 py-2 text-sm cursor-pointer transition-all duration-200"
            :class="topic.liked ? 'text-[var(--c-text-3)] bg-[var(--c-bg)]' : 'text-[var(--c-topic)] bg-[color-mix(in_srgb,var(--c-topic)_10%,white)]'"
            @click="handleLike"
          >
            {{ topic.liked ? '已点赞' : '点赞' }} {{ topic.likeCount || 0 }}
          </button>
        </div>
      </div>
    </div>

    <div v-else class="flex flex-col items-center py-16 text-[var(--c-text-3)]">
      <p class="text-sm">话题不存在或已被删除</p>
    </div>
  </div>
</template>
