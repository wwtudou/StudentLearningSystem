import api from './index'

export const fetchStudentStats = (groupBy) => api.get('/reports/students', { params: { groupBy } })
export const fetchEnrollmentStats = () => api.get('/reports/enrollments')
export const fetchGradeStats = (offeringNo) => api.get('/reports/grades', { params: { offeringNo } })
export const fetchRewardStats = () => api.get('/reports/rewards')
