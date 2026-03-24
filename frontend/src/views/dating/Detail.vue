<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '../../utils/request'
import CommunityHeader from '../../components/community/CommunityHeader.vue'

const route = useRoute()
const router = useRouter()
const item = ref(null)
const pickContent = ref('')
const pickSubmitting = ref(false)
const pickSuccessVisible = ref(false)
const dialogVisible = ref(false)
const dialogMessage = ref('')

function getGradeText(grade) {
  const map = { 1: '大一', 2: '大二', 3: '大三', 4: '大四' }
  return map[grade] || '未知'
}

function showDialog(msg) {
  dialogMessage.value = msg
  dialogVisible.value = true
}

function submitPick() {
  const text = pickContent.value && pickContent.value.trim()
  if (!text) {
    showDialog('请输入撩一下的留言信息')
    return
  }
  if (text.length > 50) {
    showDialog('撩一下输入的内容太长了')
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
  <div class="min-h-screen bg-[var(--c-bg)] pb-10">
    <CommunityHeader title="卖室友" moduleColor="#ec4899" backTo="/dating/home" />

    <div v-if="item" class="w-[90%] mx-auto mt-4 p-6 bg-[var(--c-surface)] rounded-xl shadow-sm overflow-hidden animate-[slide-up_0.4s_ease_both]">
      <div class="text-[22px] text-pink-500 font-bold mb-4">{{ item.name }}</div>
      <div class="w-full rounded-lg overflow-hidden bg-[var(--c-bg)] mb-4">
        <img :src="(item.images && item.images[0]) || item.image || '/img/dating/default-avatar.png'" :alt="item.name" class="w-full h-auto max-h-[360px] object-cover" />
      </div>
      <div class="py-4 leading-relaxed text-[var(--c-text-1)] text-base">{{ item.bio || item.content }}</div>
      <ul class="list-none m-0 p-0 w-[90%] mx-auto">
        <li class="h-11 leading-[44px] border-b border-dashed border-[var(--c-divider)] text-base text-[var(--c-text-1)]"><span class="font-bold mr-2 text-[var(--c-text-1)]">年级：</span>{{ getGradeText(item.grade) }}</li>
        <li class="h-11 leading-[44px] border-b border-dashed border-[var(--c-divider)] text-base text-[var(--c-text-1)]"><span class="font-bold mr-2 text-[var(--c-text-1)]">专业：</span>{{ item.faculty }}</li>
        <li class="h-11 leading-[44px] border-b border-dashed border-[var(--c-divider)] text-base text-[var(--c-text-1)]"><span class="font-bold mr-2 text-[var(--c-text-1)]">家乡：</span>{{ item.hometown }}</li>
        <li class="h-11 leading-[44px] border-b border-dashed border-[var(--c-divider)] text-base text-[var(--c-text-1)]"><span class="font-bold mr-2 text-[var(--c-text-1)]">QQ：</span>{{ item.contactVisible ? item.qq : '对方接受了撩一下后才可见哦' }}</li>
        <li class="h-11 leading-[44px] text-base text-[var(--c-text-1)]"><span class="font-bold mr-2 text-[var(--c-text-1)]">微信：</span>{{ item.contactVisible ? item.wechat : '对方接受了撩一下后才可见哦' }}</li>
      </ul>

      <!-- Pick section -->
      <div class="border-t-2 border-dashed border-[var(--c-divider)] pt-6 mt-6 text-center">
        <textarea v-model="pickContent" class="w-full p-4 border border-[var(--c-divider)] rounded-lg text-base min-h-[80px] box-border mb-4 text-[var(--c-text-1)] placeholder:text-[var(--c-text-3)]" placeholder="来说点什么吧，不超过50字" rows="3"></textarea>
        <button type="button" class="px-7 py-2.5 bg-pink-500 text-white border-none rounded-full text-lg cursor-pointer transition-opacity active:opacity-85 disabled:opacity-60" :disabled="pickSubmitting" @click="submitPick">撩一下</button>
      </div>
    </div>

    <!-- Success toast -->
    <div v-if="pickSuccessVisible" class="fixed inset-0 z-[1002]"></div>
    <div v-if="pickSuccessVisible" class="fixed top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 bg-black/70 text-white px-8 py-6 rounded-lg z-[1003] animate-[fade-in_0.2s_ease]">
      <p class="m-0 text-base">发送成功，请耐心等待对方回复</p>
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
