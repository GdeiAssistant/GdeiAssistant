<script setup>
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute, useRouter } from 'vue-router'
import request from '../../utils/request'
import CommunityHeader from '../../components/community/CommunityHeader.vue'
import { maskContactHandle, maskPhone } from '@/utils/mask'

const route = useRoute()
const router = useRouter()
const { t } = useI18n()
const id = route.params.id
const detail = ref(null)
const loading = ref(true)

function mapDetail(info) {
  const item = info?.item || {}
  const profile = info?.profile || {}
  return {
    title: item.name,
    desc: item.description,
    location: item.location,
    type: item.lostType,
    time: item.publishTime || '',
    images: Array.isArray(item.pictureURL) ? item.pictureURL : [],
    seller: {
      name: profile.nickname || profile.username || t('topic.anonymousUser'),
      avatar: profile.avatarURL || '/img/avatar/default.png'
    },
    contact: {
      qq: item.qq || '',
      wechat: item.wechat || '',
      phone: item.phone || ''
    }
  }
}

onMounted(async () => {
  loading.value = true
  try {
    const res = await request.get(`/lostandfound/item/id/${id}`)
    const info = res?.data
    if (info && res.success !== false) {
      detail.value = mapDetail(info)
    } else {
      detail.value = null
    }
  } catch (e) {
    detail.value = null
  } finally {
    loading.value = false
  }
})

function copyText(text) {
  if (navigator.clipboard) {
    navigator.clipboard.writeText(text).then(() => {
      // 可以显示提示
    })
  } else {
    const textarea = document.createElement('textarea')
    textarea.value = text
    document.body.appendChild(textarea)
    textarea.select()
    document.execCommand('copy')
    document.body.removeChild(textarea)
  }
}
</script>

<template>
  <div class="min-h-screen bg-[var(--c-bg)]">
    <!-- 统一顶部导航栏 -->
    <CommunityHeader :title="t('lostandfound.detail.title')" moduleColor="#3b82f6" @back="router.back()" backTo="" />

    <!-- Loading -->
    <div v-if="loading" class="flex flex-col items-center justify-center py-16 px-5 text-[var(--c-text-3)]">
      <span class="w-6 h-6 border-2 border-[var(--c-border)] border-t-blue-500 rounded-full animate-spin"></span>
      <p class="mt-3 text-sm">{{ t('communityCommon.loading') }}</p>
    </div>

    <template v-else-if="detail">
      <!-- 图片轮播 -->
      <div v-if="detail.images && detail.images.length > 0" class="w-full bg-[var(--c-surface)] overflow-x-auto overflow-y-hidden">
        <div class="flex">
          <img
            v-for="(img, index) in detail.images"
            :key="index"
            :src="img"
            :alt="t('lostandfound.detail.imageAlt', { index: index + 1 })"
            class="w-screen h-auto shrink-0 block"
          />
        </div>
      </div>

      <!-- 基本信息 -->
      <div class="bg-[var(--c-surface)] rounded-xl shadow-sm transition-transform p-4 border-b border-[var(--c-border)] rounded-none m-0">
        <h5 class="text-lg font-medium text-[var(--c-text-1)] m-0 mb-2.5 p-0">{{ detail.title }}</h5>
        <p class="text-[13px] text-[var(--c-text-3)] m-0 p-0">{{ t('lostandfound.detail.publishTime') }}<b>{{ detail.time }}</b></p>
      </div>

      <!-- 地点信息 -->
      <p class="bg-[var(--c-surface)] p-4 border-b border-[var(--c-border)] text-sm text-[var(--c-text-2)] m-0 flex items-center rounded-none">
        <span class="shrink-0">
          <i class="inline-block w-2.5 h-3.5 mr-2 align-middle bg-[url('data:image/svg+xml,%3Csvg%20xmlns=%27http://www.w3.org/2000/svg%27%20viewBox=%270%200%20384%20512%27%20fill=%27%233b82f6%27%3E%3Cpath%20d=%27M192%200C86%200%200%2086%200%20192c0%2077.4%2027%2099%20172.3%20309.7a24%2024%200%200%200%2039.4%200C357%20291%20384%20269.4%20384%20192%20384%2086%20298%200%20192%200zm0%20272a80%2080%200%201%201%200-160%2080%2080%200%200%201%200%20160z%27/%3E%3C/svg%3E')] bg-no-repeat bg-center bg-contain"></i>
          {{ detail.type === 0 ? t('lostandfound.detail.lostLocation') : t('lostandfound.detail.foundLocation') }}
        </span>
        {{ detail.location }}
      </p>

      <!-- 发布者信息 -->
      <div class="bg-[var(--c-surface)] p-4 border-b border-[var(--c-border)] rounded-none">
        <a class="flex items-center gap-2.5 no-underline text-[var(--c-text-1)]" href="javascript:;">
          <i class="w-10 h-10 rounded-full overflow-hidden block shrink-0">
            <img :src="detail.seller?.avatar || '/img/avatar/default.png'" :alt="t('profile.avatar')" class="w-full h-full object-cover" />
          </i>
          <span class="text-sm text-[var(--c-text-1)]">{{ t('lostandfound.detail.publisher') }}{{ detail.seller?.name || t('topic.anonymousUser') }}</span>
        </a>
      </div>

      <!-- 物品描述 -->
      <div class="bg-[var(--c-surface)] relative p-4 pl-[50px] border-b border-[var(--c-border)] text-sm text-[var(--c-text-2)] min-h-[50px] flex items-center rounded-none">
        <i class="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 bg-[url('data:image/svg+xml,%3Csvg%20xmlns=%27http://www.w3.org/2000/svg%27%20viewBox=%270%200%20512%20512%27%20fill=%27%233b82f6%27%3E%3Cpath%20d=%27M256%208C119%208%208%20119%208%20256s111%20248%20248%20248%20248-111%20248-248S393%208%20256%208zm0%20110a42%2042%200%201%201%200%2084%2042%2042%200%200%201%200-84zm56%20254c0%207-5%2012-12%2012h-88c-7%200-12-5-12-12v-24c0-7%205-12%2012-12h12v-64h-12c-7%200-12-5-12-12v-24c0-7%205-12%2012-12h64c7%200%2012%205%2012%2012v100h12c7%200%2012%205%2012%2012v24z%27/%3E%3C/svg%3E')] bg-no-repeat bg-center bg-contain"></i>
        <p class="m-0 p-0 leading-relaxed">{{ t('lostandfound.detail.description') }}{{ detail.desc }}</p>
      </div>

      <!-- 联系方式 -->
      <div class="bg-[var(--c-surface)] relative p-4 pl-[50px] text-sm text-[var(--c-text-2)] min-h-[50px] rounded-none">
        <i class="absolute left-4 top-4 w-5 h-5 bg-[url('data:image/svg+xml,%3Csvg%20xmlns=%27http://www.w3.org/2000/svg%27%20viewBox=%270%200%20512%20512%27%20fill=%27%233b82f6%27%3E%3Cpath%20d=%27M497%20361.8l-112-48a24%2024%200%200%200-28%206.9l-49.6%2060.6A370.7%20370.7%200%200%201%20131.6%20205l60.6-49.6a24%2024%200%200%200%206.9-28l-48-112A24.2%2024.2%200%200%200%20123.4.3L11.4%2024.3A24%2024%200%200%200-5.2e-7%2048c0%20256.5%20207.9%20464%20464%20464a24%2024%200%200%200%2023.7-11.4l24-112a24.2%2024.2%200%200%200-14.7-27.6z%27/%3E%3C/svg%3E')] bg-no-repeat bg-center bg-contain"></i>
        <span class="block mb-2.5">{{ t('lostandfound.detail.contact') }}</span>
        <div v-if="detail.contact?.qq" class="flex items-center justify-between py-2 border-b border-[var(--c-border)]">
          <span>{{ t('lostandfound.detail.qq') }}<b>{{ maskContactHandle(detail.contact.qq) }}</b></span>
          <button class="px-3 py-1 text-[13px] text-blue-500 bg-transparent border border-blue-500 rounded cursor-pointer no-underline" @click="copyText(detail.contact.qq)">{{ t('lostandfound.detail.copyQQ') }}</button>
        </div>
        <div v-if="detail.contact?.wechat" class="flex items-center justify-between py-2 border-b border-[var(--c-border)]">
          <span>{{ t('lostandfound.detail.wechat') }}<b>{{ maskContactHandle(detail.contact.wechat) }}</b></span>
          <button class="px-3 py-1 text-[13px] text-blue-500 bg-transparent border border-blue-500 rounded cursor-pointer no-underline" @click="copyText(detail.contact.wechat)">{{ t('lostandfound.detail.copyWechat') }}</button>
        </div>
        <div v-if="detail.contact?.phone" class="flex items-center justify-between py-2">
          <span>{{ t('lostandfound.detail.phone') }}<a class="text-[var(--c-text-2)]">{{ maskPhone(detail.contact.phone) }}</a></span>
          <span class="flex gap-2">
            <a :href="`tel:${detail.contact.phone}`" class="px-3 py-1 text-[13px] text-blue-500 bg-transparent border border-blue-500 rounded cursor-pointer no-underline">{{ t('lostandfound.detail.call') }}</a>
            <a :href="`sms:${detail.contact.phone}`" class="px-3 py-1 text-[13px] text-blue-500 bg-transparent border border-blue-500 rounded cursor-pointer no-underline">{{ t('lostandfound.detail.sms') }}</a>
          </span>
        </div>
      </div>
    </template>
  </div>
</template>
