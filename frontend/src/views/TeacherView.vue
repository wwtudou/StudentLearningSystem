<!-- 教师管理 -->
<script setup>
import { ref, onMounted } from 'vue'
import { fetchTeachers, createTeacher, updateTeacher } from '../api/teacher'
import { fetchCollegeOptions } from '../api/org'

const list = ref([])
const total = ref(0)
const page = ref(1)
const colleges = ref([])
const query = ref({ teacherNo: '', name: '', collegeCode: '' })
const form = ref({ teacherNo: '', name: '', collegeCode: '', status: '在职' })
const editing = ref(false)
const msg = ref('')

async function loadColleges() {
  const { data } = await fetchCollegeOptions()
  if (data.code === 0) colleges.value = data.data
}

async function load() {
  const { data } = await fetchTeachers({ ...query.value, page: page.value, pageSize: 10 })
  if (data.code === 0) { list.value = data.data.list; total.value = data.data.total }
}

function show(t) { msg.value = t; setTimeout(() => msg.value = '', 3000) }

async function submit() {
  const fn = editing.value ? updateTeacher(form.value.teacherNo, form.value) : createTeacher(form.value)
  const { data } = await fn
  if (data.code === 0) { show('保存成功'); form.value = { teacherNo: '', name: '', collegeCode: '', status: '在职' }; editing.value = false; load() }
  else show(data.message)
}

function openEdit(row) { editing.value = true; form.value = { ...row } }

onMounted(async () => { await loadColleges(); await load() })
</script>

<template>
  <div>
    <p v-if="msg" class="msg">{{ msg }}</p>
    <section class="card">
      <h2>查询</h2>
      <div class="row">
        <input v-model="query.teacherNo" placeholder="工号" />
        <input v-model="query.name" placeholder="姓名" />
        <select v-model="query.collegeCode"><option value="">全部学院</option>
          <option v-for="c in colleges" :key="c.code" :value="c.code">{{ c.name }}</option></select>
        <button @click="page=1; load()">查询</button>
      </div>
    </section>
    <section class="card">
      <h2>{{ editing ? '编辑教师' : '新增教师' }}</h2>
      <div class="row">
        <input v-model="form.teacherNo" :disabled="editing" placeholder="工号" />
        <input v-model="form.name" placeholder="姓名" />
        <select v-model="form.collegeCode"><option value="">学院</option>
          <option v-for="c in colleges" :key="c.code" :value="c.code">{{ c.name }}</option></select>
        <select v-if="editing" v-model="form.status"><option>在职</option><option>停用</option></select>
        <button @click="submit">保存</button>
        <button v-if="editing" @click="editing=false">取消</button>
      </div>
    </section>
    <section class="card">
      <table>
        <thead><tr><th>工号</th><th>姓名</th><th>学院</th><th>状态</th><th>操作</th></tr></thead>
        <tbody>
          <tr v-for="row in list" :key="row.teacherNo">
            <td>{{ row.teacherNo }}</td><td>{{ row.name }}</td><td>{{ row.collegeName }}</td><td>{{ row.status }}</td>
            <td><button @click="openEdit(row)">编辑</button></td>
          </tr>
        </tbody>
      </table>
      <div class="pager"><button :disabled="page<=1" @click="page--; load()">上一页</button><span>共 {{ total }} 条</span>
        <button :disabled="page*10>=total" @click="page++; load()">下一页</button></div>
    </section>
  </div>
</template>

<style scoped>@import '../styles/common.css';</style>
