<template>
  <div v-if="props.notices.length" class="modern-card">
    <div class="card-header">
      <div class="card-title">{{ $t('info.noticeTitle') }}</div>
      <button
        v-if="props.notices.length > defaultVisibleCount"
        type="button"
        class="more-btn"
        @click="expanded = !expanded"
      >
        {{ expanded ? $t('info.collapse') : $t('info.expand') }}
      </button>
    </div>
    <div class="notice-list">
      <div
        v-for="notice in visibleNotices"
        :key="notice.id || `${notice.title}-${notice.publishTime}`"
        class="notice-content"
      >
        <div class="notice-title">{{ notice.title }}</div>
        <div class="notice-date">{{ $t('info.noticeTime') }}{{ notice.publishTime }}</div>
        <div class="notice-body">{{ notice.content }}</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'

const props = defineProps({
  notices: {
    type: Array,
    default: () => []
  }
})

const defaultVisibleCount = 3
const expanded = ref(false)

const visibleNotices = computed(() => {
  if (expanded.value) {
    return props.notices
  }
  return props.notices.slice(0, defaultVisibleCount)
})
</script>

<style scoped>
.modern-card {
  background: #ffffff;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.03);
}
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}
.card-title {
  font-size: 17px;
  font-weight: 600;
  color: #333333;
  margin-bottom: 12px;
  display: flex;
  align-items: center;
}
.card-header .card-title {
  margin-bottom: 0;
}
.card-title::before {
  content: "";
  display: block;
  width: 4px;
  height: 16px;
  background: var(--color-primary);
  border-radius: 2px;
  margin-right: 8px;
}
.more-btn {
  border: none;
  background: transparent;
  color: #576b95;
  font-size: 13px;
  padding: 0;
}
.notice-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
  margin-top: 12px;
}
.notice-content + .notice-content {
  border-top: 1px solid #f2f2f2;
  padding-top: 14px;
}
.notice-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 6px;
}
.notice-date {
  font-size: 13px;
  color: #999;
  margin-bottom: 10px;
}
.notice-body {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
}
</style>
