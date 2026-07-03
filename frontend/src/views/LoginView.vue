<!-- 登录页 -->
<script setup>
import { ref } from "vue"
import { useRouter } from "vue-router"
import { login } from "../api/auth"

const router = useRouter()
const form = ref({ username: "admin", password: "1admin6" })
const error = ref("")
const loading = ref(false)

async function handleLogin() {
  error.value = ""
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
      <div class="login-header">
        <div class="login-icon">SLMS</div>
        <h2>学生专业学习管理系统</h2>
        <p class="login-subtitle">请登录您的账户</p>
      </div>
      <div class="login-body">
        <div class="field">
          <label class="field-label">用户名</label>
          <input class="field-input" v-model="form.username" placeholder="请输入用户名" required />
        </div>
        <div class="field">
          <label class="field-label">密　码</label>
          <input class="field-input" v-model="form.password" type="password" placeholder="请输入密码" required />
        </div>
        <p v-if="error" class="error-msg">{{ error }}</p>
        <button class="login-btn" type="submit" :disabled="loading">{{ loading ? "登录中..." : "登 录" }}</button>
        <p class="hint">提示：admin / 1admin6</p>
      </div>
    </form>
  </div>
</template>

<style scoped>
.login-wrap { display:flex; justify-content:center; align-items:center; min-height:100vh; background:linear-gradient(135deg,#eef2ff,#f0f4ff,#e8ecf4); }
.login-card { background:#fff; padding:0; border-radius:16px; width:400px; box-shadow:0 8px 30px rgba(0,0,0,0.08); overflow:hidden; }
.login-header { text-align:center; padding:36px 32px 24px; background:linear-gradient(135deg,#1a56db,#0ea5e9); color:#fff; }
.login-icon { width:56px;height:56px;margin:0 auto 16px; background:rgba(255,255,255,0.2); border-radius:14px; display:flex; align-items:center; justify-content:center; font-size:1.1rem; font-weight:700; }
.login-header h2 { margin:0 0 6px; font-size:1.2rem; font-weight:600; color:#fff; }
.login-subtitle { margin:0; font-size:0.85rem; opacity:0.8; }
.login-body { padding:28px 32px 32px; display:flex; flex-direction:column; gap:18px; }
.field { display:flex; flex-direction:column; gap:6px; }
.field-label { font-size:0.875rem; font-weight:500; color:#374151; }
.field-input { padding:11px 14px; border:1.5px solid #e2e6ed; border-radius:8px; font-size:0.9rem; color:#374151; outline:none; transition:all 0.3s ease; background:#fafbfc; width:100%; box-sizing:border-box; }
.field-input:focus { border-color:#1a56db; box-shadow:0 0 0 3px rgba(26,86,219,0.15); background:#fff; }
.login-btn { width:100%; padding:12px; background:linear-gradient(135deg,#1a56db,#1341a8); color:#fff; border:none; border-radius:8px; font-size:1rem; font-weight:500; cursor:pointer; transition:all 0.3s ease; box-shadow:0 2px 8px rgba(26,86,219,0.25); letter-spacing:0.1em; }
.login-btn:hover { transform:translateY(-1px); box-shadow:0 4px 14px rgba(26,86,219,0.35); }
.login-btn:disabled { opacity:0.6; cursor:not-allowed; }
.error-msg { color:#dc2626; font-size:0.85rem; margin:0; padding:8px 12px; background:#fef2f2; border-radius:6px; text-align:center; }
.hint { color:#9ca3af; font-size:0.8rem; text-align:center; margin:0; }
</style>
