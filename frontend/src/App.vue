<!-- 根布局：顶部导航 + 路由出口 -->
<script setup>
import { computed, onMounted, watch } from 'vue'
import { RouterLink, RouterView, useRouter, useRoute } from 'vue-router'
import { fetchMe, logout } from './api/auth'
import { useAuth } from './composables/useAuth'

const router = useRouter()
const route = useRoute()
const { user, displayRoles, setUser, clearUser, routeAllowed } = useAuth()

const navItems = [
  { path: '/', label: '首页' },
  { path: '/students', label: '学生' },
  { path: '/org', label: '院系专业' },
  { path: '/dicts', label: '字典' },
  { path: '/teachers', label: '教师' },
  { path: '/courses', label: '课程' },
  { path: '/rewards', label: '奖惩' },
  { path: '/enrollments', label: '选课' },
  { path: '/grades', label: '成绩' },
  { path: '/reports', label: '报表' },
  { path: '/db-tech', label: '高阶DB' },
  { path: '/users', label: '用户' }
]

const visibleNav = computed(() => navItems.filter(item => routeAllowed(item.path)))

async function loadUser() {
  if (route.path === '/login') return
  try {
    const { data } = await fetchMe()
    if (data.code === 0) setUser(data.data)
  } catch (_) {
    clearUser()
  }
}

async function handleLogout() {
  await logout()
  clearUser()
  router.push('/login')
}

onMounted(loadUser)
watch(() => route.path, loadUser)
</script>

<template>
  <div v-if="route.path === '/login'" class="login-layout">
    <RouterView />
  </div>
  <div v-else class="layout">
    <header>
      <div class="head-row">
        <h1>学生专业学习管理信息系统</h1>
        <div v-if="user" class="user-bar">
          <span>{{ user.realName }}（{{ displayRoles }}）</span>
          <button class="link-btn" @click="handleLogout">退出</button>
        </div>
      </div>
      <nav>
        <RouterLink v-for="item in visibleNav" :key="item.path" :to="item.path">{{ item.label }}</RouterLink>
      </nav>
    </header>
    <main>
      <RouterView />
    </main>
  </div>
</template>

<style>
* { box-sizing: border-box; }
body { margin: 0; font-family: 'Segoe UI', system-ui, sans-serif; background: #f5f6f8; color: #333; }
.layout { max-width: 1200px; margin: 0 auto; padding: 24px; }
.login-layout { min-height: 100vh; background: #f5f6f8; }
header { margin-bottom: 24px; }
.head-row { display: flex; justify-content: space-between; align-items: center; gap: 16px; }
header h1 { margin: 0 0 12px; font-size: 1.5rem; }
nav { display: flex; flex-wrap: wrap; gap: 12px; }
nav a { color: #2563eb; text-decoration: none; font-size: 14px; }
nav a.router-link-active { font-weight: bold; text-decoration: underline; }
.user-bar { font-size: 14px; display: flex; gap: 12px; align-items: center; }
.link-btn { background: none; border: none; color: #2563eb; cursor: pointer; padding: 0; }
</style>
