import api from './index'

export const fetchGrades = (params) => api.get('/grades', { params })
export const fetchMyOfferings = () => api.get('/grades/my-offerings')
export const fetchGradeRoster = (offeringNo) => api.get('/grades/roster', { params: { offeringNo } })
export const saveGrade = (data) => api.post('/grades', data)
export const submitGrades = (offeringNo) => api.post('/grades/submit', { offeringNo })
export const arrangeMakeup = (data) => api.post('/grades/makeup', data)
export const unlockStudentGrade = (studentNo, offeringNo) => api.post('/grades/unlock-student', { studentNo, offeringNo })
export const unlockGrades = (offeringNo) => api.post('/grades/unlock', { offeringNo })
export const arrangeRetake = (data) => api.post('/grades/retake', data)
