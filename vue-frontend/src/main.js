import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import './styles/theme.scss'
import './styles/responsive.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import App from './App.vue'
import router from './router'
import store from './store'
import i18n from './i18n'

const app = createApp(App)

// 注册所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// Element Plus locale 通过 App.vue 的 ElConfigProvider 响式切换，不再静态传入
app.use(ElementPlus)
app.use(router)
app.use(store)
app.use(i18n)

app.mount('#app')
