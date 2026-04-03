// @vitest-environment jsdom

import fs from 'node:fs'
import path from 'node:path'
import { describe, expect, it } from 'vitest'
import { localizeCountryCodes, loadCountryCodeCatalog, parseCountryCodesXml } from '../bindPhoneSupport'

describe('bindPhoneSupport', () => {
  it('parses the bundled country code catalog with broad coverage', () => {
    const xmlPath = path.resolve(process.cwd(), 'public/country_codes.xml')
    const xml = fs.readFileSync(xmlPath, 'utf8')

    const items = parseCountryCodesXml(xml)

    expect(items.length).toBeGreaterThan(150)
    expect(items.some((item) => item.code === '+1')).toBe(true)
    expect(items.some((item) => item.code === '+44')).toBe(true)
    expect(items.some((item) => item.code === '+81')).toBe(true)
    expect(items.some((item) => item.code === '+852')).toBe(true)
    expect(items.some((item) => item.code === '+886')).toBe(true)
  })

  it('localizes country names from region metadata instead of keeping raw Chinese labels', () => {
    const items = localizeCountryCodes([
      { iso: 'CN', code: '+86', emoji: '🇨🇳', rawName: '中国大陆' },
      { iso: 'JP', code: '+81', emoji: '🇯🇵', rawName: '日本' },
    ], 'en')

    expect(items[0].name).toContain('China')
    expect(items[1].name).toContain('Japan')
  })

  it('falls back to the bundled country list when remote requests time out', async () => {
    const fetchImpl = (_url, init = {}) => new Promise((_, reject) => {
      init.signal?.addEventListener('abort', () => reject(new Error('aborted')), { once: true })
    })

    const items = await loadCountryCodeCatalog({
      apiUrl: '/api/phone/attribution',
      locale: 'zh-CN',
      fetchImpl,
      timeoutMs: 5,
    })

    expect(items).toEqual([
      expect.objectContaining({
        iso: 'CN',
        code: '+86',
      }),
    ])
  })
})
