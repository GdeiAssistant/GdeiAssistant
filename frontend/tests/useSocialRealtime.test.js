import { describe, expect, it, vi, afterEach } from 'vitest'
import { resolveWsUrl } from '../src/composables/useSocialRealtime.js'

describe('resolveWsUrl', () => {
  afterEach(() => {
    vi.unstubAllGlobals()
  })

  it('uses page host when API base is relative', () => {
    vi.stubGlobal('window', { location: { protocol: 'http:', host: 'localhost:5173' } })
    // VITE_APP_BASE_API 在 vitest 中通常为相对 /api 或缺省
    const url = resolveWsUrl()
    expect(url.startsWith('ws://localhost:5173/api/social/realtime')).toBe(true)
  })
})
