<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '../../utils/request'
import { useScrollLoad } from '../../composables/useScrollLoad'

const router = useRouter()
const activeArea = ref(0) // 0 小姐姐 1 小哥哥

const fetchDatingData = async (page) => {
  const res = await request.get('/dating/items', {
    params: { page, limit: 10, area: activeArea.value }
  })
  return res
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

function handleLike(item) {
  if (item.isLiked === undefined) item.isLiked = false
  item.isLiked = !item.isLiked
  item.likeCount = (item.likeCount || 0) + (item.isLiked ? 1 : -1)
  if (item.likeCount < 0) item.likeCount = 0
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
    class="dating-home"
    @touchstart="handleTouchStart"
    @touchmove="handleTouchMove($event, scrollContainer)"
    @touchend="handleTouchEnd"
  >
    <div class="dating-header unified-header">
      <span class="dating-header__back" @click="router.push('/')">返回</span>
      <h1 class="dating-header__title">卖室友</h1>
      <div class="dating-header__right" @click="router.push('/dating/center')">我的</div>
    </div>

    <div class="pull-refresh-indicator" :style="{ height: pullY + 'px' }">
      <span v-if="refreshing" class="pull-refresh-text"><i class="weui-loading"></i> 正在刷新...</span>
      <span v-else-if="pullY > 50" class="pull-refresh-text">释放立即刷新</span>
      <span v-else-if="pullY > 0" class="pull-refresh-text">下拉刷新</span>
    </div>

    <!-- 性别 Tab：小姐姐 / 小哥哥（原版 JSP sexTab） -->
    <div class="dating-sex-tab">
      <ul>
        <li :class="['tab-item', { selected: activeArea === 0 }]">
          <a href="javascript:;" @click.prevent="switchArea(0)">小姐姐</a>
        </li>
        <li :class="['tab-item', { selected: activeArea === 1 }]">
          <a href="javascript:;" @click.prevent="switchArea(1)">小哥哥</a>
        </li>
      </ul>
    </div>

    <div class="dating-list">
      <div
        v-for="item in list"
        :key="item.id"
        class="dating-card"
        @click="goDetail(item.id)"
      >
        <div class="dating-card__img">
          <img :src="(item.images && item.images[0]) || item.image || '/img/dating/default-avatar.png'" :alt="item.name" />
        </div>
        <h2 class="dating-card__title">{{ item.name }}</h2>
        <div class="dating-card__info">{{ item.faculty }} {{ getGradeText(item.grade) }}学生</div>
        <div class="dating-card__info">来自{{ item.hometown || '未知' }}</div>
        <div class="dating-card__actions">
          <button type="button" class="dating-action-btn" :class="{ 'is-liked': item.isLiked }" @click.stop="handleLike(item)">
            {{ item.isLiked ? '♥' : '♡' }} {{ item.likeCount || 0 }}
          </button>
        </div>
      </div>
    </div>

    <div v-if="!loading && !refreshing && list.length === 0" class="dating-empty">暂无内容</div>
    <div v-if="loading && !refreshing" class="dating-loadmore"><i class="weui-loading"></i> 正在加载</div>
    <div v-if="finished && list.length > 0" class="dating-loadmore">已经到底了</div>

    <!-- 右下角发布悬浮按钮（原版 JSP skypub） -->
    <div class="dating-fab" @click="router.push('/dating/publish')">
      <span class="dating-fab__icon">+</span>
    </div>
  </div>
</template>

<style scoped>
.dating-home {
  background: #eee;
  min-height: 100vh;
  padding-bottom: 80px;
}

.dating-header.unified-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 44px;
  padding: 0 12px;
  background: linear-gradient(180deg, #78e2d1 0%, #6dcbbd 100%);
  color: #fff;
}
.dating-header__back { color: #fff; cursor: pointer; min-width: 48px; font-size: 14px; }
.dating-header__title { flex: 1; text-align: center; font-size: 16px; font-weight: 500; margin: 0; }
.dating-header__right { color: #fff; cursor: pointer; min-width: 48px; font-size: 14px; text-align: right; }

.pull-refresh-indicator { display: flex; align-items: center; justify-content: center; overflow: hidden; transition: height 0.3s; }
.pull-refresh-text { font-size: 14px; color: #999; }
.pull-refresh-text .weui-loading {
  width: 16px; height: 16px; border: 2px solid #e5e5e5; border-top-color: #6dcbbd; border-radius: 50%;
  animation: spin 0.8s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

.dating-sex-tab {
  background: #fff;
  margin-top: 10px;
  height: 44px;
}
.dating-sex-tab ul { display: flex; list-style: none; margin: 0; padding: 0; }
.dating-sex-tab .tab-item {
  flex: 1;
  text-align: center;
  line-height: 44px;
  font-size: 18px;
}
.dating-sex-tab .tab-item a { color: #333; text-decoration: none; }
.dating-sex-tab .tab-item.selected { background: #78e2d1; }
.dating-sex-tab .tab-item.selected a { color: #fff; }

.dating-list { padding: 10px; }
.dating-card {
  background: #fff;
  margin-bottom: 10px;
  padding: 10px;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  cursor: pointer;
}
.dating-card__img {
  width: 100%;
  aspect-ratio: 1;
  background: #ccc;
  border-radius: 6px;
  overflow: hidden;
  margin-bottom: 8px;
}
.dating-card__img img { width: 100%; height: 100%; object-fit: cover; }
.dating-card__title {
  margin: 0 0 6px;
  font-size: 18px;
  font-weight: bold;
  color: #6dcbbd;
  line-height: 1.3;
}
.dating-card__info {
  color: #666;
  font-size: 14px;
  margin-top: 2px;
  line-height: 1.5;
}
.dating-card__actions { margin-top: 10px; padding-top: 8px; border-top: 1px solid #f0f0f0; }
.dating-action-btn {
  background: none;
  border: none;
  font-size: 14px;
  color: #666;
  cursor: pointer;
}
.dating-action-btn.is-liked { color: #ff5252; font-weight: bold; }

.dating-empty { text-align: center; padding: 40px 20px; color: #999; font-size: 14px; }
.dating-loadmore { text-align: center; padding: 16px; color: #999; font-size: 14px; }
.dating-loadmore .weui-loading {
  width: 16px; height: 16px; border: 2px solid #e5e5e5; border-top-color: #6dcbbd; border-radius: 50%;
  animation: spin 0.8s linear infinite;
  display: inline-block; vertical-align: middle; margin-right: 6px;
}

.dating-fab {
  position: fixed;
  right: 20px;
  bottom: 24px;
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: #2ee9d0;
  box-shadow: 0 4px 12px rgba(46, 233, 208, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
  cursor: pointer;
}
.dating-fab__icon {
  font-size: 28px;
  line-height: 1;
  color: #fff;
  font-weight: 300;
}
</style>
