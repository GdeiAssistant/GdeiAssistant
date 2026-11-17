// @vitest-environment jsdom

import { describe, expect, it, vi } from 'vitest'
import {
  getFallbackCountryCodeItems,
  loadCountryCodeCatalog,
  localizeCountryCodes,
} from '../bindPhoneSupport'

describe('bindPhoneSupport', () => {
  it('provides broad static fallback coverage without XML parsing', () => {
    const items = getFallbackCountryCodeItems()

    expect(items.length).toBeGreaterThan(150)
    expect(items.some((item) => item.code === '+1')).toBe(true)
    expect(items.some((item) => item.code === '+44')).toBe(true)
    expect(items.some((item) => item.code === '+81')).toBe(true)
    expect(items.some((item) => item.code === '+852')).toBe(true)
    expect(items.some((item) => item.code === '+886')).toBe(true)
    expect(items.some((item) => item.code === '+86' && item.iso === 'CN')).toBe(true)
  })

  it('localizes country names from region metadata instead of keeping raw Chinese labels', () => {
    const items = localizeCountryCodes([
      { iso: 'CN', code: '+86', emoji: '🇨🇳', rawName: '中国大陆' },
      { iso: 'JP', code: '+81', emoji: '🇯🇵', rawName: '日本' },
    ], 'en')

    expect(items[0].name).toContain('China')
    expect(items[1].name).toContain('Japan')
  })

  it('loads remote catalog through unified request client and infers ISO statically', async () => {
    const get = vi.fn().mockResolvedValue({
      success: true,
      data: [
        { code: 86, flag: '🇨🇳', name: '中国' },
        { code: 852, flag: '🇭🇰', name: '中国香港' },
      ],
    })

    const items = await loadCountryCodeCatalog({
      locale: 'en',
      requestClient: { get },
    })

    expect(get).toHaveBeenCalledWith('/phone/attribution', expect.objectContaining({
      headers: { 'Accept-Language': 'en' },
      skipErrorTip: true,
    }))
    expect(items).toEqual([
      { iso: 'CN', code: '+86', emoji: '🇨🇳', rawName: '中国' },
      { iso: 'HK', code: '+852', emoji: '🇭🇰', rawName: '中国香港' },
    ])
  })

  it('uses static fallback when remote catalog is skipped or fails', async () => {
    const get = vi.fn().mockRejectedValue(new Error('timeout'))

    const skippedItems = await loadCountryCodeCatalog({
      skipRemote: true,
      locale: 'zh-CN',
      requestClient: { get },
    })
    expect(get).not.toHaveBeenCalled()
    expect(skippedItems.length).toBeGreaterThan(150)

    const fallbackItems = await loadCountryCodeCatalog({
      locale: 'zh-CN',
      requestClient: { get },
    })
    expect(fallbackItems.length).toBeGreaterThan(150)
    expect(fallbackItems.some((item) => item.iso === 'CN' && item.code === '+86')).toBe(true)
  })
})
