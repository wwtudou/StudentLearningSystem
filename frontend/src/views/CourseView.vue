<!-- 课程与开课计划 -->
<script setup>
import { ref, onMounted } from 'vue'
import { fetchCourses, createCourse, updateCourse, fetchCourseOptions, fetchSemesters, fetchOfferings, createOffering, updateOffering } from '../api/course'
import { fetchCollegeOptions } from '../api/org'
import { fetchTeacherOptions } from '../api/teacher'

const tab = ref('course')
const courses = ref([])
const courseOptions = ref([])
const offerings = ref([])
const semesters = ref([])
const colleges = ref([])
const teachers = ref([])
const courseTotal = ref(0)
const offeringTotal = ref(0)
const page = ref(1)
const msg = ref('')
const natures = ['必修', '选修', '公选']
const statuses = ['草稿', '开放选课', '选课结束', '已结束']
const courseForm = ref({ courseCode: '', courseName: '', credit: 3, hours: 48, collegeCode: '', nature: '必修', status: '启用' })
const offeringForm = ref({ offeringNo: '', courseCode: '', semesterCode: '', teacherNo: '', capacity: 50, schedule: '', status: '草稿' })
const editCourse = ref(null)
const editOffering = ref(null)

async function loadBase() {
  const [c, s, t, co] = await Promise.all([fetchCollegeOptions(), fetchSemesters(), fetchTeacherOptions(), fetchCourseOptions()])
  if (c.data.code === 0) colleges.value = c.data.data
  if (s.data.code === 0) semesters.value = s.data.data
  if (t.data.code === 0) teachers.value = t.data.data
  if (co.data.code === 0) courseOptions.value = co.data.data
}

async function loadCourses() {
  const { data } = await fetchCourses({ page: page.value, pageSize: 10 })
  if (data.code === 0) { courses.value = data.data.list; courseTotal.value = data.data.total }
}

async function loadOfferings() {
  const { data } = await fetchOfferings({ page: page.value, pageSize: 10 })
  if (data.code === 0) { offerings.value = data.data.list; offeringTotal.value = data.data.total }
}

function show(t) { msg.value = t; setTimeout(() => msg.value = '', 3000) }

async function saveCourse() {
  const fn = editCourse.value ? updateCourse(editCourse.value.courseCode, editCourse.value) : createCourse(courseForm.value)
  const { data } = await fn
  if (data.code === 0) { show('课程已保存'); editCourse.value = null; courseForm.value = { courseCode: '', courseName: '', credit: 3, hours: 48, collegeCode: '', nature: '必修', status: '启用' }; loadCourses() }
  else show(data.message)
}

async function saveOffering() {
  const fn = editOffering.value ? updateOffering(editOffering.value.offeringNo, editOffering.value) : createOffering(offeringForm.value)
  const { data } = await fn
  if (data.code === 0) { show('开课计划已保存'); editOffering.value = null; loadOfferings() }
  else show(data.message)
}

onMounted(async () => { await loadBase(); await loadCourses(); await loadOfferings() })
</script>

<template>
  <div>
    <p v-if="msg" class="msg">{{ msg }}</p>
    <div class="row">
      <button :class="{ active: tab==='course' }" @click="tab='course'">课程库</button>
      <button :class="{ active: tab==='offering' }" @click="tab='offering'">开课计划</button>
    </div>

    <template v-if="tab==='course'">
      <section class="card">
        <h2>{{ editCourse ? '编辑课程' : '新增课程' }}</h2>
        <div class="row">
          <template v-if="!editCourse">
            <input v-model="courseForm.courseCode" placeholder="课程编号" />
            <input v-model="courseForm.courseName" placeholder="课程名称" />
            <input v-model.number="courseForm.credit" type="number" step="0.5" placeholder="学分" />
            <input v-model.number="courseForm.hours" type="number" placeholder="学时" />
            <select v-model="courseForm.collegeCode"><option value="">学院</option>
              <option v-for="c in colleges" :key="c.code" :value="c.code">{{ c.name }}</option></select>
            <select v-model="courseForm.nature"><option v-for="n in natures" :key="n">{{ n }}</option></select>
          </template>
          <template v-else>
            <span>{{ editCourse.courseCode }} {{ editCourse.courseName }}</span>
            <input v-model.number="editCourse.credit" type="number" step="0.5" />
            <input v-model.number="editCourse.hours" type="number" />
            <select v-model="editCourse.collegeCode"><option v-for="c in colleges" :key="c.code" :value="c.code">{{ c.name }}</option></select>
            <select v-model="editCourse.nature"><option v-for="n in natures" :key="n">{{ n }}</option></select>
            <select v-model="editCourse.status"><option>启用</option><option>停用</option></select>
          </template>
          <button @click="saveCourse">保存</button>
          <button v-if="editCourse" @click="editCourse=null">取消</button>
        </div>
      </section>
      <section class="card">
        <table>
          <thead><tr><th>编号</th><th>名称</th><th>学分</th><th>学时</th><th>学院</th><th>性质</th><th>状态</th><th>操作</th></tr></thead>
          <tbody>
            <tr v-for="row in courses" :key="row.courseCode">
              <td>{{ row.courseCode }}</td><td>{{ row.courseName }}</td><td>{{ row.credit }}</td><td>{{ row.hours }}</td>
              <td>{{ row.collegeName }}</td><td>{{ row.nature }}</td><td>{{ row.status }}</td>
              <td><button @click="editCourse={...row}">编辑</button></td>
            </tr>
          </tbody>
        </table>
      </section>
    </template>

    <template v-else>
      <section class="card">
        <h2>{{ editOffering ? '编辑开课计划' : '新增开课计划' }}</h2>
        <div class="row">
          <template v-if="!editOffering">
            <input v-model="offeringForm.offeringNo" placeholder="计划编号" />
            <select v-model="offeringForm.courseCode"><option value="">课程</option>
              <option v-for="c in courseOptions" :key="c.code" :value="c.code">{{ c.name }}</option></select>
            <select v-model="offeringForm.semesterCode"><option value="">学期</option>
              <option v-for="s in semesters" :key="s.semesterCode" :value="s.semesterCode">{{ s.semesterName }}</option></select>
            <select v-model="offeringForm.teacherNo"><option value="">教师</option>
              <option v-for="t in teachers" :key="t.code" :value="t.code">{{ t.name }}</option></select>
            <input v-model.number="offeringForm.capacity" type="number" placeholder="容量" />
            <input v-model="offeringForm.schedule" placeholder="上课时间" />
            <select v-model="offeringForm.status"><option v-for="s in statuses" :key="s">{{ s }}</option></select>
          </template>
          <template v-else>
            <span>{{ editOffering.offeringNo }}</span>
            <select v-model="editOffering.teacherNo"><option v-for="t in teachers" :key="t.code" :value="t.code">{{ t.name }}</option></select>
            <input v-model.number="editOffering.capacity" type="number" />
            <input v-model="editOffering.schedule" />
            <select v-model="editOffering.status"><option v-for="s in statuses" :key="s">{{ s }}</option></select>
          </template>
          <button @click="saveOffering">保存</button>
          <button v-if="editOffering" @click="editOffering=null">取消</button>
        </div>
      </section>
      <section class="card">
        <table>
          <thead><tr><th>计划编号</th><th>课程</th><th>学期</th><th>教师</th><th>容量</th><th>已选</th><th>状态</th><th>操作</th></tr></thead>
          <tbody>
            <tr v-for="row in offerings" :key="row.offeringNo">
              <td>{{ row.offeringNo }}</td><td>{{ row.courseName }}</td><td>{{ row.semesterName }}</td>
              <td>{{ row.teacherName }}</td><td>{{ row.capacity }}</td><td>{{ row.enrolledCount }}</td><td>{{ row.status }}</td>
              <td><button @click="editOffering={...row}">编辑</button></td>
            </tr>
          </tbody>
        </table>
      </section>
    </template>
  </div>
</template>

<style scoped>
@import '../styles/common.css';
button.active { background:#1d4ed8; }
</style>
