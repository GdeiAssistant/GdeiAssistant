<script setup>
import { computed, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import request from '../../utils/request'
import { useToast } from '@/composables/useToast'
import CommunityHeader from '../../components/community/CommunityHeader.vue'
import { createCommunityGenderOptions } from '../community/communityContent'

const router = useRouter()
const { t } = useI18n()
const { success: toastSuccess, loading: toastLoading, hideLoading } = useToast()
const formData = ref({
  nickname: '',
  realName: '',
  myGender: '',
  receiverName: '',
  receiverGender: '',
  content: ''
})

const submitting = ref(false)
const dialogVisible = ref(false)
const dialogMessage = ref('')
const genderOptions = computed(() => createCommunityGenderOptions(t))

function showDialog(msg) {
  dialogMessage.value = msg
  dialogVisible.value = true
}

function submit() {
  const nickname = (formData.value.nickname || '').trim()
  const realname = (formData.value.realName || '').trim()
  const receiverName = (formData.value.receiverName || '').trim()
  const content = (formData.value.content || '').trim()

  if (!nickname) { showDialog(t('express.publish.nicknameRequired')); return }
  if (nickname.length > 10) { showDialog(t('express.publish.nicknameTooLong')); return }
  if (realname.length > 10) { showDialog(t('express.publish.realNameTooLong')); return }
  if (!formData.value.myGender) { showDialog(t('express.publish.myGenderRequired')); return }
  if (!receiverName) { showDialog(t('express.publish.receiverNameRequired')); return }
  if (receiverName.length > 10) { showDialog(t('express.publish.receiverNameTooLong')); return }
  if (!formData.value.receiverGender) { showDialog(t('express.publish.receiverGenderRequired')); return }
  if (!content) { showDialog(t('express.publish.contentRequired')); return }
  if (content.length > 250) { showDialog(t('express.publish.contentTooLong')); return }

  const mapGender = (v) => { if (v === 'male') return 0; if (v === 'female') return 1; return 2 }
  const payload = {
    nickname,
    realname,
    selfGender: mapGender(formData.value.myGender),
    name: receiverName,
    personGender: mapGender(formData.value.receiverGender),
    content
  }
  submitting.value = true
  toastLoading(t('express.publish.publishing'))
  request.post('/express', payload)
    .then(() => {
      hideLoading()
      toastSuccess(t('express.publish.publishSuccess'))
      setTimeout(() => router.push('/express/home'), 1500)
    })
    .catch(() => {
      submitting.value = false
      hideLoading()
    })
}
</script>

<template>
  <div class="bg-[var(--c-bg)] pb-20">
    <CommunityHeader :title="t('express.publish.title')" moduleColor="#f43f5e" backTo="/express/home" />

    <!-- 浅粉色粗体标题 -->
    <h2 class="text-center text-[22px] font-bold text-[#ffb3ba] mx-4 mt-4 mb-5 leading-tight">{{ t('express.bannerTitle') }}</h2>

    <!-- 手账风表单 -->
    <div>
      <!-- 第一个 form-section：你的信息 -->
      <div class="border-2 border-dashed border-[#81d4fa] rounded-lg pt-6 px-4 pb-4 mx-4 mt-6 mb-4 relative bg-[var(--c-surface)] shadow-sm">
        <span class="absolute -top-3 -left-0.5 bg-[#4fc3f7] text-white px-2.5 py-1 rounded-sm text-sm font-medium">{{ t('express.publish.myInfo') }}</span>
        <div>
          <div class="flex items-start py-3 border-b border-[var(--c-border)]">
            <label class="w-[70px] text-sm text-[var(--c-text-1)] leading-8">{{ t('profile.nickname') }}</label>
            <div class="flex-1">
              <input
                class="w-full h-7 text-sm text-[var(--c-text-1)] border-none outline-none bg-transparent"
                type="text"
                :placeholder="t('express.publish.nicknamePlaceholder')"
                v-model="formData.nickname"
              />
            </div>
          </div>
          <div class="flex items-start py-3 border-b border-[var(--c-border)]">
            <label class="w-[70px] text-sm text-[var(--c-text-1)] leading-8">{{ t('express.publish.realName') }}</label>
            <div class="flex-1">
              <input
                class="w-full h-7 text-sm text-[var(--c-text-1)] border-none outline-none bg-transparent"
                type="text"
                :placeholder="t('express.publish.realNamePlaceholder')"
                v-model="formData.realName"
              />
              <div class="text-xs text-[var(--c-text-3)] mt-1 leading-snug">{{ t('express.publish.realNameTip') }}</div>
            </div>
          </div>
          <div class="flex items-start py-3">
            <label class="w-[70px] text-sm text-[var(--c-text-1)] leading-8">{{ t('express.publish.gender') }}</label>
            <div class="flex-1">
              <select class="w-full text-sm text-[var(--c-text-1)] border-none outline-none bg-transparent appearance-none" v-model="formData.myGender">
                <option v-for="option in genderOptions" :key="`self-${option.value || 'empty'}`" :value="option.value">{{ option.label }}</option>
              </select>
            </div>
          </div>
        </div>
      </div>

      <!-- 第二个 form-section：TA的信息 -->
      <div class="border-2 border-dashed border-[#81d4fa] rounded-lg pt-6 px-4 pb-4 mx-4 mt-6 mb-4 relative bg-[var(--c-surface)] shadow-sm">
        <span class="absolute -top-3 -left-0.5 bg-[#4fc3f7] text-white px-2.5 py-1 rounded-sm text-sm font-medium">{{ t('express.publish.targetInfo') }}</span>
        <div>
          <div class="flex items-start py-3 border-b border-[var(--c-border)]">
            <label class="w-[70px] text-sm text-[var(--c-text-1)] leading-8">{{ t('express.publish.targetName') }}</label>
            <div class="flex-1">
              <input
                class="w-full h-7 text-sm text-[var(--c-text-1)] border-none outline-none bg-transparent"
                type="text"
                :placeholder="t('express.publish.receiverNamePlaceholder')"
                v-model="formData.receiverName"
              />
            </div>
          </div>
          <div class="flex items-start py-3">
            <label class="w-[70px] text-sm text-[var(--c-text-1)] leading-8">{{ t('express.publish.gender') }}</label>
            <div class="flex-1">
              <select class="w-full text-sm text-[var(--c-text-1)] border-none outline-none bg-transparent appearance-none" v-model="formData.receiverGender">
                <option v-for="option in genderOptions" :key="`target-${option.value || 'empty'}`" :value="option.value">{{ option.label }}</option>
              </select>
            </div>
          </div>
        </div>
      </div>

      <!-- 表白内容 -->
      <div class="border-2 border-dashed border-[#81d4fa] rounded-lg pt-6 px-4 pb-4 mx-4 mt-6 mb-4 relative bg-[var(--c-surface)] shadow-sm">
        <span class="absolute -top-3 -left-0.5 bg-[#4fc3f7] text-white px-2.5 py-1 rounded-sm text-sm font-medium">{{ t('express.publish.contentTitle') }}</span>
        <div>
          <div class="py-3">
            <textarea
              class="w-full min-h-[80px] text-sm text-[var(--c-text-1)] border-none outline-none bg-transparent py-1 resize-y"
              :placeholder="t('express.publish.contentPlaceholder')"
              rows="4"
              v-model="formData.content"
            ></textarea>
          </div>
        </div>
      </div>

      <div class="px-4 pt-5 pb-10">
        <button
          type="button"
          class="w-full h-11 flex justify-center items-center bg-[#f43f5e] text-white rounded-full text-base font-medium border-none cursor-pointer transition-opacity duration-200 active:opacity-85 disabled:opacity-60 disabled:cursor-not-allowed"
          :disabled="submitting"
          @click="submit"
        >
          {{ submitting ? t('express.publish.submitting') : t('express.publish.submitAction') }}
        </button>
      </div>
    </div>

    <!-- 提示对话框 -->
    <div v-if="dialogVisible">
      <div class="fixed inset-0 bg-black/50 z-[1000]" @click="dialogVisible = false"></div>
      <div class="fixed top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[80%] max-w-[320px] bg-[var(--c-surface)] rounded-xl z-[1001] overflow-hidden">
        <div class="text-center font-semibold text-base text-[var(--c-text-1)] py-4">{{ t('common.hint') }}</div>
        <div class="px-5 pb-4 text-sm text-[var(--c-text-1)] text-center">{{ dialogMessage }}</div>
        <div class="flex border-t border-[var(--c-border)]">
          <button class="flex-1 py-3 text-center text-sm text-[#f43f5e] font-semibold bg-transparent border-none cursor-pointer" @click="dialogVisible = false">{{ t('common.confirm') }}</button>
        </div>
      </div>
    </div>
  </div>
</template>
