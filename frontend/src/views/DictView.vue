<!-- 字典维护 -->
<script setup>
import { ref, onMounted } from 'vue'
import { fetchDictTypes, createDictType, fetchDictItems, createDictItem, updateDictItem, deleteDictItem } from '../api/dict'

const types = ref([])
const selected = ref('')
const items = ref([])
const typeForm = ref({ typeCode: '', typeName: '' })
const itemForm = ref({ itemCode: '', label: '' })
const msg = ref('')

async function loadTypes() {
  const { data } = await fetchDictTypes()
  if (data.code === 0) types.value = data.data
}

async function loadItems() {
  if (!selected.value) return
  const { data } = await fetchDictItems(selected.value)
  if (data.code === 0) items.value = data.data
}

function show(t) { msg.value = t; setTimeout(() => msg.value = '', 3000) }

async function addType() {
  const { data } = await createDictType(typeForm.value)
  if (data.code === 0) { show('类型已添加'); typeForm.value = { typeCode: '', typeName: '' }; loadTypes() }
  else show(data.message)
}

async function addItem() {
  const { data } = await createDictItem(selected.value, itemForm.value)
  if (data.code === 0) { show('字典项已添加'); itemForm.value = { itemCode: '', label: '' }; loadItems() }
  else show(data.message)
}

async function toggleItem(row) {
  const status = row.status === '启用' ? '停用' : '启用'
  const { data } = await updateDictItem(selected.value, row.itemCode, { label: row.label, status })
  if (data.code === 0) loadItems()
  else show(data.message)
}

async function removeItem(row) {
  if (!confirm('确定删除？')) return
  const { data } = await deleteDictItem(selected.value, row.itemCode)
  if (data.code === 0) loadItems()
  else show(data.message)
}

onMounted(loadTypes)
</script>

<template>
  <div>
    <p v-if="msg" class="msg">{{ msg }}</p>
    <section class="card">
      <h2>新增字典类型</h2>
      <div class="row">
        <input v-model="typeForm.typeCode" placeholder="类型编码" />
        <input v-model="typeForm.typeName" placeholder="类型名称" />
        <button @click="addType">新增</button>
      </div>
    </section>
    <section class="card">
      <h2>字典项维护</h2>
      <div class="row">
        <select v-model="selected" @change="loadItems">
          <option value="">选择字典类型</option>
          <option v-for="t in types" :key="t.typeCode" :value="t.typeCode">{{ t.typeName }}</option>
        </select>
        <input v-model="itemForm.itemCode" placeholder="项编码" />
        <input v-model="itemForm.label" placeholder="显示名称" />
        <button :disabled="!selected" @click="addItem">新增项</button>
      </div>
      <table v-if="selected">
        <thead><tr><th>编码</th><th>名称</th><th>状态</th><th>操作</th></tr></thead>
        <tbody>
          <tr v-for="row in items" :key="row.itemCode">
            <td>{{ row.itemCode }}</td><td>{{ row.label }}</td><td>{{ row.status }}</td>
            <td>
              <button @click="toggleItem(row)">{{ row.status === '启用' ? '停用' : '启用' }}</button>
              <button @click="removeItem(row)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
    </section>
  </div>
</template>

<style scoped>@import '../styles/common.css';</style>
