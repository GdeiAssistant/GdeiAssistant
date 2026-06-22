<script setup>
import { useRouter } from 'vue-router'

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
    :class="{ 'community-header--no-back': !showBack }"
    :style="{ '--module-color': moduleColor }"
  >
    <button
      v-if="showBack"
      type="button"
      class="community-header__back"
      aria-label="返回"
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
  isolation: isolate;
  overflow: hidden;
  display: grid;
  min-height: 54px;
  grid-template-columns: 44px minmax(0, 1fr) auto;
  align-items: center;
  padding: 6px 10px;
  border-bottom: 1px solid rgba(218, 229, 233, 0.78);
  background: rgba(255, 255, 255, 0.9);
  box-shadow: 0 10px 26px rgba(32, 69, 78, 0.06);
  backdrop-filter: blur(16px);
}

.community-header::before {
  position: absolute;
  top: 0;
  right: 0;
  left: 0;
  height: 3px;
  content: '';
  background: linear-gradient(90deg, var(--module-color), color-mix(in srgb, var(--module-color) 40%, #6fd8d0));
}

.community-header::after {
  position: absolute;
  inset: 0;
  z-index: -1;
  content: '';
  pointer-events: none;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.1), transparent 45%);
}

.community-header__back {
  display: grid;
  width: 36px;
  height: 36px;
  place-items: center;
  border: 1px solid rgba(205, 222, 226, 0.72);
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.76);
  color: var(--c-text-1);
  cursor: pointer;
  transition: transform 0.18s ease, border-color 0.18s ease, background 0.18s ease, box-shadow 0.18s ease;
}

.community-header__back:hover {
  transform: translateY(-1px);
  border-color: color-mix(in srgb, var(--module-color) 18%, var(--c-border));
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 10px 24px color-mix(in srgb, var(--module-color) 10%, transparent);
}

.community-header h1 {
  overflow: hidden;
  margin: 0;
  color: var(--c-text-1);
  font-size: 16px;
  font-weight: 900;
  letter-spacing: -0.01em;
  text-align: center;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.community-header__right {
  display: flex;
  min-width: 36px;
  justify-content: flex-end;
  white-space: nowrap;
}

.community-header--no-back {
  grid-template-columns: minmax(0, 1fr) auto;
  column-gap: 12px;
  padding-left: 20px;
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
    max-width: 1180px;
    min-height: 68px;
    margin: 24px auto 14px;
    grid-template-columns: 48px minmax(0, 1fr) auto;
    padding: 10px 14px;
    border: 1px solid color-mix(in srgb, var(--module-color) 20%, rgba(205, 222, 226, 0.76));
    border-radius: 24px;
    box-shadow: 0 16px 38px rgba(32, 69, 78, 0.07);
  }

  .community-header::before {
    top: 10px;
    bottom: 10px;
    left: 0;
    width: 4px;
    height: auto;
    border-radius: 0 999px 999px 0;
    background: var(--module-color);
  }

  .community-header h1 {
    font-size: 20px;
    text-align: left;
  }

  .community-header--no-back {
    grid-template-columns: minmax(0, 1fr) auto;
  }

  .community-header__back {
    width: 40px;
    height: 40px;
    border-radius: 15px;
  }
}

[data-theme="dark"] .community-header {
  --community-module-dark-accent: color-mix(in srgb, var(--module-color) 54%, var(--c-text-1));
  --community-module-dark-accent-end: color-mix(in srgb, var(--module-color) 34%, var(--c-text-2));

  border-color: rgba(68, 89, 112, 0.72);
  border-bottom-color: rgba(68, 89, 112, 0.72);
  background:
    radial-gradient(circle at 0 0, color-mix(in srgb, var(--module-color) 9%, transparent), transparent 34%),
    linear-gradient(135deg, rgba(27, 42, 58, 0.94), rgba(20, 33, 46, 0.92));
  box-shadow:
    0 14px 30px rgba(0, 0, 0, 0.2),
    inset 0 1px 0 rgba(255, 255, 255, 0.03);
}

[data-theme="dark"] .community-header::before {
  background: linear-gradient(90deg, var(--community-module-dark-accent), var(--community-module-dark-accent-end));
}

[data-theme="dark"] .community-header::after {
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.04), transparent 45%);
}

[data-theme="dark"] .community-header__back {
  border-color: rgba(68, 89, 112, 0.72);
  background: rgba(31, 47, 66, 0.88);
  color: color-mix(in srgb, var(--module-color) 18%, var(--c-text-1));
}

[data-theme="dark"] .community-header__back:hover {
  border-color: color-mix(in srgb, var(--module-color) 18%, rgba(97, 122, 147, 0.78));
  background: rgba(36, 54, 75, 0.96);
  box-shadow: 0 12px 24px color-mix(in srgb, var(--module-color) 10%, transparent);
}

[data-theme="dark"] .community-header h1 {
  color: color-mix(in srgb, var(--c-text-1) 92%, white);
}
</style>
