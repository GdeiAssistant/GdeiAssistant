import { beforeEach, describe, expect, it, vi } from 'vitest'
import { detectBrowserLocale, getSavedLocale } from './i18n'

describe('i18n locale normalization', () => {
  beforeEach(() => {
    localStorage.clear()
  })

  it('normalizes zh-Hant-HK browser locale to zh-HK', () => {
    vi.stubGlobal('navigator', { language: 'zh-Hant-HK' })

    expect(detectBrowserLocale()).toBe('zh-HK')
  })

  it('normalizes legacy saved locale variants before reuse', () => {
    localStorage.setItem('locale', 'zh-Hant-HK')

    expect(getSavedLocale()).toBe('zh-HK')
  })
})
