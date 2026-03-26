<script setup>
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import request from '../../utils/request'
import { uploadFileByPresignedUrl } from '../../utils/presignedUpload'
import { useToast } from '../../composables/useToast'
import CommunityHeader from '../../components/community/CommunityHeader.vue'

const router = useRouter()
const { t, locale } = useI18n()
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

const PUBLISH_COPY = {
  'zh-CN': {
    title: '发布资料',
    uploadAction: '上传',
    noticeTitle: '提示',
    genderFemale: '小姐姐',
    genderMale: '小哥哥',
    nicknamePlaceholder: '请输入你的昵称',
    gradePlaceholder: '请选择你的年级',
    areaPlaceholder: '请选择你的性别',
    facultyPlaceholder: '请输入你的专业',
    hometownPlaceholder: '请输入你的家乡',
    qqPlaceholder: '请输入你的QQ',
    wechatPlaceholder: '请输入你的微信',
    privacyHint: '在接受撩一下请求前，QQ和微信不会公开显示',
    warningHint: '请勿违规盗用他人照片或冒充他人，欢迎举报监督',
    contentPlaceholder: '什么样的TA会让你心动呢？谈谈你的理想对象，不超过100字',
    submitting: '提交中...',
    submitAction: '发布资料',
    invalidImageType: '不合法的图片文件类型',
    imageTooLarge: '图片文件不能超过5MB',
    nicknameInvalid: '昵称长度不合法（1-15字）',
    gradeRequired: '请选择年级',
    areaRequired: '请选择性别',
    facultyInvalid: '专业长度不合法（1-12字）',
    hometownInvalid: '家乡长度不合法（1-10字）',
    contactRequired: 'QQ号码和微信至少填写一个',
    contactInvalid: '联系方式长度不合法',
    contentRequired: '填一下你心目中的那个TA吧',
    contentTooLong: '心动条件不超过100字',
    uploading: '正在上传...',
    publishing: '正在发布...',
    publishSuccess: '发布成功'
  },
  'zh-HK': {
    title: '發布資料',
    uploadAction: '上傳',
    noticeTitle: '提示',
    genderFemale: '小姐姐',
    genderMale: '小哥哥',
    nicknamePlaceholder: '請輸入你的暱稱',
    gradePlaceholder: '請選擇你的年級',
    areaPlaceholder: '請選擇你的性別',
    facultyPlaceholder: '請輸入你的專業',
    hometownPlaceholder: '請輸入你的家鄉',
    qqPlaceholder: '請輸入你的QQ',
    wechatPlaceholder: '請輸入你的微信',
    privacyHint: '在接受撩一下請求前，QQ和微信不會公開顯示',
    warningHint: '請勿違規盜用他人照片或冒充他人，歡迎舉報監督',
    contentPlaceholder: '什麼樣的TA會讓你心動呢？談談你的理想對象，不超過100字',
    submitting: '提交中...',
    submitAction: '發布資料',
    invalidImageType: '不合法的圖片檔案類型',
    imageTooLarge: '圖片檔案不能超過5MB',
    nicknameInvalid: '暱稱長度不合法（1-15字）',
    gradeRequired: '請選擇年級',
    areaRequired: '請選擇性別',
    facultyInvalid: '專業長度不合法（1-12字）',
    hometownInvalid: '家鄉長度不合法（1-10字）',
    contactRequired: 'QQ號碼和微信至少填寫一個',
    contactInvalid: '聯絡方式長度不合法',
    contentRequired: '填一下你心目中的那個TA吧',
    contentTooLong: '心動條件不超過100字',
    uploading: '正在上傳...',
    publishing: '正在發布...',
    publishSuccess: '發布成功'
  },
  'zh-TW': {
    title: '發布資料',
    uploadAction: '上傳',
    noticeTitle: '提示',
    genderFemale: '小姐姐',
    genderMale: '小哥哥',
    nicknamePlaceholder: '請輸入你的暱稱',
    gradePlaceholder: '請選擇你的年級',
    areaPlaceholder: '請選擇你的性別',
    facultyPlaceholder: '請輸入你的專業',
    hometownPlaceholder: '請輸入你的家鄉',
    qqPlaceholder: '請輸入你的QQ',
    wechatPlaceholder: '請輸入你的微信',
    privacyHint: '在接受撩一下請求前，QQ和微信不會公開顯示',
    warningHint: '請勿違規盜用他人照片或冒充他人，歡迎檢舉監督',
    contentPlaceholder: '什麼樣的TA會讓你心動呢？談談你的理想對象，不超過100字',
    submitting: '提交中...',
    submitAction: '發布資料',
    invalidImageType: '不合法的圖片檔案類型',
    imageTooLarge: '圖片檔案不能超過5MB',
    nicknameInvalid: '暱稱長度不合法（1-15字）',
    gradeRequired: '請選擇年級',
    areaRequired: '請選擇性別',
    facultyInvalid: '專業長度不合法（1-12字）',
    hometownInvalid: '家鄉長度不合法（1-10字）',
    contactRequired: 'QQ號碼和微信至少填寫一個',
    contactInvalid: '聯絡方式長度不合法',
    contentRequired: '填一下你心目中的那個TA吧',
    contentTooLong: '心動條件不超過100字',
    uploading: '正在上傳...',
    publishing: '正在發布...',
    publishSuccess: '發布成功'
  },
  en: {
    title: 'Post Profile',
    uploadAction: 'Upload',
    noticeTitle: 'Notice',
    genderFemale: 'Female',
    genderMale: 'Male',
    nicknamePlaceholder: 'Enter your nickname',
    gradePlaceholder: 'Select your year',
    areaPlaceholder: 'Select your gender',
    facultyPlaceholder: 'Enter your major',
    hometownPlaceholder: 'Enter your hometown',
    qqPlaceholder: 'Enter your QQ',
    wechatPlaceholder: 'Enter your WeChat',
    privacyHint: 'Your QQ and WeChat will stay hidden until a message is accepted.',
    warningHint: 'Do not use someone else\'s photo or impersonate another person. Reports are welcome.',
    contentPlaceholder: 'What kind of person catches your eye? Share your ideal type in up to 100 characters.',
    submitting: 'Submitting...',
    submitAction: 'Post Profile',
    invalidImageType: 'Invalid image file type',
    imageTooLarge: 'Image files must be smaller than 5MB',
    nicknameInvalid: 'Nickname must be between 1 and 15 characters',
    gradeRequired: 'Please select your year',
    areaRequired: 'Please select your gender',
    facultyInvalid: 'Major must be between 1 and 12 characters',
    hometownInvalid: 'Hometown must be between 1 and 10 characters',
    contactRequired: 'Please provide either QQ or WeChat',
    contactInvalid: 'Contact information is too long',
    contentRequired: 'Tell us about the person you have in mind',
    contentTooLong: 'Your description must be within 100 characters',
    uploading: 'Uploading...',
    publishing: 'Publishing...',
    publishSuccess: 'Posted successfully'
  }
}

function resolveDatingLocale(value) {
  const normalized = (value || 'zh-CN').toLowerCase()
  if (normalized.startsWith('zh-hk')) return 'zh-HK'
  if (normalized.startsWith('zh-tw') || normalized.startsWith('zh-hant')) return 'zh-TW'
  if (normalized.startsWith('zh')) return 'zh-CN'
  return 'en'
}

const copy = computed(() => PUBLISH_COPY[resolveDatingLocale(locale.value)] || PUBLISH_COPY.en)
const gradeOptions = computed(() => [
  { label: t('grade.year.freshman'), value: 1 },
  { label: t('grade.year.sophomore'), value: 2 },
  { label: t('grade.year.junior'), value: 3 },
  { label: t('grade.year.senior'), value: 4 }
])
const areaOptions = computed(() => [
  { label: copy.value.genderFemale, value: 0 },
  { label: copy.value.genderMale, value: 1 }
])

function showDialog(msg) {
  dialogMessage.value = msg
  dialogVisible.value = true
}

function onFileChange(e) {
  const file = e.target.files[0]
  if (!file) return
  const allowTypes = ['image/jpg', 'image/jpeg', 'image/png', 'image/gif']
  if (!allowTypes.includes(file.type)) {
    showDialog(copy.value.invalidImageType)
    return
  }
  if (file.size > 5 * 1024 * 1024) {
    showDialog(copy.value.imageTooLarge)
    return
  }
  imageFile.value = file
  imagePreview.value = URL.createObjectURL(file)
}

function selectGrade() {
  const options = gradeOptions.value
  const idx = options.findIndex(o => o.value === formData.value.grade)
  const next = (idx + 1) % options.length
  formData.value.grade = options[next].value
  formData.value.gradeLabel = options[next].label
}

function selectArea() {
  const options = areaOptions.value
  const idx = options.findIndex(o => o.value === formData.value.area)
  const next = idx < 0 ? 0 : (idx + 1) % options.length
  formData.value.area = options[next].value
  formData.value.areaLabel = options[next].label
}

function openGradePicker() {
  selectGrade()
}
function openAreaPicker() {
  selectArea()
}

async function submit() {
  if (!formData.value.nickname || formData.value.nickname.trim().length === 0 || formData.value.nickname.trim().length > 15) {
    showDialog(copy.value.nicknameInvalid)
    return
  }
  if (formData.value.grade === '' || formData.value.grade === undefined) {
    showDialog(copy.value.gradeRequired)
    return
  }
  if (formData.value.area === '' && formData.value.areaLabel === '') {
    showDialog(copy.value.areaRequired)
    return
  }
  if (!formData.value.faculty || formData.value.faculty.trim().length === 0 || formData.value.faculty.trim().length > 12) {
    showDialog(copy.value.facultyInvalid)
    return
  }
  if (!formData.value.hometown || formData.value.hometown.trim().length === 0 || formData.value.hometown.trim().length > 10) {
    showDialog(copy.value.hometownInvalid)
    return
  }
  const hasQq = formData.value.qq && formData.value.qq.trim().length > 0
  const hasWechat = formData.value.wechat && formData.value.wechat.trim().length > 0
  if (!hasQq && !hasWechat) {
    showDialog(copy.value.contactRequired)
    return
  }
  if ((hasQq && formData.value.qq.trim().length > 15) || (hasWechat && formData.value.wechat.trim().length > 20)) {
    showDialog(copy.value.contactInvalid)
    return
  }
  if (!formData.value.content || formData.value.content.trim().length === 0) {
    showDialog(copy.value.contentRequired)
    return
  }
  if (formData.value.content.trim().length > 100) {
    showDialog(copy.value.contentTooLong)
    return
  }

  submitting.value = true
  toastLoading(imageFile.value ? copy.value.uploading : copy.value.publishing)
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
    toastSuccess(copy.value.publishSuccess)
    setTimeout(() => router.push('/dating/home'), 1500)
  } catch (_) {
    submitting.value = false
    hideLoading()
  }
}
</script>

<template>
  <div class="min-h-screen bg-[var(--c-bg)] pb-10">
    <CommunityHeader :title="copy.title" moduleColor="#ec4899" backTo="/dating/home" />

    <div class="w-[90%] mx-auto mt-4 p-6 bg-[var(--c-surface)] rounded-xl shadow-sm overflow-hidden animate-[slide-up_0.4s_ease_both]">
      <div class="text-[22px] text-pink-500 font-bold mb-6 pl-2">{{ copy.title }}</div>

      <!-- Photo upload -->
      <div class="w-full min-h-[200px] bg-[var(--c-bg)] rounded-lg mb-6 flex items-center justify-center overflow-hidden relative cursor-pointer" @click="$refs.fileInput.click()">
        <img v-if="imagePreview" :src="imagePreview" class="w-full h-auto max-h-80 object-cover" />
        <div v-else class="absolute inset-0 flex items-center justify-center">
          <span class="px-6 py-2.5 bg-pink-500 text-white rounded-full">{{ copy.uploadAction }}</span>
        </div>
        <input ref="fileInput" type="file" accept="image/*" class="opacity-0 absolute inset-0 w-full h-full cursor-pointer" @change="onFileChange" />
      </div>

      <!-- Form inputs -->
      <div class="my-6 space-y-3">
        <input type="text" class="w-full max-w-xs mx-auto block h-11 px-4 border-0 border-b-2 border-pink-500 bg-[var(--c-card)] text-base text-[var(--c-text-1)] placeholder:text-[var(--c-text-3)]" v-model="formData.nickname" :placeholder="copy.nicknamePlaceholder" />
        <input type="text" readonly class="w-full max-w-xs mx-auto block h-11 px-4 border-0 border-b-2 border-pink-500 bg-[var(--c-card)] text-base text-[var(--c-text-1)] placeholder:text-[var(--c-text-3)] cursor-pointer" :value="formData.gradeLabel" :placeholder="copy.gradePlaceholder" @click="openGradePicker" />
        <input type="text" readonly class="w-full max-w-xs mx-auto block h-11 px-4 border-0 border-b-2 border-pink-500 bg-[var(--c-card)] text-base text-[var(--c-text-1)] placeholder:text-[var(--c-text-3)] cursor-pointer" :value="formData.areaLabel" :placeholder="copy.areaPlaceholder" @click="openAreaPicker" />
        <input type="text" class="w-full max-w-xs mx-auto block h-11 px-4 border-0 border-b-2 border-pink-500 bg-[var(--c-card)] text-base text-[var(--c-text-1)] placeholder:text-[var(--c-text-3)]" v-model="formData.faculty" :placeholder="copy.facultyPlaceholder" />
        <input type="text" class="w-full max-w-xs mx-auto block h-11 px-4 border-0 border-b-2 border-pink-500 bg-[var(--c-card)] text-base text-[var(--c-text-1)] placeholder:text-[var(--c-text-3)]" v-model="formData.hometown" :placeholder="copy.hometownPlaceholder" />
        <input type="text" class="w-full max-w-xs mx-auto block h-11 px-4 border-0 border-b-2 border-pink-500 bg-[var(--c-card)] text-base text-[var(--c-text-1)] placeholder:text-[var(--c-text-3)]" v-model="formData.qq" :placeholder="copy.qqPlaceholder" />
        <input type="text" class="w-full max-w-xs mx-auto block h-11 px-4 border-0 border-b-2 border-pink-500 bg-[var(--c-card)] text-base text-[var(--c-text-1)] placeholder:text-[var(--c-text-3)]" v-model="formData.wechat" :placeholder="copy.wechatPlaceholder" />
      </div>

      <!-- Hint -->
      <div class="text-center mx-6 my-4 text-sm text-[var(--c-text-2)]">
        <span>{{ copy.privacyHint }}</span><br />
        <span class="text-red-600">{{ copy.warningHint }}</span>
      </div>

      <!-- Textarea + submit -->
      <div class="border-t-2 border-dashed border-[var(--c-divider)] pt-6 text-center">
        <textarea class="w-full max-w-xs mx-auto block p-4 border border-[var(--c-divider)] rounded-lg text-base min-h-[100px] box-border text-[var(--c-text-1)] placeholder:text-[var(--c-text-3)]" v-model="formData.content" :placeholder="copy.contentPlaceholder" rows="4"></textarea>
        <button type="button" class="mt-6 w-20 h-20 rounded-full bg-pink-500 text-white border-none text-xl cursor-pointer transition-opacity active:opacity-85 disabled:opacity-60" :disabled="submitting" @click="submit">
          {{ submitting ? copy.submitting : copy.submitAction }}
        </button>
      </div>
    </div>

    <!-- Dialog -->
    <div v-if="dialogVisible" class="fixed inset-0 bg-black/50 z-[1000]" @click="dialogVisible = false"></div>
    <div v-if="dialogVisible" class="fixed top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[280px] bg-[var(--c-surface)] rounded-xl overflow-hidden z-[1001] shadow-lg">
      <div class="text-center font-bold text-base py-4 text-[var(--c-text-1)]">{{ copy.noticeTitle }}</div>
      <div class="px-6 pb-4 text-center text-sm text-[var(--c-text-2)] leading-relaxed">{{ dialogMessage }}</div>
      <div class="border-t border-[var(--c-border)] flex">
        <a href="javascript:;" class="flex-1 text-center py-3 text-pink-500 font-medium no-underline" @click="dialogVisible = false">{{ t('common.confirm') }}</a>
      </div>
    </div>
  </div>
</template>
