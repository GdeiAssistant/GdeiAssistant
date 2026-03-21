<template>
  <div class="modern-card">
    <div class="card-header">
      <div class="card-title">
        {{ $t('info.interactionTitle') }}
        <span v-if="unreadCount > 0" class="card-badge">{{ unreadCount > 99 ? '99+' : unreadCount }}</span>
      </div>
      <button
        v-if="unreadCount > 0"
        type="button"
        class="card-action"
        @click="$emit('mark-all')"
      >
        {{ $t('info.markAllRead') }}
      </button>
    </div>
    <div v-if="!items.length" class="interaction-empty">{{ $t('info.noInteraction') }}</div>
    <div v-else class="interaction-list">
      <button
        v-for="item in items"
        :key="item.id || `${item.module}-${item.targetId || ''}-${item.targetSubId || ''}`"
        type="button"
        class="interaction-item"
        @click="$emit('select-item', item)"
      >
        <div class="interaction-item__header">
          <div class="interaction-item__title">{{ item.title || $t('info.defaultInteractionTitle') }}</div>
          <div class="interaction-item__time">{{ item.createdAt || $t('common.recentUpdate') }}</div>
        </div>
        <div class="interaction-item__content">{{ item.content || $t('info.defaultInteractionContent') }}</div>
        <div class="interaction-item__footer">
          <div class="interaction-item__badges">
            <span class="interaction-item__module">{{ getModuleLabel(item.module) }}</span>
            <span v-if="getActionLabel(item)" class="interaction-item__action">{{ getActionLabel(item) }}</span>
          </div>
          <span :class="['interaction-item__state', item.isRead ? 'is-read' : 'is-unread']">
            <span v-if="!item.isRead" class="interaction-item__dot" aria-hidden="true"></span>
            {{ item.isRead ? $t('info.read') : $t('info.unread') }}
          </span>
        </div>
      </button>
    </div>
  </div>
</template>

<script setup>
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

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

defineEmits(['select-item', 'mark-all'])

function getModuleLabel(module) {
  if (module === 'marketplace') return t('info.moduleMarketplace')
  if (module === 'lostandfound') return t('info.moduleLostAndFound')
  if (module === 'dating') return t('info.moduleDating')
  if (module === 'delivery') return t('info.moduleDelivery')
  if (module === 'secret') return t('info.moduleSecret')
  if (module === 'express') return t('info.moduleExpress')
  if (module === 'topic') return t('info.moduleTopic')
  if (module === 'photograph') return t('info.modulePhotograph')
  return t('info.moduleDefault')
}

function getActionLabel(item) {
  if (!item) return ''
  if (item.targetType === 'received') return t('info.targetReceived')
  if (item.targetType === 'sent') return t('info.targetSent')
  if (item.targetType === 'posts') return t('info.targetPosts')
  if (item.targetType === 'published') return t('info.targetPublished')
  if (item.targetType === 'accepted') return t('info.targetAccepted')
  if (item.targetType === 'comment') return t('info.targetComment')
  if (item.targetType === 'like') return t('info.targetLike')
  if (item.targetType === 'guess') return t('info.targetGuess')
  if (item.type === 'pick_received') return t('info.targetReceived')
  if (item.type === 'pick_accepted' || item.type === 'pick_rejected' || item.type === 'pick_updated') return t('info.targetSent')
  return ''
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

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.card-title {
  font-size: 17px;
  font-weight: 600;
  color: #333333;
  margin-bottom: 0;
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
  background: var(--color-primary);
  border-radius: 2px;
  margin-right: 8px;
}

.card-action {
  border: none;
  background: transparent;
  color: #576b95;
  font-size: 13px;
  padding: 0;
  flex-shrink: 0;
  cursor: pointer;
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
  gap: 12px;
}

.interaction-item__badges {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.interaction-item__module {
  font-size: 12px;
  color: #576b95;
}

.interaction-item__action {
  font-size: 12px;
  color: #7c8797;
  background: #eef2f7;
  border-radius: 999px;
  padding: 2px 8px;
}

.interaction-item__state {
  font-size: 12px;
  font-weight: 600;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.interaction-item__dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--color-primary);
  flex-shrink: 0;
}

.interaction-item__state.is-read {
  color: #999999;
}

.interaction-item__state.is-unread {
  color: var(--color-primary);
}
</style>
