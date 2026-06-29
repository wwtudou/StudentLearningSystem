<script setup>
import { ref, onMounted } from 'vue'
import api from './api'

const health = ref(null)
const error = ref(null)

onMounted(async () => {
  try {
    const { data } = await api.get('/health')
    health.value = data
  } catch (e) {
    error.value = e.message
  }
})
</script>

<template>
  <div class="page">
    <header>
      <h1>学生专业学习管理信息系统</h1>
      <p>Spring Boot + Vue3 + MySQL 8.0</p>
    </header>

    <section class="card">
      <h2>系统状态</h2>
      <p v-if="error" class="error">后端连接失败：{{ error }}</p>
      <ul v-else-if="health">
        <li>应用：{{ health.application }}</li>
        <li>状态：{{ health.status }}</li>
        <li>数据库：{{ health.database || '—' }}</li>
        <li>数据库连接：{{ health.dbConnected ? '正常' : '失败' }}</li>
      </ul>
      <p v-else>正在检测...</p>
    </section>

    <section class="card">
      <h2>功能模块（待开发）</h2>
      <ul class="modules">
        <li>学生管理</li>
        <li>奖惩管理</li>
        <li>组织管理</li>
        <li>课程与开课计划</li>
        <li>选课 / 成绩</li>
        <li>报表查询</li>
        <li>用户权限</li>
        <li>审计日志</li>
      </ul>
    </section>
  </div>
</template>

<style>
* {
  box-sizing: border-box;
}
body {
  margin: 0;
  font-family: 'Segoe UI', system-ui, sans-serif;
  background: #f5f6f8;
  color: #333;
}
.page {
  max-width: 800px;
  margin: 0 auto;
  padding: 24px;
}
header {
  margin-bottom: 24px;
}
header h1 {
  margin: 0 0 8px;
  font-size: 1.5rem;
}
header p {
  margin: 0;
  color: #666;
}
.card {
  background: #fff;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 16px 20px;
  margin-bottom: 16px;
}
.card h2 {
  margin: 0 0 12px;
  font-size: 1.1rem;
}
.error {
  color: #c0392b;
}
.modules {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px;
  padding-left: 20px;
  margin: 0;
}
</style>
