<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { login } from '../api/user.js'
import { useToast } from '@/composables/useToast'
import { isMockMode, toggleDataSourceMode } from '@/services/data-source.js'
import { MOCK_ACCOUNT_USERNAME, MOCK_ACCOUNT_PASSWORD, getMockCredentialsHint } from '@/constants/mock.js'

const router = useRouter()
const { t } = useI18n()
const { error: showError, loading: showLoading, hideLoading } = useToast()

const username = ref('')
const password = ref('')
const mockMode = ref(isMockMode())

function toggleMock() {
  toggleDataSourceMode()
  mockMode.value = isMockMode()
  if (mockMode.value) {
    username.value = MOCK_ACCOUNT_USERNAME
    password.value = MOCK_ACCOUNT_PASSWORD
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
  showLoading(t('loginPage.loading'))
  try {
    const res = await login(username.value.trim(), password.value)
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
  <div class="min-h-screen flex items-center justify-center bg-[var(--c-bg)] px-4">
    <div class="bg-[var(--c-surface)] rounded-2xl shadow-lg p-8 w-full max-w-[400px] border border-[var(--c-border)]">

      <!-- Logo / Brand -->
      <div class="flex flex-col items-center mb-8">
        <div
          class="w-14 h-14 rounded-xl bg-gradient-to-br from-emerald-400 to-green-600 flex items-center justify-center text-white text-2xl font-bold shadow-md mb-3"
        >
          G
        </div>
        <h1 class="text-xl font-semibold text-[var(--c-text)]">{{ t('loginPage.title') }}</h1>
        <p class="text-sm text-[var(--c-text-secondary)] mt-1">{{ t('loginPage.subtitle') }}</p>
      </div>

      <!-- Form -->
      <form @submit.prevent="handleLogin" class="space-y-5">
        <!-- Username -->
        <div>
          <label class="block text-sm font-medium text-[var(--c-text-secondary)] mb-1.5">{{ t('loginPage.usernameLabel') }}</label>
          <input
            v-model="username"
            type="text"
            maxlength="20"
            :placeholder="t('loginPage.usernamePlaceholder')"
            class="w-full rounded-lg border border-[var(--c-border)] bg-[var(--c-bg)] px-3.5 py-2.5 text-sm text-[var(--c-text)] placeholder-[var(--c-text-tertiary)] outline-none transition focus:border-[var(--c-primary)] focus:ring-2 focus:ring-[var(--c-primary)]/20"
          />
        </div>

        <!-- Password -->
        <div>
          <label class="block text-sm font-medium text-[var(--c-text-secondary)] mb-1.5">{{ t('loginPage.passwordLabel') }}</label>
          <input
            v-model="password"
            type="password"
            maxlength="35"
            :placeholder="t('loginPage.passwordPlaceholder')"
            class="w-full rounded-lg border border-[var(--c-border)] bg-[var(--c-bg)] px-3.5 py-2.5 text-sm text-[var(--c-text)] placeholder-[var(--c-text-tertiary)] outline-none transition focus:border-[var(--c-primary)] focus:ring-2 focus:ring-[var(--c-primary)]/20"
          />
        </div>

        <!-- Submit -->
        <button
          type="submit"
          class="w-full bg-[var(--c-primary)] text-white rounded-lg py-2.5 font-semibold hover:bg-[var(--c-primary-hover)] transition cursor-pointer"
        >
          {{ t('loginPage.submit') }}
        </button>
      </form>

      <!-- Mock mode toggle -->
      <div class="mt-5 flex items-center justify-between px-1">
        <div class="flex items-center gap-2">
          <button
            type="button"
            class="relative w-10 h-5 rounded-full transition-colors"
            :class="mockMode ? 'bg-[var(--c-primary)]' : 'bg-[var(--c-border)]'"
            @click="toggleMock"
          >
            <span
              class="absolute top-0.5 left-0.5 w-4 h-4 rounded-full bg-white shadow transition-transform"
              :class="mockMode ? 'translate-x-5' : 'translate-x-0'"
            />
          </button>
          <span class="text-xs text-[var(--c-text-3)]">{{ t('loginPage.mockMode') }}</span>
        </div>
        <span v-if="mockMode" class="text-[10px] text-[var(--c-primary)] font-medium">Mock</span>
      </div>
      <div v-if="mockMode" class="mt-2 px-3 py-2 rounded-lg bg-[var(--c-primary-50)] text-xs text-[var(--c-primary)]">
        {{ `${getMockCredentialsHint()}${t('loginPage.autoFilledSuffix')}` }}
      </div>

      <!-- Footer links -->
      <div class="mt-5 text-center text-xs text-[var(--c-text-tertiary)] leading-relaxed">
        <p>
          {{ t('loginPage.accountHelpPrefix') }}
          <router-link to="/about/account" class="text-[var(--c-primary)] hover:underline">{{ t('loginPage.accountHelpLink') }}</router-link>
        </p>
        <p class="mt-1">
          {{ t('loginPage.agreementPrefix') }}
          <router-link to="/agreement" class="text-[var(--c-primary)] hover:underline">{{ t('loginPage.agreementLink') }}</router-link>
          {{ t('loginPage.and') }}
          <router-link to="/policy/privacy" class="text-[var(--c-primary)] hover:underline">{{ t('loginPage.privacyLink') }}</router-link>
        </p>
      </div>

      <!-- Third-party login -->
      <div class="mt-8">
        <div class="flex items-center gap-3 mb-5">
          <div class="flex-1 h-px bg-[var(--c-border)]"></div>
          <span class="text-xs text-[var(--c-text-tertiary)] whitespace-nowrap">{{ t('loginPage.otherLogin') }}</span>
          <div class="flex-1 h-px bg-[var(--c-border)]"></div>
        </div>
        <div class="flex justify-center items-center gap-5">
          <img
            src="/img/login/wechat.png"
            alt="WeChat"
            class="w-8 h-8 rounded-full cursor-pointer transition-opacity hover:opacity-70 active:opacity-50"
            @click="handleThirdPartyLogin('WeChat')"
          />
          <img
            src="/img/login/qq.png"
            alt="QQ"
            class="w-8 h-8 rounded-full cursor-pointer transition-opacity hover:opacity-70 active:opacity-50"
            @click="handleThirdPartyLogin('QQ')"
          />
          <img
            src="/img/login/weibo.png"
            alt="Weibo"
            class="w-8 h-8 rounded-full cursor-pointer transition-opacity hover:opacity-70 active:opacity-50"
            @click="handleThirdPartyLogin('Weibo')"
          />
          <img
            src="/img/login/apple.png"
            alt="Apple"
            class="w-8 h-8 rounded-full cursor-pointer transition-opacity hover:opacity-70 active:opacity-50"
            @click="handleThirdPartyLogin('Apple')"
          />
        </div>
      </div>

    </div>
  </div>
</template>
