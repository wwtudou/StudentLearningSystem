<!-- 院系专业管理 -->
<script setup>
import { ref, onMounted } from 'vue'
import { fetchOrgTree, createCollege, updateCollege, createMajor, updateMajor } from '../api/org'
import { useAuth } from '../composables/useAuth'

const { writeAllowed } = useAuth()
const canEditOrg = writeAllowed('org')
const tree = ref([])
const msg = ref('')
const collegeForm = ref({ collegeCode: '', collegeName: '' })
const majorForm = ref({ majorCode: '', majorName: '', collegeCode: '' })
const editCollege = ref(null)
const editMajor = ref(null)

async function load() {
  const { data } = await fetchOrgTree()
  if (data.code === 0) tree.value = data.data
}

function show(text) { msg.value = text; setTimeout(() => msg.value = '', 3000) }

async function addCollege() {
  const { data } = await createCollege(collegeForm.value)
  if (data.code === 0) { show('学院新增成功'); collegeForm.value = { collegeCode: '', collegeName: '' }; load() }
  else show(data.message)
}

async function saveCollege() {
  try {
    const payload = { collegeName: editCollege.value.collegeName, status: editCollege.value.status }
    const { data } = await updateCollege(editCollege.value.collegeCode, payload)
    if (data.code === 0) { show('学院已更新'); editCollege.value = null; load() }
    else show(data.message)
  } catch (e) {
    show('操作失败')
  }
}

async function addMajor() {
  const { data } = await createMajor(majorForm.value)
  if (data.code === 0) { show('专业新增成功'); majorForm.value = { majorCode: '', majorName: '', collegeCode: '' }; load() }
  else show(data.message)
}

async function saveMajor() {
  const { data } = await updateMajor(editMajor.value.majorCode, editMajor.value)
  if (data.code === 0) { show('专业已更新'); editMajor.value = null; load() }
  else show(data.message)
}

onMounted(load)
</script>

<template>
  <div>
    <p v-if="msg" class="msg">{{ msg }}</p>
    <section v-if="canEditOrg" class="card">
      <h2>新增学院</h2>
      <div class="row">
        <input v-model="collegeForm.collegeCode" placeholder="学院编号" />
        <input v-model="collegeForm.collegeName" placeholder="学院名称" />
        <button @click="addCollege">新增</button>
      </div>
    </section>
    <section v-if="canEditOrg" class="card">
      <h2>新增专业</h2>
      <div class="row">
        <input v-model="majorForm.majorCode" placeholder="专业编号" />
        <input v-model="majorForm.majorName" placeholder="专业名称" />
        <select v-model="majorForm.collegeCode"><option value="">选择学院</option>
          <option v-for="c in tree" :key="c.collegeCode" :value="c.collegeCode">{{ c.collegeName }}</option>
        </select>
        <button @click="addMajor">新增</button>
      </div>
    </section>
    <section class="card">
      <h2>组织树</h2>
      <div v-for="c in tree" :key="c.collegeCode" class="block">
        <div class="row">
          <strong>{{ c.collegeCode }} - {{ c.collegeName }}</strong>
          <span class="tag">{{ c.status }}</span>
          <button v-if="canEditOrg" @click="editCollege = { ...c }">编辑</button>
        </div>
        <ul>
          <li v-for="m in c.majors" :key="m.majorCode">
            {{ m.majorCode }} - {{ m.majorName }} ({{ m.status }})
            <button v-if="canEditOrg" @click="editMajor = { ...m }">编辑</button>
          </li>
        </ul>
      </div>
    </section>
    <div v-if="editCollege" class="overlay" @click.self="editCollege=null">
      <div class="dialog">
        <h3>编辑学院</h3>
        <input v-model="editCollege.collegeName" />
        <select v-model="editCollege.status"><option>启用</option><option>停用</option></select>
        <button @click="saveCollege">保存</button>
      </div>
    </div>
    <div v-if="editMajor" class="overlay" @click.self="editMajor=null">
      <div class="dialog">
        <h3>编辑专业</h3>
        <input v-model="editMajor.majorName" />
        <select v-model="editMajor.collegeCode">
          <option v-for="c in tree" :key="c.collegeCode" :value="c.collegeCode">{{ c.collegeName }}</option>
        </select>
        <select v-model="editMajor.status"><option>启用</option><option>停用</option></select>
        <button @click="saveMajor">保存</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
@import '../styles/common.css';
.block { margin-bottom:12px; border-bottom:1px solid #eee; padding-bottom:8px; }
.tag { font-size:12px; color:#666; margin-left:8px; }
.block .row button, ul li button { padding:2px 8px; font-size:12px; margin-left:4px; min-width:unset; border-radius:4px; }
.block .row { gap:4px; flex-wrap:wrap; }
ul { padding-left:16px; margin:4px 0; list-style:none; }
ul li { padding:3px 0; font-size:14px; display:flex; align-items:center; flex-wrap:wrap; gap:4px; }
</style>
