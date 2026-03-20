<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import request from '../../utils/request'
import { useScrollLoad } from '../../composables/useScrollLoad'
import CommunityHeader from '../../components/community/CommunityHeader.vue'

const router = useRouter()
const scrollContainer = ref(null)
const activeType = ref(1) // 1: 最美生活照, 2: 最美校园照

const PAGE_SIZE = 10
const fetchPhotographList = async (page) => {
  const start = (page - 1) * PAGE_SIZE
  const type = activeType.value === 1 ? 1 : 0
  const res = await request.get(`/photograph/type/${type}/start/${start}/size/${PAGE_SIZE}`)
  const rawList = res?.data || []
  const list = Array.isArray(rawList) ? rawList.map((p) => ({
    id: p.id,
    title: p.title,
    description: p.content,
    imgUrl: p.firstImageUrl,
    photoCount: p.count,
    likeCount: p.likeCount ?? 0,
    commentCount: p.commentCount ?? 0,
    isLiked: p.liked === true
  })) : []
  return { list, hasMore: list.length >= PAGE_SIZE }
}

const {
  items: list,
  loading,
  finished,
  refreshing,
  pullY,
  loadData,
  handleScroll,
  handleTouchStart,
  handleTouchMove,
  handleTouchEnd
} = useScrollLoad(fetchPhotographList)

const setType = (type) => {
  if (activeType.value === type) return
  activeType.value = type
  loadData(true)
}

const toggleLike = (item, e) => {
  if (e) e.stopPropagation()
  if (!item) return
  if (item.isLiked) {
    return
  }
  request.post(`/photograph/id/${item.id}/like`).then(() => {
    item.isLiked = true
    item.likeCount++
  })
}

const goDetail = (id) => {
  router.push(`/photograph/detail/${id}`)
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="community-page photograph-home" :style="{ '--module-color': '#06b6d4' }">
    <CommunityHeader title="拍好校园" moduleColor="#06b6d4" backTo="/" />

    <!-- 列表滚动容器：单列卡片列表 -->
    <div
      class="photograph-scroll-container"
      ref="scrollContainer"
      @scroll="handleScroll"
      @touchstart="handleTouchStart"
      @touchmove="handleTouchMove($event, scrollContainer)"
      @touchend="handleTouchEnd"
    >
      <!-- 下拉刷新指示器 -->
      <div class="community-pull-refresh" :style="{ height: pullY + 'px' }">
        <span v-if="refreshing" class="community-pull-refresh__text">
          <i class="community-loading-spinner"></i> 正在刷新...
        </span>
        <span v-else-if="pullY > 50" class="community-pull-refresh__text">释放立即刷新</span>
        <span v-else-if="pullY > 0" class="community-pull-refresh__text">下拉刷新</span>
      </div>

      <!-- 照片卡片列表 -->
      <div class="card-list">
        <div
          v-for="(item, index) in list"
          :key="item.id"
          class="community-card card"
          :style="{ animationDelay: (index % 10) * 0.05 + 's' }"
          @click="goDetail(item.id)"
        >
          <div class="card-img">
            <figure class="card-img-tag">
              <img :src="item.imgUrl" :alt="item.title" />
            </figure>
            <!-- 多图角标：右下角显示 N图（仅多图时展示） -->
            <div class="tags" v-if="(item.photoCount || 1) > 1">
              <span class="img-num">{{ item.photoCount || 1 }}图</span>
            </div>
          </div>
          <div class="card-name">
            {{ item.title }}
          </div>
          <div class="card-say">
            {{ item.description }}
          </div>

          <!-- 卡片下方按钮组：点赞 + 评论 -->
          <div class="card-btn-group">
            <div class="btn-group-justify">
              <a
                class="btn-action"
                :class="{ liked: item.isLiked }"
                href="javascript:;"
                role="button"
                @click.stop="toggleLike(item, $event)"
              >
                <i :class="item.isLiked ? 'am-icon-check-square' : 'am-icon-check-square-o'"></i
                >{{ item.likeCount ?? item.likes }} 点赞
              </a>
              <a class="btn-action" href="javascript:;" role="button">
                <i class="am-icon-th-list"></i>{{ item.commentCount ?? 0 }} 评论
              </a>
            </div>
          </div>

        </div>
      </div>

      <!-- 空状态 -->
      <div v-if="!loading && !refreshing && list.length === 0" class="community-empty">
        <div class="community-empty__icon">📷</div>
        <p class="community-empty__text">暂无照片作品</p>
      </div>

      <!-- 上拉加载更多 -->
      <div v-if="loading && !refreshing" class="community-loadmore">
        <i class="community-loading-spinner"></i>
        <span>正在加载</span>
      </div>
      <div v-if="finished && list.length > 0" class="community-loadmore">
        <span>没有更多了</span>
      </div>
    </div>

    <!-- 底部三色操作栏 -->
    <footer class="photo-toolbar">
      <div class="toolbar-btn life" :class="{ active: activeType === 1 }" @click="setType(1)">
        <span>最美生活照</span>
      </div>
      <div class="toolbar-btn campus" :class="{ active: activeType === 2 }" @click="setType(2)">
        <span>最美校园照</span>
      </div>
      <div class="toolbar-btn upload" @click="router.push('/photograph/publish')">
        <span>我要晒照</span>
      </div>
    </footer>
  </div>
</template>

<style scoped>
/* 滚动容器 */
.photograph-scroll-container {
  height: calc(100vh - 51px - 80px);
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
  padding-bottom: 80px;
}

/* 卡片列表 */
.card-list {
  padding: var(--space-md);
}

/* 卡片样式 */
.card {
  width: 100%;
  margin-bottom: var(--space-lg);
  overflow: hidden;
  animation: community-slide-up 0.4s ease both;
}
.card-img-tag img {
  width: 100%;
  height: auto;
  display: block;
}
.card-img {
  position: relative;
}
.card-name {
  margin: var(--space-md);
  font-size: var(--font-2xl);
  font-weight: 600;
  color: var(--c-text-1);
  clear: both;
}
.card-say {
  margin: 0 var(--space-md) var(--space-md);
  font-size: var(--font-md);
  color: var(--c-text-2);
  line-height: 1.5;
}

/* 多图角标 */
.tags {
  position: absolute;
  right: var(--space-sm);
  bottom: var(--space-sm);
  display: inline-flex;
}
.img-num {
  background: var(--c-photograph);
  color: #fff;
  border-radius: var(--radius-sm);
  padding: 2px 8px;
  font-size: var(--font-sm);
  font-weight: 500;
}

/* 卡片按钮组 */
.card-btn-group {
  padding: 0 var(--space-md) var(--space-md);
}
.btn-group-justify {
  display: flex;
  gap: var(--space-sm);
}
.btn-action {
  flex: 1;
  text-align: center;
  padding: var(--space-sm) 0;
  border: none;
  border-radius: var(--radius-sm);
  cursor: pointer;
  color: #fff;
  background-color: var(--c-photograph);
  font-size: var(--font-base);
  text-decoration: none;
  transition: opacity 0.2s;
}
.btn-action:active {
  opacity: 0.85;
}
.btn-action.liked {
  background-color: #0592aa;
  background-color: color-mix(in srgb, var(--c-photograph) 80%, #000);
}
.btn-action i {
  margin-right: var(--space-xs);
}

/* 底部三色操作栏 */
.photo-toolbar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  box-shadow: var(--shadow-lg);
  border-top: 1px solid var(--c-border);
}
.photo-toolbar .toolbar-btn {
  flex: 1;
  text-align: center;
  padding: var(--space-sm) 0;
  color: #fff;
  font-size: var(--font-base);
  font-weight: 500;
  cursor: pointer;
  transition: opacity 0.2s;
}
.photo-toolbar .toolbar-btn:active {
  opacity: 0.85;
}
.photo-toolbar .life {
  background-color: #e84c3d;
}
.photo-toolbar .life.active {
  box-shadow: inset 0 -3px 0 rgba(0, 0, 0, 0.2);
}
.photo-toolbar .campus {
  background-color: #3498db;
}
.photo-toolbar .campus.active {
  box-shadow: inset 0 -3px 0 rgba(0, 0, 0, 0.2);
}
.photo-toolbar .upload {
  background-color: var(--c-photograph);
}

.card-img-tag {
  margin: 0;
  padding: 0;
}
</style>
