<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '../../utils/request'
import { useScrollLoad } from '../../composables/useScrollLoad'
import { showErrorTopTips } from '@/utils/toast.js'
import CommunityHeader from '../../components/community/CommunityHeader.vue'

const router = useRouter()
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

function getGenderClass(gender) {
  if (gender === 'male') return 'gender-male'
  if (gender === 'female') return 'gender-female'
  return 'gender-secret'
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
  const weui = typeof window !== 'undefined' && window.weui
  if (weui && typeof weui.toast === 'function') weui.toast(msg, { duration: 2000 })
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
    class="express-home"
    @touchstart="handleTouchStart"
    @touchmove="handleTouchMove($event, scrollContainer)"
    @touchend="handleTouchEnd"
  >
    <CommunityHeader title="表白墙" moduleColor="#f43f5e" />

    <!-- 导航栏正下方：居中的浅粉色粗体标题 -->
    <h2 class="express-main-title">广东第二师范学院表白墙</h2>

    <!-- 下拉刷新指示器 -->
    <div class="community-pull-refresh" :style="{ height: pullY + 'px' }">
      <span v-if="refreshing" class="community-pull-refresh__text">
        <i class="community-loading-spinner"></i> 正在刷新...
      </span>
      <span v-else-if="pullY > 50" class="community-pull-refresh__text">释放立即刷新</span>
      <span v-else-if="pullY > 0" class="community-pull-refresh__text">下拉刷新</span>
    </div>

    <!-- 表白卡片列表 -->
    <div class="express-list">
      <div
        v-for="(item, index) in list"
        :key="item.id"
        class="community-card express-card"
        :style="{ animationDelay: (index % 10) * 0.05 + 's' }"
        @click="goToDetail(item.id)"
      >
        <div class="card-header">
          <span :class="getGenderClass(item.senderGender)">{{ item.senderName }}</span>
          <span class="card-connector"> ≡❤ </span>
          <span :class="getGenderClass(item.receiverGender)">{{ item.receiverName }}</span>
        </div>

        <div class="card-body">
          {{ item.content }}
        </div>

        <div class="card-time">
          {{ item.time || '刚刚' }}
        </div>

        <div class="card-actions">
          <button type="button" class="action-btn" :class="{ 'is-liked': item.isLiked }" @click.stop="handleLike(item)">
            {{ item.isLiked ? '♥' : '♡' }} {{ item.likeCount || 0 }}
          </button>
          <button type="button" class="action-btn" :style="{ opacity: item.canGuess ? 1 : 0.4 }" @click.stop="handleGuess(item)">
            <span style="margin-right: 4px; position: relative;">☆<sup style="font-size: 10px; position: absolute; top: -4px; right: -6px;">?</sup></span> {{ item.guessCount || 0 }}/{{ item.correctCount || 0 }}
          </button>
          <button type="button" class="action-btn" @click.stop="handleComment(item)">
            💬 {{ item.commentCount || 0 }}
          </button>
        </div>
      </div>
    </div>

    <!-- 底部图例 -->
    <div class="express-legend">
      蓝色下划线：男生 / 红色下划线：女生 / 黑色下划线：其他或保密
    </div>

    <!-- 空状态 -->
    <div v-if="!loading && !refreshing && list.length === 0" class="community-empty">
      <div class="community-empty__icon">💌</div>
      <p class="community-empty__text">暂无表白墙内容</p>
    </div>

    <!-- 上拉加载更多 -->
    <div v-if="loading && !refreshing" class="community-loadmore">
      <i class="community-loading-spinner"></i>
      <span>正在加载</span>
    </div>
    <div v-if="finished && list.length > 0" class="community-loadmore">
      <span>没有更多了</span>
    </div>

    <!-- 猜名字 Dialog -->
    <div v-if="guessDialogVisible" class="community-dialog-mask" @click="guessDialogVisible = false"></div>
    <div v-if="guessDialogVisible" class="community-dialog">
      <div class="community-dialog__title">猜名字</div>
      <div class="community-dialog__body">
        <input
          type="text"
          class="express-dialog-input"
          placeholder="请输入你猜的真实姓名："
          v-model="guessInputValue"
          @keyup.enter="confirmGuess"
        />
      </div>
      <div class="community-dialog__footer">
        <button class="community-dialog__btn community-dialog__btn--cancel" @click="guessDialogVisible = false">取消</button>
        <button class="community-dialog__btn community-dialog__btn--confirm" @click="confirmGuess">确定</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.express-home {
  background: var(--c-bg);
  padding-bottom: 20px;
}

/* 浅粉色粗体标题 */
.express-main-title {
  text-align: center;
  font-size: 22px;
  font-weight: bold;
  color: #ffb3ba;
  margin: var(--space-lg) var(--space-lg) var(--space-xl);
  padding: 0;
  line-height: 1.3;
}

/* 性别虚线样式（核心灵魂） */
.gender-male {
  border-bottom: 2px dashed #4fc3f7;
  color: #4fc3f7;
}
.gender-female {
  border-bottom: 2px dashed #ff8a80;
  color: #ff8a80;
}
.gender-secret {
  border-bottom: 2px dashed var(--c-text-1);
  color: var(--c-text-1);
}

/* 表白卡片 */
.express-list {
  padding: 0 0 var(--space-xl);
}

.express-card {
  margin: var(--space-lg);
  overflow: hidden;
  animation: community-slide-up 0.4s ease both;
}

.card-header {
  text-align: center;
  font-size: 18px;
  padding: 20px var(--space-lg) 10px;
  line-height: 1.5;
}

.card-connector {
  color: var(--c-text-2);
  margin: 0 5px;
}

.card-body {
  text-align: center;
  font-size: var(--font-lg);
  padding: 10px 20px 20px;
  color: var(--c-text-1);
}

.card-time {
  text-align: right;
  font-size: var(--font-sm);
  color: var(--c-text-3);
  padding: 0 var(--space-lg) var(--space-lg);
}

.card-actions {
  display: flex;
  border-top: 1px solid var(--c-border);
  padding: 10px 0;
}

/* 操作栏按钮 */
.action-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 2px;
  padding: 10px 0;
  background: transparent;
  border: none;
  border-right: 1px solid var(--c-border);
  font-size: var(--font-base);
  color: var(--c-text-2);
  cursor: pointer;
}
.action-btn:last-child {
  border-right: none;
}
.action-btn.is-liked {
  color: #f43f5e !important;
  font-weight: bold;
}

/* 底部图例 */
.express-legend {
  text-align: center;
  font-size: var(--font-sm);
  color: var(--c-text-3);
  padding: var(--space-lg) var(--space-lg) var(--space-xl);
  line-height: 1.5;
}

/* 猜名字 Dialog 输入框 */
.express-dialog-input {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid var(--c-divider);
  border-radius: var(--radius-sm);
  font-size: var(--font-base);
  box-sizing: border-box;
  outline: none;
}
.express-dialog-input:focus {
  border-color: #f43f5e;
}
</style>
