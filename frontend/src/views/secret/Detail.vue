<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '../../utils/request'

const route = useRoute()
const router = useRouter()

const secret = ref(null)
const comments = ref([])
const commentText = ref('')
const loading = ref(true)
const dialogVisible = ref(false)
const dialogMessage = ref('')
const showDialog = (msg) => {
  dialogMessage.value = msg
  dialogVisible.value = true
}

// 播放音频
const audio = ref(null)
const playing = ref(false)

const playAudio = () => {
  if (!secret.value || secret.value.type === 0) return
  if (!audio.value) {
    audio.value = new Audio(secret.value.audioUrl)
  }
  if (audio.value.paused) {
    audio.value.play()
    playing.value = true
    audio.value.onended = () => {
      playing.value = false
    }
  }
}

// 点赞/取消点赞
const toggleLike = () => {
  if (secret.value.liked) {
    request.post(`/secret/like/${secret.value.id}`, { like: 0 }).then(() => {
      secret.value.liked = false
      secret.value.likeCount--
    })
  } else {
    request.post(`/secret/like/${secret.value.id}`, { like: 1 }).then(() => {
      secret.value.liked = true
      secret.value.likeCount++
    })
  }
}

// 提交评论
const submitComment = () => {
  if (!commentText.value || commentText.value.trim() === '') {
    showDialog('评论内容不能为空')
    return
  }
  if (commentText.value.length > 50) {
    showDialog('评论内容不能超过50字')
    return
  }
  request.post(`/secret/item/${route.params.id}/comment`, {
    comment: commentText.value
  }).then(() => {
    commentText.value = ''
    loadComments()
  }).catch(err => {
    showDialog(err.response?.data?.message || '评论失败')
  })
}

// 加载详情
const loadDetail = async () => {
  try {
    loading.value = true
    const res = await request.get(`/secret/item/${route.params.id}`)
    secret.value = res.data
    // 创建音频对象
    if (secret.value.type !== 0 && secret.value.audioUrl) {
      audio.value = new Audio(secret.value.audioUrl)
    }
  } catch (err) {
    showDialog('加载失败')
  } finally {
    loading.value = false
  }
}

// 加载评论
const loadComments = async () => {
  try {
    const res = await request.get(`/secret/item/${route.params.id}/comments`)
    comments.value = res.data || []
  } catch (err) {
    console.error('加载评论失败', err)
  }
}

const showSubmitBtn = computed(() => {
  return commentText.value && commentText.value.trim().length > 0
})

onMounted(() => {
  loadDetail()
  loadComments()
})
</script>

<template>
  <div class="secret-detail">
    <!-- 统一顶部导航栏：与二手交易模块一致 -->
    <div class="ershou-header unified-header">
      <span class="ershou-header__back" @click="router.back()">返回</span>
      <h1 class="ershou-header__title">树洞详情</h1>
      <span class="ershou-header__placeholder"></span>
    </div>

    <div v-if="loading" class="loading">
      <i class="weui-loading"></i>
      <span>加载中...</span>
    </div>

    <div v-else-if="secret" class="all">
      <!-- 树洞信息：参考原版 .secret -->
      <div :id="secret.id" class="secret" :class="`theme${secret.theme || 1}`">
        <section class="section" @click="playAudio">
          <template v-if="secret.type === 0">
            {{ secret.content }}
          </template>
          <template v-else>
            <img
              v-if="secret.theme === 1"
              id="voice"
              width="50px"
              height="50px"
              :src="playing ? '/img/secret/voice_pressed.png' : '/img/secret/voice_normal_white.png'"
              class="voice-icon"
              alt="语音"
            />
            <img
              v-else
              id="voice"
              width="50px"
              height="50px"
              :src="playing ? '/img/secret/voice_pressed.png' : '/img/secret/voice_normal.png'"
              class="voice-icon"
              alt="语音"
            />
          </template>
        </section>
        <footer>
          <div>
            <i :class="secret.liked ? 'good' : 'pregood'" @click="toggleLike"></i>
            <span>{{ secret.likeCount || 0 }}</span>
          </div>
          <div>
            <i class="comment"></i>
            <span>{{ secret.commentCount || 0 }}</span>
          </div>
        </footer>
      </div>

      <!-- 树洞信息评论：参考原版 .discuss -->
      <div v-for="(comment, index) in comments" :key="comment.id" class="discuss">
        <img :src="`/img/avatar/${comment.avatarTheme || 1}.png`" alt="" />
        <div class="info">
          <p>{{ comment.comment }}</p>
          <span>{{ index + 1 }}楼 {{ comment.publishTime }}</span>
        </div>
      </div>
    </div>

    <!-- 底部固定输入框：参考原版 .form -->
    <div class="form">
      <input
        type="text"
        name="comment"
        placeholder="匿名评论"
        v-model="commentText"
        @keyup.enter="submitComment"
      />
      <div v-if="showSubmitBtn" class="submit" @click="submitComment">发布</div>
    </div>
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
</template>

<style scoped>
.secret-detail {
  min-height: 100vh;
  background: #ececec;
  padding-bottom: 3.3rem;
}

/* 统一顶部导航栏：复用二手交易样式 */
.ershou-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 44px;
  padding: 0 12px;
  background-color: #f8f8f8;
  border-bottom: 1px solid #eee;
}
.ershou-header__back {
  font-size: 14px;
  color: #333;
  cursor: pointer;
  min-width: 48px;
  text-align: left;
}
.ershou-header__title {
  flex: 1;
  text-align: center;
  font-size: 16px;
  font-weight: 500;
  margin: 0;
  color: #333;
}
.ershou-header__placeholder {
  min-width: 48px;
  text-align: right;
}

.weui-cells__title {
  padding: 10px 15px;
  font-size: 14px;
  color: #888;
  cursor: pointer;
  background: #fff;
  border-bottom: 1px solid #e5e5e5;
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
  to { transform: rotate(360deg); }
}

.all {
  min-height: 100%;
  padding-bottom: 3.8rem;
  padding-top: 15px;
}

/* 树洞卡片：参考原版 secret-detail.css .secret */
.secret {
  margin: 20px 10px 0;
  text-align: center;
  font-size: 17px;
  line-height: 25px;
  position: relative;
  color: #fff;
  height: 240px;
  border-radius: 8px;
}

/* 卡片内容区域强制水平/垂直居中 */
.secret .section {
  display: flex !important;
  flex-direction: column !important;
  align-items: center !important;
  justify-content: center !important;
  text-align: center;
  min-height: 150px;
  padding: 20px;
  box-sizing: border-box;
  color: inherit;
  cursor: pointer;
}
.secret .section .voice-icon {
  width: 48px;
  height: 48px;
  margin: 0 auto;
}
.secret footer {
  height: 42px;
  background-color: rgba(0, 0, 0, 0.1);
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  font-size: 0;
  border-radius: 0 0 8px 8px;
}
.secret footer div {
  width: 50%;
  display: inline-block;
  font-size: 1rem;
  line-height: 40px;
  color: #fff;
  cursor: pointer;
}
.secret footer div i {
  display: inline-block;
  height: 40px;
  width: 40px;
  background-repeat: no-repeat;
  background-size: 1.1rem;
  background-position: center;
  vertical-align: middle;
}
.secret footer div .pregood {
  background-image: url(/img/secret/pregood.png);
  background-position: center 10px;
}
.secret footer div .good {
  background-image: url(/img/secret/good.png);
  background-position: center 10px;
}
.secret footer div .comment {
  background-image: url(/img/secret/comment.png);
}

/* 主题颜色：参考原版 secret-detail.css */
.theme1 {
  background-color: #fff;
  color: #000;
}
.theme1 > footer {
  background-color: rgba(0, 0, 0, 0.05);
}
.theme1 footer div {
  color: #000;
}
.theme1 footer div .pregood {
  background-image: url(/img/secret/grayg.png);
  background-position: center 10px;
}
.theme1 footer div .comment {
  background-image: url(/img/secret/grayc.png);
}

.theme2 {
  background-color: #595959;
}
.theme3 {
  background-color: #f5d676;
}
.theme4 {
  background-color: #f69695;
}
.theme5 {
  background-color: #c6a8c1;
}
.theme6 {
  background-color: #89cdcb;
}
.theme7 {
  background-color: #90cce2;
}
.theme8 {
  background-color: #6e7e90;
}
.theme9 {
  background-color: #61ae97;
}
.theme10 {
  background-color: #d3cd72;
}
.theme11 {
  background-color: #e8d5a8;
}
.theme12 {
  background-color: #daa6a1;
}

/* 评论列表：参考原版 secret-detail.css .discuss */
.discuss {
  line-height: 1.5rem;
  margin: 0 10px;
  background: #fff;
  padding: 10px;
  border: #e0e0e0 solid;
  border-width: 0 1px;
  display: flex;
  gap: 10px;
}
.discuss img {
  width: 2.5rem;
  height: 2.5rem;
  border-radius: 1.25rem;
  vertical-align: top;
  flex-shrink: 0;
}
.discuss .info {
  flex: 1;
  vertical-align: top;
}
.discuss p {
  font-weight: bolder;
  margin-bottom: 5px;
}
.discuss span {
  font-size: 0.8rem;
  color: #6f6f6f;
}

/* 底部固定输入框：参考原版 secret-detail.css .form */
.form {
  border-top: 1px solid #cacacd;
  padding: 0.5rem;
  background: #fff;
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  width: 100%;
  display: flex;
  align-items: center;
  gap: 10px;
  box-sizing: border-box;
}
.form input {
  line-height: 2.2rem;
  border: #bfc7cd 1px solid;
  flex: 1;
  border-top-left-radius: 3px;
  border-bottom-left-radius: 3px;
  text-indent: 5px;
  font-size: 1rem;
  padding: 0 10px;
}
.form input::-webkit-input-placeholder {
  font-size: 1rem;
  line-height: 2.2rem;
  font-weight: bolder;
}
.submit {
  line-height: 2.2rem;
  border: #bfc7cd 1px solid;
  width: 20%;
  border-radius: 3px;
  text-align: center;
  border-left: none;
  color: #3cb395;
  cursor: pointer;
  font-size: 1rem;
}

/* WEUI 对话框样式 */
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
  word-wrap: break-word;
  word-break: break-all;
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
