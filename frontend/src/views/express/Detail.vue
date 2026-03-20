<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '../../utils/request'
import { showErrorTopTips } from '@/utils/toast.js'
import CommunityHeader from '../../components/community/CommunityHeader.vue'

const route = useRoute()
const router = useRouter()
const item = ref(null)
const comments = ref([])
const commentInput = ref('')
const submitting = ref(false)

function mapGender(g) {
  if (g === 0) return 'male'
  if (g === 1) return 'female'
  return 'secret'
}

function getGenderClass(gender) {
  if (gender === 'male') return 'gender-male'
  if (gender === 'female') return 'gender-female'
  return 'gender-secret'
}

function handleLike() {
  if (!item.value || item.value.isLiked) return
  request.post(`/express/id/${item.value.id}/like`).then(() => {
    item.value.isLiked = true
    item.value.likeCount++
  })
}

const guessDialogVisible = ref(false)
const guessInputValue = ref('')

function openGuessDialog() {
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
  request.post(`/express/id/${item.value.id}/guess`, null, { params: { name: guessName } })
    .then((res) => {
      const correct = res?.data === true
      if (correct) {
        item.value.correctCount = (item.value.correctCount || 0) + 1
        showSuccess('恭喜你，猜对了！')
      } else {
        showErrorTopTips('猜错了，再试试看吧！')
      }
      item.value.guessCount = (item.value.guessCount || 0) + 1
    })
    .catch(() => {})
  guessDialogVisible.value = false
  guessInputValue.value = ''
}

function handleGuess() {
  // 拦截无效的猜测点击
  if (!item.value.canGuess) {
    showErrorTopTips('TA很神秘，没有留下真名让人猜哦~')
    return
  }
  openGuessDialog()
}

function submitComment() {
  if (!commentInput.value || commentInput.value.trim() === '') {
    showErrorTopTips('请输入评论内容')
    return
  }
  if (commentInput.value.trim().length > 50) {
    showErrorTopTips('评论不能超过50字')
    return
  }
  submitting.value = true
  request.post(`/express/id/${route.params.id}/comment`, null, { params: { comment: commentInput.value.trim() } })
    .then(() => {
      comments.value.push({
        id: comments.value.length + 1,
        nickname: '我',
        comment: commentInput.value.trim(),
        publishTime: '刚刚'
      })
      commentInput.value = ''
      item.value.commentCount = (item.value.commentCount || 0) + 1
      submitting.value = false
    })
    .catch(() => { submitting.value = false })
}

async function loadDetail() {
  try {
    const res = await request.get(`/express/id/${route.params.id}`)
    const e = res?.data
    if (e && res.success !== false) {
      item.value = {
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
      }
    } else {
      item.value = null
    }
  } catch (err) {
    item.value = null
  }
}

async function loadComments() {
  try {
    const res = await request.get(`/express/id/${route.params.id}/comment`)
    const raw = res?.data || []
    comments.value = Array.isArray(raw) ? raw.map((c) => ({
      id: c.id,
      nickname: c.nickname || '匿名',
      comment: c.comment,
      publishTime: c.publishTime || c.createTime || ''
    })) : []
  } catch (err) {
    comments.value = []
  }
}

onMounted(async () => {
  await loadDetail()
  if (item.value) {
    await loadComments()
  }
})
</script>

<template>
  <div class="express-detail">
    <CommunityHeader title="表白详情" moduleColor="#f43f5e" backTo="/express/home" />

    <!-- 主体容器 -->
    <div class="express-detail__container">
      <!-- 表白卡片 -->
      <div v-if="item" class="community-card express-detail-card" style="animation: community-slide-up 0.4s ease both;">
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
          <button type="button" class="action-btn" :class="{ 'is-liked': item.isLiked }" @click.stop="handleLike">
            {{ item.isLiked ? '♥' : '♡' }} {{ item.likeCount || 0 }}
          </button>
          <button type="button" class="action-btn" :style="{ opacity: item.canGuess ? 1 : 0.4 }" @click.stop="handleGuess">
            <span style="margin-right: 4px; position: relative;">☆<sup style="font-size: 10px; position: absolute; top: -4px; right: -6px;">?</sup></span> {{ item.guessCount || 0 }}/{{ item.correctCount || 0 }}
          </button>
          <button type="button" class="action-btn" @click.stop>
            💬 {{ item.commentCount || 0 }}
          </button>
        </div>
      </div>

      <!-- 评论区 -->
      <div class="express-comments community-card">
        <h3 class="express-comments__title">评论列表</h3>
        <div v-if="comments.length === 0" class="express-comments__empty">
          <p>暂无评论，快来抢沙发吧！</p>
        </div>
        <div v-else class="express-comments__list">
          <div
            v-for="(comment, index) in comments"
            :key="comment.id || index"
            class="express-comment"
          >
            <div class="express-comment__header">
              <span class="express-comment__floor">{{ index + 1 }}楼</span>
              <span class="express-comment__nickname">{{ comment.nickname }}</span>
            </div>
            <p class="express-comment__content">{{ comment.comment }}</p>
            <p class="express-comment__time">{{ comment.publishTime }}</p>
          </div>
        </div>
      </div>
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

    <!-- 底部固定评论输入框 -->
    <div class="express-comment-input">
      <input
        type="text"
        class="express-comment-input__field"
        placeholder="我想说..."
        v-model="commentInput"
        @keyup.enter="submitComment"
      />
      <button
        type="button"
        class="express-comment-input__btn"
        :disabled="submitting"
        @click="submitComment"
      >
        {{ submitting ? '发送中...' : '发送' }}
      </button>
    </div>
  </div>
</template>

<style scoped>
.express-detail {
  background: var(--c-bg);
  padding-bottom: 60px;
}

.express-detail__container {
  padding: var(--space-lg);
  padding-bottom: 60px;
}

.express-detail-card {
  overflow: hidden;
  margin-bottom: var(--space-lg);
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

/* 性别虚线样式 */
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

/* 评论区 */
.express-comments {
  padding: var(--space-lg);
  margin-bottom: var(--space-xl);
}
.express-comments__title {
  margin: 0 0 var(--space-lg);
  font-size: var(--font-lg);
  font-weight: 500;
  color: var(--c-text-1);
}
.express-comments__empty {
  text-align: center;
  padding: 40px 0;
  color: var(--c-text-3);
  font-size: var(--font-base);
}
.express-comments__list {
  display: flex;
  flex-direction: column;
  gap: var(--space-lg);
}
.express-comment {
  border-bottom: 1px solid var(--c-border);
  padding-bottom: var(--space-lg);
}
.express-comment:last-child {
  border-bottom: none;
  padding-bottom: 0;
}
.express-comment__header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: var(--space-sm);
}
.express-comment__floor {
  padding: var(--space-xs) var(--space-sm);
  background: var(--c-bg);
  color: var(--c-text-1);
  font-size: var(--font-sm);
  border-radius: var(--radius-sm);
}
.express-comment__nickname {
  color: var(--c-text-3);
  font-size: var(--font-sm);
}
.express-comment__content {
  margin: 10px 0 var(--space-sm);
  padding-left: 2em;
  font-size: var(--font-base);
  color: var(--c-text-1);
  line-height: 1.6;
  word-break: break-word;
}
.express-comment__time {
  margin: 0;
  text-align: right;
  color: var(--c-text-3);
  font-size: var(--font-sm);
}

/* 底部固定评论输入框 */
.express-comment-input {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  align-items: center;
  padding: 10px var(--space-lg);
  background: var(--c-card);
  border-top: 1px solid var(--c-border);
  z-index: 500;
}
.express-comment-input__field {
  flex: 1;
  height: 36px;
  padding: 0 12px;
  border: 1px solid var(--c-divider);
  border-radius: var(--radius-full);
  font-size: var(--font-base);
  outline: none;
  color: var(--c-text-1);
  background: var(--c-bg);
}
.express-comment-input__field:focus {
  border-color: #f43f5e;
}
.express-comment-input__btn {
  margin-left: 10px;
  padding: 0 20px;
  height: 36px;
  background-color: #f43f5e;
  color: #fff;
  border: none;
  border-radius: var(--radius-full);
  font-size: var(--font-base);
  cursor: pointer;
  transition: opacity 0.2s;
}
.express-comment-input__btn:active {
  opacity: 0.85;
}
.express-comment-input__btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>
