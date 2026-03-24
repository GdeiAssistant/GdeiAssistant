<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<template>
  <div class="min-h-screen bg-gray-50">
    <!-- Sticky Header -->
    <div class="sticky top-0 z-10 flex items-center h-12 bg-white border-b border-gray-200 px-4">
      <button type="button" class="w-15 text-sm text-gray-700 text-left cursor-pointer" @click="router.back()">返回</button>
      <h1 class="flex-1 text-center text-base font-medium text-gray-700 m-0">实名认证</h1>
      <div class="w-15"></div>
    </div>

    <!-- Content -->
    <div class="max-w-lg mx-auto px-4 py-16 text-center">
      <div class="bg-white rounded-xl shadow-sm p-8">
        <div class="text-5xl text-blue-400 mb-4">i</div>
        <h2 class="text-lg font-medium text-gray-800 mb-2">实名认证暂未开放</h2>
        <p class="text-sm text-gray-500 mb-8 leading-relaxed">
          当前实名认证能力仍在接入公安数据接口，暂未对外开放。待后端和外部校验链路就绪后再开放。
        </p>
        <button
          type="button"
          class="w-full max-w-xs mx-auto block rounded-lg bg-green-500 text-white font-medium py-2.5 px-6 active:bg-green-600 cursor-pointer"
          @click="router.back()"
        >
          返回上一页
        </button>
      </div>
    </div>
  </div>
</template>
