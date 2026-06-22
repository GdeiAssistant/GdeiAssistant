import { describe, expect, it } from 'vitest'
import { LOCALE_OPTIONS, getLocaleDisplayName, resolveSupportedLocale } from './localeOptions'

describe('localeOptions', () => {
  it('keeps zh-HK display name aligned with Hong Kong wording', () => {
    const zhHk = LOCALE_OPTIONS.find((item) => item.code === 'zh-HK')
    expect(zhHk?.label).toBe('繁體中文（香港）')
    expect(getLocaleDisplayName('zh-HK')).toBe('繁體中文（香港）')
  })

  it('normalizes locale variants before reading the display name', () => {
    expect(resolveSupportedLocale('zh-Hant-HK')).toBe('zh-HK')
    expect(resolveSupportedLocale('en-US')).toBe('en')
    expect(getLocaleDisplayName('zh-Hant-HK')).toBe('繁體中文（香港）')
  })
})
