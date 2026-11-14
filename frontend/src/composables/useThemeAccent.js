import { onBeforeUnmount, onMounted, ref } from 'vue'
import { THEME_ACCENTS, getThemeAccent, setThemeAccent } from '@/theme'

/** Reactive campus theme accent synchronized with `<html data-accent>`. */
export function useThemeAccent() {
  const accent = ref(getThemeAccent())
  let observer = null

  function syncAccent() {
    const attrAccent = document.documentElement.dataset.accent
    accent.value = THEME_ACCENTS.includes(attrAccent) ? attrAccent : getThemeAccent()
  }

  function updateAccent(value) {
    accent.value = setThemeAccent(value)
    return accent.value
  }

  onMounted(() => {
    syncAccent()
    observer = new MutationObserver(syncAccent)
    observer.observe(document.documentElement, {
      attributes: true,
      attributeFilter: ['data-accent']
    })
  })

  onBeforeUnmount(() => {
    observer?.disconnect()
    observer = null
  })

  return { accent, updateAccent }
}
