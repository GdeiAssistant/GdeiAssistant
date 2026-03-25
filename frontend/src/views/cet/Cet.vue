<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { getCetNumber, queryCetScore } from '@/api/cet'
import { useToast } from '@/composables/useToast'
import request from '@/utils/request'

const router = useRouter()
const { t } = useI18n()
const { error: showError, loading: showLoading, hideLoading } = useToast()

const examNumber = ref('')
const name = ref('')
const vcode = ref('')
const loading = ref(false)
const vcodeUrl = ref('')
const cetResult = ref(null)
const showResult = ref(false)

function onExamNumberInput(e) {
  const v = (e.target.value || '').replace(/\D/g, '').slice(0, 15)
  examNumber.value = v
}

function refreshVcode() {
  vcode.value = ''
  request.get('/cet/checkcode').then((res) => {
    const payload = res && res.data
    const base64 = typeof payload === 'string' ? payload : (payload && payload.data)
    vcodeUrl.value = base64 ? 'data:image/jpg;base64,' + base64 : ''
  }).catch(() => {
    vcodeUrl.value = ''
  })
}

function importNumber() {
  loading.value = true
  showLoading(t('cetPage.importLoading'))
  getCetNumber().then((res) => {
    if (res && res.success && res.data) {
      const d = res.data
      examNumber.value = (d.number != null) ? String(d.number) : ''
      name.value = (d.name != null && d.name !== '') ? d.name : ''
      if (!examNumber.value && !name.value) {
        showError(t('cetPage.noSavedNumber'))
      }
    } else {
      showError(t('cetPage.noSavedNumber'))
    }
  }).catch(() => {
    // 错误由 request.js 全局拦截器统一提示，此处仅关闭 Loading
  }).finally(() => {
    loading.value = false
    hideLoading()
  })
}

function submitQuery() {
  const num = String(examNumber.value || '').trim()
  const n = String(name.value || '').trim()
  const code = String(vcode.value || '').trim()
  if (!num || !n) {
    showError(t('cetPage.missingFields'))
    return
  }
  if (num.length !== 15) {
    showError(t('cetPage.invalidNumber'))
    return
  }
  if (!code) {
    showError(t('cetPage.captchaRequired'))
    return
  }

  loading.value = true
  showLoading(t('cetPage.queryLoading'))
  queryCetScore(num, n, code).then((res) => {
    if (res && res.success && res.data) {
      cetResult.value = res.data
      showResult.value = true
      refreshVcode()
    }
  }).catch(() => {
    // 错误由 request.js 全局拦截器统一提示，此处仅关闭 Loading
    refreshVcode()
  }).finally(() => {
    loading.value = false
    hideLoading()
  })
}

function reQuery() {
  showResult.value = false
  cetResult.value = null
  name.value = ''
  examNumber.value = ''
  vcode.value = ''
  refreshVcode()
}

function onSaveNumber() {
  router.push('/cet/save').catch(() => {})
}

function openChsi() {
  window.open('https://www.chsi.com.cn/cet/', '_blank')
}

function openNeea() {
  window.open('https://cet.neea.edu.cn/cet/', '_blank')
}

onMounted(() => {
  refreshVcode()
})
</script>

<template>
  <div class="min-h-screen bg-[var(--c-bg)]">
    <!-- 查询表单 -->
    <template v-if="!showResult">
      <div class="sticky top-0 z-30 flex items-center h-[52px] px-5 bg-[var(--c-surface)]/90 backdrop-blur-xl border-b border-[var(--c-border)]">
      <button @click="$router.back()" class="text-[var(--c-primary)] text-sm font-medium">&larr; {{ t('common.back') }}</button>
      <span class="flex-1 text-center text-sm font-bold">{{ t('cetPage.title') }}</span>
        <div class="w-10"></div>
      </div>

      <div class="max-w-lg mx-auto px-4 py-6">
        <!-- Form card -->
        <div class="bg-[var(--c-surface)] rounded-2xl p-5 shadow-sm border border-[var(--c-border)]">
          <div class="space-y-4">
            <!-- 考号 -->
            <div>
              <label class="text-sm font-medium text-[var(--c-text-2)] mb-1.5 block">{{ t('cetPage.examNumberLabel') }}</label>
              <div class="flex gap-2">
                <input
                  :value="examNumber"
                  type="text"
                  inputmode="numeric"
                  maxlength="15"
                  :placeholder="t('cetPage.examNumberPlaceholder')"
                  class="flex-1 min-w-0 px-3 py-2.5 border border-[var(--c-border)] rounded-lg text-sm focus:border-[var(--c-primary)] focus:ring-2 focus:ring-[var(--c-primary)]/10 outline-none bg-[var(--c-surface)]"
                  @input="onExamNumberInput"
                />
                <button
                  type="button"
                  class="shrink-0 px-3 py-2.5 text-sm font-medium text-[var(--c-primary)] border border-[var(--c-primary)]/20 rounded-lg hover:bg-[var(--c-primary)]/5 transition-colors"
                  @click.prevent="importNumber"
                >
                  {{ t('cetPage.importButton') }}
                </button>
              </div>
            </div>

            <!-- 姓名 -->
            <div>
              <label class="text-sm font-medium text-[var(--c-text-2)] mb-1.5 block">{{ t('cetPage.nameLabel') }}</label>
              <input
                v-model="name"
                type="text"
                maxlength="20"
                :placeholder="t('cetPage.namePlaceholder')"
                class="w-full px-3 py-2.5 border border-[var(--c-border)] rounded-lg text-sm focus:border-[var(--c-primary)] focus:ring-2 focus:ring-[var(--c-primary)]/10 outline-none bg-[var(--c-surface)]"
              />
            </div>

            <!-- 验证码 -->
            <div>
              <label class="text-sm font-medium text-[var(--c-text-2)] mb-1.5 block">{{ t('cetPage.captchaLabel') }}</label>
              <div class="flex gap-3 items-center">
                <input
                  v-model="vcode"
                  type="text"
                  maxlength="10"
                  :placeholder="t('cetPage.captchaPlaceholder')"
                  class="flex-1 min-w-0 px-3 py-2.5 border border-[var(--c-border)] rounded-lg text-sm focus:border-[var(--c-primary)] focus:ring-2 focus:ring-[var(--c-primary)]/10 outline-none bg-[var(--c-surface)]"
                />
                <img
                  v-if="vcodeUrl"
                  :src="vcodeUrl"
                  :alt="t('cetPage.captchaLabel')"
                  class="h-10 w-[100px] rounded-lg cursor-pointer object-cover border border-[var(--c-border)]"
                  @click="refreshVcode"
                />
                <span
                  v-else
                  class="text-sm text-[var(--c-primary)] cursor-pointer shrink-0"
                  @click="refreshVcode"
                >{{ t('cetPage.refreshCaptcha') }}</span>
              </div>
            </div>
          </div>

          <button
            type="button"
            class="w-full bg-[var(--c-primary)] text-white rounded-lg py-2.5 font-semibold mt-6 transition-opacity hover:opacity-90"
            @click="submitQuery"
          >{{ t('cetPage.submit') }}</button>
        </div>

        <p class="mt-5 text-center text-sm text-[var(--c-text-2)]">
          {{ t('cetPage.savePromptPrefix') }}
          <a href="javascript:" class="text-[var(--c-primary)] font-medium" @click.prevent="onSaveNumber">{{ t('cetPage.savePromptLink') }}</a>
        </p>

        <!-- 备用查询入口 -->
        <div class="mt-6">
          <h3 class="text-xs font-semibold text-[var(--c-text-2)] uppercase tracking-wide mb-2">{{ t('cetPage.fallbackTitle') }}</h3>
          <div class="bg-[var(--c-surface)] rounded-xl border border-[var(--c-border)] divide-y divide-[var(--c-border)]">
            <a href="javascript:" class="flex items-center justify-between px-4 py-3 text-sm" @click.prevent="openChsi">
              <span>{{ t('cetPage.fallbackChsi') }}</span>
              <span class="text-[var(--c-text-3)]">&rsaquo;</span>
            </a>
            <a href="javascript:" class="flex items-center justify-between px-4 py-3 text-sm" @click.prevent="openNeea">
              <span>{{ t('cetPage.fallbackNeea') }}</span>
              <span class="text-[var(--c-text-3)]">&rsaquo;</span>
            </a>
          </div>
        </div>
      </div>
    </template>

    <!-- 查询结果 -->
    <template v-else>
      <div class="sticky top-0 z-30 flex items-center h-[52px] px-5 bg-[var(--c-surface)]/90 backdrop-blur-xl border-b border-[var(--c-border)]">
        <button @click="$router.back()" class="text-[var(--c-primary)] text-sm font-medium">&larr; 返回</button>
        <span class="flex-1 text-center text-sm font-bold">查询结果</span>
        <div class="w-10"></div>
      </div>

      <div class="max-w-lg mx-auto px-4 py-6" v-if="cetResult">
        <p class="text-center text-sm text-[var(--c-text-2)] mb-5">成绩仅供参考，请以成绩单为准</p>

        <div class="bg-[var(--c-surface)] rounded-2xl p-5 shadow-sm border border-[var(--c-border)]">
          <h2 class="text-lg font-bold text-center mb-1">{{ cetResult.name }}</h2>
          <p class="text-sm text-[var(--c-text-2)] text-center">{{ cetResult.type }}</p>
          <p class="text-sm text-[var(--c-text-2)] text-center mb-4">{{ cetResult.school }}</p>

          <div class="bg-[var(--c-primary)]/5 rounded-xl p-4 text-center mb-4">
            <div class="text-xs text-[var(--c-text-2)] mb-1">考试总分</div>
            <div class="font-mono text-3xl font-bold text-[var(--c-primary)]">{{ cetResult.totalScore }}</div>
          </div>

          <div class="divide-y divide-[var(--c-border-light)]">
            <div class="flex justify-between py-3">
              <span class="text-sm text-[var(--c-text-2)]">听力分数</span>
              <span class="text-sm font-semibold">{{ cetResult.listeningScore }}</span>
            </div>
            <div class="flex justify-between py-3">
              <span class="text-sm text-[var(--c-text-2)]">阅读分数</span>
              <span class="text-sm font-semibold">{{ cetResult.readingScore }}</span>
            </div>
            <div class="flex justify-between py-3">
              <span class="text-sm text-[var(--c-text-2)]">写作翻译</span>
              <span class="text-sm font-semibold">{{ cetResult.writingAndTranslatingScore }}</span>
            </div>
          </div>
        </div>

        <div class="mt-5 space-y-3">
          <button
            type="button"
            class="w-full bg-[var(--c-primary)] text-white rounded-lg py-2.5 font-semibold transition-opacity hover:opacity-90"
            @click="reQuery"
          >重新查询</button>
          <button
            type="button"
            class="w-full bg-[var(--c-surface)] text-[var(--c-text)] border border-[var(--c-border)] rounded-lg py-2.5 font-semibold transition-opacity hover:opacity-80"
            @click="router.push('/')"
          >返回主页</button>
        </div>
      </div>
    </template>
  </div>
</template>
