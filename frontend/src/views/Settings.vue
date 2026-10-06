<template>
  <div class="settings-page space-y-4 pb-20">
    <!-- Privacy & Features -->
    <AppCard>
      <template #header>
        <span class="text-sm font-medium text-[var(--c-text-2)]">{{ t('settings.section.preference') }}</span>
      </template>

      <RouterLink to="/user/privacy-setting"
                  class="campus-list-row flex items-center gap-3 px-4 py-3 border-b border-[var(--c-border-light)] last:border-0 hover:bg-[var(--c-surface-hover)] cursor-pointer no-underline text-inherit">
        <Shield class="w-5 h-5 text-[var(--c-text-3)]" />
        <span class="flex-1 text-[var(--c-text-1)]">{{ $t('profile.privacySetting') }}</span>
        <ChevronRight class="w-4 h-4 text-[var(--c-text-3)]" />
      </RouterLink>

      <RouterLink to="/user/feature-manage"
                  class="campus-list-row flex items-center gap-3 px-4 py-3 border-b border-[var(--c-border-light)] hover:bg-[var(--c-surface-hover)] cursor-pointer no-underline text-inherit">
        <LayoutGrid class="w-5 h-5 text-[var(--c-text-3)]" />
        <span class="flex-1 text-[var(--c-text-1)]">{{ $t('profile.featureManage') }}</span>
        <ChevronRight class="w-4 h-4 text-[var(--c-text-3)]" />
      </RouterLink>

      <RouterLink to="/appearance"
                  class="campus-list-row flex items-center gap-3 px-4 py-3 hover:bg-[var(--c-surface-hover)] cursor-pointer no-underline text-inherit">
        <Palette class="w-5 h-5 text-[var(--c-text-3)]" />
        <span class="flex-1 text-[var(--c-text-1)]">{{ $t('appearance.title') }}</span>
        <ChevronRight class="w-4 h-4 text-[var(--c-text-3)]" />
      </RouterLink>
    </AppCard>

    <!-- Security -->
    <AppCard>
      <template #header>
        <span class="text-sm font-medium text-[var(--c-text-2)]">{{ t('settings.section.security') }}</span>
      </template>

      <button type="button"
              class="campus-list-row w-full flex items-center gap-3 px-4 py-3 border-b border-[var(--c-border-light)] hover:bg-[var(--c-surface-hover)] cursor-pointer bg-transparent border-x-0 border-t-0 text-left font-inherit"
              @click="handlePasswordClick">
        <Lock class="w-5 h-5 text-[var(--c-text-3)]" />
        <span class="flex-1 text-[var(--c-text-1)] font-medium">{{ $t('profile.changePassword') }}</span>
        <ChevronRight class="w-4 h-4 text-[var(--c-text-3)]" />
      </button>

      <RouterLink to="/about/account"
                  class="campus-list-row flex items-center gap-3 px-4 py-3 border-b border-[var(--c-border-light)] hover:bg-[var(--c-surface-hover)] cursor-pointer no-underline text-inherit">
        <Shield class="w-5 h-5 text-[var(--c-text-3)]" />
        <span class="flex-1 text-[var(--c-text-1)]">{{ $t('profile.campusCredentialManage') }}</span>
        <ChevronRight class="w-4 h-4 text-[var(--c-text-3)]" />
      </RouterLink>

      <RouterLink to="/user/login-record"
                  class="campus-list-row flex items-center gap-3 px-4 py-3 border-b border-[var(--c-border-light)] hover:bg-[var(--c-surface-hover)] cursor-pointer no-underline text-inherit">
        <Smartphone class="w-5 h-5 text-[var(--c-text-3)]" />
        <span class="flex-1 text-[var(--c-text-1)]">{{ $t('profile.loginRecord') }}</span>
        <ChevronRight class="w-4 h-4 text-[var(--c-text-3)]" />
      </RouterLink>

      <RouterLink to="/user/realname"
                  class="campus-list-row flex items-center gap-3 px-4 py-3 border-b border-[var(--c-border-light)] hover:bg-[var(--c-surface-hover)] cursor-pointer no-underline text-inherit">
        <BadgeCheck class="w-5 h-5 text-[var(--c-text-3)]" />
        <span class="flex-1 text-[var(--c-text-1)]">{{ $t('profile.realname') }}</span>
        <ChevronRight class="w-4 h-4 text-[var(--c-text-3)]" />
      </RouterLink>

      <RouterLink to="/user/bind-phone"
                  class="campus-list-row flex items-center gap-3 px-4 py-3 border-b border-[var(--c-border-light)] hover:bg-[var(--c-surface-hover)] cursor-pointer no-underline text-inherit">
        <Smartphone class="w-5 h-5 text-[var(--c-text-3)]" />
        <span class="flex-1 text-[var(--c-text-1)]">{{ $t('profile.bindPhone') }}</span>
        <ChevronRight class="w-4 h-4 text-[var(--c-text-3)]" />
      </RouterLink>

      <RouterLink to="/user/bind-email"
                  class="campus-list-row flex items-center gap-3 px-4 py-3 border-b border-[var(--c-border-light)] hover:bg-[var(--c-surface-hover)] cursor-pointer no-underline text-inherit">
        <Mail class="w-5 h-5 text-[var(--c-text-3)]" />
        <span class="flex-1 text-[var(--c-text-1)]">{{ $t('profile.bindEmail') }}</span>
        <ChevronRight class="w-4 h-4 text-[var(--c-text-3)]" />
      </RouterLink>

      <RouterLink to="/user/delete"
                  class="campus-list-row flex items-center gap-3 px-4 py-3 hover:bg-[var(--c-surface-hover)] cursor-pointer no-underline text-inherit">
        <Trash2 class="w-5 h-5 text-[var(--c-text-3)]" />
        <span class="flex-1 text-[var(--c-text-1)]">{{ $t('profile.deleteAccount') }}</span>
        <ChevronRight class="w-4 h-4 text-[var(--c-text-3)]" />
      </RouterLink>
    </AppCard>

    <!-- Help & Feedback -->
    <AppCard>
      <RouterLink to="/user/feedback"
                  class="campus-list-row flex items-center gap-3 px-4 py-3 border-b border-[var(--c-border-light)] hover:bg-[var(--c-surface-hover)] cursor-pointer no-underline text-inherit">
        <MessageSquare class="w-5 h-5 text-[var(--c-text-3)]" />
        <span class="flex-1 text-[var(--c-text-1)]">{{ $t('profile.helpFeedback') }}</span>
        <ChevronRight class="w-4 h-4 text-[var(--c-text-3)]" />
      </RouterLink>

      <a href="https://www.wjx.top/m/47687434.aspx" target="_blank" rel="noopener noreferrer"
         class="campus-list-row flex items-center gap-3 px-4 py-3 border-b border-[var(--c-border-light)] hover:bg-[var(--c-surface-hover)] cursor-pointer no-underline text-inherit">
        <MessageCircle class="w-5 h-5 text-[var(--c-text-3)]" />
        <span class="flex-1 text-[var(--c-text-1)]">{{ $t('profile.reportChannel') }}</span>
        <ChevronRight class="w-4 h-4 text-[var(--c-text-3)]" />
      </a>

      <RouterLink to="/user/download"
                  class="campus-list-row flex items-center gap-3 px-4 py-3 hover:bg-[var(--c-surface-hover)] cursor-pointer no-underline text-inherit">
        <Download class="w-5 h-5 text-[var(--c-text-3)]" />
        <span class="flex-1 text-[var(--c-text-1)]">{{ $t('profile.downloadData') }}</span>
        <ChevronRight class="w-4 h-4 text-[var(--c-text-3)]" />
      </RouterLink>
    </AppCard>

    <!-- Logout -->
    <AppCard>
      <button type="button"
              class="w-full flex items-center justify-center gap-2 px-4 py-3 bg-transparent border-0 cursor-pointer font-inherit"
              @click="handleLogoutClick">
        <LogOut class="w-5 h-5 text-red-500" />
        <span class="text-red-500 font-medium">{{ $t('profile.logout') }}</span>
      </button>
    </AppCard>

    <AppDialog
      :open="showLogoutDialog"
      :title="$t('common.hint')"
      :description="$t('profile.logoutConfirm')"
      :confirm-text="$t('profile.logoutConfirmBtn')"
      confirm-tone="danger"
      @close="showLogoutDialog = false"
      @confirm="confirmLogout"
    />

    <AppDialog
      :open="showPwdDialog"
      :title="$t('common.hint')"
      :description="$t('profile.passwordNotAvailable')"
      :confirm-text="$t('common.iKnow')"
      :show-cancel="false"
      @close="showPwdDialog = false"
      @confirm="showPwdDialog = false"
    />
  </div>
</template>

<script setup>
import { resetSocialRealtimeOnAuthChange } from '../composables/useSocialRealtime.js'
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { logout } from '../api/user.js'
import { useToast } from '@/composables/useToast'
import AppCard from '@/components/ui/AppCard.vue'
import AppDialog from '@/components/ui/AppDialog.vue'
import {
  Shield, Lock, LayoutGrid, Palette, Smartphone, BadgeCheck,
  Mail, Trash2, Download, MessageSquare, MessageCircle, LogOut, ChevronRight
} from 'lucide-vue-next'

const router = useRouter()
const { t } = useI18n()
const { success: toastSuccess } = useToast()

const showLogoutDialog = ref(false)
const showPwdDialog = ref(false)

function handlePasswordClick() { showPwdDialog.value = true }
function handleLogoutClick() { showLogoutDialog.value = true }

async function doLogout() {
  try { await logout() } catch (_) {}
  localStorage.removeItem('token')
  resetSocialRealtimeOnAuthChange()
  sessionStorage.clear()
  toastSuccess(t('common.logoutSuccess'))
  setTimeout(() => router.replace('/login'), 600)
}

function confirmLogout() {
  showLogoutDialog.value = false
  doLogout()
}
</script>

<style scoped>
.settings-page {
  width: min(720px, calc(100% - 32px));
  margin: 0 auto;
  padding-top: 24px;
}

.settings-page :deep(.app-card) {
  box-shadow: none;
}

.settings-page :deep(.app-card__header) {
  min-height: 40px;
  padding: 14px 16px 8px;
  border-bottom: 0;
}

.settings-page :deep(.app-card__header) span {
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.04em;
  color: var(--c-text-3);
}

.settings-page .campus-list-row {
  min-height: 56px;
  gap: 14px;
  padding: 10px 16px;
  border-color: var(--c-divider);
  transition: background-color 0.16s ease;
}

.settings-page .campus-list-row > svg:first-child {
  flex: none;
  box-sizing: border-box;
  width: 32px;
  height: 32px;
  padding: 7px;
  border-radius: var(--radius-control);
  background: var(--c-primary-soft);
  color: var(--c-primary);
}

.settings-page .campus-list-row > span {
  font-size: 15px;
  font-weight: 500;
}

.settings-page .campus-list-row > svg:last-child {
  color: var(--c-text-3);
  transition: transform 0.16s ease, color 0.16s ease;
}

.settings-page .campus-list-row:hover > svg:last-child {
  color: var(--c-primary);
  transform: translateX(2px);
}

.settings-page .campus-list-row:focus-visible {
  outline: 2px solid var(--c-primary);
  outline-offset: -2px;
}

.settings-page > section:last-of-type button {
  min-height: 52px;
  transition: background-color 0.16s ease;
}

.settings-page > section:last-of-type button:hover {
  background: color-mix(in srgb, var(--c-danger) 8%, var(--c-surface));
}

.settings-page > section:last-of-type :is(svg, span) {
  color: var(--c-danger);
}

@media (min-width: 1024px) {
  .settings-page {
    padding-top: 32px;
  }
}

@media (prefers-reduced-motion: reduce) {
  .settings-page .campus-list-row,
  .settings-page .campus-list-row > svg:last-child {
    transition: none;
  }
}
</style>
