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
    class="community-page dating-home"
    @touchstart="handleTouchStart"
    @touchmove="handleTouchMove($event, scrollContainer)"
    @touchend="handleTouchEnd"
  >
    <CommunityHeader title="卖室友" moduleColor="#ec4899" backTo="/">
      <template #right>
        <span class="dating-header-right" @click="router.push('/dating/center')">我的</span>
      </template>
    </CommunityHeader>

    <div class="community-pull-refresh" :style="{ height: pullY + 'px' }">
      <span v-if="refreshing" class="community-pull-refresh__text"><i class="community-loading-spinner" style="--module-color: #ec4899"></i> 正在刷新...</span>
      <span v-else-if="pullY > 50" class="community-pull-refresh__text">释放立即刷新</span>
      <span v-else-if="pullY > 0" class="community-pull-refresh__text">下拉刷新</span>
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
        v-for="(item, index) in list"
        :key="item.id"
        class="community-card dating-card"
        :style="{ animationDelay: (index % 10) * 0.05 + 's' }"
        @click="goDetail(item.id)"
      >
        <div class="dating-card__img">
          <img :src="(item.images && item.images[0]) || item.image || '/img/dating/default-avatar.png'" :alt="item.name" />
        </div>
        <h2 class="dating-card__title">{{ item.name }}</h2>
        <div class="dating-card__info">{{ item.faculty }} {{ getGradeText(item.grade) }}学生</div>
        <div class="dating-card__info">来自{{ item.hometown || '未知' }}</div>
        <p class="dating-card__desc">{{ item.content }}</p>
      </div>
    </div>

    <div v-if="!loading && !refreshing && list.length === 0" class="community-empty">
      <div class="community-empty__icon">💕</div>
      <div class="community-empty__text">暂无内容</div>
    </div>
    <div v-if="loading && !refreshing" class="community-loadmore"><i class="community-loading-spinner" style="--module-color: #ec4899"></i> 正在加载</div>
    <div v-if="finished && list.length > 0" class="community-loadmore">已经到底了</div>

    <!-- 右下角发布悬浮按钮（原版 JSP skypub） -->
    <div class="dating-fab" @click="router.push('/dating/publish')">
      <span class="dating-fab__icon">+</span>
    </div>
  </div>
</template>

<style scoped>
.dating-home {
  padding-bottom: 80px;
}

.dating-header-right {
  color: var(--c-text-2);
  cursor: pointer;
  font-size: var(--font-base);
}

.dating-sex-tab {
  background: var(--c-card);
  margin-top: var(--space-sm);
  height: 44px;
}
.dating-sex-tab ul { display: flex; list-style: none; margin: 0; padding: 0; }
.dating-sex-tab .tab-item {
  flex: 1;
  text-align: center;
  line-height: 44px;
  font-size: var(--font-xl);
  transition: background 0.3s, color 0.3s;
}
.dating-sex-tab .tab-item a { color: var(--c-text-2); text-decoration: none; }
.dating-sex-tab .tab-item.selected { background: var(--c-dating); }
.dating-sex-tab .tab-item.selected a { color: #fff; }

.dating-list { padding: var(--space-md); }
.dating-card {
  margin-bottom: var(--space-md);
  padding: var(--space-md);
  overflow: hidden;
  cursor: pointer;
  animation: community-slide-up 0.4s ease both;
}
.dating-card__img {
  width: 100%;
  aspect-ratio: 1;
  background: var(--c-border);
  border-radius: var(--radius-sm);
  overflow: hidden;
  margin-bottom: var(--space-sm);
}
.dating-card__img img { width: 100%; height: 100%; object-fit: cover; }
.dating-card__title {
  margin: 0 0 6px;
  font-size: var(--font-xl);
  font-weight: bold;
  color: var(--c-dating);
  line-height: 1.3;
}
.dating-card__info {
  color: var(--c-text-2);
  font-size: var(--font-base);
  margin-top: 2px;
  line-height: 1.5;
}
.dating-card__actions { margin-top: var(--space-md); padding-top: var(--space-sm); border-top: 1px solid var(--c-border); }
.dating-action-btn {
  background: none;
  border: none;
  font-size: var(--font-base);
  color: var(--c-text-2);
  cursor: pointer;
}
.dating-action-btn.is-liked { color: var(--c-dating); font-weight: bold; }

.dating-fab {
  position: fixed;
  right: 20px;
  bottom: 24px;
  width: 48px;
  height: 48px;
  border-radius: var(--radius-full);
  background: var(--c-dating);
  box-shadow: 0 4px 12px rgba(236, 72, 153, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
  cursor: pointer;
  transition: transform 0.2s;
}
.dating-fab:active {
  transform: scale(0.92);
}
.dating-fab__icon {
  font-size: 28px;
  line-height: 1;
  color: #fff;
  font-weight: 300;
}
</style>
