import api from './index'

export const fetchRewards = (params) => api.get('/rewards', { params })
export const createReward = (data) => api.post('/rewards', data)
export const updateReward = (no, data) => api.put(`/rewards/${no}`, data)
export const archiveReward = (no) => api.post(`/rewards/${no}/archive`)
