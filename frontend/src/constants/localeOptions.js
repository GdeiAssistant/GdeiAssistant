export const LOCALE_OPTIONS = [
  { code: 'zh-CN', label: '简体中文' },
  { code: 'zh-HK', label: '繁體中文（香港）' },
  { code: 'zh-TW', label: '繁體中文（台灣）' },
  { code: 'en', label: 'English' },
  { code: 'ja', label: '日本語' },
  { code: 'ko', label: '한국어' },
]

export function resolveSupportedLocale(code) {
  const normalized = String(code || 'zh-CN').toLowerCase()
  if (normalized === 'zh-hant-hk' || normalized.startsWith('zh-hk')) return 'zh-HK'
  if (normalized.startsWith('zh-tw') || normalized.startsWith('zh-hant')) return 'zh-TW'
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
