<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '../../utils/request'

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
    const res = await request.get(`/photograph/item/${route.params.id}`)
    work.value = res.data
    const imgs = res.data.images && res.data.images.length ? res.data.images : [res.data.imgUrl]
    images.value = imgs
  } catch (e) {
    showDialog('加载失败')
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
  if (!work.value.comments) {
    work.value.comments = []
  }
  work.value.comments.unshift({
    id: Date.now(),
    author: '匿名同学',
    avatar: '/img/avatar/default.png',
    text: newComment.value.trim(),
    time: new Date().toLocaleString('zh-CN')
  })
  work.value.commentCount = (work.value.commentCount || 0) + 1
  newComment.value = ''
}

const toggleLike = () => {
  if (!work.value) return
  if (work.value.isLiked === undefined) {
    work.value.isLiked = false
  }
  if (work.value.likeCount === undefined) {
    work.value.likeCount = work.value.likes ?? 0
  }
  work.value.isLiked = !work.value.isLiked
  work.value.likeCount += work.value.isLiked ? 1 : -1
  if (work.value.likeCount < 0) work.value.likeCount = 0
}

onMounted(() => {
  loadDetail()
})
</script>

<template>
  <div class="photograph-detail">
    <!-- 统一顶部导航栏 -->
    <div class="unified-header">
      <div class="header-left" @click="goBack">‹</div>
      <div class="header-title">作品详情</div>
      <div class="header-right"></div>
    </div>

    <div v-if="loading" class="loading">
      <i class="weui-loading"></i>
      <span>加载中...</span>
    </div>

    <div v-else-if="work" class="detail-main">
      <!-- 顶部数据看板 -->
      <div class="photo-stats">
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
          <div class="detail-info">
        <h2 class="detail-title">{{ work.title }}</h2>
        <div class="detail-meta">
          <div class="author">
            <img
              class="author-avatar"
              :src="work.author?.avatar || '/img/avatar/default.png'"
              :alt="work.author?.name || '作者'"
            />
            <span class="author-name">{{ work.author?.name || '匿名作者' }}</span>
          </div>
        </div>
        <p class="detail-time">{{ work.time }}</p>
        <p class="detail-desc">{{ work.description }}</p>

        <div class="card-btn-group">
          <div class="am-btn-group am-btn-group-justify">
            <a class="am-btn am-btn-photo" :class="{ liked: work.isLiked }" href="javascript:;" role="button" @click.stop="toggleLike">
              <i :class="work.isLiked ? 'am-icon-check-square' : 'am-icon-check-square-o'"></i
              >{{ work.likeCount ?? work.likes }} 点赞
            </a>
            <a class="am-btn am-btn-photo" href="javascript:;" role="button">
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

    <!-- WEUI 对话框 -->
    <div v-if="dialogVisible">
      <div class="weui-mask" @click="dialogVisible = false"></div>
      <div class="weui-dialog">
        <div class="weui-dialog__hd">
          <strong class="weui-dialog__title">提示</strong>
        </div>
        <div class="weui-dialog__bd">{{ dialogMessage }}</div>
        <div class="weui-dialog__ft">
          <a href="javascript:" class="weui-dialog__btn weui-dialog__btn_primary" @click="dialogVisible = false">确定</a>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.photograph-detail {
  min-height: 100vh;
  background: #f8f8f8;
}

/* 统一顶部导航栏 */
.unified-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 44px;
  padding: 0 12px;
  background-color: #f8f8f8;
  border-bottom: 1px solid #eee;
}
.header-left {
  padding: 0 10px;
  display: flex;
  align-items: center;
  cursor: pointer;
  min-width: 48px;
  font-size: 24px;
  font-weight: 300 !important;
  color: #333;
}
.header-left i,
.header-left svg,
.header-left img {
  font-size: 24px !important;
  width: 24px !important;
  height: 24px !important;
  font-weight: 300 !important;
  transform: scale(1.4);
  transform-origin: center center;
}
.header-left svg {
  stroke-width: 1 !important;
}
.header-title {
  flex: 1;
  text-align: center;
  font-size: 16px;
  font-weight: 500;
  margin: 0;
  color: #333;
}
.header-right {
  min-width: 48px;
  text-align: right;
}

.loading {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  gap: 10px;
  color: #999;
}
.loading .weui-loading {
  width: 20px;
  height: 20px;
  border: 2px solid #e5e5e5;
  border-top-color: #3cb395;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}
@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.detail-main {
  padding-bottom: 80px; /* 预留底部输入框高度 */
}

/* 顶部数据看板 */
.photo-stats {
  margin: 12px 15px;
  border-radius: 8px;
  display: flex;
  overflow: hidden; /* 确保圆角生效 */
}
.photo-stats .stat-item {
  flex: 1;
  text-align: center;
}
.photo-stats .num {
  font-size: 24px;
  font-weight: bold;
  background: #009688;
  color: #fff;
}
.photo-stats .label {
  font-size: 14px;
  background: #05574f;
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
  margin: 8px 0 4px 0;
}
.carousel-dots span {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #ccc;
  margin: 0 3px;
}
.carousel-dots .active {
  background: #009688;
}

.detail-info {
  padding: 15px;
  background: #fff;
}
.detail-title {
  font-size: 20px;
  margin: 0 0 10px 0;
}
.detail-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}
.author {
  display: flex;
  align-items: center;
}
.author-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  margin-right: 8px;
}
.author-name {
  font-size: 14px;
  color: #555;
}
.likes {
  display: flex;
  align-items: center;
  font-size: 14px;
  color: #e91e63;
}
.likes i {
  margin-right: 4px;
}
.detail-time {
  font-size: 12px;
  color: #999;
  margin-bottom: 10px;
}
.detail-desc {
  font-size: 15px;
  color: #333;
  line-height: 1.6;
}

/* 互动按钮样式：复用 am-btn-photo */
.card-btn-group {
  font-size: 20px;
  padding: 10px 0;
}
.am-btn-group-justify {
  display: flex;
}
.am-btn {
  flex: 1;
  text-align: center;
  padding: 8px 0;
  border: none;
  cursor: pointer;
}
.am-btn-photo {
  color: #fff;
  background-color: #518379;
  border-color: #ffffff;
  font-size: 80%;
}
.am-btn-photo:hover {
  color: #ffeb3b !important;
}
.am-btn-photo.liked {
  background-color: #2e8b57 !important;
  color: #fff !important;
}
.am-btn-photo.liked:hover {
  color: #fff !important;
}
.am-btn-photo i {
  margin-right: 4px;
}

/* 评论列表：头像+气泡框 */
.comment-list {
  margin-top: 10px;
}
.comment-item {
  display: flex;
  align-items: flex-start;
  margin-bottom: 10px;
}
.comment-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  margin-right: 8px;
}
.comment-bubble {
  background: #f5f5f5;
  border-radius: 4px;
  padding: 6px 10px;
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
  border-color: transparent #f5f5f5 transparent transparent;
}
.comment-author {
  margin: 0;
  font-size: 13px;
  font-weight: bold;
  color: #555;
}
.comment-text {
  margin: 2px 0;
  font-size: 14px;
  color: #333;
}
.comment-time {
  margin: 0;
  font-size: 12px;
  color: #999;
}

/* 底部评论输入栏 */
.comment-input-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  align-items: center;
  padding: 8px 10px;
  background: #fff;
  border-top: 1px solid #ddd;
}
.comment-input-bar input {
  flex: 1;
  border: 1px solid #ccc;
  border-radius: 4px;
  padding: 6px 8px;
  font-size: 14px;
  margin-right: 8px;
}
.comment-input-bar button {
  padding: 6px 12px;
  border: none;
  border-radius: 4px;
  background: #27ae60;
  color: #fff;
  font-size: 14px;
  cursor: pointer;
}

/* WEUI 对话框样式（简化版） */
.weui-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  z-index: 1000;
}
.weui-dialog {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 85%;
  max-width: 300px;
  background: #fff;
  border-radius: 8px;
  z-index: 1001;
}
.weui-dialog__hd {
  padding: 1.2em 1.6em 0.5em;
  text-align: center;
}
.weui-dialog__title {
  font-weight: 400;
  font-size: 18px;
}
.weui-dialog__bd {
  padding: 0 1.6em 0.8em;
  min-height: 40px;
  font-size: 15px;
  line-height: 1.5;
  color: #999;
  text-align: center;
}
.weui-dialog__ft {
  position: relative;
  line-height: 42px;
  display: flex;
  border-top: 1px solid #d5d5d6;
}
.weui-dialog__btn {
  flex: 1;
  text-align: center;
  text-decoration: none;
  color: #3cc51f;
  font-size: 17px;
}
.weui-dialog__btn_primary {
  color: #0bb20c;
}
</style>

