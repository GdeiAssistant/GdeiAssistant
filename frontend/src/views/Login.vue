<script setup>
import { resetSocialRealtimeOnAuthChange } from '../composables/useSocialRealtime.js'
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
      resetSocialRealtimeOnAuthChange()
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
      <div class="login-visual__top">
        <img class="login-visual__mark" src="/favicon.svg" alt="" width="40" height="40" />
      </div>
      <div class="login-visual__card">
        <h2>{{ t('loginPage.title') }}</h2>
        <p>{{ t('loginPage.visualIntro') }}</p>
      </div>
      <svg class="login-visual__grid" viewBox="0 0 400 400" preserveAspectRatio="none" focusable="false">
        <defs>
          <pattern id="login-grid" width="40" height="40" patternUnits="userSpaceOnUse">
            <path d="M40 0H0V40" fill="none" stroke="currentColor" stroke-width="1" />
          </pattern>
        </defs>
        <rect width="400" height="400" fill="url(#login-grid)" />
      </svg>
    </section>

    <section class="login-panel" aria-labelledby="login-title">
      <div class="login-panel__brand">
        <img class="login-panel__logo" src="/favicon.svg" alt="" width="44" height="44" />
        <div>
          <h1 id="login-title">{{ t('loginPage.title') }}</h1>
          <p>{{ t('loginPage.subtitle') }}</p>
        </div>
      </div>

      <div class="login-panel__notice">
        {{ t('loginPage.credentialNotice') }}
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
            role="switch"
            :aria-checked="mockMode"
            :aria-label="t('loginPage.mockMode')"
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
  grid-template-columns: minmax(0, 1fr) minmax(400px, 520px);
  background: var(--c-bg);
}

/* Brand column: solid deep brand green, quiet line grid, copy anchored bottom-left */
.login-visual {
  position: sticky;
  top: 0;
  display: flex;
  height: 100vh;
  flex-direction: column;
  justify-content: space-between;
  overflow: hidden;
  padding: 40px 48px;
  background: #0A7559;
  color: #fff;
}

[data-theme="dark"] .login-visual {
  background: #0F3A2E;
}

.login-visual__grid {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  color: rgb(255 255 255 / 7%);
  pointer-events: none;
}

.login-visual__top,
.login-visual__card {
  position: relative;
  z-index: 1;
}

.login-visual__mark {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  box-shadow: 0 0 0 1px rgb(255 255 255 / 18%);
}

.login-visual__card {
  max-width: 480px;
}

.login-visual h2 {
  margin: 0;
  color: #fff;
  font-size: clamp(36px, 4.4vw, 56px);
  font-weight: 700;
  letter-spacing: -0.01em;
  line-height: 1.1;
}

.login-visual p {
  margin: 16px 0 0;
  color: rgb(255 255 255 / 82%);
  font-size: 17px;
  line-height: 1.7;
}

/* Form column */
.login-panel {
  display: flex;
  flex-direction: column;
  gap: 20px;
  justify-content: center;
  padding: 48px clamp(24px, 4vw, 56px);
  border-left: 1px solid var(--c-border);
  background: var(--c-surface);
}

.login-panel__brand {
  display: flex;
  align-items: center;
  gap: 14px;
}

.login-panel__logo {
  display: none;
  width: 44px;
  height: 44px;
  flex: none;
  border-radius: 11px;
}

.login-panel h1 {
  margin: 0;
  color: var(--c-text-1);
  font-size: 24px;
  font-weight: 700;
  line-height: 1.3;
}

.login-panel__brand p {
  margin: 4px 0 0;
  color: var(--c-text-2);
  font-size: 14px;
}

.login-panel__notice {
  padding: 12px 14px;
  border: 1px solid color-mix(in srgb, var(--c-primary) 22%, var(--c-border));
  border-radius: var(--radius-control);
  background: var(--c-primary-soft);
  color: var(--c-text-1);
  font-size: 12px;
  line-height: 1.7;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.login-field {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.login-field span {
  color: var(--c-text-1);
  font-size: 13px;
  font-weight: 600;
}

.login-field input {
  width: 100%;
  min-height: 44px;
  padding: 0 14px;
  border: 1px solid var(--c-border);
  border-radius: var(--radius-control);
  background: var(--c-bg);
  color: var(--c-text-1);
  font-size: 15px;
  outline: none;
  transition: border-color 0.15s ease, box-shadow 0.15s ease, background-color 0.15s ease;
}

.login-field input:hover {
  border-color: color-mix(in srgb, var(--c-text-3) 50%, var(--c-border));
}

.login-field input:focus {
  border-color: var(--c-primary);
  background: var(--c-surface);
  box-shadow: 0 0 0 3px color-mix(in srgb, var(--c-primary) 18%, transparent);
}

.login-consent {
  display: grid;
  grid-template-columns: 18px minmax(0, 1fr);
  gap: 10px;
  align-items: start;
  color: var(--c-text-2);
  font-size: 12px;
  line-height: 1.7;
  cursor: pointer;
}

.login-consent input {
  width: 18px;
  height: 18px;
  margin: 2px 0 0;
  accent-color: var(--c-primary);
  cursor: pointer;
}

.login-submit {
  min-height: 46px;
  border: 0;
  border-radius: var(--radius-control);
  background: var(--c-primary);
  color: var(--c-on-primary);
  font: inherit;
  font-size: 15px;
  font-weight: 650;
  cursor: pointer;
  transition: background-color 0.15s ease;
}

.login-submit:hover {
  background: var(--c-primary-hover);
}

.login-submit:focus-visible {
  outline: 2px solid var(--c-primary);
  outline-offset: 2px;
}

.login-mock {
  padding: 12px 14px;
  border: 1px solid var(--c-border);
  border-radius: var(--radius-control);
}

.login-mock__top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  color: var(--c-text-1);
  font-size: 13px;
  font-weight: 600;
}

.login-switch {
  position: relative;
  width: 40px;
  height: 24px;
  flex: none;
  border: 0;
  border-radius: 999px;
  background: var(--c-fill-3);
  cursor: pointer;
  transition: background-color 0.15s ease;
}

.login-switch span {
  position: absolute;
  top: 3px;
  left: 3px;
  width: 18px;
  height: 18px;
  border-radius: 999px;
  background: #fff;
  box-shadow: 0 1px 2px rgb(17 32 28 / 20%);
  transition: transform 0.15s ease;
}

.login-switch--on {
  background: var(--c-primary);
}

.login-switch--on span {
  transform: translateX(16px);
}

.login-mock__hint {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-top: 8px;
  color: var(--c-text-2);
  font-size: 12px;
  line-height: 1.6;
}

.login-mock__hint p {
  margin: 0;
}

.login-mock__hint strong {
  flex: none;
  color: var(--c-primary);
  font-weight: 600;
}

.login-links {
  color: var(--c-text-2);
  font-size: 12px;
  line-height: 1.8;
}

.login-links p {
  margin: 0;
}

.login-links a {
  font-weight: 500;
}

.login-third-party {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding-top: 16px;
  border-top: 1px solid var(--c-divider);
  color: var(--c-text-3);
  font-size: 12px;
}

.login-third-party > div {
  display: flex;
  gap: 8px;
}

.login-third-party button {
  display: grid;
  width: 40px;
  height: 40px;
  place-items: center;
  border: 1px solid var(--c-border);
  border-radius: var(--radius-control);
  background: var(--c-surface);
  cursor: pointer;
}

.login-third-party button:hover {
  border-color: color-mix(in srgb, var(--c-primary) 45%, var(--c-border));
}

.login-third-party img {
  width: 20px;
  height: 20px;
  object-fit: contain;
}

[data-theme="dark"] .login-third-party img {
  filter: brightness(1.1);
}

@media (max-width: 900px) {
  .login-page {
    grid-template-columns: minmax(0, 1fr);
  }

  .login-visual {
    display: none;
  }

  .login-panel {
    justify-content: flex-start;
    min-height: 100vh;
    padding: 32px 16px calc(32px + env(safe-area-inset-bottom, 0px));
    border-left: 0;
    background: var(--c-bg);
  }

  .login-panel__logo {
    display: block;
  }

  .login-field input {
    background: var(--c-surface);
  }
}

@media (min-width: 901px) and (max-height: 760px) {
  .login-panel {
    justify-content: flex-start;
  }
}
</style>
