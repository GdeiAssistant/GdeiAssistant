<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import request from '../../utils/request'
import { useToast } from '@/composables/useToast'
import { AlertTriangle } from 'lucide-vue-next'

const router = useRouter()
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
    toastSuccess('账号已注销')

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
      <button type="button" class="w-15 text-sm text-gray-700 text-left cursor-pointer" @click="router.back()">返回</button>
      <h1 class="flex-1 text-center text-base font-medium text-gray-700 m-0">注销账户</h1>
      <div class="w-15"></div>
    </div>

    <div class="max-w-lg mx-auto px-4 py-6">
      <!-- Warning header -->
      <div class="bg-white rounded-xl shadow-sm p-8 text-center mb-3">
        <div class="text-6xl text-red-500 mb-5"><AlertTriangle class="w-16 h-16 mx-auto" /></div>
        <h2 class="text-lg font-semibold text-gray-800 leading-snug">将注销您的广东第二师范学院助手账号</h2>
      </div>

      <!-- Risk list -->
      <div class="bg-white rounded-xl shadow-sm p-5 mb-3">
        <p class="text-[15px] font-medium text-gray-800 mb-4">注销后，以下信息将被永久删除且无法恢复：</p>
        <ul class="space-y-2 mb-4">
          <li class="text-sm text-gray-500 leading-relaxed pl-5 relative before:content-['•'] before:absolute before:left-0 before:text-gray-500 before:font-bold">账号及所有个人资料将被清空</li>
          <li class="text-sm text-gray-500 leading-relaxed pl-5 relative before:content-['•'] before:absolute before:left-0 before:text-gray-500 before:font-bold">表白墙、话题、跑腿等所有发布内容将被永久删除</li>
          <li class="text-sm text-gray-500 leading-relaxed pl-5 relative before:content-['•'] before:absolute before:left-0 before:text-gray-500 before:font-bold">所有互动记录（评论、点赞等）将被清除</li>
          <li class="text-sm text-gray-500 leading-relaxed pl-5 relative before:content-['•'] before:absolute before:left-0 before:text-gray-500 before:font-bold">自定义课程以及保存的四六级准考证号信息将被删除</li>
          <li class="text-sm text-gray-500 leading-relaxed pl-5 relative before:content-['•'] before:absolute before:left-0 before:text-gray-500 before:font-bold">社交功能平台的用户数据和交易记录将被清空</li>
          <li class="text-sm text-gray-500 leading-relaxed pl-5 relative before:content-['•'] before:absolute before:left-0 before:text-gray-500 before:font-bold">绑定的手机号、邮箱地址等身份信息将被删除</li>
        </ul>
        <p class="text-[13px] text-red-500 font-medium pt-4 border-t border-gray-100">注销操作不可逆，请谨慎操作。</p>
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
          <span class="text-sm text-gray-700 leading-relaxed">我已充分阅读并同意上述注销后果</span>
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
          注销中...
        </template>
        <template v-else>确认注销</template>
      </button>
    </div>

    <!-- Confirm dialog -->
    <Teleport to="body">
      <template v-if="showConfirmDialog">
        <div class="fixed inset-0 bg-black/60 z-[1000]" @click="handleCancel"></div>
        <div class="fixed top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[85%] max-w-[300px] bg-white rounded-xl z-[1001] overflow-hidden">
          <div class="px-5 pt-5 pb-2.5 text-center">
            <strong class="text-[17px] font-medium text-gray-700">最后确认</strong>
          </div>
          <div class="px-5 pb-5 text-center text-[15px] text-gray-500 leading-relaxed">
            注销操作不可逆，确定要永久删除该账号吗？
          </div>
          <div class="flex border-t border-gray-200">
            <button
              type="button"
              class="flex-1 py-3.5 text-center text-[17px] text-gray-700 border-r border-gray-200 cursor-pointer bg-transparent"
              @click="handleCancel"
            >取消</button>
            <button
              type="button"
              class="flex-1 py-3.5 text-center text-[17px] text-red-500 font-medium cursor-pointer bg-transparent"
              @click="handleConfirmDelete"
            >残忍注销</button>
          </div>
        </div>
      </template>
    </Teleport>
  </div>
</template>
