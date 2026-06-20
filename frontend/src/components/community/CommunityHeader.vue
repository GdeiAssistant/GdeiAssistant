<script setup>
import { useRouter } from 'vue-router'

const props = defineProps({
  /** 页面标题 */
  title: { type: String, default: '' },
  /** 模块主色 */
  moduleColor: { type: String, default: '#6366f1' },
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
  <header class="community-header" :style="{ '--module-color': moduleColor }">
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
  display: grid;
  min-height: 54px;
  grid-template-columns: 44px minmax(0, 1fr) 44px;
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
  background: linear-gradient(90deg, var(--module-color), color-mix(in srgb, var(--module-color) 40%, #3aa7e8));
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
}

[data-theme="dark"] .community-header {
  border-bottom-color: rgba(45, 58, 73, 0.86);
  background: rgba(20, 27, 37, 0.9);
}

[data-theme="dark"] .community-header__back {
  border-color: rgba(45, 58, 73, 0.86);
  background: rgba(31, 41, 55, 0.76);
}
</style>
