<!-- 成绩管理 -->
<script setup>
import { ref, onMounted } from 'vue'
import { fetchGrades, saveGrade, submitGrades, arrangeMakeup, arrangeRetake } from '../api/grade'

const list = ref([])
const total = ref(0)
const page = ref(1)
const query = ref({ studentNo: '', offeringNo: '' })
const form = ref({ studentNo: '', offeringNo: '', totalScore: 80, examType: '正常' })
const submitNo = ref('')
const msg = ref('')

async function load() {
  const { data } = await fetchGrades({ ...query.value, page: page.value, pageSize: 10 })
  if (data.code === 0) { list.value = data.data.list; total.value = data.data.total }
}

function show(t) { msg.value = t; setTimeout(() => msg.value = '', 3000) }

async function doSave() {
  const { data } = await saveGrade(form.value)
  if (data.code === 0) { show('成绩已保存'); load() }
  else show(data.message)
}

async function doSubmit() {
  const { data } = await submitGrades(submitNo.value)
  if (data.code === 0) { show('已提交锁定'); load() }
  else show(data.message)
}

async function doMakeup(row) {
  const { data } = await arrangeMakeup({ studentNo: row.studentNo, offeringNo: row.offeringNo })
  if (data.code === 0) { show('已安排补考'); load() }
  else show(data.message)
}

async function doRetake(row) {
  const offeringNo = prompt('请输入重修开课计划编号')
  if (!offeringNo) return
  const { data } = await arrangeRetake({ studentNo: row.studentNo, offeringNo })
  if (data.code === 0) show('重修选课成功')
  else show(data.message)
}

onMounted(load)
</script>

<template>
  <div>
    <p v-if="msg" class="msg">{{ msg }}</p>
    <section class="card">
      <h2>录入成绩</h2>
      <div class="row">
        <input v-model="form.studentNo" placeholder="学号" />
        <input v-model="form.offeringNo" placeholder="计划编号" />
        <input v-model.number="form.totalScore" type="number" min="0" max="100" placeholder="总评" />
        <select v-model="form.examType"><option>正常</option><option>补考</option><option>重修</option></select>
        <button @click="doSave">保存</button>
      </div>
    </section>
    <section class="card">
      <h2>提交锁定</h2>
      <div class="row">
        <input v-model="submitNo" placeholder="开课计划编号" />
        <button @click="doSubmit">提交本班成绩</button>
      </div>
    </section>
    <section class="card">
      <h2>成绩列表</h2>
      <div class="row">
        <input v-model="query.studentNo" placeholder="学号" />
        <input v-model="query.offeringNo" placeholder="计划编号" />
        <button @click="page=1; load()">查询</button>
      </div>
      <table>
        <thead><tr><th>学号</th><th>姓名</th><th>课程</th><th>总评</th><th>类型</th><th>锁定</th><th>操作</th></tr></thead>
        <tbody>
          <tr v-for="row in list" :key="row.studentNo + row.offeringNo">
            <td>{{ row.studentNo }}</td><td>{{ row.studentName }}</td><td>{{ row.courseName }}</td>
            <td>{{ row.totalScore ?? '—' }}</td><td>{{ row.examType }}</td><td>{{ row.locked ? '是' : '否' }}</td>
            <td>
              <button @click="doMakeup(row)">补考</button>
              <button @click="doRetake(row)">重修</button>
            </td>
          </tr>
        </tbody>
      </table>
      <div class="pager"><button :disabled="page<=1" @click="page--; load()">上一页</button><span>共 {{ total }} 条</span>
        <button :disabled="page*10>=total" @click="page++; load()">下一页</button></div>
    </section>
  </div>
</template>

<style scoped>@import '../styles/common.css';</style>
