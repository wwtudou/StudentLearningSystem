import api from './index'

export const fetchMajorRank = (semesterCode) => api.get('/db-tech/rank', { params: { semesterCode } })
export const fetchCumulativeCredit = () => api.get('/db-tech/cumulative-credit')
export const fetchGradeChangeLogs = (studentNo) => api.get('/db-tech/grade-change-logs', { params: { studentNo } })
export const explainAuditPartition = (year) => api.get('/db-tech/explain/audit-partition', { params: { year } })
export const explainGradeIndex = (offeringNo) => api.get('/db-tech/explain/grade-index', { params: { offeringNo } })
export const explainEnrollmentIndex = (offeringNo) => api.get('/db-tech/explain/enrollment-index', { params: { offeringNo } })
