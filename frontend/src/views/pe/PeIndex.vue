<script setup>
import { ChevronLeft } from 'lucide-vue-next'
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

const isWechat = ref(true) // 默认设为 true 避免渲染闪烁

const PE_EXTERNAL_URL = import.meta.env.VITE_PE_EXTERNAL_URL
  ?? 'https://open.weixin.qq.com/connect/oauth2/authorize?appid=wxa2d196aa4b8a7600&redirect_uri=http%3A%2F%2F5itsn.com%2FWeixin%2FOAuth2%2FUserInfoCallback&response_type=code&scope=snsapi_userinfo&state=TestUrlTestResult&connect_redirect=1#wechat_redirect'

onMounted(() => {
  const ua = navigator.userAgent.toLowerCase()
  if (/micromessenger/.test(ua)) {
    window.location.replace(PE_EXTERNAL_URL)
  } else {
    isWechat.value = false
  }
})
</script>

<template>
  <div v-if="!isWechat" class="subpage min-h-screen bg-[var(--c-bg)]">
    <!-- Sticky header -->
    <div class="subpage-bar">
      <button type="button" class="subpage-bar__back" @click="$router.back()">
        <ChevronLeft :size="18" aria-hidden="true" />
        <span>{{ t('common.back') }}</span>
      </button>
      <span class="subpage-bar__title">{{ t('feature.pe.name') }}</span>
      <span aria-hidden="true"></span>
    </div>

    <div class="subpage-body max-w-lg mx-auto px-4 py-6">
      <div class="flex flex-col items-center text-center pt-12">
        <!-- Warning icon -->
        <div class="pe-wechat-warning-icon w-16 h-16 rounded-full flex items-center justify-center mb-5">
          <svg class="pe-wechat-warning-icon__glyph w-8 h-8" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" d="M12 9v3.75m-9.303 3.376c-.866 1.5.217 3.374 1.948 3.374h14.71c1.73 0 2.813-1.874 1.948-3.374L13.949 3.378c-.866-1.5-3.032-1.5-3.898 0L2.697 16.126ZM12 15.75h.007v.008H12v-.008Z" />
          </svg>
        </div>

        <h2 class="text-lg font-semibold text-[var(--c-text)] mb-2">{{ t('pePage.wechatRequiredTitle') }}</h2>
        <p class="text-sm text-[var(--c-text-secondary)] leading-relaxed max-w-xs">
          {{ t('pePage.wechatRequiredDescription') }}
        </p>

        <button
          @click="$router.back()"
          class="ui-panel mt-8 px-8 py-2.5 rounded-xl border border-[var(--c-border)] text-sm text-[var(--c-text)] bg-[var(--c-surface)] hover:bg-[var(--c-surface-hover)] transition-colors"
        >
          {{ t('common.back') }}
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.pe-wechat-warning-icon {
  background: color-mix(in srgb, var(--c-warning) 16%, var(--c-surface));
  color: var(--c-warning);
  box-shadow: none;
}

.pe-wechat-warning-icon__glyph {
  color: inherit;
}
</style>
