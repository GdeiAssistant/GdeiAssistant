<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import request from '../../utils/request'
import { useScrollLoad } from '../../composables/useScrollLoad'
import { useToast } from '../../composables/useToast'
import CommunityHeader from '../../components/community/CommunityHeader.vue'
import { createCommunityPullMessages, createMarketplaceCategoryNames } from '../community/communityContent'
import {
  Bike, Smartphone, Laptop, Cable, Camera, Zap, Dumbbell,
  Shirt, BookOpen, Key, Gamepad2, MoreHorizontal
} from 'lucide-vue-next'

const router = useRouter()
const { t } = useI18n()
const keyword = ref('')
const scrollContainer = ref(null)
const { toast } = useToast()

const PAGE_SIZE = 10

function mapErshouItemToCard(item) {
  return {
    id: item.id,
    title: item.name,
    desc: item.description,
    price: item.price,
    coverImg: Array.isArray(item.pictureURL) && item.pictureURL.length > 0 ? item.pictureURL[0] : ''
  }
}

const fetchHomeData = async (page) => {
  const start = (page - 1) * PAGE_SIZE
  const res = await request.get(`/marketplace/item/start/${start}`)
  const rawList = res?.data || []
  const list = Array.isArray(rawList) ? rawList.map(mapErshouItemToCard) : []
  return {
    list,
    hasMore: list.length >= PAGE_SIZE
  }
}

const { items: list, loading, finished, refreshing, pullY, loadData, handleScroll, handleTouchStart, handleTouchMove, handleTouchEnd } = useScrollLoad(fetchHomeData)
const pullMessages = computed(() => createCommunityPullMessages(t))
const categoryNames = computed(() => createMarketplaceCategoryNames(t))

const categories = [
  { icon: Bike, typeId: 0 },
  { icon: Smartphone, typeId: 1 },
  { icon: Laptop, typeId: 2 },
  { icon: Cable, typeId: 3 },
  { icon: Camera, typeId: 4 },
  { icon: Zap, typeId: 5 },
  { icon: Dumbbell, typeId: 6 },
  { icon: Shirt, typeId: 7 },
  { icon: BookOpen, typeId: 8 },
  { icon: Key, typeId: 9 },
  { icon: Gamepad2, typeId: 10 },
  { icon: MoreHorizontal, typeId: 11 }
]

function doSearch() {
  if (!keyword.value || keyword.value.trim() === '') {
    toast({ message: t('marketplace.searchRequired') })
    return
  }
  const k = keyword.value.trim()
  router.push({ path: '/marketplace/search', query: { keyword: k } })
}

function goType(typeId) {
  router.push({ path: '/marketplace/type', query: { type: typeId } })
}

function goDetail(id) {
  router.push(`/marketplace/detail/${id}`)
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="bg-[var(--c-bg)] min-h-screen pb-5">
    <!-- 统一顶部导航栏 -->
    <CommunityHeader :title="t('marketplace.title')" moduleColor="#10b981" />

    <!-- 搜索栏 -->
    <div class="flex items-center px-4 py-2.5 bg-[var(--c-bg)]">
      <div class="flex-1 flex items-center bg-[var(--c-surface)] rounded-full px-3 py-1.5 shadow-sm">
        <i class="inline-block w-4 h-4 shrink-0 mr-1.5 bg-[url('data:image/svg+xml,%3Csvg%20xmlns=%27http://www.w3.org/2000/svg%27%20viewBox=%270%200%2024%2024%27%20fill=%27%239ca3af%27%3E%3Cpath%20d=%27M15.5%2014h-.79l-.28-.27C15.41%2012.59%2016%2011.11%2016%209.5%2016%205.91%2013.09%203%209.5%203S3%205.91%203%209.5%205.91%2016%209.5%2016c1.61%200%203.09-.59%204.23-1.57l.27.28v.79l5%204.99L20.49%2019l-4.99-5zm-6%200C7.01%2014%205%2011.99%205%209.5S7.01%205%209.5%205%2014%207.01%2014%209.5%2011.99%2014%209.5%2014z%27/%3E%3C/svg%3E')] bg-center bg-contain bg-no-repeat"></i>
        <input type="text" :placeholder="t('marketplace.searchPlaceholder')" v-model="keyword" @keyup.enter="doSearch" class="flex-1 border-none outline-none text-sm bg-transparent min-w-0 text-[var(--c-text-1)]" />
      </div>
      <span class="text-emerald-500 text-[15px] ml-4 whitespace-nowrap cursor-pointer font-medium" @click="doSearch">{{ t('marketplace.searchAction') }}</span>
    </div>

    <!-- 分类宫格 -->
    <div class="grid! grid-cols-4 w-[calc(100%-20px)] mx-2.5 mb-4 bg-[var(--c-surface)] rounded-lg overflow-hidden shadow-sm border border-emerald-500/10">
      <div
        v-for="(cat, index) in categories"
        :key="cat.typeId"
        class="flex flex-col items-center justify-center py-3 min-h-[50px] bg-[var(--c-surface)] cursor-pointer text-center transition-colors active:bg-[var(--c-bg)]"
        @click="goType(cat.typeId)"
      >
        <component :is="cat.icon" class="w-[22px] h-[22px] mb-1.5 shrink-0 text-emerald-600" :stroke-width="1.5" />
        <span class="text-[var(--c-text-2)] block leading-3 text-xs">{{ categoryNames[cat.typeId] }}</span>
      </div>
    </div>

    <!-- 滚动容器：下拉刷新 + 上拉加载 -->
    <div
      class="h-[calc(100vh-200px)] overflow-y-auto overscroll-y-contain"
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
          <i class="w-5 h-5 border-2 border-[var(--c-border)] border-t-emerald-500 rounded-full animate-spin"></i> {{ pullMessages.refreshing }}
        </span>
        <span v-else-if="pullY > 50">{{ pullMessages.releaseToRefresh }}</span>
        <span v-else-if="pullY > 0">{{ pullMessages.pullToRefresh }}</span>
      </div>

      <!-- 商品双列网格 -->
      <div class="grid grid-cols-2 gap-2.5 px-2.5 pb-5">
        <div
          v-for="item in list"
          :key="item.id"
          class="bg-[var(--c-surface)] rounded-xl shadow-sm transition-transform active:scale-[0.985] overflow-hidden cursor-pointer"
          @click="goDetail(item.id)"
        >
          <div class="w-full aspect-square overflow-hidden bg-[var(--c-border)]">
            <img :src="item.coverImg" :alt="item.title" class="w-full h-full object-cover block" />
          </div>
          <h3 class="text-sm font-medium text-[var(--c-text-1)] mx-2 mt-2 p-0 line-clamp-2 leading-snug">{{ item.title }}</h3>
          <p class="text-xs text-[var(--c-text-3)] mx-2 mt-1 p-0 truncate">{{ item.desc }}</p>
          <em class="block text-base font-semibold text-[#e4393c] mx-2 mt-1.5 mb-2 p-0 not-italic">{{ item.price }}</em>
        </div>
      </div>

      <!-- 上拉加载更多 -->
      <div v-if="loading && !refreshing" class="flex items-center justify-center gap-2 py-4 text-sm text-[var(--c-text-3)]">
        <i class="w-5 h-5 border-2 border-[var(--c-border)] border-t-emerald-500 rounded-full animate-spin"></i>
        <span>{{ pullMessages.loading }}</span>
      </div>
      <div v-if="finished && list.length > 0" class="flex items-center justify-center py-4 text-sm text-[var(--c-text-3)]">
        <span>{{ pullMessages.noMore }}</span>
      </div>
    </div>
  </div>
</template>
