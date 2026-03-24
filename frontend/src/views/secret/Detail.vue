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
    }).catch(() => {})
  } else {
    request.post(`/secret/id/${secret.value.id}/like`, null, { params: { like: 1 } }).then(() => {
      secret.value.liked = true
      secret.value.likeCount++
    }).catch(() => {})
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

// 加载详情
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

// 加载评论
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

const themeColors = {
  1: 'var(--c-surface)',
  2: '#595959',
  3: '#f5d676',
  4: '#f69695',
  5: '#c6a8c1',
  6: '#89cdcb',
  7: '#90cce2',
  8: '#6e7e90',
  9: '#61ae97',
  10: '#d3cd72',
  11: '#e8d5a8',
  12: '#daa6a1'
}

function getThemeBg(theme) {
  return themeColors[theme] || 'var(--c-surface)'
}

function getThemeTextColor(theme) {
  return theme === 1 ? '#000' : '#fff'
}

function getFooterBg(theme) {
  return theme === 1 ? 'rgba(0,0,0,0.05)' : 'rgba(0,0,0,0.1)'
}

function getFooterTextColor(theme) {
  return theme === 1 ? '#000' : '#fff'
}

function getPregoodIcon(theme) {
  return theme === 1 ? '/img/secret/grayg.png' : '/img/secret/pregood.png'
}

function getCommentIcon(theme) {
  return theme === 1 ? '/img/secret/grayc.png' : '/img/secret/comment.png'
}

function getProgressBg(theme) {
  return theme === 1 ? 'rgba(0,0,0,0.12)' : 'rgba(255,255,255,0.3)'
}

onMounted(async () => {
  await loadDetail()
  await loadComments()
})

onBeforeUnmount(() => {
  destroyAudio()
})
</script>

<template>
  <div class="min-h-screen bg-[var(--c-bg)] pb-14" style="--module-color: #8b5cf6">
    <CommunityHeader title="树洞详情" moduleColor="#8b5cf6" backTo="/secret/home" />

    <div v-if="loading" class="flex items-center justify-center py-16 gap-2.5 text-[var(--c-text-3)]">
      <i class="w-5 h-5 border-2 border-[var(--c-border)] border-t-[#8b5cf6] rounded-full animate-spin"></i>
      <span>加载中...</span>
    </div>

    <div v-else-if="secret" class="min-h-full pb-16 pt-4">
      <!-- 树洞卡片 -->
      <div
        :id="secret.id"
        class="mx-2.5 mt-5 text-center text-[17px] leading-[25px] relative h-[240px] rounded-lg border-l-4 border-[var(--c-secret)] shadow-sm"
        :style="{ backgroundColor: getThemeBg(secret.theme || 1), color: getThemeTextColor(secret.theme || 1) }"
      >
        <section class="flex flex-col items-center justify-center text-center min-h-[150px] p-5 box-border text-inherit cursor-pointer" @click="playAudio">
          <template v-if="secret.type === 0">
            {{ secret.content }}
          </template>
          <template v-else>
            <img
              v-if="secret.theme === 1"
              width="50px"
              height="50px"
              :src="playing ? '/img/secret/voice_pressed.png' : '/img/secret/voice_normal_white.png'"
              class="w-12 h-12 mx-auto"
              alt="语音"
            />
            <img
              v-else
              width="50px"
              height="50px"
              :src="playing ? '/img/secret/voice_pressed.png' : '/img/secret/voice_normal.png'"
              class="w-12 h-12 mx-auto"
              alt="语音"
            />
            <div class="mt-3 text-xs opacity-95">{{ audioStatusText }}</div>
            <div class="mt-1.5 text-[10px] opacity-80">{{ audioTimeText }}</div>
            <div
              class="w-[min(220px,80%)] h-1.5 mt-3.5 rounded-full overflow-hidden cursor-pointer"
              :style="{ background: getProgressBg(secret.theme || 1) }"
              @click.stop="seekAudio"
            >
              <div class="h-full rounded-full opacity-90" style="background: currentColor" :style="{ width: audioProgress + '%' }"></div>
            </div>
          </template>
        </section>
        <footer
          class="h-[42px] absolute bottom-0 left-0 w-full text-[0] rounded-b-lg"
          :style="{ backgroundColor: getFooterBg(secret.theme || 1) }"
        >
          <div
            class="w-1/2 inline-block text-base leading-10 cursor-pointer"
            :style="{ color: getFooterTextColor(secret.theme || 1) }"
          >
            <i
              class="inline-block h-10 w-10 bg-no-repeat bg-[length:1.1rem] bg-center align-middle"
              :style="{ backgroundImage: `url(${secret.liked ? '/img/secret/good.png' : getPregoodIcon(secret.theme || 1)})`, backgroundPosition: 'center 10px' }"
              @click="toggleLike"
            ></i>
            <span>{{ secret.likeCount || 0 }}</span>
          </div>
          <div
            class="w-1/2 inline-block text-base leading-10 cursor-pointer"
            :style="{ color: getFooterTextColor(secret.theme || 1) }"
          >
            <i
              class="inline-block h-10 w-10 bg-no-repeat bg-[length:1.1rem] bg-center align-middle"
              :style="{ backgroundImage: `url(${getCommentIcon(secret.theme || 1)})` }"
            ></i>
            <span>{{ secret.commentCount || 0 }}</span>
          </div>
        </footer>
      </div>

      <!-- 评论列表 -->
      <div
        v-for="(comment, index) in comments"
        :key="comment.id"
        class="leading-6 mx-2.5 mt-2 bg-[var(--c-surface)] p-2 border-l-4 border-[var(--c-secret)] rounded-lg shadow-sm flex gap-2.5 animate-[community-slide-up_0.3s_ease_both]"
        :style="{ animationDelay: index * 0.05 + 's' }"
      >
        <img :src="`/img/avatar/${comment.avatarTheme || 1}.png`" alt="" class="w-10 h-10 rounded-full shrink-0" />
        <div class="flex-1">
          <p class="font-bold mb-1 text-[var(--c-text-1)]">{{ comment.comment }}</p>
          <span class="text-[10px] text-[var(--c-text-3)]">{{ index + 1 }}楼 {{ comment.publishTime }}</span>
        </div>
      </div>
    </div>

    <div v-else class="flex flex-col items-center py-16 text-[var(--c-text-3)]">
      <div class="text-5xl mb-3">📭</div>
      <p class="text-sm">树洞不存在或已删除</p>
    </div>

    <!-- 底部固定输入框 -->
    <div class="border-t border-[var(--c-border)] p-2 bg-[var(--c-surface)] fixed bottom-0 left-0 right-0 w-full flex items-center gap-2.5 box-border">
      <input
        type="text"
        name="comment"
        placeholder="匿名评论"
        class="leading-9 border border-[var(--c-border)] flex-1 rounded px-2.5 text-base outline-none"
        v-model="commentText"
        @keyup.enter="submitComment"
      />
      <div
        v-if="showSubmitBtn"
        class="leading-9 border border-[var(--c-border)] w-[20%] rounded text-center text-[var(--c-secret)] cursor-pointer text-base"
        @click="submitComment"
      >发布</div>
    </div>
  </div>

  <!-- 对话框 -->
  <div v-if="dialogVisible">
    <div class="fixed inset-0 bg-black/50 z-[1000]" @click="dialogVisible = false"></div>
    <div class="fixed top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[80%] max-w-[320px] bg-[var(--c-surface)] rounded-xl z-[1001] overflow-hidden" style="--module-color: #8b5cf6">
      <div class="text-center font-semibold text-base text-[var(--c-text-1)] py-4">提示</div>
      <div class="px-5 pb-4 text-sm text-[var(--c-text-1)] text-center">{{ dialogMessage }}</div>
      <div class="flex border-t border-[var(--c-border)]">
        <a href="javascript:" class="flex-1 py-3 text-center text-sm text-[#8b5cf6] font-semibold no-underline cursor-pointer" @click="dialogVisible = false">确定</a>
      </div>
    </div>
  </div>
</template>
