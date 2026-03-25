<script setup>
import { ref, onMounted, watch, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import request from '../../utils/request'
import { useScrollLoad } from '../../composables/useScrollLoad'
import CommunityHeader from '../../components/community/CommunityHeader.vue'
import { createCommunityPullMessages } from '../community/communityContent'

const router = useRouter()
const { t } = useI18n()
const activeType = ref(0) // 0: 寻物, 1: 招领
const scrollContainer = ref(null)
const PAGE_SIZE = 10
const pullMessages = computed(() => createCommunityPullMessages(t))

function mapItemToCard(item) {
  return {
    id: item.id,
    title: item.name,
    desc: item.description,
    type: item.lostType,
    images: Array.isArray(item.pictureURL) ? item.pictureURL : []
  }
}

const fetchLostData = async (page) => {
  const start = (page - 1) * PAGE_SIZE
  const path = activeType.value === 0
    ? `/lostandfound/lostitem/start/${start}`
    : `/lostandfound/founditem/start/${start}`
  const res = await request.get(path)
  const rawList = res?.data || []
  const list = Array.isArray(rawList) ? rawList.map(mapItemToCard) : []
  return { list, hasMore: list.length >= PAGE_SIZE }
}

const { items: list, loading, finished, refreshing, pullY, loadData, handleScroll, handleTouchStart, handleTouchMove, handleTouchEnd } = useScrollLoad(fetchLostData)

function switchType(type) {
  activeType.value = type
  loadData(true)
}

function goDetail(id) {
  router.push(`/lostandfound/detail/${id}`)
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="bg-[var(--c-bg)] min-h-screen">
    <!-- 统一顶部导航栏 -->
    <CommunityHeader :title="t('lostandfound.title')" moduleColor="#3b82f6" />

    <!-- Tab 切换：寻物启事 / 失物招领 -->
    <div class="flex bg-[var(--c-surface)] border-b border-[var(--c-border)]">
      <div
        class="flex-1 py-3.5 text-center text-[15px] cursor-pointer relative"
        :class="activeType === 0 ? 'text-blue-500' : 'text-[var(--c-text-2)]'"
        @click="switchType(0)"
      >
        <span>{{ t('lostandfound.tab.lost') }}</span>
        <span v-if="activeType === 0" class="absolute bottom-0 left-0 right-0 h-0.5 bg-blue-500"></span>
      </div>
      <div
        class="flex-1 py-3.5 text-center text-[15px] cursor-pointer relative"
        :class="activeType === 1 ? 'text-blue-500' : 'text-[var(--c-text-2)]'"
        @click="switchType(1)"
      >
        <span>{{ t('lostandfound.tab.found') }}</span>
        <span v-if="activeType === 1" class="absolute bottom-0 left-0 right-0 h-0.5 bg-blue-500"></span>
      </div>
    </div>

    <!-- 滚动容器：下拉刷新 + 上拉加载 -->
    <div
      class="h-[calc(100vh-95px)] overflow-y-auto overscroll-y-contain"
      style="-webkit-overflow-scrolling: touch"
      ref="scrollContainer"
      @scroll="handleScroll"
      @touchstart="handleTouchStart"
      @touchmove="handleTouchMove($event, scrollContainer)"
      @touchend="handleTouchEnd"
    >
      <!-- 下拉刷新指示器 -->
      <div class="flex items-center justify-center overflow-hidden text-sm text-[var(--c-text-3)]" :style="{ height: pullY + 'px' }">
        <span v-if="refreshing" class="flex items-center gap-2">
          <span class="w-5 h-5 border-2 border-[var(--c-border)] border-t-blue-500 rounded-full animate-spin"></span> {{ pullMessages.refreshing }}
        </span>
        <span v-else-if="pullY > 50">{{ pullMessages.releaseToRefresh }}</span>
        <span v-else-if="pullY > 0">{{ pullMessages.pullToRefresh }}</span>
      </div>

      <!-- 失物招领列表（双列网格） -->
      <div class="mx-auto w-full flex flex-wrap px-[2%] mt-2.5">
        <div
          v-for="item in list"
          :key="item.id"
          class="inline-block w-[46.5%] relative mx-[1%] my-1 bg-[var(--c-surface)] rounded-xl shadow-sm transition-transform active:scale-[0.985] overflow-hidden cursor-pointer"
          @click="goDetail(item.id)"
        >
          <div class="w-full h-[170px] relative overflow-hidden bg-[var(--c-border)]">
            <img v-if="item.images && item.images.length > 0" :src="item.images[0]" :alt="item.title" class="w-full h-full object-cover" />
            <div v-else class="w-full h-full bg-[var(--c-border)]"></div>
            <div
              class="absolute top-2 right-2 px-2 py-0.5 text-[11px] text-white rounded z-[1]"
              :class="item.type === 1 ? 'bg-green-500' : 'bg-amber-500'"
            >
              {{ item.type === 0 ? t('lostandfound.badge.lost') : t('lostandfound.badge.found') }}
            </div>
          </div>
          <div class="p-2">
            <h5 class="text-sm font-medium text-[var(--c-text-1)] m-0 mb-1 p-0 line-clamp-2 leading-snug">{{ item.title }}</h5>
            <p class="text-xs text-[var(--c-text-3)] m-0 p-0 line-clamp-2 leading-snug">{{ item.desc }}</p>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-if="!loading && !refreshing && list.length === 0" class="flex flex-col items-center py-16 text-[var(--c-text-3)]">
        <div class="text-3xl mb-3">&#x1f4ed;</div>
        <p class="text-sm">{{ activeType === 0 ? t('lostandfound.emptyLost') : t('lostandfound.emptyFound') }}</p>
      </div>

      <!-- 上拉加载更多 -->
      <div v-if="loading && !refreshing" class="flex items-center justify-center gap-2 py-4 text-sm text-[var(--c-text-3)]">
        <span class="w-5 h-5 border-2 border-[var(--c-border)] border-t-blue-500 rounded-full animate-spin"></span>
        <span>{{ pullMessages.loading }}</span>
      </div>
      <div v-if="finished && list.length > 0" class="flex items-center justify-center py-4 text-sm text-[var(--c-text-3)]">
        <span>{{ pullMessages.noMore }}</span>
      </div>
    </div>
  </div>
</template>
