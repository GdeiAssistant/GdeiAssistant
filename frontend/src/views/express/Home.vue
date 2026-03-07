<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '../../utils/request'
import { useScrollLoad } from '../../composables/useScrollLoad'
import { showErrorTopTips } from '@/utils/toast.js'

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
    class="dating-home"
    @touchstart="handleTouchStart"
    @touchmove="handleTouchMove($event, scrollContainer)"
    @touchend="handleTouchEnd"
  >
    <!-- 标准 .unified-header：左侧返回，中间不写字 -->
    <div class="dating-header unified-header">
      <span class="dating-header__back" @click="router.push('/')">返回</span>
      <h1 class="dating-header__title"></h1>
      <span class="dating-header__placeholder"></span>
    </div>

    <!-- 导航栏正下方：居中的巨大浅粉色粗体标题 -->
    <h2 class="dating-main-title">广东第二师范学院表白墙</h2>

    <!-- 下拉刷新指示器（顶部，不产生滚动） -->
    <div class="pull-refresh-indicator" :style="{ height: pullY + 'px' }">
      <span v-if="refreshing" class="pull-refresh-text">
        <i class="weui-loading"></i> 正在刷新...
      </span>
      <span v-else-if="pullY > 50" class="pull-refresh-text">释放立即刷新</span>
      <span v-else-if="pullY > 0" class="pull-refresh-text">下拉刷新</span>
    </div>

    <!-- 表白卡片列表：body 滚动，无内层滚动容器 -->
    <div class="dating-list">
      <div
        v-for="item in list"
        :key="item.id"
        class="dating-card"
        style="background: #fff; border-radius: 8px; margin: 15px; box-shadow: 0 2px 8px rgba(0,0,0,0.05); overflow: hidden;"
        @click="goToDetail(item.id)"
      >
        <div class="card-header" style="text-align: center; font-size: 18px; padding: 20px 15px 10px; line-height: 1.5;">
          <span :class="getGenderClass(item.senderGender)">{{ item.senderName }}</span>
          <span style="color: #666; margin: 0 5px;"> ≡❤ </span>
          <span :class="getGenderClass(item.receiverGender)">{{ item.receiverName }}</span>
        </div>

        <div class="card-body" style="text-align: center; font-size: 16px; padding: 10px 20px 20px; color: #333;">
          {{ item.content }}
        </div>

        <div class="card-time" style="text-align: right; font-size: 12px; color: #b2b2b2; padding: 0 15px 15px;">
          {{ item.time || '刚刚' }}
        </div>

        <div class="card-actions" style="display: flex; border-top: 1px solid #f0f0f0; padding: 10px 0;">
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

    <!-- 底部图例：Tabbar 上方 -->
    <div class="dating-legend">
      蓝色下划线：男生 / 红色下划线：女生 / 黑色下划线：其他或保密
    </div>

    <!-- 空状态 -->
    <div v-if="!loading && !refreshing && list.length === 0" class="dating-empty">
      <p>暂无表白墙内容</p>
    </div>

    <!-- 上拉加载更多 -->
    <div v-if="loading && !refreshing" class="dating-loadmore">
      <i class="weui-loading"></i>
      <span class="weui-loadmore__tips">正在加载</span>
    </div>
    <div v-if="finished && list.length > 0" class="dating-loadmore">
      <span class="weui-loadmore__tips">没有更多了</span>
    </div>

    <!-- 猜名字 WEUI Dialog -->
    <div v-if="guessDialogVisible" class="weui-mask" @click="guessDialogVisible = false"></div>
    <div v-if="guessDialogVisible" class="weui-dialog weui-dialog--guess">
      <div class="weui-dialog__hd"><strong class="weui-dialog__title">猜名字</strong></div>
      <div class="weui-dialog__bd">
        <input
          type="text"
          class="weui-input weui-dialog__input"
          placeholder="请输入你猜的真实姓名："
          v-model="guessInputValue"
          @keyup.enter="confirmGuess"
        />
      </div>
      <div class="weui-dialog__ft">
        <a href="javascript:" class="weui-dialog__btn weui-dialog__btn_default" @click="guessDialogVisible = false">取消</a>
        <a href="javascript:" class="weui-dialog__btn weui-dialog__btn_primary" @click="confirmGuess">确定</a>
      </div>
    </div>
  </div>
</template>

<style scoped>
.dating-home {
  background: #f5f5f5;
  padding-bottom: 60px;
}

.dating-header.unified-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 44px;
  padding: 0 12px;
  background-color: #f8f8f8;
  border-bottom: 1px solid #eee;
}
.dating-header__back {
  font-size: 14px;
  color: #333;
  cursor: pointer;
  min-width: 48px;
  text-align: left;
}
.dating-header__title {
  flex: 1;
  text-align: center;
  font-size: 16px;
  font-weight: 500;
  margin: 0;
  color: #333;
}
.dating-header__placeholder {
  min-width: 48px;
  text-align: right;
}

/* 巨大的浅粉色粗体标题：从原版 CSS 风格 */
.dating-main-title {
  text-align: center;
  font-size: 22px;
  font-weight: bold;
  color: #ffb3ba;
  margin: 16px 15px 20px;
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
  border-bottom: 2px dashed #333333;
  color: #333333;
}

.pull-refresh-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  transition: height 0.3s;
}
.pull-refresh-text {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #999;
}
.pull-refresh-text .weui-loading {
  width: 16px;
  height: 16px;
  border: 2px solid #e5e5e5;
  border-top-color: #e53935;
  border-radius: 50%;
  animation: dating-spin 0.8s linear infinite;
}
@keyframes dating-spin {
  to { transform: rotate(360deg); }
}

/* 表白卡片 */
.dating-list {
  padding: 0 0 20px;
}
.dating-card {
  cursor: pointer;
}

/* 操作栏按钮：点赞高亮 */
.action-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 2px;
  padding: 10px 0;
  background: transparent;
  border: none;
  border-right: 1px solid #f0f0f0;
  font-size: 14px;
  color: #666;
  cursor: pointer;
}
.action-btn:last-child {
  border-right: none;
}
.action-btn.is-liked {
  color: #ff5252 !important;
  font-weight: bold;
}

/* 猜名字 Dialog */
.weui-dialog--guess {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 85%;
  max-width: 320px;
  background: #fff;
  border-radius: 8px;
  z-index: 1001;
  overflow: hidden;
}
.weui-dialog__input {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #e5e5e5;
  border-radius: 4px;
  font-size: 14px;
  box-sizing: border-box;
}
.weui-dialog__ft {
  display: flex;
  border-top: 1px solid #d9d9d9;
}
.weui-dialog__btn {
  flex: 1;
  padding: 15px 0;
  text-align: center;
  font-size: 17px;
  color: #333;
  text-decoration: none;
}
.weui-dialog__btn_primary {
  color: #4fc3f7;
  font-weight: 500;
}
.weui-dialog__btn_default {
  color: #999;
}
.dating-home .weui-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  z-index: 1000;
}

/* 底部图例 */
.dating-legend {
  text-align: center;
  font-size: 12px;
  color: #999;
  padding: 16px 15px 24px;
  line-height: 1.5;
}

.dating-empty {
  text-align: center;
  padding: 60px 20px;
  color: #999;
  font-size: 14px;
}

.dating-loadmore {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
  color: #999;
  font-size: 14px;
  gap: 8px;
}
.dating-loadmore .weui-loading {
  width: 16px;
  height: 16px;
  border: 2px solid #e5e5e5;
  border-top-color: #e53935;
  border-radius: 50%;
  animation: dating-spin 0.8s linear infinite;
}
</style>
