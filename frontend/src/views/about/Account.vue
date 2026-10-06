<script setup>
import { resetSocialRealtimeOnAuthChange } from '../../composables/useSocialRealtime.js'
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useToast } from '@/composables/useToast'
import AppDialog from '@/components/ui/AppDialog.vue'
import {
  deleteCampusCredential,
  getCampusCredentialStatus,
  revokeCampusCredentialConsent,
  updateCampusQuickAuth
} from '@/api/campusCredential'

const router = useRouter()
const { t } = useI18n()
const { success: toastSuccess } = useToast()

const loading = ref(false)
const submitting = ref(false)
const status = ref(null)
const statusSupported = ref(true)
const statusError = ref('')
const confirmAction = ref('')
const confirmOpen = ref(false)

const revokeDescription = computed(() => t('about.account.revokeDescription'))
const deleteDescription = computed(() => t('about.account.deleteDescription'))

const hasLiveStatus = computed(() => !!status.value && statusSupported.value)
const canEnableQuickAuth = computed(() => !!status.value?.hasActiveConsent && !!status.value?.hasSavedCredential)
const consentStatusClass = computed(() => (status.value?.hasActiveConsent ? 'account-status--success' : 'account-status--warning'))
const credentialStatusClass = computed(() => (status.value?.hasSavedCredential ? 'account-status--success' : 'account-status--muted'))
const quickAuthStatusClass = computed(() => (status.value?.quickAuthEnabled ? 'account-status--success' : 'account-status--muted'))

const detailRows = computed(() => {
  if (!status.value) return []
  return [
    { label: t('about.account.campusAccount'), value: status.value.maskedCampusAccount || t('about.account.notProvided') },
    { label: t('about.account.consentedAt'), value: formatDate(status.value.consentedAt) },
    { label: t('about.account.revokedAt'), value: formatDate(status.value.revokedAt) }
  ]
})

function formatDate(value) {
  if (!value) return t('about.account.none')
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return t('about.account.none')
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

function hasLoginToken() {
  try {
    return !!localStorage.getItem('token')
  } catch (_) {
    return false
  }
}

async function loadStatus() {
  if (!hasLoginToken()) {
    status.value = null
    statusSupported.value = false
    statusError.value = t('about.account.loginRequired')
    return
  }

  loading.value = true
  statusError.value = ''
  try {
    status.value = await getCampusCredentialStatus()
    statusSupported.value = true
  } catch (_) {
    statusSupported.value = false
    statusError.value = t('about.account.statusUnavailable')
  } finally {
    loading.value = false
  }
}

function openConfirm(action) {
  confirmAction.value = action
  confirmOpen.value = true
}

function closeConfirm() {
  confirmOpen.value = false
  confirmAction.value = ''
}

async function performConfirmedAction() {
  if (!confirmAction.value) return
  submitting.value = true
  try {
    if (confirmAction.value === 'revoke') {
      status.value = await revokeCampusCredentialConsent()
      toastSuccess(t('about.account.revokeSuccess'))
      forceRelogin()
      return
    }
    if (confirmAction.value === 'delete') {
      status.value = await deleteCampusCredential()
      toastSuccess(t('about.account.deleteSuccess'))
      forceRelogin()
      return
    }
  } finally {
    submitting.value = false
    closeConfirm()
  }
}

async function toggleQuickAuth(enabled) {
  submitting.value = true
  try {
    status.value = await updateCampusQuickAuth(enabled)
    toastSuccess(enabled ? t('about.account.quickAuthOnSuccess') : t('about.account.quickAuthOffSuccess'))
  } finally {
    submitting.value = false
  }
}

function forceRelogin() {
  localStorage.removeItem('token')
  resetSocialRealtimeOnAuthChange()
  sessionStorage.clear()
  setTimeout(() => router.replace('/login'), 700)
}

onMounted(() => {
  loadStatus()
})
</script>

<template>
  <div class="min-h-screen bg-[var(--color-surface)]">
    <div class="sticky top-0 left-0 right-0 h-[50px] bg-[var(--color-surface)] shadow-md flex items-center px-4 z-[1000]">
      <span class="text-[var(--color-primary)] text-[15px] cursor-pointer mr-4" @click="router.back()">{{ t('common.back') }}</span>
      <h2 class="flex-1 text-center text-lg font-medium text-[var(--c-text-1)] m-0">{{ t('about.account.title') }}</h2>
      <span class="w-10"></span>
    </div>

    <div class="max-w-2xl mx-auto px-4 py-6">
      <section class="bg-[var(--color-surface)] rounded-xl p-4 shadow-sm">
        <div class="flex flex-col gap-1">
          <h3 class="m-0 text-base font-semibold text-[var(--c-text-1)]">{{ t('about.account.sectionTitle') }}</h3>
          <p class="m-0 text-sm leading-6 text-[var(--c-text-2)]">{{ t('about.account.sectionDesc') }}</p>
        </div>

        <div class="mt-4 border-t border-[var(--c-border-light)] pt-4">
          <div class="flex items-center justify-between gap-3">
            <p class="m-0 text-sm font-medium text-[var(--c-text-1)]">{{ t('about.account.statusTitle') }}</p>
            <button
              type="button"
              class="shrink-0 rounded-full border border-[var(--c-border)] bg-transparent px-3 py-1 text-xs text-[var(--c-text-2)] cursor-pointer disabled:cursor-not-allowed disabled:opacity-60"
              :disabled="loading || submitting"
              @click="loadStatus"
            >
              {{ t('about.account.refresh') }}
            </button>
          </div>
          <p v-if="loading" class="mt-3 text-sm text-[var(--c-text-3)]">{{ t('about.account.loading') }}</p>
          <template v-else-if="hasLiveStatus">
            <div class="mt-3 grid gap-2 sm:grid-cols-3">
              <div class="rounded-lg bg-[var(--c-bg)] px-3 py-3">
                <div class="text-xs text-[var(--c-text-3)]">{{ t('about.account.consentStatus') }}</div>
                <div
                  class="mt-1 text-sm font-medium"
                  :class="consentStatusClass"
                >
                  {{ status.hasActiveConsent ? t('about.account.authorized') : t('about.account.unauthorized') }}
                </div>
              </div>
              <div class="rounded-lg bg-[var(--c-bg)] px-3 py-3">
                <div class="text-xs text-[var(--c-text-3)]">{{ t('about.account.savedCredential') }}</div>
                <div
                  class="mt-1 text-sm font-medium"
                  :class="credentialStatusClass"
                >
                  {{ status.hasSavedCredential ? t('about.account.yes') : t('about.account.no') }}
                </div>
              </div>
              <div class="rounded-lg bg-[var(--c-bg)] px-3 py-3">
                <div class="text-xs text-[var(--c-text-3)]">{{ t('about.account.quickAuth') }}</div>
                <div
                  class="mt-1 text-sm font-medium"
                  :class="quickAuthStatusClass"
                >
                  {{ status.quickAuthEnabled ? t('about.account.enabled') : t('about.account.disabled') }}
                </div>
              </div>
            </div>

            <dl class="mt-4 grid gap-2 text-sm">
              <div
                v-for="row in detailRows"
                :key="row.label"
                class="flex items-start justify-between gap-4 border-t border-[var(--c-border-light)] pt-2"
              >
                <dt class="text-[var(--c-text-3)]">{{ row.label }}</dt>
                <dd class="m-0 max-w-[60%] text-right text-[var(--c-text-1)] break-words">{{ row.value }}</dd>
              </div>
            </dl>

            <div class="mt-4 flex flex-col gap-2">
              <button
                v-if="status.quickAuthEnabled"
                type="button"
                class="w-full rounded-lg border border-[var(--c-border)] bg-transparent px-4 py-2.5 text-sm text-[var(--c-text-2)] cursor-pointer disabled:cursor-not-allowed disabled:opacity-60"
                :disabled="submitting"
                @click="toggleQuickAuth(false)"
              >
                {{ t('about.account.disableQuickAuth') }}
              </button>
              <button
                v-else
                type="button"
                class="w-full rounded-lg border border-[var(--c-primary)] bg-[var(--c-primary)] px-4 py-2.5 text-sm text-white cursor-pointer disabled:cursor-not-allowed disabled:opacity-60"
                :disabled="submitting || !canEnableQuickAuth"
                @click="toggleQuickAuth(true)"
              >
                {{ t('about.account.enableQuickAuth') }}
              </button>
              <p
                v-if="!status.quickAuthEnabled && !canEnableQuickAuth"
                class="m-0 text-xs leading-5 text-[var(--c-text-3)]"
              >
                {{ t('about.account.quickAuthHint') }}
              </p>
            </div>

            <div class="mt-5 border-t border-[var(--c-border-light)] pt-4">
              <p class="m-0 text-xs font-medium text-[var(--c-text-3)]">{{ t('about.account.dangerTitle') }}</p>
              <div class="mt-3 grid gap-3 md:grid-cols-2">
                <button
                  type="button"
                  class="account-danger-action account-danger-action--warning w-full rounded-lg px-4 py-2.5 text-sm cursor-pointer disabled:cursor-not-allowed disabled:opacity-60"
                  :disabled="submitting"
                  @click="openConfirm('revoke')"
                >
                  {{ t('about.account.revoke') }}
                </button>
                <button
                  type="button"
                  class="account-danger-action account-danger-action--danger w-full rounded-lg px-4 py-2.5 text-sm cursor-pointer disabled:cursor-not-allowed disabled:opacity-60"
                  :disabled="submitting"
                  @click="openConfirm('delete')"
                >
                  {{ t('about.account.deleteCredential') }}
                </button>
              </div>
            </div>
          </template>
          <template v-else>
            <p class="mt-3 text-sm text-[var(--c-text-3)]">{{ statusError }}</p>
          </template>
        </div>

        <div class="mt-4 grid gap-3 md:grid-cols-2">
            <button
              type="button"
              class="w-full rounded-lg border border-[var(--c-primary)] bg-[var(--c-primary)] px-4 py-2.5 text-sm text-white cursor-pointer"
              @click="router.push('/user/feedback')"
            >
              {{ t('about.account.feedbackAction') }}
            </button>
            <button
              type="button"
              class="w-full rounded-lg border border-[var(--c-border)] bg-transparent px-4 py-2.5 text-sm text-[var(--c-text-2)] cursor-pointer"
              @click="router.push('/policy/privacy')"
            >
              {{ t('about.account.privacyAction') }}
            </button>
          </div>
      </section>

      <section class="mt-4 bg-[var(--color-surface)] rounded-xl p-4 shadow-sm">
        <div class="text-sm leading-relaxed text-[var(--c-text-2)] [&_p]:my-3 [&_img]:max-w-full [&_img]:h-auto [&_img]:my-4">
          <p class="text-center font-medium text-[var(--c-text-1)]">{{ t('about.account.guide.title') }}</p>
          <p class="text-center">{{ t('about.account.guide.updated') }}</p>
          <p class="indent-8">{{ t('about.account.guide.welcome') }} <span class="account-inline-emphasis">{{ t('about.account.guide.firstUse') }}</span></p>
          <p class="indent-8">{{ t('about.account.guide.login') }} <span class="account-inline-emphasis">{{ t('about.account.guide.usernameHint') }}</span> {{ t('about.account.guide.activation') }}</p>
          <p class="indent-8">{{ t('about.account.guide.steps') }}</p>
          <p class="text-center"><img class="w-full h-auto" src="/img/about/application/account_1.png" :alt="t('about.account.guide.homepageAlt')"></p>
          <p class="text-left indent-8">{{ t('about.account.guide.homepage') }}</p>
          <p class="text-center"><img class="w-full h-auto" src="/img/about/application/account_2.png" :alt="t('about.account.guide.lookupAlt')"></p>
          <p class="text-left indent-8">{{ t('about.account.guide.lookup') }}</p>
          <p class="text-center"><img class="w-full h-auto" src="/img/about/application/account_3.png" :alt="t('about.account.guide.resultAlt')"></p>
          <p class="text-left indent-8">{{ t('about.account.guide.result') }} <span class="account-inline-emphasis">{{ t('about.account.guide.passwordHint') }}</span></p>
        </div>
      </section>
      </div>

    <AppDialog
      :open="confirmOpen"
      :title="confirmAction === 'revoke' ? t('about.account.revokeTitle') : t('about.account.deleteTitle')"
      :description="confirmAction === 'revoke' ? revokeDescription : deleteDescription"
      @close="closeConfirm"
      @confirm="performConfirmedAction"
    />
  </div>
</template>

<style scoped>
.account-status--success {
  color: color-mix(in srgb, var(--c-primary) 84%, #0f766e);
}

.account-status--warning {
  color: color-mix(in srgb, var(--c-warning) 76%, #a16207);
}

.account-status--muted {
  color: var(--c-text-2);
}

.account-danger-action {
  border: 1px solid color-mix(in srgb, var(--account-danger-accent) 24%, var(--c-border));
  background: color-mix(in srgb, var(--account-danger-accent) 8%, var(--c-surface));
  color: color-mix(in srgb, var(--account-danger-accent) 76%, var(--c-text-1));
  transition: background 0.18s ease, border-color 0.18s ease, color 0.18s ease;
}

.account-danger-action--warning {
  --account-danger-accent: var(--c-warning);
}

.account-danger-action--danger {
  --account-danger-accent: var(--c-danger);
}

.account-danger-action:hover {
  background: color-mix(in srgb, var(--account-danger-accent) 12%, var(--c-surface));
}

.account-inline-emphasis {
  color: color-mix(in srgb, var(--c-danger) 74%, var(--c-text-1));
  font-weight: 600;
}

[data-theme="dark"] .account-status--success {
  color: color-mix(in srgb, var(--c-primary) 72%, #d1fae5);
}

[data-theme="dark"] .account-status--warning {
  color: color-mix(in srgb, var(--c-warning) 72%, #fef3c7);
}

[data-theme="dark"] .account-danger-action {
  border-color: color-mix(in srgb, var(--account-danger-accent) 18%, rgba(68, 89, 112, 0.74));
  background: color-mix(in srgb, var(--account-danger-accent) 10%, rgba(24, 38, 53, 0.9));
  color: color-mix(in srgb, var(--account-danger-accent) 62%, #f8fafc);
}

[data-theme="dark"] .account-danger-action:hover {
  background: color-mix(in srgb, var(--account-danger-accent) 14%, rgba(24, 38, 53, 0.9));
}

[data-theme="dark"] .account-inline-emphasis {
  color: color-mix(in srgb, var(--c-danger) 58%, #fee2e2);
}
</style>
