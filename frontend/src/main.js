import 'weui'
import { createApp } from 'vue'
import './style.css'
import './styles/community-theme.css'
import App from './App.vue'
import router from './router'
import i18n from './i18n'
import { initTheme } from './theme.js'

initTheme()

const app = createApp(App)
app.use(router)
app.use(i18n)
app.mount('#app')
