<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import request from '../../utils/request'
import { uploadFileByPresignedUrl } from '../../utils/presignedUpload'
import { useToast } from '@/composables/useToast'
import CommunityHeader from '../../components/community/CommunityHeader.vue'
import {
  createSecretSwitchCopy,
  createSecretVoiceHint,
  getSecretVoiceState
} from './secretContent'

const router = useRouter()
const { t } = useI18n()
const { loading: toastLoading, hideLoading } = useToast()

const MAX_RECORD_SECONDS = 60
const MAX_VOICE_SIZE = 1024 * 1024

const mode = ref('text')
const formData = ref({
  content: '',
  theme: 1,
  timer: 0
})
const submitting = ref(false)
const dialogVisible = ref(false)
const dialogMessage = ref('')
const recording = ref(false)
const voiceStateKey = ref('idle')
const voiceStateParams = ref({})
const voiceVolume = ref(0)
const showThemes = ref(false)
const previewPlaying = ref(false)
const recordedAudioUrl = ref('')
const recordedAudioFile = ref(null)
const recordSeconds = ref(0)

let mediaRecorder = null
let mediaStream = null
let audioContext = null
let analyser = null
let volumeAnimationId = 0
let recordTimer = null
let previewAudio = null
let recordedChunks = []

const switchCopy = computed(() => createSecretSwitchCopy(t))
const voiceState = computed(() => getSecretVoiceState(voiceStateKey.value, t, voiceStateParams.value))
const voiceHint = computed(() => {
  return createSecretVoiceHint({
    recording: recording.value,
    hasRecordedAudio: Boolean(recordedAudioFile.value),
    duration: formatSeconds(recordSeconds.value)
  }, t)
})

const showDialog = (msg) => {
  dialogMessage.value = msg
  dialogVisible.value = true
}

function setVoiceState(state, params = {}) {
  voiceStateKey.value = state
  voiceStateParams.value = params
}

const switchToWord = () => {
  stopPreviewAudio()
  if (recording.value) stopRecord()
  mode.value = 'text'
}

const switchToVoice = () => {
  mode.value = 'voice'
}

function getSupportedAudioMimeType() {
  if (typeof MediaRecorder === 'undefined') {
    return ''
  }
  const candidates = [
    'audio/webm;codecs=opus',
    'audio/webm',
    'audio/mp4',
    'audio/ogg;codecs=opus',
    'audio/ogg'
  ]
  return candidates.find((mimeType) => MediaRecorder.isTypeSupported?.(mimeType)) || ''
}

function mimeTypeToExtension(mimeType) {
  const normalized = String(mimeType || '').toLowerCase()
  if (normalized.includes('webm')) return '.webm'
  if (normalized.includes('ogg')) return '.ogg'
  if (normalized.includes('mp4')) return '.mp4'
  if (normalized.includes('wav')) return '.wav'
  if (normalized.includes('mpeg') || normalized.includes('mp3')) return '.mp3'
  return '.webm'
}

function formatSeconds(seconds) {
  const total = Math.max(0, Number(seconds) || 0)
  const minute = String(Math.floor(total / 60)).padStart(2, '0')
  const second = String(total % 60).padStart(2, '0')
  return `${minute}:${second}`
}

const selectTheme = (themeNum) => {
  formData.value.theme = themeNum
}

function resetRecordTimer() {
  if (recordTimer) {
    clearInterval(recordTimer)
    recordTimer = null
  }
}

function stopMediaStream() {
  if (mediaStream) {
    mediaStream.getTracks().forEach((track) => track.stop())
    mediaStream = null
  }
}

function stopVolumeMonitor() {
  if (volumeAnimationId) {
    cancelAnimationFrame(volumeAnimationId)
    volumeAnimationId = 0
  }
  voiceVolume.value = 0
  if (audioContext) {
    audioContext.close().catch(() => {})
    audioContext = null
  }
  analyser = null
}

function stopPreviewAudio() {
  if (previewAudio) {
    previewAudio.pause()
    previewAudio.currentTime = 0
  }
  previewPlaying.value = false
}

function revokeRecordedAudio() {
  stopPreviewAudio()
  if (recordedAudioUrl.value) {
    URL.revokeObjectURL(recordedAudioUrl.value)
    recordedAudioUrl.value = ''
  }
  recordedAudioFile.value = null
}

function updateVoiceStateAfterRecording() {
  if (recordedAudioFile.value) {
    setVoiceState('finished', { duration: formatSeconds(recordSeconds.value) })
  } else {
    setVoiceState('idle')
  }
}

async function ensureMediaStream() {
  if (!navigator.mediaDevices?.getUserMedia) {
    throw new Error(t('secret.publish.browserUnsupported'))
  }
  mediaStream = await navigator.mediaDevices.getUserMedia({ audio: true })
  return mediaStream
}

function startVolumeMonitor(stream) {
  stopVolumeMonitor()
  const AudioContextCtor = window.AudioContext || window.webkitAudioContext
  if (!AudioContextCtor) {
    return
  }
  audioContext = new AudioContextCtor()
  const source = audioContext.createMediaStreamSource(stream)
  analyser = audioContext.createAnalyser()
  analyser.fftSize = 256
  source.connect(analyser)
  const dataArray = new Uint8Array(analyser.frequencyBinCount)
  const tick = () => {
    if (!analyser) return
    analyser.getByteFrequencyData(dataArray)
    const total = dataArray.reduce((sum, value) => sum + value, 0)
    voiceVolume.value = Math.min(100, Math.round(total / dataArray.length))
    volumeAnimationId = requestAnimationFrame(tick)
  }
  tick()
}

const startRecord = async () => {
  if (recording.value || submitting.value) return
  stopPreviewAudio()
  try {
    const stream = await ensureMediaStream()
    const mimeType = getSupportedAudioMimeType()
    mediaRecorder = mimeType ? new MediaRecorder(stream, { mimeType }) : new MediaRecorder(stream)
    recordedChunks = []
    recordSeconds.value = 0
    mediaRecorder.ondataavailable = (event) => {
      if (event.data && event.data.size > 0) {
        recordedChunks.push(event.data)
      }
    }
    mediaRecorder.onstop = () => {
      stopMediaStream()
      stopVolumeMonitor()
      const finalMimeType = mediaRecorder?.mimeType || mimeType || 'audio/webm'
      const blob = new Blob(recordedChunks, { type: finalMimeType })
      if (!blob.size) {
        revokeRecordedAudio()
        setVoiceState('idle')
        return
      }
      if (blob.size > MAX_VOICE_SIZE) {
        revokeRecordedAudio()
        setVoiceState('idle')
        showDialog(t('secret.publish.voiceTooLarge'))
        return
      }
      revokeRecordedAudio()
      recordedAudioUrl.value = URL.createObjectURL(blob)
      recordedAudioFile.value = new File(
        [blob],
        `secret-voice${mimeTypeToExtension(finalMimeType)}`,
        { type: finalMimeType }
      )
      updateVoiceStateAfterRecording()
    }
    mediaRecorder.start(200)
    recording.value = true
    setVoiceState('remaining', { seconds: MAX_RECORD_SECONDS })
    startVolumeMonitor(stream)
    resetRecordTimer()
    recordTimer = setInterval(() => {
      recordSeconds.value += 1
      const remain = MAX_RECORD_SECONDS - recordSeconds.value
      if (remain <= 0) {
        stopRecord()
        return
      }
      setVoiceState('remaining', { seconds: remain })
    }, 1000)
  } catch (error) {
    stopMediaStream()
    stopVolumeMonitor()
    showDialog(error?.message || t('secret.publish.microphoneUnavailable'))
  }
}

const stopRecord = () => {
  if (!recording.value) return
  recording.value = false
  resetRecordTimer()
  setVoiceState('processing')
  if (mediaRecorder && mediaRecorder.state !== 'inactive') {
    mediaRecorder.stop()
  } else {
    stopMediaStream()
    stopVolumeMonitor()
    updateVoiceStateAfterRecording()
  }
}

const togglePreviewAudio = () => {
  if (recording.value) {
    stopRecord()
    return
  }
  if (!recordedAudioUrl.value) {
    showDialog(t('secret.publish.voiceMissing'))
    return
  }
  if (!previewAudio || previewAudio.src !== recordedAudioUrl.value) {
    previewAudio = new Audio(recordedAudioUrl.value)
    previewAudio.onended = () => {
      previewPlaying.value = false
      updateVoiceStateAfterRecording()
    }
  }
  if (previewPlaying.value) {
    stopPreviewAudio()
    updateVoiceStateAfterRecording()
  } else {
    previewAudio.play().then(() => {
      previewPlaying.value = true
      setVoiceState('preview')
    }).catch(() => {
      previewPlaying.value = false
      showDialog(t('secret.publish.previewUnsupported'))
    })
  }
}

const submit = async () => {
  if (submitting.value) return
  if (recording.value) {
    showDialog(t('secret.publish.stopRecordingFirst'))
    return
  }
  if (mode.value === 'text') {
    if (!formData.value.content || formData.value.content.trim() === '') {
      showDialog(t('secret.publish.contentRequired'))
      return
    }
    if (formData.value.content.length > 100) {
      showDialog(t('secret.publish.contentTooLong'))
      return
    }
    try {
      submitting.value = true
      toastLoading(t('secret.publish.publishing'))
      await request.post('/secret/info', {
        content: formData.value.content,
        theme: formData.value.theme,
        type: 0,
        timer: formData.value.timer
      })
      router.push('/secret/home')
    } catch (err) {
      showDialog(err?.message || t('secret.publish.submitFailed'))
    } finally {
      submitting.value = false
      hideLoading()
    }
  } else {
    if (!recordedAudioFile.value) {
      showDialog(t('secret.publish.voiceMissing'))
      return
    }
    try {
      submitting.value = true
      toastLoading(t('secret.publish.uploadingVoice'))
      const voiceKey = await uploadFileByPresignedUrl(recordedAudioFile.value, {
        fileName: recordedAudioFile.value.name
      })
      const payload = new FormData()
      payload.append('theme', String(formData.value.theme))
      payload.append('type', '1')
      payload.append('timer', String(formData.value.timer))
      payload.append('voiceKey', voiceKey)
      await request.post('/secret/info', payload)
      router.push('/secret/home')
    } catch (err) {
      showDialog(err?.message || t('secret.publish.submitFailed'))
    } finally {
      submitting.value = false
      hideLoading()
    }
  }
}

const remainingChars = computed(() => {
  return 100 - formData.value.content.length
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

onMounted(() => {
  const rand = Math.ceil(Math.random() * 12)
  formData.value.theme = rand
})

onBeforeUnmount(() => {
  stopPreviewAudio()
  if (recording.value) {
    stopRecord()
  }
  resetRecordTimer()
  stopMediaStream()
  stopVolumeMonitor()
  revokeRecordedAudio()
})
</script>

<template>
  <div class="min-h-screen bg-[var(--c-bg)] relative" style="--module-color: #8b5cf6">
    <CommunityHeader :title="t('secret.publish.title')" moduleColor="#8b5cf6" backTo="/secret/home" />

    <div
      class="relative rounded-lg mx-2.5 pb-5 border-l-4 border-[var(--c-secret)]"
      :style="{ backgroundColor: getThemeBg(formData.theme), color: formData.theme === 1 ? '#000' : '#fff' }"
    >
      <form>
        <header class="leading-10 text-center font-bold text-base border-b border-black/10 relative">
          <i
            class="inline-block w-10 h-10 bg-[length:1rem] bg-center bg-no-repeat align-middle absolute left-0 cursor-pointer bg-[url('/img/secret/back1.png')]"
            @click="router.back()"
          ></i>
          <span class="inline-block">{{ t('secret.publish.cardTitle') }}</span>
          <label class="absolute right-4 top-0 cursor-pointer text-[var(--c-secret)]" @click="submit">{{ submitting ? t('secret.publish.submitting') : t('secret.publish.submitAction') }}</label>
        </header>
        <div class="h-[284px] relative text-center">
          <div
            v-if="mode === 'voice'"
            class="relative h-[250px] flex flex-col items-center justify-center"
            @pointerdown.prevent="startRecord"
            @pointerup.prevent="stopRecord"
            @pointercancel.prevent="stopRecord"
            @pointerleave.prevent="stopRecord"
          >
            <img
              width="50px"
              height="50px"
              :src="formData.theme === 1 ? '/img/secret/voice_normal_white.png' : '/img/secret/voice_normal.png'"
              :alt="t('secret.voiceAlt')"
            />
            <br>
            <span :style="{ color: formData.theme === 1 ? '#bfbfbf' : '#fff' }">
              {{ voiceHint }}
            </span>
          </div>
          <div v-else>
            <textarea
              v-model="formData.content"
              name="content"
              maxlength="100"
              autofocus
              :placeholder="t('secret.publish.placeholder')"
              class="text-center w-full mx-auto border-none text-lg overflow-x-hidden leading-6 bg-inherit text-inherit h-auto p-5 resize-none outline-none placeholder:text-inherit placeholder:leading-6 placeholder:opacity-60"
            ></textarea>
            <div class="absolute bottom-2.5 right-4 text-sm text-[var(--c-text-3)]">{{ remainingChars }}</div>
          </div>
        </div>
      </form>
    </div>

    <div class="relative z-10 bg-[var(--c-bg)]" style="pointer-events: auto;">
      <div class="border-y border-[var(--c-border)] p-2.5 overflow-hidden bg-[var(--c-surface)]">
        <div class="h-[30px]">
          <div v-if="mode === 'voice'" @click="togglePreviewAudio">
            <img
              width="20px"
              height="20px"
              class="relative top-[7px] float-left"
              :src="recording ? '/img/secret/record.png' : '/img/secret/play.png'"
              alt=""
            />
            <p class="relative top-[5px] left-[5px] w-[150px] float-left">{{ previewPlaying ? t('secret.publish.voiceStatePreview') : voiceState }}</p>
          </div>
          <i
            class="w-[23px] h-[23px] block float-right bg-no-repeat bg-center bg-[length:100%] cursor-pointer"
            :class="showThemes ? 'bg-[url(/img/secret/pallet1.png)]' : 'bg-[url(/img/secret/pallet.png)]'"
            :style="{ position: 'relative', top: '5px' }"
            @click="showThemes = !showThemes"
          ></i>
          <div
            v-if="mode === 'voice'"
            class="relative h-[25px] mt-[5px] right-[10px] w-[85px] bg-[#cdcdcd] float-right"
          >
            <div :style="{ width: voiceVolume + '%', height: '100%', background: 'var(--c-secret)' }"></div>
          </div>
        </div>
        <div class="float-right mt-[15px]">
          <input
            type="checkbox"
            v-model="formData.timer"
            :true-value="1"
            :false-value="0"
          />
          {{ t('secret.publish.deleteAfter24h') }}
        </div>
      </div>

      <div v-if="showThemes" class="flex mt-2.5 px-2.5 gap-2.5">
        <div
          v-for="i in 6"
          :key="i"
          class="flex-1 h-[2.7rem] relative rounded cursor-pointer"
          :style="{ backgroundColor: getThemeBg(i) }"
          @click="selectTheme(i)"
        >
          <i v-if="formData.theme === i" class="inline-flex items-center justify-center w-6 h-6 absolute -right-2 -top-2 rounded-full bg-gradient-to-br from-[#a78bfa] to-[#7c3aed] shadow-[0_0.2rem_0.5rem_rgba(139,92,246,0.28)] before:content-[''] before:w-[0.38rem] before:h-[0.72rem] before:border-r-2 before:border-b-2 before:border-white before:rotate-45 before:-translate-x-[8%] before:-translate-y-[8%]"></i>
        </div>
      </div>
      <div v-if="showThemes" class="flex mt-2.5 px-2.5 gap-2.5">
        <div
          v-for="i in 6"
          :key="i + 6"
          class="flex-1 h-[2.7rem] relative rounded cursor-pointer"
          :style="{ backgroundColor: getThemeBg(i + 6) }"
          @click="selectTheme(i + 6)"
        >
          <i v-if="formData.theme === i + 6" class="inline-flex items-center justify-center w-6 h-6 absolute -right-2 -top-2 rounded-full bg-gradient-to-br from-[#a78bfa] to-[#7c3aed] shadow-[0_0.2rem_0.5rem_rgba(139,92,246,0.28)] before:content-[''] before:w-[0.38rem] before:h-[0.72rem] before:border-r-2 before:border-b-2 before:border-white before:rotate-45 before:-translate-x-[8%] before:-translate-y-[8%]"></i>
        </div>
      </div>

      <div
        v-if="mode === 'voice'"
        class="mt-4 text-[var(--c-text-3)] text-center"
      >
        {{ switchCopy.text.prefix }}
        <p class="inline text-[var(--c-secret)] cursor-pointer" @click="switchToWord">{{ switchCopy.text.action }}</p>
        {{ switchCopy.text.suffix }}
      </div>

      <div
        v-if="mode === 'text'"
        class="mt-4 text-[var(--c-text-3)] text-center"
      >
        {{ switchCopy.voice.prefix }}
        <p class="inline text-[var(--c-secret)] cursor-pointer" @click="switchToVoice">{{ switchCopy.voice.action }}</p>
        {{ switchCopy.voice.suffix }}
      </div>
    </div>
  </div>

  <div v-if="dialogVisible">
    <div class="fixed inset-0 bg-black/50 z-[1000]" @click="dialogVisible = false"></div>
    <div class="fixed top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[80%] max-w-[320px] bg-[var(--c-surface)] rounded-xl z-[1001] overflow-hidden" style="--module-color: #8b5cf6">
      <div class="text-center font-semibold text-base text-[var(--c-text-1)] py-4">{{ t('common.hint') }}</div>
      <div class="px-5 pb-4 text-sm text-[var(--c-text-1)] text-center">{{ dialogMessage }}</div>
      <div class="flex border-t border-[var(--c-border)]">
        <a href="javascript:" class="flex-1 py-3 text-center text-sm text-[#8b5cf6] font-semibold no-underline cursor-pointer" @click="dialogVisible = false">{{ t('common.confirm') }}</a>
      </div>
    </div>
  </div>
</template>
