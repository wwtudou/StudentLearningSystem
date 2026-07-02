// 前端路由配置
import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import StudentView from '../views/StudentView.vue'

const routes = [
  { path: '/', name: 'home', component: HomeView },           // 首页
  { path: '/students', name: 'students', component: StudentView }  // 学生管理
]

export default createRouter({
  history: createWebHistory(),
  routes
})
