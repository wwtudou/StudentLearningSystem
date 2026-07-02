<!-- 根布局：顶部导航 + 路由出口 -->
<script setup>
import { ref, onMounted } from 'vue'
import { RouterLink, RouterView, useRouter, useRoute } from 'vue-router'
import { fetchMe, logout } from './api/auth'

const router = useRouter()
const route = useRoute()
const user = ref(null)

async function loadUser() {
  if (route.path === '/login') return
  try {
    const { data } = await fetchMe()
    if (data.code === 0) user.value = data.data
  } catch (_) {
    user.value = null
  }
}

async function handleLogout() {
  await logout()
  user.value = null
  router.push('/login')
}

onMounted(loadUser)
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
          <span>{{ user.realName }}（{{ user.username }}）</span>
          <button class="link-btn" @click="handleLogout">退出</button>
        </div>
      </div>
      <nav>
        <RouterLink to="/">首页</RouterLink>
        <RouterLink to="/students">学生</RouterLink>
        <RouterLink to="/org">院系专业</RouterLink>
        <RouterLink to="/dicts">字典</RouterLink>
        <RouterLink to="/teachers">教师</RouterLink>
        <RouterLink to="/courses">课程</RouterLink>
        <RouterLink to="/rewards">奖惩</RouterLink>
        <RouterLink to="/enrollments">选课</RouterLink>
        <RouterLink to="/grades">成绩</RouterLink>
        <RouterLink to="/reports">报表</RouterLink>
        <RouterLink to="/users">用户</RouterLink>
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
