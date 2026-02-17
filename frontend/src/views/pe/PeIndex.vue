<template>
  <div class="weui-msg" v-if="!isWechat">
    <div class="weui-msg__icon-area"><i class="weui-icon-warn weui-icon_msg"></i></div>
    <div class="weui-msg__text-area">
      <h2 class="weui-msg__title">请在微信客户端打开</h2>
      <p class="weui-msg__desc">体质测试查询功能需要微信环境支持。请使用微信扫码或在微信中搜索打开本应用。</p>
    </div>
    <div class="weui-msg__opr-area">
      <p class="weui-btn-area">
        <a href="javascript:history.back();" class="weui-btn weui-btn_default">返回上一页</a>
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const isWechat = ref(true) // 默认设为 true 避免渲染闪烁

const PE_EXTERNAL_URL = 'https://open.weixin.qq.com/connect/oauth2/authorize?appid=wxa2d196aa4b8a7600&redirect_uri=http%3A%2F%2F5itsn.com%2FWeixin%2FOAuth2%2FUserInfoCallback&response_type=code&scope=snsapi_userinfo&state=TestUrlTestResult&connect_redirect=1#wechat_redirect'

onMounted(() => {
  const ua = navigator.userAgent.toLowerCase()
  if (/micromessenger/.test(ua)) {
    window.location.replace(PE_EXTERNAL_URL)
  } else {
    isWechat.value = false
  }
})
</script>

<style scoped>
.weui-msg {
  padding-top: 36px;
  text-align: center;
}
</style>
