import { describe, expect, it } from 'vitest'
import { readFileSync, readdirSync } from 'node:fs'
import { dirname, join } from 'node:path'
import { fileURLToPath } from 'node:url'
import i18n, { setLocale } from '../i18n.js'

const localeDir = dirname(fileURLToPath(import.meta.url))
const LOCALES = ['zh-CN', 'zh-HK', 'zh-TW', 'en', 'ja', 'ko']

function flatten(obj, prefix = '') {
  const out = {}
  for (const [key, value] of Object.entries(obj)) {
    const path = prefix ? `${prefix}.${key}` : key
    if (value && typeof value === 'object' && !Array.isArray(value)) {
      Object.assign(out, flatten(value, path))
    } else {
      out[path] = value
    }
  }
  return out
}

function placeholders(text) {
  return String(text).match(/\{[^}]+\}/g) || []
}

function loadLocale(code) {
  return JSON.parse(readFileSync(join(localeDir, `${code}.json`), 'utf8'))
}

describe('locale resources', () => {
  const maps = Object.fromEntries(LOCALES.map((code) => [code, flatten(loadLocale(code))]))
  const baseKeys = Object.keys(maps['zh-CN']).sort()

  it('keeps identical key sets and non-empty values across six locales', () => {
    for (const code of LOCALES) {
      expect(Object.keys(maps[code]).sort()).toEqual(baseKeys)
      for (const key of baseKeys) {
        expect(String(maps[code][key] || '').trim(), `${code}:${key}`).not.toBe('')
      }
    }
  })

  it('keeps placeholders aligned with zh-CN or en', () => {
    for (const code of LOCALES) {
      for (const key of baseKeys) {
        const actual = placeholders(maps[code][key])
        const zh = placeholders(maps['zh-CN'][key])
        const en = placeholders(maps.en[key])
        expect(
          actual.join('|') === zh.join('|') || actual.join('|') === en.join('|'),
          `${code}:${key} placeholders ${actual} vs zh ${zh} / en ${en}`
        ).toBe(true)
      }
    }
  })

  it('keeps Taiwan Mandarin free of Cantonese particles', () => {
    const canto = /[嘅唔咗啲撳搵]/
    const hits = Object.entries(maps['zh-TW']).filter(([, value]) => canto.test(String(value)))
    expect(hits).toEqual([])
  })

  it('keeps japanese campus credential copy free of simplified Chinese leftovers', () => {
    const hits = Object.entries(maps.ja).filter(([, value]) => String(value).includes('凭证'))
    expect(hits).toEqual([])
  })
})

describe('static i18n references', () => {
  it('only references existing locale keys from vue/js sources', () => {
    const keys = new Set(Object.keys(flatten(loadLocale('zh-CN'))))
    const root = join(localeDir, '..')
    const files = []
    const walk = (dir) => {
      for (const name of readdirSync(dir, { withFileTypes: true })) {
        if (name.name === 'locales' || name.name === 'node_modules' || name.name.endsWith('.test.js')) continue
        const path = join(dir, name.name)
        if (name.isDirectory()) walk(path)
        else if (/\.(vue|js)$/.test(name.name)) files.push(path)
      }
    }
    walk(root)

    const missing = []
    const pattern = /(?:\$t|\bt)\(\s*['"]([a-zA-Z][\w.-]*)['"]/g
    for (const file of files) {
      const text = readFileSync(file, 'utf8')
      let match
      while ((match = pattern.exec(text))) {
        const key = match[1]
        if (!keys.has(key) && !key.includes('${')) missing.push(`${file}:${key}`)
      }
    }
    expect(missing).toEqual([])
  })
})

describe('runtime locale switching', () => {
  it('loads all six languages and updates live messages, document metadata and persistence', async () => {
    const previousLocale = i18n.global.locale.value
    const previousSaved = localStorage.getItem('locale')
    const previousLang = document.documentElement.lang
    const previousTitle = document.title
    try {
      for (const code of LOCALES) {
        await setLocale(code)
        const messages = loadLocale(code)
        expect(i18n.global.locale.value).toBe(code)
        expect(i18n.global.t('privacy.field.faculty')).toBe(messages.privacy.field.faculty)
        expect(document.documentElement.lang).toBe(code)
        expect(document.title).toBe(messages.router.siteTitle)
        expect(localStorage.getItem('locale')).toBe(code)
      }
    } finally {
      await setLocale(previousLocale)
      if (previousSaved === null) localStorage.removeItem('locale')
      else localStorage.setItem('locale', previousSaved)
      document.documentElement.lang = previousLang
      document.title = previousTitle
    }
  })
})
