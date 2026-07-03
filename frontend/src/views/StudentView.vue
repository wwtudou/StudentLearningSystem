<!-- 学生信息管理页面：查询、列表、新增、编辑、删除 -->
<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import {
  fetchStudents, createStudent, updateStudent, deleteStudent,
  fetchColleges, fetchMajors
} from '../api/student'
import { useAuth } from '../composables/useAuth'

const { writeAllowed } = useAuth()
const canEditStudent = writeAllowed('student')
const query = reactive({
  studentNo: '',
  name: '',
  collegeCode: '',
  majorCode: '',
  studentStatus: ''
})
// 分页与列表状态
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const list = ref([])
const loading = ref(false)
const message = ref('')
const messageType = ref('')

// 下拉选项数据
const colleges = ref([])
const queryMajors = ref([])   // 查询区专业下拉
const formMajors = ref([])    // 表单区专业下拉
const statusOptions = ['在读', '休学', '毕业', '退学']
const genderOptions = ['男', '女']

const currentYear = new Date().getFullYear()
const yearOptions = Array.from({ length: currentYear - 2000 + 5 }, (_, i) => 2000 + i)
const ageOptions = Array.from({ length: 36 }, (_, i) => 15 + i)

// 新增/编辑弹窗
const dialogVisible = ref(false)
const isEdit = ref(false)
// 对话框内消息
const dialogMessage = ref('')
const dialogMessageType = ref('ok')
const form = reactive({
  studentNo: '',
  name: '',
  collegeCode: '',
  majorCode: '',
  age: 18,
  gender: '男',
  idCard: '',
  enrollYear: new Date().getFullYear(),
  studentStatus: '在读'
})

/** 顶部提示消息，3 秒后自动消失 */
function showMsg(text, type = 'ok') {  message.value = text
  messageType.value = type
  setTimeout(() => { message.value = '' }, 3000)
}

/** 对话框内消息（不自动消失） */
function showDialogMsg(text, type) {
  dialogMessage.value = text
  if (type) dialogMessageType.value = type
}

/** 加载学院下拉 */
async function loadColleges() {
  const { data } = await fetchColleges()
  if (data.code === 0) colleges.value = data.data
}

/** 加载查询区专业下拉 */
async function loadQueryMajors(collegeCode) {
  const { data } = await fetchMajors(collegeCode || undefined)
  if (data.code === 0) queryMajors.value = data.data
}

/** 加载表单区专业下拉 */
async function loadFormMajors(collegeCode) {
  const { data } = await fetchMajors(collegeCode || undefined)
  if (data.code === 0) formMajors.value = data.data
}

/** 分页查询学生列表 */
async function loadList() {
  loading.value = true
  try {
    const { data } = await fetchStudents({
      ...query,
      page: page.value,
      pageSize: pageSize.value
    })
    if (data.code === 0) {
      list.value = data.data.list
      total.value = data.data.total
    } else {
      showMsg(data.message, 'err')
    }
  } catch (e) {
    showMsg('加载失败', 'err')
  } finally {
    loading.value = false
  }
}

/** 重置查询条件 */
function resetQuery() {
  Object.assign(query, { studentNo: '', name: '', collegeCode: '', majorCode: '', studentStatus: '' })
  page.value = 1
  loadList()
}

/** 打开新增弹窗 */
function openCreate() {
  isEdit.value = false
  Object.assign(form, {
    studentNo: '', name: '', collegeCode: '', majorCode: '',
    age: 18, gender: '男', idCard: '',
    enrollYear: new Date().getFullYear(), studentStatus: '在读'
  })
  loadFormMajors('')
  dialogMessage.value = ''
  dialogVisible.value = true
}

/** 打开编辑弹窗（身份证留空表示不修改） */
function openEdit(row) {
  isEdit.value = true
  Object.assign(form, {
    studentNo: row.studentNo,
    name: row.name,
    collegeCode: row.collegeCode,
    majorCode: row.majorCode,
    age: row.age,
    gender: row.gender,
    idCard: '',
    enrollYear: row.enrollYear,
    studentStatus: row.studentStatus
  })
  loadFormMajors(row.collegeCode)
  dialogMessage.value = ''
  dialogVisible.value = true
}

/** 提交新增或修改 */
async function submitForm() {
  dialogMessage.value = ''
  try {
    const payload = { ...form }
    let res
    if (isEdit.value) {
      if (!payload.idCard) delete payload.idCard
      res = await updateStudent(form.studentNo, payload)
    } else {
      res = await createStudent(payload)
    }
    const { data } = res
    if (data.code === 0) {
      if (isEdit.value) {
        dialogVisible.value = false
        showMsg(data.message || '操作成功')
      } else {
        showDialogMsg(data.message || '操作成功', 'ok')
        form.studentNo = ''
        form.name = ''
        form.collegeCode = ''
        form.majorCode = ''
        form.age = 18
        form.gender = '男'
        form.idCard = ''
        form.enrollYear = new Date().getFullYear()
        form.studentStatus = '在读'
      }
      loadList()
    } else {
      showDialogMsg(data.message)
    }
  } catch (e) {
    showDialogMsg('操作失败')
  }
}

/** 逻辑删除学生 */
async function handleDelete(row) {
  if (!confirm(`确定删除学生 ${row.name}（${row.studentNo}）？`)) return
  try {
    const { data } = await deleteStudent(row.studentNo)
    if (data.code === 0) {
      showMsg('删除成功')
      loadList()
    } else {
      showMsg(data.message, 'err')
    }
  } catch (e) {
    showMsg('删除失败', 'err')
  }
}

function prevPage() {
  if (page.value > 1) { page.value--; loadList() }
}

function nextPage() {
  if (page.value * pageSize.value < total.value) { page.value++; loadList() }
}

// 切换学院时联动刷新专业下拉
watch(() => query.collegeCode, (val) => {
  query.majorCode = ''
  loadQueryMajors(val)
})

watch(() => form.collegeCode, (val) => {
  form.majorCode = ''
  loadFormMajors(val)
})

// 页面加载时初始化数据
onMounted(async () => {
  await loadColleges()
  await loadQueryMajors('')
  await loadList()
})
</script>

<template>
  <div>
    <div v-if="message" :class="['msg', messageType]">{{ message }}</div>

    <section class="card">
      <h2>查询条件</h2>
      <div class="form-row">
        <label>学号 <input v-model="query.studentNo" placeholder="模糊查询" /></label>
        <label>姓名 <input v-model="query.name" placeholder="模糊查询" /></label>
        <label>学院
          <select v-model="query.collegeCode">
            <option value="">全部</option>
            <option v-for="c in colleges" :key="c.code" :value="c.code">{{ c.name }}</option>
          </select>
        </label>
        <label>专业
          <select v-model="query.majorCode">
            <option value="">全部</option>
            <option v-for="m in queryMajors" :key="m.code" :value="m.code">{{ m.name }}</option>
          </select>
        </label>
        <label>学籍状态
          <select v-model="query.studentStatus">
            <option value="">全部</option>
            <option v-for="s in statusOptions" :key="s" :value="s">{{ s }}</option>
          </select>
        </label>
      </div>
      <div class="btns">
        <button @click="page=1; loadList()">查询</button>
        <button class="secondary" @click="resetQuery">重置</button>
        <button v-if="canEditStudent" class="primary" @click="openCreate">新增学生</button>
      </div>
    </section>

    <section class="card">
      <h2>学生列表</h2>
      <p v-if="loading">加载中...</p>
      <table v-else>
        <thead>
          <tr>
            <th>学号</th><th>姓名</th><th>学院</th><th>专业</th>
            <th>年龄</th><th>性别</th><th>身份证</th>
            <th>入学年份</th><th>学籍状态</th><th v-if="canEditStudent">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="list.length === 0"><td colspan="10" class="empty">暂无数据</td></tr>
          <tr v-for="row in list" :key="row.studentNo">
            <td>{{ row.studentNo }}</td>
            <td>{{ row.name }}</td>
            <td>{{ row.collegeName }}</td>
            <td>{{ row.majorName }}</td>
            <td>{{ row.age }}</td>
            <td>{{ row.gender }}</td>
            <td>{{ row.idCardMasked }}</td>
            <td>{{ row.enrollYear }}</td>
            <td>{{ row.studentStatus }}</td>
            <td v-if="canEditStudent" class="actions">
              <button @click="openEdit(row)">编辑</button>
              <button class="danger" @click="handleDelete(row)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
      <div class="pager">
        <button :disabled="page<=1" @click="prevPage">上一页</button>
        <span>第 {{ page }} 页，共 {{ total }} 条</span>
        <button :disabled="page*pageSize>=total" @click="nextPage">下一页</button>
      </div>
    </section>

    <div v-if="dialogVisible" class="overlay" @click.self="dialogVisible=false">
      <div class="dialog">
        <h3>{{ isEdit ? '编辑学生' : '新增学生' }}</h3>
        <div v-if="dialogMessage" :class="['dialog-msg', 'msg-' + dialogMessageType]">{{ dialogMessage }}</div>
        <div class="form-grid">
          <label>学号 * <input v-model="form.studentNo" :disabled="isEdit" /></label>
          <label>姓名 * <input v-model="form.name" /></label>
          <label>学院 *
            <select v-model="form.collegeCode">
              <option value="">请选择</option>
              <option v-for="c in colleges" :key="c.code" :value="c.code">{{ c.name }}</option>
            </select>
          </label>
          <label>专业 *
            <select v-model="form.majorCode">
              <option value="">请选择</option>
              <option v-for="m in formMajors" :key="m.code" :value="m.code">{{ m.name }}</option>
            </select>
          </label>
          <label>性别 *
            <select v-model="form.gender">
              <option v-for="g in genderOptions" :key="g" :value="g">{{ g }}</option>
            </select>
          </label>
          <label>身份证号 {{ isEdit ? '' : '*' }}
            <input v-model="form.idCard" :placeholder="isEdit ? '不修改请留空' : '18位'" />
          </label>
          <label>学籍状态 *
            <select v-model="form.studentStatus">
              <option v-for="s in statusOptions" :key="s" :value="s">{{ s }}</option>
            </select>
          </label>
          <label>年龄 *
            <select v-model.number="form.age" size="1" @focus="$event.target.size=5" @blur="$event.target.size=1" @change="$event.target.size=1">
              <option v-for="a in ageOptions" :key="a" :value="a">{{ a }}</option>
            </select>
          </label>
          <label>入学年份 *
            <select v-model.number="form.enrollYear" size="1" @focus="$event.target.size=5" @blur="$event.target.size=1" @change="$event.target.size=1">
              <option v-for="y in yearOptions" :key="y" :value="y">{{ y }}</option>
            </select>
          </label>
        </div>
        <div class="btns">
          <button class="primary" @click="submitForm">保存</button>
          <button class="secondary" @click="dialogVisible=false">取消</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.card { background:#fff; border:1px solid #e0e0e0; border-radius:8px; padding:16px 20px; margin-bottom:16px; }
.form-row { display:flex; flex-wrap:wrap; gap:12px; margin-bottom:12px; }
.form-row label { display:flex; flex-direction:column; font-size:13px; gap:4px; }
input, select { padding:6px 8px; border:1px solid #ccc; border-radius:4px; min-width:120px; }
.btns { display:flex; gap:8px; }
button { padding:6px 14px; border:1px solid #ccc; border-radius:4px; cursor:pointer; background:#fff; }
button.primary { background:#2563eb; color:#fff; border-color:#2563eb; }
button.secondary { background:#f3f4f6; }
button.danger { color:#dc2626; border-color:#fca5a5; }
button:disabled { opacity:0.5; cursor:not-allowed; }
table { width:100%; border-collapse:collapse; font-size:14px; }
th, td { border:1px solid #e5e7eb; padding:8px; text-align:left; }
th { background:#f9fafb; }
.empty { text-align:center; color:#888; }
.actions button { margin-right:4px; padding:4px 8px; font-size:12px; }
.pager { margin-top:12px; display:flex; align-items:center; gap:12px; }
.msg { padding:10px; margin-bottom:12px; border-radius:4px; }
.msg.ok { background:#ecfdf5; color:#065f46; }
.dialog-msg { padding:10px; margin-bottom:12px; border-radius:4px; }
.msg-ok { background:#ecfdf5; color:#065f46; }
.msg-err { background:#fef2f2; color:#991b1b; }
.msg.err { background:#fef2f2; color:#991b1b; }
.overlay { position:fixed; inset:0; background:rgba(0,0,0,0.4); display:flex; align-items:center; justify-content:center; z-index:100; }
.dialog { background:#fff; padding:20px; border-radius:8px; width:520px; max-height:90vh; overflow:auto; }
.form-grid { display:grid; grid-template-columns:1fr 1fr; gap:12px; margin:16px 0; }
.form-grid label { display:flex; flex-direction:column; font-size:13px; gap:4px; }
</style>
