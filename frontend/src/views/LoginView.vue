<!-- 登录页 -->
<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { login } from '../api/auth'

const router = useRouter()
const form = ref({ username: 'admin', password: '1admin6' })
const error = ref('')
const loading = ref(false)

async function handleLogin() {
  error.value = ''
  loading.value = true
  try {
    const { data } = await login(form.value)
    if (data.code === 0) {
      router.push('/')
    } else {
      error.value = data.message
    }
  } catch (e) {
    error.value = '登录失败'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-wrap">
    <form class="login-card" @submit.prevent="handleLogin">
      <h2>系统登录</h2>
      <p class="hint">密码规则：序号+类型，如 admin→1admin6，teacher01→1teacher</p>
      <label>用户名 <input v-model="form.username" required /></label>
      <label>密码 <input v-model="form.password" type="password" required /></label>
      <p v-if="error" class="error">{{ error }}</p>
      <button type="submit" :disabled="loading">{{ loading ? '登录中...' : '登录' }}</button>
    </form>
  </div>
</template>

<style scoped>
.login-wrap { display:flex; justify-content:center; padding-top:80px; }
.login-card { background:#fff; padding:32px; border-radius:8px; width:360px; display:flex; flex-direction:column; gap:12px; box-shadow:0 2px 8px rgba(0,0,0,0.08); }
input { padding:8px; border:1px solid #ccc; border-radius:4px; }
button { padding:10px; background:#2563eb; color:#fff; border:none; border-radius:4px; cursor:pointer; }
.error { color:#dc2626; font-size:14px; }
.hint { color:#666; font-size:13px; margin:0; }
</style>
