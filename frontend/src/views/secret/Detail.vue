<script setup>
import { ref, onMounted, computed, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '../../utils/request'
import CommunityHeader from '../../components/community/CommunityHeader.vue'

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

const audio = ref(null)
const playing = ref(false)
const audioLoading = ref(false)
const audioReady = ref(false)
const audioError = ref('')
const audioDuration = ref(0)
const audioCurrentTime = ref(0)

function formatAudioTime(seconds) {
  const total = Math.max(0, Math.floor(Number(seconds) || 0))
  const minute = String(Math.floor(total / 60)).padStart(2, '0')
  const second = String(total % 60).padStart(2, '0')
  return `${minute}:${second}`
}

function destroyAudio() {
  if (audio.value) {
    audio.value.pause()
    audio.value.onended = null
    audio.value.onpause = null
    audio.value.onplay = null
    audio.value.ontimeupdate = null
    audio.value.onloadedmetadata = null
    audio.value.oncanplay = null
    audio.value.onerror = null
    audio.value.src = ''
  }
  audio.value = null
  playing.value = false
  audioLoading.value = false
  audioReady.value = false
  audioError.value = ''
  audioDuration.value = 0
  audioCurrentTime.value = 0
}

function bindAudioEvents(audioElement) {
  audioElement.preload = 'metadata'
  audioElement.onloadedmetadata = () => {
    audioDuration.value = Number.isFinite(audioElement.duration) ? audioElement.duration : 0
  }
  audioElement.oncanplay = () => {
    audioReady.value = true
    audioLoading.value = false
    audioError.value = ''
  }
  audioElement.ontimeupdate = () => {
    audioCurrentTime.value = audioElement.currentTime || 0
  }
  audioElement.onplay = () => {
    playing.value = true
    audioLoading.value = false
  }
  audioElement.onpause = () => {
    playing.value = false
  }
  audioElement.onended = () => {
    playing.value = false
    audioCurrentTime.value = audioDuration.value
  }
  audioElement.onerror = () => {
    playing.value = false
    audioLoading.value = false
    audioReady.value = false
    audioError.value = '语音加载失败，请稍后重试'
  }
}

function ensureAudio() {
  if (!secret.value?.voiceURL) {
    return null
  }
  if (!audio.value) {
    const audioElement = new Audio(secret.value.voiceURL)
    bindAudioEvents(audioElement)
    audio.value = audioElement
  }
  return audio.value
}

const audioProgress = computed(() => {
  if (!audioDuration.value) return 0
  return Math.min(100, Math.max(0, (audioCurrentTime.value / audioDuration.value) * 100))
})

const audioStatusText = computed(() => {
  if (audioError.value) return audioError.value
  if (audioLoading.value && !audioReady.value) return '语音加载中...'
  if (playing.value) return '点击暂停播放'
  if (audioReady.value) return '点击继续播放'
  return '点击播放语音'
})

const audioTimeText = computed(() => {
  const current = formatAudioTime(audioCurrentTime.value)
  const duration = formatAudioTime(audioDuration.value)
  return `${current} / ${duration}`
})

const playAudio = async () => {
  if (!secret.value || secret.value.type === 0) return
  if (audioError.value && audio.value) {
    destroyAudio()
  }
  const audioElement = ensureAudio()
  if (!audioElement) return
  if (playing.value) {
    audioElement.pause()
    return
  }
  try {
    audioLoading.value = true
    await audioElement.play()
  } catch (_) {
    audioLoading.value = false
    audioError.value = '当前浏览器无法播放该语音'
  }
}

const seekAudio = (event) => {
  if (!audio.value || !audioDuration.value) return
  const target = event.currentTarget
  if (!target) return
  const rect = target.getBoundingClientRect()
  const ratio = Math.min(1, Math.max(0, (event.clientX - rect.left) / rect.width))
  audio.value.currentTime = audioDuration.value * ratio
  audioCurrentTime.value = audio.value.currentTime
}

// 点赞/取消点赞
const toggleLike = () => {
  if (secret.value.liked) {
    request.post(`/secret/id/${secret.value.id}/like`, null, { params: { like: 0 } }).then(() => {
      secret.value.liked = false
      secret.value.likeCount--
    })
  } else {
    request.post(`/secret/id/${secret.value.id}/like`, null, { params: { like: 1 } }).then(() => {
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
  request.post(`/secret/id/${route.params.id}/comment`, null, { params: { comment: commentText.value.trim() } }).then(() => {
    commentText.value = ''
    loadComments()
  }).catch(() => {})
}

// 加载详情：后端返回 Secret（content, type, theme, likeCount, commentCount, liked, voiceURL 等）
const loadDetail = async () => {
  try {
    loading.value = true
    destroyAudio()
    const res = await request.get(`/secret/id/${route.params.id}`)
    const data = res?.data
    if (data && res.success !== false) {
      secret.value = {
        ...data,
        liked: data.liked === 1
      }
      if (secret.value.type !== 0 && secret.value.voiceURL) {
        audioLoading.value = true
        const audioElement = ensureAudio()
        audioElement?.load()
      }
    } else {
      secret.value = null
    }
  } catch (err) {
    secret.value = null
  } finally {
    loading.value = false
  }
}

// 加载评论：后端返回 List<SecretComment>
const loadComments = async () => {
  try {
    const res = await request.get(`/secret/id/${route.params.id}/comments`)
    comments.value = res?.data || []
  } catch (err) {
    comments.value = []
  }
}

const showSubmitBtn = computed(() => {
  return commentText.value && commentText.value.trim().length > 0
})

onMounted(async () => {
  await loadDetail()
  await loadComments()
})

onBeforeUnmount(() => {
  destroyAudio()
})
</script>

<template>
  <div class="community-page secret-detail" style="--module-color: #8b5cf6">
    <CommunityHeader title="树洞详情" moduleColor="#8b5cf6" backTo="/secret/home" />

    <div v-if="loading" class="loading">
      <i class="community-loading-spinner"></i>
      <span>加载中...</span>
    </div>

    <div v-else-if="secret" class="all">
      <!-- 树洞信息：参考原版 .secret -->
      <div :id="secret.id" class="secret community-card" :class="`theme${secret.theme || 1}`">
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
            <div class="voice-status">{{ audioStatusText }}</div>
            <div class="voice-time">{{ audioTimeText }}</div>
            <div class="voice-progress" @click.stop="seekAudio">
              <div class="voice-progress__current" :style="{ width: audioProgress + '%' }"></div>
            </div>
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
      <div
        v-for="(comment, index) in comments"
        :key="comment.id"
        class="discuss community-card"
        :style="{ animationDelay: index * 0.05 + 's' }"
      >
        <img :src="`/img/avatar/${comment.avatarTheme || 1}.png`" alt="" />
        <div class="info">
          <p>{{ comment.comment }}</p>
          <span>{{ index + 1 }}楼 {{ comment.publishTime }}</span>
        </div>
      </div>
    </div>

    <div v-else class="community-empty">
      <div class="community-empty__icon">📭</div>
      <p class="community-empty__text">树洞不存在或已删除</p>
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

  <!-- 对话框 -->
  <div v-if="dialogVisible">
    <div class="community-dialog-mask" @click="dialogVisible = false"></div>
    <div class="community-dialog" style="--module-color: #8b5cf6">
      <div class="community-dialog__title">提示</div>
      <div class="community-dialog__body">{{ dialogMessage }}</div>
      <div class="community-dialog__footer">
        <a href="javascript:" class="community-dialog__btn community-dialog__btn--confirm" @click="dialogVisible = false">确定</a>
      </div>
    </div>
  </div>
</template>

<style scoped>
.secret-detail {
  min-height: 100vh;
  background: var(--c-bg);
  padding-bottom: 3.3rem;
}

.loading {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  gap: 10px;
  color: var(--c-text-3);
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
  border-radius: var(--radius-md);
  border-left: 4px solid var(--c-secret);
  box-shadow: var(--shadow-sm);
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
.secret .section .voice-status {
  margin-top: 12px;
  font-size: var(--font-sm);
  opacity: 0.95;
}
.secret .section .voice-time {
  margin-top: 6px;
  font-size: var(--font-xs);
  opacity: 0.8;
}
.secret .section .voice-progress {
  width: min(220px, 80%);
  height: 6px;
  margin-top: 14px;
  border-radius: var(--radius-full);
  background: rgba(255, 255, 255, 0.3);
  overflow: hidden;
  cursor: pointer;
}
.secret .section .voice-progress__current {
  height: 100%;
  border-radius: inherit;
  background: currentColor;
  opacity: 0.9;
}
.theme1 .section .voice-progress {
  background: rgba(0, 0, 0, 0.12);
}
.secret footer {
  height: 42px;
  background-color: rgba(0, 0, 0, 0.1);
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  font-size: 0;
  border-radius: 0 0 var(--radius-md) var(--radius-md);
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
  background-color: var(--c-card);
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
  margin: var(--space-sm) 10px;
  background: var(--c-card);
  padding: var(--space-sm);
  border-left: 4px solid var(--c-secret);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
  display: flex;
  gap: 10px;
  animation: community-slide-up 0.3s ease both;
}
.discuss img {
  width: 2.5rem;
  height: 2.5rem;
  border-radius: var(--radius-full);
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
  color: var(--c-text-1);
}
.discuss span {
  font-size: var(--font-xs);
  color: var(--c-text-3);
}

/* 底部固定输入框：参考原版 secret-detail.css .form */
.form {
  border-top: 1px solid var(--c-divider);
  padding: 0.5rem;
  background: var(--c-card);
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
  border: 1px solid var(--c-divider);
  flex: 1;
  border-radius: var(--radius-sm);
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
  border: 1px solid var(--c-divider);
  width: 20%;
  border-radius: var(--radius-sm);
  text-align: center;
  color: var(--c-secret);
  cursor: pointer;
  font-size: 1rem;
}
</style>
