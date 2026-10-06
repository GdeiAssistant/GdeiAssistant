import { ref, watch, onUnmounted } from 'vue'
import request from '../utils/request.js'
import { resolveChatImage } from '../views/social/chatImageSupport.js'

export function useChatImage(urlRef) {
  const src = ref(''), failed = ref(false), loading = ref(false)
  let objectUrl = null, epoch = 0
  function clear() {
    if (objectUrl) URL.revokeObjectURL(objectUrl)
    objectUrl = null; src.value = ''; loading.value = false
  }
  async function load(value) {
    const run = ++epoch
    clear(); failed.value = false
    if (!value) return
    const token = localStorage.getItem('token')
    const resolved = resolveChatImage(value, import.meta.env.VITE_APP_BASE_API, window.location.origin)
    if (!token || !resolved) { failed.value = true; return }
    loading.value = true
    try {
      const blob = await request.get(resolved.requestPath, { responseType: 'blob', skipErrorTip: true })
      if (run !== epoch || localStorage.getItem('token') !== token) return
      if (!(blob instanceof Blob) || !blob.size || !['image/jpeg', 'image/png'].includes(blob.type)) throw new Error('invalid image')
      objectUrl = URL.createObjectURL(blob); src.value = objectUrl
    } catch (_) { if (run === epoch) failed.value = true }
    finally { if (run === epoch) loading.value = false }
  }
  function invalidate() { epoch++; clear() }
  window.addEventListener('social-auth-changed', invalidate)
  watch(urlRef, load, { immediate: true })
  onUnmounted(() => { invalidate(); window.removeEventListener('social-auth-changed', invalidate) })
  return { src, failed, loading, reload: () => load(urlRef.value) }
}
