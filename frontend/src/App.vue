<!-- 根布局：顶部导航 + 路由出口 -->
<script setup>
import { RouterLink, RouterView } from 'vue-router'
</script>

<template>
  <div class="layout">
    <header>
      <h1>学生专业学习管理信息系统</h1>
      <nav>
        <RouterLink to="/">首页</RouterLink>
        <RouterLink to="/students">学生管理</RouterLink>
      </nav>
    </header>
    <main>
      <!-- 子页面在此渲染 -->
      <RouterView />
    </main>
  </div>
</template>

<style>
* { box-sizing: border-box; }
body { margin: 0; font-family: 'Segoe UI', system-ui, sans-serif; background: #f5f6f8; color: #333; }
.layout { max-width: 1100px; margin: 0 auto; padding: 24px; }
header { margin-bottom: 24px; }
header h1 { margin: 0 0 12px; font-size: 1.5rem; }
nav { display: flex; gap: 16px; }
nav a { color: #2563eb; text-decoration: none; }
nav a.router-link-active { font-weight: bold; text-decoration: underline; }
</style>
