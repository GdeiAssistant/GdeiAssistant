<script setup>
import { BellOff } from 'lucide-vue-next'

const props = defineProps({
  notices: {
    type: Array,
    default: () => []
  }
})
</script>

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
  <div v-else class="notice-empty">
    <div class="notice-empty__icon">
      <BellOff class="size-5" />
    </div>
    <div class="notice-empty__text">{{ $t('info.noNotice') }}</div>
  </div>
</template>

<style scoped>
.notice-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.notice-content {
  position: relative;
  padding: 14px 16px 14px 18px;
  border: 1px solid var(--c-border);
  border-radius: var(--radius-card);
  background: var(--c-surface);
  box-shadow: none;
}

.notice-content::before {
  position: absolute;
  top: 16px;
  bottom: 16px;
  left: 0;
  width: 3px;
  border-radius: 0 999px 999px 0;
  content: '';
  background: var(--c-primary-soft);
}

.notice-title {
  margin-bottom: 6px;
  font-size: 16px;
  font-weight: 780;
  color: var(--c-text-1);
}

.notice-date {
  margin-bottom: 9px;
  font-size: 13px;
  color: var(--c-text-3);
}

.notice-body {
  font-size: 14px;
  color: var(--c-text-2);
  line-height: 1.7;
}

.notice-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  min-height: 132px;
  padding: 24px 18px;
  text-align: center;
  border: 1px solid var(--c-border);
  border-radius: var(--radius-card);
  background: var(--c-surface);
  box-shadow: none;
}

.notice-empty__icon {
  display: grid;
  width: 52px;
  height: 52px;
  place-items: center;
  border: 1px solid var(--c-border);
  border-radius: var(--radius-card);
  background: var(--c-primary-soft);
  color: var(--c-primary);
  box-shadow: none;
}

.notice-empty__text {
  font-size: 14px;
  color: var(--color-text-tertiary);
  font-weight: 600;
}
</style>
