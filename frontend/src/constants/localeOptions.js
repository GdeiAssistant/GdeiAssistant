export const LOCALE_OPTIONS = [
  { code: 'zh-CN', label: '简体中文' },
  { code: 'zh-HK', label: '繁體中文（香港）' },
  { code: 'zh-TW', label: '繁體中文（台灣）' },
  { code: 'en', label: 'English' },
  { code: 'ja', label: '日本語' },
  { code: 'ko', label: '한국어' },
]

export function resolveSupportedLocale(code) {
  const raw = String(code || 'zh-CN').trim()
  if (!raw) return 'zh-CN'

  // RFC 5646 / Accept-Language: first language range, drop q-values.
  const firstTag = raw.split(',')[0].trim().split(';')[0].trim()
  const normalized = firstTag.replace(/_/g, '-').toLowerCase()

  // Hong Kong / Macau Traditional → zh-HK
  if (
    normalized.startsWith('zh-hk')
    || normalized.startsWith('zh-mo')
    || normalized === 'zh-hant-hk'
    || normalized === 'zh-hant-mo'
    || normalized.startsWith('zh-hant-hk')
    || normalized.startsWith('zh-hant-mo')
  ) {
    return 'zh-HK'
  }

  // Taiwan / generic zh-Hant → zh-TW
  if (normalized.startsWith('zh-tw') || normalized.startsWith('zh-hant')) {
    return 'zh-TW'
  }

  if (normalized.startsWith('zh')) return 'zh-CN'
  if (normalized.startsWith('ja')) return 'ja'
  if (normalized.startsWith('ko')) return 'ko'
  if (normalized.startsWith('en')) return 'en'
  return 'zh-CN'
}

export function getLocaleDisplayName(code) {
  const resolved = resolveSupportedLocale(code)
  return LOCALE_OPTIONS.find((item) => item.code === resolved)?.label || resolved
}
