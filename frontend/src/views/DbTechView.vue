<!-- 高阶数据库技术演示（DB-Tech-02/04/06/07） -->
<script setup>
import { ref, onMounted } from 'vue'
import {
  fetchMajorRank, fetchCumulativeCredit, fetchGradeChangeLogs,
  explainAuditPartition, explainEnrollmentIndex, explainGradeIndex
} from '../api/dbTech'

const tab = ref('rank')
const semesterCode = ref('2025-2026-1')
const rankList = ref([])
const creditList = ref([])
const changeLogs = ref([])
const explainRows = ref([])
const explainTarget = ref('audit')
const explainYear = ref(2024)
const offeringNo = ref('OFF25CS201')
const studentNo = ref('')

async function loadRank() {
  const { data } = await fetchMajorRank(semesterCode.value || undefined)
  if (data.code === 0) rankList.value = data.data
}

async function loadCredit() {
  const { data } = await fetchCumulativeCredit()
  if (data.code === 0) creditList.value = data.data
}

async function loadLogs() {
  const { data } = await fetchGradeChangeLogs(studentNo.value || undefined)
  if (data.code === 0) changeLogs.value = data.data
}

async function loadExplain() {
  let res
  if (explainTarget.value === 'audit') {
    res = await explainAuditPartition(explainYear.value)
  } else if (explainTarget.value === 'grade') {
    res = await explainGradeIndex(offeringNo.value)
  } else {
    res = await explainEnrollmentIndex(offeringNo.value)
  }
  if (res.data.code === 0) explainRows.value = res.data.data
}

onMounted(loadRank)
</script>

<template>
  <div>
    <section class="card">
      <h2>高阶数据库技术演示</h2>
      <p class="hint">选课/退课已走存储过程+触发器；本页展示窗口函数、变更日志与 EXPLAIN 分区/索引。</p>
      <div class="row">
        <button :class="{ active: tab==='rank' }" @click="tab='rank'; loadRank()">专业排名 (Tech-06)</button>
        <button :class="{ active: tab==='credit' }" @click="tab='credit'; loadCredit()">累计学分 (Tech-06)</button>
        <button :class="{ active: tab==='log' }" @click="tab='log'; loadLogs()">变更日志 (Tech-02)</button>
        <button :class="{ active: tab==='explain' }" @click="tab='explain'; loadExplain()">EXPLAIN (Tech-04/07)</button>
      </div>
    </section>

    <section v-if="tab==='rank'" class="card">
      <div class="row">
        <input v-model="semesterCode" placeholder="学期编码" />
        <button @click="loadRank">查询</button>
      </div>
      <table>
        <thead><tr><th>学号</th><th>姓名</th><th>专业</th><th>课程</th><th>总评</th><th>专业排名</th></tr></thead>
        <tbody>
          <tr v-for="r in rankList" :key="r.studentNo + r.courseName">
            <td>{{ r.studentNo }}</td><td>{{ r.studentName }}</td><td>{{ r.majorName }}</td>
            <td>{{ r.courseName }}</td><td>{{ r.totalScore }}</td><td>{{ r.majorRank }}</td>
          </tr>
        </tbody>
      </table>
    </section>

    <section v-if="tab==='credit'" class="card">
      <table>
        <thead><tr><th>学号</th><th>姓名</th><th>学期</th><th>课程</th><th>学分</th><th>总评</th><th>累计学分</th></tr></thead>
        <tbody>
          <tr v-for="(r,i) in creditList" :key="i">
            <td>{{ r.studentNo }}</td><td>{{ r.studentName }}</td><td>{{ r.semesterCode }}</td>
            <td>{{ r.courseName }}</td><td>{{ r.credit }}</td><td>{{ r.totalScore }}</td><td>{{ r.cumulativeCredit }}</td>
          </tr>
        </tbody>
      </table>
    </section>

    <section v-if="tab==='log'" class="card">
      <div class="row">
        <input v-model="studentNo" placeholder="学号筛选" />
        <button @click="loadLogs">查询</button>
      </div>
      <table>
        <thead><tr><th>编号</th><th>学号</th><th>计划</th><th>旧值</th><th>新值</th><th>修改者</th><th>时间</th></tr></thead>
        <tbody>
          <tr v-for="l in changeLogs" :key="l.logNo">
            <td>{{ l.logNo }}</td><td>{{ l.studentNo }}</td><td>{{ l.offeringNo }}</td>
            <td class="json">{{ l.oldValue }}</td><td class="json">{{ l.newValue }}</td>
            <td>{{ l.changedBy }}</td><td>{{ l.changedAt }}</td>
          </tr>
        </tbody>
      </table>
    </section>

    <section v-if="tab==='explain'" class="card">
      <div class="row">
        <select v-model="explainTarget" @change="loadExplain">
          <option value="audit">audit_log 分区 (Tech-07)</option>
          <option value="grade">grade 索引 (Tech-04)</option>
          <option value="enrollment">enrollment 索引 (Tech-04)</option>
        </select>
        <input v-if="explainTarget==='audit'" v-model.number="explainYear" type="number" />
        <input v-else v-model="offeringNo" placeholder="开课计划编号" />
        <button @click="loadExplain">EXPLAIN</button>
      </div>
      <table>
        <thead><tr><th>id</th><th>table</th><th>partitions</th><th>type</th><th>key</th><th>rows</th><th>Extra</th></tr></thead>
        <tbody>
          <tr v-for="(e,i) in explainRows" :key="i">
            <td>{{ e.id }}</td><td>{{ e.tableName }}</td><td>{{ e.partitions || '—' }}</td>
            <td>{{ e.type }}</td><td>{{ e.key || '—' }}</td><td>{{ e.rows }}</td><td>{{ e.extra }}</td>
          </tr>
        </tbody>
      </table>
    </section>
  </div>
</template>

<style scoped>
@import '../styles/common.css';
button.active { background: #1d4ed8; }
.hint { color: #666; font-size: 13px; }
.json { font-size: 11px; max-width: 180px; word-break: break-all; }
</style>
