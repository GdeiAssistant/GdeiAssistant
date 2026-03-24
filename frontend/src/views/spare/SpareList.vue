<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { querySpareRoom } from '@/api/spare'

const router = useRouter()
const route = useRoute()
const spareList = ref([])
const isLoading = ref(true)

function goBack() {
  router.back()
}

onMounted(() => {
  console.log('🚀 发起空课室请求，参数:', route.query)
  isLoading.value = true
  querySpareRoom(route.query)
    .then((res) => {
      console.log('📦 收到原始响应数据:', res)
      const targetData = res?.data?.data ?? res?.data ?? []
      if (Array.isArray(targetData)) {
        spareList.value = targetData
      } else {
        console.error('❌ 解析失败，拿到的不是数组:', targetData)
        spareList.value = []
      }
    })
    .catch((err) => {
      console.error('💥 请求报错:', err)
      spareList.value = []
    })
    .finally(() => {
      isLoading.value = false
    })
})
</script>

<template>
  <div class="min-h-screen bg-[var(--c-bg)]">
    <!-- Loading overlay -->
    <template v-if="isLoading">
      <div class="fixed inset-0 z-50 flex items-center justify-center bg-black/20">
        <div class="bg-[var(--c-surface)] rounded-xl px-6 py-5 flex flex-col items-center gap-3 shadow-lg">
          <svg class="animate-spin h-8 w-8 text-[var(--c-primary)]" viewBox="0 0 24 24"><circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" fill="none"/><path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/></svg>
          <p class="text-sm text-[var(--c-text-2)]">加载中</p>
        </div>
      </div>
    </template>

    <!-- Header -->
    <div class="sticky top-0 z-30 flex items-center h-[52px] px-5 bg-[var(--c-surface)]/90 backdrop-blur-xl border-b border-[var(--c-border)]">
      <button @click="goBack" class="text-[var(--c-primary)] text-sm font-medium">← 返回</button>
      <span class="flex-1 text-center text-sm font-bold">空课室查询</span>
      <div class="w-10"></div>
    </div>

    <div class="max-w-2xl mx-auto px-4 py-4">
      <p class="text-xs text-[var(--c-text-quaternary)] mb-3 px-1">查询结果</p>

      <!-- Results list -->
      <div v-if="spareList.length > 0" class="rounded-xl bg-[var(--c-surface)] border border-[var(--c-border)] overflow-hidden">
        <div
          v-for="(item, index) in spareList"
          :key="index"
          class="px-4 py-3.5"
          :class="index < spareList.length - 1 ? 'border-b border-[var(--c-border)]' : ''"
        >
          <h4 class="text-base font-medium text-[var(--c-text-primary)] mb-1">{{ item.name }}</h4>
          <div class="flex justify-between text-sm text-[var(--c-text-2)]">
            <span>类型: {{ item.type }}</span>
            <span>座位数: {{ item.seats }}</span>
          </div>
        </div>
      </div>

      <!-- Empty state -->
      <div v-if="!isLoading && spareList.length === 0" class="flex items-center justify-center min-h-[200px] text-sm text-[var(--c-text-quaternary)]">
        暂无空课室数据
      </div>
    </div>
  </div>
</template>
