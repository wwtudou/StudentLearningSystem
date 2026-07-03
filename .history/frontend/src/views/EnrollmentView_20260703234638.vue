<!-- 选课：上方可选课程，下方已选课程 -->
<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { fetchEnrollments, enroll, drop } from '../api/enrollment'
import { fetchOfferings } from '../api/course'
import { useAuth } from '../composables/useAuth'

const { writeAllowed, user, hasRole } = useAuth()
const canEnroll = computed(() => writeAllowed('enrollment'))
const isStudent = computed(() => hasRole('STUDENT'))
const busy = ref(false)

const offerings = ref([])
const enrolledList = ref([])
const enrolledTotal = ref(0)
const enrolledPage = ref(1)
const queryStudentNo = ref('')
const msg = ref('')
const msgType = ref('ok')
const loadingOfferings = ref(false)
const loadingEnrolled = ref(false)

const currentStudentNo = computed(() => {
  if (isStudent.value && user.value?.linkedNo) return user.value.linkedNo
  return queryStudentNo.value.trim()
})

const enrolledOfferingNos = computed(() =>
  new Set(enrolledList.value.map(e => e.offeringNo))
)

/** 可选课：开放选课；学生视角排除已选计划 */
const availableOfferings = computed(() => {
  if (!canEnroll.value || !currentStudentNo.value) {
    return offerings.value
  }
  return offerings.value.filter(o => !enrolledOfferingNos.value.has(o.offeringNo))
})

function show(text, isErr = false) {
  msg.value = text
  msgType.value = isErr ? 'err' : 'ok'
  setTimeout(() => { msg.value = '' }, 3000)
}

async function loadOfferings() {
  loadingOfferings.value = true
  try {
    const { data } = await fetchOfferings({ status: '开放选课', page: 1, pageSize: 100 })
    if (data.code === 0) offerings.value = data.data.list
  } finally {
    loadingOfferings.value = false
  }
}

async function loadEnrolled() {
  loadingEnrolled.value = true
  try {
    const pageSize = isStudent.value ? 100 : 20
    const params = {
      page: enrolledPage.value,
      pageSize
    }
    if (currentStudentNo.value) params.studentNo = currentStudentNo.value
    const { data } = await fetchEnrollments(params)
    if (data.code === 0) {
      enrolledList.value = data.data.list
      enrolledTotal.value = data.data.total
    }
  } finally {
    loadingEnrolled.value = false
  }
}

async function refreshAll() {
  await Promise.all([loadOfferings(), loadEnrolled()])
}

function canDrop(row) {
  const s = row.offeringStatus
  if (!s) return true
  if (s === '已结束' || s === '草稿') return false
  return true
}

async function doEnroll(row) {
  const studentNo = currentStudentNo.value
  if (!studentNo) {
    show('未绑定学号，请重新登录或联系管理员', true)
    return
  }
  if (!confirm(`确定选修「${row.courseName}」（${row.semesterName}）？`)) return
  busy.value = true
  try {
    const { data } = await enroll({ studentNo, offeringNo: row.offeringNo, retake: false })
    if (data.code === 0) {
      show(data.message || '选课成功')
      await refreshAll()
    } else {
      show(data.message || '选课失败', true)
    }
  } catch (e) {
    show(e.response?.data?.message || '选课失败', true)
  } finally {
    busy.value = false
  }
}

async function doDrop(row) {
  if (!canDrop(row)) {
    show(`「${row.courseName}」状态为「${row.offeringStatus || '未知'}」，不可退课`, true)
    return
  }
  if (!confirm(`确定退选「${row.courseName}」？`)) return
  busy.value = true
  try {
    const { data } = await drop(row.studentNo, row.offeringNo)
    if (data.code === 0) {
      show(data.message || '退课成功')
      await refreshAll()
    } else {
      show(data.message || '退课失败', true)
    }
  } catch (e) {
    show(e.response?.data?.message || '退课失败', true)
  } finally {
    busy.value = false
  }
}

function isFull(row) {
  return row.enrolledCount != null && row.capacity != null && row.enrolledCount >= row.capacity
}

function searchEnrolled() {
  enrolledPage.value = 1
  loadEnrolled()
}

onMounted(async () => {
  if (user.value?.linkedNo) {
    queryStudentNo.value = user.value.linkedNo
  }
  await refreshAll()
})

watch(() => user.value?.linkedNo, (no) => {
  if (no && isStudent.value) {
    queryStudentNo.value = no
    refreshAll()
  }
})
</script>

<template>
  <div>
    <p v-if="msg" :class="['msg', msgType]">{{ msg }}</p>

    <section v-if="isStudent && user" class="card hint-bar">
      <span>当前学生：<strong>{{ user.realName }}</strong>（学号 {{ user.linkedNo }}）</span>
    </section>

    <section class="card">
      <h2>可选课程</h2>
      <p class="sub">以下为当前「开放选课」的开课计划；选课后将出现在下方已选列表。</p>
      <p v-if="loadingOfferings" class="muted">加载中…</p>
      <table v-else>
        <thead>
          <tr>
            <th>课程</th>
            <th>计划编号</th>
            <th>学期</th>
            <th>授课教师</th>
            <th>上课时间</th>
            <th>已选/容量</th>
            <th v-if="isStudent">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="availableOfferings.length === 0">
            <td :colspan="isStudent ? 7 : 6" class="empty">暂无可选课程（可能已全部选完或当前学期未开放选课）</td>
          </tr>
          <tr v-for="o in availableOfferings" :key="o.offeringNo">
            <td>{{ o.courseName }}</td>
            <td>{{ o.offeringNo }}</td>
            <td>{{ o.semesterName }}</td>
            <td>{{ o.teacherName }}</td>
            <td>{{ o.schedule || '—' }}</td>
            <td>{{ o.enrolledCount }}/{{ o.capacity }}</td>
            <td v-if="isStudent">
              <button
                v-if="!isFull(o)"
                class="btn-sm"
                :disabled="busy"
                @click="doEnroll(o)"
              >选课</button>
              <span v-else class="tag-full">已满</span>
            </td>
          </tr>
        </tbody>
      </table>
    </section>

    <section class="card">
      <h2>{{ isStudent ? '我的已选课程' : '已选课程' }}</h2>
      <div v-if="!isStudent" class="row">
        
          <input v-model="queryStudentNo" placeholder="输入学号查询" />
       
        <button @click="searchEnrolled">查询</button>
        <button class="btn-secondary" @click="queryStudentNo = ''; searchEnrolled()">全部</button>
      </div>
      <p v-if="loadingEnrolled" class="muted">加载中…</p>
      <table v-else>
        <thead>
          <tr>
            <th v-if="!isStudent">学号</th>
            <th v-if="!isStudent">姓名</th>
            <th>课程</th>
            <th>计划编号</th>
            <th>学期</th>
            <th>授课教师</th>
            <th>选课时间</th>
            <th>计划状态</th>
            <th>重修</th>
            <th v-if="isStudent">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="enrolledList.length === 0">
            <td :colspan="isStudent ? 8 : 9" class="empty">暂无选课记录</td>
          </tr>
          <tr v-for="row in enrolledList" :key="row.studentNo + row.offeringNo">
            <td v-if="!isStudent">{{ row.studentNo }}</td>
            <td v-if="!isStudent">{{ row.studentName }}</td>
            <td>{{ row.courseName }}</td>
            <td>{{ row.offeringNo }}</td>
            <td>{{ row.semesterName }}</td>
            <td>{{ row.teacherName }}</td>
            <td>{{ row.enrollTime }}</td>
            <td>{{ row.offeringStatus }}</td>
            <td>{{ row.retake ? '是' : '否' }}</td>
            <td v-if="isStudent">
              <button
                v-if="canDrop(row)"
                class="btn-sm btn-danger"
                :disabled="busy"
                @click="doDrop(row)"
              >退课</button>
              <span v-else class="tag-muted">不可退</span>
            </td>
          </tr>
        </tbody>
      </table>
      <div v-if="!isStudent && enrolledTotal > 20" class="pager">
        <button :disabled="enrolledPage <= 1" @click="enrolledPage--; loadEnrolled()">上一页</button>
        <span>第 {{ enrolledPage }} 页，共 {{ enrolledTotal }} 条</span>
        <button :disabled="enrolledPage * 20 >= enrolledTotal" @click="enrolledPage++; loadEnrolled()">下一页</button>
      </div>
    </section>
  </div>
</template>

<style scoped>
@import '../styles/common.css';
.sub { color: #666; font-size: 13px; margin: 0 0 12px; }
.hint-bar { padding: 10px 16px; font-size: 14px; background: #eff6ff; border-color: #bfdbfe; }
.muted { color: #888; font-size: 14px; }
.empty { text-align: center; color: #888; }
.tag-full { font-size: 12px; color: #991b1b; }
.tag-muted { font-size: 12px; color: #888; }
.btn-sm { padding: 4px 10px; font-size: 13px; }
.btn-danger { background: #dc2626; border-color: #dc2626; }
.btn-secondary { background: #f3f4f6; color: #333; border-color: #ccc; }
.msg.ok { background: #ecfdf5; color: #065f46; }
.msg.err { background: #fef2f2; color: #991b1b; }
label { display: flex; flex-direction: column; gap: 4px; font-size: 13px; }
</style>
