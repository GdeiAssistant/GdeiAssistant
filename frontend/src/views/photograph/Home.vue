<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import request from '../../utils/request'
import { useScrollLoad } from '../../composables/useScrollLoad'
import CommunityHeader from '../../components/community/CommunityHeader.vue'

const router = useRouter()
const scrollContainer = ref(null)
const activeType = ref(1) // 1: 最美生活照, 2: 最美校园照

const PAGE_SIZE = 10
const fetchPhotographList = async (page) => {
  const start = (page - 1) * PAGE_SIZE
  const type = activeType.value === 1 ? 1 : 0
  const res = await request.get(`/photograph/type/${type}/start/${start}/size/${PAGE_SIZE}`)
  const rawList = res?.data || []
  const list = Array.isArray(rawList) ? rawList.map((p) => ({
    id: p.id,
    title: p.title,
    description: p.content,
    imgUrl: p.firstImageUrl,
    photoCount: p.count,
    likeCount: p.likeCount ?? 0,
    commentCount: p.commentCount ?? 0,
    isLiked: p.liked === true
  })) : []
  return { list, hasMore: list.length >= PAGE_SIZE }
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
  if (item.isLiked) {
    return
  }
  request.post(`/photograph/id/${item.id}/like`).then(() => {
    item.isLiked = true
    item.likeCount++
  }).catch(() => {})
}

const goDetail = (id) => {
  router.push(`/photograph/detail/${id}`)
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="min-h-screen bg-[var(--c-bg)]" :style="{ '--module-color': '#06b6d4' }">
    <CommunityHeader title="拍好校园" moduleColor="#06b6d4" backTo="/" />

    <!-- Scrollable container -->
    <div
      class="h-[calc(100vh-51px-80px)] overflow-y-auto pb-20"
      style="-webkit-overflow-scrolling: touch;"
      ref="scrollContainer"
      @scroll="handleScroll"
      @touchstart="handleTouchStart"
      @touchmove="handleTouchMove($event, scrollContainer)"
      @touchend="handleTouchEnd"
    >
      <!-- Pull refresh -->
      <div class="flex items-center justify-center overflow-hidden text-sm text-[var(--c-text-3)]" :style="{ height: pullY + 'px' }">
        <span v-if="refreshing" class="flex items-center gap-2">
          <i class="w-5 h-5 border-2 border-[var(--c-border)] border-t-cyan-500 rounded-full animate-spin"></i> 正在刷新...
        </span>
        <span v-else-if="pullY > 50">释放立即刷新</span>
        <span v-else-if="pullY > 0">下拉刷新</span>
      </div>

      <!-- Card list -->
      <div class="p-4">
        <div
          v-for="(item, index) in list"
          :key="item.id"
          class="bg-[var(--c-surface)] rounded-xl shadow-sm w-full mb-4 overflow-hidden animate-[slide-up_0.4s_ease_both] cursor-pointer"
          :style="{ animationDelay: (index % 10) * 0.05 + 's' }"
          @click="goDetail(item.id)"
        >
          <!-- Image -->
          <div class="relative">
            <figure class="m-0 p-0">
              <img :src="item.imgUrl" :alt="item.title" class="w-full h-auto block" />
            </figure>
            <div class="absolute right-2 bottom-2 inline-flex" v-if="(item.photoCount || 1) > 1">
              <span class="bg-cyan-500 text-white rounded-lg px-2 py-0.5 text-sm font-medium">{{ item.photoCount || 1 }}图</span>
            </div>
          </div>

          <!-- Title -->
          <div class="mx-4 mt-4 text-2xl font-semibold text-[var(--c-text-1)]">{{ item.title }}</div>

          <!-- Description -->
          <div class="mx-4 mb-4 mt-1 text-base text-[var(--c-text-2)] leading-relaxed">{{ item.description }}</div>

          <!-- Action buttons -->
          <div class="px-4 pb-4">
            <div class="flex gap-2">
              <a
                class="flex-1 text-center py-2 border-none rounded-lg cursor-pointer text-white text-base no-underline transition-opacity active:opacity-85"
                :class="item.isLiked ? 'bg-[color-mix(in_srgb,var(--c-photograph)_80%,#000)]' : 'bg-cyan-500'"
                href="javascript:;"
                role="button"
                @click.stop="toggleLike(item, $event)"
              >
                {{ item.likeCount ?? item.likes }} 点赞
              </a>
              <a class="flex-1 text-center py-2 border-none rounded-lg cursor-pointer text-white bg-cyan-500 text-base no-underline transition-opacity active:opacity-85" href="javascript:;" role="button">
                {{ item.commentCount ?? 0 }} 评论
              </a>
            </div>
          </div>
        </div>
      </div>

      <!-- Empty -->
      <div v-if="!loading && !refreshing && list.length === 0" class="flex flex-col items-center py-16 text-[var(--c-text-3)]">
        <div class="text-4xl mb-2">📷</div>
        <p class="text-sm">暂无照片作品</p>
      </div>

      <!-- Loading -->
      <div v-if="loading && !refreshing" class="flex items-center justify-center gap-2 py-4 text-sm text-[var(--c-text-3)]">
        <i class="w-5 h-5 border-2 border-[var(--c-border)] border-t-cyan-500 rounded-full animate-spin"></i>
        <span>正在加载</span>
      </div>
      <div v-if="finished && list.length > 0" class="text-center py-4 text-sm text-[var(--c-text-3)]">没有更多了</div>
    </div>

    <!-- Bottom toolbar -->
    <footer class="fixed bottom-0 left-0 right-0 flex shadow-lg border-t border-[var(--c-border)] z-50">
      <div
        class="flex-1 text-center py-3 text-white text-base font-medium cursor-pointer transition-opacity active:opacity-85 bg-red-500"
        :class="{ 'shadow-[inset_0_-3px_0_rgba(0,0,0,0.2)]': activeType === 1 }"
        @click="setType(1)"
      >
        <span>最美生活照</span>
      </div>
      <div
        class="flex-1 text-center py-3 text-white text-base font-medium cursor-pointer transition-opacity active:opacity-85 bg-blue-500"
        :class="{ 'shadow-[inset_0_-3px_0_rgba(0,0,0,0.2)]': activeType === 2 }"
        @click="setType(2)"
      >
        <span>最美校园照</span>
      </div>
      <div
        class="flex-1 text-center py-3 text-white text-base font-medium cursor-pointer transition-opacity active:opacity-85 bg-cyan-500"
        @click="router.push('/photograph/publish')"
      >
        <span>我要晒照</span>
      </div>
    </footer>
  </div>
</template>
