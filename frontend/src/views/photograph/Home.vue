<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import request from '../../utils/request'
import { useScrollLoad } from '../../composables/useScrollLoad'
import CommunityHeader from '../../components/community/CommunityHeader.vue'
import { createCommunityPullMessages } from '../community/communityContent'
import { getPhotographCopy } from './photographContent'

const router = useRouter()
const { t, locale } = useI18n()
const scrollContainer = ref(null)
const activeType = ref(1) // 1: 最美生活照, 2: 最美校园照
const copy = computed(() => getPhotographCopy(locale.value))
const pullMessages = computed(() => createCommunityPullMessages(t))

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
  <div class="community-stream-page community-stream-page--photograph min-h-screen bg-[var(--c-bg)]" :style="{ '--module-color': 'var(--c-photograph)' }">
    <CommunityHeader :title="t('feature.photograph.name')" moduleColor="var(--c-photograph)" backTo="/" />

    <!-- Scrollable container -->
    <div
      class="community-desktop-scroll h-[calc(100vh-51px-80px)] overflow-y-auto pb-20"
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
          <i class="w-5 h-5 border-2 border-[var(--c-border)] border-t-[var(--c-photograph)] rounded-full animate-spin"></i> {{ pullMessages.refreshing }}
        </span>
        <span v-else-if="pullY > 50">{{ pullMessages.releaseToRefresh }}</span>
        <span v-else-if="pullY > 0">{{ pullMessages.pullToRefresh }}</span>
      </div>

      <!-- Photo type switch -->
      <div class="community-photograph-switch" role="tablist" :aria-label="t('feature.photograph.name')">
        <button
          type="button"
          role="tab"
          class="community-photograph-switch__item"
          :class="{ 'community-photograph-switch__item--active': activeType === 1 }"
          :aria-selected="activeType === 1"
          @click="setType(1)"
        >
          {{ copy.lifeTab }}
        </button>
        <button
          type="button"
          role="tab"
          class="community-photograph-switch__item"
          :class="{ 'community-photograph-switch__item--active': activeType === 2 }"
          :aria-selected="activeType === 2"
          @click="setType(2)"
        >
          {{ copy.campusTab }}
        </button>
      </div>

      <!-- Card list -->
      <div class="community-desktop-card-grid p-4">
        <div
          v-for="(item, index) in list"
          :key="item.id"
          class="community-desktop-photo-card bg-[var(--c-surface)] rounded-xl shadow-sm w-full mb-4 overflow-hidden animate-[slide-up_0.4s_ease_both] cursor-pointer"
          :style="{ animationDelay: (index % 10) * 0.05 + 's' }"
          @click="goDetail(item.id)"
        >
          <!-- Image -->
          <div class="relative">
            <figure class="m-0 p-0">
              <img :src="item.imgUrl" :alt="item.title" class="w-full h-auto block" />
            </figure>
            <div class="absolute right-2 bottom-2 inline-flex" v-if="(item.photoCount || 1) > 1">
              <span class="bg-[var(--c-photograph)] text-white rounded-lg px-2 py-0.5 text-sm font-medium">{{ copy.formatImageBadge(item.photoCount || 1) }}</span>
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
                class="community-photograph-action flex-1 text-center py-2 border-none rounded-lg cursor-pointer text-white text-base no-underline transition-opacity active:opacity-85"
                :class="{ 'community-photograph-action--liked': item.isLiked }"
                href="javascript:;"
                role="button"
                @click.stop="toggleLike(item, $event)"
              >
                {{ copy.formatLikeMetric(item.likeCount ?? item.likes ?? 0) }}
              </a>
              <a class="community-photograph-action flex-1 text-center py-2 border-none rounded-lg cursor-pointer text-white text-base no-underline transition-opacity active:opacity-85" href="javascript:;" role="button">
                {{ copy.formatCommentMetric(item.commentCount ?? 0) }}
              </a>
            </div>
          </div>
        </div>
      </div>

      <!-- Empty -->
      <div v-if="!loading && !refreshing && list.length === 0" class="flex flex-col items-center py-16 text-[var(--c-text-3)]">
        <div class="text-4xl mb-2">📷</div>
        <p class="text-sm">{{ copy.empty }}</p>
      </div>

      <!-- Loading -->
      <div v-if="loading && !refreshing" class="flex items-center justify-center gap-2 py-4 text-sm text-[var(--c-text-3)]">
        <i class="w-5 h-5 border-2 border-[var(--c-border)] border-t-[var(--c-photograph)] rounded-full animate-spin"></i>
        <span>{{ pullMessages.loading }}</span>
      </div>
      <div v-if="finished && list.length > 0" class="text-center py-4 text-sm text-[var(--c-text-3)]">{{ pullMessages.noMore }}</div>
    </div>
  </div>
</template>

<style scoped>
.community-photograph-switch {
  display: flex;
  gap: 6px;
  margin: 14px 16px 0;
  padding: 4px;
  border: 1px solid color-mix(in srgb, var(--c-photograph) 18%, var(--c-border));
  border-radius: 20px;
  background: color-mix(in srgb, var(--c-photograph) 5%, var(--c-surface));
  box-shadow: 0 12px 28px color-mix(in srgb, var(--c-photograph) 8%, transparent);
}

.community-photograph-switch__item {
  display: inline-flex;
  flex: 1;
  min-height: 46px;
  align-items: center;
  justify-content: center;
  border: 0;
  border-radius: 16px;
  background: transparent;
  color: color-mix(in srgb, var(--c-photograph) 34%, var(--c-text-2));
  cursor: pointer;
  font: inherit;
  font-size: 15px;
  font-weight: 850;
  line-height: 1;
  transition: background 0.18s ease, color 0.18s ease, box-shadow 0.18s ease, transform 0.18s ease;
}

.community-photograph-switch__item:hover {
  color: color-mix(in srgb, var(--c-photograph) 78%, #0f172a);
  background: color-mix(in srgb, var(--c-photograph) 8%, transparent);
}

.community-photograph-switch__item:active {
  transform: scale(0.99);
}

.community-photograph-switch__item--active,
.community-photograph-switch__item--active:hover {
  background: linear-gradient(135deg, var(--c-photograph), color-mix(in srgb, var(--c-photograph) 72%, #0ea5e9));
  color: #fff;
  box-shadow: 0 12px 24px color-mix(in srgb, var(--c-photograph) 22%, transparent);
}

[data-theme="dark"] .community-photograph-switch {
  border-color: rgba(68, 89, 112, 0.72);
  background: rgba(24, 38, 53, 0.76);
  box-shadow: 0 14px 30px rgba(0, 0, 0, 0.18);
}

[data-theme="dark"] .community-photograph-switch__item {
  color: color-mix(in srgb, var(--c-photograph) 26%, var(--c-text-2));
}

[data-theme="dark"] .community-photograph-switch__item:hover {
  background: rgba(32, 48, 68, 0.62);
  color: color-mix(in srgb, var(--c-photograph) 34%, var(--c-text-1));
}

[data-theme="dark"] .community-photograph-switch__item--active,
[data-theme="dark"] .community-photograph-switch__item--active:hover {
  background: linear-gradient(
    135deg,
    color-mix(in srgb, var(--c-photograph) 36%, #24384d),
    color-mix(in srgb, var(--c-photograph) 22%, #172435)
  );
  color: color-mix(in srgb, var(--c-photograph) 42%, #fff);
  box-shadow: inset 0 0 0 1px color-mix(in srgb, var(--c-photograph) 18%, rgba(111, 132, 156, 0.72));
}

@media (min-width: 768px) {
  .community-photograph-switch {
    margin: 16px 16px 0;
  }
}
</style>
