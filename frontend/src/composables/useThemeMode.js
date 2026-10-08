import { onBeforeUnmount, onMounted, ref } from 'vue'

/** 响应式深色模式状态：跟随 <html data-theme>，随系统设置切换自动更新 */
export function useThemeMode() {
  const isDark = ref(false)
  let observer = null

  function sync() {
    isDark.value = document.documentElement.getAttribute('data-theme') === 'dark'
  }

  onMounted(() => {
    sync()
    observer = new MutationObserver(sync)
    observer.observe(document.documentElement, {
      attributes: true,
      attributeFilter: ['data-theme']
    })
  })

  onBeforeUnmount(() => {
    observer?.disconnect()
    observer = null
  })

  return isDark
}
