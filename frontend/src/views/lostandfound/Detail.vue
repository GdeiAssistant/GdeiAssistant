<script setup>
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute, useRouter } from 'vue-router'
import request from '../../utils/request'
import CommunityHeader from '../../components/community/CommunityHeader.vue'
import AppEmpty from '@/components/ui/AppEmpty.vue'
import { maskContactHandle, maskPhone } from '@/utils/mask'
import { SearchX } from 'lucide-vue-next'

const route = useRoute()
const router = useRouter()
const { t } = useI18n()
const id = route.params.id
const detail = ref(null)
const loading = ref(true)
const errorMessage = ref('')

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
      errorMessage.value = t('common.noData')
    }
  } catch (e) {
    detail.value = null
    errorMessage.value = t('communityCommon.loadFailed')
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
  <div class="community-lostandfound-page min-h-screen bg-[var(--c-bg)]">
    <!-- 统一顶部导航栏 -->
    <CommunityHeader :title="t('lostandfound.detail.title')" moduleColor="var(--c-lostandfound)" @back="router.back()" backTo="" />

    <!-- Loading -->
    <div v-if="loading" class="community-lostandfound-detail-state-shell px-4 pt-4">
      <div class="community-lostandfound-detail-state-card">
        <div class="flex flex-col items-center justify-center py-16 px-5 text-[var(--c-text-3)]">
          <span class="w-6 h-6 border-2 border-[var(--c-border)] border-t-[var(--c-lostandfound)] rounded-full animate-spin"></span>
          <p class="mt-3 text-sm">{{ t('communityCommon.loading') }}</p>
        </div>
      </div>
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
          <i class="community-lostandfound-detail-icon community-lostandfound-detail-icon--location inline-block w-2.5 h-3.5 mr-2 align-middle"></i>
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
        <i class="community-lostandfound-detail-icon community-lostandfound-detail-icon--info absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5"></i>
        <p class="m-0 p-0 leading-relaxed">{{ t('lostandfound.detail.description') }}{{ detail.desc }}</p>
      </div>

      <!-- 联系方式 -->
      <div class="bg-[var(--c-surface)] relative p-4 pl-[50px] text-sm text-[var(--c-text-2)] min-h-[50px] rounded-none">
        <i class="community-lostandfound-detail-icon community-lostandfound-detail-icon--contact absolute left-4 top-4 w-5 h-5"></i>
        <span class="block mb-2.5">{{ t('lostandfound.detail.contact') }}</span>
        <div v-if="detail.contact?.qq" class="flex items-center justify-between py-2 border-b border-[var(--c-border)]">
          <span>{{ t('lostandfound.detail.qq') }}<b>{{ maskContactHandle(detail.contact.qq) }}</b></span>
          <button class="community-lostandfound-contact-action px-3 py-1 text-[13px] bg-transparent border rounded cursor-pointer no-underline" @click="copyText(detail.contact.qq)">{{ t('lostandfound.detail.copyQQ') }}</button>
        </div>
        <div v-if="detail.contact?.wechat" class="flex items-center justify-between py-2 border-b border-[var(--c-border)]">
          <span>{{ t('lostandfound.detail.wechat') }}<b>{{ maskContactHandle(detail.contact.wechat) }}</b></span>
          <button class="community-lostandfound-contact-action px-3 py-1 text-[13px] bg-transparent border rounded cursor-pointer no-underline" @click="copyText(detail.contact.wechat)">{{ t('lostandfound.detail.copyWechat') }}</button>
        </div>
        <div v-if="detail.contact?.phone" class="flex items-center justify-between py-2">
          <span>{{ t('lostandfound.detail.phone') }}<a class="text-[var(--c-text-2)]">{{ maskPhone(detail.contact.phone) }}</a></span>
          <span class="flex gap-2">
            <a :href="`tel:${detail.contact.phone}`" class="community-lostandfound-contact-action px-3 py-1 text-[13px] bg-transparent border rounded cursor-pointer no-underline">{{ t('lostandfound.detail.call') }}</a>
            <a :href="`sms:${detail.contact.phone}`" class="community-lostandfound-contact-action px-3 py-1 text-[13px] bg-transparent border rounded cursor-pointer no-underline">{{ t('lostandfound.detail.sms') }}</a>
          </span>
        </div>
      </div>
    </template>

    <div v-else class="community-lostandfound-detail-state-shell px-4 pt-4 pb-6">
      <div class="community-lostandfound-detail-state-card">
        <AppEmpty
          :title="t('common.noData')"
          :description="errorMessage || t('communityCommon.loadFailed')"
          :action-text="t('common.back')"
          accent="var(--c-lostandfound)"
          action-variant="primary"
          @action="router.back()"
        >
          <template #icon>
            <SearchX class="community-lostandfound-detail-state-icon" aria-hidden="true" />
          </template>
        </AppEmpty>
      </div>
    </div>
  </div>
</template>

<style scoped>
.community-lostandfound-detail-state-shell {
  max-width: 1180px;
  margin: 0 auto;
}

.community-lostandfound-detail-state-card {
  border: 1px solid color-mix(in srgb, var(--c-lostandfound) 10%, rgba(205, 222, 226, 0.82));
  border-radius: 24px;
  background: color-mix(in srgb, var(--c-lostandfound) 3%, rgba(255, 255, 255, 0.94));
  box-shadow: 0 18px 36px rgba(32, 69, 78, 0.08);
}

.community-lostandfound-detail-state-icon {
  width: 30px;
  height: 30px;
}

.community-lostandfound-detail-icon {
  display: block;
  background-color: color-mix(in srgb, var(--c-lostandfound) 82%, var(--c-text-1));
  mask-repeat: no-repeat;
  mask-position: center;
  mask-size: contain;
  -webkit-mask-repeat: no-repeat;
  -webkit-mask-position: center;
  -webkit-mask-size: contain;
}

.community-lostandfound-detail-icon--location {
  mask-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 384 512'%3E%3Cpath d='M192 0C86 0 0 86 0 192c0 77.4 27 99 172.3 309.7a24 24 0 0 0 39.4 0C357 291 384 269.4 384 192 384 86 298 0 192 0zm0 272a80 80 0 1 1 0-160 80 80 0 0 1 0 160z'/%3E%3C/svg%3E");
  -webkit-mask-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 384 512'%3E%3Cpath d='M192 0C86 0 0 86 0 192c0 77.4 27 99 172.3 309.7a24 24 0 0 0 39.4 0C357 291 384 269.4 384 192 384 86 298 0 192 0zm0 272a80 80 0 1 1 0-160 80 80 0 0 1 0 160z'/%3E%3C/svg%3E");
}

.community-lostandfound-detail-icon--info {
  mask-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 512 512'%3E%3Cpath d='M256 8C119 8 8 119 8 256s111 248 248 248 248-111 248-248S393 8 256 8zm0 110a42 42 0 1 1 0 84 42 42 0 0 1 0-84zm56 254c0 7-5 12-12 12h-88c-7 0-12-5-12-12v-24c0-7 5-12 12-12h12v-64h-12c-7 0-12-5-12-12v-24c0-7 5-12 12-12h64c7 0 12 5 12 12v100h12c7 0 12 5 12 12v24z'/%3E%3C/svg%3E");
  -webkit-mask-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 512 512'%3E%3Cpath d='M256 8C119 8 8 119 8 256s111 248 248 248 248-111 248-248S393 8 256 8zm0 110a42 42 0 1 1 0 84 42 42 0 0 1 0-84zm56 254c0 7-5 12-12 12h-88c-7 0-12-5-12-12v-24c0-7 5-12 12-12h12v-64h-12c-7 0-12-5-12-12v-24c0-7 5-12 12-12h64c7 0 12 5 12 12v100h12c7 0 12 5 12 12v24z'/%3E%3C/svg%3E");
}

.community-lostandfound-detail-icon--contact {
  mask-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 512 512'%3E%3Cpath d='M497 361.8l-112-48a24 24 0 0 0-28 6.9l-49.6 60.6A370.7 370.7 0 0 1 131.6 205l60.6-49.6a24 24 0 0 0 6.9-28l-48-112A24.2 24.2 0 0 0 123.4.3L11.4 24.3A24 24 0 0 0 0 48c0 256.5 207.9 464 464 464a24 24 0 0 0 23.7-11.4l24-112a24.2 24.2 0 0 0-14.7-27.6z'/%3E%3C/svg%3E");
  -webkit-mask-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 512 512'%3E%3Cpath d='M497 361.8l-112-48a24 24 0 0 0-28 6.9l-49.6 60.6A370.7 370.7 0 0 1 131.6 205l60.6-49.6a24 24 0 0 0 6.9-28l-48-112A24.2 24.2 0 0 0 123.4.3L11.4 24.3A24 24 0 0 0 0 48c0 256.5 207.9 464 464 464a24 24 0 0 0 23.7-11.4l24-112a24.2 24.2 0 0 0-14.7-27.6z'/%3E%3C/svg%3E");
}

.community-lostandfound-contact-action {
  border-color: color-mix(in srgb, var(--c-lostandfound) 72%, transparent);
  color: color-mix(in srgb, var(--c-lostandfound) 84%, var(--c-text-1));
  transition: background 0.18s ease, border-color 0.18s ease, color 0.18s ease;
}

.community-lostandfound-contact-action:hover {
  background: color-mix(in srgb, var(--c-lostandfound) 8%, transparent);
}

[data-theme="dark"] .community-lostandfound-contact-action {
  border-color: color-mix(in srgb, var(--c-lostandfound) 36%, rgba(76, 101, 126, 0.84));
  color: color-mix(in srgb, var(--c-lostandfound) 54%, var(--c-text-1));
}

[data-theme="dark"] .community-lostandfound-contact-action:hover {
  background: rgba(32, 48, 68, 0.72);
}

[data-theme="dark"] .community-lostandfound-detail-icon {
  background-color: color-mix(in srgb, var(--c-lostandfound) 56%, var(--c-text-1));
}

[data-theme="dark"] .community-lostandfound-detail-state-card {
  border-color: color-mix(in srgb, var(--c-lostandfound) 12%, rgba(97, 122, 147, 0.66));
  background: rgba(24, 38, 53, 0.88);
  box-shadow: 0 22px 38px rgba(0, 0, 0, 0.22);
}
</style>
