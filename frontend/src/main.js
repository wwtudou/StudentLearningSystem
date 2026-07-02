// Vue 应用入口：挂载根组件并启用路由
import { createApp } from 'vue'
import App from './App.vue'
import router from './router'

createApp(App).use(router).mount('#app')
