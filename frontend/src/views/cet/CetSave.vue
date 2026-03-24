<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getCetNumber, saveCetNumber } from '@/api/cet'
import { useToast } from '@/composables/useToast'

const router = useRouter()
const { success: showSuccess, error: showError } = useToast()

const examNumber = ref('')
const name = ref('')
const saving = ref(false)

function submitSave() {
  const num = (examNumber.value || '').trim()
  const n = (name.value || '').trim()
  if (!num) {
    showError('请输入15位准考证号')
    return
  }
  if (num.length !== 15) {
    showError('准考证号必须为15位')
    return
  }
  saving.value = true
  saveCetNumber({ number: num, name: n }).then((res) => {
    if (res && res.success) {
      showSuccess('保存成功')
      setTimeout(() => router.back(), 800)
    } else {
      showError(res && res.message ? res.message : '保存失败')
    }
  }).catch(() => {
    // 错误由 request.js 全局拦截器统一提示，此处仅关闭 Loading
  }).finally(() => {
    saving.value = false
  })
}

onMounted(() => {
  getCetNumber().then((res) => {
    if (res && res.success && res.data) {
      const d = res.data
      if (d.number != null) examNumber.value = String(d.number)
      if (d.name != null && d.name !== '') name.value = d.name
    }
  }).catch(() => {})
})
</script>

<template>
  <div class="min-h-screen bg-[var(--c-bg)]">
    <div class="sticky top-0 z-30 flex items-center h-[52px] px-5 bg-[var(--c-surface)]/90 backdrop-blur-xl border-b border-[var(--c-border)]">
      <button @click="$router.back()" class="text-[var(--c-primary)] text-sm font-medium">&larr; 返回</button>
      <span class="flex-1 text-center text-sm font-bold">保存考号</span>
      <div class="w-10"></div>
    </div>

    <div class="max-w-lg mx-auto px-4 py-6">
      <div class="bg-[var(--c-surface)] rounded-2xl p-5 shadow-sm border border-[var(--c-border)]">
        <div class="space-y-4">
          <!-- 考号 -->
          <div>
            <label class="text-sm font-medium text-[var(--c-text-2)] mb-1.5 block">准考证号</label>
            <input
              v-model="examNumber"
              type="text"
              inputmode="numeric"
              maxlength="15"
              placeholder="请输入15位准考证号"
              class="w-full px-3 py-2.5 border border-[var(--c-border)] rounded-lg text-sm focus:border-[var(--c-primary)] focus:ring-2 focus:ring-[var(--c-primary)]/10 outline-none bg-[var(--c-surface)]"
            />
          </div>

          <!-- 姓名 -->
          <div>
            <label class="text-sm font-medium text-[var(--c-text-2)] mb-1.5 block">姓名</label>
            <input
              v-model="name"
              type="text"
              maxlength="20"
              placeholder="姓名超过3个字可只输入前3个"
              class="w-full px-3 py-2.5 border border-[var(--c-border)] rounded-lg text-sm focus:border-[var(--c-primary)] focus:ring-2 focus:ring-[var(--c-primary)]/10 outline-none bg-[var(--c-surface)]"
            />
          </div>
        </div>

        <button
          type="button"
          class="w-full bg-[var(--c-primary)] text-white rounded-lg py-2.5 font-semibold mt-6 transition-opacity hover:opacity-90"
          :disabled="saving"
          @click="submitSave"
        >
          {{ saving ? '保存中...' : '保存' }}
        </button>
      </div>
    </div>
  </div>
</template>
