<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request'
import { useToast } from '@/composables/useToast'

const router = useRouter()
const { success, loading: showLoading, hideLoading } = useToast()
const isDirectSubmit = ref(false)
const isLoading = ref(false)
const showEvaluateConfirmDialog = ref(false)

function doEvaluate() {
  showEvaluateConfirmDialog.value = true
}

function closeEvaluateConfirmDialog() {
  showEvaluateConfirmDialog.value = false
}

function confirmEvaluate() {
  closeEvaluateConfirmDialog()
  isLoading.value = true
  showLoading('自动评教中...')
  const formData = { directSubmit: isDirectSubmit.value }
  request.post('/evaluate/submit', formData)
    .then(() => {
      isLoading.value = false
      hideLoading()
      success('评价提交成功！')
    })
    .catch(() => {
      isLoading.value = false
      hideLoading()
    })
}
</script>

<template>
  <div class="min-h-screen bg-[var(--c-bg)]">
    <!-- Sticky header -->
    <div class="sticky top-0 z-30 flex items-center h-[52px] px-5 bg-[var(--c-surface)]/90 backdrop-blur-xl border-b border-[var(--c-border)]">
      <button @click="$router.back()" class="text-[var(--c-primary)] text-sm font-medium">&larr; 返回</button>
      <span class="flex-1 text-center text-sm font-bold">教学质量评价</span>
      <div class="w-10"></div>
    </div>

    <!-- Content -->
    <div class="max-w-lg mx-auto px-4 py-6">
      <div class="bg-[var(--c-surface)] rounded-2xl border border-[var(--c-border)] overflow-hidden">
        <!-- Toggle row -->
        <div class="flex items-center justify-between px-4 py-4">
          <span class="text-[15px] text-[var(--c-text)]">直接提交评教信息</span>
          <label class="relative inline-flex items-center cursor-pointer">
            <input type="checkbox" v-model="isDirectSubmit" class="sr-only peer" />
            <div class="w-11 h-6 bg-gray-300 rounded-full peer-checked:bg-[var(--c-primary)] transition-colors after:content-[''] after:absolute after:top-0.5 after:left-[2px] after:bg-white after:rounded-full after:h-5 after:w-5 after:shadow after:transition-transform peer-checked:after:translate-x-5"></div>
          </label>
        </div>
      </div>

      <!-- Submit button -->
      <button
        type="button"
        :disabled="isLoading"
        class="mt-6 w-full py-3 rounded-xl bg-[var(--c-primary)] text-white text-[15px] font-medium active:opacity-80 disabled:opacity-50 transition-opacity"
        @click="doEvaluate"
      >
        一键评教
      </button>

      <p class="mt-4 text-center text-xs text-[var(--c-text-secondary)]">
        注意：评教信息提交后，将不能再作修改。
      </p>
    </div>

    <!-- Confirm dialog -->
    <Teleport to="body">
      <template v-if="showEvaluateConfirmDialog">
        <div class="fixed inset-0 z-50 bg-black/50" @click="closeEvaluateConfirmDialog"></div>
        <div class="fixed z-50 top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[300px] bg-[var(--c-surface)] rounded-2xl overflow-hidden shadow-xl">
          <div class="pt-6 pb-3 px-5 text-center">
            <h3 class="text-base font-semibold text-[var(--c-text)]">提示</h3>
          </div>
          <div class="px-5 pb-5 text-center text-sm text-[var(--c-text-secondary)] leading-relaxed">
            确定要进行一键评教吗？此操作不可逆。
          </div>
          <div class="flex border-t border-[var(--c-border)]">
            <button
              class="flex-1 py-3.5 text-center text-[15px] text-[var(--c-text-secondary)] border-r border-[var(--c-border)] active:bg-black/5"
              @click="closeEvaluateConfirmDialog"
            >取消</button>
            <button
              class="flex-1 py-3.5 text-center text-[15px] text-[var(--c-primary)] font-medium active:bg-black/5"
              @click="confirmEvaluate"
            >确定</button>
          </div>
        </div>
      </template>
    </Teleport>
  </div>
</template>
