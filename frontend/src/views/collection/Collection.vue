<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const actions = [
  {
    id: 'search',
    title: '馆藏检索',
    description: '按书名、作者和出版社检索馆藏图书',
    path: '/library/search',
    badge: '检索'
  },
  {
    id: 'borrow',
    title: '我的借阅',
    description: '输入图书馆密码后查看借阅记录并继续续借',
    path: '/library/borrow',
    badge: '借阅'
  }
]

function goBack() {
  router.back()
}

function openAction(path) {
  if (path) {
    router.push(path)
  }
}
</script>

<template>
  <div class="min-h-screen bg-[var(--c-bg)]">
    <div class="sticky top-0 z-30 flex items-center h-[52px] px-5 bg-[var(--c-surface)]/90 backdrop-blur-xl border-b border-[var(--c-border)]">
      <button @click="goBack" class="text-[var(--c-primary)] text-sm font-medium">&larr; 返回</button>
      <span class="flex-1 text-center text-sm font-bold">图书馆</span>
      <div class="w-10"></div>
    </div>

    <div class="max-w-lg mx-auto px-4 py-6">
      <!-- Hero card -->
      <div class="rounded-2xl bg-gradient-to-br from-[var(--c-primary)]/5 to-[var(--c-surface)] p-6 shadow-sm border border-[var(--c-border)]">
        <span class="inline-flex items-center h-7 px-3 rounded-full bg-[var(--c-primary)]/10 text-[var(--c-primary)] text-xs font-semibold">图书馆</span>
        <h1 class="mt-4 text-3xl font-bold text-[var(--c-text)]">图书馆</h1>
        <p class="mt-2 text-sm leading-relaxed text-[var(--c-text-2)]">馆藏检索和我的借阅统一放在一个模块里，首页不再拆成两个平级入口。</p>
      </div>

      <!-- Action grid -->
      <div class="grid grid-cols-1 sm:grid-cols-2 gap-3.5 mt-5">
        <button
          v-for="item in actions"
          :key="item.id"
          type="button"
          class="flex flex-col items-start min-h-[180px] p-5 rounded-2xl bg-[var(--c-surface)] border border-[var(--c-border)] shadow-sm text-left hover:bg-[var(--c-surface-hover)] transition-colors"
          @click="openAction(item.path)"
        >
          <span class="inline-flex items-center h-7 px-2.5 rounded-full bg-[var(--c-primary)]/10 text-[var(--c-primary)] text-xs font-bold">{{ item.badge }}</span>
          <span class="mt-4 text-xl font-bold text-[var(--c-text)]">{{ item.title }}</span>
          <span class="mt-2.5 text-sm leading-relaxed text-[var(--c-text-2)]">{{ item.description }}</span>
        </button>
      </div>
    </div>
  </div>
</template>
