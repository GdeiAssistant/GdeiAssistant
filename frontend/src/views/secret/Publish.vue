<script setup>
import { ref, onMounted, computed, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import request from '../../utils/request'
import { uploadFileByPresignedUrl } from '../../utils/presignedUpload'
import { useToast } from '@/composables/useToast'
import CommunityHeader from '../../components/community/CommunityHeader.vue'

const router = useRouter()
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
const showDialog = (msg) => {
  dialogMessage.value = msg
  dialogVisible.value = true
}

const recording = ref(false)
const voiceState = ref('未录音')
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

const voiceHint = computed(() => {
  if (recording.value) {
    return `正在录音，已录制 ${formatSeconds(recordSeconds.value)}`
  }
  if (recordedAudioFile.value) {
    return `已录音 ${formatSeconds(recordSeconds.value)}，可试听或重新录制`
  }
  return '按住开始录音，最长不超过60秒'
})

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
    voiceState.value = `录音完成（${formatSeconds(recordSeconds.value)}）`
  } else {
    voiceState.value = '未录音'
  }
}

async function ensureMediaStream() {
  if (!navigator.mediaDevices?.getUserMedia) {
    throw new Error('当前浏览器不支持录音')
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
        voiceState.value = '未录音'
        return
      }
      if (blob.size > MAX_VOICE_SIZE) {
        revokeRecordedAudio()
        voiceState.value = '未录音'
        showDialog('语音文件大小过大，请缩短录音时长')
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
    voiceState.value = `正在录音，还剩${MAX_RECORD_SECONDS}秒`
    startVolumeMonitor(stream)
    resetRecordTimer()
    recordTimer = setInterval(() => {
      recordSeconds.value += 1
      const remain = MAX_RECORD_SECONDS - recordSeconds.value
      if (remain <= 0) {
        stopRecord()
        return
      }
      voiceState.value = `正在录音，还剩${remain}秒`
    }, 1000)
  } catch (error) {
    stopMediaStream()
    stopVolumeMonitor()
    showDialog(error?.message || '麦克风权限获取失败')
  }
}

const stopRecord = () => {
  if (!recording.value) return
  recording.value = false
  resetRecordTimer()
  voiceState.value = '录音处理中...'
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
    showDialog('未采集到任何录音信息')
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
      voiceState.value = '正在试听录音'
    }).catch(() => {
      previewPlaying.value = false
      showDialog('当前浏览器暂不支持播放该录音')
    })
  }
}

const submit = async () => {
  if (submitting.value) return
  if (recording.value) {
    showDialog('请先结束当前录音')
    return
  }
  if (mode.value === 'text') {
    if (!formData.value.content || formData.value.content.trim() === '') {
      showDialog('树洞内容不能为空！')
      return
    }
    if (formData.value.content.length > 100) {
      showDialog('树洞内容长度超过限制！')
      return
    }
    try {
      submitting.value = true
      toastLoading('正在发布...')
      await request.post('/secret/info', {
        content: formData.value.content,
        theme: formData.value.theme,
        type: 0,
        timer: formData.value.timer
      })
      router.push('/secret/home')
    } catch (err) {
      showDialog(err?.message || '提交失败')
    } finally {
      submitting.value = false
      hideLoading()
    }
  } else {
    if (!recordedAudioFile.value) {
      showDialog('未采集到任何录音信息')
      return
    }
    try {
      submitting.value = true
      toastLoading('正在上传语音...')
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
      showDialog(err?.message || '提交失败')
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
    <CommunityHeader title="发布小秘密" moduleColor="#8b5cf6" backTo="/secret/home" />

    <!-- 树洞发布框 -->
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
          <span class="inline-block">小秘密</span>
          <label class="absolute right-4 top-0 cursor-pointer text-[var(--c-secret)]" @click="submit">{{ submitting ? '发布中' : '发布' }}</label>
        </header>
        <div class="h-[284px] relative text-center">
          <!-- 语音树洞 -->
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
              alt="录音"
            />
            <br>
            <span :style="{ color: formData.theme === 1 ? '#bfbfbf' : '#fff' }">
              {{ voiceHint }}
            </span>
          </div>
          <!-- 文字树洞 -->
          <div v-else>
            <textarea
              v-model="formData.content"
              name="content"
              maxlength="100"
              autofocus
              placeholder="说个小秘密"
              class="text-center w-full mx-auto border-none text-lg overflow-x-hidden leading-6 bg-inherit text-inherit h-auto p-5 resize-none outline-none placeholder:text-inherit placeholder:leading-6 placeholder:opacity-60"
            ></textarea>
            <div class="absolute bottom-2.5 right-4 text-sm text-[var(--c-text-3)]">{{ remainingChars }}</div>
          </div>
        </div>
      </form>
    </div>

    <!-- 底部控制区 -->
    <div class="relative z-10 bg-[var(--c-bg)]" style="pointer-events: auto;">
      <!-- 操作栏 -->
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
            <p class="relative top-[5px] left-[5px] w-[150px] float-left">{{ previewPlaying ? '正在试听录音' : voiceState }}</p>
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
          24小时后删除
        </div>
      </div>

      <!-- 主题选择 -->
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

      <!-- 切换到文字树洞 -->
      <div
        v-if="mode === 'voice'"
        class="mt-4 text-[var(--c-text-3)] text-center"
      >
        切换到
        <p class="inline text-[var(--c-secret)] cursor-pointer" @click="switchToWord">文字树洞</p>
        ，用文字分享你的小秘密
      </div>

      <!-- 切换到语音树洞 -->
      <div
        v-if="mode === 'text'"
        class="mt-4 text-[var(--c-text-3)] text-center"
      >
        切换到
        <p class="inline text-[var(--c-secret)] cursor-pointer" @click="switchToVoice">语音树洞</p>
        ，用语音分享你的小秘密
      </div>
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
