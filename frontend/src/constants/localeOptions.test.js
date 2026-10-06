import { describe, expect, it } from 'vitest'
import { LOCALE_OPTIONS, getLocaleDisplayName, resolveSupportedLocale } from './localeOptions'

describe('localeOptions', () => {
  it('keeps zh-HK display name aligned with Hong Kong wording', () => {
    const zhHk = LOCALE_OPTIONS.find((item) => item.code === 'zh-HK')
    expect(zhHk?.label).toBe('繁體中文（香港）')
    expect(getLocaleDisplayName('zh-HK')).toBe('繁體中文（香港）')
  })

  it('normalizes aliases, case, underscores and Accept-Language first tag', () => {
    expect(resolveSupportedLocale('zh-Hant-HK')).toBe('zh-HK')
    expect(resolveSupportedLocale('zh_HK')).toBe('zh-HK')
    expect(resolveSupportedLocale('ZH-hk')).toBe('zh-HK')
    expect(resolveSupportedLocale('zh-MO')).toBe('zh-HK')
    expect(resolveSupportedLocale('zh-Hant-MO')).toBe('zh-HK')
    expect(resolveSupportedLocale('zh-TW')).toBe('zh-TW')
    expect(resolveSupportedLocale('zh-Hant')).toBe('zh-TW')
    expect(resolveSupportedLocale('en-US')).toBe('en')
    expect(resolveSupportedLocale('ja-JP')).toBe('ja')
    expect(resolveSupportedLocale('ko-KR')).toBe('ko')
    expect(resolveSupportedLocale('zh-HK,zh;q=0.9,en;q=0.8')).toBe('zh-HK')
    expect(resolveSupportedLocale('fr-FR')).toBe('zh-CN')
    expect(getLocaleDisplayName('zh-Hant-HK')).toBe('繁體中文（香港）')
  })

  it('does not expand the supported language list', () => {
    expect(LOCALE_OPTIONS.map((item) => item.code)).toEqual([
      'zh-CN',
      'zh-HK',
      'zh-TW',
      'en',
      'ja',
      'ko'
    ])
  })
})
