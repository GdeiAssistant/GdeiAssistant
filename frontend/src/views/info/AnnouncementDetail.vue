<template>
  <div>
    <div v-if="loading" class="flex justify-center py-12">
      <div class="w-6 h-6 border-2 border-[var(--c-primary)] border-t-transparent rounded-full animate-spin"></div>
    </div>

    <div v-else-if="item" class="bg-[var(--c-surface)] border border-[var(--c-border)] rounded-[14px] px-5 py-5">
      <h1 class="text-[18px] font-bold text-[var(--c-text-1)] leading-tight">{{ item.title }}</h1>
      <p class="mt-2 text-[13px] text-[var(--c-text-3)]">{{ item.publishTime }}</p>
      <div class="mt-4 pt-4 border-t border-[var(--c-border-light)] text-[15px] text-[var(--c-text-1)] leading-relaxed whitespace-pre-line">
        {{ item.content }}
      </div>
    </div>

    <div v-else class="py-16 text-center text-sm text-[var(--c-text-3)]">公告不存在或已删除</div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import request from '@/utils/request'

const route = useRoute()
const item = ref(null)
const loading = ref(true)

onMounted(async () => {
  try {
    const res = await request.get(`/information/announcement/id/${route.params.id}`)
    if (res?.success && res.data) {
      item.value = res.data
    }
  } finally {
    loading.value = false
  }
})
</script>
