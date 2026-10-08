<script setup>
import { postSecretByIdLike, postSecretByIdComment, getSecretById, getSecretByIdComments } from "../../api/secretEndpoints.js"

import { Inbox } from 'lucide-vue-next'
import { ref, onMounted, computed, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'

import { useThemeMode } from '../../composables/useThemeMode'
import {
  getThemeBg,
  getThemeTextColor,
  getFooterBg,
  getFooterTextColor,
  resolveNoteTheme,
  useLightVoiceAsset
} from '../../utils/secretPalette'
import CommunityHeader from '../../components/community/CommunityHeader.vue'

const route = useRoute()
const router = useRouter()
const { t } = useI18n()

const secret = ref(null)
const comments = ref([])
const commentText = ref('')
const loading = ref(true)
const dialogVisible = ref(false)
const dialogKey = ref('')
const dialogParams = ref({})
const dialogMessage = computed(() => dialogKey.value ? t(dialogKey.value, dialogParams.value) : '')
const showDialog = (key, params = {}) => {
  dialogKey.value = key
  dialogParams.value = params
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
    audioError.value = 'secret.detail.audioLoadFailed'
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
  if (audioError.value) return t(audioError.value)
  if (audioLoading.value && !audioReady.value) return t('secret.detail.audioLoading')
  if (playing.value) return t('secret.detail.audioPause')
  if (audioReady.value) return t('secret.detail.audioResume')
  return t('secret.detail.audioPlay')
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
    audioError.value = 'secret.detail.audioUnsupported'
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
    postSecretByIdLike(secret.value.id, null, { params: { like: 0 } }).then(() => {
      secret.value.liked = false
      secret.value.likeCount--
    }).catch(() => {})
  } else {
    postSecretByIdLike(secret.value.id, null, { params: { like: 1 } }).then(() => {
      secret.value.liked = true
      secret.value.likeCount++
    }).catch(() => {})
  }
}

// 提交评论
const submitComment = () => {
  if (!commentText.value || commentText.value.trim() === '') {
    showDialog('secret.detail.commentEmpty')
    return
  }
  if (commentText.value.length > 50) {
    showDialog('secret.detail.commentTooLong', { max: 50 })
    return
  }
  postSecretByIdComment(route.params.id, null, { params: { comment: commentText.value.trim() } }).then(() => {
    commentText.value = ''
    loadComments()
  }).catch(() => {})
}

// 加载详情
const loadDetail = async () => {
  try {
    loading.value = true
    destroyAudio()
    const res = await getSecretById(route.params.id)
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
    const res = await getSecretByIdComments(route.params.id)
    comments.value = res?.data || []
  } catch (err) {
    comments.value = []
  }
}

const showSubmitBtn = computed(() => {
  return commentText.value && commentText.value.trim().length > 0
})

const isDark = useThemeMode()
const noteBg = (theme) => getThemeBg(theme, isDark.value)
const noteText = (theme) => getThemeTextColor(theme, isDark.value)
const noteFooterBg = (theme) => getFooterBg(theme, isDark.value)
const noteFooterText = (theme) => getFooterTextColor(theme, isDark.value)

function getPregoodIcon(theme) {
  return resolveNoteTheme(theme) ? '/img/secret/pregood.png' : '/img/secret/grayg.png'
}

function getCommentIcon(theme) {
  return resolveNoteTheme(theme) ? '/img/secret/comment.png' : '/img/secret/grayc.png'
}

function getProgressBg(theme) {
  const note = resolveNoteTheme(theme)
  if (!note) return 'var(--c-border-light)'
  return note.light ? 'rgba(17, 32, 28, 0.14)' : 'rgba(255, 255, 255, 0.3)'
}

function voiceIcon(playingState) {
  if (playingState) return '/img/secret/voice_pressed.png'
  return useLightVoiceAsset(secret?.theme, isDark.value)
    ? '/img/secret/voice_normal.png'
    : '/img/secret/voice_normal_white.png'
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
  <div class="secret-page-root">
    <div class="min-h-screen bg-[var(--c-bg)] pb-14" style="--module-color: var(--c-secret)">
    <CommunityHeader :title="t('secret.detail.title')" moduleColor="var(--c-secret)" backTo="/secret/home" />

    <div v-if="loading" class="flex items-center justify-center py-16 gap-2.5 text-[var(--c-text-3)]">
      <i class="w-5 h-5 border-2 border-[var(--c-border)] border-t-[var(--c-secret)] rounded-full animate-spin"></i>
      <span>{{ t('common.loading') }}</span>
    </div>

    <div v-else-if="secret" class="min-h-full pb-16 pt-4">
      <!-- 树洞卡片 -->
      <div
        :id="secret.id"
        class="mx-2.5 mt-5 text-center text-[17px] leading-[25px] relative h-[240px] rounded-lg border-l-4 border-[var(--c-secret)] shadow-sm"
        :style="{ backgroundColor: noteBg(secret.theme), color: noteText(secret.theme) }"
      >
        <section class="flex flex-col items-center justify-center text-center min-h-[150px] p-5 box-border text-inherit cursor-pointer" @click="playAudio">
          <template v-if="secret.type === 0">
            {{ secret.content }}
          </template>
          <template v-else>
            <img
              width="50px"
              height="50px"
              :src="voiceIcon(playing)"
              class="w-12 h-12 mx-auto"
              :alt="t('secret.voiceAlt')"
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
          :style="{ backgroundColor: noteFooterBg(secret.theme) }"
        >
          <div
            class="w-1/2 inline-block text-base leading-10 cursor-pointer"
            :style="{ color: noteFooterText(secret.theme) }"
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
            :style="{ color: noteFooterText(secret.theme) }"
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
          <span class="text-[10px] text-[var(--c-text-3)]">{{ t('secret.detail.floor', { n: index + 1 }) }} {{ comment.publishTime }}</span>
        </div>
      </div>
    </div>

    <div v-else class="ui-empty-state flex flex-col items-center py-16 text-[var(--c-text-3)]">
      <Inbox class="mb-3" :size="40" :stroke-width="1.5" aria-hidden="true" />
      <p class="text-sm">{{ t('secret.detail.notFound') }}</p>
    </div>

    <!-- 底部固定输入框 -->
    <div class="secret-detail-commentbar border-t border-[var(--c-border)] p-2 bg-[var(--c-surface)] fixed bottom-0 left-0 right-0 w-full flex items-center gap-2.5 box-border">
      <input
        type="text"
        name="comment"
        :placeholder="t('secret.detail.commentPlaceholder')"
        class="ui-control leading-9 border border-[var(--c-border)] flex-1 rounded px-2.5 text-base outline-none"
        v-model="commentText"
        @keyup.enter="submitComment"
      />
      <div
        v-if="showSubmitBtn"
        class="leading-9 border border-[var(--c-border)] w-[20%] rounded text-center text-[var(--c-secret)] cursor-pointer text-base"
        @click="submitComment"
      >{{ t('secret.publish.submitAction') }}</div>
    </div>
  </div>

  <!-- 对话框 -->
  <div v-if="dialogVisible">
    <div class="ui-scrim fixed inset-0 bg-black/50 z-[1000]" @click="dialogVisible = false"></div>
    <div class="ui-modal fixed top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[80%] max-w-[320px] bg-[var(--c-surface)] rounded-xl z-[1001] overflow-hidden" style="--module-color: var(--c-secret)">
      <div class="text-center font-semibold text-base text-[var(--c-text-1)] py-4">{{ t('common.hint') }}</div>
      <div class="px-5 pb-4 text-sm text-[var(--c-text-1)] text-center">{{ dialogMessage }}</div>
      <div class="flex border-t border-[var(--c-border)]">
        <a href="javascript:" class="flex-1 py-3 text-center text-sm text-[var(--c-secret)] font-semibold no-underline cursor-pointer" @click="dialogVisible = false">{{ t('common.confirm') }}</a>
      </div>
    </div>
  </div>
  </div>
</template>

<style scoped>
/* 移动端社区底部 tabbar（fixed, z 500）会盖住评论输入栏：抬到 tabbar 上方 */
@media (max-width: 767px) {
  .secret-detail-commentbar {
    bottom: calc(57px + env(safe-area-inset-bottom, 0px));
  }
}
</style>
