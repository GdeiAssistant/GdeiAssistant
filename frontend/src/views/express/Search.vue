<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '../../utils/request'
import { useScrollLoad } from '../../composables/useScrollLoad'
import CommunityHeader from '../../components/community/CommunityHeader.vue'

const route = useRoute()
const router = useRouter()
const keywordInput = ref('')
const dialogVisible = ref(false)
const dialogMessage = ref('')
const scrollContainer = ref(null)
const PAGE_SIZE = 10

function showDialog(msg) {
  dialogMessage.value = msg
  dialogVisible.value = true
}

const keyword = computed(() => route.query.keyword ?? '')

const fetchSearchData = async (page) => {
  const k = (route.query.keyword ?? '').trim()
  if (k === '') {
    return { list: [], hasMore: false }
  }
  const start = (page - 1) * PAGE_SIZE
  const res = await request.get(`/express/keyword/${encodeURIComponent(k)}/start/${start}/size/${PAGE_SIZE}`)
  const rawList = res?.data || []
  const list = Array.isArray(rawList) ? rawList.map((item) => ({
    id: item.id,
    content: item.content,
    senderName: item.nickname,
    receiverName: item.name,
    senderGender: item.selfGender === 0 ? 'male' : item.selfGender === 1 ? 'female' : 'secret',
    receiverGender: item.personGender === 0 ? 'male' : item.personGender === 1 ? 'female' : 'secret',
    time: item.publishTime,
    likeCount: item.likeCount ?? 0,
    commentCount: item.commentCount ?? 0,
    isLiked: item.liked === true,
    canGuess: item.canGuess === true,
    guessCount: item.guessSum ?? 0
  })) : []
  return { list, hasMore: list.length >= PAGE_SIZE }
}

const { items: list, loading, finished, refreshing, pullY, loadData, handleScroll, handleTouchStart, handleTouchMove, handleTouchEnd } = useScrollLoad(fetchSearchData)

function getGenderColor(gender) {
  if (gender === 'male') return '#4fc3f7'
  if (gender === 'female') return '#ff8a80'
  return 'var(--c-text-1)'
}

function doSearch() {
  if (!keywordInput.value || keywordInput.value.trim() === '') {
    showDialog('请输入搜索关键词')
    return
  }
  const k = keywordInput.value.trim()
  router.push({ path: '/express/search', query: { keyword: k } })
}

function handleLike(item) {
  if (item.isLiked) return
  request.post(`/express/id/${item.id}/like`).then(() => {
    item.isLiked = true
    item.likeCount = (item.likeCount || 0) + 1
  })
}

function handleGuess(item) {
  router.push(`/express/detail/${item.id}`)
}

function handleComment(item) {
  router.push(`/express/detail/${item.id}`)
}

onMounted(() => {
  keywordInput.value = keyword.value
  if (keyword.value) {
    loadData(true)
  }
})

watch(
  keyword,
  (val) => {
    keywordInput.value = val
    if (val) {
      loadData(true)
    }
  },
  { immediate: false }
)
</script>

<template>
  <div class="min-h-screen bg-[var(--c-bg)]">
    <CommunityHeader title="搜索表白" moduleColor="#f43f5e" backTo="/express/home" />

    <!-- 搜索栏 -->
    <div class="flex items-center px-4 py-2.5 bg-[var(--c-bg)]">
      <div class="flex-1 flex items-center bg-[var(--c-surface)] rounded-full px-3 py-2 shadow-sm">
        <svg class="shrink-0 mr-2" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="#9ca3af" width="16" height="16">
          <path d="M15.5 14h-.79l-.28-.27C15.41 12.59 16 11.11 16 9.5 16 5.91 13.09 3 9.5 3S3 5.91 3 9.5 5.91 16 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z"/>
        </svg>
        <input
          type="text"
          class="flex-1 border-none outline-none text-sm bg-transparent min-w-0 text-[var(--c-text-1)]"
          placeholder="搜索发件人/收件人/内容"
          v-model="keywordInput"
          @keyup.enter="doSearch"
        />
      </div>
      <span class="text-[#f43f5e] text-sm ml-4 whitespace-nowrap cursor-pointer font-medium" @click="doSearch">搜索</span>
    </div>

    <!-- 浅粉色标题 -->
    <h2 class="text-center text-xl font-bold text-[#ffb3ba] mx-4 mt-3 mb-4 leading-tight">广东第二师范学院表白墙</h2>

    <!-- 滚动容器 -->
    <div
      class="h-[calc(100vh-180px)] overflow-y-auto [-webkit-overflow-scrolling:touch] overscroll-y-contain"
      ref="scrollContainer"
      @scroll="handleScroll"
      @touchstart="handleTouchStart"
      @touchmove="handleTouchMove($event, scrollContainer)"
      @touchend="handleTouchEnd"
    >
      <div class="flex items-center justify-center overflow-hidden text-xs text-[var(--c-text-3)]" :style="{ height: pullY + 'px' }">
        <span v-if="refreshing" class="flex items-center gap-2">
          <i class="w-5 h-5 border-2 border-[var(--c-border)] border-t-[#f43f5e] rounded-full animate-spin"></i> 正在刷新...
        </span>
        <span v-else-if="pullY > 50">释放立即刷新</span>
        <span v-else-if="pullY > 0">下拉刷新</span>
      </div>

      <div class="px-4 pb-5">
        <div
          v-for="(item, index) in list"
          :key="item.id"
          class="bg-[var(--c-surface)] rounded-xl shadow-sm mb-3 overflow-hidden animate-[community-slide-up_0.4s_ease_both]"
          :style="{ animationDelay: (index % 10) * 0.05 + 's' }"
        >
          <div class="p-4 pb-3">
            <p class="mb-2.5 text-base leading-relaxed">
              <span class="border-b-2 border-dashed" :style="{ borderColor: getGenderColor(item.senderGender), color: getGenderColor(item.senderGender) }">{{ item.senderName }}</span>
              <span class="mx-1.5 text-[#f43f5e] font-bold">≡❤</span>
              <span class="border-b-2 border-dashed" :style="{ borderColor: getGenderColor(item.receiverGender), color: getGenderColor(item.receiverGender) }">{{ item.receiverName }}</span>
            </p>
            <p class="text-sm text-[var(--c-text-1)] leading-relaxed break-words mb-1">{{ item.content }}</p>
            <p class="text-xs text-[var(--c-text-3)]">{{ item.time }}</p>
          </div>
          <div class="flex border-t border-[var(--c-border)]">
            <button type="button" class="flex-1 flex items-center justify-center gap-1 py-2.5 bg-transparent border-none border-r border-[var(--c-border)] text-sm text-[var(--c-text-2)] cursor-pointer" @click="handleLike(item)">
              <span class="text-base">♡</span>
              <span>{{ item.likeCount || 0 }}</span>
            </button>
            <button type="button" class="flex-1 flex items-center justify-center gap-1 py-2.5 bg-transparent border-none border-r border-[var(--c-border)] text-sm text-[var(--c-text-2)] cursor-pointer" @click="handleGuess(item)">
              <span class="text-base">☆</span>
              <span>{{ item.guessCount || 0 }}</span>
            </button>
            <button type="button" class="flex-1 flex items-center justify-center gap-1 py-2.5 bg-transparent border-none text-sm text-[var(--c-text-2)] cursor-pointer" @click="handleComment(item)">
              <span class="text-base">💬</span>
              <span>{{ item.commentCount || 0 }}</span>
            </button>
          </div>
        </div>
      </div>

      <div class="text-center text-xs text-[var(--c-text-3)] px-4 pt-4 pb-5 leading-relaxed">
        蓝色下划线：男生 / 红色下划线：女生 / 黑色下划线：其他或保密
      </div>

      <div v-if="!loading && !refreshing && list.length === 0" class="flex flex-col items-center py-16 text-[var(--c-text-3)]">
        <div class="text-5xl mb-3">🔍</div>
        <p class="text-sm">{{ keyword ? '未找到相关表白' : '输入关键词搜索' }}</p>
      </div>

      <div v-if="loading && !refreshing" class="flex items-center justify-center gap-2 py-4 text-sm text-[var(--c-text-3)]">
        <i class="w-5 h-5 border-2 border-[var(--c-border)] border-t-[#f43f5e] rounded-full animate-spin"></i>
        <span>正在加载</span>
      </div>
      <div v-if="finished && list.length > 0" class="flex items-center justify-center py-4 text-sm text-[var(--c-text-3)]">
        <span>没有更多了</span>
      </div>
    </div>

    <!-- 提示 Dialog -->
    <div v-if="dialogVisible">
      <div class="fixed inset-0 bg-black/50 z-[1000]" @click="dialogVisible = false"></div>
      <div class="fixed top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[80%] max-w-[320px] bg-[var(--c-surface)] rounded-xl z-[1001] overflow-hidden">
        <div class="text-center font-semibold text-base text-[var(--c-text-1)] py-4">提示</div>
        <div class="px-5 pb-4 text-sm text-[var(--c-text-1)] text-center">{{ dialogMessage }}</div>
        <div class="flex border-t border-[var(--c-border)]">
          <button class="flex-1 py-3 text-center text-sm text-[#f43f5e] font-semibold bg-transparent border-none cursor-pointer" @click="dialogVisible = false">确定</button>
        </div>
      </div>
    </div>
  </div>
</template>
