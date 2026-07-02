<!-- 选课管理 -->
<script setup>
import { ref, onMounted } from 'vue'
import { fetchEnrollments, enroll, drop } from '../api/enrollment'
import { fetchOfferings } from '../api/course'

const list = ref([])
const offerings = ref([])
const total = ref(0)
const page = ref(1)
const query = ref({ studentNo: '', offeringNo: '' })
const form = ref({ studentNo: '', offeringNo: '', retake: false })
const msg = ref('')

async function loadOfferings() {
  const { data } = await fetchOfferings({ status: '开放选课', page: 1, pageSize: 100 })
  if (data.code === 0) offerings.value = data.data.list
}

async function load() {
  const { data } = await fetchEnrollments({ ...query.value, page: page.value, pageSize: 10 })
  if (data.code === 0) { list.value = data.data.list; total.value = data.data.total }
}

function show(t) { msg.value = t; setTimeout(() => msg.value = '', 3000) }

async function doEnroll() {
  const { data } = await enroll(form.value)
  if (data.code === 0) { show('选课成功'); load(); loadOfferings() }
  else show(data.message)
}

async function doDrop(row) {
  if (!confirm('确定退课？')) return
  const { data } = await drop(row.studentNo, row.offeringNo)
  if (data.code === 0) { show('退课成功'); load(); loadOfferings() }
  else show(data.message)
}

onMounted(async () => { await loadOfferings(); await load() })
</script>

<template>
  <div>
    <p v-if="msg" class="msg">{{ msg }}</p>
    <section class="card">
      <h2>选课</h2>
      <div class="row">
        <input v-model="form.studentNo" placeholder="学号" />
        <select v-model="form.offeringNo">
          <option value="">选择开课计划</option>
          <option v-for="o in offerings" :key="o.offeringNo" :value="o.offeringNo">
            {{ o.offeringNo }} - {{ o.courseName }} ({{ o.enrolledCount }}/{{ o.capacity }})
          </option>
        </select>
        <label><input v-model="form.retake" type="checkbox" /> 重修</label>
        <button @click="doEnroll">提交选课</button>
      </div>
    </section>
    <section class="card">
      <h2>选课记录</h2>
      <div class="row">
        <input v-model="query.studentNo" placeholder="学号" />
        <input v-model="query.offeringNo" placeholder="计划编号" />
        <button @click="page=1; load()">查询</button>
      </div>
      <table>
        <thead><tr><th>学号</th><th>姓名</th><th>计划</th><th>课程</th><th>学期</th><th>重修</th><th>操作</th></tr></thead>
        <tbody>
          <tr v-for="row in list" :key="row.studentNo + row.offeringNo">
            <td>{{ row.studentNo }}</td><td>{{ row.studentName }}</td><td>{{ row.offeringNo }}</td>
            <td>{{ row.courseName }}</td><td>{{ row.semesterName }}</td><td>{{ row.retake ? '是' : '否' }}</td>
            <td><button @click="doDrop(row)">退课</button></td>
          </tr>
        </tbody>
      </table>
      <div class="pager"><button :disabled="page<=1" @click="page--; load()">上一页</button><span>共 {{ total }} 条</span>
        <button :disabled="page*10>=total" @click="page++; load()">下一页</button></div>
    </section>
  </div>
</template>

<style scoped>@import '../styles/common.css';</style>
