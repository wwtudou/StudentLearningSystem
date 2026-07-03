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

        <div v-if="user" class="user-dropdown">
          <div class="dropdown-trigger">
            <span class="avatar">{{ user.realName?.charAt(0) }}</span>
            <span class="uname">{{ user.realName }}</span>
            <span class="arrow">&#x25BE;</span>
          </div>
          <div class="dropdown-menu">
            <div class="dropdown-item muted">{{ displayRoles }}</div>
            <div class="dropdown-divider"></div>
            <div class="dropdown-item logout" @click="handleLogout">退出登录</div>
          </div>
        </div>
      </div>

      <div class="nav-wrap">
      <nav>

        <RouterLink v-for="item in visibleNav" :key="item.path" :to="item.path">{{ item.label }}</RouterLink>

      </nav></div>

    </header>

    <main>

      <RouterView />

    </main>

  </div>

</template>



<style>
* { box-sizing:border-box; }
body { margin:0; font-family:'Inter',-apple-system,BlinkMacSystemFont,'Segoe UI',system-ui,sans-serif; background:#f7f9fc; color:#374151; -webkit-font-smoothing:antialiased; }
.layout { max-width:1200px; margin:0 auto; padding:24px; }
.login-layout { min-height:100vh; background:#f7f9fc; }
header { margin-bottom:0; padding-bottom:0; border-bottom:1px solid #eef1f5; }
.head-row { display:flex; justify-content:space-between; align-items:center; gap:16px; padding:12px 0 16px; }
header h1 { margin:0; font-size:1.4rem; font-weight:700; color:#1a56db; letter-spacing:-0.01em; }
.nav-wrap { border-top:1px solid #eef1f5; padding:10px 0 0; display:flex; justify-content:space-between; align-items:center; }
nav { display:flex; flex-wrap:wrap; gap:2px; }
nav a { padding:8px 16px; color:#6b7280; text-decoration:none; font-size:0.875rem; font-weight:500; border-radius:8px; transition:all 0.3s ease; }
nav a:hover { background:#eef2ff; color:#1a56db; }
nav a.router-link-active { background:linear-gradient(135deg,#1a56db,#1341a8); color:#fff; font-weight:500; box-shadow:0 2px 8px rgba(26,86,219,0.25); }

/* User dropdown */
.user-dropdown { position:relative; cursor:pointer; }
.dropdown-trigger { display:flex; align-items:center; gap:8px; padding:6px 12px; border:1px solid #e2e6ed; border-radius:8px; background:#fff; transition:all 0.3s ease; font-size:0.875rem; }
.dropdown-trigger:hover { border-color:#c8ced9; box-shadow:0 2px 6px rgba(0,0,0,0.06); }
.avatar { width:28px; height:28px; border-radius:50%; background:linear-gradient(135deg,#1a56db,#0ea5e9); color:#fff; display:flex; align-items:center; justify-content:center; font-size:0.75rem; font-weight:600; flex-shrink:0; }
.uname { color:#374151; font-weight:500; max-width:120px; overflow:hidden; text-overflow:ellipsis; white-space:nowrap; }
.arrow { color:#9ca3af; font-size:0.7rem; transition:transform 0.3s ease; }
.user-dropdown:hover .arrow { transform:rotate(180deg); }
.dropdown-menu { display:none; position:absolute; top:calc(100% + 4px); right:0; min-width:180px; background:#fff; border:1px solid #e2e6ed; border-radius:10px; box-shadow:0 8px 24px rgba(0,0,0,0.10); z-index:50; overflow:hidden; }
.user-dropdown:hover .dropdown-menu { display:block; }
.dropdown-item { padding:10px 16px; font-size:0.875rem; color:#374151; cursor:pointer; transition:background 0.2s ease; }
.dropdown-item:hover { background:#f7f9fc; }
.dropdown-item.muted { color:#9ca3af; cursor:default; font-size:0.8125rem; }
.dropdown-item.muted:hover { background:transparent; }
.dropdown-item.logout { color:#ef4444; border-top:1px solid #eef1f5; }
.dropdown-item.logout:hover { background:#fef2f2; }
.dropdown-divider { height:1px; background:#eef1f5; margin:4px 0; }
</style>

