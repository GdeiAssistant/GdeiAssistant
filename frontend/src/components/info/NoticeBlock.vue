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
  border: 1px solid color-mix(in srgb, var(--c-primary) 10%, var(--c-border));
  border-radius: 18px;
  background:
    linear-gradient(135deg, rgba(255, 255, 255, 0.76), rgba(250, 255, 253, 0.58)),
    radial-gradient(circle at 0 0, color-mix(in srgb, var(--c-primary) 7%, transparent), transparent 30%);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.36);
}

.notice-content::before {
  position: absolute;
  top: 16px;
  bottom: 16px;
  left: 0;
  width: 3px;
  border-radius: 0 999px 999px 0;
  content: '';
  background: linear-gradient(180deg, var(--c-primary), color-mix(in srgb, var(--c-primary) 36%, var(--c-info)));
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
  border: 1px solid color-mix(in srgb, var(--c-primary) 8%, var(--c-border));
  border-radius: 18px;
  background: linear-gradient(
    180deg,
    color-mix(in srgb, var(--c-primary) 3%, rgba(255, 255, 255, 0.94)),
    color-mix(in srgb, var(--c-primary) 1%, rgba(255, 255, 255, 0.92))
  );
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.2);
}

.notice-empty__icon {
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

.notice-empty__text {
  font-size: 14px;
  color: var(--color-text-tertiary);
  font-weight: 600;
}

[data-theme="dark"] .notice-empty {
  border-color: color-mix(in srgb, var(--c-primary) 12%, rgba(68, 89, 112, 0.72));
  background: linear-gradient(180deg, rgba(26, 39, 54, 0.88), rgba(22, 34, 48, 0.92));
  box-shadow: inset 0 1px 0 rgba(148, 163, 184, 0.06);
}

[data-theme="dark"] .notice-empty__icon {
  border-color: color-mix(in srgb, var(--c-primary) 16%, rgba(68, 89, 112, 0.74));
  background: linear-gradient(
    135deg,
    color-mix(in srgb, var(--c-primary) 10%, rgba(32, 48, 68, 0.9)),
    color-mix(in srgb, var(--c-primary) 5%, rgba(24, 38, 53, 0.94))
  );
  color: color-mix(in srgb, var(--c-primary) 34%, #dbeafe);
  box-shadow: 0 16px 30px rgba(0, 0, 0, 0.16);
}

[data-theme="dark"] .notice-content {
  border-color: color-mix(in srgb, var(--c-primary) 12%, rgba(68, 89, 112, 0.72));
  background:
    radial-gradient(circle at 0 0, color-mix(in srgb, var(--c-primary) 7%, transparent), transparent 30%),
    linear-gradient(180deg, rgba(27, 40, 55, 0.86), rgba(22, 34, 48, 0.9));
  box-shadow: inset 0 1px 0 rgba(148, 163, 184, 0.06);
}

[data-theme="dark"] .notice-content::before {
  background: linear-gradient(180deg, color-mix(in srgb, var(--c-primary) 64%, #dbeafe), color-mix(in srgb, var(--c-info) 42%, var(--c-text-2)));
}
</style>
