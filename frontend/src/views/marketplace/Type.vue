<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '../../utils/request'
import { useScrollLoad } from '../../composables/useScrollLoad'
import CommunityHeader from '../../components/community/CommunityHeader.vue'

const route = useRoute()
const router = useRouter()
const scrollContainer = ref(null)

const TYPE_NAMES = ['校园代步', '手机', '电脑', '数码配件', '数码', '电器', '运动健身', '衣物伞帽', '图书教材', '租赁', '生活娱乐', '其他']
const PAGE_SIZE = 10

const typeId = computed(() => {
  const t = route.query.type
  const num = parseInt(t, 10)
  return isNaN(num) || num < 0 || num > 11 ? null : num
})

const typeName = computed(() => {
  if (typeId.value === null) return '分类'
  return TYPE_NAMES[typeId.value] ?? '分类'
})

function mapErshouItemToCard(item) {
  return {
    id: item.id,
    title: item.name,
    desc: item.description,
    price: item.price,
    coverImg: Array.isArray(item.pictureURL) && item.pictureURL.length > 0 ? item.pictureURL[0] : ''
  }
}

const fetchTypeData = async (page) => {
  if (typeId.value === null) {
    return { list: [], hasMore: false }
  }
  const start = (page - 1) * PAGE_SIZE
  const res = await request.get(`/marketplace/item/type/${typeId.value}/start/${start}`)
  const rawList = res?.data || []
  const list = Array.isArray(rawList) ? rawList.map(mapErshouItemToCard) : []
  return {
    list,
    hasMore: list.length >= PAGE_SIZE
  }
}

const { items: list, loading, finished, refreshing, pullY, loadData, handleScroll, handleTouchStart, handleTouchMove, handleTouchEnd } = useScrollLoad(fetchTypeData)

function goDetail(id) {
  router.push(`/marketplace/detail/${id}`)
}

onMounted(() => {
  loadData(true)
})

watch(
  () => route.query.type,
  () => {
    loadData(true)
  }
)
</script>

<template>
  <div class="min-h-screen bg-[var(--c-bg)] pb-5">
    <CommunityHeader :title="typeName" moduleColor="#10b981" :showBack="true" @back="router.back()" backTo="" />

    <!-- 滚动容器 -->
    <div
      class="h-[calc(100vh-51px)] overflow-y-auto overscroll-y-contain"
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
          <i class="w-5 h-5 border-2 border-[var(--c-border)] border-t-emerald-500 rounded-full animate-spin"></i> 正在刷新...
        </span>
        <span v-else-if="pullY > 50">释放立即刷新</span>
        <span v-else-if="pullY > 0">下拉刷新</span>
      </div>

      <!-- 商品双列网格 -->
      <div v-if="list.length > 0" class="grid grid-cols-2 gap-2.5 p-2.5">
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

      <!-- 空状态 -->
      <div v-if="!loading && !refreshing && list.length === 0" class="flex flex-col items-center py-16 text-[var(--c-text-3)]">
        <div class="text-3xl mb-3">?</div>
        <p class="text-sm">暂无该分类的商品</p>
      </div>

      <!-- 上拉加载更多 -->
      <div v-if="loading && !refreshing" class="flex items-center justify-center gap-2 py-4 text-sm text-[var(--c-text-3)]">
        <i class="w-5 h-5 border-2 border-[var(--c-border)] border-t-emerald-500 rounded-full animate-spin"></i>
        <span>正在加载</span>
      </div>
      <div v-if="finished && list.length > 0" class="flex items-center justify-center py-4 text-sm text-[var(--c-text-3)]">
        <span>没有更多了</span>
      </div>
    </div>
  </div>
</template>
