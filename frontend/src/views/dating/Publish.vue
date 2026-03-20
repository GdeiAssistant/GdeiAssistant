<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import request from '../../utils/request'
import { uploadFileByPresignedUrl } from '../../utils/presignedUpload'
import CommunityHeader from '../../components/community/CommunityHeader.vue'

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

function showLoading(text = '正在上传...') {
  const weui = typeof window !== 'undefined' && window.weui
  if (weui && typeof weui.loading === 'function') weui.loading(text)
}

function hideLoading() {
  const weui = typeof window !== 'undefined' && window.weui
  if (weui && typeof weui.hideLoading === 'function') weui.hideLoading()
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

async function submit() {
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
  showLoading(imageFile.value ? '正在上传...' : '正在发布...')
  try {
    const payload = new FormData()
    if (imageFile.value) {
      const imageKey = await uploadFileByPresignedUrl(imageFile.value)
      payload.append('imageKey', imageKey)
    }
    payload.append('nickname', formData.value.nickname.trim())
    payload.append('grade', String(formData.value.grade))
    payload.append('area', String(formData.value.area))
    payload.append('faculty', formData.value.faculty.trim())
    payload.append('hometown', formData.value.hometown.trim())
    payload.append('content', formData.value.content.trim())
    if (hasQq) payload.append('qq', formData.value.qq.trim())
    if (hasWechat) payload.append('wechat', formData.value.wechat.trim())

    await request.post('/dating/profile', payload)
    hideLoading()
    const weui = typeof window !== 'undefined' && window.weui
    if (weui && typeof weui.toast === 'function') weui.toast('发布成功', { duration: 1500 })
    setTimeout(() => router.push('/dating/home'), 1500)
  } catch (_) {
    submitting.value = false
    hideLoading()
  }
}
</script>

<template>
  <div class="community-page dating-publish">
    <CommunityHeader title="发布资料" moduleColor="#ec4899" backTo="/dating/home" />

    <div class="community-card dating-publish__box" style="--module-color: #ec4899">
      <div class="dating-publish__title">发布资料</div>

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
          {{ submitting ? '提交中...' : '发布资料' }}
        </button>
      </div>
    </div>

    <div v-if="dialogVisible" class="community-dialog-mask" @click="dialogVisible = false"></div>
    <div v-if="dialogVisible" class="community-dialog" style="--module-color: #ec4899">
      <div class="community-dialog__title">提示</div>
      <div class="community-dialog__body">{{ dialogMessage }}</div>
      <div class="community-dialog__footer">
        <a href="javascript:;" class="community-dialog__btn community-dialog__btn--confirm" @click="dialogVisible = false">确定</a>
      </div>
    </div>
  </div>
</template>

<style scoped>
.dating-publish { padding-bottom: 40px; }

.dating-publish__box {
  width: 90%;
  margin: var(--space-md) auto 0;
  padding: var(--space-lg);
  overflow: hidden;
  animation: community-slide-up 0.4s ease both;
}
.dating-publish__title {
  font-size: 22px;
  color: var(--c-dating);
  font-weight: bold;
  margin-bottom: var(--space-lg);
  padding-left: var(--space-sm);
}

.dating-photo {
  width: 100%; min-height: 200px; background: var(--c-bg); border-radius: var(--radius-sm); margin-bottom: var(--space-lg);
  display: flex; align-items: center; justify-content: center; overflow: hidden; position: relative;
}
.dating-photo__img { width: 100%; height: auto; max-height: 320px; object-fit: cover; }
.dating-photo__placeholder { position: absolute; inset: 0; display: flex; align-items: center; justify-content: center; }
.dating-photo__input { opacity: 0; position: absolute; inset: 0; width: 100%; height: 100%; cursor: pointer; }
.dating-photo__btn {
  padding: 10px 24px;
  background: var(--c-dating);
  color: #fff;
  border-radius: var(--radius-full);
}
.dating-photo-hint { text-align: center; margin-bottom: var(--space-lg); }
.circle-btn {
  padding: 10px 28px;
  background: var(--c-dating);
  color: #fff;
  border: none;
  border-radius: var(--radius-full);
  font-size: var(--font-lg);
  cursor: pointer;
  transition: opacity 0.2s;
}
.circle-btn:active { opacity: 0.85; }
.circle-btn:disabled { opacity: 0.6; }
.circle-btn--submit { width: 80px; height: 80px; border-radius: 50%; font-size: var(--font-xl); margin-top: var(--space-lg); }

.dating-form { margin: var(--space-lg) 0; }
.input-box {
  width: 100%; max-width: 320px; margin: var(--space-sm) auto; display: block;
  height: 44px; padding: 0 var(--space-md); border: none; border-bottom: 2px solid var(--c-dating); background: var(--c-card); font-size: var(--font-base);
  color: var(--c-text-1);
}
.input-box::placeholder { color: var(--c-text-3); }
.dating-hint { text-align: center; margin: var(--space-md) var(--space-lg); font-size: var(--font-sm); color: var(--c-text-2); }
.dating-hint__warn { color: #e53935; }
.dating-textarea-wrap { border-top: 2px dashed var(--c-divider); padding-top: var(--space-lg); text-align: center; }
.dating-textarea {
  width: 100%; max-width: 320px; margin: 0 auto var(--space-lg); padding: var(--space-md); border: 1px solid var(--c-divider); border-radius: var(--radius-sm);
  font-size: var(--font-base); min-height: 100px; display: block; box-sizing: border-box;
  color: var(--c-text-1);
}
.dating-textarea::placeholder { color: var(--c-text-3); }
</style>
