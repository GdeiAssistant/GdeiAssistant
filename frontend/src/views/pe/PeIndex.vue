<script setup>
import { ref, onMounted } from 'vue'

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
  <div v-if="!isWechat" class="min-h-screen bg-[var(--c-bg)]">
    <!-- Sticky header -->
    <div class="sticky top-0 z-30 flex items-center h-[52px] px-5 bg-[var(--c-surface)]/90 backdrop-blur-xl border-b border-[var(--c-border)]">
      <button @click="$router.back()" class="text-[var(--c-primary)] text-sm font-medium">&larr; 返回</button>
      <span class="flex-1 text-center text-sm font-bold">体质测试查询</span>
      <div class="w-10"></div>
    </div>

    <div class="max-w-lg mx-auto px-4 py-6">
      <div class="flex flex-col items-center text-center pt-12">
        <!-- Warning icon -->
        <div class="w-16 h-16 rounded-full bg-amber-100 flex items-center justify-center mb-5">
          <svg class="w-8 h-8 text-amber-500" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" d="M12 9v3.75m-9.303 3.376c-.866 1.5.217 3.374 1.948 3.374h14.71c1.73 0 2.813-1.874 1.948-3.374L13.949 3.378c-.866-1.5-3.032-1.5-3.898 0L2.697 16.126ZM12 15.75h.007v.008H12v-.008Z" />
          </svg>
        </div>

        <h2 class="text-lg font-semibold text-[var(--c-text)] mb-2">请在微信客户端打开</h2>
        <p class="text-sm text-[var(--c-text-secondary)] leading-relaxed max-w-xs">
          体质测试查询功能需要微信环境支持。请使用微信扫码或在微信中搜索打开本应用。
        </p>

        <button
          @click="$router.back()"
          class="mt-8 px-8 py-2.5 rounded-xl border border-[var(--c-border)] text-sm text-[var(--c-text)] bg-[var(--c-surface)] active:bg-black/5 transition-colors"
        >
          返回上一页
        </button>
      </div>
    </div>
  </div>
</template>
