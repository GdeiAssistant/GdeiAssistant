<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import service from '../utils/request.js'

const router = useRouter()

const username = ref('')
const password = ref('')
const showToast = ref(false)
const toastMessage = ref('模拟登录中...')

async function handleLogin() {
  if (!username.value.trim() || !password.value.trim()) {
    toastMessage.value = '请将信息填写完整'
    showToast.value = true
    setTimeout(() => { showToast.value = false }, 2000)
    return
  }
  toastMessage.value = '模拟登录中...'
  showToast.value = true
  try {
    const res = await service.post('/userlogin', {
      username: username.value.trim(),
      password: password.value
    })
    showToast.value = false
    if (res && res.success) {
      router.push('/home')
    }
  } catch {
    showToast.value = false
  }
}

function handleThirdPartyLogin(type) {
  toastMessage.value = '该登录方式暂未开放'
  showToast.value = true
  setTimeout(() => { showToast.value = false }, 2000)
}
</script>

<template>
  <div class="page">
    <div class="hd">
      <h1 class="page_title">广东二师助手</h1>
      <p class="page_desc">请登录校园网系统</p>
    </div>

    <div class="weui-cells weui-cells_form">
      <div class="weui-cell">
        <div class="weui-cell__hd">
          <label class="weui-label">账号</label>
        </div>
        <div class="weui-cell__bd weui-cell_primary">
          <input
            v-model="username"
            class="weui-input"
            type="text"
            maxlength="20"
            placeholder="请输入你的校园网账号"
          />
        </div>
      </div>
      <div class="weui-cell">
        <div class="weui-cell__hd">
          <label class="weui-label">密码</label>
        </div>
        <div class="weui-cell__bd weui-cell_primary">
          <input
            v-model="password"
            class="weui-input"
            type="password"
            maxlength="35"
            placeholder="请输入你的校园网密码"
          />
        </div>
      </div>
    </div>

    <div class="weui-btn_area">
      <a class="weui-btn weui-btn_primary" href="javascript:" @click.prevent="handleLogin">登录</a>
    </div>

    <div class="weui-footer" style="margin-top: 30px; margin-bottom: 20px; text-align: center; width: 100%;">
      <p class="weui-footer__text">
        关于登录账户请阅读
        <a class="weui-footer__link" href="/about/account">《校园网络账号说明》</a>
        <br/>
        使用前请仔细阅读
        <a class="weui-footer__link" href="/agreement">《用户协议》</a>
        和
        <a class="weui-footer__link" href="/policy/privacy">《隐私政策》</a>
      </p>
    </div>

    <div class="quick-login">
      <div class="quick-login-text">
        <p>——&nbsp;&nbsp;&nbsp;其他方式登录&nbsp;&nbsp;&nbsp;——</p>
      </div>
      <div class="third-party-login">
        <img src="/img/login/wechat.png" alt="WeChat" @click="handleThirdPartyLogin('WeChat')" />
        <img src="/img/login/qq.png" alt="QQ" @click="handleThirdPartyLogin('QQ')" />
        <img src="/img/login/weibo.png" alt="Weibo" @click="handleThirdPartyLogin('Weibo')" />
        <img src="/img/login/apple.png" alt="Apple" @click="handleThirdPartyLogin('Apple')" />
      </div>
    </div>

    <!-- WEUI Toast：模拟登录中 / 请将信息填写完整 -->
    <Teleport to="body">
      <div v-show="showToast" role="alert" class="weui-toast-wrap">
        <div class="weui-mask_transparent"></div>
        <div class="weui-toast__wrp">
          <div class="weui-toast" :class="{ 'weui-toast_text': toastMessage !== '模拟登录中...' }">
            <template v-if="toastMessage === '模拟登录中...'">
              <span class="weui-primary-loading weui-icon_toast">
                <span class="weui-primary-loading__dot"></span>
              </span>
            </template>
            <p class="weui-toast__content">{{ toastMessage }}</p>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<style scoped>
.page {
  padding: 16px;
  padding-bottom: 120px;
}
.hd {
  padding: 2em 0;
  text-align: center;
}
.page_title {
  font-size: 22px;
  font-weight: 400;
  margin: 0;
}
.page_desc {
  color: #999;
  font-size: 14px;
  margin: 0.5em 0 0;
}
.weui-footer__text {
  color: #999;
  font-size: 14px;
  margin: 0;
}
.weui-footer__link {
  color: #586c94;
}
.quick-login {
  position: absolute;
  bottom: 1rem;
  left: 0;
  right: 0;
}
.quick-login-text {
  text-align: center;
}
.quick-login-text p {
  color: gray;
  font-size: 15px;
}
.third-party-login {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 15px;
  margin-bottom: 30px;
}
.third-party-login img {
  display: block;
  width: 32px;
  height: 32px;
  margin: 0 12px;
  border-radius: 50%;
  cursor: pointer;
  transition: opacity 0.2s;
}
.third-party-login img:active {
  opacity: 0.6;
}
.weui-toast-wrap {
  position: fixed;
  left: 0;
  right: 0;
  top: 0;
  bottom: 0;
  z-index: 5000;
}
</style>
