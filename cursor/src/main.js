import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import App from './App.vue'
import router from './router'
import './styles/index.css'
import './styles/responsive.css'
import * as echarts from 'echarts' // 你已经加了

const app = createApp(App)
const pinia = createPinia()

// 注册所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 全局错误处理
app.config.errorHandler = (err, instance, info) => {
  console.error('全局错误:', err, info)
}

window.addEventListener('unhandledrejection', (event) => {
  console.warn('未处理的 Promise rejection:', event.reason)
})

app.use(pinia)
app.use(router)
app.use(ElementPlus)
app.config.globalProperties.$echarts = echarts

app.mount('#app')