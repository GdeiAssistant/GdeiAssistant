<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import request from '../../utils/request'

const router = useRouter()
const formData = ref({
  nickname: '',
  grade: '',
  gradeLabel: '',
  area: '',
  areaLabel: '',
  faculty: '',
  hometown: '',
  qq: '',
  wechat: '',
  content: ''
})
const imagePreview = ref('')
const imageFile = ref(null)
const submitting = ref(false)
const dialogVisible = ref(false)
const dialogMessage = ref('')

const gradeOptions = [
  { label: '大一', value: 1 },
  { label: '大二', value: 2 },
  { label: '大三', value: 3 },
  { label: '大四', value: 4 }
]
const areaOptions = [
  { label: '小姐姐', value: 0 },
  { label: '小哥哥', value: 1 }
]

function showDialog(msg) {
  dialogMessage.value = msg
  dialogVisible.value = true
}

function onFileChange(e) {
  const file = e.target.files[0]
  if (!file) return
  const allowTypes = ['image/jpg', 'image/jpeg', 'image/png', 'image/gif']
  if (!allowTypes.includes(file.type)) {
    showDialog('不合法的图片文件类型')
    return
  }
  if (file.size > 5 * 1024 * 1024) {
    showDialog('图片文件不能超过5MB')
    return
  }
  imageFile.value = file
  imagePreview.value = URL.createObjectURL(file)
}

function selectGrade() {
  const labels = gradeOptions.map(o => o.label)
  const idx = gradeOptions.findIndex(o => o.value === formData.value.grade)
  const current = idx >= 0 ? [labels[idx]] : [labels[0]]
  // 简单模拟选择：点击后选下一项或第一项
  const next = (idx + 1) % gradeOptions.length
  formData.value.grade = gradeOptions[next].value
  formData.value.gradeLabel = gradeOptions[next].label
}

function selectArea() {
  const idx = areaOptions.findIndex(o => o.value === formData.value.area)
  const next = idx < 0 ? 0 : (idx + 1) % areaOptions.length
  formData.value.area = areaOptions[next].value
  formData.value.areaLabel = areaOptions[next].label
}

function openGradePicker() {
  selectGrade()
}
function openAreaPicker() {
  selectArea()
}

function submit() {
  if (!imagePreview.value) {
    showDialog('上传一张美美的照片吧')
    return
  }
  if (!formData.value.nickname || formData.value.nickname.trim().length === 0 || formData.value.nickname.trim().length > 15) {
    showDialog('昵称长度不合法（1-15字）')
    return
  }
  if (formData.value.grade === '' || formData.value.grade === undefined) {
    showDialog('请选择年级')
    return
  }
  if (formData.value.area === '' && formData.value.areaLabel === '') {
    showDialog('请选择性别')
    return
  }
  if (!formData.value.faculty || formData.value.faculty.trim().length === 0 || formData.value.faculty.trim().length > 12) {
    showDialog('专业长度不合法（1-12字）')
    return
  }
  if (!formData.value.hometown || formData.value.hometown.trim().length === 0 || formData.value.hometown.trim().length > 10) {
    showDialog('家乡长度不合法（1-10字）')
    return
  }
  const hasQq = formData.value.qq && formData.value.qq.trim().length > 0
  const hasWechat = formData.value.wechat && formData.value.wechat.trim().length > 0
  if (!hasQq && !hasWechat) {
    showDialog('QQ号码和微信至少填写一个')
    return
  }
  if ((hasQq && formData.value.qq.trim().length > 15) || (hasWechat && formData.value.wechat.trim().length > 20)) {
    showDialog('联系方式长度不合法')
    return
  }
  if (!formData.value.content || formData.value.content.trim().length === 0) {
    showDialog('填一下你心目中的那个TA吧')
    return
  }
  if (formData.value.content.trim().length > 100) {
    showDialog('心动条件不超过100字')
    return
  }

  submitting.value = true
  const payload = new FormData()
  if (imageFile.value) payload.append('image', imageFile.value)
  payload.append('nickname', formData.value.nickname.trim())
  payload.append('grade', String(formData.value.grade))
  payload.append('area', String(formData.value.area))
  payload.append('faculty', formData.value.faculty.trim())
  payload.append('hometown', formData.value.hometown.trim())
  payload.append('content', formData.value.content.trim())
  if (hasQq) payload.append('qq', formData.value.qq.trim())
  if (hasWechat) payload.append('wechat', formData.value.wechat.trim())

  request.post('/dating/publish', payload)
    .then(() => {
      showDialog('发布成功')
      setTimeout(() => router.push('/dating/home'), 1500)
    })
    .catch(() => { submitting.value = false })
}
</script>

<template>
  <div class="dating-publish">
    <div class="dating-header unified-header">
      <span class="dating-header__back" @click="router.push('/dating/home')">返回</span>
      <h1 class="dating-header__title">出卖室友</h1>
      <span class="dating-header__placeholder"></span>
    </div>

    <div class="dating-publish__box">
      <div class="dating-publish__title">发布信息</div>

      <div class="dating-photo" @click="$refs.fileInput.click()">
        <img v-if="imagePreview" :src="imagePreview" class="dating-photo__img" />
        <div v-else class="dating-photo__placeholder">
          <span class="dating-photo__btn">上传</span>
        </div>
        <input ref="fileInput" type="file" accept="image/*" class="dating-photo__input" @change="onFileChange" />
      </div>

      <div class="dating-form">
        <p><input type="text" class="input-box" v-model="formData.nickname" placeholder="请输入你的昵称" /></p>
        <p><input type="text" readonly class="input-box" :value="formData.gradeLabel" placeholder="请选择你的年级" @click="openGradePicker" /></p>
        <p><input type="text" readonly class="input-box" :value="formData.areaLabel" placeholder="请选择你的性别" @click="openAreaPicker" /></p>
        <p><input type="text" class="input-box" v-model="formData.faculty" placeholder="请输入你的专业" /></p>
        <p><input type="text" class="input-box" v-model="formData.hometown" placeholder="请输入你的家乡" /></p>
        <p><input type="text" class="input-box" v-model="formData.qq" placeholder="请输入你的QQ" /></p>
        <p><input type="text" class="input-box" v-model="formData.wechat" placeholder="请输入你的微信" /></p>
      </div>

      <div class="dating-hint">
        <span>在接受撩一下请求前，QQ和微信不会公开显示</span><br />
        <span class="dating-hint__warn">请勿违规盗用他人照片或冒充他人，欢迎举报监督</span>
      </div>

      <div class="dating-textarea-wrap">
        <textarea class="dating-textarea" v-model="formData.content" placeholder="什么样的TA会让你心动呢？谈谈你的理想对象，不超过100字" rows="4"></textarea>
        <button type="button" class="circle-btn circle-btn--submit" :disabled="submitting" @click="submit">
          {{ submitting ? '提交中...' : '发布' }}
        </button>
      </div>
    </div>

    <div v-if="dialogVisible" class="weui-mask" @click="dialogVisible = false"></div>
    <div v-if="dialogVisible" class="weui-dialog">
      <div class="weui-dialog__hd"><strong class="weui-dialog__title">提示</strong></div>
      <div class="weui-dialog__bd">{{ dialogMessage }}</div>
      <div class="weui-dialog__ft">
        <a href="javascript:;" class="weui-dialog__btn weui-dialog__btn_primary" @click="dialogVisible = false">确定</a>
      </div>
    </div>
  </div>
</template>

<style scoped>
.dating-publish { background: #eee; min-height: 100vh; padding-bottom: 40px; }
.dating-header.unified-header {
  display: flex; align-items: center; justify-content: space-between;
  height: 44px; padding: 0 12px;
  background: linear-gradient(180deg, #78e2d1 0%, #6dcbbd 100%);
  color: #fff;
}
.dating-header__back { color: #fff; cursor: pointer; min-width: 48px; font-size: 14px; }
.dating-header__title { flex: 1; text-align: center; font-size: 16px; margin: 0; }
.dating-header__placeholder { min-width: 48px; }

.dating-publish__box { width: 90%; margin: 0 auto; background: #fff; overflow: hidden; padding: 15px; margin-top: 10px; border-radius: 8px; }
.dating-publish__title { font-size: 22px; color: #6dcbbd; font-weight: bold; margin-bottom: 15px; padding-left: 8px; }

.dating-photo {
  width: 100%; min-height: 200px; background: #eee; border-radius: 8px; margin-bottom: 15px;
  display: flex; align-items: center; justify-content: center; overflow: hidden; position: relative;
}
.dating-photo__img { width: 100%; height: auto; max-height: 320px; object-fit: cover; }
.dating-photo__placeholder { position: absolute; inset: 0; display: flex; align-items: center; justify-content: center; }
.dating-photo__input { opacity: 0; position: absolute; inset: 0; width: 100%; height: 100%; cursor: pointer; }
.dating-photo__btn { padding: 10px 24px; background: #2ee9d0; color: #fff; border-radius: 20px; }
.dating-photo-hint { text-align: center; margin-bottom: 15px; }
.circle-btn { padding: 10px 28px; background: #2ee9d0; color: #fff; border: none; border-radius: 24px; font-size: 16px; cursor: pointer; }
.circle-btn:disabled { opacity: 0.6; }
.circle-btn--submit { width: 80px; height: 80px; border-radius: 50%; font-size: 18px; margin-top: 15px; }

.dating-form { margin: 15px 0; }
.input-box {
  width: 100%; max-width: 320px; margin: 8px auto; display: block;
  height: 44px; padding: 0 12px; border: none; border-bottom: 2px solid #2ee8d0; background: #fff; font-size: 14px;
}
.dating-hint { text-align: center; margin: 10px 15px; font-size: 12px; color: #666; }
.dating-hint__warn { color: #e53935; }
.dating-textarea-wrap { border-top: 2px dashed #eee; padding-top: 15px; text-align: center; }
.dating-textarea {
  width: 100%; max-width: 320px; margin: 0 auto 15px; padding: 12px; border: 1px solid #ddd; border-radius: 8px;
  font-size: 14px; min-height: 100px; display: block; box-sizing: border-box;
}

.weui-mask { position: fixed; inset: 0; background: rgba(0,0,0,0.6); z-index: 1000; }
.weui-dialog {
  position: fixed; top: 50%; left: 50%; transform: translate(-50%, -50%);
  width: 85%; max-width: 300px; background: #fff; border-radius: 8px; z-index: 1001; overflow: hidden;
}
.weui-dialog__hd { padding: 16px; text-align: center; }
.weui-dialog__title { font-size: 17px; color: #333; }
.weui-dialog__bd { padding: 10px 20px; text-align: center; font-size: 15px; color: #666; }
.weui-dialog__ft { display: flex; border-top: 1px solid #eee; }
.weui-dialog__btn { flex: 1; padding: 14px; text-align: center; color: #6dcbbd; text-decoration: none; }
</style>
