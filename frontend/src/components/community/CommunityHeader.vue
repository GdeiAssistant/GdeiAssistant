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
  <div class="community-header" :style="{ '--module-color': moduleColor }">
    <div class="community-header__accent"></div>
    <div class="community-header__content">
      <span
        v-if="showBack"
        class="community-header__back"
        @click="handleBack"
      >
        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <polyline points="15 18 9 12 15 6"/>
        </svg>
      </span>
      <h1 class="community-header__title">{{ title }}</h1>
      <span class="community-header__right">
        <slot name="right" />
      </span>
    </div>
  </div>
</template>

<style scoped>
.community-header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: var(--c-card);
}

.community-header__accent {
  height: 3px;
  background: linear-gradient(90deg, var(--module-color), var(--module-color));
  background: linear-gradient(90deg, var(--module-color), color-mix(in srgb, var(--module-color) 60%, white));
}

.community-header__content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 48px;
  padding: 0 var(--space-md, 12px);
  border-bottom: 1px solid var(--c-border, #f3f4f6);
}

.community-header__back {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  cursor: pointer;
  color: var(--c-text-1, #1f2937);
  transition: background 0.2s;
  -webkit-tap-highlight-color: transparent;
}

.community-header__back:active {
  background: var(--c-bg, #f9fafb);
}

.community-header__title {
  flex: 1;
  text-align: center;
  font-size: 16px;
  font-weight: 600;
  margin: 0;
  color: var(--c-text-1, #1f2937);
  letter-spacing: 0.5px;
}

.community-header__right {
  min-width: 36px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
}
</style>
