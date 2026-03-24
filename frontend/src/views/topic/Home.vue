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

function getImageGridCols(count) {
  if (count === 1) return 'grid-cols-1'
  if (count === 2) return 'grid-cols-2'
  return 'grid-cols-3'
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
    class="bg-[var(--c-bg)] min-h-screen pb-16"
    @touchstart="handleTouchStart"
    @touchmove="handleTouchMove($event, scrollContainer)"
    @touchend="handleTouchEnd"
  >
    <CommunityHeader title="话题" moduleColor="#6366f1" />

    <div class="flex items-center justify-center overflow-hidden text-xs text-[var(--c-text-3)]" :style="{ height: pullY + 'px' }">
      <span v-if="refreshing" class="flex items-center gap-2"><i class="w-5 h-5 border-2 border-[var(--c-border)] border-t-[#6366f1] rounded-full animate-spin"></i> 正在刷新...</span>
      <span v-else-if="pullY > 50">释放立即刷新</span>
      <span v-else-if="pullY > 0">下拉刷新</span>
    </div>

    <div class="p-4 flex flex-col gap-4">
      <div
        v-for="(item, index) in list"
        :key="item.id"
        class="bg-[var(--c-surface)] rounded-xl p-4 shadow-sm animate-[community-slide-up_0.3s_ease_both]"
        :style="{ animationDelay: (index * 0.05) + 's' }"
      >
        <!-- 卡片头部 -->
        <div class="flex items-center mb-3">
          <img :src="item.userAvatar || '/img/avatar/default.png'" class="w-10 h-10 rounded-full object-cover mr-3" />
          <div class="flex-1">
            <div class="text-sm font-semibold text-[var(--c-text-1)] mb-1">{{ item.userName }}</div>
            <div class="text-xs text-[var(--c-text-3)]">{{ item.time }}</div>
          </div>
        </div>

        <!-- 卡片内容 -->
        <div class="mb-3 leading-relaxed text-sm text-[var(--c-text-1)]">
          <span class="text-[var(--c-topic)] text-base font-medium mr-1.5">{{ item.topicTag }}</span>
          <span class="text-[var(--c-text-1)]">{{ item.content }}</span>
        </div>

        <!-- 图片网格 -->
        <div v-if="item.images && item.images.length > 0" class="mb-3 grid gap-1.5" :class="[getImageGridCols(item.images.length)]">
          <div
            v-for="(img, idx) in item.images"
            :key="idx"
            class="relative rounded-md overflow-hidden cursor-pointer bg-gray-100"
            :class="item.images.length === 1 ? 'max-w-[80%] mx-auto' : 'aspect-square'"
          >
            <img
              :src="img"
              :alt="`图片${idx + 1}`"
              :class="item.images.length === 1 ? 'w-full h-auto max-h-[400px] object-contain' : 'w-full h-full object-cover'"
              @click="openImagePreview(img)"
            />
          </div>
        </div>

        <!-- 互动区 -->
        <div class="flex justify-end pt-2 border-t border-[var(--c-border)]">
          <button
            type="button"
            class="flex items-center gap-1 bg-none border-none text-sm cursor-pointer px-2 py-1 rounded transition-all duration-200"
            :class="item.isLiked ? 'text-[var(--c-topic)]' : 'text-[var(--c-text-2)]'"
            @click="handleLike(item)"
          >
            <span class="text-xl transition-transform duration-200" :class="{ 'scale-120 animate-[community-like-bounce_0.3s_ease]': item.isLiked }">{{ item.isLiked ? '❤️' : '🤍' }}</span>
            <span>{{ item.likeCount || 0 }}</span>
          </button>
        </div>
      </div>
    </div>

    <div v-if="!loading && !refreshing && list.length === 0" class="flex flex-col items-center py-16 text-[var(--c-text-3)]">
      <p class="text-sm">暂无话题</p>
    </div>
    <div v-if="loading && !refreshing" class="flex items-center justify-center gap-2 py-4 text-sm text-[var(--c-text-3)]"><i class="w-5 h-5 border-2 border-[var(--c-border)] border-t-[#6366f1] rounded-full animate-spin"></i> 正在加载</div>
    <div v-if="finished && list.length > 0" class="flex items-center justify-center py-4 text-sm text-[var(--c-text-3)]">没有更多了</div>

    <!-- 图片预览 Lightbox -->
    <div v-if="previewVisible" class="fixed inset-0 z-[2000] bg-black/90 flex items-center justify-center" @click="closeImagePreview">
      <img :src="previewImage" class="max-w-[90%] max-h-[90vh] object-contain" />
    </div>
  </div>
</template>
