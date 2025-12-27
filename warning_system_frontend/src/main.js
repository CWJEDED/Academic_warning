import { createApp } from 'vue'
import App from './App.vue'
import router from './router' // 必须导入 router

const app = createApp(App)

app.use(router) // 必须注册 router

app.mount('#app')
