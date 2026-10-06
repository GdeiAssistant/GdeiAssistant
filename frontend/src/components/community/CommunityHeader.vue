<script setup>
import { computed, useSlots } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'

const props = defineProps({
  /** 页面标题 */
  title: { type: String, default: '' },
  /** 模块主色 */
  moduleColor: { type: String, default: 'var(--c-topic)' },
  /** 返回目标路径，默认 / */
  backTo: { type: String, default: '/' },
  /** 是否显示返回按钮 */
  showBack: { type: Boolean, default: true }
})

const emit = defineEmits(['back'])
const router = useRouter()
const { t } = useI18n()
const slots = useSlots()
const hasRightSlot = computed(() => Boolean(slots.right))

function handleBack() {
  emit('back')
  if (props.backTo) {
    router.push(props.backTo)
  }
}
</script>

<template>
  <header
    class="community-header"
    :class="{ 'community-header--no-back': !showBack, 'community-header--with-right': hasRightSlot }"
    :style="{ '--module-color': moduleColor }"
  >
    <button
      v-if="showBack"
      type="button"
      class="community-header__back"
      :aria-label="t('common.back')"
      @click="handleBack"
    >
      <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <polyline points="15 18 9 12 15 6" />
      </svg>
    </button>
    <h1>{{ title }}</h1>
    <span class="community-header__right">
      <slot name="right" />
    </span>
  </header>
</template>

<style scoped>
.community-header {
  position: sticky;
  top: 0;
  z-index: 100;
  display: grid;
  min-height: 56px;
  grid-template-columns: 40px minmax(0, 1fr) auto;
  align-items: center;
  column-gap: 8px;
  padding: 8px 16px;
  border-bottom: 1px solid var(--c-border);
  background: color-mix(in srgb, var(--c-surface) 92%, transparent);
  backdrop-filter: blur(10px);
}

.community-header__back {
  display: grid;
  width: 40px;
  height: 40px;
  margin-left: -8px;
  place-items: center;
  border: 0;
  border-radius: var(--radius-control);
  background: transparent;
  color: var(--c-text-1);
  cursor: pointer;
  transition: background-color 0.15s ease, color 0.15s ease;
}

.community-header__back:hover {
  background: var(--c-primary-soft);
  color: var(--c-primary);
}

.community-header__back:focus-visible {
  outline: 2px solid var(--c-primary);
  outline-offset: 2px;
}

.community-header h1 {
  overflow: hidden;
  margin: 0;
  color: var(--c-text-1);
  font-size: 16px;
  font-weight: 650;
  text-align: center;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.community-header__right {
  display: flex;
  min-width: 40px;
  justify-content: flex-end;
  white-space: nowrap;
}

.community-header--no-back {
  grid-template-columns: minmax(0, 1fr) auto;
  column-gap: 12px;
}

.community-header--no-back h1 {
  text-align: left;
}

.community-header--no-back .community-header__right {
  min-width: 0;
}

@media (min-width: 768px) {
  .community-header {
    position: static;
    min-height: 56px;
    padding: 8px 20px;
    background: var(--c-surface);
    backdrop-filter: none;
  }

  .community-header h1 {
    font-size: 17px;
    text-align: left;
  }
}

@media (prefers-reduced-motion: reduce) {
  .community-header__back {
    transition: none;
  }
}
</style>
