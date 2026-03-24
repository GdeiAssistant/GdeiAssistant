<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '../../utils/request'
import { useScrollLoad } from '../../composables/useScrollLoad'
import { useToast } from '@/composables/useToast'
import { showErrorTopTips } from '@/utils/toast.js'
import CommunityHeader from '../../components/community/CommunityHeader.vue'

const router = useRouter()
const { success: toastSuccess } = useToast()
// 用于下拉刷新：模拟 scrollTop 为 window 的滚动位置
const scrollContainer = ref({ get scrollTop() { return window.pageYOffset || document.documentElement.scrollTop } })

const PAGE_SIZE = 10
function mapGender(g) {
  if (g === 0) return 'male'
  if (g === 1) return 'female'
  return 'secret'
}
const fetchExpressData = async (page) => {
  const start = (page - 1) * PAGE_SIZE
  const res = await request.get(`/express/start/${start}/size/${PAGE_SIZE}`)
  const rawList = res?.data || []
  const list = Array.isArray(rawList) ? rawList.map((e) => ({
    id: e.id,
    content: e.content,
    senderName: e.nickname,
    receiverName: e.name,
    senderGender: mapGender(e.selfGender),
    receiverGender: mapGender(e.personGender),
    time: e.publishTime,
    likeCount: e.likeCount ?? 0,
    commentCount: e.commentCount ?? 0,
    isLiked: e.liked === true,
    canGuess: e.canGuess === true,
    guessCount: e.guessSum ?? 0,
    correctCount: e.guessCount ?? 0
  })) : []
  return { list, hasMore: list.length >= PAGE_SIZE }
}

const { items: list, loading, finished, refreshing, pullY, loadData, handleTouchStart, handleTouchMove, handleTouchEnd } = useScrollLoad(fetchExpressData)

function getGenderColor(gender) {
  if (gender === 'male') return '#4fc3f7'
  if (gender === 'female') return '#ff8a80'
  return 'var(--c-text-1)'
}

function handleLike(item) {
  if (item.isLiked) return
  request.post(`/express/id/${item.id}/like`).then(() => {
    item.isLiked = true
    item.likeCount = (item.likeCount || 0) + 1
  })
}

const guessDialogVisible = ref(false)
const guessInputValue = ref('')
const guessTargetItem = ref(null)

function openGuessDialog(item) {
  guessTargetItem.value = item
  guessInputValue.value = ''
  guessDialogVisible.value = true
}

function showSuccess(msg) {
  toastSuccess(msg)
}

function confirmGuess() {
  const guessName = guessInputValue.value && guessInputValue.value.trim()
  if (!guessName) {
    showErrorTopTips('请输入你猜的真实姓名')
    return
  }
  const currentItem = guessTargetItem.value
  if (!currentItem) {
    guessDialogVisible.value = false
    return
  }
  request.post(`/express/id/${currentItem.id}/guess`, null, { params: { name: guessName } })
    .then((res) => {
      const correct = res?.data === true
      if (correct) {
        currentItem.correctCount = (currentItem.correctCount || 0) + 1
        showSuccess('恭喜你，猜对了！')
      } else {
        showErrorTopTips('猜错了，再试试看吧！')
      }
      currentItem.guessCount = (currentItem.guessCount || 0) + 1
    })
    .catch(() => {})
  guessDialogVisible.value = false
  guessTargetItem.value = null
  guessInputValue.value = ''
}

function handleGuess(item) {
  // 拦截无效的猜测点击
  if (!item.canGuess) {
    showErrorTopTips('TA很神秘，没有留下真名让人猜哦~')
    return
  }
  openGuessDialog(item)
}

function handleComment(item) {
  router.push(`/express/detail/${item.id}`)
}

function goToDetail(id) {
  router.push(`/express/detail/${id}`)
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
    class="bg-[var(--c-bg)] pb-5"
    @touchstart="handleTouchStart"
    @touchmove="handleTouchMove($event, scrollContainer)"
    @touchend="handleTouchEnd"
  >
    <CommunityHeader title="表白墙" moduleColor="#f43f5e" />

    <!-- 导航栏正下方：居中的浅粉色粗体标题 -->
    <h2 class="text-center text-[22px] font-bold text-[#ffb3ba] mx-4 mt-4 mb-5 leading-tight">广东第二师范学院表白墙</h2>

    <!-- 下拉刷新指示器 -->
    <div class="flex items-center justify-center overflow-hidden text-xs text-[var(--c-text-3)]" :style="{ height: pullY + 'px' }">
      <span v-if="refreshing" class="flex items-center gap-2">
        <i class="w-5 h-5 border-2 border-[var(--c-border)] border-t-[#f43f5e] rounded-full animate-spin"></i> 正在刷新...
      </span>
      <span v-else-if="pullY > 50">释放立即刷新</span>
      <span v-else-if="pullY > 0">下拉刷新</span>
    </div>

    <!-- 表白卡片列表 -->
    <div class="pb-5">
      <div
        v-for="(item, index) in list"
        :key="item.id"
        class="bg-[var(--c-surface)] rounded-xl shadow-sm mx-4 mt-4 overflow-hidden animate-[community-slide-up_0.4s_ease_both]"
        :style="{ animationDelay: (index % 10) * 0.05 + 's' }"
        @click="goToDetail(item.id)"
      >
        <div class="text-center text-lg px-4 pt-5 pb-2.5 leading-relaxed">
          <span class="border-b-2 border-dashed" :style="{ borderColor: getGenderColor(item.senderGender), color: getGenderColor(item.senderGender) }">{{ item.senderName }}</span>
          <span class="text-[var(--c-text-2)] mx-1.5"> ≡❤ </span>
          <span class="border-b-2 border-dashed" :style="{ borderColor: getGenderColor(item.receiverGender), color: getGenderColor(item.receiverGender) }">{{ item.receiverName }}</span>
        </div>

        <div class="text-center text-base px-5 pt-2.5 pb-5 text-[var(--c-text-1)]">
          {{ item.content }}
        </div>

        <div class="text-right text-xs text-[var(--c-text-3)] px-4 pb-4">
          {{ item.time || '刚刚' }}
        </div>

        <div class="flex border-t border-[var(--c-border)] py-2.5">
          <button
            type="button"
            class="flex-1 flex items-center justify-center gap-0.5 py-2.5 bg-transparent border-none border-r border-[var(--c-border)] text-sm cursor-pointer"
            :class="item.isLiked ? 'text-[#f43f5e] font-bold' : 'text-[var(--c-text-2)]'"
            @click.stop="handleLike(item)"
          >
            {{ item.isLiked ? '♥' : '♡' }} {{ item.likeCount || 0 }}
          </button>
          <button
            type="button"
            class="flex-1 flex items-center justify-center gap-0.5 py-2.5 bg-transparent border-none border-r border-[var(--c-border)] text-sm text-[var(--c-text-2)] cursor-pointer"
            :style="{ opacity: item.canGuess ? 1 : 0.4 }"
            @click.stop="handleGuess(item)"
          >
            <span class="mr-1 relative">☆<sup class="text-[10px] absolute -top-1 -right-1.5">?</sup></span> {{ item.guessCount || 0 }}/{{ item.correctCount || 0 }}
          </button>
          <button
            type="button"
            class="flex-1 flex items-center justify-center gap-0.5 py-2.5 bg-transparent border-none text-sm text-[var(--c-text-2)] cursor-pointer"
            @click.stop="handleComment(item)"
          >
            💬 {{ item.commentCount || 0 }}
          </button>
        </div>
      </div>
    </div>

    <!-- 底部图例 -->
    <div class="text-center text-xs text-[var(--c-text-3)] px-4 pt-4 pb-5 leading-relaxed">
      蓝色下划线：男生 / 红色下划线：女生 / 黑色下划线：其他或保密
    </div>

    <!-- 空状态 -->
    <div v-if="!loading && !refreshing && list.length === 0" class="flex flex-col items-center py-16 text-[var(--c-text-3)]">
      <div class="text-5xl mb-3">💌</div>
      <p class="text-sm">暂无表白墙内容</p>
    </div>

    <!-- 上拉加载更多 -->
    <div v-if="loading && !refreshing" class="flex items-center justify-center gap-2 py-4 text-sm text-[var(--c-text-3)]">
      <i class="w-5 h-5 border-2 border-[var(--c-border)] border-t-[#f43f5e] rounded-full animate-spin"></i>
      <span>正在加载</span>
    </div>
    <div v-if="finished && list.length > 0" class="flex items-center justify-center py-4 text-sm text-[var(--c-text-3)]">
      <span>没有更多了</span>
    </div>

    <!-- 猜名字 Dialog -->
    <div v-if="guessDialogVisible" class="fixed inset-0 bg-black/50 z-[1000]" @click="guessDialogVisible = false"></div>
    <div v-if="guessDialogVisible" class="fixed top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[80%] max-w-[320px] bg-[var(--c-surface)] rounded-xl z-[1001] overflow-hidden">
      <div class="text-center font-semibold text-base text-[var(--c-text-1)] py-4">猜名字</div>
      <div class="px-5 pb-4">
        <input
          type="text"
          class="w-full px-3 py-2.5 border border-[var(--c-border)] rounded-lg text-sm bg-[var(--c-surface)] outline-none focus:border-[#f43f5e] focus:ring-2 focus:ring-[#f43f5e]/10"
          placeholder="请输入你猜的真实姓名："
          v-model="guessInputValue"
          @keyup.enter="confirmGuess"
        />
      </div>
      <div class="flex border-t border-[var(--c-border)]">
        <button class="flex-1 py-3 text-center text-sm text-[var(--c-text-2)] bg-transparent border-none border-r border-[var(--c-border)] cursor-pointer" @click="guessDialogVisible = false">取消</button>
        <button class="flex-1 py-3 text-center text-sm text-[#f43f5e] font-semibold bg-transparent border-none cursor-pointer" @click="confirmGuess">确定</button>
      </div>
    </div>
  </div>
</template>
