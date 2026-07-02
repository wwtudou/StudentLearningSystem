import api from './index'

export const fetchGrades = (params) => api.get('/grades', { params })
export const saveGrade = (data) => api.post('/grades', data)
export const submitGrades = (offeringNo) => api.post('/grades/submit', { offeringNo })
export const arrangeMakeup = (data) => api.post('/grades/makeup', data)
export const arrangeRetake = (data) => api.post('/grades/retake', data)
