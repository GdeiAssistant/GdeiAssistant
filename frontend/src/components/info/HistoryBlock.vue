<template>
  <div v-if="festival" class="modern-card">
    <div class="card-title">世界上的今日</div>
    <div class="history-content">
      <p class="history-lead">
        你知道吗？每年的<span class="history-date">{{ todayLabel }}</span>，是{{ festival.name }}
      </p>
      <p
        v-for="(line, idx) in festival.description"
        :key="idx"
        class="history-desc"
      >
        {{ line }}
      </p>
    </div>
  </div>
</template>

<script setup>
defineProps({
  festival: {
    type: Object,
    default: null
  },
  todayLabel: {
    type: String,
    default: ''
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
.history-lead {
  font-size: 15px;
  color: #333;
  margin: 0 0 12px;
  line-height: 1.5;
}
.history-date {
  font-weight: 600;
  color: #09bb07;
}
.history-desc {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
  margin: 0 0 8px;
}
.history-desc:last-child {
  margin-bottom: 0;
}
</style>
