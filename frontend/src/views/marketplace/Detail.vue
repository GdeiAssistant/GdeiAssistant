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

const loading = ref(true)
const detail = ref(null)
const errorMessage = ref('')
const tipVisible = ref(false)
const tipText = ref('')

function showTopTips(text) {
  tipText.value = text
  tipVisible.value = true
  setTimeout(() => {
    tipVisible.value = false
  }, 2000)
}

function copyQQ() {
  const qq = detail.value?.contact?.qq
  if (!qq) return
  if (navigator.clipboard && navigator.clipboard.writeText) {
    navigator.clipboard.writeText(qq).then(() => {
      showTopTips(t('marketplace.detail.qqCopied'))
    }).catch(() => {
      fallbackCopy(qq)
    })
  } else {
    fallbackCopy(qq)
  }
}

function fallbackCopy(text) {
  const ta = document.createElement('textarea')
  ta.value = text
  ta.style.position = 'fixed'
  ta.style.opacity = '0'
  document.body.appendChild(ta)
  ta.select()
  try {
    document.execCommand('copy')
    showTopTips(t('marketplace.detail.qqCopied'))
  } catch (e) {
    showTopTips(t('marketplace.detail.copyFailed'))
  }
  document.body.removeChild(ta)
}

function callPhone() {
  const phone = detail.value?.contact?.phone
  if (phone) window.location.href = `tel:${phone}`
}

function mapErshouDetail(info) {
  const item = info?.secondhandItem || {}
  const profile = info?.profile || {}
  return {
    images: Array.isArray(item.pictureURL) ? item.pictureURL : [],
    price: item.price,
    title: item.name,
    location: item.location,
    desc: item.description,
    seller: {
      name: profile.nickname || profile.username || '—',
      publishTime: item.publishTime || '',
      avatar: profile.avatarURL || '/img/avatar/default.png'
    },
    contact: {
      qq: item.qq || '',
      phone: item.phone || ''
    }
  }
}

onMounted(async () => {
  loading.value = true
  try {
    const res = await request.get(`/marketplace/item/id/${id}`)
    const info = res?.data
    if (info && res.success !== false) {
      detail.value = mapErshouDetail(info)
    } else {
      detail.value = null
      errorMessage.value = res?.message || t('marketplace.detail.itemNotFound')
    }
  } catch (e) {
    detail.value = null
    errorMessage.value = t('marketplace.detail.loadFailed')
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="min-h-screen bg-[var(--c-bg)] pb-5">
    <!-- 统一顶部导航栏 -->
    <CommunityHeader :title="t('marketplace.detail.title')" moduleColor="#10b981" :showBack="true" @back="router.back()" backTo="" />

    <!-- 加载中 -->
    <div v-if="loading" class="flex flex-col items-center justify-center py-16 px-5 text-[var(--c-text-3)]">
      <div class="w-6 h-6 border-2 border-[var(--c-border)] border-t-emerald-500 rounded-full animate-spin"></div>
      <p class="mt-3 text-sm">{{ t('communityCommon.loading') }}</p>
    </div>

    <template v-else-if="detail">
      <!-- 主体 -->
      <section class="mx-2.5 mb-2.5">
        <!-- 多图 -->
        <div class="bg-[var(--c-surface)] rounded-lg overflow-hidden shadow-sm">
          <img
            v-for="(img, idx) in detail.images"
            :key="idx"
            :src="img"
            :alt="t('marketplace.detail.imageAlt', { index: idx + 1 })"
            class="w-full block rounded-lg"
            :class="idx > 0 ? 'mt-2.5' : ''"
          />
        </div>

        <!-- 商品基本信息 -->
        <div class="bg-emerald-500 px-3 py-2.5 pr-[70px] relative">
          <em class="absolute right-4 w-[55px] h-[55px] rounded-full bg-amber-500 text-center leading-[55px] text-white text-lg -top-7 z-10 not-italic">
            <b class="absolute top-1 text-xs leading-3 left-1/2 -ml-[5px] font-normal">{{ '¥' }}</b>{{ detail.price }}
          </em>
          <h5 class="text-lg text-white m-0 mb-1">{{ detail.title }}</h5>
          <p class="text-sm text-white m-0">{{ t('marketplace.detail.publishTime') }}<b>{{ detail.seller?.publishTime || '—' }}</b></p>
        </div>

        <!-- 交易地点 -->
        <p class="bg-[var(--c-surface)] py-2 px-2.5 pl-[98px] text-emerald-500 leading-5 text-sm relative m-0">
          <span class="absolute left-2.5 top-2">
            <i class="inline-block w-2.5 h-3.5 mr-1 align-middle bg-[url('data:image/svg+xml,%3Csvg%20xmlns=%27http://www.w3.org/2000/svg%27%20viewBox=%270%200%20384%20512%27%20fill=%27%2310b981%27%3E%3Cpath%20d=%27M192%200C86%200%200%2086%200%20192c0%2077.4%2027%2099%20172.3%20309.7a24%2024%200%200%200%2039.4%200C357%20291%20384%20269.4%20384%20192%20384%2086%20298%200%20192%200zm0%20272a80%2080%200%201%201%200-160%2080%2080%200%200%201%200%20160z%27/%3E%3C/svg%3E')] bg-no-repeat bg-center bg-contain"></i>
            {{ t('marketplace.detail.location') }}
          </span>
          {{ detail.location || '—' }}
        </p>
      </section>

      <section class="mx-2.5 mb-5">
        <!-- 发布者 -->
        <div class="mt-2.5 bg-[var(--c-surface)] shadow-sm rounded overflow-hidden min-h-[30px] py-5 px-4 pl-[60px] relative block">
          <i class="w-[30px] h-[30px] absolute left-4 top-5 rounded-full overflow-hidden block">
            <img :src="detail.seller?.avatar || '/img/avatar/default.png'" :alt="t('profile.avatar')" class="w-full h-full object-cover">
          </i>
          <span class="leading-[30px] text-base text-[var(--c-text-2)]">{{ t('marketplace.detail.publisher') }}{{ detail.seller?.name || '—' }}</span>
        </div>

        <!-- 商品描述 -->
        <div class="mt-2.5 bg-[var(--c-surface)] shadow-sm rounded overflow-hidden relative py-4 px-4 pl-[60px] text-sm text-[var(--c-text-2)]">
          <i class="absolute left-4 top-4 w-[30px] h-[30px] bg-[url('data:image/svg+xml,%3Csvg%20xmlns=%27http://www.w3.org/2000/svg%27%20viewBox=%270%200%20512%20512%27%20fill=%27%2310b981%27%3E%3Cpath%20d=%27M256%208C119%208%208%20119%208%20256s111%20248%20248%20248%20248-111%20248-248S393%208%20256%208zm0%20110a42%2042%200%201%201%200%2084%2042%2042%200%200%201%200-84zm56%20254c0%207-5%2012-12%2012h-88c-7%200-12-5-12-12v-24c0-7%205-12%2012-12h12v-64h-12c-7%200-12-5-12-12v-24c0-7%205-12%2012-12h64c7%200%2012%205%2012%2012v100h12c7%200%2012%205%2012%2012v24z%27/%3E%3C/svg%3E')] bg-no-repeat bg-center bg-contain"></i>
          <p class="m-0 leading-5">{{ t('marketplace.detail.description') }}{{ detail.desc || '—' }}</p>
        </div>

        <!-- 联系方式 -->
        <div class="mt-2.5 bg-[var(--c-surface)] shadow-sm rounded overflow-hidden relative py-4 px-4 pl-[60px] text-sm text-[var(--c-text-2)]">
          <i class="absolute left-4 top-4 w-[30px] h-[30px] bg-[url('data:image/svg+xml,%3Csvg%20xmlns=%27http://www.w3.org/2000/svg%27%20viewBox=%270%200%20512%20512%27%20fill=%27%2310b981%27%3E%3Cpath%20d=%27M497%20361.8l-112-48a24%2024%200%200%200-28%206.9l-49.6%2060.6A370.7%20370.7%200%200%201%20131.6%20205l60.6-49.6a24%2024%200%200%200%206.9-28l-48-112A24.2%2024.2%200%200%200%20123.4.3L11.4%2024.3A24%2024%200%200%200-5.2e-7%2048c0%20256.5%20207.9%20464%20464%20464a24%2024%200%200%200%2023.7-11.4l24-112a24.2%2024.2%200%200%200-14.7-27.6z%27/%3E%3C/svg%3E')] bg-no-repeat bg-center bg-contain"></i>
          <p class="m-0 mb-0.5 leading-5">{{ t('marketplace.detail.qq') }}<b>{{ detail.contact?.qq ? maskContactHandle(detail.contact.qq) : '—' }}</b></p>
          <p v-if="detail.contact?.phone" class="m-0 leading-5">
            <span>{{ t('marketplace.detail.phone') }}<a class="text-[var(--c-text-2)]">{{ maskPhone(detail.contact.phone) }}</a></span>
            <a :href="'tel:' + detail.contact.phone" class="ml-1.5">{{ t('marketplace.detail.call') }}</a>
            <a :href="'sms:' + detail.contact.phone" class="ml-1.5">{{ t('marketplace.detail.sms') }}</a>
          </p>
        </div>
      </section>

      <section class="text-center leading-[38px] h-[38px] mt-5 text-[var(--c-text-3)] text-xs">
        <span>{{ t('marketplace.detail.riskHint') }}</span>
      </section>
    </template>

    <!-- Empty State -->
    <div v-else class="flex flex-col items-center py-16 text-[var(--c-text-3)]">
      <p class="text-sm">{{ errorMessage || t('marketplace.detail.loadFailedOrMissing') }}</p>
    </div>

    <!-- 底部悬浮操作栏 -->
    <div v-if="detail && !loading" class="fixed bottom-0 left-0 right-0 w-full flex items-center gap-3 px-3 py-2.5 bg-[var(--c-surface)] shadow-[0_-2px_10px_rgba(0,0,0,0.08)] z-[400]">
      <button type="button" class="flex-1 h-11 border-none rounded bg-[var(--c-bg)] text-[var(--c-text-1)] text-base cursor-pointer flex items-center justify-center" @click="copyQQ">{{ t('marketplace.detail.copyQQ') }}</button>
      <button v-if="detail.contact?.phone" type="button" class="flex-1 h-11 border-none rounded bg-emerald-500 text-white text-base cursor-pointer flex items-center justify-center" @click="callPhone">{{ t('marketplace.detail.call') }}</button>
    </div>

    <!-- 底部栏占位 -->
    <div v-if="detail && !loading" class="h-[70px]"></div>

    <!-- TopTips -->
    <div v-show="tipVisible" class="fixed top-0 left-0 right-0 py-2.5 text-center text-sm z-[6000] transition-opacity duration-300 bg-black/70 text-white">{{ tipText }}</div>
  </div>
</template>
