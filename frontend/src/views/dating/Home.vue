<script setup>
import { computed, ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import request from '../../utils/request'
import { useScrollLoad } from '../../composables/useScrollLoad'
import CommunityHeader from '../../components/community/CommunityHeader.vue'
import { createCommunityPullMessages } from '../community/communityContent'
import { getDatingGradeText, getDatingHomeCopy } from './datingContent'

const router = useRouter()
const { t, locale } = useI18n()
const activeArea = ref(0) // 0 小姐姐 1 小哥哥
const copy = computed(() => getDatingHomeCopy(locale.value))
const pullMessages = computed(() => createCommunityPullMessages(t))

const PAGE_SIZE = 10
const fetchDatingData = async (page) => {
  const start = (page - 1) * PAGE_SIZE
  const res = await request.get(`/dating/profile/area/${activeArea.value}/start/${start}`)
  const rawList = res?.data || []
  const list = Array.isArray(rawList) ? rawList.map((p) => ({
    id: p.profileId,
    name: p.nickname,
    image: p.pictureURL,
    grade: p.grade,
    faculty: p.faculty,
    hometown: p.hometown,
    content: p.content
  })) : []
  return { list, hasMore: list.length >= PAGE_SIZE }
}

const { items: list, loading, finished, refreshing, pullY, loadData, handleTouchStart, handleTouchMove, handleTouchEnd } = useScrollLoad(fetchDatingData)
const scrollContainer = ref({ get scrollTop() { return window.pageYOffset || document.documentElement.scrollTop } })

function switchArea(index) {
  if (activeArea.value === index) return
  activeArea.value = index
  list.value = []
  loadData(true)
}

function goDetail(id) {
  router.push(`/dating/detail/${id}`)
}


function getGradeText(grade) {
  return getDatingGradeText(t, grade, copy.value.unknown)
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
    class="community-stream-page community-stream-page--dating community-dating-page min-h-screen bg-[var(--c-bg)] pb-20"
    @touchstart="handleTouchStart"
    @touchmove="handleTouchMove($event, scrollContainer)"
    @touchend="handleTouchEnd"
  >
    <CommunityHeader :title="t('feature.dating.name')" moduleColor="var(--c-dating)" backTo="/">
      <template #right>
        <span class="text-[var(--c-text-2)] cursor-pointer text-base" @click="router.push('/dating/center')">{{ copy.myAction }}</span>
      </template>
    </CommunityHeader>

    <!-- Pull refresh -->
    <div class="flex items-center justify-center overflow-hidden text-sm text-[var(--c-text-3)]" :style="{ height: pullY + 'px' }">
      <span v-if="refreshing" class="flex items-center gap-2"><i class="w-5 h-5 border-2 border-[var(--c-border)] border-t-[var(--c-dating)] rounded-full animate-spin"></i> {{ pullMessages.refreshing }}</span>
      <span v-else-if="pullY > 50">{{ pullMessages.releaseToRefresh }}</span>
      <span v-else-if="pullY > 0">{{ pullMessages.pullToRefresh }}</span>
    </div>

    <!-- Gender Tab -->
    <div class="community-desktop-switch community-dating-switch flex bg-[var(--c-card)] mt-2 h-11" role="tablist" :aria-label="t('feature.dating.name')">
      <button
        type="button"
        role="tab"
        class="community-dating-switch__item"
        :class="{ 'community-dating-switch__item--active': activeArea === 0 }"
        :aria-selected="activeArea === 0"
        @click="switchArea(0)"
      >{{ copy.femaleTab }}</button>
      <button
        type="button"
        role="tab"
        class="community-dating-switch__item"
        :class="{ 'community-dating-switch__item--active': activeArea === 1 }"
        :aria-selected="activeArea === 1"
        @click="switchArea(1)"
      >{{ copy.maleTab }}</button>
    </div>

    <!-- Card list -->
    <div class="community-desktop-card-grid p-4">
      <div
        v-for="(item, index) in list"
        :key="item.id"
        class="community-desktop-profile-card bg-[var(--c-surface)] rounded-xl shadow-sm mb-4 p-4 overflow-hidden cursor-pointer animate-[slide-up_0.4s_ease_both]"
        :style="{ animationDelay: (index % 10) * 0.05 + 's' }"
        @click="goDetail(item.id)"
      >
        <div class="community-desktop-card-media w-full aspect-square bg-[var(--c-border)] rounded-lg overflow-hidden mb-3">
          <img :src="(item.images && item.images[0]) || item.image || '/img/dating/default-avatar.png'" :alt="item.name" class="w-full h-full object-cover" />
        </div>
        <h2 class="community-dating-card-title m-0 mb-1.5 text-xl font-bold leading-tight">{{ item.name }}</h2>
        <div class="text-[var(--c-text-2)] text-base mt-0.5 leading-relaxed">{{ item.faculty }} {{ getGradeText(item.grade) }} {{ copy.studentSuffix }}</div>
        <div class="text-[var(--c-text-2)] text-base mt-0.5 leading-relaxed">{{ copy.fromPrefix }} {{ item.hometown || copy.unknown }}</div>
        <p class="text-[var(--c-text-2)] text-sm mt-2 leading-relaxed">{{ item.content }}</p>
      </div>
    </div>

    <!-- Empty -->
    <div v-if="!loading && !refreshing && list.length === 0" class="flex flex-col items-center py-16 text-[var(--c-text-3)]">
      <div class="text-4xl mb-2">💕</div>
      <div class="text-sm">{{ copy.empty }}</div>
    </div>

    <!-- Loading -->
    <div v-if="loading && !refreshing" class="flex items-center justify-center gap-2 py-4 text-sm text-[var(--c-text-3)]">
      <i class="w-5 h-5 border-2 border-[var(--c-border)] border-t-[var(--c-dating)] rounded-full animate-spin"></i> {{ pullMessages.loading }}
    </div>
    <div v-if="finished && list.length > 0" class="text-center py-4 text-sm text-[var(--c-text-3)]">{{ pullMessages.noMore }}</div>

    <!-- FAB -->
    <div
      class="community-mobile-only-action fixed right-5 bottom-6 w-12 h-12 rounded-full bg-[var(--c-dating)] shadow-[0_4px_12px_rgba(240,163,199,0.28)] flex items-center justify-center z-[100] cursor-pointer transition-transform active:scale-[0.92]"
      @click="router.push('/dating/publish')"
    >
      <span class="text-[28px] leading-none text-white font-light">+</span>
    </div>
  </div>
</template>

<style scoped>
.community-dating-page {
  --dating-accent: #df4d94;
  --dating-accent-strong: #cf3f84;
  --dating-accent-muted: #725f70;
  --dating-accent-active-text: #fff;
  --dating-switch-bg: color-mix(in srgb, var(--c-dating) 3%, var(--c-surface));
  --dating-switch-border: color-mix(in srgb, var(--c-dating) 16%, var(--c-border));
}

.community-dating-switch {
  gap: 4px;
  height: auto;
  padding: 4px;
  border: 1px solid var(--dating-switch-border);
  border-radius: 18px;
  background: var(--dating-switch-bg);
  overflow: hidden;
}

.community-dating-switch__item {
  display: inline-flex;
  flex: 1;
  min-height: 44px;
  align-items: center;
  justify-content: center;
  padding: 0 12px;
  border: 0;
  border-radius: 14px;
  background: transparent;
  color: var(--dating-accent-muted) !important;
  cursor: pointer;
  font: inherit;
  font-size: 20px;
  font-weight: 850;
  line-height: 1;
  transition: background 0.18s ease, color 0.18s ease, box-shadow 0.18s ease, transform 0.18s ease;
}

.community-dating-switch__item:hover {
  color: var(--dating-accent-strong) !important;
  background: color-mix(in srgb, var(--c-dating) 8%, transparent);
}

.community-dating-switch__item--active {
  background: linear-gradient(135deg, var(--dating-accent-strong), var(--dating-accent));
  color: var(--dating-accent-active-text) !important;
  box-shadow: 0 10px 24px color-mix(in srgb, var(--dating-accent) 22%, transparent);
}

.community-dating-switch__item--active:hover {
  color: var(--dating-accent-active-text) !important;
  background: linear-gradient(135deg, var(--dating-accent-strong), var(--dating-accent));
}

.community-dating-card-title {
  color: var(--dating-accent-strong);
}

[data-theme="dark"] .community-dating-page {
  --dating-accent: color-mix(in srgb, var(--c-dating) 42%, #94a3b8);
  --dating-accent-strong: color-mix(in srgb, var(--c-dating) 48%, #dbeafe);
  --dating-accent-muted: color-mix(in srgb, var(--c-dating) 24%, var(--c-text-2));
  --dating-accent-active-text: var(--dating-accent-strong);
  --dating-switch-bg: rgba(24, 38, 53, 0.76);
  --dating-switch-border: rgba(68, 89, 112, 0.72);
}

[data-theme="dark"] .community-dating-switch__item:hover {
  background: rgba(32, 48, 68, 0.62);
}

[data-theme="dark"] .community-dating-switch__item--active,
[data-theme="dark"] .community-dating-switch__item--active:hover {
  background: linear-gradient(135deg, rgba(35, 51, 70, 0.98), rgba(28, 43, 61, 0.96));
  color: var(--dating-accent-strong) !important;
  box-shadow: inset 0 0 0 1px color-mix(in srgb, var(--c-dating) 18%, rgba(111, 132, 156, 0.72));
}

@media (min-width: 768px) {
  .community-dating-switch {
    margin: 16px;
  }

  .community-dating-switch__item {
    min-height: 48px;
    font-size: 15px;
  }
}
</style>
