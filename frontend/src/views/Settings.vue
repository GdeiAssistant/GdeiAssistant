<template>
  <div class="space-y-4 pb-20">
    <!-- Privacy & Features -->
    <AppCard>
      <template #header>
        <span class="text-sm font-medium text-[var(--c-text-2)]">{{ t('settings.section.preference') }}</span>
      </template>

      <RouterLink to="/user/privacy-setting"
                  class="flex items-center gap-3 px-4 py-3 border-b border-[var(--c-border-light)] last:border-0 hover:bg-[var(--c-surface-hover)] cursor-pointer no-underline text-inherit">
        <Shield class="w-5 h-5 text-[var(--c-text-3)]" />
        <span class="flex-1 text-[var(--c-text-1)]">{{ $t('profile.privacySetting') }}</span>
        <ChevronRight class="w-4 h-4 text-[var(--c-text-3)]" />
      </RouterLink>

      <RouterLink to="/user/feature-manage"
                  class="flex items-center gap-3 px-4 py-3 border-b border-[var(--c-border-light)] hover:bg-[var(--c-surface-hover)] cursor-pointer no-underline text-inherit">
        <LayoutGrid class="w-5 h-5 text-[var(--c-text-3)]" />
        <span class="flex-1 text-[var(--c-text-1)]">{{ $t('profile.featureManage') }}</span>
        <ChevronRight class="w-4 h-4 text-[var(--c-text-3)]" />
      </RouterLink>

      <RouterLink to="/appearance"
                  class="flex items-center gap-3 px-4 py-3 hover:bg-[var(--c-surface-hover)] cursor-pointer no-underline text-inherit">
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
              class="w-full flex items-center gap-3 px-4 py-3 border-b border-[var(--c-border-light)] hover:bg-[var(--c-surface-hover)] cursor-pointer bg-transparent border-x-0 border-t-0 text-left font-inherit"
              @click="handlePasswordClick">
        <Lock class="w-5 h-5 text-[var(--c-text-3)]" />
        <span class="flex-1 text-[var(--c-text-1)]">{{ $t('profile.changePassword') }}</span>
        <ChevronRight class="w-4 h-4 text-[var(--c-text-3)]" />
      </button>

      <RouterLink to="/user/login-record"
                  class="flex items-center gap-3 px-4 py-3 border-b border-[var(--c-border-light)] hover:bg-[var(--c-surface-hover)] cursor-pointer no-underline text-inherit">
        <Smartphone class="w-5 h-5 text-[var(--c-text-3)]" />
        <span class="flex-1 text-[var(--c-text-1)]">{{ $t('profile.loginRecord') }}</span>
        <ChevronRight class="w-4 h-4 text-[var(--c-text-3)]" />
      </RouterLink>

      <RouterLink to="/user/realname"
                  class="flex items-center gap-3 px-4 py-3 border-b border-[var(--c-border-light)] hover:bg-[var(--c-surface-hover)] cursor-pointer no-underline text-inherit">
        <BadgeCheck class="w-5 h-5 text-[var(--c-text-3)]" />
        <span class="flex-1 text-[var(--c-text-1)]">{{ $t('profile.realname') }}</span>
        <ChevronRight class="w-4 h-4 text-[var(--c-text-3)]" />
      </RouterLink>

      <RouterLink to="/user/bind-phone"
                  class="flex items-center gap-3 px-4 py-3 border-b border-[var(--c-border-light)] hover:bg-[var(--c-surface-hover)] cursor-pointer no-underline text-inherit">
        <Smartphone class="w-5 h-5 text-[var(--c-text-3)]" />
        <span class="flex-1 text-[var(--c-text-1)]">{{ $t('profile.bindPhone') }}</span>
        <ChevronRight class="w-4 h-4 text-[var(--c-text-3)]" />
      </RouterLink>

      <RouterLink to="/user/bind-email"
                  class="flex items-center gap-3 px-4 py-3 border-b border-[var(--c-border-light)] hover:bg-[var(--c-surface-hover)] cursor-pointer no-underline text-inherit">
        <Mail class="w-5 h-5 text-[var(--c-text-3)]" />
        <span class="flex-1 text-[var(--c-text-1)]">{{ $t('profile.bindEmail') }}</span>
        <ChevronRight class="w-4 h-4 text-[var(--c-text-3)]" />
      </RouterLink>

      <RouterLink to="/user/delete"
                  class="flex items-center gap-3 px-4 py-3 hover:bg-[var(--c-surface-hover)] cursor-pointer no-underline text-inherit">
        <Trash2 class="w-5 h-5 text-[var(--c-text-3)]" />
        <span class="flex-1 text-[var(--c-text-1)]">{{ $t('profile.deleteAccount') }}</span>
        <ChevronRight class="w-4 h-4 text-[var(--c-text-3)]" />
      </RouterLink>
    </AppCard>

    <!-- Help & Feedback -->
    <AppCard>
      <RouterLink to="/user/feedback"
                  class="flex items-center gap-3 px-4 py-3 border-b border-[var(--c-border-light)] hover:bg-[var(--c-surface-hover)] cursor-pointer no-underline text-inherit">
        <MessageSquare class="w-5 h-5 text-[var(--c-text-3)]" />
        <span class="flex-1 text-[var(--c-text-1)]">{{ $t('profile.helpFeedback') }}</span>
        <ChevronRight class="w-4 h-4 text-[var(--c-text-3)]" />
      </RouterLink>

      <a href="https://www.wjx.top/m/47687434.aspx" target="_blank" rel="noopener noreferrer"
         class="flex items-center gap-3 px-4 py-3 border-b border-[var(--c-border-light)] hover:bg-[var(--c-surface-hover)] cursor-pointer no-underline text-inherit">
        <MessageCircle class="w-5 h-5 text-[var(--c-text-3)]" />
        <span class="flex-1 text-[var(--c-text-1)]">{{ $t('profile.reportChannel') }}</span>
        <ChevronRight class="w-4 h-4 text-[var(--c-text-3)]" />
      </a>

      <RouterLink to="/user/download"
                  class="flex items-center gap-3 px-4 py-3 hover:bg-[var(--c-surface-hover)] cursor-pointer no-underline text-inherit">
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

    <!-- Logout Dialog -->
    <Teleport to="body">
      <div v-if="showLogoutDialog" class="fixed inset-0 z-[1000] flex items-center justify-center">
        <div class="absolute inset-0 bg-black/60" @click="showLogoutDialog = false"></div>
        <div class="relative z-10 bg-[var(--c-surface)] rounded-xl w-80 overflow-hidden shadow-xl">
          <div class="px-6 pt-8 pb-4 text-center">
            <strong class="text-[17px] font-bold text-[var(--c-text-1)]">{{ $t('common.hint') }}</strong>
          </div>
          <div class="px-6 pb-8 text-[15px] text-[var(--c-text-2)] text-center">{{ $t('profile.logoutConfirm') }}</div>
          <div class="flex border-t border-[var(--c-border-light)]">
            <button type="button" class="flex-1 py-3.5 text-center bg-transparent border-0 cursor-pointer text-[var(--c-text-1)] font-inherit"
                    @click="showLogoutDialog = false">{{ $t('common.cancel') }}</button>
            <button type="button" class="flex-1 py-3.5 text-center bg-transparent border-0 border-l border-[var(--c-border-light)] cursor-pointer text-red-500 font-medium font-inherit"
                    @click="confirmLogout">{{ $t('profile.logoutConfirmBtn') }}</button>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- Password Not Available Dialog -->
    <Teleport to="body">
      <div v-if="showPwdDialog" class="fixed inset-0 z-[1000] flex items-center justify-center">
        <div class="absolute inset-0 bg-black/60" @click="showPwdDialog = false"></div>
        <div class="relative z-10 bg-[var(--c-surface)] rounded-xl w-80 overflow-hidden shadow-xl">
          <div class="px-6 pt-8 pb-4 text-center">
            <strong class="text-[17px] font-bold text-[var(--c-text-1)]">{{ $t('common.hint') }}</strong>
          </div>
          <div class="px-6 pb-8 text-[15px] text-[var(--c-text-2)] text-center">{{ $t('profile.passwordNotAvailable') }}</div>
          <div class="flex border-t border-[var(--c-border-light)]">
            <button type="button" class="flex-1 py-3.5 text-center bg-transparent border-0 cursor-pointer text-[var(--c-primary)] font-medium font-inherit"
                    @click="showPwdDialog = false">{{ $t('common.iKnow') }}</button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { logout } from '../api/user.js'
import { useToast } from '@/composables/useToast'
import AppCard from '@/components/ui/AppCard.vue'
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
  sessionStorage.clear()
  toastSuccess(t('common.logoutSuccess'))
  setTimeout(() => router.replace('/login'), 600)
}

function confirmLogout() {
  showLogoutDialog.value = false
  doLogout()
}
</script>
