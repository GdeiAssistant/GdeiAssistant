<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import request from '../../utils/request'
import { useScrollLoad } from '../../composables/useScrollLoad'
import CommunityHeader from '../../components/community/CommunityHeader.vue'
import AppEmpty from '@/components/ui/AppEmpty.vue'
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
          class="ui-panel community-desktop-photo-card bg-[var(--c-surface)] rounded-xl shadow-sm w-full mb-4 overflow-hidden animate-[slide-up_0.4s_ease_both] cursor-pointer"
          :style="{ animationDelay: (index % 10) * 0.05 + 's' }"
          @click="goDetail(item.id)"
        >
          <!-- Image -->
          <div class="relative">
            <figure class="m-0 p-0">
              <img :src="item.imgUrl" :alt="item.title" class="community-photograph-card-image w-full h-auto block" />
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
      <div v-if="!loading && !refreshing && list.length === 0" class="community-photograph-empty-shell">
        <AppEmpty
          :title="copy.empty"
          :description="t('feature.photograph.description')"
          :action-text="copy.publishAction"
          accent="var(--c-photograph)"
          action-variant="primary"
          @action="router.push('/photograph/publish')"
        >
          <template #icon>
            <span class="community-photograph-empty-icon" aria-hidden="true">◌</span>
          </template>
        </AppEmpty>
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
  border: 1px solid var(--c-border);
  border-radius: var(--radius-card);
  background: color-mix(in srgb, var(--c-photograph) 5%, var(--c-surface));
  box-shadow: none;
}

.community-photograph-switch__item {
  display: inline-flex;
  flex: 1;
  min-height: 46px;
  align-items: center;
  justify-content: center;
  border: 0;
  border-radius: var(--radius-card);
  background: transparent;
  color: var(--c-text-2);
  cursor: pointer;
  font: inherit;
  font-size: 15px;
  font-weight: 850;
  line-height: 1;
  transition: background 0.18s ease, color 0.18s ease, box-shadow 0.18s ease, transform 0.18s ease;
}

.community-photograph-switch__item:hover {
  color: var(--c-primary);
  background: color-mix(in srgb, var(--c-photograph) 8%, transparent);
}

.community-photograph-switch__item:active {
  transform: scale(0.99);
}

.community-photograph-switch__item--active,
.community-photograph-switch__item--active:hover {
  background: var(--c-primary);
  color: var(--c-on-primary);
  box-shadow: none;
}

@media (min-width: 768px) {
  .community-photograph-switch {
    margin: 16px 16px 0;
  }
}

.community-photograph-empty-shell {
  margin: 14px 16px 0;
  border: 1px solid var(--c-border);
  border-radius: var(--radius-card);
  background: var(--c-primary-soft);
  box-shadow: none;
}

.community-photograph-empty-icon {
  font-size: 32px;
  font-weight: 700;
  line-height: 1;
}

@media (max-width: 767px) {
  .community-photograph-card-image {
    height: clamp(340px, 52vh, 460px);
    object-fit: cover;
  }
}
</style>
