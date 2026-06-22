<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { login } from '../api/user.js'
import { useToast } from '@/composables/useToast'
import { isMockMode, toggleDataSourceMode } from '@/services/data-source.js'
import { MOCK_ACCOUNT_USERNAME, MOCK_ACCOUNT_PASSWORD, getMockCredentialsHint } from '@/constants/mock.js'

const router = useRouter()
const { t, locale } = useI18n()
const { error: showError, loading: showLoading, hideLoading } = useToast()

const username = ref('')
const password = ref('')
const mockMode = ref(isMockMode())
const campusCredentialConsent = ref(false)

function fillMockCredentials() {
  username.value = MOCK_ACCOUNT_USERNAME
  password.value = MOCK_ACCOUNT_PASSWORD
}

if (mockMode.value) {
  fillMockCredentials()
}

function toggleMock() {
  toggleDataSourceMode()
  mockMode.value = isMockMode()
  if (mockMode.value) {
    fillMockCredentials()
  } else {
    username.value = ''
    password.value = ''
  }
}

async function handleLogin() {
  if (!username.value.trim() || !password.value.trim()) {
    showError(t('loginPage.incompleteFields'))
    return
  }
  if (!campusCredentialConsent.value) {
    showError(t('loginPage.campusConsentRequired'))
    return
  }
  showLoading(t('loginPage.loading'))
  try {
    const res = await login(username.value.trim(), password.value, {
      campusCredentialConsent: true,
      consentScene: 'LOGIN',
      policyDate: '2026-04-25',
      effectiveDate: '2026-05-11'
    })
    hideLoading()
    // 仅当后端返回 code === 200 时存 Token 并跳转；401 或其他错误码展示后端 message 并停留在登录页
    if (res && res.code === 200 && res.data && res.data.token) {
      localStorage.setItem('token', res.data.token)
      router.push('/home')
    } else {
      showError(res?.message || t('loginPage.failed'))
    }
  } catch (err) {
    hideLoading()
    // 错误提示由 request.js 全局拦截器统一展示（如账号或密码错误、网络连接失败等），此处仅关闭加载态
  }
}

function handleThirdPartyLogin(type) {
  showError(t('loginPage.thirdPartyUnavailable'))
}
</script>

<template>
  <main class="login-page">
    <section class="login-visual" aria-hidden="true">
      <div class="login-visual__card">
        <div class="login-visual__mark">G</div>
        <h2>广东二师助手</h2>
        <p>把课表、校园卡、资讯和校园生活放在一个清爽入口里。</p>
      </div>
    </section>

    <section class="login-panel" aria-labelledby="login-title">
      <div class="login-panel__brand">
        <div class="login-panel__logo">G</div>
        <div>
          <h1 id="login-title">{{ t('loginPage.title') }}</h1>
          <p>{{ t('loginPage.subtitle') }}</p>
        </div>
      </div>

      <div class="login-panel__notice">
        校园账号凭证可能用于校园认证、快速认证和会话同步。请仅使用本人账号；如您拒绝保存相关凭证或后续通过账号设置、反馈渠道申请删除，部分校园查询或快速认证功能可能不可用。
      </div>

      <form class="login-form" @submit.prevent="handleLogin">
        <label class="login-field">
          <span>{{ t('loginPage.usernameLabel') }}</span>
          <input
            v-model="username"
            type="text"
            maxlength="20"
            autocomplete="username"
            :placeholder="t('loginPage.usernamePlaceholder')"
          />
        </label>

        <label class="login-field">
          <span>{{ t('loginPage.passwordLabel') }}</span>
          <input
            v-model="password"
            type="password"
            maxlength="35"
            autocomplete="current-password"
            :placeholder="t('loginPage.passwordPlaceholder')"
          />
        </label>

        <label class="login-consent">
          <input v-model="campusCredentialConsent" type="checkbox" />
          <span>{{ t('loginPage.campusConsentLabel') }}</span>
        </label>

        <button type="submit" class="login-submit">
          {{ t('loginPage.submit') }}
        </button>
      </form>

      <div class="login-mock">
        <div class="login-mock__top">
          <span>{{ t('loginPage.mockMode') }}</span>
          <button
            type="button"
            class="login-switch"
            :class="{ 'login-switch--on': mockMode }"
            @click="toggleMock"
          >
            <span />
          </button>
        </div>
        <div v-if="mockMode" class="login-mock__hint">
          <p>{{ `${getMockCredentialsHint(locale)}${t('loginPage.autoFilledSuffix')}` }}</p>
          <strong>{{ t('loginPage.mockActive') }}</strong>
        </div>
      </div>

      <div class="login-links">
        <p>
          {{ t('loginPage.accountHelpPrefix') }}
          <router-link to="/about/account">{{ t('loginPage.accountHelpLink') }}</router-link>
        </p>
        <p>
          {{ t('loginPage.agreementPrefix') }}
          <router-link to="/agreement">{{ t('loginPage.agreementLink') }}</router-link>
          {{ t('loginPage.and') }}
          <router-link to="/policy/privacy">{{ t('loginPage.privacyLink') }}</router-link>
        </p>
      </div>

      <div class="login-third-party">
        <span>{{ t('loginPage.otherLogin') }}</span>
        <div>
          <button type="button" @click="handleThirdPartyLogin('WeChat')"><img src="/img/login/wechat.png" alt="WeChat" /></button>
          <button type="button" @click="handleThirdPartyLogin('QQ')"><img src="/img/login/qq.png" alt="QQ" /></button>
          <button type="button" @click="handleThirdPartyLogin('Apple')"><img src="/img/login/apple.png" alt="Apple" /></button>
        </div>
      </div>
    </section>
  </main>
</template>

<style scoped>
.login-page {
  display: grid;
  min-height: 100vh;
  grid-template-columns: minmax(0, 1fr) minmax(360px, 440px);
  gap: 42px;
  align-items: center;
  padding: 48px clamp(22px, 6vw, 86px);
  background:
    radial-gradient(circle at 18% 10%, rgba(175, 225, 255, 0.5), transparent 28%),
    radial-gradient(circle at 82% 8%, rgba(183, 238, 207, 0.42), transparent 24%),
    linear-gradient(180deg, color-mix(in srgb, var(--c-bg) 72%, white 28%) 0%, var(--c-bg) 100%);
}

.login-visual {
  min-height: min(640px, 72vh);
  overflow: hidden;
  border: 1px solid rgba(205, 222, 226, 0.78);
  border-radius: 34px;
  background:
    linear-gradient(90deg, rgba(255, 255, 255, 0.86) 0%, rgba(255, 255, 255, 0.25) 56%),
    url('/img/landing/campus-hero.jpg') center / cover;
  box-shadow: 0 28px 70px rgba(32, 69, 78, 0.12);
  position: relative;
}

.login-visual__card {
  position: absolute;
  left: 38px;
  bottom: 38px;
  max-width: 390px;
  border: 1px solid rgba(255, 255, 255, 0.64);
  border-radius: 28px;
  background: rgba(255, 255, 255, 0.72);
  box-shadow: 0 22px 52px rgba(32, 69, 78, 0.12);
  padding: 26px;
  backdrop-filter: blur(18px);
}

.login-visual__mark,
.login-panel__logo {
  display: grid;
  place-items: center;
  color: #fff;
  background: linear-gradient(
    135deg,
    color-mix(in srgb, var(--c-primary) 90%, white 10%),
    color-mix(in srgb, var(--c-primary) 54%, var(--c-info) 46%)
  );
  box-shadow: 0 14px 26px color-mix(in srgb, var(--c-primary) 22%, transparent);
  font-weight: 900;
}

.login-visual__mark {
  width: 48px;
  height: 48px;
  border-radius: 18px;
  font-size: 20px;
}

.login-visual h2 {
  margin: 18px 0 8px;
  color: var(--c-text-1);
  font-size: clamp(34px, 5vw, 54px);
  font-weight: 900;
  letter-spacing: -0.055em;
}

.login-visual p {
  margin: 0;
  color: var(--c-text-2);
  font-size: 16px;
  font-weight: 650;
  line-height: 1.75;
}

.login-panel {
  border: 1px solid rgba(205, 222, 226, 0.82);
  border-radius: 30px;
  background: rgba(255, 255, 255, 0.9);
  box-shadow: 0 26px 66px rgba(32, 69, 78, 0.12);
  padding: 30px;
  backdrop-filter: blur(18px);
}

.login-panel__brand {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 24px;
}

.login-panel__logo {
  width: 52px;
  height: 52px;
  flex: none;
  border-radius: 19px;
  font-size: 22px;
}

.login-panel h1 {
  margin: 0;
  color: var(--c-text-1);
  font-size: 23px;
  font-weight: 900;
  letter-spacing: -0.03em;
}

.login-panel p {
  margin: 4px 0 0;
}

.login-panel__brand p,
.login-links,
.login-mock,
.login-panel__notice {
  color: var(--c-text-2);
  font-size: 13px;
  line-height: 1.7;
}

.login-panel__notice {
  margin-bottom: 22px;
  border: 1px solid color-mix(in srgb, var(--c-warning) 22%, var(--c-border));
  border-radius: 18px;
  background: color-mix(in srgb, var(--c-warning) 7%, rgba(255, 255, 255, 0.88));
  color: color-mix(in srgb, var(--c-warning) 78%, var(--c-text-1));
  padding: 13px 15px;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.login-field span {
  display: block;
  margin-bottom: 8px;
  color: var(--c-text-2);
  font-size: 13px;
  font-weight: 780;
}

.login-field input {
  width: 100%;
  border: 1px solid var(--c-border);
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.74);
  color: var(--c-text-1);
  font-size: 14px;
  outline: none;
  padding: 13px 14px;
  transition: border-color 0.18s ease, box-shadow 0.18s ease, background 0.18s ease;
}

.login-field input:focus {
  border-color: color-mix(in srgb, var(--c-primary) 52%, var(--c-border));
  background: #fff;
  box-shadow: 0 0 0 4px color-mix(in srgb, var(--c-primary) 13%, transparent);
}

.login-consent {
  display: flex;
  gap: 10px;
  border: 1px solid var(--c-border);
  border-radius: 18px;
  background: rgba(246, 251, 255, 0.72);
  color: var(--c-text-2);
  font-size: 12px;
  line-height: 1.75;
  padding: 13px;
}

.login-consent input {
  width: 16px;
  height: 16px;
  flex: none;
  margin-top: 3px;
  accent-color: var(--c-primary);
}

.login-submit {
  min-height: 48px;
  border: 0;
  border-radius: 16px;
  background: linear-gradient(
    135deg,
    color-mix(in srgb, var(--c-primary) 88%, white 12%),
    color-mix(in srgb, var(--c-primary-hover) 92%, #06634b 8%)
  );
  box-shadow: 0 16px 30px color-mix(in srgb, var(--c-primary) 24%, transparent);
  color: #fff;
  cursor: pointer;
  font: inherit;
  font-size: 15px;
  font-weight: 900;
}

.login-mock {
  margin-top: 18px;
  border-top: 1px solid var(--c-border);
  padding-top: 16px;
}

.login-mock__top,
.login-mock__hint,
.login-third-party > div {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.login-switch {
  position: relative;
  width: 38px;
  height: 22px;
  border: 0;
  border-radius: 999px;
  background: var(--c-border);
  cursor: pointer;
}

.login-switch span {
  position: absolute;
  left: 3px;
  top: 3px;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: #fff;
  transition: transform 0.18s ease;
}

.login-switch--on {
  background: var(--c-primary);
}

.login-switch--on span {
  transform: translateX(16px);
}

.login-mock__hint {
  margin-top: 10px;
}

.login-mock__hint p {
  flex: 1;
}

.login-mock__hint strong {
  border: 1px solid var(--c-border);
  border-radius: 999px;
  color: var(--c-primary);
  font-size: 11px;
  padding: 2px 8px;
  white-space: nowrap;
}

.login-links {
  margin-top: 18px;
  text-align: center;
}

.login-links a {
  color: var(--c-primary);
  font-weight: 780;
}

.login-third-party {
  margin-top: 24px;
  border-top: 1px solid var(--c-border);
  padding-top: 18px;
  text-align: center;
}

.login-third-party > span {
  color: var(--c-text-3);
  font-size: 12px;
}

.login-third-party > div {
  justify-content: center;
  margin-top: 13px;
}

.login-third-party button {
  display: grid;
  width: 38px;
  height: 38px;
  place-items: center;
  border: 1px solid var(--c-border);
  border-radius: 15px;
  background: rgba(255, 255, 255, 0.76);
  cursor: pointer;
}

.login-third-party img {
  width: 22px;
  height: 22px;
}

@media (max-width: 900px) {
  .login-page {
    display: flex;
    justify-content: center;
    padding: 24px 14px;
  }

  .login-visual {
    display: none;
  }

  .login-panel {
    width: min(100%, 430px);
    padding: 24px;
  }
}

[data-theme="dark"] .login-page {
  background:
    radial-gradient(circle at 18% 10%, rgba(45, 212, 191, 0.16), transparent 28%),
    var(--c-bg);
}

[data-theme="dark"] .login-panel,
[data-theme="dark"] .login-visual__card {
  border-color: rgba(45, 58, 73, 0.86);
  background: rgba(20, 27, 37, 0.9);
}

[data-theme="dark"] .login-panel__notice {
  border-color: color-mix(in srgb, var(--c-warning) 18%, rgba(68, 89, 112, 0.74));
  background: color-mix(in srgb, var(--c-warning) 10%, rgba(20, 27, 37, 0.92));
  color: color-mix(in srgb, var(--c-warning) 64%, #fef3c7);
}

[data-theme="dark"] .login-field input,
[data-theme="dark"] .login-consent,
[data-theme="dark"] .login-third-party button {
  background: rgba(24, 32, 43, 0.76);
}
</style>
