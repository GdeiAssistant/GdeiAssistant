<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '../../utils/request'
import { useScrollLoad } from '../../composables/useScrollLoad'
import CommunityHeader from '../../components/community/CommunityHeader.vue'

const router = useRouter()
const activeArea = ref(0) // 0 小姐姐 1 小哥哥

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
    content: p.content
  })) : []
  return { list, hasMore: list.length >= PAGE_SIZE }
}

const { items: list, loading, finished, refreshing, pullY, loadData, handleTouchStart, handleTouchMove, handleTouchEnd } = useScrollLoad(fetchDatingData)
const scrollContainer = ref({ get scrollTop() { return window.pageYOffset || document.documentElement.scrollTop } })

function switchArea(index) {
  activeArea.value = index
  list.value = []
  loadData(true)
}

function goDetail(id) {
  router.push(`/dating/detail/${id}`)
}


function getGradeText(grade) {
  const map = { 1: '大一', 2: '大二', 3: '大三', 4: '大四' }
  return map[grade] || '未知'
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
    class="min-h-screen bg-[var(--c-bg)] pb-20"
    @touchstart="handleTouchStart"
    @touchmove="handleTouchMove($event, scrollContainer)"
    @touchend="handleTouchEnd"
  >
    <CommunityHeader title="卖室友" moduleColor="#ec4899" backTo="/">
      <template #right>
        <span class="text-[var(--c-text-2)] cursor-pointer text-base" @click="router.push('/dating/center')">我的</span>
      </template>
    </CommunityHeader>

    <!-- Pull refresh -->
    <div class="flex items-center justify-center overflow-hidden text-sm text-[var(--c-text-3)]" :style="{ height: pullY + 'px' }">
      <span v-if="refreshing" class="flex items-center gap-2"><i class="w-5 h-5 border-2 border-[var(--c-border)] border-t-pink-500 rounded-full animate-spin"></i> 正在刷新...</span>
      <span v-else-if="pullY > 50">释放立即刷新</span>
      <span v-else-if="pullY > 0">下拉刷新</span>
    </div>

    <!-- Gender Tab -->
    <div class="flex bg-[var(--c-card)] mt-2 h-11">
      <a
        href="javascript:;"
        class="flex-1 text-center leading-[44px] text-xl transition-colors duration-300"
        :class="activeArea === 0 ? 'bg-pink-500 text-white' : 'text-[var(--c-text-2)]'"
        @click.prevent="switchArea(0)"
      >小姐姐</a>
      <a
        href="javascript:;"
        class="flex-1 text-center leading-[44px] text-xl transition-colors duration-300"
        :class="activeArea === 1 ? 'bg-pink-500 text-white' : 'text-[var(--c-text-2)]'"
        @click.prevent="switchArea(1)"
      >小哥哥</a>
    </div>

    <!-- Card list -->
    <div class="p-4">
      <div
        v-for="(item, index) in list"
        :key="item.id"
        class="bg-[var(--c-surface)] rounded-xl shadow-sm mb-4 p-4 overflow-hidden cursor-pointer animate-[slide-up_0.4s_ease_both]"
        :style="{ animationDelay: (index % 10) * 0.05 + 's' }"
        @click="goDetail(item.id)"
      >
        <div class="w-full aspect-square bg-[var(--c-border)] rounded-lg overflow-hidden mb-3">
          <img :src="(item.images && item.images[0]) || item.image || '/img/dating/default-avatar.png'" :alt="item.name" class="w-full h-full object-cover" />
        </div>
        <h2 class="m-0 mb-1.5 text-xl font-bold text-pink-500 leading-tight">{{ item.name }}</h2>
        <div class="text-[var(--c-text-2)] text-base mt-0.5 leading-relaxed">{{ item.faculty }} {{ getGradeText(item.grade) }}学生</div>
        <div class="text-[var(--c-text-2)] text-base mt-0.5 leading-relaxed">来自{{ item.hometown || '未知' }}</div>
        <p class="text-[var(--c-text-2)] text-sm mt-2 leading-relaxed">{{ item.content }}</p>
      </div>
    </div>

    <!-- Empty -->
    <div v-if="!loading && !refreshing && list.length === 0" class="flex flex-col items-center py-16 text-[var(--c-text-3)]">
      <div class="text-4xl mb-2">💕</div>
      <div class="text-sm">暂无内容</div>
    </div>

    <!-- Loading -->
    <div v-if="loading && !refreshing" class="flex items-center justify-center gap-2 py-4 text-sm text-[var(--c-text-3)]">
      <i class="w-5 h-5 border-2 border-[var(--c-border)] border-t-pink-500 rounded-full animate-spin"></i> 正在加载
    </div>
    <div v-if="finished && list.length > 0" class="text-center py-4 text-sm text-[var(--c-text-3)]">已经到底了</div>

    <!-- FAB -->
    <div
      class="fixed right-5 bottom-6 w-12 h-12 rounded-full bg-pink-500 shadow-[0_4px_12px_rgba(236,72,153,0.4)] flex items-center justify-center z-[100] cursor-pointer transition-transform active:scale-[0.92]"
      @click="router.push('/dating/publish')"
    >
      <span class="text-[28px] leading-none text-white font-light">+</span>
    </div>
  </div>
</template>
