<template>
  <div class="modern-card">
    <div class="card-title">
      统一互动消息
      <span v-if="unreadCount > 0" class="card-badge">{{ unreadCount > 99 ? '99+' : unreadCount }}</span>
    </div>
    <div v-if="!items.length" class="interaction-empty">暂无互动消息</div>
    <div v-else class="interaction-list">
      <button
        v-for="item in items"
        :key="item.id || `${item.module}-${item.targetId || ''}-${item.targetSubId || ''}`"
        type="button"
        class="interaction-item"
        @click="$emit('select-item', item)"
      >
        <div class="interaction-item__header">
          <div class="interaction-item__title">{{ item.title || '互动消息' }}</div>
          <div class="interaction-item__time">{{ item.createdAt || '最近更新' }}</div>
        </div>
        <div class="interaction-item__content">{{ item.content || '你有一条新的互动消息' }}</div>
        <div class="interaction-item__footer">
          <span class="interaction-item__module">{{ getModuleLabel(item.module) }}</span>
          <span :class="['interaction-item__state', item.isRead ? 'is-read' : 'is-unread']">
            {{ item.isRead ? '已读' : '未读' }}
          </span>
        </div>
      </button>
    </div>
  </div>
</template>

<script setup>
defineProps({
  items: {
    type: Array,
    default: () => []
  },
  unreadCount: {
    type: Number,
    default: 0
  }
})

defineEmits(['select-item'])

function getModuleLabel(module) {
  if (module === 'marketplace') {
    return '二手交易'
  }
  if (module === 'lostandfound') {
    return '失物招领'
  }
  if (module === 'dating') {
    return '卖室友'
  }
  if (module === 'delivery') {
    return '全民快递'
  }
  if (module === 'secret') {
    return '树洞'
  }
  if (module === 'express') {
    return '表白墙'
  }
  if (module === 'topic') {
    return '话题'
  }
  if (module === 'photograph') {
    return '拍好校园'
  }
  return '互动'
}
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

.card-badge {
  margin-left: 8px;
  min-width: 20px;
  height: 20px;
  padding: 0 6px;
  border-radius: 999px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: #fa5151;
  color: #fff;
  font-size: 11px;
  font-weight: 700;
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

.interaction-empty {
  padding: 12px 0 4px;
  font-size: 14px;
  color: #999999;
  text-align: center;
}

.interaction-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.interaction-item {
  width: 100%;
  border: 1px solid #f0f0f0;
  border-radius: 10px;
  background: #fafafa;
  padding: 12px;
  text-align: left;
  cursor: pointer;
}

.interaction-item:active {
  opacity: 0.85;
}

.interaction-item__header {
  display: flex;
  align-items: flex-start;
  gap: 8px;
}

.interaction-item__title {
  flex: 1;
  min-width: 0;
  font-size: 15px;
  font-weight: 600;
  color: #333333;
}

.interaction-item__time {
  flex-shrink: 0;
  font-size: 12px;
  color: #999999;
}

.interaction-item__content {
  margin-top: 8px;
  font-size: 13px;
  line-height: 1.6;
  color: #666666;
}

.interaction-item__footer {
  margin-top: 10px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.interaction-item__module {
  font-size: 12px;
  color: #576b95;
}

.interaction-item__state {
  font-size: 12px;
  font-weight: 600;
}

.interaction-item__state.is-read {
  color: #999999;
}

.interaction-item__state.is-unread {
  color: #09bb07;
}
</style>
