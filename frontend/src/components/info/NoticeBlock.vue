<template>
  <div v-if="notices.length" class="modern-card">
    <div class="card-title">通知公告</div>
    <div class="notice-list">
      <div
        v-for="notice in notices"
        :key="notice.id || `${notice.title}-${notice.publishTime}`"
        class="notice-content"
      >
        <div class="notice-title">{{ notice.title }}</div>
        <div class="notice-date">时间：{{ notice.publishTime }}</div>
        <div class="notice-body">{{ notice.content }}</div>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  notices: {
    type: Array,
    default: () => []
  }
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
.card-title {
  font-size: 17px;
  font-weight: 600;
  color: #333333;
  margin-bottom: 12px;
  display: flex;
  align-items: center;
}
.card-title::before {
  content: "";
  display: block;
  width: 4px;
  height: 16px;
  background: #09bb07;
  border-radius: 2px;
  margin-right: 8px;
}
.notice-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
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
