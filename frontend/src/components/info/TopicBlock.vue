<template>
  <div v-if="topics && topics.length" class="modern-card">
    <div class="card-title">专题阅读</div>
    <div class="topic-cells">
      <a
        v-for="(topic, idx) in topics"
        :key="idx"
        :href="topic.link || 'javascript:;'"
        class="topic-cell"
      >
        <div class="topic-cell__bd">
          <p class="topic-cell__title">{{ topic.title }}</p>
          <p class="topic-cell__desc">{{ topic.description }}</p>
        </div>
        <div class="topic-cell__ft"></div>
      </a>
    </div>
    <div
      class="topic-view-all"
      @click="$emit('view-all-topics')"
    >
      点击查看所有专题阅读内容
    </div>
  </div>
</template>

<script setup>
defineProps({
  topics: {
    type: Array,
    default: () => []
  }
})

defineEmits(['view-all-topics'])
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
.topic-cells {
  display: flex;
  flex-direction: column;
}
.topic-cells::before,
.topic-cells::after {
  display: none;
}
.topic-cell {
  display: flex;
  align-items: center;
  padding: 12px 0;
  text-decoration: none;
  color: inherit;
  border-bottom: 1px solid #f0f0f0;
}
.topic-cell:last-child {
  border-bottom: none;
}
.topic-cell__bd {
  flex: 1;
  min-width: 0;
}
.topic-cell__title {
  font-size: 15px;
  color: #333;
  margin: 0 0 4px;
}
.topic-cell__desc {
  font-size: 13px;
  color: #999;
  margin: 0;
}
.topic-cell__ft {
  width: 8px;
  height: 8px;
  border-right: 1px solid #c8c8c8;
  border-top: 1px solid #c8c8c8;
  transform: rotate(45deg);
  flex-shrink: 0;
  margin-left: 8px;
}
.topic-view-all {
  text-align: center;
  font-size: 13px;
  color: #576b95;
  margin-top: 12px;
  cursor: pointer;
}
</style>
