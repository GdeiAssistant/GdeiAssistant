<script setup>
import { computed, ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import request from '../../utils/request'
import CommunityHeader from '../../components/community/CommunityHeader.vue'

const route = useRoute()
const router = useRouter()
const { t, locale } = useI18n()
const item = ref(null)
const pickContent = ref('')
const pickSubmitting = ref(false)
const pickSuccessVisible = ref(false)
const dialogVisible = ref(false)
const dialogMessage = ref('')

function resolveDatingLocale(value) {
  const normalized = (value || 'zh-CN').toLowerCase()
  if (normalized.startsWith('zh-hk') || normalized.startsWith('zh-mo') || normalized.startsWith('zh-hant-hk') || normalized.startsWith('zh-hant-mo')) return 'zh-HK'
  if (normalized.startsWith('zh-tw') || normalized.startsWith('zh-hant')) return 'zh-TW'
  if (normalized.startsWith('zh')) return 'zh-CN'
  if (normalized.startsWith('ja')) return 'ja'
  if (normalized.startsWith('ko')) return 'ko'
  if (normalized.startsWith('en')) return 'en'
  return 'en'
}

const copy = computed(() => ({
  unknownGrade: t('dating.detail.unknownGrade'),
  noticeTitle: t('dating.detail.noticeTitle'),
  gradeLabel: t('dating.detail.gradeLabel'),
  facultyLabel: t('dating.detail.facultyLabel'),
  hometownLabel: t('dating.detail.hometownLabel'),
  qqLabel: t('dating.detail.qqLabel'),
  wechatLabel: t('dating.detail.wechatLabel'),
  hiddenContact: t('dating.detail.hiddenContact'),
  placeholder: t('dating.detail.placeholder'),
  submitAction: t('dating.detail.submitAction'),
  success: t('dating.detail.success'),
  emptyMessage: t('dating.detail.emptyMessage'),
  tooLongMessage: t('dating.detail.tooLongMessage'),
  locale: resolveDatingLocale(locale.value)
}))

function getGradeText(grade) {
  const map = {
    1: t('grade.year.freshman'),
    2: t('grade.year.sophomore'),
    3: t('grade.year.junior'),
    4: t('grade.year.senior')
  }
  return map[grade] || copy.value.unknownGrade
}

function showDialog(msg) {
  dialogMessage.value = msg
  dialogVisible.value = true
}

function submitPick() {
  const text = pickContent.value && pickContent.value.trim()
  if (!text) {
    showDialog(copy.value.emptyMessage)
    return
  }
  if (text.length > 50) {
    showDialog(copy.value.tooLongMessage)
    return
  }
  pickSubmitting.value = true
  const params = new URLSearchParams()
  params.append('profileId', String(route.params.id))
  params.append('content', text)
  request.post('/dating/pick', params)
    .then(() => {
      pickSuccessVisible.value = true
      pickContent.value = ''
      pickSubmitting.value = false
      setTimeout(() => { pickSuccessVisible.value = false }, 1500)
    })
    .catch(() => { pickSubmitting.value = false })
}

onMounted(async () => {
  try {
    const res = await request.get(`/dating/profile/id/${route.params.id}`)
    const data = res?.data
    if (data && res.success !== false) {
      const profile = data.profile || {}
      item.value = {
        id: profile.profileId,
        name: profile.nickname,
        authorId: profile.authorId || null,
        publisherLabel: profile.username || '',
        image: data.pictureURL,
        images: data.pictureURL ? [data.pictureURL] : [],
        bio: profile.content,
        content: profile.content,
        grade: profile.grade,
        faculty: profile.faculty,
        hometown: profile.hometown,
        qq: profile.qq,
        wechat: profile.wechat,
        contactVisible: data.isContactVisible === true
      }
    } else {
      item.value = null
    }
  } catch (e) {
    item.value = null
  }
})
</script>

<template>
  <div class="community-dating-page community-dating-detail-page min-h-screen bg-[var(--c-bg)] pb-10">
    <CommunityHeader :title="t('feature.dating.name')" moduleColor="var(--c-dating)" backTo="/dating/home" />

    <div v-if="item" class="community-dating-shell w-[90%] mx-auto mt-4 p-6 rounded-xl shadow-sm overflow-hidden animate-[slide-up_0.4s_ease_both]">
      <div class="community-dating-shell__title text-[22px] font-bold mb-4">{{ item.name }}</div>
      <button
        v-if="item.authorId"
        type="button"
        class="mb-3 text-sm text-[var(--c-primary)] bg-transparent border-0 p-0 cursor-pointer"
        @click="router.push(`/social/users/${item.authorId}`)"
      >{{ item.publisherLabel || t('social.viewAuthor') }}</button>
      <div class="w-full rounded-lg overflow-hidden bg-[var(--c-bg)] mb-4">
        <img :src="(item.images && item.images[0]) || item.image || '/img/dating/default-avatar.png'" :alt="item.name" class="w-full h-auto max-h-[360px] object-cover" />
      </div>
      <div class="py-4 leading-relaxed text-[var(--c-text-1)] text-base">{{ item.bio || item.content }}</div>
      <ul class="list-none m-0 p-0 w-[90%] mx-auto">
        <li class="h-11 leading-[44px] border-b border-dashed border-[var(--c-divider)] text-base text-[var(--c-text-1)]"><span class="font-bold mr-2 text-[var(--c-text-1)]">{{ copy.gradeLabel }}</span>{{ getGradeText(item.grade) }}</li>
        <li class="h-11 leading-[44px] border-b border-dashed border-[var(--c-divider)] text-base text-[var(--c-text-1)]"><span class="font-bold mr-2 text-[var(--c-text-1)]">{{ copy.facultyLabel }}</span>{{ item.faculty }}</li>
        <li class="h-11 leading-[44px] border-b border-dashed border-[var(--c-divider)] text-base text-[var(--c-text-1)]"><span class="font-bold mr-2 text-[var(--c-text-1)]">{{ copy.hometownLabel }}</span>{{ item.hometown }}</li>
        <li class="h-11 leading-[44px] border-b border-dashed border-[var(--c-divider)] text-base text-[var(--c-text-1)]"><span class="font-bold mr-2 text-[var(--c-text-1)]">{{ copy.qqLabel }}</span>{{ item.contactVisible ? item.qq : copy.hiddenContact }}</li>
        <li class="h-11 leading-[44px] text-base text-[var(--c-text-1)]"><span class="font-bold mr-2 text-[var(--c-text-1)]">{{ copy.wechatLabel }}</span>{{ item.contactVisible ? item.wechat : copy.hiddenContact }}</li>
      </ul>

      <!-- Pick section -->
      <div class="border-t-2 border-dashed border-[var(--c-divider)] pt-6 mt-6 text-center">
        <textarea v-model="pickContent" class="ui-control w-full p-4 border border-[var(--c-divider)] rounded-lg text-base min-h-[80px] box-border mb-4 text-[var(--c-text-1)] placeholder:text-[var(--c-text-3)]" :placeholder="copy.placeholder" rows="3"></textarea>
        <button type="button" class="community-dating-submit px-7 py-2.5 text-white border-none rounded-full text-lg cursor-pointer transition-opacity active:opacity-85 disabled:opacity-60" :disabled="pickSubmitting" @click="submitPick">{{ copy.submitAction }}</button>
      </div>
    </div>

    <!-- Success toast -->
    <div v-if="pickSuccessVisible" class="fixed inset-0 z-[1002]"></div>
    <div v-if="pickSuccessVisible" class="fixed top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 bg-black/70 text-white px-8 py-6 rounded-lg z-[1003] animate-[fade-in_0.2s_ease]">
      <p class="m-0 text-base">{{ copy.success }}</p>
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

.community-dating-submit {
  background: var(--c-primary);
}
</style>
