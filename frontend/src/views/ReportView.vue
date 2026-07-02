<!-- 报表统计 -->
<script setup>
import { ref, onMounted } from 'vue'
import { fetchStudentStats, fetchEnrollmentStats, fetchGradeStats, fetchRewardStats } from '../api/report'

const studentStats = ref([])
const enrollmentStats = ref([])
const gradeStats = ref([])
const rewardStats = ref([])
const groupBy = ref('college')

async function load() {
  const [s, e, g, r] = await Promise.all([
    fetchStudentStats(groupBy.value),
    fetchEnrollmentStats(),
    fetchGradeStats(),
    fetchRewardStats()
  ])
  if (s.data.code === 0) studentStats.value = s.data.data
  if (e.data.code === 0) enrollmentStats.value = e.data.data
  if (g.data.code === 0) gradeStats.value = g.data.data
  if (r.data.code === 0) rewardStats.value = r.data.data
}

onMounted(load)
</script>

<template>
  <div class="print-area">
    <div class="row no-print">
      <select v-model="groupBy" @change="load">
        <option value="college">按学院统计学生</option>
        <option value="major">按专业统计学生</option>
      </select>
      <button @click="window.print()">打印报表</button>
    </div>

    <section class="card">
      <h2>在读学生人数</h2>
      <table>
        <thead><tr><th>分组</th><th>名称</th><th>人数</th></tr></thead>
        <tbody>
          <tr v-for="row in studentStats" :key="row.groupKey">
            <td>{{ row.groupKey }}</td><td>{{ row.groupName }}</td><td>{{ row.count }}</td>
          </tr>
        </tbody>
      </table>
    </section>

    <section class="card">
      <h2>选课人数</h2>
      <table>
        <thead><tr><th>开课计划</th><th>说明</th><th>人数</th></tr></thead>
        <tbody>
          <tr v-for="row in enrollmentStats" :key="row.groupKey">
            <td>{{ row.groupKey }}</td><td>{{ row.groupName }}</td><td>{{ row.count }}</td>
          </tr>
        </tbody>
      </table>
    </section>

    <section class="card">
      <h2>成绩统计</h2>
      <table>
        <thead><tr><th>开课计划</th><th>说明</th><th>人数</th><th>平均分</th><th>及格率%</th></tr></thead>
        <tbody>
          <tr v-for="row in gradeStats" :key="row.groupKey">
            <td>{{ row.groupKey }}</td><td>{{ row.groupName }}</td><td>{{ row.count }}</td>
            <td>{{ row.avgScore ?? '—' }}</td><td>{{ row.passRate ?? '—' }}</td>
          </tr>
        </tbody>
      </table>
    </section>

    <section class="card">
      <h2>奖惩统计</h2>
      <table>
        <thead><tr><th>类型</th><th>次数</th></tr></thead>
        <tbody>
          <tr v-for="row in rewardStats" :key="row.groupKey">
            <td>{{ row.groupName }}</td><td>{{ row.count }}</td>
          </tr>
        </tbody>
      </table>
    </section>
  </div>
</template>

<style scoped>
@import '../styles/common.css';
@media print { .no-print { display:none; } .card { break-inside:avoid; } }
</style>
