<!-- 首页：检测后端与数据库连接状态 -->
<script setup>
import { ref, onMounted } from 'vue'
import api from '../api'

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
</template>

<style scoped>
.card {
  background: #fff;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 16px 20px;
}
.error { color: #c0392b; }
</style>
