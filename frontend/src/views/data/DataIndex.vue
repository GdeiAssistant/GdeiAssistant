<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<template>
  <div class="min-h-screen bg-[var(--c-bg)]">
    <div class="sticky top-0 z-30 flex items-center h-[52px] px-5 bg-[var(--c-surface)]/90 backdrop-blur-xl border-b border-[var(--c-border)]">
      <button @click="router.back()" class="text-[var(--c-primary)] text-sm font-medium">&larr; 返回</button>
      <span class="flex-1 text-center text-sm font-bold">数据查询</span>
      <div class="w-10"></div>
    </div>

    <div class="max-w-lg mx-auto px-4 py-6">
      <div class="bg-[var(--c-surface)] rounded-xl border border-[var(--c-border)] divide-y divide-[var(--c-border)]">
        <a
          href="javascript:"
          class="flex items-center justify-between px-4 py-3.5 text-sm hover:bg-[var(--c-surface-hover)] transition-colors no-underline text-[var(--c-text)]"
          @click.prevent="router.push('/data/electricity')"
        >
          <span>电费查询</span>
          <span class="text-[var(--c-text-3)]">&rsaquo;</span>
        </a>
        <a
          href="javascript:"
          class="flex items-center justify-between px-4 py-3.5 text-sm hover:bg-[var(--c-surface-hover)] transition-colors no-underline text-[var(--c-text)]"
          @click.prevent="router.push('/data/yellowpage')"
        >
          <span>黄页查询</span>
          <span class="text-[var(--c-text-3)]">&rsaquo;</span>
        </a>
      </div>
    </div>
  </div>
</template>
