import api from './index'

export const fetchTeachers = (params) => api.get('/teachers', { params })
export const createTeacher = (data) => api.post('/teachers', data)
export const updateTeacher = (no, data) => api.put(`/teachers/${no}`, data)
export const fetchTeacherOptions = (collegeCode) => api.get('/teachers/options', { params: { collegeCode } })
