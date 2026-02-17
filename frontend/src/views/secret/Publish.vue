<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import request from '../../utils/request'

const router = useRouter()

// 模式：0=文字，1=语音
const mode = ref('text') // 'text' or 'voice'
const formData = ref({
  content: '',
  theme: 1, // 默认主题1
  timer: 0 // 0=不删除，1=24小时后删除
})
const dialogVisible = ref(false)
const dialogMessage = ref('')
const showDialog = (msg) => {
  dialogMessage.value = msg
  dialogVisible.value = true
}

// 语音相关状态
const recording = ref(false)
const voiceState = ref('未录音')
const voiceVolume = ref(0)
const showThemes = ref(false)

// 切换到文字树洞
const switchToWord = () => {
  mode.value = 'text'
}

// 切换到语音树洞
const switchToVoice = () => {
  mode.value = 'voice'
}

// 开始录音
const startRecord = () => {
  recording.value = true
  voiceState.value = '正在录音，还剩60秒'
  // 模拟录音音量
  const interval = setInterval(() => {
    if (recording.value) {
      voiceVolume.value = Math.min(voiceVolume.value + 5, 100)
    } else {
      clearInterval(interval)
    }
  }, 200)
}

// 停止录音
const stopRecord = () => {
  recording.value = false
  voiceState.value = '播放录音'
  voiceVolume.value = 0
}

// 选择主题
const selectTheme = (themeNum) => {
  formData.value.theme = themeNum
}

// 提交
const submit = () => {
  if (mode.value === 'text') {
    if (!formData.value.content || formData.value.content.trim() === '') {
      showDialog('树洞内容不能为空！')
      return
    }
    if (formData.value.content.length > 100) {
      showDialog('树洞内容长度超过限制！')
      return
    }
    // 提交文字树洞
    request.post('/secret/info', {
      content: formData.value.content,
      theme: formData.value.theme,
      type: 0,
      timer: formData.value.timer
    }).then(() => {
      router.push('/secret/home')
    }).catch(err => {
      showDialog(err.response?.data?.message || '提交失败')
    })
  } else {
    // 语音树洞（模拟）
    if (voiceState.value === '未录音') {
      showDialog('未采集到任何录音信息')
      return
    }
    showDialog('语音树洞功能需要真实录音API支持')
  }
}

// 字数统计
const remainingChars = computed(() => {
  return 100 - formData.value.content.length
})

onMounted(() => {
  // 随机初始主题
  const rand = Math.ceil(Math.random() * 12)
  formData.value.theme = rand
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
          <label class="btn" @click="submit">发布</label>
        </header>
        <div class="edit" style="text-align: center">
          <!-- 语音树洞 -->
          <div
            v-if="mode === 'voice'"
            id="voice"
            class="voice-record-area"
            @touchstart.prevent="startRecord"
            @touchend.prevent="stopRecord"
            @mousedown.prevent="startRecord"
            @mouseup.prevent="stopRecord"
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
              长按开始录音，最长不超过60秒
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
          <div v-if="mode === 'voice'" @click="stopRecord">
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
            >{{ voiceState }}</p>
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
