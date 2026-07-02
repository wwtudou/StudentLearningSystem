import api from './index'

export const fetchCourses = (params) => api.get('/courses', { params })
export const createCourse = (data) => api.post('/courses', data)
export const updateCourse = (code, data) => api.put(`/courses/${code}`, data)
export const fetchCourseOptions = () => api.get('/courses/options')
export const fetchSemesters = () => api.get('/semesters')
export const fetchOfferings = (params) => api.get('/offerings', { params })
export const createOffering = (data) => api.post('/offerings', data)
export const updateOffering = (no, data) => api.put(`/offerings/${no}`, data)
