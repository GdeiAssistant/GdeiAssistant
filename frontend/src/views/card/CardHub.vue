<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const actions = [
  {
    id: 'info',
    title: '基本信息',
    description: '查看校园卡余额、状态及当前挂失情况，支持在线挂失',
    path: '/card/info',
    badge: '信息'
  },
  {
    id: 'records',
    title: '消费记录',
    description: '按日期查询校园卡当日消费流水',
    path: '/card/records',
    badge: '消费'
  }
]
</script>

<template>
  <div class="min-h-screen bg-[var(--c-bg)]">
    <div class="sticky top-0 z-30 flex items-center h-[52px] px-5 bg-[var(--c-surface)]/90 backdrop-blur-xl border-b border-[var(--c-border)]">
      <button @click="$router.back()" class="text-[var(--c-primary)] text-sm font-medium">&larr; 返回</button>
      <span class="flex-1 text-center text-sm font-bold">校园卡</span>
      <div class="w-10"></div>
    </div>

    <div class="max-w-lg mx-auto px-4 py-6">
      <!-- Hero card -->
      <div class="rounded-2xl bg-gradient-to-br from-[var(--c-primary)]/5 to-[var(--c-surface)] p-6 shadow-sm border border-[var(--c-border)]">
        <span class="inline-flex items-center h-7 px-3 rounded-full bg-[var(--c-primary)]/10 text-[var(--c-primary)] text-xs font-semibold">校园卡</span>
        <h1 class="mt-4 text-3xl font-bold text-[var(--c-text)]">校园卡</h1>
        <p class="mt-2 text-sm leading-relaxed text-[var(--c-text-2)]">查询余额、消费记录，办理挂失业务。</p>
      </div>

      <!-- Action grid -->
      <div class="grid grid-cols-1 sm:grid-cols-2 gap-3.5 mt-5">
        <button
          v-for="item in actions"
          :key="item.id"
          type="button"
          class="flex flex-col items-start min-h-[160px] p-5 rounded-2xl bg-[var(--c-surface)] border border-[var(--c-border)] shadow-sm text-left hover:bg-[var(--c-surface-hover)] transition-colors"
          @click="router.push(item.path)"
        >
          <span class="inline-flex items-center h-7 px-2.5 rounded-full bg-[var(--c-primary)]/10 text-[var(--c-primary)] text-xs font-bold">{{ item.badge }}</span>
          <span class="mt-4 text-xl font-bold text-[var(--c-text)]">{{ item.title }}</span>
          <span class="mt-2.5 text-sm leading-relaxed text-[var(--c-text-2)]">{{ item.description }}</span>
        </button>
      </div>
    </div>
  </div>
</template>
