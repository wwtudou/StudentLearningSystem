<!-- 用户管理 -->
<script setup>
import { ref, onMounted } from 'vue'
import { fetchUsers, createUser, updateUser, resetPassword, fetchRoleOptions } from '../api/user'

const list = ref([])
const roles = ref([])
const total = ref(0)
const page = ref(1)
const form = ref({ username: '', password: '', realName: '', status: '启用', roles: [] })
const edit = ref(null)
const pwdUser = ref('')
const newPwd = ref('')
const msg = ref('')

async function loadRoles() {
  const { data } = await fetchRoleOptions()
  if (data.code === 0) roles.value = data.data
}

async function load() {
  const { data } = await fetchUsers({ page: page.value, pageSize: 10 })
  if (data.code === 0) { list.value = data.data.list; total.value = data.data.total }
}

function show(t) { msg.value = t; setTimeout(() => msg.value = '', 3000) }

async function submit() {
  const payload = edit.value || form.value
  const fn = edit.value ? updateUser(edit.value.username, payload) : createUser(payload)
  const { data } = await fn
  if (data.code === 0) { show('保存成功'); edit.value = null; form.value = { username: '', password: '', realName: '', status: '启用', roles: [] }; load() }
  else show(data.message)
}

async function doReset() {
  const { data } = await resetPassword(pwdUser.value, newPwd.value)
  if (data.code === 0) { show('密码已重置'); pwdUser.value = ''; newPwd.value = '' }
  else show(data.message)
}

function toggleRole(target, code) {
  const idx = target.roles.indexOf(code)
  if (idx >= 0) target.roles.splice(idx, 1)
  else target.roles.push(code)
}

onMounted(async () => { await loadRoles(); await load() })
</script>

<template>
  <div>
    <p v-if="msg" class="msg">{{ msg }}</p>
    <section class="card">
      <h2>{{ edit ? '编辑用户' : '新增用户' }}</h2>
      <div class="row">
        <input v-model="(edit||form).username" :disabled="!!edit" placeholder="用户名" />
        <input v-if="!edit" v-model="form.password" type="password" placeholder="密码" />
        <input v-model="(edit||form).realName" placeholder="真实姓名" />
        <select v-if="edit" v-model="edit.status"><option>启用</option><option>停用</option></select>
        <span v-for="r in roles" :key="r.code" class="role">
          <label><input type="checkbox" :checked="(edit||form).roles.includes(r.code)"
            @change="toggleRole(edit||form, r.code)" /> {{ r.name }}</label>
        </span>
        <button @click="submit">保存</button>
        <button v-if="edit" @click="edit=null">取消</button>
      </div>
    </section>
    <section class="card">
      <h2>重置密码</h2>
      <div class="row">
        <input v-model="pwdUser" placeholder="用户名" />
        <input v-model="newPwd" type="password" placeholder="新密码" />
        <button @click="doReset">重置</button>
      </div>
    </section>
    <section class="card">
      <table>
        <thead><tr><th>用户名</th><th>姓名</th><th>状态</th><th>角色</th><th>操作</th></tr></thead>
        <tbody>
          <tr v-for="row in list" :key="row.username">
            <td>{{ row.username }}</td><td>{{ row.realName }}</td><td>{{ row.status }}</td>
            <td>{{ row.roles?.join(', ') }}</td>
            <td><button @click="edit={...row, roles:[...(row.roles||[])]}">编辑</button></td>
          </tr>
        </tbody>
      </table>
      <div class="pager"><button :disabled="page<=1" @click="page--; load()">上一页</button><span>共 {{ total }} 条</span>
        <button :disabled="page*10>=total" @click="page++; load()">下一页</button></div>
    </section>
  </div>
</template>

<style scoped>
@import '../styles/common.css';
.role { font-size:13px; margin-right:8px; }
</style>
