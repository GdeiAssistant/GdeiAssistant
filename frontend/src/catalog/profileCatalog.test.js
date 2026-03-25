import { describe, expect, it } from 'vitest'

import {
  getProfileCatalog,
  formatProfileOptions,
  normalizeCatalogLocale,
} from './profileCatalog'

describe('profileCatalog', () => {
  it('normalizes supported locales', () => {
    expect(normalizeCatalogLocale('en-US')).toBe('en')
    expect(normalizeCatalogLocale('zh-Hant-HK')).toBe('zh-HK')
    expect(normalizeCatalogLocale('ja-JP')).toBe('ja')
    expect(normalizeCatalogLocale('ko-KR')).toBe('ko')
  })

  it('maps faculty and major codes to localized labels', () => {
    const catalog = getProfileCatalog('en-US')

    expect(catalog.facultyLabel(11)).toBe('Department of Computer Science')
    expect(catalog.majorLabel(11, 'software_engineering')).toBe('Software Engineering')
    expect(catalog.unselectedLabel).toBe('Not selected')
    expect(catalog.otherLabel).toBe('Other')
  })

  it('formats code-only options payload into localized labels', () => {
    const localized = formatProfileOptions({
      faculties: [
        { code: 11, majors: ['software_engineering', 'network_engineering'] },
      ],
      marketplaceItemTypes: [0, 11],
      lostFoundItemTypes: [0, 11],
      lostFoundModes: [0, 1],
    }, 'en-US')

    expect(localized.faculties[0]).toEqual({
      code: 11,
      label: 'Department of Computer Science',
      majors: [
        { code: 'software_engineering', label: 'Software Engineering' },
        { code: 'network_engineering', label: 'Network Engineering' },
      ],
    })
    expect(localized.marketplaceItemTypes[1].label).toBe('Other')
    expect(localized.lostFoundModes[0].label).toBe('Lost Item Notice')
  })
})
