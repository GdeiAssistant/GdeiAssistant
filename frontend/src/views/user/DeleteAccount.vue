<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import request from '../../utils/request'
import { useToast } from '@/composables/useToast'
import { AlertTriangle } from 'lucide-vue-next'

const router = useRouter()
const { t } = useI18n()
const { success: toastSuccess } = useToast()
const agreed = ref(false)
const showConfirmDialog = ref(false)
const deleting = ref(false)

function handleDeleteClick() {
  if (!agreed.value) return
  showConfirmDialog.value = true
}

function handleCancel() {
  showConfirmDialog.value = false
}

async function handleConfirmDelete() {
  showConfirmDialog.value = false
  deleting.value = true

  try {
    await request.post('/close/submit')
    toastSuccess(t('deleteAccount.success'))

    // 清除登录态
    localStorage.clear()
    sessionStorage.clear()

    // 延迟跳转，让Toast有时间显示
    setTimeout(() => {
      router.replace('/login')
    }, 1500)
  } catch (e) {
    deleting.value = false
    // 错误由 request.js 全局拦截器统一提示
  }
}
</script>

<template>
  <div class="min-h-screen bg-gray-50 pb-6">
    <!-- Sticky Header -->
    <div class="sticky top-0 z-10 flex items-center h-12 bg-white border-b border-gray-200 px-4">
      <button type="button" class="w-15 text-sm text-gray-700 text-left cursor-pointer" @click="router.back()">{{ t('common.back') }}</button>
      <h1 class="flex-1 text-center text-base font-medium text-gray-700 m-0">{{ t('profile.deleteAccount') }}</h1>
      <div class="w-15"></div>
    </div>

    <div class="max-w-lg mx-auto px-4 py-6">
      <!-- Warning header -->
      <div class="bg-white rounded-xl shadow-sm p-8 text-center mb-3">
        <div class="text-6xl text-red-500 mb-5"><AlertTriangle class="w-16 h-16 mx-auto" /></div>
        <h2 class="text-lg font-semibold text-gray-800 leading-snug">{{ t('deleteAccount.warningTitle') }}</h2>
      </div>

      <!-- Risk list -->
      <div class="bg-white rounded-xl shadow-sm p-5 mb-3">
        <p class="text-[15px] font-medium text-gray-800 mb-4">{{ t('deleteAccount.riskTitle') }}</p>
        <ul class="space-y-2 mb-4">
          <li class="text-sm text-gray-500 leading-relaxed pl-5 relative before:content-['•'] before:absolute before:left-0 before:text-gray-500 before:font-bold">{{ t('deleteAccount.risk.account') }}</li>
          <li class="text-sm text-gray-500 leading-relaxed pl-5 relative before:content-['•'] before:absolute before:left-0 before:text-gray-500 before:font-bold">{{ t('deleteAccount.risk.posts') }}</li>
          <li class="text-sm text-gray-500 leading-relaxed pl-5 relative before:content-['•'] before:absolute before:left-0 before:text-gray-500 before:font-bold">{{ t('deleteAccount.risk.interactions') }}</li>
          <li class="text-sm text-gray-500 leading-relaxed pl-5 relative before:content-['•'] before:absolute before:left-0 before:text-gray-500 before:font-bold">{{ t('deleteAccount.risk.academic') }}</li>
          <li class="text-sm text-gray-500 leading-relaxed pl-5 relative before:content-['•'] before:absolute before:left-0 before:text-gray-500 before:font-bold">{{ t('deleteAccount.risk.community') }}</li>
          <li class="text-sm text-gray-500 leading-relaxed pl-5 relative before:content-['•'] before:absolute before:left-0 before:text-gray-500 before:font-bold">{{ t('deleteAccount.risk.identity') }}</li>
        </ul>
        <p class="text-[13px] text-red-500 font-medium pt-4 border-t border-gray-100">{{ t('deleteAccount.irreversible') }}</p>
      </div>

      <!-- Agreement checkbox -->
      <div class="bg-white rounded-xl shadow-sm p-4 mb-5">
        <label class="flex items-start gap-3 cursor-pointer">
          <div class="relative mt-0.5">
            <input
              type="checkbox"
              class="sr-only peer"
              v-model="agreed"
            />
            <div class="w-5 h-5 border border-gray-300 rounded bg-white peer-checked:bg-green-500 peer-checked:border-green-500 flex items-center justify-center after:content-[''] after:hidden peer-checked:after:block after:w-[5px] after:h-[10px] after:border-white after:border-r-2 after:border-b-2 after:rotate-45 after:-mt-0.5"></div>
          </div>
          <span class="text-sm text-gray-700 leading-relaxed">{{ t('deleteAccount.agreement') }}</span>
        </label>
      </div>

      <!-- Delete button -->
      <button
        type="button"
        class="w-full rounded-lg text-white text-[17px] font-medium py-3 flex items-center justify-center cursor-pointer disabled:opacity-60 disabled:cursor-not-allowed"
        :class="agreed && !deleting ? 'bg-red-500 active:bg-red-600' : 'bg-gray-300'"
        :disabled="!agreed || deleting"
        @click="handleDeleteClick"
      >
        <template v-if="deleting">
          <span class="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin mr-2"></span>
          {{ t('deleteAccount.deleting') }}
        </template>
        <template v-else>{{ t('deleteAccount.confirm') }}</template>
      </button>
    </div>

    <!-- Confirm dialog -->
    <Teleport to="body">
      <template v-if="showConfirmDialog">
        <div class="fixed inset-0 bg-black/60 z-[1000]" @click="handleCancel"></div>
        <div class="fixed top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[85%] max-w-[300px] bg-white rounded-xl z-[1001] overflow-hidden">
          <div class="px-5 pt-5 pb-2.5 text-center">
            <strong class="text-[17px] font-medium text-gray-700">{{ t('deleteAccount.finalConfirmTitle') }}</strong>
          </div>
          <div class="px-5 pb-5 text-center text-[15px] text-gray-500 leading-relaxed">
            {{ t('deleteAccount.finalConfirmDescription') }}
          </div>
          <div class="flex border-t border-gray-200">
            <button
              type="button"
              class="flex-1 py-3.5 text-center text-[17px] text-gray-700 border-r border-gray-200 cursor-pointer bg-transparent"
              @click="handleCancel"
            >{{ t('common.cancel') }}</button>
            <button
              type="button"
              class="flex-1 py-3.5 text-center text-[17px] text-red-500 font-medium cursor-pointer bg-transparent"
              @click="handleConfirmDelete"
            >{{ t('deleteAccount.finalConfirmAction') }}</button>
          </div>
        </div>
      </template>
    </Teleport>
  </div>
</template>
