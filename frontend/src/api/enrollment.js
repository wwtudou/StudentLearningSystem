import api from './index'

export const fetchEnrollments = (params) => api.get('/enrollments', { params })
export const enroll = (data) => api.post('/enrollments', data)
export const drop = (studentNo, offeringNo) => api.delete('/enrollments', { params: { studentNo, offeringNo } })
