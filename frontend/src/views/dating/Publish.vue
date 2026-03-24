<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import request from '../../utils/request'
import { uploadFileByPresignedUrl } from '../../utils/presignedUpload'
import { useToast } from '../../composables/useToast'
import CommunityHeader from '../../components/community/CommunityHeader.vue'

const router = useRouter()
const { success: toastSuccess, loading: toastLoading, hideLoading } = useToast()
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
  const idx = gradeOptions.findIndex(o => o.value === formData.value.grade)
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
  toastLoading(imageFile.value ? '正在上传...' : '正在发布...')
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
    toastSuccess('发布成功')
    setTimeout(() => router.push('/dating/home'), 1500)
  } catch (_) {
    submitting.value = false
    hideLoading()
  }
}
</script>

<template>
  <div class="min-h-screen bg-[var(--c-bg)] pb-10">
    <CommunityHeader title="发布资料" moduleColor="#ec4899" backTo="/dating/home" />

    <div class="w-[90%] mx-auto mt-4 p-6 bg-[var(--c-surface)] rounded-xl shadow-sm overflow-hidden animate-[slide-up_0.4s_ease_both]">
      <div class="text-[22px] text-pink-500 font-bold mb-6 pl-2">发布资料</div>

      <!-- Photo upload -->
      <div class="w-full min-h-[200px] bg-[var(--c-bg)] rounded-lg mb-6 flex items-center justify-center overflow-hidden relative cursor-pointer" @click="$refs.fileInput.click()">
        <img v-if="imagePreview" :src="imagePreview" class="w-full h-auto max-h-80 object-cover" />
        <div v-else class="absolute inset-0 flex items-center justify-center">
          <span class="px-6 py-2.5 bg-pink-500 text-white rounded-full">上传</span>
        </div>
        <input ref="fileInput" type="file" accept="image/*" class="opacity-0 absolute inset-0 w-full h-full cursor-pointer" @change="onFileChange" />
      </div>

      <!-- Form inputs -->
      <div class="my-6 space-y-3">
        <input type="text" class="w-full max-w-xs mx-auto block h-11 px-4 border-0 border-b-2 border-pink-500 bg-[var(--c-card)] text-base text-[var(--c-text-1)] placeholder:text-[var(--c-text-3)]" v-model="formData.nickname" placeholder="请输入你的昵称" />
        <input type="text" readonly class="w-full max-w-xs mx-auto block h-11 px-4 border-0 border-b-2 border-pink-500 bg-[var(--c-card)] text-base text-[var(--c-text-1)] placeholder:text-[var(--c-text-3)] cursor-pointer" :value="formData.gradeLabel" placeholder="请选择你的年级" @click="openGradePicker" />
        <input type="text" readonly class="w-full max-w-xs mx-auto block h-11 px-4 border-0 border-b-2 border-pink-500 bg-[var(--c-card)] text-base text-[var(--c-text-1)] placeholder:text-[var(--c-text-3)] cursor-pointer" :value="formData.areaLabel" placeholder="请选择你的性别" @click="openAreaPicker" />
        <input type="text" class="w-full max-w-xs mx-auto block h-11 px-4 border-0 border-b-2 border-pink-500 bg-[var(--c-card)] text-base text-[var(--c-text-1)] placeholder:text-[var(--c-text-3)]" v-model="formData.faculty" placeholder="请输入你的专业" />
        <input type="text" class="w-full max-w-xs mx-auto block h-11 px-4 border-0 border-b-2 border-pink-500 bg-[var(--c-card)] text-base text-[var(--c-text-1)] placeholder:text-[var(--c-text-3)]" v-model="formData.hometown" placeholder="请输入你的家乡" />
        <input type="text" class="w-full max-w-xs mx-auto block h-11 px-4 border-0 border-b-2 border-pink-500 bg-[var(--c-card)] text-base text-[var(--c-text-1)] placeholder:text-[var(--c-text-3)]" v-model="formData.qq" placeholder="请输入你的QQ" />
        <input type="text" class="w-full max-w-xs mx-auto block h-11 px-4 border-0 border-b-2 border-pink-500 bg-[var(--c-card)] text-base text-[var(--c-text-1)] placeholder:text-[var(--c-text-3)]" v-model="formData.wechat" placeholder="请输入你的微信" />
      </div>

      <!-- Hint -->
      <div class="text-center mx-6 my-4 text-sm text-[var(--c-text-2)]">
        <span>在接受撩一下请求前，QQ和微信不会公开显示</span><br />
        <span class="text-red-600">请勿违规盗用他人照片或冒充他人，欢迎举报监督</span>
      </div>

      <!-- Textarea + submit -->
      <div class="border-t-2 border-dashed border-[var(--c-divider)] pt-6 text-center">
        <textarea class="w-full max-w-xs mx-auto block p-4 border border-[var(--c-divider)] rounded-lg text-base min-h-[100px] box-border text-[var(--c-text-1)] placeholder:text-[var(--c-text-3)]" v-model="formData.content" placeholder="什么样的TA会让你心动呢？谈谈你的理想对象，不超过100字" rows="4"></textarea>
        <button type="button" class="mt-6 w-20 h-20 rounded-full bg-pink-500 text-white border-none text-xl cursor-pointer transition-opacity active:opacity-85 disabled:opacity-60" :disabled="submitting" @click="submit">
          {{ submitting ? '提交中...' : '发布资料' }}
        </button>
      </div>
    </div>

    <!-- Dialog -->
    <div v-if="dialogVisible" class="fixed inset-0 bg-black/50 z-[1000]" @click="dialogVisible = false"></div>
    <div v-if="dialogVisible" class="fixed top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[280px] bg-[var(--c-surface)] rounded-xl overflow-hidden z-[1001] shadow-lg">
      <div class="text-center font-bold text-base py-4 text-[var(--c-text-1)]">提示</div>
      <div class="px-6 pb-4 text-center text-sm text-[var(--c-text-2)] leading-relaxed">{{ dialogMessage }}</div>
      <div class="border-t border-[var(--c-border)] flex">
        <a href="javascript:;" class="flex-1 text-center py-3 text-pink-500 font-medium no-underline" @click="dialogVisible = false">确定</a>
      </div>
    </div>
  </div>
</template>
