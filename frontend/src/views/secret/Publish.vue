<script setup>
import { ref, onMounted, computed, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import request from '../../utils/request'
import { uploadFileByPresignedUrl } from '../../utils/presignedUpload'

const router = useRouter()

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

function showLoading(text = '正在提交...') {
  const weui = typeof window !== 'undefined' && window.weui
  if (weui && typeof weui.loading === 'function') weui.loading(text)
}

function hideLoading() {
  const weui = typeof window !== 'undefined' && window.weui
  if (weui && typeof weui.hideLoading === 'function') weui.hideLoading()
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
      showLoading('正在发布...')
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
      showLoading('正在上传语音...')
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
  <div class="secret-publish">
    <!-- 统一顶部导航栏：与二手交易模块一致 -->
    <div class="ershou-header unified-header">
      <span class="ershou-header__back" @click="router.back()">返回</span>
      <h1 class="ershou-header__title">发布小秘密</h1>
      <span class="ershou-header__placeholder"></span>
    </div>

    <!-- 树洞发布框：参考原版 secretPublish.jsp -->
    <div class="form" :class="`theme${formData.theme}`" :style="{ color: formData.theme === 1 ? '#000' : '#fff' }">
      <form>
        <header>
          <i class="back" @click="router.back()"></i>
          <span>小秘密</span>
          <label class="btn" @click="submit">{{ submitting ? '发布中' : '发布' }}</label>
        </header>
        <div class="edit" style="text-align: center">
          <!-- 语音树洞 -->
          <div
            v-if="mode === 'voice'"
            id="voice"
            class="voice-record-area"
            @pointerdown.prevent="startRecord"
            @pointerup.prevent="stopRecord"
            @pointercancel.prevent="stopRecord"
            @pointerleave.prevent="stopRecord"
          >
            <img
              id="record"
              width="50px"
              height="50px"
              :src="formData.theme === 1 ? '/img/secret/voice_normal_white.png' : '/img/secret/voice_normal.png'"
              alt="录音"
            />
            <br>
            <text id="voice_tip" :style="{ color: formData.theme === 1 ? '#bfbfbf' : '#fff' }">
              {{ voiceHint }}
            </text>
          </div>
          <!-- 文字树洞 -->
          <div v-else id="word">
            <textarea
              v-model="formData.content"
              name="content"
              id="text"
              maxlength="100"
              autofocus
              placeholder="说个小秘密"
              style="text-align: center"
            ></textarea>
            <div class="length">{{ remainingChars }}</div>
          </div>
        </div>
      </form>
    </div>

    <!-- 底部控制区：操作栏、主题选择、模式切换 -->
    <div class="publish-bottom-controls">
      <!-- 操作栏：参考原版 .bar -->
      <div class="bar">
        <div style="height:30px">
          <div v-if="mode === 'voice'" @click="togglePreviewAudio">
            <img
              id="voice_button"
              width="20px"
              height="20px"
              style="position: relative;top:7px;float: left;"
              :src="recording ? '/img/secret/record.png' : '/img/secret/play.png'"
              alt=""
            />
            <p
              id="voice_state"
              style="position:relative;top:5px;left:5px;width:150px;float:left;"
            >{{ previewPlaying ? '正在试听录音' : voiceState }}</p>
          </div>
          <i
            :class="{ 'gray-pallet': showThemes }"
            style="float: right;position:relative;top:5px"
            @click="showThemes = !showThemes"
          ></i>
          <div
            v-if="mode === 'voice'"
            id="voice_volume"
            style="position:relative;height:25px;margin-top:5px;right:10px;width:85px;background:#cdcdcd;float: right"
          >
            <div id="volume" :style="{ width: voiceVolume + '%', height: '100%', background: '#3cb395' }"></div>
          </div>
        </div>
        <div style="float:right;margin-top:15px">
          <input
            id="timer"
            type="checkbox"
            v-model="formData.timer"
            :true-value="1"
            :false-value="0"
          />
          24小时后删除
        </div>
      </div>

      <!-- 主题选择：参考原版 .themes -->
      <div v-if="showThemes" class="themes">
        <div
          v-for="i in 6"
          :key="i"
          class="theme"
          :class="`theme${i}`"
          @click="selectTheme(i)"
        >
          <i v-if="formData.theme === i" class="selected"></i>
        </div>
      </div>
      <div v-if="showThemes" class="themes">
        <div
          v-for="i in 6"
          :key="i + 6"
          class="theme"
          :class="`theme${i + 6}`"
          @click="selectTheme(i + 6)"
        >
          <i v-if="formData.theme === i + 6" class="selected"></i>
        </div>
      </div>

      <!-- 切换到文字树洞 -->
      <div
        v-if="mode === 'voice'"
        id="switchToWord"
        class="switch-text"
      >
        切换到
        <p class="switch-link" @click="switchToWord">文字树洞</p>
        ，用文字分享你的小秘密
      </div>

      <!-- 切换到语音树洞 -->
      <div
        v-if="mode === 'text'"
        id="switchToVoice"
        class="switch-text"
      >
        切换到
        <p class="switch-link" @click="switchToVoice">语音树洞</p>
        ，用语音分享你的小秘密
      </div>
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
.secret-publish {
  min-height: 100vh;
  background: #f5f5f5;
  position: relative;
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

/* 表单容器：参考原版 secret-publish.css .form */
.form {
  position: relative;
  background-color: #fff;
  border-radius: 8px;
  margin: 10px;
  padding-bottom: 20px;
}

header {
  line-height: 2.5rem;
  text-align: center;
  font-weight: bolder;
  font-size: 1rem;
  border-bottom: 1px solid rgba(0, 0, 0, 0.11);
  position: relative;
}
header i {
  display: inline-block;
  width: 2.5rem;
  height: 2.5rem;
  background-size: 1rem;
  background-position: center;
  background-repeat: no-repeat;
  vertical-align: middle;
  position: absolute;
  cursor: pointer;
}
header .back {
  background-image: url(/img/secret/back1.png);
  left: 0;
}
header .back1 {
  background-image: url(/img/secret/back.png);
  left: 0;
}
header span {
  display: inline-block;
}
.btn {
  position: absolute;
  right: 1rem;
  top: 0;
  cursor: pointer;
  color: #3cb395;
}

.edit {
  height: 284px;
  position: relative;
}
.voice {
  height: 284px;
  position: relative;
}

/* 录音区域：限制触摸范围，避免遮挡底部点击 */
.voice-record-area {
  position: relative;
  height: 250px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}
.edit textarea {
  text-align: center;
  width: 100%;
  margin: 0 auto;
  border: none;
  font-size: 18px;
  overflow-x: hidden;
  line-height: 24px;
  background-color: inherit;
  color: inherit;
  height: auto;
  padding: 20px;
  resize: none;
}
textarea::-webkit-input-placeholder {
  color: inherit;
  line-height: 1.5rem;
  opacity: 0.6;
}
.length {
  position: absolute;
  bottom: 0.6rem;
  right: 1rem;
  font-size: 14px;
  color: #999;
}

.bar {
  border: solid #d5d5d5;
  border-width: 1px 0;
  padding: 10px;
  overflow: hidden;
  background: #fff;
}
.bar i {
  width: 23px;
  height: 23px;
  display: block;
  float: right;
  background: url(/img/secret/pallet.png) no-repeat center;
  background-size: 100%;
  cursor: pointer;
}
.bar .gray-pallet {
  background: url(/img/secret/pallet1.png) no-repeat center;
  background-size: 100%;
}

/* 底部控制区域，保证可点击 */
.publish-bottom-controls {
  position: relative;
  z-index: 10 !important;
  pointer-events: auto !important;
  background-color: #f5f5f5;
}

.themes {
  display: flex;
  margin-top: 10px;
  padding: 0 10px;
  gap: 10px;
}
.themes > div {
  flex: 1;
  height: 2.7rem;
  position: relative;
  border-radius: 4px;
  cursor: pointer;
}
.selected {
  background-image: url(/img/schedule/select.png);
  display: inline-block;
  width: 1.5rem;
  height: 1.5rem;
  background-size: 1.5rem;
  background-position: center;
  position: absolute;
  right: -0.5rem;
  top: -0.5rem;
}

/* 主题颜色：参考原版 secret-publish.css */
.theme1 {
  background-color: #fff;
  color: #000;
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

.switch-text {
  margin-top: 1rem;
  color: grey;
  text-align: center;
}
.switch-link {
  display: inline;
  color: deepskyblue;
  cursor: pointer;
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
