<!-- 奖惩管理 -->
<script setup>
import { ref, onMounted } from 'vue'
import { fetchRewards, createReward, updateReward, archiveReward } from '../api/reward'

const list = ref([])
const total = ref(0)
const page = ref(1)
const query = ref({ studentNo: '', type: '', level: '' })
const form = ref({ studentNo: '', type: '奖励', level: '院级', reason: '', occurDate: '' })
const edit = ref(null)
const msg = ref('')
const types = ['奖励', '惩罚']
const levels = ['校级', '院级', '班级']

async function load() {
  const { data } = await fetchRewards({ ...query.value, page: page.value, pageSize: 10 })
  if (data.code === 0) { list.value = data.data.list; total.value = data.data.total }
}

function show(t) { msg.value = t; setTimeout(() => msg.value = '', 3000) }

async function submit() {
  const payload = edit.value || form.value
  const fn = edit.value ? updateReward(edit.value.recordNo, payload) : createReward(payload)
  const { data } = await fn
  if (data.code === 0) { show('保存成功'); edit.value = null; form.value = { studentNo: '', type: '奖励', level: '院级', reason: '', occurDate: '' }; load() }
  else show(data.message)
}

async function doArchive(row) {
  const { data } = await archiveReward(row.recordNo)
  if (data.code === 0) load()
  else show(data.message)
}

onMounted(load)
</script>

<template>
  <div>
    <p v-if="msg" class="msg">{{ msg }}</p>
    <section class="card">
      <h2>查询</h2>
      <div class="row">
        <input v-model="query.studentNo" placeholder="学号" />
        <select v-model="query.type"><option value="">全部类型</option><option v-for="t in types" :key="t">{{ t }}</option></select>
        <select v-model="query.level"><option value="">全部级别</option><option v-for="l in levels" :key="l">{{ l }}</option></select>
        <button @click="page=1; load()">查询</button>
      </div>
    </section>
    <section class="card">
      <h2>{{ edit ? '编辑奖惩' : '登记奖惩' }}</h2>
      <div class="row">
        <input v-model="(edit||form).studentNo" :disabled="!!edit" placeholder="学号" />
        <select v-model="(edit||form).type"><option v-for="t in types" :key="t">{{ t }}</option></select>
        <select v-model="(edit||form).level"><option v-for="l in levels" :key="l">{{ l }}</option></select>
        <input v-model="(edit||form).reason" placeholder="原因" />
        <input v-model="(edit||form).occurDate" type="date" />
        <button @click="submit">保存</button>
        <button v-if="edit" @click="edit=null">取消</button>
      </div>
    </section>
    <section class="card">
      <table>
        <thead><tr><th>编号</th><th>学号</th><th>姓名</th><th>类型</th><th>级别</th><th>日期</th><th>归档</th><th>操作</th></tr></thead>
        <tbody>
          <tr v-for="row in list" :key="row.recordNo">
            <td>{{ row.recordNo }}</td><td>{{ row.studentNo }}</td><td>{{ row.studentName }}</td>
            <td>{{ row.type }}</td><td>{{ row.level }}</td><td>{{ row.occurDate }}</td>
            <td>{{ row.archived ? '是' : '否' }}</td>
            <td>
              <button v-if="!row.archived" @click="edit={...row}">编辑</button>
              <button v-if="!row.archived" @click="doArchive(row)">归档</button>
            </td>
          </tr>
        </tbody>
      </table>
      <div class="pager"><button :disabled="page<=1" @click="page--; load()">上一页</button><span>共 {{ total }} 条</span>
        <button :disabled="page*10>=total" @click="page++; load()">下一页</button></div>
    </section>
  </div>
</template>

<style scoped>
@import '../styles/common.css';
table td button { padding:2px 10px; font-size:12px; min-width:unset; margin-right:4px; border-radius:4px; }
</style>
