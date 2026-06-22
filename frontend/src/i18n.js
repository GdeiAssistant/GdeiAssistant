import { createI18n } from 'vue-i18n'
import zhCN from './locales/zh-CN.json'
import { resolveSupportedLocale } from './constants/localeOptions'

export function detectBrowserLocale() {
  const lang = (typeof navigator !== 'undefined' && navigator.language) || 'zh-CN'
  return resolveSupportedLocale(lang)
}

export function getSavedLocale() {
  try {
    const saved = localStorage.getItem('locale')
    return saved ? resolveSupportedLocale(saved) : null
  } catch (e) {
    return null
  }
}

const i18n = createI18n({
  legacy: false,
  locale: getSavedLocale() || detectBrowserLocale(),
  fallbackLocale: 'zh-CN',
  messages: { 'zh-CN': zhCN }
})

const localeLoaders = import.meta.glob(['./locales/*.json', '!./locales/zh-CN.json'])

export async function setLocale(locale) {
  const normalizedLocale = resolveSupportedLocale(locale)

  if (!i18n.global.availableLocales.includes(normalizedLocale)) {
    const messages = await localeLoaders[`./locales/${normalizedLocale}.json`]()
    i18n.global.setLocaleMessage(normalizedLocale, messages.default)
  }
  i18n.global.locale.value = normalizedLocale
  localStorage.setItem('locale', normalizedLocale)
}

export default i18n
