// 学生管理相关 API 封装
import api from './index'

/** 分页查询学生 */
export function fetchStudents(params) {
  return api.get('/students', { params })
}

/** 查询单个学生 */
export function fetchStudent(studentNo) {
  return api.get(`/students/${studentNo}`)
}

/** 新增学生 */
export function createStudent(data) {
  return api.post('/students', data)
}

/** 修改学生 */
export function updateStudent(studentNo, data) {
  return api.put(`/students/${studentNo}`, data)
}

/** 删除学生（逻辑删除） */
export function deleteStudent(studentNo) {
  return api.delete(`/students/${studentNo}`)
}

/** 学院下拉选项 */
export function fetchColleges() {
  return api.get('/students/options/colleges')
}

/** 专业下拉选项 */
export function fetchMajors(collegeCode) {
  return api.get('/students/options/majors', { params: { collegeCode } })
}
