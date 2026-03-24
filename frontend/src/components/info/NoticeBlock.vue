<template>
  <div v-if="props.notices.length" class="notice-list">
    <div
      v-for="notice in props.notices"
      :key="notice.id || `${notice.title}-${notice.publishTime}`"
      class="notice-content"
    >
      <div class="notice-title">{{ notice.title }}</div>
      <div class="notice-date">{{ $t('info.noticeTime') }}{{ notice.publishTime }}</div>
      <div class="notice-body">{{ notice.content }}</div>
    </div>
  </div>
</template>

<script setup>
const props = defineProps({
  notices: {
    type: Array,
    default: () => []
  }
})
</script>

<style scoped>
.notice-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
  margin-top: 12px;
}
.notice-content + .notice-content {
  border-top: 1px solid var(--color-divider);
  padding-top: 14px;
}
.notice-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin-bottom: 6px;
}
.notice-date {
  font-size: 13px;
  color: var(--color-text-tertiary);
  margin-bottom: 10px;
}
.notice-body {
  font-size: 14px;
  color: var(--color-text-secondary);
  line-height: 1.6;
}
</style>
