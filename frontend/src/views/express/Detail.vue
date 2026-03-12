<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '../../utils/request'
import { showErrorTopTips } from '@/utils/toast.js'

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
    <!-- 统一顶部导航栏 -->
    <div class="express-header unified-header">
      <span class="express-header__back" @click="router.back()">返回</span>
      <h1 class="express-header__title">表白详情</h1>
      <span class="express-header__placeholder"></span>
    </div>

    <!-- 主体容器：padding-bottom 防遮挡底部输入框 -->
    <div class="express-detail__container">
      <!-- 表白卡片：与 Home 一致的 DOM 结构 -->
      <div v-if="item" class="dating-card" style="background: #fff; border-radius: 8px; margin: 15px; box-shadow: 0 2px 8px rgba(0,0,0,0.05); overflow: hidden;">
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
      <div class="express-comments">
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
  background: #f5f5f5;
  padding-bottom: 60px;
}

.express-header.unified-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 44px;
  padding: 0 12px;
  background-color: #f8f8f8;
  border-bottom: 1px solid #eee;
}
.express-header__back {
  font-size: 14px;
  color: #333;
  cursor: pointer;
  min-width: 48px;
  text-align: left;
}
.express-header__title {
  flex: 1;
  text-align: center;
  font-size: 16px;
  font-weight: 500;
  margin: 0;
  color: #333;
}
.express-header__placeholder {
  min-width: 48px;
  text-align: right;
}

.express-detail__container {
  padding: 15px;
  padding-bottom: 60px;
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
.express-detail .weui-dialog__ft {
  display: flex;
  border-top: 1px solid #d9d9d9;
}
.express-detail .weui-dialog__btn {
  flex: 1;
  padding: 15px 0;
  text-align: center;
  font-size: 17px;
  color: #333;
  text-decoration: none;
}
.express-detail .weui-dialog__btn_primary {
  color: #4fc3f7;
  font-weight: 500;
}
.express-detail .weui-dialog__btn_default {
  color: #999;
}
.express-detail .weui-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  z-index: 1000;
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
  border-bottom: 2px dashed #333333;
  color: #333333;
}

/* 评论区 */
.express-comments {
  background: #fff;
  border-radius: 8px;
  padding: 15px;
  margin-bottom: 20px;
}
.express-comments__title {
  margin: 0 0 15px;
  font-size: 16px;
  font-weight: 500;
  color: #333;
}
.express-comments__empty {
  text-align: center;
  padding: 40px 0;
  color: #999;
  font-size: 14px;
}
.express-comments__list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}
.express-comment {
  border-bottom: 1px solid #eee;
  padding-bottom: 15px;
}
.express-comment:last-child {
  border-bottom: none;
  padding-bottom: 0;
}
.express-comment__header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}
.express-comment__floor {
  padding: 4px 8px;
  background: #eee;
  color: #000;
  font-size: 12px;
  border-radius: 2px;
}
.express-comment__nickname {
  color: #b3b3b3;
  font-size: 12px;
}
.express-comment__content {
  margin: 10px 0 8px;
  padding-left: 2em;
  font-size: 14px;
  color: #333;
  line-height: 1.6;
  word-break: break-word;
}
.express-comment__time {
  margin: 0;
  text-align: right;
  color: #b3b3b3;
  font-size: 12px;
}

/* 底部固定评论输入框 */
.express-comment-input {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  align-items: center;
  padding: 10px 15px;
  background: #fff;
  border-top: 1px solid #eee;
  z-index: 500;
}
.express-comment-input__field {
  flex: 1;
  height: 36px;
  padding: 0 12px;
  border: 1px solid #ddd;
  border-radius: 18px;
  font-size: 14px;
  outline: none;
}
.express-comment-input__field:focus {
  border-color: #74b9ff;
}
.express-comment-input__btn {
  margin-left: 10px;
  padding: 0 20px;
  height: 36px;
  background-color: #74b9ff;
  color: #fff;
  border: none;
  border-radius: 18px;
  font-size: 14px;
  cursor: pointer;
}
.express-comment-input__btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>
