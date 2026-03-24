import { createApp } from 'vue'
import './style.css'
import App from './App.vue'
import router from './router'
import i18n, { setLocale, detectBrowserLocale, getSavedLocale } from './i18n'
import { initTheme } from './theme.js'

initTheme()

async function bootstrap() {
  const saved = getSavedLocale()
  const target = saved || detectBrowserLocale()

  // Preload the correct locale messages before mounting so the UI
  // never flashes zh-CN when the user chose a different language.
  if (target !== 'zh-CN') {
    await setLocale(target)
  }

  const app = createApp(App)
  app.use(router)
  app.use(i18n)
  app.mount('#app')
}

bootstrap()
