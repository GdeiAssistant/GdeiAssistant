import { createI18n } from 'vue-i18n'
import zhCN from './locales/zh-CN.json'

function detectBrowserLocale() {
  const lang = navigator.language
  const supported = ['zh-CN', 'zh-HK', 'zh-TW', 'en', 'ja', 'ko']
  if (supported.includes(lang)) return lang
  if (lang.startsWith('zh')) return 'zh-CN'
  if (lang.startsWith('ja')) return 'ja'
  if (lang.startsWith('ko')) return 'ko'
  if (lang.startsWith('en')) return 'en'
  return 'zh-CN'
}

const i18n = createI18n({
  legacy: false,
  locale: localStorage.getItem('locale') || detectBrowserLocale(),
  fallbackLocale: 'zh-CN',
  messages: { 'zh-CN': zhCN }
})

export async function setLocale(locale) {
  if (!i18n.global.availableLocales.includes(locale)) {
    const messages = await import(`./locales/${locale}.json`)
    i18n.global.setLocaleMessage(locale, messages.default)
  }
  i18n.global.locale.value = locale
  localStorage.setItem('locale', locale)
}

export default i18n
