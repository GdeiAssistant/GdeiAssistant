<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '../../utils/request'
import CommunityHeader from '../../components/community/CommunityHeader.vue'

const route = useRoute()
const router = useRouter()

const work = ref(null)
const loading = ref(true)
const dialogVisible = ref(false)
const dialogMessage = ref('')
const images = ref([])
const currentIndex = ref(0)
const newComment = ref('')

const showDialog = (msg) => {
  dialogMessage.value = msg
  dialogVisible.value = true
}

const loadDetail = async () => {
  try {
    loading.value = true
    const res = await request.get(`/photograph/id/${route.params.id}`)
    const data = res?.data
    if (data && res.success !== false) {
      work.value = {
        id: data.id,
        title: data.title,
        content: data.content,
        description: data.content,
        count: data.count,
        photoCount: data.count,
        createTime: data.createTime,
        time: data.createTime,
        likeCount: data.likeCount ?? 0,
        commentCount: data.commentCount ?? 0,
        isLiked: data.liked === true,
        photographCommentList: data.photographCommentList || [],
        comments: (data.photographCommentList || []).map((c) => ({
          id: c.commentId,
          author: c.nickname || '匿名',
          avatar: '/img/avatar/default.png',
          text: c.comment,
          time: c.createTime || ''
        }))
      }
      images.value = data.imageUrls && data.imageUrls.length ? data.imageUrls : (data.firstImageUrl ? [data.firstImageUrl] : [])
    } else {
      work.value = null
    }
  } catch (e) {
    work.value = null
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  router.back()
}

const onCarouselScroll = (e) => {
  const el = e.target
  if (!el) return
  const idx = Math.round(el.scrollLeft / el.clientWidth)
  currentIndex.value = idx
}

const submitComment = () => {
  if (!newComment.value.trim()) {
    showDialog('评论内容不能为空')
    return
  }
  if (newComment.value.trim().length > 50) {
    showDialog('评论不能超过50字')
    return
  }
  request.post(`/photograph/id/${route.params.id}/comment`, null, { params: { comment: newComment.value.trim() } }).then(() => {
    if (!work.value.comments) work.value.comments = []
    work.value.comments.unshift({
      id: Date.now(),
      author: '我',
      avatar: '/img/avatar/default.png',
      text: newComment.value.trim(),
      time: new Date().toLocaleString('zh-CN')
    })
    work.value.commentCount = (work.value.commentCount || 0) + 1
    newComment.value = ''
  }).catch(() => {})
}

const toggleLike = () => {
  if (!work.value || work.value.isLiked) return
  request.post(`/photograph/id/${work.value.id}/like`).then(() => {
    work.value.isLiked = true
    work.value.likeCount++
  })
}

onMounted(async () => {
  await loadDetail()
})
</script>

<template>
  <div class="community-page photograph-detail" :style="{ '--module-color': '#06b6d4' }">
    <CommunityHeader title="作品详情" moduleColor="#06b6d4" @back="goBack" :backTo="''" :showBack="true" />

    <div v-if="loading" class="community-loadmore" style="padding: 60px var(--space-xl);">
      <i class="community-loading-spinner"></i>
      <span>加载中...</span>
    </div>

    <div v-else-if="work" class="detail-main">
      <!-- 顶部数据看板 -->
      <div class="community-card photo-stats" style="animation: community-slide-up 0.3s ease both;">
        <div class="stat-item">
          <div class="num">{{ work.photoCount || (images.length || 1) }}</div>
          <div class="label">照片总数</div>
        </div>
        <div class="stat-item">
          <div class="num">{{ work.commentCount || (work.comments ? work.comments.length : 0) }}</div>
          <div class="label">评论总数</div>
        </div>
        <div class="stat-item">
          <div class="num">{{ work.likeCount ?? work.likes }}</div>
          <div class="label">点赞总数</div>
        </div>
      </div>

      <!-- 多图横向滑动区 -->
      <div class="detail-carousel" @scroll.passive="onCarouselScroll">
        <div class="carousel-track">
          <div
            v-for="(img, index) in images"
            :key="index"
            class="carousel-item"
          >
            <img :src="img" :alt="work.title" />
          </div>
        </div>
      </div>
      <div class="carousel-dots">
        <span
          v-for="(img, index) in images"
          :key="index"
          :class="{ active: index === currentIndex }"
        ></span>
      </div>

      <!-- 作品信息与互动区 -->
      <div class="community-card detail-info" style="animation: community-slide-up 0.4s ease both; animation-delay: 0.1s;">
        <h2 class="detail-title">{{ work.title }}</h2>
        <div class="detail-meta">
          <div class="author">
            <img
              class="author-avatar"
              src="/img/avatar/default.png"
              alt="作者"
            />
            <span class="author-name">匿名作者</span>
          </div>
        </div>
        <p class="detail-time">{{ work.time || work.createTime }}</p>
        <p class="detail-desc">{{ work.description || work.content }}</p>

        <div class="card-btn-group">
          <div class="btn-group-justify">
            <a class="btn-action" :class="{ liked: work.isLiked }" href="javascript:;" role="button" @click.stop="toggleLike">
              <i :class="work.isLiked ? 'am-icon-check-square' : 'am-icon-check-square-o'"></i
              >{{ work.likeCount ?? work.likes }} 点赞
            </a>
            <a class="btn-action" href="javascript:;" role="button">
              <i class="am-icon-th-list"></i>{{ work.commentCount || (work.comments ? work.comments.length : 0) }} 评论
            </a>
          </div>
        </div>

        <div class="comment-list" v-if="work.comments && work.comments.length">
          <div class="comment-item" v-for="comment in work.comments" :key="comment.id">
            <img class="comment-avatar" :src="comment.avatar" alt="avatar" />
            <div class="comment-bubble">
              <p class="comment-author">{{ comment.author }}</p>
              <p class="comment-text">{{ comment.text }}</p>
              <p class="comment-time">{{ comment.time }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 底部评论输入框 -->
    <div class="comment-input-bar">
      <input
        v-model="newComment"
        type="text"
        placeholder="说点什么..."
        @keyup.enter="submitComment"
      />
      <button type="button" @click="submitComment">发送</button>
    </div>

    <!-- 对话框 -->
    <div v-if="dialogVisible">
      <div class="community-dialog-mask" @click="dialogVisible = false"></div>
      <div class="community-dialog">
        <div class="community-dialog__title">提示</div>
        <div class="community-dialog__body">{{ dialogMessage }}</div>
        <div class="community-dialog__footer">
          <a href="javascript:" class="community-dialog__btn community-dialog__btn--confirm" @click="dialogVisible = false">确定</a>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.detail-main {
  padding-bottom: 80px;
}

/* 顶部数据看板 */
.photo-stats {
  margin: var(--space-md) var(--space-lg);
  display: flex;
  overflow: hidden;
}
.photo-stats .stat-item {
  flex: 1;
  text-align: center;
}
.photo-stats .num {
  font-size: var(--font-2xl);
  font-weight: bold;
  padding: var(--space-sm) 0;
  background: var(--c-photograph);
  color: #fff;
}
.photo-stats .label {
  font-size: var(--font-base);
  padding: var(--space-xs) 0;
  background: #058da0;
  background: color-mix(in srgb, var(--c-photograph) 70%, #000);
  color: #fff;
}

/* 多图横向滑动区 */
.detail-carousel {
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
  scroll-snap-type: x mandatory;
}
.carousel-track {
  display: flex;
}
.carousel-item {
  flex: 0 0 100%;
  scroll-snap-align: center;
}
.carousel-item img {
  width: 100%;
  max-width: 600px;
  display: block;
  margin: 0 auto;
}
.carousel-dots {
  display: flex;
  justify-content: center;
  margin: var(--space-sm) 0 var(--space-xs) 0;
}
.carousel-dots span {
  width: 6px;
  height: 6px;
  border-radius: var(--radius-full);
  background: var(--c-divider);
  margin: 0 3px;
  transition: background 0.2s;
}
.carousel-dots .active {
  background: var(--c-photograph);
}

.detail-info {
  margin: var(--space-md) var(--space-lg);
  padding: var(--space-lg);
}
.detail-title {
  font-size: var(--font-2xl);
  font-weight: 600;
  color: var(--c-text-1);
  margin: 0 0 var(--space-md) 0;
}
.detail-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--space-sm);
}
.author {
  display: flex;
  align-items: center;
}
.author-avatar {
  width: 32px;
  height: 32px;
  border-radius: var(--radius-full);
  margin-right: var(--space-sm);
}
.author-name {
  font-size: var(--font-base);
  color: var(--c-text-2);
}
.detail-time {
  font-size: var(--font-sm);
  color: var(--c-text-3);
  margin-bottom: var(--space-md);
}
.detail-desc {
  font-size: var(--font-md);
  color: var(--c-text-1);
  line-height: 1.6;
}

/* 互动按钮 */
.card-btn-group {
  padding: var(--space-md) 0;
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

/* 评论列表 */
.comment-list {
  margin-top: var(--space-md);
  border-top: 1px solid var(--c-border);
  padding-top: var(--space-md);
}
.comment-item {
  display: flex;
  align-items: flex-start;
  margin-bottom: var(--space-md);
}
.comment-avatar {
  width: 32px;
  height: 32px;
  border-radius: var(--radius-full);
  margin-right: var(--space-sm);
}
.comment-bubble {
  background: var(--c-bg);
  border-radius: var(--radius-sm);
  padding: var(--space-sm) var(--space-md);
  position: relative;
  max-width: 80%;
}
.comment-bubble::before {
  content: '';
  position: absolute;
  left: -6px;
  top: 10px;
  border-width: 6px;
  border-style: solid;
  border-color: transparent var(--c-bg) transparent transparent;
}
.comment-author {
  margin: 0;
  font-size: var(--font-sm);
  font-weight: 600;
  color: var(--c-text-2);
}
.comment-text {
  margin: 2px 0;
  font-size: var(--font-base);
  color: var(--c-text-1);
}
.comment-time {
  margin: 0;
  font-size: var(--font-xs);
  color: var(--c-text-3);
}

/* 底部评论输入栏 */
.comment-input-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  align-items: center;
  padding: var(--space-sm) var(--space-md);
  background: var(--c-card);
  border-top: 1px solid var(--c-border);
  box-shadow: var(--shadow-sm);
}
.comment-input-bar input {
  flex: 1;
  border: 1px solid var(--c-divider);
  border-radius: var(--radius-full);
  padding: var(--space-sm) var(--space-md);
  font-size: var(--font-base);
  margin-right: var(--space-sm);
  color: var(--c-text-1);
  transition: border-color 0.2s;
}
.comment-input-bar input:focus {
  outline: none;
  border-color: var(--c-photograph);
}
.comment-input-bar button {
  padding: var(--space-sm) var(--space-lg);
  border: none;
  border-radius: var(--radius-full);
  background: var(--c-photograph);
  color: #fff;
  font-size: var(--font-base);
  font-weight: 500;
  cursor: pointer;
  transition: opacity 0.2s;
}
.comment-input-bar button:active {
  opacity: 0.85;
}
</style>
