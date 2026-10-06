import { ref, watch, onUnmounted } from 'vue'
import request from '../utils/request.js'
import { resolveSocialAvatar } from './socialAvatarUrl.js'

/**
 * 通过 Bearer 鉴权拉取头像 blob，禁止把 token 放 query 或用裸 img 直打鉴权端点。
 */
export function useAuthAvatar(urlRef) {
  const src = ref('')
  let objectUrl = null
  let seq = 0

  async function load(url) {
    const my = ++seq
    revoke()
    src.value = ''
    if (!url) return
    const resolved = resolveSocialAvatar(url, import.meta.env.VITE_APP_BASE_API, window.location.origin)
    if (!resolved) return
    if (!resolved.authenticated) {
      src.value = resolved.url
      return
    }
    const token = localStorage.getItem('token')
    if (!token) return
    try {
      const res = await request.get(resolved.requestPath, {
        responseType: 'blob',
        skipErrorTip: true
      })
      if (my !== seq || localStorage.getItem('token') !== token) return
      const blob = res instanceof Blob ? res : res?.data
      if (!(blob instanceof Blob) || blob.size === 0) return
      objectUrl = URL.createObjectURL(blob)
      src.value = objectUrl
    } catch (_) {
      if (my === seq) src.value = ''
    }
  }

  function revoke() {
    if (objectUrl) {
      URL.revokeObjectURL(objectUrl)
      objectUrl = null
    }
  }

  watch(urlRef, (u) => load(u), { immediate: true })
  onUnmounted(() => {
    seq += 1
    revoke()
  })

  return { src }
}
