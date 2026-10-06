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
  area: '',
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

const PUBLISH_KEYS = ["title","uploadAction","noticeTitle","genderFemale","genderMale","nicknamePlaceholder","gradePlaceholder","areaPlaceholder","facultyPlaceholder","hometownPlaceholder","qqPlaceholder","wechatPlaceholder","privacyHint","warningHint","contentPlaceholder","submitting","submitAction","invalidImageType","imageTooLarge","nicknameInvalid","gradeRequired","areaRequired","facultyInvalid","hometownInvalid","contactRequired","contactInvalid","contentRequired","contentTooLong","uploading","publishing","publishSuccess"]
const copy = computed(() => Object.fromEntries(PUBLISH_KEYS.map(key => [key, t(`dating.publish.${key}`)])))
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

const selectedGradeLabel = computed(() => gradeOptions.value.find(option => option.value === formData.value.grade)?.label || '')
const selectedAreaLabel = computed(() => areaOptions.value.find(option => option.value === formData.value.area)?.label || '')

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
}

function selectArea() {
  const options = areaOptions.value
  const idx = options.findIndex(o => o.value === formData.value.area)
  const next = idx < 0 ? 0 : (idx + 1) % options.length
  formData.value.area = options[next].value
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
  <div class="community-dating-page community-dating-publish-page min-h-screen bg-[var(--c-bg)] pb-10">
    <CommunityHeader :title="copy.title" moduleColor="var(--c-dating)" backTo="/dating/home" />

    <div class="community-dating-shell w-[90%] mx-auto mt-4 p-6 rounded-xl shadow-sm overflow-hidden animate-[slide-up_0.4s_ease_both]">
      <div class="community-dating-shell__title text-[22px] font-bold mb-6 pl-2">{{ copy.title }}</div>

      <!-- Photo upload -->
      <div class="community-dating-upload w-full min-h-[200px] rounded-lg mb-6 flex items-center justify-center overflow-hidden relative cursor-pointer" @click="$refs.fileInput.click()">
        <img v-if="imagePreview" :src="imagePreview" class="w-full h-auto max-h-80 object-cover" />
        <div v-else class="absolute inset-0 flex items-center justify-center">
          <span class="community-dating-upload__cta px-6 py-2.5 text-white rounded-full">{{ copy.uploadAction }}</span>
        </div>
        <input ref="fileInput" type="file" accept="image/*" class="opacity-0 absolute inset-0 w-full h-full cursor-pointer" @change="onFileChange" />
      </div>

      <!-- Form inputs -->
      <div class="my-6 space-y-3">
        <input type="text" class="w-full max-w-xs mx-auto block h-11 px-4 border-0 border-b border-[var(--c-divider)] focus:border-[var(--c-primary)] outline-none bg-[var(--c-card)] text-base text-[var(--c-text-1)] placeholder:text-[var(--c-text-3)]" v-model="formData.nickname" :placeholder="copy.nicknamePlaceholder" />
        <input type="text" readonly class="w-full max-w-xs mx-auto block h-11 px-4 border-0 border-b border-[var(--c-divider)] focus:border-[var(--c-primary)] outline-none bg-[var(--c-card)] text-base text-[var(--c-text-1)] placeholder:text-[var(--c-text-3)] cursor-pointer" :value="selectedGradeLabel" :placeholder="copy.gradePlaceholder" @click="openGradePicker" />
        <input type="text" readonly class="w-full max-w-xs mx-auto block h-11 px-4 border-0 border-b border-[var(--c-divider)] focus:border-[var(--c-primary)] outline-none bg-[var(--c-card)] text-base text-[var(--c-text-1)] placeholder:text-[var(--c-text-3)] cursor-pointer" :value="selectedAreaLabel" :placeholder="copy.areaPlaceholder" @click="openAreaPicker" />
        <input type="text" class="w-full max-w-xs mx-auto block h-11 px-4 border-0 border-b border-[var(--c-divider)] focus:border-[var(--c-primary)] outline-none bg-[var(--c-card)] text-base text-[var(--c-text-1)] placeholder:text-[var(--c-text-3)]" v-model="formData.faculty" :placeholder="copy.facultyPlaceholder" />
        <input type="text" class="w-full max-w-xs mx-auto block h-11 px-4 border-0 border-b border-[var(--c-divider)] focus:border-[var(--c-primary)] outline-none bg-[var(--c-card)] text-base text-[var(--c-text-1)] placeholder:text-[var(--c-text-3)]" v-model="formData.hometown" :placeholder="copy.hometownPlaceholder" />
        <input type="text" class="w-full max-w-xs mx-auto block h-11 px-4 border-0 border-b border-[var(--c-divider)] focus:border-[var(--c-primary)] outline-none bg-[var(--c-card)] text-base text-[var(--c-text-1)] placeholder:text-[var(--c-text-3)]" v-model="formData.qq" :placeholder="copy.qqPlaceholder" />
        <input type="text" class="w-full max-w-xs mx-auto block h-11 px-4 border-0 border-b border-[var(--c-divider)] focus:border-[var(--c-primary)] outline-none bg-[var(--c-card)] text-base text-[var(--c-text-1)] placeholder:text-[var(--c-text-3)]" v-model="formData.wechat" :placeholder="copy.wechatPlaceholder" />
      </div>

      <!-- Hint -->
      <div class="text-center mx-6 my-4 text-sm text-[var(--c-text-2)]">
        <span>{{ copy.privacyHint }}</span><br />
        <span class="community-dating-warning">{{ copy.warningHint }}</span>
      </div>

      <!-- Textarea + submit -->
      <div class="border-t-2 border-dashed border-[var(--c-divider)] pt-6 text-center">
        <textarea class="ui-control w-full max-w-xs mx-auto block p-4 border border-[var(--c-divider)] rounded-lg text-base min-h-[100px] box-border text-[var(--c-text-1)] placeholder:text-[var(--c-text-3)]" v-model="formData.content" :placeholder="copy.contentPlaceholder" rows="4"></textarea>
        <button type="button" class="community-dating-submit mt-6 w-20 h-20 rounded-full text-white border-none text-xl cursor-pointer transition-opacity active:opacity-85 disabled:opacity-60" :disabled="submitting" @click="submit">
          {{ submitting ? copy.submitting : copy.submitAction }}
        </button>
      </div>
    </div>

    <!-- Dialog -->
    <div v-if="dialogVisible" class="ui-scrim fixed inset-0 bg-black/50 z-[1000]" @click="dialogVisible = false"></div>
    <div v-if="dialogVisible" class="ui-modal fixed top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[280px] bg-[var(--c-surface)] rounded-xl overflow-hidden z-[1001] shadow-lg">
      <div class="text-center font-bold text-base py-4 text-[var(--c-text-1)]">{{ copy.noticeTitle }}</div>
      <div class="px-6 pb-4 text-center text-sm text-[var(--c-text-2)] leading-relaxed">{{ dialogMessage }}</div>
      <div class="border-t border-[var(--c-border)] flex">
        <a href="javascript:;" class="flex-1 text-center py-3 text-[var(--c-dating)] font-medium no-underline" @click="dialogVisible = false">{{ t('common.confirm') }}</a>
      </div>
    </div>
  </div>
</template>

<style scoped>
.community-dating-shell {
  background: var(--c-surface);
  border: 1px solid var(--c-border);
}

.community-dating-shell__title {
  color: var(--c-primary);
}

.community-dating-upload {
  background: color-mix(in srgb, var(--c-dating) 6%, var(--c-bg));
  border: 1px dashed var(--c-border);
}

.community-dating-upload__cta {
  background: var(--c-primary);
}

.community-dating-submit {
  background: var(--c-primary);
}

.community-dating-warning {
  color: var(--c-warning);
}
</style>
