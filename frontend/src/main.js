import 'weui'
import { createApp } from 'vue'
import './style.css'
import './styles/community-theme.css'
import App from './App.vue'
import router from './router'

const app = createApp(App)
app.use(router)
app.mount('#app')
