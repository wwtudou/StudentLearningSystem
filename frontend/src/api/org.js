import api from './index'

export const fetchOrgTree = () => api.get('/org/tree')
export const fetchColleges = () => api.get('/org/colleges')
export const createCollege = (data) => api.post('/org/colleges', data)
export const updateCollege = (code, data) => api.put(`/org/colleges/${code}`, data)
export const fetchMajors = (collegeCode) => api.get('/org/majors', { params: { collegeCode } })
export const createMajor = (data) => api.post('/org/majors', data)
export const updateMajor = (code, data) => api.put(`/org/majors/${code}`, data)
export const fetchCollegeOptions = () => api.get('/org/options/colleges')
