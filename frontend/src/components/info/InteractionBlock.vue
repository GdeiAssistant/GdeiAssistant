<template>
  <div>
    <div class="flex items-center justify-between mb-3" v-if="unreadCount > 0">
      <span></span>
      <button
        type="button"
        class="text-xs text-[var(--c-text-3)] bg-transparent border-none cursor-pointer p-0"
        @click="$emit('mark-all')"
      >{{ $t('info.markAllRead') }}</button>
    </div>
    <div v-if="!items.length" class="interaction-empty">
      <div class="interaction-empty__icon">
        <Inbox class="size-5" />
      </div>
      <div class="interaction-empty__text">{{ $t('info.noInteraction') }}</div>
    </div>
    <div v-else class="interaction-list" style="margin-top:0">
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
import { Inbox } from 'lucide-vue-next'

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
  background: var(--color-surface);
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
  color: var(--color-text-primary);
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
  background: var(--c-danger);
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
  color: color-mix(in srgb, var(--c-primary) 56%, var(--c-text-2));
  font-size: 13px;
  padding: 0;
  flex-shrink: 0;
  cursor: pointer;
}

.interaction-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  min-height: 132px;
  padding: 24px 18px;
  text-align: center;
  border: 1px solid color-mix(in srgb, var(--c-primary) 8%, var(--c-border));
  border-radius: 18px;
  background: linear-gradient(
    180deg,
    color-mix(in srgb, var(--c-primary) 3%, rgba(255, 255, 255, 0.94)),
    color-mix(in srgb, var(--c-primary) 1%, rgba(255, 255, 255, 0.92))
  );
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.2);
}

.interaction-empty__icon {
  display: grid;
  width: 52px;
  height: 52px;
  place-items: center;
  border: 1px solid color-mix(in srgb, var(--c-primary) 14%, var(--c-border));
  border-radius: 18px;
  background: linear-gradient(
    135deg,
    color-mix(in srgb, var(--c-primary) 10%, rgba(255, 255, 255, 0.96)),
    color-mix(in srgb, var(--c-primary) 4%, rgba(255, 255, 255, 0.94))
  );
  color: color-mix(in srgb, var(--c-primary) 52%, var(--c-text-2));
  box-shadow: 0 14px 28px color-mix(in srgb, var(--c-primary) 8%, transparent);
}

.interaction-empty__text {
  font-size: 14px;
  color: var(--color-text-tertiary);
  font-weight: 600;
}

.interaction-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.interaction-item {
  width: 100%;
  border: 1px solid color-mix(in srgb, var(--c-primary) 10%, var(--c-border));
  border-radius: 18px;
  background:
    linear-gradient(135deg, rgba(255, 255, 255, 0.76), rgba(250, 255, 253, 0.58)),
    radial-gradient(circle at 100% 0, color-mix(in srgb, var(--c-primary) 6%, transparent), transparent 34%);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.36);
  padding: 14px;
  text-align: left;
  cursor: pointer;
  transition: transform 0.18s ease, border-color 0.18s ease, background 0.18s ease, box-shadow 0.18s ease;
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
  font-weight: 780;
  color: var(--c-text-1);
}

.interaction-item__time {
  flex-shrink: 0;
  font-size: 12px;
  color: var(--c-text-3);
}

.interaction-item__content {
  margin-top: 8px;
  font-size: 13px;
  line-height: 1.7;
  color: var(--c-text-2);
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
  color: color-mix(in srgb, var(--c-primary) 56%, var(--c-text-2));
}

.interaction-item__action {
  font-size: 12px;
  color: color-mix(in srgb, var(--c-text-2) 82%, var(--c-primary));
  background: color-mix(in srgb, var(--c-primary) 8%, var(--c-surface));
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
  color: var(--color-text-tertiary);
}

.interaction-item__state.is-unread {
  color: var(--color-primary);
}

.interaction-item:hover {
  border-color: color-mix(in srgb, var(--c-primary) 18%, var(--c-border));
  background:
    linear-gradient(135deg, rgba(255, 255, 255, 0.88), rgba(250, 255, 253, 0.72)),
    radial-gradient(circle at 100% 0, color-mix(in srgb, var(--c-primary) 8%, transparent), transparent 34%);
  box-shadow: 0 14px 30px color-mix(in srgb, var(--c-primary) 9%, transparent);
  transform: translateY(-1px);
}

[data-theme="dark"] .interaction-empty {
  border-color: color-mix(in srgb, var(--c-primary) 12%, rgba(68, 89, 112, 0.72));
  background: linear-gradient(180deg, rgba(26, 39, 54, 0.88), rgba(22, 34, 48, 0.92));
  box-shadow: inset 0 1px 0 rgba(148, 163, 184, 0.06);
}

[data-theme="dark"] .interaction-item {
  background: color-mix(in srgb, var(--c-primary) 4%, rgba(24, 38, 53, 0.96));
  border-color: color-mix(in srgb, var(--c-primary) 16%, rgba(68, 89, 112, 0.78));
}

[data-theme="dark"] .interaction-item:hover {
  background: color-mix(in srgb, var(--c-primary) 7%, rgba(32, 48, 68, 0.98));
}

[data-theme="dark"] .interaction-item__module {
  color: color-mix(in srgb, var(--c-primary) 42%, var(--c-text-2));
}

[data-theme="dark"] .interaction-item__action {
  color: color-mix(in srgb, var(--c-text-2) 88%, var(--c-primary));
  background: color-mix(in srgb, var(--c-primary) 10%, rgba(32, 48, 68, 0.9));
}

[data-theme="dark"] .interaction-empty__icon {
  border-color: color-mix(in srgb, var(--c-primary) 16%, rgba(68, 89, 112, 0.74));
  background: linear-gradient(
    135deg,
    color-mix(in srgb, var(--c-primary) 10%, rgba(32, 48, 68, 0.9)),
    color-mix(in srgb, var(--c-primary) 5%, rgba(24, 38, 53, 0.94))
  );
  color: color-mix(in srgb, var(--c-primary) 34%, #dbeafe);
  box-shadow: 0 16px 30px rgba(0, 0, 0, 0.16);
}

</style>
