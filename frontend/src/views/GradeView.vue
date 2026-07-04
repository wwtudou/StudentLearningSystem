<!-- 成绩：学生查本人；教师按教学班录入 -->
<script setup>
import { ref, computed, watch } from 'vue'
import {
  fetchGrades, fetchMyOfferings, fetchGradeRoster,
  saveGrade, submitGrades, arrangeMakeup, arrangeRetake,
  unlockGrades
} from '../api/grade'
import { fetchTeacherOptions } from '../api/teacher'
import { useAuth } from '../composables/useAuth'

const { writeAllowed, hasRole, user } = useAuth()
const canEditGrade = computed(() => writeAllowed('grade'))
const isStudent = computed(() => hasRole('STUDENT'))
const isTeacher = computed(() => hasRole('TEACHER'))
const teachers = ref([])
const selectedTeacherNo = ref('')
const filteredOfferings = computed(() => {
  if (!selectedTeacherNo.value) return []
  return myClasses.value.filter(c => c.teacherNo === selectedTeacherNo.value)
})

const msg = ref('')
const msgType = ref('ok')

/** 学生 / 管理员列表模式 */
const list = ref([])
const total = ref(0)
const page = ref(1)
const query = ref({ studentNo: '', offeringNo: '' })
/** 成绩编辑弹窗 */
const editGradeDialogVisible = ref(false)
const editGradeForm = ref({ studentNo: '', offeringNo: '', totalScore: null, examType: '正常' })
const dialogEditMsg = ref('')
const unlockConfirmVisible = ref(false)
const unlockConfirmMsg = ref('')
const unlockConfirmAction = ref(null)

function showUnlockConfirm(msg, callback) {
  unlockConfirmMsg.value = msg
  unlockConfirmAction.value = callback
  unlockConfirmVisible.value = true
}

/** 教师模式 */
const myClasses = ref([])
const selectedOfferingNo = ref('')
const roster = ref([])
const rosterLoading = ref(false)

const selectedClass = computed(() =>
  myClasses.value.find(c => c.offeringNo === selectedOfferingNo.value)
)

function show(text, isErr = false) {
  msg.value = text
  msgType.value = isErr ? 'err' : 'ok'
  setTimeout(() => { msg.value = '' }, 3000)
}

async function loadList() {
  const { data } = await fetchGrades({ ...query.value, page: page.value, pageSize: 10 })
  if (data.code === 0) {
    list.value = data.data.list
    total.value = data.data.total
  }
}

async function loadMyClasses() {
  try {
    const { data } = await fetchMyOfferings()
    if (data.code === 0) {
      myClasses.value = data.data
      const stillValid = data.data.some(c => c.offeringNo === selectedOfferingNo.value)
      if (!stillValid) {
        selectedOfferingNo.value = data.data.length === 1 ? data.data[0].offeringNo : ''
      }
    } else {
      show(data.message, true)
    }
  } catch (e) {
    show(e.response?.data?.message || '加载教学班失败', true)
  }
}

async function loadRoster() {
  if (!selectedOfferingNo.value) {
    roster.value = []
    return
  }
  rosterLoading.value = true
  try {
    const { data } = await fetchGradeRoster(selectedOfferingNo.value)
    if (data.code === 0) {
      roster.value = data.data.map(row => ({
        ...row,
        editScore: row.totalScore != null ? Number(row.totalScore) : 80,
        editExamType: row.examType || '正常'
      }))
    } else {
      show(data.message, true)
    }
  } catch (e) {
    show(e.response?.data?.message || '加载名册失败', true)
  } finally {
    rosterLoading.value = false
  }
}

async function saveRow(row) {
  if (row.locked) {
    show('该生成绩已锁定', true)
    return
  }
  try {
    const { data } = await saveGrade({
      studentNo: row.studentNo,
      offeringNo: selectedOfferingNo.value,
      totalScore: row.editScore,
      examType: row.editExamType
    })
    if (data.code === 0) {
      show(`已保存 ${row.studentName} 的成绩`)
      await loadRoster()
    } else {
      show(data.message, true)
    }
  } catch (e) {
    show(e.response?.data?.message || '保存失败', true)
  }
}

async function saveAll() {
  const pending = roster.value.filter(r => !r.locked)
  if (pending.length === 0) {
    show('没有可保存的记录', true)
    return
  }
  if (!confirm(`确定保存本班 ${pending.length} 名学生的成绩？`)) return
  let ok = 0
  for (const row of pending) {
    try {
      const { data } = await saveGrade({
        studentNo: row.studentNo,
        offeringNo: selectedOfferingNo.value,
        totalScore: row.editScore,
        examType: row.editExamType
      })
      if (data.code === 0) ok++
    } catch (_) { /* 继续 */ }
  }
  show(ok === pending.length ? '本班成绩已全部保存' : `已保存 ${ok}/${pending.length} 条`, ok < pending.length)
  await loadRoster()
}

async function loadTeachers() {
  try {
    const { data } = await fetchTeacherOptions()
    if (data.code === 0) teachers.value = data.data
  } catch (_) {}
}

async function unlockStudentRow(studentNo, offeringNo) {
  showUnlockConfirm('确定解锁该生成绩？', async () => {
    try {
      const { data } = await unlockStudentGrade(studentNo, offeringNo)
      if (data.code === 0) {
        show('该生成绩已解锁')
        await loadRoster()
      } else {
        show(data.message, true)
      }
    } catch (e) {
      show(e.response?.data?.message || '解锁失败', true)
    }
  })
}

async function submitClass() {
  if (!selectedOfferingNo.value) return
  if (!confirm('提交后将锁定本班成绩，确定提交？')) return
  const { data } = await submitGrades(selectedOfferingNo.value)
  if (data.code === 0) {
    show('本班成绩已提交锁定')
    await loadRoster()
  } else {
    show(data.message, true)
  }
}

async function unlockClass() {
  if (!selectedOfferingNo.value) return
  showUnlockConfirm('确定解锁本班成绩？解锁后可继续修改。', async () => {
    try {
      const { data } = await unlockGrades(selectedOfferingNo.value)
    if (data.code === 0) {
        show('成绩已解锁')
        await loadRoster()
      } else {
        show(data.message, true)
      }
    } catch (e) {
      show(e.response?.data?.message || '解锁失败', true)
    }
  })
}

async function doMakeup(row) {
  const { data } = await arrangeMakeup({
    studentNo: row.studentNo,
    offeringNo: row.offeringNo || selectedOfferingNo.value
  })
  if (data.code === 0) {
    show('已安排补考')
    await loadRoster()
  } else {
    show(data.message, true)
  }
}

async function doRetake(row) {
  const offeringNo = prompt('请输入重修开课计划编号')
  if (!offeringNo) return
  const { data } = await arrangeRetake({ studentNo: row.studentNo, offeringNo })
  if (data.code === 0) show('重修选课成功')
  else show(data.message, true)
}

function openGradeEdit(row) {
  editGradeForm.value = {
    studentNo: row.studentNo,
    offeringNo: row.offeringNo,
    totalScore: row.totalScore != null ? Number(row.totalScore) : null,
    examType: row.examType || '正常'
  }
  editGradeDialogVisible.value = true
}

async function saveGradeEdit() {
  dialogEditMsg.value = ''       // 新增：清空之前的消息
  try {
    const { data } = await saveGrade({
      studentNo: editGradeForm.value.studentNo,
      offeringNo: editGradeForm.value.offeringNo,
      totalScore: editGradeForm.value.totalScore,
      examType: editGradeForm.value.examType
    })
    if (data.code === 0) {
      dialogEditMsg.value = '成绩已更新'    // 改：show → dialogEditMsg.value
      editGradeDialogVisible.value = false
      await loadList()
    } else {
      dialogEditMsg.value = data.message    // 改：show → dialogEditMsg.value
    }
  } catch (e) {
    dialogEditMsg.value = e.response?.data?.message || '保存失败'  // 改
  }
}

watch(selectedOfferingNo, () => {
  if (isTeacher.value || hasRole('SYS_ADMIN')) loadRoster()
})

watch(
  () => user.value?.roles,
  async (roles) => {
    if (roles?.includes('TEACHER')) {
      await loadMyClasses()
    } else if (roles?.includes('SYS_ADMIN')) {
      await Promise.all([loadTeachers(), loadMyClasses()])
    } else if (roles?.length) {
      if (user.value?.linkedNo) {
        query.value.studentNo = user.value.linkedNo
      }
      await loadList()
    }
  },
  { immediate: true }
)
</script>

<template>
  <div>
    <p v-if="msg" :class="['msg', msgType]">{{ msg }}</p>

    <!-- 教师：按教学班录入 -->
    <template v-if="isTeacher">
      <section class="card">
        <h2>成绩录入</h2>
        <p class="sub">先选择本人授课的开课计划，再对该班学生录入总评成绩。</p>

        <div v-if="myClasses.length === 0" class="empty">暂无授课班级，请确认账号已绑定工号且已分配开课计划</div>

        <div v-else class="toolbar">
          <label class="select-label">
            <span>开课计划</span>
            <select v-model="selectedOfferingNo" class="offering-select">
              <option value="" disabled>— 请选择开课计划 —</option>
              <option
                v-for="c in myClasses"
                :key="c.offeringNo"
                :value="c.offeringNo"
              >
                {{ c.offeringNo }} · {{ c.courseName }}（{{ c.semesterName }}，{{ c.enrolledCount }}/{{ c.capacity }} 人）
              </option>
            </select>
          </label>
        </div>

        <div v-if="selectedClass" class="class-info">
          <span><strong>{{ selectedClass.courseName }}</strong></span>
          <span>{{ selectedClass.semesterName }}</span>
          <span>{{ selectedClass.schedule || '—' }}</span>
          <span class="meta">{{ selectedClass.enrolledCount }}/{{ selectedClass.capacity }} 人 · {{ selectedClass.status }}</span>
        </div>
      </section>

      <section v-if="selectedOfferingNo" class="card">
        <div class="head-row">
          <h2>{{ selectedClass?.courseName || '学生名册' }} — 学生名册</h2>
          <div class="btns" v-if="canEditGrade">
            <button @click="saveAll">保存本班</button>
            <button class="primary" @click="submitClass">提交锁定</button>
            <button v-if="hasRole('SYS_ADMIN')" class="btn-secondary" @click="unlockClass">解锁</button>
          </div>
        </div>
        <p v-if="rosterLoading" class="muted">加载中…</p>
        <table v-else>
          <thead>
            <tr>
              <th>学号</th>
              <th>姓名</th>
              <th>总评</th>
              <th>类型</th>
              <th>锁定</th>
              <th v-if="canEditGrade">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="roster.length === 0">
              <td :colspan="canEditGrade ? 6 : 5" class="empty">该班暂无选课学生</td>
            </tr>
            <tr v-for="row in roster" :key="row.studentNo">
              <td>{{ row.studentNo }}</td>
              <td>{{ row.studentName }}</td>
              <td>
                <input
                  v-if="canEditGrade && !row.locked"
                  v-model.number="row.editScore"
                  type="number"
                  min="0"
                  max="100"
                  class="score-input"
                />
                <span v-else>{{ row.totalScore ?? '—' }}</span>
              </td>
              <td>
                <select v-if="canEditGrade && !row.locked" v-model="row.editExamType">
                  <option>正常</option>
                  <option>补考</option>
                  <option>重修</option>
                </select>
                <span v-else>{{ row.examType || '—' }}</span>
              </td>
              <td>{{ row.locked ? '是' : '否' }}</td>
              <td v-if="canEditGrade">
                <button v-if="!row.locked" class="btn-sm" @click="saveRow(row)">保存</button>
                <button v-if="!row.locked && row.totalScore != null && row.totalScore < 60" class="btn-sm" @click="doMakeup(row)">补考</button>
              </td>
            </tr>
          </tbody>
        </table>
      </section>
    </template>

    <!-- 管理员：按教师查看 -->
    <template v-else-if="hasRole('SYS_ADMIN')">
      <section class="card">
        <h2>成绩管理</h2>
        <p class="sub">先选择教师，再选择该教师所教的开课计划，查看并管理学生成绩。</p>
        <div class="toolbar">
          <label class="select-label">
            <span>教师</span>
            <select v-model="selectedTeacherNo" class="offering-select">
              <option value="">— 请选择教师 —</option>
              <option v-for="t in teachers" :key="t.code" :value="t.code">{{ t.name }}</option>
            </select>
          </label>
          <label v-if="selectedTeacherNo" class="select-label" style="margin-top:8px">
            <span>开课计划</span>
            <select v-model="selectedOfferingNo" class="offering-select">
              <option value="" disabled>— 请选择开课计划 —</option>
              <option v-for="c in filteredOfferings" :key="c.offeringNo" :value="c.offeringNo">
                {{ c.offeringNo }} · {{ c.courseName }}（{{ c.semesterName }}）{{ c.enrolledCount }}/{{ c.capacity }} 人
              </option>
            </select>
          </label>
        </div>
        <div v-if="selectedClass" class="class-info">
          <span><strong>{{ selectedClass.courseName }}</strong></span>
          <span>{{ selectedClass.semesterName }}</span>
          <span>{{ selectedClass.teacherName }}</span>
          <span class="meta">{{ selectedClass.enrolledCount }}/{{ selectedClass.capacity }} 人 · {{ selectedClass.status }}</span>
        </div>
      </section>
      <section v-if="selectedOfferingNo" class="card">
        <div class="head-row">
          <h2>{{ selectedClass?.courseName || '学生名册' }} — 学生名册</h2>
          <div class="btns">
            <button class="btn-secondary" @click="unlockClass">解锁全班</button>
          </div>
        </div>
        <p v-if="rosterLoading" class="muted">加载中...</p>
        <table v-else>
          <thead>
            <tr>
              <th>学号</th><th>姓名</th><th>总评</th><th>类型</th><th>锁定</th><th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="roster.length === 0"><td colspan="6" class="empty">该班暂无选课学生</td></tr>
            <tr v-for="row in roster" :key="row.studentNo">
              <td>{{ row.studentNo }}</td>
              <td>{{ row.studentName }}</td>
              <td>{{ row.totalScore ?? '—' }}</td>
              <td>{{ row.examType || '—' }}</td>
              <td>{{ row.locked ? '是' : '否' }}</td>
              <td>
                <button v-if="row.locked" class="btn-sm" @click="unlockStudentRow(row.studentNo, row.offeringNo)">解锁</button>
                <span v-else class="tag-muted">已解锁</span>
              </td>
            </tr>
          </tbody>
        </table>
      </section>
    </template>
    <!-- 学生 / 管理员：成绩列表 -->
    <template v-else>
      <section class="card">
        <h2>{{ isStudent ? '我的成绩' : '成绩列表' }}</h2>
        <div v-if="!isStudent" class="row">
          <input v-model="query.studentNo" placeholder="学号" />
          <input v-model="query.offeringNo" placeholder="计划编号" />
          <button @click="page=1; loadList()">查询</button>
        </div>
        <table>
          <thead>
            <tr>
              <th>学号</th><th>姓名</th><th>课程</th><th>学期</th>
              <th>总评</th><th>类型</th><th>锁定</th><th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in list" :key="row.studentNo + row.offeringNo">
              <td>{{ row.studentNo }}</td>
              <td>{{ row.studentName }}</td>
              <td>{{ row.courseName }}</td>
              <td>{{ row.semesterName }}</td>
              <td>{{ row.totalScore ?? '—' }}</td>
              <td>{{ row.examType }}</td>
              <td>{{ row.locked ? '是' : '否' }}</td>
             <td>
               <button v-if="isStudent" @click="doRetake(row)">重修</button>
                <template v-if="!isStudent">
                  <button class="btn-sm" @click="openGradeEdit(row)">编辑</button>
                </template>
             </td>
            </tr>
          </tbody>
        </table>
        <div class="pager">
          <button :disabled="page<=1" @click="page--; loadList()">上一页</button>
          <span>共 {{ total }} 条</span>
          <button :disabled="page*10>=total" @click="page++; loadList()">下一页</button>
        </div>
      </section>
    </template>
  </div>
    <!-- 成绩编辑弹窗 -->
    <div v-if="editGradeDialogVisible" class="overlay" @click.self="editGradeDialogVisible=false">
      <div class="dialog">
        <h3>编辑成绩</h3>
         <p v-if="dialogEditMsg" class="dialog-msg">{{ dialogEditMsg }}</p>  
        <div class="edit-info">
          <p>学号：{{ editGradeForm.studentNo }}</p>
          <p>开课计划：{{ editGradeForm.offeringNo }}</p>
        </div>
        <label>总评成绩
          <input v-model.number="editGradeForm.totalScore" type="number" min="0" max="100" class="edit-input" />
        </label>
        <label>考试类型
          <select v-model="editGradeForm.examType" class="edit-input">
            <option>正常</option>
            <option>补考</option>
            <option>重修</option>
          </select>
        </label>
        <div class="btns">
          <button class="primary" @click="saveGradeEdit">保存</button>
          <button class="secondary" @click="editGradeDialogVisible=false">取消</button>
        </div>
      </div>
    </div>
    <!-- 确认解锁弹窗 -->
    <div v-if="unlockConfirmVisible" class="overlay" @click.self="unlockConfirmVisible=false">
      <div class="dialog dialog-sm">
        <h3>确认解锁</h3>
        <p class="delete-warning">{{ unlockConfirmMsg }}</p>
        <div class="btns" style="margin-top:16px">
          <button class="primary" @click="unlockConfirmAction?.(); unlockConfirmVisible=false">确定</button>
          <button class="secondary" @click="unlockConfirmVisible=false">取消</button>
        </div>
      </div>
    </div>
</template>

<style scoped>
@import '../styles/common.css';
.sub { color: #666; font-size: 13px; margin: 0 0 12px; }
.muted { color: #888; }
.empty { text-align: center; color: #888; padding: 12px; }
.toolbar { margin-bottom: 12px; }
.select-label { display: flex; align-items: center; gap: 12px; font-size: 14px; }
.select-label span { white-space: nowrap; font-weight: 500; }
.offering-select {
  min-width: 420px; max-width: 100%; padding: 8px 10px;
  border: 1px solid #cbd5e1; border-radius: 6px; font-size: 14px; background: #fff;
}
.class-info {
  display: flex; flex-wrap: wrap; gap: 16px; padding: 10px 12px;
  background: #f8fafc; border-radius: 6px; font-size: 14px;
}
.class-info .meta { color: #666; }
.head-row { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 12px; }
.btns { display: flex; gap: 8px; }
button.primary { background: #1d4ed8; border-color: #1d4ed8; }
.score-input { width: 72px; padding: 4px 6px; }
.btn-sm { padding: 4px 10px; font-size: 13px; }
.edit-input { padding: 8px 10px; border: 1px solid #ccc; border-radius: 4px; width: 100%; box-sizing: border-box; }
.edit-info p { margin: 4px 0; font-size: 14px; color: #555; }
.msg.ok { background: #ecfdf5; color: #065f46; padding: 10px; border-radius: 4px; margin-bottom: 12px; }
.msg.err { background: #fef2f2; color: #991b1b; padding: 10px; border-radius: 4px; margin-bottom: 12px; }
</style>
